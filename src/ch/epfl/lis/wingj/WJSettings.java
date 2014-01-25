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

import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import ch.epfl.lis.wingj.utilities.ColorUtils;
import ch.epfl.lis.wingj.utilities.FileUtils;
import ch.epfl.lis.wingj.utilities.Projections;
import ch.epfl.lis.wingj.utilities.StringUtils;

import ij.IJ;
import ij.process.ImageConverter;

/** 
 * Offers global parameters (settings) and functions used by WingJ.
 * <p>
 * WJSettings makes use of the Singleton design pattern: There's at most one
 * instance present, which can only be accessed through getInstance().
 * Settings can be loaded/saved from/to a settings file.
 * <p>
 * TODO WingJ 2.0: New design with one object for each parameter.
 * 
 * @version April 15, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJSettings {
	
	/** Set to true to enable DEBUG mode. */
	public static boolean DEBUG = false;
	
	/** File separator used by the OS. */
	public final static String FS = System.getProperty("file.separator");
	
	/** Maximum number of image channels supported. */
	public static final int NUM_CHANNELS = 4;
	/** 1D expression dataset. */
	public static final int EXPRESSION_DATASET_1D = 0;
	/** 2D individual expression dataset. */
	public static final int EXPRESSION_DATASET_2D = 1;
	/** 2D individual expression dataset (reverse). */
	public static final int EXPRESSION_DATASET_2D_REVERSE = 2;
	/** Aggregated 2D expression dataset. */
	public static final int EXPRESSION_DATASET_2D_AGGREGATED = 3;
	/** Composite image. */
	public static final int EXPRESSION_DATASET_COMPOSITE = 4;
	/** D/V boundary of the system structure. */
	public static final int BOUNDARY_DV = 0;
	/** A/P boundary of the system structure. */
	public static final int BOUNDARY_AP = 1;
	
	/** Name of the file containing the slices information (in "images" folder). */
	public static final String SLICE_DATASET_FILENAME = "WingJ_slices.txt"; // "WingJ_slices_middle.txt"
	
	/** Symbol used to distinguish between mutant marker and channel name. */
	public static final String MUTANT_SYMBOL = "-";
	
	/** Absolute difference of scale leading to ask the user if he want to accept the new scale. */
	public static final double SCALE_MAX_ABS_DIFF = 1e-6;
	
	/** Generate 1D expression profiles with always the same number of points. */
	public static final int EXPRESSION_1D_RESOLUTION_CONSTANT = 0;
	/** Generate 1D expression profiles with taking a measurement point every X [UNIT]. */
	public static final int EXPRESSION_1D_RESOLUTION_DYNAMIC = 1;
	
	/** Message displayed when catching OutOfMemoryErrors. */
	public static final String OUT_OF_MEMORY_ERROR_MESSAGE = "There is not enough memory available to run WingJ.\n\n" +
															 "Please refer to the FAQ for more detailed information\n" +
															 "on how to increase the maximum amount of memory that\n" +
															 "the Java Virtual Machine can use (http://wingj.org).";
	
	/** The unique instance of WJSettings (Singleton design pattern). */
	private static WJSettings instance_ = null;
	
	/** Name of the application. */
	private String appName_             				= "WingJ";
	/** Version of the application. */
	private String appVersion_          				= "1.0 Beta";
	/** Minimal version of ImageJ required to run WingJ. */
	private String ijRequiredVersion_					= "1.45j";
	
	// ============================================================================
	// GENERAL
	
	/** Working directory. */
	private String wd_                  				= "";
	/** Output directory. */
	private String od_                  				= "";
	/** Default batch root directory (with terminal '/'). */
	private String batchRootDirectory_					= System.getProperty("user.home");
	/** Path to last settings file opened. */
	private URI lastSettingsFileOpened_  				= null;
	
	/** Channel index of the structure to detect (default: 0). */
	private int structureChannelIndex_ = 0;
	
	/** Shows batch experiment panel. */
	private boolean showBatchExperimentPanel_ 			= false;
	
	// ============================================================================
	// SOURCE IMAGES
	
	/** Experiment name (used as root name for output files). */
	private String experimentName_						= "";
	/** Names of the genes used in WingJ. */
	private List<String> geneNames_ 					= null;
	/** Projection methods for the different channels. */
	private List<Integer> channelProjectionMethod_ 		= null;
	
	/**
	 * Distance UNIT ("nm", "um", "micron", "mm", "cm", "meter", "km" or "inch", default: um)
	 * <p>
	 * IMPORTANT: This parameter is only read at WingJ startup.
	 * */
	private String unit_								= "um";
	/** Scale [px/UNIT]. */
	private double scale_               				= 1.; //0.37841796604334377;
    
	// ============================================================================
	// DROSOPHILA WING POUCH
	// ============================================================================
	// PRE-PROCESSING
	
	/** Expected thickness in px of the A/P and D/V boundary. */
	private double expectedBoundariesThicknessInPixels_ = 20;
	/** Pre-processing threshold. */
	private int ppThld_ 		           				= 140; // 140

	// ============================================================================
	// CENTER DETECTION
	
	/** Minimum number of white pixels required in the structure skeleton. */
	private int minSkeletonSizeInPixels_				= 100;
	/** Center optimizer geometry scale. */
	private double centerOptimizerScale_                = 2.;
	
	// ============================================================================
	// COMPARTMENT BOUNDARIES DETECTION
	
	/** Length in pixel starting from the intersection point between the two compartment boundaries (default: 100). */
	private double kiteSnakeBranchLength_				= 100.;
	/** Width of the kite snake branches in px (if negative value, use expectedBoundariesThicknessInPixels_). */
	private double kiteSnakeBranchWidth_				= -30;
	/** Fluorescence-following tracker step size in px (default: 30-40). */
	private double boundaryTrackerStepSizeInPixels_     = 40; // was 30
	/** Number of steps performed by the fluorescence-following tracker per trajectory to identify. */
	private int boundaryTrackerNumSteps_                = 10;
	/** Fluorescence-following tracker geometry scale. */
	private double boundaryTrackerScale_                = 1.;
	/** Fluorescence-following tracker showtime in seconds or fraction of seconds. */
	private double boundaryTrackerShowDuration_         = 0.5;
	/** Shrinkage of each end of the A/P and D/V boundaries in pixels (default: 2). */
	private double boundaryTrackerShrinkageInPixels_	= 0.; // was 2
    
	// ============================================================================
	// COMPARTMENTS DETECTION
    
    /** Snake blur. */
    private int snakeBlur_              				= 20;
    /** Snake radius. */
    private int snakeRadius_            				= 45;
    /** Snake lambda. */
    private double snakeLambda_         				= 0.85; // was 0.85
    /** Number of nodes for the snake. */
    private int snakeNumNodes_          				= 8;
    /** Projection opacity: alpha, dilated skeleton opacity: 1-alpha. */
    private double snakeAlpha_							= 1.;
    /** Manual detection: radius of the circle used as default wing pouch shape (fraction of min(image width, image height) [0.1,0.4]). */
    private double genericStructureRadius_				= 0.1; // was 0.3
    /** Correction of the location boundaries intersection (default: true). */
	private boolean correctBoundariesIntersection_		= false;
	
	// ============================================================================
	// OUTER BOUNDARY DETECTION
	
	/** Amplitude of the correction in px to apply to the contour of the structure (optional, can also be negative). */
	private double outerBoundaryExpansion_				= expectedBoundariesThicknessInPixels_/2.;
	
	// ============================================================================
	// STRUCTURE MODEL
	
    /** Number of control points per segment (default: 3). */
    private int numStructureControlPoints_				= 3;
    
	// ============================================================================
	// DROSOPHILA EMBRYO
	// ============================================================================
	// COMPARTMENTS DETECTION
    
	/**
	 * Standard deviation of the Gaussian kernel used to smooth the image before
	 * using the snake. Its value should be strictly larger than zero.
	 */
	private double drosophilaEmbryoStdSnakeSmoothing_ = 10;
	/**
	 * Number of control points of the snake. Its value should be larger or equal
	 * than 4.
	 */
	private int drosophilaEmbryoSnakeNumNodes_ = 6;
	/**
	 * Trade-off parameter that weights the contribution of the shape-prior energy
	 * and the image energy. Its value should be larger of equal than zero, and 
	 * strictly smaller than 1.
	 */
	private double drosophilaEmbryoSnakeLambda_ = 0;
	/**
	 * Trade-off parameter that weights the contribution of the thresholded image
	 * and the original image. Its value should be within the range [0,1].
	 */
	private double drosophilaEmbryoSnakeAlpha_ = 0.5;
	
	// ============================================================================
	// 1D EXPRESSION
    
	/** Normalize expression (default: false). */
	private boolean normalizeExpression_						= false;
    
    /** Reference boundary along which expression is measured (0 = D/V boundary, 1 = A/P boundary). */
    private int expression1DBoundary_							= WJSettings.BOUNDARY_DV;
    /** Shift in [um] defining from how far from the reference boundary we are measuring expression. */
    private double expression1DTranslationOffset_ 				= 0.;
    /** Sigma of the 1D Gaussian filter in [UNIT]. */
    private double expression1DSigma_							= 4.;
    
    /** Use a fixed number of points (=0) or take a measurement points every X UNIT (=1). */
    private int expression1DResolutionStrategy_					= WJSettings.EXPRESSION_1D_RESOLUTION_DYNAMIC;
    /** Number of measurement points for 1D expression dataset (default: 1000) */
    private int expression1DNumPoints_							= 1000;
    /** Take one measurement points every X SELECTED_UNIT (default: 1.0 UNIT). */
    private double expression1DStepSize_						= 1.0;
   
    /** Save 1D expression profile in PDF format. */
    private boolean expressionDataset1dSavePdf_					= true;
    /** Save visualization of where 1D expression profile are measured. */
    private boolean expressionDataset1dSaveMeasurementDomain_	= true;
    
    @SuppressWarnings("serial")
	private List<Double> expression1DTranslationOffsets_ = new ArrayList<Double>() {{add(-25.); add(-15.); add(-5.);
    																	add(0.); add(5.); add(15.); add(25.); }};
    
	// ============================================================================
	// 2D EXPRESSION
    
	/** Threshold for the difference between two density maps for grid stitching (default: 3). */
	private double expression2DStitchingDensityDifferenceThld_	= 100.;
	/** Smoothing along stitches (default: 4). */
	private double expression2DStitchingSmoothingRange_			= 5.;
	/** Number of measurement points of the 2D expression maps. */
	private int expression2DNumPoints_							= 1001;
	
	/** Smoothing the grid density, i.e. density of the vertices of the grid (default: 10). */
	private double expression2DStitchingGridDensitySmoothing_	= 10;
	/** Density of the 2D mesh grid for visualization only (default: 0.05). */
	private double expression2DPreviewMeshGridDensity_			= 0.05;
	
	/** Equator to use for generating aggregated expression maps (default: 0 for D/V). */
	private int expression2DAggEquator_                         = WJSettings.BOUNDARY_DV;
	/** Generate standard deviation for aggregated expression maps. */
	private boolean expression2DAggStd_							= false;
	
    /** List of thresholds for stitching [-100., 100.]. */
    @SuppressWarnings("serial")
	private List<Double> expression2DThlds_ 					= new ArrayList<Double>() {{add(-100.); add(0.); add(100.);}};
	
	/** Projection method to use for generating composite images (0: MEAN for all, 1: MAX for all, -1: methods selected from GUI). */
	private int expressionCompositeProjections_ 				= -1;
	
	// ============================================================================
	// BATCH EXPERIMENTS
	
	/** Defines which channels to load automatically in batch mode. */
	private List<Boolean> batchChannelAutoLoading_ 				= null;
	
	// ============================================================================
	// OTHER PARAMETERS

    /** Default color used to draw polygons, ROIs, etc. */
    private Color defaultColor_ 								= Color.GREEN;//Color.YELLOW; //ColorUtils.hex2Rgb("69dff4");
    /** ROI stroke width for final wing pouch structure (default: 3f). */
    private float defaultStrokeWidth_ 							= 1.3f;
    /** Overlay font size coefficient for the wing pouch structure (default: 40). */
    private double structureOverlayFontSizeCoeff_ 				= 40;
    
	// ============================================================================
	// PARAMETERS SAVED BUT NOT LOADED

    /** Path containing the loaded confocal images for each channel. */
    private List<String> channelDirectories_					= null;
	/** Min slice index selected for each channel. */
	private List<Integer> expressionMinSliceIndexes_ 			= null;
	/** Max lsice index selected for each channel. */
	private List<Integer> expressionMaxSliceIndexes_ 			= null;
	/** 2D expression dataset offsets. */
	private List<Double> expression2dOffsets_					= null;
    
    // ============================================================================
    // NOT IN SETTINGS FILE
    /** Stroke width of the kite-snake and other snakes. */
    private float snakeStrokeWidth_ 							= 3.f;
    
    /** WingJ icon. */
    private List<Image> icons_									= null;
    
    public static final String CHARSET 							= "ISO-8859-1";
    
	// ============================================================================
	// PRIVATE METHODS
    
	/** Default constructor. */
	private WJSettings() {
		
		// IJ prefs
		// Do not scale the pixel display range of the images
		// XXX: Uncomment next line
		ImageConverter.setDoScaling(false);
		
		// replace '\' by '/' for Windows ('/' works fine on Win)
		wd_ = System.getProperty("user.home");
		wd_ = wd_.replace("\\", "/") + "/";
		experimentName_ = "my_experiment";
		geneNames_ = new ArrayList<String>();
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
			geneNames_.add("gene" + i);
		od_ = wd_;
		
		channelProjectionMethod_ = new ArrayList<Integer>();
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
			channelProjectionMethod_.add(Projections.PROJECTION_MEAN_METHOD); // default
		channelProjectionMethod_.set(structureChannelIndex_, Projections.PROJECTION_MAX_METHOD); // MIP required for structure detection
		
		channelDirectories_ = new ArrayList<String>();
		batchChannelAutoLoading_ = new ArrayList<Boolean>();
		expressionMinSliceIndexes_ = new ArrayList<Integer>();
		expressionMaxSliceIndexes_ = new ArrayList<Integer>();
		expression2dOffsets_ = new ArrayList<Double>();
		for (int i = 0; i < NUM_CHANNELS; i++) {
			channelDirectories_.add("");
			batchChannelAutoLoading_.add(false);
			expressionMinSliceIndexes_.add(0);
			expressionMaxSliceIndexes_.add(0);
			expression2dOffsets_.add(0.);
		}
		// by default only ch00 and ch01 should be loaded automatically in batch mode
		batchChannelAutoLoading_.set(0, true);
		batchChannelAutoLoading_.set(1, true);
		batchChannelAutoLoading_.set(2, true);
		
		try {
			// set application icon
			// the OS will choose the most suitable one
			icons_ = new ArrayList<Image>();
			URL url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-256.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-128.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-64.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-48.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-32.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-24.png");
			icons_.add(new ImageIcon(url).getImage());
			url = WingJ.class.getResource("/ch/epfl/lis/wingj/gui/rsc/wingj-icon-16.png");
			icons_.add(new ImageIcon(url).getImage());
		} catch (Exception e) {
			WJSettings.log("WARNING: Unable to load icon files.");
		}
		
		// attempt to load the default settings file, if it does exist
		try {
			loadDefaultSettingsFile();
		} catch (Exception e) {
			// do nothing
		}
	}
	
	//----------------------------------------------------------------------------
	
//	/** Bounds the value of the parameter genericShapeRadius. */
//	private double boundParameterValue(double value, double min, double max) {
//		
//		if (value > max) genericStructureRadius_ = max;
//		else if (value < min) genericStructureRadius_ = min;
//		
//		return value;
//	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the default WingJ directory path. */
	private String getWJDirectory() {
		
		return (System.getProperty("user.home") + "/wingj/");
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the URI of the settings file present in the default WingJ directory. */
	private URI getDefaultWJSettingsFileURI() throws MalformedURLException, URISyntaxException {
		
		return FileUtils.getFileURI(getWJDirectory() + "settings.txt");
	}
    
	// ============================================================================
	// PUBLIC METHODS

	/** Gets WJSettings instance. */
	static public WJSettings getInstance() {
		
		if (instance_ == null)
			instance_ = new WJSettings();
		return instance_;
	}
	
	//----------------------------------------------------------------------------

	/** Writes log. */
	public static void log(String str) {
		
		IJ.log(str);
//		if (IJ.getInstance() == null)
//			System.out.println(str);
	}
	
	//----------------------------------------------------------------------------
	
	/** Writes log content to file. */
	public static void writeLog(URI uri) {
		
		try {
			String content = IJ.getLog();
			if (content == null)
				content = "";

			FileWriter fstream = new FileWriter(new File(uri));
			BufferedWriter out = new BufferedWriter(fstream);    
			out.write(content);
			out.close();
			WJSettings.log("[x] Writing  log file (txt)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing log file (txt)");
			WJMessage.showMessage(e);
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Clears log content. */
	public static void clearLog() {
		
    	IJ.log("\\Clear");
	}	
	
	//----------------------------------------------------------------------------
	
	/** Loads WJ settings from a settings file. */
	public void loadSettings(URI uri) throws IOException, Exception {
		
		try {
			loadSettings(uri.toURL().openStream());
			lastSettingsFileOpened_ = uri;
			WJSettings.log("[x] Reading settings file (txt)");
		} catch (IOException e) {
			WJSettings.log("[ ] Reading settings file (txt)");
			throw e;
		} catch (Exception e) {
			WJSettings.log("[ ] Reading settings file (txt)");
			throw e;
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Loads WJ settings from an InputStream. */
	public void loadSettings(InputStream stream) throws Exception {

		boolean validSettings = false;
		
		try {
			Properties p  = new Properties();
			p.load(stream);
			if (validSettings = isValidSettings(p))
				loadSettings(p);
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
		
		if (!validSettings)
			throw new Exception ("WARNING: Invalid settings or settings file.");
	}
	
	//----------------------------------------------------------------------------
	
	/** Loads the last settings file opened. */
	public void loadLastSettingsOpened() throws Exception {
		
		if (lastSettingsFileOpened_ == null)
			throw new Exception("INFO: No settings file opened yet.");
		
		loadSettings(lastSettingsFileOpened_);
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns true if the given Properties object corresponds to WingJ settings. */
	private boolean isValidSettings(Properties p) {
		
		// pick two variables and both of them should be there
		return !(p.getProperty("outputDirectory") == null && p.getProperty("normalizeExpression") == null);
	}
	
	//----------------------------------------------------------------------------
	
	/** Loads WJ settings from settings file. */
	public void loadSettings(Properties p) throws Exception {
			
		// ============================================================================
		// GENERAL
		
		try {
			String od = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("outputDirectory")));
			if (od.equals(""))
				WJMessage.showMessage("WARNING: Parameter outputDirectory not found or empty.");
			else if (!new File(od).isDirectory())
				WJMessage.showMessage("WARNING: Parameter outputDirectory is not a valid directory.");
			else
				od_ = od;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter outputDirectory not found."); }
		
		try {
			int structureChannelIndex = Integer.valueOf(p.getProperty("structureChannelIndex"));
			if (structureChannelIndex < 0 || structureChannelIndex > NUM_CHANNELS-1)
				WJMessage.showMessage("WARNING: Parameter structureChannelIndex must take values between 0 and " + (NUM_CHANNELS-1) + " (reset to 0).");
			else
				structureChannelIndex_ = structureChannelIndex;
		} catch (Exception e) {
			WJMessage.showMessage("WARNING: Parameter structureChannelIndex not found.");
		}
		
		try {
			normalizeExpression_ = (Integer.valueOf(p.getProperty("normalizeExpression")) == 1);
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter normalizeExpression not found."); }
		
		try {
			WJSettings.DEBUG = (Integer.valueOf(p.getProperty("debug")) == 1);
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter debug not found."); }
		
		// ============================================================================
		// SOURCE IMAGES
		
		try {
			String experimentName = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("experimentName")));
			if (experimentName.equals(""))
				WJMessage.showMessage("WARNING: Parameter experimentName not found or empty.");
			else
				experimentName_ = experimentName;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter experimentName not found."); }
		
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			try {
				String name  = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("gene" + i + "Name")));
				if (name.equals(""))
					WJMessage.showMessage("WARNING: Parameter gene" + i + "Name not found or empty.");
				else
					setGeneNames(name, i);
			} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter gene" + i + "Name not found."); }
		}
		
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			try {
				int method = Integer.valueOf(p.getProperty("gene" + i + "ProjectionMethod"));
				if (method < 0 || method > 1)
					WJMessage.showMessage("WARNING: Parameter gene" + i + "ProjectionMethod must take values between 0 and 1.");
				else
					channelProjectionMethod_.set(i, method);
			} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter gene" + i + "ProjectionMethod not found."); }
		}
		
		try {
			String unit = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("unit")));
			if (unit.isEmpty())
				WJMessage.showMessage("WARNING: Parameter unit not found or empty.");
			else
				unit_ = unit;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter unit not found."); }
		
		try {
			double scale = Double.valueOf(p.getProperty("scale"));
			if (scale <= 0)
				WJMessage.showMessage("WARNING: Parameter scale must be positive.");
			else
				scale_ = scale;	
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter scale not found."); }
		
		// ============================================================================
		// DROSOPHILA WING POUCH STRUCTURE DETECTION
		// ============================================================================
		// PRE-PROCESSING
		
		try {
			double expectedBoundariesThicknessInPixels = Double.valueOf(p.getProperty("expectedBoundariesThicknessInPixels"));
			if (expectedBoundariesThicknessInPixels <= 0)
				WJMessage.showMessage("WARNING: Parameter expectedBoundariesThicknessInPixels must be positive.");
			else
				expectedBoundariesThicknessInPixels_ = expectedBoundariesThicknessInPixels;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expectedBoundariesThicknessInPixels not found."); }
		
		try {
			int ppThld = Integer.valueOf(p.getProperty("ppThreshold"));
			if (ppThld < 1 || ppThld > 254)
				WJMessage.showMessage("WARNING: Parameter ppThreshold must be in [1,254].");
			else
				ppThld_ = ppThld;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter ppThreshold not found."); }
		
		// ============================================================================
		// CENTER DETECTION
		
		try {
			int minSkeletonSizeInPixels = Integer.valueOf(p.getProperty("minSkeletonSizeInPixels"));
			if (minSkeletonSizeInPixels <= 0)
				WJMessage.showMessage("WARNING: Parameter minSkeletonSizeInPixels must be positive.");
			else
				minSkeletonSizeInPixels_ = minSkeletonSizeInPixels;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter minSkeletonSizeInPixels not found."); }
		
		try {
			double centerOptimizerScale = Double.valueOf(p.getProperty("centerOptimizerScale"));
			if (centerOptimizerScale <= 0)
				WJMessage.showMessage("WARNING: Parameter centerOptimizerScale must be positive.");
			else
				centerOptimizerScale_ = centerOptimizerScale;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter centerOptimizerScale not found."); }
		
		// ============================================================================
		// COMPARTMENT BOUNDARIES DETECTION
		
		try {
			double kiteSnakeBranchLength = Double.valueOf(p.getProperty("kiteSnakeBranchLength"));
			if (kiteSnakeBranchLength <= 0)
				WJMessage.showMessage("WARNING: Parameter kiteSnakeBranchLength must be positive.");
			else
				kiteSnakeBranchLength_ = kiteSnakeBranchLength;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter kiteSnakeBranchLength not found."); }
		
		try {
			// can take any values
			kiteSnakeBranchWidth_ = Double.valueOf(p.getProperty("kiteSnakeBranchWidth"));
		} catch (Exception e) {
			WJMessage.showMessage("WARNING: Parameter kiteSnakeBranchWidth not found.");
		}
		
		try {
			double boundaryTrackerStepSizeInPixels = Double.valueOf(p.getProperty("boundaryTrackerStepSizeInPixels"));
			if (boundaryTrackerStepSizeInPixels <= 0)
				WJMessage.showMessage("WARNING: Parameter boundaryTrackerStepSizeInPixels must be positive.");
			else
				boundaryTrackerStepSizeInPixels_ = boundaryTrackerStepSizeInPixels;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter boundaryTrackerStepSizeInPixels not found."); }
		
		try {
			double boundaryTrackerScale = Double.valueOf(p.getProperty("boundaryTrackerScale"));
			if (boundaryTrackerScale <= 0)
				WJMessage.showMessage("WARNING: Parameter boundaryTrackerScale must be positive.");
			else
				boundaryTrackerScale_ = boundaryTrackerScale;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter boundaryTrackerScale not found."); }
		
		try {
			double boundaryTrackerShowDuration = Double.valueOf(p.getProperty("boundaryTrackerShowDuration"));
			if (boundaryTrackerShowDuration < 0)
				WJMessage.showMessage("WARNING: Parameter boundaryTrackerShowDuration must be positive or zero.");
			else
				boundaryTrackerShowDuration_ = boundaryTrackerShowDuration;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter boundaryTrackerShowDuration not found."); }
		
		try {
			int boundaryTrackerNumSteps = Integer.valueOf(p.getProperty("boundaryTrackerNumSteps"));
			if (boundaryTrackerNumSteps <= 0)
				WJMessage.showMessage("WARNING: Parameter boundaryTrackerNumSteps must be positive.");
			else
				boundaryTrackerNumSteps_ = boundaryTrackerNumSteps;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter boundaryTrackerNumSteps not found."); }
		
//		try {
//			double boundaryTrackerShrinkageInPixels = Double.valueOf(p.getProperty("boundaryTrackerShrinkageInPixels"));
//			if (boundaryTrackerShrinkageInPixels < 0)
//				WJMessage.showMessage("WARNING: Parameter boundaryTrackerShrinkageInPixels must be positive or zero.");
//			else
//				boundaryTrackerShrinkageInPixels_ = boundaryTrackerShrinkageInPixels;
//		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter boundaryTrackerShrinkageInPixels not found."); }
	
		// ============================================================================
		// DETECTING COMPARTMENTS
	    
		try {
			int snakeBlur = Integer.valueOf(p.getProperty("snakeBlur"));
			if (snakeBlur < 0)
				WJMessage.showMessage("WARNING: Parameter snakeBlur must be positive or zero.");
			else
				snakeBlur_ = snakeBlur;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter snakeBlur not found."); }
		
		try {
			int snakeRadius = Integer.valueOf(p.getProperty("snakeRadius"));
			if (snakeRadius <= 0)
				WJMessage.showMessage("WARNING: Parameter snakeRadius must be positive.");
			else
				snakeRadius_ = snakeRadius;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter snakeRadius not found."); }
		
		try {
			double snakeLambda = Double.valueOf(p.getProperty("snakeLambda"));
			if (snakeLambda < 0 || snakeLambda >= 1)
				WJMessage.showMessage("WARNING: Parameter snakeLambda must be in [0,1[.");
			else
				snakeLambda_ = snakeLambda;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter snakeLambda not found."); }
		
		try {
			double snakeAlpha = Double.valueOf(p.getProperty("snakeAlpha"));
			if (snakeAlpha < 0 || snakeAlpha > 1)
				WJMessage.showMessage("WARNING: Parameter snakeAlpha must be in [0,1].");
			else
				snakeAlpha_ = snakeAlpha;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter snakeAlpha not found."); }
		
		try {
			int snakeNumNodes = Integer.valueOf(p.getProperty("snakeNumNodes"));
			if (snakeNumNodes < 4)
				WJMessage.showMessage("WARNING: Parameter snakeNumNodes must be larger or equal to 4.");
			else
				snakeNumNodes_ = snakeNumNodes;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter snakeNumNodes not found."); }
		
		// ============================================================================
		// OUTER BOUNDARY DETECTION
		
		try {
			double outerBoundaryExpansion = Double.valueOf(p.getProperty("outerBoundaryExpansion"));
			if (outerBoundaryExpansion < 0)
				WJMessage.showMessage("WARNING: Parameter outerBoundaryExpansion must be positive or zero.");
			else
				outerBoundaryExpansion_ = outerBoundaryExpansion;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter outerBoundaryExpansion not found."); }
	    
		// ============================================================================
		// DROSOPHILA WING POUCH MODEL
		
		try {
			int numStructureControlPoints = Integer.valueOf(p.getProperty("numStructureControlPoints"));
			if (numStructureControlPoints < 3)
				WJMessage.showMessage("WARNING: Parameter numStructureControlPoints must be larger or equal to 3.");
			else
				numStructureControlPoints_ = numStructureControlPoints;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter numStructureControlPoints not found."); }
		
		try {
			double genericStructureRadius = Double.valueOf(p.getProperty("genericStructureRadius"));
			if (genericStructureRadius <= 0)
				WJMessage.showMessage("WARNING: Parameter genericStructureRadius must be positive.");
			else
				genericStructureRadius_ = genericStructureRadius;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter genericStructureRadius not found."); }
		
		// ============================================================================
		// DROSOPHILA EMBRYO
		// ============================================================================
		// COMPARTMENTS DETECTION
		
		try {
			double drosophilaEmbryoStdSnakeSmoothing = Double.valueOf(p.getProperty("drosophilaEmbryoStdSnakeSmoothing"));
			if (drosophilaEmbryoStdSnakeSmoothing <= 0)
				WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoStdSnakeSmoothing must be positive.");
			else
				drosophilaEmbryoStdSnakeSmoothing_ = drosophilaEmbryoStdSnakeSmoothing;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoStdSnakeSmoothing not found."); }
		
		try {
			int drosophilaEmbryoSnakeNumNodes = Integer.valueOf(p.getProperty("drosophilaEmbryoSnakeNumNodes"));
			if (drosophilaEmbryoSnakeNumNodes < 4)
				WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeNumNodes must be larger or equal to 4.");
			else
				drosophilaEmbryoSnakeNumNodes_ = drosophilaEmbryoSnakeNumNodes;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeNumNodes not found."); }
		
		try {
			double drosophilaEmbryoSnakeLambda = Double.valueOf(p.getProperty("drosophilaEmbryoSnakeLambda"));
			if (drosophilaEmbryoSnakeLambda < 0 || drosophilaEmbryoSnakeLambda >= 1)
				WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeLambda must be in [0,1[.");
			else
				drosophilaEmbryoSnakeLambda_ = drosophilaEmbryoSnakeLambda;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeLambda not found."); }
		
		try {
			double drosophilaEmbryoSnakeAlpha = Double.valueOf(p.getProperty("drosophilaEmbryoSnakeAlpha"));
			if (drosophilaEmbryoSnakeAlpha < 0 || drosophilaEmbryoSnakeAlpha > 1)
				WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeAlpha must be positive.");
			else
				drosophilaEmbryoSnakeAlpha_ = drosophilaEmbryoSnakeAlpha;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter drosophilaEmbryoSnakeAlpha not found."); }
		
		// ============================================================================
		// 1D EXPRESSION
	    
		try {
			double expression1DSigma = Double.valueOf(p.getProperty("expression1DSigma"));
			if (expression1DSigma < 1)
				WJMessage.showMessage("WARNING: Parameter expression1DSigma must be larger or equal to 1.");
			else
				expression1DSigma_ = expression1DSigma;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DSigma not found."); }
		
		try {
			double expression1DTranslationOffset = Double.valueOf(p.getProperty("expression1DTranslationOffset"));
			if (expression1DTranslationOffset < -100 || expression1DTranslationOffset > 100)
				WJMessage.showMessage("WARNING: Parameter expression1DTranslationOffset must take values between -100 and 100.");
			else
				expression1DTranslationOffset_ = expression1DTranslationOffset;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DTranslationOffset not found."); }
		
		try {
			int expression1DBoundary = Integer.valueOf(p.getProperty("expression1DBoundary"));
			if (expression1DBoundary < 0 || expression1DBoundary_ > 1)
				WJMessage.showMessage("WARNING: Parameter expression1DBoundary must be 0 or 1.");
			else
				expression1DBoundary_ = expression1DBoundary;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DBoundary not found."); }
		
		try {
			int expression1DResolutionStrategy = Integer.valueOf(p.getProperty("expression1DResolutionStrategy"));
			if (expression1DResolutionStrategy < 0 || expression1DResolutionStrategy > 1)
				WJMessage.showMessage("WARNING: Parameter expression1DResolutionStrategy must be 0 or 1.");
			else
				expression1DResolutionStrategy_ = expression1DResolutionStrategy;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DResolutionStrategy not found."); }
		
		try {
			int expression1DNumPoints = Integer.valueOf(p.getProperty("expression1DNumPoints"));
			if (expression1DNumPoints <= 1)
				WJMessage.showMessage("WARNING: Parameter expression1DNumPoints must be positive.");
			else
				expression1DNumPoints_ = expression1DNumPoints;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DNumPoints not found."); }
		
		try {
			double expression1DStepSize = Double.valueOf(p.getProperty("expression1DStepSize"));
			if (expression1DStepSize <= 0)
				WJMessage.showMessage("WARNING: Parameter expression1DStepSize must be positive.");
			else
				expression1DStepSize_ = expression1DStepSize;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DStepSize not found."); }
		
//		try {
//			setExpression1DTranslationOffsets(StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("expression1DTranslationOffsets"))));
//		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression1DTranslationOffsets not found."); }
		
		try {
			expressionDataset1dSavePdf_ = (Integer.valueOf(p.getProperty("expressionDataset1dSavePdf")) == 1);
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expressionDataset1dSavePdf not found."); }
		
		try {
			expressionDataset1dSaveMeasurementDomain_ = (Integer.valueOf(p.getProperty("expressionDataset1dSaveMeasurementDomain")) == 1);
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expressionDataset1dSaveMeasurementDomain not found."); }
	    
		// ============================================================================
		// 2D EXPRESSION
		
		try {
			int expression2DNumPoints = Integer.valueOf(p.getProperty("expression2DNumPoints"));
			if (expression2DNumPoints < 1)
				WJMessage.showMessage("WARNING: Parameter expression2DNumPoints must be positive.");
			else
				expression2DNumPoints_ = expression2DNumPoints;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DNumPoints not found."); }
		
//		try {
//			expression2DStitchingGridDensitySmoothing_ = Double.valueOf(p.getProperty("expression2DStitchingGridDensitySmoothing"));
//			if (expression2DStitchingGridDensitySmoothing_ < 0)
//				throw new Exception("WARNING: Parameter expression2DStitchingGridDensitySmoothing must be positive or zero.");
//		} catch (Exception e) {
//			if (e.getMessage().isEmpty()) WJMessage.showMessage("WARNING: Parameter expression2DStitchingGridDensitySmoothing not found.");
//			else WJMessage.showMessage(e.getMessage());
//		}
		
		try {
			double expression2DStitchingDensityDifferenceThld = Double.valueOf(p.getProperty("expression2DStitchingDensityDifferenceThld"));
			if (expression2DStitchingDensityDifferenceThld < -100 || expression2DStitchingDensityDifferenceThld > 100)
				WJMessage.showMessage("WARNING: Parameter expression2DStitchingDensityDifferenceThld must take values between -100 and 100.");
			else
				expression2DStitchingDensityDifferenceThld_ = expression2DStitchingDensityDifferenceThld;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DStitchingDensityDifferenceThld not found."); }
		
		try {
			double expression2DStitchingSmoothingRange = Double.valueOf(p.getProperty("expression2DStitchingSmoothingRange"));
			if (expression2DStitchingSmoothingRange  < 0)
				WJMessage.showMessage("WARNING: Parameter expression2DStitchingSmoothingRange must be positive or zero.");
			else
				expression2DStitchingSmoothingRange_ = expression2DStitchingSmoothingRange;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DStitchingSmoothingRange not found."); }
		
		try {
			double expression2DPreviewMeshGridDensity = Double.valueOf(p.getProperty("expression2DPreviewMeshGridDensity"));
			if (expression2DPreviewMeshGridDensity <= 0)
				WJMessage.showMessage("WARNING: Parameter expression2DPreviewMeshGridDensity must be positive.");
			else
				expression2DPreviewMeshGridDensity_ = expression2DPreviewMeshGridDensity;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DPreviewMeshGridDensity not found."); }
		
		// ============================================================================
		// STRUCTURE AND EXPRESSION AGGREGATED MODELS (MEAN MODELS)
		
		try {
			int expression2DAggEquator = Integer.valueOf(p.getProperty("expression2DAggEquator"));
			if (expression2DAggEquator < 0 || expression2DAggEquator > 1)
				WJMessage.showMessage("WARNING: Parameter expression2DAggEquator must be 0 or 1.");
			else
				expression2DAggEquator_ = expression2DAggEquator;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DAggEquator not found."); }
		
		try {
			expression2DAggStd_ = (Integer.valueOf(p.getProperty("expression2DAggStd")) == 1);
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expression2DAggStd not found."); }
		
		// ============================================================================
		// COMPOSITE IMAGE
		
		try {
			int expressionCompositeProjections = Integer.valueOf(p.getProperty("expressionCompositeProjections"));
			if (expressionCompositeProjections < -1 || expressionCompositeProjections > 1)
				WJMessage.showMessage("WARNING: Parameter expressionCompositeProjections must be -1, 0 or 1.");
			else
				expressionCompositeProjections_ = expressionCompositeProjections;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expressionCompositeProjections not found."); }
	    
		// ============================================================================
		// MISC PARAMETERS
		
		try {
			defaultColor_ = ColorUtils.hex2Rgb(StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(p.getProperty("defaultColor"))));
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter defaultColor not found or invalid."); }
		
		try {
			float defaultStrokeWidth = Float.valueOf(p.getProperty("defaultStrokeWidth"));
			if (defaultStrokeWidth <= 0.f)
				WJMessage.showMessage("WARNING: Parameter defaultStrokeWidth must be positive.");
			else
				defaultStrokeWidth_ = defaultStrokeWidth;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter defaultStrokeWidth not found."); }
		
		try {
			double structureOverlayFontSizeCoeff = Double.valueOf(p.getProperty("structureOverlayFontSizeCoeff"));
			if (structureOverlayFontSizeCoeff < 0)
				WJMessage.showMessage("WARNING: Parameter structureOverlayFontSizeCoeff must be positive or zero.");
			else
				structureOverlayFontSizeCoeff_ = structureOverlayFontSizeCoeff;
		} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter structureOverlayFontSizeCoeff not found."); }
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves WJ settings to settings file. */
	public void saveSettings(URI uri) {
		
		OutputStreamWriter outputStreamWriter = null;
		PrintWriter out = null;
		try {
//	    	FileWriter fstream = new FileWriter(new File(uri));
//	    	BufferedWriter out = new BufferedWriter(fstream);
//	    	out.write(settings2String());
//	    	out.close();
	    	outputStreamWriter = new OutputStreamWriter(new FileOutputStream(new File(uri)), CHARSET);
    		out = new PrintWriter(new BufferedWriter(outputStreamWriter));
    		out.print(settings2String());
    		out.close();

	    	WJSettings.log("[x] Writing settings file (txt)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing settings file (txt)");
			WJMessage.showMessage(e);
		} finally {
			if (out != null)
				out.close();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the content of the settings as String. */
	public String settings2String() {
		
		// XXX
		String content = "";
	    content += "#########################################\n";
	    content += "# WingJ settings file\n";
	    content += "# Project website: lis.epfl.ch/wingj\n";
	    content += "#########################################\n";
	    content += "\n";
	    content += "# Comments must start with the char '#'\n";
	    content += "# Boolean values: 0 => false, 1 => true\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# GENERAL\n";
	    content += "# Output directory (default: user home directory)\n";
	    content += "outputDirectory = \"" + od_ + "\"\n";
	    content += "# Channel index of the structure to detect ([0,3], default: 0)\n";
	    content += "structureChannelIndex = " + Integer.toString(structureChannelIndex_) + "\n";
	    content += "# Normalize expression values (boolean, default: false)\n";
	    content += "normalizeExpression = " + Integer.toString(normalizeExpression_ ? 1 : 0) + "\n";
	    content += "# Debug mode (default: false)\n";
	    content += "debug = " + Integer.toString(WJSettings.DEBUG ? 1 : 0) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# SOURCE IMAGES\n";
	    content += "# Name of the experiment\n";
	    content += "experimentName = \"" + experimentName_ + "\"\n";
	    content += "# Name of the gene or protein to use for each channel (also called channel name)\n";
	    for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
	    	content += "gene" + i + "Name = \"" + geneNames_.get(i) + "\"\n";	    
	    content += "# Intensity projection method for each channel (0 = MEAN, 1 = MAX)\n";
	    for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
	    	content += "gene" + i + "ProjectionMethod = " + channelProjectionMethod_.get(i) + "\n";
		content += "# Distance unit (\"nm\", \"um\", \"micron\", \"mm\", \"cm\", \"meter\", \"km\" or \"inch\", default: um)\n";
		content += "unit = \"" + unit_ + "\"\n";
	    content += "# Scale [px/UNIT]\n";
	    content += "scale = " + Double.toString(scale_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# DROSOPHILA WING POUCH\n";
	    content += "#========================================\n";
	    content += "# PRE-PROCESSING\n";
	    content += "# Expected thickness in px of the A/P and D/V boundary (>0, default: 20)\n";
	    content += "expectedBoundariesThicknessInPixels = " + Double.toString(expectedBoundariesThicknessInPixels_) + "\n";
	    content += "# This parameter is used to generate a thresholded version of the image for extracting dominant features. This value is evaluated automatically during pre-processing. ([1,254], default: 140])\n";
	    content += "ppThreshold = " + Integer.toString(ppThld_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# DETECTING WING POUCH CENTER\n";
	    content += "# Minimum number of white pixels required in the structure skeleton (>0, default: 100)\n";
	    content += "minSkeletonSizeInPixels = " + Integer.toString(minSkeletonSizeInPixels_) + "\n";
		content += "# This parameter scales the size of the optimizer for detecting fluorescent cross-like shapes (>0, default: 2) '+'.\n";
		content += "centerOptimizerScale = " + Double.toString(centerOptimizerScale_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# DETECTING A/P AND D/V COMPARTMENT BOUNDARIES\n";
		content += "# Initial length in px of the arms of the kite snake used to identify the direction of the arms of a cross-like shape. (>0, default: 100)\n";
		content += "kiteSnakeBranchLength = " + Double.toString(kiteSnakeBranchLength_) + "\n";
		content += "# Width of the arms or branches of the kite snake in px (>0, if negative value, expectedBoundariesThicknessInPixels_ is used)\n";
		content += "kiteSnakeBranchWidth = " + Double.toString(kiteSnakeBranchWidth_) + "\n";
		content += "# Step size in px of the fluorescent boundary trackers (>0, default: 40)\n";
		content += "boundaryTrackerStepSizeInPixels = " + Double.toString(boundaryTrackerStepSizeInPixels_) + "\n";
	    content += "# Number of steps performed by the fluorescent boundary trackers along a trajectory to identify (>0, default: 10)\n";
	    content += "boundaryTrackerNumSteps = " + Integer.toString(boundaryTrackerNumSteps_) + "\n";
		content += "# Fluorescent boundary trackers: geometry scale (>0, default: 1)\n";
		content += "boundaryTrackerScale = " + Double.toString(boundaryTrackerScale_) + "\n";
		content += "# Fluorescent boundary trackers: showtime in seconds or fraction of seconds (default: 0.5)\n";
		content += "boundaryTrackerShowDuration = " + Double.toString(boundaryTrackerShowDuration_) + "\n";
//		content += "# Shrinkage of each end of the A/P and D/V boundaries identified by the tracker in pixels (>=0, default: 2)\n";
//		content += "boundaryTrackerShrinkageInPixels = " + Double.toString(boundaryTrackerShrinkageInPixels_) + "\n";
		content += "\n";
	    content += "#========================================\n";
	    content += "# DETECTING COMPARTMENTS\n";
	    content += "# Standard deviation of the Gaussian kernel used to smooth the image before using the snakes ([0,100], default: 20)\n";
	    content += "snakeBlur = " + Integer.toString(snakeBlur_) + "\n";
	    content += "# Radius in px defining the initial shape of the snakes (default: 45)\n";
	    content += "snakeRadius = " + Integer.toString(snakeRadius_) + "\n";
	    content += "# Number of nodes defining the snake (>=4, default: 8)\n";
	    content += "snakeNumNodes = " + Integer.toString(snakeNumNodes_) + "\n";
	    content += "# Trade-off parameter that weights the contribution of the image energy and the shape-prior energy. Low values of lambda give more weight to the image energy (more sensitive to image gradients) and high values give more weight to the shape regularization (preserves convexity). ([0,1[, default: 0.85)\n";
	    content += "snakeLambda = " + Double.toString(snakeLambda_) + "\n";
	    content += "# Trade-off parameter that weights the contribution of the thresholded image and the original image. Low values of alpha give more weight to the skeleton image and high values give more weight to image gradient ([0,1], default: 1)\n";
	    content += "snakeAlpha = " + Double.toString(snakeAlpha_) + "\n";
		content += "\n";
	    content += "#========================================\n";
	    content += "# DETECTING OUTER BOUNDARY\n";
	    content += "# Amplitude of the correction in px to apply to the contour of the structure. A suitable value should be about expectedBoundariesThicknessInPixels/2 (optional, can also be negative)\n";
	    content += "outerBoundaryExpansion = " + Double.toString(outerBoundaryExpansion_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# STRUCTURE MODEL\n";
	    content += "# Number of control points (yellow '+') per segment (>=3, default: 3)\n";
	    content += "numStructureControlPoints = " + Integer.toString(numStructureControlPoints_) + "\n";
//	    content += "# Correction of the location boundaries intersection (default: false)\n";
//	    content += "correctBoundariesIntersection = " + Integer.toString(correctBoundariesIntersection_ ? 1 : 0) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# DROSOPHILA EMBRYO\n";
	    content += "#========================================\n";
	    content += "# DETECTING EMBRYO CONTOUR\n";
	    content += "\n";
	    content += "# Standard deviation of the Gaussian kernel used to smooth the image before using the snake (>0, default: 10)\n";
	    content += "drosophilaEmbryoStdSnakeSmoothing = " + Double.toString(drosophilaEmbryoStdSnakeSmoothing_) + "\n";
	    content += "# Number of nodes defining the snake (>=4, default: 6)\n";
	    content += "drosophilaEmbryoSnakeNumNodes = " + Integer.toString(drosophilaEmbryoSnakeNumNodes_) + "\n";
	    content += "# Trade-off parameter that weights the contribution of the image energy and the shape-prior energy. Low values of lambda give more weight to the image energy (more sensitive to image gradients) and high values give more weight to the shape regularization (preserves convexity). ([0,1[, default: 0)\n";
	    content += "drosophilaEmbryoSnakeLambda = " + Double.toString(drosophilaEmbryoSnakeLambda_) + "\n";
	    content += "# Trade-off parameter that weights the contribution of the thresholded image and the original image. Low values of alpha give more weight to the skeleton image and high values give more weight to image gradient ([0,1], default: 0.5)\n";
	    content += "drosophilaEmbryoSnakeAlpha = " + Double.toString(drosophilaEmbryoSnakeAlpha_) + "\n";
	    content += "#========================================\n";
	    content += "# MANUAL STRUCTURE DETECTION\n";
	    content += "#========================================\n";
	    content += "# Default circle radius for the manual structure detection. Defined as a fraction of min(image width, image height) ([0.1,0.5], default: 0.1)\n";
	    content += "genericStructureRadius = " + Double.toString(genericStructureRadius_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# 1D EXPRESSION PROFILES\n";
	    content += "# Reference boundary along which expression is measured (0 = D/V boundary, 1 = A/P boundary)\n";
	    content += "expression1DBoundary = " + Integer.toString(expression1DBoundary_) + "\n";
	    content += "# If different from zero, expression is measured along a translated version of the reference boundary ([-100,100], default: 0)\n";
	    content += "expression1DTranslationOffset = " + Double.toString(expression1DTranslationOffset_) + "\n";
	    content += "# Sigma in UNIT of the 1D Gaussian filter used to measure expression ([1,50], default: 4.). The width of the visible measurement domain in WingJ corresponds to 6*sigma.\n";
	    content += "expression1DSigma = " + Double.toString(expression1DSigma_) + "\n";
	    content += "# Save expression profile in PDF format (default: true)\n";
	    content += "expressionDataset1dSavePdf = " + Integer.toString(expressionDataset1dSavePdf_ ? 1 : 0) + "\n";
	    content += "# Save visualization of where in space expression profile is measured (default: true)\n";
	    content += "expressionDataset1dSaveMeasurementDomain = " + Integer.toString(expressionDataset1dSaveMeasurementDomain_ ? 1 : 0) + "\n";
	    content += "# Strategy for defining the number of measurement points. 0 = fixed number of points, 1 = take a measurement points every X UNIT (default: 0)\n";
	    content += "expression1DResolutionStrategy = " + Integer.toString(expression1DResolutionStrategy_) + "\n";
	    content += "# Number of measurement points of expression profile (default: 1000). Used only if \"expressionDataset1dSamplingStrategy\" = 0.\n";
	    content += "expression1DNumPoints = " +  Integer.toString(expression1DNumPoints_) + "\n";
	    content += "# Take one measurement points every X UNIT (default: 1.0 um). Used only if \"expressionDataset1dSamplingStrategy\" = 1.\n";
	    content += "expression1DStepSize = " + Double.toString(expression1DStepSize_) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# 2D EXPRESSION MAPS\n";
	    content += "# -100 = D/V boundary conserved, 100 = A/P boundary conserved (default: 0 = stitching of the two expression maps together)\n";
	    content += "expression2DStitchingDensityDifferenceThld = " + Double.toString(expression2DStitchingDensityDifferenceThld_) + "\n";
	    content += "# Smoothing along stitches ([0,100], default: 5)\n";
	    content += "expression2DStitchingSmoothingRange = " + Double.toString(expression2DStitchingSmoothingRange_) + "\n";
	    content += "# Number of measurement points of the 2D expression maps (default: 1001)\n";
	    content += "expression2DNumPoints = " + Integer.toString(expression2DNumPoints_) + "\n";
	    content += "# Density of the 2D mesh grid for visualization only. Higher values lead to denser preview grids (>0, default: 0.05)\n";
	    content += "expression2DPreviewMeshGridDensity = " + Double.toString(expression2DPreviewMeshGridDensity_) + "\n";
	    content += "\n";
//	    content += "# EXPERT: Smoothing the grid density (density of the vertices of the grid, default: 10).\n";
//	    content += "expression2DStitchingGridDensitySmoothing = " + Double.toString(expression2DStitchingGridDensitySmoothing_) + "\n";
	    content += "#========================================\n";
	    content += "# MEAN MODELS\n";
	    content += "# Compartment boundary to use as reference for the generation of structure and expression aggregated models (0 for D/V or 1 for A/P)\n";
	    content += "expression2DAggEquator = " + Integer.toString(expression2DAggEquator_) + "\n";
	    content += "# Generate standard deviation for aggregated expression maps (default: false)\n";
	    content += "expression2DAggStd = " + Integer.toString(expression2DAggStd_ ? 1 : 0) + "\n";
	    content += "\n";
	    content += "#========================================\n";
	    content += "# COMPOSITE IMAGES\n";
	    content += "# Projection method to use for generating composite images (0: MEAN for all, 1: MAX for all, -1: methods selected from GUI)\n";
	    content += "expressionCompositeProjections = " + Integer.toString(expressionCompositeProjections_) + "\n";
	    content += "\n";
//	    content += "#========================================\n";
//	    content += "# BATCH MODE\n";
//	    content += "\n";
//	    content += "# Batch root directory (default: USER_HOME)\n";
//	    content += "batchRootDirectory = \"" + batchRootDirectory_ + "\"\n";
//	    content += "# Defines which channels to load automatically\n";
//	    for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
//	    	content += "batchCh0" + i + "AutoLoading = " +Integer.toString(batchChannelAutoLoading_.get(i) ? 1 : 0) + "\n";
//	    content += "# 1D expression dataset: List of translation offsets in [-100.,100.] (default: \"-25./-15./-5./0./5./15./25.\"). See parameter \"expressionOffset\". If the list of offsets is empty, 1D expression datasets are not computed.\n";
//	    content += "expression1DTranslationOffsets = \"" + getExpression1DTranslationOffsetsToString() + "\"\n";
//	    content += "# 2D expression dataset: List of stitching thresholds in [-100.,100.] (default: \"-100./0./100.\"). See parameter \"stitchingDensityDifferenceThld\". If the list of thresholds is empty, 2D expression datasets are not computed.\n";
//	    content += "expressionDataset2dThresholds = \"" + getExpressionThresholdsToString() + "\"\n";
//	    content += "\n";
	    content += "#========================================\n";
	    content += "# OTHER PARAMETERS\n";
	    content += "# Default color (default: \"00ff00\")\n";
	    content += "defaultColor = \"" + Integer.toHexString(defaultColor_.getRGB()).substring(2) + "\"\n";
	    content += "# ROI stroke width of the detected structure (default: 1.3)\n";
	    content += "defaultStrokeWidth = " + Float.toString(defaultStrokeWidth_) + "\n";
	    content += "# Overlay font size coefficient, e.g. displayed on top of the detected structure (default: 40)\n";
	    content += "structureOverlayFontSizeCoeff = " + Double.toString(structureOverlayFontSizeCoeff_) + "\n";
//	    content += "\n";
//	    content += "#========================================\n";
//	    content += "# PARAMETERS SAVED BUT NOT LOADED (EXCEPT IN BATCH MODE)\n";
//	    content += "# Path containing the loaded confocal images for each channel\n";
//	    for (int i = 0; i < WJSettings.NUM_CHANNELS; i++)
//	    	content += "ch0" + i + "Directory = \"" + channelDirectories_.get(i) + "\"\n";
//	    content += "# Selected min and max slice indexes to consider for each channal\n";
//		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
//			content += "expressionCh0" + i + "MinSliceIndex = " + expressionMinSliceIndexes_.get(i) + "\n";
//			content += "expressionCh0" + i + "MaxSliceIndex = " + expressionMaxSliceIndexes_.get(i) + "\n";
//		}
	    
	    return content;
	}
	
	//----------------------------------------------------------------------------
	
	/** Reads the last setting file open and return its content. */
	public String loadSettingsContent() throws IOException, Exception {

		if (lastSettingsFileOpened_ == null) // if not settings file loaded yet
			return settings2String();
		
		File f = new File(lastSettingsFileOpened_);
		InputStreamReader inputStreamReader = new InputStreamReader(f.toURI().toURL().openStream(), CHARSET);
		BufferedReader in = new BufferedReader(inputStreamReader);
		String str = "";
		String tmp = in.readLine();

		while (tmp != null) {
			str += tmp + "\n";
			tmp = in.readLine();
		}
		in.close();
		return str;
	}
	
	//----------------------------------------------------------------------------
	
	/** Attempts to load the default settings file which must be located at /HOME/USER/wingj/settings.txt. */
	public void loadDefaultSettingsFile() throws Exception {
		
		File settingsFile = new File(getDefaultWJSettingsFileURI());
		WJSettings.log("Reading local settings file " + settingsFile.getPath());
		if (settingsFile != null && settingsFile.isFile()) {
			loadSettings(settingsFile.toURI());
		} else {
			WJSettings.log("Local settings file not found.");
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getAppName() { return appName_; }
	public String getAppVersion() { return appVersion_; }
	public String getIjRequiredVersion() { return ijRequiredVersion_; }
	
	// ============================================================================
	// GENERAL
	
	public void setWorkingDirectory(String wd) { wd_ = wd; }
	public String getWorkingDirectory() { return wd_; }
	
	public void setOutputDirectory(String path) { od_ = path; }
	public String getOutputDirectory() { return od_; }
	
	public void setBatchRootDirectory(String path) { batchRootDirectory_ = path; }
	public String getBatchRootDirectory() { return batchRootDirectory_; }
	
	public void setLastSettingsFileOpened(URI uri) { lastSettingsFileOpened_ = uri; }
	public URI getLastSettingsFileOpened() { return lastSettingsFileOpened_; }
	
	public void setStructureChannelIndex(int index) { structureChannelIndex_ = index; }
	public int getStructureChannelIndex() { return structureChannelIndex_; }
	
	public void setShowBatchExperimentPanel(boolean b) { showBatchExperimentPanel_ = b; }
	public boolean getShowBatchExperimentPanel() { return showBatchExperimentPanel_; }
	
	// ============================================================================
	// CONFOCAL IMAGES
	
	public void setExperimentName(String id) { experimentName_ = id; }
	public String getExperimentName() { return experimentName_; }
	
	public void setGeneNames(String name, int index) { geneNames_.set(index, name); }
	public String getGeneName(int index) { return geneNames_.get(index); }
	
	public List<String> getGeneNames() { return geneNames_; }
	
	public void setChannelProjectionMethod(int channel, int method) { channelProjectionMethod_.set(channel, method); }
	public int getChannelProjectionMethod(int channel) { return channelProjectionMethod_.get(channel); }
    
	public void setUnit(String unit) { unit_ = unit; }
	public String getUnit() { return unit_; }
	
    public void setScale(double value) { scale_ = value; }
    public double getScale() { return scale_; }
    
	// ============================================================================
	// PRE-PROCESSING
    
    public void setExpectedBoundariesThicknessInPixels(double value) { expectedBoundariesThicknessInPixels_ = value; }
    public double getExpectedBoundariesThicknessInPixels() { return expectedBoundariesThicknessInPixels_; }
    
    public void setPpThreshold(int value) { ppThld_ = value; }
    public int getPpThreshold() { return ppThld_; }
    
	// ============================================================================
	// CENTER DETECTION
    
    public void setMinSkeletonSizeInPixels(int minSkeletonSizeInPixels) { minSkeletonSizeInPixels_ = minSkeletonSizeInPixels; }
    public int getMinSkeletonSizeInPixels() { return minSkeletonSizeInPixels_; }
    
    public void setCenterOptimizerScale(double scale) { centerOptimizerScale_ = scale; }
    public double getCenterOptimizerScale() { return centerOptimizerScale_; }
    
	// ============================================================================
	// COMPARTMENT BOUNDARIES DETECTION
    
    public void setKiteSnakeBranchLength(double kiteSnakeBranchLength) { kiteSnakeBranchLength_ = kiteSnakeBranchLength; }
    public double getKiteSnakeBranchLength() { return kiteSnakeBranchLength_; }
    
    public void setKiteSnakeBranchWidth(double kiteSnakeBranchWidth) { kiteSnakeBranchWidth_ = kiteSnakeBranchWidth; }
    public double getKiteSnakeBranchWidth() { return kiteSnakeBranchWidth_; }
    
    public void setBoundaryTrackerStepSizeInPixels(double boundaryTrackerStepSizeInPixels) {boundaryTrackerStepSizeInPixels_ = boundaryTrackerStepSizeInPixels; }
    public double getBoundaryTrackerStepSizeInPixels() { return boundaryTrackerStepSizeInPixels_; }
    
    public void setBoundaryTrackerNumSteps(int numSteps) { boundaryTrackerNumSteps_ = numSteps; }
    public int getBoundaryTrackerNumSteps() { return boundaryTrackerNumSteps_; }
    
    public void setBoundaryTrackerScale(double scale) { boundaryTrackerScale_ = scale; }
    public double getBoundaryTrackerScale() { return boundaryTrackerScale_; }
    
    public void setBoundaryTrackerShowDuration(double durantionInSeconds) { boundaryTrackerShowDuration_ = durantionInSeconds; }
    public double getBoundaryTrackerShowDuration() { return boundaryTrackerShowDuration_; }
    
    public void setBoundaryTrackerShrinkageInPixels(double shrinkage) { boundaryTrackerShrinkageInPixels_ = shrinkage; }
    public double getBoundaryTrackerShrinkageInPixels() { return boundaryTrackerShrinkageInPixels_; }
    
	// ============================================================================
	// SNAKES
    
    public void setSnakeBlur(int value) { snakeBlur_ = value; }
    public int getSnakeBlur() { return snakeBlur_; }
    
    public void setSnakeRadius(int value) { snakeRadius_ = value; }
    public int getSnakeRadius() { return snakeRadius_; }
    
    public void setSnakeLambda(double value) { snakeLambda_ = value; }
    public double getSnakeLambda() { return snakeLambda_; }
    
    public void setSnakeNumNodes(int value) { snakeNumNodes_ = value; }
    public int getSnakeNumNodes() { return snakeNumNodes_; }
    
    public void setSnakeAlpha(double value) { snakeAlpha_ = value; }
    public double getSnakeAlpha() { return snakeAlpha_; }
    
    public void setGenericStructureRadius(double value) { genericStructureRadius_ = value; }
    public double getGenericStructureRadius() { return genericStructureRadius_; }
    
    public void setOuterBoundaryExpansion(double d) { outerBoundaryExpansion_ = d; }
    public double getOuterBoundaryExpansion() { return outerBoundaryExpansion_; }
    
	// ============================================================================
	// DROSOPHILA EMBRYO
    
    public void setDrosophilaEmbryoStdSnakeSmoothing(double d) { drosophilaEmbryoStdSnakeSmoothing_ = d; }
    public double getDrosophilaEmbryoStdSnakeSmoothing() { return drosophilaEmbryoStdSnakeSmoothing_; }
    
    public void setDrosophilaEmbryoSnakeNumNodes(int i) { drosophilaEmbryoSnakeNumNodes_ = i; }
    public int getDrosophilaEmbryoSnakeNumNodes() { return drosophilaEmbryoSnakeNumNodes_; }
    
    public void setDrosophilaEmbryoSnakeLambda(double d) { drosophilaEmbryoSnakeLambda_ = d; }
    public double getDrosophilaEmbryoSnakeLambda() { return drosophilaEmbryoSnakeLambda_; }
    
    public void setDrosophilaEmbryoSnakeAlpha(double d) { drosophilaEmbryoSnakeAlpha_ = d; }
    public double getDrosophilaEmbryoSnakeAlpha() { return drosophilaEmbryoSnakeAlpha_; }    
    
	// ============================================================================
	// GENE EXPRESSION
    
    public void setExpression1DTranslation(double value) { expression1DTranslationOffset_ = value; }
    public double getExpression1DTranslation() { return expression1DTranslationOffset_; }
    
    public void setExpression1DSigma(double value) { expression1DSigma_ = value; }
    public double getExpression1DSigma() { return expression1DSigma_; }
    
    public void setExpression1DBoundary(int boundary) { expression1DBoundary_ = boundary; }
    public int getExpression1DBoundary() { return expression1DBoundary_; }
    
    public void setExpressionMinSliceIndex(int channel, int index) { expressionMinSliceIndexes_.set(channel, index); }
    public int getExpressionMinSliceIndex(int channel) { return expressionMinSliceIndexes_.get(channel); }
    
    public void setExpressionMaxSliceIndex(int channel, int index) { expressionMaxSliceIndexes_.set(channel, index); }
    public int getExpressionMaxSliceIndex(int channel) { return expressionMaxSliceIndexes_.get(channel); }
    
    public void setExpression2dOffset(int channel, double value) { expression2dOffsets_.set(channel, value); }
    public double getExpression2dOffset(int channel) { return expression2dOffsets_.get(channel); }
    
	// ============================================================================
	// DATASETS
    
    public void setExpression1DResolutionStrategy(int strategy) { expression1DResolutionStrategy_ = strategy; }
    public int getExpression1DResolutionStrategy() { return expression1DResolutionStrategy_; }
    
    public void setExpression1DNumPoints(int num) { expression1DNumPoints_ = num; }
    public int getExpression1DNumPoints() { return expression1DNumPoints_; }
    
    public void setExpression1DStepSize(double rate) { expression1DStepSize_ = rate; }
    public double getExpression1DStepSize() { return expression1DStepSize_; }
    
    public void setExpression2DNumPoints(int numPoints) { expression2DNumPoints_ = numPoints; }
    public int getExpression2DNumPoints() { return expression2DNumPoints_; }
    
    public void normalizeExpression(boolean b) { normalizeExpression_ = b; }
    public boolean normalizeExpression() { return normalizeExpression_; }
    
    public void correctBoundariesIntersection(boolean b) { correctBoundariesIntersection_ = b; }
    public boolean correctBoundariesIntersection() { return correctBoundariesIntersection_; }
    
    public void setExpression2DPreviewMeshGridDensity(double density) { expression2DPreviewMeshGridDensity_ = density; }
    public double getExpression2DPreviewMeshGridDensity() { return expression2DPreviewMeshGridDensity_; }
    
    public void setExpression2DStitchingGridDensitySmoothing(double smoothing) { expression2DStitchingGridDensitySmoothing_ = smoothing; }
    public double getExpression2DStitchingGridDensitySmoothing() { return expression2DStitchingGridDensitySmoothing_; }
    
    public void setExpression2DStitchingDensityDifferenceThld(double thld) { expression2DStitchingDensityDifferenceThld_ = thld; }
    public double getExpression2DStitchingDensityDifferenceThld() { return expression2DStitchingDensityDifferenceThld_; }
    
    public void setExpression2DStitchingSmoothingRange(double smoothing) { expression2DStitchingSmoothingRange_ = smoothing; }
    public double getExpression2DStitchingSmoothingRange() { return expression2DStitchingSmoothingRange_; }
    
    public void setBatchChannelAutoLoading(int index, boolean b) { batchChannelAutoLoading_.set(index, b); }
    public boolean getBatchChannelAutoLoading(int index) { return batchChannelAutoLoading_.get(index); }
    
    public void setExpression1DTranslationOffsets(String str) {
    	
    	StringTokenizer tokenizer = new StringTokenizer(str, "/");
    	expression1DTranslationOffsets_.clear();
    	while(tokenizer.hasMoreTokens())
    		expression1DTranslationOffsets_.add(Double.parseDouble(tokenizer.nextToken()));
    }
    
    public List<Double> getExpressionOffsets() { return expression1DTranslationOffsets_; }
    public String getExpression1DTranslationOffsetsToString() {
    	
    	if (expression1DTranslationOffsets_.isEmpty() || expression1DTranslationOffsets_.get(0) == null)
    		return "";
    	
    	String str = Double.toString(expression1DTranslationOffsets_.get(0));
    	for (int i = 1; i < expression1DTranslationOffsets_.size(); i++) {
    		if (expression1DTranslationOffsets_.get(i) == null) str += "/0.";
    		else str += "/" + Double.toString(expression1DTranslationOffsets_.get(i));
    	}
    	return str;
    }
    
    public void setExpressionThresholds(String str) {
    	
    	StringTokenizer tokenizer = new StringTokenizer(str, "/");
    	expression2DThlds_.clear();
    	while(tokenizer.hasMoreTokens())
    		expression2DThlds_.add(Double.parseDouble(tokenizer.nextToken()));
    }
    
    public List<Double> getExpressionThresholds() { return expression2DThlds_; }
    public String getExpressionThresholdsToString() {

    	if (expression2DThlds_.isEmpty() || expression2DThlds_.get(0) == null)
    		return "";
    	
    	String str = Double.toString(expression2DThlds_.get(0));
    	for (int i = 1; i < expression2DThlds_.size(); i++) {
    		if (expression2DThlds_.get(i) == null) str += "/0.";
    		else str += "/" + Double.toString(expression2DThlds_.get(i));
    	}
    	return str;
    }
    
    public void setExpression1DSavePdf(boolean b) { expressionDataset1dSavePdf_ = b; }
    public boolean getExpression1DSavePdf() { return expressionDataset1dSavePdf_; }
    
    public void setExpression1DSaveMeasurementDomain(boolean b) { expressionDataset1dSaveMeasurementDomain_ = b; }
    public boolean getExpression1DSaveMeasurementDomain() { return expressionDataset1dSaveMeasurementDomain_; }

    public void setExpression2DAggEquator(int equator) { expression2DAggEquator_ = equator; }
    public int getExpression2DAggEquator() { return expression2DAggEquator_; }
    
    public void setExpression2DAggStd(boolean b) { expression2DAggStd_ = b; }
    public boolean getExpression2DAggStd() { return expression2DAggStd_; }
    
    public void setExpressionCompositeProjections(int method) { expressionCompositeProjections_ = method; }
    public int getExpressionCompositeProjections() { return expressionCompositeProjections_; }
    
	// ============================================================================
	// MISC PARAMETERS
    
    public void setNumStructureControlPoints(int value) { numStructureControlPoints_ = value; }
    public int getNumStructureControlPoints() { return numStructureControlPoints_; }
    
    public void setDefaultColor(Color color) { defaultColor_ = color; }
    public Color getDefaultColor() { return defaultColor_; }
    
    public void setDefaultStrokeWidth(float width) { defaultStrokeWidth_ = width; }
    public float getDefaultStrokeWidth() { return defaultStrokeWidth_; }
    
    public void setStructureOverlayFontSizeCoeff(double coeff) { structureOverlayFontSizeCoeff_ = coeff; }
    public double getStructureOverlayFontSizeCoeff() { return structureOverlayFontSizeCoeff_; }
    
    public void setSnakeStrokeWidth(float width) { snakeStrokeWidth_ = width; }
    public float getSnakeStrokeWidth() { return snakeStrokeWidth_; }
    
	// ============================================================================
	// PARAMETERS SAVED BUT NOT LOADED
    
    public void setChannelDirectory(int index, String str) { channelDirectories_.set(index, str); }
    public String getChannelDirectory(int index) { return channelDirectories_.get(index); }
    
    public List<Image> getAppIcon() { return icons_; }
}