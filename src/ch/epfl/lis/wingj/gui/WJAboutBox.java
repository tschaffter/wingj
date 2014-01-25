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

package ch.epfl.lis.wingj.gui;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.tschaffter.gui.HyperText;

import java.awt.BorderLayout;

/** 
 * WingJ About box.
 * 
 * @version August 28, 2011
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WJAboutBox extends JDialog {

	/** Default serial version ID. */
	private static final long serialVersionUID = 1L;
	
	/** About image. */
	private Image image_;
	/** About panel. */
	private AboutImagePanel aboutImagePanel_;
	
	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public WJAboutBox() {
		
		super();
		setModal(true);
		
		Color bgColor = UIManager.getColor("Panel.background");
		
		WJSettings settings = WJSettings.getInstance();
		setTitle("About " + settings.getAppName() + " " + settings.getAppVersion());
	    setAlwaysOnTop(true);
	    
	    // layout
	    getContentPane().setLayout(new BorderLayout(0, 0));
	    
	    // header
		URL url = getClass().getResource("/ch/epfl/lis/wingj/gui/rsc/wingj_about_500.png");
	    if (url == null) {
	    	WJSettings.log("ERROR: About image not found.");
	    	return;
	    }
	    image_ = getImageFile(url);
	    image_.flush();
	    aboutImagePanel_ = new AboutImagePanel();
		aboutImagePanel_.setLayout(null);
		aboutImagePanel_.setBackground(bgColor);
		int width = image_.getWidth(null);
		int height = image_.getHeight(null);
		aboutImagePanel_.setPreferredSize(new Dimension(width, height));
		
		// create this panel to ensure that the image is horizontally centered
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0;
		c.gridx = 0;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		imagePanel.add(aboutImagePanel_, c);
		getContentPane().add(imagePanel, BorderLayout.CENTER);
		
		// info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridBagLayout());
		int gridy = 0;
		
		// vspace
		GridBagConstraints gca = new GridBagConstraints();
		gca.gridy = gridy++;
		gca.gridx = 0;
		gca.anchor = GridBagConstraints.NORTH;
		gca.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,10)), gca);
		
		// title
		JLabel title = new JLabel("<html><b>Towards unsupervised and systematic segmentation of biological organisms</b></html>");
		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.NORTH;
		gc.gridy = gridy++;
		gc.gridx = 0;
		infoPanel.add(title, gc);
		
		// vspace
		GridBagConstraints gcb = new GridBagConstraints();
		gcb.gridy = gridy++;
		gcb.gridx = 0;
		gcb.anchor = GridBagConstraints.NORTH;
		gcb.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,20)), gcb);
		
		// thomas
		JLabel thomasName = new JLabel("Thomas Schaffter");
		GridBagConstraints gc2 = new GridBagConstraints();
		gc2.anchor = GridBagConstraints.NORTH;
		gc2.gridy = gridy++;
		gc2.gridx = 0;
		infoPanel.add(thomasName, gc2);
		
		// vspace
		GridBagConstraints gcba = new GridBagConstraints();
		gcba.gridy = gridy++;
		gcba.gridx = 0;
		gcba.anchor = GridBagConstraints.NORTH;
		gcba.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,2)), gcba);
		
		HyperText thomasEmail = new HyperText("thomas.schaffter@gmail.com", "thomas_DOT_schaffter_AT_gmail_DOT_com", "<b>", "</b>") {
			/** Default serial */
			private static final long serialVersionUID = 1L;
			@Override
			public void initialize() {
				Color normal = new Color(45, 75, 153);
				Color active = new Color(80, 139, 209);
				normalColor_ = normal;
				hoverColor_ = active;
				activeColor_ = active;
				linkColor_ = normal;
			}
			@Override
			public void action() {
				try {
					openDefaultMailClient();
				} catch (Exception e) {
					WJMessage.showMessage(e);
				}
			}
		};
		GridBagConstraints gc3 = new GridBagConstraints();
		gc3.anchor = GridBagConstraints.NORTH;
		gc3.gridy = gridy++;
		gc3.gridx = 0;
		infoPanel.add(thomasEmail, gc3);
		
		// vspace
		GridBagConstraints gcc = new GridBagConstraints();
		gcc.gridy = gridy++;
		gcc.gridx = 0;
		gcc.anchor = GridBagConstraints.NORTH;
		gcc.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,10)), gcc);
		
		// ricard
		JLabel ricardName = new JLabel("Ricard Delgado-Gonzalo");
		GridBagConstraints gc4 = new GridBagConstraints();
		gc4.anchor = GridBagConstraints.NORTH;
		gc4.gridy = gridy++;
		gc4.gridx = 0;
		infoPanel.add(ricardName, gc4);
		
		// vspace
		GridBagConstraints gcca = new GridBagConstraints();
		gcca.gridy = gridy++;
		gcca.gridx = 0;
		gcca.anchor = GridBagConstraints.NORTH;
		gcca.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,2)), gcca);
		
		HyperText ricardEmail = new HyperText("ricard.delgado@gmail.com", "ricard_DOT_delgado_AT_gmail_DOT_com", "<b>", "</b>") {
			/** Default serial */
			private static final long serialVersionUID = 1L;
			@Override
			public void initialize() {
				Color normal = new Color(45, 75, 153);
				Color active = new Color(80, 139, 209);
				normalColor_ = normal;
				hoverColor_ = active;
				activeColor_ = active;
				linkColor_ = normal;
			}
			@Override
			public void action() {
				try {
					openDefaultMailClient();
				} catch (Exception e) {
					WJMessage.showMessage(e);
				}
			}
		};
		GridBagConstraints gc5 = new GridBagConstraints();
		gc5.anchor = GridBagConstraints.NORTH;
		gc5.gridy = gridy++;
		gc5.gridx = 0;
		infoPanel.add(ricardEmail, gc5);
		
		// vspace
		GridBagConstraints gcd = new GridBagConstraints();
		gcd.gridy = gridy++;
		gcd.gridx = 0;
		gcd.anchor = GridBagConstraints.NORTH;
		gcd.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,15)), gcd);
		
		// website url
		JPanel copyrightPanel = new JPanel();
		copyrightPanel.setLayout(new FlowLayout());
		// prefix
		JLabel copyright = new JLabel("Coypright © 2011-2014");
		HyperText websiteUrl = new HyperText("wingj.org", "http://wingj.org",  "<b>", "</b>") {
			/** Default serial */
			private static final long serialVersionUID = 1L;
			@Override
			public void initialize() {
				Color normal = new Color(45, 75, 153);
				Color active = new Color(80, 139, 209);
				normalColor_ = normal;
				hoverColor_ = active;
				activeColor_ = active;
				linkColor_ = normal;
			}
			@Override
			public void action() {
				try {
					openDefaultBrowserClient();
				} catch (Exception e) {
					WJMessage.showMessage(e);
				}
			}
		};
		copyrightPanel.add(copyright);
		copyrightPanel.add(websiteUrl);
		
		GridBagConstraints gc6 = new GridBagConstraints();
		gc6.anchor = GridBagConstraints.NORTH;
		gc6.gridy = gridy++;
		gc6.gridx = 0;
		infoPanel.add(copyrightPanel, gc6);
		
		// vspace
		GridBagConstraints gce = new GridBagConstraints();
		gce.gridy = gridy++;
		gce.gridx = 0;
		gce.anchor = GridBagConstraints.NORTH;
		gce.fill = GridBagConstraints.HORIZONTAL;
		infoPanel.add(Box.createRigidArea(new Dimension(0,5)), gce);
		
		getContentPane().add(infoPanel, BorderLayout.SOUTH);
		pack();
	    
		// allow the user to hide the splash screen by clicking on it
//		addMouseListener( new MouseAdapter() {
//			@Override
//			public void mousePressed(MouseEvent e) {
//				if(WJAboutBox.this.isVisible()) {
//					ImagePlusManager.getInstance().printAllNames();
//					setVisible(false);
//					dispose();
//				}
//			}
//		});
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - this.getSize().width)/2, (screenSize.height - this.getSize().height)/2);
		
		WingJ.setAppIcon(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Builds and shows the dialog. */
	public void run() {
		
	    setResizable(false);
		setVisible(true);
	}
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Returns an Image from the given URL. */
	private Image getImageFile(URL filename) {
		
	   MediaTracker tracker = new MediaTracker(this);
	   Image openedImage = getToolkit().getImage(filename);
	   tracker.addImage(openedImage,0);
	   try {
		   tracker.waitForID(0);
	   } catch (InterruptedException ie) {
		   WJSettings.log("ERROR: Media tracker can not wait.");
	   }
	   return openedImage;
	}
	
	// ============================================================================
	// INNER CLASS
	
	/** JPanel on which the about image is paint on. */
	private class AboutImagePanel extends JPanel {
		
    	/** Default serial version ID */
    	private static final long serialVersionUID = 1L;
    	
    	// ============================================================================
    	// PUBLIC METHODS
	
		/** Paints the given image as background of the panel. */
		@Override
		public void paintComponent(Graphics g) {
			
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // anti-aliasing
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.drawImage(image_, 0, 0, this);
	    }
	}
}
