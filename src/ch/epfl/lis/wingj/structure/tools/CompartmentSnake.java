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

import ij.process.FloatProcessor;

import java.awt.Color;
import java.awt.geom.Point2D;

import ch.epfl.lis.wingj.structure.geometry.Segment;
import ch.epfl.lis.wingj.utilities.Filters;

import Jama.Matrix;
import big.ij.snake2D.Snake2D;
import big.ij.snake2D.Snake2DNode;
import big.ij.snake2D.Snake2DScale;

/**
 * Contour spline snake to identify compartments delimited by fluorescence expression.
 * <p>
 * Using a penalty on the shape of the snake, even compartments that have a "hole" in their
 * contour (i.e., missing fluorescence) can be identified without that the snake leak out of
 * the compartment.
 * 
 * @version March 5, 2013
 * 
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class CompartmentSnake implements Snake2D {

	/** Snake defining control points. */
	private Snake2DNode[] coef_ = null;

	// ----------------------------------------------------------------------------
	// SNAKE CONTOUR

	/** Samples of the x coordinates of the snake contour. */
	private double[] xSnakeContour_ = null;
	/** Samples of the y coordinates of the snake contour. */
	private double[] ySnakeContour_ = null;

	/** Samples of the x coordinates of the derivative of the snake contour. */
	private double[] xSnakeTangentVector_ = null;

	/** Horizontal inferior limit of the bounding box of the snake contour. */
	private int xMinSnakeContour_ = 0;
	/** Horizontal superior limit of the bounding box of the snake contour. */
	private int xMaxSnakeContour_ = 0;
	/** Vertical inferior limit of the bounding box of the snake contour. */
	private int yMinSnakeContour_ = 0;
	/** Vertical superior limit of the bounding box of the snake contour. */
	private int yMaxSnakeContour_ = 0;
	
	// ----------------------------------------------------------------------------
	// SNAKE STATUS FIELDS

	/** Signed area of the region enclosed by the snake contour. */
	private double snakeArea_ = 0;
	/**
	 * If <code>true</code>, indicates that the user chose to interactively abort
	 * the processing of the snake. Otherwise, if <code>false</code>, indicates
	 * that the dealings with the snake were terminated without user assistance.
	 */
	private boolean canceledByUser_ = false;

	// ----------------------------------------------------------------------------
	// SNAKE OPTION FIELDS

	/** Number of spline vector coefficients. */
	private int M_ = 0;
	/** Energy trade-off factor. */
	private double lambda_ = 0;
	/** Sampling rate at which the contours are discretized. */
	private int discretizationSamplingRate_ = 0;
	
	// ----------------------------------------------------------------------------
	// SPLINE LUTS

	/** Length of the support the basis function. */
	static final private int N = BSplinesUtils.ESPLINE4SUPPORT;
	/** LUT with the samples of the B-spline basis function. */
	private double[] bSplineLUT_ = null;
	/** LUT with the samples of the derivative of the basis function. */
	private double[] bSplineDerivativeLUT_ = null;
	/** LUT with the samples of the autocorrelation function. */
	private double[] bSplineAutocorrelationLUT_ = null;

	// ----------------------------------------------------------------------------
	// IMAGE FIELDS
	
	/** Original image data filtered with a Laplacian filter. */
	private float[] filteredImage_ = null;
	/**
	 * Pre-integrated and filtered (with a Laplacian filter) image data along the
	 * vertical direction.
	 */
	private float[] preintegratedFilteredImage_ = null;

	/** Width of the original image data. */
	private int imageWidth_ = 0;
	/** Height of the original image data. */
	private int imageHeight_ = 0;
	/** Width of the original image data minus two. */
	private int imageWidthMinusTwo_ = 0;
	/** Height of the original image data minus two. */
	private int imageHeightMinusTwo_ = 0;

	// ----------------------------------------------------------------------------
	// PRIOR-SHAPE PROJECTOR FIELDS

	/** Projector to the circular-shape space. */
	private Matrix priorShapeProjectionMatrix_ = null;
	/** Complementary orthogonal projector to the circular-shape space. */
	private Matrix priorShapeOrthoProjectorMatrix_ = null;
	/** LUT of the coefficient values. */
	private double[][] vcArray_ = null;

	// ----------------------------------------------------------------------------
	// AUXILIARY FIELDS

	/** PI/M. **/
	private double PIM_ = 0;
	/** 2*PI/M. */
	private double PI2M_ = 0;
	/** N*R. */
	private int NR_ = 0;
	/** M*R. */
	private int MR_ = 0;

	/** Laplacian filter kennel. */
	private static int LAPLACIAN_KERNEL [] = { 0,-1, 0, -1, 4,-1, 0,-1, 0};

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public CompartmentSnake(FloatProcessor dilatedSkeleton, FloatProcessor mip, int M, int R, double sigma, double lambda, double alpha, Point2D.Double center, double radius){
		
		if (dilatedSkeleton == null) {
			System.err.println("Skeleton image not properly loaded.");
			return;
		}
		if (mip == null) {
			System.err.println("MIP Image not properly loaded.");
			return;
		}
		
		M_ = M;
		if (M_ < 4) {
			System.err
					.println("The minimum number of knots for this basis function is four.");
			return;
		}
	
		discretizationSamplingRate_ = R;
		NR_ = N * discretizationSamplingRate_;
		MR_ = M * discretizationSamplingRate_;
		PIM_ = Math.PI / M_;
		PI2M_ = 2 * PIM_;
		
		lambda_ = lambda;
		
		imageWidth_ = dilatedSkeleton.getWidth();
		imageHeight_ = dilatedSkeleton.getHeight();
		imageWidthMinusTwo_ = imageWidth_-2;
		imageHeightMinusTwo_ = imageHeight_-2;
		int size = imageWidth_*imageHeight_;

		float[] dilatedSkeletonData = (float[])dilatedSkeleton.getPixels();
		float[] mipData = (float[])mip.getPixels();
		double[] combinedImageData = new double[size];

		for(int i=0; i<size; i++){
			combinedImageData[i] = (1-alpha)*dilatedSkeletonData[i]+alpha*mipData[i];
		}

		FloatProcessor laplacianProcessor = new FloatProcessor(imageWidth_, imageHeight_, combinedImageData);
		laplacianProcessor.invert();
		Filters.applyGaussianFilter(laplacianProcessor, sigma);

		laplacianProcessor.convolve3x3(LAPLACIAN_KERNEL);
		laplacianProcessor.resetMinAndMax();
		laplacianProcessor.multiply(255.0 / Math.max(laplacianProcessor.getMax(), Math.abs(laplacianProcessor.getMin())));

		filteredImage_ = (float[])laplacianProcessor.getPixels();
		xSnakeContour_ = new double[MR_];
		ySnakeContour_ = new double[MR_];
		xSnakeTangentVector_ = new double[MR_];
		buildLUT();
		buildShapeProjectors();

		coef_ = new Snake2DNode[M_];	
		for(int i=0; i<M; i++){
			coef_[i] = new Snake2DNode(center.x + radius * Math.cos((2.0*Math.PI*(double)i)/(double)M), center.y + radius * Math.sin((2.0*Math.PI*(double)i)/(double)M));
		}
		setNodes(coef_);
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to compute the energy of the snake. */
	@Override
	public double energy(){

		for(int i=0; i<coef_.length; i++){
			if(coef_[i].x>(imageWidth_-1) || coef_[i].x<0 || coef_[i].y>(imageHeight_-1) || coef_[i].y<0){
				return Double.MAX_VALUE;
			}
		}
		
		if(xMinSnakeContour_<=1 || yMinSnakeContour_<=1 || xMaxSnakeContour_>=imageWidthMinusTwo_ || yMaxSnakeContour_>=imageHeightMinusTwo_){
			return(Double.MAX_VALUE);
		}else{
			double gradientBasedImageEnergy = computeImageEnergy();
			double regularizationEnergy = computeRegularizationEnergy();
			return (1-lambda_)*gradientBasedImageEnergy+lambda_*regularizationEnergy;
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * The purpose of this method is to compute the gradient of the snake energy
	 * with respect to the snake-defining nodes.
	 */
	@Override
	public Point2D.Double[] getEnergyGradient (){

		Point2D.Double[] v1 = computeImageEnergyGradient();
		Point2D.Double[] v2 = computeRegularizationEnergyGradient();
		
		
		final int K = v1.length;
		if (K != v2.length) {
			return(null);
		}
		final Point2D.Double[] g = new Point2D.Double[K];
		for (int k = 0; (k < K); k++) {
			g[k] = new Point2D.Double((1-lambda_)*v1[k].x + lambda_*v2[k].x, (1-lambda_)*v1[k].y + lambda_*v2[k].y);
		}
		
		return g;
	}

	// ----------------------------------------------------------------------------

	/** This method provides an accessor to the snake-defining nodes. */
	@Override
	public Snake2DNode[] getNodes (){

		return coef_;
	}

	// ----------------------------------------------------------------------------

	/**
	 * The purpose of this method is to determine what to draw on screen, given the
	 * current configuration of nodes.
	 */
	@Override
	public Snake2DScale[] getScales (){

		Snake2DScale[] skin = new Snake2DScale[2];
		skin[0] = new Snake2DScale(Color.YELLOW, new Color(0, 0, 0, 0), true, false);
		skin[1] = new Snake2DScale(Color.RED, new Color(0, 0, 0, 0), true, false);

		for(int k=0; k<M_; k++){
			skin[0].addPoint((int)Math.round(coef_[k].x),(int)Math.round(coef_[k].y));
		}

		int rxt, ryt;
		for(int k=0; k<MR_; k++){
			rxt = (int)Math.round(xSnakeContour_[k]);
			ryt = (int)Math.round(ySnakeContour_[k]);

			if(rxt<0){ 
				rxt = 0;
			}else if(rxt>=imageWidth_){
				rxt = imageWidth_-1;
			}

			if(ryt<0){
				ryt = 0;
			}else if(ryt>=imageHeight_){
				ryt = imageHeight_-1;
			}
			skin[1].addPoint(rxt,ryt);
		}
		return skin;
	}

	// ----------------------------------------------------------------------------

	/** The purpose of this method is to monitor the status of the snake. */
	@Override
	public boolean isAlive (){

		return true;
	}

	// ----------------------------------------------------------------------------

	/**
	 * If <code>true</code>, indicates that the user chose to interactively abort
	 * the processing of the snake.
	 * <p>
	 * Otherwise, if <code>false</code>, indicates that the dealings with the snake
	 * were terminated without user assistance.
	 */
	public boolean isCanceledByUser(){

		return canceledByUser_;
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
	public void setNodes (Snake2DNode[] node){

		for(int i=0;i<M_;i++){
			this.coef_[i].x = node[i].x;
			this.coef_[i].y = node[i].y;
		}
		updateArea();
		updateSnakeContour();
	}
	
	// ----------------------------------------------------------------------------

	/**
	 * This method is called when the methods <code>Snake2DKeeper.interact()</code>,
	 * <code>Snake2DKeeper.interactAndOptimize()</code>, and 
	 * <code>Snake2DKeeper.optimize()</code> are about to terminate.
	 */
	@Override
	public void updateStatus (boolean canceledByUser, boolean snakeDied, boolean optimalSnakeFound, Double energy){

		canceledByUser_ = canceledByUser;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Expand positively or negatively the control points defining the contour of the snake.
	 * The amplitude of the given expansion is applied from the center of mass of the snake.
	 * The amplitude of the expansion is given in px.
	 */
	public void expand(double amplitude) throws Exception {
		
		// outer nodes: outerNodes_;
		Point2D.Double cog = computeCenterOfGravity();
		Point2D.Double tmp = null;
		for (int i = 0; i < coef_.length; i++) {
			tmp = new Point2D.Double(coef_[i].x, coef_[i].y);
			Segment.movePointFromReference(tmp, cog, amplitude);
			coef_[i].setLocation(tmp);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the center of gravity of the structure based on the contour of snake. */
	public Point2D.Double computeCenterOfGravity() throws Exception {
		
		double meanx = 0.;
		double meany = 0.;
		for (int i = 0; i < coef_.length; i++) {
			meanx += coef_[i].x;
			meany += coef_[i].y;
		}
		return new Point2D.Double(meanx/coef_.length, meany/coef_.length);
	}

	// ============================================================================
	// PRIVATE METHODS

	/** Creation of all LUT. */
	private void buildLUT (){

		preintegratedFilteredImage_ = new float[imageWidth_*imageHeight_];
		float fuy_val;
		for(int i=0; i<imageWidth_; i++){
			fuy_val = 0;
			for (int j=0; j<imageHeight_; j++){
				preintegratedFilteredImage_[i+imageWidth_*j] = fuy_val;
				fuy_val += filteredImage_[i+imageWidth_*j];
			}
		}

		bSplineLUT_ = new double[NR_];
		bSplineDerivativeLUT_ = new double[NR_];
		double currentVal;
		for (int i=0; i<NR_; i++){
			currentVal = (double)i/(double)discretizationSamplingRate_;
			bSplineLUT_[i] = BSplinesUtils.ESpline4(currentVal, PI2M_);
			bSplineDerivativeLUT_[i] = BSplinesUtils.DerivativeESpline4(currentVal, PI2M_);
		}

		int qSize = 2*N-1; 
		bSplineAutocorrelationLUT_ = new double[qSize];
		for (int i=0; i<qSize; i++){
			bSplineAutocorrelationLUT_[i] = BSplinesUtils.AutocorrelationESpline4(i-N+1, PI2M_);
		}
	}

	// ----------------------------------------------------------------------------

	/** Initialization of the projectors of the elliptic shape-space. */
	private void buildShapeProjectors (){

		vcArray_ = new double[3][M_];
		double index;
		for(int i=0; i<M_; i++){
			index = -2*Math.PI*(double)i/(double)M_;
			vcArray_[0][i] = Math.sin(-index);
			vcArray_[1][i] = Math.cos(index);
			vcArray_[2][i] = 1;
		}
		Matrix Vc = new Matrix(vcArray_);
		Matrix VcT = Vc.transpose();
		priorShapeProjectionMatrix_ = VcT.times(((Vc.times(VcT)).inverse()).times(Vc));
		priorShapeOrthoProjectorMatrix_ = Matrix.identity(priorShapeProjectionMatrix_.getRowDimension(), priorShapeProjectionMatrix_.getColumnDimension()).minus(priorShapeProjectionMatrix_);
	}

	// ----------------------------------------------------------------------------

	/** Computes the prior-shape energy. */
	private double computeRegularizationEnergy (){

		double energy;
		double VcnArray[][] = new double[2][M_];
		for(int i=0; i<M_; i++){
			VcnArray[0][i] = coef_[i].x;
			VcnArray[1][i] = coef_[i].y;
		}
		Matrix Vcn = new Matrix(VcnArray);
		Vcn = Vcn.times(priorShapeOrthoProjectorMatrix_);
		energy = Vcn.normF();
		return energy*energy;
	}

	// ----------------------------------------------------------------------------

	/** Computes the derivatives of the prior-shape energy with respect to the snake defining coefficients. */
	private Point2D.Double[] computeRegularizationEnergyGradient (){	

		Point2D.Double[] gradient = new Point2D.Double[M_];
		double VcnArray[][] = new double[2][M_];
		for(int i=0; i<M_; i++){
			VcnArray[0][i] = coef_[i].x;
			VcnArray[1][i] = coef_[i].y;
		}
		Matrix Vcn = new Matrix(VcnArray);
		Vcn = Vcn.times(priorShapeOrthoProjectorMatrix_);
		for(int i=0; i<M_; i++){
			gradient[i] = new Point2D.Double(2.0*Vcn.get(0, i), 2.0*Vcn.get(1, i));
		}
		return gradient;
	}

	// ----------------------------------------------------------------------------

	/** Computes the image energy. */
	private double computeImageEnergy (){

		double energy = 0.0;
		double fuy_val;	
		int x1, x2, y1, y2;
		double DeltaX1, DeltaX2, DeltaY1;
		int width2 = imageWidth_-2;
		int height2 = imageHeight_-2;

		for (int i=0; i<MR_; i++){	
			x1 = (int)Math.floor(xSnakeContour_[i]);
			y1 = (int)Math.floor(ySnakeContour_[i]);

			if(x1<1){ 
				x1 = 1;
			}else if(x1>width2){
				x1 = width2;
			}

			if(y1<1){
				y1 = 1;
			}else if(y1>height2){
				y1 = height2;
			}

			x2 = x1+1;
			y2 = y1+1;

			DeltaX1 = xSnakeContour_[i] - x1;
			DeltaY1 = ySnakeContour_[i] - y1;	
			DeltaX2 = x2 - xSnakeContour_[i];

			fuy_val = preintegratedFilteredImage_[x1+imageWidth_*(y1-1)]*DeltaX2+preintegratedFilteredImage_[x2+imageWidth_*(y1-1)]*DeltaX1; 
			fuy_val += 0.5*((filteredImage_[x1+imageWidth_*y1]*DeltaX2+filteredImage_[x2+imageWidth_*y1]*DeltaX1)+(DeltaY1*(((filteredImage_[x1+imageWidth_*y1]*DeltaX2+filteredImage_[x2+imageWidth_*y1]*DeltaX1)*(2-DeltaY1))+((filteredImage_[x1+imageWidth_*y2]*DeltaX2+filteredImage_[x2+imageWidth_*y2]*DeltaX1)*DeltaY1))));

			energy += fuy_val*xSnakeTangentVector_[i];
		}	
		energy = energy/((double)discretizationSamplingRate_)*computeOrientation();
		return energy;
	}

	// ----------------------------------------------------------------------------

	/** 
	 * Computes the derivatives of the image energy with respect to the snake
	 * defining coefficients.
	 */
	private Point2D.Double[] computeImageEnergyGradient (){

		Point2D.Double[] gradient = new Point2D.Double[M_];
		double gradX, gradY, Qfu;
		int l_p;
		int orientation = computeOrientation();
		for(int i=0; i<M_; i++){
			gradient[i] = new Point2D.Double(0.0, 0.0);
		}
		for(int k=0; k<M_; k++){
			gradX = 0.0;
			gradY = 0.0;
			for(int l=k-N+1; l<=k+N-1; l++){
				l_p = l;
				while(l_p<0){
					l_p = l_p + M_;
				}
				while (l_p>=M_){
					l_p = l_p - M_;
				}
				Qfu = Q_fu(k,l);
				gradX -= coef_[l_p].y*Qfu;
				gradY += coef_[l_p].x*Qfu;
			}
			gradient[k].x = gradX*orientation; 
			gradient[k].y = gradY*orientation; 
		}
		return gradient;
	}

	// ----------------------------------------------------------------------------

	/** Returns the sign of the area. */
	private int computeOrientation(){
		
		return (int) Math.signum(snakeArea_);
	}

	// ----------------------------------------------------------------------------

	/** Recomputes the points of the polygons defining the contour of the snake.*/
	private void updateSnakeContour (){
		
		xMinSnakeContour_ = imageWidth_-1;
		xMaxSnakeContour_ = 0;
		yMinSnakeContour_ = imageHeight_-1;
		yMaxSnakeContour_ = 0;

		int index;
		double aux, aux2, xPrimeVal, xPosVal, yPosVal;
		for(int i=0; i<MR_; i++){
			xPosVal = 0.0;
			yPosVal = 0.0;
			xPrimeVal = 0.0;
			for(int k=0; k<M_; k++){
				index = (i-k*discretizationSamplingRate_)%(MR_);
				if (index<0) index += MR_;

				if(index>=NR_){
					continue;
				}else{
					aux = bSplineLUT_[index];
					aux2 = bSplineDerivativeLUT_[index];
				}
				xPosVal += coef_[k].x*aux;
				yPosVal += coef_[k].y*aux;
				xPrimeVal += coef_[k].x*aux2;
			}

			xSnakeContour_[i] = xPosVal;
			ySnakeContour_[i] = yPosVal;
			xSnakeTangentVector_[i] = xPrimeVal;

			if((int)Math.floor(xSnakeContour_[i])<xMinSnakeContour_) xMinSnakeContour_ = (int)Math.floor(xSnakeContour_[i]);
			if((int)Math.ceil(xSnakeContour_[i])>xMaxSnakeContour_) xMaxSnakeContour_ = (int)Math.ceil(xSnakeContour_[i]);
			if((int)Math.floor(ySnakeContour_[i])<yMinSnakeContour_) yMinSnakeContour_ = (int)Math.floor(ySnakeContour_[i]);
			if((int)Math.ceil(ySnakeContour_[i])>yMaxSnakeContour_) yMaxSnakeContour_ = (int)Math.ceil(ySnakeContour_[i]);
		}
	}

	// ----------------------------------------------------------------------------

	/** Updates the signed area of the snake. */
	private void updateArea(){
		
		double area = 0.0;
		int l_p;
		for(int k=0; k<M_; k++){
			int kN= k+N;
			for(int l=k-N+1; l<kN; l++){
				l_p = l;
				while(l_p<0) l_p += M_;

				while(l_p>=M_) l_p -= M_;

				area += coef_[k].y*coef_[l_p].x*bSplineAutocorrelationLUT_[kN-l-1];
			}
		}
		snakeArea_ = area;
	}

	// ----------------------------------------------------------------------------

	/** Function that computes the remainder of the image energy. */
	private double Q_fu (int k, int l){

		if(Math.abs(l-k)>=N) return(0.0);

		double q_fu = 0.0;
		double tmp1, tmp2, DeltaX1, DeltaY1, DeltaX2, DeltaY2;
		int index, index2;
		int x1, x2, y1, y2;
		int width2 = imageWidth_-2;
		int height2 = imageHeight_-2;


		for(int i=0; i<NR_; i++){
			index  = i+(k-l)*discretizationSamplingRate_;
			index2 = (i+discretizationSamplingRate_*k)%MR_;

			x1 = (int)Math.floor(xSnakeContour_[index2]);
			y1 = (int)Math.floor(ySnakeContour_[index2]);

			if(x1<1){ 
				x1 = 1;
			}else if(x1>width2){
				x1 = width2;
			}

			if(y1<1){
				y1 = 1;
			}else if(y1>height2){
				y1 = height2;
			}

			x2 = x1+1;
			y2 = y1+1;

			DeltaX1 = xSnakeContour_[index2] - x1;
			DeltaY1 = ySnakeContour_[index2] - y1;	

			DeltaX2 = x2 - xSnakeContour_[index2];
			DeltaY2 = y2 - ySnakeContour_[index2];	

			tmp1 = bSplineLUT_[i];

			if(index<0 || index >= NR_){
				continue;
			}else{
				tmp2 = bSplineDerivativeLUT_[index];	
			}
			q_fu += (filteredImage_[x1+imageWidth_*y1]*DeltaX2*DeltaY2+filteredImage_[x2+imageWidth_*y1]*DeltaX1*DeltaY2+filteredImage_[x1+imageWidth_*y2]*DeltaX2*DeltaY1+filteredImage_[x2+imageWidth_*y2]*DeltaX1*DeltaY1)*tmp1*tmp2;
		}
		q_fu = q_fu/(double)discretizationSamplingRate_;
		return q_fu;
	}
}