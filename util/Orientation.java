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
package ch.epfl.lis.wingj.legacy;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Defines the orientation on an image.
 *
 * IMPORTANT: the orientation of the wing may be not aligned with the image axes,
 * but usually biologists try their best to align the wing, which is true most of
 * the time.
 *
 * @version August 25, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class Orientation {

	/** North (towards the top of the image) */
	public static final int NORTH = 0;
	/** North (towards the right of the image) */
	public static final int EAST = 1;
	/** North (towards the bottom of the image) */
	public static final int SOUTH = 2;
	/** North (towards the left of the image) */
	public static final int WEST = 3;

	// DEFAULT CANONICAL ORIENTATION

	/** Dorsal side */
	private int dorsal_ = Orientation.NORTH;
	/** Ventral side */
	private int ventral_ = Orientation.SOUTH;
	/** Anterior side */
	private int anterior_ = Orientation.EAST;
	/** Posterior */
	private int posterior_ = Orientation.WEST;

	// ============================================================================
	// PRIVATE METHODS

	/** Rotate a position to the left (-90 deg) */
	private int rotateLeft(int position) {

		position = (position - 1) % 4;
		if (position < 0) position = 3;
		return position;
	}

	// ----------------------------------------------------------------------------

	/** Rotate a position to the right (90 deg) */
	private int rotateRight(int position) {

		return (position + 1) % 4;
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor */
	public Orientation() {}

	// ----------------------------------------------------------------------------

	/** Match operator */
	public boolean match(Orientation o) {

		if (this.dorsal_ != o.dorsal_) return false;
		if (this.ventral_ != o.ventral_) return true;
		if (this.anterior_ != o.anterior_) return false;
		if (this.posterior_ != o.posterior_) return false;

		return true;
	}

	// ----------------------------------------------------------------------------

	/** Swap dorsal <-> ventral sides */
	public void swapDorsalVentral() {

		int tmp = dorsal_;
		dorsal_ = ventral_;
		ventral_ = tmp;
	}

	// ----------------------------------------------------------------------------

	/** Swap anterior <-> posterior sides */
	public void swapAnteriorPosterior() {

		int tmp = anterior_;
		anterior_ = posterior_;
		posterior_ = tmp;
	}

	// ----------------------------------------------------------------------------

	/** Rotate to the left (-90 deg) */
	public void rotateLeft() {

		dorsal_ = rotateLeft(dorsal_);
		ventral_ = rotateLeft(ventral_);
		anterior_ = rotateLeft(anterior_);
		posterior_ = rotateLeft(posterior_);
	}

	// ----------------------------------------------------------------------------

	/** Rotate to the right (90 deg) */
	public void rotateRight() {

		dorsal_ = rotateRight(dorsal_);
		ventral_ = rotateRight(ventral_);
		anterior_ = rotateRight(anterior_);
		posterior_ = rotateRight(posterior_);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Re-orient the given image based on its current orientation and the target orientation.
	 * Image rotation and flip are not very expensive, otherwise there is a better way to do than
	 * apply all intermediary operations on the image (find the minimum operations to apply on the image).
	 * IMPORTANT: ImageProcessor rotation operators return new ImageProcesor.
	 */
	static public void reorient(ImagePlus img, Orientation currentOrientation, Orientation targetOrientation) throws Exception {

		if (img == null || img.getProcessor() == null)
			throw new Exception("ERROR: ImagePlus or Processor is null.");

		ImageProcessor p = img.getProcessor();
		while (currentOrientation.dorsal_ != targetOrientation.dorsal_) { // align on target dorsal/ventral axis
			currentOrientation.rotateRight();
			p = p.rotateRight();
		}

		if (currentOrientation.anterior_ != targetOrientation.anterior_) { // if true, anterior and posterior must be swapped
			currentOrientation.swapAnteriorPosterior();
			if (currentOrientation.anterior_ == Orientation.NORTH || currentOrientation.anterior_ == Orientation.SOUTH)
				p.flipVertical();
			else
				p.flipHorizontal();
		}

		img.setProcessor(p); // don't forget
	}
}
