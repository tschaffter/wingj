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

import ch.epfl.lis.wingj.WJSystem;
import ch.epfl.lis.wingj.structure.Structure;

/** 
 * Drosophila wing pouch system.
 * 
 * @version February 13, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WPouchSystem extends WJSystem {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchSystem(int id) {
		
		super(id, "DrosoWingPouchSchaffter2013");
		name_ = "Drosophila wing pouch";
		description_ = "<html><b>Unsupervised detection of the <i>Drosophila</i> wing pouch structure</b><br>" +
			"The expression of the protein Wingless (Wg) can be used to visualize the contour<br>" +
			"of the pouch and the dorsal/ventral (D/V) boundary, and the expression of Patch (Ptc)<br>" +
			"to visualize the anterior/posterior (A/P) compartment boundary.<br>" +
			"<br>" +
			"<b>T Schaffter, R Delgado-Gonzalo, F Hamaratoglu, M Affolter, M Unser, and D Floreano</b>.<br>" +
			"Towards unsupervised and systematic quantification of biological systems, vol, num, pages, 2013.</html>";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new detector for the current system. */
	@Override
	public void newStructureDetector(String experimentName) throws Exception {
		
		deleteStructureDetector();
		structureDetector_ = new WPouchStructureDetector(experimentName);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new structure object. */
	@Override
	public Structure newStructure() throws Exception {
		
		Structure structure = new WPouchStructure(new String(name_).replaceAll(" ", "_"));
		structure.setName("other-system");
		
		return structure;
	}
}
