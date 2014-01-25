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

package ch.epfl.lis.wingj.structure.drosophila.wingpouch;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.tools.Skeleton;
import ch.epfl.lis.wingj.utilities.Filters;
import ij.ImagePlus;
import ij.process.AutoThresholder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/** 
 * Pre-processing of the Wg-Ptc-AB maximum intensity projection.
 * 
 * - Blur image
 * - Threshold image
 * 
 * @version June 2, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class PreProcessing extends StructureDetectionModule {
	
	/** Auto threshold method. */
	public static AutoThresholder.Method autoThresholdMethod_ = AutoThresholder.Method.Minimum;
	/** Threshold increment automatically added to the threshold if test() returns false. */
	private int ppThresholdIncrement_ = 5;
	
	// ============================================================================
	// PUBLIC METHODS
	
   	/** Default constructor. */
   	public PreProcessing() {
   		
   		super();
   	}
   	
   	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public PreProcessing(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Pre-processing structure image";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public PreProcessing(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Pre-processing structure image";
	}
	
	// ----------------------------------------------------------------------------
	
    /**
     * Creates the maximal intensity projection from the stack of confocal images
     * featuring the structure of the wing pouch. Then, a pre-processing image is
     * computed to ease the detection using the snake.
     */
	@Override
	public void run() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		ImagePlusManager manager = ImagePlusManager.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		
		if (detector.structureProjection_ == null)
			throw new Exception("INFO: Structure projection required.");
		if (settings.getPpThreshold() >= 255)
			throw new Exception("WARNING: Pre-processing threshold value is too high.\n" +
					"\n" +
					"Modify the pre-processing bluring value and\n" +
					"re-initialize the structure detection.");
		
		// apply the mask (if any exists)
		WJSettings.log("Setting mask (if any)");
		WJImagesMask.applyMask(detector.structureProjection_);
		
		if (detector.isInteractive() && !hidden_)
			detector.getStructureProjection().show();
		
//   		double blur = settings.getPpBlur();
		double blur = PreProcessing.getPpBlurSigma();
   		int thld = settings.getPpThreshold();
   		
//   		WJSettings.log("==> Threshold: " + thld);
   		
   		// blur and thld image
   		detector.ppImage_ = blurAndThreshold(detector.structureProjection_, blur, thld);
   		manager.add(detector.ppImage_.getTitle(), detector.ppImage_, detector.isInteractive() && !hidden_); // do not show the image in hidden mode
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the pre-processed image (mask must have been already applied). */
	public static ImagePlus blurAndThreshold(ImagePlus projection, double blur, int thld) throws Exception {
		
   		// get image processor to work with
   		ImageProcessor processor = projection.getProcessor().duplicate();
   		
   		// Gaussian blurring
   		Filters.applyGaussianFilter(processor, blur);
   		
   		// thresholding image to get a binary image
   		Filters.binaryThresholdedImageFilter((FloatProcessor) processor.convertToFloat(), thld);
   		
   		ImagePlus thresholdedImage = new ImagePlus("detection_structure_preprocessed", processor);
   		return thresholdedImage;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns optimal pre-processing threshold depending on the given image and blur value. */
	public static int computeAutoPpThreshold(ImagePlus image, double blur) throws Exception {
		
   		// Gaussian blurring
		ImageProcessor ip = image.getProcessor().duplicate();
   		Filters.applyGaussianFilter(ip, blur);
   		ip.setAutoThreshold(autoThresholdMethod_, true, ImageProcessor.NO_LUT_UPDATE);
   		
   		int threshold = (int) Math.round(((double)ip.getMaxThreshold() + (double)ip.getMinThreshold()) / 2.);
   		ip = null;
   		
   		return threshold;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the visibility of all images generated during the detection process, if they exist. */
	@Override
	public void setImagesVisible(boolean visible) {
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		if (visible)	
			manager.show("detection_structure_preprocessed");
		else
			manager.hide("detection_structure_preprocessed");
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes all images generated during the detection process, if they exist. */
	@Override
	public void removeImages() {
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.remove("detection_structure_preprocessed");
	}
	
	// ----------------------------------------------------------------------------
	
	/** Tests here that the future skeleton will have at least N (arbitrary) white pixels. */
	@Override
	public boolean test() {
		
		WJSettings settings = WJSettings.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		int minSkeletonSize = settings.getMinSkeletonSizeInPixels();
		
		try {
			Skeleton skeleton = new Skeleton();
			// can takes time, I guess depending on the content of ppImage_
			// TODO: Error in IJ47e: cannot convert float to byte
			skeleton.prune((FloatProcessor)detector.ppImage_.getProcessor(), true);
			int skeletonSize = skeleton.countNonZeroPixels();
			WJSettings.log("Skeleton size: " + skeletonSize + " px");
			
			if (skeletonSize < minSkeletonSize)
				throw new Exception("Skeleton size smaller than required (" + skeletonSize + " < " + minSkeletonSize + ")");
			
		} catch (Exception e) {
			WJSettings.log("PreProcessing: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	// ----------------------------------------------------------------------------
	
	/** If test() returned false, it means that the skeleton is too small, so lets increase the threshold. */
	@Override
	public void update() {
		
		WJSettings settings = WJSettings.getInstance();
	
		int threshold = settings.getPpThreshold() + ppThresholdIncrement_;
		WJSettings.log("Adding " + ppThresholdIncrement_ + " to the pre-processing threshold (threshold is now " + threshold + ").");
		settings.setPpThreshold(threshold);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the sigma value of the 2D Gaussian filter used to smooth the image. */
	public static double getPpBlurSigma() {
		
		return WJSettings.getInstance().getExpectedBoundariesThicknessInPixels()/2.*Math.sqrt(2.*Math.log(2));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the expetect boundaries thickness in pixels from the given pre-processing sigma. */
//	public static void setPpBlurSigma() {
//		
//		WJSettings.getInstance().setExpectedBoundariesThicknessInPixels(value)
//	}
}