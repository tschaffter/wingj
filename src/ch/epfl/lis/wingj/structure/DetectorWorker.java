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

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WingJ;

import javax.swing.SwingWorker;

/**
 * Worker thread for running structure detectors.
 * 
 * @version June 2, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class DetectorWorker extends SwingWorker<Void, Void> {
	
	public static final int RUNALL  = 1;
	public static final int STEP = 2;
	public static final int REDO = 3;
	public static final int RESUME = 4;
	public static final int MANUAL = 5;
	
	/** Mode */
	private int mode_ = 0;
	
	/** Structure detector */
	private StructureDetector detector_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public DetectorWorker(StructureDetector detector) {
		
		detector_ = detector;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Run the structure detection in a worker thread. */
	@Override
	protected Void doInBackground() throws Exception {
		
		WingJ.getInstance().setGuiEnabled(false, true);
		WingJ.getInstance().setWaitingSnakeVisible(true);
		
		switch (mode_) {
			case RUNALL:
				detector_.isInteractive(false);
				detector_.runAll();
				break;
			case STEP:
				detector_.isInteractive(true);
				detector_.step();
				break;
			case REDO:
				detector_.isInteractive(true);
				detector_.redo();
				break;
			case RESUME:
				detector_.isInteractive(false);
				detector_.resume();
				break;
			case MANUAL:
				detector_.isInteractive(true);
				detector_.runManualDetection();
				break;
			default:
				throw new Exception("ERROR: Invalid mode for structure detection.");
		}
		
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** This method is called at the end of the thread. */
    @Override
    protected void done() {
		
    	try {
			get();
		} catch (Exception e) {			
			detector_.setError(true);
			WJMessage.showMessage(e);
		}
		WingJ.getInstance().setWaitingSnakeVisible(false);
		WingJ.getInstance().setGuiEnabled(true, true);
	}
    
	// ============================================================================
	// SETTERS AND GETTERS
    
    public void setMode(int mode) { mode_ = mode; }
    public int getMode() { return mode_; }
}
