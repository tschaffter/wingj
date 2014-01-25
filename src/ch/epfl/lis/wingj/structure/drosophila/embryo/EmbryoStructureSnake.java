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

import big.ij.snake2D.Snake2DNode;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.StructureSnake;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureSnake;

/** 
 * Parametric structure model for the <i>Drosophila</i> embryo.
 * <p>
 * Extends WPouchStructureSnake because the structure implement there is
 * what we need.
 * 
 * @see WPouchStructureSnake
 * 
 * @version April 19, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class EmbryoStructureSnake extends WPouchStructureSnake {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public EmbryoStructureSnake() {
		
		super();
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public EmbryoStructureSnake(int imageWidth, int imageHeight){

		super(imageWidth,imageHeight);
		initialCompartments_ = new Compartment[4];
		initialBoundaries_ = new Boundary[4];
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Copy constructor.
	 * <p>
	 * Takes as input a StructureSnake to allow this class to be extended. 
	 */
	public EmbryoStructureSnake(StructureSnake s) {

		super(s);
	}

	// ----------------------------------------------------------------------------

	//FIXME My awesome hack (revise)
	/** The purpose of this method is to modify the number of control points of the snake. */
	@Override
	public EmbryoStructureSnake getResample(int M0) throws Exception {

		EmbryoStructureSnake s = new EmbryoStructureSnake(this);
		s.resample(M0);
		return s;
	}
	
	// ----------------------------------------------------------------------------

	/** Copy operator. */
	@Override
	public EmbryoStructureSnake copy() {

		return new EmbryoStructureSnake(this);
	}

	// ----------------------------------------------------------------------------

	/** Clone operator. */
	@Override
	public EmbryoStructureSnake clone() {

		return copy();
	}

	// ----------------------------------------------------------------------------

	/** Hide and freeze the wing center control point. */
	public void hideWingCenter(){
		
		Snake2DNode[] nodes = this.getNodes();
		int length = nodes.length;
		nodes[length-1].frozen = true;
		nodes[length-1].hidden = true;
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setEmbryoCenter(Point2D.Double center) { this.setWPouchCenter(center.x, center.y); }
	public Point2D.Double getEmbryoCenter() { return this.getWPouchCenter(); }
	
	public Point2D.Double getOrientationControlPoint() { return this.getWDiscCenter(); }
	
	public Compartment getEmbryoContour () throws Exception { return this.getWPouchContour(); }
}
