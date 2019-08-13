package com.festp.maps;

public class SmallMap {
	private int id;
	private int scale;
	private int start_x, start_z;
	
	public SmallMap(int id, int scale, int start_x, int start_z)
	{
		this.id = id;
		this.scale = scale;
		this.start_x = start_x;
		this.start_z = start_z;
	}
	
	public int getId() {
		return id;
	}
	
	public int getScale() {
		return scale;
	}
	
	public int getX() {
		return start_x;
	}
	
	public int getZ() {
		return start_z;
	}
}
