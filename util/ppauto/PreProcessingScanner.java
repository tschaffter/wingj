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

package detection.ppauto;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import core.WDiscMask;
import core.WJMessage;
import core.WJSettings;
import core.WingJ;

import ij.ImagePlus;
import ij.plugin.Duplicator;

/** 
 * Goes through combinations of parameters (blur,thld) and stops when first valid parameter couple found.
 * 
 * @version September 13, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class PreProcessingScanner extends SwingWorker<Void, Void>  {
	
	/** Structure projection */
	private ImagePlus structureProjection_ = null;
	
	/** Incremental step for blur parameter */
	private double blurStep_ = 0.;
	/** Min value for blur */
	private double minBlur_ = 0.;
	/** Max value for blur */
	private double maxBlur_ = 0.;
	
	/** Incremental step for thld parameter */
	private int thldStep_ = 0;
	/** Min value for thld */
	private int minThld_ = 0;
	/** Max value for thld */
	private int maxThld_ = 0;
	
	/** First valid solution found */
	private PreProcessingTester solution_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public PreProcessingScanner(double blurStep, double minBlur, double maxBlur, int thldStep, int minThld, int maxThld) {
	
		blurStep_ = blurStep;
		minBlur_ = minBlur;
		maxBlur_ = maxBlur;
		
		thldStep_ = thldStep;
		minThld_ = minThld;
		maxThld_ = maxThld;
	}

	// ----------------------------------------------------------------------------
	
	/** Scan the different couple of parameter values (blur/thld) */
	@Override
	protected Void doInBackground() throws Exception {
		
		return findThreshold();
	}
	
	// ---------------------------------------------------------------------------
	
	/** Scan the different couple of parameter values (blur/thld) */
	public Void findThreshold() throws Exception {
		
		if (structureProjection_ == null || structureProjection_.getProcessor() == null)
			throw new Exception("ERROR: structureProjection_ is null.");
		
		if (blurStep_ == 0 || thldStep_ == 0)
			throw new Exception("ERROR: blurStep_ and thldStep_ must be different than 0.");
		
		WingJ.getInstance().setGuiEnabled(false, false);
		WingJ.getInstance().setSnakeVisible(true);
		
		try {
			// mask the image if ROI defined for original structure projection
			WDiscMask.applyMask(structureProjection_);
		} catch (Exception e) {
			// do nothing
		}
		
		// if the meaningful thld doesn't lead to valid solution
		// build a list of thread
		List<SwingWorker<Void, Void>> threads = new ArrayList<SwingWorker<Void, Void>>();
		// run thread from bigger thld to lower, but since it's done in // I don't know if
		// there is a really advantage for higher values of thld
		for (int thld = maxThld_; thld >= minThld_; thld -= thldStep_)
			threads.add(new PreProcessingBatchTester(this, Math.max(thld, minThld_), blurStep_, minBlur_, maxBlur_));
		
		// execute all thread
		for (int i = 0; i < threads.size(); i++) {
			threads.get(i).execute();
//			threads.get(i).get();
		}
		
		// wait until all threads have returned
		for (int i = 0; i < threads.size(); i++)
			threads.get(i).get();
		
		return null;
	}
	
	// ----------------------------------------------------------------------------

	/** This function is called once the scanner is done */
    @Override
    protected void done() {
		
    	try {
			get();
			
			if (solution_ != null) {
				WJSettings.log("Optimal pre-processing parameters found");
				WJSettings.log("blur: " + solution_.getBlur());
				WJSettings.log("Thld: " + solution_.getThld());
				
				// provide feedback via WingJ interface
				WingJ.getInstance().setPreProcessingParameters(solution_.getBlur(), solution_.getThld());
			}
			else {
//				// first try the meaningful thld parameter
//				int meaningfulThld = WDiscImages.computeMeaningfulThreshold(structureProjection_);
//				WingJ.getInstance().setPreProcessingParameters(meaningfulThld);
				
				String str = "No optimal pre-processing parameters found.\n\n" +
							 "The unsupervised structure detection method can succeed even without\n" +
							 "optimal pre-processing parameters. Click on \"Run\" to figure out.\n\n" +
							 "In case the structure detection method doesn't succeed,\n" +
							 "please proceed with the step-by-step or manual detection method.";
				WJMessage.showMessage(str, "INFO");
			}
			
			WingJ.getInstance().setGuiEnabled(true, true);
			WingJ.getInstance().setSnakeVisible(false);
			
		} catch (Exception e) {		
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Duplicate the given image */
	public void setStructureProjection(ImagePlus image) throws Exception {
		
		if (image == null || image.getProcessor() == null)
			throw new Exception("INFO: Single image or image stack required.");
		
		image.saveRoi();
		image.killRoi(); // first kill the ROI or the duplicate will be cropped to the ROI
		structureProjection_ = new Duplicator().run(image);
		structureProjection_.setTitle("scanner_image");
		image.restoreRoi();
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public ImagePlus getStructureProjection() { return structureProjection_; }
	
	public void setSolution(PreProcessingTester tester) { solution_ = tester; }
	public PreProcessingTester getSolution() { return solution_; }
}
