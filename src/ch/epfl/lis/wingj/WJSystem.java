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

package ch.epfl.lis.wingj;

import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.StructureDetector;

/** 
 * Biological sysmtem (organ system or body system).
 * 
 * @version February 13, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
abstract public class WJSystem {
	
	/**
	 * Unique string given to this system to identify.
	 * EACH system MUST have a different unique id.
	 */
	protected String uniqueId_ = null;
	
	/** System id set in the system manager and used to differentiate between local systems. */
	protected int id_ = -1;
	/** System name. */
	protected String name_ = "<noname>";
	/** Description. */
	protected String description_ = "<nodescription>";
	
	/** Current instance of the structure detector. */
	protected StructureDetector structureDetector_ = null;
	
	/** True if an unsupervised structure detection pipeline is provided. */
	protected boolean providesUnsupervisedStructureDetection_ = true;
	
	// ============================================================================
	// ABSTRACT METHODS
	
	/** Instantiates a new structure detector. */
	abstract public void newStructureDetector(String experimentName) throws Exception;
	/** Returns a new structure object. */
	abstract public Structure newStructure() throws Exception;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Initialize method. */
	private void initialize(int id, String uniqueId, String name, String description) {
		
		initialize(id, uniqueId, name);
		description_ = description;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialize method. */
	private void initialize(int id, String uniqueId, String name) {
		
		initialize(id, uniqueId);
		name_ = name;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialize method. */
	private void initialize(int id, String uniqueId) {
		
		id_ = id;
		uniqueId_ = uniqueId;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Deletes the current structure detector. */
	public void deleteStructureDetector() throws Exception {
		
   		if (structureDetector_ != null) {
   			structureDetector_.abort();
   			structureDetector_.clean();
   			structureDetector_ = null;
   		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. */
	public WJSystem(int id, String uniqueId, String name, String description) {
		
		initialize(id, uniqueId, name, description);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WJSystem(int id, String uniqueId, String name) {
		
		initialize(id, uniqueId, name);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WJSystem(int id, String uniqueId) {
		
		initialize(id, uniqueId);
	}
	
	// ----------------------------------------------------------------------------
	
	/** toString method returns the name of the system. */
	@Override
	public String toString() {
		
		return name_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Equals operator. */
	public boolean equals(WJSystem system) {
		
		if (system == null) return false;
		if (!(system instanceof WJSystem)) return false;
		return id_ == system.id_;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getUniqueId() { return uniqueId_; }
	
	public int getId() { return id_; }
	
	public void setName(String name) { name_ = name; }
	public String getName() { return name_; }
	
	public void setDescription(String description) { description_ = description; }
	public String getDescription() { return description_; }
	
	public StructureDetector getStructureDetector() { return structureDetector_; }
	
	public void providesUnsupervisedStructureDetection(boolean b) { providesUnsupervisedStructureDetection_ = b; }
	public boolean providesUnsupervisedStructureDetection() { return providesUnsupervisedStructureDetection_; }
}
