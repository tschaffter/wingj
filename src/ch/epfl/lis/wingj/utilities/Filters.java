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

package ch.epfl.lis.wingj.utilities;

import java.awt.geom.Point2D;

import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/** 
 * Utility methods to filter images in WingJ.
 * 
 * @version August 29, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class Filters {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** 2D Gaussian filter. */
	public static void applyGaussianFilter(ImageProcessor ip, double sigma) {
		
		GaussianBlur gaussianBlur = new GaussianBlur();
   		double accuracy = 0.02;
   		gaussianBlur.blurGaussian(ip, sigma, sigma, accuracy);
	}
	
	// ----------------------------------------------------------------------------

	/** Directional 1D Gaussian filter. */
	public static double apply1DGaussianFilterOnDirectionForPoint(float x, float y, Point2D.Double normal, FloatProcessor fp, double sigma) {
		
		fp.setInterpolationMethod(ImageProcessor.BICUBIC);
		double sigma2 = sigma*sigma;
		double range = 6*sigma;
		int nSamples = 2*(int)Math.ceil(range)+1;
		int midSample = (nSamples-1)/2;
		double value = 0;
		double normalization = 0;
		for(int i=1; i<=nSamples; i++){
			double delta = range*(i-0.5-midSample)/(double)nSamples;
			double xCoord = x+delta*normal.x;
			double yCoord = y+delta*normal.y;			
			double pixelValue =  fp.getInterpolatedValue(xCoord, yCoord);
	   		value += pixelValue*Math.exp(-(delta*delta)/(2.0*sigma2));
	   		normalization += Math.exp(-(delta*delta)/(2.0*sigma2));
		}
		return value/normalization;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Max filter. */
	public static void applyMAXFilter(FloatProcessor ip, int radius) {
		
		RankFilters rf = new RankFilters();
		rf.rank(ip, radius, RankFilters.MAX);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Filters an array with a all-pole recursive filter with periodic boundary conditions. */
	public static double[] allPoleIIRFilter (double[] s, double[] pole) {

		final int N = s.length;
		for (int p = 0, P = pole.length; (p < P); p++) {
			final double z = pole[p];
			double z1 = z;
			for (int k = N - 1; (0 < k); k--) {
				s[0] += z1 * s[k];
				z1 *= z;
			}
			s[0] /= 1.0 - z1;
			for (int k = 1; (k < N); k++) {
				s[k] += z * s[k - 1];
			}
			z1 = z;
			final int K = N - 1;
			for (int k = 0; (k < K); k++) {
				s[K] += z1 * s[k];
				z1 *= z;
			}
			s[K] *= 1.0 / (1.0 - z1);
			z1 = 1.0 - z;
			z1 *= z1;
			s[K] *= z1;
			for (int k = N - 2; (0 <= k); k--) {
				s[k] = z * s[k + 1] + z1 * s[k];
			}
		}
		return(s);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Pixel values < thld are set to 0, otherwise Float.MAX_VALUE. */
	public static void binaryThresholdedImageFilter(FloatProcessor p, double thld) throws Exception {
		
		if (p == null)
			throw new Exception("ERROR: FloatProcessor p is null.");
		
   		int width = p.getWidth();
   		int heigth = p.getHeight();
   		
   		float [] data = (float[]) p.getPixels();
   		for (int i = 0; i < width; i++) {
   			for (int j = 0; j < heigth; j++) {
   				int index = i + width*j;
   				if (data[index] > thld) data[index] = Float.MAX_VALUE;
   				else data[index] = 0;
   			}
   		}
	}
}