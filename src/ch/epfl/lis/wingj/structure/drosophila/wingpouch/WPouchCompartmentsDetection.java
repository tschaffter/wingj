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

import ij.process.FloatProcessor;

import java.awt.geom.Point2D;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.tools.CompartmentSnake;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.Compartment;

import big.ij.snake2D.Snake2DKeeper;
import big.ij.snake2D.Snake2DNode;
import big.ij.snake2D.Snake2DScale;

/** 
 * Detects the four compartments DA, DP, VA, and VP using snakes.
 *
 * @see CompartmentSnake
 *
 * @version March 4, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class WPouchCompartmentsDetection extends StructureDetectionModule {
	
	/** Snake sampling rate (was in WJSettings before). */
	protected int samplingRate_ = 50;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/**
	 * Performs the segmentation four compartments using prior-shape snakes. 
	 * @param centroids Centroids of the four compartments
	 * @param dilatedSkeleton Dilated skeleton as a binary image
	 * @param mip MIP image
	 */
	private void detectCompartments(Point2D.Double[] centroids, FloatProcessor dilatedSkeleton, FloatProcessor mip) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		
		// snake parameters
		double lambda = settings.getSnakeLambda();
		double alpha = settings.getSnakeAlpha();
		double std = settings.getSnakeBlur();
		double rad = settings.getSnakeRadius();
		int numNodes = settings.getSnakeNumNodes();
		
		// run four snakes in series
		WPouchStructureSnake snake = new WPouchStructureSnake(detector.structureProjection_.getWidth(), detector.structureProjection_.getHeight());
		detector_.setTmpStructureSnake(snake);
		
		detector.shapeSnake_ = new CompartmentSnake[4];
		for (int i = 0; i < 4; i++) {
			detector.shapeSnake_[i] = new CompartmentSnake(dilatedSkeleton, mip, numNodes, samplingRate_, std, lambda, alpha, centroids[i], rad);
			// backing up the initial nodes of the snakes
			Snake2DNode[] snakeInitialNodes = detector.shapeSnake_[i].getNodes();
			Snake2DNode[] snakeBackupNodes = new Snake2DNode[snakeInitialNodes.length];
			for(int snakeNodeCounter = 0; snakeNodeCounter < snakeInitialNodes.length; snakeNodeCounter++){
				snakeBackupNodes[snakeNodeCounter] = new Snake2DNode(snakeInitialNodes[snakeNodeCounter].x, snakeInitialNodes[snakeNodeCounter].y, snakeInitialNodes[snakeNodeCounter].frozen, snakeInitialNodes[snakeNodeCounter].hidden);
			}
			
			Snake2DKeeper keeper = new Snake2DKeeper();
			if (detector.isInteractive() && !hidden_) { // do not show the image in hidden mode
				detector.structureProjection_.show();
				keeper.optimize(detector.shapeSnake_[i], detector.structureProjection_);
				boolean done = false;
				while (!done) {
					keeper.interactAndOptimize(detector.shapeSnake_[i], detector.structureProjection_);
					
					// if the user refuses to validate, restore the initial configuration
					if (detector.shapeSnake_[i].isCanceledByUser()) {
						detector.shapeSnake_[i].setNodes(snakeBackupNodes);
						WJSettings.log("Snake cancelled. Restoring initialization.");
					} else{
						done = true;
					}
				}
			} else {
				keeper.optimize(detector.shapeSnake_[i], null);
			}
			
			Snake2DScale[] skin = detector.shapeSnake_[i].getScales();
			snake.setInitialCompartment(i, new Compartment("" + i, skin[1]));
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchCompartmentsDetection(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Detecting the four compartments.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchCompartmentsDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Detecting the four compartments.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Detects the compartments using segmentation/snakes. */
	@Override
	public void run() throws Exception {
		
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;

		Point2D.Double[] centroids = detector.kiteSnake_.getTriangleCentroids();
		detectCompartments(centroids, detector.dilatedSkeletonFp_, (FloatProcessor)detector.getStructureProjection().getProcessor().convertToFloat());
	}
}
