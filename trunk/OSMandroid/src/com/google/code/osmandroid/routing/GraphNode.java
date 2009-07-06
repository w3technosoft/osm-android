package com.google.code.osmandroid.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class GraphNode {

	private long id;
	
	private ArrayList<Long> 	 neighbours;
	private ArrayList<GraphEdge> edges;
	
	private static final int NEIGHBOURS_SIZE = 5;
	
	public GraphNode(long id) {
		
		this.id    = id;
		
		this.neighbours = new ArrayList<Long>(NEIGHBOURS_SIZE);
		this.edges = new ArrayList<GraphEdge>(NEIGHBOURS_SIZE);
	}

	public long getId(){
		return this.id;
	}
	
	public void addEgde(long dest, GraphEdge edge){

		this.neighbours.add(dest);
		this.edges.add(edge);
	}

	public void deleteEdge(long dest){
		
		int pos = this.neighbours.indexOf(dest);
		this.neighbours.remove(pos);
		this.edges.remove(pos);
	}
	
	public boolean hasEdge(long dest) {
		
		if (this.neighbours.indexOf(dest) < 0)
			return false;
		
		return true;
	}
	
	public GraphEdge getEdge(long dest){

		int pos = this.neighbours.indexOf(dest);
		return this.edges.get(pos);
	}
	
	public int getEdgeWeight(long dest){

		int pos = this.neighbours.indexOf(dest);
		return this.edges.get(pos).weight;

	}
	
	public int compareTo(GraphNode v){
		
		return (int)(this.id-v.getId());
	}	
	
	public ArrayList<Long> getNeighbours(){
		
		return this.neighbours;
	}

}
