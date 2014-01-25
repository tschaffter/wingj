/*
Copyright (c) 2010-2013 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under a Creative Commons Attribution
-NonCommercial 3.0 Unported License. Please cite the papers listed on 
http://lis.epfl.ch/wingj when using WingJ in your publication.

For commercial use, please contact Thomas Schaffter 
(thomas.schaff...@gmail.com).

A brief description of the license is available at 
http://creativecommons.org/licenses/by-nc/3.0/ and the full license at 
http://creativecommons.org/licenses/by-nc/3.0/legalcode.

The above copyright notice and this permission notice shall be included 
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import ch.epfl.lis.wingj.structure.Compartment;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * Shows an ImagePlus and provides the tools to draw a mask.
 * 
 * @version June 16, 2011
 * 
 * @author Thomas Schaffter (thomas.schff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJImagesMask implements MouseListener {
	
	/** Area of interest (AOI). */
	public Roi aoiRoi_ = null;
	/** Only used for visualization purpose. */
	private ImagePlus image_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WJImagesMask() {
		
		try {
			initializeRoi();
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows the image and provide the tools to draw a mask. */
	public void run(ImagePlus image) throws Exception {

		if (image == null)
			throw new Exception("INFO: Structure image or image stack required.");
		
		image_ = image;
		// show the image
		image_.hide();
		image_.show();
		image_.killRoi();
		
		if (aoiRoi_ != null) {
			double imagePerimeter = 2 * image_.getWidth() + 2 * image_.getHeight();
			double aoiPerimeter = aoiRoi_.getLength();
			WJSettings.log("aoiPerimeter: " + aoiPerimeter);
			// if the AOI perimeter fits the perimeter of the image (including some error as I observed),
			// don't display the ROI. Later even if the ROI is null it will be considered as taking into
			// account the entire image canvas. If the AOI looks like a custom AOI, display it.
			if (aoiPerimeter >= 0.99*imagePerimeter && aoiPerimeter <= 1.01*imagePerimeter)
				image_.killRoi();
			else
				image_.setRoi(aoiRoi_);
		}
		
		// set the freehand tool
		IJ.setTool("freehand");
		// add mouse listener		
		image_.getWindow().getCanvas().addMouseListener(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the selected mask is valid (area is non-null). */
	public boolean isMaskValid() {
		
		Roi mask = image_.getRoi();
		if (mask != null) {
			if (mask.isArea()) {
				// WJSettings.log("New mask accepted");
				aoiRoi_ = mask;
				return true;
			}
			else {
				WJSettings.log("AOI area must be non-null");
				return false;
			}
		}
		else {
			try {
				initializeRoi();
			} catch (Exception e) {}
			return false;
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Initializes the mask with no ROI at all. */
	public void initializeRoi() throws Exception {
		
		if (image_ != null && image_.getProcessor() != null) {
			image_.setRoi(0, 0, image_.getWidth(), image_.getHeight());
			image_.killRoi();
		}
		Roi.previousRoi = null;
		aoiRoi_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) { isMaskValid(); }
	
	// ----------------------------------------------------------------------------
	
	/** Caches image but where is the given compartment. */
	public static void maskImageButCompartment(ImagePlus image, Compartment c) throws Exception {
		
		image.getProcessor().fillOutside(c.toRoi());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves a binary mask of the given compartment. */
	public static void saveBinaryMask(URI uri, ImagePlus image, Compartment c) throws Exception {
		
		if (image == null || image.getProcessor() == null)
			throw new Exception("ERROR: image is null.");
		if (c == null)
			throw new Exception("ERROR: compartment c is null.");
		
		ImagePlus mask = new ImagePlus(image.getTitle() + "_mask", image.getProcessor().duplicate());
		ImageProcessor ip = mask.getProcessor();
		ip.setColor(Color.BLACK);
		ip.fillOutside(c.toRoi());
		ip.setColor(Color.WHITE);
		ip.fill(c.toRoi());
		
		ImagePlus img2 = new ImagePlus("", mask.getProcessor().convertToByte(true));
		IJ.save(img2, uri.getPath());
	}
	
	// ----------------------------------------------------------------------------

    /** Creates a circular mask (disc is made of elements 1, outside 0). */
	public static ImagePlus createCircularBinaryMask(int widthInPx) {
		
		int iCenter = (int) Math.round((widthInPx-1) / 2.0);
		int jCenter = (int) Math.round((widthInPx-1) / 2.0);
		double[] mask = new double[widthInPx * widthInPx];
		
		for (int i = 0; i < widthInPx; i++) {
			double x = i - iCenter;
			for (int j = 0; j < widthInPx; j++) {
				double y = j - jCenter;
				double d = Math.sqrt(x*x + y*y);
				if (d <= iCenter) // the radius has the same value than iCenter (widthInPx/2.0)
					mask[i + j*widthInPx] = 255; // was 1 before disabling scaling
			}
		}
		ImagePlus img = new ImagePlus(new String("circular_mask_" + new Integer(widthInPx).toString()),
									  new FloatProcessor(widthInPx, widthInPx, mask));
		
		// convert to 8-bit
		ImageConverter converter = new ImageConverter(img);
		converter.convertToGray8();
		
		return img;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets all pixel outside of the user-defined roi to black. */
	public static void applyMask(ImagePlus imgToMask) throws Exception {
		
		if (imgToMask == null || imgToMask.getProcessor() == null)
			throw new Exception("ERROR: imgToMask is null.");
		
		// make sure that the background color is black
		Color bdColorBkp = Toolbar.getBackgroundColor();
		Toolbar.setBackgroundColor(Color.WHITE);
		
		// set to black all the pixel outside of the roi
		if (WJImages.imagesMask_ != null && WJImages.imagesMask_.aoiRoi_ != null && WJImages.imagesMask_.aoiRoi_.isArea()) {	
			imgToMask.getProcessor().invert();
			imgToMask.setRoi(WJImages.imagesMask_.aoiRoi_);
			IJ.run(imgToMask, "Clear Outside", null);
			imgToMask.killRoi();
			imgToMask.getProcessor().invert();
		}
		
		// restore background color
		Toolbar.setBackgroundColor(bdColorBkp);
	}
}
