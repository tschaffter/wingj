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

package ch.epfl.lis.wingj.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;

/** 
 * Counts the number of individual experiments matching the community expression requirement and updates the GUI of WingJ once done.
 * <p>
 * Takes as input a root experiment directory that must be scanned for individual experiment
 * to generate community expression maps.
 * <p>
 * Within the root experiment directory, select only the directory (supposed to be experiment directories).
 * Within each "experiment" directory, look for a folder called "WingJ" (see parameter below). Finally,
 * the WingJ folder must contain:
 * <ul>
 * 		<li>XML structure file (e.g. structure.xml, accepts REGEX)</li>
 * 		<li>Projection of the selected channel (e.g. dadGFP_raw.tif, accepts REGEX)</li>
 * </ul>
 * <p>
 * Once the listing is complete and if the job has not be
 * canceled with the method SwingWorker.cancel(true), a JLabel from the WingJ GUI is updated
 * to show how many experiments match the input settings. If the job has been canceled, the listing
 * is not interrupted for convenience but the method done() returns immediately without
 * processing the result, i.e. without updating the JLabel from the WingJ GUI.
 * 
 * @version November 1, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WJExperimentsSelectionUpdater extends SwingWorker<Void, Void> {
	
	/** Current instance of IndividualExperimentsSelectionUpdater. */
	public static WJExperimentsSelectionUpdater instance_ = null;
	
	/** Name of the WingJ output folder. */
	public static String WINGJ_OUTPUT_DIRECTORY_NAME = "WingJ";
	
	/** Suffix to add at the end of the name of a experiment folder to ignore it. */
	public static String WINGJ_EXPERIMENT_FOLDER_IGNORE_SUFFIX = "IGNORE";
	
	/** Root experiment directory. */
	private String rootExperimentDirectory_ = null;
	/** Regex for XML structure files. */
	private String structuresFilenameRegex_ = null;
	/** Regex for image projections. */
	private String projectionsFilenameRegex_ = null;

	/** Number of perfect matches (1 structure and 1 projection found). */
	private Integer numPerfectMatches_ = 0;
	/** Number of ambiguous matches (X structures and Y projections found). */
	private Integer numAmbigousMatches_ = 0;
	
	/** Paths to structure files. */
	private List<File> structureFiles_ = null;
	/** Paths to projection files. */
	private List<File> projectionFiles_ = null;
	
	// ============================================================================
	// PROTECTED METHODS
	
	@Override
	protected Void doInBackground() throws Exception {
		
		WingJ wingj = WingJ.getInstance();
		if (rootExperimentDirectory_ == null)
			throw new Exception("ERROR: Root experiment directory is null.");
		if (structuresFilenameRegex_ == null)
			throw new Exception("ERROR: Structures filename regex is null.");
		if (projectionsFilenameRegex_ == null)
			throw new Exception("ERROR: Projections filename regex is null.");
		
		boolean invalidInput = (rootExperimentDirectory_ != null && rootExperimentDirectory_.compareTo("") == 0) ||
			(structuresFilenameRegex_ != null && structuresFilenameRegex_.compareTo("") == 0) ||
			(projectionsFilenameRegex_ != null && projectionsFilenameRegex_.compareTo("") == 0);
		
		if (invalidInput) 
			return null;
		
		// display the snake during processing
		wingj.expressionComSnake_.start();
		wingj.expressionComCardLayout_.show(wingj.expressionComSelectionPanel_, "CARD_INDIVIDUAL_EXPERIMENTS_SNAKE");

		// create root experiment directory File
		File rootDirectoryFile = new File(rootExperimentDirectory_);
		if (rootDirectoryFile == null || !rootDirectoryFile.isDirectory())
			throw new Exception("ERROR: The root experiment directory " + rootDirectoryFile + "\n" +
					" is not valid and individual experiments cannot be found.");
		
		// get the experiment directories
		String[] children = rootDirectoryFile.list(new ExperimentFilter());
		if (children == null || children.length == 0)
			return null;
		
		// from the folders that contains a "WingJ" folder, build the list of "WingJ" paths
		// then create a File and scan it for the structure and projection files
		structureFiles_.clear();
		projectionFiles_.clear();
		projectionFiles_ = new ArrayList<File>();
		File f = null;
		String sep = System.getProperty("file.separator");
		for (int i = 0; i < children.length; i++) {
			f = new File(rootExperimentDirectory_ + children[i] + sep + WINGJ_OUTPUT_DIRECTORY_NAME + sep);			
			String[] structures = f.list(new WJOutputDirectoryContentFilter(structuresFilenameRegex_));
			String[] projections = f.list(new WJOutputDirectoryContentFilter(projectionsFilenameRegex_));
			
			if (structures == null || projections == null)
				continue;
			
			if (structures.length == 1 && projections.length == 1) {
				structureFiles_.add(new File(f.getAbsolutePath() + sep + structures[0]));
				projectionFiles_.add(new File(f.getAbsolutePath() + sep + projections[0]));
				numPerfectMatches_++;
			} else if (structures.length >= 2 || projections.length >= 2)
				numAmbigousMatches_++;
		}
		
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
    @Override
    protected void done() {
		
    	// do not treat the result if process canceled
    	if (isCancelled())
    		return;
   
    	try {
			get();
			
			// displays the result
			if (numPerfectMatches_ == null)
				WingJ.setNumIndividualExperimentsForAggregatedExpression(0, 0);
			else
				WingJ.setNumIndividualExperimentsForAggregatedExpression(numPerfectMatches_, numAmbigousMatches_);
			
		} catch (Exception e) {
			e.printStackTrace();
			WJSettings.log("Error while scanning for individual experiments for generating aggregated expression maps.");
			WingJ.setNumIndividualExperimentsForAggregatedExpression(0,0);
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WJExperimentsSelectionUpdater(String rootExperimentDirectory, String structuresFilenameRegex, String projectionsFilenameRegex) {
		
		rootExperimentDirectory_ = rootExperimentDirectory;
		structuresFilenameRegex_ = structuresFilenameRegex.replace("?", ".?").replace("*", ".*?");
		projectionsFilenameRegex_ = projectionsFilenameRegex.replace("?", ".?").replace("*", ".*?");
		
		structureFiles_ = new ArrayList<File>();
		projectionFiles_ = new ArrayList<File>();
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public List<File> getStructureFiles() { return structureFiles_; }
	public List<File> getProjectionFiles() { return projectionFiles_; }
	
	// ============================================================================
	// INNER CLASSES
	
	/** Searches for individual experiment directories satisfying all the constraints. */
	private class ExperimentFilter implements FilenameFilter {

		/** Returns true if the given dir contains a folder named "WingJ". */
		@Override
		public boolean accept(File dir, String name) {
			
			// the directory must be valid
	    	if (dir == null || !dir.isDirectory())
	    		return false;
	    	
	    	// ignore hidden files starting with '.'
	    	if (name.startsWith("."))
	    		return false;
	    	
	    	// return false if the experiment folder name ends with IGNORE
	    	if (name.endsWith(WINGJ_EXPERIMENT_FOLDER_IGNORE_SUFFIX))
	    		return false;
	    	
	    	File f = new File(dir.getAbsolutePath() + System.getProperty("file.separator") + name);
	    	// get list of folders containing a "WingJ" folder
	    	String[] level1 = f.list(new WJOutputDirectoryFilter());

	    	// if 1 means that WingJ folder has been found, more than 1 should not be possible
	    	return (level1 != null && level1.length == 1);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Searches for a folder containing a "WingJ" folder. */
	private class WJOutputDirectoryFilter implements FilenameFilter {
		
		/** Goes true the entire list of potential experiment folders. */
		@Override
		public boolean accept(File dir, String name) {
			
			// must be a directory
	    	if (dir == null || !dir.isDirectory())
	    		return false;
	    	
	    	// ignore hidden files starting with '.'
	    	if (name.startsWith("."))
	    		return false;
	    	
//	    	// get the children to see if "WingJ" is inside
//	    	String[] children = dir.list(new FilenameFilter() {
//				@Override
//				public boolean accept(File dir2, String name2) {
//					// must be a directory
//			    	if (dir2 == null || !dir2.isDirectory())
//			    		return false;
//			    	// ignore hidden files starting with '.'
//			    	if (name2.startsWith("."))
//			    		return false;
//			    	if (name2.compareTo(WINGJ_OUTPUT_DIRECTORY_NAME) != 0)
//			    		return false;
//			    	
//			    	WJSettings.log(dir2.getAbsolutePath());
//			    	WJSettings.log("name: " + name2);
//					return true;
//				}
//	    	});
	    	
//	    	WJSettings.log("children length: " + children.length);
//	    	
//	    	return !(children == null || children.length != 1);
//	    	return (name.compareTo(WINGJ_OUTPUT_DIRECTORY_NAME) == 0);
	    	return (name.compareToIgnoreCase(WINGJ_OUTPUT_DIRECTORY_NAME) == 0);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Searches for a structure file. */
	private class WJOutputDirectoryContentFilter implements FilenameFilter {

		/** Regex. */
		private String regex_ = null;
		
		/** Constructor. */
		public WJOutputDirectoryContentFilter(String regex) {
			
			regex_ = regex;
		}
		
		/** Goes true the entire content of "WingJ". */
		@Override
		public boolean accept(File dir, String name) {
			
			// must be a directory
	    	if (dir == null || !dir.isDirectory())
	    		return false;
	    	
	    	// ignore hidden files starting with '.'
	    	if (name.startsWith("."))
	    		return false;
			
	    	// '(' and ')' are special symbols which can be used in regex and
	    	// lead to error if there is only one '(' or ')', for example.
	    	boolean ok = false;
	    	try {
	    		ok = name.matches(regex_);
	    	} catch (Exception e) {
	    		// do nothing and the file with invalid pattern 
	    	}
	    	
	    	return ok;
		}
	}
}
