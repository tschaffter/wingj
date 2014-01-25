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

package ch.epfl.lis.wingj.utilities;

import ij.ImagePlus;
import ij.plugin.ZProjector;

/** 
 * Computes image stack (3D image) projections.
 * 
 * @version May 30, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class Projections {
	
	/** For each pixel position XY, computes the mean pixel value along Z. */
	public static final int PROJECTION_MEAN_METHOD = ZProjector.AVG_METHOD;
	/** For each pixel position XY, keep the max pixel value along Z. */
	public static final int PROJECTION_MAX_METHOD = ZProjector.MAX_METHOD;
   	
    /** Computes the projection from a stack of images using the given projection method. */
   	public static ImagePlus doProjection(ImagePlus stack, int method) throws Exception {
   		
   		ImagePlus image = doProjection(stack, method, 1, stack.getNSlices());
   		return image;
   	}
   	
   	// ----------------------------------------------------------------------------
	
    /** Computes the projection from a stack of images defined by min and max slices using the given projection method. */
   	public static ImagePlus doProjection(ImagePlus stack, int method, int minSlice, int maxSlice) throws Exception {
   		
   		if (stack.getProcessor() == null)
   			throw new Exception("ERROR: Projection requires a valid stack.");
   		
   		int numSlices = stack.getNSlices();
   		
   		if (numSlices < 1)
   			throw new Exception("WARNING: Projection requires an image or image stack having at least one slice.");
   		if (minSlice < 1 || minSlice > numSlices)
   			throw new Exception("WARNING: Min slice index must be >= 1 and <= " + Integer.toString(numSlices) + ".\n" +
   					"Min slice index is " + minSlice + ".");
   		if (maxSlice < 1 || maxSlice > numSlices)
   			throw new Exception("WARNING: Max slice index must be >= 1, <= " + Integer.toString(numSlices) + ".\n" +
   					"Max slice index is " + maxSlice + ".");
   		if (maxSlice < minSlice)
   			throw new Exception("WARNING: Min slice index must be <= max slice index.\n" +
   					"Min slice index is " + minSlice + ".\n" +
   					"Max slice index is " + maxSlice + ".");
   		
       	// maximum projection over the z-axis
   		ZProjector projector = new ZProjector(stack);
       	projector.setMethod(method);

       	projector.setStartSlice(minSlice);
       	projector.setStopSlice(maxSlice);
       	projector.doProjection();
       	
       	projector.getProjection().setDisplayRange(0., 255.);

       	return projector.getProjection();  	
   	}
}
