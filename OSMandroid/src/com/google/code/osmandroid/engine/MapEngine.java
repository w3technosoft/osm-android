package com.google.code.osmandroid.engine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.jar.Attributes.Name;

import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.MapItem;
import com.google.code.osmandroid.mapdata.MapTile;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.sax.StartElementListener;
import android.util.Log;


public class MapEngine {

	private Map map;
	
	private static Cache 	 tileCache;
	private static final int TILE_CACHE_SIZE  = 48;

	private static Cache 	 nameCache;
	private static final int NAME_CACHE_SIZE  = 4096;
	
	private static SQLiteDatabase sqlDb;

	private static final String logTag   = "MapEngine";

	
	public MapEngine() {
		
		this.tileCache = new Cache(TILE_CACHE_SIZE);
		this.nameCache = new Cache(NAME_CACHE_SIZE);
		this.map	   = new Map("Romania", "/sdcard/osm_android_maps");
	} 
	
	public MapRect getMapRect(BoundingBox area, int zoom, boolean getNames) {
		
		MapRect mapRect					= new MapRect(area);
		int zoomLimit   		 		= (zoom + 1) * 1000;
		StringBuffer nameIdsBuffer 		= new StringBuffer();
		
		ArrayList<String>      tiles 	= this.map.tileNames;
		ArrayList<BoundingBox> boxes 	= this.map.tileBBoxes;
		Cache tileCacheLocal			= this.tileCache;
		Cache nameCacheLocal			= this.nameCache;
		ArrayList<MapItem> mapRectItems = mapRect.getMapItems();
		String tileName;
		
		int count = tiles.size();
		for (int tileIndex = 0; tileIndex < count; tileIndex++) {
			
			tileName = tiles.get(tileIndex);
			int tileMaxZoom = tileName.length()+1;
			if (tileName.equals("index")) 
				tileMaxZoom = 1;
			
			if ( tileMaxZoom > zoom )
				continue;
			
			if (area.overlaps(boxes.get(tileIndex))) {
				
				MapTile mapTile = (MapTile)tileCacheLocal.getElement(tileName);
				
				if (mapTile == null) {
					
					mapTile = MapTile.readTile(this.map.path + "/" + tileName);
					tileCacheLocal.addElement(tileName, mapTile);
				}		
				
				int numItems = mapTile.numItems();
				
				for (int i = 0; i < numItems; i++) {
					
					MapItem item = mapTile.getItem(i);
					
					if (item.type > zoomLimit) {	
						break;
					}
					
					int nameId = item.nameId;
					
					if (area.overlaps(item)) {
						
						mapRectItems.add(item);
					}
					
					if (getNames && nameId !=0 && !nameCacheLocal.containsElement(nameId)) {
						
						nameIdsBuffer.append(nameId + ",");
					}
				}
				
			}
		}

		if (getNames) {
			
			int len = nameIdsBuffer.length();
			if (len > 0) {
				
				nameIdsBuffer.deleteCharAt(len-1);
				
				getNamesFromDatabase(nameIdsBuffer.toString());
			}
	
			for (MapItem item : mapRectItems) {
				
				if (item.nameId != 0) {
	
					item.name = (String)nameCacheLocal.getElement(item.nameId);
				}
			}
		}
		
		return mapRect;
	}
	
	public void getNamesFromDatabase(String ids) {
		
		try {
			
			Class.forName("android.database.sqlite.SQLiteDatabase");
			
			this.sqlDb = SQLiteDatabase.openDatabase(DatabaseInfo.DB_URL, null, SQLiteDatabase.OPEN_READONLY);

			String query = "SELECT id, name FROM " + DatabaseInfo.TABLE_NAME + " WHERE id IN (" + ids + ")"; 
			Cursor c = this.sqlDb.rawQuery(query, null);
	    	int rowCount = c.getCount();
	    	c.moveToFirst();

	    	Cache nameCacheLocal = this.nameCache;
	    	
	    	while(!c.isAfterLast()) {
	    		
	    		Integer id   = c.getInt(0);
	    		String  name = c.getString(1);
	    		nameCacheLocal.addElement(id, name);
	    		c.moveToNext();
	    	}

	    	c.close();
	    	this.sqlDb.close();
		}
		catch (Exception e) {
			Log.e(logTag, e.getMessage());
		}
	}

	public void clearTileCache(){
		
		this.tileCache.clear();
	}

}
