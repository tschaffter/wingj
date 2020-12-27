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

import java.io.File;
import java.net.URI;
import java.text.DecimalFormat;

import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.PreProcessing;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.utilities.FileUtils;
import ch.epfl.lis.wingj.utilities.StringUtils;



/** 
 * Extends the class Benchmark to test the performance of a given auto threshold method.
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class AutoThresholdBenchmark extends ch.epfl.lis.wingj.test.Benchmark {
	
	/** Suffix of the structure projection image */
	public static final String WINGJ_STRUCTURE_PROJECTION_SUFFIX = "_projection.tif";
	/** Filename of AOI */
	public static final String WINGJ_AOI_FILENAME = "aoi.tif";
	
	/** Structure projection */
	protected ImagePlus structureProjection_ = null;
	
	/** Structure detector instance */
	protected WPouchStructureDetector detector_ = null;
	
	/** Pre-processing threshold computed */
	protected int ppThreshold_ = 0;
	/** Target structure center */
	protected int targetPpThreshold_ = 0;
	
	// ============================================================================
	// PRIVATE METHODS
	
//	/** Find a pre-processing threshold based on the number/area/shape of connected-components and other criteria */
//	@Deprecated
//	@SuppressWarnings("unused")
//	private int computePpThresholdBasedOnConnectedComponents() throws Exception {
//		
//		// open the image
//		structureProjection_ = openStructureProjection();
//		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
//			throw new Exception("ERROR: structureProjection_ is null.");
//		
//		WJSettings settings = WJSettings.getInstance();
//		double blur = settings.getPpBlur();
//		
//		// compute ppThreshold
//		PreProcessingScanner scanner = new PreProcessingScanner(blur, blur, blur, // blur
//				settings.getPpThldAutoStepSize(), settings.getPpThldAutoLowerBoundary(), settings.getPpThldAutoUpperBoundary()); // thld
//		scanner.setStructureProjection(structureProjection_);
//		scanner.findThreshold();
//		
//		// free
//		structureProjection_.close();
//		
//		// output
//		PreProcessingTester solution = scanner.getSolution();
//		if (solution != null)
//			return scanner.getSolution().getThld();
//		else
//			return 0;
//	}
	
	//----------------------------------------------------------------------------
	
//	/** Compute the auto pre-processing threshold from structure projection */
//	public static int computePpThresholdBasedOnAutoThresholder(ImagePlus image, double blur) throws Exception {
//		
//		if (image == null || image.getProcessor() == null)
//			throw new Exception("ERROR: image is null.");
//		
//		return PreProcessing.computeAutoPpThreshold(image, blur);
//	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Open projection of the structure */
	private ImagePlus openStructureProjection() throws Exception {
		
		ImagePlus projection = null;
		
		int structureChannelIndex = Integer.valueOf(properties_.getProperty("structureChannelIndex"));
		String structureChannelName = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(properties_.getProperty("gene" + structureChannelIndex + "Name")));
		
		String structureProjectionPath = directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + structureChannelName + AutoThresholdBenchmark.WINGJ_STRUCTURE_PROJECTION_SUFFIX;
		File structureProjectionFile = new File(structureProjectionPath);
		if (!structureProjectionFile.exists() || !structureProjectionFile.isFile())
			throw new Exception("ERROR: structureProjectionFile doesn't exist or is not a file");
		
		if ((projection = IJ.openImage(structureProjectionPath)) == null)
			throw new Exception("ERROR: Unable to open structure projection.");
		
		// open AOI and apply it to projection
		try {
			URI uri = FileUtils.getFileURI(directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + AutoThresholdBenchmark.WINGJ_AOI_FILENAME);
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
		
		// open target pre-processing threshold
		targetPpThreshold_ = Integer.valueOf(properties_.getProperty("ppThreshold"));
	}
	
	//----------------------------------------------------------------------------
	
	/** Cleanup method to call once the benchmark is done */
	@Override
	protected void clean() throws Exception {
		
		if (structureProjection_ != null && structureProjection_.getProcessor() != null) {
			structureProjection_.close();
			structureProjection_ = null;
		}	
	}
	
	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	public AutoThresholdBenchmark(Benchmark benchmark) {
		
		this.directoryPath_ = benchmark.directoryPath_;
		this.name_ = benchmark.name_;
		this.properties_ = benchmark.properties_;
	}
	
	//----------------------------------------------------------------------------
	
	@Override
	protected Void doInBackground() throws Exception {

		initialize();
		runPpAutoThreshold();
		clean();
		
		return null;
	}
	
	//----------------------------------------------------------------------------
	
    @SuppressWarnings("deprecation")
	@Override
    protected void done() {
    	
    	try {
    		get();

    	} catch (Exception e) {
    		e.printStackTrace();
    		AutoThresholdTest.numBenchmarksFailures_++;
    		
    	} finally {
    		AutoThresholdTest.numBenchmarksDone_++;
    		String percentDone = new DecimalFormat("#.##").format((100.*AutoThresholdTest.numBenchmarksDone_)/(double)AutoThresholdTest.numBenchmarks_);
    		System.out.println("Progress: " + percentDone + "%");
    	}
	}
	
	//----------------------------------------------------------------------------
	
	/** Compute pre-processing auto threshold */
	public void runPpAutoThreshold() throws Exception {
	
		WJSettings settings = WJSettings.getInstance();
		ppThreshold_ = PreProcessing.computeAutoPpThreshold(structureProjection_, settings.getPpBlur());
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the difference between computed threshold and target threshold */
	public double computePpThresholdError() {
		
		return Math.abs(ppThreshold_ - targetPpThreshold_);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	public int getPpThreshold() { return ppThreshold_; }
	
	public int getTargetPpThreshold() { return targetPpThreshold_; }
}
