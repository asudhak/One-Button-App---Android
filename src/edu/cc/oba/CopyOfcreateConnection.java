package edu.cc.oba;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class CopyOfcreateConnection extends Activity {
    /** Called when the activity is first created. */
	
//	public TestOBA oba;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createconnection);
        
        
        Intent this_intent= getIntent();
        String username=this_intent.getStringExtra("username");
        String password=this_intent.getStringExtra("password");
        
        final TextView conn_status= (TextView) this.findViewById(R.id.conn_status);
        conn_status.setText("Current Reservations");
        Boolean flag=false;
        
        
//        oba = new TestOBA(username,password);
		String TAG="createConnection";
//		 oba.getImageID();		
		
		//GET reservation status for 1st reservation:
		
		while(true){
			
			if(TestOBA.oba.activeRequests.size()==0){
				Toast.makeText(getBaseContext(), "No reservations", 4).show();
				break;
			}
			
			else if (TestOBA.oba.getRequestStatus(TestOBA.oba.activeRequests.get(0)).get("status").equals("ready")) {
			Log.i(TAG,"Connected");
			flag=true;
			break;
		}
		else if(TestOBA.oba.getRequestStatus(TestOBA.oba.activeRequests.get(0)).get("status").equals("ready"))
		{
		Toast.makeText(getBaseContext(), "connectign", 4).show();	
		
		}
		else break;
		}
		
		if(flag){
			String[] conn_data = TestOBA.oba.getConnectData();
			
			final TextView conn_ip_addr= (TextView) this.findViewById(R.id.conn_ip_addr);
			final TextView conn_user_name= (TextView) this.findViewById(R.id.conn_user_name);
			final TextView conn_password= (TextView) this.findViewById(R.id.conn_password);
			//final TextView conn_status= (TextView) this.findViewById(R.id.conn_status);
			
			conn_status.setText("Connected");
			
			conn_ip_addr.setText(conn_data[0]);
			conn_user_name.setText(conn_data[1]);
			
			if(conn_data[2]==password)conn_password.setText("Use your campus password");
			conn_password.setText(conn_data[2]);
			
			conn_data_secure=conn_data;
	// Now launch a Linux terminal to SSH to the reserved machine.
			// oba.cancelReservation();
		
		}
		}
        
    
    
    private String[] conn_data_secure;
    
	public void conn_cancel(View V){
	Toast.makeText(getBaseContext(), "Cancelling", Toast.LENGTH_LONG).show();
	if(TestOBA.oba.cancelReservation())
	{
		finish();
	}
		
	}
	
	public void conn_do(View v){
		Toast.makeText(getBaseContext(), "Connecting", Toast.LENGTH_LONG);
		try {
			String Conn_URI="ssh://"+conn_data_secure[1]+ "@"+conn_data_secure[0]+":22/#adith";
							
			Log.d("conn_string", Conn_URI);
			
			Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(Conn_URI));
		    startActivity(intent);
			
			
			ConnectWithPass.conn_do(conn_data_secure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		
	
}