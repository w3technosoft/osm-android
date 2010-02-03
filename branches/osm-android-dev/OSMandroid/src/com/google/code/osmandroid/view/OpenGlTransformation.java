package com.google.code.osmandroid.view;

public class OpenGlTransformation {

	public int windowWidth;
	public int windowHeight;
	
	public int screenToWorldX(int screenMinX, int screenMaxX, int x) {
    	
		long minX = screenMinX;
		long maxX = screenMaxX;
		
		long scx = (minX + (maxX - minX) * x / windowWidth);
		
		return (int)scx;		
	}

	public int screenToWorldY(int screenMinY, int screenMaxY, int y) {
		
		long minY = screenMinY;
		long maxY = screenMaxY;
		
		y = windowHeight - y;
		
		long scy = (minY + (maxY - minY) * y / windowHeight);
		
		return (int)scy;

	}
	
	public int worldToScreenX(int worldMinX, int worldMaxX, int x) {
		
		long minX = worldMinX;
		long maxX = worldMaxX;
		
		x -= worldMinX;
		
		long screenX = ((long)x * windowWidth) / (maxX - minX);
		
		return (int)screenX;
	}

	public int worldToScreenY(int worldMinY, int worldMaxY, int y) {

		long minY = worldMinY;
		long maxY = worldMaxY;
		
		y -= worldMinY;
		
		long scx = ((long)y * windowHeight) / (maxY - minY);
		
		return (int)scx;

	}
}
