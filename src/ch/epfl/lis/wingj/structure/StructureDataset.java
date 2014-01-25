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

package ch.epfl.lis.wingj.structure;

import java.io.File;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.filefilters.FilterImageTiff;

/**
 * Implements methods to save structure datasets.
 * <p>
 * This class implements a thread worker to allow saving complete dataset
 * of multiple structures in parallel.
 * 
 * @version September 6, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
abstract public class StructureDataset extends SwingWorker<Void, Void> {
	
	/** Structure object. */
	protected Structure structure_ = null;

	/** Structure visualization (image + structure + information overlay) */
	protected WJStructureViewer structureVisualization_ = null;
	
	/** Saves the dataset without prompting the user. */
	protected boolean quiet_ = true;
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Opens a dialog to save the structure to file. */
	abstract public void saveStructure() throws Exception;
	/** Opens a dialog to save the structure measurements. */
	abstract public void saveStructureMeasurements() throws Exception;
	/** Opens a dialog to save the entire structure dataset. */
	abstract public void saveStructureDataset(boolean quiet) throws Exception;
	
	// ============================================================================
	// PROTECTED METHODS

	/** Runs the thread and exports the structure dataset. */
	@Override
	protected Void doInBackground() throws Exception {

		saveStructureDataset(quiet_);
		return null;
	}
	
	// ----------------------------------------------------------------------------

	/** This function is called once all structure files have been saved. */
    @Override
    protected void done() {
		
    	try {
			get();
			
		} catch (Exception e) {		
			WJMessage.showMessage(e);
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public StructureDataset(Structure structure) {
		
		structure_ = structure;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public StructureDataset(Structure structure, WJStructureViewer structureVisualization) {
		
		structure_ = structure;
		structureVisualization_ = structureVisualization;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the structure to file directly to the given URI. */
	static public void saveStructure(URI uri, Structure structure) {
		
		try {
			structure.write(uri);
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the structure measurement to file directly to the given URI. */
	static public void saveStructureMeasurements(URI uri, Structure structure) {
		
		try {
			structure.writeStructureMeasurements(uri);
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}

	//----------------------------------------------------------------------------
	
	/** Saves the content of the structure viewer to TIF file (open a dialog to select the target file). */
	static public void saveStructurePreview(WJStructureViewer structureVisualization) throws Exception {
		
		if (structureVisualization == null)
			throw new Exception("ERROR: Structure viewer is null.");
		if (structureVisualization.getStructure() == null)
			throw new Exception("ERROR: Structure is null.");
		
		WJSettings settings = WJSettings.getInstance();
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		
		fc.setDialogTitle("Save structure preview");
		WingJ.setAppIcon(frame);
		File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
    	fc.setCurrentDirectory(f);
    	fc.addChoosableFileFilter(new FilterImageTiff());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(/*structureVisualization.getWPouch().getName() + */"structure.tif"));
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			saveStructurePreview(file.toURI(), structureVisualization);
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves the content of the structure viewer to TIF file. */
	static public void saveStructurePreview(URI uri, WJStructureViewer structureVisualization) {
		
		try {
	    	structureVisualization.save(uri);
			WJSettings.log("[x] Writing structure preview (tif)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure preview (tif)");
			WJMessage.showMessage(e);
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves the mask of the detected wing pouch structure. */
	static public void saveStructureMask(URI uri, WJStructureViewer structureVisualization) {
		
		try {
	    	// save
        	WJImagesMask.saveBinaryMask(uri, structureVisualization.getImage(), structureVisualization.getStructure());
			WJSettings.log("[x] Writing sructure binary mask (tif)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure binary mask (tif)");
			WJMessage.showMessage(e);
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves the mask of the detected wing pouch structure (open a dialog to select the target file). */
	static public void saveStructureMask(WJStructureViewer structureVisualization) throws Exception {
		
		if (structureVisualization == null)
			throw new Exception("WARNING: Structure viewer is null.");
		if (structureVisualization.getStructure() == null)
			throw new Exception("ERROR: Structure is null.");
		
		WJSettings settings = WJSettings.getInstance();
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		
		fc.setDialogTitle("Save structure mask");
		WingJ.setAppIcon(frame);
		File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
    	fc.setCurrentDirectory(f);
    	fc.addChoosableFileFilter(new FilterImageTiff());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(/*structureVisualization.getWPouch().getName() + */"structure_mask.tif"));
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			saveStructureMask(file.toURI(), structureVisualization);
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setStructureVisualization(WJStructureViewer viewer) { structureVisualization_ = viewer; }
	public void setQuiet(boolean quiet) { quiet_ = quiet; }
}
