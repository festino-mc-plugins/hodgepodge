package com.festp.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import com.festp.maps.PaletteUtils;
import com.festp.utils.Vector3i;

public class DrawingRenderer extends AbstractRenderer {
	
	static final int RENDER_DISTANCE_SQUARED = 0 * 0;
	static final int RAYS_DISTANCE_SQUARED = 100 * 100;
	static final int SHADES_COUNT = PaletteUtils.SHADES_COUNT;

	final DrawingMap map;
	
	public DrawingRenderer(DrawingMap map) {
		super(map);
		this.map = map;
	}

	@Override
	public void renderSpecific(MapView view, MapCanvas canvas, Player player) {
		if (map.needReset) {
			for (int x = 0; x < 128; x++)
				for (int y = 0; y < 128; y++)
					canvas.setPixel(x, y, (byte) 0);
			map.needReset = false;
		}
		
		Integer main_id = MapUtils.getMapId(player.getInventory().getItemInMainHand());
		if (main_id == null || main_id != view.getId())
		{
			Integer off_id = MapUtils.getMapId(player.getInventory().getItemInOffHand());
			if (off_id == null || off_id != view.getId())
				return;
		}

		Location playerLoc = player.getLocation();
		int playerX = playerLoc.getBlockX();
		int playerY = playerLoc.getBlockY();
		int playerZ = playerLoc.getBlockZ();
		
		final int xCenter = map.getX();
		final int yCenter = map.getY();
		final int zCenter = map.getZ();
		final Vector3i center = new Vector3i(xCenter, yCenter, zCenter);
		final int scale = map.getScale();
		final int width = map.getWidth();
		final int halfWidth = width / 2;
		Position pos = map.getDirection();
		World world = view.getWorld();
		DrawingMapCoordinator coords = new DrawingMapCoordinator(pos, width);

		Vector3i renderDir = coords.getWorldCoord(0, 0, 1).subtract(coords.getWorldCoord(0, 0, 0));
		Vector3i projection = new Vector3i(playerX, playerY, playerZ);
		projection = projection.subtract(renderDir.getCoordwiseMult(projection));
		projection.add(renderDir.getCoordwiseMult(center));
		Vector3i mapPlayer = coords.getMapCoord(center, projection);

		for (int i = 0; i < canvas.getCursors().size(); i++) {
			MapCursor cursor = canvas.getCursors().getCursor(i);
			if (cursor.getCaption() == player.getDisplayName()) {
				canvas.getCursors().removeCursor(cursor);
				break;
			}
		}
		if (player.isSneaking()) {
			Vector cursorPlayer = coords.getMapCoord(
					new Vector(xCenter, yCenter, zCenter),
					new Vector(projection.getX(), projection.getY(), projection.getZ()));
			double x = cursorPlayer.getX();
			double y = cursorPlayer.getY();
			if (-halfWidth <= x && x < halfWidth && -halfWidth <= y && y < halfWidth) {
				x *= 2 * scale;
				y *= 2 * scale;
				float yaw = Location.normalizeYaw(playerLoc.getYaw());
				byte dir = (byte) ((180 + yaw) / 360 * 16 - 12 - 1f/2); // -12 DOWN => east
				if (dir < 0)
					dir = (byte) (16 + dir);
				canvas.getCursors().addCursor(new MapCursor((byte) x, (byte) y, dir, MapCursor.Type.WHITE_POINTER, true, player.getName()));
			}
		}
		mapPlayer.add(new Vector3i(halfWidth, halfWidth, 0));
		final int mapPlayerX = mapPlayer.getX();
		final int mapPlayerY = mapPlayer.getY();

		boolean[][] discovered = map.getDicovered();
		if (!map.isFullDicovered()) {
			// TODO raytrace to projection
			boolean updated = false;
			for (int index = 0; index < 4; index++) {
				for (int c = 0; c < width - 1; c++) {
					// map edge point
					int xOffset, yOffset;
					if (index == 0) {
						xOffset = c;
						yOffset = 0;
					} else if (index == 1) {
						xOffset = 0;
						yOffset = 1 + c;
					} else if (index == 2) {
						xOffset = c;
						yOffset = width - 1;
					} else {
						xOffset = width - 1;
						yOffset = 1 + c;
					}

					Vector p = new Vector(mapPlayerX, mapPlayerY, 0);
					Vector dir = new Vector(xOffset, yOffset, 0).subtract(p);
					p.add(new Vector(0.5, 0.5, 0));
					if (dir.length() == 0) {
						discovered[xOffset][yOffset] = true;
						continue;
					}
					dir.normalize();
					double dirX = dir.getX() == 0 ? 0 : dir.getX() > 0 ? 1 : -1;
					double dirY = dir.getY() == 0 ? 0 : dir.getY() > 0 ? 1 : -1;
					//System.out.print("FROM " + mapPlayer.getX() + "; " + mapPlayer.getY() + " TO " + xOffset + "; " + yOffset);
					while (true) {
						double dx = Math.floor(p.getX()) - p.getX();
						if (dirX >= 0) {
							dx = 1 + dx;
						}
						double dy = Math.floor(p.getY()) - p.getY();
						if (dirY >= 0) {
							dy = 1 + dy;
						}
						double tx = dx / dir.getX();
						double ty = dy / dir.getY();
						// fix vertexes
						if (tx == 0)
							tx = 1;
						if (ty == 0)
							ty = 1;
						
						double t = Math.min(tx, ty);
						p = p.add(dir.clone().multiply(t));
						
						int x = (int) p.getX();
						int y = (int) p.getY();
						if (x < 0 || width <= x || y < 0 || width <= y)
							break;
						if (discovered[x][y])
							continue;
						int mapDx = mapPlayerX - x;
						int mapDy = mapPlayerY - y;
						if (mapDx * mapDx + mapDy * mapDy > RAYS_DISTANCE_SQUARED)
							break;
						
						Vector3i offsets = coords.getWorldCoord(x, y, 0);
						int realX = xCenter + offsets.getX();
						int realY = yCenter + offsets.getY();
						int realZ = zCenter + offsets.getZ();
						Block b = world.getBlockAt(realX, realY, realZ);
						if (b.isPassable() || !b.getType().isOccluding() || b.getType().isTransparent()) {
							discovered[x][y] = true;
							if (!updated) {
								updated = true;
							}
						} else {
							break;
						}
					}
				}
			}
			if (updated) {
				map.checkDiscovering();
				MapFileManager.saveDiscovered(map);
			}
		}

		for (int x = 0; x < width; x++)
		{
			for (int z = 0; z < width; z++)
			{
				Vector3i offsets0 = coords.getWorldCoord(x, z, 0);
				int d1 = offsets0.getX(),
					d2 = offsets0.getY(),
					d3 = offsets0.getZ();
				int dist1 = playerX - xCenter + d1,
					dist2 = playerY - yCenter + d2,
					dist3 = playerZ - zCenter + d3;
				int dist = dist1 * dist1 + dist2 * dist2 + dist3 * dist3;
				if (dist > RAYS_DISTANCE_SQUARED)
					continue;
				if (dist > RENDER_DISTANCE_SQUARED) {
					if (!map.isFullDicovered()) {
						if (!discovered[x][z]) {
							continue;
						}
					}
				}
				
				byte color = 2;
				int yScale;
				for (yScale = 1; yScale <= SHADES_COUNT; yScale++) {
					Vector3i offsets = coords.getWorldCoord(x, z, yScale);
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
				
				int px_x = x * scale;
				int px_z = z * scale;
				for (int dx = 0; dx < scale; dx++)
					for (int dz = 0; dz < scale; dz++)
						canvas.setPixel(px_x + dx, px_z + dz, color);
			}
		}
	}
}
