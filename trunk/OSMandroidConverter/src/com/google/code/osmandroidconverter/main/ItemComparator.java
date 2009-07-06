package com.google.code.osmandroidconverter.main;

import java.util.*;

import com.google.code.osmandroidconverter.mapdata.MapItem;

public class ItemComparator implements Comparator{

	public int compare(Object arg0, Object arg1) {
		
		MapItem item1 = (MapItem)arg0;
		MapItem item2 = (MapItem)arg1;
		
		int type1 = item1.type;
		int type2 = item2.type;
		
		if(type1 > type2) {
			return 1;
		} else if(type1 == type2) {
			return 0;
		} else {
			return -1;
		}
	}
}
