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

package ch.epfl.lis.wingj.utilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/** 
 * Utility methods to manipulate files.
 * 
 * @version August 29, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class FileUtils {

	// ============================================================================
	// PUBLIC METHODS
	
	/** Creates a new directory. */
	static public File mkdir(String path) throws Exception {
		
		File file = new File(path);
		if (!file.mkdir())
			throw new Exception("ERROR: Unable to mkdir " + path + ".");
		return file;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copies a file from one path to another. */
	static public void copy(String from, String to) throws Exception {
		
		File fromFile = new File(from);
		File toFile = new File(to);
		if (fromFile.isFile())
			org.apache.commons.io.FileUtils.copyFile(fromFile, toFile);
		else if (fromFile.isDirectory())
			org.apache.commons.io.FileUtils.copyDirectory(fromFile, toFile);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Lists the content of the given directory and return only elements containing the given tag. */
	static public String[] getListDir(String path, String tag) throws Exception {
		
		String[] raw = getListDir(path);
		ArrayList<String> filenames = new ArrayList<String>(Arrays.asList(raw));
		
		// remove elements which don't match
		Iterator<String> it = filenames.iterator();
		while (it.hasNext()) {
			String s = it.next();
			if (!s.contains(tag))
				it.remove();
		}

		// convert ArrayList<String> to String[]
		return filenames.toArray((new String[filenames.size()]));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Lists the content of the given directory. */
	static public String[] getListDir(String path) throws Exception {
		
		File dir = new File(path);
		if (!dir.isDirectory())
			throw new Exception("ERROR: Can not list the content of " + path + ".");
		
		String[] list1 = dir.list();
		int count = 0;
		for (int i = 0; i < list1.length; i++) {
			if (!list1[i].startsWith("."))
				count++;
		}
		String[] list = new String[count];
		count = 0;
		for(int i = 0; i < list1.length; i++) {
			if (!list1[i].startsWith("."))
				list[count++] = list1[i];	
		}
		
		// otherwise the listing doesn't make any sense
		ij.util.StringSorter.sort(list);
		
		return list;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generates URI according to the filePath. */
	public static URI getFileURI(String filePath) throws MalformedURLException, URISyntaxException {
		
	    URI uri = null;
	    filePath = filePath.trim();
	    if (filePath.indexOf("http") == 0 || filePath.indexOf("\\") == 0) {
	        if (filePath.indexOf("\\") == 0){
	            filePath = "file:" + filePath;
	            filePath = filePath.replaceAll("#", "%23");
	        }
            filePath = filePath.replaceAll(" ", "%20");
            URL url = new URL(filePath);
            uri = url.toURI();
	    } else {
	        File file = new File(filePath);
	        uri = file.toURI();
	    }
	    return uri;
	}
}
