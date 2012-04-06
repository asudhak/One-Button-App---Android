package edu.cc.oba;

import java.util.HashMap;
import java.util.Iterator;

import edu.cc.oba.TestOBA.makeReservation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
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
	public int image_id=0;
	public String[] conn_data;
	public static Boolean flag=false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooseimage);
                
        Intent this_intent= getIntent();
        String username=this_intent.getStringExtra("username");
        String password=this_intent.getStringExtra("password");
        
//        TestOBA.getUserObject(username, password);
                
        ArrayAdapter <CharSequence> adapter =
        		  new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		 
        		//GET IMAGE LIST FOR USER
        		Object[] images=TestOBA.oba.getImagesID();
        		
        		for(int i=0;i<images.length;i++)
        		{
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
						String[] items=s.getSelectedItem().toString().split(",");		
						image_id=Integer.parseInt(items[0].substring(4));		
						Log.d("ID",""+image_id);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
						image_id=1908;
						
					}
        			
				});
        		
        		
 
    }
    
    
    
    
    public void image_Reserve(View v)
  {
    	
    	
    	makeReservation reserveTask=TestOBA.oba.new makeReservation();
    	Log.d("Trying to make reservation for ", ""+image_id);
    	reserveTask.execute(image_id);
    	    }
    
    
}