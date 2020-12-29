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
import java.io.InputStream;
import java.util.Properties;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.utilities.StringUtils;



/** 
 * Defines one benchmark (i.e. one experiment).
 * 
 * @version October 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Benchmark extends SwingWorker<Void, Void> {
	
	/** Name of the folder where WingJ files are exported */
	public static final String WINGJ_FOLDER_NAME = "WingJ";
	/** Name of the settings file saved by WingJ */
	public static final String WINGJ_SETTINGS_FILENAME = "settings.txt";
	
	/** Total number of benchmarks */
	public static int numBenchmarks_ = 0;
	/** Number of benchmark done */
	public static int numBenchmarksDone_ = 0;
	/** Number of benchmarks which failed due to an (unexpected) error */
	public static int numBenchmarksFailures_ = 0;
	
	/** Absolute path to experiment folder */
	protected String directoryPath_ = "";
	/** Experiment name */
	protected String name_ = "";
	
	/** WPouchDetector instance */
	protected WPouchStructureDetector detector = null;
	
	/** Settings */
	protected Properties properties_ = null;

	// ============================================================================
	// PROTECTED METHODS
	
	/** Open settings file and read relevant parameters */
	protected void openSettings(String settingsPath) throws Exception {
		
		File settingsFile = new File(settingsPath);
		if (!settingsFile.exists() || !settingsFile.isFile())
			throw new Exception("ERROR: settingsFile doesn't exist or is not a file.");
		
		InputStream stream = settingsFile.toURI().toURL().openStream();
		
		try {
			properties_ = new Properties();
			properties_.load(stream);
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new Exception("WARNING: At least one parameter is missing from settings file.\nSee provided settings file on WingJ website (wingj.sf.net).");
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new Exception("WARNING: At least one parameter is missing from settings file.\nSee provided settings file on WingJ website (wingj.sf.net).");
		} finally {
			stream.close();
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Initialize */
	protected void initialize() throws Exception {
		
		File directoryFile = new File(directoryPath_);
		if (!directoryFile.exists() || !directoryFile.isDirectory())
			throw new Exception("ERROR: directoryFile doesn't exist or is not a directory.");
		
		try {
			// get relevant information from settings file
			openSettings(directoryPath_ + Benchmark.WINGJ_FOLDER_NAME + "/" + Benchmark.WINGJ_SETTINGS_FILENAME);
			name_ = StringUtils.stripLeadingAndTrailingQuotes(String.valueOf(properties_.getProperty("experimentName")));
		} catch (Exception e) {
			WJSettings.log("WARNING: No settings file found.");
			name_ = "experimentName";
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Cleanup method to call once the benchmark is done */
	protected void clean() throws Exception {}
	
	//----------------------------------------------------------------------------
	
	@Override
	protected Void doInBackground() throws Exception {
	
		return null;
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor */
	public Benchmark() {}
	
	//----------------------------------------------------------------------------
	
	/** Constructor */
	public Benchmark(String directoryPath) {
		
		try {
			directoryPath_ = directoryPath;
			initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getName() { return name_; }
	
	public void setDirectoryPath(String directoryPath) { directoryPath_ = directoryPath; }
	public String getDirectoryPath() { return directoryPath_; }
}
