package com.festp.maps;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;

public class SmallRenderer extends AbstractRenderer {
	
	static final int RENDER_DISTANCE_SQUARED = 128 * 128;

	private BlockContainer lastColorBlock = new BlockContainer();
	
	final SmallMap map;
	
	public SmallRenderer(SmallMap map) {
		super(map);
		this.map = map;
	}

	@Override
	protected void renderSpecific(MapView view, MapCanvas canvas, Player player) {
		Integer main_id = SmallMapManager.getMapId(player.getInventory().getItemInMainHand());
		if (main_id == null || main_id != view.getId())
		{
			Integer off_id = SmallMapManager.getMapId(player.getInventory().getItemInOffHand());
			if (off_id == null || off_id != view.getId())
				return;
		}
		//canvas.setCursors(null);new MapCursorCollection()
		int player_x = player.getLocation().getBlockX();
		int player_z = player.getLocation().getBlockZ();
		
		int scale = map.getScale();
		int blocks = 128 / scale;
		int min_x = map.getX();
		int min_z = map.getZ();
		for (int x = 0; x < blocks; x++)
		{
			// top block pseudorender
			PaletteUtils.getColor(view.getWorld(), min_x + x, min_z - 1, lastColorBlock);
			for (int z = 0; z < blocks; z++)
			{
				int real_x = min_x + x,
					real_z = min_z + z;
				int x_dist = player_x - real_x,
					z_dist = player_z - real_z;
				if (x_dist * x_dist + z_dist * z_dist > RENDER_DISTANCE_SQUARED + 1)
					continue;
				// brighter block, darker under block
				int last_y = lastColorBlock.getY();
				byte color = PaletteUtils.getColor(view.getWorld(), real_x, real_z, lastColorBlock);
				
				if (x_dist * x_dist + z_dist * z_dist > RENDER_DISTANCE_SQUARED)
					continue;
				
				if (color == PaletteUtils.getColor(PaletteUtils.WATER))
				{
					// count water blocks
					Block b = lastColorBlock.get();
					int depth = 0;
					while (b.getType() == Material.WATER && b.getY() > 0) {
						depth++;
						b = b.getRelative(BlockFace.DOWN);
					}
					if (depth < 3)
						color += 1; // brighter
					else if (depth > 6)
						color -= 1; // darker
					//some ununderstandable specific
				}
				else
				{
					int y = lastColorBlock.getY();
					if (last_y < y)
						color += 1; // brighter
					else if (last_y > y)
						color -= 1; // darker
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
