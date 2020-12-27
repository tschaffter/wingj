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
import ij.process.AutoThresholder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.lis.wingj.structure.drosophila.wingpouch.PreProcessing;



/** 
 * Tests the efficiency of the auto pre-processing of the structure detection algorithm.
 *
 * IMPORTANT: Do not use that method anymore because at the end, the best threshold method
 * has been selected by counting the number of successful structure center detections. Before
 * that, my first attempt was to select the auto pre-processing threshold method which lead
 * to the smallest error when compared to thresholds I set manually for 50 wings.
 * 
 * The test applies different auto-thresholding methods. It generates a report for each of
 * them using the benchmark made of 50 wings with age between 78 and 110 hours. It also
 * return the best method to use. The efficiency used is the different between the found
 * pre-processing threshold and the manually set threshold.
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
@Deprecated
public class AutoThresholdTest {

	/** Absolute path to mega directory */
	private String megaDirectoryPath_ = "";
	/** Root experiment names */
	private List<String> rootExperimentNames_ = new ArrayList<String>();
	/** List of Benchmark */
	private List<AutoThresholdBenchmark> benchmarks_ = new ArrayList<AutoThresholdBenchmark>();
	
	/** Name of the auto threshold method tested */
	private String autoThresholdMethodName_ = null;
	
	/** Number of benchmark done */
	public static int numBenchmarksDone_ = 0;
	/** Number of benchmarks which failed due to an (unexpected) error */
	public static int numBenchmarksFailures_ = 0;
	/** Total number of benchmarks */
	public static int numBenchmarks_ = 0;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Write the stats obtained from the benchmarks to file */
	private void writeStats() throws Exception {
		
		Map<String,Object> stats = computeStats();
		
		String content = "Total number of benchmarks: " + (Integer) stats.get("numBenchmarks") + "\n";
		
		String successRateStr = new DecimalFormat("#.##").format(((Double) stats.get("successRate")));
		content += "Number of successful pre-processing threshold inferences: " + (Integer) stats.get("numSuccessfulBenchmarks") + " (" + successRateStr + "%)\n";
		
		String averagePpThresholdErrorStr = new DecimalFormat("#.###").format((Double) stats.get("averagePpThresholdError"));
		content += "Average pre-processing threshold error: " + averagePpThresholdErrorStr + " (zero thresholds discarded)\n";
		content += "\n";
		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			successRateStr = new DecimalFormat("#.##").format(((Double) stats.get(rootExperimentName + "SuccessRate")));
			content += "Success rate for " + rootExperimentName + ": " + successRateStr + "%\n";
		}
		content += "\n";
		
		// detailed information for each experiments
		for (AutoThresholdBenchmark b : benchmarks_) {
			content += b.getName() + ":\t" + (Integer) stats.get(b.getName() + "PpThreshold") + "/" + (Integer) stats.get(b.getName() + "PpTargetThreshold") + "\n";
		}
		
		// write to file
		try {
			FileWriter fstream = new FileWriter(megaDirectoryPath_ + "AutoThresholdTest_" + autoThresholdMethodName_ + ".txt");
			BufferedWriter out = new BufferedWriter(fstream);	     
			out.write(content);
			out.close();
			System.out.println("[x] AutoThresholdTest stats (txt)");
			
		} catch (Exception e) {
			System.out.println("[ ] AutoThresholdTest stats (txt)");
			e.printStackTrace();
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Evaluate the efficiency of the structure center detection algorithm implemented in WingJ */
	public static void main(String[] args) {

		try {
			// Important to access all the functionalities of IJ.
			// Requires to have a copy of IJ_Props.txt in WingJ project directory
			new ImageJ();
			
			int bestMethod = 0;
			double bestFitness = Double.POSITIVE_INFINITY;
			
			String[] methods = AutoThresholder.getMethods();
			for (int i = 0; i < methods.length; i++) {
				
				// the method MinError generates errors
				if (methods[i].compareTo("MinError") == 0) {
					System.out.println("Skipping method " + methods[i]);
					continue;
				}
				
				System.out.println("Testing auto threshold method \n" + methods[i] + "\"");
				PreProcessing.autoThresholdMethod_ = AutoThresholder.Method.valueOf(methods[i]);
				
				AutoThresholdTest test = new AutoThresholdTest();
				test.initialize("/mnt/extra/center_detection_benchmarks_with_aoi/");
				test.setAutoThrehsoldMethodName(methods[i]);
				test.run();
				
				double currentFitness = (Double) test.computeStats().get("averagePpThresholdError");
				if (currentFitness < bestFitness) {
					bestMethod = i;
					bestFitness = currentFitness;
				}
			}
			
			System.out.println("Best auto threhold method: " + methods[bestMethod]);
			System.out.println("Average pre-processing threhsold error: " + bestFitness);
			System.out.println("Done");
			System.exit(0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Default constructor */
	public AutoThresholdTest() {}
	
	//----------------------------------------------------------------------------
	
	/** Initialize */
	public void initialize(String megaDirectoryPath) throws Exception {
		
		Benchmarks benchmarks = new Benchmarks();
		benchmarks.setMegaDirectoryPath(megaDirectoryPath);
		benchmarks.open();
		
		megaDirectoryPath_ = benchmarks.getMegaDirectoryPath();
		rootExperimentNames_ = benchmarks.getRootExperimentNames();
		
		numBenchmarks_ = 0;
		numBenchmarksDone_ = 0;
		numBenchmarksFailures_ = 0;
		
		// convert Benchmark to AutoThresholdBenchmark
		for (int i = 0; i < benchmarks.getNumBenchmarks(); i++) {
			
			Benchmark b = benchmarks.getBenchmark(i);
			
			try {
				AutoThresholdBenchmark atb = new AutoThresholdBenchmark(b);
				benchmarks_.add(atb);
				numBenchmarks_++;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("ERROR: Benchmark " + b.getName() + " has been discarded due to an error.");
			}
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Run all benchmarks */
	public void run() throws Exception {
		
		// run sequentially
		for (AutoThresholdBenchmark atb : benchmarks_)
			atb.run();
		
		writeStats();
	}
	
	//----------------------------------------------------------------------------
	
	/** Compute statistics */
	public Map<String,Object> computeStats() throws Exception {
		
		Map<String,Object> stats = new HashMap<String,Object>();	
		
		// overall success rate
		int success = 0;
		// average error
		List<Double> errors = new ArrayList<Double>();
		for (AutoThresholdBenchmark b : benchmarks_) {
			if (b.getPpThreshold() > 0) {
				success++;
				errors.add(b.computePpThresholdError());
			}
		}
		stats.put("numBenchmarks", numBenchmarks_);
		stats.put("numSuccessfulBenchmarks", success);
		stats.put("successRate", 100.*success/(double)numBenchmarks_);
		
		// average pre-processing threshold error
		double averagePpThresholdError = 0.;
		for (Double d : errors)
			averagePpThresholdError += d;
		averagePpThresholdError /= errors.size();
		stats.put("averagePpThresholdError", averagePpThresholdError);
		
		// success rate for each root experiment
		for (String rootExperimentName : rootExperimentNames_) {
			int numSuccessfulExperiments = 0;
			int numExperiments = 0;
			for (AutoThresholdBenchmark b : benchmarks_) {
				if (b.getName().contains(rootExperimentName)) {
					if (b.getPpThreshold() > 0)
						numSuccessfulExperiments++; // numerator
					numExperiments++; // denominator
				}
			}
			stats.put(rootExperimentName + "NumSuccessfulBenchmarks", numSuccessfulExperiments);
			stats.put(rootExperimentName + "SuccessRate", 100.*numSuccessfulExperiments/(double)numExperiments);
		}

		// detailed information for each experiments
		for (AutoThresholdBenchmark b : benchmarks_) {
			stats.put(b.getName() + "PpThreshold", b.getPpThreshold());
			stats.put(b.getName() + "PpTargetThreshold", b.getTargetPpThreshold());
		}
		
		return stats;
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setAutoThrehsoldMethodName(String name) { autoThresholdMethodName_ = name; }
	public String getAutoThresholdMethodName() { return autoThresholdMethodName_; }
}
