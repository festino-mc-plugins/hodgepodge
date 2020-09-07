package com.festp.maps;

import com.festp.utils.Vector3i;

public class DrawingInfo {
	
	public int scale;
	public int xCenter, yCenter, zCenter;
	public Position state;
	
	public DrawingInfo(int scale, Vector3i blockCenter, Position state) {
		this.scale = scale;
		this.xCenter = blockCenter.getX();
		this.yCenter = blockCenter.getY();
		this.zCenter = blockCenter.getZ();
		this.state = state;
	}

	public DrawingInfo(Integer scale, Integer xCenter, Integer yCenter, Integer zCenter, Position state) {
		this.scale = scale;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.zCenter = zCenter;
		this.state = state;
	}
}
