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

package ch.epfl.lis.wingj.expression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.filefilters.FilterGenericFilename;
import ch.epfl.lis.wingj.structure.Structure;

import ij.ImagePlus;

/**
 * Implements methods to save expression dataset.
 * 
 * @version August 30, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ExpressionDataset {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Visualizes 1D expression datasets.  */
	static public ExpressionDataset1D showExpressionDataset1D(int channel, ImagePlus image, Structure structure) throws Exception {
		
		// if filename is null or empty, the dataset is displayed instead of saved
		return ExpressionDataset.saveExpressionDataset1D(null, channel, image, structure);
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves 1D expression datasets (open a dialog to select generic filename). */
	static public ExpressionDataset1D saveExpressionDataset1D(int channel, ImagePlus image, Structure structure) throws Exception {
		
		if (image == null)
			throw new Exception("INFO: Single image or image stack for expression required.");
		if (structure == null)
			throw new Exception("INFO: Structure detection first required.");
		if (structure.getStructureSnake() == null)
			throw new Exception("INFO: Structure detection first required.");
		
		WingJ wingj = WingJ.getInstance();
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Save 1D expression dataset for " + wingj.getSelectedExpressionChannelName());
		WingJ.setAppIcon(frame);
		
		// get the default generic filename
     	String proposition = getExpressionDataset1dDefaultFilename(channel);
     	fc.setSelectedFile(new File(proposition));
    	fc.addChoosableFileFilter(new FilterGenericFilename());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return ExpressionDataset.saveExpressionDataset1D(file.getAbsolutePath(), channel, image, structure);
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves 1D expression datasets. */
	static public ExpressionDataset1D saveExpressionDataset1D(String filename, int channel, ImagePlus image, Structure structure) throws Exception {
	
		WJSettings settings = WJSettings.getInstance();
		
		ExpressionDataset1D dataset = new ExpressionDataset1D();
		dataset.setExpressionImage(image);
		dataset.setStructure(structure);
		
		dataset.setReferenceBoundary(settings.getExpression1DBoundary());		
		dataset.setTrajectoryOffset(settings.getExpression1DTranslation());
		dataset.setSigma(settings.getExpression1DSigma());
		
		dataset.setGeneName(settings.getGeneName(channel));
		dataset.setFilename(filename);
		
		dataset.execute();
		
		return dataset;
	}
	
	//----------------------------------------------------------------------------
	
	/** Visualizes 2D expression datasets. */
	static public ExpressionDataset2D showExpressionDataset2D(int channel, ImagePlus image, Structure structure) throws Exception {
		
		// if filename is null or empty, the dataset is displayed instead of saved
		return ExpressionDataset.saveExpressionDataset2D(null, channel, image, structure);
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves 2D expression datasets (open a dialog to select generic filename). */
	static public ExpressionDataset2D saveExpressionDataset2D(int channel, ImagePlus image, Structure structure) throws Exception {
		
		if (image == null)
			throw new Exception("INFO: Single image or image stack for expression required.");
		if (structure == null)
			throw new Exception("INFO: Structure detection first required.");
		if (structure.getStructureSnake() == null)
			throw new Exception("INFO: Structure detection first required.");
		
		WingJ wingj = WingJ.getInstance();
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Save 2D expression dataset for " + wingj.getSelectedExpressionChannelName());
		WingJ.setAppIcon(frame);
		
		// get the default generic filename
     	String proposition = getExpressionDataset2dDefaultFilename(channel);
     	fc.setSelectedFile(new File(proposition));
    	fc.addChoosableFileFilter(new FilterGenericFilename());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return ExpressionDataset.saveExpressionDataset2D(file.getAbsolutePath(), channel, image, structure);
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves 2D expression datasets. */
	static public ExpressionDataset2D saveExpressionDataset2D(String filename, int channel, ImagePlus image, Structure structure) throws Exception {
	
		WJSettings settings = WJSettings.getInstance();
		ExpressionDataset2D dataset = new ExpressionDataset2D(image, structure);
		dataset.setFilename(filename);
		dataset.setGeneName(settings.getGeneName(channel));
		dataset.setBoundaryConserved(settings.getExpression2dOffset(channel));
		dataset.execute();
		
		return dataset;
	}
	
	//----------------------------------------------------------------------------
	
	/** Shows aggregated expression datasets. */
	static public ExpressionDataset2DAggregated showAggregatedExpressionDataset2D(List<Structure> structures, List<ExpressionMap> projections) throws Exception {
		
		// if filename is null or empty, the dataset is displayed instead of saved
		return ExpressionDataset.saveAggregatedExpressionDataset2D(null, structures, projections);
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves aggregated expression datasets (open a dialog to select generic filename). */
	static public ExpressionDataset2DAggregated saveAggregatedExpressionDataset2D(List<Structure> structures, List<ExpressionMap> projections) throws Exception {
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);		
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Save aggregated expression dataset");
		WingJ.setAppIcon(frame);
		
		// get the default generic filename
     	String proposition = getAggregatedExpressionDataset2dDefaultFilename();
     	fc.setSelectedFile(new File(proposition));
    	fc.addChoosableFileFilter(new FilterGenericFilename());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
     	File f = new File(new File(WingJ.getInstance().expressionAggRootTField_.getText()).getCanonicalPath());     	
     	fc.setCurrentDirectory(f.getCanonicalFile());
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return ExpressionDataset.saveAggregatedExpressionDataset2D(file.getAbsolutePath(), structures, projections);
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves aggregated expression dataset. */
	static public ExpressionDataset2DAggregated saveAggregatedExpressionDataset2D(String filename, List<Structure> structures, List<ExpressionMap> projections) throws Exception {
		
		if (structures.size() == 0 && projections.size() == 0)
			throw new Exception("INFO: At least one individual experiment is required.");
		if (structures.size() != projections.size())
			throw new Exception("ERROR: Number of structures and projections must be equal.");
		
		ExpressionDataset2DAggregated dataset = new ExpressionDataset2DAggregated(structures, projections);
		dataset.setFilename(filename);
		dataset.execute();
		
		return dataset;
	}
	
	//----------------------------------------------------------------------------
	
	/** Shows reversed expression datasets. */
	static public ExpressionDataset2DReversed showReversedExpressionDataset2D(Structure structure, CircularExpressionMap circularMap, int equator) throws Exception {
		
		// if filename is null or empty, the dataset is displayed instead of saved
		return ExpressionDataset.saveReversedExpressionDataset2D(null, structure, circularMap, equator);
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves reversed expression datasets (open a dialog to select generic filename). */
	static public ExpressionDataset2DReversed saveReversedExpressionDataset2D(Structure structure, CircularExpressionMap circularMap, int equator) throws Exception {
		
		JFrame frame = new JFrame();
		frame.setAlwaysOnTop(true);
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Save reversed expression dataset");
		WingJ.setAppIcon(frame);
		
		// get the default generic filename
     	String proposition = getReversedExpressionDataset2dDefaultFilename();
     	fc.setSelectedFile(new File(proposition));
    	fc.addChoosableFileFilter(new FilterGenericFilename());
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int returnVal = fc.showDialog(frame, "Save");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			return ExpressionDataset.saveReversedExpressionDataset2D(file.getAbsolutePath(), structure, circularMap, equator);
		}
		return null;
	}
	
	//----------------------------------------------------------------------------
	
	/** Saves reversed expression dataset. */
	static public ExpressionDataset2DReversed saveReversedExpressionDataset2D(String filename, Structure structure, CircularExpressionMap circularMap, int equator) throws Exception {
		
		ExpressionDataset2DReversed dataset = new ExpressionDataset2DReversed(structure, circularMap, equator);
		dataset.setFilename(filename);
		dataset.execute();
		
		return dataset;
	}
	
	//----------------------------------------------------------------------------
	
	/** Gets default generic filename for saving 1D expression datasets. */
	static public String getExpressionDataset1dDefaultFilename(int channel) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
     	String proposition = /*settings.getExperimentName() + "_" +*/ settings.getGeneName(channel);
     	proposition += "_expression_profile";
		if (settings.getExpression1DBoundary() == WJSettings.BOUNDARY_DV) proposition += "_DV";
		if (settings.getExpression1DBoundary() == WJSettings.BOUNDARY_AP) proposition += "_AP";
		proposition += settings.getExpression1DTranslation();
		
		return settings.getOutputDirectory() + proposition;
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the default generic filename for saving 2D expression datasets. */
	static public String getExpressionDataset2dDefaultFilename(int channel) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
     	String proposition = /*settings.getExperimentName() + "_" +*/ settings.getGeneName(channel);
     	proposition += "_expression_map";
		
		return settings.getOutputDirectory() + proposition;
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the default generic filename for saving aggregated expression dataset. */
	static public String getAggregatedExpressionDataset2dDefaultFilename() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
     	String proposition = ""; /*settings.getExperimentName() + "_" +*/ //settings.getGeneName(channel);
     	proposition += "GENENAME_mean_model";
		
		return settings.getOutputDirectory() + proposition;
	}
	
	//----------------------------------------------------------------------------
	
	/** Returns the default generic filename for saving reversed expression dataset. */
	static public String getReversedExpressionDataset2dDefaultFilename() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
     	String proposition = ""; /*settings.getExperimentName() + "_" +*/ //settings.getGeneName(channel);
     	proposition += "GENENAME_reversed_expression_map";
		
		return settings.getOutputDirectory() + proposition;
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Saves all 1D and 2D expression datasets using multiple threads. When the function returns,
	 * the threads have already been launched.
	 */
	static public List<SwingWorker<Void, Void>> saveExpressionDataset(int channel, ImagePlus image, Structure structure) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		if (image == null)
			throw new Exception("INFO: Single image or image stack for expression required.");
		if (structure == null)
			throw new Exception("INFO: Structure detection first required.");
		if (structure.getStructureSnake() == null)
			throw new Exception("INFO: Structure detection first required.");
		
		List<SwingWorker<Void, Void>> threads = new ArrayList<SwingWorker<Void, Void>>();
		int referenceBoundaryBkp = settings.getExpression1DBoundary();
		double offsetBkp = settings.getExpression1DTranslation();
		
		// 1D
		List<Double> offsets = settings.getExpressionOffsets();
		for (int i = 0; i < offsets.size(); i++) {
			// set offset
			settings.setExpression1DTranslation(offsets.get(i));
			// along (shifted) D/V boundary
			settings.setExpression1DBoundary(WJSettings.BOUNDARY_DV);
			threads.add(ExpressionDataset.saveExpressionDataset1D(ExpressionDataset.getExpressionDataset1dDefaultFilename(channel), channel, image, structure)); // thread starts
			// along (shifted) A/P boundary
			settings.setExpression1DBoundary(WJSettings.BOUNDARY_AP);
			threads.add(ExpressionDataset.saveExpressionDataset1D(ExpressionDataset.getExpressionDataset1dDefaultFilename(channel), channel, image, structure)); // thread starts
		}
		settings.setExpression1DBoundary(referenceBoundaryBkp);
		settings.setExpression1DTranslation(offsetBkp);
		
		// 2D
		List<Double> thresholds = settings.getExpressionThresholds();
		double thresholdBkp = settings.getExpression2dOffset(channel);
		for (int i = 0; i < thresholds.size(); i++) {
			settings.setExpression2dOffset(channel, thresholds.get(i));
			threads.add(ExpressionDataset.saveExpressionDataset2D(ExpressionDataset.getExpressionDataset2dDefaultFilename(channel), channel, image, structure));
		}
		settings.setExpression2dOffset(channel, thresholdBkp);
		
		return threads;
	}
}
