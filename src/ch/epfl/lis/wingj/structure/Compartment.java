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
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.tools.ContourTracer;

/** 
 * Describes a compartment (XY closed shape).
 * <p>
 * The conversion from [px] to [UNIT] is obtain using WJSettings.getScale() such that
 * 1 [px] = WJSettings.getScale() [UNIT]. The string identifier of [UNIT] is given by
 * WJSettings.getUnit(), for instance "um", "mm", etc.
 * 
 * @version November 7, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Compartment extends ij.process.FloatPolygon {
	
	/** Name or identifier of the compartment. */
	protected String name_ = "compartment";
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public Compartment(String name) {
		
		super();
		name_ = name;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy constructor. */
	public Compartment(Compartment c) {
		
		super();

		setFloatPolygon(c);
		name_ = c.name_ + "_copy";
	}

	// ----------------------------------------------------------------------------
	
	/** Copy operator. */
	public Compartment copy() {
		
		return new Compartment(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Clone operator. */
	@Override
	public Compartment clone() {
		
		return copy();
	}
	
	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Compartment(String name, FloatPolygon p) {
		
		super();
		setFloatPolygon(p);
		name_ = name;
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Compartment(String name, Polygon p) {
		
		super();
		setPolygon(p);
		name_ = name;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the area of the compartment in [UNIT^2]. */
	public final double area() {
		
		WJSettings settings = WJSettings.getInstance();
		
        int carea = 0;
        int iminus1;
        for (int i = 0; i < npoints; i++) {
            iminus1 = i-1;
            if (iminus1 < 0)
            	iminus1 = npoints-1;
            carea += (xpoints[i] + xpoints[iminus1]) * (ypoints[i] - ypoints[iminus1]);
        }
        return Math.abs((carea/2) * Math.pow(settings.getScale(), 2));
	}
	
	// ----------------------------------------------------------------------------

	/** Computes the perimeter of the compartment in [UNIT] */
	public final double perimeter() {
		
		WJSettings settings = WJSettings.getInstance();
        return (perimeterInPx() * settings.getScale());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the perimeter of the compartment in [px]. */
	public final double perimeterInPx() {
		
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
        dx = xp[0] - xp[nPoints-1];
        dy = yp[0] - yp[nPoints-1];
        length += Math.sqrt(dx * dx+dy * dy);
        
        return (length);
	}

	// ----------------------------------------------------------------------------
	
	 /** Returns the centroid or center of mass (com) of the compartment as a Point2D.Double. */
    public final Point2D.Double centroid() {
    	
        double cx = 0.0, cy = 0.0;
        double area = area() / Math.pow(WJSettings.getInstance().getScale(), 2); // back to [px^2]
        
        for (int i = 0; i < npoints-1; i++) {
            cx += (xpoints[i] + xpoints[i+1]) * (xpoints[i] * ypoints[i+1] - ypoints[i] * xpoints[i+1]);
            cy += (ypoints[i] + ypoints[i+1]) * (xpoints[i] * ypoints[i+1] - ypoints[i] * xpoints[i+1]);
        }

        // cx and cy could be negative depending on the orientation the points are read.
        // Here, pixel XY locations are positive.
        cx = Math.abs(cx / (6 * area));
        cy = Math.abs(cy / (6 * area));
  
        return new Point2D.Double(cx, cy);
    }
    
	// ----------------------------------------------------------------------------
	
	/** Returns a ShapeRoi that describes the compartment. */
	public final ShapeRoi toRoi() {
		
		WJSettings settings = WJSettings.getInstance();
		
		PolygonRoi polygonRoi = new PolygonRoi(this.toPolygon(), Roi.POLYGON);
		ShapeRoi shapeRoi = new ShapeRoi(polygonRoi);
		shapeRoi.setName(name_);
		shapeRoi.setStrokeColor(settings.getDefaultColor());
		shapeRoi.setStrokeWidth(settings.getDefaultStrokeWidth());
		return(shapeRoi);
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
	
	/**
	 * Computes the orientation of the simple polygon that constitutes the
	 * Compartment. The returned value is +1 if the polygon is parameterized
	 * clockwise, -1 if it is parameterized counterclockwise, or 0 if the polygon
	 * does not enclose any area.
	 */
	public final double orientation() {
		
        int carea = 0;
        int iminus1;
        for (int i = 0; i < npoints; i++) {
            iminus1 = i-1;
            if (iminus1 < 0) iminus1 = npoints-1;
            carea += (xpoints[i]+xpoints[iminus1]) * (ypoints[i]-ypoints[iminus1]);
        }
        return (Math.signum(carea));
    }

	// ----------------------------------------------------------------------------
	
	/** Reverses the order of the points defining the compartment. */
	public synchronized void reverse() {
		
		float[] xtmp = xpoints.clone();
		float[] ytmp = ypoints.clone();
		
		for (int i = 0; i < npoints; i++) {
			xpoints[i] = xtmp[npoints - i - 1];
			ypoints[i] = ytmp[npoints - i - 1];
		}
	}
	
	// ----------------------------------------------------------------------------

	/** Creates a binary mask with the specified dimensions. */
	public final boolean[] getBinaryMask(int width, int height){
		
		boolean[] mask = new boolean[width*height];
		for(int i=0; i<width; i++){
			for(int j=0; j<height; j++){
				if(contains(i,j)){
					mask[i+j*width] = true;
				}else{
					mask[i+j*width] = false;
				}
			}
		}
		return mask;
	}

	// ----------------------------------------------------------------------------

	/** Creates an arc-length resampled compartment. */
	public final Compartment createResampledCompartment(){
		
		Polygon polygon = this.toPolygon();
		
		int xMax = 0;
		int yMax = 0;
		for(int i=0; i<polygon.npoints; i++){
			if(polygon.xpoints[i]>xMax) xMax = polygon.xpoints[i];
			if(polygon.ypoints[i]>yMax) yMax = polygon.ypoints[i];
		}
		boolean[] mask = getBinaryMask(xMax+2, yMax+2);
		
		ContourTracer tracer = new ContourTracer(mask, xMax+2, yMax+2);
		tracer.trace();

		Compartment resampledCompartment = new Compartment("", new Polygon(tracer.getXCoordinates(), tracer.getYCoordinates(), tracer.getNPoints()));
		return(resampledCompartment);
	}
	
	// ----------------------------------------------------------------------------

	/** Creates the convex hull of the compartment. */
	public final Compartment createConvexHull(){
		
		PolygonRoi currentRoi = new PolygonRoi(this.toPolygon(), Roi.POLYGON); 
   		Polygon convexHull = currentRoi.getConvexHull();
		return new Compartment(name_+"_convex_hull", convexHull);
	}
	
	// ----------------------------------------------------------------------------

	/**
	  * Resets this <code>Polygon</code> object to an empty polygon.
	  * The coordinate arrays and the data in them are left untouched
	  * but the number of points is reset to zero to mark the old
	  * vertex data as invalid and to start accumulating new vertex
	  * data at the beginning.
	  * <p>
	  * All internally-cached data relating to the old vertices
	  * are discarded.
	  * <p>
	  * Note that since the coordinate arrays from before the reset
	  * are reused, creating a new empty <code>Polygon</code> might
	  * be more memory efficient than resetting the current one if
	  * the number of vertices in the new polygon data is significantly
	  * smaller than the number of vertices in the data from before the
	  * reset.
	  */
	public void reset() {
		npoints = 0;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the coordinates of the compartment contour to text file in format "X \tab Y". */
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
	
	/** Translates the compartment using the given vector. */
	public void translateCompartment(double dx, double dy) throws Exception {
		
		for (int i = 0; i < npoints; i++) {
			xpoints[i] += dx;
			ypoints[i] += dy;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns an array containing the points xy0 and xy1 which are the top-left
	 * and bottom-right points of the rectangle including the compartment.
	 */
	public List<Point2D.Double> getAoiPoints() throws Exception {
		
		Point2D.Double xy0 = new Point2D.Double(Double.MAX_VALUE, Double.MAX_VALUE);
		Point2D.Double xy1 = new Point2D.Double(Double.MIN_VALUE, Double.MIN_VALUE);
		
		double x = 0.;
		double y = 0.;
		for (int i = 0; i < npoints; i++) {
			x = xpoints[i];
			y = ypoints[i];
			
			if (x < xy0.x) xy0.x = x;
			else if (x > xy1.x) xy1.x = x;
			
			if (y < xy0.y) xy0.y = y;
			else if (y > xy1.y) xy1.y = y;
		}
		
//		// want entire
//		xy0.x = Math.floor(xy0.x);
//		xy0.y = Math.floor(xy0.y);
//		xy1.x = Math.ceil(xy1.x);
//		xy1.y = Math.ceil(xy1.y);
		
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		points.add(xy0);
		points.add(xy1);
		return points;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the horizontal width of the compartment (in pixels). */
	public double getHorizontalWidth() throws Exception {
		
		List<Point2D.Double> aoi = getAoiPoints();
		return aoi.get(1).x - aoi.get(0).x;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the vertical height of the compartment (in pixels). */
	public double getVerticalHeight() throws Exception {
		
		List<Point2D.Double> aoi = getAoiPoints();
		return aoi.get(1).y - aoi.get(0).y;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a deep copy of the given Compartment[]. */
	public static Compartment[] deepCopyCompartmentArray(Compartment[] ori) {
		
		if (ori == null)
			return null;
		
		Compartment[] copy = new Compartment[ori.length];
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

	public synchronized void setPolygon(Polygon p) {
		int nPoints = p.npoints;
		int[] xp = p.xpoints;
		int[] yp = p.ypoints;
		for(int i = 0; i < nPoints; i++)
			addPoint(xp[i], yp[i]);
	}
	
	public synchronized void setFloatPolygon(FloatPolygon p) {
		int nPoints = p.npoints;
		float[] xp = p.xpoints;
		float[] yp = p.ypoints;
		for(int i = 0; i < nPoints; i++)
			addPoint(xp[i], yp[i]); // deep copy
	}
}
