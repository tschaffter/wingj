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

package ch.epfl.lis.wingj.filefilters;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/** 
 * Generic filename filter for JFileChooser.
 * 
 * @version July 9, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class FilterGenericFilename extends FileFilter {

	// ============================================================================
	// PUBLIC METHODS
	
	/** Returns true if the given file is accepted by this filter. */
	@Override
    public boolean accept(File f) {
		
        return true;
    }

	// ----------------------------------------------------------------------------
	
    /** Description of this filter. */
    @Override
	public String getDescription() {
    	
    	return ("Generic (without extension)");
    }
}