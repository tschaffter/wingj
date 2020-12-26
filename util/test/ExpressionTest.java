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

package ch.epfl.lis.wingj.test;

import ch.epfl.lis.wingj.WJSettings;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

/** 
 * Tests the algorithms for expression quantification.
 * 
 * IMPORTANT: Do not use the interface of WingJ to generate resource files for
 * the tests because the values of the interface can not be the same than those in
 * a settings file (gui2settings called, e.g. 0.85 loaded from settings file to set
 * a Spinner can become 0.850000000000001 when setting it back to WJSettings, which
 * affect for instance the lambda parameter used by WingSnake). Instead, use the
 * dedicated function from the test classes to generate resource files.
 * 
 * @version August 23, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class ExpressionTest extends Test {
	
	/** Filename of the input image projection to quantify */
	protected String expressionProjectionFilename_ = "test_expression_aip.tif";
	
	// ============================================================================
	// PROTECTED METHODS
	
	/**
	 * Load input image to test expression quantification.
	 * IMPORTANT: Use directly the AIP instead of the entire image stack.
	 */
	protected ImagePlus openExpressionProjection(String filename) throws Exception {
		
		ImagePlus projection = null;
		if ((projection = IJ.openImage(filename)) == null)
			throw new Exception("StructureTest: Unable to load expression projection");
		// convert to 32-bit
		new ImageConverter(projection).convertToGray32();
		// must be that name and no other
		projection.setTitle("expression_projection");
		
		return projection;
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor */
	public ExpressionTest() {
		
		super("expression");
		
		readPath();
		loadSettings();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Perform multiple tests to assess the performance and validity of the expression measurement */
	@Override
	public void run() throws Exception {
		
		// open the input expression image (projection)
		String filename = path_ + name_ + "/" + expressionProjectionFilename_;
		WJSettings.log("Reading expression projection " + filename);
		//ImagePlus projection = openExpressionProjection(filename);
		
		/**
		 * TEST 1: Grid
		 */
		
		/**
		 * TEST 2: Grid densities
		 */
		
		/**
		 * TEST 3: Individual circular maps
		 */
		
		/**
		 * TEST 4: Stitching mask
		 */
		
		/**
		 * TEST 5: Final circular map (composite)
		 */
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generate resource files */
	@Override
	public void generateResources() throws Exception {
		
	}
}
