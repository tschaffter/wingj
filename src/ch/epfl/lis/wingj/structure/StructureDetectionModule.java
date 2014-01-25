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

/** 
 * Abstract class for implementing one step of the detection of a morphological structure.
 * <p>
 * The order in which the detection modules are applied is defined in the class extending
 * StructureDetector. An example is given in WPouchStructureDetector.
 * <p>
 * If the module is declared as HIDDEN, the user will not be prompted during an automatic
 * detection and no visual elements will be displayed.
 * <p>
 * A module is executed by a structure detector using run(). After the completion of
 * run(), the method test() returns true if the output of the module is considered
 * successful, otherwise test() returns false. The override of test() by a module is
 * optional (by default it will return true). If test() returns true, the method run() of the
 * next module is called. If test() returns false, the method update() is called where
 * adjustments can be made, for instance changing parameter values. Then the method run()
 * will be called again until test() returns true. It is the responsability of the module to
 * check that the module will not fall in an endless loop. Typically, an Exception can be
 * thrown to stop the structure detection.
 * 
 * @version June 9, 2011
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
abstract public class StructureDetectionModule {
	
	/** Structure detector. */
	protected StructureDetector detector_ = null;
	
	/** Name/identifier of the detection step. */
	protected String name_ = "";
	/** Description of the detection step (returned by toString()). */
	protected String description_ = "";
	/** If hidden_ is true, user is not prompted (even in interactive mode) and no visual elements are shown. */
	protected boolean hidden_ = false;
	
	// ============================================================================
	// PROTECTED METHODS
	
	/** This function must implement the detection process. */
	abstract public void run() throws Exception;
	
	// ============================================================================
	// PUBLIC METHODS
	
   	/** Default constructor. */
   	public StructureDetectionModule() {}
   	
   	// ----------------------------------------------------------------------------
   	
	/** Constructor. */
	public StructureDetectionModule(String name, StructureDetector detector, boolean hidden) {
		
		name_ = name;
		detector_ = detector;
		hidden_ = hidden;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public StructureDetectionModule(String name, StructureDetector detector) {
		
		name_ = name;
		detector_ = detector;
		hidden_ = false;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Overrides toString(). */
	@Override
	public String toString() {
		
		return description_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Tests performed after run() to check the validity of the output of this module.
	 * <p>
	 * If the output of the test is not true, the update() will be called to make adjustments
	 * before running run() once again followed by test().
	 */
	public boolean test() {
		
		boolean ok = true;
		
		// Add tests here
		
		return ok;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Updates parameter values before re-starting the detection. */
	public void update() {}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the visibility of the images created by this detection module (if any). */
	public void setImagesVisible(boolean visible) {}
	
	// ----------------------------------------------------------------------------
	
	/** Removes all images created by this detection module (if any). */
	public void removeImages() {}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public StructureDetector getDetector() { return detector_; }
	
	public void setName(String name) { name_ = name; }
	public String getName() { return name_; }
	
	public void setDescription(String description) { description_ = description; }
	public String getDescription() { return description_; }
	
	public void isHidden(boolean hidden) { hidden_ = hidden; }
	public boolean isHidden() { return hidden_; }
}
