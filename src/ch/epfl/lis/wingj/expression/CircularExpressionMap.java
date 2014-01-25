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

package ch.epfl.lis.wingj.expression;

import java.awt.geom.Point2D;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.geometry.Grid;
import ch.epfl.lis.wingj.utilities.Filters;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Represents an individual or circular expression map.
 * 
 * @version March 26, 2012
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class CircularExpressionMap extends ImagePlus {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public CircularExpressionMap() {
		
		super();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public CircularExpressionMap(String title, ImageProcessor ip) {
		
		super(title, ip);
	}
	
	// ----------------------------------------------------------------------------

    /** Returns the circular sampling density map oriented in the canonical position for a particular projection mode. */
	static public FloatProcessor computeSamplingDensityMap(int width, int height, Structure structure, Grid grid, int projectionMode) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		if (structure == null)
			throw new Exception("ERROR: Structure is null.");
		if (grid == null)
			throw new Exception("ERROR: Grid is null.");
		
		int gridLength = grid.getGridLength();
		
		if (gridLength == 0)
			throw new Exception("ERROR: Grid is empty.");
		
		double[] densityMap = new double[width*height];
		for (int i = 0; i < gridLength; i++) {
			for (int j = 0; j < gridLength; j++) {
				double x = grid.getCoordinate(i, j).x;
				double y = grid.getCoordinate(i, j).y;
				
				
				int x1 = (int)Math.floor(x);
				int y1 = (int)Math.floor(y);

				if(x1<0){ 
					x1 = 0;
				}else if(x1>width-2){
					x1 = width-2;
				}

				if(y1<0){
					y1 = 0;
				}else if(y1>height-2){
					y1 = height-2;
				}
				
				int x2 = x1+1;				
				int y2 = y1+1;		

				densityMap[x1 + width*y1] += (x-x1) * (y-y1);
				densityMap[x2 + width*y1] += (x2-x) * (y-y1);
				densityMap[x1 + width*y2] += (x-x1) * (y2-y);
				densityMap[x2 + width*y2] += (x2-x) * (y2-y);
			}
		}
		
		FloatProcessor densityProcessor = new FloatProcessor(width, height, densityMap) ;
		Filters.applyGaussianFilter(densityProcessor, settings.getExpression2DStitchingGridDensitySmoothing());
		densityProcessor.setInterpolationMethod(ImageProcessor.BICUBIC);
		
		double[] wPouchSquareDensityMap = new double[gridLength * gridLength];
		for(int i=0; i<gridLength; i++) {
			for(int j=0; j<gridLength; j++){
				wPouchSquareDensityMap[i + j*gridLength] = Math.min(Math.max(densityProcessor.getInterpolatedPixel(grid.getCoordinate(i, j).x, grid.getCoordinate(i, j).y), 0.0), 255.0);
			}
		}
		
		FloatProcessor wPouchCircularDensityMapProcessor = transformSquared2CircularExpressionMap(new FloatProcessor(gridLength, gridLength, wPouchSquareDensityMap));
		CircularExpressionMap.reorientExpressionMap(wPouchCircularDensityMapProcessor, grid, structure, projectionMode);
		
		return wPouchCircularDensityMapProcessor;
	}
	
	// ----------------------------------------------------------------------------

    /** Transforms the squared expression map to a circular one by compressing in the horizontal dimension. */
	static public FloatProcessor transformSquared2CircularExpressionMap(FloatProcessor fp) {
		
		int width = fp.getWidth();
		int height = fp.getHeight();
		int iCenter = (int) Math.round((width-1) / 2.0);
		int jCenter = (int) Math.round((height-1) / 2.0);
		int h = (int) Math.round(((width+height)/2.0-1) / 2.0);
		
		double[] wPouchWarpedExpressionMap = new double[width*height];
		fp.setInterpolationMethod(ImageProcessor.BICUBIC);
		
		for (int i = 0; i < width; i++) {
			double x = i - iCenter;
			for (int j = 0; j < height; j++) {
				double y = j - jCenter;
				double scalling = Math.sqrt(1 - (y/h)*(y/h));
				double xp = x/scalling + iCenter;
				wPouchWarpedExpressionMap[i + j*width] = fp.getInterpolatedPixel(xp, j);
			}
		}
		return new FloatProcessor(fp.getWidth(), fp.getHeight(), wPouchWarpedExpressionMap);
	}
	
	// ----------------------------------------------------------------------------

    /** Rotates and flips the expression map to the canonical orientation. */
	static public void reorientExpressionMap(FloatProcessor fp, Grid grid, Structure structure, int projectionMode) throws Exception {
		
		int Npoints = (grid.getGridLength() + 1) / 2;
		
		Point2D.Double A = structure.getDVBoundary().getFirstPoint();
		Point2D.Double P = structure.getDVBoundary().getLastPoint();
		Point2D.Double D = structure.getAPBoundary().getFirstPoint();
		Point2D.Double V = structure.getAPBoundary().getLastPoint();
		
		double[][] distanceMatrix = new double[2][4];
		
		distanceMatrix[0][0] = A.distance(grid.getCoordinate(0, Npoints-1).x, grid.getCoordinate(0, Npoints-1).y);
		distanceMatrix[0][1] = P.distance(grid.getCoordinate(0, Npoints-1).x, grid.getCoordinate(0, Npoints-1).y);
		distanceMatrix[0][2] = D.distance(grid.getCoordinate(0, Npoints-1).x, grid.getCoordinate(0, Npoints-1).y);
		distanceMatrix[0][3] = V.distance(grid.getCoordinate(0, Npoints-1).x, grid.getCoordinate(0, Npoints-1).y);
		
		distanceMatrix[1][0] = A.distance(grid.getCoordinate(Npoints-1, 0).x, grid.getCoordinate(Npoints-1, 0).y);
		distanceMatrix[1][1] = P.distance(grid.getCoordinate(Npoints-1, 0).x, grid.getCoordinate(Npoints-1, 0).y);
		distanceMatrix[1][2] = D.distance(grid.getCoordinate(Npoints-1, 0).x, grid.getCoordinate(Npoints-1, 0).y);
		distanceMatrix[1][3] = V.distance(grid.getCoordinate(Npoints-1, 0).x, grid.getCoordinate(Npoints-1, 0).y);
		
		if (distanceMatrix[0][1] < distanceMatrix[0][0] &&
			distanceMatrix[0][1] < distanceMatrix[0][2] &&
			distanceMatrix[0][1] < distanceMatrix[0][3]) {
			fp.flipHorizontal();
		}
		else {
			fp.flipHorizontal();
			fp.flipVertical();
		}
		
		if (!(distanceMatrix[1][2] < distanceMatrix[1][0] &&
			  distanceMatrix[1][2] < distanceMatrix[1][1] &&
			  distanceMatrix[1][2] < distanceMatrix[1][3])) {
			fp.flipVertical();
		}
		
		if(projectionMode == WJSettings.BOUNDARY_AP){
			fp.rotate(90);
			fp.flipHorizontal();  
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the circular expression map oriented in the canonical position for a particular projection mode. */
    static public FloatProcessor computeExpressionMap(ImagePlus expression, Structure structure, Grid grid, int projectionMode) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		if (structure == null)
			throw new Exception("ERROR: Structure is null.");
		if (grid == null)
			throw new Exception("ERROR: Grid is null.");
		
		int gridLength = grid.getGridLength();
		
		if (gridLength == 0)
			throw new Exception("ERROR: grid is empty.");
		
		FloatProcessor expressionProcessor = (FloatProcessor) expression.getProcessor().convertToFloat();
		expressionProcessor.setInterpolationMethod(ImageProcessor.BICUBIC);

		double[] wPouchSquareExpressionMap = new double[gridLength * gridLength];
		for(int i=0; i<gridLength; i++) {
			for(int j=0; j<gridLength; j++) {
				double x = grid.getCoordinate(i, j).x;
				double y = grid.getCoordinate(i, j).y;
				wPouchSquareExpressionMap[i + j*gridLength] = Math.min(Math.max(expressionProcessor.getInterpolatedPixel(x, y), 0.0), 255.0);
				if(settings.normalizeExpression()) wPouchSquareExpressionMap[i+j*gridLength] /= 255.0;
			}
		}
		
		FloatProcessor wPouchSquareExpressionMapProcessor = new FloatProcessor(gridLength, gridLength, wPouchSquareExpressionMap);
		FloatProcessor wPouchCircularExpressionMapProcessor = CircularExpressionMap.transformSquared2CircularExpressionMap(wPouchSquareExpressionMapProcessor);
		CircularExpressionMap.reorientExpressionMap(wPouchCircularExpressionMapProcessor, grid, structure, projectionMode);
		
		return wPouchCircularExpressionMapProcessor;
	}
}
