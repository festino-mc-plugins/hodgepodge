package com.festp.maps;

import com.festp.utils.Vector3i;

public class DrawingMap implements IMap {
	
	private int id;
	private DrawingInfo info;
	
	public DrawingMap(int id, int scale, int xCenter, int yCenter, int zCenter, Position pos)
	{
		this.id = id;
		this.info = new DrawingInfo(scale, new Vector3i(xCenter, yCenter, zCenter), pos);
	}
	
	public DrawingMap(int id, DrawingInfo info)
	{
		this.id = id;
		this.info = info;
	}
	
	public void setInfo(DrawingInfo info) {
		this.info = info;
	}
	
	public int getId() {
		return id;
	}
	
	public int getScale() {
		return info.scale;
	}
	
	public int getX() {
		return info.xCenter;
	}
	
	public int getY() {
		return info.yCenter;
	}
	
	public int getZ() {
		return info.zCenter;
	}
	
	public Position getState() {
		return info.state;
	}
}
