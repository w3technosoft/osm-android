package com.google.code.osmandroid;

import java.io.BufferedReader;
import java.io.FileReader;

import com.google.code.osmandroid.engine.DatabaseInfo;
import com.google.code.osmandroid.routing.Route;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

public class Settings extends Activity implements RadioGroup.OnCheckedChangeListener{

	private RadioGroup        routeTypeRadioGroup;
	private RadioButton       routeShortestButton;
	private RadioButton       routeFastestButton;
	private Button            updateDbButton;
	private ToggleButton	  recordTrackButton;
	private SharedPreferences appPreferences;
	
	//FIXME: should specify in the menu the file path, so that the user can change it
	private static final String DB_FILE = "/sdcard/osm_android/names/names.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.settings);
		
		this.routeTypeRadioGroup = (RadioGroup)  findViewById(R.id.routeTypeMenu);
		this.routeFastestButton  = (RadioButton) findViewById(R.id.routeTypeFastest);
		this.routeShortestButton = (RadioButton) findViewById(R.id.routeTypeShortest);
		this.updateDbButton      = (Button)      findViewById(R.id.updateNameDatabase);
		this.recordTrackButton   = (ToggleButton)findViewById(R.id.recordTrack);
		
		this.appPreferences 	 = PreferenceManager.getDefaultSharedPreferences(this);
		
		
		this.updateDbButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				updateNameDatabase(DB_FILE);
			}
		});
		
		int routeType = this.appPreferences.getInt("route_type", Route.ROUTE_FASTEST);
		
		if (routeType == Route.ROUTE_FASTEST) {
			
			this.routeFastestButton.setChecked(true);
		}
		else if (routeType == Route.ROUTE_SHORTEST) {
			
			this.routeShortestButton.setChecked(true);		
		} 
		else {
			
			this.routeFastestButton.setChecked(false);
			this.routeShortestButton.setChecked(false);
		}
		
		this.routeTypeRadioGroup.setOnCheckedChangeListener(this);
		
		boolean record = this.appPreferences.getBoolean("record_track", false);
		this.recordTrackButton.setChecked(record);
	
		
		this.recordTrackButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				SharedPreferences.Editor editor = appPreferences.edit();
				
		        if (recordTrackButton.isChecked()) {
		        	
		        	editor.putBoolean("record_track", true); 
		        } else {
		        	
		        	editor.putBoolean("record_track", false);
		        }
		        
		        editor.commit();
		    }
		});

	}
	
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    	
    	SharedPreferences.Editor editor = this.appPreferences.edit();
    	
    	if (checkedId == R.id.routeTypeFastest) {
    		
    		editor.putInt("route_type", Route.ROUTE_FASTEST);
    	}
    	else if (checkedId == R.id.routeTypeShortest) {
    		
    		editor.putInt("route_type", Route.ROUTE_SHORTEST);	
    	}
    	else {
    		
    		editor.putInt("route_type", -1);
    	}
    	
    	editor.commit();
    }
    
    
    private void updateNameDatabase(final String filename){
    	
    	final ProgressDialog mypd = ProgressDialog.show(this,
			      null,
			      "Updating name database...",
			      false);
    	
    	new Thread() {
    		
    		public void run(){
    			
    	    	SQLiteDatabase myDb = null;
    	    	
    	        try {
    	        	
    	        	myDb = openOrCreateDatabase(DatabaseInfo.DATABASE_NAME, MODE_WORLD_READABLE, null);
    	        	
    	        	myDb.execSQL("DROP TABLE IF EXISTS " + DatabaseInfo.TABLE_NAME);
    	        	myDb.execSQL("CREATE TABLE " + DatabaseInfo.TABLE_NAME + " (id integer, name text, city text, x interger, y integer)");
    	        	
    	          	try {
    	          		
    	          		int numRecords = 0;
    	          		
    	    	   		BufferedReader br = new BufferedReader(new FileReader(filename));
    	    	   		    	   		
    	    	   		while (true) {
    	    	   			
    	    	   			String id 	= br.readLine();
    	    	   			String name = br.readLine();
    	    	   			String city = br.readLine();
    	    	   			String x	= br.readLine();
    	    	   			String y 	= br.readLine();
    	    	   			 
    	    	   			if (id ==null)
    	    	   				 break;
    	    	   			 
    	    	   			name=name.replace('"',' ');
    	    	   			name=name.replace('\'',' ');
    	    	   			city=city.replace('"',' ');
    	    	   			city=city.replace('\'',' ');
    	    	   		
    	    	   			Log.i("INSERT street no: ", String.valueOf(++numRecords));
    	    	   			
    	    	   			myDb.execSQL("INSERT INTO " + DatabaseInfo.TABLE_NAME + " VALUES (" + Integer.parseInt(id) + ",'"+name+"','"+city+"', " + Integer.parseInt(x) + "," +Integer.parseInt(y) + " )");
    	    	   		}
    	       	    }
    	       	    catch(Exception e) {
    	       	    	Log.e("", e.getMessage());
    	       	    }        	        	
    	        }
    	   	    catch(Exception e) {
    	   	    	
    	   	    	Log.e("", e.getMessage());
    	   	    }
    	   	    finally {
    	   	    	
    	   	    	if (myDb != null && myDb.isOpen()) {
    	   	    		myDb.close();
    	   	    	}
    	   	    	
    	   	    	mypd.dismiss();
    	   	    }
    	
    		}
    		
    	}.start();
    }
    
}
