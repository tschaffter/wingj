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

package ch.epfl.lis.wingj.structure.tools;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.utilities.MathUtils;

/**
 * Starts from a point and move along a fluorescent trajectory or boundary.
 * <p>
 * The tracker has a front and rear part. The tracker starts moving in the direction given
 * by the vector initialDirection_ (saved in a Point2D object). The vector initialDirection_
 * is only used for the orientation, the amplitude of the first step being defined by stepSize_.
 * At each iteration, the tracker performs successively ROTATION and TRANSLATION operations to
 * keep the fluorescent path centered. The inspiration came from a robot I built at EPFL (Robopoly)
 * which was designed to follow a black tape on a white table using an IR sensor placed at the front
 * part of the robot. Here both front and rear part of the tracker follow the path. The tracker
 * behaves has the robot and will always try to follow the path which has the highest fluorescent
 * intensity. The front point of the tracker is displayed in yellow and the rear part in cyan.
 * 
 * @see PlusShapeCenterDetector
 * 
 * @version October 20, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class FluorescenceTrajectoryTracker extends PlusShapeCenterDetector {
	
	/** If the error is smaller than the one defined, the optimization ends (in degree). */
	private static final double TRACKER_MIN_ROTATION_ANGLE_ERROR = 1.;
	
	/** Initial orientation of the tracker in image coordinate system (initially heading East, i.e. 90 degrees). */
	private static final double TRACKER_INITIAL_ORIENTATION = MathUtils.toPositiveAngle(Math.toDegrees(Math.atan2(1, 0)));
	
	/** North direction. */
	public static final int NORTH = 0;
	/** East direction. */
	public static final int EAST = 1;
	/** South direction. */
	public static final int SOUTH = 2;
	/** West direction. */
	public static final int WEST = 3;

	/** Orientation of the tracker in image coordinate system. */
	private double orientation_ = TRACKER_INITIAL_ORIENTATION;
	/** Step size in px. */
	private double stepSize_ = 30.; // was 40.
	/** Number of steps to perform. */
	private int numSteps_ = 10; // was 10
	
	/** Initial direction is a vector of amplitude equals to stepSize_ (in image coordinate system). */
	private Point2D.Double initialDirection_ = null;
	
	/** Front point centered on the fluorescent path. */
	private Point2D.Double frontMaxPoint = null;
	/** Rear point centered on the fluorescent path. */
	private Point2D.Double rearMaxPoint = null;
	
	/** Contains the center of mass of the tracker for the different steps. */
	private List<Point2D> path_ = new ArrayList<Point2D>();
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Sets the orientation bounded in [0,360[. */
	private void setOrientation(double orientation) {

		orientation_ = MathUtils.toPositiveAngle(orientation);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the absolute orientation of the tracker. */
	private void setTrackerOrientation(double angle) throws Exception {
		
		double delta = MathUtils.toPositiveAngle(angle) - MathUtils.toPositiveAngle(TRACKER_INITIAL_ORIENTATION);
		rotateRois(center_, -delta); // minus because AffineTransform does the inverse of the standard
		setOrientation(angle);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Rotates the tracker of the given delta angle. */
	private void rotateTracker(double delta) throws Exception {
		
		rotateRois(center_, -delta); // minus because AffineTransform does the inverse of the standard
		setOrientation(orientation_ + delta);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Performs one step in the current orientation and re-orient the tracker to be parallel to the fluorescent trajectory. */
	private void step(int index) throws Exception {
		
		// compute the components x and y of the step
		center_.x += Math.sin(Math.toRadians(orientation_)) * stepSize_;
		center_.y += Math.cos(Math.toRadians(orientation_)) * stepSize_;
		
		// recompute the ROIs and orient them correctly
		setRois();
		
		// translate the tracker to be centered on the fluorescence trajectory (if required)
		boolean translationConverged = optimizeTranslation();
		// rotate the tracker so that it's aligned with the fluorescent trajectory
		boolean rotationConverged = optimizeRotation();
		
		if (WJSettings.DEBUG) {
		if (!translationConverged)
			System.out.println("WARNING: Step " + index + ": translation didn't converged with error < " + CENTER_OPTIMIZER_MIN_ERROR_DIFF + " px.");
		if (!rotationConverged)
			System.out.println("WARNING: Step " + index + ": rotation didn't converged with error < " + TRACKER_MIN_ROTATION_ANGLE_ERROR + " degrees.");
		}
		
		// save the center of the tracker
		path_.add((Point2D.Double) center_.clone());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Iterative algorithm to align the tracker with the fluorescent trajectory. */
	private boolean optimizeRotation() throws Exception {
		
		int iter = 0;
		boolean ok = false;
		double delta = 0.;
		while (iter < CENTER_OPTIMIZER_MAX_ITERS && !ok) {
			
			delta = computeDelta();
			
			// compare the absolute value of the angle delta
			if (Math.abs(delta) < TRACKER_MIN_ROTATION_ANGLE_ERROR)
				ok = true;
			else {
				rotateTracker(delta);
				iter++;
			}
		}
	
		return ok;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Iterative algorithm to translate the tracker so that it's centered on the fluorescence trajectory. */
	private boolean optimizeTranslation() throws Exception {
		
		int iter = 0;
		boolean ok = false;
		while (iter < CENTER_OPTIMIZER_MAX_ITERS && !ok) {
			
			computeCorrectedCenter();
			
			if (computeCenterError() < CENTER_OPTIMIZER_MIN_ERROR_DIFF) {
				ok = true;
				correctedCenter_ = null;
			}
			else {
				center_ = correctedCenter_;
				setRois();
				iter++;	
			}
		}
		
		return ok;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the delta angle to apply in order to align the tracker with the fluorescence trajectory. */
	private double computeDelta() throws Exception {
		
		// front part of the tracker: crossIntensityProfiles_.get(1)
		// rear part of the tracker: crossIntensityProfiles_.get(3)
		measureExpression();
		
		// get the pixel index of the max mode
		int frontMaxIndex = findMaxIndex(crossIntensityProfiles_.get(1));
		int rearMaxIndex = findMaxIndex(crossIntensityProfiles_.get(3));
		
		// get a point object corresponding to the obtained pixel index
		Point2D.Double frontMaxPoint = getPointOnLine2D((Line2D.Double) profileLines_.get(1), (double) frontMaxIndex);
		Point2D.Double rearMaxPoint = getPointOnLine2D((Line2D.Double) profileLines_.get(3), (double) rearMaxIndex);
		
		// get angle between pixel index point - tracker center - first point of the Line2D used
		double frontAngle = MathUtils.angleBetween(frontMaxPoint, center_, (Point2D.Double) profileLines_.get(1).getP1()); // negative angle
		double rearAngle = MathUtils.angleBetween(rearMaxPoint, center_, (Point2D.Double) profileLines_.get(3).getP1()); // positive angle
		
		// IMPORTANT: do what follows because the previous angle can be incorrect
		if (frontAngle > 0) frontAngle -= 360.;
		if (rearAngle < 0) rearAngle += 360.;
		
		// return the average of the two angles computed
		// Example: if |frontAngle| < |rearAngle|, rotate clockwise to place the fluorescent path
		// in the center of the profileLines considered
		return (rearAngle + frontAngle) / 2.;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the coordinates of a point contained in a Line2D distant from the first point of the given distance. */
	private Point2D.Double getPointOnLine2D(Line2D.Double line, double offset) throws Exception {
		
		if (line == null)
			throw new Exception("ERROR: line is null.");
		
		double dx = line.x2 - line.x1;
		double dy = line.y2 - line.y1;
		double x = line.x1 + offset * Math.cos(Math.atan2(dy, dx));
		double y = line.y1 + offset * Math.sin(Math.atan2(dy, dx));
		
		return new Point2D.Double(x, y);
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Draws the ROIs on the given image. */
	@Override
	protected void drawRois(ImagePlus image) throws Exception {
		
		super.drawRois(image);
		
		ImageProcessor ip = image.getProcessor();
		
		// draw over background ROIs so that each has a different color
		// in order to easily know the orientation of the tracker
		// Front side of the tracker is yellow
		ip.setColor(Color.CYAN);
		image.getProcessor().draw(backgroundRois_.get(0));
		ip.setColor(Color.YELLOW);
		image.getProcessor().draw(backgroundRois_.get(1));
		ip.setColor(Color.YELLOW);
		image.getProcessor().draw(backgroundRois_.get(2));
		ip.setColor(Color.CYAN);
		image.getProcessor().draw(backgroundRois_.get(3));
		
		// points centered with the fluorescence path
		if (frontMaxPoint != null) {
			ip.setColor(Color.YELLOW);
			ip.drawOval((int) (frontMaxPoint.x - 2), (int) (frontMaxPoint.y - 2), 4, 4);
			ip.setColor(Color.CYAN);
			ip.drawOval((int) (rearMaxPoint.x - 2), (int) (rearMaxPoint.y - 2), 4, 4);
		}
		
		// center of the robot at the previous step
		ip.setColor(Color.RED);
		ip.setLineWidth(1);
		for (int i = 0; i < path_.size(); i++) {
			Point2D.Double p = (Point2D.Double) path_.get(i);
			ip.drawOval((int) (p.x - 2), (int) (p.y - 2), 4, 4);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets ROIs. */
	@Override
	protected void setRois() throws Exception {
		
		// set ROIs straight
		super.setRois();
		// set orientation to the last orientation known
		setTrackerOrientation(orientation_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the corrected center point of the tracker to have it aligned with the fluorescence trajectory. */
	@Override
	protected void computeCorrectedCenter() throws Exception {
		
		// front part of the tracker: crossIntensityProfiles_.get(1)
		// rear part of the tracker: crossIntensityProfiles_.get(3)
		measureExpression();
		
		// get the pixel index of the max mode
		int frontMaxIndex = findMaxIndex(crossIntensityProfiles_.get(1));
		int rearMaxIndex = findMaxIndex(crossIntensityProfiles_.get(3));
		
		// get a point object corresponding to the obtained pixel index
		frontMaxPoint = getPointOnLine2D((Line2D.Double) profileLines_.get(1), (double) frontMaxIndex);
		rearMaxPoint = getPointOnLine2D((Line2D.Double) profileLines_.get(3), (double) rearMaxIndex);
		
		double x = (frontMaxPoint.x + rearMaxPoint.x) / 2.;
		double y = (frontMaxPoint.y + rearMaxPoint.y) / 2.;
		correctedCenter_ = new Point2D.Double(x, y);
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public FluorescenceTrajectoryTracker() {
		
		super();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets initial direction. */
	public void setInitialDirection(Point2D.Double direction) throws Exception {
		
		setInitialDirection(direction, stepSize_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the initial direction as one among the cardinal points. */
	public void setInitialDirection(int cardinalDirection) throws Exception {
		
		switch (cardinalDirection) {
			case NORTH: initialDirection_ = new Point2D.Double(0, -stepSize_); break;
			case EAST: initialDirection_ = new Point2D.Double(stepSize_, 0); break;
			case SOUTH: initialDirection_ = new Point2D.Double(0, stepSize_); break;
			case WEST: initialDirection_ = new Point2D.Double(-stepSize_, 0); break;
			default: throw new Exception("ERROR: Invalid cardinal direction.");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets initial direction. */
	public void setInitialDirection(Point2D.Double direction, double stepSize) throws Exception {
		
		if (direction == null)
			throw new Exception("ERROR: direction is null");
		if (direction.x == 0 && direction.y == 0)
			throw new Exception("ERROR: direction is zero.");
		
		stepSize_ = stepSize;
		initialDirection_ = (Point2D.Double) direction.clone();
		
		// set the amplitude of direction to the step-size
		double norm = Math.sqrt(Math.pow(initialDirection_.x, 2) + Math.pow(initialDirection_.y, 2));
		initialDirection_.x *= stepSize_ / norm;
		initialDirection_.y *= stepSize_ / norm;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Starts tracking the fluorescence trajectory using the predefined direction. */
	public List<Point2D> track() throws Exception {
		
		return track(null, null, 0);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Starts tracking the fluorescence trajectory using the predefined direction.
	 * @param exportImage Background image displayed under the tracker at each step.
	 * @param rootFilename Base filename to export the configuration of the tracker
	 * at each step.
	 */
	public List<Point2D> track(ImagePlus exportImage, String rootFilename) throws Exception {
		
		return track(exportImage, rootFilename, 0);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Starts tracking the fluorescence trajectory using the predefined direction.
	 * @param visibleTimeInSec Time in second during which the configuration of the
	 * tracker is shown at each step.
	 */
	public List<Point2D> track(double visibleTimeInSec) throws Exception {
		
		return track(null, null, visibleTimeInSec);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Starts tracking the fluorescence trajectory using the predefined direction.
	 * @param exportImage Background image displayed under the tracker at each step.
	 * @param visibleTimeInSec Time in second during which the configuration of the
	 * tracker is shown at each step.
	 */
	public List<Point2D> track(ImagePlus exportImage, double visibleTimeInSec) throws Exception {
		
		return track(exportImage, null, visibleTimeInSec);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Starts tracking the fluorescence trajectory using the predefined direction.
	 * @param exportImage Background image displayed under the tracker at each step.
	 * @param rootFilename Base filename to export the configuration of the tracker
	 * at each step.
	 * @param visibleTimeInSec Time in second during which the configuration of the
	 * tracker is shown at each step.
	 */
	public List<Point2D> track(ImagePlus exportImage, String rootFilename, double visibleTimeInSec) throws Exception {
		
		path_.clear();
		path_.add((Point2D.Double) center_.clone());
		
		// orient the tracker in the correct direction
		double initialAngle = MathUtils.toPositiveAngle(Math.toDegrees(Math.atan2(initialDirection_.x, initialDirection_.y)));
		setTrackerOrientation(initialAngle);
		
		for (int i = 0; i < numSteps_; i++) {
			
			step(i);

			if (exportImage != null && exportImage.getProcessor() != null) {
				
//				exportImage.killRoi();
//				ImagePlus img = new Duplicator().run(exportImage);
//				new ImageConverter(img).convertToRGB();
				
				WJSettings.log(rootFilename);
				
				// draw tracker
				if (rootFilename != null) {
					ImagePlus img = new Duplicator().run(exportImage);
					new ImageConverter(img).convertToRGB();
					drawRois(exportImage, rootFilename+ "_" + i + ".tif");
				} 
//				else 
//					drawRois(exportImage);
					
				// set tracker visible (if required)
				if (visibleTimeInSec > 0) {
					exportImage.show();
					int dt = (int)(1000.*visibleTimeInSec);
					Thread.sleep(dt);
				}
//				img.close();
			}
			
			if (aoi_ != null && !aoi_.contains((float)center_.x, (float)center_.y))
				break;
		}
		return path_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Draws the ROIs on a duplicate of the image set and saved it to TIFF file. */
	@Override
	public void drawRois(String filename) throws Exception {
		
		drawRois(image_, filename);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Draws the ROIs on a duplicate of the given image and saved it to TIFF file. */
	@Override
	public void drawRois(ImagePlus image, String filename) throws Exception {
		
		if (image == null)
			throw new Exception("ERROR: image is null.");
		
		drawRois(image);
		IJ.save(image, filename);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setStepSize(double stepSize) { stepSize_ = stepSize; }
	public double getStepSize() { return stepSize_; }
	
	public void setNumSteps(int numSteps) { numSteps_ = numSteps; }
	public int getNumSteps() { return numSteps_; }
}
