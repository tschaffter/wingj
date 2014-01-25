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

import ij.ImagePlus;
import ij.process.FloatPolygon;
import ij.process.FloatProcessor;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.analytics.StructureDetectionStats;
import ch.epfl.lis.wingj.filefilters.FilterStructureXml;
import ch.epfl.lis.wingj.structure.tools.KiteSnake;
import ch.epfl.lis.wingj.structure.tools.CompartmentSnake;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.StructureDetector;
import ch.epfl.lis.wingj.structure.geometry.PolygonFactory;
import ch.epfl.lis.wingj.utilities.FileUtils;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;

/** 
 * Identifies the wing pouch contour, compartments, and A/P and D/V boundaries from Wg-Ptc-AB confocal images.
 * 
 * @version March 4, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchStructureDetector extends StructureDetector {

	// INTERNAL VARIABLES SHARED BY THE DIFFERENT STAGES OF THE WING DETECTION
	/** Pre-processed image in order to obtain a much simpler version for using snake with. */
	protected ImagePlus ppImage_ = null;
	/** Dilated skeleton used in combination with the original image to improve convergence. */
	protected FloatProcessor dilatedSkeletonFp_ = null;
	
	/** Snake tool to detect the center of the wing pouch. */
	protected KiteSnake kiteSnake_ = null;
	/** Snake tool to detect each of the four compartments of the wing pouch. */
	protected CompartmentSnake[] shapeSnake_ = null;
	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public WPouchStructureDetector(String name) {
		
		super(name);

		WPouchStructure structure = new WPouchStructure(name);
		structure.setAge("");
		
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchStructureDetector(String name, String systemName) {
		
		super(name);

		WPouchStructure structure = new WPouchStructure(name);
		structure.setSystemName(systemName);
		structure.setAge("");
		
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------

	/** Initialize the detection modules. */
	@Override
	protected void initializeDetectionModules() {

		detections_ = new ArrayList<StructureDetectionModule>();

		// sets detector_.ppImage_
		detections_.add(new PreProcessing(name_ + "_preprocessing", this));
		// sets detector_.wDiscCenter_
		detections_.add(new WDiscCenterDetection(name_ + "_disc_center", this, true)); // HIDDEN
		// sets detector_.dilatedSkeletonFp_, detector_.wPouchCenter_ and detector_.kiteSnake_
		detections_.add(new WPouchCenterDetection(name_ + "_pouch_center", this));
		// sets detector_.wingSnake_, creates detector_.shapeSnake_ and sets the four initial compartments of detector_.wingSnake_
		detections_.add(new WPouchCompartmentsDetection(name_ + "_compartments", this)); // HIDDEN
		// sets the initalWingPouchBoundary to detector_.wingSnake_    
		detections_.add(new WPouchContourDetection(name_ + "_pouch_contour", this, true)); // HIDDEN // restore to true
		// sets the four internal boundaries in detector_.wingSnake_
		detections_.add(new WPouchBoundariesDetection(name_ + "_pouch_axes", this)); // HIDDEN
		// sets detector_.wPouchCenter, detector_.wDiscCenter_, detector_.axis1_, detector_.axis2_, detector_.compartments_ and detector_.contour_
		detections_.add(new InteractiveStructure(name_ + "_pouch_edition", this, !editStructure_)); // HIDDEN
		// sets detector_.pouch_
		detections_.add(new WPouchOrientationDetection(name_ + "_orientation", this, true)); // HIDDEN
	}

	// ----------------------------------------------------------------------------

	/** Resets the detector to make it ready for a new detection. */
	@Override
	public void clean() {
	
		try {
			if (structureProjection_ != null) {
				structureProjection_.changes = false;
				structureProjection_.close(); // DO NOT FORGET TO SET IT AGAIN NEXT TIME ;)
			}
		} catch (Exception e) {
			// do nothing
		}

		try {
			// free images not required anymore
			if (detections_ != null) {	
				int n = detections_.size();
				for (int i = 0; i < n; i++)
					detections_.get(i).removeImages();
			}
		} catch (Exception e) {
			// do nothing
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Runs the early pre-processing method.
	 * IMPORTANT: This method is called when pressing on "Pre-Process". Should be
	 * used only to set variables in WJSettings because the detector instance calling
	 * this method will not be the same as the effective structure detector. For other
	 * processing, consider implementing it as a detection module.
	 */
	@Override
	public void runEarlyPreProcessing() throws Exception {
		
		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			return;
		
//		double blur = settings.getPpBlur();
		double blur = PreProcessing.getPpBlurSigma();
//		PreProcessingScanner scanner = new PreProcessingScanner(blur, blur, blur, // blur
//				settings.getPpThldAutoStepSize(), settings.getPpThldAutoLowerBoundary(), settings.getPpThldAutoUpperBoundary()); // thld
//		scanner.setStructureProjection(WDiscImages.getImageProjection(structureChannelIndex));
//		scanner.execute();
		
		// apply the mask (if any exists)
		WJSettings.log("Setting mask (if any)");
		WJImagesMask.applyMask(structureProjection_);
		
		int ppThreshold = PreProcessing.computeAutoPpThreshold(structureProjection_, blur);	
		WJSettings.log("Optimal pre-processing threshold: " + ppThreshold);
		
		WJSettings.getInstance().setPpThreshold(ppThreshold);
	}

	// ----------------------------------------------------------------------------

	/** Runs a manual detection of the wing pouch structure. */
	@Override
	public void runManualDetection() throws Exception {
		
		try {
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(false);
			// END
	
	//		WPouchStructure structure = (WPouchStructure)structure_;
	//		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
	//		
	//		if (snake == null)
	//			throw new Exception("INFO: Snake structure is null.");
			if (tmpSnake_ == null)
				throw new Exception("INFO: Tmp structure snake is null.");
	
			moduleIndex_ = 6;
			resume();
	
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(true);
			Analytics.getInstance().addStructureDetection(StructureDetectionStats.MANUAL_STRUCTURE_DETECTION, 1);
			// END
		} catch (Exception e) {
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(true);
			// END
			throw e;
		}
	}

	// ----------------------------------------------------------------------------

	/** Sets the WingSnake object with a generic structure (circle with embedded cross and extra point for disc center). */
	@Override
	public void setGenericStructure() throws Exception {

		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");

		WPouchStructure structure = (WPouchStructure)structure_;
		WJSettings settings = WJSettings.getInstance();

		// set default disc and pouch centers
		int w = structureProjection_.getWidth();
		int h = structureProjection_.getHeight();
		structure.center_ = new Point2D.Double(w/2, h/2);
		// set two axes and contour
		int radius = (int) (Math.min(w, h) * settings.getGenericStructureRadius());
		int offset = 1;
		Point2D.Double N = new Point2D.Double(structure.center_.x - offset, structure.center_.y + offset - radius);
		Point2D.Double E = new Point2D.Double(structure.center_.x + offset + radius, structure.center_.y - offset);
		Point2D.Double S = new Point2D.Double(structure.center_.x - offset, structure.center_.y + offset + radius);
		Point2D.Double W = new Point2D.Double(structure.center_.x + offset - radius, structure.center_.y - offset);
		FloatPolygon CN = PolygonFactory.createPolygonSegment(structure.center_, N, 20);
		FloatPolygon CE = PolygonFactory.createPolygonSegment(structure.center_, E, 20);
		FloatPolygon CS = PolygonFactory.createPolygonSegment(structure.center_, S, 20);
		FloatPolygon CW = PolygonFactory.createPolygonSegment(structure.center_, W, 20);
		FloatPolygon interiorContour = PolygonFactory.createPolygonCircle(structure.center_, radius, 400);

		// compute the center of mass of the wing disc
//		String name = structure_.getName();
//		WDiscCenterDetection wDiscCenterDetector = new WDiscCenterDetection(name + "_wdisc_center", this, true); // HIDDEN
//		wDiscCenterDetector.run();
		structure.discCenter_ = new Point2D.Double(w/2 - radius, h/2 - radius);

		WPouchStructureSnake snake = new WPouchStructureSnake(structureProjection_.getWidth(), structureProjection_.getHeight());
		snake.setInitialBoundary(0, new Boundary("CN", CN));
		snake.setInitialBoundary(1, new Boundary("CE", CE));
		snake.setInitialBoundary(2, new Boundary("CS", CS));
		snake.setInitialBoundary(3, new Boundary("CW", CW));
		snake.setInitialContour(new Compartment("", interiorContour));
		
		// save reference
		tmpSnake_ = snake;
//		structure.setStructureSnake(snake);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Reads a XML containing the description of a wing pouch structure.
	 * If orientation information are contained in the XML file, the detection
	 * concludes. Otherwise, the orientation is automatically inferred.
	 */
	@Override
	public void openStructure(URI uri) throws Exception {

		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure is null.");
		
		try {
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(false);
			// END
		
			int w = structureProjection_.getWidth();
			int h = structureProjection_.getHeight();
			WPouchStructureSnake snake = new WPouchStructureSnake(w,h);
			structure_.setStructureSnake(snake);
			structure_.read(uri);
			
			if (!structure_.isOrientationKnown()) {
				moduleIndex_ = 7;
				resume();
			} else {
				moduleIndex_ = 8; // required so that isComplete() returns true
				done();
			}
			
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(true);
			Analytics.getInstance().addStructureDetection(StructureDetectionStats.OPENED_STRUCTURE, 1);
			// END
		} catch (Exception e) {
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().saveStructureDetectionStats(true);
			// END
			throw e;
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Opens a structure from file (a dialog is shown to select the file). */
	@Override
	public boolean openStructure() throws Exception {

		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");

		WJSettings settings = WJSettings.getInstance();

		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			WingJ.setAppIcon(frame);

			fc.setDialogTitle("Open wing pouch structure");
			URI uri = FileUtils.getFileURI(settings.getOutputDirectory());
			File f = new File(FileUtils.getFileURI(new File(uri).getCanonicalPath()));
			fc.setCurrentDirectory(f);
			fc.addChoosableFileFilter(new FilterStructureXml());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fc.showDialog(frame, "Open");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				openStructure(file.toURI());
				return true;
			}
		} catch (Exception e) {
			WJMessage.showMessage(e);
			String msg = "Unable to open structure. Did you select the correct system?\n" +
						 "Please see the console for more information.";	
			WJMessage.showMessage(msg, "INFO");
			e.printStackTrace();
		}
		return false;
	}

	// ============================================================================
	// GETTERS AND SETTERS

	public ImagePlus getPpImage() { return ppImage_; }
	public KiteSnake getKiteSnake() { return kiteSnake_; }
}
