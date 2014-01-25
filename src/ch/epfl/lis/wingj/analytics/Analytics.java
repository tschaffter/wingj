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

import ij.ImageJ;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import ch.epfl.lis.wingj.WJSettings;
import ch.epfl.lis.wingj.WingJ;
import ch.epfl.lis.wingj.utilities.StringUtils;

import com.google.gson.Gson;

/** 
 * Handles the statistics generated during the current session.
 * 
 * @version March 31, 2013
 * 
 * @author Thomas Schaffter (thomas.schaff...@gmail.com)
 */
public class Analytics {
	
	/** Allows or not to contact the database (e.g. set to false when debugging the software that calls Analytics).  */
	private static boolean ALLOW_DB_CONNECTION = true;
	
	/** The unique instance of Analyzer (Singleton design pattern). */
	private static Analytics instance_ = null;
	
	/** Executor processing data at fixed interval. */
	protected ScheduledExecutorService executor_ = null;
	/** The method run() of this Runnable object is executed at fixed interval. */
	protected Runnable periodicTask_ = null;
	/** Time in seconds between two execution of the periodic task. */
	protected int periodicTaskIntervalInSec_ = 10;
	
	/** Address of the server for reporting stats (with final '/'). */
	protected static String ANALYTICS_SERVER_ADDRESS = "http://tschaffter.ch/projects/wingj_analytics/";
	/** Address to test if the client has access to Internet. */
	protected static String INTERNET_CONNECTION_TEST_ADDRESS = "http://www.google.com";
	
	/** Email address(es) to where server down notification should be sent (coded, use ';' to delimit several addresses). */
	protected static String CONTACT_EMAILS = "thomas.schaff...@gmail.com,ter";
	/** Email address used to fill the field "from" of the email. */
	protected static String FROM_EMAIL = "wingj@wingj.org"; // this email actually doesn't exist
	/** Host address (used for sending emails). */
	protected static String HOST = "localhost";
	
	/** Filename of the php script called to create the analytics data. */
	protected static String ANALYTICS_URL_CREATE = ANALYTICS_SERVER_ADDRESS + "analytics_create.php";
	/** Filename of the php script called to update the analytics data. */
	protected static String ANALYTICS_URL_UPDATE = ANALYTICS_SERVER_ADDRESS + "analytics_update.php";
	
	/** Id of the inserted analytics and given by the server (-1 if failed to insert). */
	protected int id_ = -1;
	
	/** Number of OutOfMemoryError exceptions caught during the session. */
	protected int numOutOfMemoryErrors_ = 0;
	
	/** Flag allowing to send only one server down email per session. */
	protected boolean serverDownEmailSent_ = false;
	/** Maximum number of emails sent per session. */
	protected int maxNumEmailsSent_ = 3;
	/** Current number of emails sent. */
	protected int numEmailsSent_ = 0;
	
	/** List of objects containing statistics about the systems used. */
	protected List<StructureDetectionStats> structureDetectionStats_ = null;
	/** Flag to enable/disable the recording of structure detection stats. */
	protected boolean saveStructureDetectionStats_ = true;
	
	/** List of objects containing statistics about the expression datasets generated. */
	protected List<ExpressionStats> expressionStats_ = null;
	
	// ============================================================================
	// PRIVATE METHODS
	
	/** Default constructor. */
	private Analytics() {
		
		try{
			initialize();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Initialization method. */
	private void initialize() throws Exception {

		structureDetectionStats_ = new ArrayList<StructureDetectionStats>();
		expressionStats_ = new ArrayList<ExpressionStats>();
		
		// decode the email addresses initially encoded to protect again spam
		decodeContactEmailAddresses();
		
		// instantiate a Runnable object to be executed at fixed interval
		periodicTask_= new Runnable() {
			public void run() {
				Analytics.getInstance().periodicTask();
			}
		};
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Attempts to create a new entry into the database.
	 * If the attempt is successful, id_ will be set with a number >= 0 
	 */
	protected void createAnalyticsDatabaseEntry() throws Exception {
		
		// do nothing if we don't have the right to contact the database
		if (!ALLOW_DB_CONNECTION)
			return;
	
		boolean isConnectedToInternet = isAddressReachable(INTERNET_CONNECTION_TEST_ADDRESS);
		boolean isAnalyticsServerReachable = isAddressReachable(ANALYTICS_SERVER_ADDRESS);
		
		// nothing to do if there is no internet connection
		if (!isConnectedToInternet)
			return;
		
		// sends an email to notify that the analytics server may be down
		if (isConnectedToInternet && !isAnalyticsServerReachable) {
			sendAnalyticsServerDownEmail(); // only one is sent per session
			return;
		}
			
		// attempts to create entry in the database by sending the parameters
		// using HTTP Post requests
		HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(ANALYTICS_URL_CREATE);
        HttpResponse httpResponse = null;
        
		// builds parameters
        // ip and timestamp are set in the php script
        SystemInfo sysinfo = SystemInfo.getInstance();
    	List<NameValuePair> params  = new ArrayList<NameValuePair>();
    	params.add(new BasicNameValuePair("macAddress", sysinfo.getMacAddress()));
    	params.add(new BasicNameValuePair("language", sysinfo.getLanguage()));
    	params.add(new BasicNameValuePair("osName", sysinfo.getOsName()));
    	params.add(new BasicNameValuePair("osVersion", sysinfo.getOsVersion()));
    	params.add(new BasicNameValuePair("osArch", sysinfo.getOsArch()));
    	params.add(new BasicNameValuePair("displayResolution", sysinfo.getDisplayResolution()));
    	params.add(new BasicNameValuePair("javaVersion", sysinfo.getJavaVersion()));
    	params.add(new BasicNameValuePair("sunArchDataModel", sysinfo.getSunArchDataModel()));
    	params.add(new BasicNameValuePair("maxHeapSizeInMb", String.valueOf(sysinfo.getMaxHeapSizeInMb())));
    	params.add(new BasicNameValuePair("appVersion", WJSettings.getInstance().getAppVersion()));
    	params.add(new BasicNameValuePair("ijVersion", ImageJ.VERSION));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
    
        // send data using HTTP Post requests
        httpResponse = httpClient.execute(httpPost);
        String json = EntityUtils.toString(httpResponse.getEntity());

        // deserializes JSON into AnalyticsServerResponse
        AnalyticsServerResponse response = null;
        try {
	        Gson gson = new Gson(); // or use new GsonBuilder().create();
	        try {
	        	response = gson.fromJson(json, AnalyticsServerResponse.class);
	        } catch (Exception e) {
	        	response = new AnalyticsServerResponse();
	        	response.setMessage("Unable to deserialize server response: " + json);
	        	throw e;
	        }
        } catch (Exception e) {
        	sendAnalyticsServerErrorEmail("Invalid INSERT JSON object received.", response, params);
        	return;
        }

        // if the database request failed or if the script failed (missing fields))
        if (response == null || response.getSuccess() != 1) { // response == null || 
        	sendAnalyticsServerErrorEmail("Valid INSERT JSON object received but unsuccessful request.", response, params);
        	return;
        }
        
        // here we received the success signal from the server
        // get the analytics id corresponding to the database entry inserted
        id_ = response.getId();
	}
	
	// ----------------------------------------------------------------------------
	
	/** Updates the database entry (invoked periodically). */
	protected void updateAnalyticsDatabase() throws Exception {
		
		// do nothing if we don't have the right to contact the database
		if (!ALLOW_DB_CONNECTION)
			return;
		
		// if id_ < 0 it means that the current session didn't
		// succeed yet to insert a new row into the database
		if (id_ < 0)
			return;
		
		boolean isConnectedToInternet = isAddressReachable(INTERNET_CONNECTION_TEST_ADDRESS);
		boolean isAnalyticsServerReachable = isAddressReachable(ANALYTICS_SERVER_ADDRESS);
		
		// nothing to do if there is no internet connection
		if (!isConnectedToInternet)
			return;
		
		// sends an email to notify that the analytics server may be down
		if (isConnectedToInternet && !isAnalyticsServerReachable) {
			sendAnalyticsServerDownEmail(); // only one is sent per session
			return;
		}
		
		// attempts to update entry in the database by sending the parameters
		// using HTTP Post requests
		HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(ANALYTICS_URL_UPDATE);
        HttpResponse httpResponse = null;
        
		// builds parameters
        // ip and timestamp are set in the php script
    	List<NameValuePair> params = new ArrayList<NameValuePair>();
    	params.add(new BasicNameValuePair("id", String.valueOf(id_)));
    	params.add(new BasicNameValuePair("maxMemoryUsedInMb", String.valueOf(SystemInfo.getInstance().getMaxMemoryUsedInMb())));
    	params.add(new BasicNameValuePair("numOutOfMemoryErrors", String.valueOf(numOutOfMemoryErrors_)));
    	params.add(new BasicNameValuePair("structureDetectionStats", (new Gson()).toJson(structureDetectionStats_))); // JSON structure
    	params.add(new BasicNameValuePair("expressionStats", (new Gson()).toJson(expressionStats_))); // JSON structure
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        // send data using HTTP Post requests
        httpResponse = httpClient.execute(httpPost);
        String json = EntityUtils.toString(httpResponse.getEntity());

        // deserializes JSON into AnalyticsServerResponse
        AnalyticsServerResponse response = null;
        try {
	        Gson gson = new Gson(); // or use new GsonBuilder().create();
	        try {
	        	response = gson.fromJson(json, AnalyticsServerResponse.class);
	        } catch (Exception e) {
	        	response = new AnalyticsServerResponse();
	        	response.setMessage("Unable to deserialize server response: " + json);
	        	throw e;
	        }
        } catch (Exception e) {
        	sendAnalyticsServerErrorEmail("Invalid UPDATE JSON object received.", response, params);
        	return;
        }

        // if the database request failed or if the script failed (missing fields)
        if (response.getSuccess() != 1) { // response == null || 
        	sendAnalyticsServerErrorEmail("Valid UPDATE JSON object received but unsuccessful request.", response, params);
        	return;
        }
	}
	
	// ----------------------------------------------------------------------------
	
	/** Decodes the contact email addresses. */
	private void decodeContactEmailAddresses() {
		
		// uses the delimiter ';' to get the individual emails
		ArrayList<String> emails = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(CONTACT_EMAILS, ";");
		while (st.hasMoreTokens())
			emails.add(st.nextToken());
		
		// patches each email
		CONTACT_EMAILS = "";
		for (int i = 0; i < emails.size(); i++) {
			st = new StringTokenizer(emails.get(i), ",");
			String address = st.nextToken();
			String complement = st.nextToken();
			emails.set(i, address.replace("...", complement));
			CONTACT_EMAILS += (i > 0 ? "," : "") + address.replace("...", complement); // use the delimiter ',' for InternetAddress
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Sends an email to reports that the analytics server is down.
	 * Source: http://acuriousanimal.com/blog/2011/10/08/sending-emails-with-java/ 
	 */
	private void sendAnalyticsServerDownEmail() {
		
		// if a server down email has already been sent, nothing to do
		if (serverDownEmailSent_)
			return;
		
		String content = "WingJ Analytics server may be down (" + ANALYTICS_SERVER_ADDRESS + ").";
		
		sendAnalyticsEmail(content);
		serverDownEmailSent_ = true; // don't forget
	}
	
	// ----------------------------------------------------------------------------
	
	/**
	 * Sends an email to notify that an analytics request failed.
	 * First, this function is not used to notify that the server is down.
	 * The main reasons for the method to be used are:
	 * - a database query failed
	 * - fields sent using POST are missing
	 */
	private void sendAnalyticsServerErrorEmail(String comment, AnalyticsServerResponse response, List<NameValuePair> params) {
		
		// here the limited number of emails accepted is handled in sendAnalyticsEmail()
		
		// prepare message content
	    String content = "WingJ Analytics server experienced an error (" + ANALYTICS_SERVER_ADDRESS + ").\n\n";
	    
	    // hint to help debugging
	    content += "Hint: " + comment + "\n\n";
	    
	    // show response
	    content += "Response: ";
	    if (response == null) content += "ERROR: response is null.\n";
	    else content += response.toString() + "\n";
	    
	    // show params
	    content += "Params: " + params.toString();
	    
	    sendAnalyticsEmail(content);
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/**
	 * Contact the server to update the analytics data.
	 * First, the server will be contacted to insert an analytics row
	 * into the database until this is done successfully. Then, the next calls
	 * will update the analytics data in the database.
	 */
	public void periodicTask() {
	
		try {
			if (id_ < 0) // entry not yet created in the database
				createAnalyticsDatabaseEntry();
			else
				updateAnalyticsDatabase();
				
		} catch (Exception e) {	
			sendAnalyticsEmail("ERROR: " + StringUtils.exceptionToString(e));
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Starts running the periodic task. */
	public void start() {
		
		// set up the period task
		try {
			if (executor_ == null) {
				// set up the executor to run the periodic task now and then at fixed interval
				executor_ = Executors.newSingleThreadScheduledExecutor();
				executor_.scheduleAtFixedRate(periodicTask_, 0, periodicTaskIntervalInSec_, TimeUnit.SECONDS);
			}
		} catch (Exception e) {}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Sends an email with the given content. */
	public void sendAnalyticsEmail(String content) {
		
		// if the maximum number of error emails that can be sent per session
		// has been reached, nothing to do
		if (numEmailsSent_ >= maxNumEmailsSent_)
			return;
		
		try {
			String host = HOST;
			String from = FROM_EMAIL;
			String to = CONTACT_EMAILS;
		
			// setup mail server
			Properties properties = System.getProperties();
			properties.setProperty("mail.smtp.host", host);

			// get the default Session object.
			Session session = Session.getDefaultInstance(properties);
			
		    // Instantiate a message
		    Message msg = new MimeMessage(session);
		 
		    // Set the FROM message
		    msg.setFrom(new InternetAddress(from));
		    
		    // add the recipient(s)
		    msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		 
		    // Set the message subject and date we sent it.
		    msg.setSubject("WingJ Analytics");
		 
		    // Set message content
		    msg.setText(content);
		 
		    // Send the message
		    Transport.send(msg);
		    
		    numEmailsSent_++; // don't forget
		}
		catch (MessagingException mex) {}
		catch (Exception e) {}
	}
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Gets Analyzer instance. */
	static public Analytics getInstance() {
		
		if (instance_ == null)
			instance_ = new Analytics();
		return instance_;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Returns true if the client can access the given address. */
	public boolean isAddressReachable(String address) {

		try {
			URL url = new URL(address);
			HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
			
			// trying to retrieve data from the source. If there
			// is no connection, this line will fail.
			urlConnect.getContent();
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	// ----------------------------------------------------------------------------
	
	/** Wrapper for adding the given number to the associated number of structure detections. */
	public void addStructureDetection(int type, double value) {
		
		if (!saveStructureDetectionStats_)
			return;
		
		try {
			// get selected system unique id
			// TODO: is based on the state of a combobox, this can be improved
			String systemId = WingJ.getInstance().getSelectedSystemUniqueId();
			
			// search for the corresponding stats object, otherwise create it
			StructureDetectionStats stats = null;
			for (int i = 0; i < structureDetectionStats_.size(); i++) {
				stats = structureDetectionStats_.get(i);
				if (stats.getSystemId().compareTo(systemId) == 0) {
					stats.addNumStructureDetections(type, value);
					return;
				}
			}
			
			// if here, create new stats object
			stats = new StructureDetectionStats(systemId);
			stats.addNumStructureDetections(type, value);
			structureDetectionStats_.add(stats);
		} catch (Exception e) {
			sendAnalyticsEmail("ERROR: " + StringUtils.exceptionToString(e));
		}
	}
	
	// ----------------------------------------------------------------------------
	
	/** Wrapper for adding the given number to the associated number of expression datasets. */
	public void addExpressionDataset(int type, int value) {
		
		try {
			// get selected system unique id
			// TODO: is based on the state of a combobox, this can be improved
			String systemId = WingJ.getInstance().getSelectedSystemUniqueId();
			
			// search for the corresponding stats object, otherwise create it
			ExpressionStats stats = null;
			for (int i = 0; i < expressionStats_.size(); i++) {
				stats = expressionStats_.get(i);
				if (stats.getSystemId().compareTo(systemId) == 0) {
					stats.addNumExpressionDatasets(type, value);
					return;
				}
			}
			
			// if here, create new stats object
			stats = new ExpressionStats(systemId);
			stats.addNumExpressionDatasets(type, value);
			expressionStats_.add(stats);
		} catch (Exception e) {
			sendAnalyticsEmail("ERROR: " + StringUtils.exceptionToString(e));
		}
	}
	
	// ============================================================================
	// SETTERS AND GETTERS
	
	public void saveStructureDetectionStats(boolean b) { saveStructureDetectionStats_ = b; }
	public void incrementNumOutOfMemoryErrors() { numOutOfMemoryErrors_++; }
	
	// ============================================================================
	// INNER CLASSES
	
	/** Used to deserialize the JSON response. */
	private class AnalyticsServerResponse {
		
		// DO NOT USE '_' at the end of variable names
		// This is required to deserialize JSON response using GSON
		 
		/** Returns 1 for success, otherwise 0. */
		private int success;
		/** Short message describing the operation. */
		private String message;
		/** More detailed information about the operation (e.g. complete error message). */
		private String details;
		/** Returns the id of the analytics row (-1 if analytics never succeeded to save). */
		private int id;
 
		/** Returns a string summarizing the values of the response fields. */
		@Override
		public String toString() {
	 
			String text = "";
			try { text += "Success: " + success + "\n"; }
			catch (Exception e) { text += "Success: Invalid value.\n"; }
			 
			try { text += "Message: " + message + "\n"; }
			catch (Exception e) { text += "Message: Invalid value.\n"; }
			 
			try { text += "Details: " + details + "\n"; }
			catch (Exception e) { text += "Details: Invalid value.\n"; }
			 
			try { text += "Id: " + id + "\n"; }
			catch (Exception e) { text += "Id: Invalid value.\n"; }
			 
			return text;
		}
 
		public int getSuccess() { return success; }
		@SuppressWarnings("unused")
		public void setSuccess(int success) { this.success = success; }
		 
		@SuppressWarnings("unused")
		public String getMessage() { return message; }
		public void setMessage(String message) { this.message = message; }
		 
		@SuppressWarnings("unused")
		public String getDetails() { return details; }
		@SuppressWarnings("unused")
		public void setDetails(String details) { this.details = details; }
		 
		public int getId() { return id; }
		@SuppressWarnings("unused")
		public void setId(int id) { this.id = id; }
	}
}
