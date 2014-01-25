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

import java.util.HashMap;

/** 
 * Utility methods to convert values to different units.
 * 
 * @version November 1, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class UnitConverter {

	/** Returns the index associated to a unit. */
	@SuppressWarnings("serial")
	public static final HashMap<String,Integer> UNIT_INDEXES = new HashMap<String,Integer>() 
		{{put("nm", 0);
		  put("um", 1);
		  put("micron", 1);
		  put("�m", 1);
		  put("µm",1);
		  put("�m",1);
		  put("mm", 2);
		  put("cm", 3);
		  put("meter", 4);
		  put("m", 4);
		  put("km", 5);
		  put("inch", 6);
		  put("pixel", 7);
		  put("px", 7);}};
		  
	/** 1 inch in mm. */
	public static final double ONE_INCH_IN_MM = 25.4;
	/** 1 mm in inch. */
	public static final double ONE_MM_IN_INCH = 1./ONE_INCH_IN_MM;

	/** Conversion from a unit to another. Diagonal is set with 1. */
	public static final double[][] UNIT_CONVERSION_COEFF = new double[][] 
	//    nm		um		mm		cm		meter	km		inch					px
	    {{1.,		1e-3,	1e-6, 	1e-7, 	1e-9, 	1e-12,	ONE_MM_IN_INCH*1e-6,	1.},
		 {1e3,		1., 	1e-3, 	1e-4, 	1e-6, 	1e-9, 	ONE_MM_IN_INCH*1e-3,	1.},
		 {1e6, 		1e3, 	1., 	1e-1, 	1e-3, 	1e-6, 	ONE_MM_IN_INCH,			1.},
		 {1e7, 		1e4, 	1e1, 	1., 	1e-2, 	1e-5, 	ONE_MM_IN_INCH*1e1,		1.},
		 {1e9, 		1e6, 	1e3, 	1e2, 	1., 	1e-2, 	ONE_MM_IN_INCH*1e3,		1.},
		 {1e12, 	1e9, 	1e6, 	1e5, 	1e2, 	1.,		ONE_MM_IN_INCH*1e6,		1.},
		 {ONE_INCH_IN_MM*1e6, ONE_INCH_IN_MM*1e3, ONE_INCH_IN_MM*1e3, ONE_INCH_IN_MM, ONE_INCH_IN_MM*1e-1, ONE_INCH_IN_MM*1e-3, ONE_INCH_IN_MM*1e-6, 1},
		 {1., 		1.,		1.,		1.,		1.,		1.,		1., 					1.}};
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Converts a distance value in the target unit. */
	public static double convertDistanceValue(double value, String unit, String targetUnit) throws Exception {
		
		// nothing to convert
		if (unit.compareTo(targetUnit) == 0) return value;
		
		try {
			int unitIndex = UNIT_INDEXES.get(unit);
			int targetUnitIndex = UNIT_INDEXES.get(targetUnit);
			return UNIT_CONVERSION_COEFF[unitIndex][targetUnitIndex] * value;
			
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new Exception("ERROR: Either the current (" + unit + ") or target (" + targetUnit + ") unit is invalid.");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("ERROR: Unkown error.\nPlease refer to the console.");
		}
	}
}