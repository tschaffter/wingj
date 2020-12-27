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

import ij.process.AutoThresholder;

import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.PreProcessing;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchCenterDetection;



/**
 * Evaluates the performance of the structure center detection algorithm.
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class StructureCenterTest {
	
	/** Prefix of the output filename */
	protected static String outputFilenamePrefix_ = "StructureCenterTest";
	
	/** List of benchmarks */
	protected List<StructureCenterBenchmark> benchmarks_ = new ArrayList<StructureCenterBenchmark>();
	/** Absolute path to mega directory */
	protected String megaDirectoryPath_ = null;
	/** Root experiment names */
	protected List<String> rootExperimentNames_ = new ArrayList<String>();
	
	/** Image rotation angle tested */
	protected double structureRotationAngle_ = 0.;
	/** Dilation radius used to dilate the skeleton */
	protected double dilationRadius_ = 1.;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Write the stats obtained from the benchmarks to file */
	private void writeAutoThresholdMethodStats(String filename, String method) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		String content = method + "\t" + stats.get("successRate") + "\t" + stats.get("averageCenterError");		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_)
			content += "\t" + stats.get(rootExperimentName + "SuccessRate");
		content += "\n";
		
		// write to file
		try {
			FileWriter fstream = new FileWriter(filename, true); // append
			BufferedWriter out = new BufferedWriter(fstream);	     
			out.write(content);
			out.close();
			System.out.println("[x] StructureCenterTest auto threshold methods stats (txt)");
			
		} catch (Exception e) {
			System.out.println("[ ] StructureCenterTest auto threshold methods stats (txt)");
			e.printStackTrace();
		}
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Compute statistics */
	protected Map<String,Object> computeStats() throws Exception {
		
		Map<String,Object> stats = new HashMap<String,Object>();
		
		// overall success rate
		int success = 0;
		List<Double> successCenterErrors = new ArrayList<Double>();
		List<Double> centerErrors = new ArrayList<Double>(); // discard INF values
		
		for (StructureCenterBenchmark b : benchmarks_) {
			double error = b.computeStructureCenterError();
			if (error < StructureCenterBenchmark.MAX_CENTER_ERROR) {
				success++;
				successCenterErrors.add(error);
			}
			if (error < Double.POSITIVE_INFINITY)
				centerErrors.add(error);	
		}
		stats.put("numBenchmarks", Benchmark.numBenchmarks_);
		stats.put("numSuccessfulBenchmarks", success);
		stats.put("successRate", 100.*success/(double)Benchmark.numBenchmarks_);
		
		Double averageCenterError = 0.;
		for (Double d : successCenterErrors)
			averageCenterError += d;
		averageCenterError /= (double)successCenterErrors.size();	
		stats.put("averageCenterError", averageCenterError);
		
		averageCenterError = 0.;
		for (Double d : centerErrors)
			averageCenterError += d;
		averageCenterError /= (double)centerErrors.size();
		stats.put("overallAverageCenterError", averageCenterError);
	
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			int numSuccessfulExperiments = 0;
			int numExperiments = 0;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (b.computeStructureCenterError() < StructureCenterBenchmark.MAX_CENTER_ERROR)
						numSuccessfulExperiments++; // numerator
					numExperiments++; // denominator
				}
			}
			stats.put(rootExperimentName + "NumSuccessfulBenchmarks", numSuccessfulExperiments);
			stats.put(rootExperimentName + "SuccessRate", 100.*numSuccessfulExperiments/(double)numExperiments);
		}
		
		// detailed information for each experiments
		double error = 0.;
		for (StructureCenterBenchmark b : benchmarks_) {
			error = b.computeStructureCenterError();
			String status = "";
			if (error < StructureCenterBenchmark.MAX_CENTER_ERROR) status = "SUCCESS";
			else if (error < Double.POSITIVE_INFINITY) status = "FAILURE";
			else status += "ERROR";
			
			stats.put(b.getName() + "Status", status);
			stats.put(b.getName() + "CenterError", error);
			stats.put(b.getName() + "StructureCenter", b.getStructureCenter());
			stats.put(b.getName() + "TargetStructureCenter", b.getTargetStructureCenter());
		}
	
		return stats;
	}
	
	//----------------------------------------------------------------------------
	
	/** Write the stats obtained from the benchmarks to file */
	protected void writeStats(String filename) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		String content = "Total number of benchmarks: " + (Integer) stats.get("numBenchmarks") + "\n";
		
		String successRateStr = new DecimalFormat("#.##").format(((Double) stats.get("successRate")));
		content += "Number of successful structure center detections: " + (Integer) stats.get("numSuccessfulBenchmarks") + " (" + successRateStr + "%)\n";

		String averageCenterErrorStr = new DecimalFormat("#.###").format((Double) stats.get("averageCenterError"));
		content += "Average center error of successful structure center detections: " + averageCenterErrorStr + "\n";
		
		averageCenterErrorStr = new DecimalFormat("#.###").format((Double) stats.get("overallAverageCenterError"));
		content += "Average center error of all structure center detections: " + averageCenterErrorStr + " (Double.POSITIVE_INFINITE discarded)\n";
		content += "\n";
		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			successRateStr = new DecimalFormat("#.##").format(((Double) stats.get(rootExperimentName + "SuccessRate")));
			content += "Success rate for " + rootExperimentName + ": " + successRateStr + "%\n";
		}
		content += "\n";
		
		// detailed information for each experiments
		for (StructureCenterBenchmark b : benchmarks_) {
			content += b.getName() + ":\t" + (String) stats.get(b.getName() + "Status") + " (" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "CenterError")) + ")";
			try {
				Point2D.Double structureCenter = (Point2D.Double) stats.get(b.getName() + "StructureCenter");
				Point2D.Double targetStructureCenter = (Point2D.Double) stats.get(b.getName() + "TargetStructureCenter");
				content += "\t(" + (int) structureCenter.x + "," + (int) structureCenter.y + ")/(" + (int) targetStructureCenter.x + "," + (int) targetStructureCenter.y + ")";
			} catch (Exception e) { content += "\tERROR"; }
			content += "\n";
		}
		
		// write to file
		try {
			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);	     
			out.write(content);
			out.close();
			System.out.println("[x] StructureCenterTest stats (txt)");
			
		} catch (Exception e) {
			System.out.println("[ ] StructureCenterTest stats (txt)");
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Write the stats obtained from the benchmarks to file */
	protected void writeAngleStats(String filename, double angle) throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		String content = angle + "\t" + stats.get("successRate") + "\t" + stats.get("averageCenterError");		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_)
			content += "\t" + stats.get(rootExperimentName + "SuccessRate");
		content += "\n";
		
		// write to file
		try {
			FileWriter fstream = new FileWriter(filename, true); // append
			BufferedWriter out = new BufferedWriter(fstream);	     
			out.write(content);
			out.close();
			System.out.println("[x] StructureCenterTest angles stats (txt)");
			
		} catch (Exception e) {
			System.out.println("[ ] StructureCenterTest angles stats (txt)");
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Write a file containing numConditions x numBenchmarks elements */
	protected void writeErrorStats() throws Exception {
	
		Map<String,Object> stats = computeStats();
		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			String content = "";
			boolean firstElement = true;
			for (StructureCenterBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (firstElement) {
						content += structureRotationAngle_;
						firstElement = false;
					}
					content += "\t" + new DecimalFormat("#.###").format((Double) stats.get(b.getName() + "CenterError"));			
				}
			}
			content += "\n";
			
			// write to file
			String filename = megaDirectoryPath_ + "StructureCenterTest_" + rootExperimentName + "_centerError_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			try {
				FileWriter fstream = new FileWriter(filename, true); // append
				BufferedWriter out = new BufferedWriter(fstream);	     
				out.write(content);
				out.close();
				System.out.println("[x] StructureCenterTest_" + rootExperimentName + "_centerError_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt");
				
			} catch (Exception e) {
				System.out.println("[ ] StructureCenterTest_" + rootExperimentName + "_centerError_angles_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt");
				e.printStackTrace();
			}
		}
	}

	// ============================================================================
	// PUBLIC METHODS
	
	/** Evaluate the efficiency of the structure center detection algorithm implemented in WingJ */
	public static void main(String[] args) {

		try {
			StructureCenterTest test = new StructureCenterTest();
			test.setMageDirectoryPath("/mnt/extra/center_detection_benchmarks_with_aoi/");
			
			/**
			 * Computes the structure center errors of the benchmark wings
			 * using the current settings of WingJ. No rotations applied.
			 */
			// String statsFilename = test.getMegaDirectoryPath() + outputFilenamePrefix_ + "_angle_" + test.getStructureRotationAngle() + ".txt";
			// test.run();
			
			/**
			 * Computes the structure center errors of the benchmark wings
			 * for different rotation angles applied.
 			 */
			 // test.runMultipleAngles(-45, 45);
			
			/**
			 * Computes the structure center errors of the benchmark wings
			 * for different auto threshold methods. No rotations applied.
			 */
			// test.runMultipleAutoThresholdMethods(AutoThresholder.getMethods());
			
			/**
			 * For each dilation value, computes the structure center errors of the
			 * benchmark wings systematically rotated with angles in [-45,45] degrees.
			 * IMPORTANT: Restore DILATION in WPouchCenterDetection 
			 */
			 test.runMultipleAnglesDilationRadii(0, 0, 1, 1);
			
//			/**
//			 * For each ppBlur value, computes the structure center errors of the
//			 * benchmark wings systematically rotated with angles in [-45,45] degrees.
//			 * IMPORTANT: No skeleton used at all
//			 */
//			 test.runMultipleAnglesPpBlurValues(0, 0, 20, 20);
			
			System.out.println("Done");
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Default constructor */
	public StructureCenterTest() {}
	
	//----------------------------------------------------------------------------
	
	/** Initialize */
	public void instantiateBenchmarks() throws Exception {
		
		if (megaDirectoryPath_ == null)
			throw new Exception("ERROR: megaDirectoryPath_ is null.");
		
		// Open and instantiate generic benchmarks
		Benchmarks benchmarks = new Benchmarks();
		benchmarks.setMegaDirectoryPath(megaDirectoryPath_);
		benchmarks.open();
		
		megaDirectoryPath_ = benchmarks.getMegaDirectoryPath();
		rootExperimentNames_ = benchmarks.getRootExperimentNames();
		
		// convert Benchmark to StructureCenterBenchmark
		Benchmark.numBenchmarks_ = 0;
		for (int i = 0; i < benchmarks.getNumBenchmarks(); i++) {
			
			Benchmark b = benchmarks.getBenchmark(i);
			
			try {
				StructureCenterBenchmark scb = new StructureCenterBenchmark(b);
				
//				if (scb.getName().compareTo("20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_78-79H_F_2") == 0) {
//				if (scb.getName().compareTo("20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_90-91H_M_5") == 0) {
//				if (scb.getName().compareTo("20100714_pent2-5-_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_3") == 0) {
//				if (scb.getName().compareTo("20100716_pmadAB_brkAB_wg-ptcAB_78-79H_M_7") == 0) {
//				if (scb.getName().compareTo("20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_6") == 0) {
//				if (scb.getName().compareTo("20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_3") == 0) {
//				if (scb.getName().compareTo("20100716_pmadAB_brkAB_wg-ptcAB_110,5-111,5H_M_2") == 0) {
//				if (scb.getName().compareTo("20100716_pmadAB_brkAB_wg-ptcAB_90,5-91,5H_M_2") == 0) {
					benchmarks_.add(scb);
					Benchmark.numBenchmarks_++;
//				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR: Benchmark " + b.getName() + " has been discarded due to an error during instantiation.");
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Default run (angle = 0, default auto threshold method) */
	public void run(String statsFilename) throws Exception {
		
		// run sequentially to not use too much memory
		// SwingWorker.run() can be called only once
		instantiateBenchmarks();
		for (StructureCenterBenchmark scb : benchmarks_) {
			scb.setAngle(structureRotationAngle_);
			try {
				System.out.println("Running " + scb.name_);
				scb.execute();
				scb.get();
			} catch (Exception e) {
				// do nothing (already catched by benchmark)
			}
		}
		
		if (statsFilename != null)
			writeStats(statsFilename);
	}
	
	//----------------------------------------------------------------------------
	
	/** Run for multiple rotation angles */
	public void runMultipleAngles(int minAngle, int maxAngle) throws Exception {
		
		// cleanup old stats file
		String angleStatsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_stats.txt";
		FileUtils.deleteQuietly(new File(angleStatsFilename));
		
		// run the test for each angle
		for (int angle = minAngle; angle <= maxAngle; angle++) {
			structureRotationAngle_ = angle;
			System.out.println("Structure rotation angle is now " + structureRotationAngle_);
			String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			run(statsFilename);
			
			writeAngleStats(angleStatsFilename, angle); // add a line to file for each angle
			writeErrorStats();
			clean();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run for multiple rotation angles */
	public void runMultipleAnglesDilationRadii(int minAngle, int maxAngle, int minDilationRadius, int maxDilationRadius) throws Exception {
		
		for (int dilationRadius = minDilationRadius; dilationRadius <= maxDilationRadius; dilationRadius++) {
		
			WPouchCenterDetection.TEST_DILATATION = (double) dilationRadius;
			System.out.println("Dilation radius is now " + WPouchCenterDetection.TEST_DILATATION);
			
			// run the test for each angle
			for (int angle = minAngle; angle <= maxAngle; angle++) {
				structureRotationAngle_ = angle;
				System.out.println("Structure rotation angle is now " + structureRotationAngle_);
				String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
				run(statsFilename);
				
				writeErrorStats();
				clean();
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run for multiple rotation angles */
	public void runMultipleAnglesPpBlurValues(int minAngle, int maxAngle, int minPpBlur, int maxPpBlur) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		for (int ppBlur = minPpBlur; ppBlur <= maxPpBlur; ppBlur++) {
			
			settings.setPpBlur(ppBlur);
			System.out.println("ppBlur is now " + settings.getPpBlur());
			
			// run the test for each angle
			for (int angle = minAngle; angle <= maxAngle; angle++) {
				structureRotationAngle_ = angle;
				System.out.println("Structure rotation angle is now " + structureRotationAngle_);
				String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_ppBlur_" + settings.getPpBlur() + ".txt";
				run(statsFilename);
				
				writeErrorStats();
				clean();
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run for multiple auto thresholding method */
	public void runMultipleAutoThresholdMethods(String[] methods) throws Exception {
		
		// cleanup old stats file
		String angleStatsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_auto-threshold-method_stats.txt";
		FileUtils.deleteQuietly(new File(angleStatsFilename));
		
		for (int i = 0; i < methods.length; i++) {
			
			// the method MinError generates errors
			if (methods[i].compareTo("MinError") == 0) {
				System.out.println("Skipping method " + methods[i]);
				continue;
			}
			System.out.println("Auto threshold method is now " + methods[i]);
			PreProcessing.autoThresholdMethod_ = AutoThresholder.Method.valueOf(methods[i]);
			String statsFilename = megaDirectoryPath_ + outputFilenamePrefix_ + "_angle_" + structureRotationAngle_ + "_dilation_" + (int) WPouchCenterDetection.TEST_DILATATION + ".txt";
			run(statsFilename);
			
			writeAutoThresholdMethodStats(angleStatsFilename, methods[i]);
			clean();
		}
		
		System.out.println("IMPORTANT: The report has been overwritten again and again. Do not use it.");
	}
	
	//----------------------------------------------------------------------------
	
	/** Cleanup. Must be done after each call to run() if called several times. */
	public void clean() {
		
		benchmarks_.clear();
		rootExperimentNames_.clear();
		
		Benchmark.numBenchmarksDone_ = 0;
		Benchmark.numBenchmarksFailures_ = 0;
		Benchmark.numBenchmarks_ = 0;
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setMageDirectoryPath(String path) { megaDirectoryPath_ = path; }
	public String getMegaDirectoryPath() { return megaDirectoryPath_; }
	
	public void setStructureRotationAngle(double angle) { structureRotationAngle_ = angle; }
	public double getStructureRotationAngle() { return structureRotationAngle_; }
	
	public void setDilationRaidus(double dilationRadius) { dilationRadius_ = dilationRadius; }
	public double getDilationRadius() { return dilationRadius_; }
}
