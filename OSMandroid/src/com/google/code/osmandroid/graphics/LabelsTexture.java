package com.google.code.osmandroid.graphics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.google.code.osmandroid.engine.MapRect;
import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.Coordinates;
import com.google.code.osmandroid.mapdata.MapItem;
import com.google.code.osmandroid.mapdata.MapPresets;
import com.google.code.osmandroid.view.OpenGlTransformation;
import com.google.code.osmandroid.view.OsmMapView;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Align;
import android.opengl.GLUtils;
import android.sax.StartElementListener;
import android.util.Log;

public class LabelsTexture {
	
	private int width;
	private int height;
	private int textureID;

    private Bitmap bitmap;
    private Canvas canvas;
    
    private Paint labelPaint;
    private PriorityQueue<MapItem> prioQueue;
    
    private static final int LABEL_PADDING  = 10;
    private static final int MAX_LABELS_PER_SCENE = 64;
    
    private BoundingBox[] currentLabels = new BoundingBox [MAX_LABELS_PER_SCENE];
    private int numLabels;
    
    public LabelsTexture() {
    	
		this.prioQueue = new PriorityQueue<MapItem>(128, new MapItemComparator());
		this.labelPaint = new Paint();
		this.labelPaint.setAntiAlias(true);
		this.labelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		this.labelPaint.setStrokeWidth(0.4f);
		this.labelPaint.setARGB(0xFF, 0x00, 0x00, 0x00);
    }
    
	public void setDimmension(int width, int height) {
		
		this.width     = width;
		this.height    = height;
	}
	
    public void initialize(GL10 gl) {
        
    	int[] textures = new int[1];
        gl.glGenTextures(1, textures, 0);
        
        this.textureID = textures[0];
        
    }

    public void shutdown(GL10 gl) {
    	
        if ( gl != null) {

        	this.bitmap.recycle();
        	this.bitmap = null;
        	this.canvas = null;
            int[] textures = new int[1];
            textures[0] = this.textureID;
            gl.glDeleteTextures(1, textures, 0);
        }
    }

    public void clearTexture(GL10 gl) {

        if (this.bitmap == null) {
        	this.bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        	this.canvas = new Canvas(this.bitmap);
        }

        this.bitmap.eraseColor(0x00000000);

        this.prioQueue.clear();
        
        this.numLabels = 0;
    }

    public void draw(GL10 gl) {
    	

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureID);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, this.bitmap, 0);
        
        
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
        
        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
        

        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);   
		gl.glEnable(GL10.GL_TEXTURE_2D);        
        
        int w = this.width;
        int h = this.height;
        
        int[] crop = new int[4];
        crop[0] = 0;
        crop[1] = h;
        crop[2] = w;
        crop[3] = -h;

        ((GL11)gl).glTexParameteriv(GL10.GL_TEXTURE_2D,
                GL11Ext.GL_TEXTURE_CROP_RECT_OES, crop, 0);
        ((GL11Ext)gl).glDrawTexiOES((int) 0, (int) 0, 0,
                (int) w, (int) h);
        
        gl.glDisable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    public void addItem(MapItem item){
    	
    	this.prioQueue.add(item);
    }
    
    public void drawLabels(MapRect mapRect, int zoomLevel) {
    	
    	
    	MapItem item;
    	int labelCount = 0;
    	
    	while ((item = this.prioQueue.poll()) != null) {
    		
    		if(++labelCount == MAX_LABELS_PER_SCENE) {
    			break;
    		}
    		
    		setPaintProperties(item.type, zoomLevel);
    		
    		if (item.getShape() == MapItem.SHAPE_LINE) {
    			drawLineLabel(mapRect, item, labelPaint);
    		}
    		else if (item.getShape() == MapItem.SHAPE_POINT) {
    			
    			drawPointLabel(mapRect, item, labelPaint, item.minX, item.minY);
    		}
    		else if (item.getShape() == MapItem.SHAPE_POLYGON) {
    			
    			Coordinates c = item.getCenter();
    			drawPointLabel(mapRect, item, this.labelPaint, c.x, c.y);
    		}
    			
    	}
    }
    
    public void drawPointLabel(MapRect mapRect, MapItem item, Paint paint, int x, int y) {
    	
    	int   itemNumNodesLocal;
    	int[] itemNodesLocal;
    	
    	int lminx;
    	int lmaxx;
    	int lminy;
    	int lmaxy;
    	
    	boolean textFits;
    	
    	OpenGlTransformation t = OsmMapView.transformation;
    	float charWidth        = paint.measureText("a");
    	
    	int minX = mapRect.minX;
    	int maxX = mapRect.maxX;
    	int minY = mapRect.minY;
    	int maxY = mapRect.maxY;
    	
		itemNumNodesLocal = item.numNodes;
		itemNodesLocal 	  = item.nodes;
    	
		int textLength 	  = (int)(charWidth * item.name.length()) + 2 * LABEL_PADDING;
			
		x = t.worldToScreenX(minX, maxX, x);
		y = t.worldToScreenY(minY, maxY, y);
		
		lminx = x - textLength/2;
		lmaxx = lminx + textLength;
		lminy = y;
		lmaxy = Math.round(y + 2 * paint.descent());
		
		int origX = x;
		int origY = y;
		
		int pos = 0;
		textFits = false;
		while (pos <= 3) {
			
			if (!overlaps(lminx, lminy, lmaxx, lmaxy)) {
			
				textFits = true;
				break;
			}
			
			x = origX;
			y = origY;
			
			lminx = x;
			lminy = y;
			lmaxx = x;
			lmaxy = y;
			
			pos++;
			
			switch (pos) {
				
				case 1:					
					lminx = x - textLength/2;
					lmaxx = lminx + textLength;
					lminy = Math.round(y - paint.descent());
					lmaxy = Math.round(y + paint.descent());
					y = lminy;
					paint.setTextAlign(Align.CENTER);
					break;

				case 2:					
					lminx = x;
					lmaxx = lminx + textLength;
					lminy = y;
					lmaxy = Math.round(y + 2 * paint.descent());
					x = lminx;
					paint.setTextAlign(Align.LEFT);
					break;
					
				case 3:					
					lminx = x;
					lmaxx = lminx + textLength;
					lminy = Math.round(y - paint.descent());
					lmaxy = Math.round(y + paint.descent());
					x = lminx;
					y = lminy;
					paint.setTextAlign(Align.LEFT);
					break;
			}
		}
		
    	if (!textFits)
    		return;
		
    	//now draw the label
    	
    	this.currentLabels[this.numLabels++] = new BoundingBox(lminx, lmaxx, lminy, lmaxy);
    	this.canvas.drawText(item.name, x, OsmMapView.transformation.windowHeight-y, paint);
    	
    }
    
    public void drawLineLabel(MapRect mapRect, MapItem item, Paint paint) {
    	
    	int x1,y1,x2,y2,x3,y3;
    	int dx, dy;
    	
    	int lminx;
    	int lmaxx;
    	int lminy;
    	int lmaxy;
    	
    	int posStart, posEnd;
    	
    	long 	streetLength;
    	int 	textLength;
    	boolean textFits;
    	
    	int   itemNumNodesLocal;
    	int[] itemNodesLocal;
    	
    	OpenGlTransformation t = OsmMapView.transformation;
    	float charWidth        = paint.measureText("a");
    	
    	int minX = mapRect.minX;
    	int maxX = mapRect.maxX;
    	int minY = mapRect.minY;
    	int maxY = mapRect.maxY;
    	
		itemNumNodesLocal = item.numNodes;
		itemNodesLocal 	  = item.nodes;
    	
		textLength 	      = (int)(charWidth * item.name.length()) + 2 * LABEL_PADDING;
		
		x1 = t.worldToScreenX(minX, maxX, itemNodesLocal[0]);
    	y1 = t.worldToScreenY(minY, maxY, itemNodesLocal[1]);
    	x2 = 0;
    	y2 = 0;
    	x3 = 0;
    	y3 = 0;
    	
    	lminx = x1;
    	lmaxx = x1;
    	lminy = y1;
    	lmaxy = y1;

    	textFits 	 = false;
    	streetLength = 0;
		posStart     = 0;
		posEnd 	     = 0;
		
		int count = itemNumNodesLocal * 2 -2;
    	for (int i = 2; i < count; i+=2) {
			
    		x2 = t.worldToScreenX(minX, maxX, itemNodesLocal[i]);
    		y2 = t.worldToScreenY(minY, maxY, itemNodesLocal[i+1]);

    		x3 = t.worldToScreenX(minX, maxX, itemNodesLocal[i+2]);
    		y3 = t.worldToScreenY(minY, maxY, itemNodesLocal[i+3]);
    		
    		if (x2 < lminx) lminx = x2;
    		if (x2 > lmaxx) lmaxx = x2;
    		if (y2 < lminy) lminy = y2;
    		if (y2 > lmaxy)	lmaxy = y2;
    		
    		dx = x2-x1;
    		dy = y2-y1;
    		double seg1 = Math.sqrt(dx*dx + dy*dy);

    		dx = x3-x2;
    		dy = y3-y2;
    		double seg2 = Math.sqrt(dx*dx + dy*dy);

    		dx = x3-x1;
    		dy = y3-y1;
    		double seg3 = Math.sqrt(dx*dx + dy*dy);
    		
    		double cos = (double)(seg1 * seg1 + seg2*seg2 - seg3*seg3) / (2*seg1*seg2);
    		if (cos != 1 && cos != -1 && Math.toDegrees(Math.acos(cos)) < 160){        		
    			posStart = i/2;
    			streetLength = 0;
            	lminx = x2;
            	lmaxx = x2;
            	lminy = y2;
            	lmaxy = y2;
    			
            	x1 = x2;
    			y1 = y2;
    			continue;
    		}
    		
    		streetLength += seg1 ;
    		
    		posEnd = i/2;
    		
    		
    		if (streetLength >= textLength) {
    			
    			if (overlaps(lminx, lminy, lmaxx, lmaxy)) {
    				
    				i = posStart * 2;
        			posStart++;
        			streetLength = 0;
        			
                	lminx = t.worldToScreenX(minX, maxX, itemNodesLocal[posStart*2]);
                	lmaxx = lminx;
                	lminy = t.worldToScreenY(minY, maxY, itemNodesLocal[posStart*2+1]);
                	lmaxy = lminy;
        			
                	x1 = lminx;
        			y1 = lminy;
        			continue;
    			}
    			else {
        			textFits = true;	
        			break;        				
    			}
    		}
    		else {
    			x1 = x2;
    			y1 = y2;
    		}
		}
    	
    	if (!textFits)
    		return;

    	//now draw the label
    	Path path      = new Path();
    	int pathStartX = t.worldToScreenX(minX, maxX, itemNodesLocal[posStart*2]);
    	int pathStartY = t.worldToScreenY(minY, maxY, itemNodesLocal[posStart*2 + 1]);
    	int pathEndX   = t.worldToScreenX(minX, maxX, itemNodesLocal[posEnd*2]);
    	int pathEndY   = t.worldToScreenY(minY, maxY, itemNodesLocal[posEnd*2 + 1]);
       	    count      = posEnd - posStart + 1;
       	
       	int windowHeight = OsmMapView.transformation.windowHeight;
               	
    	if ( pathStartX > pathEndX) {
    	
    		path.moveTo(pathEndX, windowHeight - pathEndY);
    		
    		for (int i = posEnd-1; i >=posStart; i--) {
    			
	    		x2 = t.worldToScreenX(minX, maxX, itemNodesLocal[i * 2]);
	    		y2 = t.worldToScreenY(minY, maxY, itemNodesLocal[i * 2 +1]);
	    		path.lineTo(x2, windowHeight - y2);    		
        	}
    	}
    	else {
    		
    		path.moveTo(pathStartX, windowHeight - pathStartY);
    		
    		for (int i = posStart+1; i <=posEnd; i++) {
    			
	    		x2 = t.worldToScreenX(minX, maxX, itemNodesLocal[i * 2]);
	    		y2 = t.worldToScreenY(minY, maxY, itemNodesLocal[i * 2 +1]);
	    		path.lineTo(x2, windowHeight - y2);  
        	}
    	}
    	this.currentLabels[this.numLabels++] = new BoundingBox(lminx, lmaxx, lminy, lmaxy);
    	this.canvas.drawTextOnPath(item.name, path, 0, 0, paint);
	}

    public boolean overlaps(int x1, int y1, int x2, int y2) {
    	
    	BoundingBox[] labels = this.currentLabels;
    	int count 	   	     = this.numLabels;
    	for (int i = 0; i < count; i++) {
    		
    		if (labels[i].overlaps(x1, y1, x2, y2)) {
    			return true;	
    		}
    		
    	}
    	
    	return false;
    	
    }

    private void setPaintProperties(int itemType, int zoomLevel){
    	
    	switch (itemType) {

    	case MapPresets.PLACE_CITY:
    		this.labelPaint.setTextSize(10 + (zoomLevel - itemType/1000));
    		this.labelPaint.setTextAlign(Align.CENTER);
    		break;
    	
    	case MapPresets.PLACE_VILLAGE:
    		this.labelPaint.setTextSize(10 + (zoomLevel - itemType/1000));
    		this.labelPaint.setTextAlign(Align.CENTER);
    		break;
    		
    	default:
    		this.labelPaint.setTextSize(12);
    		this.labelPaint.setTextAlign(Align.CENTER);
    	}
    }

	class MapItemComparator implements Comparator {

		public int compare(Object arg0, Object arg1) {
			
			MapItem item1 = (MapItem)arg0;
			MapItem item2 = (MapItem)arg1;
			
			int type1 = item1.type;
			int type2 = item2.type;
			
			if(type1 > type2) {
				return 1;
			} else if(type1 == type2) {
				return 0;
			} else {
				return -1;
			}
		}

	}
}
