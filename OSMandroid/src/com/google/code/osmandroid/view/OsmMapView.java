package com.google.code.osmandroid.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.google.code.osmandroid.R;
import com.google.code.osmandroid.engine.MapEngine;
import com.google.code.osmandroid.engine.MapRect;
import com.google.code.osmandroid.graphics.OsmMapRenderer;
import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.MapItem;
import com.google.code.osmandroid.mapdata.MapPresets;
import com.google.code.osmandroid.mapdata.Mercator;
import com.google.code.osmandroid.routing.Route;
import com.google.code.osmandroid.routing.RoutePoint;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.Shader.TileMode;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.TextView;

public class OsmMapView extends GLSurfaceView{
	
	// Map rendering data
	public  static OpenGlTransformation transformation;
	
	private float aspectRatio;
	private OsmMapRenderer osmRenderer;
	private static final int ICON_SIZE = 20;
	private TextView currSelection;
	
	//Map data
	private MapEngine	mapEngine;
	private MapRect 	currMapRect;
	private int 		currZoomLevel;
	
	// Control data
	private static final int[] zoomSize = { 0,
		408500000, 318500000, 188500000, 175500000, 195000000,
		11200000, 5600000, 2800000, 2800000, 1500000, 
		500000,	200000, 60000, 30000, 15000, 5000, 0
		
	};
	private int   	prevX;
	private int   	prevY;
	private boolean mapPanning = false;
	private static final int MAX_ZOOM_LEVEL = 17;
	private static final int MIN_ZOOM_LEVEL =  1;
	private boolean enablePan = true;
	
	
	// Routing Data
	private Route 	route;
	private MapItem cursor;
	private MapItem startPoint;
	private MapItem destPoint;
	private MapItem positionPoint;
	private LinkedList<MapItem> routePath;
	private static final int CURSOR_SEARCH_RADIUS = 65536;
	
	// GPS data
	private LocationProvider locationProvider;
	private LocationManager  locationManager;
	
	
	// Threading data
	private ProgressDialog mypd;
	private Builder		   noRoute;
	Handler myGUIUpdateHandler = new Handler() {

	   public void handleMessage(Message msg) {
		   
	      switch (msg.what) {
	         
	      	case ROUTE_CALCULATION_COMPLETE:
	      		if (currSelection != null) {
	      			currSelection.setText("");
	      		}
	            renderScene();
	            break;

	         case NO_ROUTE_EXISTS:
		            noRoute.show();
		            break;

	         default:
	            break;
	      }
	      super.handleMessage(msg);
	   	}
	}; 
	protected static final int ROUTE_CALCULATION_COMPLETE = 12345;
	protected static final int NO_ROUTE_EXISTS 			  = 12346;

	SharedPreferences appPreferences;

	
	private static final String logTag = "OsmMapView"; 
	
    public OsmMapView(Context context) {

        super(context);
        init(context, null);
    }
     
    public OsmMapView(Context context, AttributeSet attrs) {

    	super(context, attrs);
    	init(context, null);
    }
 
    public void init(Context context, AttributeSet attrs) {

    	this.osmRenderer = new OsmMapRenderer(context);
    	setRenderer(this.osmRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        this.appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    	
        transformation = new OpenGlTransformation();    	
        
		this.mapEngine      = new MapEngine();
		this.currMapRect    = new MapRect();
		this.currZoomLevel  = 0;
		this.route 			= new Route(this.mapEngine);
		
		this.setFocusable(true);
		
		this.cursor  		  = new MapItem();
		this.cursor.type      = MapPresets.ICON;
		this.cursor.flags     = R.drawable.dart;
		
		this.startPoint 	  = new MapItem();
		this.startPoint.type  = MapPresets.ICON;
		this.startPoint.flags = R.drawable.greenflag;
		
		this.destPoint 		  = new MapItem();  
		this.destPoint.type   = MapPresets.ICON;
		this.destPoint.flags  = R.drawable.checkedflag;
		
		this.positionPoint	      = new MapItem();  
		this.positionPoint.type   = MapPresets.ICON;
		this.positionPoint.flags  = R.drawable.position;
		
		this.noRoute = new AlertDialog.Builder(this.getContext());
		this.noRoute.setTitle("Could not calculate route");
		this.noRoute.setPositiveButton("OK", null);
		this.noRoute.setIcon(0);
		
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OsmMapView);
		int centerX = a.getInt(R.styleable.OsmMapView_centerX, 204674492); 
    	int centerY = a.getInt(R.styleable.OsmMapView_centerY, 370908198);  
    	int zoom    = a.getInt(R.styleable.OsmMapView_zoom, 15);

    	setMapArea(new BoundingBox(centerX - 53333, centerX + 53333, centerY - 30131, centerY + 30131), zoom);
    
    }
    
    public void enablePan(){
    	this.enablePan = true;
    }

    public void disablePan(){
    	this.enablePan = false;
    }
    
    public void setRouteType(int routeType){
    	
    	this.route.setRouteType(routeType);
    }
    
    public void setCurrentSelectionView(TextView view){
    	
    	this.currSelection = view;
    }
    
    public void setFocusPoint(int x, int y, int zoom){
    	
    	setMapArea(new BoundingBox(x - 53333, x + 53333, y - 30131, y + 30131), 15);
    }
    
    public void setCurrentGpsPosition(double lon, double lat){

		MapItem positionLocal    = this.positionPoint;
		MapRect currMapRectLocal = this.currMapRect;
		
		int minx   = currMapRectLocal.minX;
		int miny   = currMapRectLocal.minY;
		int maxx   = currMapRectLocal.maxX;
		int maxy   = currMapRectLocal.maxY;
		
		int t1 = (int)Math.round(Mercator.mercX(lon));
		int t2 = (int)Math.round(Mercator.mercY(lat));
		
		int worldX = (int)((float)t1/1000 * 65536);
		int worldY = (int)((float)t2/1000 * 65536);

		RoutePoint rp = route.findNearestItem(currMapRectLocal, null, worldX, worldY, true);
		
		worldX = rp.streetX;
		worldY = rp.streetY;
		
		int width  = transformation.screenToWorldX(minx, maxx, ICON_SIZE) - minx;
	
		positionLocal.minX = worldX - width/2;
		positionLocal.minY = worldY;
		positionLocal.maxX = worldX + width/2;
		positionLocal.maxY = worldY + width;
		
		int w = (currMapRectLocal.maxX - currMapRectLocal.minX)/2;
		int h = (currMapRectLocal.maxY - currMapRectLocal.minY)/2;

    	setMapArea(new BoundingBox(worldX - w, worldX + w, worldY - h, worldY + h), this.currZoomLevel);
    	
    	if (currSelection!=null) {
			if (rp.item == null || rp.item.name == null) {
				currSelection.setText("");
			}
			else {
				currSelection.setText(rp.item.name);
			}
		}


    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	super.surfaceChanged(holder, format, w, h);
    	transformation.windowHeight = h;
    	transformation.windowWidth = w;
    	this.aspectRatio = (float)w/h;
    	
    }
    
    public void setMapArea(BoundingBox area, int zoomLevel) {
    	
		MapRect currMapRectLocal   = this.currMapRect;
		currMapRectLocal.minX 	   = area.minX;
		currMapRectLocal.minY 	   = area.minY;
		currMapRectLocal.maxX 	   = area.maxX;
		currMapRectLocal.maxY 	   = area.maxY;
		
		this.currZoomLevel = zoomLevel;

		renderScene();
    }

	public void zoomIn() {
		
		if (this.currZoomLevel == MAX_ZOOM_LEVEL)
			return;
		
		MapRect currMapRectLocal  = this.currMapRect;

		long initialWidth = (long)Math.abs((long)currMapRectLocal.maxX - currMapRectLocal.minX);
		
		currMapRectLocal.minY += zoomSize[currZoomLevel];
		currMapRectLocal.maxY -= zoomSize[currZoomLevel];

		long zoomedWidth  = (long)((long)Math.abs((long)currMapRectLocal.maxY - (long)currMapRectLocal.minY) * this.aspectRatio);
		
		int step = (int)(initialWidth-zoomedWidth)/2;
		currMapRectLocal.minX += step;
		currMapRectLocal.maxX -= step;
		
		this.currZoomLevel++;
		
		renderScene();
	}
	
	public void zoomOut() {
			
		if (this.currZoomLevel == MIN_ZOOM_LEVEL)
			return;
		
		MapRect currMapRectLocal = this.currMapRect;
		
		this.currZoomLevel--;
		
		currMapRectLocal.minY -= zoomSize[currZoomLevel];
		currMapRectLocal.maxY += zoomSize[currZoomLevel];
			
		long zoomedWidth  = (long)(Math.abs((long)currMapRectLocal.maxY - currMapRectLocal.minY) * this.aspectRatio);
		long initialWidth = (long) Math.abs((long)currMapRectLocal.maxX - currMapRectLocal.minX);
		
		int size = (int)(zoomedWidth-initialWidth)/2;
		currMapRectLocal.minX -= size;
		currMapRectLocal.maxX += size;
		
		renderScene();
	}
	  
	public void setStartFlag() {

		MapItem cursorLocal 	 = this.cursor;
		MapItem startPointLocal  = this.startPoint;
		MapRect currMapRectLocal = this.currMapRect;

		int minx   = currMapRectLocal.minX;
		int miny   = currMapRectLocal.minY;
		int maxx   = currMapRectLocal.maxX;
		int maxy   = currMapRectLocal.maxY;

		int width  = transformation.screenToWorldX(0, maxx - minx, ICON_SIZE);
		
		startPointLocal.minX    = cursorLocal.minX;
		startPointLocal.minY    = cursorLocal.minY;
		startPointLocal.maxX    = startPointLocal.minX + width;
		startPointLocal.maxY    = startPointLocal.minY + width;
		
		renderScene();
		
		this.route.setStartPoint(startPointLocal.minX, startPointLocal.minY);
	}

	public void setDestFlag() {

		MapItem cursorLocal      = cursor;
		MapItem destPointLocal   = destPoint;
		MapRect currMapRectLocal = currMapRect;

		int minx   = currMapRectLocal.minX;
		int miny   = currMapRectLocal.minY;
		int maxx   = currMapRectLocal.maxX;
		int maxy   = currMapRectLocal.maxY;

		int width  = transformation.screenToWorldX(0, maxx - minx, ICON_SIZE);

		destPointLocal.minX    = cursorLocal.minX;
		destPointLocal.minY    = cursorLocal.minY;
		destPointLocal.maxX    = destPointLocal.minX + width;
		destPointLocal.maxY    = destPointLocal.minY + width;

		renderScene();
		
		this.route.setDestPoint(destPointLocal.minX, destPointLocal.minY);
	}

	public void calculateRoute() {
		
		/*
		 * Test (benchmark data)
		this.route.setStartPoint(204676269, 370898494);
		//this.route.setDestPoint(187142508, 373436777);
		this.route.setDestPoint(176454510, 374199623);
		 */
		
		mypd = ProgressDialog.show(this.getContext(),
				      null,
				      "Calculating route...",
				      false);

		Thread t = new Thread() {

		      public void run() {

		    	boolean routeExists = false;  
		    	try {
		    		long start, end;
				
		    		start = System.currentTimeMillis();
				
		    		route.buildRouteGraph();
				
		    		end = System.currentTimeMillis();
		    		Log.i(logTag, "Build route graph: " + String.valueOf(end-start) );

		    		mapEngine.clearTileCache();
		    		System.gc();
		    		
		    		start = System.currentTimeMillis();
				
		    		ArrayList<Long> routePoints = route.calculateShortestPath();
				
					end = System.currentTimeMillis();
					Log.i(logTag, "Calculate shortest path: " + String.valueOf(end-start) );
					
					if (routePoints != null) {
						
						start = System.currentTimeMillis();
						
						routePath = route.getRouteMapItems(routePoints);
						
						end = System.currentTimeMillis();
						Log.i(logTag, "Get Items: " + String.valueOf(end-start) );
	
						Log.i(logTag, "Route length: " + String.valueOf((double)Route.length/65536));
					}
					else {
						routePath = null;
					}
					
					routeExists = true;
			    	mypd.dismiss();
		    	}
			    catch (Exception e) {
			    		mypd.dismiss();
			    }
		         // Send a message to the handler
		         Message message = new Message();
		         if (routeExists)
		        	 message.what = ROUTE_CALCULATION_COMPLETE;
		         else
		        	 message.what = NO_ROUTE_EXISTS;
		         myGUIUpdateHandler.sendMessage(message);
		         }
		      };

		   t.start();

	}

	private void pan(int screenDx, int screenDy) {
		
		MapRect currMapRectLocal = this.currMapRect;
		int zoomLevelLocal	     = this.currZoomLevel;
		int stepX, stepY;
		
		stepX = transformation.screenToWorldX(0, currMapRectLocal.maxX - currMapRectLocal.minX, -screenDx);	
		stepY = transformation.screenToWorldY(0, currMapRectLocal.maxY - currMapRectLocal.minY, transformation.windowHeight-screenDy);
		
		currMapRectLocal.minX += stepX;
		currMapRectLocal.maxX += stepX;
 		
		currMapRectLocal.minY += stepY;
		currMapRectLocal.maxY += stepY;

		this.osmRenderer.setMap(currMapRectLocal, zoomLevelLocal, !this.mapPanning);
		this.requestRender();
	}

	private void setCursor(int x, int y) {

		Log.i(logTag, "Touch at:" + String.valueOf(x) + "," + String.valueOf(y));
		
		MapItem cursorLocal      = this.cursor;
		MapRect currMapRectLocal = this.currMapRect;
		
		int minx   = currMapRectLocal.minX;
		int miny   = currMapRectLocal.minY;
		int maxx   = currMapRectLocal.maxX;
		int maxy   = currMapRectLocal.maxY;
	
		int worldX = transformation.screenToWorldX(minx, maxx, x);
		int worldY = transformation.screenToWorldY(miny, maxy, y);	
		int width  = transformation.screenToWorldX(minx, maxx, ICON_SIZE) - minx;

		Log.i(logTag, "Touch at:" + worldX + "," + worldY);
		
		cursorLocal.minX = worldX;
		cursorLocal.minY = worldY;
		cursorLocal.maxX = worldX + width;
		cursorLocal.maxY = worldY + width;
		
		RoutePoint rp = route.findNearestItem(currMapRect, null, worldX, worldY, false);
		if (currSelection!=null) {
			if (rp.item == null || rp.item.name == null) {
				currSelection.setText("");
			}
			else {
				currSelection.setText(rp.item.name);
			}
		}

	}

	private void renderScene(){
 		
		Log.i(logTag, "Set view area " + currMapRect + " @ zoom " + currZoomLevel);
		
		currMapRect = mapEngine.getMapRect(currMapRect, currZoomLevel, true);
		
		MapRect currMapRectLocal 			 = this.currMapRect;
		int zoomLevelLocal  	 			 = this.currZoomLevel;
		ArrayList<MapItem> currMapItemsLocal = this.currMapRect.getMapItems();
		
		if (currMapRectLocal.overlaps(this.cursor)) {
			
			int width   = transformation.screenToWorldX(0, currMapRectLocal.maxX - currMapRectLocal.minX, ICON_SIZE);			
			this.cursor.maxX = cursor.minX + width;
			this.cursor.maxY = cursor.minY + width;
			
			currMapItemsLocal.add(this.cursor);
		}

		if (currMapRectLocal.overlaps(startPoint)) {
			
			int width  		= transformation.screenToWorldX(0, currMapRectLocal.maxX - currMapRectLocal.minX, ICON_SIZE);			
			this.startPoint.maxX = startPoint.minX + width;
			this.startPoint.maxY = startPoint.minY + width;
			
			currMapItemsLocal.add(this.startPoint);
		}
		
		if (currMapRectLocal.overlaps(this.destPoint)) {
		
			int width  		= transformation.screenToWorldX(0, currMapRectLocal.maxX - currMapRectLocal.minX, ICON_SIZE);			
			this.destPoint.maxX  = destPoint.minX + width;
			this.destPoint.maxY  = destPoint.minY + width;
			currMapItemsLocal.add(destPoint);
		}
				
		if (this.routePath != null) {
			
			for (MapItem item : this.routePath) {
				
				if (currMapRectLocal.overlaps(item)) {
					currMapItemsLocal.add(item);
				}
			}
		}

		if (currMapRectLocal.overlaps(positionPoint)) {
			
			int width  	= transformation.screenToWorldX(0, currMapRectLocal.maxX - currMapRectLocal.minX, ICON_SIZE);
			
			int centerX = (positionPoint.minX + positionPoint.maxX)/2;
			int centerY = positionPoint.minY;
			
			this.positionPoint.minX = centerX - width/2;
			this.positionPoint.maxX = centerX + width/2;
			
			this.positionPoint.minY = centerY;
			this.positionPoint.maxY = centerY + width;
			
			currMapItemsLocal.add(positionPoint);
		}

		
		this.osmRenderer.setMap(currMapRectLocal, zoomLevelLocal, !this.mapPanning);
		this.requestRender();
		
	}
		
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
    	if (!this.enablePan) {
    		
    		return true;
    	}
    	
        int x = (int)event.getX();
        int y = (int)event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dx = x - this.prevX;
                int dy = y - this.prevY;
                if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
                	this.mapPanning = true;
                	pan(2*dx, 2*dy);
                }
                break;
                
            case MotionEvent.ACTION_UP:
            	
            	if (!this.mapPanning) {
            		setCursor(x, y);
            	}
            	
            	this.mapPanning = false;
            	renderScene();
            	break;
        }
        
        this.prevX = x;
        this.prevY = y;
        return true;
    }

}
