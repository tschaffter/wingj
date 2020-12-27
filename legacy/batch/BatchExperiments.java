/*
Copyright (c) 2010-2012 Thomas Schaffter & Ricard Delgado-Gonzalo

WingJ is licensed under a
Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.

You should have received a copy of the license along with this
work. If not, see http://creativecommons.org/licenses/by-nc-nd/3.0/.

If this software was useful for your scientific work, please cite our paper(s)
listed on http://wingj.sourceforge.net.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package ch.epfl.lis.wingj.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.WJMessage;
import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.utilities.FileUtils;



/** 
 * Implements batch experiments in WingJ.
 * 
 * @version August 29, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class BatchExperiments {

	/** Directory containing the experiment folders */
	protected String rootDirectory_ = null;
	/** Name of the folder where the images are */
	protected String imagesDirectory_ = "images";
	/** Name of the target folder where the dataset will be saved */
	protected String outputDirectory_ = "WingJ";

	/** Names of the folders containing the experiments */
	protected List<String> experimentNames_ = null;

	/** Index of the first experiment */
	protected int firstExperimentIndex_ = 0;
	/** Index of the last experiment */
	protected int lastExperimentIndex_ = 0;
	/** Index of the current experiment */
	protected int currentExperimentIndex_ = 0;

	/** Is the batch experiments running ? */
	protected boolean running_ = false;

	// ============================================================================
	// PROTECTED METHODS

	/** Create a filter which returns true if one of the subfolders corresponds to imageFolder_ */
	protected FilenameFilter imagesFolderFilter() {

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (!dir.isDirectory()) return false;
				return (name.compareTo(imagesDirectory_) == 0);
			}
		};
		return filter;
	}

	// ----------------------------------------------------------------------------

	/** Create a filter which returns true if one of the subfolders corresponds to datasetsFolder_ */
	protected FilenameFilter outputFolderFilter() {

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (!dir.isDirectory()) return false;
				return (name.compareTo(outputDirectory_) == 0);
			}
		};
		return filter;
	}

	// ----------------------------------------------------------------------------

	/** Create a filter which returns true if a experiment folder matches the requirements */
	protected FilenameFilter experimentFolderFilter() {

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				// the experiment folder name must not start with "."
				if (name.startsWith(".")) return false;
				// the experiment folder must contain a folder named imagesFolder_
				File experimentFile = new File(dir.getAbsoluteFile() + "/" + name);
				String[] children = experimentFile.list(imagesFolderFilter());
				if (children == null || children.length != 1) return false;
				// the present folder match the requirements to be considered has containing an experiment
				return true;
			}
		};
		return filter;
	}

	// ----------------------------------------------------------------------------

	/** Return the given output folder if the given experiment already contains one */
	protected File getOutputDirectory(String experimentDirectoryAbsPath) {

		File experimentDirectoryFile = new File(experimentDirectoryAbsPath);
		String[] children = experimentDirectoryFile.list(outputFolderFilter());

		if (children.length == 0) return null;
		return new File(experimentDirectoryAbsPath + children[0]);
	}

	// ----------------------------------------------------------------------------

	/** List the name of the experiments and order then in alphabetical order */
	protected void listExperiments(String path) throws Exception {

		File pathFile = new File(path);
		String[] children = pathFile.list(experimentFolderFilter());

		if (children == null)
			throw new Exception("ERROR: The selected directory doesn't exist.");
		if (children.length == 0)
			throw new Exception("INFO: The select directory doesn't contain any experiments.");

		experimentNames_ = Arrays.asList(children);
		Collections.sort(experimentNames_);
	}

	// ----------------------------------------------------------------------------

	/** Start and initialize a new individual experiment */
	protected int makeOutputDirectory(String experimentName) throws Exception {

		String experimentDirectoryAbsPath = getExperimentDirectoryAbsPath(experimentName);

		File outputDirectoryFile = getOutputDirectory(experimentDirectoryAbsPath);
		if (outputDirectoryFile == null) { // the output folder doesn't exist yet
			FileUtils.mkdir(getOutputDirectoryAbsPath(experimentName));
			return JOptionPane.YES_OPTION; // go ahead
		}
		else {

			int dialogType = JOptionPane.YES_NO_OPTION;
			String message = "The output folder \"" + outputDirectory_ + "\" already exist for experiment\n" +
					experimentName + ".\n\n" +
					"Click \"Yes\" to start analyzing this experiment (old files will be removed).\n" +
					"Click \"No\" to skip this experiment and move to the next one.";

			// batch experiment just started
			if (currentExperimentIndex_ == firstExperimentIndex_)
				dialogType = JOptionPane.YES_NO_CANCEL_OPTION;

			int reply = JOptionPane.showConfirmDialog(WingJ.getInstance(),
					message,
					"WingJ message",
					dialogType);

			if (reply == JOptionPane.YES_OPTION) {

				// delete the content of the folder "WingJ" but keep the file
				// "settings.txt" and "structure.xml" (if they exist)
				cleanOutputDirectory(outputDirectoryFile);

				//	        	// delete the existing folder and create a new one
				//	        	WJSettings.log("Removing directory " + outputDirectoryAbsPath);
				//	        	FileUtils.deleteDirectory(outputDirectoryFile);
				//	        	WJSettings.log("Making output directory " + outputDirectoryAbsPath);
				//	        	Files.mkdir(outputDirectoryAbsPath);

			} else if (reply == JOptionPane.NO_OPTION) {
				// skip the current experiment and move to the next one
			} else {
				// cancel
			}
			return reply; // go ahead/skip/cancel
		}
	}

	// ----------------------------------------------------------------------------

	/** Get the name of the different channel */
	protected String[] getChannelNames(String experimentName) throws Exception {

		WJSettings settings = WJSettings.getInstance();
		String[] names = new String[WJSettings.NUM_CHANNELS];

		StringTokenizer tokenizer = new StringTokenizer(experimentName, "_");
		List<String> tokens = new ArrayList<String>();
		while(tokenizer.hasMoreTokens())
			tokens.add(tokenizer.nextToken());

		String filenamesConventionMsg = "WingJ failed to recover the channel name(s)\n" +
				"from the name of the experiment folder.\n\n" +
				"Please refer to the user manual for more information\n" +
				"about the file naming convention used by WingJ.";

		// the folder name must have at least date + 1 channel name
		if (tokens.size() < 2)
			throw new Exception("WARNING: " + filenamesConventionMsg);

		try {
			// get mutant name (if any)
			// ignore first token, i.e. the experiment date
			int firstChannelIndex = 1;
			for (firstChannelIndex = 1; firstChannelIndex < tokens.size(); firstChannelIndex++) {
				if (tokens.get(firstChannelIndex).endsWith(WJSettings.MUTANT_SYMBOL))
					continue;
				else
					break;
			}

			for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
				if (settings.getBatchChannelAutoLoading(i))
					names[i] = tokens.get(firstChannelIndex++);
				else
					names[i] = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("WARNING: " + filenamesConventionMsg);
		}

		return names;
	}

	// ----------------------------------------------------------------------------

	/** Called when all experiments have been analyzed */
	protected void done() throws Exception {

		WJMessage.showMessage("BATCH EXPERIMENTS DONE", WJMessage.INFO_PREFIX);
		currentExperimentIndex_ = lastExperimentIndex_;
		running_ = false;
	}

	// ----------------------------------------------------------------------------

	/** Clean batch mode */
	protected void clean() {

		rootDirectory_ = null;
		experimentNames_ = null;
		firstExperimentIndex_ = 0;
		lastExperimentIndex_ = 0;
		currentExperimentIndex_ = 0;
		running_ = false;
	}

	// ----------------------------------------------------------------------------

	/** Return a File if a settings file already exists in the folder WingJ */
	protected File getOldSettingsFile(String experimentName) throws Exception {

		URI uri = FileUtils.getFileURI(getOutputDirectoryAbsPath(experimentName) + "settings.txt");
		File file = new File(uri);

		if (file.isFile() && file.exists())
			return file;

		return null;
	}

	// ----------------------------------------------------------------------------

	/** Open additional parameters in settings file which are not loaded by default (e.g. slice range) */
	protected void openSuperSettings(File settingsFile) throws Exception {

		if (settingsFile == null)
			throw new Exception("ERROR: settingsFile is null.");

		InputStream stream = settingsFile.toURI().toURL().openStream();
		Properties p = null;

		WJSettings settings = WJSettings.getInstance();

		p = new Properties();
		p.load(stream);

		// load default settings
		settings.loadSettings(p);

		// load additional settings which are usually only saved but not loaded
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			try {
				settings.setExpressionMinSliceIndex(i, Integer.valueOf(p.getProperty("expressionCh0" + i + "MinSliceIndex")));
			} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expressionCh0" + i + "MinSliceIndex not found."); }

			try {
				settings.setExpressionMaxSliceIndex(i, Integer.valueOf(p.getProperty("expressionCh0" + i + "MaxSliceIndex")));
			} catch (Exception e) { WJMessage.showMessage("WARNING: Parameter expressionCh0" + i + "MaxSliceIndex not found."); }
		}
	}

	// ----------------------------------------------------------------------------

	/** Return a file if a old structure file has been found */
	protected File getOldStructureFile(String experimentName) throws Exception {

		URI uri = FileUtils.getFileURI(getOutputDirectoryAbsPath(experimentName) + "structure.xml");
		File file = new File(uri);

		if (file.isFile() && file.exists())
			return file;

		return null;
	}

	// ----------------------------------------------------------------------------

	/** Return a file if the AOI file has been found */
	protected File getAoiFile(String experimentName) throws Exception {

		URI uri = FileUtils.getFileURI(getOutputDirectoryAbsPath(experimentName) + "aoi.tif");
		File file = new File(uri);

		if (file.exists() && file.isFile())
			return file;

		return null;
	}

	// ----------------------------------------------------------------------------

	/** Remove all the files in the output folder but the files "settings.txt" and "structure.xml" */
	protected void cleanOutputDirectory(File outputDirectoryFile) throws Exception {

		if (outputDirectoryFile == null)
			throw new Exception("ERROR: outputDirectoryFile is null.");

		String[] children = outputDirectoryFile.list();
		for (int i = 0; i < children.length; i++) {
			if (children[i].compareTo("settings.txt") == 0 ||
					children[i].compareTo("structure.xml") == 0 ||
					children[i].compareTo("aoi.tif") == 0)
				continue;
			// otherwise delete the file
			URI uri = FileUtils.getFileURI(outputDirectoryFile.getAbsoluteFile() + "/" + children[i]);
			org.apache.commons.io.FileUtils.deleteQuietly(new File(uri));
		}
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor */
	public BatchExperiments() {}

	// ----------------------------------------------------------------------------

	/** Initialization */
	public void initialize(String path) throws Exception {

		try {
			rootDirectory_ = path;
			listExperiments(path);

			firstExperimentIndex_ = 0;
			lastExperimentIndex_ = experimentNames_.size() - 1; 
			currentExperimentIndex_ = firstExperimentIndex_;

			WJSettings.log("Batch: " + (experimentNames_.size()) + " experiments found");

		} catch (Exception e) {
			clean();
			throw e;
		}
	}

	// ----------------------------------------------------------------------------

	/** Skip the current experiment */
	public void skip() throws Exception {

		myfinalize();
		next();
	}

	// ----------------------------------------------------------------------------

	/** Start the batch experiments */
	public void start() throws Exception {

		WJSettings.log("Starting batch experiments");
		running_ = true;
		next();
	}

	// ----------------------------------------------------------------------------

	/** Cancel the batch experiments */
	public void cancel() throws Exception {

		WJSettings.log("Stopping batch experiment");
		running_ = false;
	}

	// ----------------------------------------------------------------------------

	/** Initialize the experiment, return true if it has been correctly initialized */
	public void next() throws Exception {

		if (!running_)
			return;

		// setup the output directory
		String experimentName = experimentNames_.get(currentExperimentIndex_);
		int status  = makeOutputDirectory(experimentName);

		switch (status) {
		case JOptionPane.YES_OPTION: break;
		case JOptionPane.NO_OPTION: skip(); return;
		case JOptionPane.CANCEL_OPTION:
			currentExperimentIndex_--;
			return;
		}

		// clear the log content
		WJSettings.clearLog();

		// update the required settings
		WJSettings settings = WJSettings.getInstance();
		settings.setOutputDirectory(getOutputDirectoryAbsPath(experimentName));
		settings.setExperimentName(experimentName);

		String[] channelNames = getChannelNames(experimentName);
		for (int i = 0; i < channelNames.length; i++)
			settings.setGeneNames(channelNames[i], i);

		// check if a settings file is in the folder WingJ
		// if yes, shall we load it ?
		File settingsFile = getOldSettingsFile(experimentName);
		if (settingsFile != null) {
			int reply = JOptionPane.showConfirmDialog(WingJ.getInstance(),
					"Open available settings ?",
					"WingJ message",
					JOptionPane.YES_NO_OPTION);

			// the settings should be loaded after loading the images
			// so if we don't want to load them, lets just set settingsFile = null
			// and it will not be used later
			if (reply == JOptionPane.NO_OPTION) {
				org.apache.commons.io.FileUtils.deleteQuietly(settingsFile);
				settingsFile = null;
			}
		}

		// open the required image stacks
		String imageDirectoryAbsPath = getImagesDirectoryAbsPath(experimentName);
		for (int i = 0; i < WJSettings.NUM_CHANNELS; i++) {
			if (settings.getBatchChannelAutoLoading(i))
				WJImages.openImageStack(i, imageDirectoryAbsPath);
		}

		// load additional settings
		if (settingsFile != null)
			openSuperSettings(settingsFile);

		// load existing AOI (if any) or set a new AOI fitting the image
		try {
			WJImages.openAoi(getAoiFile(experimentName));
			WJSettings.log("AOI found.");
		} catch (Exception e) {
			//e.printStackTrace();
		}

		WingJ.getInstance().settings2gui();

		// is there a old structure file available
		File structureFile = getOldStructureFile(experimentName);
		if (structureFile != null)
			WingJ.getInstance().openStructureFile(structureFile.toURI());
	}

	// ----------------------------------------------------------------------------

	/**
	 * Called to finalize the current experiment
	 * IMPORTANT: Something completely crazy! If the method is called "finalize", the
	 * method is called by some (random?) code. When I tried to rename it through
	 * the refractor, it says something about the rt library...
	 */
	public void myfinalize() throws Exception {

		currentExperimentIndex_++;
		if (currentExperimentIndex_ > lastExperimentIndex_)
			done();
	}

	// ----------------------------------------------------------------------------

	/** Return the progress */
	public double getProgress() {

		if (currentExperimentIndex_ == lastExperimentIndex_ && !running_) // once batch is done
			return 100.;
		else
			return ((100.*currentExperimentIndex_)/(lastExperimentIndex_ + 1.));
	}

	// ----------------------------------------------------------------------------

	/** Return the progress as a String "current/total" */
	public String getProgressAsString() {

		if (currentExperimentIndex_ == lastExperimentIndex_ && !running_) // once batch is done
			return new String((lastExperimentIndex_+1) + " / " + (lastExperimentIndex_+1));
		else
			return new String(currentExperimentIndex_ + " / " + (lastExperimentIndex_+1));
	}

	// ----------------------------------------------------------------------------
	/** Return the progress (parameter example: new DecimalFormat("#.##")) */
	public String getProgress(DecimalFormat formatter) {

		return formatter.format((100.*currentExperimentIndex_)/(double)lastExperimentIndex_);
	}

	// ============================================================================
	// SETTERS AND GETTERS

	public String getExperimentName(int index) { return experimentNames_.get(index); }
	public String getFirstExperimentName() { return experimentNames_.get(firstExperimentIndex_); }
	public String getLastExperimentName() { return experimentNames_.get(lastExperimentIndex_); }
	public String getCurrentExperimentName() { return experimentNames_.get(currentExperimentIndex_); }

	public String getExperimentDirectoryAbsPath(String experimentName) { return new String(rootDirectory_ + experimentName + "/"); }
	public String getImagesDirectoryAbsPath(String experimentName) { return new String(getExperimentDirectoryAbsPath(experimentName) + imagesDirectory_ + "/"); }
	public String getOutputDirectoryAbsPath(String experimentName) { return new String(getExperimentDirectoryAbsPath(experimentName) + outputDirectory_) + "/"; }

	public void setFirstExperimentIndex(int index) {
		firstExperimentIndex_ = index;
		currentExperimentIndex_ = index;
	}
	public int getFirstExperimentIndex() { return firstExperimentIndex_; }

	public void setLastExperimentIndex(int index) { lastExperimentIndex_ = index; }
	public int getLastExperimentIndex() { return lastExperimentIndex_; }

	public void setCurrentExperimentIndex(int index) { currentExperimentIndex_ = index; }
	public int getCurrentExperimentIndex() { return currentExperimentIndex_; }

	public void isRunning(boolean b) { running_ = b; }
	public boolean isRunning() { return running_; }
}
