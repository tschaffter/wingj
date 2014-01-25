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

import ij.ImagePlus;
import ij.plugin.Duplicator;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.analytics.Analytics;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.utilities.StringUtils;

/**
 * Wrap a circular expression map on a given structure model.
 * 
 * @version November 24, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionDataset2DReversed extends SwingWorker<Void, Void> {

	/** Target structure model. */
	protected Structure targetStructure_ = null;
	/** Circular expression map to wrap on top of the target structure. */
	protected CircularExpressionMap circularMap_ = null;	
	/** Selected boundary (A/P or D/V) used as equator when generating the circular expression map. */
	protected int equator_ = WJSettings.BOUNDARY_AP;
	
	/** Target expression map (circular map wrapped on the target structure). */
	protected ExpressionMap expressionMap_ = null;
	
	/** Structure viewer for showing the structure on top of the expression map (required to be static for closing it later). */
	public static WJStructureViewer structureViewer_ = null;

	/**
	 * If null or empty, show the 2D expression dataset generated.
	 * Otherwise, save the 2D expression dataset to files.
	 */
	protected String filename_ = null;

	// ============================================================================
	// PRIVATE METHODS

	/** Generates the reversed 2D expression dataset. */
	private void generateDataset() throws Exception {

		if (targetStructure_ == null)
			throw new Exception("ERROR: Target structure is null.");
		if (circularMap_ == null)
			throw new Exception("ERROR: Circular expression map is null.");
		if (circularMap_.getWidth() != circularMap_.getHeight())
		
		WJSettings.log("Starting computing reversed expression map.");
		WJSettings.log("A message will be displayed at the end of the process.");
		ExpressionMapsAggregator aggregator = new ExpressionMapsAggregator();
		aggregator.setTargetStructure(targetStructure_);
		aggregator.setTargetCircularExpressionMap(circularMap_);
		aggregator.setEquator(equator_);
		aggregator.run();
		
		// copies output of aggregator
		targetStructure_ = aggregator.getTargetStructure().copy();
		ImagePlus ip = new Duplicator().run(aggregator.getTargetExpressionMap());
		expressionMap_ = new ExpressionMap(ip.getTitle(), ip.getProcessor());
		expressionMap_.setTitle("reversed_expression_map");
		
		ImagePlusManager.getInstance().add(expressionMap_.getTitle(), expressionMap_, false);
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public ExpressionDataset2DReversed(Structure targetStructure, CircularExpressionMap circularMap, int equator) throws Exception {

		// clone the elements to enable running many datasets in parallel
		ImagePlus ip = new Duplicator().run(circularMap);
		circularMap_ = new CircularExpressionMap("circular_expression_map", ip.getProcessor());
		targetStructure_ = targetStructure.copy();
		equator_ = equator;
		
		ImagePlusManager.getInstance().add(circularMap_.getTitle(), circularMap_, WJSettings.DEBUG);
	}

	// ----------------------------------------------------------------------------

	/** Run method. */
	@Override
	protected Void doInBackground() throws Exception {

		WingJ.getInstance().registerActiveExpressionDatasetProcess();
		generateDataset();
		return null;
	}

	// ----------------------------------------------------------------------------

	/** This function is called once the dataset has been generated. */
	@Override
	protected void done() {

		try {
			get();
			
			// closes previous viewer (if any)
			if (structureViewer_ != null)
				structureViewer_.setVisible(false);

			if (filename_ == null || filename_.compareTo("") == 0) { // show datasets
				WJSettings.log("Showing expression dataset.");
				// preview of the grid (to show the difference with equator set to A/P or D/V)
				ImagePlusManager.getInstance().show("expression_sampling_grid_preview");
				// target expression map with structure displayed
				structureViewer_ = new WJStructureViewer(targetStructure_, expressionMap_);
				structureViewer_.setVisible(true);
				// target expression map
				WJImages.showExpressionImage(expressionMap_);
			}
			else { // save dataset to file
				
				WJSettings.log("Exporting expression dataset.");

				try {
					File file = new File(filename_ + ".tif");
					WJImages.saveExpressionImage(file.toURI().getPath(), expressionMap_);
//					if (settings.normalizeExpression())
//						WJImages.save32Bit(file.toURI().getPath(), expressionMap_);
//					else
//						WJImages.save32BitTo8BitWithoutScaling(file.toURI().getPath(), expressionMap_);
//					ImagePlus clone = new Duplicator().run(expressionMap_);
////					new ImageConverter(clone).convertToRGB();
//					ImageConverter converter = new ImageConverter(clone);
//					if (!settings.normalizeExpression()) converter.convertToGray8();
//					else converter.convertToGray32();
//					IJ.save(clone, file.toURI().getPath());
//					clone.close();
					WJSettings.log("[x] Writing reversed expression map (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing reversed expression map (tif)");
					WJMessage.showMessage(e);
				}

				try {
					File file = new File(filename_ + "_plus.tif");
					structureViewer_ = new WJStructureViewer(targetStructure_, expressionMap_, true); // use ImageWindow (not displayed)
					structureViewer_.save(file.toURI());
					WJSettings.log("[x] Writing reversed expression map with structure (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing reversed expression map with structure (tif)");
					WJMessage.showMessage(e);
				}
				
				try {
					File file = new File(filename_ + "_mask.tif");
					WJImagesMask.saveBinaryMask(file.toURI(), expressionMap_, targetStructure_);
					WJSettings.log("[x] Writing reversed expression map binary mask (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Writing reversed expression map binary mask (tif)");
					WJMessage.showMessage(e);
				}
				
				// remove images
				// normally now there should be nothing to hide...
				disposeVisibleOutput();
			}
			WJSettings.log("Done");
			
		} catch (Exception e1) {
			String eStr = StringUtils.exceptionToString(e1);
			if (eStr.contains("OutOfMemoryError")) {
				WJMessage.showMessage(WJSettings.OUT_OF_MEMORY_ERROR_MESSAGE, "ERROR");
				// DO NOT REMOVE THIS LINE
				// ANALYTICS CODE: START
				Analytics.getInstance().incrementNumOutOfMemoryErrors();
				// END
			} else {
				WJMessage.showMessage(e1);
			}
		} finally {
			WingJ.getInstance().removeActiveExpressionDatasetProcess();
		}
		
	}

	// ----------------------------------------------------------------------------

	/** Closes old expression images open (if any). */
	public static void disposeVisibleOutput() throws Exception {
		
		if (structureViewer_ != null) {
			structureViewer_.setVisible(false);
			structureViewer_ = null;
		}
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.removeAllContainingSubString("reverse"); // also include reversed ;)
		manager.removeAllContainingSubString("expression");
		manager.removeAllContainingSubString("structure");
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public void setFilename(String filename) { filename_ = filename; }
}
