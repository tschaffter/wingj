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

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;

import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ch.epfl.lis.wingj.WJSettings;

/** 
 * Describes a compartment boundary (XY trajectory).
 * <p>
 * The conversion from [px] to [UNIT] is obtain using WJSettings.getScale() such that
 * 1 [px] = WJSettings.getScale() [UNIT]. The string identifier of [UNIT] is given by
 * WJSettings.getUnit(), for instance "um", "mm", etc.
 * 
 * @version October 21, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Boundary extends ij.process.FloatPolygon {

	/** Name or identifier of the boundary. */
	protected String name_ = "";
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public Boundary() {}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Boundary(String name) {
		
		super();
		name_ = name;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy constructor. */
	public Boundary(Boundary b) {
		
		super();
		initialize(b);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialization based on a given boundary. */
	public void initialize(Boundary b) {
		
		setPolygon(b);
		name_ = b.name_ + "_copy";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy operator. */
	public Boundary copy() {
		
		return new Boundary(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Clone operator. */
	@Override
	public Boundary clone() {
		
		return copy();
	}
	
	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Boundary(String name, FloatPolygon p) {
		
		super();
		setPolygon(p);
		name_ = name;
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Boundary(String name, List<Point2D> polyline) {
		
		super();
		setPolygon(polyline);
		name_ = name;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Boundary(String name, Vector<Point2D.Double> polyline) {
		
		super();
   		for(int i=0; i< polyline.size(); i++){
   			addPoint(polyline.elementAt(i).x, polyline.elementAt(i).y);
   		}
		name_ = name;
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Boundary(String name, Point2D.Double[] polyline) {
		
		super();
   		for(int i=0; i< polyline.length; i++){
   			addPoint(polyline[i].x, polyline[i].y);
   		}
		name_ = name;
	}

	// ----------------------------------------------------------------------------
	
	/** Defines equal operator. */
	public boolean equals(Boundary s2) {
		
		double l1 = length();
		double l2 = s2.length();
		
		boolean sameName = name_ == s2.name_ ? true : false;
		boolean sameLength = l1 == l2 ? true : false;
		
		return (sameName && sameLength);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the first Point of the boundary. */
	public Point2D.Double getFirstPoint() throws Exception {
		
		int n = npoints;
		if (n < 2)
			throw new Exception("ERROR: Boundary must have at least two points.");
		
		return new Point2D.Double(xpoints[0], ypoints[0]);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the last Point of the boundary. */
	public Point2D.Double getLastPoint() throws Exception {
		
		int n = npoints;
		if (n < 2)
			throw new Exception("ERROR: Boundary must have at least two points.");
		
		return new Point2D.Double(xpoints[n-1], ypoints[n-1]);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reverses the order of the points defining the boundary. */
	public synchronized void reverse() {
		
		float[] xtmp = xpoints.clone();
		float[] ytmp = ypoints.clone();
		
		for (int i = 0; i < npoints; i++) {
			xpoints[i] = xtmp[npoints - i - 1];
			ypoints[i] = ytmp[npoints - i - 1];
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the length of the boundary in [UNIT]. */
	public final double length() {
		
		WJSettings settings = WJSettings.getInstance();
		double scale = settings.getScale();
		return lengthInPx() * scale;
	}

	// ----------------------------------------------------------------------------
	
	/** Computes the length of the boundary in [px]. */
	public final double lengthInPx() {
		
		double length = 0;
        int nPoints = npoints;
        float[] xp = xpoints;
        float[] yp = ypoints;
		double dx, dy;
		for (int i = 0; i < nPoints-1; i++) {
            dx = xp[i+1] - xp[i];
            dy = yp[i+1] - yp[i];
            length += Math.sqrt(dx * dx+dy * dy);
        }
		return length;
	}	

	// ----------------------------------------------------------------------------
	
	/** Returns a ShapeRoi which represents the boundary. */
	public final ShapeRoi toRoi() {
		
		WJSettings settings = WJSettings.getInstance();
		
		PolygonRoi polygonRoi = new PolygonRoi(this.toPolygon(), Roi.POLYLINE);
		ShapeRoi shapeRoi = new ShapeRoi(polygonRoi);
		shapeRoi.setName(name_);
		shapeRoi.setStrokeColor(settings.getDefaultColor());
		shapeRoi.setStrokeWidth(settings.getDefaultStrokeWidth());
		return shapeRoi;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a Polygon which represents the boundary. */
	public final Polygon toPolygon() {
		
		Polygon polygon = new Polygon();
		for (int i = 0; i < npoints; i++){
			polygon.addPoint((int)Math.round(xpoints[i]), (int)Math.round(ypoints[i]));
		}
		
		return polygon;
	}

	// ----------------------------------------------------------------------------
	
	/** Returns a new Boundary resampled in arc length. */
	public final Boundary resample(int nPoints) throws Exception {
		
		if(nPoints<0)
			throw new Exception("ERROR: A Boundary cannot contain a negative number of points.");
		
		if (npoints <= 0)
			throw new Exception("ERROR: Boundary object is empty.");

		Point2D.Double[] resampledCurve = new Point2D.Double[nPoints];

		if(nPoints==0)
			return new Boundary(name_, resampledCurve);
		
		if(nPoints==1){
			resampledCurve[0] = new Point2D.Double(xpoints[0], ypoints[0]);
			return new Boundary(name_, resampledCurve);
		}
		
		if(npoints == 1){
			for(int i=0; i<nPoints; i++){
				resampledCurve[i] = new Point2D.Double(xpoints[0], ypoints[0]);
			}
			return new Boundary(name_, resampledCurve);
		}
		
		double[] arcLength = new double[npoints];
		for(int i=1; i<npoints; i++){
			arcLength[i] = arcLength[i-1] + Math.sqrt((xpoints[i]-xpoints[i-1])*(xpoints[i]-xpoints[i-1])+(ypoints[i]-ypoints[i-1])*(ypoints[i]-ypoints[i-1]));
		}
		
		double delta = arcLength[npoints-1]/(nPoints-1);
		int index = 0;
		for(int i=0; i<nPoints; i++){
			double t = delta*i;
			boolean found = false;
			for(; index<(npoints-1) && !found; index++){
				if(arcLength[index]<=t && arcLength[index+1]>=t){
					found = true;
				}
			}
			index--;
			resampledCurve[i] = new Point2D.Double(
					((arcLength[index+1]-t)*xpoints[index]+(t-arcLength[index])*xpoints[index+1])/(arcLength[index+1]-arcLength[index]),
					((arcLength[index+1]-t)*ypoints[index]+(t-arcLength[index])*ypoints[index+1])/(arcLength[index+1]-arcLength[index]));
		}
		return new Boundary(name_, resampledCurve);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the sub-boundary specified by the coefficient [0.,1.] of the actual boundary length.
	 * <p>
	 * For instance, Boundary.getSubBoundaryFromCoeff(0.6) returns 60% of the original boundary.
	 */
	public final Boundary getSubBoundaryFromCoeff(double coeff) {
		
		if (coeff < 0.) coeff = 0.;
		else if (coeff > 1.) coeff = 1.;
		return getSubBoundary(coeff * length());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the sub-boundary specified by the given length in [UNIT]. */
	public final Boundary getSubBoundary(double length) {
			
		if (npoints < 2) {
			WJSettings.log("WARNING: Boundary must have at least two points.");
			return null;
		}
			
		Boundary c = new Boundary(getName() + "_sub");
		c.addPoint(xpoints[0], ypoints[0]);
		double l = 0.;
		for (int i = 0; i < npoints - 1; i++) {
			
			c.addPoint(this.xpoints[i+1], this.ypoints[i+1]);
			l = c.length();
			
			if (l > length) break;
		}
		return c;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new Boundary which is the combination of THIS and the given boundary. */
	public final Boundary join(Boundary b) {
		
		Boundary c = new Boundary(this.getName() + "_+_" + b.getName());
		for (int i = 0; i < this.npoints; i++)
			c.addPoint(this.xpoints[i], this.ypoints[i]);
		for (int i = 0; i < b.npoints; i++)
			c.addPoint(b.xpoints[i], b.ypoints[i]);
		
		return c;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Appends the given Boundary to the current one. */
	public final void append(Boundary b) {
		
		for (int i = 0; i < b.npoints; i++)
			this.addPoint(b.xpoints[i], b.ypoints[i]);
	}

	// ----------------------------------------------------------------------------

	/** Computes a smoothed version of the normal vectors for each point of the boundary. */
	public Point2D.Double[] getSmoothedNormalVectors(){
		//compute raw estimation of the tangent vectors
		double[] dx = new double[npoints];
		double[] dy = new double[npoints];
		
		dx[0] = xpoints[1]-xpoints[0];
		dy[0] = ypoints[1]-ypoints[0];
		for(int i=1; i<npoints-1; i++) {
   			dx[i] = (xpoints[i+1]-xpoints[i-1])/2.0;
   			dy[i] = (ypoints[i+1]-ypoints[i-1])/2.0;
		}
		dx[npoints-1] = xpoints[npoints-1]-xpoints[npoints-2];
		dy[npoints-1] = ypoints[npoints-1]-ypoints[npoints-2];
		
		//smooth the estimation of the tangent vectors
		double[] dxSmoothed = new double[npoints];
		double[] dySmoothed = new double[npoints];
		
		dxSmoothed[0] = (3.0*dx[0]+dx[1])/4.0;
		dySmoothed[0] = (3.0*dy[0]+dy[1])/4.0;
		for(int i=1; i<npoints-1; i++) {
			dxSmoothed[i] = (dx[i-1]+4.0*dx[i]+dx[i+1])/6.0;
			dySmoothed[i] = (dy[i-1]+4.0*dy[i]+dy[i+1])/6.0;
		}
		dxSmoothed[npoints-1] = (3.0*dx[npoints-1]+dx[npoints-2])/4.0;
		dySmoothed[npoints-1] = (3.0*dy[npoints-1]+dy[npoints-2])/4.0;

		//estimate unit normal vectors
		Point2D.Double[] normalVectors = new Point2D.Double[npoints];
		for(int i=0; i<npoints; i++) {
			double norm = Math.sqrt(dxSmoothed[i]*dxSmoothed[i]+dySmoothed[i]*dySmoothed[i]);
			normalVectors[i] = new Point2D.Double(-dySmoothed[i]/norm, dxSmoothed[i]/norm);
		}
		return(normalVectors);
	}

	// ----------------------------------------------------------------------------

	/** Creates a new boundary as a result of the intersection with a compartment. */
	public final Boundary restrictToCompartment(Compartment boundingRegion){

		Vector<Point2D.Double> polyline = new Vector<Point2D.Double>();
		for(int i=0; i<npoints; i++){
			float x = xpoints[i];
			float y = ypoints[i];
			if(boundingRegion.contains(x, y)){
				polyline.addElement(new Point2D.Double(x, y));
			}
		}
		return new Boundary(name_, polyline);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns an expand version of the current boundary in the direction of the normal of the tangent. */
	@Deprecated
	public final Boundary expand(double d) {
	
		Boundary c = new Boundary(this.getName() + "_contract");
		
		Point2D.Double[] normalVectors = getSmoothedNormalVectors();
		for(int i=0; i<npoints; i++) {
			double x = xpoints[i] + d*normalVectors[i].x;
			double y = ypoints[i] + d*normalVectors[i].y;
   			c.addPoint(x, y);
		}
		return c;
	}

	// ----------------------------------------------------------------------------
	
	/** Translates the vertices of the Polygon by deltaX along the x axis and by deltaY along the y axis. */
	public void translate(double dx, double dy) {
		
		for (int i = 0; i < npoints; i++) {
			xpoints[i] += dx;
			ypoints[i] += dy;
		}
	}

	// ----------------------------------------------------------------------------

	/** Erases the points that do not belong to a certain domain. */
	public void trimEnd(Compartment c) {
		
		Boundary b = new Boundary(this.getName());

		boolean out = false;
		for(int i=0; i<npoints && !out; i++){
			if(c.contains(xpoints[i], ypoints[i])){
				b.addPoint(xpoints[i], ypoints[i]);
			}else{
				out = true;
			}
		}
		this.npoints = b.npoints;
		this.xpoints = b.xpoints;
		this.ypoints = b.ypoints;
	}
	
	// ----------------------------------------------------------------------------

	/** Erases the points that do not belong to a certain domain. */
	public void trimEnd(double delta) throws Exception {
		
		if (npoints == 0)
			throw new Exception("ERROR: Cannot trim a boundary that has no points.");
		
		Boundary b = new Boundary(this.getName());
		double[] accumulatedDistance = new double[npoints];
		accumulatedDistance[0]=0;
		for(int i=1; i<npoints; i++){
			accumulatedDistance[i] = accumulatedDistance[i-1] + Math.sqrt((xpoints[i]-xpoints[i-1])*(xpoints[i]-xpoints[i-1])+(ypoints[i]-ypoints[i-1])*(ypoints[i]-ypoints[i-1]));
		}
		double end = accumulatedDistance[npoints-1]-delta;
		
		for(int i=0; i<npoints && (accumulatedDistance[i]<end); i++){
			b.addPoint(xpoints[i], ypoints[i]);
		}
		this.npoints = b.npoints;
		this.xpoints = b.xpoints;
		this.ypoints = b.ypoints;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the coordinates of the boundary to text file in format "X \tab Y". */
	public void writePoints(URI uri) throws Exception {
		
		if (this.npoints < 1)
			return;
		
		FileWriter fstream = new FileWriter(new File(uri));
		BufferedWriter out = new BufferedWriter(fstream);
	     		
		String content = Double.toString(this.xpoints[0]) + "\t" + Double.toString(this.ypoints[0]);
		for (int i = 1; i < this.npoints; i++)
			content += "\n" + Double.toString(this.xpoints[i]) + "\t" + Double.toString(this.ypoints[i]);

		out.write(content);
		out.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Empties this FloatPolygon. */
	public void empty() {
		
		npoints = 0;
		xpoints = new float[10];
		ypoints = new float[10];
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a deep copy of the given Boundary[]. */
	public static Boundary[] deepCopyBoundaryArray(Boundary[] ori) {
		
		if (ori == null)
			return null;
		
		Boundary[] copy = new Boundary[ori.length];
		for (int i = 0; i < copy.length; i++) {
			if (ori[i] == null)
				copy[i] = null;
			else
				copy[i] = ori[i].clone();
		}
		
		return copy;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public synchronized void setName(String name) { name_ = name; }
	public String getName() { return name_; }
	
	private synchronized void setPolygon(FloatPolygon p) {
		
		// empty this
		empty();
		
		int nPoints = p.npoints;		
		float[] xp = p.xpoints;
		float[] yp = p.ypoints;
		for (int i = 0; i < nPoints; i++)
			addPoint(xp[i], yp[i]);
	}

	private synchronized void setPolygon(List<Point2D> p) {
		
		empty();
		
		Iterator<Point2D> itr = p.iterator();
		while(itr.hasNext()) {
			Point2D point = itr.next(); 
		    addPoint(point.getX(), point.getY());
		} 
	}
}