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

package core;

import ij.ImagePlus;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import ch.epfl.lis.wingj.ImagePlusManager;
import ch.epfl.lis.wingj.WJSettings;

import morphology.WPouch;
import morphology.parsers.WPouchXml;
import filefilters.FilenameUtilities;
import filefilters.FilterGenericFilename;
import filefilters.FilterImageTif;
import filefilters.FilterStructuralPropertiesXml;
import gui.WPouchEditionDialog;
import detection.WPouchDetector;
import detection.WPouchDetectorWorker;

/**
 * Handles the edition of the detected wing pouch structure
 *
 * @version June 2, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delgado@gmail.com)
 * @author Jesus Ayala Dominguez
 */
@Deprecated
public class StructureEdition extends WPouchEditionDialog implements ActionListener, ItemListener {

	/** Default serial */
	private static final long serialVersionUID = 1L;

	/** The unique instance of WPouchEdition (Singleton design pattern) */
	private static StructureEdition instance_ = null;

	/** Identified wing pouch structure */
	private WPouch pouch_ = null;

	/** Visualize the detected wing pouch */
	private StructureVisualization viewer_ = null;

	// ============================================================================
	// PRIVATE METHODS

	/** Constructor */
	private StructureEdition() {

		setTitle("Structure");

		overlayColorButton_.addActionListener(this);
		shapeStructureButton_.addActionListener(this);
		swapBoundariesButton_.addActionListener(this);
		reverseDVBoundaryButton_.addActionListener(this);
		reverseAPBoundaryButton_.addActionListener(this);
		saveStructureButton_.addActionListener(this);
		saveDatasetButton_.addActionListener(this);
		backgroundCBox_.addActionListener(this);
		showOverlayCheckBox_.addItemListener(this);

		// window listeners
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				try {
					StructureEdition.getInstance().close();
				} catch (Exception e) {
					WJMessage.showMessage(e);
				}
			}
		});

		setWindowIcon();
		setTooltips();
	}

	// ----------------------------------------------------------------------------

	/** Show color picker (without preview) */
	private void showColorChooserWithoutPreview() {

		WJSettings settings = WJSettings.getInstance();

		final JColorChooser chooser = new JColorChooser();
		chooser.setPreviewPanel(new JPanel());
		chooser.setColor(settings.getDefaultColor());
		chooser.setBackground(getBackground());

        JDialog dialog = JColorChooser.createDialog(this, "Choose overlay color", true, chooser, new ActionListener() {
        	/** Ok action listener */
        	public void actionPerformed(ActionEvent e) {
        		try {
	        		Color color = chooser.getColor();
	        		if (color != null) {
	        			viewer_.setColor(color);
	        		}
        		} catch (Exception e1) {
        			WJMessage.showMessage(e1);
				}
			}
        }, null); // Cancel action listener

        dialog.setVisible(true);
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Get Universal instance */
	static public StructureEdition getInstance() {

		if (instance_ == null)
			instance_ = new StructureEdition();
		return instance_;
	}

	// ----------------------------------------------------------------------------

	/** Show the detected wing pouch on top of the mip image, and show tools to edit the wing pouch */
	public void run(WPouch pouch) throws Exception {

		if (viewer_ != null)
			close();

		pouch_ = pouch;

		ImagePlus image = WDiscImage.getImageProjection(0);
		viewer_ = new StructureVisualization(pouch_, image);
		viewer_.run();

		pouch2gui();
		setVisible(true);
	}

	// ----------------------------------------------------------------------------

	/** Implements actionPerformed() */
	public void actionPerformed(ActionEvent e) {

		Object source = e.getSource();

		try {
			if (source == swapBoundariesButton_) {
				pouch_.swapBoundaries();
				viewer_.updateRoiManagerContent();
			}
			else if (source == reverseDVBoundaryButton_) {
				pouch_.reverseDVBoundary();
				viewer_.updateRoiManagerContent();
			}
			else if (source == reverseAPBoundaryButton_) {
				pouch_.reverseAPBoundary();
				viewer_.updateRoiManagerContent();
			}
			else if (source == saveStructureButton_) {
				WJSettings.log("Saving structure (xml)");
				WingJ.getInstance().getWPouchDetector().saveStructure();
			}
			else if (source == saveDatasetButton_) {
				saveDataset();
			}
			else if (source == overlayColorButton_) {
				showColorChooserWithoutPreview();
			}
			else if (source == backgroundCBox_) {
				String name = (String)backgroundCBox_.getSelectedItem();
				viewer_.setImage(ImagePlusManager.getInstance().getImage(name));
			}
			else if (source == shapeStructureButton_) {
				close();
				WingJ.getInstance().getWPouchDetector().setStructureProjection(WDiscImage.getImageProjection(0)); // DO NOT FORGET ;)
				WPouchDetectorWorker worker = new WPouchDetectorWorker(WingJ.getInstance().getWPouchDetector());
				worker.setMode(WPouchDetectorWorker.MANUAL);
				worker.execute();
			}
		} catch (Exception e1) {
			WJMessage.showMessage(e1);
		}
	}

	// ----------------------------------------------------------------------------

	/** Save the complete dataset of the structural properties */
	public void saveDataset() throws Exception {

		WJSettings settings = WJSettings.getInstance();

		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();

			fc.setDialogTitle("Save structure dataset");
			File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	    	fc.setCurrentDirectory(f);
	    	fc.addChoosableFileFilter(new FilterGenericFilename());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(/*pouch_.getName() + */"structure"));

			int returnVal = fc.showDialog(frame, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

				WJSettings.log("Saving structure dataset");

				try { // save structure as XML
					WPouchDetector detector = WingJ.getInstance().getWPouchDetector();
					if (detector == null)
						throw new Exception("WARNING: detector_ is null");
					detector.saveStructure(file.getAbsolutePath() + ".xml");
					WJSettings.log("[x] Structure (xml)");
				} catch (Exception e) {
					WJSettings.log("[ ] Structure (xml)");
					WJMessage.showMessage(e);
				}

				try { // save dataset as XML
					if (pouch_ == null)
						throw new Exception("WARNING: pouch_ is null");
	            	WPouchXml parser = new WPouchXml(pouch_);
	            	parser.write(file.getAbsolutePath() + "_dataset.xml");
	            	WJSettings.log("[x] Structural properties (xml)");
				} catch (Exception e) {
					WJSettings.log("[ ] Structural properties (xml)");
					WJMessage.showMessage(e);
				}

				try { // viewer content as TIFF
					if (viewer_ == null)
						throw new Exception("WARNING: pouch_ is null");
	            	viewer_.save(file.getAbsolutePath() + ".tif");
					WJSettings.log("[x] Structure image (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Structure image (tif)");
					WJMessage.showMessage(e);
				}

				try { // export cache image
					if (viewer_ == null)
						throw new Exception("WARNING: viewer_ is null");
					if (pouch_ == null)
						throw new Exception("WARNING: pouch_ is null");
	            	WDiscMask.saveBinaryMask(file.getAbsolutePath() + "_mask.tif", viewer_.getImage(), pouch_);
					WJSettings.log("[x] Binary mask (tif)");
				} catch (Exception e) {
					WJSettings.log("[ ] Binary mask (tif)");
					WJMessage.showMessage(e);
				}

				return;
			}
			return;
		} catch (Exception e) {
			throw new Exception("ERROR: Failed to save TIFF file: " + e.toString());
		}
	}

	// ----------------------------------------------------------------------------

	/** Open a dialog to specify the filename of the target TIF image */
	public void saveTIF() throws Exception {

		WJSettings settings = WJSettings.getInstance();

		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();

			fc.setDialogTitle("Save TIFF");
			File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	    	fc.setCurrentDirectory(f);
	    	fc.addChoosableFileFilter(new FilterImageTif());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(/*pouch_.getName() + */"structure.tif"));

			int returnVal = fc.showDialog(frame, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
            	String[] ext = {"tif"};
            	file = FilenameUtilities.addExtension(file, ext);
            	WJSettings.log("Saving " + file.getAbsolutePath());
            	viewer_.save(file.getAbsolutePath());
				return;
			}
			return;
		} catch (Exception e) {
			throw new Exception("ERROR: Failed to save TIFF file: " + e.toString());
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open a dialog to specify the filename of the target XML file containing
	 * properties of the wing pouch structure.
	 */
	public void saveXml() throws Exception {

		WJSettings settings = WJSettings.getInstance();

		try {
			JFrame frame = new JFrame();
			frame.setAlwaysOnTop(true);
			JFileChooser fc = new JFileChooser();

			fc.setDialogTitle("Save dataset");
			File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
	    	fc.setCurrentDirectory(f);
	    	fc.addChoosableFileFilter(new FilterStructuralPropertiesXml());
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setSelectedFile(new File(/*pouch_.getName() + */"structure.xml"));

			int returnVal = fc.showDialog(frame, "Save");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
            	String[] ext = {"xml"};
            	file = FilenameUtilities.addExtension(file, ext);
            	WJSettings.log("Saving " + file.getAbsolutePath());
            	WPouchXml parser = new WPouchXml(pouch_);
            	parser.write(file.getAbsolutePath());
				return;
			}
			return;
		} catch (Exception e) {
			throw new Exception("ERROR: Failed to save XML file: " + e.toString());
		}
	}

	// ----------------------------------------------------------------------------

	/** Implements itemStateChanged() */
	public void itemStateChanged(ItemEvent e) {

		Object source = e.getSource();

		try {
			if (source == showOverlayCheckBox_) {
				viewer_.setOverlayVisible(showOverlayCheckBox_.isSelected());
				viewer_.update();
			}
		} catch (Exception e1) {
			WJMessage.showMessage(e1);
		}
	}

	// ----------------------------------------------------------------------------

	/** Read the content of the pouch object and the gui elements accordingly */
	public void pouch2gui() throws Exception {

		if (pouch_ == null)
			throw new Exception("ERROR: WPouchEdition::pouch2gui(): pouch: is null");

		showOverlayCheckBox_.setSelected(viewer_.isOverlayVisible());
		setRequiredImages();

		// set the correct item index in the combobox
		ImagePlusManager manager = ImagePlusManager.getInstance();
		int index = manager.getImageIndex("structure_projection");
		backgroundCBox_.setSelectedIndex(index);
	}

	// ----------------------------------------------------------------------------

	/** Read the content of the gui elements and set the pouch object accordingly */
	public void gui2pouch() throws Exception {

//		if (pouch_ == null)
//			throw new Exception("ERROR: WPouchEdition::gui2pouch(): pouch: is null");
	}

	// ----------------------------------------------------------------------------

	/** Add images to the list of background images */
	public void setRequiredImages() throws Exception {

//		ImagePlusManager manager = ImagePlusManager.getInstance();
//		backgroundCBox_.removeActionListener(this);
//
//		WJSettings.log("REGENERATE");
//
//		backgroundCBox_.removeAllItems();
//		for (int i = 0; i < manager.size(); i++) {
//			if (manager.getImage(i) != null && manager.getImage(i).getProcessor() != null && manager.getImage(i).getNSlices() > 0)
//				backgroundCBox_.addItem(manager.getName(i));
//		}
//		backgroundCBox_.addActionListener(this);
	}

	// ----------------------------------------------------------------------------

	/** Close the StackWindow and the RoiManager, and hide the WPouchEditor */
	public void close() throws Exception {

		gui2pouch();
		if (viewer_ != null)
			viewer_.close();
		dispose();
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public WPouch getWPouch() { return pouch_; }
}
