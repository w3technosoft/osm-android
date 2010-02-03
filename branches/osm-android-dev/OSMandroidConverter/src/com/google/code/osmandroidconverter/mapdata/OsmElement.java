package com.google.code.osmandroidconverter.mapdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.code.osmandroidconverter.main.MapBuilder;
import com.google.code.osmandroidconverter.main.NameRecord;
import com.google.code.osmandroidconverter.main.Point;
import com.google.code.osmandroidconverter.main.Polygon;
import com.google.code.osmandroidconverter.main.WayType;

public class OsmElement {
	
	public long  	id;
	public HashMap  attributes;
	public List 	nodeRefs;

	
	public static final int SHAPE_POINT_MASK = 0x00000001;
	public static final int SHAPE_AREA_MASK  = 0x00000002;
	public static final int SHAPE_LINE_MASK  = 0x00000004;
	
	public static final int ONE_WAY_MASK = 0x00000010;
	public static final int REVERSE_MASK = 0x00000100;
	
	public OsmElement(long id) {
		
		this.id 		= id;
		this.attributes = new HashMap<String,String>(8);
		this.nodeRefs	= new LinkedList<Long>();
	}
	
	public void addAttribute(String key, String value){

		this.attributes.put(key, value);
	}
	
	public String getAttribute(String key){
		
		if (attributes == null)
			return null;

		return (String)attributes.get(key);		
	}
	
	public void addNodeRef(long nd){

		this.nodeRefs.add(nd);
	}
	
	public int getNumNodes() {
		
		return this.nodeRefs.size();
	}
	
	public int getNodeRef(int pos){
		return (int)(Integer)nodeRefs.get(pos);
	}
	
	public String getType(){
		
		String type;

		type = (String) this.attributes.get("place");
		if (type != null ){
			return "place" + "/" + type; 
		}

		type = (String) this.attributes.get("highway");
		if (type != null ){
			return "highway" + "/" + type; 
		}
		
		type = (String) this.attributes.get("leisure");
		if (type != null ){
			return "leisure" + "/" + type; 
		}
		
		type = (String) this.attributes.get("landuse");
		if (type != null ){
			return "landuse" + "/" + type; 
		}
		
		type = (String) this.attributes.get("natural");
		if (type != null ){
			return "natural" + "/" + type; 
		}
				
		type = (String) this.attributes.get("building");
		if (type != null ){
			return "building" + "/" + type; 
		}
		return null;
			
	}

	public MapItem convert() {
		
		MapItem item = new MapItem();
		 
		item.id 	 = this.id;
		item.nameId  = 0;
		item.type    = (int)(Integer)WayType.presets.get(this.getType()); 
		item.flags	 = 0x00000000;
		
		int numNodes = this.nodeRefs.size();
		
		long startNode = (long)(Long)this.nodeRefs.get(0);
		long endNode   = (long)(Long)this.nodeRefs.get(numNodes-1);
		
		if (numNodes == 1) {
			item.flags |= SHAPE_POINT_MASK;
		}
		else if ((numNodes > 1) && (startNode == endNode) && 
				!this.getType().contains("highway")) {
			
			item.flags |= SHAPE_AREA_MASK;
		}
		else {
			item.flags |= SHAPE_LINE_MASK;
		}
		
		String oneway = this.getAttribute("oneway");
		String junction = this.getAttribute("junction");
		if ((oneway != null && oneway.equals("yes")) || (junction != null && junction.equals("roundabout"))) {
			item.flags |= ONE_WAY_MASK;
		}
		
		item.minX = BoundingBox.WORLD_MAX_X;
		item.minY = BoundingBox.WORLD_MAX_Y;
		item.maxX = BoundingBox.WORLD_MIN_X;
		item.maxY = BoundingBox.WORLD_MIN_Y;
		
		for (int nodeIdx = 0; nodeIdx < numNodes; ++nodeIdx) {
			
			OsmNode nd = (OsmNode)MapBuilder.nodeMap.get(this.nodeRefs.get(nodeIdx));
			
			if (nd.x < item.minX)
				item.minX = nd.x;
			
			if (nd.x > item.maxX)
				item.maxX = nd.x;
			
			if (nd.y < item.minY)
				item.minY = nd.y;
			
			if (nd.y > item.maxY)
				item.maxY = nd.y;
		}
		
		int itemWidth  = Math.abs(item.maxX - item.minX);
		int itemHeight = Math.abs(item.maxY - item.minY);
		int x1, x2;
		
		x1 = MapBuilder.nodeMap.get(this.nodeRefs.get(0)).x;
		x2 = MapBuilder.nodeMap.get(this.nodeRefs.get(numNodes-1)).x;
		
		if (itemWidth > itemHeight && x2 < x1) {
			
				item.flags |= REVERSE_MASK;
		}
		else if (itemWidth < itemHeight  && x2 > x1) {
			
				item.flags |= REVERSE_MASK;
		}
		
		/*
		 * Set the nodes and segments
		 */
		if ((item.flags & SHAPE_AREA_MASK) == SHAPE_AREA_MASK) {
			
		  int num = 0;
		  if (numNodes > 3) {
			   num = numNodes-1;
		  }
		  else {
			   num = numNodes;
		  }

		  Point[] vertices = new Point[num];
		   
		  for(int j = 0; j < num; j++) {	
			  
			OsmNode nd  = (OsmNode)MapBuilder.nodeMap.get(this.nodeRefs.get(j));
			vertices[j] = new Point(nd.x, nd.y);				
		  }
		  
		  Polygon poly                = new Polygon(vertices);
		  LinkedList<Point> triangles = poly.triangulate();
		  
		  item.numNodes = triangles.size(); 
		  item.nodes    = new int[item.numNodes * 2];
		  
		  int c = 0;
		  for (int i = 0; i < triangles.size(); i++) {
				
				item.nodes[c]   = (int)triangles.get(i).x;
				item.nodes[c+1] = (int)triangles.get(i).y;
				c+=2;
		  }
		  
		  item.numSegments = 1;
		  
		}
		else {
			
			item.numNodes = numNodes;
			item.nodes    = new int[item.numNodes * 2];
			
			int c = 0;
			for (int i = 0; i < item.numNodes; i ++) {
				long nodeRef    = (long)(Long)this.nodeRefs.get(i);
				OsmNode node    = MapBuilder.nodeMap.get(nodeRef);
				item.nodes[c]   = node.x;
				item.nodes[c+1] = node.y;
				c = c + 2;
			}
			
			item.numSegments = 1;
			
			// if is a street, calculate it's segments
			if (this.getType().contains("highway")) {
				
				ArrayList<Integer> segs = new ArrayList<Integer>(32); 
				int numSegs  = 0;
				int last     = 0;
				int nodeIdx  = 0;
				
				for(nodeIdx = 0; nodeIdx < numNodes; ++nodeIdx) {
					
					long nodeRef = (long)(Long)this.nodeRefs.get(nodeIdx);
					OsmNode nd   = (OsmNode)MapBuilder.nodeMap.get(nodeRef);
					
					if(nodeIdx != 0 && nd.atIntersection() && nodeIdx != numNodes-1) {
						
						segs.add(nodeIdx);
						numSegs++;
						last = nodeIdx;					
					}				
				}
				
				segs.add(nodeIdx-1);
				numSegs++;

				item.numSegments = numSegs;
				if (numSegs != 1) {
					
					item.segments = new int[numSegs];
					for (int i = 0; i < numSegs; i++) {
						item.segments[i] = (Integer)segs.get(i); 
					}
				}

			}
		}
		
		item.name = this.getAttribute("name");
		
		
		return item;
	}
	

}
