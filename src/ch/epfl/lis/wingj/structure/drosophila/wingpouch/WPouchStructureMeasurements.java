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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

/** 
 * Contains the measurements taken from the structure model of the wing pouch.
 * 
 * @version November 9, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WPouchStructureMeasurements {

	/** Number of boundaries. */
	public static final int NUM_BOUNDARIES = 2;
	/** Number of axes. */
	public static final int NUM_AXES = 4;
	/** Number of compartments (including pouch). */
	public static final int NUM_COMPARTMENTS = 5;
	
	/** D/V boundary (A-P axis). */
	public static final int BOUNDARY_DV = 0;
	/** A/P boundary (D-V axis). */
	public static final int BOUNDARY_AP = 1;
	/** C-D axis. */
	public static final int AXIS_CD = 0;
	/** C-V axis. */
	public static final int AXIS_CV = 1;
	/** C-A axis. */
	public static final int AXIS_CA = 2;
	/** C-P axis. */
	public static final int AXIS_CP = 3;
	/** DA compartment. */
	public static final int COMPARTMENT_DA = 0;
	/** DA compartment. */
	public static final int COMPARTMENT_DP = 1;
	/** DA compartment. */
	public static final int COMPARTMENT_VA = 2;
	/** DA compartment. */
	public static final int COMPARTMENT_VP = 3;
	/** Wing pouch. */
	public static final int COMPARTMENT_POUCH = 4;
	
	/** Name of the wing. */
	private String name_ = "";
	/** Age of the wing. */
	private String age_ = "";
	
	/** Boundary lengths. */
	private ArrayList<Double> boundaryLengths_ = new ArrayList<Double>(Collections.nCopies(NUM_BOUNDARIES, 0.));
	/** Axis lengths. */
	private ArrayList<Double> axisLengths_ = new ArrayList<Double>(Collections.nCopies(NUM_AXES, 0.));
	/** Compartments/pouch perimeters. */
	private ArrayList<Double> compartmentPerimeters_ = new ArrayList<Double>(Collections.nCopies(NUM_COMPARTMENTS, 0.));
	/** Compartments/pouch areas. */
	private ArrayList<Double> compartmentAreas_ = new ArrayList<Double>(Collections.nCopies(NUM_COMPARTMENTS, 0.));

	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WPouchStructureMeasurements() {}
	
	// ----------------------------------------------------------------------------
	
	/** Initialization. */
	public void initialize(WPouchStructure pouch) throws Exception {
		
		name_ = pouch.getName();
		age_ = pouch.getAge();
		
		setBoundaryLength(BOUNDARY_DV, pouch.getDVBoundary().length());
		setBoundaryLength(BOUNDARY_AP, pouch.getAPBoundary().length());
		
		setAxisLength(AXIS_CD, pouch.getCDAxis().length());
		setAxisLength(AXIS_CV, pouch.getCVAxis().length());
		setAxisLength(AXIS_CA, pouch.getCAAxis().length());
		setAxisLength(AXIS_CP, pouch.getCPAxis().length());
		
		setCompartmentPerimeter(COMPARTMENT_DA, pouch.getDACompartment().perimeter());
		setCompartmentPerimeter(COMPARTMENT_DP, pouch.getDPCompartment().perimeter());
		setCompartmentPerimeter(COMPARTMENT_VA, pouch.getVACompartment().perimeter());
		setCompartmentPerimeter(COMPARTMENT_VP, pouch.getVPCompartment().perimeter());
		setCompartmentPerimeter(COMPARTMENT_POUCH, pouch.perimeter());
		
		setCompartmentArea(COMPARTMENT_DA, pouch.getDACompartment().area());
		setCompartmentArea(COMPARTMENT_DP, pouch.getDPCompartment().area());
		setCompartmentArea(COMPARTMENT_VA, pouch.getVACompartment().area());
		setCompartmentArea(COMPARTMENT_VP, pouch.getVPCompartment().area());
		setCompartmentArea(COMPARTMENT_POUCH, pouch.area());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the two datasets match, otherwise return false (wing name and age not considered). */
	public boolean match(WPouchStructureMeasurements data) {
		
//		if (this.name_.compareTo(data.name_) != 0) return false;
//		if (this.age_.compareTo(data.age_) != 0) return false;
		
		for (int i = 0; i < NUM_BOUNDARIES; i++)
			if (Double.compare(this.boundaryLengths_.get(i), data.boundaryLengths_.get(i)) != 0) return false;
		
		for (int i = 0; i < NUM_AXES; i++)
			if (Double.compare(this.axisLengths_.get(i), data.axisLengths_.get(i)) != 0) return false;
		
		for (int i = 0; i < NUM_COMPARTMENTS; i++) {
			if (Double.compare(this.compartmentPerimeters_.get(i), data.compartmentPerimeters_.get(i)) != 0) return false;
			if (Double.compare(this.compartmentAreas_.get(i), data.compartmentAreas_.get(i)) != 0) return false;
		}
		
		return true;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves properties of the given wing pouch to XML file. */
	public void write(URI uri) throws Exception {
		
		WPouchStructureMeasurementsParser xml = new WPouchStructureMeasurementsParser();
		xml.write(uri, this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reads structural properties from XML file. */
	public static WPouchStructureMeasurements read(URI uri) throws Exception {
		
		WPouchStructureMeasurementsParser xml = new WPouchStructureMeasurementsParser();
		return xml.read(uri);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setName(String name) { name_ = name; }
	public String getName() { return name_; }
	
	public void setAge(String age) { age_ = age; }
	public String getAge() { return age_; }
	
	public void setBoundaryLength(int index, double length) { boundaryLengths_.set(index, length); }
	public double getBoundaryLength(int index) { return boundaryLengths_.get(index); }
	
	public void setAxisLength(int index, double length) { axisLengths_.set(index, length); }
	public double getAxisLength(int index) { return axisLengths_.get(index); }
	
	public void setCompartmentPerimeter(int index, double perimeter) { compartmentPerimeters_.set(index, perimeter); }
	public double getCompartmentPerimeter(int index) { return compartmentPerimeters_.get(index); }
	
	public void setCompartmentArea(int index, double area) { compartmentAreas_.set(index, area); }
	public double getCompartmentArea(int index) { return compartmentAreas_.get(index); }
}
