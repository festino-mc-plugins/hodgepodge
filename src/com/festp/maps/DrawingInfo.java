package com.festp.maps;

import org.bukkit.Location;

import com.festp.utils.Vector3i;

public class DrawingInfo {
	public static final int MAX_WIDTH = 128;
	
	public int scale;
	public int xCenter, yCenter, zCenter;
	public Position state;
	public boolean isFullDiscovered;
	public boolean[][] discovered;

	public DrawingInfo(int scale, Vector3i blockCenter, Position state) {
		this(scale, blockCenter.getX(), blockCenter.getY(), blockCenter.getZ(), state);
	}
	
	public DrawingInfo(Integer scale, Integer xCenter, Integer yCenter, Integer zCenter, Position state) {
		this.scale = scale;
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		this.zCenter = zCenter;
		this.state = state;

		this.isFullDiscovered = false;
		int width = getWidth();
		this.discovered = new boolean[width][width];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < width; y++) {
				this.discovered[x][y] = false;
			}
		}
	}
	
	public int getWidth() {
		return 128 / scale;
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
