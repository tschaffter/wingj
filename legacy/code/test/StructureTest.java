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

import java.io.File;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchSnakeStructure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.parsers.WPouchData;
import ch.epfl.lis.wingj.utilities.FileUtils;

import ch.epfl.lis.wingj.structure.Compartment;
import ch.epfl.lis.wingj.structure.DetectorWorker;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

/** 
 * Tests the algorithms for wing structure detection.
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
public class StructureTest extends Test {
	
	/** Filename of the input image projection used to detect the wing structure */
	protected String structureProjectionFilename_ = "test_structure_mip.tif";
	/** Filename of the gold standard WingSnake in XML format */
	protected String xmlSnakeFilename_ = "test_structure.xml";
	/** Filename of the structural properties of detected wing (must be redundant with WingSnake test) */
	protected String structurePropertiesFilename_ = "test_structure_properties.xml";
	
	/** Detects the wing pouch structure */
	private WPouchStructureDetector detector_ = null;
	/** Generated WPouchSnakeStructure */
	private WPouchSnakeStructure snake_ = null;
	
	// ============================================================================
	// PROTECTED METHODS
	
	/**
	 * Load input image to test the wing structure detection algorithm.
	 * IMPORTANT: Use directly the MIP instead of the entire image stack.
	 */
	protected ImagePlus openStructureProjection(String filename) throws Exception {
		
		ImagePlus projection = null;
		if ((projection = IJ.openImage(filename)) == null)
			throw new Exception("StructureTest: Unable to load structure projection");
		// convert to 32-bit
		new ImageConverter(projection).convertToGray32();
		// must be that name and no other
		projection.setTitle("structure_projection");
		
		return projection;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Detect the structure of the wing from the given projection image.
	 * IMPORTANT: The input image must be in 32-bit format and be added
	 * to the ImagePlusManager with the name "structure_projection".
	 */
	protected WPouchSnakeStructure runStructureDetection(ImagePlus structureProjection) throws Exception {
		
		detector_ = new WPouchStructureDetector("test");
		detector_.isInteractive(false);
		detector_.editStructure(false);
		detector_.showStructure(false); // do not show the output
		detector_.setStructureProjection(structureProjection);
		// instantiate and run worker
		DetectorWorker worker = new DetectorWorker(detector_);
		worker.setMode(DetectorWorker.RUNALL);
		worker.execute();
		worker.get(); // wait on the worker to have its task completed
		
		return (WPouchSnakeStructure)detector_.getStructure().getSnakeStructure();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Test the matching between the computed snake and the gold standard snake */
	protected void testWingSnake(WPouchSnakeStructure snake, WPouchSnakeStructure goldSnake) throws Exception {
		
		if (snake.match(goldSnake)) WJSettings.log("[OK] WingSnake match!");
		else throw new Exception("[ERROR] WingSnake do not match.");
	}
	
	// ----------------------------------------------------------------------------
	
	/** Test the matching of the computed structure properties and the gold standard properties */
	protected void testStructureProperties(WPouchData data, WPouchData goldData) throws Exception {
		
		if (data.match(goldData)) WJSettings.log("[OK] WPouchData match!");
		else throw new Exception("[ERROR] WPouchData do not match.");
	}
	
	// ----------------------------------------------------------------------------
	
	/** Build a Compartment defined as a triangle as big as half the image */
	protected void areaComputationTest(ImagePlus img) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		double width = img.getWidth() * img.getCalibration().pixelWidth;
		double height = img.getHeight() * img.getCalibration().pixelWidth;
		// set new scale and ignore the one in settings file
		settings.setScale(img.getCalibration().pixelWidth);
		
		Compartment c = new Compartment("triangle");
		c.addPoint(0., 0.);
		c.addPoint(0., img.getWidth());
		c.addPoint(img.getWidth(), img.getHeight());
		
		double cAreaInUm = c.area();
		double imgAreaInUm = (width*height);
		
		WJSettings.log("Image width = " + width + " um, height = " + height + " um");
		WJSettings.log("Image: 1 px = " + img.getCalibration().pixelWidth + " um");

		WJSettings.log("imgAreaInUm / 2. = " + (imgAreaInUm/2.) + " um^2");
		WJSettings.log("cAreaInUm = " + cAreaInUm + " um^2");
		
		if (cAreaInUm == (imgAreaInUm/2.)) WJSettings.log("[OK] Area computation in um.");
		else throw new Exception("[ERROR] Area computation in um.");
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor */
	public StructureTest() {
		
		super("structure");
		
		readPath();
		loadSettings();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Perform multiple tests to assess the performance and validity of the wing structure detection algorithms */
	@Override
	public void run() throws Exception {
		
		WJSettings.log("Testing structure detection algorithms...");
		
		String filename = path_ + name_ + "/" + structureProjectionFilename_;
		WJSettings.log("Reading structure projection " + filename);
		ImagePlus projection = openStructureProjection(filename);
		// next steps require the projection being added to the image manager
		ImagePlusManager.getInstance().add(projection.getTitle(), projection);
		
		/**
		 * MINOR TEST: Area
		 */
		areaComputationTest(projection);
		
		/**
		 * TEST 1: WingSnake
		 */

		// run wing structure detection
		WJSettings.log("Detecting wing structure (WingSnake)");
		snake_ = runStructureDetection(projection);
		// load gold standard
		filename = path_ + name_ + "/" + xmlSnakeFilename_;
		WJSettings.log("Reading gold standard WingSnake " + filename);
		WPouchStructure structure = new WPouchStructure("gold");
		structure.read(FileUtils.getFileURI(filename));
		
		
		testWingSnake(snake_, (WPouchSnakeStructure)structure.getSnakeStructure());
		
		/**
		 * TEST 2: WPouchData
		 */
		WJSettings.log("Generating structure properties (WPouchData) " + filename);
		WPouchData data = new WPouchData();
		data.initialize((WPouchStructure)detector_.getStructure());
		// loading gold standard
		filename = path_ + name_ + "/" + structurePropertiesFilename_;
		WJSettings.log("Reading gold standard structure WPouchData " + filename);
		WPouchData goldData = WPouchData.read(new File(filename).toURI());
		
		testStructureProperties(data, goldData);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generate resource files */
	@Override
	public void generateResources() throws Exception {
		
		throw new Exception("StructureTest::generateResources must be updated for new class architecture.");
		
//		ImagePlusManager manager = ImagePlusManager.getInstance();
//		
//		/**
//		 * TEST 1: WingSnake
//		 */
//		String filename = path_ + name_ + "/" + structureProjectionFilename_;
//		ImagePlus projection = openStructureProjection(filename);
//		manager.add(projection.getTitle(), projection);
//		// run wing structure detection
//		snake_ = runStructureDetection(projection);
//		// write WingSnake to XML file
//		filename = path_ + name_ + "/" + xmlSnakeFilename_;
//		WJSettings.log("Generating gold standard WingSnake " + filename);
//		WPouchStructureParser xmlSnake = new WPouchStructureParser(snake_);
//		xmlSnake.write(FileUtils.getFileURI(filename));
//		
//		/**
//		 * TEST 2: WPouchData
//		 */
//		WPouchData data = new WPouchData();
//		data.initialize(detector_.getWPouch());
//		filename = path_ + name_ + "/" + structurePropertiesFilename_;
//		WJSettings.log("Generating gold standard WPouchData " + filename);
//		data.write(new File(filename).toURI());
	}
}
