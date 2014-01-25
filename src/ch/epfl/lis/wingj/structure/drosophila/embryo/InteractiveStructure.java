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

package ch.epfl.lis.wingj.structure.drosophila.embryo;

import ij.gui.Roi;

import java.awt.Color;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;

import big.ij.snake2D.Snake2DKeeper;

/** 
 * Allows the user to interact with the identified structure model of the embryo before validating it.
 * 
 * @version April 19, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class InteractiveStructure extends StructureDetectionModule {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public InteractiveStructure(String name, EmbryoStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Interactive Drosophila embryo structure model.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public InteractiveStructure(String name, EmbryoStructureDetector detector) {
		
		super(name, detector);
		description_ = "Interactive Drosophila embryo structure model.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Displays the structure model identified for the embryo. */
	@Override
	public void run() throws Exception {
		
		this.getDetector();

		WJSettings settings = WJSettings.getInstance();
		EmbryoStructureDetector detector = (EmbryoStructureDetector)detector_;
		EmbryoStructure structure = (EmbryoStructure)detector.getStructure();
		EmbryoStructureSnake snake = (EmbryoStructureSnake)detector.getTmpStructureSnake();
		
		snake.setInitialwPouchCenter(structure.getCenter());
		snake.setInitialwDiscCenter(structure.getDiscCenter());
		snake.setNumControlPointsPerSegment(settings.getNumStructureControlPoints());
		snake.initialize(null);
		Roi.setColor(Color.YELLOW);
		Snake2DKeeper snakeKeeper = new Snake2DKeeper();
		
		if (detector.structureProjection_ == null)
			throw new Exception("ERROR: Structure projection required. Did you close it?");
		
		boolean done = false;
		if (!hidden_) {
			while (!done) {
			
				detector.structureProjection_.show();
				detector.structureProjection_.setColor(Color.YELLOW);
				// hold on the next instruction until the user accept or reject the structure
				snake.hideWingCenter();
				snakeKeeper.interact(snake, detector.structureProjection_);
				
				// IF THE USER REFUSES TO VALIDATE
				if (snake.isCanceledByUser()) {

				} else {
					// check that the free vertex is one of the compartment
					structure.setCenter(snake.getWPouchCenter());
					structure.setDiscCenter(snake.getWDiscCenter());
					
					// we don't care about the AD point anymore
					structure.setStructureSnake(snake);
					detector_.setTmpStructureSnake(null);
					done = true;

//					Compartment embryoContour = snake.getWPouchContour();
//					if (!embryoContour.contains((float)snake.getWDiscCenter().x, (float)snake.getWDiscCenter().y)) {
//						String msg = "Please place the free control point in\nthe dorsal-anterior compartment.";
//						WJMessage.showMessage(msg, "INFO");
//					} else {
//						// apply the changes only if the structure has been validated
//						structure.setStructureSnake(snake);
//						// delete reference to tmp snake
//						detector_.setTmpStructureSnake(null);
//						done = true;
//					}
				}
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
   	/** Sets the visibility of the images created by this detection module (if any). */
	@Override
	public void setImagesVisible(boolean visible) {
		
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		if (visible) manager.show("detection_structure_projection");
		else manager.hide("detection_structure_projection");
	}
}