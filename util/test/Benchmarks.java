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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * Opens and manages benchmarks
 * 
 * @version October 11, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Benchmarks {
	
	/** List of Benchmark */
	private List<Benchmark> benchmarks_ = new ArrayList<Benchmark>();
	
	/** Absolute path to mega directory */
	private String megaDirectoryPath_ = null;
	/** Root experiment names */
	private List<String> rootExperimentNames_ = new ArrayList<String>();
	
	// ============================================================================
	// FILENAME FILTER METHODS
	
	/** Create a filter which returns true if the file is not a hidden file starting with "." and is a directory */
	public static FilenameFilter rootExperimentDirectoryFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	if (name.startsWith(".")) return false;
		    	File f = new File(dir.getAbsoluteFile() + "/" + name);
		    	if (!f.isDirectory()) return false;
		    	return true;
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a filter which returns true the file is a valid experiment folder containing a WingJ output folder */
	public static FilenameFilter experimentDirectoryFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	File wingjOutputDirectoryFile = new File(dir.getAbsoluteFile() + "/" + name + "/" + Benchmark.WINGJ_FOLDER_NAME);
		    	return (wingjOutputDirectoryFile.exists() && wingjOutputDirectoryFile.isDirectory());
		    }
		};
		return filter;
	}
	
	// ============================================================================
	// PRIVATE METHODS
	
	/**
	 * Load all experiments which have a WingJ output directory in each root
	 * directory contained gathered in the mega directory
	 */
	private void openBenchmarks() throws Exception {
		
		if (megaDirectoryPath_ == null)
			throw new Exception("ERROR: megaDirectoryPath_ is null.");
		
		benchmarks_.clear();
		rootExperimentNames_.clear();
		
		File megaDirectoryFile = new File(megaDirectoryPath_);
		if (!megaDirectoryFile.exists() || !megaDirectoryFile.isDirectory())
			throw new Exception("ERROR: megaDirectoryFile " + megaDirectoryFile.getAbsolutePath() + " doesn't exist or is not a directory.");
		
		// get only folders and discard hidden files
		String[] megaDirectoryChildren = megaDirectoryFile.list(rootExperimentDirectoryFilter());
		Arrays.sort(megaDirectoryChildren);
		System.out.println("Mega directory " + megaDirectoryFile.getAbsolutePath());
		System.out.println("+ " + megaDirectoryChildren.length + " root directories");
		
		for (int i = 0; i < megaDirectoryChildren.length; i++) {
			String rootDirectoryPath = megaDirectoryPath_ + megaDirectoryChildren[i] + "/";
			File rootDirectoryFile = new File(rootDirectoryPath);
			if (!rootDirectoryFile.exists() || !rootDirectoryFile.isDirectory())
				throw new Exception("ERROR: rootDirectoryFile " + rootDirectoryFile.getAbsolutePath() + " doesn't exist or is not a directory.");
			
			// get only valid experiment folders which contain a WingJ output folder
			rootExperimentNames_.add(megaDirectoryChildren[i]);
			String[] rootDirectoryChildren = rootDirectoryFile.list(experimentDirectoryFilter());
			Arrays.sort(rootDirectoryChildren);
			System.out.println("Root directory " + rootDirectoryFile.getAbsolutePath());
			System.out.println("+ " + rootDirectoryChildren.length + " experiment directories");
			
			for (int j = 0; j < rootDirectoryChildren.length; j++) {
				String experimentDirectoryPath = rootDirectoryPath + rootDirectoryChildren[j] + "/";
				
				try {
					Benchmark benchmark = new Benchmark(experimentDirectoryPath);
					benchmark.initialize();
					benchmarks_.add(benchmark);
					System.out.println(rootDirectoryChildren[j]);
					Benchmark.numBenchmarks_++;
					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("ERROR: Experiment " + experimentDirectoryPath + " has been discarded due to an error.");
				}				
			}
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Show the content of the given mega directory */
	public static void main(String[] args) {

		try {
			Benchmarks benchmarks = new Benchmarks();
			benchmarks.setMegaDirectoryPath("/mnt/extra/center_detection_benchmarks/");
			benchmarks.open();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------
	
	/** Default constructor */
	public Benchmarks() {}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor */
	public Benchmarks(String megaDirectoryPath) {
		
		megaDirectoryPath_ = megaDirectoryPath;
	}

	// ----------------------------------------------------------------------------
	
	/** Open all benchmarks contained in the mega directory */
	public void open() throws Exception {
		
		openBenchmarks();
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setMegaDirectoryPath(String path) { megaDirectoryPath_ = path; }
	public String getMegaDirectoryPath() { return megaDirectoryPath_; }
	
	public Benchmark getBenchmark(int index) { return benchmarks_.get(index); }
	public int getNumBenchmarks() { return benchmarks_.size(); }
	
	public String getRootExperimentName(int index) { return rootExperimentNames_.get(index); }
	public List<String> getRootExperimentNames() { return rootExperimentNames_; }
}
