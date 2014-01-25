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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.geometry.Segment;

/** 
 * Infers the orientation of the <i>Drosophila</i> embryo structure model from hairy protein expression.
 * 
 * @version October 29, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class EmbryoOrientationDetection extends StructureDetectionModule {
	
	/** First axis */
	protected Boundary axis1_ = null;
	/** Second axis */
	protected Boundary axis2_ = null;
	/** Four compartment */
	protected List<Compartment> compartments_ = null;
	/** Wing pouch contour */
	protected Compartment contour_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public EmbryoOrientationDetection() {};
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public EmbryoOrientationDetection(String name, EmbryoStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Inferring the orientation of the Drosophila embryo.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public EmbryoOrientationDetection(String name, EmbryoStructureDetector detector) {
		
		super(name, detector);
		description_ = "Inferring the orientation of the Drosophila embryo.";
	}
	
	// ----------------------------------------------------------------------------

	/** Infers the orientation of the embryo and set the final structure model. */
	@Override
	public void run() throws Exception {
		
		EmbryoStructureDetector detector = (EmbryoStructureDetector)detector_;
		EmbryoStructure structure = (EmbryoStructure)detector.getStructure();
		EmbryoStructureSnake snake = (EmbryoStructureSnake)structure.getStructureSnake();
		
//		if (!structure.isStructureKnown())
//			throw new Exception("INFO: Orientation cannot be inferred if structure is not defined.");
		
		axis1_ = snake.getBoundary(0);
		axis2_ = snake.getBoundary(1);
		
		compartments_ = new ArrayList<Compartment>();
		compartments_.add(snake.getCompartment(0));
		compartments_.add(snake.getCompartment(1));
		compartments_.add(snake.getCompartment(2));
		compartments_.add(snake.getCompartment(3));
		
		contour_ = snake.getEmbryoContour();
		
		Boundary dv = null;
		Boundary ap = null;
		
		// ============================================================================
		// BEGIN EMBRYO ORIENTATION INFERENCE (UNSUPERVISED, USE BOUNDARIES CURVATURE)
		
		// the longest boundary is D/V
		if (axis1_.lengthInPx() > axis1_.lengthInPx()) {
			dv = axis1_;
			ap = axis2_;
		} else {
			dv = axis2_;
			ap = axis1_;
		}
		
		// assumption: the intersection of the two segments AP and DV falls falls inside
		// the DA compartment
		
		Segment s1 = new Segment(dv.getFirstPoint(), dv.getLastPoint());
		Segment s2 = new Segment(ap.getFirstPoint(), ap.getLastPoint());
		Point2D.Double pointSupposedInDACompartment = s1.intersection(s2);
		
		// if the hairy protein could be used to distinct between anterior and posterior,
		// we use here a manual detection. The free control point (disc center in the wing pouch
		// model) must be placed in the dorsal+anterior compartment. From that we can infer
		// the orientation of the embryo.
		List<Compartment> compartments = getOrientatedCompartments(ap, dv, pointSupposedInDACompartment, compartments_);
		
		Point2D.Double[] coms = new Point2D.Double[4];
		for (int i = 0; i < compartments.size(); i++)
			coms[i] = compartments.get(i).centroid();
		setOrientationFromCompartmentCenterOfMass(structure, coms);
		
		// ============================================================================
		// END EMBRYO ORIENTATION INFERENCE (UNSUPERVISED, BASED ON WING METHOD)
		
		// ============================================================================
		// BEGIN EMBRYO ORIENTATION INFERENCE (WEAKLY-SUPERVISED METHOD, REQUIRES DA COM)
		
//		// the longest boundary is D/V
//		if (axis1_.lengthInPx() > axis1_.lengthInPx()) {
//			dv = axis1_;
//			ap = axis2_;
//		} else {
//			dv = axis2_;
//			ap = axis1_;
//		}
//		
//		// if the hairy protein could be used to distinct between anterior and posterior,
//		// we use here a manual detection. The free control point (disc center in the wing pouch
//		// model) must be placed in the dorsal+anterior compartment. From that we can infer
//		// the orientation of the embryo.
//		List<Compartment> compartments = getOrientatedCompartments(ap, dv, structure.getDiscCenter(), compartments_);
		
		// ============================================================================
		// END EMBRYO ORIENTATION INFERENCE (WEAKLY-SUPERVISED METHOD, REQUIRES DA COM)
		
//		if (ap == null)
//			throw new Exception("WARNING: Unable to identify the A/P boundary.");
//		if (dv == null)
//			throw new Exception("WARNING: Unable to identify the D/V boundary.");
//		if (compartments == null || compartments.size() != 4)
//			throw new Exception("WARNING: Unable to identify the four compartments.");
//		
//		// Finally, save to pouch_ :)
//		// Update: pouch_ became structure_
//		structure.setCenter(structure.getCenter());
//		structure.setAPBoundary(ap);
//		structure.setDVBoundary(dv);
//		Compartment da = compartments.get(0);
//		Compartment dp = compartments.get(1);
//		Compartment va = compartments.get(2);
//		Compartment vp = compartments.get(3);
//		structure.setDACompartment(da);
//		structure.setDPCompartment(dp);
//		structure.setVACompartment(va);
//		structure.setVPCompartment(vp);
//		
//		structure.reset();
//		for (int i = 0; i < contour_.npoints; i++) // pouch contour
//			structure.addPoint(contour_.xpoints[i], contour_.ypoints[i]);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Sets the orientation of the structure from the snake and the center of mass
	 * of each compartment. The centers of mass must be arranged in this order:
	 * DA, DP, VA, VP.
	 */
	public void setOrientationFromCompartmentCenterOfMass(EmbryoStructure structure, Point2D.Double[] coms) throws Exception {
		
//		WJSettings.log("Calling orientation inference method based on known compartments identity.");
		
		if (coms.length < 4)
			throw new Exception("ERROR: The orientation inference requires four compartment centers of mass.");
		
		for (int i = 0; i < coms.length; i++) {
			if (coms[i] == null)
				throw new Exception("ERROR: At least one center of mass is null (com index=" + i + ").");
		}
		
		// get boundaries and compartments from the snake
		EmbryoStructureSnake snake = (EmbryoStructureSnake)structure.getStructureSnake();
		
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
		
		// draw a segment from DA centroid to DP centroid
		Segment DaCom2DpCom = new Segment(structure.getDACompartment().centroid(), structure.getDPCompartment().centroid());
		// draw a segment from DA centroid to VA centroid
		Segment DaCom2VaCom = new Segment(structure.getDACompartment().centroid(), structure.getVACompartment().centroid());
		
		// build four axes from pouch center to extremities of the two axes
		Segment s1 = new Segment(snake.getEmbryoCenter(), axis1_.getFirstPoint());
		Segment s2 = new Segment(snake.getEmbryoCenter(), axis1_.getLastPoint());
		Segment s3 = new Segment(snake.getEmbryoCenter(), axis2_.getFirstPoint());
		Segment s4 = new Segment(snake.getEmbryoCenter(), axis2_.getLastPoint());
		
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
		
//		WJSettings.log("AP: " + structure.getAPBoundary().getFirstPoint() + " -> " + structure.getAPBoundary().getLastPoint());
//		WJSettings.log("DV: " + structure.getDVBoundary().getFirstPoint() + " -> " + structure.getDVBoundary().getLastPoint());
		
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
	 * Returns the DA compartment.
	 * <p>
	 * The DA compartment is selected as the one containing the free control point.
	 * The DA compartment is removed from the given list of compartments.
	 * */
	private Compartment getDACompartment(List<Compartment> compartments, Point2D.Double discCenter) throws Exception {
		
		Compartment c = null;
		for (int i = 0; i < compartments.size(); i++) {
			c = compartments.get(i);
			if (c.contains((float)discCenter.x, (float)discCenter.y)) {
				compartments.remove(i);
				return c;
			}
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the DA, DP, VA and VP compartments in this order.
	 * <p>
	 * First, the DA compartment is selected as the one containing the free control point M.
	 * Note that in the new version, M is not the free point but another point computed from
	 * the curvature of the boundaries.
	 * <p>
	 * From the centroid dac of the DA compartment previously detected, we draw a segment from
	 * dac to the centroid of the three remaining compartments. We expect the segment
	 * dac-vac and dac-vpc to intersect with the D/V boundary. So the remaining segment
	 * that do not cross D/V defines the DP compartment. Among the remaining two segments,
	 * the shortest segment is used to find VA and so the other compartment is VP.
	 */
	private List<Compartment> getOrientatedCompartments(Boundary ap, Boundary dv, Point2D.Double M, List<Compartment> C) throws Exception {
		
		List<Compartment> compartments = new ArrayList<Compartment>();
		
		if (C.size() != 4)
			throw new Exception("ERROR: C doen't contain four compartments.");
		
		// gets DA compartment (remove DA from C).
		compartments.add(getDACompartment(C, M));
		if (C.size() != 3)
			throw new Exception("ERROR: There should be only 3 compartments remaining in C.");
		if (compartments.size() != 1 || compartments.get(0) == null)
			throw new Exception("ERROR: DA compartment not found.");
		
		// builds three segments dac-dpc, dac-vac and dac-vpc using the centroid
		// of the given compartment.
		Point2D.Double dac = compartments.get(0).centroid();
		Point2D.Double c1 = C.get(0).centroid();
		Point2D.Double c2 = C.get(1).centroid();
		Point2D.Double c3 = C.get(2).centroid();
		
		List<Segment> segments = new ArrayList<Segment>();
		segments.add(new Segment(dac, c1));
		segments.add(new Segment(dac, c2));
		segments.add(new Segment(dac, c3));
		
		// here the A/P and D/V boundary are approximated as segment
		// TODO: compute the intersection of the segments with the effective
		// A/P and D/V boundary
		Segment apSegment = new Segment(ap.getFirstPoint(), ap.getLastPoint());
		Segment dvSegment = new Segment(dv.getFirstPoint(), dv.getLastPoint());
		
		// finds DP, VP and VA
		Compartment dp = null;
		Compartment vp = null;
		Compartment va = null;
		Segment s = null;
		for (int i = 0; i < segments.size(); i++) {
			s = segments.get(i);
			if (s == null)
				throw new Exception("ERROR: s is null (orientation inference).");
			if (s.intersection(apSegment) != null && s.intersection(dvSegment) == null)
				va = C.get(i);
			else if (s.intersection(apSegment) != null && s.intersection(dvSegment) != null)
				vp = C.get(i);
			else if (s.intersection(apSegment) == null && s.intersection(dvSegment) != null)
				dp = C.get(i);
			else
				throw new Exception("ERROR: Unexpected compartments distribution.");
		}
	
		compartments.add(dp);
		compartments.add(va);
		compartments.add(vp);	
		
		return compartments;
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
}
