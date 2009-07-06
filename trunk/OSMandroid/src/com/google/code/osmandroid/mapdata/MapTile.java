package com.google.code.osmandroid.mapdata;

import java.io.DataInputStream;
import java.io.BufferedInputStream;

import java.io.File;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


import android.util.Log;

public class MapTile {

	private String              name;
	private ArrayList<MapItem> items;
	
	private static final int INITIAL_SIZE = 1000;
	
	private static final String logTag  = "MapTile";

	public MapTile(String name) {
		
		this.name     = name;
		this.items    = new ArrayList<MapItem>(INITIAL_SIZE);
	}

	public void addItem(MapItem item) {
		
		this.items.add(item);
	}

	public MapItem getItem(int pos) {
		
		return this.items.get(pos);
	}
	
	public int numItems() {
		return this.items.size();
	}
	
	public static MapTile readTile(String tile){
    	
		MapTile mapTile = new MapTile(tile);
		Log.i(logTag, "Reading " + tile);
		
		FileChannel fc;
		ByteBuffer buffer;
		
		try {
	        fc = new FileInputStream(tile).getChannel();
	        buffer = ByteBuffer.allocate((int)fc.size());
	       
	        fc.read(buffer);
	        buffer.rewind();
	        
	        while(true) {

				MapItem item  = new MapItem();
				item.id		  = buffer.getLong();
				item.nameId	  = buffer.getInt();
				item.type	  = buffer.getInt();
				item.flags	  = buffer.getInt();
				item.minX	  = buffer.getInt();
				item.maxX	  = buffer.getInt();
				item.minY	  = buffer.getInt();
				item.maxY	  = buffer.getInt();					
				item.numNodes = buffer.getInt();
				
				int numValues    = item.numNodes * 2;
				item.nodes		 = new int[numValues];
				
				int [] itemNodes = item.nodes;
				for (int i = 0; i< numValues; i++) {
					itemNodes[i]   = buffer.getInt();
   				}
				
				item.numSegments  = buffer.getInt();
				
				int   numSegments  = item.numSegments;
				
				if (numSegments != 1) {
					
					item.segments = new int[numSegments];
					int[] itemSegments = item.segments;
					
					for (int i = 0; i< numSegments; i++) {
						itemSegments[i] = buffer.getInt();
       				}
				}
				
				mapTile.addItem(item);	        	
	        }

    	}
    	catch (Exception e){   		
    		 
    	}
    	
    	fc = null;
    	buffer = null;
    	
    	return mapTile;
	}
	


}
