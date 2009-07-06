package com.google.code.osmandroid.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.util.Log;


public class DirectedGraph {

	private Map<Long, GraphNode> nodes;
	

	public DirectedGraph() {
		this.nodes  = new HashMap<Long, GraphNode>();
	}
	
	public void addDirectRoute(Long start, Long end, GraphEdge edge) {
		
		GraphNode startNode = (GraphNode)this.nodes.get(start);
		
		if (startNode != null) {
 
			startNode.addEgde(end, edge);
		}
		else {
			
			startNode = new GraphNode(start);
			startNode.addEgde(end, edge);
			this.nodes.put(start, startNode);
		}

		GraphNode endNode = (GraphNode)nodes.get(end);
		if(endNode == null)
		{
			endNode = new GraphNode(end);
			this.nodes.put(end, endNode);
		}

	}
	
	public void deleteRoute(Long start, Long end) {
		GraphNode node = (GraphNode)this.nodes.get(start);
		node.deleteEdge(end);
	}
	
	public int getDistance(Long start, Long end){
		GraphNode node = this.nodes.get(start);
		return (int)node.getEdgeWeight(end);
	}

	public ArrayList<Long> getDestinations(Long nodeId){
		
		GraphNode node = this.nodes.get(nodeId);
		
		return node.getNeighbours();		
	}
	
	public GraphNode getVertex(Long nodeId){
		GraphNode node = this.nodes.get(nodeId);

		return node;
	}

	public boolean areNeighbours(Long start, Long end) {
		

		GraphNode node = this.nodes.get(start);
		if (node == null)
			return false;
		
		ArrayList<Long> destinations = getDestinations(start);
		if (destinations != null) {
			
			for (Long dest : destinations) {
				if (end == dest) {
					return true;
				}
			}
		}

		return false;
	}
	
	public void clearGraph() {
		this.nodes.clear();
	}

	public int getNumNodes(){
		return this.nodes.size();
	}
}
