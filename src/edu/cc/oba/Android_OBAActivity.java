package edu.cc.oba;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class Android_OBAActivity extends Activity {
	
	public static SharedPreferences settings=null;	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
            ConnectivityManager cm =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);

            System.out.println(cm.getActiveNetworkInfo().toString());
        
        
        settings = getPreferences(0);
        
        
        if(settings.contains("user"))
        {
        	String username=settings.getString("user", "");
        	String password=settings.getString("pass", "");
        	TestOBA.getUserObject(username, password);
        	Intent myIntent = new Intent(Android_OBAActivity.this, mainUITabs.class);
    		myIntent.putExtra("username", username);
    		myIntent.putExtra("password", password);
    		Android_OBAActivity.this.startActivity(myIntent);	
        	
        }
        
        else{
        
        final EditText user_name=(EditText) this.findViewById(R.id.username);
	    final EditText pass_word=(EditText) this.findViewById(R.id.password); 
	    
	    //TEST CODE
	    
	    final String username=user_name.getText().toString();
	      final String password=pass_word.getText().toString();
	            
		    //Stored user name and pass in shared prefs . need more security ?
			
		
		//
	    
        final Button logIn= (Button) this.findViewById(R.id.Login);
        
    
    
        logIn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				    
			      final String username=user_name.getText().toString();
			      final String password=pass_word.getText().toString();
			      boolean auth=true;
			      try{
			    	  TestOBA.getUserObject(username, password);
			    	  auth=true;
			    	  			      }
			      catch(NullPointerException e)
			      {
			    	  Toast.makeText(getBaseContext(), "Check your Password", 4).show();
			    	  auth=false;
			    	  
			      }
			      
			      if(auth==true){
			      settings.edit().putString("user", username).commit();
				    settings.edit().putString("pass", password).commit(); 
				    
			      Log.i("SP", settings.getAll().toString());
			      		      
				Intent myIntent = new Intent(Android_OBAActivity.this, mainUITabs.class);
				myIntent.putExtra("username", username);
				myIntent.putExtra("password", password);
				Android_OBAActivity.this.startActivity(myIntent);	
				
			      }
			      
			}
        
        });
        
        }
    
    
    
    
    }
}