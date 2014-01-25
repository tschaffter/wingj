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

/** 
 * Describes a segment defined by two Points objects.
 * 
 * @version September 8, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Segment {

	/** First point of the segment, also called point K. */
	protected Point2D.Double begin_ = null;
	/** End point of the segment, also called point L. */
	protected Point2D.Double end_ = null;

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public Segment(Point2D.Double begin, Point2D.Double end) {

		begin_ = begin;
		end_ = end;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Overrides toString() method.
	 * @return String "(x1,y1) to (x2,y2)"
	 */
	@Override
	public String toString() {

		String str = "(" + begin_.x + "," + begin_.y + ") to (" + end_.x + "," + end_.y + ")";
		return str;
	}

	// ----------------------------------------------------------------------------

	/** Defines equal operator. */
	public boolean equals(Segment s2) {

		boolean sameBegin = begin_ == s2.begin_ ? true : false;
		boolean sameEnd = end_ == s2.end_ ? true : false;

		return (sameBegin && sameEnd);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Computes the intersection between this segment and the given segment. 
	 * @return Point Where the segments intersect, or null if they don't.
	 */
	public Point2D.Double intersection(Segment s2) throws Exception {

		if (this == s2)
			throw new Exception("ERROR: Cannot compute the intersection of two identical segments.");

		double x1 = begin_.x;
		double y1 = begin_.y;
		double x2 = end_.x;
		double y2 = end_.y;
		double x3 = s2.begin_.x;
		double y3 = s2.begin_.y;
		double x4 = s2.end_.x;
		double y4 = s2.end_.y;

		double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
		if (d == 0) return null;

		double xi = ((x3-x4)*(x1*y2-y1*x2)-(x1-x2)*(x3*y4-y3*x4))/d;
		double yi = ((y3-y4)*(x1*y2-y1*x2)-(y1-y2)*(x3*y4-y3*x4))/d;

		Point2D.Double p = new Point2D.Double(xi, yi);
		if (xi < Math.min(x1, x2) || xi > Math.max(x1, x2)) return null;
		if (xi < Math.min(x3, x4) || xi > Math.max(x3, x4)) return null;
		return p;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Computes the distance between the two points of this segment.
	 * @return double The distance between two points of the segment.
	 */
	public double distance() {

		return distance(begin_, end_);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Computes the distance between the two given points.
	 * @return double The distance between two points.
	 */
	public static double distance(Point2D.Double a, Point2D.Double b) {

		return Math.sqrt((b.x-a.x)*(b.x-a.x) + (b.y-a.y)*(b.y-a.y));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Moves the given node closer or further to the reference point using the given amplitude A. */
	public static void movePointFromReference(Point2D.Double pt, Point2D.Double ref, double A) throws Exception {

		// you don't use abs value and use the still point as the first one of the subtraction
		double deltaX = pt.getX() - ref.getX();
		double deltaY = pt.getY() - ref.getY();
		double d = pt.distance(ref);
		double coeff = (d+A)/d;

		pt.setLocation(ref.getX() + coeff*deltaX, ref.getY() + coeff*deltaY);
	}
}
