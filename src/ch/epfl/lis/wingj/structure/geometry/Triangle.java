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
 * Encapsulates the definition of a triangle.
 * 
 * @version November 9, 2011
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Triangle {

	/** Array containing the vertices of the triangle. */
	private Point2D.Double[] vertices_ = new Point2D.Double[3];
	/** Signed area. */
	private double signedArea_ = 0;

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public Triangle(Point2D.Double c, Point2D.Double p, Point2D.Double q){

		vertices_[0] = c;
		vertices_[1] = p;
		vertices_[2] = q;
		updateSignedArea();
	}

	// ----------------------------------------------------------------------------

	/** This method sets the coordinates of a certain vertex. */
	public void setVertex(int vertexNum, double x, double y) throws Exception{

		if(vertexNum<0 || vertexNum>=3)
			throw new Exception("WingJ Error: Incorrect number of nodes");

		vertices_[vertexNum].x = x;
		vertices_[vertexNum].y = y;			
		updateSignedArea();
	}

	// ----------------------------------------------------------------------------

	/** This method returns the perimeter of the triangle. */
	public double getPerimeter(){

		return vertices_[0].distance(vertices_[1])+vertices_[1].distance(vertices_[2])+vertices_[2].distance(vertices_[0]);
	}

	// ----------------------------------------------------------------------------

	/** This method returns the center of mass of the triangle. */
	public Point2D.Double getCentroid(){

		return new Point2D.Double((vertices_[0].x+vertices_[1].x+vertices_[2].x)/3.0, (vertices_[0].y+vertices_[1].y+vertices_[2].y)/3.0);
	}

	// ----------------------------------------------------------------------------

	/** This method updates the value of the signed area of the triangle. */
	private void updateSignedArea() {

		signedArea_ = 0.5*(-vertices_[1].x*vertices_[0].y
				+vertices_[2].x*vertices_[0].y
				+vertices_[0].x*vertices_[1].y
				-vertices_[2].x*vertices_[1].y
				-vertices_[0].x*vertices_[2].y
				+vertices_[1].x*vertices_[2].y);
	}

	// ----------------------------------------------------------------------------

	/** This method returns the x coordinate of a certain vertex. */
	public double getVertexX(int vertexNum) throws Exception { 

		if(vertexNum<0 || vertexNum>=3)
			throw new Exception("WingJ Error: Incorrect number of nodes");

		return vertices_[vertexNum].x; 
	}

	// ----------------------------------------------------------------------------

	/** This method returns the y coordinate of a certain vertex. */
	public double getVertexY(int vertexNum) throws Exception { 

		if(vertexNum<0 || vertexNum>=3)
			throw new Exception("WingJ Error: Incorrect number of nodes");

		return vertices_[vertexNum].y;
	}

	// ----------------------------------------------------------------------------

	/** 
	 * This method returns true if the vertices of the triangle are oriented clockwise,
	 * and false if the vertices of the triangle are oriented counterclockwise.
	 */
	public boolean isClockwise() { 

		return (signedArea_>0);
	}

	// ----------------------------------------------------------------------------

	/** This method returns the value of the area enclosed by the triangle. */
	public double getArea(){

		return Math.abs(signedArea_);
	}
}