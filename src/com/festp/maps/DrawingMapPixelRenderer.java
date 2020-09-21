package com.festp.maps;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.map.MapCanvas;

import com.festp.utils.Vector3i;

public class DrawingMapPixelRenderer {
	private static final int SHADES_COUNT = PaletteUtils.SHADES_COUNT;
	
	final MapCanvas canvas;
	final DrawingMap map;
	final boolean[][] discovered;
	final int scale;
	final int xCenter, yCenter, zCenter;
	final World world;
	final DrawingMapCoordinator coords;
	
	public DrawingMapPixelRenderer(MapCanvas canvas, DrawingMap map, DrawingMapCoordinator coords) {
		this.canvas = canvas;
		this.world = canvas.getMapView().getWorld();
		this.map = map;
		this.discovered = map.getDicovered();
		this.scale = map.getScale();
		this.xCenter = map.getX();
		this.yCenter = map.getY();
		this.zCenter = map.getZ();
		this.coords = coords;
	}

	public boolean tryRender(int mapX, int mapY) {
		if (!map.isFullDicovered()) {
			if (!discovered[mapX][mapY]) {
				return false;
			}
		}
		
		byte color = 2;
		int yScale;
		for (yScale = 1; yScale <= SHADES_COUNT; yScale++) {
			Vector3i offsets = coords.getWorldCoord(mapX, mapY, yScale);
			int realX = xCenter + offsets.getX();
			int realY = yCenter + offsets.getY();
			int realZ = zCenter + offsets.getZ();
			Block b = world.getBlockAt(realX, realY, realZ);
			color = PaletteUtils.getColor(b);
			if (color >= SHADES_COUNT || color < 0) {
				break;
			}
		}
		
		// -1 dark / +1 brightest / +2 darkest
		if (yScale < SHADES_COUNT) {
			color += 2 - (byte) yScale; // 1 => 1, 2 => 0, 3 => -1
		} else if (yScale == SHADES_COUNT) {
			color += 2;
		}
		
		int px_x = mapX * scale;
		int px_z = mapY * scale;
		for (int dx = 0; dx < scale; dx++)
			for (int dz = 0; dz < scale; dz++)
				canvas.setPixel(px_x + dx, px_z + dz, color);
		return true;
	}
}
