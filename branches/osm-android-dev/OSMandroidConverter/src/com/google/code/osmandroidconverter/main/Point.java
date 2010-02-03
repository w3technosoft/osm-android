package com.google.code.osmandroidconverter.main;

public class Point {

	public double x;
	public double y;
	public int type;
	
	public static final int POINT_CONVEX  = 1;
	public static final int POINT_CONCAVE = 2;
	
	public Point(double x, double y) {
		
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object obj) {
		
		Point p = (Point)obj;
		if (p.x == this.x && p.y == this.y && p.type == this.type) {
			
			return true;
		}
		else {
			return false;	
		}
		
	}
	
	public String toString(){
		
		return "("+ this.x +", "+ this.y +")";
	}
}
