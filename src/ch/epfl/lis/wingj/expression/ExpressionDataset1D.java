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
import java.io.File;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.utilities.Filters;
import ch.epfl.lis.wingj.utilities.StringUtils;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Structure;

import ij.ImagePlus;
import ij.process.FloatProcessor;

/**
 * Generates 1D expression datasets.
 * <p>
 * This class implements a thread worker to allow computing 1D expression
 * datasets in parallel. Therefore one must take care during the implementation
 * that 1) specific parameters must be set directly in the object and not taken
 * from WJSettings, 2) classes like WPouchStructure, Boundary, etc. must not be modified
 * by thread and 3) if ImagePlus or dialog (visualization, plot, etc.) are generated
 * for the sake of one thread, the others threads must not close them. In the current
 * implementation, only "scale" and "normalize" are taken from WJSettings.
 * 
 * @version September 8, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionDataset1D extends SwingWorker<Void, Void> {
	
	/** Image to quantify. */
	protected ImagePlus image_ = null;
	/** Structure object. */
	protected Structure structure_ = null;

	/** Reference boundary. */
	protected int referenceBoundary_ = WJSettings.BOUNDARY_DV;
	/** Specifies in % (takes values in [-100,100]) of the length of one axis how much the trajectory should be shifted. */
	private double trajectoryOffset_ = 0.;
	/** Sigma of the 1D Gaussian filter in [UNIT]. */
	private double sigma_ = 1.;
	/** Gene name for plot title. */
	private String geneName_ = "";
	
	/** Trajectory along which the expression is quantified. */
	private Boundary trajectory_ = null;
	/** Length in [UNIT] of the negative part of the above trajectory. */
	private double negativeTrajectoryLengthInUm_ = 0.;
	
	/** Gene expression profile. */
	private ExpressionProfile profile_ = null;
	/** Gene expression plot. */
	private ExpressionPlot plot_ = null;
	/** Gene expression domain visualization. */
	private ExpressionDomain domain_ = null;
	
	/**
	 * If null or empty, show the 1D expression dataset generated.
	 * Otherwise, save the 1D expression dataset to files
	 */
	protected String filename_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Generate the 1D expression plot. */
	private ExpressionPlot generatePlot() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
			
		String title = geneName_ + " expression level along the";
		String translationDirection = "";
		if (referenceBoundary_ == WJSettings.BOUNDARY_DV) {
			if (trajectoryOffset_ > 0) translationDirection = " with " + Math.abs(trajectoryOffset_) + "% dorsal offset";
			else if (trajectoryOffset_ < 0) translationDirection = " with " + Math.abs(trajectoryOffset_) + "% ventral offset";
			title += " D/V boundary (A-P axis)" + translationDirection;
		} else if (referenceBoundary_ == WJSettings.BOUNDARY_AP) {
			if (trajectoryOffset_ > 0) translationDirection = " with " + Math.abs(trajectoryOffset_) + "% posterior offset";
			else if (trajectoryOffset_ < 0) translationDirection = " with " + Math.abs(trajectoryOffset_) + "% anterior offset";
			title += " A/P boundary (V-D axis)" + translationDirection;
		} else
			throw new Exception("ERROR: Invalid compartment boundary.");
		
		String xlabel = "X (" + settings.getUnit() + ")";
		String ylabel = "[" + geneName_ + "] (a.u.)";

		return new ExpressionPlot(title, xlabel, ylabel, profile_);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Quantify gene expression from the given image along the specified trajectory.
	 * <p>
	 * A 1D Gaussian filter slides along the trajectory to obtain more robust quantification (domain width = 6*sigma [UNIT]).
	 * The spatial length of the negative part of the profile must also be provide in [UNIT].
	 */
	private ExpressionProfile quantifyExpression(ImagePlus image, Boundary trajectory, double sigma, double negativeLengthInUm) throws Exception {
		
		if (image == null || image.getProcessor() == null)
			throw new Exception("ERROR: Image is null.");
		if (trajectory == null)
			throw new Exception("ERROR: Trajectory is null.");
		
		WJSettings settings = WJSettings.getInstance();
		
		int length = trajectory.npoints;
		double[] xaxis = new double[length];
		double[] yaxis = new double[length];
		
		// duplicate the source
		ImagePlus source = image.duplicate();

		Point2D.Double[] normalVectors = trajectory.getSmoothedNormalVectors();
		FloatProcessor proc =  (FloatProcessor) source.getProcessor();
		
		for(int i=0; i<length; i++) {
			yaxis[i] = Filters.apply1DGaussianFilterOnDirectionForPoint(trajectory.xpoints[i], trajectory.ypoints[i], normalVectors[i], proc, sigma);
   		}

   		// set the spatial dimension (x-axis)
   		// a priori: the boundaries are correctly oriented, either D to V or A to P (should be V to D!)
   		double dx2 = 0.;
   		double dy2 = 0.;
   		xaxis[0] = 0;
   		for (int i = 1; i < length; i++) {
   			dx2 = Math.pow(trajectory.xpoints[i]-trajectory.xpoints[i-1], 2);
   			dy2 = Math.pow(trajectory.ypoints[i]-trajectory.ypoints[i-1], 2);
   			xaxis[i] = xaxis[i-1] + Math.sqrt(dx2 + dy2) * settings.getScale();
   			xaxis[i-1] = xaxis[i-1] - negativeLengthInUm;
   		}
   		xaxis[length-1] = xaxis[length-1] - negativeLengthInUm;
		   		
		ExpressionProfile profile = new ExpressionProfile();
		profile.setX(xaxis);
		profile.setY(yaxis);
		profile.setTrajectory(trajectory.copy());
		
		if (settings.normalizeExpression())
			profile.normalize();
		
		return profile;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Compute the 1D trajectory as a translated version of the selected boundary
	 * along which the expression will be quantified.
	 * @param structure Identified structure.
	 * @param referenceBoundary Takes value WJSettings.BOUNDARY_AP or WJSettings.BOUNDARY_DV.
	 * @param offset Takes values in [-1,1].
	 */
	private void computeTrajectoryAsTranslatedBoundary(Structure structure, int referenceBoundary, double offset) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		offset = trajectoryOffset_ / 100.; // convert to [-1,1]
		
		// returns trajectory_ and negativeTrajectoryLengthInUm_
		trajectory_ = new Boundary();
		negativeTrajectoryLengthInUm_ = structure.getExpressionTrajectory(trajectory_, referenceBoundary, offset);

		// set the sampling rate, i.e. the number of measurement points
		if(settings.getExpression1DResolutionStrategy() == WJSettings.EXPRESSION_1D_RESOLUTION_CONSTANT) {
			int nPoints = settings.getExpression1DNumPoints();
			if(nPoints<1)
				throw new Exception("ERROR: Invalid number of points.");
			trajectory_ = trajectory_.resample(nPoints);
		}
		else if(settings.getExpression1DResolutionStrategy() == WJSettings.EXPRESSION_1D_RESOLUTION_DYNAMIC) {
			double stepSize = settings.getExpression1DStepSize();
			if(stepSize == 0)
				throw new Exception("ERROR: Invalid step size.");
			trajectory_ = trajectory_.resample((int)Math.round(trajectory_.length() / stepSize));
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public ExpressionDataset1D() {}
	
	// ----------------------------------------------------------------------------
	
	/** Run method. */
	@Override
	protected Void doInBackground() throws Exception {

		WingJ.getInstance().registerActiveExpressionDatasetProcess();
		generateDataset();
		return null;
	}

	// ----------------------------------------------------------------------------
	
	/** This function is called once the dataset has been generated. */
    @Override
    public void done() {
		
    	try {
			get();
			export();
			
		} catch (Exception e1) {
			String eStr = StringUtils.exceptionToString(e1);
			if (eStr.contains("OutOfMemoryError")) {
				WJMessage.showMessage(WJSettings.OUT_OF_MEMORY_ERROR_MESSAGE, "ERROR");
				// DO NOT REMOVE THIS LINE
				// ANALYTICS CODE: START
				Analytics.getInstance().incrementNumOutOfMemoryErrors();
				// END
			} else {
				WJMessage.showMessage(e1);
			}
		} finally {
			WingJ.getInstance().removeActiveExpressionDatasetProcess();
		}
	}
    
    // ----------------------------------------------------------------------------
    
	/** Generate the 1D expression dataset. */
	public void generateDataset() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// define the trajectory along which the expression is measured
		// the trajectory is a vector of points
		computeTrajectoryAsTranslatedBoundary(structure_, referenceBoundary_, trajectoryOffset_); // version 1
//		computeTrajectoryFromGrid(snake_, pouch_, referenceBoundary_, trajectoryOffset_); // version 2
		
		// get profile_
		profile_ = quantifyExpression(image_, trajectory_, sigma_, negativeTrajectoryLengthInUm_);
		
		String title = geneName_ + "_expression_profile";
		switch (referenceBoundary_) {
			case WJSettings.BOUNDARY_DV: title += "_DV"; break;
			case WJSettings.BOUNDARY_AP: title += "_AP"; break;
			default: WJSettings.log("ERROR: Invalid refenrece boundary.");
		}
		title += Double.toString(trajectoryOffset_);
		profile_.setName(title);
		
		// generate plot
		if (filename_ == null || settings.getExpression1DSavePdf())
			plot_ = generatePlot(); // requires profile_
		// generate expression domain visualization
		if (filename_ == null || settings.getExpression1DSaveMeasurementDomain()) {
			domain_ = new ExpressionDomain();
			// set meaningful name
			title = title.replace("profile", "domain");
			domain_.setTitle(title);
			// generate
			domain_.setImage(image_);
			domain_.setCompartment(structure_);
			domain_.setTrajectory(trajectory_);	//trajectory_.restrictToCompartment(pouch_)		
		}
	}
	
	// ----------------------------------------------------------------------------
    
    /** Exports dataset. */
    public void export() throws Exception {
    	
		if (filename_ == null || filename_.compareTo("") == 0) { // show datasets
			WJSettings.log("Showing expression dataset.");
			// show the domain first
			domain_.generateVisualization(true);
			// and then on top display the plot
			plot_.generatePlot();
			plot_.setVisible(true);
		}
		else { // save datasets to file
			
			WJSettings.log("Exporting 1D expression profile dataset.");
			
			String referenceBoundaryStr = "";
			switch (referenceBoundary_) {
				case WJSettings.BOUNDARY_DV: referenceBoundaryStr = "D/V"; break;
				case WJSettings.BOUNDARY_AP: referenceBoundaryStr = "A/P"; break;
				default: WJSettings.log("ERROR: Invalid reference boundary.");
			}
			String offsetStr = Double.toString(trajectoryOffset_);
			
			try {
				File file = new File(filename_ + ".txt");
		    	profile_.write(file.toURI());
		    	WJSettings.log("[x] Writing " + geneName_ + " expression profile data [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (txt)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing " + geneName_ + " expression profile data [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (txt)");
				WJMessage.showMessage(e);
			}
			
			if (WJSettings.getInstance().getExpression1DSavePdf()) {
				try {						
					File file = new File(filename_ + ".pdf");
			    	plot_.generatePlot();
			    	plot_.savePDF(file.toURI());
			    	plot_.dispose();
			    	WJSettings.log("[x] Writing " + geneName_ + " expression profile plot [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (pdf)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing " + geneName_ + " expression profile plot [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (pdf)");
					WJMessage.showMessage(e);
				}
			}
			
			if (WJSettings.getInstance().getExpression1DSaveMeasurementDomain()) {
				try {
					File file = new File(filename_ + ".tif");
			    	domain_.generateVisualization(false);
			    	domain_.saveTIFF(file.toURI());
			    	// IMPORTANT: do not use disposeLastInstanceVisible()
			    	// because it could close the dialog of another
			    	// ExpressionDataset1D object
			    	domain_.close();
			    	WJSettings.log("[x] Writing " + geneName_ + " expression profile domain [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing " + geneName_ + " expression profile domain [ref = " + referenceBoundaryStr + ", offset = " + offsetStr + "] (tif)");
					WJMessage.showMessage(e);
				}
			}
		}
		WJSettings.log("Done");
    }
    
    // ----------------------------------------------------------------------------
    
	/** Closes old expression images open (if any). */
	public static void disposeVisibleOutput() throws Exception {
		
    	ExpressionPlot.disposeAll();
    	ExpressionDomain.disposeAll();
	}
    
	// ============================================================================
	// SETTERS AND GETTERS

   	public void setExpressionImage(ImagePlus image) { image_ = image; }
   	public ImagePlus getExpressionImage() { return image_; }
    
   	public void setStructure(Structure structure) { structure_ = structure; }
   	public Structure getStructure() { return structure_; }
   	
   	public void setReferenceBoundary(int reference) { referenceBoundary_ = reference; }
   	public int getReferenceBoundary() { return referenceBoundary_; }
   	
   	public void setTrajectoryOffset(double offset) { trajectoryOffset_ = offset; }
   	public double getTrajectoryOffset() { return trajectoryOffset_; }
   	
   	public void setSigma(double sigma) { sigma_ = sigma; }
   	public double getSigma() { return sigma_; }
   	
   	public void setGeneName(String name) { geneName_ = name; }
   	public String getGeneName() { return geneName_; }
   	
	public void setFilename(String filename) { filename_ = filename; }   	
   	
   	public ExpressionProfile getExpressionProfile() { return profile_; }
}
