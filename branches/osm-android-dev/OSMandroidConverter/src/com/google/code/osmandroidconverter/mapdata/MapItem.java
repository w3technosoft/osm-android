package com.google.code.osmandroidconverter.mapdata;



public class MapItem extends BoundingBox {

	public long   id;		  // id of the item
	public int    nameId; 	  // id of the item
	public String name; 	  // temporary name for this item
	public int    type;		  // it's type (street, park, building, etc)
	public int	  flags; 	  // flags
	public int    numNodes;    // number of nodes
	public int[]  nodes;		  // nodes
	public int    numSegments; // number of segments
	public int[]  segments;    //segment indexes
	
	public int getLength() {
	
		if (this.numNodes == 1)
			return 0;
		
		int length = 0;
		int iters  = this.numNodes * 2 -2;
		
		for (int i = 0; i < iters; i += 2) {
			
			int x1 = this.nodes[i];
			int y1 = this.nodes[i+1];
			int x2 = this.nodes[i+2];
			int y2 = this.nodes[i+3];

	    	long t1 = (long)(x1 - x2) * (long)(x1 - x2);
	    	long t2 = (long)(y1 - y2) * (long)(y1 - y2);
	    	
			int dist = (int)Math.sqrt(t1 + t2);
	    	
			length += dist;
		}
		
		return length;
	}

}
