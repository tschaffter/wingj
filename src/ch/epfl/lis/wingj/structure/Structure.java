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

package ch.epfl.lis.wingj.structure;

import ij.ImagePlus;

import java.awt.geom.Point2D;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJStructureViewer;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure;

/**
 * Abstract class to extend to describe a morphological structure to identify using an
 * extended instance of StructureDetector.
 * <p>
 * Each structure made of Compartment and Boundary object contains an instance of a
 * class extending StructureSnake. StructureSnake is used to provide an accurate,
 * parameterized description of Structure using B-splines.
 * <p>
 * The 2D structure itself extend the class Compartment. An example of extended classes
 * is given by WPouchStructure.
 * 
 * @version November 9, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
abstract public class Structure extends Compartment {
	
	/** Mean aggregated structure. */
	public static final int AGGREGATION_MEAN = StructureSnake.AGGREGATION_MEAN;
	/** Mean+std aggregated structure (only structure contour is affected, boundaries are still obtained by the mean method). */
	public static final int AGGREGATION_MEAN_PLUS_STD = StructureSnake.AGGREGATION_MEAN_PLUS_STD;
	/** Mean-std aggregated structure (only structure contour is affected, boundaries are still obtained by the mean method). */
	public static final int AGGREGATION_MEAN_MINUS_STD = StructureSnake.AGGREGATION_MEAN_MINUS_STD;
	
	/** Default filename for exporting structure to file. */
	public static String STRUCTURE_DEFAULT_FILENAME = "structure.xml";
	
	/** Snake version of the structure made of B-splines and used to modify the structure geometry. */
	protected StructureSnake snake_ = null;
	
	/** Is true if the structure has been identified. */
	protected boolean isStructureKnown_ = false;
	/** Is true if the orientation of the structure has been identified. */
	protected boolean isOrientationKnown_ = false;

	// ============================================================================
	// ABSTRACT METHODS
	
	/** Copy operator. */
	@Override
	abstract public Structure copy();
	
	/** Saves to file the data required to reconstruct the structure. */
	abstract public void write(URI uri) throws Exception;
	/** Loads the data required to reconstruct the structure. */
	abstract public void read(URI uri) throws Exception;
	
	/** Saves the structure dataset (structure measurements) to file. */
	abstract public void writeStructureMeasurements(URI uri) throws Exception;
	/** Returns the structure dataset in a readable format. */
	abstract public String getReadableStructureDataset() throws Exception;
	
	/** Saves to file the data required to reconstruct the structure (opens a Save dialog). */
	abstract public void write() throws Exception;
	/** Saves the structure dataset (structure meansurement) to file (opens a Save dialog). */
	abstract public void writeStructureMeasurements() throws Exception;
	/** Saves entire structure dataset to file. */
	abstract public void writeStructureDataset(WJStructureViewer structureViewer) throws Exception;
	
	/** Returns dorsal/ventral (D/V) boundary/axis. */
	abstract public Boundary getAPBoundary();
	/** Returns anterior/posterior (A/P) boundary/axis. */
	abstract public Boundary getDVBoundary();
	
	/** Swaps D/V and A/P axes. */
	abstract public void swapAxes() throws Exception;
	/** Reverses the direction of the A/P axis. */
	abstract public void reverseAPAxisDirection() throws Exception;
	/** Reverses the direction of the D/V axis. */
	abstract public void reverseDVAxisDirection() throws Exception;
	
	/** Reverses in the horizontal dimension the whole structure. */
	abstract public void flipHorizontally() throws Exception;
	/** Reverses in the vertical dimension the whole structure. */
	abstract public void flipVertically() throws Exception;
	
	/** Translates the structure using the given vector. */
	abstract public void translate(double dx, double dy) throws Exception;
	/** Rotates the structure using the given angle in radians. */
	abstract public void rotate(double angle) throws Exception;
	/** Moves the structure so that its center of mass is centered on the given target point. */
	abstract public void moveToCom(Point2D.Double target) throws Exception;
	/** Moves the structure so that its center centered on the given target point. */
	abstract public void moveToCenter(Point2D.Double target) throws Exception;
	/** Moves the structure so that it touches the top and left borders of the image. Returns dx and dy. */
	abstract public double[] moveToTopLeftCorner() throws Exception;
	
	/**
	 * Sets the structure in a predefined orientation and returns 1) the rotation angle,
	 * 2) if the structure has been flipped horizontally (1) or not (0).
	 */
	abstract public double[] setCanonicalOrientation() throws Exception;
	/** Rotates the current structure to minimize a certain disparity measure towards a reference structure. */
	abstract public void align(Structure reference) throws Exception;
	
	/**
	 * Computes a Boundary trajectory along which expression can be measured (passed by reference).
	 * <p>
	 * The double value returned is the length of the negative part in the
	 * current unit. For instance, Boundary.length() returns the value in the correct unit.
	 * @param boundary Trajectory along which expression will be measured.
	 * @param referenceBoundary Takes value WJSettings.BOUNDARY_AP or WJSettings.BOUNDARY_DV.
	 * @param offset Takes values in [-1,1].
	 */
	abstract public double getExpressionTrajectory(Boundary boundary, int referenceBoundary, double offset) throws Exception;
	
	/** Returns a new structure that aggregates the given structures. */
	abstract public Structure aggregate(String name, List<Structure> structures, int aggregationMode) throws Exception;
	/** Sets this structure as the aggregate of the given structures. */
	abstract public void aggregate(List<Structure> structures, int aggregationMode) throws Exception;
	
	/**
	 * Returns an object extending the abstract class Overlay for displaying structure on top of an image.
	 * If singleImage is true, an ImageWindow object is used instead of a StackWindow.
	 * @see ch.epfl.lis.wingj.WJStructureViewer
	 */
	abstract public Overlay getStructureOverlay(ImagePlus image, boolean singleImage);
	
	/** Returns a new StructureDataset corresponding to this structure. */
	abstract public StructureDataset newStructureDataset();
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public Structure(String name) {
		
		super(name);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy constructor. */
	public Structure(Structure c) {
		
		super(c);

		if (c.snake_ != null)
			snake_ = c.snake_.copy();
		else
			snake_ = null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the aggregate structure computed from the given multiple structures described in files. */
	public Structure aggregateFromFiles(List<URI> uris) throws Exception {
		
		if (uris == null)
			throw new Exception("ERROR: List of structure URIs is null.");
		if (uris.size() < 2)
			throw new Exception("ERROR: At least two structure URIs are required for a meaningful synthesis.");
		
		WPouchStructure structure = null;
		List<Structure> structures = new ArrayList<Structure>();
		for (URI uri : uris) {
			try {
				structure = new WPouchStructure("structure");
				structure.read(uri);
				structures.add(structure);
			} catch (Exception e) {
				WJSettings.log("ERROR: Unable to create structure from file. Skipping it.");
				WJMessage.showMessage(e);
			}
		}
		
		return aggregate("aggregated-structure", structures);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns a new structure that aggregates the given structures (here mean aggregation is used). */
	public Structure aggregate(String name, List<Structure> structures) throws Exception {
		
		return aggregate(name, structures, Structure.AGGREGATION_MEAN);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets this structure as the aggregate of the given structures (here mean aggregation is used). */
	public void aggregate(List<Structure> structures) throws Exception {
		
		aggregate(structures, Structure.AGGREGATION_MEAN);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public StructureSnake getStructureSnake() { return snake_; }
	public void setStructureSnake(StructureSnake snake) { snake_ = snake; }
	
	public void isStructureKnown(boolean b) { isStructureKnown_ = b; }
	public boolean isStructureKnown() { return isStructureKnown_; }
	
	public void isOrientationKnown(boolean b) { isOrientationKnown_ = b; }
	public boolean isOrientationKnown() { return isOrientationKnown_; }
}