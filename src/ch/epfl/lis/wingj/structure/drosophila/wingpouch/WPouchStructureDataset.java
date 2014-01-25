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

package ch.epfl.lis.wingj.structure.drosophila.wingpouch;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.filefilters.FilterGenericFilename;
import ch.epfl.lis.wingj.filefilters.FilterStructureMeasurementsXml;
import ch.epfl.lis.wingj.filefilters.FilterStructureXml;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureDataset;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureSnake;
import ch.epfl.lis.wingj.utilities.FileUtils;

/**
 * Implements methods to save wing structure datasets.
 * <p>
 * This class implements a thread worker to allow saving complete dataset
 * of several structures in parallel.
 * 
 * @version September 6, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WPouchStructureDataset extends StructureDataset {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor with given structure object. */
	public WPouchStructureDataset(Structure structure) {
		
		super(structure);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor with given structure object and structure viewer. */
	public WPouchStructureDataset(Structure structure, WJStructureViewer structureVisualization) {
		
		super(structure, structureVisualization);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to save the structure to file (here in XML and TXT format). */
	@Override
	public void saveStructure() throws Exception {
	
		WPouchStructure structure = (WPouchStructure)structure_;
		
		if (structure == null)
			throw new Exception("ERROR: Structure is null.");
		
		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
		
		if (snake == null)
			throw new Exception("ERROR: Snake structure is null.");
		
		WJSettings settings = WJSettings.getInstance();
		
		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			WingJ.setAppIcon(frame);
			
			fc.setDialogTitle("Save structure model");
			File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	    	fc.setCurrentDirectory(f);
	    	fc.addChoosableFileFilter(new FilterStructureXml());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(Structure.STRUCTURE_DEFAULT_FILENAME));
			
			int returnVal = fc.showDialog(frame, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				WPouchStructureDataset.saveStructure(file.toURI(), structure);
			}
			return;
		} catch (Exception e) {
			throw new Exception("ERROR: Failed to save XML file: " + e.toString() + ".");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens a dialog to save the structure measurements. */
	@Override
	public void saveStructureMeasurements() throws Exception {
		
		WPouchStructure structure = (WPouchStructure)structure_;
		
		if (structure == null)
			throw new Exception("ERROR: Structure is null.");
		
		WJSettings settings = WJSettings.getInstance();
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		
		fc.setDialogTitle("Save structure measurements");
		WingJ.setAppIcon(frame);
		File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
    	fc.setCurrentDirectory(f);
    	fc.addChoosableFileFilter(new FilterStructureMeasurementsXml());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(/*pouch.getName() + */"structure_measurements.xml"));
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			structure.writeStructureMeasurements(file.toURI());
		}
	}
	
	//----------------------------------------------------------------------------
	
	/** Opens a dialog to save the entire structure dataset. */
	@Override
	public void saveStructureDataset(boolean quiet) throws Exception {
		
		WPouchStructure structure = (WPouchStructure)structure_;
		
		WJSettings settings = WJSettings.getInstance();
		
		if (structure.getStructureSnake() == null)
			throw new Exception("ERROR: Snake structure is null.");
		if (structureVisualization_ == null)
			throw new Exception("ERROR: Structure preview is null.");
		if (structureVisualization_.getStructure() == null)
			throw new Exception("ERROR: Structure is null.");
		
		String root = "";
		if (quiet)
			root = settings.getOutputDirectory() + settings.getExperimentName(); /* + settings.getExperimentName()*/
		else {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();
			
			fc.setDialogTitle("Save structure dataset");
			WingJ.setAppIcon(frame);
			File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	    	fc.setCurrentDirectory(f);
	    	fc.addChoosableFileFilter(new FilterGenericFilename());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(structureVisualization_.getStructure().getName()));
			int returnVal = fc.showDialog(frame, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION)
				root = fc.getSelectedFile().getPath();
		}
		
		// save dataset
		if (root != "") {
			WPouchStructureDataset.saveStructure(FileUtils.getFileURI(root + "_structure.xml"), structure);
			WPouchStructureDataset.saveStructureMeasurements(FileUtils.getFileURI(root + "_structure_measurements.xml"), structure);
			WPouchStructureDataset.saveStructurePreview(FileUtils.getFileURI(root + "_structure.tif"), structureVisualization_);
			WPouchStructureDataset.saveStructureMask(FileUtils.getFileURI(root + "_structure_mask.tif"), structureVisualization_);
		}
	}
}
