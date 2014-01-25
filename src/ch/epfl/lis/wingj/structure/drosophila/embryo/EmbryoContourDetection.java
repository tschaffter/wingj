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

import java.awt.geom.Point2D;

import big.ij.snake2D.Snake2DKeeper;
import big.ij.snake2D.Snake2DNode;

import ij.process.AutoThresholder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ch.epfl.lis.wingj.WJImagesMask;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.tools.EmbryoSnake;
import ch.epfl.lis.wingj.utilities.Filters;

/** 
 * Identifies the contour of the <i>Drosophila</i> embryo using a snake algorithm.
 * 
 * @version March 6, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class EmbryoContourDetection extends StructureDetectionModule {
	
//	/**
//	 * Standard deviation of the Gaussian kernel used to smooth the image before
//	 * using the snake. Its value should be strictly larger than zero.
//	 */
//	private double stdSnakeSmoothing = 10;
//	/**
//	 * Number of control points of the snake. Its value should be larger or equal
//	 * than 4.
//	 */
//	private int snakeNumNodes = 6;
//	/**
//	 * Trade-off parameter that weights the contribution of the shape-prior energy
//	 * and the image energy. Its value should be larger of equal than zero, and 
//	 * strictly smaller than 1.
//	 */
//	private double snakeLambda = 0;
//	/**
//	 * Trade-off parameter that weights the contribution of the thresholded image
//	 * and the original image. Its value should be within the range [0,1].
//	 */
//	private double snakeAlpha = 0.5;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public EmbryoContourDetection() {};
	
	// ----------------------------------------------------------------------------
	
	/** Constructor with module name and the reference to a detector and visibility status. */
	public EmbryoContourDetection(String name, EmbryoStructureDetector detector, boolean hidden) {
		
		super(name, detector);
		description_ = "Detecting the contour of the embryo.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor with module name and the reference to a detector. */
	public EmbryoContourDetection(String name, EmbryoStructureDetector detector) {
		
		super(name, detector);
		description_ = "Detecting the contour of the embryo.";
	}
	
	// ----------------------------------------------------------------------------

	/** Detects the contour of the embryo. */
	@Override
	public void run() throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		double stdSnakeSmoothing = settings.getDrosophilaEmbryoStdSnakeSmoothing();
		int snakeNumNodes = settings.getDrosophilaEmbryoSnakeNumNodes();
		double snakeLambda = settings.getDrosophilaEmbryoSnakeLambda();
		double snakeAlpha = settings.getDrosophilaEmbryoSnakeAlpha();
		
		EmbryoStructureDetector detector = (EmbryoStructureDetector)detector_;
		EmbryoStructureSnake snake = new EmbryoStructureSnake(detector.structureProjection_.getWidth(), detector.structureProjection_.getHeight());
		EmbryoStructure structure = (EmbryoStructure)detector.getStructure();

		WJImagesMask.applyMask(detector.structureProjection_);

		FloatProcessor thresholdedImage = (FloatProcessor)detector.structureProjection_.getProcessor().duplicate().convertToFloat();
		thresholdedImage.resetMinAndMax();
		thresholdedImage.setAutoThreshold(AutoThresholder.Method.Minimum, true, ImageProcessor.NO_LUT_UPDATE);
		int threshold = (int) thresholdedImage.getMinThreshold();
		Filters.binaryThresholdedImageFilter(thresholdedImage, threshold);
		
		float[] thresholdedPixels = (float[]) thresholdedImage.getPixels();
		// compute centroid
		double M00 = 0;
		double M10 = 0;
		double M01 = 0;
		for(int x = 0; x < detector.structureProjection_.getWidth(); x++){
			for(int y = 0; y < detector.structureProjection_.getHeight(); y++){
				if(thresholdedPixels[x+detector.structureProjection_.getWidth()*y]!=0){
					M00 += 1;
					M10 += x;
					M01 += y;
					thresholdedPixels[x+detector.structureProjection_.getWidth()*y] = 255;
				}
			}
		}
		Point2D.Double center = new Point2D.Double(M10/M00, M01/M00);
		
		
		Filters.applyGaussianFilter(thresholdedImage, stdSnakeSmoothing);

		double rad = Math.sqrt(M00/Math.PI);
		EmbryoSnake embryoSnake = new EmbryoSnake(thresholdedImage, (FloatProcessor)detector.structureProjection_.getProcessor(), snakeNumNodes, 200, stdSnakeSmoothing, snakeLambda, snakeAlpha, center, rad);
		
		// backing up the nodes
		Snake2DNode[] snakeInitialNodes = embryoSnake.getNodes();
		Snake2DNode[] snakeBackupNodes = new Snake2DNode[snakeInitialNodes.length];
		for(int snakeNodeCounter = 0; snakeNodeCounter < snakeInitialNodes.length; snakeNodeCounter++){
			snakeBackupNodes[snakeNodeCounter] = new Snake2DNode(snakeInitialNodes[snakeNodeCounter].x, snakeInitialNodes[snakeNodeCounter].y, snakeInitialNodes[snakeNodeCounter].frozen, snakeInitialNodes[snakeNodeCounter].hidden);
		}
		
		Snake2DKeeper keeper = new Snake2DKeeper();
		if(detector.isInteractive() && !hidden_){ // do not show the image in hidden mode
			detector.structureProjection_.show();
			keeper.optimize(embryoSnake, detector.structureProjection_);
			boolean done = false;
			while (!done) {
				keeper.interactAndOptimize(embryoSnake, detector.structureProjection_);
				
				// if the user refuses to validate, restore the initial configuration
				if (embryoSnake.isCanceledByUser()) {
					embryoSnake.setNodes(snakeBackupNodes);
					WJSettings.log("Snake cancelled. Restoring initialization.");
				} else {
					done = true;
				}
			}
		} else {
			keeper.optimize(embryoSnake, null);
		}
		
		Point2D.Double snakeCentroid = embryoSnake.getCentroid();
		structure.setDiscCenter(center);
		structure.setCenter(snakeCentroid);
		
		Compartment embryoBoundary = new Compartment("", embryoSnake.getBoundary());
		snake.setInitialContour(embryoBoundary);

		detector.setEmbryoSnake(embryoSnake);
		
		// save the reference of snake to tmpSnake_
		detector_.setTmpStructureSnake(snake);
	}
}