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

/**
 * Utilities for exponential B-splines.
 * 
 * @version March 5, 2013
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 */
public class BSplinesUtils {

	/**
	 * Length of the support of the exponential B-spline basis function with
	 * three roots.
	 */
	public static int ESPLINE3SUPPORT = 3;

	/**
	 * Length of the support of the exponential B-spline basis function with
	 * four roots.
	 */
	public static int ESPLINE4SUPPORT = 4;

	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Causal exponential B-Spline with parameters (0,-j alpha, j alpha).
	 */
	public static double ESpline3(double t, double alpha) {
		
		double alphaHalf = alpha / 2.0;
		double ESplineValue = 0.0;
		double eta = 2 * (1 - Math.cos(alpha)) / (alpha * alpha);
		if ((t >= 0) & (t <= 1)) {
			ESplineValue = (2 * (1 - Math.cos(alphaHalf * t)
					* Math.cos(alphaHalf * t)));
		} else if ((t > 1) & (t <= 2)) {
			ESplineValue = (Math.cos(alpha * (t - 2))
					+ Math.cos(alpha * (t - 1)) - 2 * Math.cos(alpha));
		} else if ((t > 2) & (t <= 3)) {
			ESplineValue = (1 - Math.cos(alpha * (t - 3)));
		}
		ESplineValue = ESplineValue / (alpha * alpha * eta);
		return ESplineValue;
	}

	// ----------------------------------------------------------------------------
	
	/**
	 * Causal exponential B-Spline with parameters (0, 0,-j alpha, j alpha).
	 */
	public static double ESpline4 (double t, double alpha){

		double ESplineValue = 0.0;
		double eta = 2*(1-Math.cos(alpha))/(alpha*alpha);
		if ((t>=0) & (t<=1)){
			ESplineValue = t - Math.sin(alpha*t)/alpha;
		}else if ((t>1) & (t<=2)){
			ESplineValue = 2 - t + 2*Math.sin(alpha*(t-1))/alpha + Math.sin(alpha*(t-2))/alpha - 2*Math.cos(alpha)*t + 2*Math.cos(alpha);
		}else if ((t>2) & (t<=3)){
			ESplineValue = t - 2 - 4*Math.cos(alpha) - 2*Math.sin(alpha*(t-3))/alpha + 2*Math.cos(alpha)*(t-1) - Math.sin(alpha*(t-2))/alpha;
		}else if ((t>3) & (t<=4)){
			ESplineValue = 4 - t + Math.sin(alpha*(t-4))/alpha;
		}else{
			ESplineValue = (double)(0.0);
		}
		ESplineValue = ESplineValue/(alpha*alpha*eta);
		return ESplineValue;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Derivative of the causal exponential B-Spline with parameters (0,-j
	 * alpha, j alpha).
	 */
	public static double DerivativeESpline3(double t, double alpha) {
		
		double ESplinePrimeValue = 0.0;
		double eta = 2 * (1 - Math.cos(alpha)) / (alpha * alpha);
		if ((t >= 0) & (t <= 1)) {
			ESplinePrimeValue = Math.sin(alpha * t);
		} else if ((t > 1) & (t <= 2)) {
			ESplinePrimeValue = -(Math.sin(alpha * (t - 2)) + Math.sin(alpha
					* (t - 1)));
		} else if ((t > 2) & (t <= 3)) {
			ESplinePrimeValue = Math.sin(alpha * (t - 3));
		}
		ESplinePrimeValue = ESplinePrimeValue / (alpha * eta);
		return ESplinePrimeValue;
	}

	// ----------------------------------------------------------------------------
	
	/**
	 * Derivative of the causal exponential B-Spline with parameters (0, 0,-j
	 * alpha, j alpha).
	 */
	public static double DerivativeESpline4 (double t, double alpha){

		double ESplinePrimeValue = 0.0;
		double eta = 2*(1-Math.cos(alpha))/(alpha*alpha);
		if ((t>=0) & (t<=1)){
			ESplinePrimeValue = 1 - Math.cos(alpha*t);
		}else if ((t>1) & (t<=2)){
			ESplinePrimeValue =  - 1 + 2*Math.cos(alpha*(t-1)) + Math.cos(alpha*(t-2)) - 2*Math.cos(alpha);
		}else if ((t>2) & (t<=3)){
			ESplinePrimeValue = 1 - 2*Math.cos(alpha*(t-3)) + 2*Math.cos(alpha) - Math.cos(alpha*(t-2));
		}else if ((t>3) & (t<=4)){
			ESplinePrimeValue = - 1 + Math.cos(alpha*(t-4));
		}else{
			ESplinePrimeValue = 0.0;
		}
		ESplinePrimeValue = ESplinePrimeValue/(alpha*alpha*eta);
		return ESplinePrimeValue;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Sampled autocorrelation of the exponential B-spline of order three.
	 */
	public static double AutocorrelationESpline3(int l, double alpha) {
		double PIM_ = 0.5 * alpha;
		double M_ = Math.PI / PIM_;

		double value = 0.0;
		switch (l) {
		case -2:
			value = (1.0 / 8.0)
					* (Math.PI * Math.cos(PIM_) * Math.sin(PIM_) - M_ + M_
							* Math.cos(PIM_) * Math.cos(PIM_))
					/ (M_ * (1.0 - 2.0 * Math.cos(PIM_) * Math.cos(PIM_) + Math
							.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)));
			break;
		case -1:
			value = -0.25
					* (Math.PI * Math.cos(PIM_) * Math.sin(PIM_) + M_ - 3.0 * M_
							* Math.cos(PIM_) * Math.cos(PIM_) + 2.0 * M_
							* Math.cos(PIM_) * Math.cos(PIM_) * Math.cos(PIM_)
							* Math.cos(PIM_))
					/ (M_ * (1.0 - 2.0 * Math.cos(PIM_) * Math.cos(PIM_) + Math
							.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)));
			break;
		case 1:
			value = 0.25
					* (Math.PI * Math.cos(PIM_) * Math.sin(PIM_) + M_ - 3.0 * M_
							* Math.cos(PIM_) * Math.cos(PIM_) + 2.0 * M_
							* Math.cos(PIM_) * Math.cos(PIM_) * Math.cos(PIM_)
							* Math.cos(PIM_))
					/ (M_ * (1.0 - 2.0 * Math.cos(PIM_) * Math.cos(PIM_) + Math
							.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)));
			break;
		case 2:
			value = -(1.0 / 8.0)
					* (Math.PI * Math.cos(PIM_) * Math.sin(PIM_) - M_ + M_
							* Math.cos(PIM_) * Math.cos(PIM_))
					/ (M_ * (1.0 - 2.0 * Math.cos(PIM_) * Math.cos(PIM_) + Math
							.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)
							* Math.cos(PIM_)));
			break;
		default:
			value = 0.0;
			break;
		}
		return value;
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Sampled autocorrelation of the exponential B-spline of order four.
	 */
	public static double AutocorrelationESpline4 (int l, double alpha){
		double PIM_ = 0.5 * alpha;
		double M_ = Math.PI / PIM_;
		
		double value = 0.0;
		switch (l) {
		case -3:
			value = -(1.0/32.0)*(2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)-2.0*(double)M_*(double)M_+Math.PI*Math.PI+(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1.0-2.0*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		case -2:  
			value =  (1.0/8.0)*(-2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)+2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)-Math.PI*Math.PI+2.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_)+(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1.0-2.0*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		case -1:  
			value = -(1.0/32.0)*(16.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)+6.0*(double)M_*(double)M_+16.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)-22.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)+5.0*(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_)+5.0*Math.PI*Math.PI-16.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1-2*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		case 1:  
			value =  (1.0/32.0)*(16.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)+6.0*(double)M_*(double)M_+16.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)-22.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)+5.0*(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_)+5.0*Math.PI*Math.PI-16.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1-2*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		case 2:  
			value = -(1.0/8.0)*(-2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)+2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)-Math.PI*Math.PI+2.0*Math.PI*Math.PI*Math.cos(PIM_)*Math.cos(PIM_)+(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1.0-2.0*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		case 3:  
			value =  (1.0/32.0)*(2.0*(double)M_*(double)M_*Math.cos(PIM_)*Math.cos(PIM_)-2.0*(double)M_*(double)M_+Math.PI*Math.PI+(double)M_*Math.PI*Math.sin(PIM_)*Math.cos(PIM_))/(Math.PI*Math.PI*(1.0-2.0*Math.cos(PIM_)*Math.cos(PIM_)+Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)*Math.cos(PIM_)));
			break;
		default: 
			value = 0.0;
			break;
		}
		return(value);
	}
}
