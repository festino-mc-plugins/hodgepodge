package com.festp.maps;

import org.bukkit.Location;

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
	
	public static DrawingInfo buildFrom(Location loc) {
		int xCenter = (int) Math.round(loc.getX()),
			yCenter = (int) Math.round(loc.getY()),
			zCenter = (int) Math.round(loc.getZ());
		Position state = Position.get(loc);
		if (state.isUp() || state.isVertical()) {
			yCenter += 1;
		}
		
		float yaw = loc.getYaw() - 45;
		double yawFrom45 = yaw - 90 * Math.floor(yaw / 90);
		int scale = 1;
		if (yawFrom45 < 45 - 20) {
			scale = 8;
		} else if (yawFrom45 < 45) {
			scale = 4;
		} else if (yawFrom45 < 45 + 20) {
			scale = 2;
		}
		return new DrawingInfo(scale, xCenter, yCenter, zCenter, state);
	}
}
