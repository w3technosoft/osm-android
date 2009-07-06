package com.google.code.osmandroidconverter.main;

import java.util.*;

public class TileComparator implements Comparator {
	
	public int compare(Object arg0, Object arg1) {
		
		String tile1 = (String)arg0;
		String tile2 = (String)arg1;
		
		if(tile1.equals("index"))
			return -1;

		if(tile2.equals("index"))
			return 1;
		
		
		if(tile1.length() > tile2.length()) {
			return 1;
		} else if(tile1.length() == tile2.length()) {
			
			if(tile1.equals(tile2))
				return 0;
			
			String[] st = new String[2];
			st[0] = tile1;
			st[1] = tile2;
	  	    java.util.Arrays.sort(st);
	  	    if(st[0].equals(tile1))
	  	    	return -1;
	  	    else 
	  	    	return 1;
		} else {
			return -1;
		}
	}
}
