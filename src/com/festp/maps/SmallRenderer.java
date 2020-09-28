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
		Integer mainId = MapUtils.getMapId(player.getInventory().getItemInMainHand());
		if (mainId == null || mainId != view.getId())
		{
			Integer offId = MapUtils.getMapId(player.getInventory().getItemInOffHand());
			if (offId == null || offId != view.getId())
				return;
		}
		//canvas.setCursors(null);new MapCursorCollection()
		int playerX = player.getLocation().getBlockX();
		int playerZ = player.getLocation().getBlockZ();
		
		int scale = map.getScale();
		int width = 128 / scale;
		int minX = map.getX();
		int minZ = map.getZ();
		for (int x = 0; x < width; x++)
		{
			// top block pseudorender
			PaletteUtils.getColor(view.getWorld(), minX + x, minZ - 1, lastColorBlock);
			for (int z = 0; z < width; z++)
			{
				int realX = minX + x,
					realZ = minZ + z;
				int distX = playerX - realX,
					distZ = playerZ - realZ;
				if (distX * distX + distZ * distZ > RENDER_DISTANCE_SQUARED + 1)
					continue;
				// brighter block, darker under block
				int lastY = lastColorBlock.getY();
				byte color = PaletteUtils.getColor(view.getWorld(), realX, realZ, lastColorBlock);
				
				if (distX * distX + distZ * distZ > RENDER_DISTANCE_SQUARED)
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
					if (lastY < y)
						color += 1; // brighter
					else if (lastY > y)
						color -= 1; // darker
				}
				
				int pxX = x * scale;
				int pxZ = z * scale;
				for (int dx = 0; dx < scale; dx++)
					for (int dz = 0; dz < scale; dz++)
						canvas.setPixel(pxX + dx, pxZ + dz, color);
			}
		}
	}
}
