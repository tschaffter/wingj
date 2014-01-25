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

package ch.epfl.lis.wingj.structure.drosophila.wingpouch;

import ij.ImagePlus;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.structure.Overlay;
import ch.epfl.lis.wingj.structure.Structure;

/** 
 * Adds information on top of the preview of the wing pouch structure.
 * 
 * @see ch.epfl.lis.wingj.structure.Overlay
 * @see ch.epfl.lis.wingj.WJStructureViewer
 * 
 * @version November 23, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WPouchOverlay extends Overlay {
	
	/** Default serial. */
	private static final long serialVersionUID = 1L;

	/** Centroid of the DA compartment. */
	private Point2D.Double daCentroid_ = null;
	/** Centroid of the DP compartment. */
	private Point2D.Double dpCentroid_ = null;
	/** Centroid of the VA compartment. */
	private Point2D.Double vaCentroid_ = null;
	/** Centroid of the VP compartment. */
	private Point2D.Double vpCentroid_ = null;

	// ============================================================================
	// PUBLIC METHODS

	/** Constructor. */
	public WPouchOverlay(Structure structure, ImagePlus imp, boolean useImageWindow) {

		super(structure, imp, useImageWindow);
	}

	// ----------------------------------------------------------------------------
	
	/** Constructor. */
	public WPouchOverlay(Structure structure, ImagePlus imp) {

		super(structure, imp);
	}

	// ----------------------------------------------------------------------------
	
	/** Paints the structure of the drosophila wing pouch on the overlay. */
	@Override
	protected void paintStructure(Graphics g) {
		
		WJSettings settings = WJSettings.getInstance();
		WPouchStructure structure = (WPouchStructure)structure_;
		
		// set font
		g.setColor(settings.getDefaultColor());
		
		// set pen stroke
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(new BasicStroke(settings.getDefaultStrokeWidth()));
		
		// wing pouch contour
		g.drawPolygon(magnifyPolygon(structure, rectOffset_, magnification));
		// D/V and A/P boundaries
		Polygon b = magnifyPolygon(structure.getDVBoundary(), rectOffset_, magnification);
		g.drawPolyline(b.xpoints, b.ypoints, b.npoints);
		b = magnifyPolygon(structure.getAPBoundary(), rectOffset_, magnification);
		g.drawPolyline(b.xpoints, b.ypoints, b.npoints);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Paints the name of the compartments around the location of their center of mass. */
	@Override
	protected void paintInformation(Graphics g) {
		
		WJSettings settings = WJSettings.getInstance();
		
		WPouchStructure s = (WPouchStructure)structure_;
		daCentroid_ = s.getDACompartment().centroid();
		dpCentroid_ = s.getDPCompartment().centroid();
		vaCentroid_ = s.getVACompartment().centroid();
		vpCentroid_ = s.getVPCompartment().centroid();
		
		// set font
		g.setFont(new Font(null, Font.PLAIN, (int)Math.round(settings.getStructureOverlayFontSizeCoeff()*magnification_)));
		g.setColor(settings.getDefaultColor());
		FontMetrics fm = g.getFontMetrics();
		
		// paint
		g.drawString("DA", (int)(magnification_*(daCentroid_.x-rectOffset_.x))-fm.stringWidth("DA")/2, (int)(magnification_*(daCentroid_.y-rectOffset_.y))+fm.getAscent()/3);
		g.drawString("DP", (int)(magnification_*(dpCentroid_.x-rectOffset_.x))-fm.stringWidth("DP")/2, (int)(magnification_*(dpCentroid_.y-rectOffset_.y))+fm.getAscent()/3);
		g.drawString("VA", (int)(magnification_*(vaCentroid_.x-rectOffset_.x))-fm.stringWidth("VA")/2, (int)(magnification_*(vaCentroid_.y-rectOffset_.y))+fm.getAscent()/3);
		g.drawString("VP", (int)(magnification_*(vpCentroid_.x-rectOffset_.x))-fm.stringWidth("VP")/2, (int)(magnification_*(vpCentroid_.y-rectOffset_.y))+fm.getAscent()/3);
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public void setDACentroid(Point2D.Double p) { daCentroid_ = p; }
	public void setDPCentroid(Point2D.Double p) { dpCentroid_ = p; }
	public void setVACentroid(Point2D.Double p) { vaCentroid_ = p; }
	public void setVPCentroid(Point2D.Double p) { vpCentroid_ = p; }
}