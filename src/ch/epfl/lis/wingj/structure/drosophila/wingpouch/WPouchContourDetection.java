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

import java.awt.Polygon;
import java.awt.Rectangle;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.StructureDetectionModule;
import ch.epfl.lis.wingj.structure.tools.ContourTracer;
import ch.epfl.lis.wingj.structure.tools.Dilation;

import ch.epfl.lis.wingj.structure.Compartment;

import ij.gui.ShapeRoi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/** 
 * Detects the contour of the wing pouch.
 * <p>
 * <ul>
 * <li>Computes the exterior contour by dilating the four compartments</li>
 * <li>Computes the interior contour by growing a mass connecting all four compartments</li>
 * </ul>
 * 
 * @version October 4, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class WPouchContourDetection extends StructureDetectionModule {
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Computes the internal contour of the wing pouch. */
	private void detectWPouchContour() throws Exception {
		
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;;
		WPouchStructureSnake snake = (WPouchStructureSnake)detector.getTmpStructureSnake();
	
		// compute the hull from the four compartments returned by the kite snake
		ShapeRoi allCompartments = snake.getAllCompartmentsAsShape();
		
		Rectangle rect = allCompartments.getBounds();
		ByteProcessor totalRegions = new ByteProcessor(detector.structureProjection_.getWidth(), detector.structureProjection_.getHeight());
   		ImageProcessor mask = allCompartments.getMask();
   		totalRegions.insert(mask, rect.x, rect.y);
   		FloatProcessor regionsCorrectSize = (FloatProcessor) totalRegions.duplicate().convertToFloat();
		
		Polygon regionConnection = new Polygon();
		for(int i = 0; i < 4; i++)
			regionConnection.addPoint((int)Math.round(snake.getInitialCompartment(i).centroid().x), (int)Math.round(snake.getInitialCompartment(i).centroid().y));
		
		// connects the four compartments to generate a four-leaf clover
   		for (int i = 0; i < totalRegions.getWidth(); i++) {
   			for (int j = 0; j < totalRegions.getHeight(); j++) {
   				if(regionConnection.contains(i, j)) {
   					regionsCorrectSize.putPixelValue(i, j, 255);
   				}
   			}
   		}
   		
   		// dilates to compensate the gap between the outer boundary model and the effective contour
   		// of the wing pouch
   		FloatProcessor dilated = Dilation.dilate(regionsCorrectSize, (int)WJSettings.getInstance().getOuterBoundaryExpansion());

   		// get the contour of the binary shape
   		ContourTracer tracer = new ContourTracer((float[])dilated.getPixels(), dilated.getWidth(), dilated.getHeight());
   		tracer.trace();
   		
   		Compartment internalBoundary = new Compartment("", tracer.getTrace());
   		snake.setInitialContour(internalBoundary.createConvexHull());
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WPouchContourDetection(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Detecting the contour of the wing pouch.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchContourDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Detecting the contour of the wing pouch.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Detects the contour of the wing pouch. */
	@Override
	public void run() throws Exception {
		
		detectWPouchContour();
	}
}