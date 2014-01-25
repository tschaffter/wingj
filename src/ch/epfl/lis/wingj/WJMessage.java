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

import javax.swing.JOptionPane;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;

/**
 * Shows a message in a single JDialog. Supported types are INFO, WARNING, and ERROR.
 * 
 * @version May 31, 2012
 *
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 * @author Ricard Delgado-Gonzalo (ricard.delg...@gmail.com)
 */
public class WJMessage {

	/** Prefix that an exception message should contain to be considered as INFO. */
	public static final String INFO_PREFIX  = "INFO";
	/** Prefix that an exception message should contain to be considered as WARNING. */
	public static final String WARNING_PREFIX  = "WARNING";
	/** Prefix that an exception message should contain to be considered as ERROR. */
	public static final String ERROR_PREFIX = "ERROR";

	/** Title of the message dialog. */
	private String title_ = "WingJ message";

	// ============================================================================
	// PRIVATE METHODS

	/** Displays message. */
	private void displayMessage(String message) {

		if (message == null) {
			showErrorMessage("Unknown exception. Please refer to the Java stack trace.");
			return;
		}

		if (message.startsWith(INFO_PREFIX)) showInfoMessage(message.substring(INFO_PREFIX.length() + 2));
		else if (message.startsWith(WARNING_PREFIX)) showWarningMessage(message.substring(WARNING_PREFIX.length() + 2));
		else if (message.startsWith(ERROR_PREFIX)) showErrorMessage(message.substring(ERROR_PREFIX.length() + 2));
		else showWarningMessage(message);
	}

	// ============================================================================
	// PUBLIC METHODS

	/** Default constructor. */
	public WJMessage() {}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public WJMessage(Exception e) {

		if (e == null) {
			showErrorMessage("Unknown exception");
			return;
		}

		e.printStackTrace();

		String message = "";
		if (e.getCause() == null || e.getCause().getMessage() == null) message = e.getMessage();
		else message = e.getCause().getMessage();

		displayMessage(message);
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public WJMessage(Error e) {

		if (e == null) {
			showErrorMessage("Unknown exception");
			return;
		}

		e.printStackTrace();

		String message = "";
		if (e.getCause() == null || e.getCause().getMessage() == null) message = e.getMessage();
		else message = e.getCause().getMessage();

		displayMessage(message);
	}

	// ----------------------------------------------------------------------------

	/** Constructor. */
	public WJMessage(String message, String type) {

		if (message == null) {
			showErrorMessage("Null message");
			return;
		}

		if (type == null) {
			showErrorMessage(message);
			return;
		}

		if (type.compareTo(INFO_PREFIX) == 0) showInfoMessage(message);
		else if (type.compareTo(WARNING_PREFIX) == 0) showWarningMessage(message);
		else if (type.compareTo(ERROR_PREFIX) == 0) showErrorMessage(message);
		else showWarningMessage(message);
	}

	// ----------------------------------------------------------------------------

	/** Shows message (type depends on the message prefix). */
	public static void showMessage(Exception e) {

		new WJMessage(e);
	}

	// ----------------------------------------------------------------------------

	/** Shows message (type depends on the message prefix). */
	public static void showMessage(Error e) {

		new WJMessage(e);
	}

	// ----------------------------------------------------------------------------

	/** Shows message with given type. */
	public static void showMessage(String message, String type) {

		new WJMessage(message, type);
	}

	// ----------------------------------------------------------------------------

//	/** Displays a message requesting a number in a spinner. */
//	public static int showSpinnerDialog(String message, Integer initValue) {
//
//		Integer min = new Integer(0);
//		Integer max = new Integer(255); 
//		Integer step = new Integer(1); 
//		SpinnerNumberModel sModel = new SpinnerNumberModel(initValue, min, max, step); 
//		JSpinner spinner = new JSpinner(sModel);
//		int returnValue = JOptionPane.showOptionDialog(WingJ.getInstance(), spinner, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
//		
//		// Ricard: This approach is dangerous because the returned value of JOptionPane
//		// can be positive and be in conflict with the spinner number.
//		if(returnValue==JOptionPane.CLOSED_OPTION){ // value: -1
//			return JOptionPane.CLOSED_OPTION;	
//		} else if (returnValue == JOptionPane.CANCEL_OPTION){ // value: 2
//			return JOptionPane.CANCEL_OPTION;
//		}
//		return sModel.getNumber().intValue();
//	}

	// ----------------------------------------------------------------------------

	/** Shows message (type is INFO). */
	public static void showMessage(String message) {

		if (message.startsWith(INFO_PREFIX)) new WJMessage().showInfoMessage(message.substring(INFO_PREFIX.length() + 1, message.length()));
		else if (message.startsWith(WARNING_PREFIX)) new WJMessage().showWarningMessage(message.substring(WARNING_PREFIX.length() + 1, message.length()));
		else if (message.startsWith(ERROR_PREFIX)) new WJMessage().showErrorMessage(message.substring(ERROR_PREFIX.length() + 1, message.length()));
		else new WJMessage().showInfoMessage(message);
	}

	// ============================================================================
	// PRIVATE METHODS

	/** Displays an INFO dialog and write the message to log. */
	private void showInfoMessage(String message) {

		if (message != null) {
			try {
				JOptionPane.showMessageDialog(WingJ.getInstance(),
						message,
						title_,
						JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e) {
				// do nothing
			}

			WJSettings.log(INFO_PREFIX + ": " + message);
		}
	}

	// ----------------------------------------------------------------------------

	/** Displays a WARNING dialog and write the message to log. */
	private void showWarningMessage(String message) {

		if (message != null) {
			try {
				JOptionPane.showMessageDialog(WingJ.getInstance(),
						message,
						title_,
						JOptionPane.WARNING_MESSAGE);
			} catch (Exception e) {
				// do nothing
			}

			WJSettings.log(WARNING_PREFIX + ": " + message);
		}
	}

	// ----------------------------------------------------------------------------

	/** Displays an ERROR dialog and write the message to log. */
	private void showErrorMessage(String message) {

		if (message != null) {
			try {
				JOptionPane.showMessageDialog(WingJ.getInstance(),
						message,
						title_,
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				// do nothing
			}

			WJSettings.log(ERROR_PREFIX + ": " + message);
		}
	}
}
