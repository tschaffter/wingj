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

package ch.epfl.lis.wingj.batch;

/**
 * Represents a single gene/channel/image stack to open in WingJ.
 * 
 * @version December 3, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class Gene {
	
	/** Name of the gene. */
	protected String name_ = "";
	
	/** Tag used to identify the images to load ('.' for selecting all images). */
	protected String selectionTag_ = ".";
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public Gene(String name, String selectionTag) {
		
		name_ = name;
		selectionTag_ = selectionTag;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getName() { return name_; }
	public String getSelectionTag() { return selectionTag_; }
}
