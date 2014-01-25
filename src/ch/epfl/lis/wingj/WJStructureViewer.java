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

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ch.epfl.lis.wingj.structure.Overlay;
import ch.epfl.lis.wingj.structure.Structure;

import ij.ImagePlus;

/** 
 * Displays a Structure object (+ information overlay) on top of an image.
 * 
 * @version Ocotber 21, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJStructureViewer {
	
	/** Structure. */
	protected Structure structure_ = null;
	
	/** Overlay to paint on top of the input images. */
	protected Overlay overlay_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor. If singleImage is true, an ImageWindow object is used instead of a StackWindow. */
	public WJStructureViewer(Structure structure, ImagePlus image, boolean singleImage) {
		
		structure_ = structure;
		overlay_ = structure.getStructureOverlay(image, singleImage);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Constructor. If singleImage is true, an ImageWindow object is used instead of a StackWindow. */
	public WJStructureViewer(Structure structure, ImagePlus image) {
		
		structure_ = structure;
		overlay_ = structure.getStructureOverlay(image, false);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows the detected structure on top of the image using the ROI manager. */
	public void run() throws Exception {
	
		if (overlay_ != null)
			overlay_.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Repaints the viewer. */
	public void update() throws Exception {
		
		if (overlay_ != null)
			overlay_.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Updates the content of the ROI manager. */
	public void updateRoiManagerContent() throws Exception {
	
		if (overlay_ != null)
			overlay_.repaint();
	}

	// ----------------------------------------------------------------------------
	
	/** Sets the color of the overlay. */
	public void setColor(Color color) {
		
		WJSettings.getInstance().setDefaultColor(color);
		overlay_.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the identified structure to image file (extension defines the image format). */
	public void save(URI uri) throws Exception {
		
		if (overlay_ != null)
			overlay_.save(uri);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the content of the viewer as an ImagePlus. */
	public ImagePlus toImagePlus() throws Exception {
		
		return new ImagePlus("structure-viewer-content", Toolkit.getDefaultToolkit().createImage(overlay_.overlayToBufferedImage().getSource()));
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the visibility of the viewer and ROI manager. */
	public void setVisible(boolean b) {
		
		if (overlay_ != null)
			overlay_.getImageWindow().setVisible(b);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows color picker (without preview). */
	public void setColorFromColorChooser() {
		
		WJSettings settings = WJSettings.getInstance();	
		
		final JColorChooser chooser = new JColorChooser();
		chooser.setPreviewPanel(new JPanel());
		chooser.setColor(settings.getDefaultColor());
		chooser.setBackground((new JFrame()).getContentPane().getBackground());
		
        JDialog dialog = JColorChooser.createDialog(WingJ.getInstance(), "Set color", true, chooser, new ActionListener() {
        	/** OK action listener */
        	@Override
			public void actionPerformed(ActionEvent e) {
        		try {
	        		Color color = chooser.getColor();
	        		if (color != null) {
	        			setColor(color);
	        		}
        		} catch (Exception e1) {
        			WJMessage.showMessage(e1);
				}
			}
        }, null); // Cancel action listener
        
        dialog.setVisible(true);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows the shape of the structure. */
	public void isStructureVisible(boolean visible) {
		
		if (overlay_ != null)
			overlay_.isStructureVisible(visible);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Is the shape of the structure visible? */
	public boolean isStructureVisible() {
		
		if (overlay_ != null)
			return overlay_.isStructureVisible();
		
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows text information. */
	public void isTextVisible(boolean visible) {
		
		if (overlay_ != null)
			overlay_.isInformationVisible(visible);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Is the text information visible? */
	public boolean isTextVisible() {
		
		if (overlay_ != null)
			return overlay_.isInformationVisible();
		
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the image. */
	public void setImage(ImagePlus image) throws Exception {
		
		if (overlay_ != null)
			overlay_.setImage(image);
	}
	
	// ============================================================================
	// GETTERS AND SETTERS
	
	public Structure getStructure() { return structure_; }
	public ImagePlus getImage() { return overlay_.getImage(); }
}
