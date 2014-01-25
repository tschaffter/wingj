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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ch.epfl.lis.wingj.WJSettings;

/** 
 * Utility methods to process filenames.
 * 
 * @version June 13, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class FilenameUtils {

	// ============================================================================
	// STATIC METHODS
    
	/** Gets the extension of a file. */
    public static String getExtension(File f) {
    	return getExtension(f.getName());
    }
    
    // ----------------------------------------------------------------------------
    
    /** Gets the extension of a filename. */
    public static String getExtension(String s) {
    	String ext = null;
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
    
    // ----------------------------------------------------------------------------
    
	/** Takes a filename with path and returns just the filename. */
	public static String getFilenameWithoutPath(String path) {
		File f = new File(path);
		return f.getName();
	}
	
	// ----------------------------------------------------------------------------

	/** Returns the directory of the given path. */
	public static String getDirectory(String path) {
		File f = new File(path);
		return f.getParent();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Gets a filename without its extension. */
	public static String getFilenameWithoutExtension(String fullPath) {
		
		char extensionSeparator = '.';
		String pathSeparator = System.getProperty("file.separator");
		
		 int dot = fullPath.lastIndexOf(extensionSeparator);
		 if (dot == -1)
			 dot = fullPath.length();
		 
	     int sep = fullPath.lastIndexOf(pathSeparator);
	     
	     return fullPath.substring(sep + 1, dot);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes the extension from a string (use apache method instead). */
	@Deprecated
	public static String removeExtension(String fullPath) {
		
		char extensionSeparator = '.';
		 int dot = fullPath.lastIndexOf(extensionSeparator);
		 if (dot == -1)
			 dot = fullPath.length();
		 return fullPath.substring(0, dot);
	}

	// ----------------------------------------------------------------------------
	
	/**
	 * Checks is the input filename has already one of the specified extensions.
	 * Otherwise, the first extension of the set is added to the filename.
	 */
	public static String addExtension(String filename, String[] extension) {
		
		String fileSeparator = System.getProperty("file.separator");
		// Get the eventuall position of a file separator
		int sep = filename.lastIndexOf(fileSeparator);
		String prefix = "";
		if (sep != -1) { // If path before filename, path saved in prefix and is removed from filename
			prefix = filename.substring(0, sep+1);
			filename = getFilenameWithoutPath(filename);
		}
		
		// Get the position of the last dot (if exist)
		int dot = filename.lastIndexOf('.');
		if (dot == -1) { // No point, so no given extension
			return prefix + filename + "." + extension[0];
		}
		
		String givenExt = filename.substring(dot+1, filename.length());
		for (int i=0; i < extension.length; i++) {
			if (givenExt.compareToIgnoreCase(extension[i]) == 0)
				return prefix + filename;
		}
		// If here, we should add an extension
		filename += "." + extension[0];
		
		return prefix + filename;		
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Checks is the input filename has already one of the specified extensions.
	 * Otherwise, the first extension of the set is added to the filename.
	 */
	@Deprecated
	public static File addExtension(File file, String[] extension) {
		String filename = file.getAbsolutePath();
		filename = addExtension(filename, extension);
		return new File(filename);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns if yes or not a file should be written. */
	public static boolean writeOrAbort(String path, JFrame frame) {
		// Check if the file already exists
		if (fileAlreadyExist(path)) {
			int n = JOptionPane.showConfirmDialog(
//					GnwGuiSettings.getInstance().getGnwGui().getFrame(),
					frame,
				    path
				    + "\n\n"
				    + "The selected filename already exists. If you\n"
				    + "continue, the contents of the existing file will\n"
				    + "be replaced.\n"
				    + "\n"
				    + "Do you want to continue?",
				    "Replace file",
				    JOptionPane.YES_NO_OPTION);

			if (n == JOptionPane.YES_OPTION)
				return true; // If the user selected YES
			else
				return false;
		} else
			return true;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the given path points on an already existing file. */
	public static boolean fileAlreadyExist(String path) {
		File file = new File(path);
		return file.exists();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reads a directory with the specified extension. */
	public static ArrayList<File> readDirectory(String directory,final String ext, boolean rec) {
		
		ArrayList<File> list = new ArrayList<File>();
		ArrayList<File> children = null;
		//Filter to read only files
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File file){
				if (file.isDirectory()) {
					return !file.getName().startsWith(".");
				}
				else if(file.isFile()) {
					return file.getName().endsWith(ext);
				}
				
				return false;
			}
		};
		
		File dir = new File(directory);
	
		if ( !dir.isDirectory() && dir.exists())
			dir = new File(FilenameUtils.getDirectory(directory));
		
		if ( !dir.exists() ) {
			return null;
		}
		
		File[] files = dir.listFiles(filter);
		for(int i=0;i<files.length;i++) {
			if ( files[i].isDirectory() && rec) {
				children = readDirectory(files[i].getAbsolutePath(), ext, rec);
				for (int j=0;j<children.size();j++)
					list.add(children.get(j));
			}
			if ( files[i].isFile())
				list.add(files[i]);
		}
		
		return list;
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Returns the absolute path of the files found in the given directory and whose
	 * filename matches the given regex.
	 */
	public static List<String> selectFilenames(String directory, String regex) throws Exception {
		
		if (!directory.endsWith(WJSettings.FS))
			directory += WJSettings.FS;
		
		regex = regex.replace("?", ".?").replace("*", ".*?");
		
		List<String> filenames = new ArrayList<String>();
		File rootDirectoryFile = new File(directory);
		String[] children = rootDirectoryFile.list();
		for (int i = 0; i < children.length; i++) {
			if (children[i].matches(regex))
				filenames.add(directory + children[i]);
		}
		
		return filenames;
	}
}
