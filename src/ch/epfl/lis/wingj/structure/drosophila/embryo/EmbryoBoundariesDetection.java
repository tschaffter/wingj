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
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.tools.EmbryoSnake;

/** 
 * Builds A/P and D/V boundaries for the <i>Drosophila</i> embryo.
 * 
 * @version March 5, 2013
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class EmbryoBoundariesDetection extends StructureDetectionModule {
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public EmbryoBoundariesDetection() {};
	
	// ----------------------------------------------------------------------------
	
	/** Constructor with module name and the reference to a detector and visibility status. */
	public EmbryoBoundariesDetection(String name, EmbryoStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Building Drosophila embryo A/P and D/V boundary.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor with module name and the reference to a detector. */
	public EmbryoBoundariesDetection(String name, EmbryoStructureDetector detector) {
		
		super(name, detector);
		description_ = "Building Drosophila embryo A/P and D/V boundary.";
	}
	
	// ----------------------------------------------------------------------------

	/** Builds the A/P and D/V boundary of the embryo. */
	@Override
	public void run() throws Exception {
		
		EmbryoStructureDetector detector = (EmbryoStructureDetector)detector_;
		EmbryoStructureSnake snake = (EmbryoStructureSnake)detector.getTmpStructureSnake();
		
		EmbryoSnake embryoSnake = detector.getEmbryoSnake();
		
		Point2D.Double snakeCentroid = embryoSnake.getCentroid();
		Point2D.Double N = new Point2D.Double(embryoSnake.getScales()[1].xpoints[embryoSnake.getExtremeIndices(0)],embryoSnake.getScales()[1].ypoints[embryoSnake.getExtremeIndices(0)]);
		Point2D.Double S = new Point2D.Double(embryoSnake.getScales()[1].xpoints[embryoSnake.getExtremeIndices(1)],embryoSnake.getScales()[1].ypoints[embryoSnake.getExtremeIndices(1)]);
		Point2D.Double E = new Point2D.Double(embryoSnake.getScales()[1].xpoints[embryoSnake.getExtremeIndices(2)],embryoSnake.getScales()[1].ypoints[embryoSnake.getExtremeIndices(2)]);
		Point2D.Double W = new Point2D.Double(embryoSnake.getScales()[1].xpoints[embryoSnake.getExtremeIndices(3)],embryoSnake.getScales()[1].ypoints[embryoSnake.getExtremeIndices(3)]);

		
		List<Point2D> northPath = new ArrayList<Point2D>();
		northPath.add(snakeCentroid);
		northPath.add(N);
		Boundary northBoundary = new Boundary("N",northPath);
		northBoundary = northBoundary.resample((int)Math.round(northBoundary.lengthInPx()/2.0));
		snake.setInitialBoundary(0, northBoundary);
		
		List<Point2D> southPath = new ArrayList<Point2D>();
		southPath.add(snakeCentroid);
		southPath.add(S);
		Boundary southBoundary = new Boundary("S",southPath);
		southBoundary = southBoundary.resample((int)Math.round(southBoundary.lengthInPx()/2.0));
		snake.setInitialBoundary(2, southBoundary);
		
		List<Point2D> eastPath = new ArrayList<Point2D>();
		eastPath.add(snakeCentroid);
		eastPath.add(E);
		Boundary eastBoundary = new Boundary("E",eastPath);
		eastBoundary = eastBoundary.resample((int)Math.round(eastBoundary.lengthInPx()/2.0));
		snake.setInitialBoundary(1, eastBoundary);
		
		List<Point2D> westPath = new ArrayList<Point2D>();
		westPath.add(snakeCentroid);
		westPath.add(W);
		Boundary westBoundary = new Boundary("W",westPath);
		westBoundary = westBoundary.resample((int)Math.round(westBoundary.lengthInPx()/2.0));
		snake.setInitialBoundary(3, westBoundary);
	}
}
