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

import java.net.URI;

import javax.swing.SwingWorker;

import ch.epfl.lis.wingj.StructureViewer;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.utilities.FileUtils;

/** 
 * Thread worker to run a single experiment from the batch experiments.
 * 
 * @version September 6, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class BatchExperimentDataset extends SwingWorker<Void, Void> {

	/** Structure detector. */
	@SuppressWarnings("unused")
	private WPouchStructureDetector detector_ = null;
	/** Structure visualization */
	@SuppressWarnings("unused")
	private StructureViewer structureVisualization_ = null;

	// ============================================================================
	// PROTECTED METHODS

	/** Run the thread */
	@Override
	protected Void doInBackground() throws Exception {
		
		throw new Exception("ERROR: Fix BatchExperimentDataset::doInBackground().");

//		WJSettings settings = WJSettings.getInstance();
//
//		// disable GUI interface
//		WingJ.getInstance().setGuiEnabled(false, false);
//		WingJ.getInstance().setWaitingSnakeVisible(true);
//
//		// export all datasets without prompting user for filenames)
//		List<SwingWorker<Void, Void>> threads = new ArrayList<SwingWorker<Void, Void>>();
//
//		// projection of each channel loaded
//		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
//			if (WJImages.numSlices_[i] > 0) {
//				try {
//					ImagePlus projection = WingJ.getInstance().computeProjectionForExpressionQuantification(i);
//					new ImageConverter(projection).convertToGray8();
//					IJ.save(projection, settings.getOutputDirectory() + settings.getGeneNames(i) + "_projection.tif");
//					projection.close();
//					WJSettings.log("[x] " + settings.getGeneNames(i) + " projection slices "  + settings.getExpressionMinSliceIndex(i) + "-" + settings.getExpressionMaxSliceIndex(i) + " (tif)");
//				} catch (Exception e) {
//					WJSettings.log("[ ] " + settings.getGeneNames(i) + " projection slices "  + settings.getExpressionMinSliceIndex(i) + "-" + settings.getExpressionMaxSliceIndex(i) + " (tif)");
//					WJMessage.showMessage(e);
//				}
//			}
//		}
//
//		WJSettings.getInstance().saveSettings(FileUtils.getFileURI(settings.getOutputDirectory() + "settings.txt"));
//		WJImages.computeComposite(FileUtils.getFileURI(settings.getOutputDirectory() + "composite.tif"));
//		WJImages.saveAoi(FileUtils.getFileURI(settings.getOutputDirectory() + "aoi.tif"));
//
//		// structure dataset
//		StructureDataset structureDataset = new StructureDataset(detector_, structureVisualization_);
//		structureDataset.execute();
//		threads.add(structureDataset);
//
//		//    	// expression datasets (1D and 2D)
//		//    	// those thread are starting immediately
//		//    	for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
//		//    		if (WDiscImages.numSlices_[i] > 0) {
//		//	    		// quantify only channel which have image stack loaded
//		//	    		ImagePlus projection = WingJ.getInstance().computeProjectionForExpressionQuantification(i);
//		//	    		if (projection != null)
//		//	    			threads.addAll(ExpressionDataset.saveExpressionDataset(i, projection, snake_, structureVisualization_.getWPouch())); // multiple threads
//		//    		}
//		//    	}
//
//		// wait for all thread
//		for (int i = 0; i < threads.size(); i++)
//			threads.get(i).get();
//
//		return null;
	}

	// ----------------------------------------------------------------------------

	/** This function is called once all datasets have been saved */
	@Override
	protected void done() {

		try {
			get();

			// save log
			WJSettings settings = WJSettings.getInstance();
			URI uri = FileUtils.getFileURI(settings.getOutputDirectory() + "log.txt");
			WJSettings.writeLog(uri);

			// enable GUI interface
			WingJ.getInstance().setGuiEnabled(true, true);
			WingJ.getInstance().setWaitingSnakeVisible(false);
			// finalize and move to next experiment (if any)
			WingJ.getInstance().finilizeExperiment();

		} catch (Exception e) {		
			WJMessage.showMessage(e);
		}
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor */
	public BatchExperimentDataset(WPouchStructureDetector detector, StructureViewer structureVisualization) {

		detector_ = detector;
//		snake_ = snake;
		structureVisualization_ = structureVisualization;
	}
}
