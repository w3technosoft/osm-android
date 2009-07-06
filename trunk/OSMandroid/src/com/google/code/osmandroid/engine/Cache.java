package com.google.code.osmandroid.engine;

import java.util.*;

import android.util.Log;

public class Cache {

	
	private int cacheSize;
	private int numEntries;
	private int index;
	private HashMap<Object, Object> table;
	private CacheElement[] cache;
	
	public Cache(int size) {
		
		this.cacheSize = size;
		this.table = new HashMap<Object, Object>(size);
		this.cache = new CacheElement[size];
		for (int i = 0; i < size; i++) {
			this.cache[i] = new CacheElement();
		}
	}
	
	public synchronized Object getElement(Object key) {

		Object obj;

		obj = this.table.get(key);

		if (obj != null) {
			
			CacheElement element = (CacheElement) obj;
			element.setHitCount(element.getHitCount() + 1);
			return element.getObjectValue();
		}
		
		return null;
	}

	public synchronized boolean containsElement(Object key){
		
		if(this.table.containsKey(key))
			return true;
		
		return false;
	}
	
	public final synchronized void addElement(Object key, Object value) {

		Object obj;
		
		obj = this.table.get(key);

		if (obj != null) {
			
			CacheElement element;

			// Just replace the value.
			element = (CacheElement) obj;
			element.setObjectValue(value);
			element.setObjectKey(key);

			return;
		}

		if (!isFull()) {

			this.index = this.numEntries;
			++this.numEntries;
		} 
		else {
			CacheElement element = removeLfuElement();
			this.index = element.getIndex();
			this.table.remove(element.getObjectKey());
		}
		
		this.cache[this.index].setObjectValue(value);
		this.cache[this.index].setObjectKey(key);
		this.cache[this.index].setIndex(this.index);
		this.table.put(key, this.cache[this.index]);
	}

	public CacheElement removeLfuElement() {

		CacheElement[] elements = getElementsFromTable();
		CacheElement leastElement = leastHit(elements);
		return leastElement;
	}

	public static CacheElement leastHit(CacheElement[] elements) {

		CacheElement lowestElement = null;
		
		for (int i = 0; i < elements.length; i++) {
			
			CacheElement element = elements[i];
			
			if (lowestElement == null) {
				lowestElement = element;
			} 
			else {
				if (element.getHitCount() < lowestElement.getHitCount()) {
					lowestElement = element;
				}
			}
		}
		
		return lowestElement;
	}

	public void clear(){

		this.numEntries = 0;
		this.table 		= new HashMap<Object, Object>(this.cacheSize);
		this.cache 		= new CacheElement[this.cacheSize];
		
		for (int i = 0; i < this.cacheSize; i++) {
			this.cache[i] = new CacheElement();
		}
	}
	
	public CacheElement[] getElementsFromTable() {
		
		CacheElement[] array = new CacheElement[this.numEntries];
		this.table.values().toArray(array);
		return array;	
	}
	
	private boolean isFull() {
		
		if (this.numEntries == this.cacheSize) {
			return true;
		}
		
		return false;
	}
}
