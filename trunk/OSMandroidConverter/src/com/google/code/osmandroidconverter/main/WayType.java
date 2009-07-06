package com.google.code.osmandroidconverter.main;


import java.util.*;

public class WayType {
	
	public static Map presets = new HashMap<String,Integer>();
	
	static {
		presets.put("highway/motorway",      6001);
		presets.put("highway/motorway_link", 6002);
		presets.put("place/city", 			 6003);
		
		presets.put("highway/trunk",         7001);
		presets.put("highway/trunk_link",  	 7002);
		
		presets.put("highway/primary",       8001);
		
		presets.put("natural/water",         9001);
		presets.put("place/town", 			 9002);
		
		presets.put("highway/secondary",     10001);
		presets.put("place/village", 		 10002);
		
		presets.put("landuse/industrial",    12001);
			
		
		presets.put("highway/tertiary",     13001);
		presets.put("leisure/park",        	13002);
		presets.put("leisure/stadium",     	13003);
		presets.put("landuse/cemetery",    	13004);		
		
		presets.put("highway/residential",  14001);
		presets.put("highway/service",      14002);
		presets.put("highway/unclassified", 14003);
	}
	
}
