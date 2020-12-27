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

import ij.process.FloatProcessor;

import java.awt.geom.Point2D;

/**
 * Fits an ellipse to a polygon.
 * <p>
 * The current implementation is based on the code of ImageJ.
 *
 * @version September 28, 2011
 *
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com).
 */
public class EllipseFitter {

	/** Pi/2. */
	private static final double HALFPI = Math.PI/2.0;

	/** Map with all regions. */
	private FloatProcessor labeledMap_;

	/** X centroid. */
	private double xCenter_;
	/** Y centroid. */
	private double yCenter_;
	/** Length of major axis. */
	private double majorAxis_;
	/** Length of minor axis. */
	private double minorAxis_;
	/** Angle of the elliptic fitting. */
	private double angle_;
	/** Number of pixels in the region. */
    private int pixNumber_;

    /** \sum x. */
    private double xsum_;
    /** \sum y. */
    private double ysum_;
    /** \sum x^2. */
    private double x2sum_;
    /** \sum y^2. */
    private double y2sum_;
    /** \sum x*y. */
    private double xysum_;

    /** Minimum x coordinate of the bounding box of the region to fit the ellipse. */
    private int xMin_;
    /** Minimum y coordinate of the bounding box of the region to fit the ellipse. */
    private int yMin_;
    /** Maximum x coordinate of the bounding box of the region to fit the ellipse. */
    private int xMax_;
    /** Maximum y coordinate of the bounding box of the region to fit the ellipse. */
    private int yMax_;

    /** X centroid relative to the bounding box. */
    private double xm_;
    /** Y centroid relative to the bounding box. */
    private double ym_;
    /** Centered moment (2,0). */
    private double u20_;
    /** Centered moment (0,2). */
    private double u02_;
    /** Centered moment (1,1). */
    private double u11_;

    // ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
    public EllipseFitter(FloatProcessor labeledMap){

    	labeledMap_ = labeledMap;
    }

    // ============================================================================

	/** Performs the ellipse fitting one one region. */
    public void fit(int region, int xMin, int xMax, int yMin, int yMax) throws Exception {

        xMin_ = xMin;
        yMin_ = yMin;
        xMax_ = xMax;
        yMax_ = yMax;
        computeEllipseParam(region);
    }

	// ============================================================================
	// PRIVATE METHODS


    /** Computes the parameters of the ellipse. */
    private void computeEllipseParam(int region) throws Exception {

        double a11, a12, a22, m4, z, scale, tmp, xoffset, yoffset;
        computeSums(region);
        computeMoments();
        m4 = 4.0 * Math.abs(u02_ * u20_ - u11_ * u11_);
        if (m4 < 0.000001)
            m4 = 0.000001;
        a11 = u02_ / m4;
        a12 = u11_ / m4;
        a22 = u20_ / m4;
        xoffset = xm_;
        yoffset = ym_;

        tmp = a11 - a22;
        if (tmp == 0.0)
            tmp = 0.000001;
        angle_ = 0.5 * Math.atan(2.0 * a12 / tmp);
        if (angle_ < 0.0)
            angle_ += HALFPI;
        if (a12 > 0.0)
            angle_ += HALFPI;
        else if (a12 == 0.0) {
            if (a22 > a11) {
                angle_ = 0.0;
                tmp = a22;
                a22 = a11;
                a11 = tmp;
            } else if (a11 != a22)
                angle_ = HALFPI;
        }
        tmp = Math.sin(angle_);
        if (tmp == 0.0)
            tmp = 0.000001;
        z = a12 * Math.cos(angle_) / tmp;
        majorAxis_ = Math.sqrt (1.0 / Math.abs(a22 + z));
        minorAxis_ = Math.sqrt (1.0 / Math.abs(a11 - z));
        scale = Math.sqrt (pixNumber_ / (Math.PI * majorAxis_ * minorAxis_)); //equalize areas
        majorAxis_ = majorAxis_*scale*2.0;
        minorAxis_ = minorAxis_*scale*2.0;
        if (majorAxis_ < minorAxis_) {
            tmp = majorAxis_;
            majorAxis_ = minorAxis_;
            minorAxis_ = tmp;
        }
        xCenter_ = xoffset + 0.5;
        yCenter_ = yoffset + 0.5;
    }

	// ----------------------------------------------------------------------------

    /** Computes the sums of the image. */
    private void computeSums(int region) throws Exception {

		if (labeledMap_ == null)
			throw new Exception("ERROR: labeledMap_ is null.");

    	float[] labeledMapPixels = (float[])labeledMap_.getPixels();
    	int width = labeledMap_.getWidth();

        xsum_ = 0.0;
        ysum_ = 0.0;
        x2sum_ = 0.0;
        y2sum_ = 0.0;
        xysum_ = 0.0;
        pixNumber_ = 0;
        int bitcountOfLine = 0;
        double xe, ye;
        int xSumOfLine = 0;

        for (int y=yMin_; y<=yMax_; y++) {
            bitcountOfLine = 0;
            xSumOfLine = 0;
            for (int x=xMin_; x<=xMax_; x++) {
            	if((int)labeledMapPixels[x+y*width] == region) {
                    bitcountOfLine++;
                    xSumOfLine += x;
                    x2sum_ += x * x;
                }
            }
            xsum_ += xSumOfLine;
            ysum_ += bitcountOfLine * y;
            ye = y;
            xe = xSumOfLine;
            xysum_ += xe*ye;
            y2sum_ += ye*ye*bitcountOfLine;
            pixNumber_ += bitcountOfLine;
        }
    }

	// ----------------------------------------------------------------------------

    /** Computes the moments of the image. */
    private void computeMoments() {

        double   x1, y1, x2, y2, xy;

        if (pixNumber_ == 0)
            return;

        x2sum_ += 0.08333333 * pixNumber_;
        y2sum_ += 0.08333333 * pixNumber_;
        x1 = xsum_/pixNumber_;
        y1 = ysum_ / pixNumber_;
        x2 = x2sum_ / pixNumber_;
        y2 = y2sum_ / pixNumber_;
        xy = xysum_ / pixNumber_;
        xm_ = x1;
        ym_ = y1;
        u20_ = x2 - (x1 * x1);
        u02_ = y2 - (y1 * y1);
        u11_ = xy - x1 * y1;
    }

	// ============================================================================
	// SETTERS AND GETTERS

    public Point2D.Double getCenter() { return new Point2D.Double(xCenter_, yCenter_); }
    public double getMajorAxis() { return majorAxis_; }
    public double getMinorAxis() { return minorAxis_; }
    public double getAngle() { return angle_; }
}