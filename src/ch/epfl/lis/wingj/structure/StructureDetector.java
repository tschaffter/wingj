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

import java.net.URI;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.analytics.StructureDetectionStats;

import ij.ImagePlus;
import ij.plugin.Duplicator;

/** 
 * Abstract class for implementing a morphological structure detector.
 * <p>
 * This class provides method to run a structure detection. The detection
 * is composed of different detection modules run one after another in a
 * specific order defined in the class extending this class.
 * <p>
 * The method runAll() runs all modules one after another. step() runs
 * only one module before stopping. pause() stops the detection at the first
 * occasion when pressed after runAll() or resume(). resume() is used to
 * resume the automatic detection after having paused it. step() runs a
 * single detection module. abort() cancels the current detection. A new
 * detection can be run abort() with runAll() or step().
 *
 * @see Structure
 * @see StructureSnake
 * @see StructureDetectionModule
 * 
 * @version October 10, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
abstract public class StructureDetector {
	
	/** Name of the detector (used also to name the modules). */
	protected String name_ = "structure-detector";

	/**
	 * Image projection used to detect the structure.
	 * <p>
	 * This member variable is declared public to enable quick access from the detection modules.
	 */
	public ImagePlus structureProjection_ = null;
	
	/** List of detection steps to perform. */
	protected List<StructureDetectionModule> detections_ = null;
	
	/** Structure object identified */
	protected Structure structure_ = null;
	
	/** Current detection module index. */
	protected int moduleIndex_ = 0;
	/** Stop/pause variable used to signal if the detection should stop. */
	protected boolean stop_ = false;
	/** Error variable used to signal if the detection encountered an error. */
	protected boolean error_ = false;
	/** Set to true to allow the user to interact with the detection. */
	protected boolean interactive_ = false;
	/** Allow the user to edit the identified structure. */
	protected boolean editStructure_ = true;
	/** Show the identified structure at the end of the process. */
	protected boolean showStructure_ = true;
	
	/** Temporary structure snake used , e.g. when resampling and editing a snake. */
	protected StructureSnake tmpSnake_ = null;
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/**
	 * Runs the early pre-processing method.
	 * IMPORTANT: This method is called when pressing on "Pre-Process". Should be
	 * used only to set variables in WJSettings because the detector instance calling
	 * this method will not be the same as the effective structure detector. For other
	 * processing, consider implementing it as a detection module.
	 */
	abstract public void runEarlyPreProcessing() throws Exception;
	/** Initializes the detection modules. */
	abstract protected void initializeDetectionModules();
	/** Resets the detector to make it ready for a new detection. */
	abstract public void clean();
	/** Runs a manual detection of the structure. */
	abstract public void runManualDetection() throws Exception;
	/** Sets the structure with a predefined/generic shape. */
	abstract public void setGenericStructure() throws Exception;
	/** Opens a structure from file. */
	abstract public void openStructure(URI uri) throws Exception;
	/** Opens a structure from file (a dialog is shown to select the file). */
	abstract public boolean openStructure() throws Exception;
	
	// ============================================================================
	// PRIVATE METHODS

	/**
	 * Identify step-by-step a morphological structure from images.
	 * <p>
	 * Returns the index of the next detection module index.
	 */
	protected int step(int step) throws Exception {

		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("ERROR: Single image or image stack required.");
		if (step < 0 || step > detections_.size())
			throw new Exception("ERROR: Invalid detection index.");
//		if (step == detections_.size())
//			throw new Exception("INFO: Structure detection is done.");

		// Hide images generated during the last stage
		if (step > 0)
			detections_.get(step-1).setImagesVisible(false);
		
		StructureDetectionModule  detection = detections_.get(step);
		String suffix = " (" + (step+1) + "/" + detections_.size() + ")";
		WJSettings.log(detection.toString() + suffix);

		// RUN TASK
		detection.removeImages();
		detection.run();
		while (!detection.test()) {
			if (stop_) // in case detection is stopped during tests
				return moduleIndex_;
			detection.update();
			WJSettings.log(detection.toString() + suffix);
			detection.removeImages();
			detection.run();
		}
		// do level++ here
		moduleIndex_++;
		
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		if (interactive_) // supervised detection
		  Analytics.getInstance().addStructureDetection(StructureDetectionStats.SUPERVISED_STRUCTURE_DETECTION, 1.0/detections_.size());
		else // automatic detection
		  Analytics.getInstance().addStructureDetection(StructureDetectionStats.AUTO_STRUCTURE_DETECTION, 1.0/detections_.size());
		// END

		// Check if the last detection module has been performed
		if (isComplete()) {
			done();
			return moduleIndex_;
		}
		// If the detection module is in hidden mode, run automatically the next one
		if (interactive_ && detection.isHidden()) {
			WJSettings.log("Running automatically the next detection module.");
			step();
		}
		
		return moduleIndex_;
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
   	/** Default constructor. */
   	public StructureDetector(String name) {
   		
   		name_ = name;
   		initializeDetectionModules();
   	}
   	
	// ----------------------------------------------------------------------------

	/** Runs a new structure detection. */
	public void runAll() throws Exception {

		moduleIndex_ = 0;
		resume();
	}
	
	// ----------------------------------------------------------------------------

	/** Runs the structure detection from the current detection module. */
	public void resume() throws Exception {
		
		stop_ = false; // reset
		for (int i = moduleIndex_; i <= detections_.size(); i++) {
			if (stop_) {
				WJSettings.log("INFO: Structure detection canceled.");
				return;
			}
			i = step();
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Performs one detection module and return the index of the next module to run. */
	public int step() throws Exception {

		return step(moduleIndex_); // before level_++
	}
	
	// ----------------------------------------------------------------------------

	/** Reruns the last detection module. */
	public void redo() throws Exception {

		if (stop_)
			throw new Exception("INFO: First start a new structure detection.");

		moduleIndex_--;
		step();
	}
   	
	// ----------------------------------------------------------------------------

	/** Called after applying the last detection module. */
	protected void done() throws Exception {

		clean();
		
		// important for going to Structure panel
		int M0 = structure_.getStructureSnake().getNumControlPointsPerSegment();
		WJSettings.getInstance().setNumStructureControlPoints(M0);

		// Show the detected structure on top of the mip image
		// Show also tools to edit the detected structure
		if (showStructure_)
			WingJ.getInstance().openStructureEditor();

		// Show a dialog stating about the detection of the wing pouch
		// Update: Don't show the dialog, it's annoying.
		String msg = "Structure detection done.\n";
		WJSettings.log(msg);
	}
	
	// ----------------------------------------------------------------------------

	/** Returns true if the detection is complete. */
	public boolean isComplete() {

		return (moduleIndex_ >= detections_.size());
	}
	
	// ----------------------------------------------------------------------------

	/** Sets stop variable to true to stop the detection. */
	public void pause() {

		stop_ = true;
	}
	
	// ----------------------------------------------------------------------------

	/** Aborts the current detection. */
	public void abort() {

		reset();
		moduleIndex_ = detections_.size();
	}
	
	// ----------------------------------------------------------------------------

	/** Resets the detector. */
	public void reset() {

		moduleIndex_ = 0;
		stop_ = false;
		error_ = false;

		clean();
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public Structure getStructure() { return structure_; }
	
	public void setStructureProjection(ImagePlus image) throws Exception {

		if (image == null || image.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");

		image.restoreRoi();
		if (image.getRoi() != null) {
			image.saveRoi();
			image.killRoi(); // first kill the ROI or the duplicate will be cropped to the ROI
		}
		structureProjection_ = new Duplicator().run(image);
		structureProjection_.setTitle("detection_structure_projection");
		image.restoreRoi();
	}
	public ImagePlus getStructureProjection() { return structureProjection_; }

	public void setError(boolean b) { error_ = b; }
	public boolean getError() { return error_; }

	public void isInteractive(boolean b) { interactive_ = b; }
	public boolean isInteractive() { return interactive_; }

	public void editStructure(boolean b) { editStructure_ = b; }
	public boolean editStructure() { return editStructure_; }

	public void showStructure(boolean b) { showStructure_ = b; }
	public boolean showStructure() { return showStructure_; }
	
	public void setName(String name) { name_ = name; }
	public String getName() { return name_; }
	
	public void setTmpStructureSnake(StructureSnake snake) { tmpSnake_ = snake; }
	public StructureSnake getTmpStructureSnake() { return tmpSnake_; }
}