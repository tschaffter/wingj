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

package ch.epfl.lis.wingj.analytics;

/** 
 * Provides statistics about the expression quantification.
 * 
 * @version March 31, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ExpressionStats {
	
	public static final int EXPRESSION_PROFILE = 0;
	public static final int EXPRESSION_MAP = 1;
	public static final int EXPRESSION_MAP_REVERSED = 2;
	public static final int MEAN_MODEL = 3;
	public static final int COMPOSITE = 4;
	
	/** Unique id of the structure system. */
	private String systemId_ = null;
	
	/** Number of expression profiles generated. */
	private int numExpressionProfiles_ = 0;
	/** Number of expression maps generated. */
	private int numExpressionMaps_ = 0;
	/** Number of times an expression map has been reversed. */
	private int numExpressionMapsReversed_ = 0;
	/** Number of structure and expression aggregated models (mean models) generated. */
	private int numMeanModels_ = 0;
	/** Number of RGB composite images generated. */
	private int numComposites_ = 0;
	
	// ============================================================================
	// PRIVATE PUBLIC
	
	/** Constructor. */
	public ExpressionStats(String systemId) {
		
		systemId_ = systemId;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Add the given quantity to the associated number of expression datasets. */
	public void addNumExpressionDatasets(int type, int value) throws Exception {
		
		switch(type) {
		case EXPRESSION_PROFILE: addNumExpressionProfiles(value); break;
		case EXPRESSION_MAP: addNumExpressionMaps(value); break;
		case EXPRESSION_MAP_REVERSED: addNumExpressionMapsReversed(value); break;
		case MEAN_MODEL: addNumMeanModels(value); break;
		case COMPOSITE: addNumComposites(value); break;
		default: throw new Exception("ERROR: Unknown expression dataset type.");
		}
	}

	// ============================================================================
	// SETTERS AND GETTERS
	
	public void addNumExpressionProfiles(int value) { numExpressionProfiles_ += value; }
	public void addNumExpressionMaps(int value) { numExpressionMaps_ += value; }
	public void addNumExpressionMapsReversed(int value) { numExpressionMapsReversed_ += value; }
	public void addNumMeanModels(int value) { numMeanModels_ += value; }
	public void addNumComposites(int value) { numComposites_ += value; }
	
	public String getSystemId() { return systemId_; }
	public int getNumExpressionProfiles() { return numExpressionProfiles_; }
	public int getNumExpressionMaps() { return numExpressionMaps_; }
	public int getNumExpressionMapsReversed() { return numExpressionMapsReversed_; }
	public int getNumMeanModels() { return numMeanModels_; }
	public int getNumComposites() { return numComposites_; }
}
