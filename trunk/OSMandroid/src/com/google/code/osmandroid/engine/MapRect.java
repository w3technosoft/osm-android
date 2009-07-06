package com.google.code.osmandroid.engine;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.MapItem;


public class MapRect extends BoundingBox {

	public ArrayList<MapItem> items;
	public static final int INITIAL_SIZE = 256;
	
	public MapRect() {
		
		this.items = new ArrayList<MapItem>(INITIAL_SIZE);
	}
	
	public MapRect(BoundingBox area) {
		
		super(area);
		this.items = new ArrayList<MapItem>(INITIAL_SIZE);
	}
	
}
