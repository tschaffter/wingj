/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

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

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WingJ;

/**
 * Tests the aggregation of many WPouchStructure objects.
 *
 * @version November 28, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchStructureAggregationTest {

	/** Benchmarks directory (don't forget final /). */
//	private String benchmarkDirectory_ = "file:///home/tschaffter/devel/java/WingJ/benchmarks/wingpouch_pmadAB_brkAB_wg-ptcAB_90H/";
	private String benchmarkDirectory_ = "file:///Users/ricard/Desktop/20121129_pmadAB_brkAB_wg-ptcAB_90-91H/";

	/** List of URIs describing WPouchStructure objects. */
	private List<URI> structureUris = null;

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public WPouchStructureAggregationTest() {}

	// ----------------------------------------------------------------------------

	/** Main method. */
	public static void main(String[] args) {

		try {
			WPouchStructureAggregationTest synthesisTest = new WPouchStructureAggregationTest();
			synthesisTest.run();
		} catch (Exception e) {
			WJSettings.log("ERROR: Structures synthesis failed.");
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------

	/** Run method. */
	public void run() throws Exception {

		new ImageJ(); // not required to display images but tools can be handy
		WingJ.getInstance(); // instantiate WingJ
		WJSettings.getInstance(); // initialize settings

		// benchmark
		structureUris = new ArrayList<URI>();
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_1/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_2/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_3/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_4/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_5/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_6/WingJ/structure.xml"));
		structureUris.add(new URI(benchmarkDirectory_ + "20100716_pmadAB_brkAB_wg-ptcAB_90-91H_M_6/WingJ/structure.xml"));
		// structure synthesis
		WPouchStructure structure = new WPouchStructure("source");
		structure = (WPouchStructure)structure.aggregateFromFiles(structureUris);

		// moves centroid of the structure to the center of the image
		structure.setCanonicalOrientation();
		structure.moveToTopLeftCorner();

		// visualize structure
		ImagePlus background = IJ.createImage("averaged_structure", "black", (int)Math.ceil(structure.getHorizontalWidth()), (int)Math.ceil(structure.getVerticalHeight()), 1);
		ImagePlusManager manager = ImagePlusManager.getInstance();
		manager.add(background.getTitle(), background, false);

		WJStructureViewer viewer = new WJStructureViewer(structure, background);
		viewer.run();
	}
}
