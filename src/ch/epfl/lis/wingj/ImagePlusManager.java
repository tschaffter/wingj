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

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * Image manager for WingJ for handling multiple ImagePlus objects.
 * <p>
 * ImagePlusManager makes use of the Singleton design pattern. There's at most one
 * instance present, which can only be accessed through getInstance().
 * 
 * @version October 21, 2012
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class ImagePlusManager {
	
	/** The unique instance of ImagePlusManager (Singleton design pattern). */
	private static ImagePlusManager instance_ = null;
	
	/** Images to manage. */
	private List<Item> items_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Default constructor. */
	private ImagePlusManager() {
		
		items_ = new ArrayList<Item>();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes all image but don't remove the references. */
	private void closeAll() {
		
		int n = items_.size();
		for (int i = 0; i < n; i++)
			items_.get(i).close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the MyImagePlus item matching the given name. */
	private Item getItem(String name) {
		
		int n = items_.size();
		for (int i = 0; i < n; i++) {
			if (items_.get(i).name_.compareTo(name) == 0)
				return items_.get(i);
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the MyImagePlus items containing the given sub string. */
	private List<Item> getItemsContainingSubString(String substr) {
		
		int n = items_.size();
		List<Item> list = new ArrayList<Item>();
		for (int i = 0; i < n; i++) {
			if (items_.get(i).name_.contains(substr))
				list.add(items_.get(i));
		}
		return list;
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Gets Universal instance. */
	static public ImagePlusManager getInstance() {
		
		if (instance_ == null)
			instance_ = new ImagePlusManager();
		
		return instance_;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Adds an ImagePlus identified by a name to the manager. If an image with the
	 * same name already exist, it is deleted.
	 */
	public void add(String name, ImagePlus image) throws Exception {
		
		remove(name);
		add(name, image, false); // by default, do not display the image
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Adds an ImagePlus identified by a name to the manager. If an image with the
	 * same name already exist, it is deleted.
	 */
	public void add(String name, ImagePlus image, boolean show) throws Exception {

		remove(name);
		Item item = new Item(name, image);
		items_.add(item);
		if (show)
			item.show();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes the item identified by the given name, if it exists. */
	public void remove(String name) {
		
		Item item = getItem(name);
		if (item != null) {
			item.close();
			items_.remove(item);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes all item whose name contains the given sub string. */
	public void removeAllContainingSubString(String substr) {
		
		List<Item> list = getItemsContainingSubString(substr);
		for (Item i : list) {
			i.close();
			items_.remove(i);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Removes all items but the ones specified as "img1 img2 ... img3". */
	public void removeAllBut(String but) {
		
		List<String> keep = Arrays.asList(but.split(" "));
		List<Item> backup = new ArrayList<Item>();
		for (int i = 0; i < keep.size(); i++) {
			Item item = getItem(keep.get(i));
			backup.add(item);
			items_.remove(item);
		}
		removeAll();
		
		for (int i = 0; i < keep.size(); i++)
			items_.add(backup.get(i));
		backup.clear();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the index of the image maching the given name, or -1 if not found. */
	public int getImageIndex(String name) {

		int n = items_.size();
		for (int i = 0; i < n; i++) {
			if (items_.get(i).name_.compareTo(name) == 0)
				return i;
		}
		return -1;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the ImagePlus matching the given name, or null if not found. */
	public ImagePlus getImage(String name) {

		int n = items_.size();
		for (int i = 0; i < n; i++) {
			if (items_.get(i).name_.compareTo(name) == 0)
				return items_.get(i).image_;
		}
		return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the ImagePlus matching the given index, or null if not found. */
	public ImagePlus getImage(int index) {
		
		if (index >= 0 && index < items_.size())
			return items_.get(index).image_;
		else
			return null;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows the ImagePlus identified by the given name, if it exists. */
	public void show(String name) {
		
		Item item = getItem(name);
		if (item != null) {
			item.hide();
			item.show();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Hides the ImagePlus identified by the given name, if it exists. */
	public void hide(String name) {
		
		Item item = getItem(name);
		if (item != null) item.hide();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Shows all ImagePlus contained in the manager. */
	public void showAll() {
		
		int n = items_.size();
		for (int i = 0; i < n; i++)
			items_.get(i).show();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Hides all ImagePlus contained in the manager. */
	public void hideAll() {
		
		int n = items_.size();
		for (int i = 0; i < n; i++)
			items_.get(i).hide();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Closes all the images before removing the references. */
	public void removeAll() {
		
		closeAll();
		items_.clear();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Prints the name of each images referenced in the manager. */
	public void printAllNames() {
		
		for (int i = 0; i < items_.size(); i++)
			System.out.println("Image " + i + ": " + items_.get(i).name_);
	}
	
	// ============================================================================
	// SETTERS AND GETTERS

	/** Returns the number of images in the manager. */
	public int size() throws Exception { return items_.size(); }
	/** Returns the name of the image associated to the image index. */
	public String getName(int index) { return items_.get(index).name_; }
	
	// ============================================================================
	// INNER CLASSES

	private class Item {
		
		/** Identifier */
		protected String name_ = null;
		/** Image */
		protected ImagePlus image_ = null;
		
		// ============================================================================
		// PUBLIC METHODS
		
		/** Constructor */
		public Item(String name, ImagePlus image) {
			
			if ((name_ = name) == null)
				name_ = "";
			image_ = image;
		}
		
		// ----------------------------------------------------------------------------
		
		/** Close the image (i.e. set the ImageProcessor to null) */
		public void close() {
			
			image_.killRoi();
			image_.close();
			image_.flush();
		}
		
		// ----------------------------------------------------------------------------
		
		public void show() { image_.setDisplayRange(0., 255.); image_.show(); } //image_.show();
		public void hide() { image_.hide(); }
	}
}
