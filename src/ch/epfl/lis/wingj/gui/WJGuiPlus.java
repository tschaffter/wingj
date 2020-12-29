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

package ch.epfl.lis.wingj.gui;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Toolbar;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;

import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJSystem;
import ch.epfl.lis.wingj.WJSystemManager;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.filefilters.FilterStructureXml;
import ch.epfl.lis.wingj.utilities.FileUtils;
import ch.epfl.lis.wingj.utilities.FilenameUtils;
import ch.epfl.lis.wingj.utilities.Projections;
import ch.tschaffter.gui.SpinnerSliderCoupler;

/** 
 * Initializes the interface of WingJ and defines methods tightly associated to the GUI.
 * 
 * @version August 28, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
abstract public class WJGuiPlus extends WJGui implements ActionListener, ItemListener, ChangeListener {

	/** Default serial. */
	private static final long serialVersionUID = 1L;
	
	/** Save the caret position in the settings text pane.  */
	public static int SETTINGS_CARET_POSITION = 0;
	
	/** StyledDocument for settings text pane. */
	protected StyledDocument doc_ = null;
	/** Style for settings text pane. */
	protected Style style_ = null;
	
	/** StyledDocument for structure dataset text pane. */
	protected StyledDocument structureDatasetDoc_ = null;
	/** Style for structure dataset text pane. */
	protected Style structureDatasetStyle_ = null;
	
	/** Document of the JTextField associated to the name of the structure. */
	protected Document wingNameDocument_;
	
	/** List of check boxes for structure channel index for easy access. */
	protected JRadioButton[] structureChannelIndexCBoxes_ = {ch00CBox_, ch01CBox_, ch02CBox_, ch03CBox_};
	
	/** Backup of the selected background image for the structure detection. */
	protected int structureBgImgBkp_ = -1;
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Resets WingJ and make it ready to start a new experiment. */
	abstract protected void reset() throws Exception;
	/** Called each time the JTextField defining the name of the current structure is updated. */
	abstract protected void setStructureName();
	/** Runs auto search of pre-processing parameters */
	abstract public void structureDetectionPreProcess() throws Exception;
	/** Runs automatic structure detection. */
	abstract public void runStructureDetection() throws Exception;
	/** Opens the structure editor. This method is also called by the detector package. */
	abstract public void openStructureEditor() throws Exception;
	/** Shows panel where expression quantification can be performed. */
	abstract public void showExpressionQuantificationControlers() throws Exception;
//	/** Called when accepting an individual experiment from a batch of experiments */
//	abstract protected void acceptIndividualExperiment() throws Exception;
//	/** Called to skip an individual experiment and move to the next one */
//	abstract protected void skipIndividualExperiment() throws Exception;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Defines and adds listeners to controllers of the graphical interface of WingJ. */
	private void initializeListeners() {
		
		// set specific controllers
		batchAcceptButton_.setEnabled(false);
		batchSkipButton_.setEnabled(false);
		
		// WINDOW LISTENERS
		// Override the default behavior of the closing button of the application
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				
				try {
			        int reply = JOptionPane.showConfirmDialog(WingJ.getInstance(),
					   		"Exit WingJ ?",
					   		"WingJ message",
					   		JOptionPane.YES_NO_OPTION);
			        
			        if (reply == JOptionPane.NO_OPTION) return;
		        	exitWingJ();
				} catch (Exception e) {
					WJMessage.showMessage(e);
				}
			}
		});
		
		// FILL SYSTEM COMBOBOX
		WJSystemManager smanager = WJSystemManager.getInstance();
		systemCBox_.removeAllItems();
		systemCBox_.setModel(new DefaultComboBoxModel(smanager.getSystemNames()));
		// set tooltip
		String systemDescription = WJSystemManager.getInstance().getSystemDescription(systemCBox_.getSelectedIndex());
		systemCBox_.setToolTipText(systemDescription);
		
		// ACTION LISTENERS
		gene0ProjectionMaxRButton_.addActionListener(this);
		gene0ProjectionMeanRButton_.addActionListener(this);
		gene1ProjectionMaxRButton_.addActionListener(this);
		gene1ProjectionMeanRButton_.addActionListener(this);
		gene2ProjectionMaxRButton_.addActionListener(this);
		gene2ProjectionMeanRButton_.addActionListener(this);
		gene3ProjectionMaxRButton_.addActionListener(this);
		gene3ProjectionMeanRButton_.addActionListener(this);
		exportProjections_.addActionListener(this);
		detectionRunAllButton_.addActionListener(this);
		detectionPauseButton_.addActionListener(this);
		detectionResumeButton_.addActionListener(this);
		detectionAbortButton_.addActionListener(this);
		detectionStepButton_.addActionListener(this);
		detectionRedoStepButton_.addActionListener(this);
		detectionManualButton_.addActionListener(this);
		detectionLoadButton_.addActionListener(this);
		detectionStructureButton_.addActionListener(this);
		ch00DirectoryButton_.addActionListener(this);      
		ch01DirectoryButton_.addActionListener(this);
		ch02DirectoryButton_.addActionListener(this);
		ch03DirectoryButton_.addActionListener(this);
		nameTField_.addActionListener(this);
		maskButton_.addActionListener(this);
        outputButton_.addActionListener(this);
        aboutButton_.addActionListener(this);
        batchButton_.addActionListener(this);
        resetButton_.addActionListener(this);
		batchAcceptButton_.addActionListener(this);
		batchSkipButton_.addActionListener(this);
		quantifyExpressionButton_.addActionListener(this);
		preprocessingScanButton_.addActionListener(this);
		// action listener for the settings panel
		setSettingsButton_.addActionListener(this);
		settingsCloseButton_.addActionListener(this);
		settingsReloadButton_.addActionListener(this);
		settingsSaveButton_.addActionListener(this);
		settingsLoadButton_.addActionListener(this);
		// action listener for the structure panel
		swapBoundariesButton_.addActionListener(this);
		reverseDVBoundaryButton_.addActionListener(this);
		reverseAPBoundaryButton_.addActionListener(this);
		editStructureButton_.addActionListener(this);
		setColorButton_.addActionListener(this);
		backgroundCBox_.addActionListener(this);
		saveStructureButton_.addActionListener(this);
		exportStructurePropertiesButton_.addActionListener(this);
		exportPreviewImageButton_.addActionListener(this);
		exportBinaryMaskButton_.addActionListener(this);
		exportAllButton_.addActionListener(this);
		structureCloseButton_.addActionListener(this);
		// action listener for the batch panel		
		batchBrowseButton_.addActionListener(this);
		batchBeginButton_.addActionListener(this);
		batchExitButton_.addActionListener(this);
		batchCloseButton_.addActionListener(this);
		// action listers for expression panel
        expressionDatasetShowButton_.addActionListener(this);
        expressionDatasetHideButton_.addActionListener(this);
        expressionDimensionDatasetCBox_.addActionListener(this);
        expressionExportDatasetButton_.addActionListener(this);
		expressionSelectedChannelCBox_.addActionListener(this);
		expressionBackButton_.addActionListener(this);
		expression1DResolutionConstantRButton_.addActionListener(this);
		expression1DResolutionDynamicRButton_.addActionListener(this);
		expressionComRootBrowse_.addActionListener(this);
		expression2DReverseStructureBrowse_.addActionListener(this);
		expression2DReverseMapBrowse_.addActionListener(this);
		expression2DReverseEquatorAPRButton_.addActionListener(this);
		expression2DReverseEquatorDVRButton_.addActionListener(this);
		expression2DReverseCurrentModelRButton_.addActionListener(this);
		expression2DReverseOtherModelRButton_.addActionListener(this);
		expressionCompositeRedCBox_.addActionListener(this);
		expressionCompositeGreenCBox_.addActionListener(this);
		expressionCompositeBlueCBox_.addActionListener(this);
		
		// ITEM LISTENERS
		systemCBox_.addItemListener(this);
		showOverlayInformationCBox_.addItemListener(this);
		showOverlayStructureCBox_.addItemListener(this);
		ch00CBox_.addItemListener(this);
		ch01CBox_.addItemListener(this);
		ch02CBox_.addItemListener(this);
		ch03CBox_.addItemListener(this);
		
		// DOCUMENT LISTENERS
		// add document listener to the experiment name field to update continuously
		// the name of the detected structure
		wingNameDocument_ = nameTField_.getDocument();
		wingNameDocument_.addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) { setStructureName(); }
			@Override
			public void insertUpdate(DocumentEvent arg0) { setStructureName(); }
			@Override
			public void removeUpdate(DocumentEvent arg0) { setStructureName(); }
		});
		
		// CHANGE LISTENERS
		batchFirstExperimentIndexSpinner_.addChangeListener(this);
		batchLastExperimentIndexSpinner_.addChangeListener(this);
		batchCurrentExperimentIndexSpinner_.addChangeListener(this);
		
		// don't display right know a few compenents to not have a big
		// interface if pack() is not called
		expression1DPanel_.setVisible(false);
		expression2DPanel_.setVisible(false);
		expression2DAggregatedPanel_.setVisible(false);
		expressionCompositePanel_.setVisible(false);
		
		// COMMUNITY EXPRESSION
		// set initial content
		expressionAggRootTField_.setText(System.getProperty("user.home") + System.getProperty("file.separator"));
		expressionAggStructureTField_.setText("*_structure.xml");
		expressionAggProjectionTField_.setText("*_projection.tif");
		
//		expressionAggRootTField_.setText("/mnt/extra/wingj_benchmarks/20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H/");
//		expressionAggStructureTField_.setText("*_structure.xml");
//		expressionAggProjectionTField_.setText("wg-ptcAB*_projection.tif");
		
//		expressionAggRootTField_.setText("/home/tschaffter/devel/java/WingJ/benchmarks/wingpouch_pmadAB_brkAB_wg-ptcAB_90H/");
//		expressionAggStructureTField_.setText("structure.xml");
//		expressionAggProjectionTField_.setText("wg-ptcAB_projection.tif");
		// add action listeners
		expressionAggRootTField_.addActionListener(this);
		expressionAggStructureTField_.addActionListener(this);
		expressionAggProjectionTField_.addActionListener(this);
		// add document lister to know when counting the number of selected networks
		expressionAggRootTField_.getDocument().addDocumentListener(new IndividualExperimentsDocumentListener());
		expressionAggStructureTField_.getDocument().addDocumentListener(new IndividualExperimentsDocumentListener());
		expressionAggProjectionTField_.getDocument().addDocumentListener(new IndividualExperimentsDocumentListener());
		
		// INDIVIDUAL EXPRESSION (REVERSE)
		
		// FIRE A FEW ELEMENTS
		expression1DResolutionConstantRButton_.doClick();
		expression2DReverseCurrentModelRButton_.doClick();
		
		pack();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Set message bar.
	 * <br>
	 * IMPORTANT: The first message will determine the width of the application
	 * (if longer than the other components). The following messages will automatically
	 * fit the defined width.
	 */
	private void initializeAppTips() {
		
		try {
			URL tipsUrl = getClass().getResource("/ch/epfl/lis/wingj/gui/rsc/html-messages.txt");
			if (tipsUrl == null)
				throw new Exception("WARNING: File containing tips not found.");
			
			msgBar_.loadHtmlMessages(tipsUrl);
			Collections.shuffle(msgBar_.getMessages());
	
			msgBar_.setNormalDuration(1 * 120000);
			msgBar_.setExtendedDuration(1 * 120000);
			msgBar_.setHideAfterFirstMessageDuration(1000 * 60000);
			
			msgBar_.setMessage(msgBar_.getMessages().size() - 1);
			msgBar_.start();
		} catch (Exception e) {
			// WJMessage.showMessage(e); // do not use otherwise cycling error with WingJ.getInstance()
			e.printStackTrace();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the default location of the IJ toolbar and WingJ dialog. */
	protected void setWJDefaultLocation() {
		
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		final int SPACE = 30;
		final int IJ_LOG_WIDTH = 400;
		final int IJ_LOG_HEIGHT = 250;
		
		// IJ toolbar to the upper-right corner
		Container ijToolbar = Toolbar.getInstance().getParent();
		Point ijLocation = new Point(screenDim.width-ijToolbar.getWidth(), 0);
		ijToolbar.setLocation(ijLocation);
		
		// WingJ right below the IJ toolbar sticking to the right side of the screen
		Point wjLocation = new Point(screenDim.width-getWidth(), 0+ijToolbar.getHeight()+SPACE);
		setLocation(wjLocation);
		
		// If enough space, IJ log window below WingJ aligned with the right side of the screen.
		// Otherwise, IJ log window to the upper-left corner.
		Frame frame = WindowManager.getFrame("Log"); 
		frame.setSize(IJ_LOG_WIDTH, IJ_LOG_HEIGHT);
		String macro = null;
		if (ijToolbar.getHeight()+getHeight()+IJ_LOG_HEIGHT+(2*SPACE) < screenDim.height) {
			macro = "if (isOpen(\"Log\")){\n"
					+ "selectWindow(\"Log\");\n"
					+ "setLocation(" + Double.toString(screenDim.width-IJ_LOG_WIDTH) + "," + Double.toString(ijToolbar.getHeight()+getHeight()+(2*SPACE)) + ");\n"
					+ "}";
		} else {
			macro = "if (isOpen(\"Log\")){\n"
					+ "selectWindow(\"Log\");\n"
					+ "setLocation(0,0);\n"
					+ "}";
		}
		IJ.runMacro(macro);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Groups the JRadioButtons. */
	private void setRadioButtonGroups() {
		
		ButtonGroup structureProjectionMethodGroup = new ButtonGroup();
		structureProjectionMethodGroup.add(gene0ProjectionMeanRButton_);
		structureProjectionMethodGroup.add(gene0ProjectionMaxRButton_);
		
		ButtonGroup gene1ProjectionMethodGroup = new ButtonGroup();
		gene1ProjectionMethodGroup.add(gene1ProjectionMeanRButton_);
		gene1ProjectionMethodGroup.add(gene1ProjectionMaxRButton_);
		
		ButtonGroup gene2ProjectionMethodGroup = new ButtonGroup();
		gene2ProjectionMethodGroup.add(gene2ProjectionMeanRButton_);
		gene2ProjectionMethodGroup.add(gene2ProjectionMaxRButton_);
		
		ButtonGroup gene3ProjectionMethodGroup = new ButtonGroup();
		gene3ProjectionMethodGroup.add(gene3ProjectionMeanRButton_);
		gene3ProjectionMethodGroup.add(gene3ProjectionMaxRButton_);
		
		ButtonGroup structureChannelIndexGroup = new ButtonGroup();
		structureChannelIndexGroup.add(ch00CBox_);
		structureChannelIndexGroup.add(ch01CBox_);
		structureChannelIndexGroup.add(ch02CBox_);
		structureChannelIndexGroup.add(ch03CBox_);
		
		ButtonGroup expression1DResolutionGroup = new ButtonGroup();
		expression1DResolutionGroup.add(expression1DResolutionConstantRButton_);
		expression1DResolutionGroup.add(expression1DResolutionDynamicRButton_);
		
		ButtonGroup expression2DReverseResolutionGroup = new ButtonGroup();
		expression2DReverseResolutionGroup.add(expression2DReverseEquatorAPRButton_);
		expression2DReverseResolutionGroup.add(expression2DReverseEquatorDVRButton_);
		
		ButtonGroup expression2DReverseSelectedModelGroup = new ButtonGroup();
		expression2DReverseSelectedModelGroup.add(expression2DReverseCurrentModelRButton_);
		expression2DReverseSelectedModelGroup.add(expression2DReverseOtherModelRButton_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Links the sliders of the interface to the associated JSpinner. */
	private void setSliders() {
		
		SpinnerSliderCoupler coupler = null;
		
		// ---------- EXPRESSION 1D TRANSLATION ----------		
		try {
			coupler = new SpinnerSliderCoupler(expression1DTranslationSpinner_, expression1DTranslationSlider_);
			coupler.initialize(new Double(String.valueOf(expression1DTranslationModel_.getValue())).doubleValue(),
					new Double(String.valueOf(expression1DTranslationModel_.getMinimum())).doubleValue(),
					new Double(String.valueOf(expression1DTranslationModel_.getMaximum())).doubleValue(),
					new Double(String.valueOf(expression1DTranslationModel_.getStepSize())).doubleValue());
			coupler.couple();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ---------- EXPRESSION 1D SIGMA ----------
		try {
			coupler = new SpinnerSliderCoupler(expression1DSigmaSpinner_, expression1DSigmaSlider_);
			coupler.initialize(new Double(String.valueOf(expression1DSigmaModel_.getValue())).doubleValue(),
					new Double(String.valueOf(expression1DSigmaModel_.getMinimum())).doubleValue(),
					new Double(String.valueOf(expression1DSigmaModel_.getMaximum())).doubleValue(),
					new Double(String.valueOf(expression1DSigmaModel_.getStepSize())).doubleValue());
			coupler.couple();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ---------- EXPRESSION 2D BOUNDARY CONSERVED ----------
		try {
			SpinnerNumberModel model = (SpinnerNumberModel) expression2DThresholdSpinner_.getModel();
			coupler = new SpinnerSliderCoupler(expression2DThresholdSpinner_, expression2DThresholdSlider_);
			coupler.initialize(new Double(String.valueOf(model.getValue())).doubleValue(),
					new Double(String.valueOf(model.getMinimum())).doubleValue(),
					new Double(String.valueOf(model.getMaximum())).doubleValue(),
					new Double(String.valueOf(model.getStepSize())).doubleValue());
			coupler.couple();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ---------- EXPRESSION 2D STITCHING SMOOTHING RANGE ----------
		try {
			SpinnerNumberModel model = (SpinnerNumberModel) expression2DStitchingSmoothingRangeSpinner_.getModel();
			coupler = new SpinnerSliderCoupler(expression2DStitchingSmoothingRangeSpinner_, expression2DStitchingSmoothingRangeSlider_);
			coupler.initialize(new Double(String.valueOf(model.getValue())).doubleValue(),
					new Double(String.valueOf(model.getMinimum())).doubleValue(),
					new Double(String.valueOf(model.getMaximum())).doubleValue(),
					new Double(String.valueOf(model.getStepSize())).doubleValue());
			coupler.couple();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------

   	/** Called when leaving WingJ (reset WingJ and display a signature in the console). */
   	private void exitWingJ() throws Exception {
   		
   		reset();
   		
   		WJSettings.log("\n");
   		WJSettings.log("Project website: http://wingj.org");
   		WJSettings.log("Copyright (c) 2011-2013 Thomas Schaffter & Ricard Delgado-Gonzalo");
   		setVisible(false);
   	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Pops up a dialog to ask user if he want to run a new structure detection. */
	protected boolean runNewStructureDetection() {

		String message = "Start new structure detection?";
		int reply = JOptionPane.showConfirmDialog(this, message, "Structure detection", JOptionPane.YES_NO_OPTION);
		return (reply == JOptionPane.YES_OPTION);
	}
	
	// ----------------------------------------------------------------------------
	
	protected void showMainPanel() { contentLayout_.show(getContentPane(), "MAIN"); }
	protected void showSettingsPanel() { contentLayout_.show(getContentPane(), "SETTINGS"); }
	protected void showStructurePanel() { contentLayout_.show(getContentPane(), "STRUCTURE"); }
	protected void showBatchPanel() { contentLayout_.show(getContentPane(), "BATCH"); }
	protected void showExpressionPanel() { contentLayout_.show(getContentPane(), "EXPRESSION"); }
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Updates the interface when the dimension of the selected expression dataset has been modified.
	 * <p>
	 * If 1D expression dataset is selected, the corresponding box is set visible and the 2D expression
	 * dataset box is hidden. The opposite operation is performed when the 2D expression dataset is
	 * selected.
	 */
	protected void updateDatasetDimension() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		gui2settings();
		
		// replace the occurent "unit" by the current unit string
    	String strToUpdate = expression1DResolutionDynamicRButton_.getText().replaceAll("unit", settings.getUnit());
    	expression1DResolutionDynamicRButton_.setText(strToUpdate);
    	strToUpdate = expression1DResolutionDynamicUnit_.getText().replaceAll("unit", settings.getUnit());
    	expression1DResolutionDynamicUnit_.setText(strToUpdate);
		
		int dimension = expressionDimensionDatasetCBox_.getSelectedIndex();
		if (dimension == WJSettings.EXPRESSION_DATASET_1D) {

			expression1DPanel_.setVisible(true);
			expression2DPanel_.setVisible(false);
			expression2DReversePanel_.setVisible(false);
			expression2DAggregatedPanel_.setVisible(false);
			expressionCompositePanel_.setVisible(false);
			
			expressionSelectedChannelCBox_.setEnabled(true);
			expressionDatasetShowButton_.setText("Quantify");
			expressionNormalizedCBox_.setEnabled(true);
		}
		else if (dimension == WJSettings.EXPRESSION_DATASET_2D) {
			
			expression1DPanel_.setVisible(false);
			expression2DPanel_.setVisible(true);
			expression2DReversePanel_.setVisible(false);
			expression2DAggregatedPanel_.setVisible(false);
			expressionCompositePanel_.setVisible(false);
			
			expressionSelectedChannelCBox_.setEnabled(true);
			expressionDatasetShowButton_.setText("Quantify");
			expressionNormalizedCBox_.setEnabled(true);
		}
		else if (dimension == WJSettings.EXPRESSION_DATASET_2D_REVERSE) {
			
			expression1DPanel_.setVisible(false);
			expression2DPanel_.setVisible(false);
			expression2DReversePanel_.setVisible(true);
			expression2DAggregatedPanel_.setVisible(false);
			expressionCompositePanel_.setVisible(false);
			
			expressionSelectedChannelCBox_.setEnabled(false);
			expressionDatasetShowButton_.setText("Generate");
			expressionNormalizedCBox_.setEnabled(false);
		}
		else if (dimension == WJSettings.EXPRESSION_DATASET_2D_AGGREGATED) {
			
			expression1DPanel_.setVisible(false);
			expression2DPanel_.setVisible(false);
			expression2DReversePanel_.setVisible(false);
			expression2DAggregatedPanel_.setVisible(true);
			expressionCompositePanel_.setVisible(false);
			
			expressionSelectedChannelCBox_.setEnabled(false);
			expressionDatasetShowButton_.setText("Generate");
			expressionNormalizedCBox_.setEnabled(false);
		}
		else if (dimension == WJSettings.EXPRESSION_DATASET_COMPOSITE) {
			
			expression1DPanel_.setVisible(false);
			expression2DPanel_.setVisible(false);
			expression2DReversePanel_.setVisible(false);
			expression2DAggregatedPanel_.setVisible(false);
			expressionCompositePanel_.setVisible(true);

			expressionSelectedChannelCBox_.setEnabled(false);
			expressionDatasetShowButton_.setText("Generate");
			expressionNormalizedCBox_.setEnabled(false);
		}
		else
			WJSettings.log("ERROR: Invalid expression dataset dimension.");
		
		settings2gui();
	}
	
	//----------------------------------------------------------------------------
	
	/** Prints the given String content to the settings text pane. */
	protected void printSettingsContent(String data) throws BadLocationException {
		
		doc_.remove(0, doc_.getLength()); // clean the content of the settings window
		doc_.insertString(0, data, style_); // insert the content of the settings file loaded
	}
	
	//----------------------------------------------------------------------------
	
	/** Prints the given String content to the settings text pane. */
	protected void printStructureDatasetContent(String data) throws BadLocationException {
		
		structureDatasetDoc_.remove(0, structureDatasetDoc_.getLength()); // clean the content
		structureDatasetDoc_.insertString(0, data, structureDatasetStyle_); // insert the content
		structureDatasetTextPane_.setCaretPosition(0);
	}
	
	//----------------------------------------------------------------------------
	
	/** Displays the content of the last settings file loaded. */
	protected void updateSettingsContent(boolean fromFile) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		try {
			if (fromFile)
				printSettingsContent(settings.loadSettingsContent());
			else
				printSettingsContent(WJSettings.getInstance().settings2String());
			
			settingsTextPane_.setCaretPosition(SETTINGS_CARET_POSITION);
		} catch (BadLocationException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			throw e;
		} catch (IOException e) {
			printSettingsContent("Unable to display settings file content, see console for details.");
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Gets content of settings text pane and apply. */
	protected void applySettings() throws Exception {
		
		// get the content of the settings pane
		String data = doc_.getText(0, doc_.getLength());
		
		// for the patch on Windows
		data = data.replace("\\", "/");
		
		SETTINGS_CARET_POSITION = settingsTextPane_.getCaretPosition();
		InputStream is = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
		WJSettings.getInstance().loadSettings(is);
		settings2gui(); // update GUI content
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the selected intensity projection method for the given channel. */
	protected int getProjectionMethod(int channel) {
		
		if (channel == 0 && gene0ProjectionMaxRButton_.isSelected()) return Projections.PROJECTION_MAX_METHOD;
		if (channel == 0 && gene0ProjectionMeanRButton_.isSelected()) return Projections.PROJECTION_MEAN_METHOD;
		if (channel == 1 && gene1ProjectionMaxRButton_.isSelected()) return Projections.PROJECTION_MAX_METHOD;
		if (channel == 1 && gene1ProjectionMeanRButton_.isSelected()) return Projections.PROJECTION_MEAN_METHOD;
		if (channel == 2 && gene2ProjectionMaxRButton_.isSelected()) return Projections.PROJECTION_MAX_METHOD;
		if (channel == 2 && gene2ProjectionMeanRButton_.isSelected()) return Projections.PROJECTION_MEAN_METHOD;
		if (channel == 3 && gene3ProjectionMaxRButton_.isSelected()) return Projections.PROJECTION_MAX_METHOD;
		if (channel == 3 && gene3ProjectionMeanRButton_.isSelected()) return Projections.PROJECTION_MEAN_METHOD;
		return -1;
	}
	
	//----------------------------------------------------------------------------
	
	/** Updates the content of the RGB combobox from expression panel. */
	protected void updateExpressionRgbCBox() throws Exception {
		
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
		
		for (int i = 0; i < selectedChannelIndexes.length; i++)
			WJImages.colorChannelIndex_.set(i, selectedChannelIndexes[i]); // i gives the color
		
////    	int selectedGene = expressionSelectedChannelCBox_.getSelectedIndex();
//		int selectedGene = getSelectedExpressionChannelIndex();
//    	int newColorIndex = expressionColorChannelCBox_.getSelectedIndex();
//    	int oldColorIndex = WJImages.colorChannelIndex_.get(selectedGene);
//    	if (newColorIndex == oldColorIndex) return; // nothing to do
//    	// switch the color channel of two genes
//    	int otherGene = WJImages.colorChannelIndex_.indexOf(newColorIndex);
//    	WJImages.colorChannelIndex_.set(otherGene, oldColorIndex);
//    	WJImages.colorChannelIndex_.set(selectedGene, newColorIndex);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Updates min and max slice index spinners. */
	protected void setMinMaxSliceIndexSpinners(int channel) {
		
		JSpinner smin = minSliceIndexes_.get(channel);
		JSpinner smax = maxSliceIndexes_.get(channel);
		smin.removeChangeListener(this);
		smax.removeChangeListener(this);
		
		// current value, min, max, step size
		SpinnerModel minModel = new SpinnerNumberModel(1, 1, (int) WJImages.numSlices_[channel], 1);
		SpinnerModel maxModel = new SpinnerNumberModel((int) WJImages.numSlices_[channel], 1, (int) WJImages.numSlices_[channel], 1);
		smin.setModel(minModel);
		smax.setModel(maxModel);
		
		smin.addChangeListener(this);
		smax.addChangeListener(this);
	}
	
	//----------------------------------------------------------------------------
	
	/** Updates the controls in the expression panel. */
	protected void updateChannelPanel(int channel) throws Exception {
		
//		int index = expressionSelectedChannelCBox_.getSelectedIndex();
		setMinMaxSliceIndexSpinners(channel);
//		updateExpressionPanel(index);
	}
	
	//----------------------------------------------------------------------------
	
	/** Updates the controls in the expression panel. */
	protected void updateExpressionPanel(int channel) throws Exception {
		
		// set the correct channel
		expressionSelectedChannelCBox_.removeActionListener(this);
		expressionSelectedChannelCBox_.setSelectedIndex(channel);
		expressionSelectedChannelCBox_.addActionListener(this);
//		expressionColorChannelCBox_.removeActionListener(this);
//		expressionColorChannelCBox_.setSelectedIndex(WJImages.colorChannelIndex_.get(channel));
//		expressionColorChannelCBox_.addActionListener(this);
		
		// community expression map
//		expressionAggProjectionTField_.setText(getSelectedChannelName() + ExpressionDataset2D.EXPRESSION_PROJECTION_SUFFIX);
	}
	
	//----------------------------------------------------------------------------
	
	/** Opens a dialog to select the folder containing the experiments. */
	protected String selectBatchDirectory() throws Exception {
		
		try {
	    	JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Select batch directory");
	     	setAppIcon(frame);
	     	
	     	// Set the current directory to the working directory
	     	WJSettings settings = WJSettings.getInstance();
	     	File f = new File(new File(settings.getBatchRootDirectory()).getCanonicalPath());
	     	fc.setCurrentDirectory(f);
	     	
	     	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	     	int returnVal = fc.showDialog(frame, "Ok");
	     	if (returnVal == JFileChooser.APPROVE_OPTION) {
	     		File file = fc.getSelectedFile();
	     		return new String(file.getAbsolutePath() + "/");
	     	}
		} catch (Exception e) {
			throw new Exception(e);
		}
		
		return null;
	}
	
	//----------------------------------------------------------------------------
	
	/** Initializes the controllers of the structure panel. */
	protected void initializeStructurePanel() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// adds images to the background images collection
		backgroundCBox_.removeActionListener(this);
		backgroundCBox_.removeAllItems();
		
		// updates number of control points (e.g. required after loading a structure)
		numStructureControlPoints_.setValue(settings.getNumStructureControlPoints());
		
		// fills the combobox
		ImagePlus img = null;
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			img = WJImages.getImageStack(i);
			if (img == null || img.getProcessor() == null)
				continue; // so there is no stack nor projection
			backgroundCBox_.addItem(i + " " + settings.getGeneName(i) + " stack"); // add stack
			img = WJImages.getImageProjection(i); // as there is a stack, the projection will be computed if not already done
			backgroundCBox_.addItem(i + " " + settings.getGeneName(i) + " projection"); // add stack
		}
		
		// set the default background
		// structureBgImgBkp_ is reseted when calling WingJ.reset()
		if (structureBgImgBkp_ == -1) {
			// select the projection of the structure channel
			int structureChannel = settings.getStructureChannelIndex();
			String str = null;
			for (int i = 0; i < backgroundCBox_.getItemCount(); i++) {
				str = (String)backgroundCBox_.getItemAt(i);
				if (str.startsWith(String.valueOf(structureChannel)) && str.endsWith("projection")) {
					structureBgImgBkp_ = i;
					backgroundCBox_.setSelectedIndex(i);
					break;
				}
			}
		} else {
			try {
				// generates a out of bounds exception if selected backgroup data have been deleted
				backgroundCBox_.setSelectedIndex(structureBgImgBkp_);
			} catch (Exception e) {
				if (backgroundCBox_.getItemCount() > 0)
					backgroundCBox_.setSelectedIndex(0);
				else
					throw new Exception("WARNING: At least one image or image stack is required.");
			}
			
		}
			
		backgroundCBox_.addActionListener(this);
		
		updateStructureMeasurementsDisplayed(); // XXX
	}
	
	// ----------------------------------------------------------------------------
	
	/** Updates the data displayed for the structure measurements. */
	protected void updateStructureMeasurementsDisplayed() throws Exception {

		try {
			String dataset = WingJ.getInstance().getSystem().getStructureDetector().getStructure().getReadableStructureDataset();
			printStructureDatasetContent(dataset);
		} catch (Exception e) {
			printStructureDatasetContent("Unable to display structure dataset, see console for details.");
			throw e;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to select the output directory and set it in WJSettings. */
	protected void setOutputDirectory() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		try {
	    	JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Select output directory");
	     	setAppIcon(frame);
	     	 
	     	// Set the current directory to the working directory
	     	File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	     	fc.setCurrentDirectory(f);
	     	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	     	
	     	int returnVal = fc.showOpenDialog(frame);
	     	if (returnVal == JFileChooser.APPROVE_OPTION) {
	     		// Add system-dependent path separator (if required)
	     		String directory = fc.getSelectedFile().getPath();
	     		if (!directory.endsWith(File.separator))
	     			directory += "/";
	     		directory = directory.replace("\\", "/");
	     		settings.setOutputDirectory(directory);
	     		outputTField_.setText(directory);
	     	}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to select the root experiment directory. */
	protected void setRootExperimentDirectory() throws Exception {
		
//		WJSettings settings = WJSettings.getInstance();
		
		try {
	    	JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Select root experiment directory");
	     	setAppIcon(frame);
	     	 
	     	// Set the current directory to the working directory
//	     	File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	     	File f = new File(new File(expressionAggRootTField_.getText()).getCanonicalPath());
	     	fc.setCurrentDirectory(f);
	     	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	     	
	     	int returnVal = fc.showOpenDialog(frame);
	     	if (returnVal == JFileChooser.APPROVE_OPTION) {
	     		// Add system-dependent path separator (if required)
	     		String directory = fc.getSelectedFile().getPath();
	     		if (!directory.endsWith(File.separator))
	     			directory += File.separator;
	     		expressionAggRootTField_.setText(directory);
	     	}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to select the structure model file for reversing an individual expression map. */
	public void setExpression2DReverseStructureFile() throws Exception {
		
		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			setAppIcon(frame);
			
			WJSettings settings = WJSettings.getInstance();
			
			// the directory to look is the current one
			String dir = FilenameUtils.getDirectory(expression2DReverseStructureTField_.getText());
			if (dir == null)
				dir = settings.getOutputDirectory();
			

			fc.setDialogTitle("Select structure model");
			URI uri = FileUtils.getFileURI(dir);
			File f = new File(FileUtils.getFileURI(new File(uri).getCanonicalPath()));
			fc.setCurrentDirectory(f);
			fc.addChoosableFileFilter(new FilterStructureXml());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fc.showDialog(frame, "Select");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				expression2DReverseStructureTField_.setText(file.getAbsolutePath());
			}
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to select the circular expression map for reversing an individual expression map. */
	public void setExpression2DReverseCircularMapeFile() throws Exception {
		
		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			setAppIcon(frame);
			
			WJSettings settings = WJSettings.getInstance();
			
			// the directory to look is the current one
			String dir = FilenameUtils.getDirectory(expression2DReverseMapTField_.getText());
			if (dir == null)
				dir = settings.getOutputDirectory();
			

			fc.setDialogTitle("Select circular expression map");
			URI uri = FileUtils.getFileURI(dir);
			File f = new File(FileUtils.getFileURI(new File(uri).getCanonicalPath()));
			fc.setCurrentDirectory(f);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int returnVal = fc.showDialog(frame, "Select");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				expression2DReverseMapTField_.setText(file.getAbsolutePath());
			}
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}

	//----------------------------------------------------------------------------

	/** Opens a dialog to specify where to save the settings file and save it. */
	protected void saveSettings() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		//Event in selecting path for saving the output
    	JFrame frame = new JFrame();
     	frame.setAlwaysOnTop(true);
    	JFileChooser fc = new JFileChooser();
     	fc.setDialogTitle("Save settings");
     	fc.setApproveButtonText("Save");
     	setAppIcon(frame);
     	fc.setSelectedFile(new File(settings.getExperimentName() + "_settings.txt"));
     	 
     	// Set the current directory to the working directory
     	File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
     	fc.setCurrentDirectory(f);
     	fc.setDialogType(JFileChooser.SAVE_DIALOG);
 
     	int returnVal = fc.showSaveDialog(frame);
     	if (returnVal == JFileChooser.APPROVE_OPTION)
			saveSettings(fc.getSelectedFile().toURI());
	}
	
	//----------------------------------------------------------------------------

	/** Saves settings to file. */
	protected void saveSettings(URI uri) throws Exception {

		applySettings();
		WJSettings.getInstance().saveSettings(uri);
	}
	
	//----------------------------------------------------------------------------

	/** Opens a dialog to specify to select the settings file to load and load it. */
	protected void loadSettings() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		//Event in selecting path for saving the output
    	JFrame frame = new JFrame();
     	frame.setAlwaysOnTop(true);
    	JFileChooser fc = new JFileChooser();
     	fc.setDialogTitle("Opening settings");
     	fc.setApproveButtonText("Open");
     	setAppIcon(frame);
     	 
     	// Set the current directory to the working directory
     	File f = new File(new File(settings.getWorkingDirectory()).getCanonicalPath());
     	fc.setCurrentDirectory(f);    	 
     	fc.setDialogType(JFileChooser.OPEN_DIALOG);
     	
     	int returnVal = fc.showOpenDialog(frame);
     	if (returnVal == JFileChooser.APPROVE_OPTION) {
     		settings.loadSettings(fc.getSelectedFile().toURI());
     		settings2gui();
     	}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets a JLabel to show the current image scale. */
	protected void setScaleLabel() {
		
		WJSettings settings = WJSettings.getInstance();
		scaleLabel_.setText("1 px = " + (new DecimalFormat("#.###")).format(settings.getScale()) + " " + settings.getUnit());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the tooltip delay, etc. */
	protected static void setToolTipManagerPreferences() {
		
		ToolTipManager.sharedInstance().setInitialDelay(1200);
		ToolTipManager.sharedInstance().setReshowDelay(100);
		ToolTipManager.sharedInstance().setDismissDelay(20000);
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WJGuiPlus() {
		
		WJSettings settings = WJSettings.getInstance();
		
		// IMPORTANT: initialize the GUI controls before adding action listeners to them
		settings2gui();
		
		this.setTitle(settings.getAppName() + " " + settings.getAppVersion());
		
		setAppIcon(this);
		setSliders();
		setRadioButtonGroups();
		setWaitingSnakeVisible(false);
		setExpressionWaitingSnakeVisible(false);
		doc_ = (StyledDocument) settingsTextPane_.getDocument();
		style_ = doc_.addStyle("settingsStyle", null);
		structureDatasetDoc_ = (StyledDocument) structureDatasetTextPane_.getDocument();
		structureDatasetStyle_ = structureDatasetDoc_.addStyle("structureDatasetStyle", null);
		
		initializeListeners();
		
		pack();
		// set the minimum size to the pack size
		setMinimumSize(getPreferredSize());
		
		// set apptips after pack so that messages fit the app width
		initializeAppTips();
		
		setToolTipManagerPreferences();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the waiting snake visible (when a process is running). */
	public void setWaitingSnakeVisible(boolean b) {
		
		if (b) {
			snake_.setVisible(true);
			snake_.start();
			snakeLayout_.show(snakePanel_, "card_snake");
		}
		else {
			snake_.setVisible(false);
			snake_.stop();
			snakeLayout_.show(snakePanel_, "card_decoy");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the waiting snake visible when generating expression dataset (when a process is running). */
	public void setExpressionWaitingSnakeVisible(boolean b) {
		
		if (b) {
			expressionSnake_.setVisible(true);
			expressionSnake_.start();
			expressionSnakeLayout_.show(expressionSnakePanel_, "card_snake");
		}
		else {
			expressionSnake_.setVisible(false);
			expressionSnake_.stop();
			expressionSnakeLayout_.show(expressionSnakePanel_, "card_decoy");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Locks the GUI when processes are running. */
	public void setGuiEnabled(boolean e, boolean detectionPauseButton) {
		
		outputTField_.setEnabled(e);
		outputButton_.setEnabled(e);
		setSettingsButton_.setEnabled(e);
		systemCBox_.setEnabled(e);
		
		batchButton_.setEnabled(e);
		batchAcceptButton_.setEnabled(e);
		batchSkipButton_.setEnabled(e);
		
		for (int i = 0; i < structureChannelIndexCBoxes_.length; i++)
			structureChannelIndexCBoxes_[i].setEnabled(e);
		
		nameTField_.setEnabled(e);
		ch00NameTField_.setEnabled(e);
		ch01NameTField_.setEnabled(e);
		ch02NameTField_.setEnabled(e);
		ch03NameTField_.setEnabled(e);
		ch00DirectoryButton_.setEnabled(e);
		ch01DirectoryButton_.setEnabled(e);
		ch02DirectoryButton_.setEnabled(e);
		ch03DirectoryButton_.setEnabled(e);
		maskButton_.setEnabled(e);
		resetButton_.setEnabled(e);
		gene0ProjectionMeanRButton_.setEnabled(e);
		gene0ProjectionMaxRButton_.setEnabled(e);
		gene1ProjectionMeanRButton_.setEnabled(e);
		gene1ProjectionMaxRButton_.setEnabled(e);
		gene2ProjectionMeanRButton_.setEnabled(e);
		gene2ProjectionMaxRButton_.setEnabled(e);
		gene3ProjectionMeanRButton_.setEnabled(e);
		gene3ProjectionMaxRButton_.setEnabled(e);
		for (JSpinner s : minSliceIndexes_)
			s.setEnabled(e);
		for (JSpinner s : maxSliceIndexes_)
			s.setEnabled(e);
		
		boolean e2 = e;
		WJSystem system = WJSystemManager.getInstance().getSystem(getSelectedSystemId());
		if (system != null)
			e2 = e && system.providesUnsupervisedStructureDetection();
			
		preprocessingScanButton_.setEnabled(e2);
		detectionRunAllButton_.setEnabled(e2);
		detectionResumeButton_.setEnabled(e2);
		detectionStepButton_.setEnabled(e2);
		detectionRedoStepButton_.setEnabled(e2);
		
		// special cases
		detectionPauseButton_.setEnabled(e2 && detectionPauseButton);
		
		detectionAbortButton_.setEnabled(e);
		detectionManualButton_.setEnabled(e);
		detectionLoadButton_.setEnabled(e);
		detectionStructureButton_.setEnabled(e);
		
		expression1DBoundaryCBox_.setEnabled(e);
		expression1DTranslationSlider_.setEnabled(e);
		expression1DTranslationSpinner_.setEnabled(e);
		expression1DSigmaSlider_.setEnabled(e);
		expression1DSigmaSpinner_.setEnabled(e);
		
		expressionSelectedChannelCBox_.setEnabled(e);
		expressionDimensionDatasetCBox_.setEnabled(e);
		expressionDatasetShowButton_.setEnabled(e);
		expressionDatasetHideButton_.setEnabled(e);
		expressionExportDatasetButton_.setEnabled(e);
		
		expressionBackButton_.setEnabled(e);
		quantifyExpressionButton_.setEnabled(e);
		
		swapBoundariesButton_.setEnabled(e);
		reverseDVBoundaryButton_.setEnabled(e);
		reverseAPBoundaryButton_.setEnabled(e);
		editStructureButton_.setEnabled(e);
		
		numStructureControlPoints_.setEnabled(e);
		backgroundCBox_.setEnabled(e);
		
		showOverlayInformationCBox_.setEnabled(e);
		showOverlayStructureCBox_.setEnabled(e);
		setColorButton_.setEnabled(e);
		
		expression1DResolutionConstantRButton_.setEnabled(e);
		expression1DResolutionDynamicRButton_.setEnabled(e);
		expression1DNumPointsSpinner_.setEnabled(e);
		expression1DNumPointsPerUnitSpinner_.setEnabled(e);
		
		saveStructureButton_.setEnabled(e);
		exportStructurePropertiesButton_.setEnabled(e);
		exportBinaryMaskButton_.setEnabled(e);
		exportPreviewImageButton_.setEnabled(e);
		exportAllButton_.setEnabled(e);
		structureCloseButton_.setEnabled(e);
		
		batchAcceptButton_.setEnabled(e);
		batchSkipButton_.setEnabled(e);
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves values of GUI to WJSettings. */
	public void gui2settings() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// kind of a good place to do that
		WJImages.update();
		
		// ============================================================================
		// INPUT AND OUTPUT
		
		settings.setExperimentName(nameTField_.getText());
		settings.setGeneNames(ch00NameTField_.getText(), 0);
		settings.setGeneNames(ch01NameTField_.getText(), 1);
		settings.setGeneNames(ch02NameTField_.getText(), 2);
		settings.setGeneNames(ch03NameTField_.getText(), 3);
		settings.setOutputDirectory(outputTField_.getText());
		settings.setBatchRootDirectory(batchDirectoryTField_.getText());
		
		// ============================================================================
		// FLUORESCENT CONFOCAL IMAGES
		
		for (int i = 0; i < structureChannelIndexCBoxes_.length; i++) {
			if (structureChannelIndexCBoxes_[i].isSelected()) {
				settings.setStructureChannelIndex(i);
				break;
			}
		}
		
		if (gene0ProjectionMaxRButton_.isSelected()) settings.setChannelProjectionMethod(0, Projections.PROJECTION_MAX_METHOD);
		else if (gene0ProjectionMeanRButton_.isSelected()) settings.setChannelProjectionMethod(0, Projections.PROJECTION_MEAN_METHOD);
		if (gene1ProjectionMaxRButton_.isSelected()) settings.setChannelProjectionMethod(1, Projections.PROJECTION_MAX_METHOD);
		else if (gene1ProjectionMeanRButton_.isSelected()) settings.setChannelProjectionMethod(1, Projections.PROJECTION_MEAN_METHOD);
		if (gene2ProjectionMaxRButton_.isSelected()) settings.setChannelProjectionMethod(2, Projections.PROJECTION_MAX_METHOD);
		else if (gene2ProjectionMeanRButton_.isSelected()) settings.setChannelProjectionMethod(2, Projections.PROJECTION_MEAN_METHOD);
		if (gene3ProjectionMaxRButton_.isSelected()) settings.setChannelProjectionMethod(3, Projections.PROJECTION_MAX_METHOD);
		else if (gene3ProjectionMeanRButton_.isSelected()) settings.setChannelProjectionMethod(3, Projections.PROJECTION_MEAN_METHOD);
		
		// ============================================================================
		// PRE-PROCESSING
	    
//		settings.setPpThreshold(ppThldModel_.getNumber().intValue());
//		settings.setPpBlur(ppBlurModel_.getNumber().intValue());
		
		// ============================================================================
		// STRUCTURE
		
		settings.setNumStructureControlPoints((Integer)numStructureControlPoints_.getModel().getValue());
		
		// ============================================================================
		// 1D EXPRESSION
		
		settings.normalizeExpression(expressionNormalizedCBox_.isSelected());
		
		settings.setExpression1DBoundary(expression1DBoundaryCBox_.getSelectedIndex());
		settings.setExpression1DTranslation((Double)expression1DTranslationSpinner_.getModel().getValue());
		settings.setExpression1DSigma(expression1DSigmaModel_.getNumber().doubleValue());
		
		if (expression1DResolutionConstantRButton_.isSelected())
			settings.setExpression1DResolutionStrategy(WJSettings.EXPRESSION_1D_RESOLUTION_CONSTANT);
		else if (expression1DResolutionDynamicRButton_.isSelected())
			settings.setExpression1DResolutionStrategy(WJSettings.EXPRESSION_1D_RESOLUTION_DYNAMIC);
		settings.setExpression1DNumPoints((Integer) expression1DNumPointsSpinner_.getModel().getValue());
		settings.setExpression1DStepSize((Double) expression1DNumPointsPerUnitSpinner_.getModel().getValue());
		
		// ============================================================================
		// 2D EXPRESSION
		
		settings.setExpression2DStitchingDensityDifferenceThld((Double) expression2DThresholdSpinner_.getModel().getValue());
		settings.setExpression2DStitchingSmoothingRange((Double) expression2DStitchingSmoothingRangeSpinner_.getModel().getValue());
		
		int dataset = getSelectedExpressionDatasetDimension();
		if (dataset == WJSettings.EXPRESSION_DATASET_2D) {
			settings.setExpression2DNumPoints((Integer) expression2DNumPointsSpinner_.getModel().getValue());
		} else if (dataset == WJSettings.EXPRESSION_DATASET_2D_AGGREGATED) {
			settings.setExpression2DNumPoints((Integer) expression2DAggResolutionSpinner_.getModel().getValue());
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Updates the GUI with values in WJSettings. */
	public void settings2gui() {
		
		WJSettings settings = WJSettings.getInstance();
		
		// ============================================================================
		// INPUT AND OUTPUT
		
		nameTField_.setText(settings.getExperimentName());
		outputTField_.setText(settings.getOutputDirectory());
		batchDirectoryTField_.setText(settings.getBatchRootDirectory());
		batchExperimentPanel_.setVisible(settings.getShowBatchExperimentPanel());
		
		// ============================================================================
		// FLUORESCENT CONFOCAL IMAGES
		
		ch00NameTField_.setText(settings.getGeneName(0));
		ch01NameTField_.setText(settings.getGeneName(1));
		ch02NameTField_.setText(settings.getGeneName(2));
		ch03NameTField_.setText(settings.getGeneName(3));
		
		// image stack: min slice indexes
		for (int i = 0; i < minSliceIndexes_.size(); i++) {
			JSpinner s = minSliceIndexes_.get(i);
			s.removeChangeListener(this);
			if (WJImages.numSlices_[i] == 0) // channel not open
				s.setModel(new SpinnerNumberModel(0, 0, 0, 1));
			else
				s.setModel(new SpinnerNumberModel(new Integer(WJImages.firstSlicesIndex_[i]).intValue(),
						new Integer(WJImages.numSlices_[i] > 0 ? 1 : 0).intValue(), 
						new Integer(WJImages.numSlices_[i]).intValue(),
						1));
			s.addChangeListener(this);
		}
		// image stacks: max slice indexes
		for (int i = 0; i < maxSliceIndexes_.size(); i++) {
			JSpinner s = maxSliceIndexes_.get(i);
			s.removeChangeListener(this);
			if (WJImages.numSlices_[i] == 0) // channel not open
				s.setModel(new SpinnerNumberModel(0, 0, 0, 1));
			else
				s.setModel(new SpinnerNumberModel(new Integer(WJImages.lastSlicesIndex_[i]).intValue(),
						new Integer(WJImages.numSlices_[i] > 0 ? 1 : 0).intValue(), 
						new Integer(WJImages.numSlices_[i]).intValue(),
						1));
			s.addChangeListener(this);
		}
		
		structureChannelIndexCBoxes_[settings.getStructureChannelIndex()].setSelected(true);
		
		switch (settings.getChannelProjectionMethod(0)) {
			case Projections.PROJECTION_MAX_METHOD: gene0ProjectionMaxRButton_.setSelected(true); break;
			case Projections.PROJECTION_MEAN_METHOD: gene0ProjectionMeanRButton_.setSelected(true); break;
		}
		switch (settings.getChannelProjectionMethod(1)) {
			case Projections.PROJECTION_MAX_METHOD: gene1ProjectionMaxRButton_.setSelected(true); break;
			case Projections.PROJECTION_MEAN_METHOD: gene1ProjectionMeanRButton_.setSelected(true); break;
		}
		switch (settings.getChannelProjectionMethod(2)) {
			case Projections.PROJECTION_MAX_METHOD: gene2ProjectionMaxRButton_.setSelected(true); break;
			case Projections.PROJECTION_MEAN_METHOD: gene2ProjectionMeanRButton_.setSelected(true); break;
		}
		switch (settings.getChannelProjectionMethod(3)) {
			case Projections.PROJECTION_MAX_METHOD: gene3ProjectionMaxRButton_.setSelected(true); break;
			case Projections.PROJECTION_MEAN_METHOD: gene3ProjectionMeanRButton_.setSelected(true); break;
		}
		
		setScaleLabel();
		
		// ============================================================================
		// PRE-PROCESSING
		
		// ===========================================================================
		// SEGMENTATION
		
		// ============================================================================
		// STRUCTURE
		
		numStructureControlPoints_.setValue(settings.getNumStructureControlPoints());
	    
		// ============================================================================
		// 1D EXPRESSION
		
		expressionNormalizedCBox_.setSelected(settings.normalizeExpression());
		
		expression1DBoundaryCBox_.setSelectedIndex(settings.getExpression1DBoundary());
		expression1DSigmaModel_.setValue(settings.getExpression1DSigma());
		expression1DTranslationModel_.setValue(settings.getExpression1DTranslation());
		
		if (settings.getExpression1DResolutionStrategy() == WJSettings.EXPRESSION_1D_RESOLUTION_CONSTANT)
			expression1DResolutionConstantRButton_.setSelected(true);
		else if (settings.getExpression1DResolutionStrategy() == WJSettings.EXPRESSION_1D_RESOLUTION_DYNAMIC)
			expression1DResolutionDynamicRButton_.setSelected(true);
		expression1DNumPointsSpinner_.getModel().setValue(settings.getExpression1DNumPoints());
		expression1DNumPointsPerUnitSpinner_.getModel().setValue(settings.getExpression1DStepSize());
		
		// ============================================================================
		// 2D EXPRESSION
		
		SpinnerNumberModel model = (SpinnerNumberModel) expression2DThresholdSpinner_.getModel();
		model.setValue(settings.getExpression2DStitchingDensityDifferenceThld());
		model = (SpinnerNumberModel) expression2DStitchingSmoothingRangeSpinner_.getModel();
		model.setValue(settings.getExpression2DStitchingSmoothingRange());
		model = (SpinnerNumberModel) expression2DNumPointsSpinner_.getModel();
		model.setValue(settings.getExpression2DNumPoints());
		
		model = (SpinnerNumberModel) expression2DAggResolutionSpinner_.getModel();
		model.setValue(settings.getExpression2DNumPoints());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the index of the selected gene/channel. */
	public int getSelectedExpressionChannelIndex() throws Exception {
		
		if (expressionSelectedChannelCBox_.getItemCount() < 1)
			throw new Exception("INFO: Single image or image stack required.");
		
		String selection = (String)expressionSelectedChannelCBox_.getSelectedItem();
		return Integer.parseInt(selection.substring(0, 1));
		//return expressionSelectedChannelCBox_.getSelectedIndex();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the name of the selected gene/channel. */
	public String getSelectedExpressionChannelName() throws Exception {
		
     	String channelName = "";
     	if (expressionSelectedChannelCBox_.getSelectedIndex() == 0) channelName = ch00NameTField_.getText();
     	else if (expressionSelectedChannelCBox_.getSelectedIndex() == 1) channelName = ch01NameTField_.getText();
     	else if (expressionSelectedChannelCBox_.getSelectedIndex() == 2) channelName = ch02NameTField_.getText();
     	else if (expressionSelectedChannelCBox_.getSelectedIndex() == 3) channelName = ch03NameTField_.getText();
     	else throw new Exception("ERROR: Invalid gene/channel index.");
     	return channelName;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the dimension of the selected expression dataset. */
	public int getSelectedExpressionDatasetDimension() throws Exception {
		
		return expressionDimensionDatasetCBox_.getSelectedIndex();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the id of the selected system (index of the selected item in the system combobox). */
	public int getSelectedSystemId() {
		
		return systemCBox_.getSelectedIndex();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the unique string id of the selected system (index of the selected item in the system combobox). */
	public String getSelectedSystemUniqueId() {
		
		return  WJSystemManager.getInstance().getSystem(systemCBox_.getSelectedIndex()).getUniqueId();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the index of the system to analyze. */
	public String getSelectedSystemName() {
		
		return (String)systemCBox_.getSelectedItem();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Displays the number perfect and ambigous individual experiments for community expression maps. */
	public static void setNumIndividualExperimentsForAggregatedExpression(int numPerfectMatches, int numAmbigousMatches) {
		
		String str = numPerfectMatches + " individual experiment";
		if (numPerfectMatches > 1)
			str += "s";
		str += " matching";
		if (numAmbigousMatches > 0) {
			str += " (+" + numAmbigousMatches + " ambigous result";
			if (numAmbigousMatches > 1)
				str += "s";
			str += ")";
		}
		
		WingJ wingj = WingJ.getInstance();
		wingj.expressionComSnake_.stop(); // in case it is running
		wingj.expressionComSelectionLabel_.setText(str);
		wingj.expressionComCardLayout_.show(wingj.expressionComSelectionPanel_, "CARD_INDIVIDUAL_EXPERIMENTS_LABEL");
	}
	
	//----------------------------------------------------------------------------
	
	/** Sets the application icon to the given window. */
	public static void setAppIcon(Window window) {
		
		try {
			window.setIconImages(WJSettings.getInstance().getAppIcon());
		} catch (Exception e) {
			// do nothing
		}
	}
	
	// ============================================================================
	// INNER CLASSES
	
	/** Lists individual experiments matching the input given in the community expression panel. */
	private class IndividualExperimentsDocumentListener implements DocumentListener {
	 
	    @Override
		public void insertUpdate(DocumentEvent event) {
	    	updateIndividualExperimentsSelectionForCommunityExpression();
	    }
	    @Override
		public void removeUpdate(DocumentEvent e) {
	    	updateIndividualExperimentsSelectionForCommunityExpression();
	    }
	    @Override
		public void changedUpdate(DocumentEvent e) {
	    	updateIndividualExperimentsSelectionForCommunityExpression();
	    }
	    
	    private void updateIndividualExperimentsSelectionForCommunityExpression() {
	    	
	    	try {
	    		String rootExperimentDirectory = expressionAggRootTField_.getText();
	    		String structuresRegex = expressionAggStructureTField_.getText();
	    		String projectionsRegex = expressionAggProjectionTField_.getText();
	    		
	    		// cancel the previous update (if any)
	    		if (WJExperimentsSelectionUpdater.instance_ != null)
	    			WJExperimentsSelectionUpdater.instance_.cancel(true);
	    		// run new update
	    		WJExperimentsSelectionUpdater.instance_ = new WJExperimentsSelectionUpdater(rootExperimentDirectory, structuresRegex, projectionsRegex);
	    		WJExperimentsSelectionUpdater.instance_.execute();
	    	} catch (Exception e) {
	    		WJSettings.log("Error while updating the networks selection.");
	    		e.printStackTrace();
	    		WingJ.setNumIndividualExperimentsForAggregatedExpression(0,0);
	    	}
	    }
	}
}
