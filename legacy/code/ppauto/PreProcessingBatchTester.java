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

import ij.ImagePlus;

import javax.swing.SwingWorker;

import core.WJMessage;

/** 
 * Tests several values of thld one after the other for one pre-defined blur value.
 * 
 * @version September 13, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class PreProcessingBatchTester extends SwingWorker<Void, Void> {

	/** Reference to the scanner */
	private PreProcessingScanner scanner_ = null;
	
	/** Structure projection */
	private ImagePlus structureProjection_ = null;
	/** Thld parameter */
	private int thld_ = 0;
	
	/** Incremental step for blur */
	private double blurStep_ = 0.;
	/** Min value for blur */
	private double minBlur_ = 0.;
	/** Max value for blur */
	private double maxBlur_ = 0.;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public PreProcessingBatchTester(PreProcessingScanner scanner, int thld, double blurStep, double minBlur, double maxBlur) {
		
		scanner_ = scanner;
		structureProjection_ = scanner.getStructureProjection();

		thld_ = thld;
		blurStep_ = blurStep;
		minBlur_ = minBlur;
		maxBlur_ = maxBlur;
	}
	
	// ----------------------------------------------------------------------------

	/** Test one by one the different blur value */
	@Override
	protected Void doInBackground() throws Exception {

		for (double blur = minBlur_; blur <= maxBlur_; blur += blurStep_) {
			
			// if a valid solution has already been found, exit
			if (scanner_.getSolution() != null)
				break;
			
			PreProcessingTester tester = new PreProcessingTester(structureProjection_, Math.min(blur, maxBlur_), thld_);
			
			// if the result is positive and no solution has been found so far, save solution and exit
			if (tester.test() && scanner_.getSolution() == null) {
				scanner_.setSolution(tester);
				break;
			}	
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------

	/** This function is called once the thread is done */
    @Override
    protected void done() {
		
    	try {
			get();
			
		} catch (Exception e) {		
			WJMessage.showMessage(e);
		}
	}
}
