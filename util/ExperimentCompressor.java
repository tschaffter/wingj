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

package ch.epfl.lis.wingj.utilities;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageConverter;

import java.io.File;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;

import ch.epfl.lis.wingj.WJImages;
import ch.epfl.lis.wingj.test.Benchmark;
import ch.epfl.lis.wingj.test.Benchmarks;



/**
 * Replaces the content of the directory "images" by the MIP of each channel.
 *
 * @version November 9, 2011
 *
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class ExperimentCompressor {

	/** Absolute path to mega directory */
	protected String megaDirectoryPath_ = null;

	/** List of experiments to compress */
	protected Benchmarks benchmarks_ = null;

	// ============================================================================
	// PRIVATE METHODS

	/** Initialize */
	public void instantiateBenchmarks() throws Exception {

		if (megaDirectoryPath_ == null)
			throw new Exception("ERROR: megaDirectoryPath_ is null.");

		// Open and instantiate generic benchmarks
		benchmarks_ = new Benchmarks();
		benchmarks_.setMegaDirectoryPath(megaDirectoryPath_);
		benchmarks_.open();
	}

	// ----------------------------------------------------------------------------

	/** Remove all the files in the directory "images" */
	protected void cleanImagesDirectory(File imagesDirectoryFile) throws Exception {

		if (imagesDirectoryFile == null)
			throw new Exception("ERROR: imagesDirectoryFile is null.");

		String[] children = imagesDirectoryFile.list();
		for (int i = 0; i < children.length; i++)
			FileUtils.deleteQuietly(new File(imagesDirectoryFile.getAbsoluteFile() + "/" + children[i]));
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Replaces the content of the folder "images" by the MIP of each channel. */
	public static void main(String[] args) {

		try {
			ExperimentCompressor compressor = new ExperimentCompressor();
			compressor.setMegaDirectoryPath("/mnt/extra/to_compress/all_experiments_2/");
			compressor.run();

			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//----------------------------------------------------------------------------

	/** Compress each available experiments */
	public void run() throws Exception {

		// open and register experiments
		instantiateBenchmarks();

		// compress each experiment
		Benchmark b = null;
		for (int i = 0; i < benchmarks_.getNumBenchmarks(); i++) {
			b = benchmarks_.getBenchmark(i);
			try {
				System.out.println("INFO: Compressing " + b.getName());
				System.out.println("INFO: Experiment directory: " + b.getDirectoryPath());
				compressImages(b);
			} catch (Exception e) {
				System.out.println("ERROR: Unable to compress experiment " + b.getName() + ".");
				e.printStackTrace();
			} finally {
				System.out.println("INFO: Progress: " + new DecimalFormat("#.##").format((100. * i) / (double)Benchmark.numBenchmarks_) + "%");
			}
		}

		System.out.println("Done");
	}

	//----------------------------------------------------------------------------

	/** Compress the folder "images" of an experiment */
	public void compressImages(Benchmark experiment) throws Exception {

		String imagesDirectoryPath = experiment.getDirectoryPath() + "images/";
		File imagesDirectoryFile = new File(imagesDirectoryPath);

		if (imagesDirectoryFile == null || !imagesDirectoryFile.isDirectory())
			throw new Exception("WARNING: Directory \"images\" not found.");

		// for each channel
		for (int channel = 0; channel < 4; channel++) {
			try {
				WJImages.openImageStack(channel, imagesDirectoryPath);
				System.out.println("INFO: Channel " + channel + " found.");
			} catch (Exception e) {
				// if no images associated to the given channel, do nothing
			}
		}

		// delete silently the content of the folder "images"
		cleanImagesDirectory(imagesDirectoryFile);

		// save projections
		for (int channel = 0; channel < 4; channel ++) {
			ImagePlus mip = null;
			if ((mip = WJImages.getImageProjection(channel)) != null) {
				new ImageConverter(mip).convertToRGB();
				IJ.save(mip, imagesDirectoryPath + "mip_z00_ch0" + channel + ".tif");
				mip.close();
			}
		}
	}

	// ============================================================================
	// GETTERS AND SETTERS

	public void setMegaDirectoryPath(String path) { megaDirectoryPath_ = path; }
	public String getMegaDirectoryPath() { return megaDirectoryPath_; }
}
