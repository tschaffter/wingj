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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

/** 
 * Provides statistics about the structure detection of a given system.
 * 
 * @version March 31, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class StructureDetectionStats {
	
	public static final int AUTO_STRUCTURE_DETECTION = 0;
	public static final int SUPERVISED_STRUCTURE_DETECTION = 1;
	public static final int MANUAL_STRUCTURE_DETECTION = 2;
	public static final int OPENED_STRUCTURE = 3;
	
	/** Unique id of the structure system. */
	private String systemId_ = null;
	
	/** Number of automatic detections (can be a fraction). */
	private double numAutoDetections_ = 0.;
	/** Number of supervised (step by step) detections (can be a fraction). */
	private double numSupervisedDetections_ = 0.;
	/** Number of manual detection (is an integer). */
	private int numManualDetections_ = 0;
	/** Number of structures opened from files (is an integer). */
	private int numOpenedStructures_ = 0;
	
	// ============================================================================
	// PRIVATE PUBLIC
	
	/** Constructor. */
	public StructureDetectionStats(String systemId) {
		
		systemId_ = systemId;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Add the given quantity to the associated number of detections. */
	public void addNumStructureDetections(int type, double value) throws Exception {
		
		switch(type) {
		case AUTO_STRUCTURE_DETECTION: addNumAutoDetections(value); break;
		case SUPERVISED_STRUCTURE_DETECTION: addNumSupervisedDetections(value); break;
		case MANUAL_STRUCTURE_DETECTION: addNumManualDetections((int)value); break;
		case OPENED_STRUCTURE: addNumOpenedStructures((int)value); break;
		default: throw new Exception("ERROR: Unknown structure detection type.");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Main method. */
	public static void main(String[] args) {
		
		Gson gson = new Gson();
		List<StructureDetectionStats> list = new ArrayList<StructureDetectionStats>();
		list.add(new StructureDetectionStats("a"));
		list.add(new StructureDetectionStats("b"));
		
		System.out.println("list: " + gson.toJson(list));
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void addNumAutoDetections(double value) { numAutoDetections_ += value; }
	public void addNumSupervisedDetections(double value) { numSupervisedDetections_ += value; }
	public void addNumManualDetections(int value) { numManualDetections_ += value; }
	public void addNumOpenedStructures(int value) { numOpenedStructures_ += value; }
	
	public String getSystemId() { return systemId_; }
	public double getNumAutoDetections() { return numAutoDetections_; }
	public double getNumSupervisedDetections() { return numSupervisedDetections_; }
	public int getNumManualDetections() { return numManualDetections_; }
	public int getNumOpenedStructures() { return numOpenedStructures_; }
}
