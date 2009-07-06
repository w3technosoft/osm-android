package com.google.code.osmandroidconverter.mapdata;

import java.util.LinkedList;

public class Tile {

	private String name;
	private LinkedList<MapItem> mapItems = new LinkedList<MapItem>();
	
	public Tile(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	} 
	
	
	public void addItem(MapItem item){
		mapItems.add(item);
	}
	
	public void addMapItems(LinkedList<MapItem> collection, int size){
		mapItems.addAll(collection);
	}
	
	public LinkedList<MapItem> getMapItems(){
		return mapItems;
	}
	
	public int size() {
		
		int size = 0;
		
		for (MapItem item : this.mapItems) {
		
			size += 8 + 4 + 4 + 4 +4; // id, nameId, type, flags, numNodes
			size += (item.numNodes * 2) * 4;
			size += 4; //numSegments
			if (item.numSegments !=1 ) {
				size += item.numSegments * 4;
			}
		}
		
		return size;
	}
}
