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

import ij.process.FloatProcessor;

/** 
 * Implements efficient dilate image functions.
 *
 * @version March 7, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class Dilation {
	
	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Dilates the shape(s) defined by pixel values > 0 and returns a new binary
	 * FloatProcessor. The dilation algorithm applied dilates the binary shape(s) iteratively,
	 * each iteration adding 1px layer around the binary shape(s).
	 */
	public static FloatProcessor dilate(FloatProcessor fp, int numIters) throws Exception {
		
		if (fp == null)
			throw new Exception("ERROR: FloatProcessor is null.");
		
		int[][] binaryMask = new int[fp.getWidth()][fp.getHeight()];
		float[] data = (float[]) fp.getPixels();
		
		// creates the binary mask
		for (int i = 0; i < fp.getWidth(); i ++) {
			for (int j = 0; j < fp.getHeight(); j++) {
				binaryMask[i][j] = data[j*fp.getWidth()+i] > 0 ? 1 : 0;
			}
		}
		
		// iterative dilation by one
		for (int i = 0; i < numIters; i++)
			binaryMask = dilate(binaryMask);
		
		return new FloatProcessor(binaryMask);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Best dilate by one solution. Cost: O(n^2).
	 * Source: http://ostermiller.org/dilate_and_erode.html 
	 */
	public static int[][] dilate(int[][] image){
	    for (int i=0; i<image.length; i++){
	        for (int j=0; j<image[i].length; j++){
	            if (image[i][j] == 1){
	                if (i>0 && image[i-1][j]==0) image[i-1][j] = 2;
	                if (j>0 && image[i][j-1]==0) image[i][j-1] = 2;
	                if (i+1<image.length && image[i+1][j]==0) image[i+1][j] = 2;
	                if (j+1<image[i].length && image[i][j+1]==0) image[i][j+1] = 2;
	            }
	        }
	    }
	    for (int i=0; i<image.length; i++){
	        for (int j=0; j<image[i].length; j++){
	            if (image[i][j] == 2){
	                image[i][j] = 1;
	            }
	        }
	    }
	    return image;
	}
}
