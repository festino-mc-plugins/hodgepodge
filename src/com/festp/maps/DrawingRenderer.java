package com.festp.maps;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import com.festp.maps.PaletteUtils;
import com.festp.utils.Vector3i;

public class DrawingRenderer extends AbstractRenderer {
	
	static final int RENDER_DISTANCE_SQUARED = /*6 * 6; */ 128 * 128;
	static final int SHADES_COUNT = PaletteUtils.SHADES_COUNT;

	final DrawingMap map;
	
	public DrawingRenderer(DrawingMap map) {
		super(map);
		this.map = map;
	}

	@Override
	public void renderSpecific(MapView view, MapCanvas canvas, Player player) {
		Integer main_id = MapUtils.getMapId(player.getInventory().getItemInMainHand());
		if (main_id == null || main_id != view.getId())
		{
			Integer off_id = MapUtils.getMapId(player.getInventory().getItemInOffHand());
			if (off_id == null || off_id != view.getId())
				return;
		}

		int playerX = player.getLocation().getBlockX();
		int playerZ = player.getLocation().getBlockZ();
		
		int xCenter = map.getX();
		int yCenter = map.getY();
		int zCenter = map.getZ();
		int scale = map.getScale();
		int width = 128 / scale;
		int halfWidth = width / 2;
		Position pos = map.getState();
		DrawingMapCoordinator coords = new DrawingMapCoordinator(pos, width);
		//System.out.print(xCenter+" "+yCenter+" "+zCenter+" "+pos+" => "+coords.getWorldCoord(halfWidth, halfWidth, 1));
		Block b = null;
		// TODO update rework (rays? render fov?)
		for (int x = 0; x < width; x++)
		{
			for (int z = 0; z < width; z++)
			{
				// TODO update rework (rays? render fov?)
				int d1 = x - halfWidth,
					d2 = z - halfWidth;
				int dist1 = playerX - xCenter + d1,
					dist2 = playerZ - zCenter + d2;
				if (dist1 * dist1 + dist2 * dist2 > RENDER_DISTANCE_SQUARED)
					continue;
				
				byte color = 2;
				int yScale;
				for (yScale = 1; yScale <= SHADES_COUNT; yScale++) {
					Vector3i offsets = coords.getWorldCoord(x, z, yScale);
					int realX = xCenter + offsets.getX();
					int realY = yCenter + offsets.getY();
					int realZ = zCenter + offsets.getZ();
					b = view.getWorld().getBlockAt(realX, realY, realZ);
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
