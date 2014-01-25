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
import javax.swing.filechooser.*;

import ch.epfl.lis.wingj.utilities.FilenameUtils;

/** 
 * PNG file filter for JFileChooser.
 * 
 * @version June 13, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class FilterImagePng extends FileFilter {

	/** File extension. */
	public static final String ext = "png";
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Returns true if the given file is accepted by this filter. */
	@Override
    public boolean accept(File f) {
		
        if (f.isDirectory())
            return true;

        String extension = FilenameUtils.getExtension(f);
        if (extension != null) {
            if (extension.equals(ext))
                    return true;
            else
                return false;
        }
        return false;
    }
	
	// ----------------------------------------------------------------------------

    /** Description of this filter. */
    @Override
	public String getDescription() {
    	
    	return ("PNG (png)");
    }
}