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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.utilities.FileUtils;



/** 
 * Provides a template for implementing a test.
 * 
 * @version July 4, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
abstract public class Test {
	
	/**
	 * Read the file path.txt to know where are the resource files required
	 * IMPORTANT: Usually I use a method so that I can access files even if
	 * they are located in a jar file. The problem here is that there are
	 * resource files which are TIFF files. One strategy to load an image
	 * from jar file is to use getClass().getResource(String) to get the URL
	 * and then Toolkit.getDefaultToolkit().getImage(URL). However getImage(URL)
	 * only supports GIF, JPEG and PNG images. */
	protected String path_ = "";
	
	/** Name of the test whith is also the name of the sub-folder containing resource files */
	protected String name_ = "";
	
	/** Filename of the settings file to use (all tests must use the same) */
	protected String settingsFilename_ = "test-settings.txt";
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Performs multiple tests */
	abstract public void run() throws Exception;
	/** Generate resource files. Must be run only when the application is working fine. */
	abstract public void generateResources() throws Exception;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public Test(String name) {
		
		name_ = name;
		
		WJSettings.log("// ----------------------------------------------------------------------------");
		WJSettings.log("Testing " + name);
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Read rsc/path.txt to know where are the resource files */
	protected void readPath() {
		
		URL url = getClass().getResource("rsc/path.txt");
		BufferedReader reader = null;
		
		try {
			URLConnection con = url.openConnection();
			con.connect();
			InputStream urlfs = con.getInputStream();
			reader = new BufferedReader(new InputStreamReader(urlfs));
			
			// read only the first line
			path_ = reader.readLine();
			
		} catch (FileNotFoundException e) {
			WJSettings.log("TEST: path.txt not found.");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			WJSettings.log("TEST: An error occured when reading path.txt.");
			e.printStackTrace();
			System.exit(-1);
		}
		finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				WJSettings.log("TEST: Unable to close path.txt.");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Load template settings file (common to all tests) */
	protected void loadSettings() {
		
		try {
			String filename = path_ + settingsFilename_;
			WJSettings.log("Loading template settings file " + filename);
			URI uri = FileUtils.getFileURI(filename);
			WJSettings.getInstance().loadSettings(uri);
		} catch (Exception e) {
			WJSettings.log("Unable to load test settings file");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
