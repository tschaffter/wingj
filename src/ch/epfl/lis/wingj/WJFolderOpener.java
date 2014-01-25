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

import java.awt.Checkbox;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.awt.image.ColorModel;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.VirtualStack;
import ij.gui.GenericDialog;
import ij.io.FileInfo;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.DicomTools;

/** 
 * Modified version of ij.plugin.FolderOpener to access important variables.
 * <p>
 * That would have been nice to have used protected variables in ij.plugin.FolderOpener. ;)
 * 
 * @version December 4, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class WJFolderOpener implements PlugIn {

private static String[] excludedTypes = {".txt", ".lut", ".roi", ".pty", ".hdr", ".java", ".ijm", ".py", ".js", ".bsh", ".xml"};
private static boolean staticSortFileNames = true;
private static boolean staticOpenAsVirtualStack;
private boolean convertToRGB;
private boolean sortFileNames = true;
private boolean openAsVirtualStack;
private double scale = 100.0;
private int n, start, increment;
private String filter;
private boolean isRegex;
private FileInfo fi;
private String info1;
private ImagePlus image;
/** XXX: If true, indicates that the automatic images loading has been performed. */
private boolean auto_ = false;
/** XXX: True if the user canceled the open dialog. */
public boolean openDialogCanceled_ = false;
/** XXX: Number of images effectively opened. */
public int numImagesOpened_ = 0;

/** Opens the images in the specified directory as a stack. */
public static ImagePlus open(String path) {		
	WJFolderOpener fo = new WJFolderOpener();
	fo.run(path);
	return fo.image;
}

/** Opens the images in the specified directory as a stack. */
public ImagePlus openFolder(String path) {
	run(path);
	return image;
}

@Override
public void run(String arg) {
	String directory = null;
	if (arg!=null && !arg.equals("")) {
		directory = arg;
	} else {
		if (!IJ.macroRunning()) {
			sortFileNames = staticSortFileNames;
			openAsVirtualStack = staticOpenAsVirtualStack;
		}
		arg = null;
		String title = "Open Image Sequence...";
		String macroOptions = Macro.getOptions();
		if (macroOptions!=null) {
			directory = Macro.getValue(macroOptions, title, null);
			if (directory!=null) {
				directory = OpenDialog.lookupPathVariable(directory);
				File f = new File(directory);
				if (!f.isDirectory() && (f.exists()||directory.lastIndexOf(".")>directory.length()-5))
					directory = f.getParent();
			}
		}
		if (directory==null) {
			
//			directory = IJ.getDirectory(title);
			
			JFrame frame = new JFrame();
	     	frame.setAlwaysOnTop(true);
	    	JFileChooser fc = new JFileChooser();
	     	fc.setDialogTitle("Select images directory");
	     	WingJ.setAppIcon(frame);
	     	
	     	WJSettings settings = WJSettings.getInstance();
	     	
	     	try {
		     	// Set the current directory to the working directory
		     	File f = new File(new File(settings.getOutputDirectory()).getCanonicalPath());
		     	fc.setCurrentDirectory(f);
		     	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		     	
		     	int returnVal = fc.showOpenDialog(frame);
		     	if (returnVal == JFileChooser.APPROVE_OPTION) {
		     		directory = fc.getSelectedFile().getPath();
		    		if (!directory.endsWith(File.separator))
		     			directory += "/";
		     	}
	     	} catch (Exception e) {
	     		WJSettings.log("ERROR: Unable to select images directory.");
	     	}
		}
	}
	if (directory==null) {
		openDialogCanceled_ = true;
		return;
	}
	
	// XXX: Tests if automatic loading can be done

	try {
		auto_ = WingJ.getInstance().automaticImagesLoading(directory);
	} catch (Exception e) {
		WJSettings.log("ERROR: Unable to automatically load images (see logs).");
		e.printStackTrace();
		auto_ = false;
	}
	if (auto_)
		return;
	
	String[] list = (new File(directory)).list();
	if (list==null)
		return;
	String title = directory;
	if (title.endsWith(File.separator) || title.endsWith("/"))
		title = title.substring(0, title.length()-1);
	int index = title.lastIndexOf(File.separatorChar);
	if (index!=-1) title = title.substring(index + 1);
	if (title.endsWith(":"))
		title = title.substring(0, title.length()-1);
	
	IJ.register(WJFolderOpener.class);
	list = trimFileList(list);
	if (list==null) return;
	if (IJ.debugMode) IJ.log("WJFolderOpener: "+directory+" ("+list.length+" files)");
	int width=0,height=0,depth=0,bitDepth=0;
	ImageStack stack = null;
	double min = Double.MAX_VALUE;
	double max = -Double.MAX_VALUE;
	Calibration cal = null;
	boolean allSameCalibration = true;
	IJ.resetEscape();		
	try {
		for (int i=0; i<list.length; i++) {
			IJ.redirectErrorMessages();
			ImagePlus imp = (new Opener()).openImage(directory, list[i]);
			if (imp!=null) {
				width = imp.getWidth();
				height = imp.getHeight();
				bitDepth = imp.getBitDepth();
				fi = imp.getOriginalFileInfo();
				if (arg==null) {
					if (!showDialog(imp, list))
						return;
				} else {
					n = list.length;
					start = 1;
					increment = 1;
				}
				break;
			}
		}
		if (width==0) {
			IJ.error("Import Sequence", "This folder does not appear to contain any TIFF,\n"
			+ "JPEG, BMP, DICOM, GIF, FITS or PGM files.");
			return;
		}

		if (filter!=null && (filter.equals("") || filter.equals("*")))
			filter = null;
		if (filter!=null) {
			int filteredImages = 0;
				for (int i=0; i<list.length; i++) {
				if (isRegex&&list[i].matches(filter))
					filteredImages++;
				else if (list[i].indexOf(filter)>=0)
					filteredImages++;
					else
						list[i] = null;
				}
				if (filteredImages==0) {
					if (isRegex)
						IJ.error("Import Sequence", "None of the file names match the regular expression.");
					else
						IJ.error("Import Sequence", "None of the "+list.length+" files contain\n the string '"+filter+"' in their name.");
					return;
				}
				String[] list2 = new String[filteredImages];
				int j = 0;
				for (int i=0; i<list.length; i++) {
					if (list[i]!=null)
						list2[j++] = list[i];
				}
				list = list2;
			}
		if (sortFileNames)
			list = sortFileList(list);

		if (n<1)
			n = list.length;
		if (start<1 || start>list.length)
			start = 1;
		if (start+n-1>list.length)
			n = list.length-start+1;
		int count = 0;
		int counter = 0;
		ImagePlus imp = null;
		for (int i=start-1; i<list.length; i++) {
			if ((counter++%increment)!=0)
				continue;
			Opener opener = new Opener();
			opener.setSilentMode(true);
			IJ.redirectErrorMessages();
			if (!openAsVirtualStack||stack==null)
				imp = opener.openImage(directory, list[i]);
			if (imp!=null && stack==null) {
				// XXX: increment number of images effectively opened
				numImagesOpened_++;
				
				width = imp.getWidth();
				height = imp.getHeight();
				depth = imp.getStackSize();
				bitDepth = imp.getBitDepth();
				cal = imp.getCalibration();
				if (convertToRGB) bitDepth = 24;
				ColorModel cm = imp.getProcessor().getColorModel();
				if (openAsVirtualStack) {
					stack = new VirtualStack(width, height, cm, directory);
					((VirtualStack)stack).setBitDepth(bitDepth);
				} else if (scale<100.0)						
					stack = new ImageStack((int)(width*scale/100.0), (int)(height*scale/100.0), cm);
				else
					stack = new ImageStack(width, height, cm);
				info1 = (String)imp.getProperty("Info");
			}
			if (imp==null)
				continue;
			if (imp.getWidth()!=width || imp.getHeight()!=height) {
				IJ.log(list[i] + ": wrong size; "+width+"x"+height+" expected, "+imp.getWidth()+"x"+imp.getHeight()+" found");
				continue;
			}
			String label = imp.getTitle();
			if (depth==1) {
				String info = (String)imp.getProperty("Info");
				if (info!=null)
					label += "\n" + info;
			}
			if (imp.getCalibration().pixelWidth!=cal.pixelWidth)
				allSameCalibration = false;
			ImageStack inputStack = imp.getStack();
			for (int slice=1; slice<=inputStack.getSize(); slice++) {
				ImageProcessor ip = inputStack.getProcessor(slice);
				int bitDepth2 = imp.getBitDepth();
				if (!openAsVirtualStack) {
					if (convertToRGB) {
						ip = ip.convertToRGB();
						bitDepth2 = 24;
					}
					if (bitDepth2!=bitDepth) {
						if (bitDepth==8) {
							ip = ip.convertToByte(true);
							bitDepth2 = 8;
						} else if (bitDepth==24) {
							ip = ip.convertToRGB();
							bitDepth2 = 24;
						}
					}
					if (bitDepth2!=bitDepth) {
						IJ.log(list[i] + ": wrong bit depth; "+bitDepth+" expected, "+bitDepth2+" found");
						break;
					}
				}
				if (slice==1) count++;
				IJ.showStatus(count+"/"+n);
				IJ.showProgress(count, n);
				if (scale<100.0)
					ip = ip.resize((int)(width*scale/100.0), (int)(height*scale/100.0));
				if (ip.getMin()<min) min = ip.getMin();
				if (ip.getMax()>max) max = ip.getMax();
				String label2 = label;
				//if (depth>1) label2 = null;
				if (openAsVirtualStack) {
					if (slice==1) ((VirtualStack)stack).addSlice(list[i]);
				} else
					stack.addSlice(label2, ip);
			}
			if (count>=n)
				break;
			if (IJ.escapePressed())
				{IJ.beep(); break;}
			//System.gc();
		}
	} catch(OutOfMemoryError e) {
		IJ.outOfMemory("WJFolderOpener");
		if (stack!=null) stack.trim();
	}
	if (stack!=null && stack.getSize()>0) {
		ImagePlus imp2 = new ImagePlus(title, stack);
		if (imp2.getType()==ImagePlus.GRAY16 || imp2.getType()==ImagePlus.GRAY32)
			imp2.getProcessor().setMinAndMax(min, max);
		if (fi==null)
			fi = new FileInfo();
		fi.fileFormat = FileInfo.UNKNOWN;
		fi.fileName = "";
		fi.directory = directory;
		imp2.setFileInfo(fi); // saves FileInfo of the first image
		if (allSameCalibration) {
			// use calibration from first image
			if (scale!=100.0 && cal.scaled()) {
				cal.pixelWidth /= scale/100.0;
				cal.pixelHeight /= scale/100.0;
			}
			if (cal.pixelWidth!=1.0 && cal.pixelDepth==1.0)
				cal.pixelDepth = cal.pixelWidth;
			if (cal.pixelWidth<=0.0001 && cal.getUnit().equals("cm")) {
				cal.pixelWidth *= 10000.0;
				cal.pixelHeight *= 10000.0;
				cal.pixelDepth *= 10000.0;
				cal.setUnit("um");
			}
			imp2.setCalibration(cal);
		}
		if (info1!=null && info1.lastIndexOf("7FE0,0010")>0) {
			stack = DicomTools.sort(stack);
			imp2.setStack(stack);
			double voxelDepth = DicomTools.getVoxelDepth(stack);
			if (voxelDepth>0.0) {
				if (IJ.debugMode) IJ.log("DICOM voxel depth set to "+voxelDepth+" ("+cal.pixelDepth+")");
				cal.pixelDepth = voxelDepth;
				imp2.setCalibration(cal);
			}
		}
		if (imp2.getStackSize()==1 && info1!=null)
			imp2.setProperty("Info", info1);
		if (arg==null) {
			imp2.show();
		}
		else
			image = imp2;
	}
	IJ.showProgress(1.0);
}

boolean showDialog(ImagePlus imp, String[] list) {
	int fileCount = list.length;
	WJFolderOpenerDialog gd = new WJFolderOpenerDialog("Sequence Options", imp, list);
	gd.addNumericField("Number of images:", fileCount, 0);
	gd.addNumericField("Starting image:", 1, 0);
	gd.addNumericField("Increment:", 1, 0);
	gd.addNumericField("Scale images:", scale, 0, 4, "%");
	gd.addStringField("File name contains:", "", 10);
	gd.addStringField("or enter pattern:", "", 10);
	gd.addCheckbox("Convert_to_RGB", convertToRGB);
	gd.addCheckbox("Sort names numerically", sortFileNames);
	gd.addCheckbox("Use virtual stack", openAsVirtualStack);
	gd.addMessage("10000 x 10000 x 1000 (100.3MB)");
	gd.addHelp(IJ.URL+"/docs/menus/file.html#seq1");
	gd.showDialog();
	if (gd.wasCanceled())
		return false;
	n = (int)gd.getNextNumber();
	start = (int)gd.getNextNumber();
	increment = (int)gd.getNextNumber();
	if (increment<1)
		increment = 1;
	scale = gd.getNextNumber();
	if (scale<5.0) scale = 5.0;
	if (scale>100.0) scale = 100.0;
	filter = gd.getNextString();
	String regex = gd.getNextString();
	if (!regex.equals("")) {
		filter = regex;
		isRegex = true;
	}
	convertToRGB = gd.getNextBoolean();
	sortFileNames = gd.getNextBoolean();
	openAsVirtualStack = gd.getNextBoolean();
	if (openAsVirtualStack)
		scale = 100.0;
	if (!IJ.macroRunning()) {
		staticSortFileNames = sortFileNames;
		staticOpenAsVirtualStack = openAsVirtualStack;
	}
	return true;
}

/** Removes names that start with "." or end with ".db". ".txt", ".lut", "roi", ".pty" or ".hdr", ".py", etc. */
public String[] trimFileList(String[] rawlist) {
	int count = 0;
	for (int i=0; i< rawlist.length; i++) {
		String name = rawlist[i];
		if (name.startsWith(".")||name.equals("Thumbs.db")||excludedFileType(name))
			rawlist[i] = null;
		else
			count++;
	}
	if (count==0) return null;
	String[] list = rawlist;
	if (count<rawlist.length) {
		list = new String[count];
		int index = 0;
		for (int i=0; i< rawlist.length; i++) {
			if (rawlist[i]!=null)
				list[index++] = rawlist[i];
		}
	}
	return list;
}

/* Returns true if 'name' ends with ".txt", ".lut", ".roi", ".pty", ".hdr", ".java", ".ijm", ".py", ".js" or ".bsh. */
public static boolean excludedFileType(String name) {
	if (name==null) return true;
	for (int i=0; i<excludedTypes.length; i++) {
		if (name.endsWith(excludedTypes[i]))
			return true;
	}
	return false;
}

/** Sorts the file names into numeric order. */
public String[] sortFileList(String[] list) {
	int listLength = list.length;
	boolean allSameLength = true;
	int len0 = list[0].length();
	for (int i=0; i<listLength; i++) {
		if (list[i].length()!=len0) {
			allSameLength = false;
			break;
		}
	}
	if (allSameLength)
		{ij.util.StringSorter.sort(list); return list;}
	int maxDigits = 15;		
	String[] list2 = null;	
	char ch;	
	for (int i=0; i<listLength; i++) {
		int len = list[i].length();
		String num = "";
		for (int j=0; j<len; j++) {
			ch = list[i].charAt(j);
			if (ch>=48&&ch<=57) num += ch;
		}
		if (list2==null) list2 = new String[listLength];
		if (num.length()==0) num = "aaaaaa";
		num = "000000000000000" + num; // prepend maxDigits leading zeroes
		num = num.substring(num.length()-maxDigits);
		list2[i] = num + list[i];
	}
	if (list2!=null) {
		ij.util.StringSorter.sort(list2);
		for (int i=0; i<listLength; i++)
			list2[i] = list2[i].substring(maxDigits);
		return list2;	
	} else {
		ij.util.StringSorter.sort(list);
		return list;   
	}	
}

public void openAsVirtualStack(boolean b) {
	openAsVirtualStack = b;
}

public void sortFileNames(boolean b) {
	sortFileNames = b;
}

/** XXX: Method added for WingJ. Returns the filter entered by the user. */
public String getFilter() { return filter; }

/** XXX: Returns true if the automatic images loading has been performed. */
public boolean getAuto() { return auto_; }


} // WJFolderOpener

class WJFolderOpenerDialog extends GenericDialog {

	private static final long serialVersionUID = 1L;
	ImagePlus imp;
	int fileCount;
	boolean eightBits, rgb;
	String[] list;
	boolean isRegex;

public WJFolderOpenerDialog(String title, ImagePlus imp, String[] list) {
	super(title);
	this.imp = imp;
	this.list = list;
	this.fileCount = list.length;
}

@Override
protected void setup() {
		eightBits = ((Checkbox)checkbox.elementAt(0)).getState();
		rgb = ((Checkbox)checkbox.elementAt(1)).getState();
	setStackInfo();
}
	
@Override
public void itemStateChanged(ItemEvent e) {
}

@Override
public void textValueChanged(TextEvent e) {
		setStackInfo();
}

void setStackInfo() {
	int width = imp.getWidth();
	int height = imp.getHeight();
	int depth = imp.getStackSize();
	int bytesPerPixel = 1;
		int n = getNumber(numberField.elementAt(0));
	int start = getNumber(numberField.elementAt(1));
	int inc = getNumber(numberField.elementAt(2));
	double scale = getNumber(numberField.elementAt(3));
	if (scale<5.0) scale = 5.0;
	if (scale>100.0) scale = 100.0;
	
	if (n<1) n = fileCount;
	if (start<1 || start>fileCount) start = 1;
	if (start+n-1>fileCount)
		n = fileCount-start+1;
	if (inc<1) inc = 1;
		TextField tf = (TextField)stringField.elementAt(0);
		String filter = tf.getText();
	tf = (TextField)stringField.elementAt(1);
		String regex = tf.getText();
	if (!regex.equals("")) {
		filter = regex;
		isRegex = true;
	}
		if (!filter.equals("") && !filter.equals("*")) {
			int n2 = 0;
		for (int i=0; i<list.length; i++) {
			if (isRegex&&list[i].matches(filter))
				n2++;
			else if (list[i].indexOf(filter)>=0)
				n2++;
		}
		if (n2<n) n = n2;
		}
	switch (imp.getType()) {
		case ImagePlus.GRAY16:
			bytesPerPixel=2;break;
		case ImagePlus.COLOR_RGB:
		case ImagePlus.GRAY32:
			bytesPerPixel=4; break;
	}
	if (eightBits)
		bytesPerPixel = 1;
	if (rgb)
		bytesPerPixel = 4;
	width = (int)(width*scale/100.0);
	height = (int)(height*scale/100.0);
	int n2 = ((fileCount-start+1)*depth)/inc;
	if (n2<0) n2 = 0;
	if (n2>n) n2 = n;
	double size = ((double)width*height*n2*bytesPerPixel)/(1024*1024);
		((Label)theLabel).setText(width+" x "+height+" x "+n2+" ("+IJ.d2s(size,1)+"MB)");
}

public int getNumber(Object field) {
	TextField tf = (TextField)field;
	String theText = tf.getText();
//	double value;
	Double d;
	try {d = new Double(theText);}
	catch (NumberFormatException e){
		d = null;
	}
	if (d!=null)
		return (int)d.doubleValue();
	else
		return 0;
  }

} // WJFolderOpenerDialog
