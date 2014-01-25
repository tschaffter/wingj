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

package ch.epfl.lis.wingj.utilities;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.CanvasResizer;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ch.epfl.lis.wingj.WJSettings;

/**
 * Utility methods for manipulating images.
 * 
 * @version December 8, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ImageUtils {

	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Finds the minimum and maximum pixel values from the given images and applies
	 * them to every images by changing their display range.
	 */
	public static void setRelativeDisplayRange(List<ImagePlus> images) throws Exception {
		
		double minValue = Double.MAX_VALUE;
		double maxValue = Double.MIN_VALUE;
		float[] values = null;
		for (ImagePlus ip : images) {
			values = (float[])ip.getProcessor().getPixels();
			for (int i = 0; i < values.length; i++) {
				if (values[i] < minValue)
					minValue = values[i];
				if (values[i] > maxValue)
					maxValue = values[i];
			}
		}
		
		WJSettings.log("Min pixel value: " + minValue);
		WJSettings.log("Max pixel value: " + maxValue);
		
		for (ImagePlus imp : images)
			imp.setDisplayRange(minValue, maxValue);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Finds the minimum and maximum width and height and resized every image. */
	public static void setRelativeCanvasDimensions(List<ImagePlus> images) throws Exception {
		
		if (images == null || images.isEmpty())
			return;
		
		// identifies the largest image width and height and use these dimensions
		// to resize the canvas of each image
		int wmax = 0;
		int hmax = 0;
		for (int i = 0; i < images.size(); i++) {
			if (images.get(i).getWidth() > wmax)
				wmax = images.get(i).getWidth();
			if (images.get(i).getHeight() > hmax)
				hmax = images.get(i).getHeight();
		}
		
		WJSettings.log("Max width: " + wmax);
		WJSettings.log("Max height: " + hmax);
		
		CanvasResizer resizer = new CanvasResizer();
		IJ.setBackgroundColor(0, 0, 0);
		int xoff = 0;
		int yoff = 0;
		for (int i = 0; i < images.size(); i++) {
			xoff = (wmax - images.get(i).getWidth())/2;
			yoff = (hmax - images.get(i).getHeight())/2;
			images.set(i, new ImagePlus("image_" + i + "_canvasResized", resizer.expandImage(images.get(i).getProcessor(), wmax, hmax, xoff, yoff)));
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Finds the minimum and maximum width and height and resized every image.
	 * Then resize every image to match the target dimension. 
	 */
	public static void setRelativeCanvasDimensions(List<ImagePlus> images, Dimension targetDims) throws Exception {
		
		// gives the imges the same canvas size based on the largest one
		setRelativeCanvasDimensions(images);
		
		// resize the canvas
		if (images == null || images.isEmpty() || images.get(0) == null)
			return;
		
		Dimension originalDims = new Dimension(images.get(0).getWidth(), images.get(0).getHeight());
		Dimension newDims = ImageUtils.getNewDimensionsWithAspectRatioConserved(originalDims, targetDims);
		if (newDims.getWidth() <= originalDims.getWidth()) // we don't shrink the canvas
			return;
		
		WJSettings.log("New width: " + newDims.width);
		WJSettings.log("New height: " + newDims.height);
		
		CanvasResizer resizer = new CanvasResizer();
		IJ.setBackgroundColor(0, 0, 0);
		int xoff = 0;
		int yoff = 0;
		for (int i = 0; i < images.size(); i++) {
			xoff = ((int)newDims.getWidth() - images.get(i).getWidth())/2;
			yoff = ((int)newDims.getHeight() - images.get(i).getHeight())/2;
			images.set(i, new ImagePlus("image_" + i + "_canvasResized", resizer.expandImage(images.get(i).getProcessor(), (int)newDims.getWidth(), (int)newDims.getHeight(), xoff, yoff)));
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Uniformizes the pixels values of the given list of images.
	 * Images are exported as FILENAME_SUFFIX (FILENAME is stripped from its extension
	 * and the '_' should be included in SUFFIX as well as the extension).
	 */
	public static void uniformizePixelValues(List<String> imageFilenames, String suffix) throws Exception {
		
		// disables IJ scaling of the pixel values
		boolean scalingBkp = ImageConverter.getDoScaling();
		ImageConverter.setDoScaling(false);
		
		// gets images
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		ImagePlus imp = null;
		for (String filename : imageFilenames) {
			imp = IJ.openImage(filename);
			new ImageConverter(imp).convertToGray32();
			images.add(imp);
		}
		
		// applies the same display range to every image 
		ImageUtils.setRelativeDisplayRange(images);
		
		// save the images with SCALING ENABLED
		ImageConverter.setDoScaling(true);
		String filename = null;
		for (int i = 0; i < imageFilenames.size(); i++) {
			filename = FilenameUtils.removeExtension(imageFilenames.get(i)) + suffix;
			imp = images.get(i);
			new ImageConverter(imp).convertToGray8();
			IJ.save(imp, filename);
		}

		// restore scaling settings
		ImageConverter.setDoScaling(scalingBkp);		
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Uniformizes the canvas dimensions of the given list of images.
	 * Images are exported as FILENAME_SUFFIX (FILENAME is stripped from its extension
	 * and the '_' should be included in SUFFIX as well as the extension).
	 */
	public static void uniformizeCanvasDimensions(List<String> imageFilenames, String suffix) throws Exception {
		
		// gets images
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		ImagePlus imp = null;
		for (String filename : imageFilenames) {
			imp = IJ.openImage(filename);
			new ImageConverter(imp).convertToGray32();
			images.add(imp);
		}
		
		// applies the same display range to every image 
		ImageUtils.setRelativeCanvasDimensions(images);
		
		// save the images with SCALING ENABLED
		ImageConverter.setDoScaling(true);
		String filename = null;
		for (int i = 0; i < imageFilenames.size(); i++) {
			filename = FilenameUtils.removeExtension(imageFilenames.get(i)) + suffix;
			imp = images.get(i);
			new ImageConverter(imp).convertToGray8();
			IJ.save(imp, filename);
		}		
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Uniformizes the canvas dimensions of the given list of images.
	 * Images are exported as FILENAME_SUFFIX (FILENAME is stripped from its extension
	 * and the '_' should be included in SUFFIX as well as the extension).
	 */
	public static void uniformizePixelValuesAndCanvasDimensions(List<String> imageFilenames, String suffix) throws Exception {
		
		// disables IJ scaling of the pixel values
		boolean scalingBkp = ImageConverter.getDoScaling();
		ImageConverter.setDoScaling(false);
		
		// gets images
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		ImagePlus imp = null;
		for (String filename : imageFilenames) {
			imp = IJ.openImage(filename);
			new ImageConverter(imp).convertToGray32();
			images.add(imp);
		}
		
		// applies the same display range to every image 
		ImageUtils.setRelativeDisplayRange(images);
		// applies the same display range to every image 
		ImageUtils.setRelativeCanvasDimensions(images);
		
		// save the images with SCALING ENABLED
		ImageConverter.setDoScaling(true);
		String filename = null;
		for (int i = 0; i < imageFilenames.size(); i++) {
			filename = FilenameUtils.removeExtension(imageFilenames.get(i)) + suffix;
			imp = images.get(i);
			new ImageConverter(imp).convertToGray8();
			IJ.save(imp, filename);
		}

		// restore scaling settings
		ImageConverter.setDoScaling(scalingBkp);		
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the new image dimensions from the original and target dimensions.
	 * The new dimensions conserve the ratio of the original dimensions.
	 */
	public static Dimension getNewDimensionsWithAspectRatioConserved(Dimension original, Dimension target) throws Exception {
		
		if (original == null)
			throw new Exception("ERROR: Original dimensions are null.");
		if (target == null)
			throw new Exception("ERROR: Target dimensions are null.");
		
		// resizes the images if required
		float ratioW = 0.f;
		float ratioH = 0.f;
		float ratio = 0.f;
		int newWidth = 0;
		int newHeight = 0;
		
		// calculates resize ratios for resizing
		if (target.width > 0)
			ratioW = (float)target.width / original.width;
		else
			ratioW = Float.MAX_VALUE;
		
		if (target.height > 0)
			ratioH = (float)target.height / original.height;
		else
			ratioH = Float.MAX_VALUE;

		// smaller ratio will ensure that the image fits in the view
		ratio = ratioW < ratioH ? ratioW : ratioH;
		
		newWidth = (int)(original.width*ratio);
		newHeight = (int)(original.height*ratio);
		
		return new Dimension(newWidth, newHeight);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the maximum pixel values in the image. */
	public static double getMaxPixelValue(ImagePlus img) throws Exception {
		
		if (!(img.getProcessor() instanceof FloatProcessor))
			throw new Exception("ERROR: getMaxPixelValue(ImagePlus) requires a FloatProcessor.");
			
		double maxValue = Double.MIN_VALUE;
		float[] values = null;
		values = (float[])img.getProcessor().getPixels();
		for (int i = 0; i < values.length; i++) {
			if (values[i] > maxValue)
				maxValue = values[i];
		}
		return maxValue;			
	}
	
	// ----------------------------------------------------------------------------
	
	/** Multiplies the pixel values of the given image to 255. */
	public static void scalePixelValues(ImagePlus img, double max) throws Exception {
		
		double factor = max/getMaxPixelValue(img);
		img.getProcessor().multiply(factor);
	}
}
