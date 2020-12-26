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

import ch.epfl.lis.wingj.utilities.FileUtils;


/** 
 * Converts pent experiment sent by Fisun beginning of November 2011.
 * 
 * The implementation is non-destructive for input files (copy but don't move).
 * 
 * @version September 8, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class BatchExperimentsConverterPent extends BatchExperimentsConverter {

	
	// ============================================================================
	// PROTECTED METHODS
	
	/** Create a filter which returns true if the folder can be considered as an old experiment folder */
	@Override
	protected FilenameFilter oldExperimentFolderFilter() {
		
		FilenameFilter filter = new FilenameFilter() {
		    @Override
			public boolean accept(File dir, String name) {
		    	try {
			    	// the experiment folder name must not start with "."
			    	if (name.startsWith(".")) return false;
			    	// check if the folder contains at least one tif file
			    	URI uri = FileUtils.getFileURI(dir.getAbsoluteFile() + "/" + name);
			    	File experimentFile = new File(uri);
			    	String[] children = experimentFile.list();
			    	if (children == null) return false;
			    	for (int i = 0; i < children.length; i++) {
			    		if (children[i].endsWith(".tif")) return true; // must contain at least one tif file
			    	}
			    	return false;
		    	} catch (Exception e) {
		    		return false;
		    	}
		    }
		};
		return filter;
	}
	
	// ----------------------------------------------------------------------------

	/** List the names of all old experiments (folder names) */
	@Override
	protected String[] listExperimentFolderNames(String directory) throws Exception {
		
		URI uri = FileUtils.getFileURI(directory);
		File oldDirectoryFile = new File(uri);
		
		if (!oldDirectoryFile.exists())
			throw new Exception("ERROR: The old root directory " + directory + " doesn't exist.");
		if (!oldDirectoryFile.isDirectory())
			throw new Exception("ERROR: The old root directory " + directory + " is not a directory.");
		
		return oldDirectoryFile.list(oldExperimentFolderFilter());
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Main */
	public static void main(String[] args) {
		
		try {			
			
			// + update the name of a list of files/folders
//			BatchExperimentsConverterPent converter = new BatchExperimentsConverterPent();
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_72-73H");
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_76,5-77,5H");
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_79-80H");
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_89-90H");
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_100-101H");
//			converter.updateFilenames("/mnt/extra/2011_pent_wt-and-mutant/yw_salAB_dadGFP/salAB_dadGFP_wg-ptcAB_110,5-111,5H");
			
			// ----------------------------------------------------------------------------
			
			BatchExperimentsConverterPent converter = new BatchExperimentsConverterPent();
			converter.setOldRootDirectory("/mnt/extra/source");
			converter.setNewRootDirectory("/mnt/extra/target");
			converter.run();
			
			// ----------------------------------------------------------------------------
			
			// + convert experiments
//			String oldSuperRootDirectory = "/mnt/extra/pent_fisun";
//			String newSuperRootDirectory = "/mnt/extra/2011_pent_wt-and-mutant";
//			
//			File oldSuperRootFile = new File(oldSuperRootDirectory);
//			String[] folders = oldSuperRootFile.list();
//			
//			if (folders == null)
//				throw new Exception("ERROR: No files in " + oldSuperRootDirectory);
//
//			for (int i = 0; i < folders.length; i++) {
//				File file = new File(oldSuperRootDirectory + "/" + folders[i]);
//				
//				if (file.isFile()) continue; // we process only folders
//				
//				// create new folder
//				String newRootDirectory = newSuperRootDirectory + "/" + BatchExperimentsConverter.updateSingleFilename(folders[i]);
//				Files.mkdir(newRootDirectory);
//				
//				BatchExperimentsConverterPent converter = new BatchExperimentsConverterPent();
//				converter.setOldRootDirectory(oldSuperRootDirectory + "/" + folders[i]);
//				converter.setNewRootDirectory(newRootDirectory);
//				converter.run();
//			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Default constructor */
	public BatchExperimentsConverterPent() {
		
		super();
		
		// October 3
		// pent experiments sent by Fisun are most of the time (check to be sure!)
		// ch00 -> G -> must become ch01
		// ch01 -> R -> must become ch00
		// ch02 -> B -> must become ch02
		// experiment names are already correct (RGB)
		
		oldTags_.set(0, "ch00"); newTags_.set(0, "ch01");
		oldTags_.set(1, "ch01"); newTags_.set(1, "ch00");
		oldTags_.set(2, "ch02"); newTags_.set(2, "ch02");

		for (int i = 0; i < oldChannelFolders_.size(); i++)
			oldChannelFolders_.set(i, ".");
	}
}
