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

import ij.gui.ShapeRoi;
import ij.process.FloatPolygon;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import big.ij.snake2D.Snake2DNode;
import big.ij.snake2D.Snake2DScale;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.StructureSnake;
import ch.epfl.lis.wingj.structure.geometry.Segment;
import ch.epfl.lis.wingj.utilities.Filters;
import ch.epfl.lis.wingj.utilities.MathUtils;

/** 
 * Describes a structure as a snake/B-splines.
 * 
 * @version November 28, 2011
 *
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WPouchStructureSnake extends StructureSnake {
	
	/** Spline coefficients that determine the outer contour of the snake. */
	protected Snake2DNode[] outerNodes_ = null;
	/** Control points that determine the interior boundaries. */
	protected Snake2DNode[][] innerNodes_ = null;
	
	/** Control point that represents the center of the wing pouch. */
	private Snake2DNode wPouchCenterNode_ = null;
	/** Control point that represents the center of the wing disc. */
	private Snake2DNode wDiscCenterNode_ = null;
	
	/** LUT with the samples of the x coordinates of the outer snake contour. */
	protected double[] xOuterPosSkin_ = null;
	/** LUT with the samples of the y coordinates of the outer snake contour. */
	protected double[] yOuterPosSkin_ = null;
	/** LUT with the samples of the x coordinates of the inner snake boundary. */
	protected double[][] xInnerPosSkin_ = null;
	/** LUT with the samples of the y coordinates of the inner snake boundary. */
	protected double[][] yInnerPosSkin_ = null;
	
	/** Total number of control points for the outer contour. */
	private int M_ = 0;
	/** Product of M and R. */
	protected int MR_ = 0;
	/** Pi/M. */
	protected double PIM_ = 0;
	/** 2*Pi/M. */
	protected double PI2M_ = 0;
	
	/** Array with the output of the shape snakes. */
	protected Compartment[] initialCompartments_ = null;
	/** Array with the output of the line-following trackers. */
	protected Boundary[] initialBoundaries_ = null;
	/** Initial wing pouch internal compartment needed when the automatic fitting is performed. */
	protected Compartment initialWingPouchBoundary_ = null;
	/** Initial wing pouch center needed when the automatic fitting is performed. */
	protected Point2D.Double initialwPouchCenter_ = null;
	/** Initial wing disc center needed when the automatic fitting is performed. */
	protected Point2D.Double initialwDiscCenter_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public WPouchStructureSnake() {
		
		super();
	}

	// ----------------------------------------------------------------------------

	/** Constructor of the spline snake. */
	public WPouchStructureSnake(int imageWidth, int imageHeight){

		super(imageWidth,imageHeight);
		initialCompartments_ = new Compartment[4];
		initialBoundaries_ = new Boundary[4];
	}

	// ----------------------------------------------------------------------------

	/**
	 * Copy constructor.
	 * <p>
	 * Takes as input a StructureSnake to allow this class to be extended. 
	 */
	public WPouchStructureSnake(StructureSnake s) {

		super(s);

		WPouchStructureSnake snake = (WPouchStructureSnake)s;

		if (snake.outerNodes_ != null) outerNodes_ = deepCopySnake2DNodeArray(snake.outerNodes_); // ok
		if (snake.innerNodes_ != null) innerNodes_ = deepCopySnake2DNodeArrayOfArray(snake.innerNodes_); // ok
		if (snake.wPouchCenterNode_ != null) wPouchCenterNode_ = (Snake2DNode) snake.wPouchCenterNode_.clone(); // ok (hope that Snake2DNode.clone() is overridden)
		if (snake.wDiscCenterNode_ != null) wDiscCenterNode_ = (Snake2DNode) snake.wDiscCenterNode_.clone(); // ok
		
		if (snake.xOuterPosSkin_ != null) xOuterPosSkin_ = snake.xOuterPosSkin_.clone(); // ok
		if (snake.yOuterPosSkin_ != null) yOuterPosSkin_ = snake.yOuterPosSkin_.clone(); // ok
		if (snake.xInnerPosSkin_ != null) xInnerPosSkin_ = MathUtils.deepCopyDoubleArrayOfArray(snake.xInnerPosSkin_); // ok		
		if (snake.yInnerPosSkin_ != null) yInnerPosSkin_ = MathUtils.deepCopyDoubleArrayOfArray(snake.yInnerPosSkin_); // ok

		M_ = snake.M_;
		MR_ = snake.MR_;
		PIM_ = snake.PIM_;
		PI2M_ = snake.PI2M_;
		
		if (snake.initialCompartments_ != null) initialCompartments_ = Compartment.deepCopyCompartmentArray(snake.initialCompartments_); // ok
		if (snake.initialBoundaries_ != null) initialBoundaries_ = Boundary.deepCopyBoundaryArray(snake.initialBoundaries_); // ok;
		if (snake.initialWingPouchBoundary_ != null) initialWingPouchBoundary_ = snake.initialWingPouchBoundary_.clone();
		if (snake.initialwPouchCenter_ != null) initialwPouchCenter_ = (Point2D.Double)snake.initialwPouchCenter_.clone();
		if (snake.initialwDiscCenter_ != null) initialwDiscCenter_ = (Point2D.Double)snake.initialwDiscCenter_.clone();
	}

	// ----------------------------------------------------------------------------

	/** Copy operator. */
	@Override
	public WPouchStructureSnake copy() {

		return new WPouchStructureSnake(this);
	}

	// ----------------------------------------------------------------------------

	/** Clone operator. */
	@Override
	public WPouchStructureSnake clone() {

		return copy();
	}
	
	// ----------------------------------------------------------------------------

	/** Returns true if the two WingSnake are identical regarding the parameters considered. */
	@Override
	public boolean match(StructureSnake s) throws Exception {

		WPouchStructureSnake snake = (WPouchStructureSnake)s;
		
		if (this.M0_ != snake.M0_) return false;

		Snake2DNode[] nodes = this.getNodes();

		for (int i = 0; i < nodes.length; i++) {
			if (Double.compare(this.allNodes_[i].x, nodes[i].x) != 0) return false;
			if (Double.compare(this.allNodes_[i].y, nodes[i].y) != 0) return false;
		}

		return true;
	}

	// ----------------------------------------------------------------------------

	/** Builds the WingSnake model if all structure information have been set. */
	@Override
	public void initialize(Snake2DNode[] newNodes) throws Exception {

		WJSettings settings = WJSettings.getInstance();

		if (!initialized_) {
			if(newNodes==null)
				checkInitialization(); // generates an Exception is something wrong
			
			R_ = (int)Math.ceil((settings.getExpression1DNumPoints()-1)/(2.0*M0_));
			M_ = 4*M0_;
			NR_ = N_*R_;
			MR_= M_*R_;
			PIM_= Math.PI/M_;
			PI2M_= 2.0*PIM_;

			xOuterPosSkin_ = new double[MR_];
			yOuterPosSkin_ = new double[MR_];

			xInnerPosSkin_ = new double[4][(M0_+1)*R_];
			yInnerPosSkin_ = new double[4][(M0_+1)*R_];

			si_ = new SplineInterpolator();

			eSplineFunc_ = new double[NR_];
			for (int i=0; i<NR_; i++){
				eSplineFunc_[i] = ESpline4((double)i/(double)R_);
			}

			allNodes_ = new Snake2DNode[8*M0_-2];
			innerNodes_ = new Snake2DNode[4][M0_-1];

			if(newNodes == null){
				// centers
				wPouchCenterNode_ = new Snake2DNode(initialwPouchCenter_.x, initialwPouchCenter_.y);
				wDiscCenterNode_ = new Snake2DNode(initialwDiscCenter_.x, initialwDiscCenter_.y);

				Compartment resampledBoundary = initialWingPouchBoundary_.createResampledCompartment();
				if(resampledBoundary.orientation() == -1) resampledBoundary.reverse();

				int[] connectionIndexes = findBranchJoints(resampledBoundary);

				int nPointsBoundary = resampledBoundary.npoints;
				float[] xPointsBoundary = resampledBoundary.xpoints;
				float[] yPointsBoundary = resampledBoundary.ypoints;

				Point2D.Double[] sampledOuterContour = new Point2D.Double[M_];

				for(int arcNum = 0; arcNum<4; arcNum++){
					int arcPoints = connectionIndexes[(arcNum+1)%4]-connectionIndexes[arcNum];
					if(arcPoints<0) arcPoints += nPointsBoundary;
					int j = 0;
					for(int i=0+arcNum*M0_; i<M0_+arcNum*M0_; i++){
						int index = ((int)Math.round(connectionIndexes[arcNum] + (double)arcPoints*(double)j/((double)M0_)))%nPointsBoundary;
						sampledOuterContour[i] = new Snake2DNode(xPointsBoundary[index],yPointsBoundary[index]);
						j++;
					}
					distributeOnPath(innerNodes_[arcNum], initialBoundaries_[arcNum], initialBoundaries_[arcNum].lengthInPx());
				}

				outerNodes_ = getSplineKnots(sampledOuterContour);

				// load all parameters in allNodes_
				int cont = 0;
				for(int i=0; i<M_; i++){
					allNodes_[cont] = outerNodes_[i];
					cont++;
				}	
				for(int arcNum=0; arcNum<4; arcNum++){
					for(int i=0; i<(M0_-1); i++){
						allNodes_[cont] = innerNodes_[arcNum][i];
						cont++;
					}	
				}

				allNodes_[cont] = wPouchCenterNode_;
				allNodes_[cont+1] = wDiscCenterNode_;

				// compute the snake contour
				computeSnakeSkin();
				
			}else{
				wPouchCenterNode_ = new Snake2DNode(0, 0);
				wDiscCenterNode_ = new Snake2DNode(0, 0);

				for(int i=0; i<M0_-1; i++){
					for(int branchCounter=0; branchCounter<4; branchCounter++){
						innerNodes_[branchCounter][i] = new Snake2DNode(0, 0);
					}
				}

				outerNodes_ = new Snake2DNode[M_];
				for (int i = 0; i < M_; i++){
					outerNodes_[i] = new Snake2DNode(0, 0);
				}

				int cont = 0;

				for(int i=0; i<M_; i++){
					allNodes_[cont] = outerNodes_[i];
					cont++;
				}	

				for(int branchNumber=0; branchNumber<4; branchNumber++){
					for(int i=0; i<(M0_-1); i++){
						allNodes_[cont] = innerNodes_[branchNumber][i];
						cont++;
					}
				}

				allNodes_[cont] = wPouchCenterNode_;
				allNodes_[cont+1] = wDiscCenterNode_;
				
				setNodes(newNodes);
			}

			if (settings.correctBoundariesIntersection())
				correctIntersection();

			initialized_ = true;	
		}
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to modify the number of control points of the snake. */
	@Override
	public WPouchStructureSnake getResample(int M0) throws Exception {

		WPouchStructureSnake ws = new WPouchStructureSnake(this);
		ws.resample(M0);
		return ws;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to determine what to draw on screen, given the current configuration of nodes. */
	@Override
	public Snake2DScale[] getScales () {

		Snake2DScale[] skin = new Snake2DScale[8];
		skin[0] = new Snake2DScale(Color.YELLOW, new Color(0, 0, 0, 0), true, false);
		skin[1] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), true, false);
		skin[2] = new Snake2DScale(Color.YELLOW, new Color(0, 0, 0, 0), false, false);
		skin[3] = new Snake2DScale(Color.YELLOW, new Color(0, 0, 0, 0), false, false);
		skin[4] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
		skin[5] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
		skin[6] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
		skin[7] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);

		for(int k=0; k<M_; k++){
			skin[0].addPoint((int)Math.round(outerNodes_[k].x),(int)Math.round(outerNodes_[k].y));
		}

		for(int k=0; k<MR_; k++){
			skin[1].addPoint((int)Math.round(xOuterPosSkin_[k]), (int)Math.round(yOuterPosSkin_[k]));
		}

		skin[2].addPoint((int)Math.round(outerNodes_[0].x), (int)Math.round(outerNodes_[0].y));
		for(int k=0; k<(M0_-1); k++){
			skin[2].addPoint((int)Math.round(innerNodes_[0][k].x), (int)Math.round(innerNodes_[0][k].y));
		}
		skin[2].addPoint((int)Math.round(wPouchCenterNode_.x), (int)Math.round(wPouchCenterNode_.y));
		for(int k=M0_-2; k>=0; k--){
			skin[2].addPoint((int)Math.round(innerNodes_[2][k].x), (int)Math.round(innerNodes_[2][k].y));
		}
		skin[2].addPoint((int)Math.round(outerNodes_[2*M0_].x), (int)Math.round(outerNodes_[2*M0_].y));

		skin[3].addPoint((int)Math.round(outerNodes_[M0_].x), (int)Math.round(outerNodes_[M0_].y));
		for(int k=0; k<(M0_-1); k++){
			skin[3].addPoint((int)Math.round(innerNodes_[1][k].x), (int)Math.round(innerNodes_[1][k].y));
		}
		skin[3].addPoint((int)Math.round(wPouchCenterNode_.x), (int)Math.round(wPouchCenterNode_.y));
		for(int k=M0_-2; k>=0; k--){
			skin[3].addPoint((int)Math.round(innerNodes_[3][k].x), (int)Math.round(innerNodes_[3][k].y));
		}
		skin[3].addPoint((int)Math.round(outerNodes_[3*M0_].x), (int)Math.round(outerNodes_[3*M0_].y));
		for(int k=0; k<M0_*R_; k++){
			for(int branchCounter=0; branchCounter<4; branchCounter++){
				skin[4+branchCounter].addPoint((int)Math.round(xInnerPosSkin_[branchCounter][k]),(int)Math.round(yInnerPosSkin_[branchCounter][k]));
			}
		}
		return(skin);
	}

	// ----------------------------------------------------------------------------

	/** 
	 * This method provides a mutator to the snake-defining nodes. It will be called repeatedly by
	 * the methods Snake2DKeeper.interact() and Snake2DKeeper.optimize(). These calls are unconditional 
	 * and may happen whether the method isAlive() returns true or false.
	 */
	@Override
	public void setNodes (Snake2DNode[] node) {

		for(int i=0;i<=(8*M0_-3);i++){
			allNodes_[i].x = node[i].x;
			allNodes_[i].y = node[i].y;
		}

		int cont = 0;
		for(int i=0; i<M_; i++){
			outerNodes_[i].x = allNodes_[cont].x;
			outerNodes_[i].y = allNodes_[cont].y;
			cont++;
		}

		for(int branchNumber=0; branchNumber<4; branchNumber++){
			for(int i=0; i<(M0_-1); i++){
				innerNodes_[branchNumber][i].x = allNodes_[cont].x;
				innerNodes_[branchNumber][i].y = allNodes_[cont].y;
				cont++;
			}
		}

		wPouchCenterNode_.x = allNodes_[cont].x;
		wPouchCenterNode_.y = allNodes_[cont].y;
		cont++;

		wDiscCenterNode_.x = allNodes_[cont].x;
		wDiscCenterNode_.y = allNodes_[cont].y;

		try {
			computeSnakeSkin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------

	/** Retrieves one of the four compartments provided by the editable shape. */
	public Compartment getCompartment (int i) throws Exception {

		Compartment c = new Compartment("");

		for(int k=(i*M0_+2)*R_; k<=((i+1)*M0_+2)*R_; k++){
			int index = k%MR_;
			if(k<0) index += MR_;
			c.addPoint((int)Math.round(xOuterPosSkin_[index]),(int)Math.round(yOuterPosSkin_[index]));
		}

		for(int k=0; k<M0_*R_; k++){
			c.addPoint((int)Math.round(xInnerPosSkin_[(i+1)%4][k]),(int)Math.round(yInnerPosSkin_[(i+1)%4][k]));
		}
		c.addPoint((int)Math.round(wPouchCenterNode_.x), (int)Math.round(wPouchCenterNode_.y));
		for(int k=0; k<M0_*R_; k++){
			c.addPoint((int)Math.round(xInnerPosSkin_[i][M0_*R_-k-1]),(int)Math.round(yInnerPosSkin_[i][M0_*R_-k-1]));
		}
		return c;
	}

	// ----------------------------------------------------------------------------

	/** Retrieves the four initial compartments. */
	public ShapeRoi getAllCompartmentsAsShape () throws Exception {

		if(initialCompartments_[0]!=null && initialCompartments_[1]!=null && initialCompartments_[2]!=null && initialCompartments_[3]!=null){
			ShapeRoi[] compartmentShape = new ShapeRoi[4];
			for(int i=0; i<4; i++){
				compartmentShape[i] = new ShapeRoi(initialCompartments_[i].toPolygon());
			}
			return ((compartmentShape[0].or(compartmentShape[1])).or(compartmentShape[2])).or(compartmentShape[3]); 
		}
		return null;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Retrieves one of the two internal boundaries provided by the editable shape.
	 * 0 = A/P boundary
	 * 1 = D/V boundary  
	 */
	public Boundary getBoundary (int i) throws Exception {

		Boundary b = new Boundary("");
		for(int k=0; k<M0_*R_; k++){
			b.addPoint(xInnerPosSkin_[i][k], yInnerPosSkin_[i][k]);
		}
		b.addPoint(wPouchCenterNode_.x, wPouchCenterNode_.y);
		for(int k=0; k<M0_*R_; k++){
			b.addPoint(xInnerPosSkin_[i+2][M0_*R_-k-1], yInnerPosSkin_[i+2][M0_*R_-k-1]);
		}
		return b;
	}

	// ----------------------------------------------------------------------------

	/** Retrieves the wing pouch contour provided by the editable shape. */
	public Compartment getWPouchContour() throws Exception {

		Compartment contour = new Compartment("");
		for(int k=0; k<MR_; k++){
			contour.addPoint((int)Math.round(xOuterPosSkin_[k]), (int)Math.round(yOuterPosSkin_[k]));
		}
		return contour;
	}

	// ----------------------------------------------------------------------------

	/** Retrieves the point on the exterior contour where the an axis attaches. */
	@Override
	public Snake2DNode getAnchorPointOnContour(int axis) throws Exception {

		return(new Snake2DNode(xOuterPosSkin_[((axis*M0_+2)*R_)%(M_*R_)],yOuterPosSkin_[((axis*M0_+2)*R_)%(M_*R_)]));
	}

	// ----------------------------------------------------------------------------

	/** Retrieves a list of points containing the coordinates of the exterior outline. */
	@Override
	public Point2D.Double[] getExteriorArchCoordinates(int archNumber) throws Exception {

		Point2D.Double[] archCoordinates = new Point2D.Double[M0_*R_+1];

		switch(archNumber){
		case 0:
			for(int k=2*R_; k<=(M0_+2)*R_; k++){
				int j = k%MR_;
				if(k<0) j += MR_;
				archCoordinates[k-2*R_] = new Point2D.Double(xOuterPosSkin_[j], yOuterPosSkin_[j]);
			}
			break;
		case 1:
			for(int k=(M0_+2)*R_; k<=(2*M0_+2)*R_; k++){
				int j = k%MR_;
				if(k<0) j += MR_;
				archCoordinates[k-(M0_+2)*R_] = new Point2D.Double(xOuterPosSkin_[j], yOuterPosSkin_[j]);
			}
			break;
		case 2:
			for(int k=(2*M0_+2)*R_; k<=(3*M0_+2)*R_; k++){
				int j = k%MR_;
				if(k<0) j += MR_;
				archCoordinates[k-(2*M0_+2)*R_] = new Point2D.Double(xOuterPosSkin_[j], yOuterPosSkin_[j]);
			}
			break;
		case 3:
			for(int k=(3*M0_+2)*R_; k<=(4*M0_+2)*R_; k++){
				int j = k%MR_;
				if(k<0) j += MR_;
				archCoordinates[k-(3*M0_+2)*R_] = new Point2D.Double(xOuterPosSkin_[j], yOuterPosSkin_[j]);
			}
			break;
		default:
		}
		return(archCoordinates);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Reorganizes allNodes using D, P, V, A points. */
	@Override
	public void reorganizeAllNodesFromDPVA(Point2D.Double D, Point2D.Double P, Point2D.Double V, Point2D.Double A) throws Exception {
		
		// this vector will contains the snake nodes group indexes associated
		// to the directions D, P, V, and A
		int[] indexes = new int[4];
		
		double smallestDistance = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double currentDistace = D.distance(getExteriorArchCoordinates(i)[0]);
			if(currentDistace<=smallestDistance){
				smallestDistance = currentDistace;
				indexes[0] = i;
			}
		}

		smallestDistance = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double currentDistace = P.distance(getExteriorArchCoordinates(i)[0]);
			if(currentDistace<=smallestDistance){
				smallestDistance = currentDistace;
				indexes[1] = i;
			}
		}
		
		smallestDistance = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double currentDistace = V.distance(getExteriorArchCoordinates(i)[0]);
			if(currentDistace<=smallestDistance){
				smallestDistance = currentDistace;
				indexes[2] = i;
			}
		}
		
		smallestDistance = Double.MAX_VALUE;
		for(int i=0; i<4; i++){
			double currentDistace = A.distance(getExteriorArchCoordinates(i)[0]);
			if(currentDistace<=smallestDistance){
				smallestDistance = currentDistace;
				indexes[3] = i;
			}
		}
		
		int sum = 0;
		for (int i = 0; i < indexes.length; i++)
			sum += indexes[i];
		if (sum != 6)
			throw new Exception("ERROR: Unable to apply orientation to structure snake.");
		
		//get chirality of the system
		Compartment c = new Compartment("chirality tester");
		c.addPoint(D.x,D.y);
		c.addPoint(P.x,P.y);
		c.addPoint(V.x,V.y);
		c.addPoint(A.x,A.y);

		// 4M0 + 4(M0-1) + 2 (pouch and disc center)
		Snake2DNode[] reorganizedNodes = new Snake2DNode[8*M0_-2];
		if(c.orientation() > 0){
			// add contour nodes
			int j = -1; // index of the group of nodes
			int target = 0; // index of the element to write in reorganizedNodes
			for (int i = 0; i < 4; i++) {
				j = indexes[i];
				int initIndex = j*M0_;
				int finalIndex = (j+1)*M0_;
				for (int k = initIndex; k < finalIndex; k++){
					reorganizedNodes[target++] = new Snake2DNode(allNodes_[k].x, allNodes_[k].y);
				}
			}
			
			// add branch nodes
			int shift = 4*M0_;
			for (int i = 0; i < 4; i++) {
				j = indexes[i];
				int initIndex = shift + j*(M0_-1);
				int finalIndex = shift + (j+1)*(M0_-1);
				for (int k = initIndex; k < finalIndex; k++){
					reorganizedNodes[target++] = new Snake2DNode(allNodes_[k].x,allNodes_[k].y);
				}
			}
			
			// add pouch and disc center
			reorganizedNodes[target++] = new Snake2DNode(allNodes_[allNodes_.length-2].x,allNodes_[allNodes_.length-2].y);
			reorganizedNodes[target++] = new Snake2DNode(allNodes_[allNodes_.length-1].x,allNodes_[allNodes_.length-1].y);
		}else{
			// add contour nodes
			int j = -1; // index of the group of nodes
			int target = 0; // index of the element to write in reorganizedNodes
			for (int i = 0; i < 4; i++) {
				j = indexes[i];
				int initIndex = j*M0_;
				int finalIndex = (j-1)*M0_;
				for (int k = initIndex; k > finalIndex; k--){
					reorganizedNodes[target++] = new Snake2DNode(allNodes_[(k+4*M0_)%(4*M0_)].x,allNodes_[(k+4*M0_)%(4*M0_)].y);
				}
			}
			
			// add branch nodes
			int shift = 4*M0_;
			for (int i = 0; i < 4; i++) {
				j = indexes[i];
				int initIndex = shift + j*(M0_-1);
				int finalIndex = shift + (j+1)*(M0_-1);
				for (int k = initIndex; k < finalIndex; k++){
					reorganizedNodes[target++] = new Snake2DNode(allNodes_[k].x,allNodes_[k].y);
				}
			}
			
			// add pouch and disc center
			reorganizedNodes[target++] = new Snake2DNode(allNodes_[allNodes_.length-2].x,allNodes_[allNodes_.length-2].y);
			reorganizedNodes[target++] = new Snake2DNode(allNodes_[allNodes_.length-1].x,allNodes_[allNodes_.length-1].y);
		}
		setNodes(reorganizedNodes);
	}
	
	// ----------------------------------------------------------------------------

	/** Sets this snake as the synthesis of multiple snake structures. */
	@Override
	public void aggregate(List<StructureSnake> snakes, int aggregationMode) throws Exception {
		
		if (snakes == null)
			throw new Exception("ERROR: List of structure snakes null.");
		if (snakes.size() < 2)
			throw new Exception("ERROR: At least two structure snakes are required for a meaningful synthesis.");
		
		// Determine the structure with more control points (more resolution)
		int M0 = 0;
		for(StructureSnake snake: snakes){
			int localM0 = snake.getNumControlPointsPerSegment();
			if(M0<localM0)
				M0 = localM0; 
		}
		
		// Resample all snakes to match the highest resolution
		for(StructureSnake snake: snakes){
			int localM0 = snake.getNumControlPointsPerSegment();
			if(M0!=localM0){
				snake.resample(M0);
			}
		}
		
		// Average control points
		int nControlPoints = snakes.get(0).getNodes().length;
		Snake2DNode[] averageSnakeNodes = new Snake2DNode[nControlPoints];
		for(int i=0; i<nControlPoints; i++){
			averageSnakeNodes[i] = new Snake2DNode(0,0);
		}
		
//		if(WJSettings.DEBUG) {
//			ImagePlusManager manager = ImagePlusManager.getInstance();
//			StructureSnake snake = null;
//			for(int i = 0; i < snakes.size(); i++) {
//				snake = snakes.get(i);
//				Snake2DNode[] snakeNodes = snake.getNodes();
//				ImagePlus background = IJ.createImage("snake_" + i, "black", 1024, 1024, 1);
//				for (int j = 0; j < nControlPoints; j++)
//					background.getProcessor().set((int)snakeNodes[j].x, (int)snakeNodes[j].y, 10*j);
//				background.getProcessor().filter(ImageProcessor.MAX);
//				background.getProcessor().filter(ImageProcessor.MAX);
//				background.getProcessor().filter(ImageProcessor.MAX);
//				background.getProcessor().filter(ImageProcessor.MAX);
//				background.getProcessor().filter(ImageProcessor.MAX);
//				manager.add(background.getTitle(), background, true);
//			}
//		}
		
		for(StructureSnake snake: snakes){
			Snake2DNode[] snakeNodes = snake.getNodes();
			for(int i=0; i<nControlPoints; i++){
				averageSnakeNodes[i].x += snakeNodes[i].x;
				averageSnakeNodes[i].y += snakeNodes[i].y;
			}
		}
		for(int i=0; i<nControlPoints; i++){
			averageSnakeNodes[i].x /= snakes.size();
			averageSnakeNodes[i].y /= snakes.size();
		}
		
		// computes the mean of the pouch center
		Snake2DNode[] selection = getNodes(snakes, averageSnakeNodes.length-2);
		Snake2DNode center = averageNode(selection);
		averageSnakeNodes[averageSnakeNodes.length-2] = center;
		// computes the mean of the disc center
		selection = getNodes(snakes, averageSnakeNodes.length-1);
		averageSnakeNodes[averageSnakeNodes.length-1] = averageNode(selection);
		
		// for the first 4*M0_ nodes (contour nodes), aggregates the nodes depending on the given aggregation mode
		int n = 4*M0;
		int i = 0;
		for (i = 0; i < n; i++) {
			selection = getNodes(snakes, i);
			averageSnakeNodes[i] = getNodeFromNodeCloud(selection, center, aggregationMode);
		}
		
		// for the next 4*(M0_-1) nodes (boundary nodes), average them
		n += 4*(M0-1);
		for (; i < n; i++) {
			selection = getNodes(snakes, i);
			averageSnakeNodes[i] = getNodeFromNodeCloud(selection, center, AGGREGATION_MEAN);
		}
		
		setNumControlPointsPerSegment(M0);
		initialize(averageSnakeNodes);
	}
	
	// ============================================================================
	// PRIVATE METHODS

	/** The purpose of this method is to modify the number of control points of the snake. */
	@Override
	public void resample(int M0) throws Exception {

		WJSettings settings = WJSettings.getInstance();

		if(M0<3)
			throw new Exception("ERROR: Number of control points per segment must be >= 3.");
		if(M0_ == M0)
			return; // do nothing

		if (initialized_) {
			if(M0_!=M0){
				Boundary[] resampledOuterNodes = new Boundary[4]; 
				for(int arcNum = 0; arcNum<4; arcNum++){
					Vector<Point2D.Double> arcPolyline = new Vector<Point2D.Double>();
					for(int i=0; i<=M0_; i++){
						arcPolyline.addElement(outerNodes_[(i+arcNum*M0_)%outerNodes_.length]);
					}
					resampledOuterNodes[arcNum] = new Boundary("auxiliar polyline", arcPolyline);
					resampledOuterNodes[arcNum] = resampledOuterNodes[arcNum].resample(M0+1);
					//the last point is redundant
				}

				Boundary[] resampledInnerNodes = new Boundary[4]; 
				for(int arcNum = 0; arcNum<4; arcNum++){
					Vector<Point2D.Double> polyline = new Vector<Point2D.Double>();
					polyline.addElement(getAnchorPointOnContour(arcNum));
					for(int i=0; i<innerNodes_[arcNum].length; i++){
						polyline.addElement(innerNodes_[arcNum][i]);
					}
					polyline.addElement(wPouchCenterNode_);

					resampledInnerNodes[arcNum] = new Boundary("auxiliar polyline", polyline);
					resampledInnerNodes[arcNum] = resampledInnerNodes[arcNum].resample(M0+1);
					// the first point is redundant,
					// it corresponds to the snake center
				}

				R_ = (int)Math.ceil((settings.getExpression1DNumPoints()-1)/(2.0*M0));
				M0_= M0;
				M_ = 4*M0_;
				NR_ = N_*R_;
				MR_= M_*R_;
				PIM_= Math.PI/M_;
				PI2M_= 2.0*PIM_;

				xOuterPosSkin_ = new double[MR_];
				yOuterPosSkin_ = new double[MR_];

				xInnerPosSkin_ = new double[4][(M0_+1)*R_];
				yInnerPosSkin_ = new double[4][(M0_+1)*R_];

				eSplineFunc_ = new double[NR_];
				for (int i=0; i<NR_; i++){
					eSplineFunc_[i] = ESpline4((double)i/(double)R_);
				}

				allNodes_ = new Snake2DNode[8*M0_-2];
				innerNodes_ = new Snake2DNode[4][M0_-1];
				outerNodes_ = new Snake2DNode[M_];

				for(int arcNum = 0; arcNum<4; arcNum++){
					float[] x = resampledOuterNodes[arcNum].xpoints;
					float[] y = resampledOuterNodes[arcNum].ypoints;
					for(int i=0; i<M0_; i++){
						outerNodes_[i+arcNum*M0_] = new Snake2DNode(x[i],y[i]);
					}
				}

				for(int arcNum = 0; arcNum<4; arcNum++){
					float[] x = resampledInnerNodes[arcNum].xpoints;
					float[] y = resampledInnerNodes[arcNum].ypoints;
					for(int i=0; i<(M0_-1); i++){
						innerNodes_[arcNum][i] = new Snake2DNode(x[i+1],y[i+1]);
					}
				}

				// load all parameters in allNodes_
				int cont = 0;
				for(int i=0; i<M_; i++){
					allNodes_[cont] = outerNodes_[i];
					cont++;
				}	
				for(int arcNum=0; arcNum<4; arcNum++){
					for(int i=0; i<(M0_-1); i++){
						allNodes_[cont] = innerNodes_[arcNum][i];
						cont++;
					}	
				}

				allNodes_[cont] = wPouchCenterNode_;
				allNodes_[cont+1] = wDiscCenterNode_;

				// compute the snake contour
				computeSnakeSkin();
			}
		}else{
			throw new Exception("WingSnake has not been initialized yet!");
		}
	}

	// ----------------------------------------------------------------------------

	/** Reverses in the horizontal dimension all snake nodes with respect to the input point. */
	@Override
	public void flipHorizontally(Point2D.Double center) throws Exception {
		if (center == null)
			throw new Exception("ERROR: Axis of reflection is null.");
		
		Snake2DNode[] reflectedNodes = new Snake2DNode[allNodes_.length];

		for(int i=0; i<allNodes_.length; i++)
			reflectedNodes[i] = new Snake2DNode(2.0 * center.x - allNodes_[i].x, allNodes_[i].y);

		this.setNodes(reflectedNodes);
	}
	
	// ----------------------------------------------------------------------------

	/** Reverses in the vertically dimension all snake nodes with respect to the input point. */
	@Override
	public void flipVertically(Point2D.Double center) throws Exception {
		if (center == null)
			throw new Exception("ERROR: Axis of reflection is null.");
		
		Snake2DNode[] reflectedNodes = new Snake2DNode[allNodes_.length];

		for(int i=0; i<allNodes_.length; i++)
			reflectedNodes[i] = new Snake2DNode(center.x, 2.0 * allNodes_[i].y - allNodes_[i].y);

		this.setNodes(reflectedNodes);
	}

	// ----------------------------------------------------------------------------

	/** Rotates all snake nodes a given angle with respect to a specified center. */
	@Override
	public void rotate(Point2D.Double center, double angle) throws Exception {

		if (center == null)
			throw new Exception("ERROR: Center of rotation is null.");
		
		Snake2DNode[] rotatedNodes = new Snake2DNode[allNodes_.length];
		for(int i=0; i<allNodes_.length; i++)
			rotatedNodes[i] = new Snake2DNode(Math.cos(angle)*(allNodes_[i].x-center.x) - Math.sin(angle)*(allNodes_[i].y-center.y) + center.x,Math.sin(angle)*(allNodes_[i].x-center.x) + Math.cos(angle)*(allNodes_[i].y-center.y) + center.y);
		
		this.setNodes(rotatedNodes);
	}

	// ----------------------------------------------------------------------------
	
	/** Translates the snake using the given vector. */
	@Override
	public void translate(double dx, double dy) throws Exception {
	
		Snake2DNode[] shiftedNodes = new Snake2DNode[allNodes_.length];
		for(int i=0; i<allNodes_.length; i++)
			shiftedNodes[i] = new Snake2DNode(allNodes_[i].x + dx, allNodes_[i].y + dy);
		
		this.setNodes(shiftedNodes);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Checks if all initial information is ready to build the WingSnake model. */
	@Override
	protected void checkInitialization () throws Exception{

		if(initialwDiscCenter_==null){
			WJSettings.log("WARNING: WingSnake can not be built. Wing disc center missing.");
			throw new Exception("WARNING: The structure detection failed. Please see the console.\n\n" +
					"Click on \"Step\" for step-by-step detection.\nClick on \"Manual\" for manual detection.");
		}

		if(initialwPouchCenter_==null){
			WJSettings.log("WARNING: WingSnake can not be built. Wing pouch center missing.");
			throw new Exception("WARNING: The structure detection failed. Please see the console.\n\n" +
					"Click on \"Step\" for step-by-step detection.\nClick on \"Manual\" for manual detection.");
		}

		if(initialWingPouchBoundary_==null){
			WJSettings.log("WARNING: WingSnake can not be built. Wing pouch external contour missing.");
			throw new Exception("WARNING: The structure detection failed. Please see the console.\n\n" +
					"Click on \"Step\" for step-by-step detection.\nClick on \"Manual\" for manual detection.");
		}

		for(int i=0; i<4; i++){
			if(initialBoundaries_[i]==null){
				WJSettings.log("WARNING: WingSnake can not be built. Wing boundary number "+i+" missing.");
				throw new Exception("WARNING: The structure detection failed. Please see the console.\n\n" +
						"Click on \"Step\" for step-by-step detection.\nClick on \"Manual\" for manual detection.");
			}else{
				if(initialBoundaries_[i].npoints<3){
					WJSettings.log("WARNING: WingSnake can not be built. Wing boundary number "+i+" invalid.");
					throw new Exception("ERROR: The structure detection failed. Please see the console.\n\n" +
							"Click on \"Step\" for step-by-step detection.\nClick on \"Manual\" for manual detection.");
				}
			}
		}
	}

	// ============================================================================
	// PRIVATE METHODS
 
	/** Corrects the intersection point (intersection of the D/V and A/P boundaries). */
	private void correctIntersection() throws Exception {

		double x1 = innerNodes_[0][innerNodes_[0].length-1].x;
		double y1 = innerNodes_[0][innerNodes_[0].length-1].y;

		double x2 = innerNodes_[2][innerNodes_[2].length-1].x;
		double y2 = innerNodes_[2][innerNodes_[2].length-1].y;

		double x3 = innerNodes_[1][innerNodes_[1].length-1].x;
		double y3 = innerNodes_[1][innerNodes_[1].length-1].y;

		double x4 = innerNodes_[3][innerNodes_[3].length-1].x;
		double y4 = innerNodes_[3][innerNodes_[3].length-1].y;

		allNodes_[allNodes_.length-2].x = ((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));
		allNodes_[allNodes_.length-2].y = ((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/((x1-x2)*(y3-y4)-(y1-y2)*(x3-x4));

		setNodes(allNodes_);
	}

	// ----------------------------------------------------------------------------

	/** Searches the indices (within the outline of the compartment) of the closest points to the boundary ends. */
	private int[] findBranchJoints (Compartment compartment) throws Exception {

		int[] connectionIndexes = new int[4];
		int nPoints = compartment.npoints;
		float[] xPoints = compartment.xpoints;
		float[] yPoints = compartment.ypoints;
		for(int i=0; i<4; i++){
			double jointX = initialBoundaries_[i].getLastPoint().x;
			double jointY = initialBoundaries_[i].getLastPoint().y;

			double optimalDist = java.lang.Double.MAX_VALUE;
			double currentDist = 0;
			int optimalIndex = 0;

			for(int j=0; j<nPoints; j++){
				double dx = (jointX-xPoints[j]);
				double dy = (jointY-yPoints[j]);
				currentDist = Math.sqrt(dx*dx+dy*dy);
				if(currentDist<optimalDist){
					optimalDist = currentDist;
					optimalIndex = j;
				}
			}
			connectionIndexes[i] = optimalIndex;
		}
		return connectionIndexes;
	}

	// ----------------------------------------------------------------------------

	/** Distributes linearly snake nodes over a polygon. */
	private void distributeOnPath (Snake2DNode[] nodeArray, FloatPolygon path, double length){

		int nPointsPath = nodeArray.length;
		double step = length/(nPointsPath+1);

		int nPoints = path.npoints;
		float[] xPoints = path.xpoints;
		float[] yPoints = path.ypoints;

		double accumulatedLength = 0;
		boolean done = false;
		for(int i=0; i<(nPoints-1) && !done; i++){
			float xi = xPoints[i];
			float yi = yPoints[i];
			float xi1 = xPoints[i+1];
			float yi1 = yPoints[i+1];

			double segmentLength = Math.sqrt((xi1-xi)*(xi1-xi)+(yi1-yi)*(yi1-yi));

			int index1 = (int)Math.floor(accumulatedLength/step);
			int index2 = (int)Math.floor((accumulatedLength+segmentLength)/step);

			if(index1!=index2){
				nodeArray[nPointsPath-index1-1] = new Snake2DNode((xi+xi1)/2.0, (yi+yi1)/2.0);
			}
			if(index2==nPointsPath){
				done = true;
			}else{
				accumulatedLength += segmentLength;
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes and stores in a LUT the samples of the trace of the snake. */
	@Override
	protected void computeSnakeSkin () throws Exception{

		computeBoundarySnakeSkin();
		computeAxesSnakeSkin();
	}

	// ----------------------------------------------------------------------------

	/** Computes and stores in a LUT the samples of the trace of the outside contour of the snake. */
	private void computeBoundarySnakeSkin (){

		int index;
		double aux, xPosVal, yPosVal;
		for(int i=0; i<MR_; i++){
			xPosVal = 0.0;
			yPosVal = 0.0;
			for(int k=0; k<M_; k++){
				index = (i-k*R_)%(MR_);
				if (index<0) index += MR_;

				if(index>=NR_){
					continue;
				}else{
					aux = eSplineFunc_[index];
				}
				xPosVal += outerNodes_[k].x*aux;
				yPosVal += outerNodes_[k].y*aux;
			}
			xOuterPosSkin_[i] = xPosVal;
			yOuterPosSkin_[i] = yPosVal;
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes the trace of one axis of the wing pouch. */
	private Point2D.Double[] computeAxisTrace(int nPoints, Snake2DNode wPouchCenterAnchorPoint, Snake2DNode[] intermediateAnchorPoints, Point2D.Double externalContourAnchorPoint) throws Exception {

		Point2D.Double[] anchorPoints = new Point2D.Double[M0_+1];
		anchorPoints[0] = externalContourAnchorPoint;
		for(int i=1; i<M0_; i++){
			anchorPoints[i] = intermediateAnchorPoints[i-1];
		}
		anchorPoints[M0_] = wPouchCenterAnchorPoint;

		PolynomialSplineFunction[] psf = naturalCubicSplineInterpolator(anchorPoints);

		Point2D.Double[] axis = new Point2D.Double[nPoints];
		for(int i=0; i<nPoints; i++){
			axis[i] = new Point2D.Double(psf[0].value((double)i/(double)R_), psf[1].value((double)i/(double)R_));
		}
		return axis;
	}

	// ----------------------------------------------------------------------------

	/** Computes and stores in a LUT the samples of the trace of the central cross of the snake. */
	private void computeAxesSnakeSkin () throws Exception {

		for(int axis=0; axis<4; axis++){
			Point2D.Double[] axisTrace = computeAxisTrace(M0_*R_, wPouchCenterNode_, innerNodes_[axis], getAnchorPointOnContour(axis));

			for(int i=0; i<M0_*R_; i++){
				xInnerPosSkin_[axis][i] = axisTrace[i].x;
				yInnerPosSkin_[axis][i] = axisTrace[i].y;
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Exponential B-spline of order four. */
	private double ESpline4(double t){

		double ESplineValue = 0.0;
		double eta = 2*(1-Math.cos(PI2M_))/(PI2M_*PI2M_);
		if ((t>=0) & (t<=1)){
			ESplineValue = t - Math.sin(PI2M_*t)/PI2M_;
		}else if ((t>1) & (t<=2)){
			ESplineValue = 2 - t + 2*Math.sin(PI2M_*(t-1))/PI2M_ + Math.sin(PI2M_*(t-2))/PI2M_ - 2*Math.cos(PI2M_)*t + 2*Math.cos(PI2M_);
		}else if ((t>2) & (t<=3)){
			ESplineValue = t - 2 - 4*Math.cos(PI2M_) - 2*Math.sin(PI2M_*(t-3))/PI2M_ + 2*Math.cos(PI2M_)*(t-1) - Math.sin(PI2M_*(t-2))/PI2M_;
		}else if ((t>3) & (t<=4)){
			ESplineValue = 4 - t + Math.sin(PI2M_*(t-4))/PI2M_;
		}else{
			ESplineValue = (double)(0.0);
		}
		ESplineValue = ESplineValue/(PI2M_*PI2M_*eta);
		return(ESplineValue);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Computes the location of the spline coefficients given an array of points the spline must 
	 * interpolate using exponential B-splines of order four.
	 */
	private Snake2DNode[] getSplineKnots(Point2D.Double[] contour) {

		double[] knotsX = new double[M_];
		double[] knotsY = new double[M_];

		for (int i = 0; i < M_; i++){
			knotsX[i] = contour[i].x;
			knotsY[i] = contour[i].y;
		}

		double b = ESpline4(2.0);
		double[] pole = {(-b+Math.sqrt(2*b-1))/(1-b)};
		knotsX = Filters.allPoleIIRFilter(knotsX, pole);
		knotsY = Filters.allPoleIIRFilter(knotsY, pole);

		Snake2DNode[] newCoeff = new Snake2DNode[M_];  
		for (int i = 0; i < M_; i++){
			if(knotsX[i]<0) knotsX[i] = 0;
			if(knotsX[i]>=(width_-1)) knotsX[i] = width_-1;
			if(knotsY[i]<0) knotsY[i] = 0;
			if(knotsY[i]>(height_-1)) knotsY[i] = height_-1;
			newCoeff[i] = new Snake2DNode(knotsX[i], knotsY[i]);
		}
		return newCoeff;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Expand positively or negatively the control points defining the contour of the structure.
	 * The amplitude of the given expansion is applied from the center of mass of the structure.
	 * The amplitude of the expansion is given in px.
	 */
	public void expand(double amplitude) throws Exception {
		
		if (outerNodes_ == null)
			throw new Exception("Snake outer nodes are null.");
		
		// outer nodes: outerNodes_;
		Point2D.Double cog = computeCenterOfGravity(); // wPouchCenterNode_
		Point2D.Double tmp = null;
		for (int i = 0; i < outerNodes_.length; i++) {
			tmp = new Point2D.Double(outerNodes_[i].x, outerNodes_[i].y);
			Segment.movePointFromReference(tmp, cog, amplitude);
			outerNodes_[i].setLocation(tmp);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the center of gravity of the structure based on the contour of structure. */
	public Point2D.Double computeCenterOfGravity() throws Exception {
		
		double meanx = 0.;
		double meany = 0.;
		for (int i = 0; i < outerNodes_.length; i++) {
			meanx += outerNodes_[i].x;
			meany += outerNodes_[i].y;
		}
		return new Point2D.Double (meanx/outerNodes_.length, meany/outerNodes_.length);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	public void setInitialContour (Compartment wingPouchBoundary) { initialWingPouchBoundary_ = wingPouchBoundary; }
	public Compartment getInitialContour () { return initialWingPouchBoundary_; }

	public void setInitialwPouchCenter (Point2D.Double initialwPouchCenter) { initialwPouchCenter_ = initialwPouchCenter; }
	public void setWPouchCenter(double x, double y) { wPouchCenterNode_ = new Snake2DNode(x, y); }
	public Point2D.Double getWPouchCenter () { return(new Point2D.Double(wPouchCenterNode_.x, wPouchCenterNode_.y)); }

	public void setInitialwDiscCenter (Point2D.Double initialwDiscCenter) { initialwDiscCenter_ = initialwDiscCenter; }
	public void setWDiscCenter (double x, double y) { wDiscCenterNode_ = new Snake2DNode(x, y); }
	public Point2D.Double getWDiscCenter () { return(new Point2D.Double(wDiscCenterNode_.x, wDiscCenterNode_.y)); }

	public void setInitialCompartment (int i, Compartment compartment){ initialCompartments_[i] = compartment; }
	public Compartment getInitialCompartment (int i){ return initialCompartments_[i]; }

	public void setInitialBoundary (int i, Boundary boundary){ initialBoundaries_[i] = boundary; }
}