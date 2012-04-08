package edu.cc.oba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

//import static org.developerworks.android.BaseFeedParser.*;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.xmlrpc.android.*;


public class TestOBA extends Activity {
	public static TestOBA oba;
	private String username;
	
	private String password;

	public ArrayList<Integer> activeRequests;
	public static HashMap obaImagesHash;

	public XMLRPCClient client;
	//here

	public TestOBA(final String username, final String password) {
		this.username = username;
		this.password = password;
		this.activeRequests = new ArrayList<Integer>();
		
//		this.context=getBaseContext();
				
		this.client = new XMLRPCClient("https://vcl.ncsu.edu/scheduling/index.php?mode=xmlrpccall",username, password);
			}
//		//here
	
	
	private Object xmlRPCcall(String op_name, Object[] params) {
		Object result = null;

		try {
			result = client.callEx(op_name, params);

			if (result instanceof HashMap) {
				// do nothing
				return (HashMap) result;
			} else {
				Object[] result_array = (Object[]) result;
//				for (int i = 0; i < result_array.length; i++)
					//System.out.println(result_array[i]);
				return result_array;
			}

		} catch (XMLRPCException e) {
//			Log.d("REASON",e.getCause().toString());
			e.printStackTrace();
			Log.i("ERROR", e.getMessage());
		Toast.makeText(getBaseContext(), "Login Failed", 4).show();
		finish();
			
			
		}

		return result;
	}

	 private HashMap parseResult(Object[] result) {
	 HashMap<String, Object> resultHash = new HashMap<String, Object>();
	
	 for(Object each_obj : result) {
	 Object[] each_obj_array = (Object[])each_obj;
	 resultHash.put((String)each_obj_array[0], each_obj_array[1]);
	 }
	
	 return resultHash;
	 }

	public Object[] getImagesID() {
		String[] params = null;
		return (Object[]) xmlRPCcall("XMLRPCgetImages", params);
		
	}
	
	
	
	
	
	
	public void makeReservation(int image_id)
	{
		
		String[] params = new String[3];
//		int image_id = 2422; // VCL2.2.1 SandBox image id 
		

		params[0] = Integer.toString(image_id);
		params[1] = "now";
		params[2] = Integer.toString(60); // Resever for one hour

		
		//here
		HashMap result = (HashMap) xmlRPCcall("XMLRPCaddRequest", params);

			
		Log.d("Hash Table",result.toString());
		
				
		
		}
		
		
	public HashMap getRequestStatus(int request_id) {
		Object[] params = new Object[1];
		params[0] = request_id;
		return (HashMap) xmlRPCcall("XMLRPCgetRequestStatus", params);
	}
	
	public HashMap getRequestIDs() {
		Object[] params = new Object[1];
		params[0] = "";
		return (HashMap) xmlRPCcall("XMLRPCgetRequestIds", params);
	}
	
	

	public Boolean cancelReservation() {
		if (!this.activeRequests.isEmpty()) {
			int request_id = this.activeRequests.get(0);
			Object[] params = new Object[1];
			params[0] = request_id;
			HashMap result = (HashMap) xmlRPCcall("XMLRPCendRequest", params);

			if (result.get("status").equals("success")) {
				System.out
						.println("End reservation, request id: " + request_id);
				this.activeRequests.remove(0);
				return true;
			} else {
				System.err.println("Fail to end reservation, request id: "
						+ request_id);
				return false;
			}
		}
		return false;
	}
	
	public Boolean cancelReservation(int request_id) {


			Object[] params = new Object[1];
			params[0] = request_id;
			HashMap result = (HashMap) xmlRPCcall("XMLRPCendRequest", params);

			if (result.get("status").equals("success")) {
				System.out
						.println("End reservation, request id: " + request_id);
				this.activeRequests.remove(0);
				return true;
			} else {
				System.err.println("Fail to end reservation, request id: "
						+ request_id);
				return false;
			}
	}
	
	

	public String[] getConnectData() {
		InetAddress addr;
		String ipAddr="";

		
	        try {
	            URL autoIP = new URL("http://api.externalip.net/ip/");
	            BufferedReader in = new BufferedReader( new InputStreamReader(autoIP.openStream()));
	            ipAddr = (in.readLine()).trim();
	            
	         }catch (Exception e){
		    	e.printStackTrace();
	            
		    }
	    
		

		Object[] params = { this.activeRequests.get(0), ipAddr };
		// Object[] params = { 1744326, ipAddr };

		HashMap result = (HashMap) xmlRPCcall("XMLRPCgetRequestConnectData",
				params);

		String[] conn_data = null;
		if (result.get("status").equals("ready")) {
			conn_data = new String[3];
			conn_data[0] = (String) result.get("serverIP");
			conn_data[1] = (String) result.get("user");
			conn_data[2] = (String) result.get("password");

			System.out.println("Reserved IP: " + conn_data[0]);
			Log.d("Reserved IP: ", conn_data[0] );
			System.out.println("Username: " + conn_data[1]);
			Log.d("Username: ", conn_data[1] );
			if (conn_data[2].equals("")) {
				conn_data[2] = this.password;//Security Issue
				System.out.println("Password: (use your campus password)");
			} else {
				System.out.println("Password: " + conn_data[2]);
				Log.d("Password: ", conn_data[2] );
			}
		} else if (result.get("status").equals("notready")) {
			System.err.println("The reservation is not ready.");
		} else {
			System.err.println("Fail to get the connect data.");
		}

		return conn_data;
	}

	//Overridden GetConnectData ( Possible delete of previous)
	public String[] getConnectData(int req_id) {
		InetAddress addr;
		String ipAddr;

		try {
			addr = InetAddress.getLocalHost();
			// Get IP Address
			ipAddr = addr.getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		Object[] params = { req_id, ipAddr };
		// Object[] params = { 1744326, ipAddr };

		HashMap result = (HashMap) xmlRPCcall("XMLRPCgetRequestConnectData",
				params);

		String[] conn_data = null;
		if (result.get("status").equals("ready")) {
			conn_data = new String[3];
			conn_data[0] = (String) result.get("serverIP");
			conn_data[1] = (String) result.get("user");
			conn_data[2] = (String) result.get("password");

			System.out.println("Reserved IP: " + conn_data[0]);
			Log.d("Reserved IP: ", conn_data[0] );
			System.out.println("Username: " + conn_data[1]);
			Log.d("Username: ", conn_data[1] );
			if (conn_data[2].equals("")) {
				conn_data[2] = this.password;//Security Issue
				System.out.println("Password: (use your campus password)");
			} else {
				System.out.println("Password: " + conn_data[2]);
				Log.d("Password: ", conn_data[2] );
			}
		} else if (result.get("status").equals("notready")) {
			System.err.println("The reservation is not ready.");
		} else {
			System.err.println("Fail to get the connect data.");
		}

		return conn_data;
	}

	
	private void termLaunch(String[] conn_data) {
		String term = "/usr/bin/gnome-terminal";
		// In order to automatic use ssh to login, we need "sshpass" to provide
		// the password to the shell
		String ssh_command = "sshpass -p " + conn_data[2]
				+ " ssh -o StrictHostKeyChecking=no " + conn_data[1] + "@"
				+ conn_data[0];
		System.out.println(ssh_command);

		// Using string array is due to the requirement of the argument accepted
		// by rt.exec.
		String[] commands = new String[] { term, "-e", ssh_command};
		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Now launch the terminal");
			rt.exec(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getUserObject(String username, String password)
	{
		oba=new TestOBA(username, password);
		HashMap requests=oba.getRequestIDs();
		
		Object[] req_id=(Object[]) requests.get("requests");
		   for (Object object : req_id) {
	        
	    	obaImagesHash = (HashMap)object;
	        System.out.println("Adding to array: "+obaImagesHash.get("requestid").toString());
	        oba.activeRequests.add(Integer.parseInt(obaImagesHash.get("requestid").toString()));

	       	    }
	    
		Log.d("Requests for this user ", requests.toString());
		Log.d("Requests for this user ", oba.activeRequests.toString());
	}


}