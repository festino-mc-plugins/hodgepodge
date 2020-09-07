package com.festp.maps;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import com.festp.maps.PaletteUtils;

public class DrawingRenderer extends AbstractRenderer {
	
	static final int RENDER_DISTANCE_SQUARED = 128 * 128;
	static final int SHADES_COUNT = PaletteUtils.SHADES_COUNT;

	final DrawingMap map;
	
	public DrawingRenderer(DrawingMap map) {
		super(map);
		this.map = map;
	}

	@Override
	public void renderSpecific(MapView view, MapCanvas canvas, Player player) {
		Integer main_id = SmallMapManager.getMapId(player.getInventory().getItemInMainHand());
		if (main_id == null || main_id != view.getId())
		{
			Integer off_id = SmallMapManager.getMapId(player.getInventory().getItemInOffHand());
			if (off_id == null || off_id != view.getId())
				return;
		}

		int player_x = player.getLocation().getBlockX();
		int player_z = player.getLocation().getBlockZ();
		
		int scale = map.getScale();
		int width = 128 / scale;
		int xCenter = map.getX();
		int yCenter = map.getY();
		int zCenter = map.getZ();
		Position pos = map.getState();
		Block b = null;
		// TODO x, YYY, z
		int xStep = 1;
		int yStep = -1;
		int zStep = 1;
		for (int x = 0; x < width; x += xStep)
		{
			for (int z = 0; z < width; z += zStep)
			{
				// TODO update rework 
				int real_x = xCenter + x - width / 2,
					real_z = zCenter + z - width / 2;
				int x_dist = player_x - real_x,
					z_dist = player_z - real_z;
				if (x_dist * x_dist + z_dist * z_dist > RENDER_DISTANCE_SQUARED)
					continue;
				
				byte color = 2;
				int yScale;
				for (yScale = 1; yScale <= SHADES_COUNT; yScale++) {
					b = view.getWorld().getBlockAt(real_x, yCenter + yStep * yScale, real_z);
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
				
				int px_x = x * scale;
				int px_z = z * scale;
				for (int dx = 0; dx < scale; dx++)
					for (int dz = 0; dz < scale; dz++)
						canvas.setPixel(px_x + dx, px_z + dz, color);
			}
		}
	}
	
}
