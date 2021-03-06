package edu.cc.oba;

import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;


public class mainUITabs extends TabActivity {
	public static TabHost mTabHost;
	

	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		ConnectivityManager cm = null;
		try{
			cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		}
		catch(NullPointerException e)
		{
			Toast.makeText(getBaseContext(), "No Internet ConnectioN Found", 4).show();
			finish();
		}
		    
		   try{ if( cm.getActiveNetworkInfo().isConnectedOrConnecting()==false)
		    {
		    	Toast.makeText(getBaseContext(), "No Internet ConnectioN Found", 4).show();
		    }}
		   catch(NullPointerException e)
	    	  {
	    		Toast.makeText(getBaseContext(), "No Internet ConnectioN Found", 4).show();
	    		  finish();
	    	  }
		
		setContentView(R.layout.tab);
	    mTabHost=getTabHost();
	    
	    

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Reusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab
	    

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, chooseImage.class); 
	    intent.putExtra("username", getIntent().getStringExtra("username"));
		intent.putExtra("password", getIntent().getStringExtra("password"));
	    
	    // Initialize a TabSpec for each tab and add it to the TabHost
		intent = new Intent().setClass(this, oneButtons.class);
		spec = tabHost.newTabSpec("One Buttons").setIndicator("One Buttons",
                res.getDrawable(R.drawable.tab1))
            .setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, chooseImage.class);		
		spec = tabHost.newTabSpec("New Reservation").setIndicator("New Reservation",
	                      res.getDrawable(R.drawable.tab2))
	                  .setContent(intent);
	    tabHost.addTab(spec);
	    

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, createConnection.class);
	    intent.putExtra("username", getIntent().getStringExtra("username"));
		intent.putExtra("password", getIntent().getStringExtra("password"));
		
	    spec = tabHost.newTabSpec("Current Reservation").setIndicator("Current Reservation",
	                      res.getDrawable(R.drawable.tab3)).setContent(intent);
	    tabHost.addTab(spec);
	    
	    

	    	    
	}
	
	//Catch Back button event
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	 AlertDialog.Builder adb = new AlertDialog.Builder(this);
	            adb.setCancelable(true);
	            
	            adb.setNeutralButton("Yes", new DialogInterface.OnClickListener(){
	          	  @Override
	        		public void onClick(DialogInterface arg0, int arg1) {
	        			// TODO Auto-generated method stub
	        			//CODE TO EXTEND RESERVATION
	          		  finish();
	        		}
	            });
	          	  
	          	  adb.setMessage("Do you Want to Exit");
	          	  adb.show();
	          	  
	            
	            adb.setNegativeButton("No", new DialogInterface.OnClickListener(){
	          	
	      		@Override
	      		public void onClick(DialogInterface arg0, int arg1) {
	      			// TODO Auto-generated method stub
	      			
	      			     				
	      		}
	          	  
	            });
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}

	 
    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.layout.mainmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
 
        switch (item.getItemId())
        {
        case R.id.menu_logout:
            // Single menu item is selected do something
            // Ex: launching new activity/screen or show alert message
            Toast.makeText(getBaseContext(), "Logging out", Toast.LENGTH_SHORT).show();
            
            Android_OBAActivity.settings.edit().clear().commit();
            finish();
            return true;
 
        case R.id.menu_about:
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage("VCL - One Button App v1.0 \n " +
        			"This product includes software developed by : \n" +
        			"the Apache Software Foundation http://www.apache.org/ \n" +
        			"Android : http://developer.android.com \n" +
        			"VCL : http://vcl.ncsu.edu")
        	       .setCancelable(false)
        	             	       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert = builder.create();
        	alert.show();
            return true;
 
        case R.id.menu_help:
        	AlertDialog.Builder builder_help = new AlertDialog.Builder(this);
        	builder_help.setMessage("=====HELP=====\n" +
        			"This Application is used to reserve and launch applications at the click of a button.\n" +
        			"1. The entire framework is based on the NCSU's VCL Cloud \n" +
        			"2. For more information on VCL and other troubleshooting, please visit www.vcl.ncsu.edu \n" +
        			"3. For troubleshooting information please consult the manual available at the above website\n" +
        			"4. This application needs external 3rd party Software to launch reservations. Please install Remote Rdp and ConnectBot for Windows and Unix based reservations\n")
        	       .setCancelable(false)
        	             	       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	                dialog.cancel();
        	           }
        	       });
        	AlertDialog alert_help = builder_help.create();
        	alert_help.show();
            return true;
  
        default:
            return super.onOptionsItemSelected(item);
        }
    }
	
	
	
}
