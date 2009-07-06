package com.google.code.osmandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class OSMandroid extends Activity {
	
	private ImageButton viewMapButton;
	private ImageButton searchButton;
	private ImageButton settingsButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		this.viewMapButton  = (ImageButton) findViewById(R.id.map_button);
		this.searchButton   = (ImageButton) findViewById(R.id.search_button);
		this.settingsButton = (ImageButton) findViewById(R.id.settings_button);
		
		this.viewMapButton.setOnClickListener(new OnClickListener() {
			@Override 
			public void onClick(View v) {
				startActivity(new Intent("com.google.code.osmandroid.PLAN_ON_MAP"));
			}
		});
		
		this.searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent("com.google.code.osmandroid.SEARCH_ON_MAP"));
			}
		});

		this.settingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent("com.google.code.osmandroid.SETTINGS"));
			}
		});
		
    }
}