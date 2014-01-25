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

package ch.epfl.lis.wingj.batch;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.expression.ExpressionDataset2DAggregated;
import ch.epfl.lis.wingj.expression.ExpressionMap;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.utilities.ImageUtils;

/**
 * Describes a batch of experiments.
 * <p>
 * Examples are provided for processing many experiments part of a batch. An experiment should
 * be seen as the quantification of a single biological system (or organ system or body system).
 * <p>
 * Batch processing of experiments will be made possible through a graphical user interface
 * in a future release of WingJ. The code available in this package has been implemented as a
 * first step to achieve this and to already enable batch processing inside the frame of this
 * sandbox. Note that the code below has not been optimized and has only been used so to run
 * very specific experiments. Yet the methods have been designed to be modular and thus
 * easily reusable.
 * <p>
 * Please read carefully the comments as they can be of great help for adapting the code
 * code for different applications.
 * 
 * @see Experiment
 * 
 * @version December 16, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ExperimentBatch {
	
	/** Static reference to this batch. */
	public static ExperimentBatch batch_ = null;
	
	/** List of Experiment objects. */
	protected List<Experiment> experiments_ = null;
	
	/** Index of the next experiments to run. */
	protected int nextExperimentIndex_ = 0;
	
	/** List of "experiments root directory". */
	protected List<String> rootDirectories_ = null;
	
	// ============================================================================
	// These two variables are used to generate asynchronous aggregated models, i.e.
	// where the number of experiments used for generating the target aggregated model
	// may be different from the number of experiments from which expression is quantified.
	
	/** List of experiments to use for generating the target aggregated structure model. */
	protected List<Experiment> experimentsForAggStructureModel_ = null;
	/** List of experiments to use for quantifying expression. */
	protected List<Experiment> experimentsForExpressionQuantification_ = null;
	/** Gene names to consider in this experiment. */
	protected List<String> aggGeneNames_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public ExperimentBatch() {
		
		experiments_ = new ArrayList<Experiment>();
		rootDirectories_ = new ArrayList<String>();
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_78-79H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_99-100H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/");
		
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_79-80H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_89-90H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_100-101H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H/");
		
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_79-80H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_87-88H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_100-101H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_111-112H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H/");
		rootDirectories_.add("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H/");	
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public ExperimentBatch(List<Experiment> experimentsForAggStructureModel, List<Experiment> experimentsForExpressionQuantification) {
		
		experimentsForAggStructureModel_ = experimentsForAggStructureModel;
		experimentsForExpressionQuantification_ = experimentsForExpressionQuantification;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Main method. */
	public static void main(String[] args) {
		
		try {
//			new ImageJ(); // not required to display images but tools can be handy
			
			batch_ = new ExperimentBatch();
			
			// ============================================================================
			// Uses the experiments defined in the methods included at the end
			// of this class
			
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt80hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt90hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt100hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt110hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent80hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent90hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent100hPmadBrkWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent110hPmadBrkWgPtc());
//			
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt80hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt90hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt100hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt110hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent80hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent90hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent100hSalDadWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent110hSalDadWgPtc());
//			
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt80hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt90hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt100hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getWt110hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent80hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent90hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent100hBrkOmbWgPtc());
//			batch_.experiments_.addAll(ExperimentDefinitions.getPent110hBrkOmbWgPtc());
			
			
			// ============================================================================
			// Provides experiments by only giving the "experiments root directory". The folder
			// contained in this root directory are considered as experiment if:
			// 1. The slice dataset Experiment.SLICE_DATASET_FILENAME exists
			// 2. The structure model Experiment.STRUCTURE_MODEL_FILENAME exists
			// A folder inside the root directory is ignored if its name ends
			// with "IGNORE".
			
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_78-79H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_99-100H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/"));
//			
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_79-80H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_89-90H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_100-101H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H/"));
//			
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_79-80H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_87-88H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_100-101H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_111-112H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H/"));
//			batch_.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H/"));
//
//			// calls this method to regenerate slice datasets, for instance
////			batch_.runNextExperiment();
//			
//			for (int i = 0; i < batch_.experiments_.size(); i++) {
//				try {
//					batch_.getExperiment().get(i).run();
//					WJSettings.log("[x] Experiment complete (" + batch_.getExperiment().get(i).getDirectory() + ")");
//				} catch (Exception exception) {
//					WJSettings.log("[ ] Experiment " + batch_.getExperiment().get(i).getDirectory() + " failed: " + exception.getMessage());
//					exception.printStackTrace();
//				}
//			}
			
			// ============================================================================
			// Aggregates structure models from many experiments
			
//			aggregateStructureModelsDemo();
			
			// ============================================================================
			// Generates a representative model from similar experiments. First, an aggregated
			// structure model is computed before aggregating expression information. Finally,
			// both structure and expression models are integrated to generate a consistent
			// model.
			// For a better visualization of the expression information, all the models
			// generated are browsed to find the minimum and maximum pixel values from
			// expression information. Then, the expression information of every aggregated
			// model is scaled so that the minimum pixel value found is black and the maximum
			
			// pixel value found is white (i.e. the contrast is increased).
//			for (int i = 0; i < batch_.rootDirectories_.size(); i++)
//				aggregateExperiments(batch_.rootDirectories_.get(i));
			
			// ============================================================================
			// Uniformizes the pixel values of every image to use the entire display range
			// [0,255]. This method can also be used to set the canvas of each image to the
			// same dimensions, that is, the dimensions of the larger image. Then, every
			// image can be resized to the same dimensions.
			
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_mask.tif", false, true, null, ".png");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_mask.tif", false, true, new Dimension(350, 0), ".gif");
			
			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_meanPlusStd_mask.tif", false, true, null, ".png");
			
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_circular.tif", true, false, new Dimension(1001, 0), ".png");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*_sampling_grid_preview.tif", false, true, null, ".png");
			
			
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H.tif", true, true, new Dimension(700, 0), ".jpg");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H.tif", true, true, null, ".jpg");
			
			// ============================================================================
			// Aggregates models but where the number of experiments used to generated the
			// aggregated model is not the same as the experiments used to generated
			// the aggregated expression maps.
			
//			aggregateStructureExpressionModelsAsyncDemo("/mnt/extra/wingviewer_images/");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_circular.tif", true, false, new Dimension(1001, 0), ".png");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_circular_std.tif", false, false, new Dimension(1001, 0), ".png");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H.tif", true, true, null, "_HOT.png");
//			uniformizeAggregatedExpressionMaps("/mnt/extra/wingviewer_images/", "*H_std.tif", false, true, null, ".png");
			
			
			WJSettings.log("ExperimentBatch done");
		} catch (Exception e) {
			WJSettings.log("ERROR: ExperimentBatch failed.");
			e.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Runs the next experiment. */
	public void runNextExperiment() throws Exception {
		
		if (nextExperimentIndex_ < experiments_.size()) {
			experiments_.get(nextExperimentIndex_).run();
			WJSettings.log("[x] Experiment complete (" + experiments_.get(nextExperimentIndex_).getDirectory() + ")");
			nextExperimentIndex_++;
		} else
			System.exit(0);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Adds experiments to the batch. */
	public void addAll(String rootDirectory) throws Exception {
		
		if (experiments_ != null)
			experiments_.addAll(getExperiments(rootDirectory));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Adds experiments to the batch. */
	public void addAll(List<Experiment> experiments) throws Exception {
		
		if (experiments_ != null)
			experiments_.addAll(experiments);
	}
	
	// ----------------------------------------------------------------------------
	
	public List<Experiment> getExperiment() { return experiments_; }
	
	// ----------------------------------------------------------------------------
	
	/** Returns a list of experiments found in the given directory and considered as valid. */
	public static List<Experiment> getExperiments(String rootDirectory) throws Exception {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		
		File f = new File(rootDirectory);
		if (!f.exists() || !f.isDirectory())
			return experiments;
		
		// lists folders associated to valid experiments
		String[] children = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir + WJSettings.FS + name);
				if (!f.isDirectory())
					return false;
				if (name.endsWith("IGNORE"))
					return false;
				
				// look for the output directory (must have been created before hand)
				String filename = dir + WJSettings.FS + name + WJSettings.FS + Experiment.OUTPUT_DIRECTORY;
				File f2 = new File(filename);
//				if (!f2.exists() || !f2.isDirectory()) {
//					WJSettings.log("Output directory doesn't exist: " + filename);
//					return false;
//				}
				
				// look for the structure model file
				filename = dir + WJSettings.FS + name + WJSettings.FS + Experiment.STRUCTURE_MODEL_FILENAME;
				f2 = new File(filename);
				if (!f2.exists() || !f2.isFile()) {
					WJSettings.log("Structure model doesn't exist: " + filename);
					return false;
				}
				
				// look for the slice dataset
				filename = dir + WJSettings.FS + name + WJSettings.FS + Experiment.SLICE_DATASET_FILENAME;
				f2 = new File(filename);
				if (!f2.exists() || !f2.isFile()) {
					WJSettings.log("Slice dataset doesn't exist: " + filename);
					return false;
				}
				
				// if we are here this means that all the requirements are satisfied
				return true;
		    }
		});
		
		String experimentDirectory = null;
		for (int i = 0; i < children.length; i++) {
			experimentDirectory = rootDirectory + children[i] + WJSettings.FS;
			WJSettings.log("Adding experiment " + experimentDirectory);
			experiments.add(new Experiment(batch_, experimentDirectory, null));
		}
		
		return experiments;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Aggregates the given experiments and saves the files to the root directory (with final separator). */
	public static void aggregateExperiments(String rootDirectory) throws Exception {
		
		// gets valid experiments
		List<Experiment> experiments = getExperiments(rootDirectory);
		
		// settings
		int numPoints = 2001;
		int equator = WJSettings.BOUNDARY_DV;
		boolean std = false;
		WJSettings settings = WJSettings.getInstance();
		settings.setExpression2DNumPoints(numPoints);
		settings.setExpression2DAggEquator(equator);
		settings.setExpression2DAggStd(std);
		settings.setDefaultColor(Color.WHITE);
		settings.setDefaultStrokeWidth(1f);
		
		
		// gets all the structures
		List<Structure> structures = new ArrayList<Structure>();
		Experiment e = null;
		for (int i = 0; i < experiments.size(); i++) {
			e = experiments.get(i);
			e.openStructureModel();
			structures.add(e.getStructure().copy());
		}
		
		// gets the channels to aggregate from the first experiment
		e = experiments.get(0);
		e.initialize();
		List<Gene> genes = e.getGenes();
		e.finalize();

		List<ExpressionMap> projections = new ArrayList<ExpressionMap>();
		for (int i = 0; i < genes.size(); i++) {
			if (genes.get(i) == null)
				continue; // skip this gene/channel
			
			String geneName = genes.get(i).getName();
			// loads the projection that is in the output directory
			projections.clear();
			for (int j = 0; j < experiments.size(); j++) {
				String filename = experiments.get(j).getOutputDirectory() + geneName + "_projection.tif";
				ImagePlus ip = IJ.openImage(filename);
				projections.add(new ExpressionMap(geneName + "_projection", ip.duplicate().getProcessor()));
			}
			
			// here we have everything we need to start aggregating
			ExpressionDataset2DAggregated dataset = new ExpressionDataset2DAggregated(structures, projections);
			dataset.setFilename(rootDirectory + geneName + "_agg_expression_map");
			
			dataset.generateDataset();
			dataset.export();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Method to uniformize the pixel values and size of images. */
	public static void uniformizeAggregatedExpressionMaps(String directory, String regex, boolean scalePixelValues, boolean resizeCanvas, Dimension outputImageSize, String outputFilenameSuffix) throws Exception {
		
		// disable IJ scaling of the pixel values
		boolean scalingBkp = ImageConverter.getDoScaling();
		ImageConverter.setDoScaling(false);
		
		// gets filenames
		List<String> filenames = ch.epfl.lis.wingj.utilities.FilenameUtils.selectFilenames(directory, regex);
		
		// XXX: ignores "wg-ptcAB" images because max
		// intensity projection was used for them
		List<String> tmpList = new ArrayList<String>();
		for (String filename : filenames) {
			if (!filename.contains("wg-ptcAB"))
				tmpList.add(filename);
//			if (filename.contains("pmadAB"))
//				tmpList.add(filename);
		}
		filenames.clear();
		filenames.addAll(tmpList);

		// gets images
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		ImagePlus imp = null;
		for (String filename : filenames) {
			imp = IJ.openImage(filename);
			new ImageConverter(imp).convertToGray32();
			images.add(imp);
		}
		
		// applies the same display range to every image
		if (scalePixelValues) {
			ImageUtils.setRelativeDisplayRange(images);
			ImageConverter.setDoScaling(true);
			for (int i = 0; i < images.size(); i++) {
				new ImageConverter(images.get(i)).convertToGray8();
			}
		}
		// resizes the canvas so that all images have the same
		if (resizeCanvas)
			ImageUtils.setRelativeCanvasDimensions(images); // new Dimension(800, 0)
		
		// resizes the images if required
		if (outputImageSize != null) {
			Dimension newDims = null;
			for (int i = 0; i < images.size(); i++) {
				imp = images.get(i);
				newDims = ImageUtils.getNewDimensionsWithAspectRatioConserved(new Dimension(imp.getWidth(), imp.getHeight()), outputImageSize);
				images.set(i, new ImagePlus(imp.getTitle(), imp.getProcessor().resize(newDims.width, newDims.height)));
			}
		}
		
		// save the images
		String filename = null;
		for (int i = 0; i < filenames.size(); i++) {
			filename = FilenameUtils.removeExtension(filenames.get(i)) + outputFilenameSuffix;
			IJ.save(images.get(i), filename);
		}

		// restore scaling settings
		ImageConverter.setDoScaling(scalingBkp);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Aggregates the structure models from the given experiments and save it to file. */
	public static void aggregateStructuresAndExport(List<Experiment> experiments, String aggStructureFilename) throws Exception {
		
		// the given experiments are expected to be valid, i.e. containing a structure model
		// gets all the structures
		List<Structure> structures = Experiment.getStructures(experiments);
		
		// ready to aggregate the structures
		// uses the first structure model as template
		Structure meanStructure = structures.get(0).aggregate("mean-super-structure", structures, Structure.AGGREGATION_MEAN);
		Structure meanPlusStdStructure = structures.get(0).aggregate("meanPlusStd-super-structure", structures, Structure.AGGREGATION_MEAN_PLUS_STD);
		Structure meanMinusStdStructure = structures.get(0).aggregate("meanMinusStd-super-structure", structures, Structure.AGGREGATION_MEAN_MINUS_STD);
		
		// aligns them (rotation)
		// uses the biggest structure as reference (mean+std)
		double[] transforms = meanPlusStdStructure.setCanonicalOrientation();
		meanStructure.rotate(transforms[0]);
		meanMinusStdStructure.rotate(transforms[0]);

		// translation so that the biggest structure touches the top and left border
		// of the image space
		double[] dxdy = meanPlusStdStructure.moveToTopLeftCorner();
		meanStructure.translate(dxdy[0], dxdy[1]);
		meanMinusStdStructure.translate(dxdy[0], dxdy[1]);
		
		meanStructure.write(new URI("file://" + aggStructureFilename + "_mean.xml"));
		meanPlusStdStructure.write(new URI("file://" + aggStructureFilename + "_meanPlusStd.xml"));
		meanMinusStdStructure.write(new URI("file://" + aggStructureFilename + "_meanMinusStd.xml"));
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Exports aggregated expression maps and names them so that they can be used by the wing viewer
	 * available on the project website (lis.epfl.ch/wingj).
	 * Options:
	 * <ul>
	 * <li>Pent2-5 mutant experiments contain the string "pent2-5" in their filename</li>
	 * <li>The first token of the filename of an aggregated map is the gene name (separated by '_')</li>
	 * <li>The age token ends with 'H' and the extracted age is somehow rounded to multiple of 10 hours (see below)</li>
	 * <li>Ages are rounded to the nearest 10</li>
	 * </ul>
	 */
	public void exportAggExpressionMapsForWingViewer(String directory, List<String> inputFilenames, List<ImagePlus> expressionMaps) throws Exception {
		
		if (inputFilenames.size() != expressionMaps.size())
			throw new Exception("ERROR: The number of filenames and images must be the same.");
		
		String filename = null;
		for (int i = 0; i < inputFilenames.size(); i++) {
			// sets genotype
			if (inputFilenames.get(i).contains("pent2-5"))
				filename = "pent";
			else
				filename = "wt";
			
			// sets gene name
			// gets the filename without directory, extract the first token with '_'
			// as separator
			String name = ch.epfl.lis.wingj.utilities.FilenameUtils.getFilenameWithoutPath(inputFilenames.get(i));
			StringTokenizer tokenizer = new StringTokenizer(name, "_");
			filename += "_" + tokenizer.nextToken();
			
			// the age token ends with 'H' and the extracted age is somehow rounded
			// to multiple of 10 hours (see below)
			// there should be no other tokens ending with 'H' is the filename convention
			// defined in the WingJ user manual is respected
			// start by getting the experiment directory
			tokenizer = new StringTokenizer(inputFilenames.get(i), WJSettings.FS);
			// get last-1 token from "experiment root directory"
			String experimentName = "";		
			while (tokenizer.hasMoreTokens()) {
				if (tokenizer.countTokens() == 2)
					experimentName = tokenizer.nextToken();
				else
					tokenizer.nextToken();
			}
			tokenizer = new StringTokenizer(experimentName, "_");
			String ageStr = null;
			boolean problemo = true;
			while (tokenizer.hasMoreTokens()) {
				ageStr = tokenizer.nextToken();
				if (ageStr.endsWith("H")) {
					problemo = false;
					break;
				}
			}
			if (problemo)
				throw new Exception("ERROR: Age not found among " + experimentName);
			// removes 'H'
			ageStr = ageStr.substring(0, ageStr.length()-1);
			// looks for number separated by '-'
			tokenizer = new StringTokenizer(ageStr, "-");
			double age = 0.;
			int n = 0;
			while (tokenizer.hasMoreTokens()) {
				age += Double.parseDouble(tokenizer.nextToken().replace(",", "."));
				n++;
			}
			age /= n; // average
			// round to nearest 10
			int ageInt = (int)(10*(Math.round(age/10.)));
			ageStr = ageInt + "h";
			filename += "_" + ageInt;
			
			// adds extension
			filename += ".jpg";
			
			// adds directory 
//			filename = ch.epfl.lis.wingj.utilities.FilenameUtils.getDirectory(inputFilenames.get(i)) + WJSettings.FS + filename;
			filename = directory + filename;
			
			IJ.save(expressionMaps.get(i), filename);
		}
	}
	
	// ============================================================================
	// DEMOS
	
	/**
	 * Aggregates the structure models of many experiments and export them (mean, mean+std
	 * and mean-std structures) to files.
	 */
	public static void aggregateStructureModelsDemo() throws Exception {
		
		List<Experiment> experiments = new ArrayList<Experiment>();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_78-79H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_79-80H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_79-80H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_wt_80H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_89-90H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_87-88H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_wt_90H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_99-100H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_100-101H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_100-101H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_wt_100H");

		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101129_salAB_dadGFP_wg-ptcAB_110,5-111,5H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110302_brkAB_ombAB_wg-ptcAB_111-112H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_wt_110H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_79-80H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_79-80H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_pent_80H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_90-91H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_87-88H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_pent_90H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_99-100H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_99-101H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_100-101H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_pent_100H");
		
		experiments.clear();
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20101127_pent2-5-_salAB_dadGFP_wg-ptcAB_114-115H/"));
		experiments.addAll(getExperiments("/mnt/extra/wingj_benchmarks/20110306_pent2-5-_brkAB_ombAB_wg-ptcAB_109-110.5H/"));
		aggregateStructuresAndExport(experiments, "/mnt/extra/wingviewer_images/structures/agg_structure_pent_110H");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Aggregates models but where the number of experiments used to generated the
	 * aggregated model is not the same as the experiments used to generated
	 * the aggregated expression maps. The output directory must includes the final
	 * separator ('/' or '\').
	 * <p>
	 * TODO: Optimize for memory.
	 */
	public static void aggregateStructureExpressionModelsAsyncDemo(String outputDirectory) throws Exception {
		
		// Settings
		int numPoints = 2001;
		int equator = WJSettings.BOUNDARY_DV;
		boolean std = true;
		WJSettings settings = WJSettings.getInstance();
		settings.setExpression2DNumPoints(numPoints);
		settings.setExpression2DAggEquator(equator);
		settings.setExpression2DAggStd(std);
		settings.setDefaultColor(Color.WHITE);
		settings.setDefaultStrokeWidth(1f);

		
		List<ExperimentBatch> batches = ExperimentDefinitions.getAggExperimentBatches();
		Set<String> genesDone = new HashSet<String>();
		
		// for each batch
		for (ExperimentBatch batch : batches) {
			// gets structures for target aggregated structure models
			List<Structure> structuresForAggStructureModel = Experiment.getStructures(batch.getExperimentsForAggStructureModel());
			// initializes experiments before accessing the genes
			for (Experiment e : batch.getExperimentsForExpressionQuantification())
				e.initialize();
			// finds all the genes that the given experiments have in common
			List<String> geneNames = Experiment.getCommonGeneNames(batch.getExperimentsForExpressionQuantification());
			WJSettings.log("Common genes: " + geneNames);
			// closes image stack (we have the projections now)
			// projections are not registered in the manager
			ImagePlusManager.getInstance().removeAll();
			
			if (geneNames.size() == 0)
				throw new Exception("ERROR: The given experiments don't have any gene in common.");
			for (int g = 0; g < geneNames.size(); g++) {
				
//				if (geneNames.get(g).compareTo("salAB") != 0)
//					continue;
				
				// gets structures and projections for expression quantification
				List<Structure> structuresForExpression = Experiment.getStructures(batch.getExperimentsForExpressionQuantification());
				List<ExpressionMap> projections = Experiment.getProjections(batch.getExperimentsForExpressionQuantification(), geneNames.get(g));
				WJSettings.log("Num. of structures for structure model: " + structuresForAggStructureModel.size());
				WJSettings.log("Num. of structures for expression: " + structuresForExpression.size());
				WJSettings.log("Num. of projections for " + geneNames.get(g) + ": " + projections.size());
				
				if (structuresForExpression.size() != projections.size())
					throw new Exception("ERROR: The number of structures and projections found are not the same for " + geneNames.get(g) + ".");
				
				// before generating the dataset, we must figure out if the data are related to wild type
				// or mutant experiments. The age should also be found. These two informations are extracted
				// from the experiment name of the first experiment.
				String firstExperimentDirectory = batch.getExperimentsForExpressionQuantification().get(0).getDirectory();
				String experimentName = Experiment.getExperimentNameFromExperimentDirectory(firstExperimentDirectory);
				List<String> mutantNames = Experiment.getMutantNamesFromExperimentName(experimentName); // we expect a single mutant
				if (mutantNames.isEmpty())
					mutantNames.add("wt");
				String ageStr = Experiment.roundAgeStringToNearest10(Experiment.getAgeStringFromExperimentName(experimentName));
				
				// builds generic filename
				String filename = outputDirectory + mutantNames.get(0) + "_" + geneNames.get(g) + "_" + ageStr;
				
				// generates dataset
				// XXX: processes only if the given gene as not yet been processed from another experiment
				if (!genesDone.contains(geneNames.get(g))) {
					ExpressionDataset2DAggregated dataset = new ExpressionDataset2DAggregated(structuresForExpression, structuresForAggStructureModel, projections);
					dataset.generateDataset();
					
					// exports dataset to files
					dataset.setFilename(filename);
					dataset.export();
					
					genesDone.add(geneNames.get(g));
				}
			}
			
			// cleans
			ImagePlusManager.getInstance().removeAll();
			for (Experiment exp : batch.getExperimentsForAggStructureModel())
				exp.finalize();
			for (Experiment exp : batch.getExperimentsForExpressionQuantification())
				exp.finalize();
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setExperimentsForAggStructureModel(List<Experiment> experiments) { experimentsForAggStructureModel_ = experiments; }
	public List<Experiment> getExperimentsForAggStructureModel() { return experimentsForAggStructureModel_; }
	
	public void setExperimentsForExpressionQuantification(List<Experiment> experiments) { experimentsForExpressionQuantification_ = experiments; }
	public List<Experiment> getExperimentsForExpressionQuantification() { return experimentsForExpressionQuantification_; }
}
