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
import ij.plugin.Duplicator;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.utilities.Filters;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.tools.FluorescenceTrajectoryTracker;

/** 
 * Detects the trajectory of each of the four <i>half-boundaries</i> forming the A/P and D/V boundaries.
 * 
 * @version October 25, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchBoundariesDetection extends StructureDetectionModule {
	
	/** Image used as input by the tracker. */
	private ImagePlus optimizerImage_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchBoundariesDetection(String name, WPouchStructureDetector detector, boolean hidden) {

		super(name, detector, hidden);
		description_ = "Detecting the trajectory of the A/P and D/V boundaries.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchBoundariesDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Detecting the trajectory of the A/P and D/V boundaries.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Performs the extraction of the four axes of the wing pouch. */
	@Override
	public void run() throws Exception {

		detectAxes();
	}
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Performs the extraction of the four axes of the wing pouch. */
	private void detectAxes() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		WPouchStructure structure = (WPouchStructure)detector.getStructure();
		WPouchStructureSnake snake = (WPouchStructureSnake)detector.getTmpStructureSnake();
		
		Compartment boundingRegion = snake.getInitialContour();
		double shrinckage = settings.getBoundaryTrackerShrinkageInPixels();// 2 //tracker.getOverallRoiDims().width;

		// get a copy of the wing pouch center
		Point2D.Double wPouchCenter = (Point2D.Double) structure.center_.clone();
		
		// compute a slightly blurred image to use with the tracker
		optimizerImage_ = new Duplicator().run(detector.structureProjection_);
   		Filters.applyGaussianFilter(optimizerImage_.getProcessor(), PreProcessing.getPpBlurSigma()/2.);
   		
   		// get wing pouch contour previously detected
   		Compartment aoi = snake.getInitialContour();
   		
   		FluorescenceTrajectoryTracker tracker = new FluorescenceTrajectoryTracker();
//   		ImagePlus backgroundImage = new Duplicator().run(optimizerImage_);
		tracker.setImage(optimizerImage_);
		// get dimension of the tracker
		int expectedBoundariesThickness = (int)settings.getExpectedBoundariesThicknessInPixels();
		Dimension overall = new Dimension(3*expectedBoundariesThickness, 3*expectedBoundariesThickness);
		Dimension background = new Dimension(expectedBoundariesThickness, expectedBoundariesThickness);
		double scale = settings.getBoundaryTrackerScale();
		tracker.setStepSize(settings.getBoundaryTrackerStepSizeInPixels());
		tracker.setNumSteps(settings.getBoundaryTrackerNumSteps());
		
		double trackerShowTime = settings.getBoundaryTrackerShowDuration(); // if negative, do not show the tracker
		boolean showTracker = detector.isInteractive() && !hidden_;
		
		// run the trackers to detect the compartment boundaries and trim the last points
		tracker.initialize(wPouchCenter, aoi, overall, background, scale);
		tracker.setInitialDirection(detector.kiteSnake_.getNorthDirection());
		List<Point2D> northPath = null;
		if (showTracker) northPath = tracker.track(optimizerImage_, trackerShowTime);
		else northPath = tracker.track();
		Boundary northBoundary = new Boundary("N",northPath);
		northBoundary = northBoundary.resample((int)Math.round(northBoundary.lengthInPx()/2.0));
		northBoundary.trimEnd(boundingRegion);
		northBoundary.trimEnd(shrinckage);
		snake.setInitialBoundary(0, northBoundary);

		tracker.initialize(wPouchCenter, aoi, overall, background, scale);
		tracker.setInitialDirection(detector.kiteSnake_.getEastDirection());
		List<Point2D> eastPath = null;
		if (showTracker) eastPath = tracker.track(optimizerImage_, trackerShowTime);
		else eastPath = tracker.track();	
		Boundary eastBoundary = new Boundary("E",eastPath);
		eastBoundary = eastBoundary.resample((int)Math.round(eastBoundary.lengthInPx()/2.0));
		eastBoundary.trimEnd(boundingRegion);
		eastBoundary.trimEnd(shrinckage);
		snake.setInitialBoundary(1, eastBoundary);

		tracker.initialize(wPouchCenter, aoi, overall, background, scale);
		tracker.setInitialDirection(detector.kiteSnake_.getSouthDirection());
		List<Point2D> southPath = null;
		if (showTracker) southPath = tracker.track(optimizerImage_, trackerShowTime);
		else southPath = tracker.track();	
		Boundary southBoundary = new Boundary("S",southPath);
		southBoundary = southBoundary.resample((int)Math.round(southBoundary.lengthInPx()/2.0));
		southBoundary.trimEnd(boundingRegion);
		southBoundary.trimEnd(shrinckage);
		snake.setInitialBoundary(2, southBoundary);

		tracker.initialize(wPouchCenter, aoi, overall, background, scale);
		tracker.setInitialDirection(detector.kiteSnake_.getWestDirection());
		List<Point2D> westPath = null;
		if (showTracker) westPath = tracker.track(optimizerImage_, trackerShowTime);
		else westPath = tracker.track();	
		Boundary westBoundary = new Boundary("W",westPath);
		westBoundary = westBoundary.resample((int)Math.round(westBoundary.lengthInPx()/2.0));
		westBoundary.trimEnd(boundingRegion);
		westBoundary.trimEnd(shrinckage);
		snake.setInitialBoundary(3, westBoundary);
		
		optimizerImage_.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes all images created by this detection module (if any). */
	@Override
	public void setImagesVisible(boolean visible) {
		
		if (optimizerImage_ != null) {
			if (visible) optimizerImage_.show();
			else optimizerImage_.hide();
		}
	}
}