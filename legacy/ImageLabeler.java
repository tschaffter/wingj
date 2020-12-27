/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

WingJ is licensed under a
Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.

You should have received a copy of the license along with this
work. If not, see http://creativecommons.org/licenses/by-nc-nd/3.0/.

If this software was useful for your scientific work, please cite our paper(s)
listed on http://lis.epfl.ch/wingj.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.utilities;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

/**
 * Implements an image labeler using digital 4-connected topology
 * and computes morphological features of the labeled regions.
 *
 * @version September 28, 2011
 *
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ImageLabeler {

	/** Denotes the background. */
	public static int BACKGROUND = 0;
	/** Denotes the foreground. */
	public static int FOREGROUND = 255;

	/** Initial image without labels. */
	private ByteProcessor initialMap_;
	/** Final image with labels. */
	private FloatProcessor labeledMap_;
	/** Total number of labels. */
	private int nLabels_ = 0;

	/** Areas of the labeled regions. */
	private int[] areas_ = null;
	/** Perimeters of the labeled regions. */
	private int[] perimeters_ = null;
	/** Length of the shared boundaries of the labeled regions. */
	private int[][] regionInterfaceLengths_ = null;
	/** Determines which regions are in contact with the boundaries of the image. */
	private boolean[] isUnbounded_ = null;
	/** Minimum x value for each region. */
	private int[] xMin_ = null;
	/** Minimum y value for each region. */
	private int[] yMin_ = null;
	/** Maximum x value for each region. */
	private int[] xMax_ = null;
	/** Maximum y value for each region. */
	private int[] yMax_ = null;
	/** Angles of the orientation of the ellipse fitting for each region. */
	private double[] ellipseAngles_ = null;
	/** Centers of the ellipse fitting for each region. */
	private Point2D.Double[] ellipseCenters_ = null;
	/** Major axis of the ellipse fitting for each region. */
	private double[] ellipseMajorAxis_ = null;
	/** Minor axis of the ellipse fitting for each region. */
	private double[] ellipseMinorAxis_ = null;

	/** Denotes the state of the vertex in the class equivalence labeling. */
	private enum VertexState { 	UNVISITED,
								VISITING,
								VISITED    }

	// ============================================================================
	// PUBLIC METHODS

	public ImageLabeler(ByteProcessor initialMap){

		initialMap_ = initialMap;
	}

	// ============================================================================

	/** Returns a labeling using 4-connected digital topology. */
	public void label4ConnectedComponents() throws Exception {

		if (initialMap_ == null)
			throw new Exception("ERROR: initialMap_ is null.");

		int width = initialMap_.getWidth();
		int height = initialMap_.getHeight();

		byte[] initialMapPixels = (byte[])initialMap_.getPixels();
		double[] labeledMapPixels = new double[width*height];

		boolean found = false;
		int initialX = 0;
		int initialY = 0;

		for(int i=0; i<width && !found; i++){
			for(int j=0; j<height && !found; j++){
				if((initialMapPixels[i+j*width]&0xFF)==FOREGROUND){
					found = true;
					initialX = i;
					initialY = j;
				}
			}
		}

		Set<Point> E = new HashSet<Point>();

		if(found){
			int currentColor = 1;
			for(int i=initialX; i<width; i++){
				for(int j=initialY; j<height; j++){
					if((initialMapPixels[i+width*j]&0xFF)==FOREGROUND){
						if(i==0 && j==0){
							labeledMapPixels[0] = currentColor;
							E.add(new Point(currentColor,currentColor));
							currentColor++;
						}else if(i==0){
							if((initialMapPixels[width*(j-1)]&0xFF)==BACKGROUND){
								labeledMapPixels[width*j] = currentColor;
								E.add(new Point(currentColor,currentColor));
								currentColor++;
							}else if((initialMapPixels[width*(j-1)]&0xFF)==FOREGROUND){
								labeledMapPixels[i+width*j] = labeledMapPixels[width*(j-1)];
							}
						}else if(j==0){
							if((initialMapPixels[i-1+width*j]&0xFF)==BACKGROUND){
								labeledMapPixels[i] = currentColor;
								E.add(new Point(currentColor,currentColor));
								currentColor++;
							}else if((initialMapPixels[i-1+width*j]&0xFF)==FOREGROUND){
								labeledMapPixels[i] = labeledMapPixels[i-1];
							}
						}else{
							if((initialMapPixels[i+width*(j-1)]&0xFF)==BACKGROUND && (initialMapPixels[i-1+width*j]&0xFF)==BACKGROUND){
								labeledMapPixels[i+width*j] = currentColor;
								E.add(new Point(currentColor,currentColor));
								currentColor++;
							}else if((initialMapPixels[i+width*(j-1)]&0xFF)==FOREGROUND && (initialMapPixels[i-1+width*j]&0xFF)==BACKGROUND){
								labeledMapPixels[i+width*j] = labeledMapPixels[i+width*(j-1)];
							}else if((initialMapPixels[i+width*(j-1)]&0xFF)==BACKGROUND && (initialMapPixels[i-1+width*j]&0xFF)==FOREGROUND){
								labeledMapPixels[i+width*j] = labeledMapPixels[i-1+width*j];
							}else if((initialMapPixels[i+width*(j-1)]&0xFF)==FOREGROUND && (initialMapPixels[i-1+width*j]&0xFF)==FOREGROUND){
								labeledMapPixels[i+width*j] = labeledMapPixels[i-1+width*j];
								int colorU = (int)labeledMapPixels[i+width*(j-1)];
								int colorL = (int)labeledMapPixels[i-1+width*j];
								if(colorU<colorL){
									E.add(new Point(colorU,colorL));
								}else{
									E.add(new Point(colorL,colorU));
								}
							}
						}
					}
				}
			}

			int[] equivalenceTable = labelGraph(E, currentColor+1);

			for(int k=0; k<equivalenceTable.length; k++){
				if(equivalenceTable[k]>nLabels_){
					nLabels_ = equivalenceTable[k];
				}
			}

			for(int k=0; k<labeledMapPixels.length; k++){
				labeledMapPixels[k] = equivalenceTable[(int)labeledMapPixels[k]];
			}
		}
		labeledMap_ = new FloatProcessor(width, height, labeledMapPixels);
		computeRegionFeatures();
	}

	// ============================================================================
	// PRIVATE METHODS

	private void computeRegionFeatures() throws Exception {

		computeRegionExtremes();
		findUnboundedRegions();
		computeRegionAreas();
		computeRegionPerimeters();
		computeRegionInterfacesLengths();
		computeEllipseFit();
	}

	// ============================================================================

	/** Fits an ellipse to all regions. */
	private void computeEllipseFit() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

		EllipseFitter ef = new EllipseFitter(labeledMap_);

		ellipseAngles_ = new double[nLabels_];
		ellipseCenters_ = new Point2D.Double[nLabels_];
		ellipseMajorAxis_ = new double[nLabels_];
		ellipseMinorAxis_ = new double[nLabels_];

		for(int i=0; i<nLabels_; i++){
			ef.fit(i, xMin_[i], xMax_[i], yMin_[i], yMax_[i]);
			ellipseAngles_[i] = ef.getAngle();
			ellipseCenters_[i] = ef.getCenter();
			ellipseMajorAxis_[i] = ef.getMajorAxis();
			ellipseMinorAxis_[i] = ef.getMinorAxis();
		}
	}

	// ============================================================================

	/** Labels the connected components of a graph. */
	private int[] labelGraph(Set<Point> G, int vertexCount){

        int[] labels = new int[vertexCount];
        VertexState[] state = new VertexState[vertexCount];

        for(int i=0; i<vertexCount; i++){
        	labels[i] = -1;
        }

        int labelCount = 0;
        for (int i=0; i<vertexCount; i++){
        	if(labels[i]==-1){
        		for(int j=0; j<vertexCount; j++){
                    state[j] = VertexState.UNVISITED;
        		}
        		runDFS(i, state, vertexCount, G);
        		for(int j=0; j<vertexCount; j++){
        			if(state[j]==VertexState.VISITED){
        				labels[j] = labelCount;
        			}
        		}
        		labelCount++;
        	}
        }
        return labels;
    }

   	// ----------------------------------------------------------------------------

	/** Depth-first search algorithm. */
    private void runDFS(int u, VertexState[] state, int vertexCount, Set<Point> G){

    	state[u] = VertexState.VISITING;
        for (int v = 0; v < vertexCount; v++){
        	if (G.contains(new Point((int)Math.min(u,v),(int)Math.max(u,v))) && state[v] == VertexState.UNVISITED){
        		runDFS(v, state, vertexCount, G);
            }
        }
        state[u] = VertexState.VISITED;
    }

   	// ----------------------------------------------------------------------------

	/** Determines the bounding boxes of the regions. */
    private void computeRegionExtremes() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

		xMin_ = new int[nLabels_];
		yMin_ = new int[nLabels_];
		xMax_ = new int[nLabels_];
		yMax_ = new int[nLabels_];

		int width = labeledMap_.getWidth();
		int height = labeledMap_.getHeight();
		float[] labeledSkeletonPixels = (float[])labeledMap_.getPixels();

		for(int i=0; i<nLabels_; i++){
			xMin_[i] = width;
			yMin_[i] = height;
			xMax_[i] = 0;
			yMax_[i] = 0;
		}

		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				int val = (int)labeledSkeletonPixels[x+width*y];
				if(x<xMin_[val]) xMin_[val] = x;
				if(y<yMin_[val]) yMin_[val] = y;
				if(x>xMax_[val]) xMax_[val] = x;
				if(y>yMax_[val]) yMax_[val] = y;
			}
		}
	}

   	// ----------------------------------------------------------------------------

	/** Determines the regions that are in contact with the border of the image. */
	private void findUnboundedRegions() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");
		if (xMin_ == null)
			throw new Exception("ERROR: xMin_ is null.");
		if (xMax_ == null)
			throw new Exception("ERROR: xMax_ is null.");
		if (yMin_ == null)
			throw new Exception("ERROR: yMin_ is null.");
		if (yMax_ == null)
			throw new Exception("ERROR: yMax_ is null.");

		isUnbounded_ = new boolean[nLabels_];

		int width_1 = labeledMap_.getWidth()-1;
		int height_1 = labeledMap_.getHeight()-1;

		for(int i=0; i<nLabels_; i++){
			if(xMin_[i] == 0 || yMin_[i] == 0 || xMax_[i] == width_1 || yMax_[i] == height_1)
				isUnbounded_[i] = true;
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes the area of each region of the skeleton. */
	private void computeRegionAreas() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

		areas_ = new int[nLabels_];
		float[] labeledSkeletonPixels = (float[])labeledMap_.getPixels();
		for(int i=0; i<labeledSkeletonPixels.length; i++){
			areas_[(int)labeledSkeletonPixels[i]]++;
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes the area of each region of the skeleton. */
	private void computeRegionPerimeters() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

		perimeters_ = new int[nLabels_];
		int width = labeledMap_.getWidth();
		int height = labeledMap_.getHeight();
		float[] labeledSkeletonPixels = (float[])labeledMap_.getPixels();

		boolean[] isConnected = new boolean[nLabels_];

		for(int i=1; i<width-1; i++){
			for(int j=1; j<height-1; j++){
				if(labeledSkeletonPixels[i+width*j]==0){
					for(int ii=i-1; ii<=i+1; ii++){
						for(int jj=j-1; jj<=j+1; jj++){
							isConnected[(int)labeledSkeletonPixels[ii+width*jj]] = true;
						}
					}
					for(int k=0; k<isConnected.length; k++){
						if(isConnected[k]){
							perimeters_[k]++;
						}
						isConnected[k] = false;
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes the area of each region of the skeleton. */
	private void computeRegionInterfacesLengths() throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

		regionInterfaceLengths_ = new int[nLabels_][nLabels_];

		int width = labeledMap_.getWidth();
		int height = labeledMap_.getHeight();
		float[] labeledSkeletonPixels = (float[])labeledMap_.getPixels();

		boolean[] isConnected = new boolean[nLabels_];

		for(int i=1; i<width-1; i++){
			for(int j=1; j<height-1; j++){
				if(labeledSkeletonPixels[i+width*j]==0){
					for(int ii=i-1; ii<=i+1; ii++){
						for(int jj=j-1; jj<=j+1; jj++){
							isConnected[(int)labeledSkeletonPixels[ii+width*jj]] = true;
						}
					}
					for(int k1=0; k1<isConnected.length; k1++){
						if(isConnected[k1]){
							for(int k2=k1; k2<isConnected.length; k2++){
								if(isConnected[k2]){
									regionInterfaceLengths_[k1][k2]++;
									regionInterfaceLengths_[k2][k1]++;
								}
							}

						}
						isConnected[k1] = false;
					}
				}
			}
		}
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public FloatProcessor getLabeledMap() { return labeledMap_; }
	public int getNLabels() { return nLabels_; }
	public int[] getAreas() { return areas_; }
	public int[] getPerimeters() { return perimeters_; }
	public int[][] getRegionInterfaceLengths() { return regionInterfaceLengths_; }
	public boolean[] getIsUnbounded() { return isUnbounded_; }
	public int[] getXMin() { return xMin_; }
	public int[] getYMin() { return yMin_; }
	public int[] getXMax() { return xMax_; }
	public int[] getYMax() { return yMax_; }
	public double[] getEllipseAngles() { return ellipseAngles_; }
	public Point2D.Double[] getEllipseCenters() { return ellipseCenters_; }
	public double[] getEllipseMajorAxis() { return ellipseMajorAxis_; }
	public double[] getEllipseMinorAxis() { return ellipseMinorAxis_; }
}