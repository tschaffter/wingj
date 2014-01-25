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

package ch.epfl.lis.wingj.analytics;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

/** 
 * Provides informations about the running Java/OS system.
 * 
 * @version March 31, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class SystemInfo {
	
	/** The unique instance of SystemInfo (Singleton design pattern). */
	private static SystemInfo instance_ = null;
	
	/** Name of the OS. */
	protected String osName_ = "";
	/** Version of the OS. */
	protected String osVersion_ = "";
	/** Architecture of the OS (e.g. x86, x86_64, amd64, etc.). */
	protected String osArch_ = "";
	/** Version of the JVM. */
	protected String javaVersion_ = "";
	/** Sun arch data model (32 or 64 expected). */
	protected String sunArchDataModel_ = "";
	/** Max heap size. */
	protected int maxHeapSizeInMb_ = -1;
	/** Max memory used (can only increase). */
	protected int maxMemoryUsedInMb_ = -1;
	/**
	 * CURRENT free memory:  current total memory allocated - memory used.
	 * This is not the amount of RAM available. 
	 */
	protected int freeMemoryInMb_ = -1;
	/** Country (depends on the info given by the user, it's not geolocation). */
	protected String country_ = "";
	/** Language. */
	protected String language_ = "";
	/** Display dimension (format: "WxH"). */
	protected String displayResolution_ = "";
	/** Localhost MAC address. */
	protected String macAddress_ = "";
	
	// ============================================================================
	// PRIVATE METHODS
    
	/** Default constructor. */
	private SystemInfo() {
		
		initialize();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initializes by collecting the system info. */
	protected void initialize() {
		
		try { osName_ = System.getProperty("os.name"); }
		catch (Exception e) {}
		
		try { osVersion_ = System.getProperty("os.version"); }
		catch (Exception e) {}
		
		try { osArch_ = System.getProperty("os.arch"); }
		catch (Exception e) {}
		
		try { javaVersion_ = System.getProperty("java.version"); }
		catch (Exception e) {}
		
		try { sunArchDataModel_ = System.getProperty("sun.arch.data.model"); }
		catch (Exception e) {}
		
		try { maxHeapSizeInMb_ = (int) (Runtime.getRuntime().maxMemory()/(1024*1024)); }
		catch (Exception e) {}
		
		try { maxMemoryUsedInMb_ = (int) (Runtime.getRuntime().totalMemory()/(1024*1024)); }
		catch (Exception e) {}

		try { freeMemoryInMb_ = (int) (Runtime.getRuntime().freeMemory()/(1024*1024)); }
		catch (Exception e) {}
		
		try { country_ = Locale.getDefault().getCountry(); }
		catch (Exception e) {}
		
		try { language_ = Locale.getDefault().getLanguage(); }
		catch (Exception e) {}
		
		try {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			displayResolution_ = dim.width + "x" + dim.height;
		} catch (Exception e) {}
		
		try { macAddress_ = getFirstMacAddressFound(); }
		catch (Exception e) {}
	}
	
	// ============================================================================
	// PUBLIC METHODS

	/** Gets SystemInfo instance. */
	static public SystemInfo getInstance() {
		
		if (instance_ == null)
			instance_ = new SystemInfo();
		return instance_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Main method. */
	public static void main(String[] args) {
		
		System.out.println(SystemInfo.getInstance().getJsonString());
		
//		System.out.println(SystemInfo.getInstance().toString());
	}
	
	// ----------------------------------------------------------------------------
	
	/** Overrides toString(). */
	public String toString() {
		
		String str = "";

		str += "os.name: " + osName_ + "\n";
		str += "os.version: " + osVersion_ + "\n";
		str += "os.arch: " + osArch_ + "\n";
		str += "java.version: " + javaVersion_ + "\n";
		str += "sun.arch.data.model: " + sunArchDataModel_ + "\n";
		str += "max heap size (Mb): " +  maxHeapSizeInMb_ + "\n";
		str += "max memory used (Mb): " +  maxMemoryUsedInMb_ + "\n";
		str += "country: " + country_ + "\n";
		str += "language: " + language_ + "\n";
		str += "display resolution: " + displayResolution_ + "\n";
		str += "mac address: " + macAddress_;

		return str;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns the system info as a JSON array string. */
	public String getJsonString() {
		
		String json = "[";
		
		json += "[\"os.name\", \"" + osName_ + "\"],";
		json += "[\"os.version\", \"" + osVersion_ + "\"],";
		json += "[\"os.arch\", \"" + osArch_ + "\"],";
		json += "[\"java.version\", \"" + javaVersion_ + "\"],";
		json += "[\"sun.arch.data.model\", \"" + sunArchDataModel_ + "\"],";
		json += "[\"maxHeapSizeInMb\", " + maxHeapSizeInMb_ + "],";
		json += "[\"maxMemoryUsedInMb\", " + maxMemoryUsedInMb_ + "],";
		json += "[\"freeMemory\", " + freeMemoryInMb_ + "],";
		json += "[\"country\", \"" + country_ + "\"],";
		json += "[\"language\", \"" + language_ + "\"],";
		json += "[\"displayResolution\", \"" + displayResolution_ + "\"],";
		json += "[\"maxAddress\", \"" + macAddress_ + "\"]";
		
		json += "]";
		return json;
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Returns the localhost MAC address.
	 * Since JDK 1.6, Java developers are able to access network card detail via NetworkInterface class.
	 */
	public String getFirstMacAddressFound() throws Exception {
 
		// note that there can be zero or more than one network card
		// virtual machines for instance may not have a network card
		// here get all the card interface
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); // required Java 6+
		
		// I don't really care which mac address it is, supposing that the hardware configuration
		// doesn't change this is enough to identify a computer
		NetworkInterface nif = null;
		byte[] mac = null;
		while (interfaces.hasMoreElements() && (mac == null || mac.length == 0)) {
			try {
				nif = interfaces.nextElement();
				mac = nif.getHardwareAddress();
			} catch (Exception e) {}
		}
	
		// here we didn't find a card with a mac address
		if (mac == null)
			return "xx:xx:xx:xx:xx:xx";

		// get it in a human readable format
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++) {
			sb.append(String.format("%1$02X", mac[i]) + ((i < mac.length-1) ? ":" : ""));
		}
		return sb.toString();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Refreshes the current amount of memory used and returns it. */
	public int getMaxMemoryUsedInMb() {
		
		maxMemoryUsedInMb_ = (int) (Runtime.getRuntime().totalMemory()/(1024*1024));
		return maxMemoryUsedInMb_;
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public String getOsName() { return osName_; }
	public String getOsVersion() { return osVersion_; }
	public String getOsArch() { return osArch_; }
	public String getJavaVersion() { return javaVersion_; }
	public String getSunArchDataModel() { return sunArchDataModel_; }
	public int getMaxHeapSizeInMb() { return maxHeapSizeInMb_; }
	public int getFreeMemoryInMb() { return freeMemoryInMb_; }

	public String getCountry() { return country_; }
	public String getLanguage() { return language_; }
	public String getDisplayResolution() { return displayResolution_; }
	public String getMacAddress() { return macAddress_; }
}
