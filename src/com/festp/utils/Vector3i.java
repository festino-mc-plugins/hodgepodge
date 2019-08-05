package com.festp.utils;

public class Vector3i {
	
	protected int x, y, z;
	
	public Vector3i(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}

	public void setX(int new_x) {
		x = new_x;
	}

	public void setY(int new_y) {
		y = new_y;
	}

	public void setZ(int new_z) {
		z = new_z;
	}
}
