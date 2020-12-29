/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

WingJ is licensed under a
Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.

You should have received a copy of the license along with this
work. If not, see http://creativecommons.org/licenses/by-nc-nd/3.0/.

If this software was useful for your scientific work, please cite our paper(s)
listed on http://wingj.sourceforge.net.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package detection.ppauto;

import ij.ImagePlus;

/** 
 * Tests if the couple of parameters (blur, thld) lead to a valid skeleton when applied on a given structure projection.
 * 
 * @version September 13, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class PreProcessingTester {

	/** Structure projection */
	@SuppressWarnings("unused")
	private ImagePlus structureProjection_ = null;
	
	/** Blur parameter */
	private double blur_ = 0.;
	/** Thld parameter */
	private int thld_ = 0;
	
	/** Skeleton obtained */
	private ImagePlus skeletonImage_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public PreProcessingTester(ImagePlus structureProjection, double blur, int thld) {
		
		structureProjection_ = structureProjection;
		
		blur_ = blur;
		thld_ = thld;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Test the combination of the two parameters blur and thld */
	public boolean test() throws Exception {
		
		throw new Exception("ERROR: test() not implemented.");
		
//		return false;
		
//		PreProcessing pp = new PreProcessing();
//		ImagePlus ppImage = pp.blurAndThreshold(structureProjection_, blur_, thld_);
//		
//		Skeleton skeleton = new Skeleton();
//		skeleton.skeletonize((FloatProcessor) ppImage.getProcessor());
//		skeleton.prune(skeleton.sk);
//		
//		// save skeleton image
//		skeletonImage_ = skeleton.getSkeletonImage();
//		skeleton.searchAllForkPoints();
//		skeleton.labelSkeleton();
//		
//		return (skeleton.computeKiteInitialization()!=null);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	public double getBlur() { return blur_; }
	public int getThld() { return thld_; }
	
	public ImagePlus getSkeletonImage() { return skeletonImage_; }
}
