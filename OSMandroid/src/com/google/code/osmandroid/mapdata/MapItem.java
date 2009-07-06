package com.google.code.osmandroid.mapdata;


public class MapItem extends BoundingBox{

	public long   id;		  
	public int    nameId;  
	public int    type;		 
	public int	  flags; 	  
	public int    numNodes;
	public int[]  nodes;
	public int    numSegments;
	public int[]  segments;
	public String name;
	
	public static final int SHAPE_POINT   = 1;
	public static final int SHAPE_POLYGON = 2;
	public static final int SHAPE_LINE    = 3;
	

	public MapItem() {
		
	}

	public int getLength() {
		
		double   length    = 0;
		int   count  	= this.numNodes * 2 -2;
		int[] itemNodes = this.nodes;
		
		for (int i = 0; i < count; i += 2) {
			
			int x1 = itemNodes[i];
			int y1 = itemNodes[i+1];
			int x2 = itemNodes[i+2];
			int y2 = itemNodes[i+3];

	    	long t1 = (long)(x1 - x2) * (long)(x1 - x2);
	    	long t2 = (long)(y1 - y2) * (long)(y1 - y2);
	    	
			length += Math.sqrt(t1 + t2);;
		}
		
		return (int)length;
	}
	
	public int getSegmentLength(int offset) {
		
		int   itemNumSegments = this.numSegments;
		int[] itemSegments    = this.segments;
		int   itemNumNodes    = this.numNodes;
		int[] itemNodes       = this.nodes;
		
		if (offset > itemNumSegments || offset < 1) {
			throw new ArrayIndexOutOfBoundsException();
		}
		
		int startNodeIndex = 0;
		int endNodeIndex   = 0;
		
		if (offset == 1) {
			startNodeIndex = 0;
			if (itemNumSegments == 1) {
				endNodeIndex = itemNumNodes -1;
			}
			else {
				endNodeIndex = itemSegments[0];
			}
		}
		else if (offset == 2) {
			startNodeIndex = itemSegments[0];
			endNodeIndex   = itemSegments[1];
		}
		else {
			startNodeIndex = itemSegments[offset - 2];
			endNodeIndex   = itemSegments[offset - 1];
		}

		
		int length = 0;
		int iters  = endNodeIndex * 2;
		
		for (int i = startNodeIndex * 2; i < iters; i+= 2) {
			
			int x1 = itemNodes[i];
			int y1 = itemNodes[i + 1];
			int x2 = itemNodes[i + 2];
			int y2 = itemNodes[i + 3];
			
	    	long t1 = (long)(x1 - x2) * (long)(x1 - x2);
	    	long t2 = (long)(y1 - y2) * (long)(y1 - y2);
	    	
	    	length += (int)Math.sqrt(t1 + t2);
		}
		
		return length;
	}
	
	//FIXME: return the actual centroid of the element
	public Coordinates getCenter() {

		int numNodes = this.numNodes;
		int[] nodes  = this.nodes;
		
		if (numNodes == 1) {
			return new Coordinates(nodes[0], nodes[1]);
		}
		
		int centerX = (this.maxX + this.minX) / 2;
		int centerY = (this.maxY + this.minY) / 2;

		return new Coordinates(centerX, centerY);
   		
	}
	
	public int getStartPosition(int offset) {

		int startIdx;
		
		if (offset == 1) {			
			startIdx = 0;
		}
		else {
			startIdx = this.segments[offset - 2];
		}
		return 2 * startIdx;
	} 
	
	public int getEndPosition(int offset) {
		
		int   endIdx 		  = -1;
		int[] itemSegments    = this.segments;
		
		if (offset == 1) {
			if (this.numSegments == 1) {
				endIdx = this.numNodes - 1;
			}
			else {
				endIdx = itemSegments[0];
			}
		}
		else {
			endIdx = itemSegments[offset - 1];
		}
		
		return 2 * endIdx;
	}
	
	public int getShape() {
		
		int shape = this.flags & 0x0000000F;
		
		if (shape == 1) {
		
			return MapItem.SHAPE_POINT;
		}
		else if (shape == 2) {
			
			return MapItem.SHAPE_POLYGON;
		}
		else {
			
			return MapItem.SHAPE_LINE;
		}
	}
	
	public boolean isOneWay() {

		if ((this.flags & 0x00000010) == 0x00000010)
			return true;
		else
			return false;

	}

}
