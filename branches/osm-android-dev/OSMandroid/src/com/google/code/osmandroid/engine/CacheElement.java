package com.google.code.osmandroid.engine;

public class CacheElement {

	private Object 	objectValue;
	private Object 	objectKey;
	private int 	index;
	private int 	hitCount;

	public void setObjectKey(Object key) {
		this.objectKey = key;
	}
	
	public void setObjectValue(Object value) {
		this.objectValue = value;
	}
	
	public Object getObjectValue() {
		return objectValue;
	}
	
	public Object getObjectKey() {
		return objectKey;
	}
	
	public int getHitCount() {
		return hitCount;
	}
	
	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
}