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

import java.awt.geom.Point2D;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import ch.epfl.lis.wingj.utilities.MathUtils;

import big.ij.snake2D.Snake2D;
import big.ij.snake2D.Snake2DNode;

/**
 * Abstract class to extend for descrbing a morphological structure using B-splines.
 * <p>
 * B-splines are used to obtain a parameterized description of a morphological
 * structure such as the <i>Drosophila</i> wing pouch or embryo. Look at the implementation
 * of WPouchStructureSnake for an idea of how to extend this class.
 * 
 * @see ch.epfl.lis.wingj.structure.Structure
 * @see ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureSnake
 * 
 * @version November 9, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
abstract public class StructureSnake implements Snake2D {
	
	/** Mean aggregated structure. */
	public static final int AGGREGATION_MEAN = 0;
	/** Mean+std aggregated structure (only structure contour is affected, boundaries are still obtained by the mean method). */
	public static final int AGGREGATION_MEAN_PLUS_STD = 1;
	/** Mean-std aggregated structure (only structure contour is affected, boundaries are still obtained by the mean method). */
	public static final int AGGREGATION_MEAN_MINUS_STD = 2;
	
	/** Entire list of nodes defining the snake. */
	protected Snake2DNode[] allNodes_ = null;
	
	/** LUT for the exponential B-spline basis function. */
	protected double[] eSplineFunc_ = null;
	
	/** Width of the original image data. */
	protected int width_ = 0;
	/** Height of the original image data. */
	protected int height_ = 0;
	
	/** Support of the exponential B-spline basis function. */
	protected int N_ = 4;
	/** Sampling rate that is used when contours are discretized. */
	protected int R_ = 0;
	/** Product of N and R. */
	protected int NR_ = 0;
	/** Number of control points per section. */
	protected int M0_ = 0;
	
	/** If true, indicates that the snake has been initialized. */
	protected boolean initialized_ = false;
	
	/**
	 * Returns true if the structure has been rejected by the user.
	 * <p>
	 * If true, indicates that the user chose to interactively abort
	 * the processing of the snake. Otherwise, if false, indicates that
	 * the dealings with the snake were terminated without user assistance.
	 */
	protected boolean canceledByUser_ = false;
	
	/** Auxiliary natural cubic spline interpolator. */
	protected SplineInterpolator si_ = null;
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Copy operator. */
	abstract public StructureSnake copy();
	/** Returns true if the current and given snakes are equals. */
	abstract public boolean match(StructureSnake snake) throws Exception;
	/** Resamples the structure using lesser or more control points. */
	abstract public void resample(int M0) throws Exception;
	/** Returns a new StructureSnake instance obtained from resampling this. */
	abstract public StructureSnake getResample(int M0) throws Exception;
	/** Checks initialization. */
	abstract protected void checkInitialization() throws Exception;
	/** Computes the outline of the snake. */
	abstract protected void computeSnakeSkin() throws Exception;
	/** Initialization. */
	abstract public void initialize(Snake2DNode[] newNodes) throws Exception;
	
	/** Reverses in the horizontal dimension the whole snake. */
	abstract public void flipHorizontally(Point2D.Double center) throws Exception;
	/** Reverses in the vertical dimension the whole snake. */
	abstract public void flipVertically(Point2D.Double center) throws Exception;
	
	/** Translates the snake using the given vector. */
	abstract public void translate(double dx, double dy) throws Exception;
	/** Rotates the snake using the given angle in radians. */
	abstract public void rotate(Point2D.Double center, double angle) throws Exception;
	
	/**
	 * Retrieves the point on the exterior contour where the an axis attaches.
	 * <p>
	 * These points correspond to {D,V,A,P}. Note that there is no systematic
	 * correspondence between the value of the parameter axis and any of the
	 * points {D,V,A,P}.
	 * @param axis Takes values in [0,3].
	 */
	abstract public Snake2DNode getAnchorPointOnContour(int axis) throws Exception;
	
	/**
	 * Retrieves a list of points containing the coordinates of the exterior outline.
	 * <p>
	 * Note that there is no systematic correspondence between the value of the parameter
	 * axis and the order of the output points.
	 */
	abstract public Point2D.Double[] getExteriorArchCoordinates(int axis) throws Exception;
	
	/** Sets this snake as the synthesis of multiple snake structures. */
	abstract public void aggregate(List<StructureSnake> snakes, int aggregationMode) throws Exception;
	
	/** Reorganizes allNodes using D, P, V, A points. */
	abstract public void reorganizeAllNodesFromDPVA(Point2D.Double D, Point2D.Double P, Point2D.Double V, Point2D.Double A) throws Exception;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public StructureSnake() {
		
		super();
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public StructureSnake(int imageWidth, int imageHeight){

		super();
		width_ = imageWidth;
		height_ = imageHeight;
	}

	// ----------------------------------------------------------------------------

	/** Copy constructor. */
	public StructureSnake(StructureSnake snake) {

		super();

		if (snake.allNodes_ != null) { // ok
//			allNodes_ = snake.allNodes_.clone(); // shallow copy!!!
			allNodes_ = deepCopySnake2DNodeArray(snake.allNodes_);
		}
		
		if (snake.eSplineFunc_ != null) // ok
			eSplineFunc_ = snake.eSplineFunc_.clone();
		
		width_ = snake.width_; // ok
		height_ = snake.height_; // ok

		N_ = snake.N_; // ok
		R_ = snake.R_; // ok
		NR_ = snake.NR_; // ok
		M0_ = snake.M0_; // ok

		initialized_ = snake.initialized_;
		canceledByUser_ = snake.canceledByUser_;

		si_ = new SplineInterpolator(); // it's not a clone but a brand new SI
	}

	// ----------------------------------------------------------------------------

	/** Clone operator. */
	@Override
	public StructureSnake clone() {

		return copy();
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to compute the energy of the snake. */
	@Override
	public double energy () {

		return 0;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to compute the gradient of the snake energy with respect to the snake-defining nodes. */
	@Override
	public Point2D.Double[] getEnergyGradient (){

		return null;
	}

	// ----------------------------------------------------------------------------

	/** This method provides an accessor to the snake-defining nodes. */
	@Override
	public Snake2DNode[] getNodes (){

		return allNodes_;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to monitor the status of the snake. */
	@Override
	public boolean isAlive (){

		return true;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to know if the initialization process has been executed. */
	public boolean isInitialized (){

		return initialized_;
	}

	// ----------------------------------------------------------------------------

	/** 
	 * If true, indicates that the user chose to interactively abort the processing of the snake.
	 * <p>
	 * Otherwise, if false, indicates that the dealings with the snake were terminated without user assistance.
	 */
	public boolean isCanceledByUser (){

		return canceledByUser_;
	}

	// ----------------------------------------------------------------------------

	/** This method is called when the methods Snake2DKeeper.interact(), Snake2DKeeper.interactAndOptimize(), and Snake2DKeeper.optimize() are about to terminate. */
	@Override
	public void updateStatus (boolean canceledByUser, boolean snakeDied, boolean optimalSnakeFound, java.lang.Double energy){

		canceledByUser_ = canceledByUser;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a deep copy of the given Snake2DNode array. */
	public static Snake2DNode[] deepCopySnake2DNodeArray(Snake2DNode[] ori) {
		
		if (ori == null)
			return null;
		
		// clone method of Point2D.Double works
		// has the clone method of Snake2DNode (extends Point2D.Double) been implemented ?
		Snake2DNode[] copy = new Snake2DNode[ori.length];
		for (int i = 0; i < copy.length; i++)
			copy[i] = (Snake2DNode)ori[i].clone();
		
		return copy;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a deep copy of the given Snake2DNode array or array. */
	public static Snake2DNode[][] deepCopySnake2DNodeArrayOfArray(Snake2DNode[][] ori) {
		
		if (ori == null)
			return null;
		
		// clone method of Point2D.Double works
		// has the clone method of Snake2DNode (extends Point2D.Double) been implemented ?
		
		Snake2DNode[][] copy = new Snake2DNode[ori.length][ori[0].length];
		Snake2DNode tmp = null;
		for (int i = 0; i < copy.length; i++) {
			for (int j = 0; j < copy[0].length; j++) {
				tmp = ori[i][j];
				if (tmp != null)
					copy[i][j] = (Snake2DNode)tmp.clone();
				else
					copy[i][j] = null;
			}
		}
		
		return copy;
	}

	// ============================================================================
	// PROTECTED METHODS

	/** Performs cubic natural spline interpolation. */
	protected PolynomialSplineFunction[] naturalCubicSplineInterpolator (Point2D.Double[] anchorPoints){

		PolynomialSplineFunction[] polynomialSplineFunctions = new PolynomialSplineFunction[2];
		double[] t = new double[M0_+1]; 
		double[] x = new double[M0_+1]; 
		double[] y = new double[M0_+1];

		for(int i=0; i<=M0_; i++){
			t[i] = i;
			x[i] = anchorPoints[i].x;
			y[i] = anchorPoints[i].y;
		}

		polynomialSplineFunctions[0] = (PolynomialSplineFunction) si_.interpolate(t, x);
		polynomialSplineFunctions[1] = (PolynomialSplineFunction) si_.interpolate(t, y);
		return polynomialSplineFunctions;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the mean points from the given points. */
	protected Snake2DNode averageNode(final Snake2DNode[] points) throws Exception {
		
		if (points == null)
			return null;

		double meanx = 0.;
		double meany = 0.;
		for (int i = 0; i < points.length; i++) { // throws an exception if one point is null
			meanx += points[i].x;
			meany += points[i].y;
		}
		
		return new Snake2DNode(meanx/points.length, meany/points.length);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * From the cloud of points given, returns the mean/mean+std/mean-std node respectively from
	 * the given center node. See comments for additional information.
	 * TODO: Use vectors to get ride of the angles
	 */
	public static Snake2DNode getNodeFromNodeCloud(final Snake2DNode[] points, final Snake2DNode center, int aggregationMode) throws Exception {
		
		if (points == null)
			throw new Exception("ERROR: Cloud of points is null.");
		if (center == null)
			throw new Exception("ERROR: Center point is null.");
		
		// computes the mean and std of the points along the x- and y-axis
		Double[] xs = new Double[points.length];
		Double[] ys = new Double[points.length];
		for (int i = 0; i < points.length; i++) {
			xs[i] = points[i].x;
			ys[i] = points[i].y;
		}
		Double[] meanxAndStdx = MathUtils.computeMeanAndStd(xs);
		Double[] meanyAndStdy = MathUtils.computeMeanAndStd(ys);
		
		// creates the mean point from the cloud and returns it if the aggregation mode is MEAN
		Snake2DNode controlPoint = new Snake2DNode(meanxAndStdx[0], meanyAndStdy[0]);
		if (aggregationMode == AGGREGATION_MEAN)
			return controlPoint;
		
		double stdx = meanxAndStdx[1];
		double stdy = meanyAndStdy[1];
		
		// ============================================================
		// VERSION 2: use the std of the distances |center - point|
//		Double[] centerToPointsLengths = new Double[points.length];
//		for (int i = 0; i < points.length; i++)
//			centerToPointsLengths[i] = center.distance(points[i]);
//		Double[] centerToPointsLengthsMeanAndStd = MathUtils.computeMeanAndStd(centerToPointsLengths);
		// ============================================================
		
		// defines a reference point on the right side of the center which is on the same horizontal line
		// this point is then used as a reference to compute angles
		Snake2DNode ref = new Snake2DNode(center.x + 1, center.y);
		
		// angle between mean point - center - reference points
		double alpha = MathUtils.positveAngleBetween(controlPoint, center, ref);
		
		// the sign of cos and sin for this angle and in the four cadrans is
		// -- / +-
		// -+ / ++
		double cosAlpha = Math.cos(Math.toRadians(alpha));
		double sinAlpha = Math.sin(Math.toRadians(alpha));
		
		// the signs can be used to know if stdx or stdy must be added or subtracted
		// (we are in image space)
		Snake2DNode stdxNode = (Snake2DNode)controlPoint.clone();
		Snake2DNode stdyNode = (Snake2DNode)controlPoint.clone();
		stdxNode.x += MathUtils.sign(cosAlpha)*stdx;
		stdyNode.y -= MathUtils.sign(sinAlpha)*stdy;
		
		// computes an extension of the mean point to use as reference for future
		// angle measurements
		double dx = 1.1*(controlPoint.x - center.x) + center.x; // 2 is taken arbitrarily
		double dy = 1.1*(controlPoint.y - center.y) + center.y;
		Snake2DNode extendedControlPoint = new Snake2DNode(dx, dy);
		
		// compute angles extendedControlPoint - controlPoint - stdxNode
		// compute angles extendedControlPoint - controlPoint - stdyNode
		double stdxAngle = MathUtils.positveAngleBetween(extendedControlPoint, controlPoint, stdxNode);
		double stdyAngle = MathUtils.positveAngleBetween(extendedControlPoint, controlPoint, stdyNode);
		
		// the smallest angle can be use to project directly the associated std
		// the other is projected using 90-angle
		double stdxProj = 0.;
		double stdyProj = 0.;
		if (stdxAngle <= stdyAngle) {
			stdxProj = stdx * Math.cos(Math.toRadians(stdxAngle));
			stdyProj = stdy * Math.cos(Math.toRadians(90-stdxAngle));
		} else {
			stdxProj = stdx * Math.cos(Math.toRadians(90-stdyAngle));
			stdyProj = stdy * Math.cos(Math.toRadians(stdyAngle));
		}
	
		// value to add to the control point in the direction defined by center -> control point
		double centerToControlPointLength = Math.sqrt(Math.pow(controlPoint.x - center.x, 2) + Math.pow(controlPoint.y - center.y, 2));
		double stdLength = (stdxProj + stdyProj); // looks like version 2, do not use /2.
		
		// ============================================================
		// VERSION 2
//		stdLength = centerToPointsLengthsMeanAndStd[1];
		// ============================================================
		
		if (aggregationMode == AGGREGATION_MEAN_PLUS_STD) {
			double controlPointPlusStdX = center.x + (controlPoint.x - center.x) * ((centerToControlPointLength + stdLength) / centerToControlPointLength);
			double controlPointPlusStdY = center.y + (controlPoint.y - center.y) * ((centerToControlPointLength + stdLength) / centerToControlPointLength);
			return new Snake2DNode(controlPointPlusStdX, controlPointPlusStdY);
		} else if (aggregationMode == AGGREGATION_MEAN_MINUS_STD) {
			double controlPointMinusStdX = center.x + (controlPoint.x - center.x) * ((centerToControlPointLength - stdLength) / centerToControlPointLength);
			double controlPointMinusStdY = center.y + (controlPoint.y - center.y) * ((centerToControlPointLength - stdLength) / centerToControlPointLength);
			return new Snake2DNode(controlPointMinusStdX, controlPointMinusStdY);
		} else
			throw new Exception("ERROR: Unknown aggregation mode.");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Helper method that returns an Snake2DNode[] containing the ith node from each snake.
	 * allNodes_ must have the same length in every snake to make sense.
	 */
	public static Snake2DNode[] getNodes(final List<StructureSnake> snakes, int nodeIndex) throws Exception {
		
		if (snakes == null)
			return null;
		
		Snake2DNode[] nodes = new Snake2DNode[snakes.size()];
		// throws an excpetion if one snake is null
		for (int i = 0; i < snakes.size(); i++)
			nodes[i] = snakes.get(i).getNodes()[nodeIndex];
		
		return nodes;
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public void setNumControlPointsPerSegment(int numPoints) { M0_ = numPoints; }
	public int getNumControlPointsPerSegment() { return M0_; }
}