/*
Copyright (c) 2010-2013 Thomas Schaffter & Ricard Delgado-Gonzalo

WingJ is licensed under a
Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.

You should have received a copy of the license along with this
work. If not, see http://creativecommons.org/licenses/by-nc-nd/3.0/.

If this software was useful for your scientific work, please cite our paper(s)
listed on http://lis.epfl.ch/wingj.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.structure.drosophila.wingpouch;

import static java.lang.Math.sqrt;

import ij.IJ;
import ij.ImagePlus;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.Overlay;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureDataset;
import ch.epfl.lis.wingj.structure.StructureSnake;

/** 
 * This class describes the structure of the Drosophila wing pouch.
 * <p>
 * The wing pouch structure is composed of:
 * <ul>
 * 		<li>Dorsal-ventral boundary (D/V)
 * 		<li>Anterior-posterior boundary (A/P)
 * 		<li>Dorsal-anterior compartment (DA)
 * 		<li>Dorsal-posterior compartment (DP)
 * 		<li>Ventral-anterior compartment (VA)
 * 		<li>Ventral-posterior compartment (VP)
 * </ul>
 * <p>
 * Conventions
 * <ul>
 * 		<li>The wing pouch center is defined as the intersection of the A/P and D/B boundary.
 * 		<li>The D/V boundary corresponds to the A-P axis.
 * 		<li>The A/P boundary corresponds to the V-D axis.
 * 		<li>The D/V boundary starts at the V side.
 * 		<li>The A/P boundary starts at the A side.
 * 		<li>The contour of the DA compartment starts at the wing pouch center direction Dorsal.
 * 		<li>The contour of the DP compartment starts at the wing pouch center direction Posterior.
 * 		<li>The contour of the VP compartment starts at the wing pouch center direction Ventral.
 * 		<li>The contour of the VA compartment starts at the wing pouch center direction Anterior.
 * 		<li>The D/V boundary makes a U (bottom of the U oriented to Ventral).
 * 		<li>The A/P boundary makes a C (left side of the C oriented to Posterior).
 * 		<li>Physical units are given by the meta-information of the input images and are typically [um] and [um^2] for area.
 * </ul>
 *           
 * @version February 20, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchStructure extends Structure {
	
	/** Smallest value considered returned by sqrt operation. */
	private static double SQRT_TINY = sqrt((double)Float.intBitsToFloat((int)0x33FFFFFF));
	
	/** Age of the wing pouch, usually in hours after egg laying (AEL). */
	protected String age_ = "";
	
	/** Wing disc center. */
	protected Point2D.Double discCenter_ = null;
	
	/** Wing pouch center which is the intersection of the D/V and A/P boundaries. */
	protected Point2D.Double center_ = null;
	
	/** Dorsal-ventral (DV) boundary (A -> P). */
	protected Boundary dv_ = null;
	/** Anterior-posterior (AP) boundary (D -> V). */
	protected Boundary ap_ = null;

	/** DA compartment. */
	protected Compartment da_ = null;
	/** DP compartment. */
	protected Compartment dp_ = null;
	/** VA compartment. */
	protected Compartment va_ = null;
	/** VP compartment. */
	protected Compartment vp_ = null;
	
	/** Name of the system for structure dataset. */
	protected String systemName_ = "drosophila-wing-pouch";
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchStructure(String name) {
	
		// the wing pouch itself is a compartment
		super(name);
		
		// centers
		discCenter_ = new Point2D.Double();
		center_ = new Point2D.Double();
		
		// create boundaries
		dv_ = new Boundary(name + "-DV");
		ap_ = new Boundary(name + "-AP");
		
		// create compartments
		da_ = new Compartment(name + "-da");
		dp_ = new Compartment(name + "-dp");
		va_ = new Compartment(name + "-va");
		vp_ = new Compartment(name + "-vp");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy constructor.
	 * <p>
	 * Takes as input a Structure to allow this class to be extended. 
	 */
	public WPouchStructure(Structure s) {
		
		super(s); // copy snake
		
		WPouchStructure structure = (WPouchStructure)s;
		age_ = structure.age_; // ok
		
		discCenter_ = (Point2D.Double)structure.discCenter_.clone(); // ok
		center_ = (Point2D.Double)structure.center_.clone(); // ok
		
		dv_ = structure.dv_.copy(); // ok
		ap_ = structure.ap_.copy(); // ok
		
		da_ = structure.da_.copy(); // ok
		dp_ = structure.dp_.copy(); // ok
		va_ = structure.va_.copy(); // ok
		vp_ = structure.vp_.copy(); // ok
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy operator. */
	@Override
	public WPouchStructure copy() {
		
		return new WPouchStructure(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Clone operator. */
	@Override
	public WPouchStructure clone() {
		
		return copy();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Sets the structure in a predefined orientation and returns 1) the rotation angle,
	 * 2) if the structure has been flipped horizontally (1) or not (0).
	 */
	@Override
	public double[] setCanonicalOrientation() throws Exception {
		
		// orientation and if the structure has been flipped horizontally (1 or 0)
		double[] transforms = new double[2];
		
		Compartment c = new Compartment("DPVA");

		Point2D.Double D = getAPBoundary().getFirstPoint();
		Point2D.Double V = getAPBoundary().getLastPoint();
		Point2D.Double A = getDVBoundary().getFirstPoint();
		Point2D.Double P = getDVBoundary().getLastPoint();
		
		c.addPoint(D.x, D.y);
		c.addPoint(P.x, P.y);
		c.addPoint(V.x, V.y);
		c.addPoint(A.x, A.y);
		
		if(c.orientation() == -1) {
			flipHorizontally();
			transforms[1] = 1;
		}
		
		// Current structure points
		Compartment dpCompartment = getDPCompartment();
		Point2D.Double dpCentroid = dpCompartment.centroid();
		Compartment daCompartment = getDACompartment();
		Point2D.Double daCentroid = daCompartment.centroid();
		Compartment vaCompartment = getVACompartment();
		Point2D.Double vaCentroid = vaCompartment.centroid();
		Compartment vpCompartment = getVPCompartment();
		Point2D.Double vpCentroid = vpCompartment.centroid();
		Point2D.Double C = getCenter();
		
		// Compute the branch unit vectors of the reference structure
		Point2D.Double v1 = new Point2D.Double(1, -1);
		double v1Norm = v1.distance(0, 0);
		v1.x /= v1Norm;
		v1.y /= v1Norm;
		Point2D.Double v2 = new Point2D.Double(-1, -1);
		double v2Norm = v2.distance(0, 0);
		v2.x /= v2Norm;
		v2.y /= v2Norm;
		Point2D.Double v3 = new Point2D.Double(-1, 1);
		double v3Norm = v3.distance(0, 0);
		v3.x /= v3Norm;
		v3.y /= v3Norm;
		Point2D.Double v4 = new Point2D.Double(1, 1);
		double v4Norm = v4.distance(0, 0);
		v4.x /= v4Norm;
		v4.y /= v4Norm;
		
		// Compute the branch unit vectors of the current structure
		Point2D.Double u1 = new Point2D.Double(dpCentroid.x-C.x, dpCentroid.y-C.y);
		double u1Norm = u1.distance(0, 0);
		u1.x /= u1Norm;
		u1.y /= u1Norm;
		Point2D.Double u2 = new Point2D.Double(daCentroid.x-C.x, daCentroid.y-C.y);
		double u2Norm = u2.distance(0, 0);
		u2.x /= u2Norm;
		u2.y /= u2Norm;
		Point2D.Double u3 = new Point2D.Double(vaCentroid.x-C.x, vaCentroid.y-C.y);
		double u3Norm = u3.distance(0, 0);
		u3.x /= u3Norm;
		u3.y /= u3Norm;
		Point2D.Double u4 = new Point2D.Double(vpCentroid.x-C.x, vpCentroid.y-C.y);
		double u4Norm = u4.distance(0, 0);
		u4.x /= u4Norm;
		u4.y /= u4Norm;
		
		// Compute best realignement
		double[] theta = new double[4];
		theta[0] = -Math.acos(-((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y)));
		
		theta[1] = Math.acos(-((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y)));
		
		theta[2] = -Math.acos((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y));
		
		theta[3] = Math.acos((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y));
		
		
		int bestIndex = 0;
		double minScore = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double x1 = v1.x - (u1.x * Math.cos(theta[i]) - u1.y * Math.sin(theta[i]));
			double y1 = v1.x - (u1.x * Math.sin(theta[i]) + u1.y * Math.cos(theta[i]));
			double x2 = v2.x - (u2.x * Math.cos(theta[i]) - u2.y * Math.sin(theta[i]));
			double y2 = v2.x - (u2.x * Math.sin(theta[i]) + u2.y * Math.cos(theta[i]));
			double x3 = v3.x - (u3.x * Math.cos(theta[i]) - u3.y * Math.sin(theta[i]));
			double y3 = v3.x - (u3.x * Math.sin(theta[i]) + u3.y * Math.cos(theta[i]));
			double x4 = v4.x - (u4.x * Math.cos(theta[i]) - u4.y * Math.sin(theta[i]));
			double y4 = v4.x - (u4.x * Math.sin(theta[i]) + u4.y * Math.cos(theta[i]));
			double score =  x1*x1 + y1*y1 + x2*x2 + y2*y2 + x3*x3 + y3*y3 + x4*x4 + y4*y4;
			if(score<minScore){
				minScore = score;
				bestIndex = i;
			}
		}
		
		rotate(theta[bestIndex]);
		transforms[0] = -theta[bestIndex];
		
		return transforms;
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Rotates the current structure in such a way that the quadratic difference of
	 * the angles formed by the four axis between both structures is minimized. Both
	 * structures need to be in canonical orientation. In addition, the structure
	 * is moved so that its centroid is centered with the centroid of the reference
	 * structure.
	 */
	@Override
	public void align(Structure reference) throws Exception {

		if(reference == null)
			throw new Exception("ERROR: The reference structure is null.");

		// Reference structure points
		Compartment dpCompartmentRef = ((WPouchStructure)reference).getDPCompartment();
		Point2D.Double dpCentroidRef = dpCompartmentRef.centroid();
		Compartment daCompartmentRef = ((WPouchStructure)reference).getDACompartment();
		Point2D.Double daCentroidRef = daCompartmentRef.centroid();
		Compartment vaCompartmentRef = ((WPouchStructure)reference).getVACompartment();
		Point2D.Double vaCentroidRef = vaCompartmentRef.centroid();
		Compartment vpCompartmentRef = ((WPouchStructure)reference).getVPCompartment();
		Point2D.Double vpCentroidRef = vpCompartmentRef.centroid();
		Point2D.Double CRef = ((WPouchStructure)reference).getCenter();
		
		// Current structure points
		Compartment dpCompartment = getDPCompartment();
		Point2D.Double dpCentroid = dpCompartment.centroid();
		Compartment daCompartment = getDACompartment();
		Point2D.Double daCentroid = daCompartment.centroid();
		Compartment vaCompartment = getVACompartment();
		Point2D.Double vaCentroid = vaCompartment.centroid();
		Compartment vpCompartment = getVPCompartment();
		Point2D.Double vpCentroid = vpCompartment.centroid();
		Point2D.Double C = getCenter();
		
		// Compute the branch unit vectors of the reference structure
		Point2D.Double v1 = new Point2D.Double(dpCentroidRef.x-CRef.x, dpCentroidRef.y-CRef.y);
		double v1Norm = v1.distance(0, 0);
		v1.x /= v1Norm;
		v1.y /= v1Norm;
		Point2D.Double v2 = new Point2D.Double(daCentroidRef.x-CRef.x, daCentroidRef.y-CRef.y);
		double v2Norm = v2.distance(0, 0);
		v2.x /= v2Norm;
		v2.y /= v2Norm;
		Point2D.Double v3 = new Point2D.Double(vaCentroidRef.x-CRef.x, vaCentroidRef.y-CRef.y);
		double v3Norm = v3.distance(0, 0);
		v3.x /= v3Norm;
		v3.y /= v3Norm;
		Point2D.Double v4 = new Point2D.Double(vpCentroidRef.x-CRef.x, vpCentroidRef.y-CRef.y);
		double v4Norm = v4.distance(0, 0);
		v4.x /= v4Norm;
		v4.y /= v4Norm;
		
		// Compute the branch unit vectors of the current structure
		Point2D.Double u1 = new Point2D.Double(dpCentroid.x-C.x, dpCentroid.y-C.y);
		double u1Norm = u1.distance(0, 0);
		u1.x /= u1Norm;
		u1.y /= u1Norm;
		Point2D.Double u2 = new Point2D.Double(daCentroid.x-C.x, daCentroid.y-C.y);
		double u2Norm = u2.distance(0, 0);
		u2.x /= u2Norm;
		u2.y /= u2Norm;
		Point2D.Double u3 = new Point2D.Double(vaCentroid.x-C.x, vaCentroid.y-C.y);
		double u3Norm = u3.distance(0, 0);
		u3.x /= u3Norm;
		u3.y /= u3Norm;
		Point2D.Double u4 = new Point2D.Double(vpCentroid.x-C.x, vpCentroid.y-C.y);
		double u4Norm = u4.distance(0, 0);
		u4.x /= u4Norm;
		u4.y /= u4Norm;
		
		// Compute best realignement
		double[] theta = new double[4];
		theta[0] = -Math.acos(-((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y)));
		
		theta[1] = Math.acos(-((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y)));
		
		theta[2] = -Math.acos((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y));
		
		theta[3] = Math.acos((u1.x*v1.x + u1.y*v1.y + u2.x*v2.x + u2.y*v2.y + u3.x*v3.x + u3.y*v3.y + 
				u4.x*v4.x + u4.y*v4.y)/
				Math.sqrt(u1.x*u1.x*v1.x*v1.x + u1.y*u1.y*v1.x*v1.x + u1.x*u1.x*v1.y*v1.y + u1.y*u1.y*v1.y*v1.y + 
						2*u1.x*u2.x*v1.x*v2.x + 2*u1.y*u2.y*v1.x*v2.x + 2*u1.y*u2.x*v1.y*v2.x - 
						2*u1.x*u2.y*v1.y*v2.x + u2.x*u2.x*v2.x*v2.x + u2.y*u2.y*v2.x*v2.x - 
						2*u1.y*u2.x*v1.x*v2.y + 2*u1.x*u2.y*v1.x*v2.y + 2*u1.x*u2.x*v1.y*v2.y + 
						2*u1.y*u2.y*v1.y*v2.y + u2.x*u2.x*v2.y*v2.y + u2.y*u2.y*v2.y*v2.y + 
						2*u1.x*u3.x*v1.x*v3.x + 2*u1.y*u3.y*v1.x*v3.x + 2*u1.y*u3.x*v1.y*v3.x - 
						2*u1.x*u3.y*v1.y*v3.x + 2*u2.x*u3.x*v2.x*v3.x + 2*u2.y*u3.y*v2.x*v3.x + 
						2*u2.y*u3.x*v2.y*v3.x - 2*u2.x*u3.y*v2.y*v3.x + u3.x*u3.x*v3.x*v3.x + 
						u3.y*u3.y*v3.x*v3.x - 2*u1.y*u3.x*v1.x*v3.y + 2*u1.x*u3.y*v1.x*v3.y + 
						2*u1.x*u3.x*v1.y*v3.y + 2*u1.y*u3.y*v1.y*v3.y - 2*u2.y*u3.x*v2.x*v3.y + 
						2*u2.x*u3.y*v2.x*v3.y + 2*u2.x*u3.x*v2.y*v3.y + 2*u2.y*u3.y*v2.y*v3.y + 
						u3.x*u3.x*v3.y*v3.y + 
						u3.y*u3.y*v3.y*v3.y + 2*u1.x*u4.x*v1.x*v4.x + 2*u1.y*u4.y*v1.x*v4.x + 
						2*u1.y*u4.x*v1.y*v4.x - 2*u1.x*u4.y*v1.y*v4.x + 2*u2.x*u4.x*v2.x*v4.x + 
						2*u2.y*u4.y*v2.x*v4.x + 2*u2.y*u4.x*v2.y*v4.x - 2*u2.x*u4.y*v2.y*v4.x + 
						2*u3.x*u4.x*v3.x*v4.x + 2*u3.y*u4.y*v3.x*v4.x + 2*u3.y*u4.x*v3.y*v4.x - 
						2*u3.x*u4.y*v3.y*v4.x + u4.x*u4.x*v4.x*v4.x + u4.y*u4.y*v4.x*v4.x - 
						2*u1.y*u4.x*v1.x*v4.y + 2*u1.x*u4.y*v1.x*v4.y + 2*u1.x*u4.x*v1.y*v4.y + 
						2*u1.y*u4.y*v1.y*v4.y - 2*u2.y*u4.x*v2.x*v4.y + 2*u2.x*u4.y*v2.x*v4.y + 
						2*u2.x*u4.x*v2.y*v4.y + 2*u2.y*u4.y*v2.y*v4.y - 2*u3.y*u4.x*v3.x*v4.y + 
						2*u3.x*u4.y*v3.x*v4.y + 2*u3.x*u4.x*v3.y*v4.y + 2*u3.y*u4.y*v3.y*v4.y + 
						u4.x*u4.x*v4.y*v4.y + u4.y*u4.y*v4.y*v4.y));
		
		int bestIndex = 0;
		double minScore = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double score = matchingScore(theta[i], (WPouchStructure) reference);
			if(score<minScore){
				minScore = score;
				bestIndex = i;
			}
		}
		rotate(theta[bestIndex]);
		moveToCom(reference.centroid());
	}

	// ----------------------------------------------------------------------------
	
	/** Moves the structure so that it's center of mass is centered on the given target point. */
	@Override
	public void moveToCom(Point2D.Double target) throws Exception {
	
		double dx = target.x - centroid().x;
		double dy = target.y - centroid().y;
		
		translate(dx, dy);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Moves the structure so that it's center is centered on the given target point. */
	@Override
	public void moveToCenter(Point2D.Double target) throws Exception {
	
		List<Point2D.Double> aoi = getAoiPoints();
		double cx = (aoi.get(0).x + aoi.get(1).x) / 2.;
		double cy = (aoi.get(0).y + aoi.get(1).y) / 2.;
		
		double dx = target.x - cx;
		double dy = target.y - cy;
		
		translate(dx, dy);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Moves the structure so that it touches the top and left borders of the image. */
	@Override
	public double[] moveToTopLeftCorner() throws Exception {
		
		List<Point2D.Double> aoi = getAoiPoints();
		double dx = 0. - aoi.get(0).x;
		double dy = 0. - aoi.get(0).y;
		
		translate(dx, dy);
		
		double[] dxdy = new double[2];
		dxdy[0] = dx;
		dxdy[1] = dy;
		return dxdy;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Translates the structure using the given vector. */
	@Override
	public void translate(double dx, double dy) throws Exception {
		
		center_.x += dx;
		center_.y += dy;
		discCenter_.x += dx;
		discCenter_.y += dy;
		
		// boundaries
		dv_.translate(dx, dy);
		ap_.translate(dx, dy);
		
		// compartments
		da_.translateCompartment(dx, dy);
		dp_.translateCompartment(dx, dy);
		va_.translateCompartment(dx, dy);
		vp_.translateCompartment(dx, dy);
		
		// structure contour
		translateCompartment(dx, dy);
		
		if (snake_ == null)
			throw new Exception("ERROR: Snake is null.");
		
		// translate the snake
		snake_.translate(dx, dy);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Reverse the orientation of the D/V boundary. The identity of the compartments
	 * at the same time.
	 */
	@Override
	public synchronized void reverseDVAxisDirection() throws Exception {
		
		dv_.reverse();
		
		// The polarity along the AP axis changed
		Compartment temp1 = da_;
		Compartment temp2 = va_;
		setDACompartment(dp_);
		setVACompartment(vp_);
		setDPCompartment(temp1);
		setVPCompartment(temp2);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Reverse the orientation of the A/P boundary. The identity of the compartments
	 * at the same time.
	 */
	@Override
	public synchronized void reverseAPAxisDirection() throws Exception {
		
		ap_.reverse();
		
		// The polarity along the DV axis changed
		Compartment temp1 = da_;
		Compartment temp2 = dp_;
		setDACompartment(va_);
		setDPCompartment(vp_);
		setVACompartment(temp1);
		setVPCompartment(temp2);
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Reverses in the horizontal dimension all compartments and boundaries of the
	 * structure with respect to the wing pouch center.
	 */
	@Override
	public void flipHorizontally() throws Exception {
		
		discCenter_.x = 2.0 * center_.x - discCenter_.x;
		for(int i=0; i<this.npoints; i++)
			this.xpoints[i] = (float) (2.0 * center_.x - this.xpoints[i]);
		for(int i=0; i<dv_.npoints; i++)
			dv_.xpoints[i] = (float) (2.0 * center_.x - dv_.xpoints[i]);
		for(int i=0; i<ap_.npoints; i++)
			ap_.xpoints[i] = (float) (2.0 * center_.x - ap_.xpoints[i]);
		for(int i=0; i<da_.npoints; i++)
			da_.xpoints[i] = (float) (2.0 * center_.x - da_.xpoints[i]);
		for(int i=0; i<dp_.npoints; i++)
			dp_.xpoints[i] = (float) (2.0 * center_.x - dp_.xpoints[i]);
		for(int i=0; i<va_.npoints; i++)
			va_.xpoints[i] = (float) (2.0 * center_.x - va_.xpoints[i]);
		for(int i=0; i<vp_.npoints; i++)
			vp_.xpoints[i] = (float) (2.0 * center_.x - vp_.xpoints[i]);
		snake_.flipHorizontally(center_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Reverses in the vertically dimension all compartments and boundaries of the
	 * structure with respect to the wing pouch center.
	 */
	@Override
	public void flipVertically() throws Exception {
		
		discCenter_.y = 2.0 * center_.y - discCenter_.y;
		for(int i=0; i<this.npoints; i++)
			this.ypoints[i] = (float) (2.0 * center_.y - this.ypoints[i]);
		for(int i=0; i<dv_.npoints; i++)
			dv_.ypoints[i] = (float) (2.0 * center_.y - dv_.ypoints[i]);
		for(int i=0; i<ap_.npoints; i++)
			ap_.ypoints[i] = (float) (2.0 * center_.y - ap_.ypoints[i]);
		for(int i=0; i<da_.npoints; i++)
			da_.ypoints[i] = (float) (2.0 * center_.y - da_.ypoints[i]);
		for(int i=0; i<dp_.npoints; i++)
			dp_.ypoints[i] = (float) (2.0 * center_.y - dp_.ypoints[i]);
		for(int i=0; i<va_.npoints; i++)
			va_.ypoints[i] = (float) (2.0 * center_.y - va_.ypoints[i]);
		for(int i=0; i<vp_.npoints; i++)
			vp_.ypoints[i] = (float) (2.0 * center_.y - vp_.ypoints[i]);	
		snake_.flipVertically(center_);
	}

	// ----------------------------------------------------------------------------

	/** 
	 * Rotates all compartments and boundaries of the structure with respect to the
	 * wing pouch center by a fixed angle.
	 */
	@Override
	public void rotate(double angle) throws Exception {

		discCenter_.x = (float) (Math.cos(angle)*(discCenter_.x-center_.x) - Math.sin(angle)*(discCenter_.y-center_.y) + center_.x);
		discCenter_.y = (float) (Math.sin(angle)*(discCenter_.x-center_.x) + Math.cos(angle)*(discCenter_.y-center_.y) + center_.y);
		for(int i=0; i<this.npoints; i++){
			double xr = Math.cos(angle)*(this.xpoints[i]-center_.x) - Math.sin(angle)*(this.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(this.xpoints[i]-center_.x) + Math.cos(angle)*(this.ypoints[i]-center_.y) + center_.y;
			this.xpoints[i] = (float) xr;
			this.ypoints[i] = (float) yr;
		}
		for(int i=0; i<dv_.npoints; i++){
			double xr = Math.cos(angle)*(dv_.xpoints[i]-center_.x) - Math.sin(angle)*(dv_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(dv_.xpoints[i]-center_.x) + Math.cos(angle)*(dv_.ypoints[i]-center_.y) + center_.y;
			dv_.xpoints[i] = (float) xr;
			dv_.ypoints[i] = (float) yr;
		}
		for(int i=0; i<ap_.npoints; i++){
			double xr = Math.cos(angle)*(ap_.xpoints[i]-center_.x) - Math.sin(angle)*(ap_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(ap_.xpoints[i]-center_.x) + Math.cos(angle)*(ap_.ypoints[i]-center_.y) + center_.y;
			ap_.xpoints[i] = (float) xr;
			ap_.ypoints[i] = (float) yr;
		}
		for(int i=0; i<da_.npoints; i++){
			double xr = Math.cos(angle)*(da_.xpoints[i]-center_.x) - Math.sin(angle)*(da_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(da_.xpoints[i]-center_.x) + Math.cos(angle)*(da_.ypoints[i]-center_.y) + center_.y;
			da_.xpoints[i] = (float) xr;
			da_.ypoints[i] = (float) yr;
		}
		for(int i=0; i<dp_.npoints; i++){
			double xr = Math.cos(angle)*(dp_.xpoints[i]-center_.x) - Math.sin(angle)*(dp_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(dp_.xpoints[i]-center_.x) + Math.cos(angle)*(dp_.ypoints[i]-center_.y) + center_.y;
			dp_.xpoints[i] = (float) xr;
			dp_.ypoints[i] = (float) yr;
		}
		for(int i=0; i<va_.npoints; i++){
			double xr = Math.cos(angle)*(va_.xpoints[i]-center_.x) - Math.sin(angle)*(va_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(va_.xpoints[i]-center_.x) + Math.cos(angle)*(va_.ypoints[i]-center_.y) + center_.y;
			va_.xpoints[i] = (float) xr;
			va_.ypoints[i] = (float) yr;
		}
		for(int i=0; i<vp_.npoints; i++){
			double xr = Math.cos(angle)*(vp_.xpoints[i]-center_.x) - Math.sin(angle)*(vp_.ypoints[i]-center_.y) + center_.x;
			double yr = Math.sin(angle)*(vp_.xpoints[i]-center_.x) + Math.cos(angle)*(vp_.ypoints[i]-center_.y) + center_.y;
			vp_.xpoints[i] = (float) xr;
			vp_.ypoints[i] = (float) yr;
		}
		snake_.rotate(center_, angle);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Swaps the identity of the two boundaries. The identity of the compartments.
	 * at the same time.
	 */
	@Override
	public synchronized void swapAxes() throws Exception {
		
		Boundary temp = dv_;
		setDVBoundary(ap_);
		setAPBoundary(temp);
		
		// Two diagonal compartments don't have to be touched
		Compartment temp2 = dp_;
		setDPCompartment(va_);
		setVACompartment(temp2);
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Returns the boundary starting from the wing pouch center to the extremity D.
	 * A priori: dv_ is oriented from D to V
	 */
	public final Boundary getCDAxis() throws Exception {
		
		Boundary boundary = new Boundary("CD axis");
		
		float xtmp = 0;
		float ytmp = 0;
		int n = ap_.npoints;
		for (int i = 0; i < n; i++) {
			xtmp = ap_.xpoints[i];
			ytmp = ap_.ypoints[i];
			boundary.addPoint(xtmp, ytmp);
			
			if (center_.distance(xtmp,ytmp)<SQRT_TINY) {
				boundary.reverse();
				return boundary;
			}
		}
		throw new Exception("ERROR: The point " + center_ + " was not found in the A/P boundary.");
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Returns the boundary starting from the wing pouch center to the extremity V.
	 * A priori: dv_ is oriented from D to V
	 */
	public final Boundary getCVAxis() throws Exception {
		
		Boundary boundary = new Boundary("CV axis");

		float xtmp = 0;
		float ytmp = 0;
		int n = ap_.npoints;
		boolean save = false;
		for (int i = 0; i < n; i++) {
			xtmp = ap_.xpoints[i];
			ytmp = ap_.ypoints[i];
		
			if (center_.distance(xtmp,ytmp)<SQRT_TINY) save = true;
			if (save) boundary.addPoint(xtmp, ytmp);	
		}
		if (!save) throw new Exception("ERROR: The point " + center_ + " was not found in the A/P boundary.");
		return boundary;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Returns the boundary starting from the wing pouch center to the extremity A.
	 * A priori: ap_ is oriented from A to P
	 */
	public final Boundary getCAAxis() throws Exception {
		
		Boundary boundary = new Boundary("AP axis");
		
		float xtmp = 0;
		float ytmp = 0;
		int n = dv_.npoints;
		for (int i = 0; i < n; i++) {
			xtmp = dv_.xpoints[i];
			ytmp = dv_.ypoints[i];
			boundary.addPoint(xtmp, ytmp);
			
			if (center_.distance(xtmp,ytmp)<SQRT_TINY) {
				boundary.reverse();
				return boundary;
			}
		}
		throw new Exception("ERROR: The point " + center_ + " was not found in the D/V boundary.");
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Returns the boundary starting from the wing pouch center to the extremity P.
	 * A priori: ap_ is oriented from A to P
	 */
	public final Boundary getCPAxis() throws Exception {
		
		Boundary boundary = new Boundary("CV axis");
		
		float xtmp = 0;
		float ytmp = 0;
		int n = dv_.npoints;
		boolean save = false;
		for (int i = 0; i < n; i++) {
			xtmp = dv_.xpoints[i];
			ytmp = dv_.ypoints[i];
		
			if (center_.distance(xtmp,ytmp)<SQRT_TINY) save = true;
			if (save) boundary.addPoint(xtmp, ytmp);	
		}
		if (!save) throw new Exception("WARNING: The point " + center_ + " was not found in the D/V boundary.");
		return boundary;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the structure properties of the wing pouch to file. */
	@Override
	public void writeStructureMeasurements(URI uri) throws Exception {
		
		try {
			WPouchStructureMeasurementsParser writer = new WPouchStructureMeasurementsParser(this);
			writer.write(uri);
        	WJSettings.log("[x] Writing structure measurements (xml)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure measurements (xml)");
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the structure properties of the wing pouch to file (opens a Save dialog). */
	@Override
	public void writeStructureMeasurements() throws Exception {
		
		WPouchStructureDataset structureDataset = new WPouchStructureDataset(this);
		structureDataset.saveStructureMeasurements();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Save to file the data required to reconstruct the structure. Here the data
	 * are 1) a list of nodes required to create the snake structure and 2) the center
	 * of gravity of each of the four compartment to obtain the information about the
	 * orientation of the structure. */
	@Override
	public void write(URI uri) throws Exception {
		
		// save structure XML
		try {
			WPouchStructureParser parser = new WPouchStructureParser(this);
			parser.write(uri);
			WJSettings.log("[x] Writing structure (xml)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure (xml)");
			WJMessage.showMessage(e);
		}
		
		// save structure to TXT
		try {
			// build URI for each element to save from the given URI.
			String filename = uri.getPath();
			filename = FilenameUtils.removeExtension(filename);//FilenameUtils.removeExtension(filename);
			// prepare file URI
			URI dvBoundaryUri = (new File(filename + "_A-P.txt")).toURI();
			URI apBoundaryUri = (new File(filename + "_V-D.txt")).toURI();
			URI contourUri = (new File(filename + "_contour.txt")).toURI();
			// write to file
			getDVBoundary().writePoints(dvBoundaryUri);
			getAPBoundary().reverse(); // to get V -> D
			getAPBoundary().writePoints(apBoundaryUri);
			getAPBoundary().reverse(); // restore
			writePoints(contourUri);
	    	WJSettings.log("[x] Writing structure (txt)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure (txt)");
			WJMessage.showMessage(e);
		}
	}
	// ----------------------------------------------------------------------------
	
	/** Uses the class WPouchStructureDataset to show a Save dialog and save the structure to file. */
	@Override
	public void write() throws Exception {

		WPouchStructureDataset structureDataset = new WPouchStructureDataset(this);
		structureDataset.saveStructure();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Read from file the data required to reconstruct the structure. See the description
	 * of the method write(). After loading the data, this object is initialized based
	 * on the data contained in the file. */
	@Override
	public void read(URI uri) throws Exception {

		try {
			WPouchStructureParser parser = new WPouchStructureParser(this);
			try {
				parser.read(uri);
			} catch (Exception e) {
				WJSettings.log("Trying to read old structure format.");
				parser.readVersion1(uri);
			}
			WJSettings.log("[x] Reading structure (xml)");
		} catch (Exception e) {
			WJSettings.log("[ ] Reading structure (xml)");
			throw e;
//			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the structure dataset in a readable format (areas, perimeters, etc.). */
	@Override
	public String getReadableStructureDataset() throws Exception {
		
		String unit = WJSettings.getInstance().getUnit();
		String areaUnit = unit + "^2";
//		DecimalFormat miniFormatter = new DecimalFormat("#.###");
		DecimalFormat formatter = new DecimalFormat("#.######");
		String content = "";
		double value = 0.;
		
		content += systemName_ + " name: " + this.name_ + "\n";		
		value = perimeter();
		content += systemName_ + " perimeter: " + formatter.format(value) + " " + unit + "\n";
		value = area();
		content += systemName_ + " area: " + formatter.format(value) + " " + areaUnit + "\n";
		content += "\n";
		content += "---------- BOUNDARIES ----------\n";
		value = dv_.length();
		content += "D/V length: " + formatter.format(value) + " " + unit + "\n";
		value = getCAAxis().length();
		content += "Including C-A axis length: " + formatter.format(value) + " " + unit + "\n";
		value = getCPAxis().length();
		content += "Including C-P axis length: " + formatter.format(value) + " " + unit + "\n";
		value = ap_.length();
		content += "A/P length: " + formatter.format(value) + " " + unit + "\n";
		value = getCVAxis().length();
		content += "Including C-V axis length: " + formatter.format(value) + " " + unit + "\n";
		value = getCDAxis().length();
		content += "Including C-D axis length: " + formatter.format(value) + " " + unit + "\n";
		content += "\n";
		content += "---------- COMPARTMENTS ----------\n";
		value = getDACompartment().perimeter();
		content += "DA perimeter: " + formatter.format(value) + " " + unit + "\n";
		value = getDACompartment().area();
		content += "DA area: " + formatter.format(value) + " " + areaUnit + "\n";
		value = getDPCompartment().perimeter();
		content += "DP perimeter: " + formatter.format(value) + " " + unit + "\n";
		value = getDPCompartment().area();
		content += "DP area: " + formatter.format(value) + " " + areaUnit + "\n";
		value = getVACompartment().perimeter();
		content += "VA perimeter: " + formatter.format(value) + " " + unit + "\n";
		value = getVACompartment().area();
		content += "VA area: " + formatter.format(value) + " " + areaUnit + "\n";
		value = getVPCompartment().perimeter();
		content += "VP perimeter: " + formatter.format(value) + " " + unit + "\n";
		value = getVPCompartment().area();
		content += "VP area: " + formatter.format(value) + " " + areaUnit;
		
		return content;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves entire structure dataset to file. */
	@Override
	public void writeStructureDataset(WJStructureViewer structureViewer) throws Exception {
		
		WPouchStructureDataset structureDataset = new WPouchStructureDataset(this, structureViewer);
		structureDataset.saveStructureDataset(false);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Computes a Boundary trajectory along which expression can be measured (passed by
	 * reference). First, the offset is used to translate the trajectory from the reference
	 * boundary. The boundary is then cropped using THIS structure. The double value returned
	 * is the length of the negative part in the current unit. For instance, Boundary.length()
	 * returns the value in the correct unit.
	 * <p> 
	 * IMPORTANT: The first boundary convention is used, that is the D point is the 
	 * 
	 * @param trajectory Trajectory along which expression will be measured.
	 * @param referenceBoundary Takes value WJSettings.BOUNDARY_AP or WJSettings.BOUNDARY_DV.
	 * @param offset Takes values in [-1,1].
	 */
	@Override
	public double getExpressionTrajectory(Boundary trajectory, int referenceBoundary, double offset) throws Exception {
		
		if (trajectory == null)
			throw new Exception("ERROR: Trajectory is null.");
		trajectory.empty();

		Point2D.Double newCenter = null;
		Boundary negBoundary = null;
		Boundary posBoundary = null;
		String boundaryName = null;
		if (referenceBoundary == WJSettings.BOUNDARY_DV) {
			negBoundary = getCAAxis().clone();
			negBoundary.reverse();
			posBoundary = getCPAxis();
			boundaryName = "D/V";
			
			// compute the center of the shifted trajectory
			if (offset < 0) newCenter = getCVAxis().getSubBoundaryFromCoeff(Math.abs(offset)).getLastPoint();
			else newCenter = getCDAxis().getSubBoundaryFromCoeff(Math.abs(offset)).getLastPoint();
		}
		else if (referenceBoundary == WJSettings.BOUNDARY_AP) {
			negBoundary = getCVAxis();
			negBoundary.reverse();
			posBoundary = getCDAxis();
			boundaryName = "A/P";
			
			// compute the center of the shifted trajectory
			if (offset < 0) newCenter = getCAAxis().getSubBoundaryFromCoeff(Math.abs(offset)).getLastPoint();
			else newCenter = getCPAxis().getSubBoundaryFromCoeff(Math.abs(offset)).getLastPoint();
		}
		else {
			throw new Exception("ERROR: Unknown reference boundary.");
		}
		
		// translates and crop the negative boundary
		negBoundary.translate(newCenter.x - center_.x, newCenter.y - center_.y);
		negBoundary = negBoundary.restrictToCompartment(this);
		
		// translates and crop the positive boundary
		posBoundary.translate(newCenter.x - center_.x, newCenter.y - center_.y);
		posBoundary = posBoundary.restrictToCompartment(this);
		
		// combines negBoundary and posBoundary to form the trajectory
		trajectory.empty();
		trajectory.append(negBoundary);
		trajectory.append(posBoundary);
		trajectory.setName(boundaryName);
		
		return negBoundary.length();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new structure that aggregates the given structures. */
	@Override
	public Structure aggregate(String name, List<Structure> structures, int aggregationMode) throws Exception {
		
		WPouchStructure structure = new WPouchStructure(name);
		structure.aggregate(structures, aggregationMode);
		
		return structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Main method. */
	public static void main(String[] args) {
		
		try {
			WPouchStructure structure = new WPouchStructure("structure");
			URI uri = new URI("file:///mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_5/WingJ/my_experiment_structure.xml");
			structure.read(uri);
			structure.printDVAP();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Sets this structure as the aggregate of the given structures. */
	@Override
	public void aggregate(List<Structure> structures, int aggregationMode) throws Exception {

		if (structures == null)
			throw new Exception("ERROR: List of structures is null.");
		if (structures.size() < 2)
			throw new Exception("ERROR: At least two structure are required for a meaningful synthesis.");
	
		// copy the list of structure otherwise the input structures will be modified (translation, rotation, etc.)
		List<Structure> structuresCopy = new ArrayList<Structure>();
		Structure structureCopy = null;
		for (Structure structure : structures) {
			structureCopy = structure.copy();
			Point2D.Double D = structureCopy.getAPBoundary().getFirstPoint();
			Point2D.Double V = structureCopy.getAPBoundary().getLastPoint();
			Point2D.Double A = structureCopy.getDVBoundary().getFirstPoint();
			Point2D.Double P = structureCopy.getDVBoundary().getLastPoint();
			structureCopy.getStructureSnake().reorganizeAllNodesFromDPVA(D, P, V, A);
			structuresCopy.add(structureCopy);
		}
		
		Structure referenceStructure = structuresCopy.get(0);
		Structure structure = null;
		for (int i = 0; i < structuresCopy.size(); i++) {
			structure = structuresCopy.get(i);
			structure.setCanonicalOrientation();
			if(structure!=referenceStructure)
				structure.align(referenceStructure);
			if (WJSettings.DEBUG) {
				ImagePlusManager manager = ImagePlusManager.getInstance();
				ImagePlus background = IJ.createImage("structure_" + i + "_aligned_with_reference", "black", 1024, 1024, 1);
				manager.add(background.getTitle(), background, false);
				WJStructureViewer viewer = new WJStructureViewer(structure.copy(), background);
				viewer.run();
			}
		}
		
		// get structure snakes
		List<StructureSnake> snakes = new ArrayList<StructureSnake>();
		for (Structure s : structuresCopy)
			snakes.add(s.getStructureSnake());
		
		// structure snake synthesis
		snake_ = new WPouchStructureSnake();
		snake_.aggregate(snakes, aggregationMode);
		
		// initialize structure (with Compartment and Boundary objects)
		WPouchStructureSnake snake = (WPouchStructureSnake)snake_;
		this.setCenter(snake.getWPouchCenter());
		this.setDiscCenter(snake.getWDiscCenter());
		
		// boundaries
//		Boundary dv = snake.getBoundary(1);
//		if(dv.getFirstPoint().y > dv.getLastPoint().y){
//			dv.reverse(); // first point on anterior side
//		}
//		Boundary ap = snake.getBoundary(0);
//		if(ap.getFirstPoint().x > ap.getLastPoint().x){
//			ap.reverse(); // first point on anterior side
//		}
		Boundary dv = snake.getBoundary(1);
		if(dv.getFirstPoint().x > dv.getLastPoint().x){
			dv.reverse(); // first point on anterior side
		}
		Boundary ap = snake.getBoundary(0);
		if(ap.getFirstPoint().y > ap.getLastPoint().y){
			ap.reverse(); // first point on anterior side
		}
		this.setDVBoundary(dv);
		this.setAPBoundary(ap);
		
		// compartments
		this.setDACompartment(snake.getCompartment(3));
		this.setDPCompartment(snake.getCompartment(0));
		this.setVACompartment(snake.getCompartment(2));
		this.setVPCompartment(snake.getCompartment(1));
		
		// pouch compartment itself
		reset();
		Compartment contour = snake.getWPouchContour();
		for (int i = 0; i < contour.npoints; i++) // pouch contour
			addPoint(contour.xpoints[i], contour.ypoints[i]);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns an object extending the abstract class Overlay for displaying structure on top of an image.
	 * If singleImage is true, an ImageWindow object is used instead of a StackWindow.
	 * @see ch.epfl.lis.wingj.WJStructureViewer
	 */
	@Override
	public Overlay getStructureOverlay(ImagePlus image, boolean singleImage) {
		
		return new WPouchOverlay(this, image, singleImage);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Prints the points D, V, A, and P to the standard output. */
	public void printDVAP() throws Exception {
		
		Point2D.Double D = getAPBoundary().getFirstPoint();
		Point2D.Double V = getAPBoundary().getLastPoint();
		Point2D.Double A = getDVBoundary().getFirstPoint();
		Point2D.Double P = getDVBoundary().getLastPoint();
		WJSettings.log("D: " + D);
		WJSettings.log("V: " + V);
		WJSettings.log("A: " + A);
		WJSettings.log("P: " + P);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new WPouchStructureDataset corresponding to this structure. */
	@Override
	public StructureDataset newStructureDataset() {
		
		return new WPouchStructureDataset(this);
	}
	
	// ============================================================================
	// PRIVATE METHODS

	/** 
	 * Computes the disparity in alignment of between the current structure and a
	 * rotates structure. Both structures need to be in canonical orientation.
	 */
	private double matchingScore(double theta, WPouchStructure reference){
		
		// Reference structure points
		Compartment dpCompartmentRef = reference.getDPCompartment();
		Point2D.Double dpCentroidRef = dpCompartmentRef.centroid();
		Compartment daCompartmentRef = reference.getDACompartment();
		Point2D.Double daCentroidRef = daCompartmentRef.centroid();
		Compartment vaCompartmentRef = reference.getVACompartment();
		Point2D.Double vaCentroidRef = vaCompartmentRef.centroid();
		Compartment vpCompartmentRef = reference.getVPCompartment();
		Point2D.Double vpCentroidRef = vpCompartmentRef.centroid();
		Point2D.Double CRef = reference.getCenter();
		
		// Current structure points
		Compartment dpCompartment = getDPCompartment();
		Point2D.Double dpCentroid = dpCompartment.centroid();
		Compartment daCompartment = getDACompartment();
		Point2D.Double daCentroid = daCompartment.centroid();
		Compartment vaCompartment = getVACompartment();
		Point2D.Double vaCentroid = vaCompartment.centroid();
		Compartment vpCompartment = getVPCompartment();
		Point2D.Double vpCentroid = vpCompartment.centroid();
		Point2D.Double C = getCenter();
		
		// Compute the branch unit vectors of the reference structure
		Point2D.Double v1 = new Point2D.Double(dpCentroidRef.x-CRef.x, dpCentroidRef.y-CRef.y);
		double v1Norm = v1.distance(0, 0);
		v1.x /= v1Norm;
		v1.y /= v1Norm;
		Point2D.Double v2 = new Point2D.Double(daCentroidRef.x-CRef.x, daCentroidRef.y-CRef.y);
		double v2Norm = v2.distance(0, 0);
		v2.x /= v2Norm;
		v2.y /= v2Norm;
		Point2D.Double v3 = new Point2D.Double(vaCentroidRef.x-CRef.x, vaCentroidRef.y-CRef.y);
		double v3Norm = v3.distance(0, 0);
		v3.x /= v3Norm;
		v3.y /= v3Norm;
		Point2D.Double v4 = new Point2D.Double(vpCentroidRef.x-CRef.x, vpCentroidRef.y-CRef.y);
		double v4Norm = v4.distance(0, 0);
		v4.x /= v4Norm;
		v4.y /= v4Norm;
		
		// Compute the branch unit vectors of the current structure
		Point2D.Double u1 = new Point2D.Double(dpCentroid.x-C.x, dpCentroid.y-C.y);
		double u1Norm = u1.distance(0, 0);
		u1.x /= u1Norm;
		u1.y /= u1Norm;
		Point2D.Double u2 = new Point2D.Double(daCentroid.x-C.x, daCentroid.y-C.y);
		double u2Norm = u2.distance(0, 0);
		u2.x /= u2Norm;
		u2.y /= u2Norm;
		Point2D.Double u3 = new Point2D.Double(vaCentroid.x-C.x, vaCentroid.y-C.y);
		double u3Norm = u3.distance(0, 0);
		u3.x /= u3Norm;
		u3.y /= u3Norm;
		Point2D.Double u4 = new Point2D.Double(vpCentroid.x-C.x, vpCentroid.y-C.y);
		double u4Norm = u4.distance(0, 0);
		u4.x /= u4Norm;
		u4.y /= u4Norm;
		
		double x1 = v1.x - (u1.x * Math.cos(theta) - u1.y * Math.sin(theta));
		double y1 = v1.x - (u1.x * Math.sin(theta) + u1.y * Math.cos(theta));
		
		double x2 = v2.x - (u2.x * Math.cos(theta) - u2.y * Math.sin(theta));
		double y2 = v2.x - (u2.x * Math.sin(theta) + u2.y * Math.cos(theta));

		double x3 = v3.x - (u3.x * Math.cos(theta) - u3.y * Math.sin(theta));
		double y3 = v3.x - (u3.x * Math.sin(theta) + u3.y * Math.cos(theta));

		double x4 = v4.x - (u4.x * Math.cos(theta) - u4.y * Math.sin(theta));
		double y4 = v4.x - (u4.x * Math.sin(theta) + u4.y * Math.cos(theta));
		
		return x1*x1 + y1*y1 + x2*x2 + y2*y2 + x3*x3 + y3*y3 + x4*x4 + y4*y4;
	}

	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setAge(String age) { age_ = age; }
	public String getAge() { return age_; }
	
	public void setDVBoundary(Boundary dv) {
		dv_ = dv;
		dv_.setName("D/V boundary");
	}
	@Override
	public Boundary getDVBoundary() { return dv_; }
	
	public void setAPBoundary(Boundary ap) {
		ap_ = ap;
		ap_.setName("A/P boundary");
	}
	@Override
	public Boundary getAPBoundary() { return ap_; }
	
	public void setDACompartment(Compartment da) {
		da_ = da;
		da_.setName("DA compartment");
	}
	public Compartment getDACompartment() { return da_; }
	
	public void setDPCompartment(Compartment dp) {
		dp_ = dp;
		dp_.setName("DP compartment");
	}
	public Compartment getDPCompartment() { return dp_; }
	
	public void setVACompartment(Compartment va) {
		va_ = va;
		va_.setName("VA compartment");
	}
	public Compartment getVACompartment() { return va_; }

	public void setVPCompartment(Compartment vp) {
		vp_ = vp;
		vp_.setName("VP compartment");
	}
	public Compartment getVPCompartment() { return vp_; }
	
	public void setDiscCenter(Point2D.Double p) { discCenter_ = (Point2D.Double)p.clone(); }
	public Point2D.Double getDiscCenter() { return discCenter_; }
	
	public void setCenter(Point2D.Double p) { center_ = (Point2D.Double)p.clone(); }
	public Point2D.Double getCenter() { return center_; }
	
	public void setSystemName(String systemName) { systemName_ = systemName; }
}