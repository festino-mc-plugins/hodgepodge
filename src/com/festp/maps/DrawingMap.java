package com.festp.maps;

import com.festp.utils.Vector3i;

public class DrawingMap implements IMap {
	
	private int id;
	private DrawingInfo info;
	public boolean needReset = false;
	
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
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " #" + getId() + " {x=" + getX() + ", y=" + getY() + ", z=" + getZ()
				+ ", scale=" + getScale() + ", pos=" + getDirection() + "}"; 
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
	
	public Position getDirection() {
		return info.state;
	}
	
	public int getWidth() {
		return info.getWidth();
	}
	
	public boolean isFullDicovered() {
		return info.isFullDiscovered;
	}
	
	public boolean[][] getDicovered() {
		return info.discovered;
	}
	
	public void checkDiscovering() {
		boolean isFullDiscovered = true;
		int width = getWidth();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				if (!info.discovered[x][y]) {
					isFullDiscovered = false;
					break;
				}
			}
		}
		if (isFullDiscovered) {
			info.discovered = null;
			info.isFullDiscovered = true;
			MapFileManager.save(this);
		}
	}
}
