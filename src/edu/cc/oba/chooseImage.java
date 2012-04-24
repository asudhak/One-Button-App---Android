package edu.cc.oba;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class chooseImage extends Activity {
	/** Called when the activity is first created. */
	public int image_id = 0;
	public String[] conn_data;
	public static Boolean flag = false;

	ProgressThread progThread;
	ProgressDialog progDialog;
	Button processBar;
	int typeBar; // Determines type progress bar: 0 = spinner, 1 = horizontal
	int delay = 1000; // Milliseconds of delay in the update loop (1 second)
	int maxBarValue = 100; // Maximum value of horizontal progress bar
	long initTime = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chooseimage);

		Intent this_intent = getIntent();
		String username = this_intent.getStringExtra("username");
		String password = this_intent.getStringExtra("password");

		// TestOBA.getUserObject(username, password);

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// GET IMAGE LIST FOR USER
		Object[] images = TestOBA.oba.getImagesID();

		for (int i = 0; i < images.length; i++) {
			adapter.add(((HashMap) images[i]).toString());
		}

		final Spinner s = (Spinner) findViewById(R.id.image_spinner);

		s.setAdapter(adapter);

		Log.d("Selected ITEM", s.getSelectedItem().toString());

		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				String[] items = s.getSelectedItem().toString().split(",");
				image_id = Integer.parseInt(items[0].substring(4));
				Log.d("ID", "" + image_id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				image_id = 1908;

			}

		});

		processBar = (Button) findViewById(R.id.ProBar);
		processBar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				typeBar = 1;
				maxBarValue = maxBarValue
						- (int) ((System.currentTimeMillis() - initTime) / 1000);
				if (maxBarValue < 0) {
					maxBarValue = 0;
				}
				showDialog(typeBar);
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

	public void image_Reserve(View v) {

		// makeReservation reserveTask=TestOBA.oba.new makeReservation();
		Log.d("Trying to make reservation for ", "" + image_id);
		Toast.makeText(getBaseContext(), "Your One Button will be created", 3);
		TestOBA.oba.makeReservation(image_id);

		// Here we can only handle process bar for one request at one time. The
		// concurrent process bars can be as TODO work.
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

		System.out.println("The requestInfo is: " + list.toString());

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).toString().contains("time=")) {
				int start = list.get(i).toString().indexOf("time=");
				int end = list.get(i).toString().indexOf("requestid");
				String loadingTime = list.get(i).toString()
						.substring(start + 5, end - 2);
				initTime = System.currentTimeMillis();

				maxBarValue = (Integer.parseInt(loadingTime) * 60 - (int) ((System
						.currentTimeMillis() - initTime) / 1000)); // seconds
				System.out.println("The loadingTime is: " + maxBarValue
						+ " and the time=" + loadingTime);
			}
		}

	}

	// Method to create a progress bar dialog of either spinner or horizontal
	// type
	@Override
	protected Dialog onCreateDialog(int typeBar) {

		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progDialog.setMax(maxBarValue);
		progDialog.setMessage("On Processing ...");
		progThread = new ProgressThread(handler);
		progThread.start();
		return progDialog;
	}

	// Handler on the main (UI) thread that will receive messages from the
	// second thread and update the progress.

	final Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// Get the current value of the variable total from the message data
			// and update the progress bar.
			int total = msg.getData().getInt("total");
			progDialog.setProgress(total);
			if (total <= 0) {
				dismissDialog(typeBar);
				progThread.setState(ProgressThread.DONE);
			}
		}
	};

	// Inner class that performs progress calculations on a second thread.
	// Implement
	// the thread by subclassing Thread and overriding its run() method. Also
	// provide
	// a setState(state) method to stop the thread gracefully.

	private class ProgressThread extends Thread {

		// Class constants defining state of the thread
		final static int DONE = 0;
		final static int RUNNING = 1;

		Handler mHandler;
		int mState;
		int total;

		// Constructor with an argument that specifies Handler on main thread
		// to which messages will be sent by this thread.

		ProgressThread(Handler h) {
			mHandler = h;
		}

		// Override the run() method that will be invoked automatically when
		// the Thread starts. Do the work required to update the progress bar on
		// this
		// thread but send a message to the Handler on the main UI thread to
		// actually
		// change the visual representation of the progress. In this example we
		// count
		// the index total down to zero, so the horizontal progress bar will
		// start full and
		// count down.

		@Override
		public void run() {
			mState = RUNNING;
			total = maxBarValue;
			while (mState == RUNNING) {
				// The method Thread.sleep throws an InterruptedException if
				// Thread.interrupt()
				// were to be issued while thread is sleeping; the exception
				// must be caught.
				try {
					// Control speed of update (but precision of delay not
					// guaranteed)
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					Log.e("ERROR", "Thread was Interrupted");
				}

				// Send message (with current value of total as data) to Handler
				// on UI thread
				// so that it can update the progress bar.

				Message msg = mHandler.obtainMessage();
				Bundle b = new Bundle();
				b.putInt("total", total);
				msg.setData(b);
				mHandler.sendMessage(msg);

				total--; // Count down
			}
		}

		// Set current state of thread (use state=ProgressThread.DONE to stop
		// thread)
		public void setState(int state) {
			mState = state;
		}
	}

}