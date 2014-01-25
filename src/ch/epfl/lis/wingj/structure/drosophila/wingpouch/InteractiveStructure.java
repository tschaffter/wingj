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

import ij.gui.Roi;

import java.awt.Color;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;

import big.ij.snake2D.Snake2DKeeper;

/** 
 * Allows the user to interact with the detected wing pouch.
 * 
 * @version November 23, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class InteractiveStructure extends StructureDetectionModule {
	
	/** Flag to say if we are still in the detection pipeline or if we are in edition mode. */
//	private boolean pureEditionMode_ = false;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public InteractiveStructure(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Interactive Drosophila wing pouch structure model.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public InteractiveStructure(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Interactive Drosophila wing pouch structure model.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Displays the detected wing pouch and allows the user to edit it before validation. */
	@Override
	public void run() throws Exception {

		WJSettings settings = WJSettings.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		WPouchStructure structure = (WPouchStructure)detector.getStructure();
//		WPouchStructureSnake snake = (WPouchStructureSnake)structure.getStructureSnake();
		WPouchStructureSnake snake = (WPouchStructureSnake)detector_.getTmpStructureSnake();
		
		snake.setInitialwPouchCenter(structure.center_);
		snake.setInitialwDiscCenter(structure.discCenter_);
		snake.setNumControlPointsPerSegment(settings.getNumStructureControlPoints());
		snake.initialize(null);
		
		// scale up the outer boundary to compensate the gap error
		// amplitude of the expansion is given in px
		// the inner boundaries may be more accurate if this operation is done
		// before detecting them
//		snake.expand(settings.getOuterBoundaryExpansion());
		
		Roi.setColor(Color.YELLOW);
		Snake2DKeeper wingKeeper = new Snake2DKeeper();
		
		if (detector.structureProjection_ == null)
			throw new Exception("ERROR: Structure projection required. Did you close it?");
		
		if (!hidden_) {	
			detector.structureProjection_.show();
			detector.structureProjection_.setColor(Color.YELLOW);
			// hold on the next instruction until the user accept or reject the structure
				
			boolean done = false;
			while (!done) {
				wingKeeper.interact(snake, detector.structureProjection_);
				
				// IF THE USER REFUSES TO VALIDATE
				if (snake.isCanceledByUser()) {
					
//        			throw new Exception("INFO: Structure edition canceled.");
					
//					if (!pureEditionMode_) {
//	        			throw new Exception("INFO: Structure detection canceled.\n" +
//								"\n" +
//							    "Click on \"Erase\" and \"Run Detection\" to restart.\n" +
//						        "Click on \"Erase\" and \"Step\" to restart step-by-step.");
//					}
				} else {
					// apply the changes only if the structure has been validated
					structure.center_ = snake.getWPouchCenter();
					structure.discCenter_ = snake.getWDiscCenter();
					structure.setStructureSnake(snake);
					// delete reference to tmp snake
					detector_.setTmpStructureSnake(null);
					done = true;
				}
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
   	/** Sets the visibility of all images generated during the detection process, if they exist. */
	@Override
	public void setImagesVisible(boolean visible) {
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		if (visible) manager.show("detection_structure_projection");
		else manager.hide("detection_structure_projection");
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
//	public void setPureEditionMode(boolean b) { pureEditionMode_ = b; }
//	public boolean getPureEditionMode() { return pureEditionMode_; }
}