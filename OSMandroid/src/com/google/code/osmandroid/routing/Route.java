package com.google.code.osmandroid.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import com.google.code.osmandroid.engine.MapEngine;
import com.google.code.osmandroid.engine.MapRect;
import com.google.code.osmandroid.mapdata.BoundingBox;
import com.google.code.osmandroid.mapdata.MapItem;
import com.google.code.osmandroid.mapdata.MapPresets;

import android.provider.Settings.System;
import android.util.Log;

public class Route {

	private RoutePoint destPoint;
	private RoutePoint startPoint;
		
	public static double length;
	
	private DirectedGraph osmGraph;
	private MapEngine     mapEngine;
	
	private static int[] streetIds = {MapPresets.STREET_MOTORWAY, 
		  MapPresets.STREET_MOTORWAY_LINK,
		  MapPresets.STREET_TRUNK,    MapPresets.STREET_TRUNK_LINK,
		  MapPresets.STREET_PRIMARY,  MapPresets.STREET_SECONDARY,
		  MapPresets.STREET_TERTIARY, MapPresets.STREET_RESIDENTIAL,
		  MapPresets.STREET_SERVICE,  MapPresets.STREET_UNCLASSIFIED};
	
	private int routeType;
	
	private static final int ALL_STREETS_ZOOM_LEVEL  	  = 14;
	private static final int MAIN_STREETS_ZOOM_LEVEL 	  = 10;
	private static final int NEAREST_STREET_SEARCH_RADIUS = 65536;
	private static final int NEAREST_STREET_MAX_ITERATIONS = 4;
	
	private static final int LOCAL_AREA_DISTANCE     = (int)(4.0 * 65536);
	private static final int LOCAL_AREA_EXTRA_RADIUS = (int)(0.5 * 65536);
	
	private static final int ROUTE_POINT_GRAPH_RADIUS = (int)(2.0 * 65536);
	
	public static final int ROUTE_SHORTEST = 1;
	public static final int ROUTE_FASTEST  = 2;
	
	public Route(MapEngine mapEngine){
		
		this.mapEngine = mapEngine;
		this.osmGraph  = new DirectedGraph();
		this.routeType = Route.ROUTE_FASTEST;
		
		Arrays.sort(streetIds);
	}
	
	public void setRouteType(int routeType) {
		
		this.routeType = routeType;
	}
	
	public RoutePoint getNearestRoutePoint(int x, int y) {
		
		RoutePoint point = null;
		
		boolean ready = false;
		int iteration = 1;
		while (!ready && iteration <= NEAREST_STREET_MAX_ITERATIONS) {
			
			BoundingBox box = BoundingBox.getBoxFromCoords(x, y, iteration * NEAREST_STREET_SEARCH_RADIUS);
			MapRect mapRect = this.mapEngine.getMapRect(box, ALL_STREETS_ZOOM_LEVEL, false);
			iteration++;
			
			point = findNearestItem(mapRect, null, x, y, true);
			if (point != null && point.item != null) {
				ready = true;
			}
		}
		
		return point;
	}
	
	public void setStartPoint(int x, int y){
		
		this.startPoint = getNearestRoutePoint(x, y);		
	}
	
	public void setDestPoint(int x, int y){
		
		this.destPoint = getNearestRoutePoint(x, y);
	}

	public void buildRouteGraph() {
		
		
		int startX = this.startPoint.item.minX;
		int startY = this.startPoint.item.minY;
		int destX  = this.destPoint.item.minX;
		int destY  = this.destPoint.item.minY;
		
		this.osmGraph.clearGraph();
		
    	long t1 = Math.abs((long)startX - destX);
    	long t2 = Math.abs((long)startY - destY);    	
		int dist = (int)Math.sqrt(t1 * t1 + t2 * t2);
		
		if (dist <= LOCAL_AREA_DISTANCE) {
			
    		BoundingBox oneRectangle = BoundingBox.getBoxFromCoords(startX, 
    				startY, destX, destY, LOCAL_AREA_EXTRA_RADIUS);
    		
    		MapRect mapRect = this.mapEngine.getMapRect(oneRectangle, ALL_STREETS_ZOOM_LEVEL, false);
    		
    		addToGraph(mapRect);
		}
		else {

        	BoundingBox startBox = BoundingBox.getBoxFromCoords(startX, 
        										startY, ROUTE_POINT_GRAPH_RADIUS);
        	
        	// build a bounding box centered on the destination point with 2 km radius
        	BoundingBox destBox  = BoundingBox.getBoxFromCoords(destX, 
        										destY, ROUTE_POINT_GRAPH_RADIUS);
        	
        	// build a bounding box containing the start and destination point + some extra length
        	BoundingBox wholeBox = BoundingBox.getBoxFromCoords(startX, 
        			startY, destX, destY, (int)(dist / 5 ));
        	
        	MapRect mapRect;
        	
        	mapRect = this.mapEngine.getMapRect(startBox, ALL_STREETS_ZOOM_LEVEL, false);
        	addToGraph(mapRect);
        	
        	mapRect  = this.mapEngine.getMapRect(destBox,  ALL_STREETS_ZOOM_LEVEL, false);
        	addToGraph(mapRect);
        	
        	mapRect = this.mapEngine.getMapRect(wholeBox, MAIN_STREETS_ZOOM_LEVEL, false);
        	addToGraph(mapRect);
        	
        	mapRect = null;
		}
		
		// split the edge containing the start/destination point
		splitEdgeAtRoutePoint(this.startPoint);
		splitEdgeAtRoutePoint(this.destPoint);
	}
	
	public ArrayList<Long> calculateShortestPath() {
		
		long start, end;
		
		long startNodeID = ((long)this.startPoint.streetX << 32) + this.startPoint.streetY;
		long endNodeID  = ((long)this.destPoint.streetX << 32) + this.destPoint.streetY;
		
		
		ShortestPathAlgorithm engine = new ShortestPathAlgorithm(this.osmGraph);
		engine.execute(startNodeID, endNodeID);
		
	   	ArrayList<Long> tmp = new ArrayList<Long>();
	   	int numNodes = 0;
    	try{ 
    		for (long node = endNodeID; node != startNodeID; node = engine.getPredecessor(node)) {
    			tmp.add(node);
    			numNodes++;
   	 		}
    		tmp.add(startNodeID);
    		numNodes++;
    	}
    	catch (Exception e) {
    		return null;
    	}
    	Collections.reverse(tmp);
    	
    	return tmp;
	}
	
	public LinkedList<MapItem> getRouteMapItems(ArrayList<Long> routePoints) {
		
		LinkedList<MapItem> routeMapItems = new LinkedList<MapItem>();
		length = 0;
		
    	for (int i = 0; i< routePoints.size()-1; i++) {
    		
    		Long from = routePoints.get(i);
    		Long to   = routePoints.get(i+1);
    		
    		GraphNode fromNode = this.osmGraph.getVertex(from);
    		GraphEdge edge     = fromNode.getEdge(to);
    		
  		
    		//this is a whole segment of a map item
    		if (edge.item.type == MapPresets.ROUTE_SUBITEM) {
    			routeMapItems.add(edge.item);
    			length += ((double)edge.weight / getWeightRatio(edge.item.type));
    		}
    		else {
    			int offset          = edge.offset;
    			int startSegmentPos = edge.item.getStartPosition(offset);
    			int endSegmentPos   = edge.item.getEndPosition(offset);	
    			int numNodes        = (endSegmentPos - startSegmentPos)/2 + 1;
    			
    			MapItem routeItem  = new MapItem();
    			routeItem.numNodes = numNodes;
    			routeItem.nodes    = new int[numNodes * 2];
    			routeItem.type	   = MapPresets.ROUTE;
    			
    			int iters = numNodes * 2;
    			for (int nodeIdx = 0; nodeIdx < iters; nodeIdx ++) { 
    				routeItem.nodes[nodeIdx] = edge.item.nodes[startSegmentPos + nodeIdx];
    			}
    			
    			routeItem.name  = edge.item.name;
    			routeItem.minX  = edge.item.minX;
    			routeItem.minY  = edge.item.minY;
    			routeItem.maxX  = edge.item.maxX;
    			routeItem.maxY  = edge.item.maxY;
    			routeItem.flags = edge.item.flags;
    			routeItem.numSegments = 1;
    			
    			length += ((double)edge.weight / getWeightRatio(edge.item.type));
    			
    			routeMapItems.add(routeItem);
    		}

    	}
		
    	return routeMapItems;
		
	}
	
	public void addToGraph(MapRect mapRect) {
		
		ArrayList<MapItem> items = mapRect.getMapItems();
		DirectedGraph graph      = this.osmGraph;
		
		for (MapItem item : items) {
						
			if (Arrays.binarySearch(streetIds, item.type) < 0) {
				continue;
			}
			
			int   x1, y1, x2, y2;
			int   length      	 = 0;
			int   startSegmentPos= 0;
			int   endSegmentPos	 = 0;
			int   numSegments	 = item.numSegments;
			int   numNodes	 	 = item.numNodes;
			int[] nodes      	 = item.nodes;
			int[] segments    	 = item.segments;
			long  startNodeID 	 = 0;
			long  endNodeID   	 = 0;
			
			for (int offset = 1; offset <= numSegments; offset++) {
				
				startSegmentPos = item.getStartPosition(offset);
				endSegmentPos   = item.getEndPosition(offset);
				
				x1 = nodes[startSegmentPos];
				y1 = nodes[startSegmentPos + 1];
				
				x2 = nodes[endSegmentPos];
				y2 = nodes[endSegmentPos + 1];
				
				startNodeID  = ((long)x1 << 32) + y1;
				endNodeID    = ((long)x2 << 32) + y2;
				
				length = 0;
				for (int i = startSegmentPos; i <= endSegmentPos - 2; i+=2) {
					
			    	long t1 = (long)(nodes[i]   - nodes[i+2]) * (nodes[i]   - nodes[i+2]);
			    	long t2 = (long)(nodes[i+1] - nodes[i+3]) * (nodes[i+1] - nodes[i+3]);
			    	
					int dist = (int)Math.sqrt(t1 + t2);
					length += dist;
				}

				//length = (int)(item.weightRatio * length); 
				GraphEdge edge = new GraphEdge(item, length, offset);

						
				// if there already is a direct route beetween the two points
				// we'll need to split one egde to avoid building a multigraph
				if (graph.areNeighbours(startNodeID, endNodeID)) {
					
					// if the edge is the same, ignore it
					GraphNode startNode    = graph.getVertex(startNodeID);
					GraphEdge existingEdge = startNode.getEdge(endNodeID);
					
					if (edge.item.id == existingEdge.item.id && edge.offset == existingEdge.offset)
						continue;
					
					// if the current element is composed only of two nodes,
					// we'll have to split the existing edge
					if((endSegmentPos - startSegmentPos) == 2) {
						splitEdge(startNodeID, endNodeID);
						graph.addDirectRoute(startNodeID, endNodeID, edge);
						if (!item.isOneWay()) {
							graph.addDirectRoute(endNodeID, startNodeID, edge);
						}
					}
					// if the current node has more than 2 nodes, we'll
					// insert it in the graph overwrtting the previous edge
					// we'll spli the newly created edge and then reinsert 
					// the orignial edge
					else {
						GraphEdge oldEdge = graph.getVertex(startNodeID).getEdge(endNodeID);
						
						graph.addDirectRoute(startNodeID, endNodeID, edge);
						if (!item.isOneWay()) {
							graph.addDirectRoute(endNodeID, startNodeID, edge);
						}
						splitEdge(startNodeID, endNodeID);
						
						graph.addDirectRoute(startNodeID, endNodeID, oldEdge);
						if (!oldEdge.item.isOneWay()) {
							graph.addDirectRoute(endNodeID, startNodeID, oldEdge);
						}
					}
				}
				else {
					graph.addDirectRoute(startNodeID, endNodeID, edge);

					if (!item.isOneWay()) {
						graph.addDirectRoute(endNodeID, startNodeID, edge);
					}
				}

			}
			
		}//while loop
		
	}
	
	public void splitEdge(long startNodeID, long endNodeID) {
		
		DirectedGraph graph = this.osmGraph;
		
		GraphNode startNode = graph.getVertex(startNodeID);
		if (startNode == null) {
			throw new RuntimeException("Invalid start node " + startNodeID );
		}
		
		GraphEdge oldEdge = startNode.getEdge(endNodeID);
		
		MapItem item = oldEdge.item;
		if (item == null) {
			throw new NullPointerException();
		}
		
		int offset  = oldEdge.offset;
		int[] nodes = item.nodes;
		
		int startSegmentPos = item.getStartPosition(offset);
		int endSegmentPos   = item.getEndPosition(offset);
		int midPos   = startSegmentPos + (endSegmentPos - startSegmentPos) / 2;
		if ((midPos % 2) == 1)
			midPos--;
		
		int length1 = 0;
		for (int i = startSegmentPos; i <= midPos - 2; i+=2) {
			
	    	long t1 = (long)(nodes[i]   - nodes[i+2]) * (nodes[i]   - nodes[i+2]);
	    	long t2 = (long)(nodes[i+1] - nodes[i+3]) * (nodes[i+1] - nodes[i+3]);
	    	
			int dist = (int)Math.sqrt(t1 + t2);
			length1 += dist;
		}
		
		float weightRatio = getWeightRatio(item.type); 
		
		length1 = (int)(weightRatio * length1); 

		int length2 = 0;
		for (int i = midPos; i <= endSegmentPos - 2; i+=2) {
			
	    	long t1 = (long)(nodes[i]   - nodes[i+2]) * (nodes[i]   - nodes[i+2]);
	    	long t2 = (long)(nodes[i+1] - nodes[i+3]) * (nodes[i+1] - nodes[i+3]);
	    	
			int dist = (int)Math.sqrt(t1 + t2);
			length2 += dist;
		}		
		
		length2 = (int)(weightRatio * length2); 
		
	
		int x0 = nodes[midPos];
		int y0 = nodes[midPos + 1];
		long midNodeID = ((long)x0 << 32) + y0;
		
		//remove the original edge(s)
		graph.deleteRoute(startNodeID, endNodeID);
		if (item.isOneWay() == false) {
			graph.deleteRoute(endNodeID, startNodeID);
		}
		
		//add the 2(4) new edges
		GraphEdge edge1 = new GraphEdge(item, length1, offset);
		graph.addDirectRoute(startNodeID, midNodeID, edge1);
		if (item.isOneWay() == false) {
			graph.addDirectRoute(midNodeID, startNodeID, edge1);
		}

		GraphEdge edge2 = new GraphEdge(item, length2, offset);
		graph.addDirectRoute(midNodeID, endNodeID, edge2);
		if (item.isOneWay() == false) {
			graph.addDirectRoute(endNodeID, midNodeID, edge2);
		}
	}

	public void splitEdgeAtRoutePoint(RoutePoint point) {
		
		GraphEdge edge1, edge2;
		int offset, pos;
		long startNodeID, endNodeID, midNodeID;
		int startSegmentPos, endSegmentPos;
		int[] nodes;
		int end;
		
		//split the segment containing the start point
		offset   = point.offset;
		nodes    = point.item.nodes;
		startSegmentPos = point.item.getStartPosition(offset);
    	endSegmentPos   = point.item.getEndPosition(offset);
    	
    	startNodeID = ((long)nodes[startSegmentPos] << 32) + nodes[startSegmentPos + 1];
    	midNodeID   = ((long)point.streetX << 32)   + point.streetY;
    	endNodeID   = ((long)nodes[endSegmentPos] << 32)  + nodes[endSegmentPos + 1];
    	
    	MapItem item1     = new MapItem();
    	item1.type        = MapPresets.ROUTE_SUBITEM;
    	item1.flags       = point.item.flags;
    	item1.numSegments = 1;
    	item1.numNodes    = point.pos - startSegmentPos/2 + 2;
    	item1.nodes       = new int[item1.numNodes * 2];
    	
    	item1.minX		  = Integer.MAX_VALUE;
    	item1.minY		  = Integer.MAX_VALUE;
    	item1.maxX		  = Integer.MIN_VALUE;
    	item1.maxY		  = Integer.MIN_VALUE;
    	
		pos = 0;
		end = point.pos + 1;
		for (int i = startSegmentPos/2 ; i < end; i++) {
						
 	    	item1.nodes[pos]   = point.item.nodes[i*2];
 	    	item1.nodes[pos+1] = point.item.nodes[i*2+1];
 	    	
 	    	if (item1.nodes[pos] < item1.minX)
 	    		item1.minX = item1.nodes[pos];
 	    	
 	    	if (item1.nodes[pos] > item1.maxX)
 	    		item1.maxX = item1.nodes[pos];
 	    	
 	    	if (item1.nodes[pos+1] < item1.minY)
 	    		item1.minY = item1.nodes[pos+1];
 	    	if (item1.nodes[pos+1] > item1.maxY)
 	    		item1.maxY = item1.nodes[pos+1];
 	    	pos+=2;
    	}
		item1.nodes[pos]   = point.streetX;
		item1.nodes[pos+1] = point.streetY;
    	if (item1.nodes[pos] < item1.minX)
    		item1.minX = item1.nodes[pos];
    	
    	if (item1.nodes[pos] > item1.maxX)
    		item1.maxX = item1.nodes[pos];
    	
    	if (item1.nodes[pos+1] < item1.minY)
    		item1.minY = item1.nodes[pos+1];
    	if (item1.nodes[pos+1] > item1.maxY)
    		item1.maxY = item1.nodes[pos+1];		
		
    	
    	MapItem item2     = new MapItem();
    	item2.type        = MapPresets.ROUTE_SUBITEM;
    	item2.flags       = point.item.flags;
    	item2.numSegments = 1;
    	item2.numNodes    = endSegmentPos/2 - point.pos + 1;
    	item2.nodes       = new int[item2.numNodes * 2];
    	
		item2.nodes[0]    = point.streetX;
		item2.nodes[1]    = point.streetY;
		
    	item2.minX		  = item2.nodes[0];
    	item2.maxX		  = item2.nodes[0];
    	item2.minY		  = item2.nodes[1];
    	item2.maxY		  = item2.nodes[1];
    	
		pos = 2;
		end = endSegmentPos/2+1;
		for (int i = point.pos + 1; i < end; i++) {
 	    	item2.nodes[pos]   = point.item.nodes[i*2];
 	    	item2.nodes[pos+1] = point.item.nodes[i*2+1];
 	    	
 	    	if (item2.nodes[pos] < item2.minX)
 	    		item2.minX = item2.nodes[pos];
 	    	if (item2.nodes[pos] > item2.maxX)
 	    		item2.maxX = item2.nodes[pos];
 	    	
 	    	if (item2.nodes[pos+1] < item2.minY)
 	    		item2.minY = item2.nodes[pos+1];
 	    	if (item2.nodes[pos+1] > item2.maxY)
 	    		item2.maxY = item2.nodes[pos+1];
 	    	
 	    	pos+=2;
    	}
		
		float weightRatio = getWeightRatio(point.item.type);
		
		edge1 = new GraphEdge(item1, (int)(point.lenFromStart * weightRatio), offset);
		edge2 = new GraphEdge(item2, (int)(point.lenToEnd * weightRatio), offset);
		
		if (edge1.weight > 0 && edge2.weight > 0) {
		
			this.osmGraph.deleteRoute(startNodeID, endNodeID);
			if (!point.item.isOneWay()){
				this.osmGraph.deleteRoute(endNodeID, startNodeID);
			}
			
			this.osmGraph.addDirectRoute(startNodeID, midNodeID, edge1);
			this.osmGraph.addDirectRoute(midNodeID, endNodeID, edge2);
			if (!point.item.isOneWay()){
				this.osmGraph.addDirectRoute(midNodeID, startNodeID, edge1);
				this.osmGraph.addDirectRoute(endNodeID, midNodeID, edge2);
			}

		}
	}
	
	public float getWeightRatio(int itemType){
		
		if (routeType == Route.ROUTE_SHORTEST) {
			return 1.0f;
		}
		
		switch (itemType) {
			
			case MapPresets.STREET_MOTORWAY:
			case MapPresets.STREET_MOTORWAY_LINK:
				return 0.5f;

			case MapPresets.STREET_TRUNK:
			case MapPresets.STREET_TRUNK_LINK:
				return 0.7f;

			case MapPresets.STREET_PRIMARY:
				return 1.0f;
		
			case MapPresets.STREET_SECONDARY:
				return 2.0f;
		
			case MapPresets.STREET_TERTIARY:
				return 3.0f;
		
			case MapPresets.STREET_RESIDENTIAL:
				return 5.0f;
		
			default:
				return 10f;
		}		
	}
	
	
	

	public RoutePoint findNearestItem(MapRect mapRect, BoundingBox area, int worldX, int worldY, boolean streetsOnly) {

		RoutePoint rp = new RoutePoint();
		
		ArrayList<MapItem> items = mapRect.getMapItems();
		
		float    minDistance = Float.MAX_VALUE;
		MapItem  minItem     = null;
		int      minOffset   = 0;

	    for (MapItem item : items) {

	        // if only streets are required and this item is not a street, skip it
			if (streetsOnly && Arrays.binarySearch(this.streetIds, item.type) < 0) {
				continue;
			}
	        
	        /* if we need elements from a specific area of this mapRect, check to see if
	           the current item is included in this area */
	        if (area != null && !area.overlaps(item)) {
	            continue;
	        }
	        
	        // do not inspect non-map elements
			if(item.numNodes < 2 || item.type > 20000)
				continue;

			int offset          = 0;			
			int startSegmentPos = 0;
			int endSegmentPos   = 0;
			int numSegs         = item.numSegments;

	        // start looping through the item's segments
	        for (int segmentIdx = 0; segmentIdx < numSegs; segmentIdx++) {

			    offset++;
			    startSegmentPos = item.getStartPosition(offset);
			    endSegmentPos   = item.getEndPosition(offset);

				/* iterate through every line segment and find out the 
				   nearest point to worldX worldY coordinates */
				int count   = endSegmentPos -2;
				for (int nodeIdx = startSegmentPos; nodeIdx <= count; nodeIdx += 2) {
					
					long x1 ,y1, x2, y2;
					long itemX, itemY;
					
					x1 = item.nodes[nodeIdx];
					y1 = item.nodes[nodeIdx + 1];
					
					x2 = item.nodes[nodeIdx + 2];
					y2 = item.nodes[nodeIdx + 3];
					
					long A = worldX - x1;
					long B = worldY - y1;
					long C = x2 - x1;
					long D = y2 - y1;

					long dot = A * C + B * D;
					long len_sq = C * C + D * D;
					double param = (double)dot / (double)len_sq;
					
					if(param < 0) {
					    itemX = x1;
					    itemY = y1;
					}
					else if(param > 1) {
					    itemX = x2;
					    itemY = y2;
					}
					else {
					    itemX = (int)(x1 + param * C);
					    itemY = (int)(y1 + param * D);
					}
					
					int dist = (int)Math.sqrt((worldX - itemX) * (worldX - itemX) 
										+ (worldY - itemY) * (worldY - itemY));
					
					if (dist < minDistance) {
						
						minDistance = dist;
						minItem     = item;
						minOffset   = offset;
						
						rp.actualX = worldX;
						rp.actualY = worldY;
						rp.streetX = (int)itemX;
						rp.streetY = (int)itemY;
						rp.pos	   = nodeIdx/2;
						rp.item    = item;
						rp.offset  = offset;

						rp.lenFromStart = (int)Math.sqrt((double)(x1-rp.streetX)*(x1-rp.streetX) + (double)(y1-rp.streetY)*(y1-rp.streetY));
						rp.lenToEnd     = (int)Math.sqrt((double)(x2-rp.streetX)*(x2-rp.streetX) + (double)(y2-rp.streetY)*(y2-rp.streetY));
						
						int len, t;
						len = 0;
						t   = rp.pos * 2;
						for (int i = startSegmentPos; i < t; i+=2) {
							long t1 = (long)item.nodes[i] - item.nodes[i+2];
							long t2 = (long)item.nodes[i+1] - item.nodes[i+3];
							len += (int)Math.sqrt(t1*t1 + t2*t2);
						}
						rp.lenFromStart += len;

						len = 0;
						t   = (rp.pos+1) * 2;
						for (int i = t; i <= count; i+=2) {
							long t1 = (long)item.nodes[i] - item.nodes[i+2];
							long t2 = (long)item.nodes[i+1] - item.nodes[i+3];
							len += (int)Math.sqrt(t1*t1 + t2*t2);
						}
						rp.lenToEnd += len;
						
						rp.lenExtra     = (int)dist;
					} //mindistance
					
	            }//node loop
	        }//segment loop
	    }

	    return rp;
	}

}
