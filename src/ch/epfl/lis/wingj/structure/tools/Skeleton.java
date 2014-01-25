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

import ch.epfl.lis.wingj.utilities.Filters;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

/** 
 * Computes a pruned skeleton of the wing pouch structure.
 * 
 * @version March 5, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Skeleton {
	
	/** ByteProcessor of the skeleton. */
	private ByteProcessor skeletonBp_ = null;
	
	/** Maximum length of the pruning. */
	private static int PRUNNINGLENGTH = 100;
	
	// ============================================================================
	// PUBLIC METHODS
   	
   	/** Default constructor. */
   	public Skeleton() {}
   	
   	// ----------------------------------------------------------------------------
	
	/** Skeletonizes the given FloatProcessor which must be binary. */
	public FloatProcessor skeletonize(FloatProcessor binaryImage, boolean prune, double dilation) throws Exception {
		
		prune(binaryImage, prune);
		FloatProcessor skeletonFp = (FloatProcessor) skeletonBp_.convertToFloat();
		
		// dilation if required
		if (dilation > 0)
			Filters.applyMAXFilter(skeletonFp, (int) dilation);
		
		return skeletonFp;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Prunes given binary image. */
	public ByteProcessor prune(FloatProcessor binaryImage, boolean prune) throws Exception {
		
		// build the binary skeleton
		skeletonBp_ = (ByteProcessor) binaryImage.convertToByte(false);
		skeletonBp_.invertLut();
		skeletonBp_.skeletonize();
		skeletonBp_.invertLut();
		
		// prune the skeleton if required

		if (prune)
			prune(skeletonBp_);
		
		return skeletonBp_;
	}

	// ----------------------------------------------------------------------------
	
	/** Prunes the given skeleton to remove undesired branches. */
	public static void prune(ByteProcessor skeleton) throws Exception {
		
		if(skeleton == null)
   			throw new Exception("ERROR: skeleton is null.");
   		
   		int width = skeleton.getWidth();
   		int height = skeleton.getHeight();
   		
   		byte[] skeletonPixels = (byte[])skeleton.getPixels();
		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if (skeletonPixels[i+j*width] != 0) {
					isEndPoint(skeleton, i, j, -1, -1, 0);
				}
			}
		}
		// clear points that are 4-connected to exactly three points
		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if (skeletonPixels[i+j*width] != 0) {
					int count = 0;
					if (skeletonPixels[(i-1)+(j)*width] != 0) count++;
					if (skeletonPixels[(i+1)+(j)*width] != 0) count++;
					if (skeletonPixels[(i)+(j-1)*width] != 0) count++;
					if (skeletonPixels[(i)+(j+1)*width] != 0) count++;
					if(count==3) skeletonPixels[i+j*width] = 0;
				}
			}
		}
		
		// clear points that are "removable" (Prop 1 from P. Dimitrov, C.Philipps, and K.Siddiqi "Robust and Efficient Skeletal Graphs")
   		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if ((skeletonPixels[i+j*width]&0xFF) != 0) {
					byte val = skeletonPixels[i+j*width];
					skeletonPixels[i+j*width] = 0;
					//vertices
					int vertices = 0;
					for(int k = i-1; k <= i+1; k++){
						for(int l = j-1; l <= j+1; l++){
							if ((skeletonPixels[k+l*width]&0xFF) != 0) {
								vertices++;
							}
						}
					}
					//edges
					int edges = 0;
					for(int k = i-1; k <= i+1; k++){
						for(int l = j-1; l <= j+1; l++){
							if ((skeletonPixels[k+l*width]&0xFF) != 0) {
								for(int kk = Math.max(k-1, i-1); kk <= Math.min(k+1, i+1); kk++){
									for(int ll = Math.max(l-1, j-1); ll <= Math.min(l+1, j+1); ll++){
										if(!(k==kk && l==ll)){
											if ((skeletonPixels[kk+ll*width]&0xFF) != 0) {
												edges++;
											}
										}
									}
								}
							}
						}
					}
					if(vertices-edges/2 != 1){
						skeletonPixels[i+j*width] = val;
					}else{
						boolean endPoint = false;
						
						if(vertices == 1){
							// has exactly ONE 8-connected neighbor
							endPoint = true;
						}else if (vertices == 2){
							// has exactly TWO neighbor which are 4-adjacent
							int adj4 = 0;
							for(int k = i-1; k <= i+1; k++){
								for(int l = j-1; l <= j+1; l++){
									if ((skeletonPixels[k+l*width]&0xFF) != 0) {
										for(int kk = Math.max(k-1, i-1); kk <= Math.min(k+1, i+1); kk++){
											for(int ll = Math.max(l-1, j-1); ll <= Math.min(l+1, j+1); ll++){
												if(!(k==kk && l==ll)){
													if ((skeletonPixels[kk+ll*width]&0xFF) != 0) {
														if((kk-k)*(kk-k)+(ll-l)*(ll-l)<2){
															adj4++;
														}
													}
												}
											}
										}
									}
								}
							}
							if(adj4!=0){
								endPoint = true;
							}
						}else{
							endPoint = false;
						}
						//if it is NOT an end-point, remove
						if(endPoint){
							skeletonPixels[i+j*width] = val;
						}
					}
				}
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the number of non zero pixels (i.e. pixels that are not 0). */
	public int countNonZeroPixels() throws Exception {
		
		return countNonZeroPixels(skeletonBp_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the number of non zero pixels (i.e. pixels that are not 0). */
	public static int countNonZeroPixels(ByteProcessor bp) throws Exception {
		
		if (bp == null)
			throw new Exception("ERROR: ByteProcessor is null.");
		
   		int width = bp.getWidth();
   		int height = bp.getHeight();
   		
   		byte[] bpPixels = (byte[]) bp.getPixels();
   		int counter = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ((bpPixels[x+y*width]&0xFF) != 0)
					counter++;
			}
		}
		return counter;
	}

	// ============================================================================
	// PRIVATE METHODS
	
	/** Checks if the pixel with coordinates Point of a skeleton is an end-point. */
   	private static boolean isEndPoint(ByteProcessor skeleton, int x, int y, int xOld, int yOld, int depth) throws Exception {

   		if(skeleton == null)
   			throw new Exception("ERROR: skeleton is null.");

		if(depth < Skeleton.PRUNNINGLENGTH){
	   		int width = skeleton.getWidth();
	   		byte[] skeletonPixels = (byte[])skeleton.getPixels();   		
	   		// checking neighborhood
	   		int ii = -1;
	   		int jj = -1;
	   		int count = 0;
	   		for (int k = x-1; k <= x+1; k++) {
	   			for (int l = y-1; l <= y+1; l++) {
	   				if ((skeletonPixels[k+l*width]) != 0) {
	   					if(!(k==x && l==y)){
			   				if (!(k==xOld && l==yOld)) {
			   					count++;
			   					ii = k;
			   					jj = l;
			   				}
	   					}
	   				}
	   			}
	   		}
	   		if(count == 0){
	   			skeletonPixels[x+y*width] = 0;
	   			return true;
	   		}else if(count == 1){
	   			depth++;
	   			if(isEndPoint(skeleton, ii, jj, x, y, depth)){
	   				skeletonPixels[x+y*width] = 0;
		   			return true;
	   			}else{
	   				return false;
	   			}
	   		}else if(count > 1){
	   			return true;
	   		}
		}else{
	   		return false;
		}
		return false;
   	}
}