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

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;

import java.awt.Component;
import javax.swing.Box;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import java.awt.CardLayout;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JCheckBox;
import javax.swing.JProgressBar;

import ch.tschaffter.apptips.AppTips;
import ch.tschaffter.gui.Snake;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;

/** 
 * GUI of WingJ.
 * <p>
 * Interface designed with WindowBuilder.
 * 
 * @version August 20, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJGui extends JFrame {

	/** Default serial version ID. */
	private static final long serialVersionUID = 1L;
	/** Browse confocal image ch00. */
	protected JButton ch00DirectoryButton_;
	/** Browse confocal images ch01. */
	protected JButton ch01DirectoryButton_;
	/** Browse confocal images ch02. */
	protected JButton ch02DirectoryButton_;
	/** Browse confocal images ch03. */
	protected JButton ch03DirectoryButton_;	
	/** Set mask around one structure. */
	protected JButton maskButton_;
	/** Browse output directory. */
	protected JButton outputButton_;
	/** Run a complete structure detection. */
	protected JButton detectionRunAllButton_;
	/** Cancel the running structure detection. */
	protected  JButton detectionPauseButton_;
	/** Resume the structure detection from where it is. */
	protected JButton detectionResumeButton_;
	/** Run only one step of the structure detection. */
	protected JButton detectionStepButton_;
	/** Redo the previous step of the structure detection. */
	protected JButton detectionRedoStepButton_;
	/** Run manual detection of the structure. */
	protected JButton detectionManualButton_;
	/** Show and edit the polarity of the identified structure structure. */
	protected JButton detectionStructureButton_;
	/** Reset the structure detector. */
	protected JButton detectionAbortButton_;
	/** Display WJ about dialog. */
	protected JButton aboutButton_;
	/** Close all images opened by WingJ. */
	protected JButton resetButton_;
	/** Display the settings panel. */
	protected JButton setSettingsButton_;
	/** Load structure from XML file. */
	protected JButton detectionLoadButton_;
	/** Open Batch mode. */
	protected JButton batchButton_;
	/** Go to expression panel. */
	protected JButton quantifyExpressionButton_;
	/** Pre-processing scan button. */
	protected JButton preprocessingScanButton_;
	
	/** Accept the current experiment and move to the next batch experiment. */
	protected JButton batchAcceptButton_;
	/** Skip the current experiment and move to the next one. */
	protected JButton batchSkipButton_;
	
	// JTextFields
	/** Structure name. */
	protected JTextField nameTField_;
	/** Directory to ch00 confocal images. */
	protected JTextField ch00NameTField_;
	/** Directory to ch01 confocal images. */
	protected JTextField ch01NameTField_;
	/** Directory to ch02 confocal images. */
	protected JTextField ch02NameTField_;
	/** Directory to ch03 confocal images. */
	protected JTextField ch03NameTField_;
	
	/** Output directory. */
	protected JTextField outputTField_;
	
	// Spinner models
    /** StructureSnake.M0. */
    protected SpinnerNumberModel m0Model_ = new SpinnerNumberModel(3, 3, 20, 1);
    /** Model for expression shift. */
    protected SpinnerNumberModel expression1DTranslationModel_ = new SpinnerNumberModel(0., -100., 100., 1.);
    /** Model for expression sigma.*/
    protected SpinnerNumberModel expression1DSigmaModel_ = new SpinnerNumberModel(10., 1., 100., 1.);
	
	// JRadioButtons
	/** Mean projection method for structure. */
	protected JRadioButton gene0ProjectionMeanRButton_;
	/** Max projection method for structure. */
	protected JRadioButton gene0ProjectionMaxRButton_;
	/** Mean projection method for gene1. */
	protected JRadioButton gene1ProjectionMeanRButton_;
	/** Max projection method for gene1. */
	protected JRadioButton gene1ProjectionMaxRButton_;
	/** Mean projection method for gene2. */
	protected JRadioButton gene2ProjectionMeanRButton_;
	/** Max projection method for gene2. */
	protected JRadioButton gene2ProjectionMaxRButton_;
	/** Mean projection method for gene3. */
	protected JRadioButton gene3ProjectionMeanRButton_;
	/** Max projection method for gene3. */
	protected JRadioButton gene3ProjectionMaxRButton_;
	
	// JLabel
	/** Image scale. */
	protected JLabel scaleLabel_;
	
	// Snake
	/** Snake to show when a process is running (name: wingj_snake). */
	protected Snake snake_;
	/** Snake panel. */
	protected JPanel snakePanel_;
	/** Layout controlling the appearance of the snake. */
	protected CardLayout snakeLayout_;
	
	// Expression snake
	/** Snake to show when a process is running (name: wingj_snake). */
	protected Snake expressionSnake_;
	/** Snake panel. */
	protected JPanel expressionSnakePanel_;
	/** Layout controlling the appearance of the snake. */
	protected CardLayout expressionSnakeLayout_;
	
	// ============================================================================
	// EXPRESSION PANEL
	
	/** Show selected expression dataset. */
	protected JButton expressionDatasetShowButton_;
	/** Close expression dataset visualization. */
	protected JButton expressionDatasetHideButton_;
	/** Save expression to dataset. */
	protected JButton expressionExportDatasetButton_;
	/** Go back to the main interface. */
	protected JButton expressionBackButton_;
	
	/** Selected gene whose expression will be measured. */
	protected JComboBox<String> expressionSelectedChannelCBox_;
	/** Select type of expression dataset for preview. */
	protected JComboBox<String> expressionDimensionDatasetCBox_;
	
	/** Message bar. */
	protected AppTips msgBar_ = null;
	
	// ============================================================================
	// SETTINGS PANEL
	
	/** Text pane where the content of the settings file is displayed. */
	protected JTextPane settingsTextPane_;
	/** Go back to the main WingJ interface. */
	protected JButton settingsCloseButton_;
	/** Reload the last settings file opened. */
	protected JButton settingsReloadButton_;
	/** Load the settings from file. */
	protected JButton settingsLoadButton_;
	/** Save the settings to file. */
	protected JButton settingsSaveButton_;
	
	// ============================================================================
	// BATCH PANEL
	
	/** Directory containing the experiment folders. */
	protected JTextField batchDirectoryTField_;
	/** Browse button to select the batch directory. */
	protected JButton batchBrowseButton_;
	/** Index of the first experiment to analyze. */
	protected JSpinner batchFirstExperimentIndexSpinner_;
	/** Index of the last experiment to analyze. */
	protected JSpinner batchLastExperimentIndexSpinner_;
	/** Index of the last experiment to analyze. */
	protected JSpinner batchCurrentExperimentIndexSpinner_;
	/** Progress bar. */
	protected JProgressBar batchProgress_;
	/** Begin batch experiments. */
	protected JButton batchBeginButton_;
	/** Exit batch mode. */
	protected JButton batchExitButton_;
	/** Return to the main interface. */
	protected JButton batchCloseButton_;
	
	/** Name of the first experiment. */
	protected JLabel batchFirstExperimentLabel_;
	/** Name of the last experiment. */
	protected JLabel batchLastExperimentLabel_;
	/** Name of the current experiment. */
	protected JLabel batchCurrentExperimentLabel_;
	
	/** Model for the first experiment index. */
    protected SpinnerNumberModel batchFirstExperimentIndexModel_ = new SpinnerNumberModel(0, 0, 0, 1);
	/** Model for the last experiment index. */
    protected SpinnerNumberModel batchLastExperimentIndexModel_ = new SpinnerNumberModel(0, 0, 0, 1);
	/** Model for the current experiment index. */
    protected SpinnerNumberModel batchCurrentExperimentIndexModel_ = new SpinnerNumberModel(0, 0, 0, 1);
 
    // TODO: sort (make local or field) and comment
	private final JPanel contentPanel_ = new JPanel();
	protected CardLayout contentLayout_;
	protected JCheckBox showOverlayInformationCBox_;
	protected JCheckBox showOverlayStructureCBox_;
	protected JButton setColorButton_;
	protected JComboBox<String> backgroundCBox_;
	protected JButton swapBoundariesButton_;
	protected JButton reverseDVBoundaryButton_;
	protected JButton reverseAPBoundaryButton_;
	protected JButton editStructureButton_;
	protected JPanel panel_3;
	protected JPanel panel_4;
	protected JButton saveStructureButton_;
	protected JButton exportStructurePropertiesButton_;
	protected JButton exportPreviewImageButton_;
	protected JButton exportBinaryMaskButton_;
	protected JButton exportAllButton_;
	protected JButton structureCloseButton_;
	protected JRadioButton ch00CBox_;
	protected JRadioButton ch01CBox_;
	protected JRadioButton ch02CBox_;
	protected JRadioButton ch03CBox_;
	
	protected JPanel settingsPanel_;
	protected JPanel structurePanel_;
	protected JPanel batchPanel_;
	protected JPanel expressionPanel_;
	protected JComboBox<String> systemCBox_;
	protected JSpinner ch00MinSliceIndex_;
	protected JSpinner ch00MaxSliceIndex_;
	protected JSpinner ch01MinSliceIndex_;
	protected JSpinner ch02MinSliceIndex_;
	protected JSpinner ch03MinSliceIndex_;
	protected JSpinner ch01MaxSliceIndex_;
	protected JSpinner ch02MaxSliceIndex_;
	protected JSpinner ch03MaxSliceIndex_;
	private JPanel panel_6;
	private JPanel panel_8;
	protected JProgressBar batchProgress2_;
	protected JPanel batchExperimentPanel_;
	private JPanel panel_10;
	private JLabel lblProgress_1;
	
	private JPanel panel_5;
	protected JSpinner numStructureControlPoints_;
	
	/** Contains the spinner for the min index of each channel */
	protected List<JSpinner> minSliceIndexes_ = null;
	/** Contains the spinner for the max index of each channel */
	protected List<JSpinner> maxSliceIndexes_ = null;
	private JLabel lblPointsPerSegment;
	protected JComboBox<String> expression1DBoundaryCBox_;
	protected JSlider expression1DTranslationSlider_;
	protected JSpinner expression1DTranslationSpinner_;
	protected JSlider expression1DSigmaSlider_;
	protected JSpinner expression1DSigmaSpinner_;
	protected JSlider expression2DThresholdSlider_;
	protected JSpinner expression2DThresholdSpinner_;
	protected JSpinner expression2DNumPointsSpinner_;
	protected JRadioButton expression1DResolutionDynamicRButton_;
	protected JSpinner expression1DNumPointsSpinner_;
	protected JSpinner expression1DNumPointsPerUnitSpinner_;
	protected JRadioButton expression1DResolutionConstantRButton_;
	protected JLabel expression1DResolutionDynamicUnit_;
	protected JSlider expression2DStitchingSmoothingRangeSlider_;
	protected JSpinner expression2DStitchingSmoothingRangeSpinner_;
	private JLabel lblAp_1;
	protected JCheckBox expressionNormalizedCBox_;
	private JLabel lblNewLabel;
	protected JPanel expression1DPanel_;
	protected JPanel expression2DPanel_;
	protected JPanel expression2DAggregatedPanel_;
	
	/** JTextPane included in the JScrollPane for displaying structure text information. */
	protected JTextPane structureDatasetTextPane_;
	/** Used to show structure text information. */
	protected JScrollPane structureDatasetScrollPane;
	@SuppressWarnings("unused")
	private JLabel lblNewLabel_1;
	public JTextField expressionAggRootTField_;
	protected JButton expressionComRootBrowse_;
	protected JTextField expressionAggStructureTField_;
	protected JTextField expressionAggProjectionTField_;
	public JPanel expressionComSelectionPanel_;
	protected JLabel expressionComSelectionLabel_;
	public Snake expressionComSnake_;
	public CardLayout expressionComCardLayout_;
	private JLabel label_1;
	private JLabel lblResolution_1;
	protected JSpinner expression2DAggResolutionSpinner_;
	protected JPanel expression2DReversePanel_;
	protected JTextField expression2DReverseStructureTField_;
	protected JTextField expression2DReverseMapTField_;
	protected JButton expression2DReverseStructureBrowse_;
	protected JButton expression2DReverseMapBrowse_;
	protected JRadioButton expression2DReverseEquatorAPRButton_;
	protected JRadioButton expression2DReverseEquatorDVRButton_;
	protected JRadioButton expression2DReverseCurrentModelRButton_;
	protected JRadioButton expression2DReverseOtherModelRButton_;
	protected JButton exportProjections_;
	private Component horizontalStrut_1;
	protected JPanel expressionCompositePanel_;
	protected JComboBox<String> expressionCompositeRedCBox_;
	protected JComboBox<String> expressionCompositeGreenCBox_;
	protected JComboBox<String> expressionCompositeBlueCBox_;
	private Component horizontalStrut_2;
	private JPanel expressionSuperSnakePanel;
	private Component horizontalStrut_3;

	// ============================================================================
	// PUBLIC METHODS

	/** 
	 * Launch the application
	 */
	public static void main(String[] args) {
		try {
			WJGui dialog = new WJGui();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public WJGui() {
		setTitle("WingJ");
		contentLayout_ = new CardLayout(0, 0);
		getContentPane().setLayout(contentLayout_);
		contentPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel_, "MAIN");
		contentPanel_.setLayout(new BorderLayout(0, 0));
		{
			JPanel mainPanel = new JPanel();
			contentPanel_.add(mainPanel, BorderLayout.CENTER);
			GridBagLayout gbl_mainPanel = new GridBagLayout();
			gbl_mainPanel.columnWidths = new int[]{0, 0};
			gbl_mainPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
			gbl_mainPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_mainPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
			mainPanel.setLayout(gbl_mainPanel);
			{
				JPanel wdPanel = new JPanel();
				wdPanel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "General", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_wdPanel = new GridBagConstraints();
				gbc_wdPanel.insets = new Insets(0, 0, 5, 0);
				gbc_wdPanel.fill = GridBagConstraints.HORIZONTAL;
				gbc_wdPanel.gridx = 0;
				gbc_wdPanel.gridy = 0;
				mainPanel.add(wdPanel, gbc_wdPanel);
				GridBagLayout gbl_wdPanel = new GridBagLayout();
				gbl_wdPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_wdPanel.rowHeights = new int[]{0, 0, 0, 0};
				gbl_wdPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_wdPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0};
				wdPanel.setLayout(gbl_wdPanel);
				{
					JLabel organismLabel = new JLabel("System:");
					GridBagConstraints gbc_organismLabel = new GridBagConstraints();
					gbc_organismLabel.anchor = GridBagConstraints.WEST;
					gbc_organismLabel.insets = new Insets(0, 5, 5, 5);
					gbc_organismLabel.gridx = 0;
					gbc_organismLabel.gridy = 0;
					wdPanel.add(organismLabel, gbc_organismLabel);
				}
				{
					systemCBox_ = new JComboBox<String>();
					systemCBox_.setToolTipText("Select a biological system to quantify");
					GridBagConstraints gbc_systemCBoxd_ = new GridBagConstraints();
					gbc_systemCBoxd_.fill = GridBagConstraints.HORIZONTAL;
					gbc_systemCBoxd_.gridwidth = 4;
					gbc_systemCBoxd_.insets = new Insets(0, 0, 5, 5);
					gbc_systemCBoxd_.gridx = 1;
					gbc_systemCBoxd_.gridy = 0;
					wdPanel.add(systemCBox_, gbc_systemCBoxd_);
				}
				{
					JLabel lblNewLabel_5 = new JLabel("Output: ");
					GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
					gbc_lblNewLabel_5.insets = new Insets(0, 5, 5, 5);
					gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
					gbc_lblNewLabel_5.gridx = 0;
					gbc_lblNewLabel_5.gridy = 1;
					wdPanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
				}
				{
					outputTField_ = new JTextField();
					outputTField_.setToolTipText("Output directory");
					outputTField_.setEditable(false);
					GridBagConstraints gbc_outputTField_ = new GridBagConstraints();
					gbc_outputTField_.insets = new Insets(0, 0, 5, 5);
					gbc_outputTField_.anchor = GridBagConstraints.WEST;
					gbc_outputTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_outputTField_.gridwidth = 3;
					gbc_outputTField_.gridx = 1;
					gbc_outputTField_.gridy = 1;
					wdPanel.add(outputTField_, gbc_outputTField_);
					outputTField_.setColumns(10);
				}
				{
					outputButton_ = new JButton("Browse");
					outputButton_.setToolTipText("Select output directory");
					outputButton_.setIcon(null);
					GridBagConstraints gbc_outputButton_ = new GridBagConstraints();
					gbc_outputButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_outputButton_.insets = new Insets(0, 0, 5, 5);
					gbc_outputButton_.gridx = 4;
					gbc_outputButton_.gridy = 1;
					wdPanel.add(outputButton_, gbc_outputButton_);
				}
				{
					JPanel panel = new JPanel();
					GridBagConstraints gbc_panel = new GridBagConstraints();
					gbc_panel.anchor = GridBagConstraints.WEST;
					gbc_panel.gridwidth = 4;
					gbc_panel.fill = GridBagConstraints.VERTICAL;
					gbc_panel.gridx = 1;
					gbc_panel.gridy = 2;
					wdPanel.add(panel, gbc_panel);
					GridBagLayout gbl_panel = new GridBagLayout();
					gbl_panel.columnWidths = new int[]{71, 0, 0};
					gbl_panel.rowHeights = new int[]{23, 0};
					gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
					gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel.setLayout(gbl_panel);
					{
						setSettingsButton_ = new JButton("Settings");
						GridBagConstraints gbc_setSettingsButton_ = new GridBagConstraints();
						gbc_setSettingsButton_.insets = new Insets(0, 0, 0, 5);
						gbc_setSettingsButton_.anchor = GridBagConstraints.NORTHWEST;
						gbc_setSettingsButton_.gridx = 0;
						gbc_setSettingsButton_.gridy = 0;
						panel.add(setSettingsButton_, gbc_setSettingsButton_);
						setSettingsButton_.setToolTipText("Expert settings");
					}
					{
						aboutButton_ = new JButton("About");
						GridBagConstraints gbc_aboutButton_ = new GridBagConstraints();
						gbc_aboutButton_.gridx = 1;
						gbc_aboutButton_.gridy = 0;
						panel.add(aboutButton_, gbc_aboutButton_);
						aboutButton_.setToolTipText("About WingJ");
					}
				}
			}
			{
				JPanel confocalPanel = new JPanel();
				confocalPanel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Experiment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_confocalPanel = new GridBagConstraints();
				gbc_confocalPanel.insets = new Insets(0, 0, 5, 0);
				gbc_confocalPanel.fill = GridBagConstraints.HORIZONTAL;
				gbc_confocalPanel.gridx = 0;
				gbc_confocalPanel.gridy = 1;
				mainPanel.add(confocalPanel, gbc_confocalPanel);
				GridBagLayout gbl_confocalPanel = new GridBagLayout();
				gbl_confocalPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
				gbl_confocalPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
				gbl_confocalPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
				gbl_confocalPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				confocalPanel.setLayout(gbl_confocalPanel);
				{
					JLabel lblPlop = new JLabel("Name:");
					GridBagConstraints gbc_lblPlop = new GridBagConstraints();
					gbc_lblPlop.anchor = GridBagConstraints.WEST;
					gbc_lblPlop.insets = new Insets(0, 5, 5, 5);
					gbc_lblPlop.gridx = 0;
					gbc_lblPlop.gridy = 0;
					confocalPanel.add(lblPlop, gbc_lblPlop);
				}
				{
					nameTField_ = new JTextField();
					nameTField_.setToolTipText("Name of the experiment");
					GridBagConstraints gbc_idTField_ = new GridBagConstraints();
					gbc_idTField_.insets = new Insets(0, 0, 5, 5);
					gbc_idTField_.gridwidth = 6;
					gbc_idTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_idTField_.gridx = 1;
					gbc_idTField_.gridy = 0;
					confocalPanel.add(nameTField_, gbc_idTField_);
					nameTField_.setColumns(1);
				}
				{
					resetButton_ = new JButton("Reset");
					resetButton_.setToolTipText("<html>Reset experiment (closes all the windows open)</html>");
					GridBagConstraints gbc_closeAllButton_ = new GridBagConstraints();
					gbc_closeAllButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_closeAllButton_.insets = new Insets(0, 0, 5, 5);
					gbc_closeAllButton_.gridx = 7;
					gbc_closeAllButton_.gridy = 0;
					confocalPanel.add(resetButton_, gbc_closeAllButton_);
				}
				{
					ch00CBox_ = new JRadioButton("Ch 0:");
					ch00CBox_.setToolTipText("Select as structure channel");
					GridBagConstraints gbc_ch00CBox_ = new GridBagConstraints();
					gbc_ch00CBox_.anchor = GridBagConstraints.WEST;
					gbc_ch00CBox_.insets = new Insets(0, 0, 5, 5);
					gbc_ch00CBox_.gridx = 0;
					gbc_ch00CBox_.gridy = 1;
					confocalPanel.add(ch00CBox_, gbc_ch00CBox_);
				}
				{
					ch00NameTField_ = new JTextField();
					ch00NameTField_.setToolTipText("Name of the gene/protein/channel");
					GridBagConstraints gbc_structureName_ = new GridBagConstraints();
					gbc_structureName_.fill = GridBagConstraints.HORIZONTAL;
					gbc_structureName_.insets = new Insets(0, 0, 5, 5);
					gbc_structureName_.gridx = 1;
					gbc_structureName_.gridy = 1;
					ch00NameTField_.setColumns(6);
					confocalPanel.add(ch00NameTField_, gbc_structureName_);
					
				}
				{
					ch00MinSliceIndex_ = new JSpinner();
					ch00MinSliceIndex_.setToolTipText("Index of the first slice to consider");
					GridBagConstraints gbc_ch00MinSliceIndex_ = new GridBagConstraints();
					gbc_ch00MinSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch00MinSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch00MinSliceIndex_.gridx = 2;
					gbc_ch00MinSliceIndex_.gridy = 1;
					confocalPanel.add(ch00MinSliceIndex_, gbc_ch00MinSliceIndex_);
//					ch00MinSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch00MinSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel lblTo = new JLabel("to");
					GridBagConstraints gbc_lblTo = new GridBagConstraints();
					gbc_lblTo.insets = new Insets(0, 0, 5, 5);
					gbc_lblTo.gridx = 3;
					gbc_lblTo.gridy = 1;
					confocalPanel.add(lblTo, gbc_lblTo);
				}
				{
					ch00MaxSliceIndex_ = new JSpinner();
					ch00MaxSliceIndex_.setToolTipText("Index of the last slice to consider");
					GridBagConstraints gbc_ch00MaxSliceIndex_ = new GridBagConstraints();
					gbc_ch00MaxSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch00MaxSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch00MaxSliceIndex_.gridx = 4;
					gbc_ch00MaxSliceIndex_.gridy = 1;
					confocalPanel.add(ch00MaxSliceIndex_, gbc_ch00MaxSliceIndex_);
//					ch00MaxSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch00MaxSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					gene0ProjectionMeanRButton_ = new JRadioButton("Mean");
					gene0ProjectionMeanRButton_.setToolTipText("Average intensity projection");
					GridBagConstraints gbc_structureProjectionMeanRButton_ = new GridBagConstraints();
					gbc_structureProjectionMeanRButton_.anchor = GridBagConstraints.WEST;
					gbc_structureProjectionMeanRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_structureProjectionMeanRButton_.gridx = 5;
					gbc_structureProjectionMeanRButton_.gridy = 1;
					confocalPanel.add(gene0ProjectionMeanRButton_, gbc_structureProjectionMeanRButton_);
				}
				{
					gene0ProjectionMaxRButton_ = new JRadioButton("Max");
					gene0ProjectionMaxRButton_.setToolTipText("Maximum intensity projection");
					GridBagConstraints gbc_structureProjectionMaxRButton_ = new GridBagConstraints();
					gbc_structureProjectionMaxRButton_.anchor = GridBagConstraints.WEST;
					gbc_structureProjectionMaxRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_structureProjectionMaxRButton_.gridx = 6;
					gbc_structureProjectionMaxRButton_.gridy = 1;
					confocalPanel.add(gene0ProjectionMaxRButton_, gbc_structureProjectionMaxRButton_);
				}
				{
					ch00DirectoryButton_ = new JButton("Browse");
					ch00DirectoryButton_.setToolTipText("Select a directory containing an image or image stack");
					ch00DirectoryButton_.setIcon(null);
					GridBagConstraints gbc_ch00DirectoryButton_ = new GridBagConstraints();
					gbc_ch00DirectoryButton_.insets = new Insets(0, 0, 5, 5);
					gbc_ch00DirectoryButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch00DirectoryButton_.gridx = 7;
					gbc_ch00DirectoryButton_.gridy = 1;
					confocalPanel.add(ch00DirectoryButton_, gbc_ch00DirectoryButton_);
				}
				{
					ch01CBox_ = new JRadioButton("Ch 1:");
					ch01CBox_.setToolTipText("Select as structure channel");
					GridBagConstraints gbc_ch01CBox_ = new GridBagConstraints();
					gbc_ch01CBox_.anchor = GridBagConstraints.WEST;
					gbc_ch01CBox_.insets = new Insets(0, 0, 5, 5);
					gbc_ch01CBox_.gridx = 0;
					gbc_ch01CBox_.gridy = 2;
					confocalPanel.add(ch01CBox_, gbc_ch01CBox_);
				}
				{
					ch01NameTField_ = new JTextField();
					ch01NameTField_.setToolTipText("Name of the gene/protein/channel");
					GridBagConstraints gbc_gene1Name_ = new GridBagConstraints();
					gbc_gene1Name_.fill = GridBagConstraints.HORIZONTAL;
					gbc_gene1Name_.insets = new Insets(0, 0, 5, 5);
					gbc_gene1Name_.gridx = 1;
					gbc_gene1Name_.gridy = 2;
					confocalPanel.add(ch01NameTField_, gbc_gene1Name_);
					ch01NameTField_.setColumns(5);
				}
				{
					ch01MinSliceIndex_ = new JSpinner();
					ch01MinSliceIndex_.setToolTipText("Index of the first slice to consider");
					GridBagConstraints gbc_ch01MinSliceIndex_ = new GridBagConstraints();
					gbc_ch01MinSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch01MinSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch01MinSliceIndex_.gridx = 2;
					gbc_ch01MinSliceIndex_.gridy = 2;
					confocalPanel.add(ch01MinSliceIndex_, gbc_ch01MinSliceIndex_);
//					ch01MinSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch01MinSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel lblTo = new JLabel("to");
					GridBagConstraints gbc_lblTo = new GridBagConstraints();
					gbc_lblTo.insets = new Insets(0, 0, 5, 5);
					gbc_lblTo.gridx = 3;
					gbc_lblTo.gridy = 2;
					confocalPanel.add(lblTo, gbc_lblTo);
				}
				{
					ch01MaxSliceIndex_ = new JSpinner();
					ch01MaxSliceIndex_.setToolTipText("Index of the last slice to consider");
					GridBagConstraints gbc_ch01MaxSliceIndex_ = new GridBagConstraints();
					gbc_ch01MaxSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch01MaxSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch01MaxSliceIndex_.gridx = 4;
					gbc_ch01MaxSliceIndex_.gridy = 2;
					confocalPanel.add(ch01MaxSliceIndex_, gbc_ch01MaxSliceIndex_);
//					ch01MaxSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch01MaxSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					gene1ProjectionMeanRButton_ = new JRadioButton("Mean");
					gene1ProjectionMeanRButton_.setToolTipText("Average intensity projection");
					GridBagConstraints gbc_gene1ProjectionMeanRButton_ = new GridBagConstraints();
					gbc_gene1ProjectionMeanRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene1ProjectionMeanRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene1ProjectionMeanRButton_.gridx = 5;
					gbc_gene1ProjectionMeanRButton_.gridy = 2;
					confocalPanel.add(gene1ProjectionMeanRButton_, gbc_gene1ProjectionMeanRButton_);
				}
				{
					gene1ProjectionMaxRButton_ = new JRadioButton("Max");
					gene1ProjectionMaxRButton_.setToolTipText("Maximum intensity projection");
					GridBagConstraints gbc_gene1ProjectionMaxRButton_ = new GridBagConstraints();
					gbc_gene1ProjectionMaxRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene1ProjectionMaxRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene1ProjectionMaxRButton_.gridx = 6;
					gbc_gene1ProjectionMaxRButton_.gridy = 2;
					confocalPanel.add(gene1ProjectionMaxRButton_, gbc_gene1ProjectionMaxRButton_);
				}
				{
					ch01DirectoryButton_ = new JButton("Browse");
					ch01DirectoryButton_.setToolTipText("Select a directory containing an image or image stack");
					ch01DirectoryButton_.setIcon(null);
					GridBagConstraints gbc_ch01DirectoryButton_ = new GridBagConstraints();
					gbc_ch01DirectoryButton_.insets = new Insets(0, 0, 5, 5);
					gbc_ch01DirectoryButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch01DirectoryButton_.gridx = 7;
					gbc_ch01DirectoryButton_.gridy = 2;
					confocalPanel.add(ch01DirectoryButton_, gbc_ch01DirectoryButton_);
				}
				{
					ch02CBox_ = new JRadioButton("Ch 2:");
					ch02CBox_.setToolTipText("Select as structure channel");
					GridBagConstraints gbc_ch02CBox_ = new GridBagConstraints();
					gbc_ch02CBox_.anchor = GridBagConstraints.WEST;
					gbc_ch02CBox_.insets = new Insets(0, 0, 5, 5);
					gbc_ch02CBox_.gridx = 0;
					gbc_ch02CBox_.gridy = 3;
					confocalPanel.add(ch02CBox_, gbc_ch02CBox_);
				}
				{
					ch02NameTField_ = new JTextField();
					ch02NameTField_.setToolTipText("Name of the gene/protein/channel");
					GridBagConstraints gbc_gene2Name_ = new GridBagConstraints();
					gbc_gene2Name_.fill = GridBagConstraints.HORIZONTAL;
					gbc_gene2Name_.insets = new Insets(0, 0, 5, 5);
					gbc_gene2Name_.gridx = 1;
					gbc_gene2Name_.gridy = 3;
					confocalPanel.add(ch02NameTField_, gbc_gene2Name_);
					ch02NameTField_.setColumns(5);
				}
				{
					ch02MinSliceIndex_ = new JSpinner();
					ch02MinSliceIndex_.setToolTipText("Index of the first slice to consider");
					GridBagConstraints gbc_ch02MinSliceIndex_ = new GridBagConstraints();
					gbc_ch02MinSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch02MinSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch02MinSliceIndex_.gridx = 2;
					gbc_ch02MinSliceIndex_.gridy = 3;
					confocalPanel.add(ch02MinSliceIndex_, gbc_ch02MinSliceIndex_);
//					ch02MinSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch02MinSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel lblTo = new JLabel("to");
					GridBagConstraints gbc_lblTo = new GridBagConstraints();
					gbc_lblTo.insets = new Insets(0, 0, 5, 5);
					gbc_lblTo.gridx = 3;
					gbc_lblTo.gridy = 3;
					confocalPanel.add(lblTo, gbc_lblTo);
				}
				{
					ch02MaxSliceIndex_ = new JSpinner();
					ch02MaxSliceIndex_.setToolTipText("Index of the last slice to consider");
					GridBagConstraints gbc_ch02MaxSliceIndex_ = new GridBagConstraints();
					gbc_ch02MaxSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch02MaxSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch02MaxSliceIndex_.gridx = 4;
					gbc_ch02MaxSliceIndex_.gridy = 3;
					confocalPanel.add(ch02MaxSliceIndex_, gbc_ch02MaxSliceIndex_);
//					ch02MaxSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch02MaxSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					gene2ProjectionMeanRButton_ = new JRadioButton("Mean");
					gene2ProjectionMeanRButton_.setToolTipText("Average intensity projection");
					GridBagConstraints gbc_gene2ProjectionMeanRButton_ = new GridBagConstraints();
					gbc_gene2ProjectionMeanRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene2ProjectionMeanRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene2ProjectionMeanRButton_.gridx = 5;
					gbc_gene2ProjectionMeanRButton_.gridy = 3;
					confocalPanel.add(gene2ProjectionMeanRButton_, gbc_gene2ProjectionMeanRButton_);
				}
				{
					gene2ProjectionMaxRButton_ = new JRadioButton("Max");
					gene2ProjectionMaxRButton_.setToolTipText("Maximum intensity projection");
					GridBagConstraints gbc_gene2ProjectionMaxRButton_ = new GridBagConstraints();
					gbc_gene2ProjectionMaxRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene2ProjectionMaxRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene2ProjectionMaxRButton_.gridx = 6;
					gbc_gene2ProjectionMaxRButton_.gridy = 3;
					confocalPanel.add(gene2ProjectionMaxRButton_, gbc_gene2ProjectionMaxRButton_);
				}
				{
					ch02DirectoryButton_ = new JButton("Browse");
					ch02DirectoryButton_.setToolTipText("Select a directory containing an image or image stack");
					ch02DirectoryButton_.setIcon(null);
					GridBagConstraints gbc_ch02DirectoryButton_ = new GridBagConstraints();
					gbc_ch02DirectoryButton_.insets = new Insets(0, 0, 5, 5);
					gbc_ch02DirectoryButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch02DirectoryButton_.gridx = 7;
					gbc_ch02DirectoryButton_.gridy = 3;
					confocalPanel.add(ch02DirectoryButton_, gbc_ch02DirectoryButton_);
				}
				{
					ch03CBox_ = new JRadioButton("Ch 3:");
					ch03CBox_.setToolTipText("Select as structure channel");
					GridBagConstraints gbc_ch03CBox_ = new GridBagConstraints();
					gbc_ch03CBox_.anchor = GridBagConstraints.WEST;
					gbc_ch03CBox_.insets = new Insets(0, 0, 5, 5);
					gbc_ch03CBox_.gridx = 0;
					gbc_ch03CBox_.gridy = 4;
					confocalPanel.add(ch03CBox_, gbc_ch03CBox_);
				}
				{
					ch03NameTField_ = new JTextField();
					ch03NameTField_.setToolTipText("Name of the gene/protein/channel");
					GridBagConstraints gbc_ch03NameTField_ = new GridBagConstraints();
					gbc_ch03NameTField_.insets = new Insets(0, 0, 5, 5);
					gbc_ch03NameTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch03NameTField_.gridx = 1;
					gbc_ch03NameTField_.gridy = 4;
					confocalPanel.add(ch03NameTField_, gbc_ch03NameTField_);
					ch03NameTField_.setColumns(5);
				}
				{
					ch03MinSliceIndex_ = new JSpinner();
					ch03MinSliceIndex_.setToolTipText("Index of the first slice to consider");
					GridBagConstraints gbc_ch03MinSliceIndex_ = new GridBagConstraints();
					gbc_ch03MinSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch03MinSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch03MinSliceIndex_.gridx = 2;
					gbc_ch03MinSliceIndex_.gridy = 4;
					confocalPanel.add(ch03MinSliceIndex_, gbc_ch03MinSliceIndex_);
//					ch03MinSliceIndex_.setPreferredSize(new Dimension(45, 20));
//					((JSpinner.DefaultEditor)ch03MinSliceIndex_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel lblTo = new JLabel("to");
					GridBagConstraints gbc_lblTo = new GridBagConstraints();
					gbc_lblTo.insets = new Insets(0, 0, 5, 5);
					gbc_lblTo.gridx = 3;
					gbc_lblTo.gridy = 4;
					confocalPanel.add(lblTo, gbc_lblTo);
				}
				{
					ch03MaxSliceIndex_ = new JSpinner();
					ch03MaxSliceIndex_.setToolTipText("Index of the last slice to consider");
					GridBagConstraints gbc_ch03MaxSliceIndex_ = new GridBagConstraints();
					gbc_ch03MaxSliceIndex_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch03MaxSliceIndex_.insets = new Insets(0, 0, 5, 5);
					gbc_ch03MaxSliceIndex_.gridx = 4;
					gbc_ch03MaxSliceIndex_.gridy = 4;
					confocalPanel.add(ch03MaxSliceIndex_, gbc_ch03MaxSliceIndex_);
//					ch03MaxSliceIndex_.setPreferredSize(new Dimension(40, 20));
//					((JSpinner.DefaultEditor)ch03MaxSliceIndex_.getEditor()).getTextField().setColumns(2);
				}
				{
					gene3ProjectionMeanRButton_ = new JRadioButton("Mean");
					gene3ProjectionMeanRButton_.setToolTipText("Average intensity projection");
					GridBagConstraints gbc_gene3ProjectionMeanRButton_ = new GridBagConstraints();
					gbc_gene3ProjectionMeanRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene3ProjectionMeanRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene3ProjectionMeanRButton_.gridx = 5;
					gbc_gene3ProjectionMeanRButton_.gridy = 4;
					confocalPanel.add(gene3ProjectionMeanRButton_, gbc_gene3ProjectionMeanRButton_);
				}
				{
					gene3ProjectionMaxRButton_ = new JRadioButton("Max");
					gene3ProjectionMaxRButton_.setToolTipText("Maximum intensity projection");
					GridBagConstraints gbc_gene3ProjectionMaxRButton_ = new GridBagConstraints();
					gbc_gene3ProjectionMaxRButton_.anchor = GridBagConstraints.WEST;
					gbc_gene3ProjectionMaxRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_gene3ProjectionMaxRButton_.gridx = 6;
					gbc_gene3ProjectionMaxRButton_.gridy = 4;
					confocalPanel.add(gene3ProjectionMaxRButton_, gbc_gene3ProjectionMaxRButton_);
				}
				{
					ch03DirectoryButton_ = new JButton("Browse");
					ch03DirectoryButton_.setToolTipText("Select a directory containing an image or image stack");
					ch03DirectoryButton_.setIcon(null);
					GridBagConstraints gbc_ch03DirectoryButton_ = new GridBagConstraints();
					gbc_ch03DirectoryButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_ch03DirectoryButton_.insets = new Insets(0, 0, 5, 5);
					gbc_ch03DirectoryButton_.gridx = 7;
					gbc_ch03DirectoryButton_.gridy = 4;
					confocalPanel.add(ch03DirectoryButton_, gbc_ch03DirectoryButton_);
				}
				{
					scaleLabel_ = new JLabel("1 px = 1 um");
					GridBagConstraints gbc_scaleLabel_ = new GridBagConstraints();
					gbc_scaleLabel_.anchor = GridBagConstraints.WEST;
					gbc_scaleLabel_.gridwidth = 5;
					gbc_scaleLabel_.insets = new Insets(0, 5, 0, 5);
					gbc_scaleLabel_.gridx = 0;
					gbc_scaleLabel_.gridy = 5;
					confocalPanel.add(scaleLabel_, gbc_scaleLabel_);
					scaleLabel_.setToolTipText("<html>Relation between pixels and meaningful physical units.<br>If not automatically set after loading a stack of images,<br>go to <i>Settings</i> and set the parameters <i>scale</i> and <i>unit</i>.</html>");
				}
				{
					maskButton_ = new JButton("Set AOI");
					GridBagConstraints gbc_maskButton_ = new GridBagConstraints();
					gbc_maskButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_maskButton_.gridwidth = 2;
					gbc_maskButton_.insets = new Insets(0, 0, 0, 5);
					gbc_maskButton_.gridx = 5;
					gbc_maskButton_.gridy = 5;
					confocalPanel.add(maskButton_, gbc_maskButton_);
					maskButton_.setIcon(null);
					maskButton_.setToolTipText("Set an area of interest (AOI)");
				}
				{
					exportProjections_ = new JButton("Save");
					exportProjections_.setToolTipText("<html>Saves the image dataset to the images folder and<br>export the projections to the\nselected output directory</html>");
					GridBagConstraints gbc_exportProjections_ = new GridBagConstraints();
					gbc_exportProjections_.insets = new Insets(0, 0, 0, 5);
					gbc_exportProjections_.fill = GridBagConstraints.HORIZONTAL;
					gbc_exportProjections_.gridx = 7;
					gbc_exportProjections_.gridy = 5;
					confocalPanel.add(exportProjections_, gbc_exportProjections_);
				}
			}
			{
				JPanel detectionAndSnakePanel = new JPanel();
				GridBagConstraints gbc_detectionAndSnakePanel = new GridBagConstraints();
				gbc_detectionAndSnakePanel.insets = new Insets(0, 0, 5, 0);
				gbc_detectionAndSnakePanel.fill = GridBagConstraints.BOTH;
				gbc_detectionAndSnakePanel.gridx = 0;
				gbc_detectionAndSnakePanel.gridy = 2;
				mainPanel.add(detectionAndSnakePanel, gbc_detectionAndSnakePanel);
				GridBagLayout gbl_detectionAndSnakePanel = new GridBagLayout();
				gbl_detectionAndSnakePanel.columnWidths = new int[]{0, 0, 0};
				gbl_detectionAndSnakePanel.rowHeights = new int[]{0, 0};
				gbl_detectionAndSnakePanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
				gbl_detectionAndSnakePanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				detectionAndSnakePanel.setLayout(gbl_detectionAndSnakePanel);
				{
					JPanel detectionControlPanel = new JPanel();
					GridBagConstraints gbc_detectionControlPanel = new GridBagConstraints();
					gbc_detectionControlPanel.fill = GridBagConstraints.HORIZONTAL;
					gbc_detectionControlPanel.gridx = 0;
					gbc_detectionControlPanel.gridy = 0;
					detectionAndSnakePanel.add(detectionControlPanel, gbc_detectionControlPanel);
					detectionControlPanel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Structure Detection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					detectionControlPanel.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel = new JPanel();
						detectionControlPanel.add(panel);
						panel.setLayout(new GridLayout(0, 3, 0, 0));
						{
							preprocessingScanButton_ = new JButton("Pre-Process");
							panel.add(preprocessingScanButton_);
							preprocessingScanButton_.setToolTipText("Pre-process images before structure detection");
							preprocessingScanButton_.setIcon(null);
						}
						{
							detectionRunAllButton_ = new JButton("Run Detection");
							detectionRunAllButton_.setToolTipText("Run the detection to generate a structure model");
							detectionRunAllButton_.setIcon(null);
							panel.add(detectionRunAllButton_);
						}
						{
							detectionPauseButton_ = new JButton("Pause");
							detectionPauseButton_.setToolTipText("Pause the structure detection");
							detectionPauseButton_.setIcon(null);
							panel.add(detectionPauseButton_);
						}
						{
							detectionResumeButton_ = new JButton("Resume");
							detectionResumeButton_.setToolTipText("Resume the structure detection");
							detectionResumeButton_.setIcon(null);
							panel.add(detectionResumeButton_);
						}
						{
							detectionStepButton_ = new JButton("Step");
							detectionStepButton_.setToolTipText("Apply a single detection module");
							detectionStepButton_.setIcon(null);
							panel.add(detectionStepButton_);
						}
						{
							detectionRedoStepButton_ = new JButton("Redo Step");
							detectionRedoStepButton_.setToolTipText("Run again the last detection module applied");
							detectionRedoStepButton_.setIcon(null);
							panel.add(detectionRedoStepButton_);
						}
						{
							detectionAbortButton_ = new JButton("Erase");
							detectionAbortButton_.setToolTipText("Delete the current structure model");
							detectionAbortButton_.setIcon(null);
							panel.add(detectionAbortButton_);
						}
						{
							detectionManualButton_ = new JButton("Manual");
							detectionManualButton_.setToolTipText("Manual structure detection");
							detectionManualButton_.setIcon(null);
							panel.add(detectionManualButton_);
						}
						{
							detectionLoadButton_ = new JButton("Import");
							detectionLoadButton_.setToolTipText("Load structure model from file");
							detectionLoadButton_.setIcon(null);
							panel.add(detectionLoadButton_);
						}
					}
					{
						Component horizontalStrut = Box.createHorizontalStrut(5);
						detectionControlPanel.add(horizontalStrut, BorderLayout.WEST);
					}
					{
						Component horizontalStrut = Box.createHorizontalStrut(5);
						detectionControlPanel.add(horizontalStrut, BorderLayout.EAST);
					}
				}
				{
					JPanel panel_1 = new JPanel();
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.gridx = 1;
					gbc_panel_1.gridy = 0;
					detectionAndSnakePanel.add(panel_1, gbc_panel_1);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						snakePanel_ = new JPanel();
						panel_1.add(snakePanel_, BorderLayout.CENTER);
						snakeLayout_ = new CardLayout(0, 0);
						snakePanel_.setLayout(snakeLayout_);
						{
							snake_ = new Snake();
							snakePanel_.add(snake_, "card_snake");
							snake_.setLayout(new GridBagLayout());
							snake_.setName("snake_");
						}
						{
							Component rigidArea = Box.createRigidArea(new Dimension(60, 60));
							snakePanel_.add(rigidArea, "card_decoy");
						}
					}
				}
			}
			{
				{
					{
						settingsPanel_ = new JPanel();
						settingsPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
						getContentPane().add(settingsPanel_, "SETTINGS");
						settingsPanel_.setLayout(new BorderLayout(0, 0));
						{
							JPanel panel = new JPanel();
							panel.setBorder(new EmptyBorder(5, 0, 0, 0));
							settingsPanel_.add(panel, BorderLayout.SOUTH);
							GridBagLayout gbl_panel = new GridBagLayout();
							gbl_panel.columnWidths = new int[]{0};
							gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
							gbl_panel.columnWeights = new double[]{1.0};
							gbl_panel.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
							panel.setLayout(gbl_panel);
							{
								JPanel panel_1 = new JPanel();
								GridBagConstraints gbc_panel_1 = new GridBagConstraints();
								gbc_panel_1.insets = new Insets(0, 0, 5, 0);
								gbc_panel_1.fill = GridBagConstraints.BOTH;
								gbc_panel_1.gridx = 0;
								gbc_panel_1.gridy = 0;
								panel.add(panel_1, gbc_panel_1);
								GridBagLayout gbl_panel_1 = new GridBagLayout();
								gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
								gbl_panel_1.rowHeights = new int[]{23, 0};
								gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
								gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
								panel_1.setLayout(gbl_panel_1);
								{
									settingsCloseButton_ = new JButton("Save and Close");
									settingsCloseButton_.setToolTipText("Apply settings and go back to the main interface");
									GridBagConstraints gbc_settingsCloseButton_ = new GridBagConstraints();
									gbc_settingsCloseButton_.anchor = GridBagConstraints.WEST;
									gbc_settingsCloseButton_.insets = new Insets(0, 0, 0, 5);
									gbc_settingsCloseButton_.gridx = 0;
									gbc_settingsCloseButton_.gridy = 0;
									panel_1.add(settingsCloseButton_, gbc_settingsCloseButton_);
								}
								{
									panel_5 = new JPanel();
									GridBagConstraints gbc_panel_5 = new GridBagConstraints();
									gbc_panel_5.anchor = GridBagConstraints.EAST;
									gbc_panel_5.gridwidth = 4;
									gbc_panel_5.fill = GridBagConstraints.VERTICAL;
									gbc_panel_5.gridx = 1;
									gbc_panel_5.gridy = 0;
									panel_1.add(panel_5, gbc_panel_5);
									GridBagLayout gbl_panel_5 = new GridBagLayout();
									gbl_panel_5.columnWidths = new int[]{46, 0, 0, 0};
									gbl_panel_5.rowHeights = new int[]{14, 0};
									gbl_panel_5.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
									gbl_panel_5.rowWeights = new double[]{0.0, Double.MIN_VALUE};
									panel_5.setLayout(gbl_panel_5);
									{
										settingsLoadButton_ = new JButton("Load");
										GridBagConstraints gbc_settingsLoadButton_ = new GridBagConstraints();
										gbc_settingsLoadButton_.insets = new Insets(0, 0, 0, 5);
										gbc_settingsLoadButton_.gridx = 0;
										gbc_settingsLoadButton_.gridy = 0;
										panel_5.add(settingsLoadButton_, gbc_settingsLoadButton_);
										settingsLoadButton_.setToolTipText("Load settings from file in text format");
									}
									{
										settingsReloadButton_ = new JButton("Reload");
										GridBagConstraints gbc_settingsReloadButton_ = new GridBagConstraints();
										gbc_settingsReloadButton_.insets = new Insets(0, 0, 0, 5);
										gbc_settingsReloadButton_.gridx = 1;
										gbc_settingsReloadButton_.gridy = 0;
										panel_5.add(settingsReloadButton_, gbc_settingsReloadButton_);
										settingsReloadButton_.setToolTipText("Reload last settings file opened");
									}
									{
										settingsSaveButton_ = new JButton("Export");
										GridBagConstraints gbc_settingsSaveButton_ = new GridBagConstraints();
										gbc_settingsSaveButton_.gridx = 2;
										gbc_settingsSaveButton_.gridy = 0;
										panel_5.add(settingsSaveButton_, gbc_settingsSaveButton_);
										settingsSaveButton_.setToolTipText("Save settings to file in text format");
									}
								}
							}
						}
						{
							JScrollPane settingsScrollPane = new JScrollPane();
							settingsPanel_.add(settingsScrollPane, BorderLayout.CENTER);
							{
								settingsTextPane_ = new JTextPane();
								settingsScrollPane.setViewportView(settingsTextPane_);
							}
						}
					}
				}
			}
			{
				panel_6 = new JPanel();
				panel_6.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Datasets", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel_6 = new GridBagConstraints();
				gbc_panel_6.insets = new Insets(0, 0, 5, 0);
				gbc_panel_6.fill = GridBagConstraints.BOTH;
				gbc_panel_6.gridx = 0;
				gbc_panel_6.gridy = 3;
				mainPanel.add(panel_6, gbc_panel_6);
				GridBagLayout gbl_panel_6 = new GridBagLayout();
				gbl_panel_6.columnWidths = new int[]{0, 0};
				gbl_panel_6.rowHeights = new int[]{0, 0};
				gbl_panel_6.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel_6.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				panel_6.setLayout(gbl_panel_6);
				{
					panel_8 = new JPanel();
					GridBagConstraints gbc_panel_8 = new GridBagConstraints();
					gbc_panel_8.insets = new Insets(0, 5, 0, 5);
					gbc_panel_8.fill = GridBagConstraints.BOTH;
					gbc_panel_8.gridx = 0;
					gbc_panel_8.gridy = 0;
					panel_6.add(panel_8, gbc_panel_8);
					panel_8.setLayout(new GridLayout(0, 2, 0, 0));
					{
						detectionStructureButton_ = new JButton("Structure");
						panel_8.add(detectionStructureButton_);
						detectionStructureButton_.setToolTipText("<html>Go to the <b>Structure panel</b> to edit, visualize and export the<br> current structure model</html>");
						detectionStructureButton_.setIcon(null);
					}
					{
						quantifyExpressionButton_ = new JButton("Expression");
						panel_8.add(quantifyExpressionButton_);
						quantifyExpressionButton_.setIcon(null);
						quantifyExpressionButton_.setToolTipText("<html>Go to the <b>Expression panel</b> to quantify expression inside the<br> space of the current structure model</html>");
					}
				}
			}
			{
				batchExperimentPanel_ = new JPanel();
				batchExperimentPanel_.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Batch Experiment", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_batchExperimentPanel_ = new GridBagConstraints();
				gbc_batchExperimentPanel_.insets = new Insets(0, 0, 5, 0);
				gbc_batchExperimentPanel_.fill = GridBagConstraints.BOTH;
				gbc_batchExperimentPanel_.gridx = 0;
				gbc_batchExperimentPanel_.gridy = 4;
				mainPanel.add(batchExperimentPanel_, gbc_batchExperimentPanel_);
				GridBagLayout gbl_batchExperimentPanel_ = new GridBagLayout();
				gbl_batchExperimentPanel_.columnWidths = new int[]{0, 0};
				gbl_batchExperimentPanel_.rowHeights = new int[]{0, 0};
				gbl_batchExperimentPanel_.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_batchExperimentPanel_.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				batchExperimentPanel_.setLayout(gbl_batchExperimentPanel_);
				{
					panel_10 = new JPanel();
					GridBagConstraints gbc_panel_10 = new GridBagConstraints();
					gbc_panel_10.insets = new Insets(0, 5, 0, 5);
					gbc_panel_10.fill = GridBagConstraints.BOTH;
					gbc_panel_10.gridx = 0;
					gbc_panel_10.gridy = 0;
					batchExperimentPanel_.add(panel_10, gbc_panel_10);
					GridBagLayout gbl_panel_10 = new GridBagLayout();
					gbl_panel_10.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
					gbl_panel_10.rowHeights = new int[]{0, 0};
					gbl_panel_10.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
					gbl_panel_10.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel_10.setLayout(gbl_panel_10);
					{
						batchButton_ = new JButton("Initialize");
						GridBagConstraints gbc_batchButton_ = new GridBagConstraints();
						gbc_batchButton_.anchor = GridBagConstraints.NORTH;
						gbc_batchButton_.insets = new Insets(0, 0, 0, 5);
						gbc_batchButton_.gridx = 0;
						gbc_batchButton_.gridy = 0;
						panel_10.add(batchButton_, gbc_batchButton_);
						batchButton_.setToolTipText("Setup batch experiments");
					}
					{
						batchAcceptButton_ = new JButton("Accept");
						GridBagConstraints gbc_batchAcceptButton_ = new GridBagConstraints();
						gbc_batchAcceptButton_.insets = new Insets(0, 0, 0, 5);
						gbc_batchAcceptButton_.gridx = 1;
						gbc_batchAcceptButton_.gridy = 0;
						panel_10.add(batchAcceptButton_, gbc_batchAcceptButton_);
						batchAcceptButton_.setToolTipText("<html>Validate current experiment before starting the next one</html>");
						batchAcceptButton_.setIcon(null);
					}
					{
						batchSkipButton_ = new JButton("Skip");
						GridBagConstraints gbc_batchSkipButton_ = new GridBagConstraints();
						gbc_batchSkipButton_.insets = new Insets(0, 0, 0, 5);
						gbc_batchSkipButton_.gridx = 2;
						gbc_batchSkipButton_.gridy = 0;
						panel_10.add(batchSkipButton_, gbc_batchSkipButton_);
						batchSkipButton_.setToolTipText("<html>Skip current experiment before starting the next one</html>");
						batchSkipButton_.setIcon(null);
					}
					{
						lblProgress_1 = new JLabel("Progress :");
						GridBagConstraints gbc_lblProgress_1 = new GridBagConstraints();
						gbc_lblProgress_1.insets = new Insets(0, 10, 0, 5);
						gbc_lblProgress_1.gridx = 3;
						gbc_lblProgress_1.gridy = 0;
						panel_10.add(lblProgress_1, gbc_lblProgress_1);
					}
					{
						batchProgress2_ = new JProgressBar();
						GridBagConstraints gbc_progressBar = new GridBagConstraints();
						gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
						gbc_progressBar.gridx = 4;
						gbc_progressBar.gridy = 0;
						batchProgress2_.setPreferredSize(new Dimension(50, batchProgress2_.getPreferredSize().height));
						panel_10.add(batchProgress2_, gbc_progressBar);
						batchProgress2_.setToolTipText("Batch progress");
					}
				}
			}
			{
				JPanel panel = new JPanel();
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 5;
				mainPanel.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					Component verticalStrut = Box.createVerticalStrut(40);
					GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
					gbc_verticalStrut.insets = new Insets(0, 0, 0, 5);
					gbc_verticalStrut.gridx = 0;
					gbc_verticalStrut.gridy = 0;
					panel.add(verticalStrut, gbc_verticalStrut);
				}
				
				// message bar
				msgBar_ = new AppTips();
				msgBar_.setToolTipText("");
				msgBar_.setLeftPreferredSize(new Dimension(18, 0));
				msgBar_.setRightPreferredSize(new Dimension(50, 0));
				GridBagConstraints gbc_msgBar_ = new GridBagConstraints();
				gbc_msgBar_.insets = new Insets(0, 0, 0, 5);
				gbc_msgBar_.fill = GridBagConstraints.BOTH;
				gbc_msgBar_.gridx = 1;
				gbc_msgBar_.gridy = 0;
				panel.add(msgBar_, gbc_msgBar_);
				{
					Component verticalStrut = Box.createVerticalStrut(40);
					GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
					gbc_verticalStrut.gridx = 2;
					gbc_verticalStrut.gridy = 0;
					panel.add(verticalStrut, gbc_verticalStrut);
				}
			}
		}
		{
			structurePanel_ = new JPanel();
			structurePanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(structurePanel_, "STRUCTURE");
			GridBagLayout gbl_structurePanel_ = new GridBagLayout();
			gbl_structurePanel_.columnWidths = new int[]{0, 0};
			gbl_structurePanel_.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_structurePanel_.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_structurePanel_.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
			structurePanel_.setLayout(gbl_structurePanel_);
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Structure Preview", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 0);
				gbc_panel.fill = GridBagConstraints.BOTH;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 0;
				structurePanel_.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					JLabel lblBackgroundImage = new JLabel("Background image:");
					GridBagConstraints gbc_lblBackgroundImage = new GridBagConstraints();
					gbc_lblBackgroundImage.insets = new Insets(0, 5, 5, 5);
					gbc_lblBackgroundImage.anchor = GridBagConstraints.WEST;
					gbc_lblBackgroundImage.gridx = 0;
					gbc_lblBackgroundImage.gridy = 0;
					panel.add(lblBackgroundImage, gbc_lblBackgroundImage);
				}
				{
					backgroundCBox_ = new JComboBox<String>();
					backgroundCBox_.setToolTipText("Set the background image of the structure viewer");
					GridBagConstraints gbc_backgroundCBox_ = new GridBagConstraints();
					gbc_backgroundCBox_.anchor = GridBagConstraints.WEST;
					gbc_backgroundCBox_.gridwidth = 4;
					gbc_backgroundCBox_.insets = new Insets(0, 5, 5, 0);
					gbc_backgroundCBox_.gridx = 1;
					gbc_backgroundCBox_.gridy = 0;
					panel.add(backgroundCBox_, gbc_backgroundCBox_);
				}
				{
					showOverlayStructureCBox_ = new JCheckBox("Show structure model");
					showOverlayStructureCBox_.setSelected(true);
					showOverlayStructureCBox_.setToolTipText("Show structure model in the viewer");
					GridBagConstraints gbc_showOverlayStructureCBox_ = new GridBagConstraints();
					gbc_showOverlayStructureCBox_.insets = new Insets(0, 0, 5, 5);
					gbc_showOverlayStructureCBox_.anchor = GridBagConstraints.WEST;
					gbc_showOverlayStructureCBox_.gridwidth = 3;
					gbc_showOverlayStructureCBox_.gridx = 0;
					gbc_showOverlayStructureCBox_.gridy = 1;
					panel.add(showOverlayStructureCBox_, gbc_showOverlayStructureCBox_);
				}
				{
					horizontalStrut_3 = Box.createHorizontalStrut(20);
					GridBagConstraints gbc_horizontalStrut_3 = new GridBagConstraints();
					gbc_horizontalStrut_3.fill = GridBagConstraints.HORIZONTAL;
					gbc_horizontalStrut_3.insets = new Insets(0, 0, 5, 5);
					gbc_horizontalStrut_3.gridx = 3;
					gbc_horizontalStrut_3.gridy = 1;
					panel.add(horizontalStrut_3, gbc_horizontalStrut_3);
				}
				{
					setColorButton_ = new JButton("Set Color");
					GridBagConstraints gbc_setColorButton_ = new GridBagConstraints();
					gbc_setColorButton_.fill = GridBagConstraints.VERTICAL;
					gbc_setColorButton_.gridx = 4;
					gbc_setColorButton_.gridy = 2;
					panel.add(setColorButton_, gbc_setColorButton_);
					setColorButton_.setToolTipText("Set the color of the structure model and label displayed in the viewer");
				}
				{
					showOverlayInformationCBox_ = new JCheckBox("Show labels");
					showOverlayInformationCBox_.setSelected(true);
					showOverlayInformationCBox_.setToolTipText("Show additional information in the viewer");
					GridBagConstraints gbc_showOverlayInformationCBox_ = new GridBagConstraints();
					gbc_showOverlayInformationCBox_.anchor = GridBagConstraints.WEST;
					gbc_showOverlayInformationCBox_.gridwidth = 3;
					gbc_showOverlayInformationCBox_.insets = new Insets(0, 0, 0, 5);
					gbc_showOverlayInformationCBox_.gridx = 0;
					gbc_showOverlayInformationCBox_.gridy = 2;
					panel.add(showOverlayInformationCBox_, gbc_showOverlayInformationCBox_);
				}
			}
			{
				JPanel panel = new JPanel();
				panel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Structure Edition", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel = new GridBagConstraints();
				gbc_panel.insets = new Insets(0, 0, 5, 0);
				gbc_panel.fill = GridBagConstraints.HORIZONTAL;
				gbc_panel.gridx = 0;
				gbc_panel.gridy = 1;
				structurePanel_.add(panel, gbc_panel);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0};
				gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{1.0, 1.0, 1.0};
				panel.setLayout(gbl_panel);
				{
					JLabel lblPlop = new JLabel("Num. control points :");
					GridBagConstraints gbc_lblPlop = new GridBagConstraints();
					gbc_lblPlop.anchor = GridBagConstraints.WEST;
					gbc_lblPlop.insets = new Insets(0, 5, 5, 5);
					gbc_lblPlop.gridx = 0;
					gbc_lblPlop.gridy = 1;
					panel.add(lblPlop, gbc_lblPlop);
				}
				{
					numStructureControlPoints_ = new JSpinner(m0Model_);
					numStructureControlPoints_.setToolTipText("Number of control points of the parametric structure model");
					GridBagConstraints gbc_numStructureControlPoints_ = new GridBagConstraints();
					gbc_numStructureControlPoints_.anchor = GridBagConstraints.WEST;
					gbc_numStructureControlPoints_.insets = new Insets(0, 0, 5, 5);
					gbc_numStructureControlPoints_.gridx = 1;
					gbc_numStructureControlPoints_.gridy = 1;
					panel.add(numStructureControlPoints_, gbc_numStructureControlPoints_);
					numStructureControlPoints_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)numStructureControlPoints_.getEditor()).getTextField().setColumns(3);
				}
				{
					lblPointsPerSegment = new JLabel("per segment");
					GridBagConstraints gbc_lblPointsPerSegment = new GridBagConstraints();
					gbc_lblPointsPerSegment.anchor = GridBagConstraints.WEST;
					gbc_lblPointsPerSegment.insets = new Insets(0, 5, 5, 0);
					gbc_lblPointsPerSegment.gridx = 2;
					gbc_lblPointsPerSegment.gridy = 1;
					panel.add(lblPointsPerSegment, gbc_lblPointsPerSegment);
				}
				{
					JPanel panel_1 = new JPanel();
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.gridwidth = 3;
					gbc_panel_1.insets = new Insets(0, 5, 0, 5);
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 2;
					panel.add(panel_1, gbc_panel_1);
					panel_1.setLayout(new GridLayout(0, 2, 0, 0));
					{
						editStructureButton_ = new JButton("Edit Structure");
						editStructureButton_.setToolTipText("<html>Edit the shape of the structure model. Change the background image<br>of the viewer if that can help to generate more accurate model.</html>");
						panel_1.add(editStructureButton_);
					}
					{
						swapBoundariesButton_ = new JButton("Swap A-P and D-V");
						swapBoundariesButton_.setToolTipText("Swal the identity of the A-P with the D-V axis");
						panel_1.add(swapBoundariesButton_);
					}
					{
						reverseDVBoundaryButton_ = new JButton("Reverse A-P");
						reverseDVBoundaryButton_.setToolTipText("Reverse the orientation of the A-P axis");
						panel_1.add(reverseDVBoundaryButton_);
					}
					{
						reverseAPBoundaryButton_ = new JButton("Reverse D-V");
						reverseAPBoundaryButton_.setToolTipText("Reverse the orientation of the D-V axis");
						panel_1.add(reverseAPBoundaryButton_);
					}
				}
			}
			{
				JPanel structureDataPanel = new JPanel();
				structureDataPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Structure Measurements", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_structureDataPanel = new GridBagConstraints();
				gbc_structureDataPanel.gridheight = 3;
				gbc_structureDataPanel.insets = new Insets(0, 0, 5, 0);
				gbc_structureDataPanel.fill = GridBagConstraints.BOTH;
				gbc_structureDataPanel.gridx = 0;
				gbc_structureDataPanel.gridy = 2;
				structurePanel_.add(structureDataPanel, gbc_structureDataPanel);
				structureDataPanel.setLayout(new BorderLayout(0, 0));
				{
					structureDatasetScrollPane = new JScrollPane();
					structureDataPanel.add(structureDatasetScrollPane, BorderLayout.CENTER);
					{
						structureDatasetTextPane_ = new JTextPane();
						structureDatasetTextPane_.setEditable(false);
						structureDatasetScrollPane.setViewportView(structureDatasetTextPane_);
					}
				}
				{
					Component horizontalStrut = Box.createHorizontalStrut(5);
					structureDataPanel.add(horizontalStrut, BorderLayout.EAST);
				}
				{
					Component verticalStrut = Box.createVerticalStrut(5);
					structureDataPanel.add(verticalStrut, BorderLayout.SOUTH);
				}
				{
					Component rigidArea = Box.createRigidArea(new Dimension(5, 145));
					structureDataPanel.add(rigidArea, BorderLayout.WEST);
				}
			}
			{
				panel_3 = new JPanel();
				panel_3.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Export", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_panel_3 = new GridBagConstraints();
				gbc_panel_3.insets = new Insets(0, 0, 10, 0);
				gbc_panel_3.fill = GridBagConstraints.BOTH;
				gbc_panel_3.gridx = 0;
				gbc_panel_3.gridy = 5;
				structurePanel_.add(panel_3, gbc_panel_3);
				GridBagLayout gbl_panel_3 = new GridBagLayout();
				gbl_panel_3.columnWidths = new int[]{0, 0};
				gbl_panel_3.rowHeights = new int[]{0, 0};
				gbl_panel_3.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
				panel_3.setLayout(gbl_panel_3);
				{
					panel_4 = new JPanel();
					GridBagConstraints gbc_panel_4 = new GridBagConstraints();
					gbc_panel_4.insets = new Insets(0, 5, 0, 5);
					gbc_panel_4.fill = GridBagConstraints.BOTH;
					gbc_panel_4.gridx = 0;
					gbc_panel_4.gridy = 0;
					panel_3.add(panel_4, gbc_panel_4);
					panel_4.setLayout(new GridLayout(0, 2, 0, 0));
					{
						saveStructureButton_ = new JButton("Structure Model");
						saveStructureButton_.setToolTipText("Save structure model to file.Then the model can be imported in WingJ later");
						panel_4.add(saveStructureButton_);
					}
					{
						exportStructurePropertiesButton_ = new JButton("Measurements");
						exportStructurePropertiesButton_.setToolTipText("Save measurements taken from the structure model to file");
						panel_4.add(exportStructurePropertiesButton_);
					}
					{
						exportBinaryMaskButton_ = new JButton("Mask");
						exportBinaryMaskButton_.setToolTipText("Save a binary mask based on the structure model");
						panel_4.add(exportBinaryMaskButton_);
					}
					{
						exportPreviewImageButton_ = new JButton("Preview");
						exportPreviewImageButton_.setToolTipText("<html>Save a preview of the structure model in TIFF format (content<br>of the structure viewer)</html>");
						panel_4.add(exportPreviewImageButton_);
					}
					{
						exportAllButton_ = new JButton("Dataset");
						exportAllButton_.setToolTipText("<html>Export the entire structure dataset (structure model, measurements,<br> mask and structure preview)</html>");
						panel_4.add(exportAllButton_);
					}
				}
			}
			{
				structureCloseButton_ = new JButton("Close");
				structureCloseButton_.setToolTipText("<html>Close the <i>Structure panel</i> and go back to the main interface</html>");
				GridBagConstraints gbc_structureCloseButton_ = new GridBagConstraints();
				gbc_structureCloseButton_.insets = new Insets(0, 10, 10, 10);
				gbc_structureCloseButton_.fill = GridBagConstraints.HORIZONTAL;
				gbc_structureCloseButton_.gridx = 0;
				gbc_structureCloseButton_.gridy = 6;
				structurePanel_.add(structureCloseButton_, gbc_structureCloseButton_);
			}
		}
		{
			batchPanel_ = new JPanel();
			getContentPane().add(batchPanel_, "BATCH");
			batchPanel_.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel = new JPanel();
				batchPanel_.add(panel, BorderLayout.CENTER);
				GridBagLayout gbl_panel = new GridBagLayout();
				gbl_panel.columnWidths = new int[]{0, 0};
				gbl_panel.rowHeights = new int[]{0, 0, 0};
				gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
				gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
				panel.setLayout(gbl_panel);
				{
					JPanel panel_1 = new JPanel();
					panel_1.setBorder(new TitledBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Batch Experiment", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
					GridBagConstraints gbc_panel_1 = new GridBagConstraints();
					gbc_panel_1.insets = new Insets(5, 5, 10, 5);
					gbc_panel_1.fill = GridBagConstraints.BOTH;
					gbc_panel_1.gridx = 0;
					gbc_panel_1.gridy = 0;
					panel.add(panel_1, gbc_panel_1);
					GridBagLayout gbl_panel_1 = new GridBagLayout();
					gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
					gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
					gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
					gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
					panel_1.setLayout(gbl_panel_1);
					{
						JLabel lblDirectory = new JLabel("Directory :");
						GridBagConstraints gbc_lblDirectory = new GridBagConstraints();
						gbc_lblDirectory.insets = new Insets(0, 5, 5, 5);
						gbc_lblDirectory.anchor = GridBagConstraints.WEST;
						gbc_lblDirectory.gridx = 0;
						gbc_lblDirectory.gridy = 0;
						panel_1.add(lblDirectory, gbc_lblDirectory);
					}
					{
						batchDirectoryTField_ = new JTextField();
						batchDirectoryTField_.setToolTipText("<html>Directory containing multiple experiments.<br>\nSee user manually for the definition of an experiment folder.</html>");
						batchDirectoryTField_.setEditable(false);
						GridBagConstraints gbc_batchDirectoryTField_ = new GridBagConstraints();
						gbc_batchDirectoryTField_.gridwidth = 3;
						gbc_batchDirectoryTField_.insets = new Insets(0, 0, 5, 5);
						gbc_batchDirectoryTField_.fill = GridBagConstraints.HORIZONTAL;
						gbc_batchDirectoryTField_.gridx = 1;
						gbc_batchDirectoryTField_.gridy = 0;
						panel_1.add(batchDirectoryTField_, gbc_batchDirectoryTField_);
						batchDirectoryTField_.setColumns(10);
					}
					{
						batchBrowseButton_ = new JButton("Browse");
						batchBrowseButton_.setToolTipText("<html>Set directory containing multiple experiments.<br>\nSee user manually for the definition of an experiment folder.</html>");
						batchBrowseButton_.setIcon(null);
						GridBagConstraints gbc_batchBrowseButton_ = new GridBagConstraints();
						gbc_batchBrowseButton_.insets = new Insets(0, 0, 5, 5);
						gbc_batchBrowseButton_.gridx = 4;
						gbc_batchBrowseButton_.gridy = 0;
						panel_1.add(batchBrowseButton_, gbc_batchBrowseButton_);
					}
					{
						JLabel lblStartIndex = new JLabel("First :");
						GridBagConstraints gbc_lblStartIndex = new GridBagConstraints();
						gbc_lblStartIndex.anchor = GridBagConstraints.WEST;
						gbc_lblStartIndex.insets = new Insets(0, 5, 5, 5);
						gbc_lblStartIndex.gridx = 0;
						gbc_lblStartIndex.gridy = 1;
						panel_1.add(lblStartIndex, gbc_lblStartIndex);
					}
					{
						batchFirstExperimentIndexSpinner_ = new JSpinner(batchFirstExperimentIndexModel_);
						batchFirstExperimentIndexSpinner_.setToolTipText("First experiment index");
						GridBagConstraints gbc_batchFirstExperimentSpinner_ = new GridBagConstraints();
						gbc_batchFirstExperimentSpinner_.anchor = GridBagConstraints.WEST;
						gbc_batchFirstExperimentSpinner_.insets = new Insets(0, 0, 5, 10);
						gbc_batchFirstExperimentSpinner_.gridx = 1;
						gbc_batchFirstExperimentSpinner_.gridy = 1;
						panel_1.add(batchFirstExperimentIndexSpinner_, gbc_batchFirstExperimentSpinner_);
						batchFirstExperimentIndexSpinner_.setPreferredSize(new Dimension(45, 20));
						((JSpinner.DefaultEditor)batchFirstExperimentIndexSpinner_.getEditor()).getTextField().setColumns(3);
					}
					{
						batchFirstExperimentLabel_ = new JLabel("     ");
						GridBagConstraints gbc_batchFirstExperimentLabel_ = new GridBagConstraints();
						gbc_batchFirstExperimentLabel_.anchor = GridBagConstraints.WEST;
						gbc_batchFirstExperimentLabel_.gridwidth = 3;
						gbc_batchFirstExperimentLabel_.insets = new Insets(0, 0, 5, 5);
						gbc_batchFirstExperimentLabel_.gridx = 2;
						gbc_batchFirstExperimentLabel_.gridy = 1;
						panel_1.add(batchFirstExperimentLabel_, gbc_batchFirstExperimentLabel_);
					}
					{
						JLabel lblLastExperiment = new JLabel("Last :");
						GridBagConstraints gbc_lblLastExperiment = new GridBagConstraints();
						gbc_lblLastExperiment.anchor = GridBagConstraints.WEST;
						gbc_lblLastExperiment.insets = new Insets(0, 5, 5, 5);
						gbc_lblLastExperiment.gridx = 0;
						gbc_lblLastExperiment.gridy = 2;
						panel_1.add(lblLastExperiment, gbc_lblLastExperiment);
					}
					{
						batchLastExperimentIndexSpinner_ = new JSpinner(batchLastExperimentIndexModel_);
						batchLastExperimentIndexSpinner_.setToolTipText("Last experiment index");
						GridBagConstraints gbc_batchLastExperimentSpinner_ = new GridBagConstraints();
						gbc_batchLastExperimentSpinner_.anchor = GridBagConstraints.WEST;
						gbc_batchLastExperimentSpinner_.insets = new Insets(0, 0, 5, 10);
						gbc_batchLastExperimentSpinner_.gridx = 1;
						gbc_batchLastExperimentSpinner_.gridy = 2;
						panel_1.add(batchLastExperimentIndexSpinner_, gbc_batchLastExperimentSpinner_);
						batchLastExperimentIndexSpinner_.setPreferredSize(new Dimension(45, 20));
						((JSpinner.DefaultEditor)batchLastExperimentIndexSpinner_.getEditor()).getTextField().setColumns(3);
					}
					{
						batchLastExperimentLabel_ = new JLabel("     ");
						GridBagConstraints gbc_batchLastExperimentLabel_ = new GridBagConstraints();
						gbc_batchLastExperimentLabel_.anchor = GridBagConstraints.WEST;
						gbc_batchLastExperimentLabel_.gridwidth = 3;
						gbc_batchLastExperimentLabel_.insets = new Insets(0, 0, 5, 5);
						gbc_batchLastExperimentLabel_.gridx = 2;
						gbc_batchLastExperimentLabel_.gridy = 2;
						panel_1.add(batchLastExperimentLabel_, gbc_batchLastExperimentLabel_);
					}
					{
						JLabel lblCurrent = new JLabel("Current :");
						GridBagConstraints gbc_lblCurrent = new GridBagConstraints();
						gbc_lblCurrent.anchor = GridBagConstraints.WEST;
						gbc_lblCurrent.insets = new Insets(0, 5, 5, 5);
						gbc_lblCurrent.gridx = 0;
						gbc_lblCurrent.gridy = 3;
						panel_1.add(lblCurrent, gbc_lblCurrent);
					}
					{
						batchCurrentExperimentIndexSpinner_ = new JSpinner(batchCurrentExperimentIndexModel_);
						batchCurrentExperimentIndexSpinner_.setToolTipText("Current experiment index");
						batchCurrentExperimentIndexSpinner_.setEnabled(false);
						GridBagConstraints gbc_spinner = new GridBagConstraints();
						gbc_spinner.anchor = GridBagConstraints.WEST;
						gbc_spinner.insets = new Insets(0, 0, 5, 10);
						gbc_spinner.gridx = 1;
						gbc_spinner.gridy = 3;
						panel_1.add(batchCurrentExperimentIndexSpinner_, gbc_spinner);
						batchCurrentExperimentIndexSpinner_.setPreferredSize(new Dimension(45, 20));
						((JSpinner.DefaultEditor)batchCurrentExperimentIndexSpinner_.getEditor()).getTextField().setColumns(3);
					}
					{
						batchCurrentExperimentLabel_ = new JLabel("     ");
						GridBagConstraints gbc_batchCurrentExperimentLabel_ = new GridBagConstraints();
						gbc_batchCurrentExperimentLabel_.anchor = GridBagConstraints.WEST;
						gbc_batchCurrentExperimentLabel_.gridwidth = 3;
						gbc_batchCurrentExperimentLabel_.insets = new Insets(0, 0, 5, 5);
						gbc_batchCurrentExperimentLabel_.gridx = 2;
						gbc_batchCurrentExperimentLabel_.gridy = 3;
						panel_1.add(batchCurrentExperimentLabel_, gbc_batchCurrentExperimentLabel_);
					}
					{
						Component verticalStrut = Box.createVerticalStrut(5);
						GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
						gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
						gbc_verticalStrut.gridx = 0;
						gbc_verticalStrut.gridy = 4;
						panel_1.add(verticalStrut, gbc_verticalStrut);
					}
					{
						JLabel lblProgress = new JLabel("Progress :");
						GridBagConstraints gbc_lblProgress = new GridBagConstraints();
						gbc_lblProgress.anchor = GridBagConstraints.WEST;
						gbc_lblProgress.insets = new Insets(0, 5, 5, 5);
						gbc_lblProgress.gridx = 0;
						gbc_lblProgress.gridy = 5;
						panel_1.add(lblProgress, gbc_lblProgress);
					}
					{
						batchProgress_ = new JProgressBar();
						batchProgress_.setToolTipText("Batch progress");
						GridBagConstraints gbc_batchProgress_ = new GridBagConstraints();
						gbc_batchProgress_.insets = new Insets(0, 0, 5, 10);
						gbc_batchProgress_.gridwidth = 4;
						gbc_batchProgress_.fill = GridBagConstraints.BOTH;
						gbc_batchProgress_.gridx = 1;
						gbc_batchProgress_.gridy = 5;
						panel_1.add(batchProgress_, gbc_batchProgress_);
					}
					{
						Component verticalStrut = Box.createVerticalStrut(5);
						GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
						gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
						gbc_verticalStrut.gridx = 0;
						gbc_verticalStrut.gridy = 6;
						panel_1.add(verticalStrut, gbc_verticalStrut);
					}
					{
						JPanel batchButtonsPanel = new JPanel();
						GridBagConstraints gbc_batchButtonsPanel = new GridBagConstraints();
						gbc_batchButtonsPanel.fill = GridBagConstraints.HORIZONTAL;
						gbc_batchButtonsPanel.gridwidth = 5;
						gbc_batchButtonsPanel.insets = new Insets(0, 5, 0, 5);
						gbc_batchButtonsPanel.gridx = 0;
						gbc_batchButtonsPanel.gridy = 7;
						panel_1.add(batchButtonsPanel, gbc_batchButtonsPanel);
						batchButtonsPanel.setLayout(new GridLayout(0, 2, 0, 0));
						{
							batchBeginButton_ = new JButton("Start Batch");
							batchBeginButton_.setToolTipText("Start batch experiment");
							batchBeginButton_.setIcon(null);
							batchButtonsPanel.add(batchBeginButton_);
						}
						{
							batchExitButton_ = new JButton("Stop Batch");
							batchExitButton_.setToolTipText("Stop batch experiment");
							batchButtonsPanel.add(batchExitButton_);
						}
					}
				}
				{
					batchCloseButton_ = new JButton("Close");
					batchCloseButton_.setToolTipText("Go back to main interface");
					GridBagConstraints gbc_batchCloseButton_ = new GridBagConstraints();
					gbc_batchCloseButton_.fill = GridBagConstraints.HORIZONTAL;
					gbc_batchCloseButton_.insets = new Insets(0, 10, 0, 10);
					gbc_batchCloseButton_.gridx = 0;
					gbc_batchCloseButton_.gridy = 1;
					panel.add(batchCloseButton_, gbc_batchCloseButton_);
				}
			}
		}
		{
			expressionPanel_ = new JPanel();
			expressionPanel_.setBorder(new EmptyBorder(5, 5, 5, 5));
			getContentPane().add(expressionPanel_, "EXPRESSION");
			GridBagLayout gbl_expressionPanel = new GridBagLayout();
			gbl_expressionPanel.columnWidths = new int[]{0, 0};
			gbl_expressionPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
			gbl_expressionPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
			gbl_expressionPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
			expressionPanel_.setLayout(gbl_expressionPanel);
			JPanel expressionPanel_1 = new JPanel();
			GridBagConstraints gbc_expressionPanel_1 = new GridBagConstraints();
			gbc_expressionPanel_1.insets = new Insets(0, 0, 5, 0);
			gbc_expressionPanel_1.anchor = GridBagConstraints.NORTH;
			gbc_expressionPanel_1.fill = GridBagConstraints.HORIZONTAL;
			gbc_expressionPanel_1.gridx = 0;
			gbc_expressionPanel_1.gridy = 0;
			expressionPanel_.add(expressionPanel_1, gbc_expressionPanel_1);
			expressionPanel_1.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Expression Quantification", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			GridBagLayout gbl_expressionPanel_1 = new GridBagLayout();
			gbl_expressionPanel_1.columnWidths = new int[]{0, 0, 0, 0};
			gbl_expressionPanel_1.rowHeights = new int[]{0, 0, 0};
			gbl_expressionPanel_1.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0};
			gbl_expressionPanel_1.rowWeights = new double[]{0.0, 0.0, 0.0};
			expressionPanel_1.setLayout(gbl_expressionPanel_1);
			{
				JLabel lblDimension = new JLabel("Dataset:");
				lblDimension.setToolTipText("");
				GridBagConstraints gbc_lblDimension = new GridBagConstraints();
				gbc_lblDimension.anchor = GridBagConstraints.WEST;
				gbc_lblDimension.insets = new Insets(5, 5, 5, 5);
				gbc_lblDimension.gridx = 0;
				gbc_lblDimension.gridy = 0;
				expressionPanel_1.add(lblDimension, gbc_lblDimension);
			}
			{
				expressionDimensionDatasetCBox_ = new JComboBox<String>();
				expressionDimensionDatasetCBox_.setToolTipText("Type of expression dataset");
				expressionDimensionDatasetCBox_.setModel(new DefaultComboBoxModel<String>(new String[] {"Individual profiles", "Individual maps", "Reverse individual maps", "Mean models", "Composite images"}));
				GridBagConstraints gbc_expressionDatasetType_ = new GridBagConstraints();
				gbc_expressionDatasetType_.anchor = GridBagConstraints.WEST;
				gbc_expressionDatasetType_.insets = new Insets(5, 5, 5, 5);
				gbc_expressionDatasetType_.gridx = 1;
				gbc_expressionDatasetType_.gridy = 0;
				expressionPanel_1.add(expressionDimensionDatasetCBox_, gbc_expressionDatasetType_);
			}
			{
				lblNewLabel = new JLabel("");
				GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
				gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
				gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
				gbc_lblNewLabel.gridx = 2;
				gbc_lblNewLabel.gridy = 0;
				expressionPanel_1.add(lblNewLabel, gbc_lblNewLabel);
			}
			{
				expressionDatasetShowButton_ = new JButton("Quantify");
				GridBagConstraints gbc_expressionDatasetShowButton_ = new GridBagConstraints();
				gbc_expressionDatasetShowButton_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionDatasetShowButton_.gridx = 3;
				gbc_expressionDatasetShowButton_.gridy = 0;
				expressionPanel_1.add(expressionDatasetShowButton_, gbc_expressionDatasetShowButton_);
				expressionDatasetShowButton_.setToolTipText("Compute and show expression dataset");
			}
			{
				JLabel lblSelection = new JLabel("Channel:");
				GridBagConstraints gbc_lblSelection = new GridBagConstraints();
				gbc_lblSelection.insets = new Insets(0, 5, 5, 5);
				gbc_lblSelection.anchor = GridBagConstraints.WEST;
				gbc_lblSelection.gridx = 0;
				gbc_lblSelection.gridy = 1;
				expressionPanel_1.add(lblSelection, gbc_lblSelection);
			}
			{
				expressionSelectedChannelCBox_ = new JComboBox<String>();
				expressionSelectedChannelCBox_.setToolTipText("Selected image channel");
				GridBagConstraints gbc_expressionSelectedGeneCBox_ = new GridBagConstraints();
				gbc_expressionSelectedGeneCBox_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionSelectedGeneCBox_.insets = new Insets(0, 5, 5, 5);
				gbc_expressionSelectedGeneCBox_.gridx = 1;
				gbc_expressionSelectedGeneCBox_.gridy = 1;
				expressionPanel_1.add(expressionSelectedChannelCBox_, gbc_expressionSelectedGeneCBox_);
			}
			{
				expressionDatasetHideButton_ = new JButton("Close Windows");
				GridBagConstraints gbc_expressionDatasetHideButton_ = new GridBagConstraints();
				gbc_expressionDatasetHideButton_.insets = new Insets(0, 0, 5, 0);
				gbc_expressionDatasetHideButton_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionDatasetHideButton_.gridx = 3;
				gbc_expressionDatasetHideButton_.gridy = 1;
				expressionPanel_1.add(expressionDatasetHideButton_, gbc_expressionDatasetHideButton_);
				expressionDatasetHideButton_.setToolTipText("Close all expression windows");
			}
			{
				JLabel lblNormalized = new JLabel("Normalize:");
				GridBagConstraints gbc_lblNormalized = new GridBagConstraints();
				gbc_lblNormalized.insets = new Insets(0, 5, 0, 5);
				gbc_lblNormalized.gridx = 0;
				gbc_lblNormalized.gridy = 2;
				expressionPanel_1.add(lblNormalized, gbc_lblNormalized);
			}
			{
				expressionNormalizedCBox_ = new JCheckBox("");
				expressionNormalizedCBox_.setToolTipText("Normalize expression by 255");
				GridBagConstraints gbc_expressionNormalizedCBox_ = new GridBagConstraints();
				gbc_expressionNormalizedCBox_.anchor = GridBagConstraints.WEST;
				gbc_expressionNormalizedCBox_.insets = new Insets(0, 5, 0, 5);
				gbc_expressionNormalizedCBox_.gridx = 1;
				gbc_expressionNormalizedCBox_.gridy = 2;
				expressionPanel_1.add(expressionNormalizedCBox_, gbc_expressionNormalizedCBox_);
			}
			{
				expressionExportDatasetButton_ = new JButton("Export Dataset");
				GridBagConstraints gbc_expressionExportDatasetButton_ = new GridBagConstraints();
				gbc_expressionExportDatasetButton_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionExportDatasetButton_.gridx = 3;
				gbc_expressionExportDatasetButton_.gridy = 2;
				expressionPanel_1.add(expressionExportDatasetButton_, gbc_expressionExportDatasetButton_);
				expressionExportDatasetButton_.setToolTipText("Compute and export expression dataset");
			}
			{
				lblNewLabel_1 = new JLabel("<html>Multiple datasets can be generated in parallel.<br>See the console for process end notification.</html>");
				GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
				gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
				gbc_lblNewLabel_1.gridwidth = 4;
				gbc_lblNewLabel_1.insets = new Insets(0, 5, 0, 5);
				gbc_lblNewLabel_1.gridx = 0;
				gbc_lblNewLabel_1.gridy = 4;
//				expressionPanel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
			}
			{
				expression1DPanel_ = new JPanel();
				expression1DPanel_.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Expression Profiles", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				GridBagConstraints gbc_expression1DPanel_ = new GridBagConstraints();
				gbc_expression1DPanel_.anchor = GridBagConstraints.NORTH;
				gbc_expression1DPanel_.insets = new Insets(0, 0, 10, 0);
				gbc_expression1DPanel_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expression1DPanel_.gridx = 0;
				gbc_expression1DPanel_.gridy = 1;
				expressionPanel_.add(expression1DPanel_, gbc_expression1DPanel_);
				GridBagLayout gbl_expression1DPanel_ = new GridBagLayout();
				gbl_expression1DPanel_.columnWidths = new int[]{0, 0, 0, 0, 0};
				gbl_expression1DPanel_.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
				gbl_expression1DPanel_.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_expression1DPanel_.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				expression1DPanel_.setLayout(gbl_expression1DPanel_);
				{
					JLabel lblBoundary = new JLabel("Boundary:");
					GridBagConstraints gbc_lblBoundary = new GridBagConstraints();
					gbc_lblBoundary.anchor = GridBagConstraints.WEST;
					gbc_lblBoundary.insets = new Insets(0, 5, 5, 5);
					gbc_lblBoundary.gridx = 0;
					gbc_lblBoundary.gridy = 0;
					expression1DPanel_.add(lblBoundary, gbc_lblBoundary);
				}
				{
					expression1DBoundaryCBox_ = new JComboBox<String>();
					expression1DBoundaryCBox_.setToolTipText("Reference boundary");
					expression1DBoundaryCBox_.setModel(new DefaultComboBoxModel<String>(new String[] {"Dorsal/ventral", "Anterior/posterior"}));
					GridBagConstraints gbc_expression1DBoundaryCBox_ = new GridBagConstraints();
					gbc_expression1DBoundaryCBox_.anchor = GridBagConstraints.WEST;
					gbc_expression1DBoundaryCBox_.insets = new Insets(0, 5, 5, 5);
					gbc_expression1DBoundaryCBox_.gridx = 1;
					gbc_expression1DBoundaryCBox_.gridy = 0;
					expression1DPanel_.add(expression1DBoundaryCBox_, gbc_expression1DBoundaryCBox_);
				}
				{
					JLabel lblTranslationOffset = new JLabel("Offset:");
					GridBagConstraints gbc_lblTranslationOffset = new GridBagConstraints();
					gbc_lblTranslationOffset.anchor = GridBagConstraints.WEST;
					gbc_lblTranslationOffset.insets = new Insets(0, 5, 5, 5);
					gbc_lblTranslationOffset.gridx = 0;
					gbc_lblTranslationOffset.gridy = 1;
					expression1DPanel_.add(lblTranslationOffset, gbc_lblTranslationOffset);
				}
				{
					expression1DTranslationSlider_ = new JSlider();
					expression1DTranslationSlider_.setToolTipText("<html>If the reference boundary is D/V, negative/positive values shift the<br> trajectory towards the ventral/dorsal side. If the reference boundary is A/P,<br> negative/positive offsets shift the trajectory towards the anterior/posterior side.</html>");
					GridBagConstraints gbc_expression1DTranslationSlider_ = new GridBagConstraints();
					gbc_expression1DTranslationSlider_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression1DTranslationSlider_.insets = new Insets(0, 5, 5, 5);
					gbc_expression1DTranslationSlider_.gridx = 1;
					gbc_expression1DTranslationSlider_.gridy = 1;
					expression1DPanel_.add(expression1DTranslationSlider_, gbc_expression1DTranslationSlider_);
				}
				{
					expression1DTranslationSpinner_ = new JSpinner(expression1DTranslationModel_);
					expression1DTranslationSpinner_.setToolTipText("<html>If the reference boundary is D/V, negative/positive values shift the<br> trajectory towards the ventral/dorsal side. If the reference boundary is A/P,<br> negative/positive offsets shift the trajectory towards the anterior/posterior side.</html>");
					GridBagConstraints gbc_expression1DTranslationSpinner_ = new GridBagConstraints();
					gbc_expression1DTranslationSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression1DTranslationSpinner_.insets = new Insets(0, 0, 5, 5);
					gbc_expression1DTranslationSpinner_.gridx = 2;
					gbc_expression1DTranslationSpinner_.gridy = 1;
					expression1DPanel_.add(expression1DTranslationSpinner_, gbc_expression1DTranslationSpinner_);
					expression1DTranslationSpinner_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)expression1DTranslationSpinner_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel label = new JLabel("%");
					GridBagConstraints gbc_label = new GridBagConstraints();
					gbc_label.anchor = GridBagConstraints.WEST;
					gbc_label.insets = new Insets(0, 5, 5, 0);
					gbc_label.gridx = 3;
					gbc_label.gridy = 1;
					expression1DPanel_.add(label, gbc_label);
				}
				{
					JLabel lblSigma = new JLabel("Sigma:");
					GridBagConstraints gbc_lblSigma = new GridBagConstraints();
					gbc_lblSigma.anchor = GridBagConstraints.WEST;
					gbc_lblSigma.insets = new Insets(0, 5, 5, 5);
					gbc_lblSigma.gridx = 0;
					gbc_lblSigma.gridy = 2;
					expression1DPanel_.add(lblSigma, gbc_lblSigma);
				}
				{
					expression1DSigmaSlider_ = new JSlider();
					expression1DSigmaSlider_.setToolTipText("<html>Standard deviation of the Gaussian filter<br>\r\nNote: Measurement domain width equals 6*std.</html>");
					GridBagConstraints gbc_expression1DSigmaSlider_ = new GridBagConstraints();
					gbc_expression1DSigmaSlider_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression1DSigmaSlider_.insets = new Insets(0, 5, 5, 5);
					gbc_expression1DSigmaSlider_.gridx = 1;
					gbc_expression1DSigmaSlider_.gridy = 2;
//					expression1DPanel.add(expression1DSigmaSlider_, gbc_expression1DSigmaSlider_);
				}
				{
					expression1DSigmaSpinner_ = new JSpinner(expression1DSigmaModel_);
					expression1DSigmaSpinner_.setToolTipText("<html>Standard deviation of the 1D Gaussian filter used to define the<br> measurement domain width, which is set to 6*std.</html>");
					GridBagConstraints gbc_expression1DSigmaSpinner_ = new GridBagConstraints();
					gbc_expression1DSigmaSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression1DSigmaSpinner_.insets = new Insets(0, 0, 5, 5);
					gbc_expression1DSigmaSpinner_.gridx = 2;
					gbc_expression1DSigmaSpinner_.gridy = 2;
					expression1DPanel_.add(expression1DSigmaSpinner_, gbc_expression1DSigmaSpinner_);
					expression1DSigmaSpinner_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)expression1DSigmaSpinner_.getEditor()).getTextField().setColumns(3);
				}
				{
					JLabel lblPx = new JLabel("px");
					GridBagConstraints gbc_lblPx = new GridBagConstraints();
					gbc_lblPx.anchor = GridBagConstraints.WEST;
					gbc_lblPx.insets = new Insets(0, 5, 5, 0);
					gbc_lblPx.gridx = 3;
					gbc_lblPx.gridy = 2;
					expression1DPanel_.add(lblPx, gbc_lblPx);
				}
				{
					JLabel lblResolution = new JLabel("Resolution:");
					GridBagConstraints gbc_lblResolution = new GridBagConstraints();
					gbc_lblResolution.anchor = GridBagConstraints.WEST;
					gbc_lblResolution.insets = new Insets(0, 5, 5, 5);
					gbc_lblResolution.gridx = 0;
					gbc_lblResolution.gridy = 3;
					expression1DPanel_.add(lblResolution, gbc_lblResolution);
				}
				{
					expression1DResolutionConstantRButton_ = new JRadioButton("Fixed");
					expression1DResolutionConstantRButton_.setToolTipText("Number of measurement points taken along the trajectory");
					expression1DResolutionConstantRButton_.setSelected(true);
					GridBagConstraints gbc_expression1DResolutionConstantRButton_ = new GridBagConstraints();
					gbc_expression1DResolutionConstantRButton_.anchor = GridBagConstraints.WEST;
					gbc_expression1DResolutionConstantRButton_.insets = new Insets(0, 0, 5, 5);
					gbc_expression1DResolutionConstantRButton_.gridx = 1;
					gbc_expression1DResolutionConstantRButton_.gridy = 3;
					expression1DPanel_.add(expression1DResolutionConstantRButton_, gbc_expression1DResolutionConstantRButton_);
				}
				expression1DNumPointsSpinner_ = new JSpinner();
				expression1DNumPointsSpinner_.setToolTipText("Number of measurement points taken along the trajectory");
				expression1DNumPointsSpinner_.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(10), null, new Integer(1)));
				GridBagConstraints gbc_expression1DNumPointsSpinner_ = new GridBagConstraints();
				gbc_expression1DNumPointsSpinner_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expression1DNumPointsSpinner_.insets = new Insets(0, 0, 5, 5);
				gbc_expression1DNumPointsSpinner_.gridx = 2;
				gbc_expression1DNumPointsSpinner_.gridy = 3;
				expression1DPanel_.add(expression1DNumPointsSpinner_, gbc_expression1DNumPointsSpinner_);
				expression1DNumPointsSpinner_.setPreferredSize(new Dimension(60, 20));
				((JSpinner.DefaultEditor)expression1DNumPointsSpinner_.getEditor()).getTextField().setColumns(3);
				{
					JLabel lblPoints = new JLabel("points");
					GridBagConstraints gbc_lblPoints = new GridBagConstraints();
					gbc_lblPoints.anchor = GridBagConstraints.WEST;
					gbc_lblPoints.insets = new Insets(0, 5, 5, 0);
					gbc_lblPoints.gridx = 3;
					gbc_lblPoints.gridy = 3;
					expression1DPanel_.add(lblPoints, gbc_lblPoints);
				}
				{
					expression1DResolutionDynamicRButton_ = new JRadioButton("One point every");
					expression1DResolutionDynamicRButton_.setToolTipText("<html>Distance between to measurement points. The unit is defined by the<br> meta-information contained in the images or as defined in <i>Settings</i>.</html>");
					GridBagConstraints gbc_expression1DResolutionDynamicRButton_ = new GridBagConstraints();
					gbc_expression1DResolutionDynamicRButton_.anchor = GridBagConstraints.WEST;
					gbc_expression1DResolutionDynamicRButton_.insets = new Insets(0, 0, 0, 5);
					gbc_expression1DResolutionDynamicRButton_.gridx = 1;
					gbc_expression1DResolutionDynamicRButton_.gridy = 4;
					expression1DPanel_.add(expression1DResolutionDynamicRButton_, gbc_expression1DResolutionDynamicRButton_);
				}
				{
					expression1DNumPointsPerUnitSpinner_ = new JSpinner();
					expression1DNumPointsPerUnitSpinner_.setToolTipText("<html>Distance between to measurement points. The unit is defined by the<br> meta-information contained in the images or as defined in <i>Settings</i>.</html>");
					expression1DNumPointsPerUnitSpinner_.setModel(new SpinnerNumberModel(1.00, 0.01, null, 0.1));
					GridBagConstraints gbc_expression1DNumPointsPerUnitSpinner_ = new GridBagConstraints();
					gbc_expression1DNumPointsPerUnitSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression1DNumPointsPerUnitSpinner_.insets = new Insets(0, 0, 0, 5);
					gbc_expression1DNumPointsPerUnitSpinner_.gridx = 2;
					gbc_expression1DNumPointsPerUnitSpinner_.gridy = 4;
					expression1DPanel_.add(expression1DNumPointsPerUnitSpinner_, gbc_expression1DNumPointsPerUnitSpinner_);
					expression1DNumPointsPerUnitSpinner_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)expression1DNumPointsPerUnitSpinner_.getEditor()).getTextField().setColumns(3);
				}
				{
					expression1DResolutionDynamicUnit_ = new JLabel("unit");
					GridBagConstraints gbc_expression1DResolutionDynamicUnit_ = new GridBagConstraints();
					gbc_expression1DResolutionDynamicUnit_.insets = new Insets(0, 5, 0, 0);
					gbc_expression1DResolutionDynamicUnit_.anchor = GridBagConstraints.WEST;
					gbc_expression1DResolutionDynamicUnit_.gridx = 3;
					gbc_expression1DResolutionDynamicUnit_.gridy = 4;
					expression1DPanel_.add(expression1DResolutionDynamicUnit_, gbc_expression1DResolutionDynamicUnit_);
				}
				{
				}
			}
			{
				expression2DPanel_ = new JPanel();
				expression2DPanel_.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Expression Maps", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
				GridBagConstraints gbc_expression2DPanel = new GridBagConstraints();
				gbc_expression2DPanel.anchor = GridBagConstraints.NORTH;
				gbc_expression2DPanel.insets = new Insets(0, 0, 10, 0);
				gbc_expression2DPanel.fill = GridBagConstraints.HORIZONTAL;
				gbc_expression2DPanel.gridx = 0;
				gbc_expression2DPanel.gridy = 2;
				expressionPanel_.add(expression2DPanel_, gbc_expression2DPanel);
				GridBagLayout gbl_expression2DPanel = new GridBagLayout();
				gbl_expression2DPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
				gbl_expression2DPanel.rowHeights = new int[]{0, 0, 0};
				gbl_expression2DPanel.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				gbl_expression2DPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
				expression2DPanel_.setLayout(gbl_expression2DPanel);
				{
					lblAp_1 = new JLabel("D/V");
					GridBagConstraints gbc_lblAp_1 = new GridBagConstraints();
					gbc_lblAp_1.anchor = GridBagConstraints.WEST;
					gbc_lblAp_1.insets = new Insets(0, 0, 5, 5);
					gbc_lblAp_1.gridx = 3;
					gbc_lblAp_1.gridy = 0;
					expression2DPanel_.add(lblAp_1, gbc_lblAp_1);
				}
				{
					expression2DStitchingSmoothingRangeSlider_ = new JSlider();
					GridBagConstraints gbc_expression2DStitchingSmoothingRangeSlider_ = new GridBagConstraints();
					gbc_expression2DStitchingSmoothingRangeSlider_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DStitchingSmoothingRangeSlider_.gridwidth = 3;
					gbc_expression2DStitchingSmoothingRangeSlider_.insets = new Insets(0, 5, 5, 5);
					gbc_expression2DStitchingSmoothingRangeSlider_.gridx = 1;
					gbc_expression2DStitchingSmoothingRangeSlider_.gridy = 1;
//					panel.add(expression2DStitchingSmoothingRangeSlider_, gbc_expression2DStitchingSmoothingRangeSlider_);
				}
				{
					label_1 = new JLabel("%");
					GridBagConstraints gbc_label_1 = new GridBagConstraints();
					gbc_label_1.anchor = GridBagConstraints.WEST;
					gbc_label_1.insets = new Insets(0, 5, 5, 0);
					gbc_label_1.gridx = 5;
					gbc_label_1.gridy = 0;
					expression2DPanel_.add(label_1, gbc_label_1);
				}
				{
					expression2DStitchingSmoothingRangeSpinner_ = new JSpinner();
					expression2DStitchingSmoothingRangeSpinner_.setToolTipText("<html>Parameter to smooth the stitching boundaries between the two original maps<br> (obtained for <i>Boundary conserved</i> set to -100 and 100%)</html>");
					expression2DStitchingSmoothingRangeSpinner_.setModel(new SpinnerNumberModel(10.0, 0.0, 100.0, 1.0));
					GridBagConstraints gbc_expression2DStitchingSmoothingRangeSpinner_ = new GridBagConstraints();
					gbc_expression2DStitchingSmoothingRangeSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DStitchingSmoothingRangeSpinner_.insets = new Insets(0, 5, 5, 5);
					gbc_expression2DStitchingSmoothingRangeSpinner_.gridx = 4;
					gbc_expression2DStitchingSmoothingRangeSpinner_.gridy = 1;
					expression2DPanel_.add(expression2DStitchingSmoothingRangeSpinner_, gbc_expression2DStitchingSmoothingRangeSpinner_);
				}
				{
					JLabel label = new JLabel("%");
					GridBagConstraints gbc_label = new GridBagConstraints();
					gbc_label.anchor = GridBagConstraints.WEST;
					gbc_label.insets = new Insets(0, 5, 5, 0);
					gbc_label.gridx = 5;
					gbc_label.gridy = 1;
					expression2DPanel_.add(label, gbc_label);
				}
				{
					JLabel lblSampleRate = new JLabel("Resolution:");
					GridBagConstraints gbc_lblSampleRate = new GridBagConstraints();
					gbc_lblSampleRate.anchor = GridBagConstraints.WEST;
					gbc_lblSampleRate.insets = new Insets(0, 5, 0, 5);
					gbc_lblSampleRate.gridx = 0;
					gbc_lblSampleRate.gridy = 2;
					expression2DPanel_.add(lblSampleRate, gbc_lblSampleRate);
				}
				{
					JLabel lblBoundaryConserved = new JLabel("Boundary conserved:");
					GridBagConstraints gbc_lblBoundaryConserved = new GridBagConstraints();
					gbc_lblBoundaryConserved.anchor = GridBagConstraints.WEST;
					gbc_lblBoundaryConserved.insets = new Insets(0, 5, 5, 5);
					gbc_lblBoundaryConserved.gridx = 0;
					gbc_lblBoundaryConserved.gridy = 0;
					expression2DPanel_.add(lblBoundaryConserved, gbc_lblBoundaryConserved);
				}
				{
					JLabel lblConservedAxis = new JLabel("A/P");
					GridBagConstraints gbc_lblConservedAxis = new GridBagConstraints();
					gbc_lblConservedAxis.anchor = GridBagConstraints.WEST;
					gbc_lblConservedAxis.insets = new Insets(0, 5, 5, 5);
					gbc_lblConservedAxis.gridx = 1;
					gbc_lblConservedAxis.gridy = 0;
					expression2DPanel_.add(lblConservedAxis, gbc_lblConservedAxis);
				}
				{
					expression2DThresholdSlider_ = new JSlider();
					expression2DThresholdSlider_.setToolTipText("<html>If set to -100, a circular expression map is generated where expression is<br> conserved along the A/P boundary. Values in ]-100,100[ try to combine the <br>two original maps (obtained with -100 and 100) to generate a single circular<br> map where expression is conserved along both the A/P and D/V<br> boundaries. However distortions may appear at the location of the stitching.</html>");
					GridBagConstraints gbc_expression2DThresholdSlider_ = new GridBagConstraints();
					gbc_expression2DThresholdSlider_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DThresholdSlider_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DThresholdSlider_.gridx = 2;
					gbc_expression2DThresholdSlider_.gridy = 0;
					expression2DThresholdSlider_.setPreferredSize(new Dimension(100,expression2DThresholdSlider_.getPreferredSize().height));
					expression2DPanel_.add(expression2DThresholdSlider_, gbc_expression2DThresholdSlider_);
				}
				{
					expression2DThresholdSpinner_ = new JSpinner();
					expression2DThresholdSpinner_.setToolTipText("<html>If set to -100, a circular expression map is generated where expression is<br> conserved along the A/P boundary. Values in ]-100,100[ try to combine the <br>two original maps (obtained with -100 and 100) to generate a single circular<br> map where expression is conserved along both the A/P and D/V<br> boundaries. However distortions may appear at the location of the stitching.</html>");
					expression2DThresholdSpinner_.setModel(new SpinnerNumberModel(0.0, -100.0, 100.0, 1.0));
					GridBagConstraints gbc_expression2DThresholdSpinner_ = new GridBagConstraints();
					gbc_expression2DThresholdSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DThresholdSpinner_.insets = new Insets(0, 5, 5, 5);
					gbc_expression2DThresholdSpinner_.gridx = 4;
					gbc_expression2DThresholdSpinner_.gridy = 0;
					expression2DPanel_.add(expression2DThresholdSpinner_, gbc_expression2DThresholdSpinner_);
				}
				{
					JLabel lblStitchingRange = new JLabel("Stitching smoothing:");
					GridBagConstraints gbc_lblStitchingRange = new GridBagConstraints();
					gbc_lblStitchingRange.anchor = GridBagConstraints.WEST;
					gbc_lblStitchingRange.insets = new Insets(0, 5, 5, 5);
					gbc_lblStitchingRange.gridx = 0;
					gbc_lblStitchingRange.gridy = 1;
					expression2DPanel_.add(lblStitchingRange, gbc_lblStitchingRange);
				}
				{
					expression2DNumPointsSpinner_ = new JSpinner();
					expression2DNumPointsSpinner_.setToolTipText("Dimension of the expression map in pixels (square image)");
					GridBagConstraints gbc_expression2DNumPointsSpinner_ = new GridBagConstraints();
					gbc_expression2DNumPointsSpinner_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DNumPointsSpinner_.insets = new Insets(0, 0, 0, 5);
					gbc_expression2DNumPointsSpinner_.gridx = 4;
					gbc_expression2DNumPointsSpinner_.gridy = 2;
					expression2DPanel_.add(expression2DNumPointsSpinner_, gbc_expression2DNumPointsSpinner_);
					expression2DNumPointsSpinner_.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(10), null, new Integer(1)));
					expression2DNumPointsSpinner_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)expression2DNumPointsSpinner_.getEditor()).getTextField().setColumns(3);
					{
						JLabel lblPoints = new JLabel("points");
						GridBagConstraints gbc_lblPoints = new GridBagConstraints();
						gbc_lblPoints.insets = new Insets(0, 5, 0, 0);
						gbc_lblPoints.gridx = 5;
						gbc_lblPoints.gridy = 2;
						expression2DPanel_.add(lblPoints, gbc_lblPoints);
					}
				}
				{
				}
			}
			{
				expression2DReversePanel_ = new JPanel();
				expression2DReversePanel_.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Reverse Expression Maps", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
				GridBagConstraints gbc_expression2DReversePanel_ = new GridBagConstraints();
				gbc_expression2DReversePanel_.anchor = GridBagConstraints.NORTH;
				gbc_expression2DReversePanel_.insets = new Insets(0, 0, 10, 0);
				gbc_expression2DReversePanel_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expression2DReversePanel_.gridx = 0;
				gbc_expression2DReversePanel_.gridy = 3;
				expressionPanel_.add(expression2DReversePanel_, gbc_expression2DReversePanel_);
				GridBagLayout gbl_expression2DReversePanel_ = new GridBagLayout();
				gbl_expression2DReversePanel_.columnWidths = new int[]{0, 0, 0, 0, 0};
				gbl_expression2DReversePanel_.rowHeights = new int[]{0, 0, 0, 0, 0};
				gbl_expression2DReversePanel_.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_expression2DReversePanel_.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
				expression2DReversePanel_.setLayout(gbl_expression2DReversePanel_);
				{
					expression2DReverseCurrentModelRButton_ = new JRadioButton("Use current structure model");
					expression2DReverseCurrentModelRButton_.setToolTipText("<html>Wrap the circular map on the structure model currently<br>\nselected. This model can be edited by clicking on <i>Structure</i><br>\nfrom the main interface.</html>");
					expression2DReverseCurrentModelRButton_.setSelected(true);
					GridBagConstraints gbc_expression2DReverseCurrentModelButton_ = new GridBagConstraints();
					gbc_expression2DReverseCurrentModelButton_.anchor = GridBagConstraints.WEST;
					gbc_expression2DReverseCurrentModelButton_.gridwidth = 3;
					gbc_expression2DReverseCurrentModelButton_.insets = new Insets(0, 5, 5, 5);
					gbc_expression2DReverseCurrentModelButton_.gridx = 0;
					gbc_expression2DReverseCurrentModelButton_.gridy = 0;
					expression2DReversePanel_.add(expression2DReverseCurrentModelRButton_, gbc_expression2DReverseCurrentModelButton_);
				}
				{
					expression2DReverseOtherModelRButton_ = new JRadioButton("Other model:");
					expression2DReverseOtherModelRButton_.setToolTipText("<html>Wrap the circular map on a structure model loaded from file.</html>");
					GridBagConstraints gbc_expression2DReverseOtherModel_ = new GridBagConstraints();
					gbc_expression2DReverseOtherModel_.anchor = GridBagConstraints.WEST;
					gbc_expression2DReverseOtherModel_.insets = new Insets(0, 5, 5, 5);
					gbc_expression2DReverseOtherModel_.gridx = 0;
					gbc_expression2DReverseOtherModel_.gridy = 1;
					expression2DReversePanel_.add(expression2DReverseOtherModelRButton_, gbc_expression2DReverseOtherModel_);
				}
				{
					expression2DReverseStructureTField_ = new JTextField();
					expression2DReverseStructureTField_.setEditable(false);
					GridBagConstraints gbc_expression2DReverseStructureTField_ = new GridBagConstraints();
					gbc_expression2DReverseStructureTField_.gridwidth = 2;
					gbc_expression2DReverseStructureTField_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DReverseStructureTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DReverseStructureTField_.gridx = 1;
					gbc_expression2DReverseStructureTField_.gridy = 1;
					expression2DReversePanel_.add(expression2DReverseStructureTField_, gbc_expression2DReverseStructureTField_);
					expression2DReverseStructureTField_.setColumns(10);
				}
				{
					expression2DReverseStructureBrowse_ = new JButton("Browse");
					expression2DReverseStructureBrowse_.setToolTipText("<html>Select a structure model on which the given circular map<br>\nwill be wrapped.</html>");
					GridBagConstraints gbc_expression2DReverseStructureBrowse_ = new GridBagConstraints();
					gbc_expression2DReverseStructureBrowse_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DReverseStructureBrowse_.gridx = 3;
					gbc_expression2DReverseStructureBrowse_.gridy = 1;
					expression2DReversePanel_.add(expression2DReverseStructureBrowse_, gbc_expression2DReverseStructureBrowse_);
				}
				{
					JLabel lblCircularMap = new JLabel("Circular map:");
					GridBagConstraints gbc_lblCircularMap = new GridBagConstraints();
					gbc_lblCircularMap.insets = new Insets(0, 10, 5, 5);
					gbc_lblCircularMap.anchor = GridBagConstraints.WEST;
					gbc_lblCircularMap.gridx = 0;
					gbc_lblCircularMap.gridy = 2;
					expression2DReversePanel_.add(lblCircularMap, gbc_lblCircularMap);
				}
				{
					expression2DReverseMapTField_ = new JTextField();
					expression2DReverseMapTField_.setEditable(false);
					GridBagConstraints gbc_expression2DReverseMapTField_ = new GridBagConstraints();
					gbc_expression2DReverseMapTField_.gridwidth = 2;
					gbc_expression2DReverseMapTField_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DReverseMapTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expression2DReverseMapTField_.gridx = 1;
					gbc_expression2DReverseMapTField_.gridy = 2;
					expression2DReversePanel_.add(expression2DReverseMapTField_, gbc_expression2DReverseMapTField_);
					expression2DReverseMapTField_.setColumns(10);
				}
				{
					expression2DReverseMapBrowse_ = new JButton("Browse");
					expression2DReverseMapBrowse_.setToolTipText("<html>Select a circular map to wrap on the selected structure model.<br>\nCircular maps are square images including information inside a disc<br>\ncentered in the image and whose diameter is equal to the size of the<br>\nimage. Circular expression maps are typically generated using the expression<br>\ndataset <i>Individual expression map</i>. They can also be other maps<br>\ngenerated using the <i>WingJ Matlab Toolbox</i>.</html>");
					GridBagConstraints gbc_expression2DReverseMapBrowse_ = new GridBagConstraints();
					gbc_expression2DReverseMapBrowse_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DReverseMapBrowse_.gridx = 3;
					gbc_expression2DReverseMapBrowse_.gridy = 2;
					expression2DReversePanel_.add(expression2DReverseMapBrowse_, gbc_expression2DReverseMapBrowse_);
				}
				{
					JLabel lblEquatorUsed = new JLabel("Boundary conserved:");
					GridBagConstraints gbc_lblEquatorUsed = new GridBagConstraints();
					gbc_lblEquatorUsed.insets = new Insets(0, 10, 0, 5);
					gbc_lblEquatorUsed.anchor = GridBagConstraints.WEST;
					gbc_lblEquatorUsed.gridx = 0;
					gbc_lblEquatorUsed.gridy = 3;
					expression2DReversePanel_.add(lblEquatorUsed, gbc_lblEquatorUsed);
				}
				{
					JPanel panel = new JPanel();
					GridBagConstraints gbc_panel = new GridBagConstraints();
					gbc_panel.gridwidth = 2;
					gbc_panel.anchor = GridBagConstraints.WEST;
					gbc_panel.insets = new Insets(0, 0, 0, 5);
					gbc_panel.gridx = 1;
					gbc_panel.gridy = 3;
					expression2DReversePanel_.add(panel, gbc_panel);
					GridBagLayout gbl_panel = new GridBagLayout();
					gbl_panel.columnWidths = new int[]{48, 50, 0};
					gbl_panel.rowHeights = new int[]{23, 0};
					gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
					gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
					panel.setLayout(gbl_panel);
					{
						expression2DReverseEquatorAPRButton_ = new JRadioButton("A/P");
						expression2DReverseEquatorAPRButton_.setToolTipText("<html>Set the equator of the grid used to wrap the circular map along the<br>\nA/P boundary. This choice should be consistent with the <i>Boundary</i><br>\n<i>conserved</i> selected at the time of generating the individual<br>\nexpression map.</html>");
						GridBagConstraints gbc_expression2DReverseEquatorAP_ = new GridBagConstraints();
						gbc_expression2DReverseEquatorAP_.anchor = GridBagConstraints.NORTHWEST;
						gbc_expression2DReverseEquatorAP_.insets = new Insets(0, 0, 0, 5);
						gbc_expression2DReverseEquatorAP_.gridx = 0;
						gbc_expression2DReverseEquatorAP_.gridy = 0;
						panel.add(expression2DReverseEquatorAPRButton_, gbc_expression2DReverseEquatorAP_);
					}
					{
						expression2DReverseEquatorDVRButton_ = new JRadioButton("D/V");
						expression2DReverseEquatorDVRButton_.setSelected(true);
						expression2DReverseEquatorDVRButton_.setToolTipText("<html>Set the equator of the grid used to wrap the circular map along the<br>\nD/V boundary. This choice should be consistent with the <i>Boundary</i><br>\n<i>conserved</i> selected at the time of generating the individual<br>\nexpression map.</html>");
						GridBagConstraints gbc_expression2DReverseEquatorDV = new GridBagConstraints();
						gbc_expression2DReverseEquatorDV.anchor = GridBagConstraints.NORTHWEST;
						gbc_expression2DReverseEquatorDV.gridx = 1;
						gbc_expression2DReverseEquatorDV.gridy = 0;
						panel.add(expression2DReverseEquatorDVRButton_, gbc_expression2DReverseEquatorDV);
					}
				}
			}
			{
				expression2DAggregatedPanel_ = new JPanel();
				expression2DAggregatedPanel_.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)), "Mean models", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
				GridBagConstraints gbc_expression2DCommunityPanel_ = new GridBagConstraints();
				gbc_expression2DCommunityPanel_.anchor = GridBagConstraints.NORTH;
				gbc_expression2DCommunityPanel_.insets = new Insets(0, 0, 10, 0);
				gbc_expression2DCommunityPanel_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expression2DCommunityPanel_.gridx = 0;
				gbc_expression2DCommunityPanel_.gridy = 4;
				expressionPanel_.add(expression2DAggregatedPanel_, gbc_expression2DCommunityPanel_); // THOMAS
				expression2DAggregatedPanel_.setVisible(false); // XXX: Added otherwise the left button of the app tips bar doesn't display on Windows :/
				GridBagLayout gbl_expression2DCommunityPanel_ = new GridBagLayout();
				gbl_expression2DCommunityPanel_.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
				gbl_expression2DCommunityPanel_.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
				gbl_expression2DCommunityPanel_.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_expression2DCommunityPanel_.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
				expression2DAggregatedPanel_.setLayout(gbl_expression2DCommunityPanel_);
				{
					JLabel lblRootExperiment = new JLabel("Experiments root dir.:");
					GridBagConstraints gbc_lblRootExperiment = new GridBagConstraints();
					gbc_lblRootExperiment.anchor = GridBagConstraints.WEST;
					gbc_lblRootExperiment.insets = new Insets(0, 5, 5, 5);
					gbc_lblRootExperiment.gridx = 0;
					gbc_lblRootExperiment.gridy = 0;
					expression2DAggregatedPanel_.add(lblRootExperiment, gbc_lblRootExperiment);
				}
				{
					expressionAggRootTField_ = new JTextField();
					expressionAggRootTField_.setToolTipText("Experiments root directory (see WingJ User Manual)");
					expressionAggRootTField_.setEditable(false);
					GridBagConstraints gbc_expressionComRootTField_ = new GridBagConstraints();
					gbc_expressionComRootTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionComRootTField_.gridwidth = 3;
					gbc_expressionComRootTField_.insets = new Insets(0, 0, 5, 5);
					gbc_expressionComRootTField_.gridx = 1;
					gbc_expressionComRootTField_.gridy = 0;
					expression2DAggregatedPanel_.add(expressionAggRootTField_, gbc_expressionComRootTField_);
					expressionAggRootTField_.setColumns(10);
				}
				{
					expressionComRootBrowse_ = new JButton("Browse");
					expressionComRootBrowse_.setToolTipText("<html>Select an experiments root directory (see WingJ User Manual)</html>");
					GridBagConstraints gbc_expressionComRootBrowse_ = new GridBagConstraints();
					gbc_expressionComRootBrowse_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionComRootBrowse_.insets = new Insets(0, 0, 5, 0);
					gbc_expressionComRootBrowse_.gridx = 4;
					gbc_expressionComRootBrowse_.gridy = 0;
					expression2DAggregatedPanel_.add(expressionComRootBrowse_, gbc_expressionComRootBrowse_);
				}
				{
					JLabel lblStructureFilename = new JLabel("Structures regex:");
					GridBagConstraints gbc_lblStructureFilename = new GridBagConstraints();
					gbc_lblStructureFilename.anchor = GridBagConstraints.WEST;
					gbc_lblStructureFilename.insets = new Insets(0, 5, 5, 5);
					gbc_lblStructureFilename.gridx = 0;
					gbc_lblStructureFilename.gridy = 1;
					expression2DAggregatedPanel_.add(lblStructureFilename, gbc_lblStructureFilename);
				}
				{
					expressionAggStructureTField_ = new JTextField();
					expressionAggStructureTField_.setToolTipText("<html>Regular expression to select the WingJ structure model files<br>(see WingJ User Manual)</html>");
					GridBagConstraints gbc_expressionComStructureTField_ = new GridBagConstraints();
					gbc_expressionComStructureTField_.gridwidth = 3;
					gbc_expressionComStructureTField_.insets = new Insets(0, 0, 5, 5);
					gbc_expressionComStructureTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionComStructureTField_.gridx = 1;
					gbc_expressionComStructureTField_.gridy = 1;
					expression2DAggregatedPanel_.add(expressionAggStructureTField_, gbc_expressionComStructureTField_);
					expressionAggStructureTField_.setColumns(10);
				}
				{
					JLabel lblProjectionFilename = new JLabel("Projections regex:");
					GridBagConstraints gbc_lblProjectionFilename = new GridBagConstraints();
					gbc_lblProjectionFilename.insets = new Insets(0, 5, 5, 5);
					gbc_lblProjectionFilename.anchor = GridBagConstraints.WEST;
					gbc_lblProjectionFilename.gridx = 0;
					gbc_lblProjectionFilename.gridy = 2;
					expression2DAggregatedPanel_.add(lblProjectionFilename, gbc_lblProjectionFilename);
				}
				{
					expressionAggProjectionTField_ = new JTextField();
					expressionAggProjectionTField_.setToolTipText("<html>Regular expression to select the image projections to aggregate<br>(see WingJ User Manual)</html>");
					GridBagConstraints gbc_expressionComProjectionTField_ = new GridBagConstraints();
					gbc_expressionComProjectionTField_.gridwidth = 3;
					gbc_expressionComProjectionTField_.insets = new Insets(0, 0, 5, 5);
					gbc_expressionComProjectionTField_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionComProjectionTField_.gridx = 1;
					gbc_expressionComProjectionTField_.gridy = 2;
					expression2DAggregatedPanel_.add(expressionAggProjectionTField_, gbc_expressionComProjectionTField_);
					expressionAggProjectionTField_.setColumns(10);
				}
				{
					lblResolution_1 = new JLabel("Resolution:");
					GridBagConstraints gbc_lblResolution_1 = new GridBagConstraints();
					gbc_lblResolution_1.anchor = GridBagConstraints.WEST;
					gbc_lblResolution_1.insets = new Insets(0, 5, 5, 5);
					gbc_lblResolution_1.gridx = 0;
					gbc_lblResolution_1.gridy = 3;
					expression2DAggregatedPanel_.add(lblResolution_1, gbc_lblResolution_1);
				}
				{
					expression2DAggResolutionSpinner_ = new JSpinner();
					expression2DAggResolutionSpinner_.setModel(new SpinnerNumberModel(new Integer(1000), new Integer(10), null, new Integer(10)));
					GridBagConstraints gbc_expression2DAggResolution_ = new GridBagConstraints();
					gbc_expression2DAggResolution_.anchor = GridBagConstraints.WEST;
					gbc_expression2DAggResolution_.insets = new Insets(0, 0, 5, 5);
					gbc_expression2DAggResolution_.gridx = 1;
					gbc_expression2DAggResolution_.gridy = 3;
					expression2DAggResolutionSpinner_.setPreferredSize(new Dimension(60, 20));
					((JSpinner.DefaultEditor)expression2DAggResolutionSpinner_.getEditor()).getTextField().setColumns(3);
					expression2DAggregatedPanel_.add(expression2DAggResolutionSpinner_, gbc_expression2DAggResolution_);
				}
				{
					JLabel lblPx = new JLabel("points");
					GridBagConstraints gbc_lblPx = new GridBagConstraints();
					gbc_lblPx.anchor = GridBagConstraints.WEST;
					gbc_lblPx.insets = new Insets(0, 5, 5, 5);
					gbc_lblPx.gridx = 2;
					gbc_lblPx.gridy = 3;
					expression2DAggregatedPanel_.add(lblPx, gbc_lblPx);
				}
				{
					horizontalStrut_1 = Box.createHorizontalStrut(20);
					GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
					gbc_horizontalStrut_1.fill = GridBagConstraints.HORIZONTAL;
					gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 5);
					gbc_horizontalStrut_1.gridx = 3;
					gbc_horizontalStrut_1.gridy = 3;
					expression2DAggregatedPanel_.add(horizontalStrut_1, gbc_horizontalStrut_1);
				}
				{
					expressionComSelectionPanel_ = new JPanel();
					GridBagConstraints gbc_expressionComSelectionPanel_ = new GridBagConstraints();
					gbc_expressionComSelectionPanel_.gridwidth = 3;
					gbc_expressionComSelectionPanel_.insets = new Insets(0, 5, 0, 5);
					gbc_expressionComSelectionPanel_.fill = GridBagConstraints.BOTH;
					gbc_expressionComSelectionPanel_.gridx = 0;
					gbc_expressionComSelectionPanel_.gridy = 4;
					expression2DAggregatedPanel_.add(expressionComSelectionPanel_, gbc_expressionComSelectionPanel_);
					
					expressionComCardLayout_ = new CardLayout(0, 0);
					
					expressionComSelectionPanel_.setLayout(expressionComCardLayout_);
					{
						expressionComSelectionLabel_ = new JLabel("0 individual experiments matching");
						expressionComSelectionLabel_.setToolTipText("<html>Number of experiments detected and suitable for generating an<br> aggregated expression map. An experiment is considered as ambiguous if<br> the number of structure files for this experiment is larger than one, or if<br> similarly if the number of image projections is larger than one (see <b>WingJ<br> User Manual</b>).</html>");
						expressionComSelectionPanel_.add(expressionComSelectionLabel_, "CARD_INDIVIDUAL_EXPERIMENTS_LABEL");
					}
					{
						expressionComSnake_ = new Snake();
						expressionComSnake_.setSnakeCenterX(0);
						expressionComSnake_.setSnakeCenterY(0);
						expressionComSnake_.setNumPathBullets(8);
						expressionComSnake_.setNumSnakeBullets(5);
						expressionComSnake_.setR(5.5f);
						expressionComSnake_.setr(1.5f);
						expressionComSelectionPanel_.add(expressionComSnake_, "CARD_INDIVIDUAL_EXPERIMENTS_SNAKE");
						expressionComSnake_.setLayout(null);
					}
				}
			}
			{
				expressionCompositePanel_ = new JPanel();
				expressionCompositePanel_.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), "Composite images", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
				GridBagConstraints gbc_expressionCompositePanel_ = new GridBagConstraints();
				gbc_expressionCompositePanel_.anchor = GridBagConstraints.NORTH;
				gbc_expressionCompositePanel_.insets = new Insets(0, 0, 10, 0);
				gbc_expressionCompositePanel_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionCompositePanel_.gridx = 0;
				gbc_expressionCompositePanel_.gridy = 5;
				expressionPanel_.add(expressionCompositePanel_, gbc_expressionCompositePanel_);
				GridBagLayout gbl_expressionCompositePanel_ = new GridBagLayout();
				gbl_expressionCompositePanel_.columnWidths = new int[]{0, 0, 0, 0, 0};
				gbl_expressionCompositePanel_.rowHeights = new int[]{0, 0, 0, 0};
				gbl_expressionCompositePanel_.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
				gbl_expressionCompositePanel_.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
				expressionCompositePanel_.setLayout(gbl_expressionCompositePanel_);
				{
					JLabel redColor = new JLabel("Red:");
					GridBagConstraints gbc_redColor = new GridBagConstraints();
					gbc_redColor.anchor = GridBagConstraints.WEST;
					gbc_redColor.insets = new Insets(5, 5, 5, 5);
					gbc_redColor.gridx = 0;
					gbc_redColor.gridy = 0;
					expressionCompositePanel_.add(redColor, gbc_redColor);
					redColor.setToolTipText("");
				}
				{
					expressionCompositeRedCBox_ = new JComboBox<String>();
					expressionCompositeRedCBox_.setModel(new DefaultComboBoxModel<String>(new String[] {"0", "1", "2", "3", "None"}));
					expressionCompositeRedCBox_.setSelectedIndex(0);
					expressionCompositeRedCBox_.setToolTipText("Select the channel to display in red in the composite image");
					GridBagConstraints gbc_expressionCompositeRedCBox_ = new GridBagConstraints();
					gbc_expressionCompositeRedCBox_.insets = new Insets(0, 5, 5, 5);
					gbc_expressionCompositeRedCBox_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionCompositeRedCBox_.gridx = 1;
					gbc_expressionCompositeRedCBox_.gridy = 0;
					expressionCompositePanel_.add(expressionCompositeRedCBox_, gbc_expressionCompositeRedCBox_);
				}
				{
					horizontalStrut_2 = Box.createHorizontalStrut(20);
					GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
					gbc_horizontalStrut_2.fill = GridBagConstraints.HORIZONTAL;
					gbc_horizontalStrut_2.insets = new Insets(0, 0, 5, 5);
					gbc_horizontalStrut_2.gridx = 2;
					gbc_horizontalStrut_2.gridy = 0;
					expressionCompositePanel_.add(horizontalStrut_2, gbc_horizontalStrut_2);
				}
				{
					JLabel lblGreen = new JLabel("Green:");
					GridBagConstraints gbc_lblGreen = new GridBagConstraints();
					gbc_lblGreen.anchor = GridBagConstraints.WEST;
					gbc_lblGreen.insets = new Insets(0, 5, 5, 5);
					gbc_lblGreen.gridx = 0;
					gbc_lblGreen.gridy = 1;
					expressionCompositePanel_.add(lblGreen, gbc_lblGreen);
				}
				{
					expressionCompositeGreenCBox_ = new JComboBox<String>();
					expressionCompositeGreenCBox_.setModel(new DefaultComboBoxModel<String>(new String[] {"0", "1", "2", "3", "None"}));
					expressionCompositeGreenCBox_.setSelectedIndex(1);
					expressionCompositeGreenCBox_.setToolTipText("Select the channel to display in green in the composite image");
					GridBagConstraints gbc_expressionCompositeGreenCBox_ = new GridBagConstraints();
					gbc_expressionCompositeGreenCBox_.insets = new Insets(0, 5, 5, 5);
					gbc_expressionCompositeGreenCBox_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionCompositeGreenCBox_.gridx = 1;
					gbc_expressionCompositeGreenCBox_.gridy = 1;
					expressionCompositePanel_.add(expressionCompositeGreenCBox_, gbc_expressionCompositeGreenCBox_);
				}
				{
					JLabel lblBlue = new JLabel("Blue:");
					GridBagConstraints gbc_lblBlue = new GridBagConstraints();
					gbc_lblBlue.anchor = GridBagConstraints.WEST;
					gbc_lblBlue.insets = new Insets(0, 5, 0, 5);
					gbc_lblBlue.gridx = 0;
					gbc_lblBlue.gridy = 2;
					expressionCompositePanel_.add(lblBlue, gbc_lblBlue);
				}
				{
					expressionCompositeBlueCBox_ = new JComboBox<String>();
					expressionCompositeBlueCBox_.setModel(new DefaultComboBoxModel<String>(new String[] {"0", "1", "2", "3", "None"}));
					expressionCompositeBlueCBox_.setSelectedIndex(2);
					expressionCompositeBlueCBox_.setToolTipText("Select the channel to display in blue in the composite image");
					GridBagConstraints gbc_expressionCompositeBlueCBox_ = new GridBagConstraints();
					gbc_expressionCompositeBlueCBox_.insets = new Insets(0, 5, 0, 5);
					gbc_expressionCompositeBlueCBox_.fill = GridBagConstraints.HORIZONTAL;
					gbc_expressionCompositeBlueCBox_.gridx = 1;
					gbc_expressionCompositeBlueCBox_.gridy = 2;
					expressionCompositePanel_.add(expressionCompositeBlueCBox_, gbc_expressionCompositeBlueCBox_);
				}
			}
			{
				expressionBackButton_ = new JButton("Close");
				expressionBackButton_.setToolTipText("<html>Close the <b>Expression panel</b> and go back to the main interface</html>");
				GridBagConstraints gbc_expressionBackButton_ = new GridBagConstraints();
				gbc_expressionBackButton_.insets = new Insets(0, 10, 5, 10);
				gbc_expressionBackButton_.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionBackButton_.gridx = 0;
				gbc_expressionBackButton_.gridy = 6;
				expressionPanel_.add(expressionBackButton_, gbc_expressionBackButton_);
			}
			{
				expressionSuperSnakePanel = new JPanel();
				GridBagConstraints gbc_expressionSuperSnakePanel = new GridBagConstraints();
				gbc_expressionSuperSnakePanel.anchor = GridBagConstraints.NORTH;
				gbc_expressionSuperSnakePanel.insets = new Insets(0, 0, 5, 0);
				gbc_expressionSuperSnakePanel.fill = GridBagConstraints.HORIZONTAL;
				gbc_expressionSuperSnakePanel.gridx = 0;
				gbc_expressionSuperSnakePanel.gridy = 7;
				expressionPanel_.add(expressionSuperSnakePanel, gbc_expressionSuperSnakePanel);
				expressionSuperSnakePanel.setLayout(new BorderLayout(0, 0));
				
				
				

				expressionSuperSnakePanel.setLayout(new BorderLayout(0, 0));
					{
						expressionSnakePanel_ = new JPanel();
						expressionSuperSnakePanel.add(expressionSnakePanel_, BorderLayout.EAST);
						expressionSnakeLayout_ = new CardLayout(0, 0);
						expressionSnakePanel_.setLayout(expressionSnakeLayout_);
						{
							expressionSnake_ = new Snake();
							expressionSnakePanel_.add(expressionSnake_, "card_snake");
							expressionSnake_.setLayout(new GridBagLayout());
							expressionSnake_.setName("snake_");
						}
						{
							Component rigidArea = Box.createRigidArea(new Dimension(60, 60));
							expressionSnakePanel_.add(rigidArea, "card_decoy");
						}
					}

				
				
				
				
				
				
				
				
				
				
				
			}
		}
		
		// proxies
		minSliceIndexes_ = new ArrayList<JSpinner>();
		minSliceIndexes_.add(ch00MinSliceIndex_);
		minSliceIndexes_.add(ch01MinSliceIndex_);
		minSliceIndexes_.add(ch02MinSliceIndex_);
		minSliceIndexes_.add(ch03MinSliceIndex_);
		
		maxSliceIndexes_ = new ArrayList<JSpinner>();
		maxSliceIndexes_.add(ch00MaxSliceIndex_);
		maxSliceIndexes_.add(ch01MaxSliceIndex_);
		maxSliceIndexes_.add(ch02MaxSliceIndex_);
		maxSliceIndexes_.add(ch03MaxSliceIndex_);
		
		pack();
	}
}