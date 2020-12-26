/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://wingj.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package gui;

import java.awt.EventQueue;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.net.URL;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import java.awt.Insets;
import javax.swing.border.EmptyBorder;

/**
 * GUI to edit the detected wing pouch structure
 *
 * @version June 10, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 * @author Jesus Ayala Dominguez
 */
public class WPouchEditionDialog extends JDialog {

	/** Default serial version ID */
	private static final long serialVersionUID = 1L;
	/** Show overlay */
	protected JCheckBox showOverlayCheckBox_;
	/** Choose the color of the overlay */
	protected JButton overlayColorButton_;

	/** Modify the shape of the detected structure */
	protected JButton shapeStructureButton_;
	/** Swap the the D/V and A/P boundaries */
	protected JButton swapBoundariesButton_;
	/** Reverse the orientation of the D/V boundary */
	protected JButton reverseDVBoundaryButton_;
	/** Reverse the orientation of the A/P boundary */
	protected JButton reverseAPBoundaryButton_;

	/** Export the structure to XML file */
	protected JButton saveStructureButton_;
	/** Save data about the wing pouch structure */
	protected JButton saveDatasetButton_;

	/** List all available background images */
	protected JComboBox backgroundCBox_;

	// ============================================================================
	// PUBLIC METHODS

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					WPouchEditionDialog dialog = new WPouchEditionDialog();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public WPouchEditionDialog() {
		setTitle("");

		setBounds(100, 100, 192, 261);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 2, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		contentPanel.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{240, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{.0, 1.0, 0.0, 1.0};
		panel.setLayout(gbl_panel);

		JPanel wingInformationPanel = new JPanel();
		wingInformationPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Overlay", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		GridBagConstraints gbc_wingInformationPanel = new GridBagConstraints();
		gbc_wingInformationPanel.insets = new Insets(0, 0, 5, 0);
		gbc_wingInformationPanel.fill = GridBagConstraints.BOTH;
		gbc_wingInformationPanel.gridx = 0;
		gbc_wingInformationPanel.gridy = 0;
		panel.add(wingInformationPanel, gbc_wingInformationPanel);
		GridBagLayout gbl_wingInformationPanel = new GridBagLayout();
		gbl_wingInformationPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_wingInformationPanel.rowHeights = new int[]{0};
		gbl_wingInformationPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_wingInformationPanel.rowWeights = new double[]{0.0};
		wingInformationPanel.setLayout(gbl_wingInformationPanel);

		showOverlayCheckBox_ = new JCheckBox("Show");
		GridBagConstraints gbc_showOverlayCheckBox_ = new GridBagConstraints();
		gbc_showOverlayCheckBox_.gridwidth = 2;
		gbc_showOverlayCheckBox_.anchor = GridBagConstraints.WEST;
		gbc_showOverlayCheckBox_.gridx = 0;
		gbc_showOverlayCheckBox_.gridy = 0;
		wingInformationPanel.add(showOverlayCheckBox_, gbc_showOverlayCheckBox_);

		overlayColorButton_ = new JButton("Set Color");
		GridBagConstraints gbc_overlayColorButton = new GridBagConstraints();
		gbc_overlayColorButton.anchor = GridBagConstraints.EAST;
		gbc_overlayColorButton.gridx = 4;
		gbc_overlayColorButton.gridy = 0;
		wingInformationPanel.add(overlayColorButton_, gbc_overlayColorButton);

		JPanel actionsPanel = new JPanel();
		actionsPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Actions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		GridBagConstraints gbc_actionsPanel = new GridBagConstraints();
		gbc_actionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_actionsPanel.fill = GridBagConstraints.BOTH;
		gbc_actionsPanel.gridx = 0;
		gbc_actionsPanel.gridy = 1;
		panel.add(actionsPanel, gbc_actionsPanel);
		GridBagLayout gbl_actionsPanel = new GridBagLayout();
		gbl_actionsPanel.columnWidths = new int[]{230, 0};
		gbl_actionsPanel.rowHeights = new int[]{0, 0, 0, 25, 25, 0};
		gbl_actionsPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_actionsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		actionsPanel.setLayout(gbl_actionsPanel);

		swapBoundariesButton_ = new JButton("Swap D/V and A/P boundaries");
		GridBagConstraints gbc_swapBoundariesButton_ = new GridBagConstraints();
		gbc_swapBoundariesButton_.fill = GridBagConstraints.BOTH;
		gbc_swapBoundariesButton_.gridx = 0;
		gbc_swapBoundariesButton_.gridy = 0;
		actionsPanel.add(swapBoundariesButton_, gbc_swapBoundariesButton_);

		reverseDVBoundaryButton_ = new JButton("Reverse D/V boundary");
		GridBagConstraints gbc_reverseDVBoundaryButton_ = new GridBagConstraints();
		gbc_reverseDVBoundaryButton_.fill = GridBagConstraints.BOTH;
		gbc_reverseDVBoundaryButton_.gridx = 0;
		gbc_reverseDVBoundaryButton_.gridy = 1;
		actionsPanel.add(reverseDVBoundaryButton_, gbc_reverseDVBoundaryButton_);

		reverseAPBoundaryButton_ = new JButton("Reverse A/P boundary");
		GridBagConstraints gbc_reverseAPBoundaryButton_ = new GridBagConstraints();
		gbc_reverseAPBoundaryButton_.insets = new Insets(0, 0, 10, 0);
		gbc_reverseAPBoundaryButton_.fill = GridBagConstraints.BOTH;
		gbc_reverseAPBoundaryButton_.gridx = 0;
		gbc_reverseAPBoundaryButton_.gridy = 2;
		actionsPanel.add(reverseAPBoundaryButton_, gbc_reverseAPBoundaryButton_);

		shapeStructureButton_ = new JButton("Edit Structure");
		GridBagConstraints gbc_shapeStructureButton_ = new GridBagConstraints();
		gbc_shapeStructureButton_.fill = GridBagConstraints.BOTH;
		gbc_shapeStructureButton_.gridx = 0;
		gbc_shapeStructureButton_.gridy = 3;
		actionsPanel.add(shapeStructureButton_, gbc_shapeStructureButton_);

		saveStructureButton_ = new JButton("Save Structure");
		GridBagConstraints gbc_saveStructureButton_ = new GridBagConstraints();
		gbc_saveStructureButton_.fill = GridBagConstraints.BOTH;
		gbc_saveStructureButton_.gridx = 0;
		gbc_saveStructureButton_.gridy = 4;
		actionsPanel.add(saveStructureButton_, gbc_saveStructureButton_);

		JPanel backgroundPanel = new JPanel();
		backgroundPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Background Image", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		GridBagConstraints gbc_backgroundPanel = new GridBagConstraints();
		gbc_backgroundPanel.insets = new Insets(0, 0, 5, 0);
		gbc_backgroundPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_backgroundPanel.gridx = 0;
		gbc_backgroundPanel.gridy = 2;
		panel.add(backgroundPanel, gbc_backgroundPanel);
		GridBagLayout gbl_backgroundPanel = new GridBagLayout();
		gbl_backgroundPanel.columnWidths = new int[]{0, 0};
		gbl_backgroundPanel.rowHeights = new int[]{0, 0};
		gbl_backgroundPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_backgroundPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		backgroundPanel.setLayout(gbl_backgroundPanel);

		backgroundCBox_ = new JComboBox();
		GridBagConstraints gbc_backgroundCBox_ = new GridBagConstraints();
		gbc_backgroundCBox_.fill = GridBagConstraints.HORIZONTAL;
		gbc_backgroundCBox_.gridx = 0;
		gbc_backgroundCBox_.gridy = 0;
		backgroundPanel.add(backgroundCBox_, gbc_backgroundCBox_);

		saveDatasetButton_ = new JButton("Save Dataset");
		GridBagConstraints gbc_saveDatasetButton_ = new GridBagConstraints();
		gbc_saveDatasetButton_.insets = new Insets(0, 0, 5, 0);
		gbc_saveDatasetButton_.fill = GridBagConstraints.BOTH;
		gbc_saveDatasetButton_.gridx = 0;
		gbc_saveDatasetButton_.gridy = 3;
		panel.add(saveDatasetButton_, gbc_saveDatasetButton_);
		saveDatasetButton_.setPreferredSize(new Dimension((int)saveDatasetButton_.getPreferredSize().getWidth(), (int)(saveDatasetButton_.getPreferredSize().getHeight() * 1.5)));

		pack();
	}

	// ----------------------------------------------------------------------------

	/** Set the window icon */
	public void setWindowIcon() {

		try {
			URL url = getClass().getResource("/rsc/wingj_logo.png");
			setIconImage(new ImageIcon(url).getImage());
		} catch (NoSuchMethodError e) {
			//
		}
	}

	// ----------------------------------------------------------------------------

	/** Set the tooltips */
	public void setTooltips() {

		ToolTipManager.sharedInstance().setInitialDelay(1000);
		ToolTipManager.sharedInstance().setDismissDelay(12000);

		showOverlayCheckBox_.setToolTipText			("<html>Show the detected orientation of the wing</html>");
		overlayColorButton_.setToolTipText			("<html>Set the overlay color</html>");

		shapeStructureButton_.setToolTipText		("Edit the shape of the detected structure");
		saveStructureButton_.setToolTipText			("Export the detected structure to XML file");
		swapBoundariesButton_.setToolTipText		("<html>Swap the D/V boundary and the A/P boundary</html>");
		reverseDVBoundaryButton_.setToolTipText		("<html>Reverse the orientation of the D/V boundary</html>");
		reverseAPBoundaryButton_.setToolTipText		("<html>Reverse the orientation of the A/P boundary</html>");

		backgroundCBox_.setToolTipText				("<html>Set background image</html>");

		saveDatasetButton_.setToolTipText			("<html>Save <i>structural properties</i> of the detected structure (boundary<br>" +
													 "lengths, compartment areas, etc.)<br>" +
													 "<b>Files saved</b>:<br>" +
													 "- <i>experiment_name</i>_structure.xml (dataset)<br>" +
													 "- <i>experiment_name</i>_structure.tif (structure image)<br>" +
													 "<b>Note</b>: Matlab scripts are available on the website of WingJ to read<br>" +
													 "the generated XML files.<br></html>");
	}
}
