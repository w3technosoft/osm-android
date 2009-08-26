package com.google.code.osmandroidconverter.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.google.code.osmandroidconverter.mapdata.City;
import com.google.code.osmandroidconverter.mapdata.Mercator;
import com.google.code.osmandroidconverter.mapdata.OsmElement;
import com.google.code.osmandroidconverter.mapdata.OsmNode;


public class OsmContentHandler implements ContentHandler {

	private OsmNode 	currNode;
	private OsmElement  currElement;
	
	private HashMap    			   nodeMap;
	private HashMap    			   cityMap;
	private LinkedList<OsmElement> elements;
	private LinkedList<String> 	   excludedAttributes;
	
	private static final int MAX_TILE_DEPTH = 17;
	
	public OsmContentHandler() {
		
		this.nodeMap  = MapBuilder.nodeMap;
		this.cityMap  = MapBuilder.cityMap;
		this.elements = MapBuilder.osmElements;
		
		this.excludedAttributes = new LinkedList<String>();
		this.excludedAttributes.add("created_by");
		this.excludedAttributes.add("user");
		this.excludedAttributes.add("timestamp");
		this.excludedAttributes.add("visible");
		
	}
	
	public void startNode(Attributes atts) {

		long  id  = Long.parseLong(atts.getValue("id"));
		float lat = Float.parseFloat(atts.getValue("lat"));
		float lon = Float.parseFloat(atts.getValue("lon"));
		
		int mercX = (int)((Mercator.mercX(lon)/1000)*(1<<16));
		int mercY = (int)((Mercator.mercY(lat)/1000)*(1<<16));
		
		this.currNode = new OsmNode(id, mercX, mercY);
	}
	
	public void endNode() {
		
		this.nodeMap.put(currNode.id, currNode);
		
		String place = currNode.getAttribute("place");
		if (place != null && (place.equals("city") || 
				place.equals("village") || place.equals("town"))) {
			
			String name = currNode.getAttribute("name");
			if (name == null)
				return;
			
			this.cityMap.put(name, new City(this.currNode.x, this.currNode.y, place));
			
			OsmElement element = new OsmElement(this.currNode.id);
			element.addAttribute("place", place);
			element.addAttribute("name", name);
			element.addNodeRef(this.currNode.id);
			
			this.elements.add(element);
		}
		
		currNode = null;
	}
	
	public void startWay(Attributes atts) {

		long id = Long.parseLong(atts.getValue("id"));
		this.currElement = new OsmElement(id);
	}

	public void endWay() {
		
		if (WayType.presets.get(currElement.getType()) != null) {
				this.elements.add(currElement);
		}
		
		currElement = null;
	}
	
	public void startNd(Attributes atts) {

		long nodeRef = Long.parseLong(atts.getValue("ref"));
		
		OsmNode nd = (OsmNode) nodeMap.get(nodeRef);
		if (nd != null) {
			nd.belongsToWay();
		}
		
		currElement.addNodeRef(nodeRef);
	}
	
	public void endNd() {

	}
	
	public void startTag(Attributes atts) {

		String key = atts.getValue("k");
		String value = atts.getValue("v");

		/* don't waste memory for this attributes */
		if (this.excludedAttributes.contains(key))
			return;

		/* current processed element is a node */
		if (this.currNode != null) {
			this.currNode.addAttribute(key, value);
		}
		/* current processed element is a way */
		else if (this.currElement != null) {
			this.currElement.addAttribute(key, value);
		}
	}
	
	public void endTag() {
		
	}
	
	public void startBounds(Attributes atts) {
		
		int minY = (int)((Mercator.mercY(Double.parseDouble(atts.getValue("minlat")))/1000) * 65536);
		int minX = (int)((Mercator.mercX(Double.parseDouble(atts.getValue("minlon")))/1000) * 65536);
		int maxY = (int)((Mercator.mercY(Double.parseDouble(atts.getValue("maxlat")))/1000) * 65536);
		int maxX = (int)((Mercator.mercX(Double.parseDouble(atts.getValue("maxlon")))/1000) * 65536);
		
		MapBuilder.mapRegion.minX = minX;
		MapBuilder.mapRegion.minY = minY;
		MapBuilder.mapRegion.maxX = maxX;
		MapBuilder.mapRegion.maxY = maxY;

	}
	
	/* ContentHandler interface */

	public void startElement(String namespaceURI, String localName,
			String rawName, Attributes atts) throws SAXException {

		if (localName.equals("node")) {
			startNode(atts);
		} 
		else if (localName.equals("way")) {
			startWay(atts);
		} 
		else if (localName.equals("nd")) {
			startNd(atts);
		} 
		else if (localName.equals("tag")) {
			startTag(atts);
		} 
		else if (localName.equals("bounds")) {
			startBounds(atts);
		}
	}

	public void endElement(String namespaceURI, String localName, String rawName) {
		
		if (localName.equals("node")) {
			endNode();
		} 
		else if (localName.equals("way")) {
			endWay();
		} 
		else if (localName.equals("nd")) {
			endNd();
		} 
		else if (localName.equals("tag")) {
			endTag();
		} 
	}

	public void characters(char[] ch, int start, int end) throws SAXException {

	}

	public void ignorableWhitespace(char[] ch, int start, int end)
			throws SAXException {

	}

	public void skippedEntity(String name) throws SAXException {

	}

	public void setDocumentLocator(Locator locator) {

	}

	public void startDocument() throws SAXException {

	}

	public void endDocument() throws SAXException {

	}

	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	public void startPrefixMapping(String prefix, String uri) {

	}

	public void endPrefixMapping(String prefix) {

	}
}
