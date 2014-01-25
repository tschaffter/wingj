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

import ij.ImagePlus;
import ij.process.ColorProcessor;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/** 
 * Describes a 2D structured grid where each element is a quadrilateral. The structure is
 * stored in a two-dimensional array with the coordinates of the grid-points. The number
 * of points of the grid in each dimension is the same.
 * 
 * @version November 9, 2011
 *
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class Grid {
	
	/** The equator on Earth is horizontal. EQUATOR_HORIZONTAL defines an horizontal equator. */
	public static final int EQUATOR_HORIZONTAL = 1;
	/** The equator on Earth is horizontal. EQUATOR_VERTICAL defines an vertical equator. */
	public static final int EQUATOR_VERTICAL = 2;

	/** Coordinates of the points of the grid. */
	private Point2D.Double[][] gridCoordinates_ = null;
	/** Number of points dimension-wise of the grid. */
	private int gridLength_ = 0;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Initialization. */
	private void initialize(int length) {
		
		gridCoordinates_ = new Point2D.Double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++)
				gridCoordinates_[i][j] = new Point2D.Double(0.0, 0.0);
		}	
	}
	
	// ----------------------------------------------------------------------------
	
	/** Draws a line between two points of the grid. */
	private void drawLine(ColorProcessor cp, int i1, int i2, int j1, int j2) {
		
		cp.drawLine((int)Math.round(gridCoordinates_[i1][j1].x),
				(int)Math.round(gridCoordinates_[i1][j1].y),
				(int)Math.round(gridCoordinates_[i2][j2].x),
				(int)Math.round(gridCoordinates_[i2][j2].y));
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public Grid() {}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Grid(int gridLength) {

		gridLength_ = gridLength;
		initialize(gridLength_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the two grids have the same coordinates. */
	public boolean match(Grid g) {
		
		if (this.gridLength_ != g.gridLength_) return false;
		for (int i = 0; i < this.gridLength_; i++) {
			for (int j = 0; j < this.gridLength_; j++)
				if (!this.getCoordinate(i, j).equals(g.getCoordinate(i, j))) return false;
		}
		return true;
	}

	// ----------------------------------------------------------------------------

	/** Draws the sampling grid on the given image and return the new ImagePlus object created. */
	public ImagePlus draw(ImagePlus imp, Color color, int downSampling) throws Exception {

		if (downSampling < 1)
			throw new Exception("downSampling factor not valid");

		ColorProcessor cp = null;
		String title = null;
		if(imp!=null){
			cp = (ColorProcessor) imp.getProcessor().duplicate().convertToRGB();
			title = imp.getTitle();
		}else{
			cp = new ColorProcessor(gridLength_, gridLength_);
			title = "";
		}
		cp.setColor(color);
		
		// draw lines in the first dimension
		for (int i = 0; i < gridLength_; i += downSampling) {
			if (i + downSampling < gridLength_) {
				for (int j = 0; j < gridLength_; j += downSampling) {
					if (j + downSampling < gridLength_) drawLine(cp, i, i, j, j+downSampling);
					else drawLine(cp, i, i, j, gridLength_-1);
				}
			}
		}
		// adjustment not to miss the final point
		for (int j = 0; j < gridLength_; j += downSampling) {
			if (j + downSampling < gridLength_) drawLine(cp, gridLength_-1, gridLength_-1, j, j+downSampling);
			else drawLine(cp, gridLength_-1, gridLength_-1, j, gridLength_-1);
		}
		// draw lines in the second dimension
		for (int j = 0; j < gridLength_; j += downSampling) {
			if (j + downSampling < gridLength_) {
				for (int i = 0; i < gridLength_; i += downSampling) {
					if (i + downSampling < gridLength_) drawLine(cp, i, i+downSampling, j, j);
					else drawLine(cp, i, gridLength_-1, j, j);
				}
			}
		}
		// adjustment not to miss the final point
		for (int i = 0; i < gridLength_; i += downSampling) {
			if (i + downSampling < gridLength_) drawLine(cp, i, i+downSampling, gridLength_-1, gridLength_-1);
			else drawLine(cp, i, gridLength_-1, gridLength_-1, gridLength_-1);
		}
		
		return (new ImagePlus(title+ "_grid_preview", cp));
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Saves the grid coordinates to text file.
	 * <p>
	 * X and Y coordinates of a Point2D.Double are written next to each other separated by "\t".
	 */
	public void write(String filename) throws Exception {
		
		FileWriter fw = new FileWriter(filename, false);
		double x = 0.;
		double y = 0.;
		for (int i = 0; i < gridLength_; i++) {
			x = gridCoordinates_[i][0].x;
			y = gridCoordinates_[i][0].y;
			fw.write(String.format("%.6f", x) + "\t" + String.format("%.6f", y));
			for (int j = 1; j < gridLength_; j++) {
				x = gridCoordinates_[i][j].x;
				y = gridCoordinates_[i][j].y;
				fw.write("\t" + String.format("%.6f", x) + "\t" + String.format("%.6f", y));
			}
			fw.write("\n");
		}
		fw.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Loads grid coordinates from text file.
	 * <p>
	 * X and Y coordinates of a Point2D.Double are written next to each other separated by "\t".
	 */
	public void read(String filename) throws Exception {
		
		// read the text file containing the grid coordinates
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();

        // set the grid coordinates
        initialize(lines.size());
        StringTokenizer st = null;
        for (int i = 0; i < gridLength_; i++) {
        	st = new StringTokenizer(lines.get(i), "\t");
        	int j = 0;
        	while (st.hasMoreTokens()) {
        		gridCoordinates_[i][j].x = Double.parseDouble(st.nextToken());
        		gridCoordinates_[i][j++].y = Double.parseDouble(st.nextToken());
        	 }
        }
	}
	
	// ----------------------------------------------------------------------------

	/** Creates a grid which looks like being wrapped around a sphere. */
	static public Grid generateSphereLikeGrid(int length, int equator) {

		Grid g = new Grid(length);

		// compute grid center and radius
		int iCenter = (int) Math.round((length-1) / 2.0);
		int jCenter = (int) Math.round((length-1) / 2.0);
		int h = (int) Math.round((length-1) / 2.0);
		
		double x = 0.;
		double y = 0.;
		double scaling = 0.;
		if (equator == Grid.EQUATOR_VERTICAL) {
			double yp = 0.;
			for (int i = 0; i < length; i++) {
				for(int j = 0; j < length; j++) {
					x = i - iCenter;
					y = j - jCenter;
					scaling = Math.sqrt(1 - (x/h)*(x/h));
					yp = y*scaling + jCenter;
					g.setCoordinate(i, j, new Point2D.Double(i, yp));
				}
			}
		}else if (equator == Grid.EQUATOR_HORIZONTAL) {
			double xp = 0.;
			for(int i = 0; i < length; i++) {
				for(int j = 0; j < length; j++) {
					x = i - iCenter;
					y = j - jCenter;
					scaling = Math.sqrt(1 - (y/h)*(y/h));
					xp = x*scaling + iCenter;
					g.setCoordinate(i, j, new Point2D.Double(xp, j));
				}
			}
		}
		return g;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	/** Sets the number of samples per dimension of the grid. */
	public int getGridLength() { return gridLength_; }

	/** Gets the coordinate position of an element of the grid. */
	public Point2D.Double getCoordinate(int i, int j) { return gridCoordinates_[i][j]; }
	
	/** Sets the coordinate position of an element of the grid.*/
	public void setCoordinate(int i, int j, Point2D.Double p) { gridCoordinates_[i][j] = p; }
	
	/** Sets the coordinate positions of a row in the the grid.*/
	public void setCoordinatesRow(int index, Point2D.Double[] line) {
		for (int i = 0; i < gridLength_; i++)
			setCoordinate(i, index, line[i]);
	}
	
	/** Sets the coordinate positions of a column in the the grid.*/
	public void setCoordinatesColumn(int index, Point2D.Double[] line) {
		for (int i = 0; i < gridLength_; i++)
			setCoordinate(index, i, line[i]);
	}
}