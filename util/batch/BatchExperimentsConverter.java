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

package ch.epfl.lis.wingj.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.utilities.FileUtils;



/** 
 * Converts old experiment folders to new standard compatible with the batch mode of WingJ.
 * 
 * The implementation is non-destructive for input files (copy but don't move).
 * 
 * @version August 27, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class BatchExperimentsConverter {
	
	/** OLD: root directory containing the experiments */
	protected String oldRootDirectory_ = "/mnt/extra/source"; // remove "!" to unlock the converter
	/** NEW: root directory which will contain the experiments compatible with Batch Mode */
	protected String newRootDirectory_ = "/mnt/extra/target";
	
	/** Name of the folder where the images are */
	protected String imagesFolder_ = "images";
	/** Name of the folder where all remaining file which are not input images are placed to */
	protected String miscFolder_ = "misc";
	
	/** OLD: the names of the three channels directory */
	@SuppressWarnings("serial")
	protected ArrayList<String> oldChannelFolders_ = new ArrayList<String>() {{add("ch01"); add("ch02"); add("ch03");}};
	
	/** OLD: tags to be replace */
	@SuppressWarnings("serial")
	public static ArrayList<String> oldTags_ = new ArrayList<String>() {{add("ch01"); add("ch02"); add("ch03"); add("P-mad-ab_"); add("Brk-ab_");
																	 add("Wg-Ptc-ab_"); add("Wg-Ptc_"); add("26deg_"); add("1u-distance_"); add("26-degree_");
																	 add("nov27-settings_"); add("Sal_"); add("dad-GFP_"); add("female"); add("male");
																	 add(" "); add("A0V0_"); add("A0V1_"); add("A1V0_"); add("A1V1_");
																	 add("40X_"); add("pent2-5_"); add("wg-ptcX_");}};
	/** NEW: tags to use */
	@SuppressWarnings("serial")
	public static ArrayList<String> newTags_ = new ArrayList<String>() {{add("ch00"); add("ch01"); add("ch02"); add("pmadAB_"); add("brkAB_");
																	add("wg-ptcAB_"); add("wg-ptcAB_"); add(""); add(""); add("");
																	add(""); add("salAB_"); add("dadGFP_"); add("F_"); add("M_");
																	add("_"); add(""); add(""); add(""); add("");
																	add(""); add("pent2-5-_"); add("wg-ptcAB_");}};
	
	/** OLD: Names of the folders containing the experiments */
	protected String[] oldExperimentNames_ = null;
	/** NEW: Names of the updated experiment */
	protected String[] newExperimentNames_ = null;
	
	// ============================================================================
	// FILENAME FILTER METHODS
	
	/** Create a filter which returns true if the file is not a hidden file starting with "." */
	public static FilenameFilter hidenFilenameFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	return !name.startsWith(".");
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a filter which returns true if the file is has a ".tif" extension */
	public static FilenameFilter tifFilenameFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	if (dir.isDirectory()) return false;
		    	return name.endsWith(".tif");
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a filter which returns true if the folder contains one of the old channel folders */
	protected FilenameFilter oldChannelFolderFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	if (!dir.isDirectory()) return false;
		    	if (name.compareTo(oldChannelFolders_.get(0)) == 0) return true;
		    	if (name.compareTo(oldChannelFolders_.get(1)) == 0) return true;
		    	if (name.compareTo(oldChannelFolders_.get(2)) == 0) return true;
		    	return false;
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a filter which returns true if the file can be considered as a misc file */
	protected FilenameFilter miscFileFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	if (dir.isDirectory()) {
			    	if (name.compareTo(oldChannelFolders_.get(0)) == 0) return false;
			    	if (name.compareTo(oldChannelFolders_.get(1)) == 0) return false;
			    	if (name.compareTo(oldChannelFolders_.get(2)) == 0) return false;
		    	}
		    	if (name.endsWith(".tif")) return false;
		    	return true;
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Create a filter which returns true if the folder can be considered as an old experiment folder */
	protected FilenameFilter oldExperimentFolderFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	try {
			    	// the experiment folder name must not start with "."
			    	if (name.startsWith(".")) return false;
			    	// check if the folder contains all three old channel folders
			    	URI uri = FileUtils.getFileURI(dir.getAbsoluteFile() + "/" + name);
			    	File experimentFile = new File(uri);
			    	String[] children = experimentFile.list(oldChannelFolderFilter());
			    	if (children == null || children.length != oldChannelFolders_.size()) return false;
			    	// should be an old experiment folder
			    	return true;
		    	} catch (Exception e) {
		    		return false;
		    	}
		    }
		};
		return filter;
	}
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** List the names of all old experiments (folder names) */
	protected String[] listExperimentFolderNames(String directory) throws Exception {
		
		URI uri = FileUtils.getFileURI(directory);
		File oldDirectoryFile = new File(uri);
		
		if (!oldDirectoryFile.exists())
			throw new Exception("ERROR: The old root directory " + directory + " doesn't exist.");
		if (!oldDirectoryFile.isDirectory())
			throw new Exception("ERROR: The old root directory " + directory + " is not a directory.");
		
		return oldDirectoryFile.list(oldExperimentFolderFilter());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy all old images to imagesDirectory. Channel substrings are replaced if required (e.g. *ch01*.tif -> *ch00*.tif) */
	protected void copyImages(int index, String imagesDirectory) throws Exception {
		
		String oldExperimentName = oldExperimentNames_[index];
		String newExperimentName = newExperimentNames_[index];
		
		String modifiedImageName = "";
		for (int i = 0; i < oldChannelFolders_.size(); i++) {
			URI uri = FileUtils.getFileURI(oldRootDirectory_ + "/" + oldExperimentName + "/" + oldChannelFolders_.get(i));
			File oldChannelDirectory = new File(uri);
			String[] children = oldChannelDirectory.list(); // list all
			if (children == null) continue; // go to the next subfolder
			for (int j = 0; j < children.length; j++) {
				// replace the channel substring of the image filename
				modifiedImageName = updateSingleFilename(children[j]);
				
				// copy the image file
				if (modifiedImageName.endsWith(".tif")) {
					FileUtils.copy(oldChannelDirectory.getAbsolutePath() + "/" + children[j], imagesDirectory + "/" + modifiedImageName);
//					System.out.println(children[j] + " -> " + modifiedImageName);
				}
				// or to the misc folder
				else
					FileUtils.copy(oldChannelDirectory.getAbsolutePath() + "/" + children[j], newRootDirectory_ + "/" + newExperimentName + "/" + miscFolder_ + "/" + modifiedImageName);
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy all old misc files to miscDirectory */
	protected void copyMisc(int index, String miscDirectory) throws Exception {
		
		String oldExperimentName = oldExperimentNames_[index];
		
		URI uri = FileUtils.getFileURI(oldRootDirectory_ + "/" + oldExperimentName);
		File oldExperimentDirectory = new File(uri);
		String[] children = oldExperimentDirectory.list(miscFileFilter());
		if (children == null) return;
		for (int i = 0; i < children.length; i++)			
			FileUtils.copy(oldExperimentDirectory.getAbsolutePath() + "/" + children[i], miscDirectory + "/" + children[i]);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Convert an old experiment to a new one */
	protected void convert(int index) throws Exception {
		
		String newExperimentName = newExperimentNames_[index];
		
		// build the name of the new directories
		String newExperimentDirectory = newRootDirectory_ + "/" + newExperimentName; // update tags
		String imagesDirectory = newExperimentDirectory + "/" + imagesFolder_;
		String miscDirectory = newExperimentDirectory + "/" + miscFolder_;
		// make the directories
		FileUtils.mkdir(newExperimentDirectory);
		FileUtils.mkdir(imagesDirectory);
		FileUtils.mkdir(miscDirectory);
		
		copyMisc(index, miscDirectory); // copy all misc files
		copyImages(index, imagesDirectory); // copy all the images
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Update the tags contained in a filename (not a path)
	 * IMPORTANT: if the filename starts with "ch0" (only contained in image filenames),
	 * do only ONE replacement and then skip.
	 */
	public static String updateSingleFilename(String str) throws Exception { // TODO
		
		if (oldTags_.size() != newTags_.size())
			throw new Exception("ERROR: Lists of old and new tags must have the same size.");
		
		for (int i = 0; i < oldTags_.size(); i++) {
			// exception rule
			if (oldTags_.get(i).startsWith("ch0") && str.contains(oldTags_.get(i))) {
				str = str.replaceAll(oldTags_.get(i), newTags_.get(i));
				return str;
			}
			str = str.replaceAll(oldTags_.get(i), newTags_.get(i));
		}
		
		return str;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Update the tags contained in a list of filenames (not a paths) */
	public static String[] updateSingleFilenames(String[] list) throws Exception {
		
		String[] output = new String[list.length];
		for (int i = 0; i < list.length; i++)
			output[i] = updateSingleFilename(list[i]);
		return output;
	}

	// ============================================================================
	// PUBLIC METHODS
	
	/** Main */
	public static void main(String[] args) {
		
		try {
			BatchExperimentsConverter converter = new BatchExperimentsConverter();
			converter.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Default constructor */
	public BatchExperimentsConverter() {}
	
	// ----------------------------------------------------------------------------
	
	/** Run conversion */
	public void run() throws Exception {
		
		// list all experiments identified as such
		oldExperimentNames_ = listExperimentFolderNames(oldRootDirectory_);
		// updated experiments names
		newExperimentNames_ = updateSingleFilenames(oldExperimentNames_);
		
		// Updating and copying the old experiments
		DecimalFormat myFormatter = new DecimalFormat("#.##");
		int numExperiments = oldExperimentNames_.length;
		for (int i = 0; i < numExperiments; i++) {
			System.out.println("Converting " + oldExperimentNames_[i] + "\n" +
					"\t=> " + newExperimentNames_[i] + " (" + myFormatter.format((100.*i)/(double)numExperiments) + "%)");
			convert(i);
		}
		System.out.println("Done");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy the files which have at least one tag to replace.
	 * IMPORTANT: The files are not renamed but are copied to the new target name.
	 * Check before that enough disc space is available.
	 */
	public void updateFilenames(String directory) throws Exception {
		
		URI uri = FileUtils.getFileURI(directory);
		File file = new File(uri);
		String[] folders = file.list(BatchExperimentsConverter.hidenFilenameFilter());
		if (folders == null)
			throw new Exception("ERROR: No files in " + directory);
		
		String oldString = null;
		String newString = null;
		for (int i = 0; i < folders.length; i++) {
			
			oldString = directory + "/" + folders[i];
			newString = directory + "/" + BatchExperimentsConverter.updateSingleFilename(folders[i]);
			
			if (oldString.compareTo(newString) == 0) {
				
				WJSettings.log("Skipping " + oldString);
				continue;
			}
			
			System.out.println("Updating " + oldString + " (copied not renamed)\n" +
					"\t=> " + newString + " (" + (i+1) + "/" + folders.length + ")");
			FileUtils.copy(oldString, newString);
		}
		
		System.out.println("Done");
		System.out.println("Original files can now be deleted.");
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setOldRootDirectory(String directory) { oldRootDirectory_ = directory; };
	public void setNewRootDirectory(String directory) { newRootDirectory_ = directory; }
	
	public ArrayList<String> getOldChannelFolders() { return oldChannelFolders_; }
}
