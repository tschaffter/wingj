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

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.plugin.filter.EDM;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.structure.geometry.FlatSphericalGridMaker;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.geometry.Grid;
import ch.epfl.lis.wingj.utilities.StringUtils;

/**
 * Generates 2D expression datasets.
 * <p>
 * This class implements a thread worker to allow computing 2D expression
 * datasets in parallel. Therefore one must take care during the implementation
 * that 1) specific parameters must be set directly in the object and not taken
 * from WJSettings, 2) classes like WPouchStructure, Boundary, etc. must not be modified
 * by thread and 3) if ImagePlus or dialog (visualization, plot, etc.) are generated
 * for the sake of one thread, the others threads must not close them.
 * 
 * @version May 18, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionDataset2D extends SwingWorker<Void, Void> {
	
	/** Default filename suffix for exporting raw projections. */
	public static final String EXPRESSION_PROJECTION_SUFFIX = "_projection";

	/** Image to quantify. */
	protected ImagePlus image_ = null;
	/** Structure object. */
	protected Structure structure_ = null;
	
	/** Gene name for plot title. */
	protected String geneName_ = "";
	/** Expression map boundary conserved value. */
	protected double boundaryConserved_ = 0.;

	/** 2D expression dataset (morphed version of the input image). */
	protected ImagePlus expressionMap_ = null;
	/** D/V circular density map. */
	protected ImagePlus dvCircularDensityMap_ = null;
	/** A/P circular density map. */
	protected ImagePlus apCircularDensityMap_ = null;

	/**
	 * If null or empty, show the 2D expression dataset generated.
	 * Otherwise, save the 2D expression dataset to files.
	 */
	protected String filename_ = null;

	/** Auxiliary class to perform Euclidean Distance Map (EDM). */
	protected EDM edm_ = null;

	// ============================================================================
	// PRIVATE METHODS

	/** Generates the 2D expression dataset. */
	private void generateDataset() throws Exception {
		
		WJSettings.log("Computing individual expression map.");
		WJSettings.log("A message will be displayed at the end of the process.");

		expressionMap_ = computeExpressionMap();
	}

	// ----------------------------------------------------------------------------

	/** Combines two morphed expression maps. */
	private ImagePlus computeExpressionMap() throws Exception {

		WJSettings settings = WJSettings.getInstance();
		ImagePlusManager manager = ImagePlusManager.getInstance();

		// generate two grids, one where the D-V axis is not distorted and the other where the A-P axis is not distorted
		FlatSphericalGridMaker wPouchMorpher = new FlatSphericalGridMaker(structure_);

		boundaryConserved_ = settings.getExpression2DStitchingDensityDifferenceThld();

		int nPoints = settings.getExpression2DNumPoints();
		if (nPoints % 2 == 0) { // nPoints must be even
			nPoints = (nPoints/2)+1;
		} else {
			nPoints = ((nPoints-1)/2)+1;
		}

		Grid gridDVEquator = wPouchMorpher.generateSphereLikeGrid(WJSettings.BOUNDARY_DV, nPoints);
		Grid gridAPEquator = wPouchMorpher.generateSphereLikeGrid(WJSettings.BOUNDARY_AP, nPoints);

		FloatProcessor expressionDVEquator = CircularExpressionMap.computeExpressionMap(image_, structure_, gridDVEquator, WJSettings.BOUNDARY_DV);
		FloatProcessor expressionAPEquator = CircularExpressionMap.computeExpressionMap(image_, structure_, gridAPEquator, WJSettings.BOUNDARY_AP);
		expressionAPEquator.flipVertical();

		FloatProcessor samplingDensityDVEquator = CircularExpressionMap.computeSamplingDensityMap(image_.getWidth(), image_.getHeight(), structure_, gridDVEquator, WJSettings.BOUNDARY_DV);
		FloatProcessor samplingDensityAPEquator = CircularExpressionMap.computeSamplingDensityMap(image_.getWidth(), image_.getHeight(), structure_, gridAPEquator, WJSettings.BOUNDARY_AP);

		ImagePlus img = null;
		
		if (WJSettings.DEBUG) {
			dvCircularDensityMap_ = new ImagePlus(geneName_ + "_morphing_dv_conserved_density_map", samplingDensityDVEquator);
			manager.add(dvCircularDensityMap_.getTitle(), dvCircularDensityMap_, WJSettings.DEBUG);
			IJ.run("Fire");
			apCircularDensityMap_ = new ImagePlus(geneName_ + "_morphing_ap_conserved_density_map", samplingDensityAPEquator);
			manager.add(apCircularDensityMap_.getTitle(), apCircularDensityMap_, WJSettings.DEBUG);
			IJ.run("Fire");
		}
		
		int downSampling = (int)Math.round(1/settings.getExpression2DPreviewMeshGridDensity());
		if (WJSettings.DEBUG) {
			img = gridDVEquator.draw(image_.duplicate(), settings.getDefaultColor(), downSampling);
			img.setTitle(geneName_ + "_overlayed_grid_dv_conserved");
			manager.add(img.getTitle(), img, true);
			img = gridAPEquator.draw(image_.duplicate(), settings.getDefaultColor(), downSampling);
			img.setTitle(geneName_ + "_overlayed_grid_ap_conserved");
			manager.add(img.getTitle(), img, true);
		}

		img = new ImagePlus(geneName_ + "_morphing_dv_conserved", expressionDVEquator);
		manager.add(img.getTitle(), img, WJSettings.DEBUG);
		Grid shpereGridDVEquator = Grid.generateSphereLikeGrid(gridDVEquator.getGridLength(), Grid.EQUATOR_HORIZONTAL);
		img = shpereGridDVEquator.draw(img.duplicate(), settings.getDefaultColor(), downSampling);
		img.setTitle("expression_sampling_grid_for_dv_conserved");
		manager.add(img.getTitle(), img, WJSettings.DEBUG);

		img = new ImagePlus(geneName_ + "_morphing_ap_conserved", expressionAPEquator);
		manager.add(img.getTitle(), img, WJSettings.DEBUG);
		Grid shpereGridAPEquator = Grid.generateSphereLikeGrid(gridAPEquator.getGridLength(), Grid.EQUATOR_VERTICAL);
		img = shpereGridAPEquator.draw(img.duplicate(), settings.getDefaultColor(), downSampling);
		img.setTitle("expression_sampling_grid_for_ap_conserved");
		manager.add(img.getTitle(), img, WJSettings.DEBUG);

		float[] expressionDVEquatorPixels = (float[])expressionDVEquator.getPixels();
		float[] expressionAPEquatorPixels = (float[])expressionAPEquator.getPixels();

		float[] samplingDensityDVEquatorPixels = (float[])samplingDensityDVEquator.getPixels();
		float[] samplingDensityAPEquatorPixels = (float[])samplingDensityAPEquator.getPixels();

		// final expression dataset, which is a composition made from two morphed images (stitching)
		double[] expressionCompositePixels = new double[expressionDVEquatorPixels.length];

		byte[] maskDVEquatorPixels = new byte[samplingDensityDVEquatorPixels.length];
		byte[] maskAPEquatorPixels = new byte[samplingDensityAPEquatorPixels.length];

		double minSamplingDensityDiff = Double.MAX_VALUE;
		double maxSamplingDensityDiff = 0;

		for(int i=0; i<samplingDensityAPEquatorPixels.length; i++){
			double diff = samplingDensityAPEquatorPixels[i]-samplingDensityDVEquatorPixels[i];
			if(diff<minSamplingDensityDiff){
				minSamplingDensityDiff = diff;
			}
			if(diff>maxSamplingDensityDiff){
				maxSamplingDensityDiff = diff;
			}
		}

		double stitchingRange = -boundaryConserved_/100.0 * Math.max(Math.abs(minSamplingDensityDiff), Math.abs(maxSamplingDensityDiff));

		int xc = (expressionAPEquator.getWidth()-1)/2;
		int yc = (expressionAPEquator.getHeight()-1)/2;
		int rad = Math.min(xc, yc);
		for(int x=0; x<expressionAPEquator.getWidth(); x++){
			for(int y=0; y<expressionAPEquator.getHeight(); y++){
				int index = x+y*expressionAPEquator.getWidth();
				if(Point2D.distanceSq(xc, yc, x, y)>=rad*rad){
					maskDVEquatorPixels[index] = 1;
					maskAPEquatorPixels[index] = 1;
				}else{
					if(samplingDensityAPEquatorPixels[index]-samplingDensityDVEquatorPixels[index] < stitchingRange){
						maskDVEquatorPixels[index] = 1;
						maskAPEquatorPixels[index] = 0;
					}else{
						maskDVEquatorPixels[index] = 0;
						maskAPEquatorPixels[index] = 1;
					}
				}
			}
		}

		FloatProcessor maskDVEquatorEDM = (FloatProcessor) edm_.make16bitEDM(new ByteProcessor(expressionDVEquator.getWidth(), expressionDVEquator.getHeight(), maskDVEquatorPixels, null)).convertToFloat();
		FloatProcessor maskAPEquatorEDM = (FloatProcessor) edm_.make16bitEDM(new ByteProcessor(expressionAPEquator.getWidth(), expressionAPEquator.getHeight(), maskAPEquatorPixels, null)).convertToFloat();

		float[] maskDVEquatorEDMPixels = (float[])maskDVEquatorEDM.getPixels();
		float[] maskAPEquatorEDMPixels = (float[])maskAPEquatorEDM.getPixels();

		double maxEDMRange = 0;
		for (int i = 0; i < maskDVEquatorEDMPixels.length; i++) {
			double diff = Math.abs((double)maskDVEquatorEDMPixels[i] - (double)maskAPEquatorEDMPixels[i]);
			if (diff>maxEDMRange){
				maxEDMRange = diff;
			}
		}

		double stitchingSmoothingRange = maxEDMRange*Math.abs(settings.getExpression2DStitchingSmoothingRange())/100.0;
		float[] maskAPEquatorPreservedPixels = new float[samplingDensityAPEquatorPixels.length];

		float maxValAP = 0;
		for(int x=0; x<maskAPEquatorEDM.getWidth(); x++){
			for(int y=0; y<maskAPEquatorEDM.getHeight(); y++){
				int index = x+y*maskAPEquatorEDM.getWidth();
				if (maskAPEquatorEDMPixels[index] == 0){
					maskAPEquatorPreservedPixels[index] = -1;
				}else if(maskAPEquatorEDMPixels[index] <= 2*stitchingSmoothingRange) {
					if(Point2D.distanceSq(xc, yc, x, y)<rad*rad){
						maskAPEquatorPreservedPixels[index] = maskAPEquatorEDMPixels[index];
						if(maxValAP<maskAPEquatorPreservedPixels[index])
							maxValAP = maskAPEquatorPreservedPixels[index];
					}else{
						maskAPEquatorPreservedPixels[index] = 0;
					}
				}
			}
		}

		for(int i=0; i<maskAPEquatorPreservedPixels.length; i++){
			if(maskAPEquatorPreservedPixels[i]>0){
				if(maxValAP!=0){
					maskAPEquatorPreservedPixels[i] = (maxValAP-maskAPEquatorPreservedPixels[i])/maxValAP;
				}else{
					maskAPEquatorPreservedPixels[i] = 0;
				}
			}else if(maskAPEquatorPreservedPixels[i]==-1){
				maskAPEquatorPreservedPixels[i] = 1;
			}
		}

		if (WJSettings.DEBUG) {
			ImagePlus wPouchCircularMask0Image = new ImagePlus(geneName_ + "_stitching_mask_ap_conserved_" + Double.toString(boundaryConserved_), new FloatProcessor(maskDVEquatorEDM.getWidth(), maskDVEquatorEDM.getHeight(), maskAPEquatorPreservedPixels, null));
			manager.add(wPouchCircularMask0Image.getTitle(), wPouchCircularMask0Image, true);
		}

		for (int i = 0; i < expressionCompositePixels.length; i++){
			expressionCompositePixels[i] = (1-maskAPEquatorPreservedPixels[i]) * expressionDVEquatorPixels[i] + maskAPEquatorPreservedPixels[i] * expressionAPEquatorPixels[i];
		}

		// Prepare to return the 2D expression map
		FloatProcessor compositeFp = new FloatProcessor(maskDVEquatorEDM.getWidth(), maskDVEquatorEDM.getHeight(), expressionCompositePixels);
		ImagePlus composite = new ImagePlus(geneName_ + "_expression_map_" + Double.toString(boundaryConserved_), compositeFp);
		manager.add(composite.getTitle(), composite, false);

		// return always a 32-bit image
		return composite;
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public ExpressionDataset2D(ImagePlus image, Structure structure) throws Exception {

		image_ = new Duplicator().run(image);
		// convert and copy the structure reference
		structure_ = structure.copy();
//		if (structure instanceof WPouchStructure)
//			structure_ = ((WPouchStructure)structure).copy();
//		else
//			throw new Exception("ERROR: Unknown structure instanceof.");
		edm_ = new EDM();
	}

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
	protected void done() {

		try {
			get();

			if (filename_ == null || filename_.compareTo("") == 0) { // show datasets
				WJSettings.log("Showing expression dataset.");
				WJImages.showExpressionImage(expressionMap_);
				ImagePlusManager.getInstance().show(geneName_ + "_expression_map_" + Double.toString(boundaryConserved_));
			}
			else { // save dataset to file
				
				WJSettings.log("Exporting 2D expression map dataset.");
				String stitchingStr = Double.toString(boundaryConserved_);

//				try {
//					File file = new File(filename_ + EXPRESSION_PROJECTION_SUFFIX + ".tif");
//					WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), image_);
////					ImagePlus clone = new Duplicator().run(image_);
////					new ImageConverter(clone).convertToRGB();
//////					WJImagesMask.maskImageButCompartment(clone, structure_);
////					IJ.save(clone, file.toURI().getPath());
////					clone.close();
//					WJSettings.log("[x] Writing " + geneName_ + " original expression (tif)");
//				} catch (Exception e) {
//					WJSettings.log("[ ] Writing " + geneName_ + " original expression (tif)");
//					WJMessage.showMessage(e);
//				}
//
//				try {
//					File file = new File(filename_ + EXPRESSION_PROJECTION_SUFFIX + "_mask.tif");
//					WingJ wj = WingJ.getInstance();
//					WJImagesMask.saveBinaryMask(file.toURI(), wj.getStructureVisualization().getImage(), wj.getStructureVisualization().getStructure());
//					WJSettings.log("[x] Writing " + geneName_ + " original expression binary mask (tif)");
//				} catch (Exception e) {
//					WJSettings.log("[ ] Writing " + geneName_ + " original expression binary mask (tif)");
//					WJMessage.showMessage(e);
//				}

				try {
					File file = new File(filename_ + "_" + stitchingStr + ".tif");
					WJImages.saveExpressionImage(file.toURI().getPath(), expressionMap_);
//					if (settings.normalizeExpression())
//						WJImages.save32Bit(file.toURI().getPath(), expressionMap_);
//					else
//						WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), expressionMap_);
					
//					// if the expression is not normalized, save image in 255 bits
//					// to make it visible on Mac OS X, for instance.
//					ImageConverter converter = new ImageConverter(expressionMap_);
//					if (!settings.normalizeExpression()) converter.convertToGray8();
//					else converter.convertToGray32();
//					IJ.save(expressionMap_, file.toURI().getPath());
//					// remove image
//					ImagePlusManager manager = ImagePlusManager.getInstance();
//					manager.remove(expressionMap_.getTitle());
					WJSettings.log("[x] Writing " + geneName_ + " individual expression map [boundary conserved = " + stitchingStr + "] (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing " + geneName_ + " individual expression map [boundary conserved = " + stitchingStr + "] (tif)");
					WJMessage.showMessage(e);
				}

				try {
					File file = new File(filename_ + "_mask.tif");
					ImagePlus mask = WJImagesMask.createCircularBinaryMask(expressionMap_.getWidth()); // square image dimensions
					IJ.save(mask, file.toURI().getPath());
					WJSettings.log("[x] Writing " + geneName_ + " individual expression map binary mask (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing " + geneName_ + " individual expression map binary mask (tif)");
					WJMessage.showMessage(e);
				}
				
//				// EXTRA
//				// D/V and A/P circular density map
//				try {
//					File file = new File(filename_ + "_density_map_DV.tif");
//					ImageConverter converter = new ImageConverter(dvCircularDensityMap_);
//					converter.convertToGray32();
//					IJ.save(dvCircularDensityMap_, file.toURI().getPath());
//					// remove image
//					ImagePlusManager manager = ImagePlusManager.getInstance();
//					manager.remove(dvCircularDensityMap_.getTitle());
//					WJSettings.log("[x] Writing " + geneName_ + " 2D expression density map D/V (tif)");
//				} catch (Exception e) {
//					WJSettings.log("[ ] Writing " + geneName_ + " 2D expression density map D/V (tif)");
//					WJMessage.showMessage(e);
//				}
//				
//				try {
//					File file = new File(filename_ + "_density_map_AP.tif");
//					ImageConverter converter = new ImageConverter(apCircularDensityMap_);
//					converter.convertToGray32();
//					IJ.save(apCircularDensityMap_, file.toURI().getPath());
//					// remove image
//					ImagePlusManager manager = ImagePlusManager.getInstance();
//					manager.remove(apCircularDensityMap_.getTitle());
//					WJSettings.log("[x] Writing " + geneName_ + " 2D expression density map A/P (tif)");
//				} catch (Exception e) {
//					WJSettings.log("[ ] Writing " + geneName_ + " 2D expression density map A/P (tif)");
//					WJMessage.showMessage(e);
//				}
				
				// remove images
				disposeVisibleOutput();
			}
			WJSettings.log("Done");
			
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

	/** Closes old expression images open (if any). */
	public static void disposeVisibleOutput() throws Exception {

		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.removeAllContainingSubString("morphing");
		manager.removeAllContainingSubString("stitching");
		manager.removeAllContainingSubString("expression");
		manager.removeAllContainingSubString("overlay");
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public void setFilename(String filename) { filename_ = filename; }

	public void setGeneName(String name) { geneName_ = name; }
	public String getGeneName() { return geneName_; }

	public void setBoundaryConserved(double value) { boundaryConserved_ = value; }
	public double getBoundaryConserved() { return boundaryConserved_; }
}
