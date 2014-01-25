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

package ch.epfl.lis.wingj.structure.tools;

import ij.IJ;

import java.awt.Point;
import java.awt.Polygon;
import java.util.Vector;

/** 
 * Square tracing algorithm.
 * <p>
 * The idea behind the square tracing algorithm is very simple; this could be 
 * attributed to the fact that the algorithm was one of the first attempts to 
 * extract the contour of a binary pattern.
 * <p>
 * Source: http://www.imageprocessingplace.com/downloads_V3/root_downloads/tutorials/contour_tracing_Abeer_George_Ghuneim/square.html
 * 
 * @version March 5, 2013
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ContourTracer {

	/** Binary mask of the object to trace. */
	private boolean[] binaryMask_ = null;
	/** Width of the input binary mask. */
	private int width_ = 0;
	/** Height of the input binary mask. */
	private int height_ = 0;

	/** Path with the outline of the traced object. */
	private Vector<Point> path_ = null;

	/** Identifier of the up direction. */
	private static final int UP = 0;
	/** Identifier of the down direction. */
	private static final int DOWN = 1;
	/** Identifier of the left direction. */
	private static final int LEFT = 2;
	/** Identifier of the right direction. */
	private static final int RIGHT = 3;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public ContourTracer(boolean[] T, int width, int height){

		binaryMask_ = T;
		width_ = width;
		height_ = height;
		path_ = new Vector<Point>();
	}
	
	// ----------------------------------------------------------------------------

	/** Constructor. */
	public ContourTracer(float[] T, int width, int height){
		
		this.binaryMask_ = new boolean[width*height];
		for(int i =0; i<T.length; i++){
   			if(T[i]!=0){
   				this.binaryMask_[i] = true;
   			}else{
   				this.binaryMask_[i] = false;
   			}
   		}
		
		width_ = width;
		height_ = height;
		path_ = new Vector<Point>();
	}
	
	// ----------------------------------------------------------------------------

	/** Traces the outline of a binary region using the Square Tracing Algorithm. */
	public void trace() {
		
		int sx = 0;
		int sy = 0;
		int px = 0;
		int py = 0;
		int direction = UP;
		
		boolean found = false;
		for(int y=height_-1; y>=0 && !found; y--){
			for(int x=0; x<width_ && !found; x++){
				if(binaryMask_[x+y*width_]){
					found = true;
					sx = x;
					sy = y;
					px = x;
					py = y;
					path_.addElement(new Point(x,y));
				}
			}
		}
		
		direction = RIGHT;
		px--;
		boolean cond;
		while(!(sx==px && sy==py)){
			
			if(px<0 || px>=width_ || py<0 || py>=height_){
				cond=false;
			}else{
				cond = binaryMask_[px+py*width_]; 
			}
			if(cond){
				path_.addElement(new Point(px,py));
				switch(direction){
					case UP:
						px++;
						direction = LEFT;
						break;
					case DOWN:
						px--;
						direction = RIGHT;
						break;
					case LEFT:
						py--;
						direction = DOWN;
						break;
					case RIGHT: 
						py++;
						direction = UP;
						break;
					default:
						IJ.error("Orientation unknown.");
						break;
				}
			}else{
				switch(direction){
				case UP:
					px--;
					direction = RIGHT;
					break;
				case DOWN:
					px++;
					direction = LEFT;
					break;
				case LEFT:
					py++;
					direction = UP;
					break;
				case RIGHT: 
					py--;
					direction = DOWN;
					break;
				default:
					IJ.error("Orientation unknown.");
					break;
				}
			}
		}
		removeDuplicates();
	}

	// ============================================================================
	// PRIVATE METHODS
	
	/** Removes duplicated points. */
	private void removeDuplicates() {
		if(path_!=null){
			if(path_.size()>1){
				for(int i=0; i<path_.size()-1; i++){
					if(path_.elementAt(i).x == path_.elementAt(i+1).x && path_.elementAt(i).y == path_.elementAt(i+1).y){
						path_.removeElementAt(i+1);
						i--;
					}
				}
			}
		}
	}

	// ============================================================================
	// SETTERS AND GETTERS

	/** Retrieves the trace. */
	public Polygon getTrace(){
		
		return new Polygon(getXCoordinates(), getYCoordinates(), getNPoints());
	}

	// ----------------------------------------------------------------------------

	/** Retrieves the x's coordinates of the trace. */
	public int[] getXCoordinates() {
		
		int nPoints = path_.size();
		int[] xCoordinates = new int[nPoints];
		for(int i=0; i<path_.size(); i++){
			xCoordinates[i] = path_.elementAt(i).x;
		}
		return xCoordinates;
	}
	
	// ----------------------------------------------------------------------------

	/** Retrieves the y's coordinates of the trace. */
	public int[] getYCoordinates() {
		
		int nPoints = path_.size();
		int[] yCoordinates = new int[nPoints];
		for(int i=0; i<nPoints; i++){
			yCoordinates[i] = path_.elementAt(i).y;
		}
		return yCoordinates;
	}	
	
	// ----------------------------------------------------------------------------
	
	/** Retrieves the total number of points in the trace. */
	public int getNPoints() {
		
		return path_.size();
	}
}