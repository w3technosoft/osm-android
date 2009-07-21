package com.google.code.osmandroidconverter.mapdata;

public class City extends Coordinates{
	
	public String type;
	
	public City() {
		
	}
	
	public City(int x, int y, String type) {
		super.x  = x;
		super.y  = y;
		this.type = type;
	}
}
