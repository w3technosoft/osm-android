package com.google.code.osmandroid.mapdata;

public class BoundingBox {

	public int minX;
	public int minY;
	public int maxX;
	public int maxY;

	public static final int WORLD_MIN_X = -1310720000;
	public static final int WORLD_MIN_Y = -1310720000;
	public static final int WORLD_MAX_X = 1310720000;
	public static final int WORLD_MAX_Y = 1310720000;
	
	public BoundingBox() {
		
		this.minX = 0;
		this.minY = 0;
		this.maxX = 0;
		this.maxY = 0;
	}

	public BoundingBox(BoundingBox bbox) {
		
		this.minX = bbox.minX;
		this.minY = bbox.minY;
		this.maxX = bbox.maxX;
		this.maxY = bbox.maxY;
	}

	public BoundingBox(int minX, int maxX, int minY, int maxY) {
		
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public boolean overlaps(BoundingBox box){
		
		return overlaps(box.minX, box.minY, box.maxX, box.maxY);		
	}
	
	public boolean overlaps(int minX, int minY, int maxX, int maxY) {
		
		if(maxX <= this.minX || minX >= this.maxX)
			return false;

		if(maxY <= this.minY || minY >= this.maxY)
			return false;

		return true;
	}
	
	public static BoundingBox getBoxFromTileName(String name) {
		
		BoundingBox box  = new BoundingBox();
		BoundingBox world = new BoundingBox(BoundingBox.WORLD_MIN_X,BoundingBox.WORLD_MAX_X, BoundingBox.WORLD_MIN_Y, BoundingBox.WORLD_MAX_Y); 
				
		int minX = world.minX;
		int maxX = world.maxX;
		int minY = world.minY;
		int maxY = world.maxY;
		
		if (name.equals("index")) {
			return world;
		}
		
		for (int i = 0; i< name.length(); ++i) {
			
			char ch = name.charAt(i);
			
			if (ch == 'a'){
				maxX = (minX + maxX)/2;
				minY = (minY + maxY)/2;
			}
			else if (ch == 'b'){
				minX = (minX + maxX)/2;
				minY = (minY + maxY)/2;				
			}
			else if (ch == 'c'){
				maxX = (minX + maxX)/2;
				maxY = (minY + maxY)/2;
			}
			else if (ch == 'd'){
				minX = (minX + maxX)/2;
				maxY = (minY + maxY)/2;				
			}
  
		}

		box.minX = minX;
		box.maxX = maxX;
		box.minY = minY;
		box.maxY = maxY;
		
		return box;
	}
	
	public static BoundingBox getBoxFromCoords(int pointX, int pointY, int radius) {
    	
    	int minX, minY, maxX, maxY;
    	
    	minX = maxX = pointX;
    	minY = maxY = pointY;
    	
    	minX -= radius;
    	maxX += radius;
    	minY -= radius;
    	maxY += radius;
    	
    	return new BoundingBox(minX, maxX, minY, maxY);
    }
    
    public static BoundingBox getBoxFromCoords(int x1, int y1, int x2, int y2, int extraLen) {
    	
    	int minX, minY, maxX, maxY;
    	
    	if(x1 <= x2) {
    		minX = x1;
    		maxX = x2;
    	}
    	else {
    		minX = x2;
    		maxX = x1;    		
    	}

    	if(y1 <= y2) {
    		minY = y1;
    		maxY = y2;
    	}
    	else {
    		minY = y2;
    		maxY = y1;    		
    	}
    	
    	minX -= extraLen;
    	maxX += extraLen;
    	minY -= extraLen;
    	maxY += extraLen;
    	
    	return new BoundingBox(minX, maxX, minY, maxY);
    }

    public boolean equals(Object object) {
    	
        if (object instanceof BoundingBox) {
            BoundingBox bbox = (BoundingBox)object;
            return (this.minX == bbox.minX) && (this.maxX == bbox.maxX) &&
            			(this.minY == bbox.minY) && (this.maxY == bbox.maxY);
        }
        return false;
    }
    
	public String toString() {
		
		return "x: (" + this.minX + " " + this.maxX + "), y: (" + this.minY + " " + this.maxY + ")";
	}

}
