package com.google.code.osmandroid.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.google.code.osmandroid.R;
import com.google.code.osmandroid.engine.MapRect;
import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.MapItem;
import com.google.code.osmandroid.mapdata.MapPresets;
import com.google.code.osmandroid.view.OsmMapView;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class OsmMapRenderer implements GLSurfaceView.Renderer {
	
	private Context 		context;
	private MapRect			mapRect;
	private int     		zoomLevel;
	private GLTextures		textures;
	private LabelsTexture 	labelsTexture;
	private boolean 		renderLabels;
	
	private static final int MAX_NODES = 2000;
	static int[] openglBugWorkaround = new int[MAX_NODES * 2];
	
	private static HashMap<Integer, Integer> zlayout;
	
	private static final int MAX_TEXTURES = 4;
	

    public OsmMapRenderer(Context context) {
    	
    	this.context = context;
    	
		this.textures = new GLTextures(context, MAX_TEXTURES);
		this.textures.add(R.drawable.dart);
		this.textures.add(R.drawable.greenflag);
		this.textures.add(R.drawable.checkedflag);
		this.textures.add(R.drawable.position);
		
		this.labelsTexture = new LabelsTexture();
		
		this.zlayout = new HashMap<Integer, Integer>();
		
		this.renderLabels = true;
		
		initZlayout();
    }
    
	public void setMap(MapRect mapRect, int zoomLevel, boolean renderLabels) {
		
		this.mapRect   	  = mapRect;
		this.zoomLevel 	  = zoomLevel;
		this.renderLabels = renderLabels;
	}
	
    public int[] getConfigSpec() {

        int[] configSpec = {
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_NONE
        };
        return configSpec;
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	
    	gl.glClearColorx(61937, 61166, 59624, 0);

		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		this.textures.loadTextures(gl);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        
    	gl.glViewport(0, 0, w, h);
    	
    	OsmMapView.transformation.windowHeight = h;
    	OsmMapView.transformation.windowWidth  = w;
    	this.labelsTexture.setDimmension(w, h);
    }
    
    
    public void onDrawFrame(GL10 gl) {
    	
    	MapRect mapRectLocal 			 = this.mapRect;
    	int zoomLevelLocal				 = this.zoomLevel;
    	ArrayList<MapItem> mapItemsLocal = mapRectLocal.items;
    	boolean renderLabelsLocal		 = this.renderLabels;
    	
    	int   itemNumNodesLocal;
    	int[] itemNodesLocal;
    	HashMap<Integer, Integer> zlayoutLocal = this.zlayout;
    	
		int minX = mapRectLocal.minX;
		int maxX = mapRectLocal.maxX;
		int minY = mapRectLocal.minY;
		int maxY = mapRectLocal.maxY;

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrthox(0, maxX - minX,
				    0, maxY - minY,
				    655360, -655360);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatex(0, 0, -65536);

		
		this.labelsTexture.clearTexture(gl);
	
		for(MapItem item : mapItemsLocal) {
			

			int itemTypeLocal = item.type;
			itemNumNodesLocal = item.numNodes;
			itemNodesLocal    = item.nodes;

			
			if (itemTypeLocal == MapPresets.ICON) {
				
				
				if ( this.textures.setTexture(gl, item.flags)) {
					
			        gl.glDisable(GL10.GL_DEPTH_TEST);
			        gl.glEnable(GL10.GL_BLEND);
			        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);   
			        gl.glEnable(GL10.GL_TEXTURE_2D);
			        
			        int itemMinX = OsmMapView.transformation.worldToScreenX(minX, maxX, item.minX);
			        int itemMinY = OsmMapView.transformation.worldToScreenY(minY, maxY, item.minY);
			        int itemMaxX = OsmMapView.transformation.worldToScreenX(minX, maxX, item.maxX);
			        int itemMaxY = OsmMapView.transformation.worldToScreenY(minY, maxY, item.maxY);
			        
			        int[] crop     = new int[4];
			        crop[0] =  0;
			        crop[1] =  itemMaxY - itemMinY;
			        crop[2] =  itemMaxX - itemMinX;
			        crop[3] =  -(itemMaxY - itemMinY)+1; 
			        
			        ((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D, 
			        		GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
			        
			        ((GL11Ext)gl).glDrawTexiOES((int)itemMinX, (int) itemMinY, 0,
			                (int) (itemMaxX - itemMinX), (int) (itemMaxY - itemMinY));
			        
			        gl.glDisable(GL10.GL_BLEND);
			        gl.glDisable(GL10.GL_TEXTURE_2D);
			        gl.glEnable(GL10.GL_DEPTH_TEST);
				}
				
				continue;
			}
			
			//if it is a very tiny element that won't be noticed on the map
			//don't lose time to draw it
			int minLength = (5  - (zoomLevelLocal - 6)) * 65536;
			if (item.numNodes != 1 &&(item.maxX-item.minX < minLength) && (item.maxY-item.minY < minLength))
				continue;


			itemNumNodesLocal = itemNumNodesLocal > MAX_NODES ? MAX_NODES : itemNumNodesLocal;
			int count = itemNumNodesLocal * 2;
			for (int i = 0; i < count; i += 2) {

				this.openglBugWorkaround[i]     = itemNodesLocal[i]     - minX;
				this.openglBugWorkaround[i + 1] = itemNodesLocal[i + 1] - minY;
			
			}

			if (item.type == MapPresets.ROUTE || item.type == MapPresets.ROUTE_SUBITEM) {
				gl.glLineWidthx(196608);
			}
			else {
				int lineWidth = (zoomLevelLocal - itemTypeLocal / 1000 + 1) ;
				gl.glLineWidthx(lineWidth < 8 ? lineWidth<<16 : 8<<16);
			}
			
			setColor(gl, itemTypeLocal);
			
			int zIndex    = 0;
			Integer value = zlayoutLocal.get(itemTypeLocal);
			if (value != null) {
				zIndex = value.intValue();
			}
			
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glDepthMask(true);
			gl.glDepthFunc(GL10.GL_LEQUAL); 
			
			gl.glPushMatrix();
				gl.glTranslatex(0, 0, -zIndex << 10);
				if (item.getShape() == MapItem.SHAPE_LINE) {				
					gl.glVertexPointer(2, GL10.GL_FIXED, 0, IntBuffer.wrap(this.openglBugWorkaround));
					gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, itemNumNodesLocal);
				} 
				else if (item.getShape() == MapItem.SHAPE_POLYGON) {
					gl.glVertexPointer(2, GL10.GL_FIXED, 0, IntBuffer.wrap(this.openglBugWorkaround));
					gl.glDrawArrays(GL10.GL_TRIANGLES, 0, itemNumNodesLocal);
				}
			gl.glPopMatrix();

			if (renderLabelsLocal) {
				renderLabel(gl, item, mapRectLocal, zoomLevelLocal);
			}
		}
		
		this.labelsTexture.drawLabels(mapRectLocal, zoomLevelLocal);
		this.labelsTexture.draw(gl);
		
    }

    public void renderLabel(GL10 gl, MapItem item, MapRect mapRect, int zoomLevel) {
   
		String itemName = item.name;
		int itemType    = item.type;

		if (itemName == null || itemType == MapPresets.ROUTE)
			return;

		int[] itemNodesLocal  = item.nodes;
		int itemNumNodesLocal = item.numNodes;
		

		int itemMinX = item.minX;
		int itemMinY = item.minY;
		int itemMaxX = item.maxX;
		int itemMaxY = item.maxY;
		
		int weight = (int)(getLabelWeight(zoomLevel, itemType) * 65536);
		
		if ((Math.abs(itemMaxX - itemMinX) < weight) && (Math.abs(itemMaxY - itemMinY) < weight)) {
			return;
		} 
	
		this.labelsTexture.addItem(item);

    }
    
    public float getLabelWeight(int zoomLevel, int itemType) {
    	
    	float weight = 100 << 16;
    	
    	switch(itemType){
    	
    		case MapPresets.STREET_PRIMARY:
    			if (zoomLevel > 12) {
    				weight = (float) (1.6 - (zoomLevel - 13) * 0.5);
    			}
    			break;
    			
    		case MapPresets.STREET_SECONDARY:
    			if (zoomLevel > 13) {
    				weight = (float) (1 - (zoomLevel - 14) * 0.2);	
    			}
    			break;
    			
    		case MapPresets.STREET_TERTIARY:
    			if (zoomLevel > 14) {
    				weight = (float) (1 - (zoomLevel - 15) * 0.1);
    			}
    			break;
    			
    		case MapPresets.STREET_RESIDENTIAL:
    			if (zoomLevel > 14) {
    				weight = (float) (0.2 - (zoomLevel - 16) * 0.1);
    			}
    			break;
    			
    		case MapPresets.PLACE_CITY:
    			if (zoomLevel > 5 && zoomLevel < 12) {
    				weight = 0;
    			}
    			break;
    		
    		case MapPresets.PLACE_TOWN:
    			if (zoomLevel > 9 && zoomLevel < 13) {
    				weight = 0;
    			}
    			break;
    			
    		case MapPresets.PLACE_VILLAGE:
    			if (zoomLevel > 10 && zoomLevel < 14) {
    				weight = 0;
    			}
    			break;
    			
    		case MapPresets.NATURAL_WATER:
    			if (zoomLevel >= 12 ) {
    				weight = (float) (4 - (zoomLevel - 12) * 0.4);
    			}
    			break;
    		case MapPresets.LEISURE_PARK:
    		case MapPresets.LEISURE_STADIUM:
    			if (zoomLevel > 14 ) {
    				weight = 0;
    			}
    			break;
    			
    		default:
    				break;
    	}
		
		return weight;
    }
    
	public static void setColor(GL10 gl, int itemType) {
		
		switch (itemType) {
		
			case MapPresets.BOUNDARY_NATION:
				gl.glColor4x(59110, 55255, 57054, 0);
				break;
				
			case MapPresets.STREET_PRIMARY:
				gl.glColor4x(60395, 27756, 38850, 0);
				break;
				
			case MapPresets.STREET_SECONDARY:
				gl.glColor4x(65021, 46260, 42148, 0);
				break;

			case MapPresets.STREET_TERTIARY:
				gl.glColor4x(65536, 55000, 42662, 0);
				break;
			
			case MapPresets.STREET_RESIDENTIAL:
				gl.glColor4x(43652, 43652, 43652, 0);
				break;
				
			case MapPresets.STREET_UNCLASSIFIED:	
			case MapPresets.STREET_SERVICE:
				gl.glColor4x(52652, 54250, 52250, 0);
				break;

			case MapPresets.STREET_MOTORWAY:
			case MapPresets.STREET_MOTORWAY_LINK:
				gl.glColor4x(0, 20000, 30000, 0);
				break;

			case MapPresets.STREET_TRUNK:
			case MapPresets.STREET_TRUNK_LINK:
				gl.glColor4x(43176, 56026, 43176, 0);
				break;
				
			case MapPresets.NATURAL_WATER:
				gl.glColor4x(46517, 53456, 53456, 0);
				break;	
				
			case MapPresets.LANDUSE_INDUSTRIAL:
				gl.glColor4x(57054, 53713, 54741, 0);
				break;

			case MapPresets.LANDUSE_CEMETERY:
				gl.glColor4x(43433, 51914, 44718, 0);
				break;
				
			case MapPresets.LEISURE_PARK:
				gl.glColor4x(46774, 64764, 46774, 0);
				break;
				
			case MapPresets.LEISURE_STADIUM:
				gl.glColor4x(13107, 52428, 39321, 0);
				break;

			case MapPresets.PLACE_CITY:
				gl.glColor4x(52428, 39321, 39321, 0);
				break;
				
			case MapPresets.BUILDING_YES:
				gl.glColor4x(52428, 39321, 39321, 0);
				break;

			case MapPresets.ROUTE:
			case MapPresets.ROUTE_SUBITEM:
				gl.glColor4x(0, 0, 65536, 0);
				break;
				
			default:
				gl.glColor4x(0, 0, 0, 0);
		}
	}
	
	private static void initZlayout() {
		
		int i = 1;
		HashMap<Integer, Integer> layout = zlayout;
		
		layout.put(MapPresets.NATURAL_WATER, 	 	 i++);
		layout.put(MapPresets.STREET_UNCLASSIFIED,   i++);
		layout.put(MapPresets.STREET_SERVICE, 		 i++);
		layout.put(MapPresets.STREET_RESIDENTIAL, 	 i++);
		layout.put(MapPresets.LEISURE_PARK, 	 	 i++);
		layout.put(MapPresets.LEISURE_STADIUM, 		 i++);
		layout.put(MapPresets.LANDUSE_CEMETERY, 	 i++);
		layout.put(MapPresets.LANDUSE_INDUSTRIAL, 	 i++);
		layout.put(MapPresets.STREET_TERTIARY, 		 i++);
		layout.put(MapPresets.STREET_SECONDARY,	 	 i++);
		layout.put(MapPresets.STREET_PRIMARY, 		 i++);
		layout.put(MapPresets.STREET_TRUNK_LINK, 	 i++);
		layout.put(MapPresets.STREET_TRUNK, 		 i++);
		layout.put(MapPresets.STREET_MOTORWAY_LINK,  i++);
		layout.put(MapPresets.STREET_MOTORWAY, 	 	 i++);
		layout.put(MapPresets.ROUTE, 		 		 i++);
		layout.put(MapPresets.ROUTE_SUBITEM, 		 i++);
		layout.put(MapPresets.PLACE_VILLAGE, 		 i++);
		layout.put(MapPresets.PLACE_TOWN, 			 i++);	
		layout.put(MapPresets.PLACE_CITY, 	 		 i++);
	}
}
