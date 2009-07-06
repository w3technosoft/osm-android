package com.google.code.osmandroid.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import com.google.code.osmandroid.mapdata.BoundingBox;

import android.util.Log;

public class Map extends BoundingBox {

	public String name;
	public String path;
	
	//FIXME: temporary solution
	public String[] 	 tileNames;
	public BoundingBox[] tileBBoxes; 
	
	public Map(String name, String path) {
		
		super(new BoundingBox(BoundingBox.WORLD_MIN_X, BoundingBox.WORLD_MAX_X, BoundingBox.WORLD_MIN_Y, BoundingBox.WORLD_MAX_Y));
		
		this.name = name;
		this.path = path;
		
		init();
	}
	

	private void init() {
		
		File folder        = new File(this.path);
        File[] listOfFiles = folder.listFiles();
        
        if (listOfFiles == null)
        	return;
        
        int numFiles       = listOfFiles.length;
       
        this.tileNames  = new String[numFiles];
        this.tileBBoxes = new BoundingBox[numFiles];
        
        String[]      names = this.tileNames;
        BoundingBox[] boxes = this.tileBBoxes;
        
        for (int i = 0; i < numFiles; i++) {

        	if (!listOfFiles[i].isFile()) 
            	continue;
            
        	names[i] = listOfFiles[i].getName();
        	boxes[i] = BoundingBox.getBoxFromTileName(names[i]);
        }
		
	}

	public void setBoundingBox(int minX, int maxX, int minY, int maxY) {
		
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

}
