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

package ch.epfl.lis.wingj.structure.drosophila.embryo;

import java.awt.geom.Point2D;
import java.io.File;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;

import ij.ImagePlus;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Overlay;
import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure;

/** 
 * This class describes the structure of the Drosophila embryo.
 * <p>
 * This class extends WPouchStructure as the structure of the embryo is modeled
 * the same way as we do for the wing pouch. Only constructor and copy/clone
 * operators are redefined.
 * 
 * @see WPouchStructure
 *           
 * @version October 25, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class EmbryoStructure extends WPouchStructure {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public EmbryoStructure(String name) {
		
		super(name);
		systemName_ = "drosophila-embryo"; // for structure measurements
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Copy constructor.
	 * <p>
	 * Takes as input a Structure to allow this class to be extended. 
	 */
	public EmbryoStructure(Structure structure) {
		
		super(structure);
		systemName_ = "drosophila-embryo"; // for structure measurements
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy operator. */
	@Override
	public EmbryoStructure copy() {
		
		return new EmbryoStructure(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Clone operator. */
	@Override
	public EmbryoStructure clone() {
		
		return copy();
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns an object extending the abstract class Overlay for displaying structure on top of an image.
	 * If singleImage is true, an ImageWindow object is used instead of a StackWindow.
	 * @see ch.epfl.lis.wingj.WJStructureViewer
	 */
	@Override
	public Overlay getStructureOverlay(ImagePlus image, boolean singleImage) {
		
		return new EmbryoOverlay(this, image, singleImage);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Save to file the data required to reconstruct the structure. Here the data
	 * are 1) a list of nodes required to create the snake structure and 2) the center
	 * of gravity of each of the four compartment to obtain the information about the
	 * orientation of the structure. */
	@Override
	public void write(URI uri) throws Exception {
		
		// save structure XML
		try {
			EmbryoStructureParser parser = new EmbryoStructureParser(this);
			parser.write(uri);
			WJSettings.log("[x] Writing structure (xml)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure (xml)");
			WJMessage.showMessage(e);
		}
		
		// save structure to TXT
		try {
			// build URI for each element to save from the given URI.
			String filename = uri.getPath();
			filename = FilenameUtils.removeExtension(filename);//FilenameUtils.removeExtension(filename);
			// prepare file URI
			URI dvBoundaryUri = (new File(filename + "_A-P.txt")).toURI();
			URI apBoundaryUri = (new File(filename + "_V-D.txt")).toURI();
			URI contourUri = (new File(filename + "_contour.txt")).toURI();
			// write to file
			getDVBoundary().writePoints(dvBoundaryUri);
			getAPBoundary().reverse(); // to get V -> D
			getAPBoundary().writePoints(apBoundaryUri);
			getAPBoundary().reverse(); // restore
			writePoints(contourUri);
	    	WJSettings.log("[x] Writing structure (txt)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing structure (txt)");
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Read from file the data required to reconstruct the structure. See the description
	 * of the method write(). After loading the data, this object is initialized based
	 * on the data contained in the file. */
	@Override
	public void read(URI uri) throws Exception {

		try {
			EmbryoStructureParser parser = new EmbryoStructureParser(this);
			parser.read(uri);
			WJSettings.log("[x] Reading structure (xml)");
		} catch (Exception e) {
			WJSettings.log("[ ] Reading structure (xml)");
			throw e;
//			WJMessage.showMessage(e);
		}
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public void setOrientationControlPoint(Point2D.Double point) { this.discCenter_ = point; }
	public Point2D.Double getOrientationControlPoint() { return this.discCenter_; }
	
	public void setEmbryoCenter(Point2D.Double center) { this.center_ = center; }
	public Point2D.Double getEmbryoCenter() { return this.center_; }
}
