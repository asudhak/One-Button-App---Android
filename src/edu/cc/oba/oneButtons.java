package edu.cc.oba;

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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class oneButtons extends Activity {
	public static TabHost mTabHost;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.onebutton);

		// List view of all one buttons (Only Name)
		// get active requests for the user (For now)
		updateListView();

		updateHistory();

		mainUITabs.mTabHost
				.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

					@Override
					public void onTabChanged(String tabId) {
						// TODO Auto-generated method stub
						if (tabId.compareTo(("One Buttons")) == 0) {

							updateListView();
						}

					}

				});

		// On Click of Listview, get putty/rdp

		// Show user active and inactive one buttons

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setCancelable(true);

			adb.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					// CODE TO EXTEND RESERVATION
					finish();
				}
			});

			adb.setMessage("Do you Want to Exit");
			adb.show();

			adb.setNegativeButton("No", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub

				}

			});
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateListView() {
		ArrayList<Map<String, String>> list = build();

		/* here we add the item into the history database! */
		ContentValues values = new ContentValues();
		ContentResolver cr = getContentResolver();
		Cursor c = cr.query(ImageDB.CONTENT_URI, null, null, null, null);
		boolean dup = false;

		String date = (String) android.text.format.DateFormat.format(
				"yyyy-MM-dd", new java.util.Date());

		for (int i = 0; i < list.size(); i++) {
			c = cr.query(ImageDB.CONTENT_URI, null, null, null, null);
			dup = false;

			if (c.moveToFirst()) {
				do {
					String id = c.getString(c
							.getColumnIndex(ImageDB.KEY_REQUESTID));

					if (list.get(i).get("requestid").toString().equals(id) == true) {
						// the item is duplicated
						dup = true;
						System.out.println("We find the duplicated item " + i);
						break;
					}

				} while (c.moveToNext());

				if (dup == false) {// if it is not dumplicated, insert the item.
					values.clear();
					values.put(ImageDB.KEY_IMAGENAME,
							list.get(i).get("imagename").toString());
					values.put(ImageDB.KEY_REQUESTID,
							list.get(i).get("requestid").toString());
					values.put(ImageDB.KEY_STATUS, date);
					getContentResolver().insert(ImageDB.CONTENT_URI, values);
					System.out.println("Success Insert! " + values.toString());
				} // if
			}// if
			else { // the database is empty
				values.clear();
				values.put(ImageDB.KEY_IMAGENAME, list.get(i).get("imagename")
						.toString());
				values.put(ImageDB.KEY_REQUESTID, list.get(i).get("requestid")
						.toString());
				values.put(ImageDB.KEY_STATUS, date);
				getContentResolver().insert(ImageDB.CONTENT_URI, values);
				System.out.println("Success Insert! " + values.toString());
			}
		}

		ArrayList<Map<String, String>> list_active = new ArrayList<Map<String, String>>();
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).get("status").toString().equals("loading")
					|| list.get(i).get("status").toString().equals("ready")) {
				boolean add = list_active.add(list.get(i));
				System.out.println("Active_list adding " + add);
			} else
				System.out.println("It is not an active item!");
		}

		if (list_active.size() > 0) {
			ListView listView = (ListView) findViewById(R.id.listView);
			String[] actitems = new String[list_active.size()];
			for (int i = 0; i < list_active.size(); i++) {
				System.out.println("The imgInfo:"
						+ list_active.get(i).toString());
				String[] splitInfo = list_active.get(i).toString().split(",");
				String imgInforeqid = "", imgInfostatus = "", imgInfoname = "";
				for (int j = 0; j < splitInfo.length; j++) {
					if (splitInfo[j].contains("requestid")) {
						imgInforeqid = splitInfo[j];
					}
					if (splitInfo[j].contains("status")) {
						imgInfostatus = splitInfo[j];
					}
					if (splitInfo[j].contains("imagename")) {
						imgInfoname = splitInfo[j];
					}
				}

				int start = (imgInforeqid).indexOf("requestid=");
				String reqID = imgInforeqid.substring(start + 10);
				start = (imgInfostatus).indexOf("status=");
				String Status = imgInfostatus.substring(start + 7);
				start = (imgInfoname).indexOf("imagename=");
				String imgName = imgInfoname.substring(start + 10);
				reqID = "ID=".concat(reqID).concat(", ");
				Status = "STS=".concat(Status).concat(", ");
				imgName = "IMG=".concat(imgName);
				actitems[i] = reqID.concat(Status).concat(imgName);
				// System.out.println("the splitInfo is: " + splitInfo[0] + " "
				// + splitInfo[1] + " " + splitInfo[2] + " " + splitInfo[3] +
				// " " + splitInfo[4] + " " + splitInfo[5]);
			}
			SpecialAdapter adapter = new SpecialAdapter(this, actitems);
			listView.setAdapter(adapter);
			listView.setTextFilterEnabled(true);
		}
	}

	private void updateHistory() {
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(ImageDB.CONTENT_URI, null, null, null, null);

		String[] from = { ImageDB.KEY_IMAGENAME, ImageDB.KEY_STATUS };
		int[] to = { R.id.hisimageName, R.id.hisimageTime };
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.hislistrow, cursor, from, to);

		final ListView listView = (ListView) findViewById(R.id.listView_inactive);
		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<Map<String, String>> build() {
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
			list.add(obaImagesHash);

		}

		return list;
	}

	static class ViewHolder {
		TextView text;
	}

	private class SpecialAdapter extends BaseAdapter {
		// Defining the background color of rows. The row will alternate between
		// green light and green dark.
		private int[] colors = new int[] { 0xAAf6ffc8, 0xAA538d00 };
		private LayoutInflater mInflater;

		// The variable that will hold our text data to be tied to list.
		private String[] data;

		public SpecialAdapter(Context context, String[] items) {
			mInflater = LayoutInflater.from(context);
			this.data = items;
		}

		@Override
		public int getCount() {
			return data.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		// A view to hold each row in the list
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.listviewrow, null);

				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.headline);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// Bind the data efficiently with the holder.
			// System.out.println("The holder is: " + holder.toString());
			holder.text.setText(data[position]);

			// Set the background color depending of odd/even colorPos result
			int colorPos = position % colors.length;
			convertView.setBackgroundColor(colors[colorPos]);

			return convertView;
		}
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
			if (conn_data_secure[2]
					.equalsIgnoreCase(Android_OBAActivity.settings.getString(
							"pass", ""))) {
				tickerText = "Use your Campus Password";
			}

			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon, tickerText, when);

			CharSequence contentTitle = "Password for this reservation is";

			CharSequence contentText = conn_data_secure[2];
			// If password and stored password are the same
			if (conn_data_secure[2]
					.equalsIgnoreCase(Android_OBAActivity.settings.getString(
							"pass", ""))) {
				contentText = "Use your Campus Password";
			}

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
				AlertDialog.Builder builder_install = new AlertDialog.Builder(
						this);
				builder_install
						.setMessage(
								"This action requires ConnectBot to be installed. Do you want to Install connectBot from the market >")
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										Intent goToMarket = new Intent(
												Intent.ACTION_VIEW).setData(Uri
												.parse("market://details?id=org.connectbot"));
										startActivity(goToMarket);

									}
								})
						.setCancelable(false)
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert_install = builder_install.create();
				alert_install.show();

			}

			notificationManager.notify(1, notification);
			// notificationManager.cancelAll();
			ConnectWithPass.conn_do(conn_data_secure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loginToPlatform(View v) {
		System.out.println("HELLO, THIS IS HAIQING");

		String text = ((TextView) v.findViewById(R.id.headline)).getText()
				.toString();
		System.out.println("The textInfo is: " + text);
		// // final ListView listView = (ListView) findViewById(R.id.listView);
		// //
		// // listView.setOnItemClickListener(new OnItemClickListener() {
		// // public void onItemClick(AdapterView<?> arg0, View v, int position,
		// // long id) {
		// // Toast.makeText(getBaseContext(),
		// //
		// ((TextView)v.findViewById(R.id.lngValue)).getText().toString(),4).show();

		int start = text.indexOf("ID=");
		int end = text.indexOf("STS=");
		int req_id = Integer.parseInt(text.substring(start + 3, end - 2));

		start = text.indexOf("STS=");
		end = text.indexOf("IMG=");
		String status = text.substring(start + 4, end - 2);

		start = text.indexOf("IMG=");
		end = text.indexOf("}");
		String imagename = text.substring(start + 4, end);

		// System.out.println("ID: " + req_id + " STS: " + status + " IMG: " +
		// imagename);

		if (status.compareToIgnoreCase("ready") == 0) {
			String[] conn_data = TestOBA.oba.getConnectData(req_id);

			Toast.makeText(getBaseContext(), conn_data.toString(),
					Toast.LENGTH_LONG);
			Log.i("COnnDATA", conn_data.toString());

			if (imagename.contains("Win"))
				StartRdpIntent(conn_data);
			else
				conn_do_ssh(conn_data, imagename);
		} else if (status.compareToIgnoreCase("loading") == 0) {
			Toast.makeText(getBaseContext(),
					"Please wait till your reservation is ready",
					Toast.LENGTH_LONG).show();
		} else if (status.compareToIgnoreCase("timedout") == 0) {
			Toast.makeText(getBaseContext(), "Your reservation has timed out",
					Toast.LENGTH_LONG).show();
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
			AlertDialog.Builder builder_install = new AlertDialog.Builder(this);
			builder_install
					.setMessage(
							"This action requires ConnectBot to be installed. Do you want to Install connectBot from the market >")
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent goToMarket = new Intent(
											Intent.ACTION_VIEW).setData(Uri
											.parse("market://details?id=org.connectbot"));
									startActivity(goToMarket);

								}
							})
					.setCancelable(false)
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert_install = builder_install.create();
			alert_install.show();

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
