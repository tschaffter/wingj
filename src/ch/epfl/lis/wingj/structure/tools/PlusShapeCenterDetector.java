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
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import ch.epfl.lis.wingj.structure.Compartment;

/**
 * Tests one point which can be considered as the center of the wing pouch and tries to optimize it.
 * 
 * @version October 16, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class PlusShapeCenterDetector {
	
	/** Default overall size of the optimizer. */
	public static final Dimension CENTER_OPTIMIZER_DEFAULT_OVERALL_GEOMETRY = new Dimension(80, 80);
	/** Default background ROIs size of the optimizer. */
	public static final Dimension CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY = new Dimension(50, 50);
	
	/** center_ default color. */
	public static final Color CENTER_COLOR = Color.RED;
	/** correctedCenter_ default color. */
	public static final Color CORRECTED_CENTER_COLOR = Color.GREEN;
	/** Background default color. */
	public static final Color BACKGROUND_COLOR = Color.RED;
	/** Cross default color. */
	public static final Color CROSS_COLOR = Color.BLUE;
	/** Profile lines default color. */
	public static final Color PROFILE_LINES_COLOR = Color.BLUE;
	
	/** Line width used to draw ROIs */
	public static int ROI_DRAWING_LINE_WIDTH = 1;
	
	/** Maximum number of steps done in optimize(). */
	protected static int CENTER_OPTIMIZER_MAX_ITERS = 10;
	/** If the error is smaller than the one defined, the optimization ends (in px). */
	protected static double CENTER_OPTIMIZER_MIN_ERROR_DIFF = 4.;
	
	/** Image (ideally already blurred with ppBlur/2.). */
	protected ImagePlus image_ = null;
	
	/** Center to test. */
	protected Point2D.Double center_ = null;
	/** Corrected center. */
	protected Point2D.Double correctedCenter_ = null;
	
	/** Dimensions of the overall center optimizer. */
	protected Dimension overallRoiDims_ = null;
	/** Dimensions of the ROIs where background expression intensities are measured. */
	protected Dimension backgroundRoiDims_ = null;
	
	/** Background ROI. */
	protected List<PolygonRoi> backgroundRois_ = new ArrayList<PolygonRoi>();
	/** Cross ROI. */
	protected List<PolygonRoi> crossRois_ = new ArrayList<PolygonRoi>();
	/** Lines along which pixels value is read. */
	protected List<Line2D> profileLines_ = new ArrayList<Line2D>();
	
	/** Mean expression intensity for each background ROIs. */
	protected List<Double> backgroundMeanIntensities_ = new ArrayList<Double>();
	/** Mean expression intensitiy for each cross ROIs. */
	protected List<Double> crossMeanIntensities_ = new ArrayList<Double>();
	
	/** Mean expression intensity for all background ROIs. */
	protected double backgroundIntensityMean_ = 0.;
	/** Standard deviation intensity for all background ROIs. */
	protected double backgroundIntensityStd_ = 0.;
	
	/** Mean expression intensity for all cross ROIs. */
	protected double crossIntensityMean_ = 0.;
	/** Standard deviation intensity for all cross ROIs. */
	protected double crossIntensityStd_ = 0.;
	
	/** Cross pixel values profiles. */
	protected List<Double[]> crossIntensityProfiles_ = new ArrayList<Double[]>();
	
	/** If the tracker leaves this domain while being optimized, it stops. */
	protected Compartment aoi_ = null;
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Sets ROIs. */
	protected void setRois() throws Exception {
		
		if (center_ == null)
			throw new Exception("ERROR: center_ is null");
		if (overallRoiDims_ == null)
			throw new Exception("ERROR: backgroundRoiOrigin_ is null.");
		if (backgroundRoiDims_ == null || backgroundRoiDims_.width == 0 || backgroundRoiDims_.height == 0)
			throw new Exception("ERROR: backgroundRoiDims_ is null or has at least one dimension equals zero.");
		
		backgroundRois_.clear();
		crossRois_.clear();
		profileLines_.clear();
		
		// BACKGROUND ROIS
		// upper left
		float x0 = (float) (center_.x - overallRoiDims_.width);
		float y0 = (float) (center_.y - overallRoiDims_.height);
		float width = backgroundRoiDims_.width;
		float height = backgroundRoiDims_.height;
		float[] xpointsB1 = {x0, x0+width, x0+width, x0}; // IMPORTANT: new PolygonRoi keep the reference, i.e. doesn't make a copy
		float[] ypointsB1 = {y0, y0, y0+height, y0+height};
		backgroundRois_.add(new PolygonRoi(xpointsB1, ypointsB1, 4, Roi.POLYGON));
		// upper right
		x0 = (float) (center_.x + overallRoiDims_.width - backgroundRoiDims_.width);
		y0 = (float) (center_.y - overallRoiDims_.height);
		float[] xpointsB2 = {x0, x0+width, x0+width, x0};
		float[] ypointsB2 = {y0, y0, y0+height, y0+height};
		backgroundRois_.add(new PolygonRoi(xpointsB2, ypointsB2, 4, Roi.POLYGON));
		// bottom right
		x0 = (float) (center_.x + overallRoiDims_.width - backgroundRoiDims_.width);
		y0 = (float) (center_.y + overallRoiDims_.height - backgroundRoiDims_.height);	
		float[] xpointsB3 = {x0, x0+width, x0+width, x0};
		float[] ypointsB3 = {y0, y0, y0+height, y0+height};
		backgroundRois_.add(new PolygonRoi(xpointsB3, ypointsB3, 4, Roi.POLYGON));
		// bottom left
		x0 = (float) (center_.x - overallRoiDims_.width);
		y0 = (float) (center_.y + overallRoiDims_.height - backgroundRoiDims_.height);	
		float[] xpointsB4 = {x0, x0+width, x0+width, x0};
		float[] ypointsB4 = {y0, y0, y0+height, y0+height};
		backgroundRois_.add(new PolygonRoi(xpointsB4, ypointsB4, 4, Roi.POLYGON));
		
		// CROSS ROIS
		// -+ROI_DRAWING_LINE_WIDTH to not overlap with backgorund ROIs
		// North
		x0 = (float) (backgroundRois_.get(0).getBounds().x + backgroundRoiDims_.width + ROI_DRAWING_LINE_WIDTH);
		y0 = (float) (backgroundRois_.get(0).getBounds().y);
		width = 2 * (overallRoiDims_.width - backgroundRoiDims_.width - ROI_DRAWING_LINE_WIDTH);
		height = backgroundRoiDims_.height;
		float[] xpointsC1 = {x0, x0+width, x0+width, x0};
		float[] ypointsC1 = {y0, y0, y0+height, y0+height};
		crossRois_.add(new PolygonRoi(xpointsC1, ypointsC1, 4, Roi.POLYGON));
		// East
		x0 = (float) (backgroundRois_.get(1).getBounds().x);
		y0 = (float) (backgroundRois_.get(1).getBounds().y + backgroundRoiDims_.height + ROI_DRAWING_LINE_WIDTH);
		width = backgroundRoiDims_.width;
		height = 2 * (overallRoiDims_.height - backgroundRoiDims_.height - ROI_DRAWING_LINE_WIDTH);
		float[] xpointsC2 = {x0, x0+width, x0+width, x0};
		float[] ypointsC2 = {y0, y0, y0+height, y0+height};
		crossRois_.add(new PolygonRoi(xpointsC2, ypointsC2, 4, Roi.POLYGON));
		// South
		width = 2 * (overallRoiDims_.width - backgroundRoiDims_.width - ROI_DRAWING_LINE_WIDTH);
		height = backgroundRoiDims_.height;
		x0 = (float) (backgroundRois_.get(2).getBounds().x - width - ROI_DRAWING_LINE_WIDTH);
		y0 = (float) (backgroundRois_.get(2).getBounds().y);
		float[] xpointsC3 = {x0, x0+width, x0+width, x0};
		float[] ypointsC3 = {y0, y0, y0+height, y0+height};
		crossRois_.add(new PolygonRoi(xpointsC3, ypointsC3, 4, Roi.POLYGON));
		// West
		width = backgroundRoiDims_.width;
		height = 2 * (overallRoiDims_.height - backgroundRoiDims_.height - ROI_DRAWING_LINE_WIDTH);
		x0 = (float) (backgroundRois_.get(3).getBounds().x);
		y0 = (float) (backgroundRois_.get(3).getBounds().y - height - ROI_DRAWING_LINE_WIDTH);
		float[] xpointsC4 = {x0, x0+width, x0+width, x0};
		float[] ypointsC4 = {y0, y0, y0+height, y0+height};
		crossRois_.add(new PolygonRoi(xpointsC4, ypointsC4, 4, Roi.POLYGON));
		
		// COMPUTE THE PROFILE DIMENSIONS
		// North		
		double x1 = crossRois_.get(0).getBounds().x;
		double y1 = crossRois_.get(0).getBounds().y + crossRois_.get(0).getBounds().height/2.;
		double x2 = x1 + crossRois_.get(0).getBounds().getWidth();
		double y2 = y1;
		profileLines_.add(new Line2D.Double(x1, y1, x2, y2));
		// East
		x1 = crossRois_.get(1).getBounds().x + crossRois_.get(1).getBounds().width/2.;
		y1 = crossRois_.get(1).getBounds().y;
		x2 = x1;
		y2 = y1 + crossRois_.get(1).getBounds().height;
		profileLines_.add(new Line2D.Double(x1, y1, x2, y2));
		// South
		x1 = crossRois_.get(2).getBounds().x;
		y1 = crossRois_.get(2).getBounds().y + crossRois_.get(2).getBounds().height/2.;
		x2 = x1 + crossRois_.get(2).getBounds().width;
		y2 = y1;
		profileLines_.add(new Line2D.Double(x1, y1, x2, y2));
		// West
		x1 = crossRois_.get(3).getBounds().x + crossRois_.get(3).getBounds().width/2.;
		y1 = crossRois_.get(3).getBounds().y;
		x2 = x1;
		y2 = y1 + crossRois_.get(3).getBounds().height;
		profileLines_.add(new Line2D.Double(x1, y1, x2, y2));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Rotates the tracker around the given point clockwise (angle in degree). */
	protected void rotateRois(Point2D.Double anchor, double angle) throws Exception {
		
		PolygonRoi roi = null;
		
		// rotate background squares
		for (int i = 0; i < backgroundRois_.size(); i++) {
			roi = backgroundRois_.get(i);
			backgroundRois_.set(i, rotateRoi(roi, anchor, angle));			
		}
		
		// rotate the cross
		for (int i = 0; i < crossRois_.size(); i++) {
			roi = crossRois_.get(i);
			crossRois_.set(i, rotateRoi(roi, anchor, angle));			
		}
		
		for (int i = 0; i < profileLines_.size(); i++) {
			Line2D.Double line = (Line2D.Double) profileLines_.get(i);
			profileLines_.set(i, rotateLine2D(line, anchor, angle));
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Rotates a single ROI around the given point clockwise (angle in degree). */
	protected PolygonRoi rotateRoi(PolygonRoi roi, Point2D.Double anchor, double angle) throws Exception {
		
		if (roi == null)
			throw new Exception("ERROR: roi is null.");
		
		int N = roi.getNCoordinates();
		int x0 = roi.getBounds().x;
		int y0 = roi.getBounds().y;
		
		// format the points for the transformer
		float[] srcPts = new float[2*N];
		float[] dstPts = new float[2*N];
		for (int j = 0; j < N; j++) {
			srcPts[2*j] = x0 + roi.getXCoordinates()[j];
			srcPts[2*j+1] = y0 + roi.getYCoordinates()[j];
		}
		
		// transforms
		AffineTransform transformer = AffineTransform.getRotateInstance(Math.toRadians(angle), anchor.x, anchor.y);
		transformer.transform(srcPts, 0, dstPts, 0, N);
		
		// create a new RoiPolygon
		float[] xpoints = new float[N];
		float[] ypoints = new float[N];
		for (int j = 0; j < N; j++) {
			xpoints[j] = dstPts[2*j];
			ypoints[j] = dstPts[2*j+1];
		}
		
		return new PolygonRoi(xpoints, ypoints, xpoints.length, Roi.POLYGON);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Rotates a single Line2D object around the given point clockwise (angle in degree). */
	protected Line2D.Double rotateLine2D(Line2D.Double line, Point2D.Double anchor, double angle) throws Exception {
	
		if (line == null)
			throw new Exception("ERROR: line is null.");
		
		// format the points for the transformer
		float[] srcPts = new float[4];
		float[] dstPts = new float[4];
		srcPts[0] = (float) line.x1; srcPts[1] = (float) line.y1; srcPts[2] = (float) line.x2; srcPts[3] = (float) line.y2;
		
		// transforms
		AffineTransform transformer = AffineTransform.getRotateInstance(Math.toRadians(angle), anchor.x, anchor.y);
		transformer.transform(srcPts, 0, dstPts, 0, 2);
		
		return new Line2D.Double(dstPts[0], dstPts[1], dstPts[2], dstPts[3]);
	}

	// ----------------------------------------------------------------------------
	
	/** Draws the ROIs on the given image. */
	protected void drawRois(ImagePlus image) throws Exception {
		
		if (image == null || image.getProcessor() == null)
			throw new Exception("ERROR: image is null.");
		if (center_ == null)
			throw new Exception("ERROR: center_ is null.");
		if (backgroundRois_ == null)
			throw new Exception("ERROR: backgroundRois_ is null.");
		if (crossRois_ == null)
			throw new Exception("ERROR: crossRois_ is null.");
	
		ImageProcessor ip = image.getProcessor();
		
		// draw center
		if (CENTER_COLOR != null) {
			ip.setColor(CENTER_COLOR);
			ip.drawOval((int) (center_.x - 2), (int) (center_.y - 2), 4, 4);
		}
		
		// draw background ROIs
		if (BACKGROUND_COLOR != null) {
			ip.setColor(BACKGROUND_COLOR);
			for (PolygonRoi roi : backgroundRois_) {
				roi.setStrokeWidth(ROI_DRAWING_LINE_WIDTH);
				image.getProcessor().draw(roi);
			}
		}
		
		// draw cross ROIs
		if (CROSS_COLOR != null) {
			ip.setColor(CROSS_COLOR);
			for (Roi roi : crossRois_) {
				roi.setStrokeWidth(ROI_DRAWING_LINE_WIDTH);
				image.getProcessor().draw(roi);
			}
		}
		
		// draw profile lines
		if (PROFILE_LINES_COLOR != null) {
			ip.setColor(PROFILE_LINES_COLOR);
			ip.setLineWidth(ROI_DRAWING_LINE_WIDTH);
			for (int i = 0; i < profileLines_.size(); i++) {
				Line2D.Double line = (Line2D.Double) profileLines_.get(i);
				ip.moveTo((int) line.x1, (int) line.y1);
				ip.lineTo((int) line.x2, (int) line.y2);
//				image.getProcessor().drawLine((int) line.x1, (int) line.y1, (int) line.x2, (int) line.y2);
			}
		}
		
		// draw corrected center
		if (correctedCenter_ != null && CORRECTED_CENTER_COLOR != null) {
			ip.setColor(CORRECTED_CENTER_COLOR);
			ip.drawOval((int) (correctedCenter_.x - 2), (int) (correctedCenter_.y - 2), 4, 4);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Measures mean expression intensity in each ROIs. */
	protected void measureExpression() throws Exception {
		
		if (image_ == null || image_.getProcessor() == null)
			throw new Exception("ERROR: image is null.");
		
		backgroundMeanIntensities_.clear();
		crossMeanIntensities_.clear();
		crossIntensityProfiles_.clear();
		
		// compute background means
		for (PolygonRoi roi : backgroundRois_) {
			image_.setRoi(roi);
			if (roi != null)
				backgroundMeanIntensities_.add(image_.getStatistics(ImageStatistics.MEAN).mean);
		}
		
		// compute cross means
		for (PolygonRoi roi : crossRois_) {
			image_.setRoi(roi);
			if (roi != null)
				crossMeanIntensities_.add(image_.getStatistics(ImageStatistics.MEAN).mean);
		}
		
		// compute background mean
		backgroundIntensityMean_ = 0.;
		for (Double d : backgroundMeanIntensities_)
			backgroundIntensityMean_ += d;
		backgroundIntensityMean_ /= backgroundMeanIntensities_.size();
		
		// compute cross mean
		crossIntensityMean_ = 0.;
		for (Double d : crossMeanIntensities_)
			crossIntensityMean_ += d;
		crossIntensityMean_ /= crossMeanIntensities_.size();
		
		// compute background std
		double sum = 0.;
		for (int i = 0; i < backgroundMeanIntensities_.size(); i++)
			sum += Math.pow(backgroundMeanIntensities_.get(i) - backgroundIntensityMean_, 2);
		backgroundIntensityStd_ = Math.sqrt(sum / (double) backgroundMeanIntensities_.size());
		
		// compute cross std
		sum = 0.;
		for (int i = 0; i < crossMeanIntensities_.size(); i++)
			sum += Math.pow(crossMeanIntensities_.get(i) - crossIntensityMean_, 2);
		crossIntensityStd_ = Math.sqrt(sum / (double) crossMeanIntensities_.size());
		
		// compute background profiles
		ImageProcessor ip = image_.getProcessor();
		for (int i = 0; i < profileLines_.size(); i++) {
			Line2D.Double line = (Line2D.Double) profileLines_.get(i);
			crossIntensityProfiles_.add(ArrayUtils.toObject(ip.getLine(line.x1, line.y1, line.x2, line.y2)));
		}
		
//		// save to file
//		FileWriter fstream = new FileWriter("/home/tschaffter/profiles.txt");
//		BufferedWriter out = new BufferedWriter(fstream);
//		String content = "";
//		// suppose that u and v have the same dimension
//		for (int i = 0; i < crossIntensityProfiles_.get(0).length; i++) {
//			content += crossIntensityProfiles_.get(0)[i].toString();
//			for (int j = 1; j < crossIntensityProfiles_.size(); j++)
//				content += "\t" + crossIntensityProfiles_.get(j)[i].toString();
//			content += "\n";
//			
//		}
//		out.write(content);
//		out.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Suggests a new center based on expression profiles measured the cross ROIs. */
	protected void computeCorrectedCenter() throws Exception {
		
		if (crossRois_ == null || crossRois_.size() != 4)
			throw new Exception("ERROR: crossRois_ is null or doesn't meet the requirements.");
		
		int northMaxIndex = findMaxIndex(crossIntensityProfiles_.get(0));
		int eastMaxIndex = findMaxIndex(crossIntensityProfiles_.get(1));
		int southMaxIndex = findMaxIndex(crossIntensityProfiles_.get(2));
		int westMaxIndex = findMaxIndex(crossIntensityProfiles_.get(3));
		
		double x =  ((northMaxIndex + southMaxIndex) / 2.) + crossRois_.get(0).getBounds().x;
		double y = ((eastMaxIndex + westMaxIndex) / 2.) + crossRois_.get(1).getBounds().y ;
		correctedCenter_ = new Point2D.Double(x, y);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the distance between the center and the corrected center. */
	protected double computeCenterError() throws Exception {
		
		if (center_ == null)
			throw new Exception("ERROR: center_ is null.");
		if (correctedCenter_ == null)
			throw new Exception("ERROR: correctedCenter_ is null.");
		
		return center_.distance(correctedCenter_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy constructor. */
	protected PlusShapeCenterDetector(PlusShapeCenterDetector optimizer) {
		
		if (optimizer.image_ != null)
			image_ = new Duplicator().run(optimizer.image_);
		if (optimizer.center_ != null)
			center_ = (Point2D.Double) optimizer.center_.clone();
		if (optimizer.correctedCenter_ != null)
			correctedCenter_ = (Point2D.Double) optimizer.correctedCenter_.clone();
		if (optimizer.overallRoiDims_ != null)
			overallRoiDims_ = (Dimension) optimizer.overallRoiDims_.clone();
		if (optimizer.backgroundRoiDims_ != null)
			backgroundRoiDims_ = (Dimension) optimizer.backgroundRoiDims_.clone();
	}

	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public PlusShapeCenterDetector() {}
	
	// ----------------------------------------------------------------------------
	
	/** Copy operator. */
	public PlusShapeCenterDetector copy() throws Exception {
		
		PlusShapeCenterDetector optimizer = new PlusShapeCenterDetector(this);
		optimizer.setRois();
		
		return optimizer;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes from a center point only. */
	public void initialize(Point2D.Double center) throws Exception {
		
		initialize(center, null, CENTER_OPTIMIZER_DEFAULT_OVERALL_GEOMETRY, CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY, 1.0);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes from a center point only. */
	public void initialize(Point2D.Double center, Compartment aoi) throws Exception {
		
		initialize(center, aoi, CENTER_OPTIMIZER_DEFAULT_OVERALL_GEOMETRY, CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY, 1.0);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes from a center point and a scale factor. */
	public void initialize(Point2D.Double center, Compartment aoi, double scaleCoeff) throws Exception {
		
		initialize(center, aoi, CENTER_OPTIMIZER_DEFAULT_OVERALL_GEOMETRY, CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY, scaleCoeff);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialize. */
	public void initialize(Point2D.Double center, Compartment aoi, Dimension overallRoiDims, Dimension backgroundRoiDims) throws Exception {
		
		initialize(center, aoi, overallRoiDims, backgroundRoiDims, 1.0);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes (optimizer geometry scaled using the given scale coefficient). */
	public void initialize(Point2D.Double center, Compartment aoi, Dimension overallRoiDims, Dimension backgroundRoiDims, double scaleCoeff) throws Exception {
		
//		if (scaleCoeff < 0.1 || scaleCoeff > 1.0)
//			throw new Exception("ERROR: scaleCoeff must be in [0.1, 1.0]");
		if (scaleCoeff < 0)
			throw new Exception("ERROR: Scale must be positive.");
		
		center_ = (Point2D.Double) center.clone();
		
		overallRoiDims_ = overallRoiDims;
		overallRoiDims_.width *= scaleCoeff;
		overallRoiDims_.height *= scaleCoeff;
		
		// initially overallRoiDims_ represented half of the length of a side
		// for user, it's more intuitive to given the length of the entire side, so:
		overallRoiDims_.width *= 0.5;
		overallRoiDims_.height *= 0.5;
		
		backgroundRoiDims_ = backgroundRoiDims;
		backgroundRoiDims_.width *= scaleCoeff;
		backgroundRoiDims_.height *= scaleCoeff;
		
		aoi_ = aoi;

		setRois();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Run. */
	public void run() throws Exception {
		
		measureExpression();
		computeCorrectedCenter();
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Optimizes the current center.
	 * <p>
	 * IMPORTANT: After the optimization, center_ and correctedCenter_ are identical.
	 */
	public boolean optimize() throws Exception {
		
		int iter = 0;
		boolean ok = false;
		while (iter < CENTER_OPTIMIZER_MAX_ITERS && !ok) {
			
			if (iter > 0)
				center_ = correctedCenter_;

			setRois();
			measureExpression();
			computeCorrectedCenter();
			
			if (computeCenterError() < CENTER_OPTIMIZER_MIN_ERROR_DIFF)
				ok = true;
			
			iter++;
		}
		
		// set the best point found
		center_ = correctedCenter_;
		setRois();
		
		return ok;
	}

	// ----------------------------------------------------------------------------
	
	/** Draws the ROIs on a duplicate of the image set and saved it to TIFF file. */
	public void drawRois(String filename) throws Exception {
		
		drawRois(image_, filename);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Draws the ROIs on a duplicate of the given image and saved it to TIFF file. */
	public void drawRois(ImagePlus image, String filename) throws Exception {
		
		if (image == null)
			throw new Exception("ERROR: image is null.");
		
		image.killRoi();
		ImagePlus img = new Duplicator().run(image);
		new ImageConverter(img).convertToRGB();
		drawRois(img);
		
		// save to file
		IJ.save(img, filename);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Prints intensity measurements. */
	public void printIntensityMeasurements() throws Exception {
		
		for (int i = 0; i < backgroundMeanIntensities_.size(); i++)
			System.out.println("Background intensity " + i + ": " + backgroundMeanIntensities_.get(i));
		System.out.println("Background intensity mean: " + backgroundIntensityMean_);
		System.out.println("Background intensity std: " + backgroundIntensityStd_);
		
		for (int i = 0; i < crossMeanIntensities_.size(); i++)
			System.out.println("Cross intensity " + i + ": " + crossMeanIntensities_.get(i));
		System.out.println("Cross intensity mean: " + crossIntensityMean_);
		System.out.println("Cross intensity std: " + crossIntensityStd_);
	}
	
   	// ----------------------------------------------------------------------------
	
	/** Returns the index of the largest value in the given array. */
	public static int findMaxIndex(Double[] A) {
		
		int index = 0;
		Double max = 0.;
		for (int i = 0; i < A.length; i++) {
			if (A[i] > max) {
				index = i;
				max = A[i];
			}
		}
		return index;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Cleans. */
	public void clean() {
		
		if (image_ != null)
			image_.close();
		
		backgroundRois_.clear();
		crossRois_.clear();
		profileLines_.clear();
		backgroundMeanIntensities_.clear();
		crossMeanIntensities_.clear();
		crossIntensityProfiles_.clear();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the ratio cross intensity over background intensity. */
	public double getScore() throws Exception {
		
		return crossIntensityMean_ / backgroundIntensityMean_;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setImage(ImagePlus image) { image_ = image; }
	public ImagePlus getImage() { return image_; }
	
	public Point2D.Double getCenter() { return center_; }
	public Point2D.Double getCorrectedCenter() { return correctedCenter_; }
	
	public Dimension getOverallRoiDims() { return overallRoiDims_; }
}
