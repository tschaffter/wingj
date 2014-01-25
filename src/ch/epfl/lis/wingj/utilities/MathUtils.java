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
import java.util.List;

/**
 * Utility methods for maths.
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class MathUtils {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Sets angle in degree in [0, 360]. */
	public static double toPositiveAngle(double degree) {
			
		if (degree < 0)
			degree += 360;
		if (degree >= 360)
			degree -= 360;
		
		return degree;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns positive angle between P1-P2-P3. */
	public static double positveAngleBetween(Point2D.Double P1, Point2D.Double P2, Point2D.Double P3) throws Exception {
		
		return MathUtils.toPositiveAngle(angleBetween(P1, P2, P3));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the angle P1-P2-P3. */
	public static double angleBetween(Point2D.Double P1, Point2D.Double P2, Point2D.Double P3) throws Exception {

		// Method 1
//		Vector3D v1 = new Vector3D(P1.x - P2.x, P1.y - P2.y, 0);
//		Vector3D v2 = new Vector3D(P3.x - P2.x, P3.y - P2.y, 0);
//		
//		v1.normalize();
//		v2.normalize();
//		
//		double alpha = Math.toDegrees(v2.getAlpha() - v1.getAlpha());
//		return alpha;
	
		// Method 2
		return Math.toDegrees(Math.atan2(P1.x - P2.x, P1.y - P2.y) - Math.atan2(P3.x- P2.x, P3.y- P2.y));
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Computes mean and std of the given vector.
	 * TODO: Use computeMeanAndStd(double[]) with (Double[])data.toArray().
	 */
	public static Double[] computeMeanAndStd(List<Double> data) throws Exception {
		
		if (data == null)
			throw new Exception("ERROR: data is null.");
		
		Double[] output = new Double[2];

		// compute mean
		output[0] = 0.;
		for (Double d : data)
			output[0] += d;
		output[0] /= data.size();
		
		// compute std
		double sum = 0.;
		for (int i = 0; i < data.size(); i++)
			sum += Math.pow(data.get(i) - output[0], 2);
		output[1] = Math.sqrt(sum / (double) data.size());
		
		return output;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes mean and std of the given vector. */
	public static Double[] computeMeanAndStd(final Double[] data) throws Exception {
		
		if (data == null)
			throw new Exception("ERROR: data is null.");
		
		Double[] output = new Double[2];

		// compute mean
		output[0] = 0.;
		for (Double d : data)
			output[0] += d;
		output[0] /= data.length;
		
		// compute std
		double sum = 0.;
		for (int i = 0; i < data.length; i++)
			sum += Math.pow(data[i] - output[0], 2);
		output[1] = Math.sqrt(sum / (double) data.length);
		
		return output;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes mean and standard error of the given vector. */
	public static Double[] computeMeanAndSe(final Double[] data) throws Exception {
		
		if (data == null)
			throw new Exception("ERROR: data is null.");
		
		Double[] output = new Double[2];

		// compute mean
		output[0] = 0.;
		for (Double d : data)
			output[0] += d;
		output[0] /= data.length;
		
		// compute std
		double sum = 0.;
		for (int i = 0; i < data.length; i++)
			sum += Math.pow(data[i] - output[0], 2);
		output[1] = Math.sqrt(sum / (double) data.length);
		output[1] /= Math.sqrt(data.length);
		
		return output;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a deep copy of a double[][]. */
	public static double[][] deepCopyDoubleArrayOfArray(double[][] ori) {
		
		if (ori == null)
			return null;
		
		double[][] copy = new double[ori.length][ori[0].length];
		
		for (int i = 0; i < copy.length; i++) {
			for (int j = 0; j < copy[0].length; j++) {
				copy[i][j] = ori[i][j];
			}
		}
		
		return copy;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the sign of a double value. */
	public static double sign(final double x) {
		
	      if (Double.isNaN(x))
	          return Double.NaN;
	      
	      return (x == 0.0) ? 0.0 : (x > 0.0) ? 1.0 : -1.0;
	  }
	
	// ----------------------------------------------------------------------------
	
	/** Main method for test purpose. */
	public static void main(String[] args) {
		
		try {
//			Point2D.Double ori = new Point2D.Double(0., 0.);
//			Point2D.Double pt1 = new Point2D.Double(1., 0.); // horizontal
//			Point2D.Double pt2 = new Point2D.Double(1, -1);
//			
//			double alpha = positveAngleBetween(pt2, ori, pt1);
//			
//			WJSettings.log("angle: " + alpha);
			
			
			
			
			
//			new ImageJ();
//			
//			double r = 100;
//			Snake2DNode center = new Snake2DNode(200, 200);
//			Snake2DNode[] points = new Snake2DNode[8];
//			points[0] = new Snake2DNode(r, 0);
//			points[1] = new Snake2DNode(0, r);
//			points[2] = new Snake2DNode(-r, 0);
//			points[3] = new Snake2DNode(0, -r);
//			points[4] = new Snake2DNode(Math.cos(Math.PI/4.)*r, -Math.sin(Math.PI/4.)*r);
//			points[5] = new Snake2DNode(-Math.cos(Math.PI/4.)*r, -Math.sin(Math.PI/4.)*r);
//			points[6] = new Snake2DNode(-Math.cos(Math.PI/4.)*r, Math.sin(Math.PI/4.)*r);
//			points[7] = new Snake2DNode(Math.cos(Math.PI/4.)*r, Math.sin(Math.PI/4.)*r);
//			for (Snake2DNode node : points) {
//				node.x += center.x;
//				node.y += center.y;
//			}
//			
//			
//			double stdx = 50.;
//			double stdy = 100.;
//			
//			Snake2DNode ref = new Snake2DNode(center.x + 1, center.y); // on the right horizontal of the center
//			Snake2DNode controlPoint = points[7];
//			
//			double alpha = positveAngleBetween(controlPoint, center, ref);
//			WJSettings.log("angle: " + alpha);
//			
//			// the sign of cos and sin for this angle and in the four cadrans is
//			// -- / +-
//			// -+ / ++
//			double cosAlpha = Math.cos(Math.toRadians(alpha));
//			double sinAlpha = Math.sin(Math.toRadians(alpha));
//
//			
//			// the signs can be used to know if stdx or stdy must be added or subtracted
//			// (we are in image space)
//			Snake2DNode stdxNode = (Snake2DNode)controlPoint.clone();
//			Snake2DNode stdyNode = (Snake2DNode)controlPoint.clone();
//			stdxNode.x += sign(cosAlpha)*stdx;
//			stdyNode.y -= sign(sinAlpha)*stdy;
//			
//			// compute an extension of the control points to use as reference for
//			// angle measurement
//			double dx = 1.1*(controlPoint.x - center.x) + center.x; // 2 is taken arbitrarily
//			double dy = 1.1*(controlPoint.y - center.y) + center.y;
//			Snake2DNode extendedControlPoint = new Snake2DNode(dx, dy);
//			
//			// compute angles extendedControlPoint - controlPoint - stdxNode
//			// compute angles extendedControlPoint - controlPoint - stdyNode
//			double stdxAngle = positveAngleBetween(extendedControlPoint, controlPoint, stdxNode);
//			double stdyAngle = positveAngleBetween(extendedControlPoint, controlPoint, stdyNode);
//			
//			// the smallest angle can be use to project directly the associated std
//			// the other is projected using 90-angle
//			double stdxProj = 0.;
//			double stdyProj = 0.;
//			if (stdxAngle <= stdyAngle) {
//				stdxProj = stdx * Math.cos(Math.toRadians(stdxAngle));
//				stdyProj = stdy * Math.cos(Math.toRadians(90-stdxAngle));
//			} else {
//				stdxProj = stdx * Math.cos(Math.toRadians(90-stdyAngle));
//				stdyProj = stdy * Math.cos(Math.toRadians(stdyAngle));
//			}
//		
//			// value to add to the control point in the direction defined by center -> control point
//			double centerToControlPointLength = Math.sqrt(Math.pow(controlPoint.x - center.x, 2) + Math.pow(controlPoint.y - center.y, 2));
//			double extraLength = (stdxProj + stdyProj) / 2.;
//			double controlPointPlusStdX = center.x + (controlPoint.x - center.x) * ((centerToControlPointLength + extraLength) / centerToControlPointLength);
//			double controlPointPlusStdY = center.y + (controlPoint.y - center.y) * ((centerToControlPointLength + extraLength) / centerToControlPointLength);
//			Snake2DNode controlPointPlusStd = new Snake2DNode(controlPointPlusStdX, controlPointPlusStdY);
//			
//			
//			WJSettings.log("cos stdx angle: " + stdxAngle);
//			WJSettings.log("cos stdy angle: " + stdyAngle);
//			
//			
//			ImagePlus background = IJ.createImage("background", "black", 400, 400, 1);
//			for(int i=0; i<points.length; i++){
//				background.getProcessor().set((int)points[i].x, (int)points[i].y, 255);
//			}
//			
//			background.getProcessor().set((int)stdxNode.x, (int)stdxNode.y, 100);
//			background.getProcessor().set((int)stdyNode.x, (int)stdyNode.y, 255);
//			
//			background.getProcessor().set((int)controlPointPlusStd.x, (int)controlPointPlusStd.y, 255);
//			
//			background.getProcessor().filter(ImageProcessor.MAX);
//			background.getProcessor().filter(ImageProcessor.MAX);
//			background.getProcessor().filter(ImageProcessor.MAX);
////			background.getProcessor().filter(ImageProcessor.MAX);
////			background.getProcessor().filter(ImageProcessor.MAX);
//			background.show();
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
