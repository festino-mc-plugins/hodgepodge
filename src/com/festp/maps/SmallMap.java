package com.festp.maps;

public class SmallMap implements IMap {
	private int id;
	private int scale;
	private int startX, startZ;
	
	public SmallMap(int id, int scale, int start_x, int start_z)
	{
		this.id = id;
		this.scale = scale;
		this.startX = start_x;
		this.startZ = start_z;
	}
	
	public int getId() {
		return id;
	}
	
	public int getScale() {
		return scale;
	}
	
	public int getX() {
		return startX;
	}
	
	public int getZ() {
		return startZ;
	}
}
