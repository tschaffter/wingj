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

import javax.swing.UIManager;

import ch.epfl.lis.wingj.gui.WJAboutBox;

/** 
 * Runs a Singleton instance of WingJ.
 * 
 * @version June 21, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJPlugIn implements ij.plugin.PlugIn {

	// ============================================================================
	// PUBLIC METHODS
	
	/** Runs the Singleton instance of WingJ. */
	@Override
	public void run(String arg) {
		
	    try {
	    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	    	System.out.println("Error setting native LAF: " + e);
	    }
		
		WingJ wj = WingJ.getInstance();
		wj.run();
	
		wj.setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows WingJ About box. */
	public static void showAbout() {
		
    	WJAboutBox about = new WJAboutBox();
    	about.run();
	}	
}