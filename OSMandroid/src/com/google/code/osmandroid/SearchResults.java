package com.google.code.osmandroid;

import com.google.code.osmandroid.engine.DatabaseInfo;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchResults extends ListActivity {
	
	private String[] nameList;
	private int[]    lonList;
	private int[]    latList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
    	super.onCreate(savedInstanceState);

        Bundle extras  = getIntent().getExtras();
        String address = null;
        String city    = null;
        
        if (extras != null) {
        	
        	address = extras.getString("address");
        	city    = extras.getString("city");
        	
        	SQLiteDatabase myDb = null;
        	String query;
        	
        	try {
        		
	        	myDb = this.openOrCreateDatabase(DatabaseInfo.DATABASE_NAME, MODE_PRIVATE, null);   	
	        	
	        	if (address == null || address.equals("")) {
		        	
	        		query = "SELECT name, x, y FROM " + DatabaseInfo.TABLE_NAME +
							" WHERE city='" + city + "' AND " + 
							"name = '" + city + "'";
	        	}
	        	else {
	        		
	        		query = "SELECT name, x, y FROM " + DatabaseInfo.TABLE_NAME +
	        							" WHERE city='" + city + "' AND " + 
	        									"name LIKE '%" + address + "%'";
	        	}
	        	
	        	Cursor c = myDb.rawQuery(query, null);
	        	int rowCount = c.getCount();
	        	
	        	if (rowCount == 0) {
	        		
	        		nameList    = new String[1];
	        		nameList[0] = "No results found";
	        	}
	        	else {
	        		
		            nameList = new String[rowCount];
		            latList  = new int[rowCount];
		            lonList  = new int[rowCount];
	
		            int i = 0;
		            c.moveToFirst();
		            do {
		            	
		            	nameList[i] = c.getString(0);
		            	lonList[i]  = c.getInt(1);
		            	latList[i]  = c.getInt(2);
		            	i++;
		            	
		            } while (c.moveToNext() && !c.isAfterLast());            	
	
	        	}
        	}
        	catch (Exception e) {
        		
          	   Log.e("", e.getMessage());
            } 
            finally {
            	 
              if (myDb != null && myDb.isOpen())
                   myDb.close();
             }
        
        }
        else{
    		nameList    = new String[1];
    		nameList[0] = "No results found";
        }
        
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameList));
        getListView().setTextFilterEnabled(true);
	}
	
    public void onListItemClick(ListView parent, View v,int position, long id) {
    	
    	Intent intent = new Intent("com.google.code.osmandroid.PLAN_ON_MAP");
    	intent.putExtra("centerX", lonList[position]);
    	intent.putExtra("centerY", latList[position]);
    	intent.putExtra("zoom", 15);
    	
    	startActivity(intent);
    }

}
