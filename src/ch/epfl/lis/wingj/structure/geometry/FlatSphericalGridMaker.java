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

package ch.epfl.lis.wingj.structure.geometry;

import java.awt.geom.Point2D;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureSnake;
import ch.epfl.lis.wingj.structure.geometry.Grid;

/**
 * Implements methods to build a flat spherical grid used to generate
 * 2D expression maps in WingJ.
 * <p>
 * Requires a Structure object that provides an external contour that will match a circle
 * and two boundaries that will match the horizontal and vertical axes inside the circle.
 * 
 * @version February 25, 2013
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class FlatSphericalGridMaker {

	/** Structure object. */
	private Structure structure_ = null;
	
	/** Auxiliary natural cubic spline interpolator. */
	private SplineInterpolator si_ = null;
	
	/** Smallest float value considered. */
	private static double TINY = (double)Float.intBitsToFloat((int)0x33FFFFFF);

	// ============================================================================
	// PRIVATE METHODS

	/** Initialization. */
	private void initiliaze() {

		si_ = new SplineInterpolator();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the trace of one axis of the structure. */
	private Point2D.Double[] computeAxisTrace(int nPoints, Point2D.Double wPouchCenterAnchorPoint,
			Point2D.Double[] intermediateAnchorPoints, Point2D.Double externalContourAnchorPoint) throws Exception {
		
		StructureSnake snake = structure_.getStructureSnake();
		
		int M0 = snake.getNumControlPointsPerSegment();
		
		Point2D.Double[] anchorPoints = new Point2D.Double[M0+1];
		anchorPoints[0] = externalContourAnchorPoint;
		for(int i=1; i<M0; i++){
			anchorPoints[i] = intermediateAnchorPoints[i-1];
		}
		anchorPoints[M0] = wPouchCenterAnchorPoint;
		
		PolynomialSplineFunction[] psf = naturalCubicSplineInterpolator(anchorPoints);

		Point2D.Double[] axis = new Point2D.Double[nPoints];
		for(int i=0; i<nPoints; i++){
			axis[i] = new Point2D.Double(psf[0].value(M0*(1.0-(double)i/(double)(nPoints-1))), psf[1].value(M0*(1.0-(double)i/(double)(nPoints-1))));
		}
		return axis;
	}

	// ----------------------------------------------------------------------------

	/** Performs cubic natural spline interpolation. */
	private PolynomialSplineFunction[] naturalCubicSplineInterpolator (Point2D.Double[] anchorPoints){

		StructureSnake snake = structure_.getStructureSnake();
		
		int M0 = snake.getNumControlPointsPerSegment();
		
		PolynomialSplineFunction[] polynomialSplineFunctions = new PolynomialSplineFunction[2];
		double[] t = new double[M0+1]; 
		double[] x = new double[M0+1]; 
		double[] y = new double[M0+1]; 

		for(int i=0; i<=M0; i++){
			t[i] = i;
			x[i] = anchorPoints[i].x;
			y[i] = anchorPoints[i].y;
		}
		
		polynomialSplineFunctions[0] = (PolynomialSplineFunction) si_.interpolate(t, x);
		polynomialSplineFunctions[1] = (PolynomialSplineFunction) si_.interpolate(t, y);
		return polynomialSplineFunctions;
	}

	// ----------------------------------------------------------------------------
	
	/** Performs an affine transformation on a curve such that the transformed curve interpolates p, q and r. */
	private Point2D.Double[] morphAxis(Point2D.Double[] topAxis, Point2D.Double[] bottomAxis, Point2D.Double p, Point2D.Double q, Point2D.Double r){
		
		int nPoints = topAxis.length;
		Point2D.Double p0 = topAxis[nPoints-1];
		Point2D.Double q0 = topAxis[0];
		Point2D.Double r0 = bottomAxis[nPoints-1];

		double Deltapq0X = p0.x-q0.x;
		double Deltapq0Y = p0.y-q0.y;
		double Deltapr0X = p0.x-r0.x;
		double Deltapr0Y = p0.y-r0.y;
		double DeltapqX = p.x-q.x;
		double DeltapqY = p.y-q.y;
		double DeltaprX = p.x-r.x;
		double DeltaprY = p.y-r.y;
		
		double det = Deltapr0Y*Deltapq0X-Deltapr0X*Deltapq0Y;
		if(Math.abs(det)<TINY){
			WJSettings.log(""+p0+""+q0+""+r0+" are collinear.");
			WJSettings.log("Reshaping structure.");
			p0.x--;
			Deltapq0X = p0.x-q0.x;
			Deltapq0Y = p0.y-q0.y;
			Deltapr0X = p0.x-r0.x;
			Deltapr0Y = p0.y-r0.y;
		}
		
		double a11 = (Deltapr0Y*DeltapqX-Deltapq0Y*DeltaprX)/det;
		double a12 = (-Deltapr0X*DeltapqX+Deltapq0X*DeltaprX)/det;
		double a21 = (Deltapr0Y*DeltapqY-Deltapq0Y*DeltaprY)/det;
		double a22 = (-Deltapr0X*DeltapqY+Deltapq0X*DeltaprY)/det;
		
		double b1 = (p.x+q.x-(a11*(p0.x+q0.x)+a12*(p0.y+q0.y)))/2.0;
		double b2 = (p.y+q.y-(a21*(p0.x+q0.x)+a22*(p0.y+q0.y)))/2.0;
		
		Point2D.Double[] morphedAxis = new Point2D.Double[2*nPoints-1];
		
		int cont = 0;
		for(int i=0; i<nPoints; i++, cont++){
			Point2D.Double point = topAxis[nPoints-i-1];
			morphedAxis[cont] = new Point2D.Double(a11*point.x+a12*point.y+b1, a21*point.x+a22*point.y+b2);
		}
		for(int i=1; i<nPoints; i++, cont++){
			Point2D.Double point = bottomAxis[i];
			morphedAxis[cont] = new Point2D.Double(a11*point.x+a12*point.y+b1, a21*point.x+a22*point.y+b2);
		}
		
		return morphedAxis;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reverses the order of a list of points within an array. */
	private Point2D.Double[] reverse(Point2D.Double[] curve) {
		
		Point2D.Double[] reverserCurve = new Point2D.Double[curve.length];
		for(int i=0; i<curve.length; i++){
			reverserCurve[i] = curve[curve.length-i-1];
		}
		return reverserCurve;
	}

	// ----------------------------------------------------------------------------

	/** Parameterizes a curve to arc length parameterization with a given number of points. */
	private Point2D.Double[] arcLengthResampling(Point2D.Double[] curve, int nPoints){
		
		int nSamples = curve.length;
		double[] arcLength = new double[nSamples];
	
		for(int i=1; i<nSamples; i++){
			arcLength[i] = arcLength[i-1] + curve[i].distance(curve[i-1]);
		}
		
		Point2D.Double[] resampledCurve = new Point2D.Double[nPoints];
		double delta = arcLength[nSamples-1]/(nPoints-1);
		int index = 0;
		for(int i=0; i<nPoints; i++){
			double t = delta*i;
			boolean found = false;
			for(; index<(nSamples-1) && !found; index++){
				if(arcLength[index]<=t && arcLength[index+1]>=t){
					found = true;
				}
			}
			index--;
			resampledCurve[i] = new Point2D.Double(
					((arcLength[index+1]-t)*curve[index].x+(t-arcLength[index])*curve[index+1].x)/(arcLength[index+1]-arcLength[index]),
					((arcLength[index+1]-t)*curve[index].y+(t-arcLength[index])*curve[index+1].y)/(arcLength[index+1]-arcLength[index]));
		}
		return resampledCurve;
	}

	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public FlatSphericalGridMaker(Structure structure) {
		
		structure_ = structure;
		initiliaze();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Computes the expression map using the as reference the warped axis of the wing pouch.
	 * If projectionMode is WJSettings.DV_BOUNDARY, the D/V boundary corresponds to the equator
	 * of the mapping. If projectionMode is WJSettings.AP_BOUNDARY, the A/P boundary corresponds
	 * to the equator of the mapping.
	 */
	public Grid generateSphereLikeGrid(int projectionMode, int nPoints) throws Exception {
		
		if (structure_ == null) {
			WJSettings.log("ERROR: Structure is null.");
			throw new Exception("ERROR: The expression map cannot be built (structure is missing).\n" +
				"Please perform a complete sutrcture detection or import a structure.");
		}
		if (structure_.getAPBoundary() == null) {
			WJSettings.log("ERROR: A/P boundary is null.");
			throw new Exception("ERROR: The expression map cannot be built (A/P boundary is missing).\n" +
				"Please perform a complete sutrcture detection or import a structure.");
		}
		if (structure_.getDVBoundary() == null) {
			WJSettings.log("ERROR: D/V boundary is null.");
			throw new Exception("ERROR: The expression map cannot be built (D/V boundary is missing).\n" +
				"Please perform a complete sutrcture detection or import a structure.");
		}
		if (structure_.getStructureSnake() == null) {
			WJSettings.log("ERROR: Snake structure is null.");
			throw new Exception("ERROR: The expression map cannot be built (structure snake is missing).\n" +
				"Please perform a complete sutrcture detection or import a structure.");
		}
		
		StructureSnake snake = structure_.getStructureSnake();
		
		int M0 = snake.getNumControlPointsPerSegment();
		Point2D.Double[] wSnakeNodes = snake.getNodes();
		Point2D.Double wPouchCenterNode = wSnakeNodes[8*M0-4];
		Point2D.Double[][] axisIntermediateAnchorPoints = new Point2D.Double[4][M0-1];
		
		int cont = 4*M0;
		for(int branchNumber=0; branchNumber<4; branchNumber++){
			for(int i=0; i<(M0-1); i++){
				axisIntermediateAnchorPoints[branchNumber][i] = wSnakeNodes[cont];
				cont++;
			}
		}

		Point2D.Double[] axis1 = arcLengthResampling(computeAxisTrace(nPoints, wPouchCenterNode, 
				axisIntermediateAnchorPoints[0], snake.getAnchorPointOnContour(0)), nPoints);
		Point2D.Double[] axis2 = arcLengthResampling(computeAxisTrace(nPoints, wPouchCenterNode, 
				axisIntermediateAnchorPoints[1], snake.getAnchorPointOnContour(1)), nPoints);
		Point2D.Double[] axis3 = arcLengthResampling(computeAxisTrace(nPoints, wPouchCenterNode, 
				axisIntermediateAnchorPoints[2], snake.getAnchorPointOnContour(2)), nPoints);
		Point2D.Double[] axis4 = arcLengthResampling(computeAxisTrace(nPoints, wPouchCenterNode, 
				axisIntermediateAnchorPoints[3], snake.getAnchorPointOnContour(3)), nPoints);
		
		//identify axis
		Point2D.Double axis1_LastExtreme = axis1[nPoints-1];
		Point2D.Double axis2_LastExtreme = axis2[nPoints-1];
		Point2D.Double axis3_LastExtreme = axis3[nPoints-1];
		Point2D.Double axis4_LastExtreme = axis4[nPoints-1];

		Point2D.Double A = structure_.getDVBoundary().getFirstPoint();
		Point2D.Double D = structure_.getAPBoundary().getFirstPoint();
		
		double[] distanceToA = new double[4];
		distanceToA[0] = A.distance(axis1_LastExtreme);
		distanceToA[1] = A.distance(axis2_LastExtreme);
		distanceToA[2] = A.distance(axis3_LastExtreme);
		distanceToA[3] = A.distance(axis4_LastExtreme);
		
		Point2D.Double[] axisCA = null;
		Point2D.Double[] axisCP = null;
		if (distanceToA[0] < distanceToA[1] && distanceToA[0] < distanceToA[2] && distanceToA[0] < distanceToA[3]){
			//axis1_LastExtreme is A
			//axis3_LastExtreme is P
			axisCA = axis1;
			axisCP = axis3;
		}else if(distanceToA[1] < distanceToA[0] && distanceToA[1] < distanceToA[2] && distanceToA[1] < distanceToA[3]){
			//axis2_LastExtreme is A
			//axis4_LastExtreme is P
			axisCA = axis2;
			axisCP = axis4;
		}else if(distanceToA[2] < distanceToA[0] && distanceToA[2] < distanceToA[1] && distanceToA[2] < distanceToA[3]){
			//axis3_LastExtreme is A
			//axis1_LastExtreme is P
			axisCA = axis3;
			axisCP = axis1;
		}else if(distanceToA[3] < distanceToA[0] && distanceToA[3] < distanceToA[1] && distanceToA[3] < distanceToA[2]){
			//axis4_LastExtreme is A
			//axis2_LastExtreme is P
			axisCA = axis4;
			axisCP = axis2;
		}
		
		double[] distanceToD = new double[4];
		distanceToD[0] = D.distance(axis1_LastExtreme);
		distanceToD[1] = D.distance(axis2_LastExtreme);
		distanceToD[2] = D.distance(axis3_LastExtreme);
		distanceToD[3] = D.distance(axis4_LastExtreme);		
		
		Point2D.Double[] axisCD = null;
		Point2D.Double[] axisCV = null;
		if (distanceToD[0] < distanceToD[1] && distanceToD[0] < distanceToD[2] && distanceToD[0] < distanceToD[3]){
			//axis1_LastExtreme is D
			//axis3_LastExtreme is V
			axisCD = axis1;
			axisCV = axis3;
		}else if(distanceToD[1] < distanceToD[0] && distanceToD[1] < distanceToD[2] && distanceToD[1] < distanceToD[3]){
			//axis2_LastExtreme is D
			//axis4_LastExtreme is V
			axisCD = axis2;
			axisCV = axis4;
		}else if(distanceToD[2] < distanceToD[0] && distanceToD[2] < distanceToD[1] && distanceToD[2] < distanceToD[3]){
			//axis3_LastExtreme is D
			//axis1_LastExtreme is V
			axisCD = axis3;
			axisCV = axis1;
		}else if(distanceToD[3] < distanceToD[0] && distanceToD[3] < distanceToD[1] && distanceToD[3] < distanceToD[2]){
			//axis4_LastExtreme is D
			//axis2_LastExtreme is V
			axisCD = axis4;
			axisCV = axis2;
		}
		
		Point2D.Double[] arch1 = arcLengthResampling(snake.getExteriorArchCoordinates(0), nPoints);
		Point2D.Double[] arch2 = arcLengthResampling(snake.getExteriorArchCoordinates(1), nPoints);
		Point2D.Double[] arch3 = arcLengthResampling(snake.getExteriorArchCoordinates(2), nPoints);
		Point2D.Double[] arch4 = arcLengthResampling(snake.getExteriorArchCoordinates(3), nPoints);

		Point2D.Double[] archAD = null;
		if(axis1==axisCA && axis2==axisCD){
			archAD = arch1;
		}else if(axis2==axisCA && axis1==axisCD){
			archAD = reverse(arch1);
		}else if(axis2==axisCA && axis3==axisCD){
			archAD = arch2;
		}else if(axis3==axisCA && axis2==axisCD){
			archAD = reverse(arch2);
		}else if(axis3==axisCA && axis4==axisCD){
			archAD = arch3;
		}else if(axis4==axisCA && axis3==axisCD){
			archAD = reverse(arch3);
		}else if(axis4==axisCA && axis1==axisCD){
			archAD = arch4;
		}else if(axis1==axisCA && axis4==axisCD){
			archAD = reverse(arch4);
		}else{
			throw new Exception("ERROR: The expression map cannot be built: Impossible to determine the AD arch.\n" +
					"Please abort and try running the detection step-by-step (press button \"Step\").");
		}

		Point2D.Double[] archDP = null;
		if(axis1==axisCD && axis2==axisCP){
			archDP = arch1;
		}else if(axis2==axisCD && axis1==axisCP){
			archDP = reverse(arch1);
		}else if(axis2==axisCD && axis3==axisCP){
			archDP = arch2;
		}else if(axis3==axisCD && axis2==axisCP){
			archDP = reverse(arch2);
		}else if(axis3==axisCD && axis4==axisCP){
			archDP = arch3;
		}else if(axis4==axisCD && axis3==axisCP){
			archDP = reverse(arch3);
		}else if(axis4==axisCD && axis1==axisCP){
			archDP = arch4;
		}else if(axis1==axisCD && axis4==axisCP){
			archDP = reverse(arch4);
		}else{
			throw new Exception("ERROR: The expression map cannot be built. Impossible to determine the DP arch.\n" +
					"Please abort and try running the detection step-by-step (press button \"Step\").");
		}

		Point2D.Double[] archPV = null;
		if(axis1==axisCP && axis2==axisCV){
			archPV = arch1;
		}else if(axis2==axisCP && axis1==axisCV){
			archPV = reverse(arch1);
		}else if(axis2==axisCP && axis3==axisCV){
			archPV = arch2;
		}else if(axis3==axisCP && axis2==axisCV){
			archPV = reverse(arch2);
		}else if(axis3==axisCP && axis4==axisCV){
			archPV = arch3;
		}else if(axis4==axisCP && axis3==axisCV){
			archPV = reverse(arch3);
		}else if(axis4==axisCP && axis1==axisCV){
			archPV = arch4;
		}else if(axis1==axisCP && axis4==axisCV){
			archPV = reverse(arch4);
		}else{
			throw new Exception("ERROR: The expression map cannot be built. Impossible to determine the PV arch.\n" +
					"Please abort and try running the detection step-by-step (press button \"Step\").");
		}
		
		Point2D.Double[] archVA = null;
		if(axis1==axisCV && axis2==axisCA){
			archVA = arch1;
		}else if(axis2==axisCV && axis1==axisCA){
			archVA = reverse(arch1);
		}else if(axis2==axisCV && axis3==axisCA){
			archVA = arch2;
		}else if(axis3==axisCV && axis2==axisCA){
			archVA = reverse(arch2);
		}else if(axis3==axisCV && axis4==axisCA){
			archVA = arch3;
		}else if(axis4==axisCV && axis3==axisCA){
			archVA = reverse(arch3);
		}else if(axis4==axisCV && axis1==axisCA){
			archVA = arch4;
		}else if(axis1==axisCV && axis4==axisCA){
			archVA = reverse(arch4);
		}else{
			throw new Exception("ERROR: The expression map cannot be built. Impossible to determine the VA arch.\n" +
					"Please abort and try running the detection step-by-step (press button \"Step\").");
		}
		
		int gridLength = 2*nPoints-1;
		Grid grid = new Grid(gridLength);
		if (projectionMode == WJSettings.BOUNDARY_AP){
			// A/P boundary is the equator 
			// DV axis is the equator 
			for (int i = 0; i < nPoints; i++) {
				Point2D.Double top = archAD[i];
				Point2D.Double middle = axisCA[nPoints-1-i];
				Point2D.Double bottom = archVA[nPoints-1-i];
				grid.setCoordinatesRow(i, morphAxis(axisCD, axisCV, top, middle, bottom));
			}
			for (int i = nPoints; i < (2*nPoints - 1); i++) {
				Point2D.Double top = archDP[i-nPoints];
				Point2D.Double middle = axisCP[i-nPoints];
				Point2D.Double bottom = archPV[2*nPoints-1-i];
				grid.setCoordinatesRow(i, morphAxis(axisCD, axisCV, top, middle, bottom));
			}
		}else if (projectionMode == WJSettings.BOUNDARY_DV){
			// D/V boundary is the equator 
			// AP axis is the equator 
			for (int i = 0; i < nPoints; i++) {
				Point2D.Double top = archDP[i];
				Point2D.Double middle = axisCD[nPoints-1-i];
				Point2D.Double bottom = archAD[nPoints-1-i];
				grid.setCoordinatesRow(i, morphAxis(axisCP, axisCA, top, middle, bottom));
			}
			for (int i = nPoints; i < (2*nPoints - 1); i++) {
				Point2D.Double top = archPV[i-nPoints];
				Point2D.Double middle = axisCV[i-nPoints];
				Point2D.Double bottom = archVA[2*nPoints-1-i];
				grid.setCoordinatesRow(i, morphAxis(axisCP, axisCA, top, middle, bottom));
				
			}
		}else{
			throw new Exception("ERROR: The expression map cannot be built. Invalid reference axis.\n" +
					"Please, contact the developers.");
		}
		return grid;
	}
}