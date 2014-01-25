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

package ch.epfl.lis.wingj.structure;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URI;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.utilities.ImageUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.macro.Interpreter;
import ij.plugin.Duplicator;
import ij.process.FloatPolygon;

/** 
 * Abstract class for implementing overlay to paint on top of the structure viewer.
 * 
 * @see ch.epfl.lis.wingj.WJStructureViewer
 * 
 * @version October 25, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
abstract public class Overlay extends ImageCanvas {
	
	/** Default serial. */
	private static final long serialVersionUID = 1L;
	
	/** Stackwindow containing the present overlay. */
	protected ImageWindow stackWindow_ = null;
	/** Size of the image. */
	protected Rectangle rectOffset_ = null;

	/** Magnification. */
	protected double magnification_ = 1;

	/** Structure object. */
	protected Structure structure_ = null;
	
	/** Shows the structure. */
	protected boolean showStructure_ = true;
	/** Shows additional information such as compartment labels. */
	protected boolean showInformation_ = true;
	
	// ============================================================================
	// ABSTRACT METHODS

	/** Paints the structure on the overlay. */
	abstract protected void paintStructure(Graphics g);
	/** Paints information on the overlay, for instance compartments name. */
	abstract protected void paintInformation(Graphics g);
	
	// ============================================================================
	// PROTECTED METHODS

	/** Magnify a Polygon */
	protected Polygon magnifyPolygon(FloatPolygon polygon, Rectangle rectOffset, double magnification) {

		Polygon p = new Polygon();
		for (int i = 0; i < polygon.npoints; i++) {
			p.addPoint((int) (magnification*(polygon.xpoints[i]-rectOffset.x)),
					(int) (magnification*(polygon.ypoints[i]-rectOffset.y)));
		}
		return p;
	}
	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Constructor.
	 * The ImageWindow is not shown anymore thanks to using the batchMode trick. 
	 */
	public Overlay(Structure structure, ImagePlus imp, boolean useImageWindow) {
		
		super(imp);

		try {
			setImage(imp, useImageWindow);
//			WingJ.setAppIcon(stackWindow_);
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * The ImageWindow is not shown anymore thanks to using the batchMode trick. 
	 */
	public Overlay(Structure structure, ImagePlus imp) {
		
		super(imp);

		try {
			setImage(imp, false);
//			WingJ.setAppIcon(stackWindow_);
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Sets the background image stack. If useImageWindow is true, use an ImageWindow (single image)
	 * rather than the default StackWindow which can display image stack.
	 */
	public void setImage(ImagePlus image, boolean useImageWindow) throws Exception {

		if (image == null)
			throw new Exception("ERROR: image is null.");

		if (image.getProcessor() == null)
			throw new Exception("WARNING: The selected image doesn't exist anymore. Did you close it?");

		// creating this clone doesn't show it
		image.killRoi();
		ImagePlus clone = new Duplicator().run(image);
		image.restoreRoi();
		
		// in case the image has been normalized
		try {
			if (ImageUtils.getMaxPixelValue(clone) <= 1.5)
				ImageUtils.scalePixelValues(clone, 255.);
		} catch (Exception e) {}
		clone.setDisplayRange(0., 255.);

		if (stackWindow_ == null) {

			boolean batchMode = Interpreter.isBatchMode(); // is ok with ImageWindow but not with StackWindow (required for displaying stacks)
			Interpreter.batchMode = true;
			
			if (useImageWindow)
				stackWindow_ = new ImageWindow(clone, this);
			else // default
				stackWindow_ = new StackWindow(clone, this);

			Interpreter.batchMode = batchMode;
			// initially setImage() behave differently for closing...
			// I think there is also a difference concerning the magnification (updateImage is preferably)
			stackWindow_.updateImage(stackWindow_.getImagePlus());
		}
		else {
			stackWindow_.getImagePlus().setProcessor(clone.getProcessor());
			stackWindow_.getImagePlus().setStack(clone.getImageStack());
			stackWindow_.updateImage(stackWindow_.getImagePlus());
		}
		stackWindow_.setTitle(clone.getTitle());
	}
	
	// ----------------------------------------------------------------------------

	/** Sets the background image stack. */
	public void setImage(ImagePlus image) throws Exception {

		setImage(image, false);
	}
	
	// ----------------------------------------------------------------------------

	/** Called when the structure viewer is closed. */
	public void dispose() {

		if (stackWindow_ != null && !stackWindow_.isClosed()) {
			stackWindow_.close();
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Closes the StackWindow. */
	public void close() {

		if (stackWindow_ == null)
			return;
		try {
			stackWindow_.close();
			stackWindow_ = null;
		} catch (Exception e) {
			// here if stackWindow_ closed manually
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Overrides repaint(). */
	@Override
	public void repaint() {
		super.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Paints the overlay. */
	@Override
	public void paint(Graphics g) {

		super.paint(g);
		
		magnification_ = getMagnification();
		rectOffset_ = getSrcRect();
		
		try {
			if (showStructure_)
				paintStructure(g);
			if (showInformation_)
				paintInformation(g);
		} catch (Exception e) {
			// Exception is sometimes thrown by Java code
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Returns a BufferedImage from the content of the StackWindow. */
	public BufferedImage overlayToBufferedImage() {

		ImagePlus image = stackWindow_.getImagePlus();
		int width = image.getWidth();
		int  height = image.getHeight();
		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D)bi.getGraphics();

		double tmpMagnification = getMagnification();
		setMagnification(1);
		paint(g);
		setMagnification(tmpMagnification);

		return bi;
	}
	
	// ----------------------------------------------------------------------------

	/**  Saves the detected structure and overlay to image file (extension defines the image format). */
	public void save(URI uri) throws Exception {

		ImagePlus img = new ImagePlus("structure_save", Toolkit.getDefaultToolkit().createImage(overlayToBufferedImage().getSource()));
		IJ.save(img, uri.getPath());
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	public ImageWindow getImageWindow() { return stackWindow_; }
	
	public void setStructure(Structure structure) { structure_ = structure; }

	public void isInformationVisible(boolean b) { showInformation_ = b; }
	public boolean isInformationVisible() { return showInformation_; }

	public void isStructureVisible(boolean b) { showStructure_ = b; }
	public boolean isStructureVisible() { return showStructure_; }
}
