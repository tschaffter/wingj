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

package ch.epfl.lis.wingj.structure.tools;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatProcessor;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.structure.geometry.Triangle;
import ch.epfl.lis.wingj.utilities.MathUtils;


import big.ij.snake2D.Snake2D;
import big.ij.snake2D.Snake2DNode;
import big.ij.snake2D.Snake2DScale;

/**
 * Implements an active contour method (snake) to identify fluorescence features
 * with cross-like shape.
 * <p>
 * The snake is parameterized by 5 points: the kite center, and 4 external points 
 * that define the exterior contour of the kite.
 * <p>
 * The energy is composed of two terms: the image energy, that leads the central 
 * cross of the kite towards regions in the image with high pixel values; and the
 * regularization energy that prevents the external kite points to move far away 
 * of a region around the kite center.
 * 
 * @version March 5, 2013
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class KiteSnake implements Snake2D {

	/**
	 * Array of length 5, containing the control points of the snake.
	 * <p>
	 * The first 4 elements of the array contain the 4 exterior points sorted in
	 * clockwise or counter-clockwise manner. The last position contains the kite
	 * center.
	 */
	private Snake2DNode[] node_ = new Snake2DNode[5];

	// ----------------------------------------------------------------------------
	// SNAKE CONTOUR

	/**
	 * Array of length 4, containing the triangles that compose the snake.
	 * <p>
	 * The nodes of the i-th triangle are, in order, <code>node_[4]</code>,
	 * <code>node_[i]</code> and <code>node_[(i+1) mod 4]</code>.
	 */
	private Triangle[] outerTriangles_ = new Triangle[4];
	/**
	 * Array of length 4, containing the inner triangles defined inside the outer
	 * triangles to form the cross.
	 * <p>
	 * The ordering is equivalent to the one of <code>outerTriangles_</code>.
	 */
	private Triangle[] innerTriangles_ = new Triangle[4];

	
	// ----------------------------------------------------------------------------
	// SNAKE STATUS FIELDS

	/**
	 * If <code>true</code> indicates that the snake is able to keep being
	 * optimized.
	 */
	private boolean alive_ = true;
	/**
	 * If <code>true</code>, indicates that the user chose to interactively abort
	 * the processing of the snake. Otherwise, if <code>false</code>, indicates
	 * that the dealings with the snake were terminated without user assistance.
	 */
	private boolean canceledByUser_ = false;
	/**
	 * Number of iterations left when the <code>immortal_</code> is
	 * <code>false</code>.
	 */
	private int life_ = 0;

	/** Initial branch length */
	private double initialBranchLength_ = 100.;
	/** Initial kite center. */
	private Point2D.Double initialCenter_ = null;

	// ----------------------------------------------------------------------------
	// SNAKE OPTION FIELDS

	/**
	 * Maximum number of iterations allowed when the <code>immortal_</code> is
	 * <code>false</code>.
	 */
	private int maxNumIters_ = 0;
	/**
	 * If <code>true</code> indicates that the snake will keep iterating till the
	 * optimizer decides so.
	 */
	private boolean immortal_ = false;
	/** Inner and outer triangle areas ratio to affect dynamic branch widths. */
	private double innerOuterTriangleAreasRatio_ = 0.5;

	/** Energy value when the snake is built. */
	private double initialEnergy_ = 0;
	/** Energy value before the optimization process ends. */
	private Double finalEnergy_ = null;

	// ----------------------------------------------------------------------------
	// IMAGE FIELDS
	
	/** Original image data. */
	private float[] image_ = null;
	/** Pre-integrated image data along the vertical direction. */
	private double[] preintegratedImage_ = null;
	
	/** Width of the original image data. */
	private int imageWidth_ = 0;
	/** Height of the original image data. */
	private int imageHeight_ = 0;
	/** Width of the original image data minus two. */
	private int imageWidthMinusTwo_ = 0;
	/** Height of the original image data minus two. */
	private int imageHeightMinusTwo_ = 0;


	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public KiteSnake () {}

	// ----------------------------------------------------------------------------

	/** Build the KiteSnake once initialization is done. */
	public void build () throws Exception {

		if(image_==null)
			throw new Exception("ERROR: Image not set.");
		if(initialCenter_==null)
			throw new Exception("ERROR: Initial kite center not set.");

		for(int i=0; i<4; i++){
			outerTriangles_[i] = new Triangle(new Point2D.Double(0.0,0.0),new Point2D.Double(0.0,0.0),new Point2D.Double(0.0,0.0));
			innerTriangles_[i] = new Triangle(new Point2D.Double(0.0,0.0),new Point2D.Double(0.0,0.0),new Point2D.Double(0.0,0.0));
		}
		life_ = maxNumIters_;
		initNodes(initialCenter_, initialBranchLength_);
	}

	// ----------------------------------------------------------------------------

	/** Sets the image and builds the appropriate LUT's. */
	public void setImage (FloatProcessor ip, double sigma){

		FloatProcessor ipDup = (FloatProcessor) ip.duplicate();
		image_ = (float[])ipDup.getPixels();
		imageWidth_ = ipDup.getWidth();
		imageHeight_ = ipDup.getHeight();
		imageWidthMinusTwo_ = imageWidth_-2;
		imageHeightMinusTwo_ = imageHeight_-2;
		computePreIntegratedImage();
	}

	// ----------------------------------------------------------------------------

	/** Prevents the center of the snake to be modified. */
	public void freezeKiteCenter (boolean freeze){

		node_[4].frozen = freeze;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to compute the energy of the snake. */
	@Override
	public double energy (){

		life_--;
		if(life_==0) alive_ = false;

		for(int i=0; i<node_.length; i++){
			if(node_[i].x>(imageWidth_-1) || node_[i].x<0 || node_[i].y>(imageHeight_-1) || node_[i].y<0){
				return Double.MAX_VALUE;
			}
		}
		
		double energy = 0;
		try {
			energy = computeContrastEnergy() + computeKiteSnakeEnergyPenalty();
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}

		if (life_ == maxNumIters_-1)
			initialEnergy_ = energy;

		return energy;
	}

	// ----------------------------------------------------------------------------

	/**
	 * The purpose of this method is to compute the gradient of the snake energy
	 * with respect to the snake-defining nodes.
	 */
	@Override
	public Point2D.Double[] getEnergyGradient (){

		return null;
	}

	// ----------------------------------------------------------------------------

	/** This method provides an accessor to the snake-defining nodes. */
	@Override
	public Snake2DNode[] getNodes (){

		return node_;
	}

	// ----------------------------------------------------------------------------

	/**
	 * The purpose of this method is to determine what to draw on screen, given the
	 * current configuration of nodes.
	 */
	@Override
	public Snake2DScale[] getScales (){

		Snake2DScale[] skin = new Snake2DScale[7];

		skin[0] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), true, false);
		skin[0].addPoint((int)this.node_[0].x,(int)this.node_[0].y );
		skin[0].addPoint((int)this.node_[1].x,(int)this.node_[1].y );
		skin[0].addPoint((int)this.node_[2].x,(int)this.node_[2].y );
		skin[0].addPoint((int)this.node_[3].x,(int)this.node_[3].y );

		skin[1] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
		skin[1].addPoint((int)(Math.round(node_[0].x)),(int)(Math.round(node_[0].y)));
		skin[1].addPoint((int)(Math.round(node_[4].x)),(int)(Math.round(node_[4].y)));
		skin[1].addPoint((int)(Math.round(node_[2].x)),(int)(Math.round(node_[2].y)));

		skin[2] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
		skin[2].addPoint((int)(Math.round(node_[1].x)),(int)(Math.round(node_[1].y)));
		skin[2].addPoint((int)(Math.round(node_[4].x)),(int)(Math.round(node_[4].y)));
		skin[2].addPoint((int)(Math.round(node_[3].x)),(int)(Math.round(node_[3].y)));

		try {
			for(int i=0; i<4; i++){
				skin[i+3] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), false, false);
				skin[i+3].addPoint((int)(Math.round(innerTriangles_[i].getVertexX(1))),(int)Math.round(innerTriangles_[i].getVertexY(1)));
				skin[i+3].addPoint((int)(Math.round(innerTriangles_[i].getVertexX(0))),(int)Math.round(innerTriangles_[i].getVertexY(0)));
				skin[i+3].addPoint((int)(Math.round(innerTriangles_[i].getVertexX(2))),(int)Math.round(innerTriangles_[i].getVertexY(2)));
			}
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
		return skin;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to monitor the status of the snake. */
	@Override
	public boolean isAlive () {

		if(immortal_) 
			return true;
		else
			return alive_;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Sets the status of the snake to alive (<code>alive_=true</code>), and
	 * restores the maximum number iterations to the original one.
	 */
	public void reviveSnake () {

		alive_ = true;
		life_ = maxNumIters_;
	}

	// ----------------------------------------------------------------------------

	/** 
	 * This method provides a mutator to the snake-defining nodes.
	 * <p>
	 * It will be called repeatedly by the methods
	 * <code>Snake2DKeeper.interact()</code> and
	 * <code>Snake2DKeeper.optimize()</code>. These calls are unconditional and may
	 * happen whether the method <code>isAlive()</code> returns <code>true</code>
	 * or <code>false</code>.
	 */
	@Override
	public void setNodes (Snake2DNode[] inNode) {

		try {
			for(int i=0; i<5; i++){
				if(inNode[i].x == Double.NaN || inNode[i].x == Double.POSITIVE_INFINITY || inNode[i].x == Double.NEGATIVE_INFINITY){
					alive_ = false;
					return;
				}
				if(inNode[i].y == Double.NaN || inNode[i].y == Double.POSITIVE_INFINITY || inNode[i].y == Double.NEGATIVE_INFINITY){
					alive_ = false;
					return;
				}
				node_[i].x = inNode[i].x;
				node_[i].y = inNode[i].y;
				node_[i].frozen = inNode[i].frozen;
				node_[i].hidden = inNode[i].hidden;
			}
			updateTriangles();
		} catch (Exception e) {
			WJMessage.showMessage(e);
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * If <code>true</code>, indicates that the user chose to interactively abort
	 * the processing of the snake.
	 * <p>
	 * Otherwise, if <code>false</code>, indicates that the dealings with the snake
	 * were terminated without user assistance.
	 */
	public boolean isCanceledByUser (){

		return canceledByUser_;
	}

	// ----------------------------------------------------------------------------

	/**
	 * This method is called when the methods <code>Snake2DKeeper.interact()</code>,
	 * <code>Snake2DKeeper.interactAndOptimize()</code>, and 
	 * <code>Snake2DKeeper.optimize()</code> are about to terminate.
	 */
	@Override
	public void updateStatus (boolean canceledByUser, boolean snakeDied, boolean optimalSnakeFound, Double energy) {

		finalEnergy_ = energy;
		canceledByUser_ = canceledByUser;
	}

	// ----------------------------------------------------------------------------

	/**
	 * The purpose of this method is to determine the centroids of each triangle
	 * that constitute the KineSnake.
	 */
	public Point2D.Double[] getTriangleCentroids () {

		Point2D.Double[] centroids = new Point2D.Double[4];
		for(int i=0; i<4; i++){
			centroids[i] = innerTriangles_[i].getCentroid();
		}
		return centroids;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Provides an unit vector pointing to direction of the West control point of
	 * the Kite.
	 */
	public Point2D.Double getWestDirection (){

		double d = getWestBranchLength(); 
		return new Point2D.Double((node_[0].x-node_[4].x)/d, (node_[0].y-node_[4].y)/d);
	}

	// ----------------------------------------------------------------------------

	/**
	 *  Provides an unit vector pointing to direction of the North control point of
	 *  the Kite.
	 */
	public Point2D.Double getNorthDirection (){

		double d = getNorthBranchLength();
		return new Point2D.Double((node_[1].x-node_[4].x)/d, (node_[1].y-node_[4].y)/d);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Provides an unit vector pointing to direction of the East control point of
	 * the Kite.
	 */
	public Point2D.Double getEastDirection (){

		double d = getEastBranchLength(); 
		return new Point2D.Double((node_[2].x-node_[4].x)/d, (node_[2].y-node_[4].y)/d);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Provides an unit vector pointing to direction of the South control point of
	 * the Kite.
	 */
	public Point2D.Double getSouthDirection (){

		double d = getSouthBranchLength(); 
		return new Point2D.Double((node_[3].x-node_[4].x)/d, (node_[3].y-node_[4].y)/d);
	}

	// ----------------------------------------------------------------------------

	/** Return the length in pixel of the i-th branch. */
	public double getBranchLength (int branchIndex) {

		return node_[branchIndex].distance(node_[4]);
	}

	// ----------------------------------------------------------------------------

	/** Return the distance in pixel of the West branch. */
	public double getWestBranchLength () { 

		return getBranchLength(0); 
	}

	// ----------------------------------------------------------------------------

	/** Return the distance in pixel of the North branch. */
	public double getNorthBranchLength () { 

		return getBranchLength(1);
	}

	// ----------------------------------------------------------------------------

	/** Return the distance in pixel of the East branch. */
	public double getEastBranchLength () { 

		return getBranchLength(2);
	}

	// ----------------------------------------------------------------------------

	/** Return the distance in pixel of the South branch */
	public double getSouthBranchLength () { 

		return getBranchLength(3);
	}

	// ----------------------------------------------------------------------------

	/** Provides the center of the KiteSnake. */
	public Point2D.Double getKiteCenter (){

		return node_[4];
	}

	// ----------------------------------------------------------------------------

	/**
	 * Returns <code>true</code> if the KiteSnake converged after optimization.
	 */
	public boolean hasConverged () throws Exception {

		if (finalEnergy_ != null) {
			if (finalEnergy_.isInfinite() || finalEnergy_.isNaN())
				return false;
			if (finalEnergy_.doubleValue() > initialEnergy_)
				return false;
		}

		if (!isKiteConvex())
			return false;

		return true;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Returns <code>true</code> if the sum of the four branch angles is 360
	 * degrees.
	 */
	public boolean areBranchAnglesConsistent () throws Exception {

		List<Double> angles = computePositiveBranchAngles();
		double anglesSum = 0.;
		for (Double angle : angles)
			anglesSum += angle;

		return (Math.abs(anglesSum-360.) < 1e-12);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Returns <code>true</code> if each branch has a length longer than the given
	 * one in pixel.
	 */
	public boolean areBranchesLongerThan (double minLength) throws Exception {

		for (int i = 0; i < 4; i++) {
			if (getBranchLength(i) < minLength)
				return false;
		}
		return true;
	}

	// ----------------------------------------------------------------------------

	/**
	 * Computes the angles between the four branches of the KiteSnake (positive
	 * angles).
	 */
	public List<Double> computePositiveBranchAngles () throws Exception {

		// normalized
		List<Point2D> directions = new ArrayList<Point2D>();
		directions.add(getNorthDirection());
		directions.add(getEastDirection());
		directions.add(getSouthDirection());
		directions.add(getWestDirection());

		List<Double> angles = new ArrayList<Double>();
		Point2D.Double origin = new Point2D.Double(0., 0.);
		for (int i = 0; i < directions.size(); i++)
			angles.add(MathUtils.angleBetween((Point2D.Double) directions.get((i+1)%directions.size()), origin, (Point2D.Double) directions.get(i%directions.size())));

		// from observations
		for (int i = 0; i < angles.size(); i++) {
			// if angle > 0 (typically 280), remove 360 and then take absolute value
			if (angles.get(i) > 0)
				angles.set(i, angles.get(i)-360);
			// if negative (typically -89), take absolute value
			if (angles.get(i) < 0)
				angles.set(i, Math.abs(angles.get(i)));
		}

		return angles;
	}

	// ----------------------------------------------------------------------------

	/** 
	 * Sets the parameters of the KiteSnake such that the initialization matches a
	 * specific length and width.
	 * <p>
	 * IMPORTANT: This method has to be invoked before the build method in order to
	 * the parameters to be set correctly.
	 */
	public void setGeometry (double initialBranchLength, double initialBranchWidth) throws Exception {

		initialBranchLength_ = initialBranchLength;
		innerOuterTriangleAreasRatio_ = (1.0-initialBranchWidth/initialBranchLength)*(1.0-initialBranchWidth/initialBranchLength);

		if(innerOuterTriangleAreasRatio_<=0 || innerOuterTriangleAreasRatio_>=1)
			throw new Exception("WingJ Error: KiteSnake geometry parameters are not valid.");
	}

	// ============================================================================
	// PRIVATE METHODS

	/** This method returns <code>true</code> if the exterior kite points define a
	 * convex shape.
	 */
	private boolean isKiteConvex () {

		Polygon pol = new Polygon();
		for(int i=0; i<4; i++){
			pol.addPoint((int)node_[i].x, (int)node_[i].y);
		}

		PolygonRoi currentRoi = new PolygonRoi(pol, Roi.POLYGON); 
		Polygon convexHull = currentRoi.getConvexHull();
		if(convexHull.npoints!=4){
			return false;
		}
		return true;
	}

	// ----------------------------------------------------------------------------

	/** This method initializes the snake defining nodes in a cross shape. */
	private void initNodes (Point2D.Double center, double length){

		node_[0] = new Snake2DNode(Math.min(Math.max(center.x-length, 1), imageWidthMinusTwo_), Math.min(Math.max(center.y, 1), imageHeightMinusTwo_));
		node_[1] = new Snake2DNode(Math.min(Math.max(center.x, 1), imageWidthMinusTwo_), Math.min(Math.max(center.y-length, 1), imageHeightMinusTwo_));
		node_[2] = new Snake2DNode(Math.min(Math.max(center.x+length, 1), imageWidthMinusTwo_), Math.min(Math.max(center.y, 1), imageHeightMinusTwo_));
		node_[3] = new Snake2DNode(Math.min(Math.max(center.x, 1), imageWidthMinusTwo_), Math.min(Math.max(center.y+length, 1), imageHeightMinusTwo_));
		node_[4] = new Snake2DNode(center.x,center.y);
		setNodes(node_);
	}

	// ----------------------------------------------------------------------------

	/** Computes the pre-integrated image. */
	private void computePreIntegratedImage () {

		preintegratedImage_ = new double[imageWidth_*imageHeight_];
		double fy_val;
		for(int x=0; x<imageWidth_; x++){
			fy_val = 0.0;
			for (int y=0; y<imageHeight_; y++){
				int index = x+imageWidth_*y;
				fy_val += (double)image_[index];
				preintegratedImage_[index] = fy_val;
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Computes the points that define an inner triangles. */
	private void updateTriangles () throws Exception {

		if (innerOuterTriangleAreasRatio_ <= 0 || innerOuterTriangleAreasRatio_ >= 1)
			throw new Exception("ERROR: Invalid innerOuterTriangleAreasRatio_.");

		double delta = 0.5*(1.0-Math.sqrt(innerOuterTriangleAreasRatio_));
		for(int triangle=0; triangle<4; triangle++){
			Point2D.Double c = node_[4];
			Point2D.Double p = node_[triangle%4];
			Point2D.Double q = node_[(triangle+1)%4];

			outerTriangles_[triangle].setVertex(0, c.x, c.y);
			outerTriangles_[triangle].setVertex(1, p.x, p.y);
			outerTriangles_[triangle].setVertex(2, q.x, q.y);

			innerTriangles_[triangle].setVertex(0, delta*(q.x+p.x)+(1-2.0*delta)*c.x, delta*(q.y+p.y)+(1-2.0*delta)*c.y);
			innerTriangles_[triangle].setVertex(1, p.x+delta*(q.x-p.x), p.y+delta*(q.y-p.y));
			innerTriangles_[triangle].setVertex(2, q.x+delta*(p.x-q.x), q.y+delta*(p.y-q.y));
		}
	}

	// ----------------------------------------------------------------------------

	/** Returns the energy of the KiteSnake. */
	private double computeContrastEnergy () throws Exception {

		// energy of the entire kite
		double Eout = 0;
		for(int i = 0; i < outerTriangles_.length; i++)
			Eout += integrateTriangle(outerTriangles_[i]);

		// energy of the inner triangles
		double Ein = 0;
		for(int i = 0; i < innerTriangles_.length; i++)
			Ein += integrateTriangle(innerTriangles_[i]);

		double totalArea = 0.;
		for (int i = 0; i < outerTriangles_.length; i++)
			totalArea += outerTriangles_[i].getArea();

		return (Eout - Ein) / totalArea;
	}

	// ----------------------------------------------------------------------------

	/** Returns the penalty of the KiteSnake. */
	private double computeKiteSnakeEnergyPenalty () {

		double penalty = 0;
		// penalty to keep the branch length as close as possible to the initial branch length
		double distance = 0.;
		for(int i = 0; i < 4; i++) {
			distance = node_[4].distance(node_[i]) - initialBranchLength_;
			penalty += distance*distance;
		}

		return penalty;
	}

	// ----------------------------------------------------------------------------

	/** Performs the integration of the image function over a given triangle. */
	private double integrateTriangle (Triangle t) throws Exception {

		int discretizationSamplingRate = (int)Math.ceil(Math.max(t.getPerimeter(),5));

		double Int = 0;
		double fuy_val;
		int x1, x2, y1, y2;
		double x, y, dx, dy;
		double DeltaX1, DeltaX2, DeltaY1;

		for(int j=0; j<3; j++){
			dx = t.getVertexX((j+1)%3) - t.getVertexX(j%3); 
			dy = t.getVertexY((j+1)%3) - t.getVertexY(j%3);

			for(int i=0; i<discretizationSamplingRate; i++){
				x = t.getVertexX(j%3) + dx * (double)i/(double)discretizationSamplingRate;
				y = t.getVertexY(j%3) + dy * (double)i/(double)discretizationSamplingRate;

				x1 = (int)Math.floor(x);
				y1 = (int)Math.floor(y);

				if(x1<1){
					x1 = 1;
				}else if(x1>imageWidthMinusTwo_){
					x1 = imageWidthMinusTwo_;
				}

				if(y1<1){
					y1 = 1;
				}else if(y1>imageHeightMinusTwo_){
					y1 = imageHeightMinusTwo_;
				}

				x2 = x1+1;
				y2 = y1+1;

				DeltaX1 = x - x1;
				DeltaY1 = y - y1;
				DeltaX2 = x2 - x;

				fuy_val = preintegratedImage_[x1+imageWidth_*(y1-1)]*DeltaX2+preintegratedImage_[x2+imageWidth_*(y1-1)]*DeltaX1; 
				fuy_val += 0.5*((image_[x1+imageWidth_*y1]*DeltaX2+image_[x2+imageWidth_*y1]*DeltaX1)+(DeltaY1*(((image_[x1+imageWidth_*y1]*DeltaX2+image_[x2+imageWidth_*y1]*DeltaX1)*(2-DeltaY1))+((image_[x1+imageWidth_*y2]*DeltaX2+image_[x2+imageWidth_*y2]*DeltaX1)*DeltaY1))));

				Int += fuy_val*dx;
			}
		}
		Int = Int/(discretizationSamplingRate);

		if(t.isClockwise()){
			return Int;
		}else{
			return -Int;
		}
	}

	// ============================================================================
	// GETTERS AND SETTERS

	public void setInitialKiteCenter(Point2D.Double center){ initialCenter_ = center; }
	public Point2D.Double getInitialKiteCenter(){ return initialCenter_; }

	public void setMaxNumIters(int maxNumIters) { maxNumIters_ = maxNumIters; }
	public int getMaxNumIters() { return maxNumIters_; }

	public boolean isImmortal() { return immortal_; }
	public void setImmortal(boolean immortal) { immortal_ = immortal; }
}