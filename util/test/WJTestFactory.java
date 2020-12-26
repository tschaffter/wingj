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

import java.util.ArrayList;
import java.util.List;

/** 
 * Performs multiple tests to assess the performance of the algorithms implemented in WingJ.
 * 
 * @version August 23, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class WJTestFactory {
	
	/** Structure detection tests */
	public static final int WJ_TEST_STRUCTURE = 1;
	/** 1D expression quantification test */
	public static final int WJ_TEST_EXPRESSION_1D = 2;
	/** 2D expression quantification test */
	public static final int WJ_TEST_EXPRESSION_2D = 3;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public WJTestFactory() {}
	
	// ----------------------------------------------------------------------------
	
	/** Performs all tests */
	public void runAll() throws Exception {
		
		List<Test> tests = new ArrayList<Test>();
		tests.add(new StructureTest());
		
		for (int i = 0; i < tests.size(); i++)
			tests.get(i).run(); // stops after first test failure
	}
	
	// ----------------------------------------------------------------------------
	
	/** Re-generate all the resource files required for testing */
	public void generateResources() throws Exception {
		
		List<Test> tests = new ArrayList<Test>();
		tests.add(new StructureTest());
//		tests.add(new ExpressionTest());
		
		for (int i = 0; i < tests.size(); i++)
			tests.get(i).generateResources();
	}
}
