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

package ch.epfl.lis.wingj;

/**
 * Custom exception that descends from Java's Exception class. It is
 * intended to deal with the situations when the user-specified threshold
 * generated an almost-empty skeleton.
 *
 * @version May 30, 2012
 *
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 */
public class WJThresholdException extends Exception {

	/** Generated serial. */
	private static final long serialVersionUID = 3676686219554049674L;

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public WJThresholdException() {

		super();
	}

	// ----------------------------------------------------------------------------

	/** Default constructor. */
	public WJThresholdException(String str) {

		super(str);
	}

}
