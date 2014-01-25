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

import ij.ImagePlus;
import ij.plugin.Duplicator;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.tools.KiteSnake;
import ch.epfl.lis.wingj.structure.tools.PlusShapeCenterDetector;
import ch.epfl.lis.wingj.structure.tools.Skeleton;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.utilities.Filters;

import big.ij.snake2D.Snake2DKeeper;
import big.ij.snake2D.Snake2DNode;

/** 
 * Identifies the center of the wing pouch from the skeleton of the wing pouch structure
 * <p>
 * <ul>
 * <li>Compute initial wing pouch center from blurred skeleton</li>
 * <li>Compute refined wing pouch center form skeleton and initial center</li>
 * </ul>
 * 
 * @version March 3, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class WPouchCenterDetection extends StructureDetectionModule {
	
	/**
	 * Detects all the modes with amplitude > MODES_RELATIVE_MIN_AMPLITUDE * max of the first mode
	 * Use in the detection of the candidate wing pouch center from projection of the dilated skeleton
	 * on x- and y-axis of the image.
	 */
	private static final double MODES_RELATIVE_MIN_AMPLITUDE = 0.75;
	
	/**
	 * Dilation of the skeleton.
	 * IMPORTANT: only used for test in StructureCenterTest 
	 */
	public static double TEST_DILATATION = 1;
	/**
	 * Initial length in pixel of the KiteSnake branches.
	 * IMPORTANT: only used for test in KiteSnakeTest
	 */
	public static double TEST_KITESNAKE_INITIAL_BRANCH_LENGTH = 100;
	/**
	 * Fixed width of the KiteSnake branches.
	 * IMPORTANT: only used for test in KiteSnakeTest
	 */
	public static double TEST_KITESNAKE_FIXED_WIDTH = 2;
	/**
	 * Ration between cross and background areas of the KiteSnake. Determines the dynamic branch widths.
	 * IMPORTANT: only used for test in KiteSnakeTest
	 */
	public static double TEST_KITESNAKE_DYNAMIC_WIDTH_RATIO = 0.5;

	/** Maximum number of iterations the KiteSnake can run */
	private static final int KITESNAKE_MAX_NUM_ITERATIONS = 5000;
	
	/** Allows to disable the KiteSnake, for instance when testing the performance of the structure center optimization. */
	private boolean kiteSnakeDisabled_ = false;
	
	// ============================================================================
	// PRIVATE METHODS

	/** Returns the index of the largest value in the given array using the given boundaries. */
	private static int findMaxIndex(int[] A, int start, int end) {
		
		int index = 0;
		int max = 0;
		for (int i = start; i < end; i++) {
			if (A[i] > max) {
				index = i;
				max = A[i];
			}
		}
		return index;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the index of the largest value in the given array. */
	private static int findMaxIndex(int[] A) {
		
		return findMaxIndex(A, 0, A.length);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Looks at the given projection and return the coordinates which can be part of the effective structure center. */
	private static List<Integer> computePotentialCenterCoordinates(int[] projection, double truncCoeff, int modeRange) throws Exception {
		
		if (projection == null)
			throw new Exception("ERROR: projection is null.");
		
		int maxIndex = findMaxIndex(projection);
		int max = projection[maxIndex];
		int limit = (int) (truncCoeff * max);
		
		// truncate projection using limit
		for (int i = 0; i < projection.length; i++) {
			if (projection[i] < limit)
				projection[i] = 0;
		}

		// look for each mode max
		List<Integer> coordinates = new ArrayList<Integer>();
		while (true) {
			// find the coordinate of the max
			maxIndex = findMaxIndex(projection);
			if (projection[maxIndex] < limit)
				break;
			
			coordinates.add(maxIndex);
			
			// remove this mode from the projection data
			int start = Math.max(0, maxIndex - (int) (modeRange/2.));
			int end = Math.min(projection.length, maxIndex + (int) (modeRange/2.));
			for (int i = start; i < end; i++)
				projection[i] = 0;
		}
		return coordinates;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a list of potential center points based on the projection of the dilated skeleton. */
	private static List<Point> computeCenterCandidatesFromSkeletonProjections(ByteProcessor bp) throws Exception {
		
		int width = bp.getWidth();
		int height = bp.getHeight();
		
		int[] projectionX = new int[width];
		int[] projectionY = new int[height];
		
		// compute projections
		byte[] bpPixels = (byte[])bp.getPixels();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ((bpPixels[x+y*width]&0xFF) != 0) {
					projectionX[x]++;
					projectionY[y]++;
				}
			}
		}
		
//		// save histograms to file
//		FileWriter fstream = new FileWriter("/home/tschaffter/Documents/WingJ_experiments/20111108_StructureCenterTest_projection_only/data/uv.txt");
//		BufferedWriter out = new BufferedWriter(fstream);
//		String content = "";
//		// suppose that u and v have the same dimension
//		for (int i = 0; i < projectionX.length; i++)
//			content += Integer.toString(projectionX[i]) + "\t" + Integer.toString(projectionY[i]) + "\n";
//		out.write(content);
//		out.close();

		WJSettings settings = WJSettings.getInstance();
		
//		int modeRange = (int) (6 * settings.getPpBlur());
		int modeRange = (int) (3 * settings.getExpectedBoundariesThicknessInPixels());
		List<Integer> xCandidates = computePotentialCenterCoordinates(projectionX, MODES_RELATIVE_MIN_AMPLITUDE, modeRange);
		List<Integer> yCandidates = computePotentialCenterCoordinates(projectionY, MODES_RELATIVE_MIN_AMPLITUDE, modeRange);
		
		// build all possible point combinations
		List<Point> centerCandidates = new ArrayList<Point>();
		for (int i = 0; i < xCandidates.size(); i++) {
			for (int j = 0; j < yCandidates.size(); j++)
				centerCandidates.add(new Point(xCandidates.get(i), yCandidates.get(j)));	
		}
		
		return centerCandidates;
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchCenterDetection(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Detecting the intersection of the A/P and D/V boundary.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchCenterDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Detecting the intersection of the A/P and D/V boundary.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Identifies the center of the wing pouch and run the kite snake. */
	@Override
	public void run() throws Exception {

		WJSettings settings = WJSettings.getInstance();
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		
		// compute center candidates from X and Y projections of the dilated skeleton
		double dilationFactor = settings.getExpectedBoundariesThicknessInPixels(); // DILATION
		Skeleton skeleton = new Skeleton();
		detector.dilatedSkeletonFp_ = skeleton.skeletonize((FloatProcessor) detector.ppImage_.getProcessor(), true, dilationFactor);
		List<Point> centerCandidates = WPouchCenterDetection.computeCenterCandidatesFromSkeletonProjections((ByteProcessor) detector.dilatedSkeletonFp_.convertToByte(false));

		// TODO Thomas: to delete
//		ImagePlus dilatedSkeleton = new ImagePlus("dilated_skeleton", detector.dilatedSkeletonFp_);
//		new ImageConverter(dilatedSkeleton).convertToGray8();
//		IJ.save(dilatedSkeleton, "/home/tschaffter/Documents/WingJ_experiments/20111108_StructureCenterTest_projection_only/data/dilated_skeleton.tif");
		
		// optimize each center candidates
		ImagePlus optimizerImage = new Duplicator().run(detector.structureProjection_);
		
   		Filters.applyGaussianFilter(optimizerImage.getProcessor(), PreProcessing.getPpBlurSigma()/2.);
   		Point2D.Double bestCenterCandidate = null;
   		double bestScore = 0.;
   		for (int i = 0; i < centerCandidates.size(); i++) {
   	   		PlusShapeCenterDetector optimizer = new PlusShapeCenterDetector();
   			optimizer.setImage(new Duplicator().run(optimizerImage));
   			// set the geometry of the center optimizer
   			// 1. dimension of the entire optimizer
   			// set it as a square whose sides are 3-4 times the expected fitness of the boundaries
   			int expectedBoundariesThickness = (int)settings.getExpectedBoundariesThicknessInPixels();
   			Dimension optimizerDim = new Dimension(3*expectedBoundariesThickness, 3*expectedBoundariesThickness);
   			// 2. dimension of the four background squares
   			Dimension backgroundDim = new Dimension(expectedBoundariesThickness, expectedBoundariesThickness);
   			// 3. scaling coefficient (default: 1)
   			double scaleCoeff = settings.getCenterOptimizerScale();

//   			int size = (int) (2*settings.getKiteSnakeBranchWidth()) + 2*WPouchCenterDetectionOptimizer.CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY.width;
//   			optimizer.initialize(new Point2D.Double(centerCandidates.get(i).x, centerCandidates.get(i).y), new Dimension(size, size), WPouchCenterDetectionOptimizer.CENTER_OPTIMIZER_DEFAULT_BACKGROUND_GEOMETRY, 1);
   			optimizer.initialize(new Point2D.Double(centerCandidates.get(i).x, centerCandidates.get(i).y), null, optimizerDim, backgroundDim, scaleCoeff);
   			optimizer.optimize();
   			
   			if (optimizer.getScore() > bestScore) {
   				bestScore = optimizer.getScore();
   				bestCenterCandidate = optimizer.getCorrectedCenter();
   				 // optimizer.drawRois("/home/tschaffter/CenterTest.tif");
   			}
   			optimizer.clean();
   		}
		
   		// final wing pouch center (could be null)
   		WPouchStructure structure = (WPouchStructure)detector.getStructure();
   		structure.center_ = (Point2D.Double) bestCenterCandidate.clone();
   		WJSettings.log("Wing pouch center: " + structure.center_);
   		
   		// must be true only during tests (see package "test")
   		if (kiteSnakeDisabled_)
   			return;
   		
   		// run KiteSnake
   		Snake2DKeeper keeper = new Snake2DKeeper();
   		if (structure.center_ != null) {
   			detector.kiteSnake_ = new KiteSnake();
   			detector.kiteSnake_.setImage((FloatProcessor)detector.structureProjection_.getProcessor(), PreProcessing.getPpBlurSigma());
   			detector.kiteSnake_.setInitialKiteCenter(structure.center_);
   			double kiteSnakeBranchesWidth = settings.getKiteSnakeBranchWidth();
   			if (kiteSnakeBranchesWidth < 0)
   				kiteSnakeBranchesWidth = settings.getExpectedBoundariesThicknessInPixels();
   			detector.kiteSnake_.setGeometry(settings.getKiteSnakeBranchLength(), kiteSnakeBranchesWidth);
   	   		detector.kiteSnake_.setMaxNumIters(WPouchCenterDetection.KITESNAKE_MAX_NUM_ITERATIONS);
   	   		detector.kiteSnake_.build();

   	   		// backing up the initial configuration of the KiteSnake
			Snake2DNode[] snakeInitialNodes = detector.kiteSnake_.getNodes();
			Snake2DNode[] snakeBackupNodes = new Snake2DNode[snakeInitialNodes.length];
			for(int snakeNodeCounter = 0; snakeNodeCounter < snakeInitialNodes.length; snakeNodeCounter++){
				snakeBackupNodes[snakeNodeCounter] = new Snake2DNode(snakeInitialNodes[snakeNodeCounter].x, snakeInitialNodes[snakeNodeCounter].y, snakeInitialNodes[snakeNodeCounter].frozen, snakeInitialNodes[snakeNodeCounter].hidden);
			}
   			if (detector.isInteractive() && !hidden_) {
   				detector.structureProjection_.hide();
   				detector.structureProjection_.show();
   				detector.kiteSnake_.freezeKiteCenter(true);
   				detector.kiteSnake_.setImmortal(false);
   				keeper.optimize(detector.kiteSnake_, detector.structureProjection_);
   				detector.kiteSnake_.freezeKiteCenter(false);
   				detector.kiteSnake_.reviveSnake();
   				detector.kiteSnake_.setImmortal(true);
   				
   				boolean done = false;
   				while (!done) {
   					keeper.interactAndOptimize(detector.kiteSnake_, detector.structureProjection_);
   					// IF THE USER REFUSES TO VALIDATE
   					if (detector.kiteSnake_.isCanceledByUser()) {
   						detector.kiteSnake_.setNodes(snakeBackupNodes);
   						WJSettings.log("Snake cancelled. Restoring initialization.");
   					} else{
   						done = true;
   					}
   				}
   			} else{
   				detector.kiteSnake_.freezeKiteCenter(true);
   				detector.kiteSnake_.setImmortal(false);
   				keeper.optimize(detector.kiteSnake_, null);
   				detector.kiteSnake_.freezeKiteCenter(false);
   			}
   		} else {
   			structure.center_ = new Point2D.Double(detector.dilatedSkeletonFp_.getWidth()/2.0, detector.dilatedSkeletonFp_.getHeight()/2.0);
   			detector.kiteSnake_ = new KiteSnake();
   			detector.kiteSnake_.setImage((FloatProcessor)detector.structureProjection_.getProcessor(), PreProcessing.getPpBlurSigma());
   			detector.kiteSnake_.setInitialKiteCenter(structure.center_);
   			detector.kiteSnake_.setGeometry(settings.getKiteSnakeBranchLength(), settings.getKiteSnakeBranchWidth());
   	   		detector.kiteSnake_.setMaxNumIters(WPouchCenterDetection.KITESNAKE_MAX_NUM_ITERATIONS);
   	   		detector.kiteSnake_.build();
   			
   			detector.structureProjection_.hide();
			detector.structureProjection_.show();
			detector.kiteSnake_.setImmortal(true);
			
			boolean done = false;
			Snake2DNode[] snakeInitialNodes = detector.kiteSnake_.getNodes();
			Snake2DNode[] snakeBackupNodes = new Snake2DNode[snakeInitialNodes.length];
			for(int snakeNodeCounter = 0; snakeNodeCounter < snakeInitialNodes.length; snakeNodeCounter++){
				snakeBackupNodes[snakeNodeCounter] = new Snake2DNode(snakeInitialNodes[snakeNodeCounter].x, snakeInitialNodes[snakeNodeCounter].y, snakeInitialNodes[snakeNodeCounter].frozen, snakeInitialNodes[snakeNodeCounter].hidden);
			}
			while (!done) {
				keeper.interactAndOptimize(detector.kiteSnake_, detector.structureProjection_);
				
				// IF THE USER REFUSES TO VALIDATE
				if (detector.kiteSnake_.isCanceledByUser()) {
					detector.kiteSnake_.setNodes(snakeBackupNodes);
					WJSettings.log("Snake cancelled. Restoring initialization.");
				} else{
					done = true;
				}
			}
   		}
   		structure.center_ = detector.kiteSnake_.getKiteCenter();
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void isKiteSnakeDisabled(boolean b) { kiteSnakeDisabled_ = b; }
	public boolean isKiteSnakeDisabled() { return kiteSnakeDisabled_; }
}