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

import java.io.File;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Duplicator;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.utilities.StringUtils;

/**
 * Generates community 2D expression datasets.
 * <p>
 * This class implements a thread worker to allow computing community expression
 * datasets in parallel. Therefore one must take care during the implementation
 * that 1) specific parameters must be set directly in the object and not taken
 * from WJSettings, 2) classes like Structure, Boundary, etc. must not be modified
 * by thread and 3) if ImagePlus or dialog (visualization, plot, etc.) are generated
 * for the sake of one thread, the others threads must not close them.
 * <p>
 * UPDATE: Now supports the use of different structures for generating the target
 * aggregated structure.
 * 
 * @version November 22, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionDataset2DAggregated extends SwingWorker<Void, Void> {
	
	/** Default filename suffix for exporting raw projections. */
	public static final String EXPRESSION_PROJECTION_SUFFIX = "_raw.tif";

	/** List of Structure objects from which average Structure is computed. */
	protected List<Structure> structures_ = null;
	/** List of Structure objects EXCLUSIVE to the generation of the target structure. */
	protected List<Structure> structuresForTargetStructure_ = null;
	/** List of image projections to quantify.  */
	protected List<ExpressionMap> projections_ = null;
	
	/** Mean aggregated structure. */
	protected Structure meanAggregatedStructure_ = null;
	/** Mean+std aggregated structure. */
	protected Structure meanPlusStdAggregatedStructure_ = null;
	/** Mean-std aggregated structure. */
	protected Structure meanMinusStdAggregatedStructure_ = null;

	/** Aggregated circular expression map (mean). */
	protected CircularExpressionMap aggregatedCircularExpressionMap_ = null;
	/** Aggregated std circular expression map. */
	protected CircularExpressionMap aggregatedStdCircularExpressionMap_ = null;
	
	/** Aggregated expression map (mean). */
	protected ExpressionMap aggregatedExpressionMap_ = null;
	/** Aggregated std expression map. */
	protected ExpressionMap aggregatedStdExpressionMap_ = null;
	
	/** Structure viewer for showing the structure on top of the expression map (required to be static for closing it later). */
	public static WJStructureViewer structureViewer_ = null;
	/** Structure viewer for show the mean+std aggregated structure. */
	public static WJStructureViewer meanPlusStdStructureViewer_ = null;
	/** Structure viewer for show the mean-std aggregated structure. */
	public static WJStructureViewer meanMinusStdStructureViewer_ = null;

	/**
	 * If null or empty, shows the community expression dataset generated.
	 * Otherwise, saves the community expression dataset to files.
	 */
	protected String filename_ = null;

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public ExpressionDataset2DAggregated(List<Structure> structures, List<ExpressionMap> projections) throws Exception {

		structures_ = structures;
		projections_ = projections;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public ExpressionDataset2DAggregated(List<Structure> structures, List<Structure> structuresForTargetStructure, List<ExpressionMap> projections) throws Exception {

		structures_ = structures;
		structuresForTargetStructure_ = structuresForTargetStructure;
		projections_ = projections;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generates the community expression dataset. */
	public void generateDataset() throws Exception {
		
		if (structures_ == null)
			throw new Exception("ERROR: List of structures is null.");
		if (projections_ == null)
			throw new Exception("ERROR: List of projections is null.");
		
		// get image dimensions
		if (projections_.get(0) == null)
			throw new Exception("ERROR: First projection is null.");
//		Dimension dim = new Dimension(projections_.get(0).getWidth(), projections_.get(0).getHeight());
		
		WJSettings.log("Starting computing aggregated expression map.");
		WJSettings.log("A message will be displayed at the end of the process.");
		ExpressionMapsAggregator aggregator = new ExpressionMapsAggregator(structures_, structuresForTargetStructure_, projections_);
		aggregator.run();
		
		// copies output of aggregator
		ImagePlusManager manager = ImagePlusManager.getInstance();
		meanAggregatedStructure_ = aggregator.getTargetStructure().copy();
		ImagePlus ip = null;
		try {
			ip = new Duplicator().run(aggregator.getTargetCircularExpressionMap());
			ip.setTitle("mean_circular_expression_map");
			aggregatedCircularExpressionMap_ = new CircularExpressionMap(ip.getTitle(), ip.getProcessor());
			manager.add(aggregatedCircularExpressionMap_.getTitle(), aggregatedCircularExpressionMap_);
		} catch (Exception e) {}
		
		try { // throws an exception is STD map not computed
			ip = new Duplicator().run(aggregator.getTargetStdCircularExpressionMap());
			ip.setTitle("std_circular_expression_map");
			aggregatedStdCircularExpressionMap_ = new CircularExpressionMap(ip.getTitle(), ip.getProcessor());
			manager.add(aggregatedStdCircularExpressionMap_.getTitle(), aggregatedStdCircularExpressionMap_);
		} catch (Exception e) {}
		
		try {
			ip = new Duplicator().run(aggregator.getTargetExpressionMap());
			ip.setTitle("mean_expression_map");
			aggregatedExpressionMap_ = new ExpressionMap(ip.getTitle(), ip.getProcessor());
			manager.add(aggregatedExpressionMap_.getTitle(), aggregatedExpressionMap_);
		} catch (Exception e) {}
		
		try { // throws an exception is STD map not computed
			ip = new Duplicator().run(aggregator.getTargetStdExpressionMap());
			ip.setTitle("std_expression_map");
			aggregatedStdExpressionMap_ = new ExpressionMap(ip.getTitle(), ip.getProcessor());
			manager.add(aggregatedStdExpressionMap_.getTitle(), aggregatedStdExpressionMap_);
		} catch (Exception e) {}
		
		
		// in addition, get the mean+std and mean-std aggregated structures
		meanPlusStdAggregatedStructure_ = aggregator.getMeanPlusStdAggregatedStructure().copy();
		meanMinusStdAggregatedStructure_ = aggregator.getMeanMinusStdAggregatedStructure().copy();
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
    
    /** Exports dataset. */
    public void export() throws Exception {
    	
    	ImagePlusManager manager = ImagePlusManager.getInstance();
    	
		// closes previous viewers (if any)
		if (structureViewer_ != null)
			structureViewer_.setVisible(false);
//		if (meanPlusStdStructureViewer_ != null)
//			meanPlusStdStructureViewer_.setVisible(false);
//		if (meanMinusStdStructureViewer_ != null)
//			meanMinusStdStructureViewer_.setVisible(false);
		
		if (filename_ == null || filename_.compareTo("") == 0) { // show datasets
			WJSettings.log("Showing expression dataset.");
			if (aggregatedStdCircularExpressionMap_ != null) {
				WJImages.showExpressionImage(aggregatedStdCircularExpressionMap_);
//				IJ.run("Fire");
			}
			WJImages.showExpressionImage(aggregatedCircularExpressionMap_);
			if (aggregatedStdExpressionMap_ != null) {
				WJImages.showExpressionImage(aggregatedStdExpressionMap_);
//				IJ.run("Fire");
			}
			// target expression map with structure displayed
			structureViewer_ = new WJStructureViewer(meanAggregatedStructure_, aggregatedExpressionMap_);
			structureViewer_.setVisible(true);
			// target expression map
			WJImages.showExpressionImage(aggregatedExpressionMap_);
		}
		else { // save dataset to file
			
			WJSettings.log("Exporting expression dataset.");

			// save aggregated structure model
			try {
				File file = new File(filename_ + ".xml");
				meanAggregatedStructure_.write(file.toURI());
				WJSettings.log("[x] Writing mean structure model (xml)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean structure model (xml)");
				WJMessage.showMessage(e);
			}
			
			// save mean+std aggregated structure model
			try {
				File file = new File(filename_ + "_meanPlusStd.xml");
				meanPlusStdAggregatedStructure_.write(file.toURI());
				WJSettings.log("[x] Writing mean+std structure model (xml)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean+std structure model (xml)");
				WJMessage.showMessage(e);
			}
			
//			// save mean-std aggregated structure model
//			try {
//				File file = new File(filename_ + "_meanMinusStd.xml");
//				meanMinusStdAggregatedStructure_.write(file.toURI());
//				WJSettings.log("[x] Writing " + geneName_ + " aggregated structure model -STD (xml)");
//			} catch (Exception e) {
//				WJSettings.log("[ ] Writing " + geneName_ + " aggregated structure model -STD (xml)");
//				WJMessage.showMessage(e);
//			}
			
			// aggregated circular expression dataset
			try {
				File file = new File(filename_ + "_circular.tif");//EXPRESSION_PROJECTION_SUFFIX);
				WJImages.saveExpressionImage(file.toURI().getPath(), aggregatedCircularExpressionMap_);
//				if (settings.normalizeExpression())
//					WJImages.save32Bit(file.toURI().getPath(), aggregatedCircularExpressionMap_);
//				else
//					WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), aggregatedCircularExpressionMap_);
				
//				ImagePlus clone = new Duplicator().run(aggregatedCircularExpressionMap_);
////				new ImageConverter(clone).convertToRGB();
//				ImageConverter converter = new ImageConverter(clone);
//				if (!settings.normalizeExpression()) converter.convertToGray8();
//				else converter.convertToGray32();
//				IJ.save(clone, file.toURI().getPath());
//				clone.close();
				WJSettings.log("[x] Writing mean circular expression map (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean circular expression map (tif)");
				WJMessage.showMessage(e);
			}
			
			// aggregated std circular expression dataset
			try {
				if (aggregatedStdCircularExpressionMap_ != null) {
					File file = new File(filename_ + "_circular_std.tif");//EXPRESSION_PROJECTION_SUFFIX);
					WJImages.saveExpressionImage(file.toURI().getPath(), aggregatedStdCircularExpressionMap_);
//					if (settings.normalizeExpression())
//						WJImages.save32Bit(file.toURI().getPath(), aggregatedStdCircularExpressionMap_);
//					else
//						WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), aggregatedStdCircularExpressionMap_);
					WJSettings.log("[x] Writing std circular expression map (tif)");
				}
			} catch (Exception e) {
				WJSettings.log("[ ] Writing std circular expression map (tif)");
				WJMessage.showMessage(e);
			}
			
			// circular mask
			try {
				File file = new File(filename_ + "_circular_mask.tif");
				ImagePlus mask = WJImagesMask.createCircularBinaryMask(aggregatedCircularExpressionMap_.getWidth()); // square image dimensions
				IJ.save(mask, file.toURI().getPath());
				WJSettings.log("[x] Writing mean circular expression map binary mask (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean circular expression map binary mask (tif)");
				WJMessage.showMessage(e);
			}
			
			// aggregated expression dataset
			try {
				File file = new File(filename_ + ".tif");//EXPRESSION_PROJECTION_SUFFIX);
				WJImages.saveExpressionImage(file.toURI().getPath(), aggregatedExpressionMap_);
//				if (settings.normalizeExpression())
//					WJImages.save32Bit(file.toURI().getPath(), aggregatedExpressionMap_);
//				else
//					WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), aggregatedExpressionMap_);
				
//				ImagePlus clone = new Duplicator().run(aggregatedExpressionMap_);
////				new ImageConverter(clone).convertToRGB();
//				ImageConverter converter = new ImageConverter(clone);
//				if (!settings.normalizeExpression()) converter.convertToGray8();
//				else converter.convertToGray32();
//				IJ.save(clone, file.toURI().getPath());
//				clone.close();
				WJSettings.log("[x] Writing mean expression map (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean expression map (tif)");
				WJMessage.showMessage(e);
			}
			
			// std aggregated expression dataset
			try {
				if (aggregatedStdExpressionMap_ != null) {
					File file = new File(filename_ + "_std.tif");//EXPRESSION_PROJECTION_SUFFIX);
					WJImages.saveExpressionImage(file.toURI().getPath(), aggregatedStdExpressionMap_);
//					WindowManager.setTempCurrentImage(aggregatedStdExpressionMap_);
//					IJ.run("Fire");
//					if (settings.normalizeExpression())
//						WJImages.save32Bit(file.toURI().getPath(), aggregatedStdExpressionMap_);
//					else
//						WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), aggregatedStdExpressionMap_);
					
//					ImagePlus clone = new Duplicator().run(aggregatedExpressionMap_);
////					new ImageConverter(clone).convertToRGB();
//					ImageConverter converter = new ImageConverter(clone);
//					if (!settings.normalizeExpression()) converter.convertToGray8();
//					else converter.convertToGray32();
//					IJ.save(clone, file.toURI().getPath());
//					clone.close();
					WJSettings.log("[x] Writing std expression map (tif)");
				}
			} catch (Exception e) {
				WJSettings.log("[ ] Writing std expression map (tif)");
				WJMessage.showMessage(e);
			}
			
			// aggregated expression dataset + structure on top of it
			try {
				File file = new File(filename_ + "_plus.tif");//EXPRESSION_PROJECTION_SUFFIX);
				// creates structure viewer
				structureViewer_ = new WJStructureViewer(meanAggregatedStructure_, aggregatedExpressionMap_, true); // use ImageWindow (not displayed)
				structureViewer_.save(file.toURI());
				WJSettings.log("[x] Writing mean expression map with structure (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean expression map with structure (tif)");
				WJMessage.showMessage(e);
			}
			
			// aggregated std expression dataset + structure on top of it
			try {
				if (aggregatedStdExpressionMap_ != null) {
					File file = new File(filename_ + "_std_plus.tif");//EXPRESSION_PROJECTION_SUFFIX);
					// creates structure viewer
					structureViewer_ = new WJStructureViewer(meanAggregatedStructure_, aggregatedStdExpressionMap_, true); // use ImageWindow (not displayed)
					structureViewer_.save(file.toURI());
					WJSettings.log("[x] Writing std expression map with structure (tif)");
				}
			} catch (Exception e) {
				WJSettings.log("[ ] Writing std expression map with structure (tif)");
				WJMessage.showMessage(e);
			}

			// binary mask corresponding to the aggregated structure
			try {
				File file = new File(filename_ + "_mask.tif");
				WJImagesMask.saveBinaryMask(file.toURI(), aggregatedExpressionMap_, meanAggregatedStructure_);
				WJSettings.log("[x] Writing mean expression map binary mask (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean expression map binary mask (tif)");
				WJMessage.showMessage(e);
			}
			
			// exporting the grid preview of the aggregated structure
			try {
				File file = new File(filename_ + "_sampling_grid_preview.tif");
//				WJImages.saveRgbImage(file.toURI().getPath(), manager.getImage("expression_sampling_grid_preview"));
				
				IJ.save(manager.getImage("expression_sampling_grid_preview"), file.toURI().getPath());
//				if (settings.normalizeExpression())
//					WJImages.save32Bit(file.toURI().getPath(), manager.getImage("expression_sampling_grid_preview"));
//				else
//					WJImages.save32BitToRGB(file.toURI().getPath(), manager.getImage("expression_sampling_grid_preview"));
				
//				ImagePlus clone = new Duplicator().run(manager.getImage("expression_grid_preview"));
//				ImageConverter converter = new ImageConverter(clone);
//				if (!settings.normalizeExpression()) converter.convertToRGB();
//				else converter.convertToGray32();
//				IJ.save(clone, file.toURI().getPath());
//				clone.close();
				WJSettings.log("[x] Writing mean expression map sampling grid preview (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean expression map sampling grid preview (tif)");
				WJMessage.showMessage(e);
			}
			
			// mean+std aggregated structure preview
			try {
				File file = new File(filename_ + "_meanPlusStd.tif");//EXPRESSION_PROJECTION_SUFFIX);
				// creates structure viewer
				ImagePlus ip = new Duplicator().run(aggregatedExpressionMap_);
				ip.setTitle(ip.getTitle() + "_meanPlusStd");
				meanPlusStdStructureViewer_ = new WJStructureViewer(meanPlusStdAggregatedStructure_, ip, true); // use ImageWindow (not displayed)
				meanPlusStdStructureViewer_.save(file.toURI());
				WJSettings.log("[x] Writing mean expression map with mean+std structure (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean expression map with mean+std structure (tif)");
				WJMessage.showMessage(e);
			}

			// mean+std aggregated structure preview mask
			try {
				File file = new File(filename_ + "_meanPlusStd_mask.tif");
				WJImagesMask.saveBinaryMask(file.toURI(), aggregatedExpressionMap_, meanPlusStdAggregatedStructure_);
				WJSettings.log("[x] Writing mean+std structure binary mask (tif)");
			} catch (Exception e) {
				WJSettings.log("[ ] Writing mean+std structure binary mask (tif)");
				WJMessage.showMessage(e);
			}
			
			// remove images
			disposeVisibleOutput();
		}
		WJSettings.log("Done");
    }

	// ----------------------------------------------------------------------------

	/** Closes old expression images open (if any). */
	public static void disposeVisibleOutput() throws Exception {

		if (structureViewer_ != null) {
			structureViewer_.setVisible(false);
			structureViewer_ = null;
		}
		if (meanPlusStdStructureViewer_ != null) {
			meanPlusStdStructureViewer_.setVisible(false);
			meanPlusStdStructureViewer_ = null;
		}
		if (meanMinusStdStructureViewer_ != null) {
			meanMinusStdStructureViewer_.setVisible(false); // FIXME bug: don't always hide (from remote desktop only?)
			meanMinusStdStructureViewer_ = null;
		}
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.removeAllContainingSubString("aggregate"); // also include aggregated ;)
		manager.removeAllContainingSubString("expression");
		manager.removeAllContainingSubString("structure");
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public void setFilename(String filename) { filename_ = filename; }
}
