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

import ij.ImagePlus;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.expression.ExpressionDataset;
import ch.epfl.lis.wingj.expression.ExpressionDataset1D;
import ch.epfl.lis.wingj.expression.ExpressionMap;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureDataset;
import ch.epfl.lis.wingj.utilities.Projections;
import ch.tschaffter.utils.FileUtils;

/**
 * This class represent a single experiment to be used for batch processing.
 * <p>
 * This class is initialized with the absolute path to the "experiment folder"
 * which should includes the folder IMAGES_DIRECTORY, which in turn contains
 * the image stacks. If the folder IMAGES_DIRECTORY contains the "slice dataset"
 * file WJSettings.SLICE_DATASET_FILENAME, this allows the class to know which
 * image stacks should be loaded and what are other settings such that the minimum
 * and maximum slice indexes or the image projection method associated to each
 * channel. If this file doesn't exist, Gene objects must be added to this experiment.
 * <p>
 * The aim of this class is to includes all the information of an experiment and
 * at some point to not be dependent on shared instances (e.g. WJImages). This will
 * ultimately allow to run multiple experiments in parallel even if one should be
 * careful and correctly evaluate the amount of memory required for each experiment.
 * But the design is not complete yet and so only one experiment can be processed
 * at a time because, for instance, WJSettings is set will data specific to a
 * given experiment.
 * <p>
 * In a future version of WingJ, batch processing of experiments will get a
 * dedicated interface. For now, batch experiments should be run from custom code
 * (e.g. ExperimentBatch).
 * <p>
 * TODO: Decouple the opening of the slice dataset (i.e. creation of Gene objects)
 * and the opening of the image stacks.
 * 
 * @see Gene
 * @see ExperimentBatch
 * 
 * @version December 5, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class Experiment {
	
	/** Sub-directory contained in the experiment directory that contains the image stacks (with final separator). */
	public static String IMAGES_DIRECTORY = "images" + WJSettings.FS;	
	/** WingJ output directory (must have been created manually for safety reason) (with final separator). */
	public static String OUTPUT_DIRECTORY = "WingJBatch_middle10_sigma5" + WJSettings.FS;
	
	/** Relative location of the structure model file inside the experiment directory. */
	public static String STRUCTURE_MODEL_FILENAME = OUTPUT_DIRECTORY + "my_experiment4agg_structure.xml";
	/** Relative location of the slice dataset. */
	public static String SLICE_DATASET_FILENAME = Experiment.IMAGES_DIRECTORY + WJSettings.SLICE_DATASET_FILENAME;

	
	/** Reference to the BatchExperiment. */
	protected ExperimentBatch batch_ = null;
	
	/** Path to the experiment directory (includes final separator). */
	protected String directory_ = null;
	
	/**
	 * List of the genes/markers (in the order ch00, ch01, ...).
	 * If the channel i is not used, genes_.get(i) must be set to null.
	 */
	public List<Gene> genes_ = new ArrayList<Gene>();
	
	/** List of projections computed from the loaded image stacks (same order as genes). */
	protected List<ImagePlus> projections_ = new ArrayList<ImagePlus>();
	
	/** List of minimum slice indexes. */
	public List<Integer> minSlices_ = new ArrayList<Integer>();
	/** List of maximum slice indexes. */
	public List<Integer> maxSlices_ = new ArrayList<Integer>();
	/** List of projection methods (same order as genes) (ignored if slice dataset is loaded). */
	@SuppressWarnings("serial")
	protected List<Integer> projectionMethods_ = new ArrayList<Integer>() {{add(Projections.PROJECTION_MEAN_METHOD);
																			add(Projections.PROJECTION_MEAN_METHOD);
																			add(Projections.PROJECTION_MAX_METHOD);
																			add(Projections.PROJECTION_MEAN_METHOD);}};
	/** Index of the channel used for structure detection. */
	public int structureChannel_ = 2;
	/** Max projection of the structure channel. */
	protected ImagePlus structureMaxProjection_ = null;
																			
	/** Structure model. */
	protected Structure structure_ = null;
	
	/** Middles slices. For selecting the range of slices only. */
	protected Integer[] middleSliceIndexes_ = null;
	/** Half range. For selecting the range of slices only. */
	protected int halfSliceRange_ = 5;
		
	// ============================================================================
	// PUBLIC METHODS
																			
	/** Constructor. */
	public Experiment(String directory) {
		
		directory_ = directory;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Experiment(ExperimentBatch batch, String directory) {
		
		batch_ = batch;
		directory_ = directory;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Experiment(ExperimentBatch batch, String directory, List<Gene> genes) {
		
		batch_ = batch;
		directory_ = directory;
		genes_ = genes;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public Experiment(String directory, List<Gene> genes) {
		
		directory_ = directory;
		genes_ = genes;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Main method. */
	public static void main(String[] args) {
		
		try {	
			Experiment experiment = new Experiment("/home/tschaffter/20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_1/");
			experiment.addGene(new Gene("pmadAB", "ch00"));
			experiment.addGene(new Gene("pmadAB", "ch01"), 1, 16);
			experiment.addGene(new Gene("pmadAB", "ch02"));
			
			experiment.run();
			
			WJSettings.log("Experiment done");
		} catch (Exception e) {
			WJSettings.log("ERROR: Experiment failed.");
			e.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}

	// ----------------------------------------------------------------------------
	
	/** Creates the output directory and returns true if this folder exists at the end of the method call. */
	public boolean createOutputDirectory() throws Exception {
		
		String filename = directory_ + OUTPUT_DIRECTORY;
		try {
			FileUtils.mkdir(filename);
			WJSettings.log("[x] Creating output directory " + filename);
		} catch (Exception e) {
			File f = new File(directory_ + OUTPUT_DIRECTORY);
			if (!f.exists() || !f.isDirectory()) {
				WJSettings.log("[ERROR] Creating output directory " + filename);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes the variables and open the image stacks. */
	public void initialize() throws Exception {
		
		// create output directory if required
		if (!createOutputDirectory()) {
			WJSettings.log("Unable to create/access the output directory " + directory_ + OUTPUT_DIRECTORY);
			return;
		}
		
		// set output directory
		WJSettings settings = WJSettings.getInstance();
		settings.setOutputDirectory(directory_ + OUTPUT_DIRECTORY);
		settings.setExperimentName("my_experiment");
		
		openImageStacksAndComputeProjections();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Finalizes (clean). */
	@Override
	public void finalize() throws Exception {
		
		// closes all images registered in the manager
		ImagePlusManager.getInstance().removeAll();
		
		for (int i = 0; i < projections_.size(); i++) {
			if (projections_.get(i) != null) {
				projections_.get(i).close();
			}
		}
		if (structureMaxProjection_ != null)
			structureMaxProjection_.close();
		
		System.gc();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Run method. */
	public void run() throws Exception {
		
		WJSettings.log("Processing " + directory_);
		
		initialize();
		
//		Thread t = new Thread(new MiddleSliceSelector(this));
//        t.start();
		
        exportDatasets();
        finalize();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Exports datasets. */
	protected void exportDatasets() throws Exception {
		
//		exportProjectionDataset();
		openStructureModel();
		exportStructureDataset();
//		exportExpressionProfileDataset();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Signals batch_ to run the next experiment. */
	protected void runNextExperiment() throws Exception {
		
		if (batch_ != null)
			batch_.runNextExperiment();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens stack of images. */
	protected void openImageStacksAndComputeProjections() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// genes_.get(i) null to not use the channel i
		if (genes_ != null) {
			for (int i = 0; i < genes_.size(); i++) {	
				// VERSION 1: based on genes_
				// skip this gene ?
				if (genes_.get(i) == null) {
					projections_.set(i, null);
					continue;
				}
				
				// opens and registers the image stack
				ImagePlus stack = WJImages.openImageStack(genes_.get(i).getSelectionTag(), directory_ + IMAGES_DIRECTORY);
				if (stack != null) {
					WJImages.registerImageStack(i, stack);
					
					if (minSlices_.get(i) == null)
						minSlices_.set(i, WJImages.firstSlicesIndex_[i]);
					if (maxSlices_.get(i) == null)
						maxSlices_.set(i, WJImages.lastSlicesIndex_[i]);
							
					settings.setGeneNames(genes_.get(i).getName(), i);
					WJImages.firstSlicesIndex_[i] = minSlices_.get(i);
					WJImages.lastSlicesIndex_[i] = maxSlices_.get(i);
					WJImages.selectionTags_[i] = genes_.get(i).getSelectionTag();
					settings.setExpressionMinSliceIndex(i, minSlices_.get(i));
					settings.setExpressionMaxSliceIndex(i, maxSlices_.get(i));
					settings.setChannelProjectionMethod(i, projectionMethods_.get(i));
				} else
					WJSettings.log("ERROR: Unable to load images for " + genes_.get(i).getSelectionTag() + " in " + directory_ + IMAGES_DIRECTORY);
			}
		}
		else {
			// VERSION 2: opens images from the content of the slice dataset
			File f = new File(directory_ + IMAGES_DIRECTORY + WJSettings.SLICE_DATASET_FILENAME);
			WJImages.readSliceDatasetAndOpenImages(f.toURI());
			
			minSlices_.clear();
			maxSlices_.clear();
			genes_ = new ArrayList<Gene>();
			for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
				if (WJImages.firstSlicesIndex_[i] != 0 && WJImages.lastSlicesIndex_[i] != 0) {
					minSlices_.add(WJImages.firstSlicesIndex_[i]);
					maxSlices_.add(WJImages.lastSlicesIndex_[i]);
					projectionMethods_.set(i, settings.getChannelProjectionMethod(i));
					genes_.add(new Gene(settings.getGeneName(i), WJImages.selectionTags_[i]));
				}
			}
		}
		
//		// tweaks the min and max slice indexes
//		// first find the middle slice for each range and then +- 4 slices
//		for (int i = 0; i < genes_.size(); i++) {
//			if (genes_.get(i) == null)
//				continue;
//			
//			// custom rule
//			if (genes_.get(i).getName().compareTo("wg-ptcAB") == 0)
//				continue;
//			
////			int middleSlice = (int)Math.round((minSlices_.get(i)+maxSlices_.get(i))/2.);
//			int middleSlice = middleSliceIndexes_[i];
//			
//			int a = middleSlice - halfSliceRange_;
//			int b = middleSlice + halfSliceRange_;
//			if (a < 1)
//				a = 1;
//			if (b > settings.getExpressionMaxSliceIndex(i))
//				b = settings.getExpressionMaxSliceIndex(i);
//			if (b-a != 2*halfSliceRange_)
//				WJSettings.log("Only " + ((b-a)+1) + " slices for " + genes_.get(i).getName() + " in " + directory_);
//			if (b-a <= 1)
//				throw new Exception("ERROR: " + ((b-a)+1) + " slices for " + genes_.get(i).getName() + " in " + directory_);
//			
//			// redundant but set minSlices_, maxSlices_, settings.setExpressionMinSliceIndex(channel, index)
//			// and settings.setExpressionMaxSliceIndex(channel, index)
//			// WJImages.firstSlicesIndex_[i] and WJImages.lastSlicesIndex_[i] should not been
//			// used outside than for the loading of image stacks, which is beside this.
//			minSlices_.set(i, a);
//			maxSlices_.set(i, b);
//			settings.setExpressionMinSliceIndex(i, a);
//			settings.setExpressionMaxSliceIndex(i, b);
//		}
		
		
		// compute projections
		ImagePlusManager manager = ImagePlusManager.getInstance();
		ImagePlus ip = null;
		for (int i = 0; i < genes_.size(); i++) {
			WJSettings.log("Computing projection: " + minSlices_.get(i) + "-" + maxSlices_.get(i) + " (mode: " + projectionMethods_.get(i) + ")");
			ip = Projections.doProjection(WJImages.getImageStack(i), projectionMethods_.get(i), minSlices_.get(i), maxSlices_.get(i));
			if (ip == null)
				throw new Exception("ERROR: Failed to compute image projection " + i);
			projections_.add(ip);
//			manager.add("channel_" + i + "_projection", ip); // need to be commented for batch experiments
			
			if (i == structureChannel_) {
				structureMaxProjection_ = Projections.doProjection(WJImages.getImageStack(i), Projections.PROJECTION_MAX_METHOD, minSlices_.get(i), maxSlices_.get(i));
				manager.add("structure_projection", structureMaxProjection_);
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the min and max slices from a middle slice. */
	public int[] getMinAndMaxFromMiddleSlice(int channel, int middleSlice) throws Exception {
		
		Integer maxmax = maxSlices_.get(channel);
		
		int a = middleSlice - halfSliceRange_;
		int b = middleSlice + halfSliceRange_;
		if (a < 1)
			a = 1;
		if (b > maxmax)
			b = maxmax;
		if (b-a != 2*halfSliceRange_)
			WJSettings.log("Only " + ((b-a)+1) + " slices for " + genes_.get(channel).getName() + " in " + directory_);
		if (b-a <= 1)
			throw new Exception("ERROR: " + ((b-a)+1) + " slices for " + genes_.get(channel).getName() + " in " + directory_);
		
		int[] minAndMax = new int[2];
		minAndMax[0] = a;
		minAndMax[1] = b;
		
		return minAndMax;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Exports slice information from information local to this experiment. */
	protected void exportSliceInformation() throws Exception {
		
		exportSliceInformation(null);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Exports slice information from information local to this experiment. */
	protected void exportSliceInformation(Integer[] middleSliceIndexes) throws Exception {
			
		try {
//			File f = new File(directory_ + IMAGES_DIRECTORY + WJSettings.SLICE_DATASET_FILENAME);
			File f = new File(directory_ + IMAGES_DIRECTORY + "WingJ_slices_middle_TMP.txt");
			FileWriter fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);    
			
			String content = "";
			for (int i = 0; i < genes_.size(); i++) {
				if (genes_.get(i) != null) {
					
					if (middleSliceIndexes != null && middleSliceIndexes[i] != null && i < 2) { // last hack to only consider two first channels
						int[] minAndMax = getMinAndMaxFromMiddleSlice(i, middleSliceIndexes[i]);
						minSlices_.set(i, minAndMax[0]);
						maxSlices_.set(i, minAndMax[1]);
					}
					
					content += genes_.get(i).getName() + "\t";
					content += genes_.get(i).getSelectionTag() + "\t";
					content += minSlices_.get(i) + "\t";
					content += maxSlices_.get(i) + "\t";
					content += projectionMethods_.get(i);
					content += "\n";
				}
			}
			out.write(content);
			out.close();
			WJSettings.log("[x] Writing  slice dataset");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing slice dataset");
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Saves the projections to files.
	 * Requires the settings to have been set correctly and the image stacks to be in the manager.
	 * IMPORTANT: Uses settings..getExpressionMaxSliceIndex(i)-like methods.
	 */
	protected void exportProjectionDataset() throws Exception {
		
		WingJ.exportProjections(directory_ + OUTPUT_DIRECTORY);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens the structure. */
	protected void openStructureModel() throws Exception {
		
		String filename = directory_ + STRUCTURE_MODEL_FILENAME;
		structure_ = WingJ.getInstance().getSystem().newStructure();
		structure_.read(new URI("file://" + filename));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Exports the structure dataset to the output folder. */
	protected void exportStructureDataset() throws Exception {
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure is null.");
		if (structureMaxProjection_ == null)
			throw new Exception("ERROR: Structure projection is null.");
		
		// create a structure viewer that will be exported by the dataset
		WJStructureViewer viewer = new WJStructureViewer(structure_, structureMaxProjection_.duplicate());
		StructureDataset dataset = structure_.newStructureDataset();
		dataset.setStructureVisualization(viewer);
		
		dataset.run(); // instead of execute()
		dataset.get();
		
		viewer.setVisible(false);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Quantifies expression along trajectories and export the dataset. */
	protected void exportExpressionProfileDataset() throws Exception {
		
		if (structure_ == null)
			throw new Exception("ERROR: Structure is null.");
		
		WJSettings settings = WJSettings.getInstance();
		settings.setExpression1DSaveMeasurementDomain(true);
		settings.setExpression1DSavePdf(true);
		
		ExpressionDataset1D dataset = null;
		for (int i = 0; i < genes_.size(); i++) {
			if (projections_.get(i) == null || projections_.get(i).getProcessor() == null)
				continue;
			
			projections_.get(i).duplicate();
			
			dataset = new ExpressionDataset1D();
			dataset.setExpressionImage(projections_.get(i).duplicate());
			dataset.setStructure(structure_.copy());
			dataset.setGeneName(genes_.get(i).getName());
			
			dataset.setReferenceBoundary(settings.getExpression1DBoundary());
			dataset.setTrajectoryOffset(settings.getExpression1DTranslation());
			dataset.setSigma(5/*settings.getExpression1DSigma()*/);
			settings.setExpression1DSigma(5);
			dataset.setFilename(ExpressionDataset.getExpressionDataset1dDefaultFilename(i));
			
			dataset.generateDataset(); // instead of execute() or run() because anyway done() is called asynchronously
			dataset.export();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the list of Structure objects from the given list of experiments. */
	public static List<Structure> getStructures(List<Experiment> experiments) throws Exception {
		
		// gets all the structures
		List<Structure> structures = new ArrayList<Structure>();
		Experiment e = null;
		for (int i = 0; i < experiments.size(); i++) {
			try {
				e = experiments.get(i);
				e.openStructureModel();
				structures.add(e.getStructure().copy());
			} catch (Exception exc) {
				WJSettings.log("ERROR: Unable to get structure from experiment " + e.getDirectory());
				exc.printStackTrace();
			}
		}
		return structures;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the projections associated to the given gene from the given list of experiments. */
	public static List<ExpressionMap> getProjections(List<Experiment> experiments, String geneName) throws Exception {
		
		// gets all the expression projections
		List<ExpressionMap> projections = new ArrayList<ExpressionMap>();
		Experiment e = null;
		for (int i = 0; i < experiments.size(); i++) {
			try {
				e = experiments.get(i);
				e.openImageStacksAndComputeProjections();
				
				// looks for the given geneName
				List<Gene> genes = e.getGenes();
				for (int g = 0; g < genes.size(); g++) {
					if (genes.get(g) == null)
						continue;
					if (genes.get(g).getName().compareTo(geneName) == 0) {
						ImagePlus imp = e.getProjection(g);
						if (imp != null && imp.getProcessor() != null)
							projections.add(new ExpressionMap(imp.getTitle(), imp.duplicate().getProcessor()));
					}
				}
			} catch (Exception exc) {
				WJSettings.log("ERROR: Unable to get projection from experiment " + e.getDirectory());
				exc.printStackTrace();
			}
		}
		return projections;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the gene names from the given experiment name. */
	public static List<String> getGeneNamesFromExperimentName(String str) throws Exception {
		
		StringTokenizer tokenizer = new StringTokenizer(str, "_");
		
		// The gene names starts from the second token if there are no mutant
		// or from the first token after the last mutant.
		// The last gene name is before the age.
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			tokens.add(tokenizer.nextToken());
		
		// remove experiment date
		tokens.remove(0);
		
		// remove mutants (end with "-")
		boolean stop = false;
		while (!stop) {
			if (tokens.get(0).endsWith("-"))
				tokens.remove(0);
			else
				stop = true;
		}
		
		// start from the end and removes tokens up to age
		stop = false;
		while (!stop) {
			if (tokens.get(tokens.size()-1).endsWith("H"))
				stop = true;
			
			tokens.remove(tokens.size()-1);
		}
		
		return tokens;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the names of the mutant from the given experiment name. */
	public static List<String> getMutantNamesFromExperimentName(String str) throws Exception {
		
		StringTokenizer tokenizer = new StringTokenizer(str, "_");
		
		// The gene names starts from the second token if there are no mutant
		// or from the first token after the last mutant.
		// The last gene name is before the age.
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			tokens.add(tokenizer.nextToken());
		
		// remove experiment date
		tokens.remove(0);
		
		// remove mutants (end with "-")
		List<String> mutantNames = new ArrayList<String>();
		boolean stop = false;
		while (!stop) {
			if (tokens.get(0).endsWith("-")) {
				mutantNames.add(tokens.get(0));
				tokens.remove(0);
			} else
				stop = true;
		}
		
		// strips the mutant names from their terminal "-"
		for (int i = 0; i < mutantNames.size(); i++)
			mutantNames.set(i, mutantNames.get(i).substring(0,mutantNames.get(i).length()-1));
		
		return mutantNames;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the age string from the given experiment name. */
	public static String getAgeStringFromExperimentName(String str) throws Exception {
		
		StringTokenizer tokenizer = new StringTokenizer(str, "_");
		
		// The gene names starts from the second token if there are no mutant
		// or from the first token after the last mutant.
		// The last gene name is before the age.
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
			tokens.add(tokenizer.nextToken());
		
		// start from the end and removes tokens up to age
		String ageStr = null;
		boolean stop = false;
		while (!stop) {
			if (tokens.get(tokens.size()-1).endsWith("H")) // case sensitive
				stop = true;
			
			ageStr = tokens.get(tokens.size()-1);
			tokens.remove(tokens.size()-1);
		}
		return ageStr;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Rounds the given string age to the nearest multiple of 10. */
	public static String roundAgeStringToNearest10(String ageStr) throws Exception {
		
		if (!ageStr.endsWith("H"))
			throw new Exception("ERROR: " + ageStr + " is not a valid age string (doesn't end with H).");
			
		// remove 'H'
		ageStr = ageStr.substring(0, ageStr.length()-1);
		// looks for number separated by '-'
		StringTokenizer tokenizer = new StringTokenizer(ageStr, "-");
		double age = 0.;
		int n = 0;
		while (tokenizer.hasMoreTokens()) {
			age += Double.parseDouble(tokenizer.nextToken().replace(",", "."));
			n++;
		}
		age /= n; // average
		// round to nearest 10
		int ageInt = (int)(10*(Math.round(age/10.)));
		ageStr = ageInt + "H";
		
		return ageStr;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the list of gene names common to the given experiments. */
	public static List<String> getCommonGeneNames(List<Experiment> experiments) throws Exception {
		
		Set<String> uniqueGeneNames = new HashSet<String>(); // prevents duplicates
		List<String> allGeneNames = new ArrayList<String>();
		List<Gene> genes = null;
		for (Experiment e : experiments) {
			genes = e.getGenes();
			for (Gene g : genes) {
				if (g != null) {
					uniqueGeneNames.add(g.getName());
					allGeneNames.add(g.getName());
				}
			}
		}
		
		List<String> commonGeneNames = new ArrayList<String>();
		for (String name : uniqueGeneNames) {
			// looks at the frequency of the gene name
			int numOccurrences = Collections.frequency(allGeneNames, name);
			// only keeps the following ones
			if (numOccurrences == experiments.size())
				commonGeneNames.add(name);
		}
		
		return commonGeneNames;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the name of the experiment from the given experiment directory. */
	public static String getExperimentNameFromExperimentDirectory(String directory) throws Exception {
		
		StringTokenizer tokenizer = new StringTokenizer(directory, WJSettings.FS);
		String experimentName = null;
		while (tokenizer.hasMoreTokens())
			experimentName = tokenizer.nextToken();
		return experimentName;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getDirectory() { return directory_; }
	
	public void addGene(Gene gene) { genes_.add(gene); minSlices_.add(null); maxSlices_.add(null); }
	public void addGene(Gene gene, Integer minSlice, Integer maxSlice) { genes_.add(gene); minSlices_.add(minSlice); maxSlices_.add(maxSlice); }
	
	public List<Gene> getGenes() { return genes_; }
	public List<ImagePlus> getProjections() { return projections_; }
	
	public Structure getStructure() { return structure_; }
	
	public String getOutputDirectory() { return new String(directory_ + OUTPUT_DIRECTORY); }
	
	public ImagePlus getProjection(int index) { return projections_.get(index); }
	
	/**
	 * Interface for entering the index of the middle slice selected for each channel.
	 * <p>
	 * This interface has been developed to easily define the middle slice to consider
	 * for each channel. First, the method run() of the Experiment object is called
	 * and the image stack of each channel is displayed. Then this thread is called
	 * and a GUI is displayed where the user can specify the middle index to select
	 * for each stack after having browse them. By clicking on the button Ok, the range
	 * middle slice +- Experiment.halfSliceRange_ is defined and the slice dataset
	 * is saved to Experiment.SLICE_DATASET_FILENAME before calling the running the next
	 * experiment included in the batch of experiments.
	 */
	@SuppressWarnings("unused")
	private static class MiddleSliceSelector implements Runnable, ActionListener {
		
		/** Reference to Experiment object. */
		protected Experiment experiment_ = null;
		 
		/** Dialog. */
		protected JDialog dialog_ = null;
		/** Fields that will contains the index of the middle slices as entered by the user. */
		protected JTextField[] middleSliceIndexesTFields_ = null;
		/** Ok button. */
		protected JButton okButton_ = null;
		
		// ============================================================================
		// PUBLIC METHODS
		
		/** Constructor. */
		public MiddleSliceSelector(Experiment experiment) {
			experiment_ = experiment;
		}
		
		// ----------------------------------------------------------------------------

		/** Run method. */
		@Override
		public void run() {

			dialog_ = new JDialog(new JFrame(), false); // Sets its owner but makes it non-modal
			okButton_ = new JButton("Ok");
    	
			// set layout
	    	dialog_.getContentPane().setLayout(new GridBagLayout());    	
	    	GridBagConstraints c = new GridBagConstraints();
	    	c.fill = GridBagConstraints.NONE;
	    	middleSliceIndexesTFields_ = new JTextField[WJSettings.NUM_CHANNELS];
	    	for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
	    		c.gridx = 0;
	    		c.gridy = i;
	    		dialog_.getContentPane().add(new JLabel("Ch" + i + ": "), c);
	    		middleSliceIndexesTFields_[i] = new JTextField(5);
	    		c.gridx = 1;
	    		dialog_.getContentPane().add(middleSliceIndexesTFields_[i], c);
	    	}
	    	c.fill = GridBagConstraints.HORIZONTAL;
	    	c.gridwidth = 2;
	    	c.gridx = 0;
	    	c.gridy = WJSettings.NUM_CHANNELS;
	    	dialog_.getContentPane().add(okButton_, c);
	    	
	    	okButton_.addActionListener(this);
	    	
	    	dialog_.pack();
	    	
	    	// centers the dialog on the screen
	    	final Toolkit toolkit = Toolkit.getDefaultToolkit();
	    	final Dimension screenSize = toolkit.getScreenSize();
	    	final int x = (screenSize.width - dialog_.getWidth()) / 2;
	    	final int y = (screenSize.height - dialog_.getHeight()) / 2;
	    	dialog_.setLocation(x, y);
	    	
	    	dialog_.setVisible(true);
		}
		
		// ----------------------------------------------------------------------------

		@Override
		public void actionPerformed(ActionEvent e) {
	
			Object source = e.getSource();
			if (source == okButton_) {			
				try {
					// gets the values entered by the user
					Integer[] middleSliceIndexes = new Integer[middleSliceIndexesTFields_.length];
					for (int i = 0; i < middleSliceIndexesTFields_.length; i++) {
						String text = middleSliceIndexesTFields_[i].getText();
						if (text.compareTo("") != 0)
							middleSliceIndexes[i] = Integer.parseInt(middleSliceIndexesTFields_[i].getText());
						else
							middleSliceIndexes[i] = null;
					}
					
					// exports the slice information
					experiment_.exportSliceInformation(middleSliceIndexes);
					experiment_.runNextExperiment();
					dialog_.setVisible(false);
					
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
