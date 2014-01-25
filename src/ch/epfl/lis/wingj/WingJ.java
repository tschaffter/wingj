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

package ch.epfl.lis.wingj;

import ij.IJ;
import ij.ImagePlus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureDetector;
import ch.epfl.lis.wingj.structure.DetectorWorker;
import ch.epfl.lis.wingj.structure.StructureDataset;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.analytics.ExpressionStats;
import ch.epfl.lis.wingj.expression.CircularExpressionMap;
import ch.epfl.lis.wingj.expression.ExpressionDataset2DAggregated;
import ch.epfl.lis.wingj.expression.ExpressionDataset;
import ch.epfl.lis.wingj.expression.ExpressionDataset1D;
import ch.epfl.lis.wingj.expression.ExpressionDataset2D;
import ch.epfl.lis.wingj.expression.ExpressionDataset2DReversed;
import ch.epfl.lis.wingj.expression.ExpressionDomain;
import ch.epfl.lis.wingj.expression.ExpressionMap;
import ch.epfl.lis.wingj.expression.ExpressionPlot;
import ch.epfl.lis.wingj.gui.WJExperimentsSelectionUpdater;
import ch.epfl.lis.wingj.gui.WJAboutBox;
import ch.epfl.lis.wingj.gui.WJGuiPlus;
import ch.epfl.lis.wingj.utilities.FilenameUtils;
import ch.epfl.lis.wingj.utilities.Projections;

/**
 * Main class of WingJ.
 * <p>
 * Please refer to the website of WingJ for more detailed information about the methods
 * implemented in WingJ.
 * <p>
 * Project web page: lis.epfl.ch/wingj
 * 
 * @version October 21, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WingJ extends WJGuiPlus implements ActionListener {
	
	/** Default serial. */
	private static final long serialVersionUID = 1L;
	
	/** The unique instance of WingJ (Singleton design pattern). */
	private static WingJ instance_ = null;
	
	/** Selected system (include structure detector). */
	private WJSystem system_ = null;
	
//	/** Structure detector. */
//	private StructureDetector detector_ = null;
	
	/** Structure visualization. */
	private WJStructureViewer structureVisualization_ = null;
	
//	/** TODO: Batch experiments. */
//	private BatchExperiments batchExperiments_ = null;
	
	/** Number of expression datasets being generated. */
	private int numActiveExpressionDatasetProcesses_ = 0;
	
	// ============================================================================
	// PRIVATE METHODS

	/** Default constructor (private for Singleton pattern). */
	private WingJ() {
		
		super();
		
		WJSettings settings = WJSettings.getInstance();
		String version = settings.getIjRequiredVersion();
		if (IJ.versionLessThan(version)) { // IJ already shows a dialog
			//WJMessage.showMessage("WingJ requires ImageJ " + version + " or later.");
			System.exit(0);
		}
		
		// sets the active system
		system_ = WJSystemManager.getInstance().getSystem(getSelectedSystemId());
		
		// DO NOT REMOVE THIS LINE
		// ANALYTICS CODE: START
		Analytics.getInstance().start();
		// END
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Resets WingJ (close windows, reset parameters, etc.) to be ready for a new experiment. */
	@Override
	protected void reset() throws Exception {
		
   		ImagePlusManager.getInstance().removeAll();
   		ExpressionPlot.disposeAll();
   		ExpressionDomain.disposeAll();
   		WJImages.clean();
   		system_.deleteStructureDetector();
   		structureVisualization_ = null;
   		WJImages.allowImagesAutoLoading_ = true;
   		structureBgImgBkp_ = -1;
   		
   		// refresh the gui
   		settings2gui();
	}
	
	//----------------------------------------------------------------------------
	
	/** Creates a new structure detector. */
	protected boolean createStructureDetector() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// asks for confirmation
    	if (system_ != null && system_.getStructureDetector() != null) {	        		
    		if (system_.getStructureDetector().isComplete()) {
    			if (!runNewStructureDetection())
    				return false;
    		}
    	}
		
		// instantiates structure detector for the selected system
		int selectedSystemId = getSelectedSystemId();
		system_ = WJSystemManager.getInstance().getSystem(selectedSystemId);
		system_.newStructureDetector(settings.getExperimentName());
		
		return true;
	}
	
	//----------------------------------------------------------------------------
	
	/** Updates the name of the current structure. */
	@Override
	protected void setStructureName() {

		StructureDetector detector = system_.getStructureDetector();
		if (detector != null && detector.getStructure() != null)
			detector.getStructure().setName(nameTField_.getText());
	}
	
	//----------------------------------------------------------------------------
	
	/** Shows or saves gene expression dataset. */
	protected void computeExpressionDataset(boolean save) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
    	int datasetType = getSelectedExpressionDatasetDimension();
		
    	if (system_.getStructureDetector() == null)
    		system_.newStructureDetector(settings.getExperimentName());	
    	StructureDetector detector = system_.getStructureDetector();
//    	WJSettings.log("Generating dataset for " + system_.getName() + ".");
		
    	if (datasetType == WJSettings.EXPRESSION_DATASET_1D || datasetType == WJSettings.EXPRESSION_DATASET_2D) {
			String msg = "INFO: Structure model required.";
	    	if (detector == null || detector.getStructure() == null || detector.getStructure().getStructureSnake() == null)
	    		throw new Exception(msg);
		}
    	
		if (datasetType == WJSettings.EXPRESSION_DATASET_1D) {
			int channel = getSelectedExpressionChannelIndex();
			if (save) ExpressionDataset.saveExpressionDataset1D(channel, computeProjectionForExpressionQuantification(), detector.getStructure());
			else ExpressionDataset.showExpressionDataset1D(channel, computeProjectionForExpressionQuantification(), detector.getStructure());
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().addExpressionDataset(ExpressionStats.EXPRESSION_PROFILE, 1);
			// END
		}
		else if (datasetType == WJSettings.EXPRESSION_DATASET_2D) {
			int channel = getSelectedExpressionChannelIndex();
			if (save) ExpressionDataset.saveExpressionDataset2D(channel, computeProjectionForExpressionQuantification(), detector.getStructure());
			else ExpressionDataset.showExpressionDataset2D(channel, computeProjectionForExpressionQuantification(), detector.getStructure());
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().addExpressionDataset(ExpressionStats.EXPRESSION_MAP, 1);
			// END
		}
		else if (datasetType == WJSettings.EXPRESSION_DATASET_2D_REVERSE) {
			computeReversedExpressionDataset(save);
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().addExpressionDataset(ExpressionStats.EXPRESSION_MAP_REVERSED, 1);
			// END
		}
		else if (datasetType == WJSettings.EXPRESSION_DATASET_2D_AGGREGATED) {
			computeAggregatedExpressionDataset(save);
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().addExpressionDataset(ExpressionStats.MEAN_MODEL, 1);
			// END
		}
		else if (datasetType == WJSettings.EXPRESSION_DATASET_COMPOSITE) {
        	computeComposite(save);
        	// DO NOT REMOVE THIS LINE
        	// ANALYTICS CODE: START
        	Analytics.getInstance().addExpressionDataset(ExpressionStats.COMPOSITE, 1);
        	// END
		}
		else
			throw new Exception("ERROR: Invalid dimension for expression dataset.");
	}
	
	//----------------------------------------------------------------------------
	
//	/** Exports gene expression dataset. */
//	protected void saveExpressionDataset() throws Exception {
//		
//    	if (detector_ == null)
//    		throw new Exception("INFO: Structure detection first required.");
//    	if (detector_.getStructure() == null)
//    		throw new Exception("INFO: Structure detection first required.");
//    	if (detector_.getStructure().getStructureSnake() == null)
//    		throw new Exception("INFO: Structure detection first required.");
//    	
//    	int channel = expressionSelectedChannelCBox_.getSelectedIndex();
//		int dimension = expressionDimensionDatasetCBox_.getSelectedIndex();
//		
//		if (dimension == WJSettings.EXPRESSION_DATASET_1D)
//			ExpressionDataset.saveExpressionDataset1D(channel, computeProjectionForExpressionQuantification(), detector_.getStructure());
//		else if (dimension == WJSettings.EXPRESSION_DATASET_2D)
//			ExpressionDataset.saveExpressionDataset2D(channel, computeProjectionForExpressionQuantification(), detector_.getStructure());
//		else
//			throw new Exception("ERROR: Invalid dimension expression dataset.");
//	}
	
	//----------------------------------------------------------------------------
	
	/** Shows or saves the reversed expression dataset. */
	protected void computeReversedExpressionDataset(boolean save) throws Exception {
		
		// prepares structure
		StructureDetector detector = system_.getStructureDetector();
		Structure targetStructure = null;
		if (expression2DReverseCurrentModelRButton_.isSelected()) { // get current structure
	    	if (detector == null || detector.getStructure() == null || detector.getStructure().getStructureSnake() == null)
	    		throw new Exception("INFO: Structure model required.");
			targetStructure = detector.getStructure();
		} else if (expression2DReverseOtherModelRButton_.isSelected()) { // open structure from file
			if (expression2DReverseStructureTField_.getText().compareTo("") == 0)
				throw new Exception("INFO: Please select a structure model.");
			targetStructure = system_.newStructure();
			targetStructure.read(new File(expression2DReverseStructureTField_.getText()).toURI());	
		} else
			throw new Exception("ERROR: Unknown structure for generating reversed expression map.");
		
		// open circular expression map
		if (expression2DReverseMapTField_.getText().compareTo("") == 0)
			throw new Exception("INFO: Please select a circular expression map.");
		ImagePlus ip = IJ.openImage(expression2DReverseMapTField_.getText());
		CircularExpressionMap circularMap = new CircularExpressionMap("circular_expression_map", ip.getProcessor());
		
		// equator
		int equator = WJSettings.BOUNDARY_AP;
		if (expression2DReverseEquatorDVRButton_.isSelected())
			equator = WJSettings.BOUNDARY_DV;
		
		if (save) ExpressionDataset.saveReversedExpressionDataset2D(targetStructure, circularMap, equator);
		else ExpressionDataset.showReversedExpressionDataset2D(targetStructure, circularMap, equator);
	}
	
	//----------------------------------------------------------------------------
	
	/** Shows or saves the community expression dataset. */
	protected void computeAggregatedExpressionDataset(boolean save) throws Exception {
		
		List<File> structureFiles = null;
		List<File> projectionFiles = null;
		boolean error = false;
		try {
			// updates the list of files selected using regex
			WJExperimentsSelectionUpdater.instance_.execute();
			structureFiles = WJExperimentsSelectionUpdater.instance_.getStructureFiles();
			projectionFiles = WJExperimentsSelectionUpdater.instance_.getProjectionFiles();
		} catch (Exception e) {
			e.printStackTrace();
			error = true;
		}
		if (error || structureFiles == null || structureFiles.size() < 1)
			throw new Exception("INFO: At least two experiments are required.");
		
		// Listing structure
		for (int i = 0; i < structureFiles.size(); i++)
			WJSettings.log("Selecting structure " + i + ": " + structureFiles.get(i).getAbsolutePath());
		
		// loads structures
		List<Structure> structures = new ArrayList<Structure>();

		Structure structure = null;
		for (File f : structureFiles) {
			structure = system_.newStructure();
			structure.read(f.toURI());
			structures.add(structure);
		}
		
		// loads image projections
		List<ExpressionMap> projections = new ArrayList<ExpressionMap>();
		ImagePlus ip = null;
		for (File f : projectionFiles) {
//			projections.add((new Opener()).openImage(f.getAbsolutePath()));
			ip = IJ.openImage(f.getAbsolutePath());
			String title = FilenameUtils.getFilenameWithoutPath(f.getAbsolutePath());
			title = FilenameUtils.getFilenameWithoutExtension(title);
			projections.add(new ExpressionMap(title, ip.getProcessor()));
		}
		
//		int channel = getSelectedExpressionChannelIndex();
		if (save) ExpressionDataset.saveAggregatedExpressionDataset2D(structures, projections);
		else ExpressionDataset.showAggregatedExpressionDataset2D(structures, projections);
	}
	
	//----------------------------------------------------------------------------
	/** Shows or saves the composite image. */
	protected void computeComposite(boolean save) throws Exception {
		
    	int w = WJImages.areImageWidthsConsistent();
    	int h = WJImages.areImageHeightsConsistent();
    	if (w == 0 && h == 0)
    		throw new Exception("INFO: Requires at least one image or image stack to be open.");
		
		WJSettings settings = WJSettings.getInstance();
		
		if (save) {
			//Event in selecting path for saving the output
	    	JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Save composite image");
	     	fc.setApproveButtonText("Save");
	     	setAppIcon(frame);
	     	
	     	fc.setSelectedFile(new File(settings.getExperimentName() + "_composite.tif"));
	     	 
	     	// Set the current directory to the working directory
	     	File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	     	fc.setCurrentDirectory(f);    	 
	     	fc.setDialogType(JFileChooser.SAVE_DIALOG);
	 
	     	int returnVal = fc.showSaveDialog(frame);
	     	if (returnVal == JFileChooser.APPROVE_OPTION)
	     		WJImages.computeComposite(fc.getSelectedFile().toURI());
		} else
			WJImages.computeComposite();
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns a new projection of for the selected image stack and selected channel from the GUI. */
	private ImagePlus computeProjectionForExpressionQuantification() throws Exception {
		
		WJImages.update();
		int channel = this.getSelectedExpressionChannelIndex();
		if (WJImages.getImageStack(channel) == null || WJImages.getImageStack(channel).getProcessor() == null)
			throw new Exception("INFO: Single image or image stack for \"" + getSelectedExpressionChannelName()  + "\" required.");
		
		return computeProjectionForExpressionQuantification(channel);
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns a new projection of for the selected image stack and given channel. */
	public ImagePlus computeProjectionForExpressionQuantification(int channel) throws Exception {
		
		WJImages.update();
		if (WJImages.getImageStack(channel) == null || WJImages.getImageStack(channel).getProcessor() == null)
			return null;
		
		WJSettings settings = WJSettings.getInstance();
		String channelName = settings.getGeneName(channel);
		int minSlice = settings.getExpressionMinSliceIndex(channel);
		int maxSlice = settings.getExpressionMaxSliceIndex(channel);
	
		ImagePlus image = Projections.doProjection(WJImages.getImageStack(channel), getProjectionMethod(channel), minSlice, maxSlice);
		
		// add it to the manager
		image.setTitle(channelName + "_expression_projection_" + Integer.toString(minSlice) + "-" + Integer.toString(maxSlice));
		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.add(image.getTitle(), image);
		
		return image;
	}
	
	// ----------------------------------------------------------------------------
	
//	/** Called when opening a batch experiment folder. */
//	private void openBatchExperimentsDirectory() throws Exception {
//		
//		if (batchExperiments_ != null && batchExperiments_.isRunning())
//			throw new Exception("INFO: A batch experiment is running.\nYou must first stop it before selecting a new batch experiment.");
//		
//		WJSettings settings = WJSettings.getInstance();
//		
//    	String path = selectBatchDirectory();
//    	if (path != null) {
//   		
//    		// create new batch experiments
//    		BatchExperiments batchExperiments = new BatchExperiments();
//    		batchExperiments.initialize(path);
//    		
//    		// if here means that the new experiment has successfully initialized
//    		batchExperiments_ = batchExperiments;
//    		
//    		// set displayed path
//    		settings.setBatchRootDirectory(path);
//    		batchDirectoryTField_.setText(path);
//    		
//    		// set first experiment index
//    		batchFirstExperimentIndexModel_.setValue(1); // to avoid OutOfBoundsException
//    		batchFirstExperimentIndexModel_.setMinimum(batchExperiments_.getFirstExperimentIndex() + 1);
//    		batchFirstExperimentIndexModel_.setMaximum(batchExperiments_.getLastExperimentIndex() + 1);
//    		batchFirstExperimentIndexModel_.setValue(batchExperiments_.getFirstExperimentIndex() + 1);
//    		// set last experiment index
//    		batchLastExperimentIndexModel_.setValue(1); // to avoid OutOfBoundsException
//    		batchLastExperimentIndexModel_.setMinimum(batchExperiments_.getFirstExperimentIndex() + 1);
//    		batchLastExperimentIndexModel_.setMaximum(batchExperiments_.getLastExperimentIndex() + 1);
//    		batchLastExperimentIndexModel_.setValue(batchExperiments_.getLastExperimentIndex() + 1);
//    		// set current experiment index
//    		batchCurrentExperimentIndexModel_.setValue(1); // to avoid OutOfBoundsException
//    		batchCurrentExperimentIndexModel_.setMinimum(batchExperiments_.getFirstExperimentIndex() + 1);
//    		batchCurrentExperimentIndexModel_.setMaximum(batchExperiments_.getLastExperimentIndex() + 1);
//    		batchCurrentExperimentIndexModel_.setValue(batchExperiments_.getFirstExperimentIndex() + 1);
//    		
//    		// set progress bar to zero
////    		batchProgress_.setStringPainted(true); // XXX: Issue on Win 7 (hideous blue bar)
//    		batchProgress_.setValue((int) 0);
//    		batchProgress_.setString(0 + " / " + (batchExperiments_.getLastExperimentIndex() + 1));
////    		batchProgress2_.setStringPainted(true); // XXX: Issue on Win 7 (hideous blue bar)
//    		batchProgress2_.setValue((int) 0);
//    		batchProgress2_.setString(0 + " / " + (batchExperiments_.getLastExperimentIndex() + 1));
//    	}
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Called to start new batch experiments. */
//	private void startBatchExperiments() throws Exception {
//		
//    	if (batchExperiments_ == null)
//    		throw new Exception("INFO: Select a directory containing experiments.");
//    	
//    	if (batchExperiments_.isRunning())
//    		throw new Exception("INFO: A batch experiment is running.\nYou must first stop it before starting a new batch experiment.");
//    	
//    	batchExperiments_.setFirstExperimentIndex(batchFirstExperimentIndexModel_.getNumber().intValue() - 1);
//    	batchExperiments_.setLastExperimentIndex(batchLastExperimentIndexModel_.getNumber().intValue() - 1); 	
//    	batchExperiments_.start();
//    	
//    	// if the user clicked on cancel
//    	if (batchExperiments_.getCurrentExperimentIndex() < 0) {
//    		batchExperiments_.cancel();
//    		return;
//    	}
// 
//    	// otherwise
//    	if (batchExperiments_.isRunning()) {
//	    	batchCurrentExperimentIndexModel_.setValue(batchExperiments_.getCurrentExperimentIndex() + 1);
//	    	batchProgress_.setValue((int) batchExperiments_.getProgress());
//	    	batchProgress_.setString(batchExperiments_.getProgressAsString());
//	    	batchProgress2_.setValue((int) batchExperiments_.getProgress());
//	    	batchProgress2_.setString(batchExperiments_.getProgressAsString());
//	    	// go back to main interface
////	    	updateChannelPanel();
//	    	setScaleLabel();
//	    	batch2main();
//    	}
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Exit bach experiments mode. */
//	private void exitBatchExperiments() throws Exception {
//		
//    	if (batchExperiments_ == null || !batchExperiments_.isRunning())
//    		return;
//    	
//		batchExperiments_.cancel();
//		batchCurrentExperimentIndexModel_.setValue(0);
//		batchProgress_.setValue((int) 0);
//    	batchProgress_.setString(0 + " / " + batchExperiments_.getLastExperimentIndex());
//    	batchProgress2_.setValue((int) 0);
//    	batchProgress2_.setString(0 + " / " + batchExperiments_.getLastExperimentIndex());
//    	reset();
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Called when accepting an individual experiment from a batch of experiments. */
//	@Override
//	protected void acceptIndividualExperiment() throws Exception {
//		
////    	if (batchExperiments_ == null || !batchExperiments_.isRunning())
////    		throw new Exception("INFO: No batch experiment is running.");
////    	if (detector_ == null || detector_.getStructure() == null || detector_.getStructure().getSnakeStructure() == null)
////    		throw new Exception("INFO: Structure detection first required.");
////    	if (structureVisualization_ == null)
////    		throw new Exception("ERROR: structureVisualization_ is null.");
////    	
////    	BatchExperimentDataset worker = new BatchExperimentDataset(detector_, structureVisualization_);
////    	worker.execute();
//		
//		throw new Exception("Fix WingJ::acceptIndividualExperiment()");
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Called to skip an individual experiment and move to the next one. */
//	@Override
//	protected void skipIndividualExperiment() throws Exception {
//		
//    	if (batchExperiments_ == null || !batchExperiments_.isRunning())
//    		throw new Exception("INFO: No batch experiment is running.");
//    	
//		reset();
//    	
//    	batchExperiments_.skip();
//    	batchCurrentExperimentIndexModel_.setValue(batchExperiments_.getCurrentExperimentIndex() + 1);
//    	batchProgress_.setValue((int) batchExperiments_.getProgress());
//    	batchProgress_.setString(batchExperiments_.getProgressAsString());
//    	batchProgress2_.setValue((int) batchExperiments_.getProgress());
//    	batchProgress2_.setString(batchExperiments_.getProgressAsString());
//	}
	
	// ----------------------------------------------------------------------------
	
//	/** Called when going back to the main interface of WingJ from the batch panel. */
//	private void batch2main() {
//		
//    	batchAcceptButton_.setEnabled(batchExperiments_ != null && batchExperiments_.isRunning());
//    	batchSkipButton_.setEnabled(batchExperiments_ != null && batchExperiments_.isRunning());
//    	showMainPanel();
//	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Returns the Singleton instance of WingJ. */
	static public WingJ getInstance() {
		
		if (instance_ == null)
			instance_ = new WingJ();
		return instance_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Runs WingJ. */
	public void run() {
		
//		try {
//			WJTestFactory factory = new WJTestFactory();
////			factory.generateResources();
//			factory.runAll();
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			System.exit(-1);
//		}
//		System.exit(0);
		
		setWJDefaultLocation();
		setVisible(true);
	}
	
	// ----------------------------------------------------------------------------

	/** Called each time an ActionEvent is generated. */
	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		
		WJSettings settings = WJSettings.getInstance();
		Object source = e.getSource();
		int structureChannelIndex = settings.getStructureChannelIndex();
		
		try {
			// ----------------------------------------------------------------------------
			// MAIN SECTION
			
			if (source == gene0ProjectionMaxRButton_ || source == gene0ProjectionMeanRButton_) {
				gui2settings();
				WJImages.computeImageProjection(0, settings.getExpressionMinSliceIndex(0), settings.getExpressionMaxSliceIndex(0));
				WJImages.getImageProjection(0).show();
			}
			else if (source == gene1ProjectionMaxRButton_ || source == gene1ProjectionMeanRButton_) {
				gui2settings();
				WJImages.computeImageProjection(1, settings.getExpressionMinSliceIndex(1), settings.getExpressionMaxSliceIndex(1));
				WJImages.getImageProjection(1).show();
			}
			else if (source == gene2ProjectionMaxRButton_ || source == gene2ProjectionMeanRButton_) {
				gui2settings();
				WJImages.computeImageProjection(2, settings.getExpressionMinSliceIndex(2), settings.getExpressionMaxSliceIndex(2));
				WJImages.getImageProjection(2).show();
			}
			else if (source == gene3ProjectionMaxRButton_ || source == gene3ProjectionMeanRButton_) {
				gui2settings();
				WJImages.computeImageProjection(3, settings.getExpressionMinSliceIndex(3), settings.getExpressionMaxSliceIndex(3));
				WJImages.getImageProjection(3).show();
			}
			else if (source == exportProjections_) {
				gui2settings();
				WingJ.exportSliceInformation();
				WingJ.exportProjections(null);
			}
			else if (source == ch00DirectoryButton_) {
				gui2settings();
				if (WJImages.openImageStack(0)) {
					setScaleLabel();
					updateChannelPanel(0);
					if (structureChannelIndex == 0) {
						system_.deleteStructureDetector();
						WJImages.imagesMask_ = new WJImagesMask();
					}
				}
	        }
			else if (source == ch01DirectoryButton_) {
				gui2settings();
				if (WJImages.openImageStack(1)) {
					setScaleLabel();
					updateChannelPanel(1);
					if (structureChannelIndex == 1) {
						system_.deleteStructureDetector();
						WJImages.imagesMask_ = new WJImagesMask();
					}
				}
	        }
			else if (source == ch02DirectoryButton_) {
				gui2settings();
				if (WJImages.openImageStack(2)) {
					setScaleLabel();
					updateChannelPanel(2);
					if (structureChannelIndex == 2) {
						system_.deleteStructureDetector();
						WJImages.imagesMask_ = new WJImagesMask();
					}
				}
	        }
			else if (source == ch03DirectoryButton_) {
				gui2settings();
				if (WJImages.openImageStack(3)) {
					setScaleLabel();
					updateChannelPanel(3);
					if (structureChannelIndex == 3) {
						system_.deleteStructureDetector();
						WJImages.imagesMask_ = new WJImagesMask();
					}
				}
	        }
			else if (source == maskButton_) {
				if (WJImages.imagesMask_ == null)
					WJImages.imagesMask_ = new WJImagesMask();
				WJImages.imagesMask_.run(computeProjectionForExpressionQuantification(structureChannelIndex));
			}
			else if(source == outputButton_) {
				setOutputDirectory();
			}
			else if (source == preprocessingScanButton_) {
				structureDetectionPreProcess();
			}
	        else if (source == detectionRunAllButton_) {
	        	runStructureDetection();
	        }
	        else if (source == detectionPauseButton_) {
	        	if (system_.getStructureDetector() != null) {
	        		system_.getStructureDetector().pause(); // will stop at the next before starting the next level
	        	}
	        }
	        else if (source == detectionResumeButton_) {
	        	if (system_.getStructureDetector() == null)
	        		throw new Exception("INFO: First start a new structure detection.");
	        	else {
	        		if (system_.getStructureDetector().isComplete()) {
	        			throw new Exception("INFO: First start a new structure detection.\n");
	        		}
	        		
	        		gui2settings(); // never forget to do that before processing outside of this class
	        		DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
	        		worker.setMode(DetectorWorker.RESUME);
	        		worker.execute();
	        	}
	        }
	        else if (source == detectionAbortButton_) {
	        	system_.deleteStructureDetector();
	        }
	        else if (source == detectionStepButton_) {
        		gui2settings(); // never forget to do that before processing outside of this class
        		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
        				settings.getExpressionMaxSliceIndex(structureChannelIndex));
        		
        		if (system_.getStructureDetector() == null) {
	        		if (createStructureDetector())
	        			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
        		}
        		
        		if (system_.getStructureDetector().isComplete())
        			throw new Exception("INFO: First start a new structure detection.");
        		else {
	        		gui2settings(); // never forget to do that before processing outside of this class
	        		DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
	        		worker.setMode(DetectorWorker.STEP);
	        		worker.execute();
	        	}
	        }
	        else if (source == detectionRedoStepButton_) {
	        	if (system_.getStructureDetector() == null)
	        		throw new Exception("INFO: First start a new structure detection.");
	        	else {
	        		if (system_.getStructureDetector().isComplete()) {
	        			throw new Exception("INFO: First start a new structure detection.\n");
	        		}
	        		
	        		gui2settings(); // never forget to do that before processing outside of this class
	        		DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
	        		worker.setMode(DetectorWorker.REDO);
	        		worker.execute();
	        	} 	 
	        }
	        else if (source == detectionManualButton_) {
        		gui2settings(); // never forget to do that before processing outside of this class
        		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
        				settings.getExpressionMaxSliceIndex(structureChannelIndex));
        		
        		if (createStructureDetector()) {
        			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
        			system_.getStructureDetector().setGenericStructure();
	        		
	        		DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
	        		worker.setMode(DetectorWorker.MANUAL);
	        		worker.execute();
        		}
	        }
	        else if (source == detectionLoadButton_) {
        		gui2settings(); // never forget to do that before processing outside of this class
        		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
        				settings.getExpressionMaxSliceIndex(structureChannelIndex));
        		
        		if (createStructureDetector()) {
        			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
    				system_.getStructureDetector().openStructure();
        		}
	        }
	        else if (source == detectionStructureButton_) {
	        	openStructureEditor();
	        }
	        else if (source == settingsReloadButton_) {
	        	WJSettings.log("Loading settings from " + settings.getLastSettingsFileOpened());
	        	settings.loadLastSettingsOpened();
	        	updateSettingsContent(true);
	        }
	        else if (source == settingsSaveButton_) {
	        	saveSettings();
	        }
	        else if (source == settingsLoadButton_) {
	        	loadSettings();
	        	updateSettingsContent(true);
	        	system_.deleteStructureDetector();
	        }
	        else if (source == aboutButton_) {
	        	WJAboutBox about = new WJAboutBox();
	        	about.run();
	        }
	        else if (source == expressionDatasetShowButton_) {
	        	gui2settings();
	        	computeExpressionDataset(false);
	        }
	        else if (source == expressionDatasetHideButton_) {
	        	ExpressionDataset1D.disposeVisibleOutput();
	        	ExpressionDataset2D.disposeVisibleOutput();
	        	ExpressionDataset2DReversed.disposeVisibleOutput();
	        	ExpressionDataset2DAggregated.disposeVisibleOutput();
	        	WJImages.disposeComposite();
	        }
	        else if (source == expressionExportDatasetButton_) {
	        	gui2settings();
	        	computeExpressionDataset(true);
	        }
	        else if (source == resetButton_) {
	        	reset();
	        }
	        else if (source == expressionSelectedChannelCBox_) {
	        	updateExpressionPanel(expressionSelectedChannelCBox_.getSelectedIndex());
	        }
	        else if (source == expressionCompositeRedCBox_ || 
	        		 source == expressionCompositeGreenCBox_ ||
	        		 source == expressionCompositeBlueCBox_) {
	        	updateExpressionRgbCBox();
	        }
	        else if (source == expressionDimensionDatasetCBox_) {
	        	updateDatasetDimension();
	        }
	        else if (source == batchButton_) {
//	        	batchProgress_.setEnabled(batchExperiments_ != null);
//	        	batchProgress2_.setEnabled(batchExperiments_ != null);
//	        	showBatchPanel();
	        }
	        else if (source == quantifyExpressionButton_) {
	        	showExpressionQuantificationControlers();
	        }
	        else if (source == expression1DResolutionConstantRButton_) {
	        	expression1DNumPointsSpinner_.setEnabled(true);
	        	expression1DNumPointsPerUnitSpinner_.setEnabled(false); 	
	        }
	        else if (source == expression1DResolutionDynamicRButton_) {
	        	expression1DNumPointsSpinner_.setEnabled(false);
	        	expression1DNumPointsPerUnitSpinner_.setEnabled(true);
	        }
	        else if (source == expressionComRootBrowse_) {
	        	setRootExperimentDirectory();
	        }
	        else if (source == expression2DReverseStructureBrowse_) {
	        	setExpression2DReverseStructureFile();
	        }
	        else if (source == expression2DReverseMapBrowse_) {
	        	setExpression2DReverseCircularMapeFile();
	        }
	        else if (source == expression2DReverseCurrentModelRButton_) {
	        	expression2DReverseStructureBrowse_.setEnabled(false);
	        }
	        else if (source == expression2DReverseOtherModelRButton_) {
	        	expression2DReverseStructureBrowse_.setEnabled(true);
	        }
			
			// ----------------------------------------------------------------------------
			// SETTINGS SECTION
			
	        else if (source == setSettingsButton_) {
	        	gui2settings();
	        	updateSettingsContent(false);
	        	showSettingsPanel();
	        }
	        else if (source == settingsCloseButton_) {
	        	WJSettings.log("Saving settings.");
	        	applySettings();
	        	setScaleLabel();
	        	showMainPanel();
	        }
			
			// ----------------------------------------------------------------------------
			// STRUCTURE SECTION

			if (source == swapBoundariesButton_) {
				system_.getStructureDetector().getStructure().swapAxes();
				updateStructureMeasurementsDisplayed();
				structureVisualization_.updateRoiManagerContent();
			}
			else if (source == reverseDVBoundaryButton_) {
				system_.getStructureDetector().getStructure().reverseDVAxisDirection();
				updateStructureMeasurementsDisplayed();
				structureVisualization_.updateRoiManagerContent();
			}
			else if (source == reverseAPBoundaryButton_) {
				system_.getStructureDetector().getStructure().reverseAPAxisDirection();
				updateStructureMeasurementsDisplayed();
				structureVisualization_.updateRoiManagerContent();
			}
			else if (source == editStructureButton_) {
				gui2settings();
				if (structureVisualization_ != null)
					structureVisualization_.setVisible(false);
	 
				// set background image in the detector
				ImagePlusManager manager = ImagePlusManager.getInstance();
				String name = (String) backgroundCBox_.getSelectedItem();
				int index = Integer.parseInt(name.substring(0, 1));
				String type = "stack";
				if (name.endsWith("projection")) type = "projection";
				ImagePlus background = manager.getImage("ch" + index + "_" + type);
				
				if (background == null)
					background = WJImages.getImageProjection(structureChannelIndex);
				system_.getStructureDetector().setStructureProjection(background);
				
				// if it's a stack displayed, get the current slice
				if (type.compareTo("stack") == 0)
					system_.getStructureDetector().getStructureProjection().setSlice(structureVisualization_.getImage().getSlice());
				
				// generate a new snake with the given number of control points
				// if the structure is rejected, this new snake is just deleted
				system_.getStructureDetector().setTmpStructureSnake(system_.getStructureDetector().getStructure().getStructureSnake().getResample(settings.getNumStructureControlPoints()));
				
				DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
				worker.setMode(DetectorWorker.MANUAL);
				worker.execute();
			}
			else if (source == setColorButton_) {
				if (structureVisualization_ == null)
					throw new Exception("ERROR: structureViewer_ is null.");
				structureVisualization_.setColorFromColorChooser();
			}
			else if (source == backgroundCBox_) {
				if (structureVisualization_ == null) return;
				// format item in combobox: "X geneName stack|projection"
				// format in manager: "chX_stack|projection"
				structureBgImgBkp_ = backgroundCBox_.getSelectedIndex();
				String name = (String) backgroundCBox_.getSelectedItem();
				int index = Integer.parseInt(name.substring(0, 1));
				String type = "stack";
				if (name.endsWith("projection")) type = "projection";
				structureVisualization_.setImage(ImagePlusManager.getInstance().getImage("ch" + index + "_" + type)); //ImagePlusManager.getInstance().getImage(name)
			}
			else if (source == saveStructureButton_) {
				system_.getStructureDetector().getStructure().write();
			}
			else if (source == exportStructurePropertiesButton_) {
				system_.getStructureDetector().getStructure().writeStructureMeasurements();
			}
			else if (source == exportPreviewImageButton_) {
				StructureDataset.saveStructurePreview(structureVisualization_);
			}
			else if (source == exportBinaryMaskButton_) {
				StructureDataset.saveStructureMask(structureVisualization_);
			}
			else if (source == exportAllButton_) {
				system_.getStructureDetector().getStructure().writeStructureDataset(structureVisualization_);
			}
	        else if (source == structureCloseButton_) {
	        	if (structureVisualization_ != null) 
	        		structureVisualization_.setVisible(false);
	        	showMainPanel();
	        }
			
			// ----------------------------------------------------------------------------
			// BATCH EXPERIMENTS SECTION
			
	        else if (source == batchBrowseButton_) {
//	        	openBatchExperimentsDirectory();
	        }
	        else if (source == batchBeginButton_) {
//	        	startBatchExperiments();
	        }
	        else if (source == batchExitButton_) {
//	        	exitBatchExperiments();
	        }
	        else if (source == batchCloseButton_) {
//	        	batch2main();
	        }
	        else if (source == batchAcceptButton_) {
//	        	acceptIndividualExperiment();
	        }
	        else if (source == batchSkipButton_) {
//	        	skipIndividualExperiment();
	        }
			
			// ----------------------------------------------------------------------------
			// BATCH EXPERIMENTS SECTION
			
	        else if (source == expressionBackButton_) {
	        	gui2settings();
	        	showMainPanel();
	        }
			
			toFront();
			
		} catch (OutOfMemoryError oome) {
			WJMessage.showMessage(WJSettings.OUT_OF_MEMORY_ERROR_MESSAGE, "ERROR");
			// DO NOT REMOVE THIS LINE
			// ANALYTICS CODE: START
			Analytics.getInstance().incrementNumOutOfMemoryErrors();
			// END
		} catch (Exception e1) {
			WJMessage.showMessage(e1);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Implements itemStateChanged(). */
	@Override
	public void itemStateChanged(ItemEvent e) {

		Object source = e.getSource();
		
		try {
			if (source == systemCBox_) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					system_.deleteStructureDetector();
					setSystem();
				}
			}
			else if (source == showOverlayInformationCBox_) {
				if (structureVisualization_ == null) return;
				structureVisualization_.isTextVisible(showOverlayInformationCBox_.isSelected());
				structureVisualization_.update();
			}
			else if (source == showOverlayStructureCBox_) {
				if (structureVisualization_ == null) return;
				structureVisualization_.isStructureVisible(showOverlayStructureCBox_.isSelected());
				structureVisualization_.update();
			}
//			else if (source == showBoundariesCBox_) {
//				if (structureVisualization_ == null) return;
//				structureVisualization_.isBoundariesVisible(showBoundariesCBox_.isSelected());
//				structureVisualization_.update();
//			}
			
			gui2settings();
			
		} catch (Exception e1) {
			WJMessage.showMessage(e1);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Implements stateChanged(). */
	@Override
	public void stateChanged(ChangeEvent e) {

		WJSettings settings = WJSettings.getInstance();
		Object source = e.getSource();
		
		try {
			if (source == batchFirstExperimentIndexSpinner_) {
//				int index = batchFirstExperimentIndexModel_.getNumber().intValue();
//				if (index == 0) batchFirstExperimentLabel_.setText("");
//				else batchFirstExperimentLabel_.setText(batchExperiments_.getExperimentName(index - 1));
			}
			else if (source == batchLastExperimentIndexSpinner_) {
//				int index = batchLastExperimentIndexModel_.getNumber().intValue();
//				if (index == 0) batchLastExperimentLabel_.setText("");
//				else batchLastExperimentLabel_.setText(batchExperiments_.getExperimentName(index - 1));
			}
			else if (source == batchCurrentExperimentIndexSpinner_) {
//				int index = batchCurrentExperimentIndexModel_.getNumber().intValue();
//				if (index == 0) batchCurrentExperimentLabel_.setText("");
//				else batchCurrentExperimentLabel_.setText(batchExperiments_.getExperimentName(index - 1));
			}
			else if (source == minSliceIndexes_.get(0)) {
				int value = (Integer) minSliceIndexes_.get(0).getModel().getValue();
				settings.setExpressionMinSliceIndex(0, value);
			}
			else if (source == minSliceIndexes_.get(1)) {
				int value = (Integer) minSliceIndexes_.get(1).getModel().getValue();
				settings.setExpressionMinSliceIndex(1, value);
			}
			else if (source == minSliceIndexes_.get(2)) {
				int value = (Integer) minSliceIndexes_.get(2).getModel().getValue();
				settings.setExpressionMinSliceIndex(2, value);
			}
			else if (source == minSliceIndexes_.get(3)) {
				int value = (Integer) minSliceIndexes_.get(3).getModel().getValue();
				settings.setExpressionMinSliceIndex(3, value);
			}
			else if (source == maxSliceIndexes_.get(0)) {
				int value = (Integer) maxSliceIndexes_.get(0).getModel().getValue();
				settings.setExpressionMaxSliceIndex(0, value);
			}
			else if (source == maxSliceIndexes_.get(1)) {
				int value = (Integer) maxSliceIndexes_.get(1).getModel().getValue();
				settings.setExpressionMaxSliceIndex(1, value);
			}
			else if (source == maxSliceIndexes_.get(2)) {
				int value = (Integer) maxSliceIndexes_.get(2).getModel().getValue();
				settings.setExpressionMaxSliceIndex(2, value);
			}
			else if (source == maxSliceIndexes_.get(3)) {
				int value = (Integer) maxSliceIndexes_.get(3).getModel().getValue();
				settings.setExpressionMaxSliceIndex(3, value);
			}
		} catch (Exception e1) {
			WJMessage.showMessage(e1);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens the structure editor. This method is also called by the detector package. */
	@Override
	public void openStructureEditor() throws Exception {
		
		gui2settings();

    	if (system_.getStructureDetector() != null && system_.getStructureDetector().isComplete()) {
    		initializeStructurePanel();
    		// get background image from background combobox
    		ImagePlusManager manager = ImagePlusManager.getInstance();
			String name = (String) backgroundCBox_.getSelectedItem();
			int index = Integer.parseInt(name.substring(0, 1));
			String type = "stack";
			if (name.endsWith("projection")) type = "projection";
			ImagePlus background = manager.getImage("ch" + index + "_" + type);
			
			// here the background can be null if the image has been manually closed
			if (background == null || background.getProcessor() == null)
				throw new Exception("INFO: Image data \"" + "ch" + index + "_" + type + "\" are required for structure preview.\n\nPlease load them again in channel " + index + ".");
			
    		structureVisualization_ = new WJStructureViewer(system_.getStructureDetector().getStructure(), background);
    		// get the slice index from the detector
    		int slice = system_.getStructureDetector().getStructureProjection().getSlice();
    		structureVisualization_.getImage().setSlice(slice);
    		structureVisualization_.setVisible(true);
    		showStructurePanel();
    		setToolTipManagerPreferences();
    	} else
			throw new Exception("INFO: Structure model required.");
	}
	
	// ----------------------------------------------------------------------------
	
	/** Called when the Pre-Process button is clicked. */
	@Override
	public void structureDetectionPreProcess() throws Exception {
		
		WJSettings.log("Pre-preprocessing");
		
		WJSettings settings = WJSettings.getInstance();
		int structureChannelIndex = settings.getStructureChannelIndex();
		
		gui2settings();
		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
				settings.getExpressionMaxSliceIndex(structureChannelIndex));
		
		// sets the projection of the detector and runs the initialization method
		if (system_ != null && system_.getStructureDetector() == null ) {
			system_.newStructureDetector("tmp");
			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
		}
		
		// runs early pre-processing which set exclusively parameter values in WJSettings
		system_.getStructureDetector().runEarlyPreProcessing();
		
		// deletes the tmp detector because we can't use it any further
		system_.deleteStructureDetector();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Runs structure detection. */
	@Override
	public void runStructureDetection() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		int structureChannelIndex = settings.getStructureChannelIndex();
		
    	if (system_.getStructureDetector() != null && runNewStructureDetection()) {
    		system_.deleteStructureDetector();
    	}
    	if (system_.getStructureDetector() == null) {
    		gui2settings(); // never forget to do that before processing outside of this class
    		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
    				settings.getExpressionMaxSliceIndex(structureChannelIndex));
    		
    		if (createStructureDetector()) {
    			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
	    		DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
	    		worker.setMode(DetectorWorker.RUNALL);
	    		worker.execute();
    		}
    	}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows panel where expression quantification can be performed. */
	@Override
	public void showExpressionQuantificationControlers() throws Exception {
		
//    	if (detector_ == null)
//			throw new Exception("INFO: First start a new structure detection.");
		
    	gui2settings();
    	WJSettings settings = WJSettings.getInstance();
    	
    	// initialize gene/channel combobox for selection
    	// NOTE: the user may have changed the name from the main interface
    	expressionSelectedChannelCBox_.removeActionListener(this);
    	// the first letter of the selected expression channel name gives
    	// the index of the channel selected
    	int selectedChannelIndex = 0;
    	if (expressionSelectedChannelCBox_.getItemCount() > 0)
	    	selectedChannelIndex = getSelectedExpressionChannelIndex();
    	
    	expressionSelectedChannelCBox_.removeAllItems(); // XXX
	    	
    	int toSelect = 0;
    	for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
    		if (WJImages.numSlices_[i] != 0) {
    			expressionSelectedChannelCBox_.addItem(i + " " + settings.getGeneName(i));
    			if (i == selectedChannelIndex)
    				toSelect = expressionSelectedChannelCBox_.getItemCount()-1;
    		}
    	}
    	if (expressionSelectedChannelCBox_.getItemCount() > 0)
    		expressionSelectedChannelCBox_.setSelectedIndex(toSelect);
    	
//    	int channel = this.getSelectedChannelIndex(); // save index
//    	expressionSelectedChannelCBox_.removeAllItems();
//    	for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
//    		String suffix = WJSettings.getInstance().getGeneName(i);
//    		if (!suffix.isEmpty())
//    			suffix = " (" + suffix + ")";
//    		expressionSelectedChannelCBox_.addItem(new String(i + suffix));
//    	}
//    	expressionSelectedChannelCBox_.setSelectedIndex(channel); // restore selection
    	expressionSelectedChannelCBox_.addActionListener(this);
    	
		// for each RGB color, a channel is selected using a combobox
		// format of the items: "Channel X"
		int[] selectedChannelIndexes = new int[3];
		if (expressionCompositeRedCBox_.getSelectedIndex() != WJSettings.NUM_CHANNELS)
			selectedChannelIndexes[0] = Integer.parseInt(((String)expressionCompositeRedCBox_.getSelectedItem()).substring(0, 1));
		else
			selectedChannelIndexes[0] = WJSettings.NUM_CHANNELS;
		
		if (expressionCompositeGreenCBox_.getSelectedIndex() != WJSettings.NUM_CHANNELS)
			selectedChannelIndexes[1] = Integer.parseInt(((String)expressionCompositeGreenCBox_.getSelectedItem()).substring(0, 1));
		else
			selectedChannelIndexes[1] = WJSettings.NUM_CHANNELS;
		
		if (expressionCompositeBlueCBox_.getSelectedIndex() != WJSettings.NUM_CHANNELS)
			selectedChannelIndexes[2] = Integer.parseInt(((String)expressionCompositeBlueCBox_.getSelectedItem()).substring(0, 1));
		else
			selectedChannelIndexes[2] = WJSettings.NUM_CHANNELS;
    	
		// update combobox with the name of the channel
		expressionCompositeRedCBox_.removeActionListener(this);
		expressionCompositeGreenCBox_.removeActionListener(this);
		expressionCompositeBlueCBox_.removeActionListener(this);
		
		expressionCompositeRedCBox_.removeAllItems();
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
			expressionCompositeRedCBox_.addItem(i + " " + (WJImages.getImageStack(i) != null ? settings.getGeneName(i) : "-"));
		expressionCompositeRedCBox_.addItem("None");
		expressionCompositeRedCBox_.setSelectedIndex(selectedChannelIndexes[0]);
		
		expressionCompositeGreenCBox_.removeAllItems();
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
			expressionCompositeGreenCBox_.addItem(i + " " + (WJImages.getImageStack(i) != null ? settings.getGeneName(i) : "-"));
		expressionCompositeGreenCBox_.addItem("None");
		expressionCompositeGreenCBox_.setSelectedIndex(selectedChannelIndexes[1]);
		
		expressionCompositeBlueCBox_.removeAllItems();
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
			expressionCompositeBlueCBox_.addItem(i + " " + (WJImages.getImageStack(i) != null ? settings.getGeneName(i) : "-"));
		expressionCompositeBlueCBox_.addItem("None");
		expressionCompositeBlueCBox_.setSelectedIndex(selectedChannelIndexes[2]);
		
		expressionCompositeRedCBox_.addActionListener(this);
		expressionCompositeGreenCBox_.addActionListener(this);
		expressionCompositeBlueCBox_.addActionListener(this);
    	
    	updateDatasetDimension();  	
    	showExpressionPanel();
//    	setToolTipManagerPreferences();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Loads a structure file and show it. */
	public void openStructureFile(URI uri) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		int structureChannelIndex = settings.getStructureChannelIndex();
		
		WJImages.computeImageProjection(structureChannelIndex, settings.getExpressionMinSliceIndex(structureChannelIndex),
				settings.getExpressionMaxSliceIndex(structureChannelIndex));
		
		if (createStructureDetector()) {
			system_.getStructureDetector().setStructureProjection(WJImages.getImageProjection(structureChannelIndex));
			system_.getStructureDetector().openStructure(uri);
	
			DetectorWorker worker = new DetectorWorker(system_.getStructureDetector());
			worker.setMode(DetectorWorker.MANUAL);
			worker.execute();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Called when the system selection changed. */
	private void setSystem() throws Exception {
		
		WJSystemManager smanager = WJSystemManager.getInstance();
		
		// set combobox tooltip with the system description
		String systemDescription = smanager.getSystemDescription(systemCBox_.getSelectedIndex());
		systemCBox_.setToolTipText(systemDescription);
		
		String systemName = getSelectedSystemName();
		WJSettings.log("Selecting " + systemName + ".");		
		system_ = WJSystemManager.getInstance().getSystem(getSelectedSystemId());
		
		boolean b = system_.providesUnsupervisedStructureDetection_;
		preprocessingScanButton_.setEnabled(b);
		detectionRunAllButton_.setEnabled(b);
		detectionPauseButton_.setEnabled(b);
		detectionResumeButton_.setEnabled(b);
		detectionStepButton_.setEnabled(b);
		detectionRedoStepButton_.setEnabled(b);
	}
	
	// ----------------------------------------------------------------------------
	
//	/** Returns a new Structure object based on the selected system model. */
//	public Structure newStructure() throws Exception {
//		
//		String systemName = getSelectedSystemName();
//		if (systemName.compareTo(WJSystemManager.DROSOPHILA_MELANOGASTER_WING_POUCH) == 0) {
//			return new WPouchStructure("drosophila_wing_pouch");
//		} else if (systemName.compareTo(WJSystemManager.DROSOPHILA_MELANOGASTER_EMBRYO) == 0) {
//			return new EmbryoStructure("embryo_structure");
//		} else
//			throw new Exception("ERROR: Unknown system.");
//	}
	
//	/** Finalizes an experiment from batch experiments and move to the next one. */
//	public void finilizeExperiment() throws Exception {
//		
//    	batchExperiments_.myfinalize();
//    	reset();
//    	
//    	batchExperiments_.next();
//    	batchCurrentExperimentIndexModel_.setValue(batchExperiments_.getCurrentExperimentIndex() + 1);
//    	batchProgress_.setValue((int) batchExperiments_.getProgress());
//    	batchProgress_.setString(batchExperiments_.getProgressAsString());
//    	batchProgress2_.setValue((int) batchExperiments_.getProgress());
//    	batchProgress2_.setString(batchExperiments_.getProgressAsString());
//    	
//    	// go back to main interface
////    	updateChannelPanel();
//    	batch2main();
//	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Exports the projections of the available channel. If the given path is empty or null,
	 * exports automatically to the output directory.
	 * @param directory Path to where the projections must be saved (requires final file separator). If null or empty, save to output directory.
	 */
	public static void exportProjections(String directory) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		String outputDirectory = directory;
		if (outputDirectory == null || outputDirectory.compareTo("") == 0)
			outputDirectory = settings.getOutputDirectory();
		
		String projectionType = "";
		String filename = null;
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			if (!(WJImages.getImageStack(i) == null || WJImages.getImageStack(i).getProcessor() == null)) {
				try {
					String channelName = settings.getGeneName(i);
					int minSlice = settings.getExpressionMinSliceIndex(i);
					int maxSlice = settings.getExpressionMaxSliceIndex(i);
					
					int projectionMethod = WingJ.getInstance().getProjectionMethod(i);
					if (projectionMethod == Projections.PROJECTION_MEAN_METHOD)
						projectionType = "mean";
					else if (projectionMethod == Projections.PROJECTION_MAX_METHOD)
						projectionType = "max";
					else
						throw new Exception("ERROR: Unknown Z-projection method.");
					
					// XXX: Change next line
//					ImagePlus image = Projections.doProjection(WJImages.getImageStack(i), 0, minSlice, maxSlice);
					ImagePlus image = Projections.doProjection(WJImages.getImageStack(i), projectionMethod, minSlice, maxSlice);
					filename = outputDirectory + channelName + "_projection.tif";
					// XXX: Change next line
					WJImages.save32bitTo8bit(filename, image);
					WJSettings.log("[x] Writing " + channelName + " projection (slices " + minSlice + " to " + maxSlice + ", " + projectionType + ")");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing " + filename);
					e.printStackTrace();
				}
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Method to automatically load stack of images. First, look for the file
	 * WJSettings.SLICE_DATASET_FILENAME in the selected images folder. If it
	 * doesn't exit, the method ends. Otherwise, the content of this file is loaded
	 * and the channels are loaded depending on the content of this file.
	 */
	public boolean automaticImagesLoading(String imagesDirectory) throws Exception {
		
		if (!WJImages.allowImagesAutoLoading_)
			return false; // then manually open the image stack
		
		// does the slice dataset exist ?
		try {
			File f = new File(imagesDirectory + WJSettings.SLICE_DATASET_FILENAME);
			if (f.isFile() && f.exists()) {
				// a image dataset has been found. Use it or load manually image stack
				String ratio1 = "Manual";
				String ratio2 = "Dataset";
				
				Object[] options = {ratio1, ratio2};
				int n = JOptionPane.showOptionDialog(WingJ.getInstance(),
				    "Image selection mode?",
				    "Image selection mode",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[1]);
				
				if (n == JOptionPane.YES_OPTION)
					return false;
				
				gui2settings();
				WJImages.readSliceDatasetAndOpenImages(f.toURI());
				settings2gui();
				
				// allows to auto load image stacks once per "session"
				// click on Reset to clear this flag
				WJImages.allowImagesAutoLoading_ = false;
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Saves the image slices information in the folder "images" and saves
	 * the projections in the output directory without prompting the user for interaction.
	 * If the "images" directory is not found, ask the user to give it.
	 */
	public static void exportSliceInformation() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		boolean atLeastOneStackLoaded = false;
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			if (WJImages.getImageStack(i) != null) {
				atLeastOneStackLoaded = true;
				break;
			}
		}
		if (!atLeastOneStackLoaded)
			throw new Exception("INFO: At least one channel must contain images.");
		
		// get the images directory from the output directory
		String directory = FilenameUtils.getDirectory(settings.getOutputDirectory()) + "/" + "images" + "/";
		File f = new File(directory);
		if (!f.exists() || !f.isDirectory()) {
			// prompt the user to specify a directory
	    	JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Select \"images\" directory");
	     	WingJ.setAppIcon(frame);
	     	 
	     	// Set the current directory to the working directory
	     	File f2 = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	     	fc.setCurrentDirectory(f2);
	     	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	     	
	     	int returnVal = fc.showOpenDialog(frame);
	     	if (returnVal == JFileChooser.APPROVE_OPTION) {
	     		// Add system-dependent path separator (if required)
	     		directory = fc.getSelectedFile().getPath();
	     	} else
	     		return;
		}
		
 		directory = directory.replace("\\", "/");
 		if (!directory.endsWith(System.getProperty("file.separator")) && !directory.endsWith("/"))
 			directory += "/";
		
		// here we have the directory (with final separator) where slice information should be saved.
		f = new File(directory + WJSettings.SLICE_DATASET_FILENAME);
		WJImages.writeSliceDataset(f);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Increments the number of expression datasets being processed. */
	public void registerActiveExpressionDatasetProcess() {
		
		numActiveExpressionDatasetProcesses_++;
		setExpressionWaitingSnakeVisible(numActiveExpressionDatasetProcesses_ == 1);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Decrements the number of expression datasets being processed. */
	public void removeActiveExpressionDatasetProcess() {
		
		numActiveExpressionDatasetProcesses_--;
		setExpressionWaitingSnakeVisible(!(numActiveExpressionDatasetProcesses_ == 0));
	}

	// ============================================================================
	// SETTERS AND GETTERS
	
//	public StructureDetector getDetector() { return detector_; }
	public WJSystem getSystem() { return system_; }
	public WJStructureViewer getStructureVisualization() { return structureVisualization_; }
}