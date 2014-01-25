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

package ch.epfl.lis.wingj.structure.drosophila.embryo;

import ij.process.FloatPolygon;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.analytics.StructureDetectionStats;
import ch.epfl.lis.wingj.filefilters.FilterStructureXml;
import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.StructureDetector;
import ch.epfl.lis.wingj.structure.geometry.PolygonFactory;
import ch.epfl.lis.wingj.structure.tools.EmbryoSnake;
import ch.epfl.lis.wingj.utilities.FileUtils;

/** 
 * Generates a structure model of the <i>Drosophila</i> embryo.
 * 
 * @version October 29, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class EmbryoStructureDetector extends StructureDetector {
	
	// INTERNAL VARIABLES SHARED BY THE DIFFERENT STAGES OF THE EMBRYO DETECTION
	/** Snake tool to detect the boundary of the embryo. */
	private EmbryoSnake embryoSnake_ = null;
	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public EmbryoStructureDetector(String name) {
		
		super(name);

		EmbryoStructure structure = new EmbryoStructure(name);
		structure.setAge("");
		
		structure_ = structure;
	}

	// ----------------------------------------------------------------------------

	/** Initializes the detection modules. */
	@Override
	protected void initializeDetectionModules() {

		detections_ = new ArrayList<StructureDetectionModule>();
		// detects the contour of the embryo
		detections_.add(new EmbryoContourDetection(name_ + "_contour", this));
		// builds the A/P and D/V boundary
		detections_.add(new EmbryoBoundariesDetection(name_ + "_boundaries", this, true)); // HIDDEN
		// interactive structure
		detections_.add(new InteractiveStructure(name_ + "_embryo_edition", this, !editStructure_));
		// infers the orientation of the embryo
		detections_.add(new EmbryoOrientationDetection(name_ + "_orientation", this, true)); // HIDDEN
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
		
		WJSettings.log("Nothing to pre-process for the embryo.");
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
		
			if (tmpSnake_ == null)
				throw new Exception("INFO: Tmp structure snake is null.");
	
			moduleIndex_ = 2;
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

	/** Sets the snake object with a generic structure (circle with embedded cross and extra point for orientation inference). */
	@Override
	public void setGenericStructure() throws Exception {
		
		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");

		EmbryoStructure structure = (EmbryoStructure)structure_;
		WJSettings settings = WJSettings.getInstance();

		// set default disc and pouch centers
		int w = structureProjection_.getWidth();
		int h = structureProjection_.getHeight();
		Point2D.Double structureCenter = new Point2D.Double(w/2, h/2);
		structure.setCenter(structureCenter);
		// set two axes and contour
		int radius = (int) (Math.min(w, h) * settings.getGenericStructureRadius());
		int offset = 2; // 1px: leads to errors in structure orientation inference
		Point2D.Double N = new Point2D.Double(structureCenter.x + offset, structureCenter.y + offset - radius);
		Point2D.Double E = new Point2D.Double(structureCenter.x + offset + radius, structureCenter.y + offset);
		Point2D.Double S = new Point2D.Double(structureCenter.x + offset, structureCenter.y + offset + radius);
		Point2D.Double W = new Point2D.Double(structureCenter.x + offset - radius, structureCenter.y + offset);
		FloatPolygon CN = PolygonFactory.createPolygonSegment(structureCenter, N, 20);
		FloatPolygon CE = PolygonFactory.createPolygonSegment(structureCenter, E, 20);
		FloatPolygon CS = PolygonFactory.createPolygonSegment(structureCenter, S, 20);
		FloatPolygon CW = PolygonFactory.createPolygonSegment(structureCenter, W, 20);
		FloatPolygon interiorContour = PolygonFactory.createPolygonCircle(structureCenter, radius, 400);

		// compute the center of mass of the wing disc
//		String name = structure_.getName();
//		WDiscCenterDetection wDiscCenterDetector = new WDiscCenterDetection(name + "_wdisc_center", this, true); // HIDDEN
//		wDiscCenterDetector.run();
		structure.setDiscCenter(new Point2D.Double(w/2 - radius, h/2 - radius));

		EmbryoStructureSnake snake = new EmbryoStructureSnake(structureProjection_.getWidth(), structureProjection_.getHeight());
		snake.setInitialBoundary(0, new Boundary("CN", CN));
		snake.setInitialBoundary(1, new Boundary("CE", CE));
		snake.setInitialBoundary(2, new Boundary("CS", CS));
		snake.setInitialBoundary(3, new Boundary("CW", CW));
		snake.setInitialContour(new Compartment("", interiorContour));
		
		// save reference
		tmpSnake_ = snake;
//		structure.setStructureSnake(snake);

//		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
//			throw new Exception("INFO: Single image or image stack required.");
//
//		EmbryoStructure structure = (EmbryoStructure)structure_;
//		WJSettings settings = WJSettings.getInstance();
//
//		// set default disc and pouch centers
//		int w = structureProjection_.getWidth();
//		int h = structureProjection_.getHeight();
//		
//		Point2D.Double center = new Point2D.Double(w/2, h/2);
//		// set two axes and contour
//		int radius = (int) (Math.min(w, h) * settings.getGenericStructureRadius());
//		int offset = 5;
//		Point2D.Double N = new Point2D.Double(center.x + offset, center.y + offset - radius);
//		Point2D.Double E = new Point2D.Double(center.x + offset + radius, center.y + offset);
//		Point2D.Double S = new Point2D.Double(center.x + offset, center.y + offset + radius);
//		Point2D.Double W = new Point2D.Double(center.x + offset - radius, center.y + offset);
//		FloatPolygon CN = PolygonFactory.createPolygonSegment(center, N, 20);
//		FloatPolygon CE = PolygonFactory.createPolygonSegment(center, E, 20);
//		FloatPolygon CS = PolygonFactory.createPolygonSegment(center, S, 20);
//		FloatPolygon CW = PolygonFactory.createPolygonSegment(center, W, 20);
//		FloatPolygon interiorContour = PolygonFactory.createPolygonCircle(center, radius, 400);
//		structure.setCenter(center);
//
//		// control point for manual orientation inference
//		structure.setDiscCenter(new Point2D.Double(center.x - radius, center.y - radius));
//
//		EmbryoStructureSnake snake = new EmbryoStructureSnake(structureProjection_.getWidth(), structureProjection_.getHeight());
//		snake.setInitialBoundary(0, new Boundary("CN", CN));
//		snake.setInitialBoundary(1, new Boundary("CE", CE));
//		snake.setInitialBoundary(2, new Boundary("CS", CS));
//		snake.setInitialBoundary(3, new Boundary("CW", CW));
//		snake.setInitialContour(new Compartment("", interiorContour));
//		
//		// save reference
//		structure.setStructureSnake(snake);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Reads a XML containing the description of a embryo structure.
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
			EmbryoStructureSnake snake = new EmbryoStructureSnake(w,h);
			structure_.setStructureSnake(snake);
			structure_.read(uri);
			
			if (!structure_.isOrientationKnown()) {
				moduleIndex_ = 3;
				resume();
			} else {
				moduleIndex_ = 4; // required so that isComplete() returns true
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

	/**Opens a structure from file (a dialog is shown to select the file). */
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

			fc.setDialogTitle("Open embryo structure");
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

	public EmbryoSnake getEmbryoSnake() { return embryoSnake_;	}
	public void setEmbryoSnake(EmbryoSnake embryoSnake) { embryoSnake_ = embryoSnake; }
}
