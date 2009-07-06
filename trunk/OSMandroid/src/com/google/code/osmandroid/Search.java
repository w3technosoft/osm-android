package com.google.code.osmandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Search  extends Activity {

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.search);
    
        Button searchButton   = (Button)findViewById(R.id.search_button);
        searchButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        	    grabEnteredText();
        	}
        });
    }
	
    private void grabEnteredText() {
        
    	EditText addressEditText = (EditText)findViewById(R.id.search_address_text);
    	EditText cityEditText    = (EditText)findViewById(R.id.search_city_text);
    	
    	String address = addressEditText.getText().toString().trim();
    	String city    = cityEditText.getText().toString().trim();
    	
    	Intent intent = new Intent("com.google.code.osmandroid.DISPLAY_RESULTS");
    	intent.putExtra("address", address);
    	intent.putExtra("city", city);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    	startActivity(intent);
    }
	
}
