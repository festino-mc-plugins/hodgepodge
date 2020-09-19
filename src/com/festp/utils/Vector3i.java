package com.festp.utils;

public class Vector3i implements Cloneable {
	
	protected int x, y, z;
	
	public Vector3i(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3i clone() {
		return new Vector3i(x, y, z);
	}
	
	public Vector3i add(int x, int y, int z) {
		return new Vector3i(this.x + x, this.y + y, this.z + z);
	}
	
	@Override
	public String toString() {
		return "Vector3i{" + x + ", " + y + ", " + z + "}";
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
	
	/** MODIFIES VECTOR */
	public Vector3i add(Vector3i vec) {
		x += vec.x;
		y += vec.y;
		z += vec.z;
		return this;
	}
	
	/** MODIFIES VECTOR */
	public Vector3i subtract(Vector3i vec) {
		x -= vec.x;
		y -= vec.y;
		z -= vec.z;
		return this;
	}
	
	/** MODIFIES VECTOR */
	public Vector3i multiply(double mult) {
		x *= mult;
		y *= mult;
		z *= mult;
		return this;
	}
	
	public Vector3i getCoordwiseMult(Vector3i v2) {
		return new Vector3i(x * v2.x, y * v2.y, z * v2.z);
	}
	
	public int getDotProduct(Vector3i v2) {
		return x * v2.x + y * v2.y + z * v2.z;
	}
}
