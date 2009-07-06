package com.google.code.osmandroidconverter.mapdata;

import java.util.HashMap;

public class OsmNode extends Coordinates{

	public long id = 0;
	
	public HashMap<String, String> attributes;
	
	private int atIntersection = 0;
	
	
	public OsmNode(long id, int x, int y){
		
		this.id = id;
		super.x = x;
		super.y = y;
	}
	
	public void belongsToWay() {
		
		/* this node is part of multiple streets
		 * (at an intersection)
		 */
		this.atIntersection++;
	}
	
	public boolean atIntersection() {
		
		if (atIntersection > 1)
			return true;
		
		return false;
	}
	
	public void addAttribute(String key, String value){

		if (this.attributes == null)
			this.attributes = new HashMap<String,String>();
		
		this.attributes.put(key, value);
	} 
	
	public String getAttribute(String key){
		
		if (this.attributes == null)
			return null;
		
		return (String)this.attributes.get(key);
	}
}
