package com.google.code.osmandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.routing.Route;
import com.google.code.osmandroid.view.OsmMapView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class ViewMap extends Activity implements LocationListener {

	private OsmMapView  osmMapView;
	private ImageButton zoomInButton;
	private ImageButton zoomOutButton;
	private Button      setStartButton;
	private Button      setDestButton;
	private Button      calcRouteButton;
	private Button      searchButton;
	private Button      navigateButton;
	private TextView    currSelectionView;
	
	private LocationManager  locationManager;

	private int routeType;
	
	private int mapMode;
	private static final int MAP_MODE_NAVIGATE    = 1;
	private static final int MAP_MODE_PLAN_ON_MAP = 2;
	
	
	private boolean      recordTrack;
	private String	     gpxLogFile;
	private FileWriter   gpxLogWriter;
	private final String gpx_dir = "/sdcard/osm_android/gpx";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.mapview);
		
		this.osmMapView        = (OsmMapView)  findViewById(R.id.osmmap);
		this.zoomInButton      = (ImageButton) findViewById(R.id.zoom_in);
		this.zoomOutButton     = (ImageButton) findViewById(R.id.zoom_out);
		this.setStartButton    = (Button)   findViewById(R.id.setstart);
		this.setDestButton     = (Button)   findViewById(R.id.setdest);
		this.calcRouteButton   = (Button)   findViewById(R.id.calcroute);
		this.searchButton      = (Button)   findViewById(R.id.search);
		this.navigateButton    = (Button)   findViewById(R.id.navigate);
		this.currSelectionView = (TextView) findViewById(R.id.curr_selection);
		
		this.locationManager   = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		// Set the view which will show the name of the current selected item 
		this.osmMapView.setCurrentSelectionView(this.currSelectionView);
		
		// Default mode is plan on map
		this.mapMode = MAP_MODE_PLAN_ON_MAP;
		
		// Set the click handlers
		this.zoomInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				osmMapView.zoomIn();
			}
		});

		this.zoomOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				osmMapView.zoomOut();
			}
		});

		this.setStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				osmMapView.setStartFlag();
			}
		});

		this.setDestButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				osmMapView.setDestFlag();
			}
		});

		this.calcRouteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				osmMapView.calculateRoute();
			}
		});

		this.searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent("com.google.code.osmandroid.SEARCH_ON_MAP"));
			}
		});

		navigateButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				if (mapMode == ViewMap.MAP_MODE_NAVIGATE) {
					
					mapMode = ViewMap.MAP_MODE_PLAN_ON_MAP;
					enablePanOnMap();
				}
				else {
					
					mapMode = ViewMap.MAP_MODE_NAVIGATE;
					enableNavigation();
				}
			}
		});
		
		getPreferences();

		this.osmMapView.setRouteType(this.routeType);
		setFocusPoint(getIntent().getExtras());
		
		this.gpxLogFile   = this.getNextGpxFilename();
		this.gpxLogWriter = null;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		
		getPreferences();
		this.osmMapView.setRouteType(this.routeType);
		setFocusPoint(intent.getExtras());
	}

	private void setFocusPoint(Bundle extras) {
		
		if (extras != null) {

			int centerX   = extras.getInt("centerX");
			int centerY   = extras.getInt("centerY");
			int zoomLevel = extras.getInt("zoom");
			
			this.osmMapView.setFocusPoint(centerX, centerY, zoomLevel);
		}

	}
	
	private void enableNavigation() {

		this.navigateButton.setText("Plan on map");
		this.setStartButton.setVisibility(View.INVISIBLE);
		this.setDestButton.setVisibility(View.INVISIBLE);
		this.calcRouteButton.setVisibility(View.INVISIBLE);
		
		this.osmMapView.disablePan();
		
		this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	private void enablePanOnMap() {

		this.navigateButton.setText("Navigate");	
		this.setStartButton.setVisibility(View.VISIBLE);
		this.setDestButton.setVisibility(View.VISIBLE);
		this.calcRouteButton.setVisibility(View.VISIBLE);
		
		this.osmMapView.enablePan();
		
		this.locationManager.removeUpdates(this);
	}
	
    public void onLocationChanged(Location loc) {
    	
    	if (this.mapMode == MAP_MODE_NAVIGATE) {

    		osmMapView.setCurrentGpsPosition(loc.getLongitude(), loc.getLatitude());
    	}   	
    	
    	if (this.recordTrack == true) {
    	
    		updateGpxTrack(loc.getLongitude(), loc.getLatitude(), loc.getAltitude());
    	}
    }

    public void onProviderDisabled(String provider) {}
    
    public void onProviderEnabled(String provider) {}
    
    public void onStatusChanged(String provider, int status, Bundle extras) {}

	private void getPreferences() {
		
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		this.routeType   = app_preferences.getInt("route_type", Route.ROUTE_FASTEST);
		this.recordTrack = app_preferences.getBoolean("record_track", false); 
	}
	
	private String getNextGpxFilename() {
		
		String prefix = "track-";
		
		File folder        = new File(this.gpx_dir);
        File[] listOfFiles = folder.listFiles();
        
        int max_track_no = 1;
        
        if (listOfFiles == null)
        	return prefix + String.valueOf(max_track_no);
        
        for (File file : listOfFiles) {
        	
        	if (!file.isFile()) {
        		continue;
        	}
        	
        	
        	String tileName = file.getName();
        	if (tileName.indexOf(prefix) == 0) {
        		
        		String suffix = tileName.substring(prefix.length(), tileName.length());
        	
        		try {
        			int id = Integer.parseInt(suffix);
        			if (id > max_track_no) {
        				
        				max_track_no = id;
        			}
        		}
        		catch (Exception e) {
        			continue;
        		}
        	}
        }
        
		return prefix+String.valueOf(max_track_no);
	}
	
	private void startGpxRecording(){
		
		String filename = this.getNextGpxFilename();
		try {
			
			this.gpxLogWriter = new FileWriter(new File(this.gpx_dir + getNextGpxFilename()));
			this.gpxLogWriter.write("start\n");
		}
		catch (IOException e) {
			
			this.recordTrack = false;
			SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = appPreferences.edit();
			editor.putBoolean("record_track", false);
			editor.commit();
			
			//TODO: notify user
		}
	}
	
	private void stopGpxRecording(){
		
		if (this.gpxLogWriter == null) {
			return;
		}
		
		try {
			this.gpxLogWriter.write("stop\n");
			this.gpxLogWriter.flush();
			this.gpxLogWriter = null;
		}
		catch (IOException e) {
			this.gpxLogWriter = null;
		}
	}
	
	private void updateGpxTrack(double lon, double lat, double alt){
		
		if (this.gpxLogWriter == null) {
			return;
		}
		
		try {
						
			this.gpxLogWriter.write("lon, lat");
		}
		catch (IOException e) {
			
		}
		
	}
	/*

	protected void onResume() {
		
		super.onResume();
		
		boolean prevState = this.recordTrack;
		getPreferences();
		if (prevState == true && this.recordTrack == false) {
			//finalize gpx track
		}
		else if (prevState == false && this.recordTrack == true) {
			//create gpx file
		}
	}
	
	protected void onDestroy() {
		
		super.onDestroy();
		
		if (this.recordTrack == true) {
			//inalize gpx track
		}
	}
	*/
}
