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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import ch.epfl.lis.wingj.utilities.FileUtils;
import ch.epfl.lis.wingj.utilities.FilenameUtils;
import ch.epfl.lis.wingj.utilities.ImageUtils;
import ch.epfl.lis.wingj.utilities.Projections;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

/**
 * Represents a stack of images (3D image).
 * 
 * @version October 21, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJImages {
	
	/** Names of the image stack. */
	public static String[] imageStackNames_ = {"ch0_stack", "ch1_stack", "ch2_stack", "ch3_stack"};
	/** Names of the image projections. */
	public static String[] imageProjectionNames_ = {"ch0_projection", "ch1_projection", "ch2_projection", "ch3_projection"};
	
	/** Mask to only consider one system when multiple systems are on the same image. */
	public static WJImagesMask imagesMask_ = null;
	
	/** Contains the width of each image stack. */
	@SuppressWarnings("serial")
	public static List<Integer> imageWidths_ = new ArrayList<Integer>() {{add(0); add(0); add(0); add(0);}};
	/** Contains the height of each image stack. */
	@SuppressWarnings("serial")
	public static List<Integer> imageHeights_ = new ArrayList<Integer>() {{add(0); add(0); add(0); add(0);}};
	
	/** Contains the number of slices loaded for each channel. */
	public static Integer[] numSlices_ = {0, 0, 0, 0};
	/** First slice index to consider. */
	public static Integer[] firstSlicesIndex_ = {0, 0, 0, 0};
	/** Last slice index to consider. */
	public static Integer[] lastSlicesIndex_ = {0, 0, 0, 0};
	/** Selection tag used for each channel. */
	public static String[] selectionTags_ = {"", "", "", ""};
	
	/** Channel index associated to each RGB color. */
	@SuppressWarnings("serial")
	public static List<Integer> colorChannelIndex_ = new ArrayList<Integer>() {{add(0); add(1); add(2);}};
	
	/** To get a trace of the selection tag entered by the user (I don't like this approach). */
	private static String tmpSelectionTag_ = null;
	
	/** Flag to enable the automatic loading of image stacks only once (click on Reset to clear it). */
	public static boolean allowImagesAutoLoading_ = true;
	
	/** Flag to indicate if an image scale has already been loaded. */
	public static boolean scaleAlreadyLoaded_ = false;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Opens an image sequence using ImageJ methods. */
	static private ImagePlus openImageStackUsingPlugin(String absPath) throws Exception {
		
		// uses a redefined IJ plugin to load image stack (VERSION 3)
		WJFolderOpener opener = new WJFolderOpener();
		opener.run(absPath); // pauses here
		
		if (opener.numImagesOpened_ < 1)
			return null;
		
		if (opener.openDialogCanceled_)
			return null;
		
		if (opener.getAuto())
			return null;
		
		// A current proble is that if the loading failed, the current image refers
		// to the last image opened. Since we should not edit the reference to
		// WindowManager.getCurrentImage(), we need a flag that tell use if the above
		// code effectively opened images
		ImagePlus img = WindowManager.getCurrentImage();
		tmpSelectionTag_ = opener.getFilter();
		
		// if user cancel the dialog to select the image directory
		if (img == null)
			return null; // if e.g. 0 slices were selected	

		if (img.getNSlices() > 1) new StackConverter(img).convertToGray32();
		else new ImageConverter(img).convertToGray32();
		return img;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens an image stack from a list of image filenames. */
	static private ImagePlus openImageStack(String[] filenames, String folder) throws Exception {
		
		if (filenames == null || filenames.length == 0)
			throw new Exception("WARNING: Unable to find any images in:\n" + folder);
		
		// Initializing stack with the first image
		ImagePlus img = (new Opener()).openImage(folder, filenames[0]);
		int nx = img.getWidth();
		int ny = img.getHeight();
		
		ImageStack originalStack = new ImageStack(nx,ny);
		FloatProcessor fpch = null;
		int Nstack = filenames.length;
		for (int frame = 0; frame < Nstack; frame++) {
			fpch = (FloatProcessor) (new Opener()).openImage(folder, filenames[frame]).getProcessor().toFloat(0, null);
			originalStack.addSlice("", fpch);
		}
		ImagePlus img2 = new ImagePlus("", originalStack);
		
		// keep original distance information
		FileInfo fi = img.getOriginalFileInfo();
		Calibration c = img2.getCalibration();
		c.setUnit(fi.unit);
		c.pixelWidth = fi.pixelWidth;
		c.pixelHeight = fi.pixelHeight;
		
		// magic code from ImageJ FolderOpener.java
		if (c.pixelWidth<=0.0001 && c.getUnit().equals("cm")) {
			c.pixelWidth *= 10000.0;
			c.pixelHeight *= 10000.0;
			c.pixelDepth *= 10000.0;
			c.setUnit("um");
		}
		
		return img2;
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Clears reference of images closed (among others). */
	public static void update() {
		
		ImagePlus images = null;
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			images = getImageStack(i);
			if (images == null || images.getProcessor() == null) {
				imageWidths_.set(i, 0);
				imageHeights_.set(i, 0);
				numSlices_[i] = 0;
				firstSlicesIndex_[i] = 0;
				lastSlicesIndex_[i] = 0;
			}
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens an image sequence. */
	public static boolean openImageStack(int channel, String directory) throws Exception {
		
		ImagePlus img = openImageStack("ch0" + channel, directory);
		
		boolean success = false;
		if (img != null) {
			// update image directory info
			// not the best place to do it
			WJSettings settings = WJSettings.getInstance();
			settings.setChannelDirectory(channel, directory);
		
			success = registerImageStack(channel, img);
		}
		return success;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens an image sequence. */
	public static boolean openImageStack(int channel) throws Exception {
		
		ImagePlus img = openImageStack("", null);
		
		boolean success = false;
		if (img != null) {
			success = registerImageStack(channel, img);
		}
		return success;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens an image sequence. */
	public static ImagePlus openImageStack(String selectionTag, String directory) throws Exception {
		
		ImagePlus img = null;
		if (directory == null || directory.isEmpty()) {
			WJSettings.log("Opening images using ImageStack plugin.");
			img = WJImages.openImageStackUsingPlugin(null);
		} else { // for WingJ batch mode
			String[] filenames = FileUtils.getListDir(directory, selectionTag);
			if (filenames.length == 0)
				throw new Exception("INFO: Unable to find any images whose names contain the substring \"" + selectionTag + "\" in:\n" + directory);
			img = openImageStack(filenames, directory);
		}
		return img;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Registers the given image stack in WJImages. */
	public static boolean registerImageStack(int channel, ImagePlus img) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		if (img != null) {
			String name = WJImages.imageStackNames_[channel];
			WJSettings.log("Opening " + name);
			img.setTitle(name);
			
			ImagePlusManager manager = ImagePlusManager.getInstance();
			manager.remove(name); // remove old item, if any existing
			manager.add(name, img, true); // TODO: set back to true
			
			WJSettings.getInstance().setChannelDirectory(channel, IJ.getDirectory("image")); // save directory

			// update image dimensions
			imageWidths_.set(channel, img.getWidth());
			imageHeights_.set(channel, img.getHeight());
			
			// update slices info
			numSlices_[channel] = img.getNSlices();
			firstSlicesIndex_[channel] = 1;
			lastSlicesIndex_[channel] = img.getNSlices();
			
			if (WJImages.tmpSelectionTag_ == null || WJImages.tmpSelectionTag_.compareTo("") == 0)
				WJImages.tmpSelectionTag_ = "*";
			selectionTags_[channel] = WJImages.tmpSelectionTag_;
			
			settings.setExpressionMinSliceIndex(channel, 1);
			settings.setExpressionMaxSliceIndex(channel, img.getNSlices());
			
			// display the middle slice
			img.setSlice((int)Math.round(img.getNSlices()/2.));
			
			WJSettings.log("Image width: " + img.getWidth());
			WJSettings.log("Image height: " + img.getWidth());
			WJSettings.log("Number of slices: " + img.getNSlices());
			
			// set the preferred scale and distance unit
			setDistanceUnit(img);
			
			return true;
		}
		return false;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Calibrates the distance unit, pixel width and height of the given image. */
	public static ImagePlus setDistanceUnit(ImagePlus img) throws Exception {
	
		WJSettings settings = WJSettings.getInstance();
		
		// get scale and settings of the image
		Calibration c = img.getCalibration();
		double imgScale = c.pixelWidth;
		String imgUnit = c.getUnit();
		
		// convert the calibration of the given image
//		c.pixelWidth = UnitConverter.convertDistanceValue(c.pixelWidth, unit, targetUnit); // do only a conversion to match the settings unit
//		c.pixelHeight = UnitConverter.convertDistanceValue(c.pixelHeight, unit, targetUnit); // do only a conversion to match the settings unit
//		c.setUnit(targetUnit);
		
		if (c.pixelWidth != c.pixelHeight)
			throw new Exception("WARNING: Image scale is not the same horizontally and vertically.");
		
        // with more details for the console
    	WJSettings.log("Current scale: 1 px = " + new DecimalFormat("#.######").format(settings.getScale()) + " " + settings.getUnit());
    	WJSettings.log("Image scale: 1 px = " + new DecimalFormat("#.######").format(imgScale) + " " + imgUnit);
		
		// if first time, apply the img settings
		if (!scaleAlreadyLoaded_) {
        	settings.setUnit(imgUnit);
        	settings.setScale(imgScale);
        	scaleAlreadyLoaded_ = true;
		}
		else if (imgUnit.compareTo(settings.getUnit()) != 0 || Math.abs(imgScale - settings.getScale()) > 1E-6) {

            String ratio1 = "1 px = " + new DecimalFormat("#.###").format(settings.getScale()) + " " + settings.getUnit();
            String ratio2 = "1 px = " + new DecimalFormat("#.###").format(imgScale) + " " + imgUnit;

            Object[] options = {ratio1, ratio2};
            int n = JOptionPane.showOptionDialog(WingJ.getInstance(),
                "Please select the image scale to apply.",
                "Select image scale",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]);

            if (n == JOptionPane.NO_OPTION) {
            	settings.setUnit(imgUnit);
            	settings.setScale(imgScale);   
            }
		}

        return img;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes the projection of the given channel. */
	public static void computeImageProjection(int channel, int minSlice, int maxSlice) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		ImagePlusManager manager = ImagePlusManager.getInstance();
		
		ImagePlus images = getImageStack(channel);
		if (images == null)
			throw new Exception("INFO: Single image or image stack for channel " + channel + " required.");
		
		String method = "";
		if (settings.getChannelProjectionMethod(channel) == Projections.PROJECTION_MEAN_METHOD)
			method = "mean";
		else if (settings.getChannelProjectionMethod(channel) == Projections.PROJECTION_MAX_METHOD)
			method = "maximum";
		
		WJSettings.log("Projecting ch0" + channel + " using slices " + minSlice + "-" + maxSlice + " (" + method + ").");
		
		ImagePlus projection = Projections.doProjection(images, settings.getChannelProjectionMethod(channel), minSlice, maxSlice);
		projection.setDisplayRange(0., 255.);
		
		projection.setTitle(imageProjectionNames_[channel]);
		manager.remove(imageProjectionNames_[channel]); // remove old items, if any existing
		manager.add(imageProjectionNames_[channel], projection);		
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the loaded images have the same width value. */
	public static int areImageWidthsConsistent() {
		
		return areDimensionsConsistent(imageWidths_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the loaded images have the same height value. */
	public static int areImageHeightsConsistent() {
		
		return areDimensionsConsistent(imageHeights_);
	}
	
	// ----------------------------------------------------------------------------
	
	/** If the given list contains non-null elements, return true. */
	public static int areDimensionsConsistent(List<Integer> list) {
		
		List<Integer> l = new ArrayList<Integer>(list.size());
		l.addAll(list);
		Collections.sort(l);
		int firstNonZero = l.lastIndexOf(0) + 1;
		if (firstNonZero == l.size()) return 0; // no images loaded
		if (firstNonZero == l.size()-1) return l.get(l.size()-1); // only one non-null element
		
		// test that the value of the non-null element series are the same
		for (int i = firstNonZero + 1; i < l.size(); i++) {
			if (l.get(i).compareTo(l.get(i-1)) != 0) return -1;
		}
		return l.get(firstNonZero);
	}

	// ----------------------------------------------------------------------------
	
	/** Computes and shows composite image. */
	public static void computeComposite() throws Exception {
		
		computeComposite(null);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Computes composite image and 1) shows it if filename = "" or 2) don't show it and save it to file. */
	public static void computeComposite(URI uri) throws Exception {
		
    	int w = WJImages.areImageWidthsConsistent();
    	int h = WJImages.areImageHeightsConsistent();
    	if (w == 0 && h == 0)
    		throw new Exception("INFO: Requires at least one image or image stack to be open.");
    	if (w == -1 || h == -1)
    		throw new Exception("WARNING: At least one image stack doesn't\n" +
    							"have the same dimensions than another.");
    	
		WJSettings.log("Generating composite image.");
    
    	WJSettings settings = WJSettings.getInstance();
    	ImageStack[] stacks = new ImageStack[3];

    	for (int i = 0; i < colorChannelIndex_.size(); i++) {
    		int channelIndex = colorChannelIndex_.get(i);
    		if (channelIndex == WJSettings.NUM_CHANNELS) {
    			stacks[i] = null;
    			continue;
    		}
    		
    		int projectionMethod = settings.getExpressionCompositeProjections();
    		if (projectionMethod < 0)
    			projectionMethod = settings.getChannelProjectionMethod(channelIndex); // method defined by the user for this channel

    		if (WJImages.getImageStack(channelIndex) != null && WJImages.getImageStack(channelIndex).getProcessor() != null) {
    			stacks[i] = Projections.doProjection(WJImages.getImageStack(channelIndex),
	    				projectionMethod,
						settings.getExpressionMinSliceIndex(channelIndex),
						settings.getExpressionMaxSliceIndex(channelIndex)).getImageStack();
    		} else
    			stacks[i] = null;
    	}
    	
    	ImageStack stack = new RGBStackMerge().mergeStacks(w, h, 1,
			stacks[0], // red
			stacks[1], // green
			stacks[2], // blue
			true); // keep
    	
    	String name = "composite";
    	ImagePlusManager manager = ImagePlusManager.getInstance();
    	ImagePlus img = new ImagePlus(name, stack);
    	
    	if (uri == null) {
	    	manager.remove(name);
	    	manager.add(img.getTitle(), img, true);
    	}
    	else { // save the image to file
    		try {
    			IJ.save(img, uri.getPath());
    			WJSettings.log("[x] Writing composite image (tif)");
    		} catch (Exception e) {
    			WJSettings.log("[ ] Writing composite image (tif)");
    			WJMessage.showMessage(e);
    		}
    	}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes the window of the composite image. */
	public static void disposeComposite() throws Exception {
		
		ImagePlusManager.getInstance().remove("composite");
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Saves the AOI to TIFF file.
	 * <p>
	 * If the user didn't explicitly defined an AOI, the AOI is a white rectangle
	 * which has the same dimension that the confocal image.
	 */
	public static void saveAoi(URI uri) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		// create base image for AOI
		int structureChannelIndex = settings.getStructureChannelIndex();
		getImageProjection(structureChannelIndex).saveRoi();
		getImageProjection(structureChannelIndex).killRoi();
		ImagePlus aoi = new Duplicator().run(getImageProjection(structureChannelIndex));
		getImageProjection(structureChannelIndex).restoreRoi();
		aoi.setTitle("aoi");
		
		// save a white image
		if (imagesMask_ == null || imagesMask_.aoiRoi_ == null) {
			aoi.setRoi(0, 0, aoi.getWidth(), aoi.getHeight());
			IJ.run(aoi, "Clear", null);
		}
		else {
			aoi.setRoi(imagesMask_.aoiRoi_);
			IJ.run(aoi, "Clear Outside", null);
			aoi.saveRoi();
			aoi.killRoi();
			aoi.getProcessor().invert();
			aoi.restoreRoi();
			IJ.run(aoi, "Clear", null);
		}
		aoi.killRoi();
		
		// convert to 8-bit
		new ImageConverter(aoi).convertToGray8();
		
		try {
			IJ.save(aoi, uri.getPath());
			WJSettings.log("[x] Writing AOI (tif)");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing AOI (tif)");
			WJMessage.showMessage(e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Opens AOI from TIFF file. */
	public static void openAoi(File aoiFile) throws Exception {
		
		if (aoiFile == null || !aoiFile.exists() || !aoiFile.isFile()) {
			WJSettings.log("There is no AOI available.");
			return;
		}
		
		ImagePlus aoiImage = IJ.openImage(aoiFile.getAbsolutePath());
		if (aoiImage == null || aoiImage.getProcessor() == null)
			throw new Exception("ERROR: aoiImage is null.");
		
		// invert the image because auto selection works on black objects
		aoiImage.getProcessor().invert();
		IJ.run(aoiImage, "Create Selection", null);
		Roi aoiRoi = aoiImage.getRoi();
		
		if (aoiRoi == null || !aoiRoi.isArea())
			throw new Exception("ERROR: aoiRoi is not a valid AOI.");
		
		if (WJImages.imagesMask_ == null)
			WJImages.imagesMask_ = new WJImagesMask();	
		WJImages.imagesMask_.aoiRoi_ = aoiRoi;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Cleans and reinitializes. */
	public static void clean() throws Exception {

		imagesMask_ = null;
		for (int i = 0; i < imageWidths_.size(); i++)
			imageWidths_.set(i, i);
		for (int i = 0; i < imageHeights_.size(); i++)
			imageHeights_.set(i, i);
		for (int i = 0; i < numSlices_.length; i++)
			numSlices_[i] = 0;
		for (int i = 0; i < firstSlicesIndex_.length; i++)
			firstSlicesIndex_[i] = 0;
		for (int i = 0; i < lastSlicesIndex_.length; i++)
			lastSlicesIndex_[i] = 0;
//		for (int i = 0; i < colorChannelIndex_.size(); i++)
//			colorChannelIndex_.set(i, i);
		
		// extra
		Roi.previousRoi = null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows the given ImagePlus object using the dynamic range related to WJSettings.normalizeExpression_. */
	public static void showExpressionImage(ImagePlus img) throws Exception {

		// this should not appends but I've seen max of normalized images to be 1.02...
		try {
			if (ImageUtils.getMaxPixelValue(img) <= 1.5)
				img.setDisplayRange(0., 1.);
			else
				img.setDisplayRange(0., 255.);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		img.show();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the given ImagePlus object using the dynamic range related to WJSettings.normalizeExpression_. */
	public static void saveExpressionImage(String filename, ImagePlus img) throws Exception {
		
		// this should not appends but I've seen max of normalized images to be 1.02...
		try {
			if (ImageUtils.getMaxPixelValue(img) <= 1.5) {
				img.setDisplayRange(0., 1.);
				IJ.save(img, filename);
			} else {
				save32bitTo8bit(filename, img);
			}
		} catch (Exception e) {
			save32bitTo8bit(filename, img);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saves the given 32-bit ImagePlus object to 8-bit. */
	public static void save32bitTo8bit(String filename, ImagePlus img) throws Exception {
		
		img.setDisplayRange(0., 255.);
		ImageProcessor ip = img.getProcessor().convertToByte(true);
		ImagePlus img2 = new ImagePlus("", ip);
		IJ.save(img2, filename);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Reads a slice dataset file and performs the actions required.
	 * Assumes that if the user left the selection tag blank or used
	 * the wild card '*', the slice dataset will contains the symbol
	 * '*' for the selection tag.
	 */
	public static void readSliceDatasetAndOpenImages(URI uri) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		try{
			FileInputStream fstream = new FileInputStream(new File(uri));
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
	  
			// parses maximum WJSettings.NUM_CHANNELS line
			int lineIndex = 0;
			String imagesDirectory = FilenameUtils.getDirectory(new File(uri).getAbsolutePath());
			while ((strLine = br.readLine()) != null && lineIndex < WJSettings.NUM_CHANNELS)   {
				if (strLine.compareTo("") != 0) {
					
					WJSettings.log("Opening channel " + lineIndex);
		
					// get channel information
					StringTokenizer tokenizer = new StringTokenizer(strLine, "\t");
					String channelName = tokenizer.nextToken();
					String channelSelectionTag = tokenizer.nextToken();
					int channelMinSlice = Integer.parseInt(tokenizer.nextToken());
					int channelMaxSlice = Integer.parseInt(tokenizer.nextToken());
					int projectionMethod = Integer.parseInt(tokenizer.nextToken());
					
					// load channel
					ImagePlus ip = openImageStack(channelSelectionTag, imagesDirectory);
					if (ip != null) {
						registerImageStack(lineIndex, ip);
						
						firstSlicesIndex_[lineIndex] = channelMinSlice;
						lastSlicesIndex_[lineIndex] = channelMaxSlice;
						numSlices_[lineIndex] = ip.getNSlices();
						selectionTags_[lineIndex] = channelSelectionTag;
						settings.setExpressionMinSliceIndex(lineIndex, channelMinSlice);
						settings.setExpressionMaxSliceIndex(lineIndex, channelMaxSlice);
						settings.setGeneNames(channelName, lineIndex);
						settings.setChannelProjectionMethod(lineIndex, projectionMethod);
					} else
						WJSettings.log("ERROR: Unable to load images for " + channelSelectionTag + " in " + imagesDirectory);
				}
				lineIndex++;
			}
			in.close();
			WJSettings.log("[x] Reading slice dataset and opening image stacks");
		} catch (Exception e) {
			WJSettings.log("[ ] Reading slice dataset and opening image stacks");
			e.printStackTrace();
			throw new Exception("ERROR: Unable to parse slices information.");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Saved the slices information to the text file pointed by the given URI. */
	public static void writeSliceDataset(File f) throws Exception {
		
		WJSettings settings = WJSettings.getInstance();
		
		try {
			FileWriter fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);    
			
			String content = "";
			for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
				
				if (WJImages.firstSlicesIndex_[i] != 0 && WJImages.lastSlicesIndex_[i] != 0) {
					content += settings.getGeneName(i) + "\t";
					content += WJImages.selectionTags_[i] + "\t";
					content += settings.getExpressionMinSliceIndex(i) + "\t";
					content += settings.getExpressionMaxSliceIndex(i) + "\t";
					content += settings.getChannelProjectionMethod(i);
				}
				
				if (i < WJSettings.NUM_CHANNELS - 1)
					content += "\n";
			}
			out.write(content);
			out.close();
			WJSettings.log("[x] Writing  slice dataset");
		} catch (Exception e) {
			WJSettings.log("[ ] Writing slice dataset");
			WJMessage.showMessage(e);
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	/** Returns the image stack corresponding to the given channel. */
	public static ImagePlus getImageStack(int channel) {
		ImagePlusManager manager = ImagePlusManager.getInstance();
		return manager.getImage(imageStackNames_[channel]);
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the image projection corresponding to the given channel. */
	public static ImagePlus getImageProjection(int channel) throws Exception {
		ImagePlusManager manager = ImagePlusManager.getInstance();
		ImagePlus img = manager.getImage(imageProjectionNames_[channel]);
		if (img != null)
			return img;
		// if the projection has not been computed so far, look for the stack and if it's
		// there, generate the projection
		ImagePlus stack = getImageStack(channel);
		if (stack != null) {
			WJSettings settings = WJSettings.getInstance();
			computeImageProjection(channel, settings.getExpressionMinSliceIndex(channel), settings.getExpressionMaxSliceIndex(channel));
		}
		return manager.getImage(imageProjectionNames_[channel]);
	}
}
