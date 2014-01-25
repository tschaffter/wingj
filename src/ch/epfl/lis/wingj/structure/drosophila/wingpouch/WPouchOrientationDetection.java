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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.geometry.Segment;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;

/** 
 * Identifies the orientation of the wing (A/P and D/V directions).
 * 
 * @version October 24, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchOrientationDetection extends StructureDetectionModule {
	
	/** First axis. */
	protected Boundary axis1_ = null;
	/** Second axis. */
	protected Boundary axis2_ = null;
	/** Four compartment. */
	protected List<Compartment> compartments_ = null;
	/** Wing pouch contour. */
	protected Compartment contour_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WPouchOrientationDetection() {};
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchOrientationDetection(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector);
		description_ = "Inferring the orientation of the wing pouch.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchOrientationDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Inferring the orientation of the wing pouch.";
	}
	
	// ----------------------------------------------------------------------------

	/** Identifies the polarity of the wing disc. */
	@Override
	public void run() throws Exception {
		
//		WJSettings.log("Calling orientation inference method based on morphology.");
		
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		WPouchStructure structure = (WPouchStructure)detector.getStructure();
		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
		
//		if (!structure.isStructureKnown())
//			throw new Exception("INFO: Orientation cannot be inferred if structure is not defined.");
		
		axis1_ = snake.getBoundary(0);
		axis2_ = snake.getBoundary(1);
		
		compartments_ = new ArrayList<Compartment>();
		compartments_.add(snake.getCompartment(0));
		compartments_.add(snake.getCompartment(1));
		compartments_.add(snake.getCompartment(2));
		compartments_.add(snake.getCompartment(3));
		
		contour_ = snake.getWPouchContour();
		
		Boundary dv = null;
		Boundary ap = null;

		// ============================================================================
		// BEGIN WING ORIENTATION INFERENCE
		
		// Detection of the identity and orientation of the A/P boundary (defined by point D and V)
		if ((ap = findAPBoundary(axis1_, axis2_, structure.discCenter_)) != null) {
			
			if (ap == axis1_)
				dv = axis2_;
			else
				dv = axis1_;
		}
		else {
			// For now, it's impossible to arrive here and it's fine
			
//			// So set the longest boundary as the AP axis (but we don't know which point is P and which one is A!)
//			dv = getLongestBoundary();
//			if (dv == axis1_) ap = axis2_;
//			else ap = axis1_;
//			
//			// If here, ask the user to check the result of the identification
		}
		
		// Now we have the A/P boundary
		// Set the D/V boundary (defined by point A and P)
		setDVBoundaryOrientation(ap, dv, structure.center_);
		
		// Knowing the orientation of the D/V and A/P boundaries, the identity of the compartments is inferred
		// If everything went well, compartments_ will be empty after calling findCompartments()
		List<Compartment> compartments = findCompartments(ap, dv, structure.center_, compartments_);
		
		// ============================================================================
		// END WING ORIENTATION INFERENCE
		
		if (structure.center_ == null)
			throw new Exception("ERROR: Unable to identify the structure center.");
		if (ap == null)
			throw new Exception("ERROR: Unable to identify the A/P boundary.");
		if (dv == null)
			throw new Exception("ERROR: Unable to identify the D/V boundary.");
		if (compartments == null || compartments.size() != 4)
			throw new Exception("ERROR: Unable to identify the four compartments.");
		
		// Finally, save to pouch_ :)
		// Update: pouch_ became structure_
		structure.setCenter(structure.center_);
		structure.setAPBoundary(ap);
		structure.setDVBoundary(dv);
		Compartment da = compartments.get(0);
		Compartment dp = compartments.get(1);
		Compartment va = compartments.get(2);
		Compartment vp = compartments.get(3);
		structure.setDACompartment(da);
		structure.setDPCompartment(dp);
		structure.setVACompartment(va);
		structure.setVPCompartment(vp);
		
		structure.reset();
		for (int i = 0; i < contour_.npoints; i++) // pouch contour
			structure.addPoint(contour_.xpoints[i], contour_.ypoints[i]);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Sets the orientation of the structure from the snake and the center of mass
	 * of each compartment. The centers of mass must be arranged in this order:
	 * DA, DP, VA, VP.
	 */
	public void setOrientationFromCompartmentCenterOfMass(WPouchStructure structure, Point2D.Double[] coms) throws Exception {
		
//		WJSettings.log("Calling orientation inference method based on known compartments identity.");
		
		if (coms.length < 4)
			throw new Exception("ERROR: The orientation inference requires four compartment centers of mass.");
		
		for (int i = 0; i < coms.length; i++) {
			if (coms[i] == null)
				throw new Exception("ERROR: At least one center of mass is null (com index=" + i + ").");
		}
		
		// get boundaries and compartments from the snake
		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
		
		axis1_ = snake.getBoundary(0);
		axis2_ = snake.getBoundary(1);
		
		compartments_ = new ArrayList<Compartment>();
		compartments_.add(snake.getCompartment(0));
		compartments_.add(snake.getCompartment(1));
		compartments_.add(snake.getCompartment(2));
		compartments_.add(snake.getCompartment(3));
		
		// infer the identity of each compartment (see order in method description)
		for (int i = 0; i < compartments_.size(); i++) {
			if (compartments_.get(i).contains((float)coms[0].x, (float)coms[0].y))
				structure.setDACompartment(compartments_.get(i));
			// I could remove the compartment from the list...
		}
		
		for (int i = 0; i < compartments_.size(); i++) {
			if (compartments_.get(i).contains((float)coms[1].x, (float)coms[1].y))
				structure.setDPCompartment(compartments_.get(i));
			// I could remove the compartment from the list...
		}
		
		for (int i = 0; i < compartments_.size(); i++) {
			if (compartments_.get(i).contains((float)coms[2].x, (float)coms[2].y))
				structure.setVACompartment(compartments_.get(i));
			// I could remove the compartment from the list...
		}
		
		for (int i = 0; i < compartments_.size(); i++) {
			if (compartments_.get(i).contains((float)coms[3].x, (float)coms[3].y))
				structure.setVPCompartment(compartments_.get(i));
			// I could remove the compartment from the list...
		}
		
//		// infer the identity of the boundaries
//		Boundary dv = null;
//		Boundary ap = null;

//		// Detection of the identity and orientation of the A/P boundary (defined by point D and V)
//		if ((ap = findAPBoundary(axis1_, axis2_, structure.discCenter_)) != null) {
//			
//			if (ap == axis1_)
//				dv = axis2_;
//			else
//				dv = axis1_;
//		}
//		else {
//			// For now, it's impossible to arrive here and it's fine
//			
////			// So set the longest boundary as the AP axis (but we don't know which point is P and which one is A!)
////			dv = getLongestBoundary();
////			if (dv == axis1_) ap = axis2_;
////			else ap = axis1_;
////			
////			// If here, ask the user to check the result of the identification
//		}
//		
//		// Now we have the A/P boundary
//		// Set the D/V boundary (defined by point A and P)
//		setDVBoundaryOrientation(ap, dv, structure.center_);
//		
//		structure.setAPBoundary(ap);
//		structure.setDVBoundary(dv);
		
		// draw a segment from DA centroid to DP centroid
		Segment DaCom2DpCom = new Segment(structure.getDACompartment().centroid(), structure.getDPCompartment().centroid());
		// draw a segment from DA centroid to VA centroid
		Segment DaCom2VaCom = new Segment(structure.getDACompartment().centroid(), structure.getVACompartment().centroid());
		
		// build four axes from pouch center to extremities of the two axes
		Segment s1 = new Segment(snake.getWPouchCenter(), axis1_.getFirstPoint());
		Segment s2 = new Segment(snake.getWPouchCenter(), axis1_.getLastPoint());
		Segment s3 = new Segment(snake.getWPouchCenter(), axis2_.getFirstPoint());
		Segment s4 = new Segment(snake.getWPouchCenter(), axis2_.getLastPoint());
		
		// set A/P boundary
		if (DaCom2DpCom.intersection(s1) != null) { // first point of axis1 is D
			structure.setAPBoundary(axis1_);
		}
		else if (DaCom2DpCom.intersection(s2) != null) { // last point of axis1 is D
			Boundary b = axis1_.copy();
			b.reverse();
			structure.setAPBoundary(b);
//			structure.reverseAPAxisDirection(); // !! also reverse the compartments !!
		}
		if (DaCom2DpCom.intersection(s3) != null) { // first point of axis2 is D
			structure.setAPBoundary(axis2_);
		}
		else if (DaCom2DpCom.intersection(s4) != null) { // last point of axis2 is D
			Boundary b = axis2_.copy();
			b.reverse();
			structure.setAPBoundary(b);
//			structure.reverseAPAxisDirection(); // !! also reverse the compartments !!
		}
		
		// set D/V boundary
		if (DaCom2VaCom.intersection(s1) != null) { // first point of axis1 is A
			structure.setDVBoundary(axis1_);
		}
		else if (DaCom2VaCom.intersection(s2) != null) { // last point of axis1 is P
			Boundary b = axis1_.copy();
			b.reverse();
			structure.setDVBoundary(b);
//			structure.reverseDVAxisDirection(); // !! also reverse the compartments !!
		}
		if (DaCom2VaCom.intersection(s3) != null) { // first point of axis2 is A
			structure.setDVBoundary(axis2_);
		}
		else if (DaCom2VaCom.intersection(s4) != null) { // last point of axis2 is P
			Boundary b = axis2_.copy();
			b.reverse();
			structure.setDVBoundary(b);
//			structure.reverseDVAxisDirection(); // !! also reverse the compartments !!
		}
		
		// set contour (XXX: not sure this is still required)
		contour_ = snake.getWPouchContour();
		structure.reset();
		for (int i = 0; i < contour_.npoints; i++) // pouch contour
			structure.addPoint(contour_.xpoints[i], contour_.ypoints[i]);
		
//		WJSettings.log("Found da com: " + structure.getDACompartment().centroid());
//		WJSettings.log("Found dp com: " + structure.getDPCompartment().centroid());
//		WJSettings.log("Found va com: " + structure.getVACompartment().centroid());
//		WJSettings.log("Found vp com: " + structure.getVPCompartment().centroid());
	}
	
	// ============================================================================
	// PRIVATE METHODS
	
	/**
   	 * Identifies the A/P boundary (= DV axis). First, get the four extremity Points of the two boundary.
   	 * Also, compute the centroid of the wing disc (not the wing pouch!). Among the four points, the point D is the
   	 * closest to the COM. The DV axis is therefore identified. If the point D correspond to the last point
   	 * of the DV axis, inverse the orientation of the DV axis.
   	 */
   	private Boundary findAPBoundary(Boundary a, Boundary b, Point2D.Double N) throws Exception {
   		
   		// Get all required points
   		Point2D.Double A1 = a.getFirstPoint();
   		Point2D.Double A2 = a.getLastPoint();
   		Point2D.Double B1 = b.getFirstPoint();
   		Point2D.Double B2 = b.getLastPoint();
   		
   		// Compute distances to wing disc centroid, N
   		double dA1N = Segment.distance(A1, N);
   		double dA2N = Segment.distance(A2, N);
   		double dB1N = Segment.distance(B1, N);
   		double dB2N = Segment.distance(B2, N);
   		
   		if (dA1N <= dA2N && dA1N <= dB1N && dA1N <= dB2N) {
   			// A1 is likely to be the point D
   			return a;
   		}
   		else if (dA2N <= dA1N && dA2N <= dB1N && dA2N <= dB2N) {
   			// A2 is likely to be the point D but it must be the first point of the boundary
   			a.reverse();
   			return a;
   		}
   		else if (dB1N <= dA1N && dB1N <= dA2N && dB1N <= dB2N) {
   			// B1 is likely to be the point D
   			return b;
   		}
   		else if (dB2N <= dA1N && dB2N <= dA2N && dB2N <= dB1N) {
   			// B2 is likely to be the point D but it must be the first point of the boundary
   			b.reverse();
   			return b;
   		}
   		
   		// Must never arrive here
   		return null;
   	}
	
   	// ----------------------------------------------------------------------------
   	
   	/** Sets the correct orientation of the D/V boundary. */
   	private void setDVBoundaryOrientation(Boundary ap, Boundary dv, Point2D.Double M) throws Exception {
   		
   		// Get all required points
   		Point2D.Double D = ap.getFirstPoint();
   		Point2D.Double V = ap.getLastPoint();
   		Point2D.Double K = dv.getFirstPoint();
   		Point2D.Double L = dv.getLastPoint();

   		// Construct segments
   		Segment DV = new Segment(D, V);
   		Segment KM = new Segment(K, M);
   		Segment LM = new Segment(L, M);
   		
   		// If KM intersects DV, K is the point A and the orientation of the boundary dv is correct.
   		// If LM intersects DV, L is the point A and the orientation of the boundary dv must be reversed.
   		if (KM.intersection(DV) != null) {
   			// nothing to do
   		}
   		else if (LM.intersection(DV) != null)
   			dv.reverse();
   		else
   			throw new Exception("ERROR: Unable to find the orientation of the D/V boundary.");
   	}
	
   	// ----------------------------------------------------------------------------
   	
   	/** Returns list containing the DA, DP, VA, VP compartments. */
   	private List<Compartment> findCompartments(Boundary ap, Boundary dv, Point2D.Double M, List<Compartment> C) throws Exception {
   		
   		WJSettings.log("Identifying four compartments polarity");
   		List<Compartment> compartments = new ArrayList<Compartment>();
   		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		WPouchStructure structure = (WPouchStructure)detector.getStructure();
   		
   		// Get all required points
   		Point2D.Double D = ap.getFirstPoint();
   		Point2D.Double V = ap.getLastPoint();
   		Point2D.Double A = dv.getFirstPoint();
   		Point2D.Double P = dv.getLastPoint();
   		
   		WJSettings.log("Point D: " + D);
   		WJSettings.log("Point V: " + V);
   		WJSettings.log("Point A: " + A);
   		WJSettings.log("Point P: " + P);
   		
   		WJSettings.log("Wing disc center: " + structure.discCenter_);
   		WJSettings.log("Wing pouch center: " + M);
   		
   		// Before going further, it is important for the following algorithm that
   		// the compartments listed in C are in a circular order and not in a Z order.
   		// For example: DA, DP, VP, VA are in circular order
   		// For example: DA, DP, VA, VP are in Z order
   		setCompartmentsInCircularOrder(C);
   		
   		List<Point2D.Double> centroids = new ArrayList<Point2D.Double>();
   		centroids.add(C.get(0).centroid());
   		centroids.add(C.get(1).centroid());
   		centroids.add(C.get(2).centroid());
   		centroids.add(C.get(3).centroid());
   		
   		for (int i = 0; i < centroids.size(); i++)
   			WJSettings.log("Compartment " + i + " centroid: " + centroids.get(i));
   		
   		// Construct segments
   		Segment DM = new Segment(D, M);
   		Segment VM = new Segment(V, M);
   		Segment AM = new Segment(A, M);
   		Segment PM = new Segment(P, M);
   		
   		int N = C.size();   		
   		// Find compartment DA
   		for (int i = 0; i < N; i++) {
   			Segment s1 = new Segment(centroids.get(i), centroids.get((i+1)%N));
   			Segment s2 = new Segment(centroids.get((i+1)%N), centroids.get((i+2)%N));
   			
   			if ((AM.intersection(s1) != null && DM.intersection(s2) != null) || (AM.intersection(s2) != null && DM.intersection(s1) != null)) {
//   				WJSettings.log("DA compartment found (compartment " + i + ")");
   				compartments.add(C.get((i+1)%N));
   				break;
   			}
   		}
   		
   		// Find compartment DP
   		for (int i = 0; i < N; i++) {
   			Segment s1 = new Segment(centroids.get(i), centroids.get((i+1)%N));
   			Segment s2 = new Segment(centroids.get((i+1)%N), centroids.get((i+2)%N));
   			
   			if ((DM.intersection(s1) != null && PM.intersection(s2) != null) || (DM.intersection(s2) != null && PM.intersection(s1) != null)) {
//   				WJSettings.log("DP compartment found (compartment " + i + ")");
   				compartments.add(C.get((i+1)%N));
   				break;
   			}
   		}
   		
   		// Find compartment VA
   		for (int i = 0; i < N; i++) {
   			Segment s1 = new Segment(centroids.get(i), centroids.get((i+1)%N));
   			Segment s2 = new Segment(centroids.get((i+1)%N), centroids.get((i+2)%N));
   			
   			if ((VM.intersection(s1) != null && AM.intersection(s2) != null) || (VM.intersection(s2) != null && AM.intersection(s1) != null)) {
//   				WJSettings.log("VA compartment found (compartment " + i + ")");
   				compartments.add(C.get((i+1)%N));
   				break;
   			}
   		}
   		
   		// Find compartment VP
   		for (int i = 0; i < N; i++) {
   			Segment s1 = new Segment(centroids.get(i), centroids.get((i+1)%N));
   			Segment s2 = new Segment(centroids.get((i+1)%N), centroids.get((i+2)%N));
   			
   			if ((PM.intersection(s1) != null && VM.intersection(s2) != null) || (PM.intersection(s2) != null && VM.intersection(s1) != null)) {
//   				WJSettings.log("VP compartment found (compartment " + i + ")");
   				compartments.add(C.get((i+1)%N));
   				break;
   			}
   		}
   		
   		// check that compartments contains four compartments
   		if (compartments.size() != N) {
   			WJMessage.showMessage("Unable to find the identity of the compartments.\n" +
   								  "Please check the orientation of the structure model.", WJMessage.WARNING_PREFIX);
   			// empty the identified compartments and put all the compartment together
   			compartments.clear();
   			compartments.addAll(C);
   		}
   		
   		// check that compartments contains unique compartment
   		outerloop : for (int i = 0; i < N; i++) {
   			for (int j = i; j < N; j++) {
   				if (i != j && compartments.get(i) == compartments.get(j)) {
   		   			WJMessage.showMessage("Unable to find the identity of the compartments.\n" +
								  		  "Please check the orientation of the structure model.", WJMessage.WARNING_PREFIX);
   		   			break outerloop;
   				}	
   			}
   		}
   		
   		return compartments;
   	}
	
   	// ----------------------------------------------------------------------------
   	
   	/**
   	 * Sets the given list of compartment in circular order. If the compartments are not in circular order,
   	 * the list is randomly shuffled until a circular order is found. There is tiny possibility that
   	 * the code stays stuck there that's why I'd prefer a deterministic approach. For now, it should
   	 * be good enough.
   	 */
   	private void setCompartmentsInCircularOrder(List<Compartment> compartments) throws Exception {
   		
   		int maxIters = 1000;
   		int iter = 0;
   		while (!compartmentsInCircularOrder(compartments) && iter < maxIters) {
	   		Random rand = new Random();
	   		Collections.shuffle(compartments, rand);
	   		iter++;
   		}
   		
   		if (iter >= maxIters)
   			throw new Exception("ERROR: Unable to list the compartments in circular order.");
   	}
   	
   	// ----------------------------------------------------------------------------
   	
   	/**
   	 * Tests if the compartments are listed in circular order. To do so, I take the
   	 * centroid of each compartment (C0, C1, C2, and C3). If the segments (C0,C2) and 
   	 * (C1,C3) do not intersect, the circular order is achieved.
   	 */
   	private boolean compartmentsInCircularOrder(List<Compartment> compartments) throws Exception {
   		
   		// Get required points
   		Point2D.Double C0 = compartments.get(0).centroid();
   		Point2D.Double C1 = compartments.get(1).centroid();
   		Point2D.Double C2 = compartments.get(2).centroid();
   		Point2D.Double C3 = compartments.get(3).centroid();
   		
   		// Construct the diagonal segments
   		Segment diag1 = new Segment(C0, C2);
   		Segment diag2 = new Segment(C1, C3);
   		
   		// Return true is the two diagonal intersect
   		return (diag1.intersection(diag2) != null);
   	}
}
