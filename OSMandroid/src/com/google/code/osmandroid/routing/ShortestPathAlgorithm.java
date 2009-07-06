package com.google.code.osmandroid.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import android.util.Log;

public class ShortestPathAlgorithm {

	
    private final DirectedGraph osmGraph;
    
    private final HashMap<Long, Integer> shortestDistances = new HashMap<Long, Integer>(4096);
    
    private final HashSet<Long>       settledNodes = new HashSet<Long>(4096);
    private final HashMap<Long, Long> predecessors = new HashMap<Long, Long>(4096);

	public  static final int INFINITE_DISTANCE = Integer.MAX_VALUE;

    private final Comparator<Long> shortestDistanceComparator =
    	new Comparator<Long>() {
        	public int compare(Long left, Long right) {
        		
            // note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
            int result = (int)(getShortestDistance(left) - getShortestDistance(right));
            
            return (result == 0) ? (int)(left-right) : result;
        }
    };
    	
 
    private final TreeSet<Long> unsettledNodes = new TreeSet<Long>(shortestDistanceComparator);
 
    public ShortestPathAlgorithm(DirectedGraph osmGraph) {
        this.osmGraph     = osmGraph;
    }
    
    private void init(Long start) {

    	this.settledNodes.clear();
    	this.unsettledNodes.clear();
        
    	this.shortestDistances.clear();
    	this.predecessors.clear();
        
        setShortestDistance(start, 0);
        this.unsettledNodes.add(start);
    }

    public void execute(long start, long destination) {
    	
        init(start);
  
        // the current node
        Long node;
        TreeSet<Long> unsetledNodeLocal = this.unsettledNodes;
        HashSet<Long> settledNodesLocal = this.settledNodes;
        
        // extract the node with the shortest distance
        while ((node = unsetledNodeLocal.first()) != null) {
        	
        	unsetledNodeLocal.remove(node);
        	
            // destination reached, stop
            if (node.longValue() == destination) 
            	break;
            
            settledNodesLocal.add(node);
        
            relaxNeighbors(node);
        }

    }

    private void relaxNeighbors(Long u) {

    	DirectedGraph graph = this.osmGraph;
    	
    	ArrayList<Long> destinations = graph.getDestinations(u);
    	
    	if (destinations == null)
    		return;
    	
    	for (Long dest : destinations) {
    		
            // skip node already settled
            if (isSettled(dest)) 
            	continue;

            int shortDist = getShortestDistance(u) + graph.getDistance(u, dest);

            if (shortDist < getShortestDistance(dest)) {
            	// assign new shortest distance and mark unsettled
                setShortestDistance(dest, shortDist);
            	
        		// assign predecessor in shortest path
                setPredecessor(dest, u);

            }
        }        

    }
    
    private boolean isSettled(Long v) {
        return this.settledNodes.contains(v);
    }
    
    public Integer getShortestDistance(Long node) {
    	
        Integer d = this.shortestDistances.get(node);
        return (d == null) ? INFINITE_DISTANCE : d;
    }

    private void setShortestDistance(Long node, Integer weight) {

    	this.unsettledNodes.remove(node);
        
    	this.shortestDistances.put(node, weight);
        
    	this.unsettledNodes.add(node);        
    }

    public long getPredecessor(Long node) {
    	
        return this.predecessors.get(node);
    }
    
    private void setPredecessor(Long a, Long b) {
    	
    	this.predecessors.put(a, b);
    }

}
