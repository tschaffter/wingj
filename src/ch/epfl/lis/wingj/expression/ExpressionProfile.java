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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;

import ch.epfl.lis.wingj.structure.Boundary;

/** 
 * Gene expression profile measured along a trajectory inside the structure model.
 * <p>
 * A/P is the reference boundary: expression is measured from anterior to posterior side.
 * D/V is the reference boundary: expression is measured from the ventral to dorsal side.
 * x=0 corresponds to where the A/P and D/V boundaries intersect.
 * Unit of the x-axis are [UNIT] (see WJSettings)
 * Values of the y-axis (expression level) are in [0,255] or [0,1] if normalized.
 * 
 * @version August 31, 2011
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class ExpressionProfile {
	
	/** Name of identifier of the profile. */
	private String name_ = "generic_profile";
	
	/** Distance in [UNIT] along the x-axis. */
	private double[] X_ = null;
	/** Contains the expression level. */
	private double[] Y_ = null;
	
	/** XY coordinates of the measurement points. */
	private Boundary trajectory_ = null;
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Default constructor. */
	public ExpressionProfile() {}
	
	// ----------------------------------------------------------------------------
	
	/** Copy constructor. */
	public ExpressionProfile(ExpressionProfile profile) {

		this.name_ = profile.name_;
		this.Y_ = profile.Y_;
		this.X_ = profile.X_;
	}
	
	// ----------------------------------------------------------------------------

	/** Constructor. */
	public ExpressionProfile(double[] xaxis, double[] expression) {
		X_ = xaxis;
		Y_ = expression;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Copy method. */
	public ExpressionProfile copy() {

		return new ExpressionProfile(this);
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Saves the expression level in a four column file (x-axis position \TAB expression \TAB x-axis coordinates \TAB y-axis coordinates).
	 * If the trajectory is not specified, the output file will contain only the two first columns.
	 */
	public void write(URI uri) throws Exception {
		
		if (X_.length != Y_.length)
			throw new Exception("ERROR: Length of x-axis and y-axis must be the same.");
		
		FileWriter fstream = new FileWriter(new File(uri));
		BufferedWriter out = new BufferedWriter(fstream);
	     
		String content = "";
		
		if (trajectory_ == null) {
			for (int i = 0; i < X_.length; i++)
				content += Double.toString(X_[i]) + "\t" + Double.toString(Y_[i]) + "\n";
		} else {
			float[] x = trajectory_.xpoints;
			float[] y = trajectory_.ypoints;
			for (int i = 0; i < X_.length; i++)
				content += Double.toString(X_[i]) + "\t" + Double.toString(Y_[i]) + "\t" + Float.toString(x[i]) + "\t" + Float.toString(y[i]) + "\n";
		}
	     
		out.write(content);
		out.close();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Normalize expression values (division by 255.0). */
	public void normalize() {
		
		for (int i = 0; i < Y_.length; i++) {
			Y_[i] = Y_[i] / 255.0;
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void setName(String name) { name_ = name; }
	public String getName() { return name_; }

	public void setY(double[] expression) { Y_ = expression; }
	public double[] getY() { return Y_; }
	
	public void setX(double[] xaxis) { X_ = xaxis; }
	public double[] getX() { return X_; }
	
	public void setTrajectory(Boundary b) { trajectory_ = b; }
	public Boundary getTrajectory() { return trajectory_; }
}