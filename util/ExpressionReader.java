/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://wingj.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package expression;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.geom.Point2D;

import morphology.Boundary;
import utilities.Filters;
import core.WJSettings;

/**
 * Measures the protein concentration gradient along the given boundary.
 *
 * @version June 16, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 */
public class ExpressionReader {

	/** The unique instance of ExpressionReader (Singleton design pattern) */
	private static ExpressionReader instance_ = null;

	/** Projection of the expression image stack */
	private ImagePlus expressionImage_ = null;

	/** Boundary along which the gene expression will be measured */
	private Boundary boundary_ = null;

	/** Length of the negative domain of the spatial dimension */
	private double originshift_ = 0.;
	/** Sigma of the Gaussian used to filter the measurement in [px] */
	private double sigma_ = 1.;
	/** Offset in % (for plot title) */
	private int offset_ = 0;
	/** Gene name */
	private String geneName_ = "";

	/** Gene expression profile */
	private ExpressionProfile profile_ = null;
	/** Plot of the gene expression profile */
	private ExpressionPlot plot_ = null;

	// ============================================================================
	// PRIVATE METHODS

	/** Constructor */
	private ExpressionReader() {}

	// ----------------------------------------------------------------------------

	/** Returns an exception if input images are not compatible */
	private void checkStructureAndExpressionImagesCompatibility() throws Exception {

//		ImagePlus structure = WingJ.getInstance().getStructureStack();
//		ImagePlus expression = WingJ.getInstance().getExpressionStack();
//
//		int ch01Width = structure.getWidth();
//		int ch01Height = structure.getHeight();
//		int ch02Width = expression.getWidth();
//		int ch02Height = expression.getHeight();
//
//		if (ch01Width != ch02Width)
//			throw new Exception("WARNING: Structure and gene expression images must have the same width");
//		if (ch01Height != ch02Height)
//			throw new Exception("WARNING: Structure and gene expression images must have the same height");
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Get instance */
	static public ExpressionReader getInstance() {

		if (instance_ == null)
			instance_ = new ExpressionReader();
		return instance_;
	}

	// ----------------------------------------------------------------------------

	/** Initialize */
	public void initialize(ImagePlus image, Boundary boundary, double sigma, double xshift) {

		expressionImage_ = image;
		boundary_ = boundary;
		sigma_ = sigma;
		originshift_ = xshift;
	}

	// ----------------------------------------------------------------------------

	/** Interface to measure the gene expression profile */
	public void measureExpression() throws Exception {

		profile_ = measureExpression(expressionImage_, boundary_, sigma_, originshift_);
	}

	// ----------------------------------------------------------------------------

	/** Generic function to read the expression profile along a given boundary from confocal images */
	private ExpressionProfile measureExpression(ImagePlus images, Boundary boundary, double sigma, double xshift) throws Exception {

		WJSettings settings = WJSettings.getInstance();

		if (images == null)
			throw new Exception("INFO: Single image or image stack required.");
		if (boundary == null)
			throw new Exception("ERROR: boundary is null.");

		// Check that the structure stack and expression stack have the same dimensions
		// If not, an exception is thrown
		checkStructureAndExpressionImagesCompatibility();

		int length = boundary.npoints;
		double[] xaxis = new double[length];
		double[] yaxis = new double[length];

		Point2D.Double[] normalVectors = boundary.getSmoothedNormalVectors();
		FloatProcessor proc =  (FloatProcessor) expressionImage_.getProcessor();

		for(int i=0; i<length; i++) {
			yaxis[i] = Filters.apply1DGaussianFilterOnDirectionForPoint(boundary.xpoints[i], boundary.ypoints[i], normalVectors[i], proc, sigma);
   		}

   		// set the spatial dimension (x-axis)
   		// a priori: the boundaries are correctly oriented, either D to V or A to P
   		double dx2 = 0.;
   		double dy2 = 0.;
   		xaxis[0] = 0;
   		for (int i = 1; i < length; i++) {
   			dx2 = Math.pow(boundary.xpoints[i]-boundary.xpoints[i-1], 2);
   			dy2 = Math.pow(boundary.ypoints[i]-boundary.ypoints[i-1], 2);
   			xaxis[i] = xaxis[i-1] + Math.sqrt(dx2 + dy2) / settings.getScale();
   			xaxis[i-1] = xaxis[i-1] - originshift_;
   		}
   		xaxis[length-1] = xaxis[length-1] - originshift_;

		ExpressionProfile profile = new ExpressionProfile();
		profile.setName(boundary.getName() + "-profile");

		profile.setX(xaxis);
		profile.setY(yaxis);

		if (settings.normalizeExpression())
			profile.normalize();

		return profile;
	}

	// ----------------------------------------------------------------------------

	/** Shows a plot of the gene expression profile and show the plot */
	public void plot() throws Exception {

		plot(true);
	}

	// ----------------------------------------------------------------------------

	/** Shows a plot of the gene expression profile and show it if visible is true */
	public void plot(boolean visible) throws Exception {

		if (plot_ != null)
			plot_.dispose();

		String prefix = "Expression";
		if (geneName_.compareTo("") != 0) prefix = geneName_ + " expression";

		if (boundary_.getName().contains("D/V")) {
			plot_ = new ExpressionPlot(prefix + " along D/V boundary (offset " + offset_ + "%)", "D/V boundary (AP axis) [um]", profile_);
		}
		else if (boundary_.getName().contains("A/P")) {
			plot_ = new ExpressionPlot(prefix + " along A/P boundary (offset " + offset_ + "%)", "A/P boundary (DV axis) [um]", profile_);
		}
		else throw new Exception("ERROR: The boundary name must contain either \"D/V\" or \"A/P\".");
		plot_.plot(visible);
	}

	// ----------------------------------------------------------------------------

	/** Save the measured expression profile to file */
	public void save(String filename) throws Exception {

		if (profile_ == null)
			throw new Exception("INFO: profile_ is null.");

		profile_.save(filename);
	}

	// ----------------------------------------------------------------------------

	/** Close the plot */
	public void close() {

		try {
			if (plot_ != null) {
				plot_.dispose();
			}
		} catch (Exception e) {
			// here if the plot has been already closed
		}
	}

	// ============================================================================
	// SETTERS AND GETTERS

   	public void setBoundary(Boundary boundary) { boundary_ = boundary; }
   	public Boundary getBoundary() { return boundary_; }

   	public ImagePlus getExpressionImage() { return expressionImage_; }

   	public ExpressionProfile getExpressionProfile() { return profile_; }
   	public ExpressionPlot getExpressionPlot() { return plot_; }

   	public void setOffset(int value) { offset_ = value; }
   	public int getOffset() { return offset_; }

   	public void setGeneName(String name) { geneName_ = name; }
   	public String getGeneName() { return geneName_; }
}
