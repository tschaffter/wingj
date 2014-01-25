/*
		deleteStructureDetector();
		structureDetector_ = new WPouchStructureDetector(experimentName);Copyright (c) 2010-2013 Thomas Schaffter & Ricard Delgado-Gonzalo

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

import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.structure.Structure;
import ch.epfl.lis.wingj.structure.drosophila.embryo.EmbryoSystem;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructure;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchStructureDetector;
import ch.epfl.lis.wingj.structure.drosophila.wingpouch.WPouchSystem;

/** 
 * Lists the systems (biological organisms or organs) implemented in WingJ.
 * <p>
 * Uses Singleton design pattern.
 * 
 * @version October 25, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WJSystemManager {
	
	/** The unique instance of WJSystemManager (Singleton design pattern). */
	private static WJSystemManager instance_ = null;
	
	/** List of systems registered. */
	private List<WJSystem> systems_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Default constructor. */
	private WJSystemManager() {
		
		systems_ = new ArrayList<WJSystem>();
		
		int systemId_ = 0;
		
		// ============================================================================
		// DROSOPHILA WING POUCH
		
		try {
			systems_.add(new WPouchSystem(systemId_++));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ============================================================================
		// DROSOPHILA EMBRYO
		
		try {
			systems_.add(new EmbryoSystem(systemId_++));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ============================================================================
		// ADD SYSTEM HERE
		
		// ============================================================================
		// OTHER SYSTEM
		
		try {
			systems_.add(new OtherSystem(systemId_++));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Returns the unique instance of the system manager. */
	public static WJSystemManager getInstance() {
		
		if (instance_ == null)
			instance_ = new WJSystemManager();
		return instance_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns an array containing the name of the WJSystem objects (e.g. to initialize JComboBox). */
	public String[] getSystemNames() {
		
		String[] names = new String[systems_.size()];
		for (int i = 0; i < names.length; i++)
			names[i] = systems_.get(i).getName();
		
		return names;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the reference of the given system. */
	public WJSystem getSystem(int id) {
		
		if (id > systems_.size()-1) return null;
		if (systems_.get(id) == null) return null;
		return systems_.get(id);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the description associated to the given system id. */
	public String getSystemName(int id) {
		
		if (id > systems_.size()-1) return null;
		if (systems_.get(id) == null) return null;
		return systems_.get(id).getName();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the description associated to the given system id. */
	public String getSystemDescription(int id) {
		
		if (id > systems_.size()-1) return null;
		if (systems_.get(id) == null) return null;
		return systems_.get(id).getDescription();
	}
	
//	/** Returns the description associated to the given system name. */
//	public String getSystemDescription(String name) {
//		
//		for (WJSystem system : systems_) {
//			if (system.getName().compareTo(name) == 0)
//				return system.getDescription();
//		}
//		return "ERROR: System not found.";
//	}
	
	// ============================================================================
	// INNER CLASSES
	
	private class OtherSystem extends WJSystem {

		/** Constructor. */
		public OtherSystem(int id) {
			super(id, "OtherSystem");
			name_ = "Other system";
			description_ = "<html>Represents any system which doesn't have an unsupervised<br>" +
				"structure detection method implemented in WingJ.</html>";
			providesUnsupervisedStructureDetection_ = false;
		}

		@Override
		public void newStructureDetector(String experimentName) throws Exception {
			
			deleteStructureDetector();
			structureDetector_ = new WPouchStructureDetector(experimentName, "other-system");
		}

		@Override
		public Structure newStructure() throws Exception {
			
			WPouchStructure structure = new WPouchStructure(new String(name_).replaceAll(" ", "_"));
			structure.setSystemName("other-system");
			return structure;
		}
	}
}
