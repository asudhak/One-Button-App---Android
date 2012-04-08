package edu.cc.oba;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class oneButtons extends Activity {
	public static TabHost mTabHost;
	final String imageStatus = null;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.onebutton);

		// One Buttons

		// List view of all one buttons (Only Name)
		// get active requests for the user (For now) and inactive list.
		updateListViewActive();
		updateListViewInactive();

		final ListView listView = (ListView) findViewById(R.id.listView);

		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// Toast.makeText(getBaseContext(),
				// ((TextView)v.findViewById(R.id.lngValue)).getText().toString(),4).show();

				int req_id = Integer.parseInt(((TextView) v
						.findViewById(R.id.imageID)).getText().toString());
				String imagename = ((TextView) v.findViewById(R.id.imageName))
						.getText().toString();
				String status = ((TextView) v.findViewById(R.id.imageStatus))
						.getText().toString();

				if (status.compareToIgnoreCase("ready") == 0) {
					String[] conn_data = TestOBA.oba.getConnectData(req_id);

					Toast.makeText(getBaseContext(), conn_data.toString(),
							Toast.LENGTH_LONG);
					Log.i("COnnDATA", conn_data.toString());
					
					 if(isHostRDPReady(conn_data[0]))
						StartRdpIntent(conn_data);
					else
						conn_do_ssh(conn_data, imagename); // On Click of
															// Listview, get
															// putty/rdp
				} else if (status.compareToIgnoreCase("loading") == 0) {
					Toast.makeText(getBaseContext(),
							"Please wait till your reservation is ready",
							Toast.LENGTH_LONG).show();
				} else if (status.compareToIgnoreCase("timedout") == 0) {
					Toast.makeText(getBaseContext(),
							"Your reservation has timed out", Toast.LENGTH_LONG)
							.show();
					updateListViewActive();
					updateListViewInactive(); // at the same time, we need to
												// add it into the inactive
												// list and update the active list.
				}

			}

		});
		
		final ListView listViewInactive = (ListView) findViewById(R.id.listView_inactive);
		
		listViewInactive.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// Toast.makeText(getBaseContext(),
				// ((TextView)v.findViewById(R.id.lngValue)).getText().toString(),4).show();

				final int request_id = Integer.parseInt(((TextView) v
						.findViewById(R.id.listView_inactive)).getText()
						.toString());
				
				AlertDialog adb = new AlertDialog.Builder(getBaseContext()).create();
				adb.setCancelable(true);
				adb.setButton("ACTIVATE",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								// CODE TO EXTEND RESERVATION

							}
						});
			}

		});

		mainUITabs.mTabHost
				.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

					@Override
					public void onTabChanged(String tabId) {
						// TODO Auto-generated method stub
						if (tabId.compareTo(("One Buttons")) == 0) {
							updateListViewActive();
							updateListViewInactive();
						}
					}
				});
		// Show user active and inactive one buttons
	}

	private void updateListViewActive() {
		ArrayList<Map<String, String>> list = build(1);
		
		String[] from = { "imagename", "requestid", "status" };
		int[] to = { R.id.imageName, R.id.imageID, R.id.imageStatus };

		System.out.println(list.toString());

		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.row,
				from, to);

		final ListView listView = (ListView) findViewById(R.id.listView);

		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);

	}

	private void updateListViewInactive() {
		ArrayList<Map<String, String>> list = build(0);

		String[] from = { "imagename", "requestid", "status" };
		int[] to = { R.id.imageName, R.id.imageID, R.id.imageStatus };

		System.out.println(list.toString());

		SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.row,
				from, to);

		final ListView listView = (ListView) findViewById(R.id.listView_inactive);

		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<Map<String, String>> build(int act) {
		ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
		HashMap requests = TestOBA.oba.getRequestIDs();
		Object[] req_id = (Object[]) requests.get("requests");

		for (Object object : req_id) {

			Map obaImagesHash = (Map) object;
			HashMap req_status = TestOBA.oba.getRequestStatus(Integer
					.parseInt((String) obaImagesHash.get("requestid")));
			obaImagesHash.putAll(req_status);
			System.out.println("Adding to array: "
					+ obaImagesHash.get("requestid").toString());
			if (act == 0){
				if (obaImagesHash.get("status").toString().compareToIgnoreCase("timedout") == 0)
					list.add(obaImagesHash);
			}
			else{
				if (obaImagesHash.get("status").toString().compareToIgnoreCase("ready") == 0 || obaImagesHash.get("status").toString().compareToIgnoreCase("loading") == 0)
					list.add(obaImagesHash);
			}
		}

		return list;
	}

	
	/**
	 * Determine if the host is ready for an RDP connection
	 * @param ipAddress
	 * @return
	 */
	public static boolean isHostRDPReady(String ipAddress){
		try {
			@SuppressWarnings("unused")
			Socket socket = new Socket(ipAddress, 3389);
		}catch(Exception e) {
			return false;
		}
		
		return true;
	}
	static int i;

	public void conn_do_ssh(String[] conn_data_secure, String imagename) {

		Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_LONG);
		i++;
		try {
			String Conn_URI = "ssh://" + conn_data_secure[1] + "@"
					+ conn_data_secure[0] + ":22/#" + imagename;

			Log.d("conn_string", Conn_URI);

			// Show password in notification bar
			Context context = this.getApplicationContext();
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(NOTIFICATION_SERVICE);

			int icon = R.drawable.ic_launcher;
			CharSequence tickerText = conn_data_secure[2];
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			CharSequence contentTitle = "Password for this reservation is";
			CharSequence contentText = conn_data_secure[2];
			Intent notificationIntent = new Intent(this, oneButtons.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			// Start Connect BOT
			// TODO enclose with try catch and handle catch prompting user to
			// install connectBOT
			try {
				Intent intent = new Intent("android.intent.action.VIEW",
						Uri.parse(Conn_URI));
				startActivity(intent);
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText(getBaseContext(), "Please install connectBot",
						Toast.LENGTH_LONG).show();
			}

			notificationManager.notify(1, notification);
			// notificationManager.cancelAll();
			ConnectWithPass.conn_do(conn_data_secure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// CODE TO CONNECT RDP:
	public static final String REMOTE_SERVER = "server";
	public static final String REMOTE_PORT = "port";
	public static final String REMOTE_DOMAIN = "domain";
	public static final String REMOTE_USER = "user";
	public static final String REMOTE_PASSWORD = "password";
	public static final String REMOTE_WIDTH = "width";
	public static final String REMOTE_HEIGHT = "height";
	public static final String REMOTE_OPTIONS = "options";
	public static final String REMOTE_PROGRAM = "program";
	public static final String REMOTE_COLOR = "color";
	public static final String REMOTE_AS_HOST = "as_host"; // the client
															// computer name

	// option values for REMOTE_OPTIONS
	public static final int OPTION_CONSOLE = 0x00000001;// connet to console
														// session
	public static final int OPTION_CLIPBOARD = 0x00000002;// clipbaord
															// copy/paste
	public static final int OPTION_SDCARD = 0x00000004; // mount sdcard
	public static final int OPTION_SOUND = 0x00000008; // audio playback
	public static final int OPTION_LEAVE_SOUND = 0x00000010; // leave sound in
																// remote
																// computer
	public static final int OPTION_BEST_EFFECT = 0x00000020;// best display
															// effect
	public static final int OPTION_RECORD_AUDIO = 0x00000040;// audio recording

	public static String LOG = "RDP_TEST";

	public static String walter_rdp_uri = "com.toremote.serversmanager";

	public static String HOST = "152.7.99.198";

	public static String USER = "asudhak";

	public static String PORT = "3389";

	public static String PWD = "nkAGCW";

	public static String DOMAIN = "";

	public void StartRdpIntent(String[] conn_data) {
		Log.v(LOG, "sending an intent to walter App");
		Intent intent = new Intent(Intent.ACTION_VIEW);
		// Uri m_uri = Uri.parse(walter_rdp_uri);
		// for enterprise version
		// intent.setComponent(new ComponentName("com.toremote.serversmanager",
		// "com.toremote.RemoteActivity"));
		// for standard version
		// intent.setComponent(new ComponentName("org.toremote.serversmanager",
		// "com.toremote.RemoteActivity"));
		// for lite version
		try {
			intent.setComponent(new ComponentName("org.toremote.rdpdemo",
					"com.toremote.RemoteActivity"));
		} catch (ActivityNotFoundException e) {
			Toast.makeText(
					getBaseContext(),
					"Please install REMOTE RDP from the Android Market to be able to remotely connect to this Reservation",
					Toast.LENGTH_LONG).show();
		}
		
		
		intent.putExtra(REMOTE_SERVER, conn_data[0]);
		intent.putExtra(REMOTE_PORT, PORT);
		// following are optional:
		intent.putExtra(REMOTE_USER, conn_data[1]);
		intent.putExtra(REMOTE_PASSWORD, conn_data[2]);
		intent.putExtra(REMOTE_DOMAIN, DOMAIN);
		intent.putExtra(REMOTE_WIDTH, "0");// "0" means "fit Device screen"
		intent.putExtra(REMOTE_HEIGHT, "0");// "0" means "fit Device screen"
		intent.putExtra(REMOTE_COLOR, 16);// 16 bit color
		intent.putExtra(REMOTE_OPTIONS, OPTION_CLIPBOARD | OPTION_SOUND
				| OPTION_SDCARD | OPTION_RECORD_AUDIO | OPTION_CONSOLE
				| OPTION_BEST_EFFECT);
		startActivity(intent);
	}

	// END CODE TO CONNECT RDP:

}
