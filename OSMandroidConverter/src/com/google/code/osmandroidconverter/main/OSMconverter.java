package com.google.code.osmandroidconverter.main;

import org.apache.log4j.BasicConfigurator;


public class OSMconverter {

	private static final String sourceFile  = "osmdata/map.osm";
	private static final String tilesDir    = "osm_android_maps";
	private static final String namesFile	= "osm_names/names.txt";
	
	public static void main(String[] args) {
		
		BasicConfigurator.configure();

		MapBuilder mapBuilder = new MapBuilder();
		mapBuilder.parseOsmFile(sourceFile);
		mapBuilder.buildTiles();
		mapBuilder.mergeTiles();
		try {
			mapBuilder.writeTiles(tilesDir);
			mapBuilder.writeNameRecords(namesFile);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
