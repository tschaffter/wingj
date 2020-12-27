/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

WingJ is licensed under a
Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.

You should have received a copy of the license along with this
work. If not, see http://creativecommons.org/licenses/by-nc-nd/3.0/.

If this software was useful for your scientific work, please cite our paper(s)
listed on http://wingj.sourceforge.net.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.test;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;

import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.snakes.KiteSnake;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.PreProcessing;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchCenterDetection;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchSnakeStructure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureParser;
import ch.epfl.lis.wingj.utilities.FileUtils;
import ch.epfl.lis.wingj.utilities.StringUtils;


/** 
 * Extends the class Benchmark to test the structure center detection algorithm.
 * 
 * First, the structure projection image is opened and the AOI is applied (if any).
 * The structure image is pre-processed (blurring and thresholding) and raw candidates
 * for the structure center are generated from the maximum of the dominant modes of the
 * image x- and y-axis projection. Each raw center candidate is optimized and the one
 * having the largest ratio crossIntensity/backgroundIntensity is selected. The performance
 * of the structure center detection method is the number of successfully recovered
 * centers over a benchmark of 50 wings aged between 78 and 110 hours.
 * 
 * Moreover, it is possible to apply this test for different values of rotation angle
 * applied to the structure projection. The range of angles can go for instance from
 * -45 to 45 degrees.
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class StructureCenterBenchmark extends ch.epfl.lis.wingj.test.Benchmark {
	
	/** Computed structure center evaluated as good if |center - targetCenter| < MAX_CENTER_ERROR (in px) */
	public static final double MAX_CENTER_ERROR = 50;
	/** KiteSnake branch angles are good if within 90 +- error below */
	public static double KITESNAKE_ANGLE_ERROR = 30.;
	/** Set to true if the KiteSnake must be computed */
	public static boolean KITESNAKE_ENABLED = false;
	
	/** Suffix of the structure projection image */
	public static final String WINGJ_STRUCTURE_PROJECTION_SUFFIX = "_projection.tif";
	/** Filename of AOI */
	public static final String WINGJ_AOI_FILENAME = "aoi.tif";
	/** Name of the XML structure file saved by WingJ */
	public static final String WINGJ_STRUCTURE_FILENAME = "structure.xml";
	
	/** Structure projection */
	protected ImagePlus structureProjection_ = null;
	
	/** Structure center computed */
	protected Point2D.Double structureCenter_ = null;
	/** Target structure center */
	protected Point2D.Double targetStructureCenter_ = null;
	
	/** KiteSnake instance */
	protected KiteSnake kiteSnake_ = null;
	
	/** Rotation angle of the original structure image. Rotates the image or selection 'angle' degrees clockwise. */
	protected double angle_ = 0.;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Return the intersection point of the structure contained in the given XML file */
	private Point2D.Double getTargetStructureCenterFromXmlStructure() throws Exception {
		
		Point2D.Double targetCenter = null;
		
		URI uri = FileUtils.getFileURI(directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + StructureCenterBenchmark.WINGJ_STRUCTURE_FILENAME);
		File structureFile = new File(uri);
		if (!structureFile.exists() || !structureFile.isFile())
			throw new Exception("ERROR: structureFile doesn't exist or is not a file.");
		
		// create WPouchSnakeStructure and extract target center
		WPouchStructure structure = new WPouchStructure("target");		
    	WPouchStructureParser parser = new WPouchStructureParser(structure);
    	parser.read(uri);
    	targetCenter = ((WPouchSnakeStructure)structure.getSnakeStructure()).getWPouchCenter();
		
		return targetCenter;
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Open projection of the structure */
	protected ImagePlus openStructureProjection() throws Exception {
		
		ImagePlus projection = null;
		
		int structureChannelIndex = Integer.valueOf(properties_.getProperty("structureChannelIndex"));
		String structureChannelName = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(properties_.getProperty("gene" + structureChannelIndex + "Name")));
		
		String structureProjectionPath = directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + structureChannelName + StructureCenterBenchmark.WINGJ_STRUCTURE_PROJECTION_SUFFIX;
		File structureProjectionFile = new File(structureProjectionPath);
		if (!structureProjectionFile.exists() || !structureProjectionFile.isFile())
			throw new Exception("ERROR: structureProjectionFile doesn't exist or is not a file");
		
		if ((projection = IJ.openImage(structureProjectionPath)) == null)
			throw new Exception("ERROR: Unable to open structure projection.");
		
		// open AOI and apply it to projection
		try {
			URI uri = FileUtils.getFileURI(directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + StructureCenterBenchmark.WINGJ_AOI_FILENAME);
			File aoiFile = new File(uri);
			WJImages.openAoi(aoiFile);
			WJImagesMask.applyMask(projection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// convert to 32-bit
		new ImageConverter(projection).convertToGray32();
		// must be that name and no other
		projection.setTitle("structure_projection");
		
		return projection;
	}
	
	//----------------------------------------------------------------------------
	
	/** Initialize */
	@Override
	protected void initialize() throws Exception {
	
		// Benchmark.initialize()
		super.initialize();
		
		// open the image
		// IMPORTANT: the AOI is opened and applied here (if any)
		structureProjection_ = openStructureProjection();
		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("ERROR: structureProjection_ is null.");
	
		// rotate the structure projection (if required)
		if (angle_ != 0)
			structureProjection_.getProcessor().rotate(angle_);
		
		structureCenter_ = null;
		targetStructureCenter_ = getTargetStructureCenterFromXmlStructure();
	}
	
	//----------------------------------------------------------------------------
	
	/** Cleanup method to call once the benchmark is done */
	@Override
	protected void clean() throws Exception {
		
		// Benchmark.clean()
		super.clean();
		
		if (structureProjection_ != null && structureProjection_.getProcessor() != null) {
			structureProjection_.close();
			structureProjection_ = null;
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	public StructureCenterBenchmark(Benchmark benchmark) {
		
		this.directoryPath_ = benchmark.directoryPath_;
		this.name_ = benchmark.name_;
		this.properties_ = benchmark.properties_;
	}

	//----------------------------------------------------------------------------
	
	@Override
	protected Void doInBackground() throws Exception {

		initialize();
		runStructureCenterDetection();
		clean();

		return null;
	}
	
	//----------------------------------------------------------------------------
	
    @Override
    protected void done() {
    	
    	try {
    		get();

    	} catch (Exception e) {
    		e.printStackTrace();
    		Benchmark.numBenchmarksFailures_++;
    		System.out.println("ERROR: Benchmark " + name_ + " failed");
    		
    	} finally {
    		Benchmark.numBenchmarksDone_++;
    		String percentDone = new DecimalFormat("#.##").format((100.*Benchmark.numBenchmarksDone_)/(double)Benchmark.numBenchmarks_);
    		System.out.println("Progress: " + percentDone + "%");
    	}
	}
	
	//----------------------------------------------------------------------------
	
	/** Detect the center of the structure (ppThld from settings is used) */
	public void runStructureCenterDetection() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		WPouchStructureDetector detector = new WPouchStructureDetector(name_);
		detector.setStructureProjection(structureProjection_);
		detector.isInteractive(false);
		
		// Step 1: Pre-processing
		PreProcessing ppDetection = new PreProcessing(name_ + "_preprocessing", detector);
		// compute suitable pre-processing threshold for the given blur value
		settings.setPpBlur(Double.valueOf(properties_.getProperty("ppBlur")));
		int ppThreshold = PreProcessing.computeAutoPpThreshold(structureProjection_, settings.getPpBlur());
		settings.setPpThreshold(ppThreshold);
		ppDetection.run();
		
		// Step 2: Wing pouch center detection
		WPouchCenterDetection wPouchCenterDetection = new WPouchCenterDetection(name_ + "_wpouch_center", detector);
		wPouchCenterDetection.isKiteSnakeDisabled(!KITESNAKE_ENABLED);
		detector.isInteractive(true);
		wPouchCenterDetection.run();

		WPouchStructure structure = (WPouchStructure)detector.getStructure();
		structureCenter_ = (Point2D.Double)structure.getCenter().clone();
		kiteSnake_ = detector.getKiteSnake();
		
		// close figures, etc.
		detector.clean();
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Return the distance between the computed center and target center.
	 * If one of the center is null, return Double.POSITIVE_INFINITY;
	 */
	public double computeStructureCenterError() throws Exception {
		
		if (structureCenter_ == null || targetStructureCenter_ == null)
			return Double.POSITIVE_INFINITY;
		
		return structureCenter_.distance(targetStructureCenter_);
	}
	
	//----------------------------------------------------------------------------
	
	/** Return true if the KiteSnake is considered as valid */
	public boolean isKiteSnakeValid() throws Exception {
		
		if (kiteSnake_ == null)
			throw new Exception("ERROR: kiteSnake_ is null.");
		
		boolean convergence = kiteSnake_.hasConverged();
		boolean branchAnglesConsistent = kiteSnake_.areBranchAnglesConsistent();
		boolean minBranchLengthRespected = kiteSnake_.areBranchesLongerThan(10.); // doesn't make much sense anymore
		
		return (convergence && branchAnglesConsistent && minBranchLengthRespected);
	}
	
	// ============================================================================
	// GETTERS AND SETTERS

	public Point2D.Double getStructureCenter() { return structureCenter_; }
	public Point2D.Double getTargetStructureCenter() { return targetStructureCenter_; }
	
	public void setAngle(double angle) { angle_ = angle; }
	public double getAngle() { return angle_; }
	
	public KiteSnake getKiteSnake() { return kiteSnake_; }
}
