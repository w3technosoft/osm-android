package com.google.code.osmandroid.routing;

import com.google.code.osmandroid.mapdata.MapItem;


public class GraphEdge {
	
	public MapItem item;	
	public int     weight;
	public int 	   offset;
	
	public GraphEdge() {
		
	}
	
	public GraphEdge(MapItem item, int weight, int offset) {
		this.item   = item;
		this.weight = weight;
		this.offset = offset;

	}

}
