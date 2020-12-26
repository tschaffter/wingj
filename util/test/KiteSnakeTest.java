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

import ij.ImageJ;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchCenterDetection;
import ch.epfl.lis.wingj.utilities.MathUtils;



/**
 * Evaluates the performance of the KiteSnake (branch angles should be almost right).
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class KiteSnakeTest extends StructureCenterTest {
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Compute statistics */
	@Override
	protected Map<String,Object> computeStats() throws Exception {
		
		Map<String,Object> stats = new HashMap<String,Object>();
		
		// overall success rate
		int success = 0;		
		for (StructureCenterBenchmark b : benchmarks_) {
			if (b.isKiteSnakeValid())
				success++;	
		}
		stats.put("numBenchmarks", Benchmark.numBenchmarks_);
		stats.put("numSuccessfulBenchmarks", success);
		stats.put("successRate", 100.*success/(double)Benchmark.numBenchmarks_);
		
		// successful KiteSnake right angles mean and std
		List<Double> allAngles = new ArrayList<Double>();
		for (StructureCenterBenchmark b : benchmarks_) {
			if (b.getKiteSnake().hasConverged())
				allAngles.addAll(b.getKiteSnake().computePositiveBranchAngles());
		}
		Double[] anglesMeanAndStd = MathUtils.computeMeanAndStd(allAngles);
		stats.put("convergedKiteSnakeAnglesMean", anglesMeanAndStd[0]);
		stats.put("convergedKiteSnakeAnglesStd", anglesMeanAndStd[1]);
	
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			int numSuccessfulExperiments = 0;
			int numExperiments = 0;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (b.isKiteSnakeValid())
						numSuccessfulExperiments++; // numerator
					numExperiments++; // denominator
				}
			}
			stats.put(rootExperimentName + "NumSuccessfulBenchmarks", numSuccessfulExperiments);
			stats.put(rootExperimentName + "SuccessRate", 100.*numSuccessfulExperiments/(double)numExperiments);
		}
		
		// detailed information for each experiments
		for (StructureCenterBenchmark b : benchmarks_) {
			// status
			int statusCode = 0;
			if (b.getKiteSnake().hasConverged())
				statusCode += 1;
			if (b.getKiteSnake().areBranchAnglesConsistent())
				statusCode += 2;
			if (b.getKiteSnake().areBranchesLongerThan(10.)) // doesn't make much sense anymore
				statusCode += 4;
			stats.put(b.getName() + "Status", statusCode);

			// branch angles
			List<Double> angles = b.getKiteSnake().computePositiveBranchAngles();
			for (int i = 0; i < angles.size(); i++)
				stats.put(b.getName() + "Angle" + i, angles.get(i));
			
			// branch lengths
			for (int i = 0; i < 4; i++)
				stats.put(b.getName() + "Length" + i, b.getKiteSnake().getBranchLength(i));
		}
	
		return stats;
	}
	
	//----------------------------------------------------------------------------
	
	/** Write the stats obtained from the benchmarks to file */
	@Override
	protected void writeStats(String filename) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		String content = "Total number of benchmarks: " + (Integer) stats.get("numBenchmarks") + "\n";
		
		String successRateStr = new DecimalFormat("#.##").format(((Double) stats.get("successRate")));
		content += "Number of successful KiteSnake (converged && branchAngles consistent && branchLengths >= Math.min(10., initialLength)): " + (Integer) stats.get("numSuccessfulBenchmarks") + " (" + successRateStr + "%)\n";
		
		String successfulKiteSnakeAnglesMeanStr = new DecimalFormat("#.###").format(((Double) stats.get("convergedKiteSnakeAnglesMean")));
		String successfulKiteSnakeAnglesStdStr = new DecimalFormat("#.###").format(((Double) stats.get("convergedKiteSnakeAnglesStd")));
		content += "Branch angles mean (only converged KiteSnake): " + successfulKiteSnakeAnglesMeanStr + "\n";
		content += "Branch angles std (only converged KiteSnake): " + successfulKiteSnakeAnglesStdStr + "\n";

		content += "\n";
		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			successRateStr = new DecimalFormat("#.##").format(((Double) stats.get(rootExperimentName + "SuccessRate")));
			content += "Success rate for " + rootExperimentName + ": " + successRateStr + "%\n";
		}
		content += "\n";
		
		// detailed information for each experiments
		for (StructureCenterBenchmark b : benchmarks_) { 
			content += b.getName() + ":";
			// status
			content += "\t" + (Integer) stats.get(b.getName() + "Status");
			// branch angles
			for (int i = 0; i < 4; i++)
				content += "\t" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "Angle" + i));
			// branch lengths
			for (int i = 0; i < 4; i++)
				content += "\t" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "Length" + i));
			content += "\n";
		}
		
		// write to file
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);	     
			out.write(content);
			out.close();
			System.out.println("[x] KiteSnakeTest stats (txt)");
			
		} catch (Exception e) {
			System.out.println("[ ] KiteSnakeTest stats (txt)");
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Write a file containing numConditions x numBenchmarks elements */
	protected void writeStatusStats(String suffix) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		// get status for each benchmark
		for (String rootExperimentName : rootExperimentNames_) {
			String content = "";
			boolean firstElement = true;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (firstElement) {
						content += WPouchCenterDetection.TEST_KITESNAKE_INITIAL_BRANCH_LENGTH; // TODO Thomas: restore structureRotationAngle_;
						firstElement = false;
					}
					int statusCode = (Integer) stats.get(b.getName() + "Status");
					content += "\t" + statusCode;
				}
			}
			content += "\n";
			
			// write to file
			String filename = megaDirectoryPath_ + "KiteSnakeTest_" + rootExperimentName + suffix; // "_status_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			try {
				FileWriter fstream = new FileWriter(filename, true); // append
				BufferedWriter out = new BufferedWriter(fstream);	     
				out.write(content);
				out.close();
				System.out.println("[x] " + filename);
				
			} catch (Exception e) {
				System.out.println("[ ] " + filename);
				e.printStackTrace();
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Write a file containing numConditions x (numBenchmarks elements x 4 almost right angles) */
	protected void writeBranchAnglesStats(String suffix) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		// get four branch angles for each benchmark
		for (String rootExperimentName : rootExperimentNames_) {
			String content = "";
			boolean firstElement = true;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (firstElement) {
						content += structureRotationAngle_;
						firstElement = false;
					}
					for (int i = 0; i < 4; i++)
						content += "\t" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "Angle" + i));
				}
			}
			content += "\n";
			
			// write to file
			String filename = megaDirectoryPath_ + "KiteSnakeTest_" + rootExperimentName + suffix; // "_status_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			try {
				FileWriter fstream = new FileWriter(filename, true); // append
				BufferedWriter out = new BufferedWriter(fstream);	     
				out.write(content);
				out.close();
				System.out.println("[x] " + filename);
				
			} catch (Exception e) {
				System.out.println("[ ] " + filename);
				e.printStackTrace();
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Write a file containing numConditions x (numBenchmarks elements x 4 branch lengths) */
	protected void writeBranchLengthsStats(String suffix) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		// get four branch angles for each benchmark
		for (String rootExperimentName : rootExperimentNames_) {
			String content = "";
			boolean firstElement = true;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (firstElement) {
						content += structureRotationAngle_;
						firstElement = false;
					}
					for (int i = 0; i < 4; i++)
						content += "\t" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "Length" + i));
				}
			}
			content += "\n";
			
			// write to file
			String filename = megaDirectoryPath_ + "KiteSnakeTest_" + rootExperimentName + suffix; // "_status_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			try {
				FileWriter fstream = new FileWriter(filename, true); // append
				BufferedWriter out = new BufferedWriter(fstream);	     
				out.write(content);
				out.close();
				System.out.println("[x] " + filename);
				
			} catch (Exception e) {
				System.out.println("[ ] " + filename);
				e.printStackTrace();
			}
		}
	}

	// ============================================================================
	// PUBLIC METHODS
	
	/** Evaluates the performance of the KiteSnake (branch angles should be almost right). */
	public static void main(String[] args) {

		try {
			// useful to interact with the KiteSnake
			new ImageJ();
			
			// enable KiteSnake and configure
			StructureCenterBenchmark.KITESNAKE_ENABLED = true;
			KiteSnakeTest.outputFilenamePrefix_ = "KiteSnakeTest";
			
			KiteSnakeTest test = new KiteSnakeTest();
			test.setMageDirectoryPath("/mnt/extra/center_detection_benchmarks_with_aoi/");
			
			/**
			 * Optimize KiteSnake for each of the benchmark wings
			 * using the current settings of WingJ. No rotations applied.
			 */
//			String statsFilename = test.getMegaDirectoryPath() + outputFilenamePrefix_ + "_angle_" + test.getStructureRotationAngle() + ".txt";
//			test.run(statsFilename);
			
			/**
			 * Evaluate the effect of the initial branches length of the KiteSnake.
			 * No rotation applied and dynamic branch width used (first default settings before test).
			 */
			test.runMultipleInitialBranchLengths(60, 60);
			
			/**
			 * Evaluate the effect of a fixed branch width for different rotation
			 * angles applied.
			 */
			// test.runMultipleAnglesFixedBranchWidths(-45, 45, 1, 50);
			
			/**
			 * Evaluate the effect of a dynamic branch width for different rotation
			 * angles applied. Ratios must be multiplied here by 1000.
			 */
//			 test.runMultipleAnglesDynamicBranchWidths(-10, -10, 50, 450);
			
			System.out.println("Done");
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Default constructor */
	public KiteSnakeTest() {}
	
	//----------------------------------------------------------------------------
	
	/** Run test for multiple initial branch lengths */
	public void runMultipleInitialBranchLengths(int minBranchLength, int maxbranchLength) throws Exception {
		
		int initialLengthStepSize = 5; // in pixel
		
		// run the test for each length
		for (int branchLength = minBranchLength; branchLength <= maxbranchLength; branchLength += initialLengthStepSize) {
		
			WPouchCenterDetection.TEST_KITESNAKE_INITIAL_BRANCH_LENGTH = branchLength;
			System.out.println("KiteSnake initial branch length is now " + branchLength);
			
			String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_initialLength_" + branchLength + ".txt";
			run(statsFilename);
			
			String suffix = "_status_initialLength.txt";
			writeStatusStats(suffix);
			clean();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run test for different angles and fixed branch widths */
	public void runMultipleAnglesFixedBranchWidths(int minAngle, int maxAngle, int minFixedBranchWidth, int maxFixedBranchWidth) throws Exception {
		
		int fixedBranchWidthStepSize = 1; // in pixel
		
		// run the test for each fixed branch width
		for (int fixedBranchWidth = minFixedBranchWidth; fixedBranchWidth <= maxFixedBranchWidth; fixedBranchWidth += fixedBranchWidthStepSize) {
			
			WPouchCenterDetection.TEST_KITESNAKE_FIXED_WIDTH = fixedBranchWidth;
			System.out.println("KiteSnake fixed branch width is now " + fixedBranchWidth);
			
			// run the test for each angle
			for (int angle = minAngle; angle <= maxAngle; angle++) {
				structureRotationAngle_ = angle;
				System.out.println("Structure rotation angle is now " + structureRotationAngle_);
				String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_fixedBranchWidth_" + fixedBranchWidth + ".txt";
				run(statsFilename);
				
				String suffix = "_status_angles_fixedBranchWidth_" + fixedBranchWidth + ".txt";
				writeStatusStats(suffix);
				suffix = "_branchAngles_angles_fixedBranchWidth_" + fixedBranchWidth + ".txt";
				writeBranchAnglesStats(suffix);
				suffix = "_branchLengths_angles_fixedBranchWidth_" + fixedBranchWidth + ".txt";
				writeBranchLengthsStats(suffix);
				clean();
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run test for different angles and dynamic branch widths (min and max ratios must be multiplied by 1000) */
	public void runMultipleAnglesDynamicBranchWidths(int minAngle, int maxAngle, int minInnerOuterTriangleAreasRatio, int maxInnerOuterTriangleAreasRatio) throws Exception {
		
		int innerOuterTriangleAreasRation = (int) (1000. * 0.05); // increment is 0.01
		
		// run the test for each dynamic branch width
		for (int innerOuterTriangleAreasRatio = minInnerOuterTriangleAreasRatio; innerOuterTriangleAreasRation <= maxInnerOuterTriangleAreasRatio; innerOuterTriangleAreasRation += innerOuterTriangleAreasRation) {
			
			WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO = innerOuterTriangleAreasRatio / 1000.;
			System.out.println("KiteSnake inner/outer triangle areas ratio is now " + WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO);
			
			// run the test for each angle
			for (int angle = minAngle; angle <= maxAngle; angle++) {
				structureRotationAngle_ = angle;
				System.out.println("Structure rotation angle is now " + structureRotationAngle_);
				String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_innerOuterTriangleAreasRatio_" + WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO + ".txt";
				run(statsFilename);
				
				String suffix = "_status_angles_dynamicBranchWidth_" + WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO + ".txt";
				writeStatusStats(suffix);
				suffix = "_branchAngles_angles_dynamicBranchWidth_" + WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO + ".txt";
				writeBranchAnglesStats(suffix);
				suffix = "_branchLengths_angles_dynamicBranchWidth_" + WPouchCenterDetection.TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO + ".txt";
				writeBranchLengthsStats(suffix);
				clean();
			}
		}
	}
}
