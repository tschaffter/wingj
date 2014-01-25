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

package ch.epfl.lis.wingj.expression;

import java.awt.Color;
import java.awt.event.WindowListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Boundary;
import ch.epfl.lis.wingj.structure.Compartment;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.macro.Interpreter;
import ij.plugin.Duplicator;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 * Shows the spatial domain where expression profiles are quantified.
 * 
 * @version August 31, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class ExpressionDomain {
	
	/** Instances of ExpressionDomain. */
	private static List<ExpressionDomain> instances_ = new ArrayList<ExpressionDomain>();
	
	/** Projection of the expression image stack. */
	private ImageWindow imageWindow_ = null;
	
	/** Window title. */
	private String title_ = "expression_domain";
	
	/** Contour of the structure. */
	private Compartment contour_ = null;
	/** Reference boundary along which the expression is measured. */
	private Boundary boundary_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public ExpressionDomain() {
		
		instances_.add(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sets the background image of the domain viewer. */
	public void setImage(ImagePlus image) throws Exception {
		
		if (image == null)
			throw new Exception("ERROR: image is null."); 
		
		ImagePlus clone = new Duplicator().run(image);
		ImageConverter converter = new ImageConverter(clone);
		converter.convertToRGB();
		
		if (imageWindow_ == null) {
			boolean batchMode = Interpreter.isBatchMode();
			Interpreter.batchMode = true;
			imageWindow_ = new ImageWindow(clone);
			Interpreter.batchMode = batchMode;
		}
		else {
			imageWindow_.getImagePlus().setProcessor(clone.getProcessor());
		}
		
		imageWindow_.setTitle(title_);
		imageWindow_.repaint();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Generates expression domain visualization. */
	public void generateVisualization(boolean visible) throws Exception {
		
		if (imageWindow_ == null)
			throw new Exception("ERROR: stackWindow_ is null.");
		if (imageWindow_.getImagePlus() == null || imageWindow_.getImagePlus().getProcessor() == null)
			throw new Exception("ERROR: stackWindow_.getImagePlus() is null.");
		
		ImagePlus image = imageWindow_.getImagePlus();
		ImageProcessor ip = image.getProcessor();
		ip.setColor(WJSettings.getInstance().getDefaultColor());
		
		if (contour_ != null) ip.draw(contour_.toRoi());
		
		if (boundary_ != null) {
			Roi pr = new PolygonRoi(boundary_.resample((int)Math.round((double)boundary_.lengthInPx()/Math.PI)).toPolygon(), Roi.FREELINE);
			pr.setStrokeColor(new Color(WJSettings.getInstance().getDefaultColor().getRed(),
										WJSettings.getInstance().getDefaultColor().getGreen(),
										WJSettings.getInstance().getDefaultColor().getBlue(), 128));
			Roi.setColor(WJSettings.getInstance().getDefaultColor());
			pr.updateWideLine((int)Math.round(6.0*WJSettings.getInstance().getExpression1DSigma()));
			image.setRoi(pr);
		}
		ImagePlus finalImage = image.flatten();
		image.killRoi();
		finalImage.setTitle(title_);
		setImage(finalImage);
		
		// show the image if required
		imageWindow_.setVisible(visible);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes the visualization. */
	public void close() {
		
		if (imageWindow_ != null && !imageWindow_.isClosed() ) {
			
			// it has been see that an exception could be thrown some
			// time after the successful closing of stackWindow_. I don't
			// get why so I simply remove all the listener of the visualization.
			WindowListener[] listeners = imageWindow_.getWindowListeners();
			for (int i = 0; i < listeners.length; i++)
				imageWindow_.removeWindowListener(listeners[i]);
			
			imageWindow_.close();
			imageWindow_ = null;
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves domain where expression is measured as PNG. */
	public void saveTIFF(URI uri) throws Exception {
		
		IJ.save(imageWindow_.getImagePlus(), uri.getPath());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes all instances of ExpressionDomain in instances_. */
	public static void disposeAll() throws Exception {
		
		for (int i = 0; i < instances_.size(); i++) {
			try {
				instances_.get(i).close(); // instances_.get(i) could be null
			} catch (Exception e) {}
		}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	public void setTitle(String title) { title_ = title; }
	public void setCompartment(Compartment contour) { contour_ = contour; }
	public void setTrajectory(Boundary boundary) { boundary_ = boundary; }
}