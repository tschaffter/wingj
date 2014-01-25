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

import ij.ImagePlus;

import java.awt.geom.Point2D;

import ch.epfl.lis.wingj.structure.StructureDetectionModule;

/** 
 * Computes the center of the wing disc (required for the inference of the wing orientation).
 * 
 * @version September 8, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WDiscCenterDetection extends StructureDetectionModule {
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Computes the center of mass of an image. */
	private Point2D.Double computeCenterOfMass(ImagePlus image) {
		
		float[] pixels = (float[])image.getProcessor().getPixels();
		int width = image.getWidth();
		int height = image.getHeight();		
		
		double M00 = 0;
		double M10 = 0;
		double M01 = 0;
		
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				M00 += pixels[x+width*y];
				M10 += x*pixels[x+width*y];
				M01 += y*pixels[x+width*y];
			}
		}
		return new Point2D.Double(M10/M00, M01/M00);
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WDiscCenterDetection(String name, WPouchStructureDetector detector, boolean hidden) {
		
		super(name, detector, hidden);
		description_ = "Computing the center of mass of the wing imaginal disc.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WDiscCenterDetection(String name, WPouchStructureDetector detector) {
		
		super(name, detector);
		description_ = "Computing the center of mass of the wing imaginal disc.";
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the center of the wing disc (not wing pouch). */
	@Override
	public void run() throws Exception {
		
		WPouchStructureDetector detector = (WPouchStructureDetector)detector_;
		
		// compute the center of mass of the structure projection
		WPouchStructure structure = (WPouchStructure)detector.getStructure();
		structure.discCenter_ = computeCenterOfMass(detector.structureProjection_);
	}
}
