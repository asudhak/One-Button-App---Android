package edu.cc.oba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class createConnection extends Activity {
	/** Called when the activity is first created. */
	public static createConnection connect;

	// public TestOBA oba;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createconnection);

		Intent this_intent = getIntent();
		String username = this_intent.getStringExtra("username");
		String password = this_intent.getStringExtra("password");

		final TextView conn_status = (TextView) this
				.findViewById(R.id.conn_status);
		conn_status.setText("Current Reservations");
		Boolean flag = false;
		doCheckUpdates();

		// GET ALL REQUEST IDs:
		ProgressDialog pd = new ProgressDialog(this);
		this.pd = ProgressDialog.show(this, "Working..", "Downloading Data...",
				true, false);
		updateListView();
		final ListView listView = (ListView) findViewById(R.id.tab3_listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				// Toast.makeText(getBaseContext(),
				// ((TextView)v.findViewById(R.id.lngValue)).getText().toString(),4).show();

				final int request_id = Integer.parseInt(((TextView) v
						.findViewById(R.id.tab3_hidden_req)).getText()
						.toString());

				// Toast.makeText(getBaseContext(),
				// arg0.findViewById(R.id.lngValue).getResources().toString(),4).show();

				AlertDialog.Builder adb = new AlertDialog.Builder(
						createConnection.this);
				adb.setCancelable(true);

				adb.setNeutralButton("Extend",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								// CODE TO EXTEND RESERVATION

							}
						});

				adb.setMessage("Select an Option");

				adb.setPositiveButton("Delete Reservation",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								// DELETE RESERVATION CODE
								TestOBA.oba.cancelReservation(request_id);
								updateListView();

							}

						});

				adb.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub

							}
						});

				adb.show();
			}

		});

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

	public void updateListView() {

		String[] from = { "imagename", "status", "time", "requestid" };
		int[] to = { R.id.tab3_imageName, R.id.tab3_status, R.id.tab3_time,
				R.id.tab3_hidden_req };

		ArrayList list = getUserRequests();

		System.out.println(list.toString());

		SimpleAdapter adapter = new SimpleAdapter(this, list,
				R.layout.row_status, from, to);

		final ListView listView = (ListView) findViewById(R.id.tab3_listView);

		listView.setAdapter(adapter);
		listView.setTextFilterEnabled(true);
	}

	public ArrayList getUserRequests() {
		Object[] req_id = (Object[]) TestOBA.oba.getRequestIDs()
				.get("requests");

		ArrayList list = new ArrayList();
		System.out.println("Started");

		for (Object object : req_id) {

			HashMap obaImagesHash = (HashMap) object;
			System.out.println("Adding to array: "
					+ obaImagesHash.get("requestid").toString());
			System.out.println(obaImagesHash.get("imagename"));
			HashMap req_status = TestOBA.oba.getRequestStatus(Integer
					.parseInt((String) obaImagesHash.get("requestid")));
			obaImagesHash.putAll(req_status);
			System.out.println(req_status.get("status"));
			list.add(obaImagesHash);
		}

		System.out.println(list.toString());
		this.pd.dismiss();
		return list;
	}

	private String[] conn_data_secure;

	public void conn_cancel(View V) {
		Toast.makeText(getBaseContext(), "Cancelling", Toast.LENGTH_LONG)
				.show();
		if (TestOBA.oba.cancelReservation()) {
			finish();
		}

	}

	private ProgressDialog pd = null;

	public void refresh(View v) {
		this.pd = ProgressDialog.show(this, "Working..", "Downloading Data...",
				true, false);

		updateListView();

	}

	public void conn_do(View v) {
		Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_LONG);
		try {
			String Conn_URI = "ssh://" + conn_data_secure[1] + "@"
					+ conn_data_secure[0] + ":22/#adith";

			Log.d("conn_string", Conn_URI);

			Intent intent = new Intent("android.intent.action.VIEW",
					Uri.parse(Conn_URI));
			startActivity(intent);

			ConnectWithPass.conn_do(conn_data_secure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	TimerTask scanTask;
	final Handler handler = new Handler();
	Timer t = new Timer();

	public void doCheckUpdates() {

		scanTask = new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(), "List Updated", 4)
								.show();
						updateListView();
						Log.d("TIMER", "Timer set off");
					}
				});
			}
		};

		t.schedule(scanTask, 300, 20000);

	}

}