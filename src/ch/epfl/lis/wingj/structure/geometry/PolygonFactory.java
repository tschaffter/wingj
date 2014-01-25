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

import ij.process.FloatPolygon;

import java.awt.geom.Point2D;

/** 
 * Generates various Polygon shapes.
 * 
 * @version September 8, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class PolygonFactory {

	// ============================================================================
	// STATIC METHODS

	/** Returns a segment defined by two Point as a Polygon. */
	public static FloatPolygon createPolygonSegment(Point2D.Double begin, Point2D.Double end, int numPoints) {

		FloatPolygon segment = new FloatPolygon();
		double dx = (end.x - begin.x) / (double)numPoints;
		double dy = (end.y - begin.y) / (double)numPoints;
		for (int i = 0; i < numPoints; i++) {
			segment.addPoint(begin.x + i*dx, begin.y + i*dy);
		}
		return segment;
	}

	// ----------------------------------------------------------------------------

	/** Returns a circle defined by a center, radius and number of points as a Polygon. */
	public static FloatPolygon createPolygonCircle(Point2D.Double center, int radius, int numPoints) {

		FloatPolygon circle = new FloatPolygon();

		double oneAngle = 2*Math.PI / numPoints;
		for (int i = 0; i < numPoints; i++) {
			circle.addPoint(radius * Math.cos(-i * oneAngle) + center.x, 
					radius * Math.sin(-i * oneAngle) + center.y);
		}
		return circle;
	}
}
