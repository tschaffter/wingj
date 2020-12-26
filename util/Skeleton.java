/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://wingj.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package detection;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Vector;

import utilities.Filters;
import utilities.ImageLabeler;
import core.ImagePlusManager;
import core.WJSettings;

/**
 * Computes the skeleton of the wing pouch structure.
 *
 * @version October 14, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 */
public class Skeleton extends Detection {

	/** Maximum length of the pruning. */
	private static int PRUNNINGLENGTH = 100;
	/** Minimum number of 8-connected neighbors for a skeleton point to be considered a fork point. */
	private static int MINNGEIGHBORSFORK = 3;

	/** Remove fork points in the margins */
	private static boolean CENTER_POINTS_REMOVAL_MARGINS = true;
	/** Remove fork points with area < minArea */
	private static boolean CENTER_POINTS_REMOVAL_MIN_AREA = true;
	/** Remove fork points with invalid major/minor ratio */
	private static boolean CENTER_POINTS_REMOVAL_MAJOR_MINOR_RATIO = true;
	/** Remove fork points with invalid mutual ratio */
	private static boolean CENTER_POINTS_REMOVAL_MUTUAL_RATIO = true;

	/** Container for the skeleton. */
	private ByteProcessor skeleton_ = null;
	/** Container for the labeled skeleton. */
	private FloatProcessor labeledSkeleton_ = null;
	/** Number of 4-connected components enclosed by the skeleton. */
	private int nRegions_ = 0;
	/** Points of the skeleton with at least three 8-connected neighbors. */
	private Vector<Point> forkPoints_ = null;
	/** Informs of the fork points that passed the removal tests. */
	boolean[] isValidForkPoint_ = null;
	/** Informs of the connected components that passed the removal tests. */
	boolean[] isValidCC_ = null;

	/** Index of the fork points that will be used as a center of the kite-snake. */
	private int kiteCenterIndex_ = -1;
	/** Half size of the bounding box of the successful center. */
	private int acceptedSearchBoxHalfSize_ = -1;

	/** Auxiliary class to label the 4-connected regions determined by the skeleton. */
	private ImageLabeler imageLabeler_ = null;

	/** If true, the algorithm tries to decrease the value of the search box when computing the intersection with the skeleton. */
	private boolean shrinkingBoxStrategy_ = false;

	//TODO move to the settings class
	private int centerDetectionMode = 0;

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public Skeleton() {

		super();
	}

   	// ----------------------------------------------------------------------------

	/** Constructor. */
	public Skeleton(String name, WPouchDetector detector, boolean hidden) {

		super(name, detector, hidden);
		description_ = "Computing wing structure skeleton";
	}

	// ----------------------------------------------------------------------------

	/** Constructor */
	public Skeleton(String name, WPouchDetector detector) {

		super(name, detector);
		description_ = "Computing wing structure skeleton";
	}

	// ----------------------------------------------------------------------------

	/** Computes the skeleton of the wing pouch structure. */
	public void run() throws Exception {

		ImagePlusManager manager = ImagePlusManager.getInstance();

		// skeletonize
		FloatProcessor skeleton = skeletonize((FloatProcessor) (detector_.ppImage_.getProcessor().duplicate()), false, 0.);
		detector_.skeletonImage_ = (ByteProcessor) skeleton.convertToByte(false);

		// prune (modify skeleton_)
		prune(skeleton_);

		ImagePlus skeletonImage = new ImagePlus("detection_structure_skeleton", skeleton_);
		manager.add(skeletonImage.getTitle(), skeletonImage); // do not show (instead show labeled cc)

		if(centerDetectionMode==0){
			//OLD MODE

			detector_.kiteInitialPoints_ = null;
		}else if(centerDetectionMode==1){
			//NEW MODE

			// compute fork points
			searchAllForkPoints();
			// compute label map
			labelSkeleton();
			// build initialization for the KiteSnake
			detector_.kiteInitialPoints_ = computeKiteInitialization();
			ImagePlus labeledSkeleton = generateConnectedComponentImage();
	   		manager.add("detection_structure_skeleton_cc", labeledSkeleton, detector_.isInteractive() && !hidden_);
		}
	}

	// ----------------------------------------------------------------------------

   	/** Set the visibility of all images generated during the detection process, if they exist. */
	@Override
	public void setImagesVisible(boolean visible) {

		ImagePlusManager manager = ImagePlusManager.getInstance();

		if (visible) {
			manager.show("detection_structure_skeleton");
			manager.show("detection_structure_skeleton_cc");
		}
		else {
			manager.hide("detection_structure_skeleton");
			manager.hide("detection_structure_skeleton_cc");
		}
	}

	// ----------------------------------------------------------------------------

	/** Remove all images generated during the detection process, if they exist. */
	public void removeImages() {

		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.remove("detection_structure_skeleton");
		manager.remove("detection_structure_skeleton_cc");
	}

	// ----------------------------------------------------------------------------

	/** Skeletonize the given FloatProcessor which must be binary */
	public static FloatProcessor skeletonize(FloatProcessor binaryImage, boolean prune, double dilation) throws Exception {

		// build the binary skeleton
		ByteProcessor skeletonBp = (ByteProcessor) binaryImage.convertToByte(false);
		skeletonBp.invertLut();
		skeletonBp.skeletonize();
		skeletonBp.invertLut();

		// prune the skeleton if required
		if (prune)
			prune(skeletonBp);

		FloatProcessor skeletonFp = (FloatProcessor) skeletonBp.convertToFloat();

		// dilation if required
		if (dilation > 0)
			Filters.applyMAXFilter(skeletonFp, (int)Math.round(2*dilation));

		return skeletonFp;
	}

	// ----------------------------------------------------------------------------

//	/** Skeletonize a binary image (without pruning) */
//	public ImagePlus skeletonize(FloatProcessor binaryImage) throws Exception {
//
//		// build the binary skeleton
//		ByteProcessor skeletonProcessor = (ByteProcessor) binaryImage.convertToByte(false);
//		skeletonProcessor.invertLut();
//		skeletonProcessor.skeletonize();
//		skeletonProcessor.invertLut();
//
//		// save here (before pruning)
//		skeleton_ = skeletonProcessor;
//
//		ImagePlus skeletonImage = new ImagePlus("skeleton", skeleton_);
//		return skeletonImage;
//	}

	// ----------------------------------------------------------------------------

	/** Prune the given skeleton to remove undesired branches.  */
	public static void prune(ByteProcessor skeleton) throws Exception {

		if(skeleton == null)
   			throw new Exception("ERROR: skeleton is null.");

   		int width = skeleton.getWidth();
   		int height = skeleton.getHeight();

   		byte[] skeletonPixels = (byte[])skeleton.getPixels();
		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if (skeletonPixels[i+j*width] != 0) {
					isEndPoint(skeleton, i, j, -1, -1, 0);
				}
			}
		}
		// clear points that are 4-connected to exactly three points
		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if (skeletonPixels[i+j*width] != 0) {
					int count = 0;
					if (skeletonPixels[(i-1)+(j)*width] != 0) count++;
					if (skeletonPixels[(i+1)+(j)*width] != 0) count++;
					if (skeletonPixels[(i)+(j-1)*width] != 0) count++;
					if (skeletonPixels[(i)+(j+1)*width] != 0) count++;
					if(count==3) skeletonPixels[i+j*width] = 0;
				}
			}
		}

		// clear points that are "removable" (Prop 1 from P. Dimitrov, C.Philipps, and K.Siddiqi "Robust and Efficient Skeletal Graphs")
   		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if ((skeletonPixels[i+j*width]&0xFF) != 0) {
					byte val = skeletonPixels[i+j*width];
					skeletonPixels[i+j*width] = 0;
					//vertices
					int vertices = 0;
					for(int k = i-1; k <= i+1; k++){
						for(int l = j-1; l <= j+1; l++){
							if ((skeletonPixels[k+l*width]&0xFF) != 0) {
								vertices++;
							}
						}
					}
					//edges
					int edges = 0;
					for(int k = i-1; k <= i+1; k++){
						for(int l = j-1; l <= j+1; l++){
							if ((skeletonPixels[k+l*width]&0xFF) != 0) {
								for(int kk = Math.max(k-1, i-1); kk <= Math.min(k+1, i+1); kk++){
									for(int ll = Math.max(l-1, j-1); ll <= Math.min(l+1, j+1); ll++){
										if(!(k==kk && l==ll)){
											if ((skeletonPixels[kk+ll*width]&0xFF) != 0) {
												edges++;
											}
										}
									}
								}
							}
						}
					}
					if(vertices-edges/2 != 1){
						skeletonPixels[i+j*width] = val;
					}else{
						boolean endPoint = false;

						if(vertices == 1){
							// has exactly ONE 8-connected neighbor
							endPoint = true;
						}else if (vertices == 2){
							// has exactly TWO neighbor which are 4-adjacent
							int adj4 = 0;
							for(int k = i-1; k <= i+1; k++){
								for(int l = j-1; l <= j+1; l++){
									if ((skeletonPixels[k+l*width]&0xFF) != 0) {
										for(int kk = Math.max(k-1, i-1); kk <= Math.min(k+1, i+1); kk++){
											for(int ll = Math.max(l-1, j-1); ll <= Math.min(l+1, j+1); ll++){
												if(!(k==kk && l==ll)){
													if ((skeletonPixels[kk+ll*width]&0xFF) != 0) {
														if((kk-k)*(kk-k)+(ll-l)*(ll-l)<2){
															adj4++;
														}
													}
												}
											}
										}
									}
								}
							}
							if(adj4!=0){
								endPoint = true;
							}
						}else{
							endPoint = false;
						}
						//if it is NOT an end-point, remove
						if(endPoint){
							skeletonPixels[i+j*width] = val;
						}
					}
				}
			}
		}
	}

   	// ============================================================================

   	/** Labels all 8-connected components of the skeleton. */
	public void labelSkeleton() throws Exception{

		if(skeleton_==null)
			throw new Exception("The binary skeleton is not well defined.");

		ByteProcessor invertedSkeleton = (ByteProcessor) skeleton_.duplicate();
		invertedSkeleton.invert();

		imageLabeler_ = new ImageLabeler(invertedSkeleton);
		imageLabeler_.label4ConnectedComponents();

		labeledSkeleton_ = imageLabeler_.getLabeledMap();
		nRegions_ = imageLabeler_.getNLabels();
	}

	// ============================================================================

	/** Computes the points used by the KiteSnake as a initialization. */
	public Point2D.Double[] computeKiteInitialization() throws Exception{

		if (forkPoints_ == null)
			throw new Exception("forkPoints_ is null.");

		WJSettings settings = WJSettings.getInstance();

		int defaultSearchBoxHalfSize = settings.getSearchSquareHalfSize();

		discardUnvalidForkPointsAndCC();
		if (isValidForkPoint_ == null || isValidCC_ == null)
			return null;

		int nForkPoints = isValidForkPoint_.length;
		//now we check for 4 cc
		for(int i=0; i<nForkPoints; i++){
			if(isValidForkPoint_[i]){
				Vector<Point> kitePoints = computeBoxedSkeletonIntersection(forkPoints_.elementAt(i), defaultSearchBoxHalfSize);

				if(kitePoints.size()==4){
					int differentValidCC = countSurroundingValidCC(kitePoints);
					if(differentValidCC==4){ // best case
						kiteCenterIndex_ = i;
						acceptedSearchBoxHalfSize_ = defaultSearchBoxHalfSize;
						return computeKitePointsFromRhombus(forkPoints_.elementAt(i), kitePoints);
					}
				}else if(kitePoints.size()>4 && shrinkingBoxStrategy_){
					kitePoints = computeBoxedSkeletonIntersection(forkPoints_.elementAt(i), (int)Math.round(defaultSearchBoxHalfSize/2.0));
					if(kitePoints.size()==4){
						int differentValidCC = countSurroundingValidCC(kitePoints);
						if(differentValidCC==4){ // best case
							kiteCenterIndex_ = i;
							acceptedSearchBoxHalfSize_ = (int)Math.round(defaultSearchBoxHalfSize/2.0);
							return computeKitePointsFromRhombus(forkPoints_.elementAt(i), kitePoints);
						}
					}
				}
			}
		}
		//now we check for 3 cc
		for(int i=0; i<nForkPoints; i++){
			if(isValidForkPoint_[i]){
				Vector<Point> kitePoints = computeBoxedSkeletonIntersection(forkPoints_.elementAt(i), defaultSearchBoxHalfSize);

				if(kitePoints.size()==4){
					int differentValidCC = countSurroundingValidCC(kitePoints);
					if(differentValidCC==3){ // best case
						kiteCenterIndex_ = i;
						acceptedSearchBoxHalfSize_ = defaultSearchBoxHalfSize;
						return computeKitePointsFromRhombus(forkPoints_.elementAt(i), kitePoints);
					}
				}else if(kitePoints.size()>4 && shrinkingBoxStrategy_){
					kitePoints = computeBoxedSkeletonIntersection(forkPoints_.elementAt(i), (int)Math.round(defaultSearchBoxHalfSize/2.0));
					if(kitePoints.size()==4){
						int differentValidCC = countSurroundingValidCC(kitePoints);
						if(differentValidCC==3){ // best case
							kiteCenterIndex_ = i;
							acceptedSearchBoxHalfSize_ = (int)Math.round(defaultSearchBoxHalfSize/2.0);
							return computeKitePointsFromRhombus(forkPoints_.elementAt(i), kitePoints);
						}
					}
				}
			}
		}
		return null;
	}

   	// ============================================================================

   	/** Looks for all points within the skeleton that have at least MINNGEIGHBORSFORK 8-connected neighbors. */
   	public void searchAllForkPoints() throws Exception{

		if (skeleton_ == null)
			throw new Exception("ERROR: skeleton_ is null.");

   		int width = skeleton_.getWidth();
   		int height = skeleton_.getHeight();

   		ByteProcessor invertedSkeleton = (ByteProcessor) skeleton_.duplicate();
   		invertedSkeleton.invert();
   		byte[] skeletonPixels = (byte[])skeleton_.getPixels();

   		forkPoints_ = new Vector<Point>();

		for (int i = 1; i < width-1; i++) {
			for (int j = 1; j < height-1; j++) {
				if ((skeletonPixels[i+j*width]&0xFF) != 0) {
					int nNeighbors = 0;
					for(int k = i-1; k < i+2; k++){
						for(int l = j-1; l < j+2; l++){
							if ((skeletonPixels[k+l*width]&0xFF) != 0) {
								nNeighbors++;
							}
						}
					}
					if (nNeighbors-1 >= MINNGEIGHBORSFORK) {
						forkPoints_.addElement(new Point(i,j));
					}
				}
			}
		}
   	}

	// ============================================================================
	// PRIVATE METHODS

	/** Computes the intersection of the skeleton with of a box centered in a given point. */
	private Vector<Point> computeBoxedSkeletonIntersection(Point p, int searchSquareHalfSize) throws Exception {

   		if (skeleton_ == null)
   			throw new Exception("skeleton_ is null.");

		byte[] skeletonPixels = (byte[])skeleton_.getPixels();
		int width = skeleton_.getWidth();
		int height = skeleton_.getHeight();

		int xMin = p.x-searchSquareHalfSize;
		int yMin = p.y-searchSquareHalfSize;
		int xMax = p.x+searchSquareHalfSize;
		int yMax = p.y+searchSquareHalfSize;

		Vector<Point> skletonIntersections = new Vector<Point>();

		if(xMin>=0 && yMin>=0 && xMax<=width-1 && yMax<height-1){
			int x,y;
			x = xMin;
			for(y=yMin; y<yMax; y++){
				if((skeletonPixels[x+width*y]&0xFF)!=0){
					boolean isTooClose = false;
					for(int k=0; k<skletonIntersections.size() && !isTooClose; k++){
						if(skletonIntersections.elementAt(k).distance(x, y)<searchSquareHalfSize/10.0){
							isTooClose = true;
						}
					}
					if(!isTooClose){
						skletonIntersections.addElement(new Point(x,y));
					}
				}
			}
			y = yMax;
			for(x=xMin; x<xMax; x++){
				if((skeletonPixels[x+width*y]&0xFF)!=0){
					boolean isTooClose = false;
					for(int k=0; k<skletonIntersections.size() && !isTooClose; k++){
						if(skletonIntersections.elementAt(k).distance(x, y)<searchSquareHalfSize/10.0){
							isTooClose = true;
						}
					}
					if(!isTooClose){
						skletonIntersections.addElement(new Point(x,y));
					}
				}
			}
			x = xMax;
			for(y=yMax; y>yMin; y--){
				if((skeletonPixels[x+width*y]&0xFF)!=0){
					boolean isTooClose = false;
					for(int k=0; k<skletonIntersections.size() && !isTooClose; k++){
						if(skletonIntersections.elementAt(k).distance(x, y)<searchSquareHalfSize/10.0){
							isTooClose = true;
						}
					}
					if(!isTooClose){
						skletonIntersections.addElement(new Point(x,y));
					}
				}
			}
			y = yMin;
			for(x=xMax; x>xMin; x--){
				if((skeletonPixels[x+width*y]&0xFF)!=0){
					boolean isTooClose = false;
					for(int k=0; k<skletonIntersections.size() && !isTooClose; k++){
						if(skletonIntersections.elementAt(k).distance(x, y)<searchSquareHalfSize/10.0){
							isTooClose = true;
						}
					}
					if(!isTooClose){
						skletonIntersections.addElement(new Point(x,y));
					}
				}
			}
		}
		return skletonIntersections;
	}

	// ============================================================================

   	/** Compute the KiteSnake defining nodes from a rhombus and a central point. */
	private Point2D.Double[] computeKitePointsFromRhombus(Point center, Vector<Point> rhombus) throws Exception{

		if (center == null)
			throw new Exception("center is null.");
		if (rhombus == null)
			throw new Exception("rhombus is null.");

		WJSettings settings = WJSettings.getInstance();

		double snakeLength = 10*settings.getPpBlur();

		Point2D.Double[] kiteNodes = new Point2D.Double[5];
		for(int l=0; l<4; l++){
			double length = center.distance(rhombus.elementAt(l));
			double dx = (rhombus.elementAt(l).x-center.x)/length;
			double dy = (rhombus.elementAt(l).y-center.y)/length;
			kiteNodes[l] = new Point2D.Double(center.x + snakeLength*dx, center.y + snakeLength*dy);
		}
		kiteNodes[4] = new Point2D.Double(center.x,center.y);
		return kiteNodes;
	}

   	// ============================================================================

	/** Counts the number of different valid connected components that lay in the center of the edged of the a rhombus. */
	private int countSurroundingValidCC (Vector<Point> rhombusVertices) throws Exception{

		if (labeledSkeleton_ == null)
			throw new Exception("labeledSkeleton_ is null.");
		if	(rhombusVertices == null)
			throw new Exception("kitePoints is null.");

		float[] labeledSkeletonPixels = (float[])labeledSkeleton_.getPixels();
		int width = labeledSkeleton_.getWidth();

		boolean[] sampledCC = new boolean[nRegions_];
		for(int i=0; i<rhombusVertices.size(); i++){
			int samplingX = (int)Math.round((rhombusVertices.elementAt(i).x+rhombusVertices.elementAt((i+1)%4).x)/2.0);
			int samplingY = (int)Math.round((rhombusVertices.elementAt(i).y+rhombusVertices.elementAt((i+1)%4).y)/2.0);
			sampledCC[(int)labeledSkeletonPixels[samplingX+width*samplingY]] = true;
		}

		int differentValidCC = 0;
		for(int i=0; i<nRegions_; i++){
			if(sampledCC[i] && isValidCC_[i]){
				differentValidCC++;
			}
		}
		return differentValidCC;
	}

	// ============================================================================

	/** Return an ImagePlus object representing the labeled connected components. */
	private ImagePlus generateConnectedComponentImage() throws Exception {

		if (forkPoints_ == null)
			throw new Exception("ERROR: forks_ is null.");
		if (labeledSkeleton_ == null)
			throw new Exception("ERROR: labeledSkeleton_ is null.");
		if (skeleton_ == null)
			throw new Exception("ERROR: skeleton_ is null.");

		WJSettings settings = WJSettings.getInstance();

		double centerMargin = settings.getWPouchCenterMargin();

		int width = labeledSkeleton_.getWidth();
		int height = labeledSkeleton_.getHeight();

		ColorProcessor cp = new ColorProcessor(width, height);

		float[] labeledSkeletonPixels = (float[])labeledSkeleton_.getPixels();
		byte[] skeletonPixels = (byte[])skeleton_.getPixels();

		if(isValidForkPoint_ == null || isValidCC_ == null)
			discardUnvalidForkPointsAndCC();

		// draw labeled connected  components
		if(isValidCC_ != null){
			cp.setColor(Color.RED);
			for(int x=0; x<width; x++){
				for(int y=0; y<height; y++){
					double regionNumber = labeledSkeletonPixels[x+width*y];
					if(isValidCC_[(int)regionNumber]){
						cp.putPixelValue(x, y, (int)Math.round((regionNumber)*255.0/nRegions_));
					}else{
						cp.drawDot(x, y);
					}
				}
			}
		}else{
			for(int x=0; x<width; x++){
				for(int y=0; y<height; y++){
					cp.putPixelValue(x, y, (int)Math.round(labeledSkeletonPixels[x+width*y]*255.0/nRegions_));
				}
			}
		}

		// draw skeleton with (255,255,255)
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				if((skeletonPixels[x+width*y]&0xFF) != 0){
					cp.putPixelValue(x, y, 255);
				}
			}
		}

		// draw gating area
   		int xMin = (int)Math.floor(centerMargin*width);
   		int xMax = (int)Math.ceil((1.0-centerMargin)*width);
   		int yMin = (int)Math.floor(centerMargin*height);
   		int yMax = (int)Math.ceil((1.0-centerMargin)*height);

   		cp.setColor(Color.BLUE);
		cp.drawLine(xMin, yMin, xMin, yMax);
		cp.drawLine(xMin, yMax, xMax, yMax);
		cp.drawLine(xMax, yMax, xMax, yMin);
		cp.drawLine(xMax, yMin, xMin, yMin);

		int searchSquareHalfSize = settings.getSearchSquareHalfSize();

		// draw all fork points
   		int nForkPoints = forkPoints_.size();
   		cp.setColor(Color.RED);
   		for(int i=0; i<nForkPoints; i++){
			cp.drawDot(forkPoints_.elementAt(i).x, forkPoints_.elementAt(i).y);
		}

   		// draw valid fork points
		if(isValidForkPoint_ != null){
			cp.setColor(Color.YELLOW);
			for(int i=0; i<nForkPoints; i++){
				if(isValidForkPoint_[i]) {
					Point fork = forkPoints_.elementAt(i);
					if(i!=kiteCenterIndex_){
						cp.drawRect(fork.x-searchSquareHalfSize, fork.y-searchSquareHalfSize, 2*searchSquareHalfSize+1, 2*searchSquareHalfSize+1);
						cp.drawDot(fork.x, fork.y);
					}
				}
			}
		}

   		// draw winning fork point
		if(isValidForkPoint_ != null){
			if(kiteCenterIndex_ != -1){
		   		cp.setColor(Color.GREEN);
				Point fork = forkPoints_.elementAt(kiteCenterIndex_);
				cp.drawRect(fork.x-acceptedSearchBoxHalfSize_, fork.y-acceptedSearchBoxHalfSize_, 2*acceptedSearchBoxHalfSize_+1, 2*acceptedSearchBoxHalfSize_+1);
				cp.drawRect(fork.x-acceptedSearchBoxHalfSize_-1, fork.y-acceptedSearchBoxHalfSize_-1, 2*acceptedSearchBoxHalfSize_+3, 2*acceptedSearchBoxHalfSize_+3);
				cp.drawRect(fork.x-acceptedSearchBoxHalfSize_+1, fork.y-acceptedSearchBoxHalfSize_+1, 2*acceptedSearchBoxHalfSize_-1, 2*acceptedSearchBoxHalfSize_-1);
				cp.drawDot(fork.x, fork.y);
			}
		}

		cp.resetMinAndMax();
		return new ImagePlus("detection_structure_skeleton_cc", cp);
	}

	// ----------------------------------------------------------------------------

	/** Determine the validity of the fork points based on their position with respect to the image boundary. */
	private void discardUnvalidForkPointsByLocation() throws Exception {

		if (skeleton_ == null)
			throw new Exception("ERROR: skeleton_ is null.");
		if (forkPoints_ == null)
			throw new Exception("ERROR: forkPoints_ is null.");
		if (isValidForkPoint_ == null)
			throw new Exception("ERROR: isValidForkPoint_ is null.");

   		WJSettings settings = WJSettings.getInstance();

   		double centerMargin = settings.getWPouchCenterMargin();

		int width = skeleton_.getWidth();
   		int height = skeleton_.getHeight();

   		int xMin = (int)Math.floor(centerMargin*width);
   		int xMax = (int)Math.ceil((1.0-centerMargin)*width);
   		int yMin = (int)Math.floor(centerMargin*height);
   		int yMax = (int)Math.ceil((1.0-centerMargin)*height);

		int nForkPoints = forkPoints_.size();
		for(int i=0; i<nForkPoints; i++){
			if(isValidForkPoint_[i]){
				int x = forkPoints_.elementAt(i).x;
				int y = forkPoints_.elementAt(i).y;
				if(x<xMin || y<yMin || x>xMax || y>yMax){
					isValidForkPoint_[i] = false;
				}
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Determines the validity of the connected components of the labeled skeleton image and the fork points. */
	private void discardUnvalidForkPointsAndCC() throws Exception {

		if (forkPoints_ == null)
			throw new Exception("ERROR: forkPoints_ is null.");

		int nForkPoints = forkPoints_.size();
		isValidForkPoint_ = new boolean[nForkPoints];
		isValidCC_ = new boolean[nRegions_];
		for(int i=0; i<nForkPoints; i++){
			isValidForkPoint_[i] = true;
		}
		for(int i=0; i<nRegions_; i++){
			isValidCC_[i] = true;
		}

		if (CENTER_POINTS_REMOVAL_MARGINS) discardUnvalidForkPointsByLocation();
		if (CENTER_POINTS_REMOVAL_MIN_AREA) discardUnvalidCCBySize();
		if (CENTER_POINTS_REMOVAL_MAJOR_MINOR_RATIO) discardUnvalidCCByAspectRatio();
		if (CENTER_POINTS_REMOVAL_MUTUAL_RATIO) discardUnvalidForkPointsByMutualInterface();
	}

	// ----------------------------------------------------------------------------

	/** Determine the validity of the fork points based on the boundaries of the neighboring connected components. */
	private void discardUnvalidForkPointsByMutualInterface() throws Exception {

		if (forkPoints_ == null)
			throw new Exception("ERROR: forkPoints_ is null.");
		if (imageLabeler_ == null)
			throw new Exception("ERROR: imageLabeler_ is null.");
		if (labeledSkeleton_ == null)
			throw new Exception("ERROR: labeledSkeleton_ is null.");
		if (isValidForkPoint_ == null)
			throw new Exception("ERROR: isValidForkPoint_ is null.");

   		WJSettings settings = WJSettings.getInstance();

   		double lowerRatioBoundary = settings.getCcMutualRatioLowerBoundary();
		double upperRatioBoundary = settings.getCcMutualRatioUpperBoundary();

		float[] labeledSkeletonPixels = (float[])labeledSkeleton_.getPixels();
		int[] regionPerimeters = imageLabeler_.getPerimeters();
		int[][] interfaceLength = imageLabeler_.getRegionInterfaceLengths();
		boolean[] isUnbondedRegion = imageLabeler_.getIsUnbounded();
		int width = labeledSkeleton_.getWidth();

		boolean[] isConnected = new boolean[nRegions_];

   		int nForkPoints = forkPoints_.size();
		for(int i=0; i<nForkPoints; i++){
			if(isValidForkPoint_[i]){
				Point forkPoint = forkPoints_.elementAt(i);
				// list region index around a 8-connected neighborhood
				for(int ii=forkPoint.x-1; ii<=forkPoint.x+1; ii++){
					for(int jj=forkPoint.y-1; jj<=forkPoint.y+1; jj++){
						isConnected[(int)labeledSkeletonPixels[ii+width*jj]] = true;
					}
				}
				// take one cc
				// k1=0 is the skeleton
				boolean isValid = false;
				for(int k1=1; k1<nRegions_; k1++) {
					if(!isUnbondedRegion[k1]) {
						if(isConnected[k1]) {
							// to create all possible pairs of two cc
							for(int k2=k1+1; k2<isConnected.length; k2++) {
								if(!isUnbondedRegion[k2]){
									if(isConnected[k2]){
										double ratio1 = (double)interfaceLength[k1][k2]/(double)regionPerimeters[k1];
										double ratio2 = (double)interfaceLength[k1][k2]/(double)regionPerimeters[k2];

										if(ratio1>lowerRatioBoundary && ratio1<upperRatioBoundary &&
												ratio2>lowerRatioBoundary && ratio2<upperRatioBoundary){
											isValid = true;
										}
									}
								}
							}
						}
					}
					isConnected[k1] = false;
				}
				isValidForkPoint_[i] = isValid;
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Determine the validity of the connected components based on their aspect ratio. */
	private void discardUnvalidCCByAspectRatio() throws Exception {

		if (imageLabeler_ == null)
			throw new Exception("ERROR: imageLabeler_ is null.");
		if (isValidCC_ == null)
			throw new Exception("ERROR: isValidCC_ is null.");

   		WJSettings settings = WJSettings.getInstance();

   		double minAspectRatio = 1.0/settings.getCcMajorMinorUpperBoundary();
   		double maxAspectRatio = 1.0/settings.getCcMajorMinorLowerBoundary();

		double[] ellipseMajorAxis = imageLabeler_.getEllipseMajorAxis();
		double[] ellipseMinorAxis = imageLabeler_.getEllipseMinorAxis();

		for(int i=0; i<nRegions_; i++){
			if(isValidCC_[i]){
				double aspectRatio = ellipseMinorAxis[i]/ellipseMajorAxis[i];
				if(aspectRatio<minAspectRatio || aspectRatio>maxAspectRatio){
					isValidCC_[i] = false;
				}
			}
		}
	}

	// ----------------------------------------------------------------------------

	/** Determine the validity of the connected components based on their area. */
	private void discardUnvalidCCBySize() throws Exception {

		if (imageLabeler_ == null)
			throw new Exception("ERROR: imageLabeler_ is null.");
		if (isValidCC_ == null)
			throw new Exception("ERROR: isValidCC_ is null.");

   		WJSettings settings = WJSettings.getInstance();

   		int minArea = settings.getCcMinArea();

		int[] regionAreas = imageLabeler_.getAreas();

		for(int i=0; i<nRegions_; i++){
			if(isValidCC_[i]){
				if(regionAreas[i]<minArea){
					isValidCC_[i] = false;
				}
			}
		}
	}

   	// ============================================================================

	/** Checks if the pixel with coordinates Point of a skeleton is an end-point */
   	private static boolean isEndPoint(ByteProcessor skeleton, int x, int y, int xOld, int yOld, int depth) throws Exception {

   		if(skeleton == null)
   			throw new Exception("ERROR: skeleton is null.");

		if(depth < Skeleton.PRUNNINGLENGTH){
	   		int width = skeleton.getWidth();
	   		byte[] skeletonPixels = (byte[])skeleton.getPixels();
	   		// checking neighborhood
	   		int ii = -1;
	   		int jj = -1;
	   		int count = 0;
	   		for (int k = x-1; k <= x+1; k++) {
	   			for (int l = y-1; l <= y+1; l++) {
	   				if ((skeletonPixels[k+l*width]) != 0) {
	   					if(!(k==x && l==y)){
			   				if (!(k==xOld && l==yOld)) {
			   					count++;
			   					ii = k;
			   					jj = l;
			   				}
	   					}
	   				}
	   			}
	   		}
	   		if(count == 0){
	   			skeletonPixels[x+y*width] = 0;
	   			return true;
	   		}else if(count == 1){
	   			depth++;
	   			if(isEndPoint(skeleton, ii, jj, x, y, depth)){
	   				skeletonPixels[x+y*width] = 0;
		   			return true;
	   			}else{
	   				return false;
	   			}
	   		}else if(count > 1){
	   			return true;
	   		}
		}else{
	   		return false;
		}
		return false;
   	}

	// ============================================================================
	// SETTERS AND GETTERS

	public ImagePlus getSkeletonImage() { return new ImagePlus("skeleton", skeleton_); }
	public boolean getShrinkingBoxStrategy() { return shrinkingBoxStrategy_; }
}
