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

	static final int RENDER_DISTANCE = 6;
	static final int RENDER_DISTANCE_SQUARED = RENDER_DISTANCE * RENDER_DISTANCE;
	static final int RAYS_DISTANCE_SQUARED = 100 * 100;
	private static final int GRID_ROWS = 4;
	private static final int GRID_SIZE = GRID_ROWS * GRID_ROWS;
	private static final int MAX_ROW_PIXELS = 128;
	private static final int MAX_PIXELS = MAX_ROW_PIXELS * MAX_ROW_PIXELS;
	private static final int RENDER_QUOTA = (MAX_PIXELS / 20) * 9 / 10;

	public final DrawingMap map;
	private DrawingMapGrid grid = null;
	
	public DrawingRenderer(DrawingMap map) {
		super(map);
		this.map = map;
	}

	@Override
	public void renderSpecific(MapView view, MapCanvas canvas, Player player) {
		if (map.needReset) {
			grid = null;
			byte initColor = PaletteUtils.getColor(PaletteUtils.TRANSPARENT);
			for (int x = 0; x < 128; x++)
				for (int y = 0; y < 128; y++)
					canvas.setPixel(x, y, initColor);
			map.needReset = false;
		}
		if (grid == null) {
			grid = new DrawingMapGrid(GRID_SIZE, map.getWidth(), System.currentTimeMillis());
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
		Vector3i renderCoord = renderDir.getCoordwiseMult(renderDir);
		Vector3i dv = renderCoord.getCoordwiseMult(center.clone().subtract(projection));
		boolean canRender = true;
		if (dv.lengthSquared() != 0) {
			int dist = (int) dv.length();
			dv.normalize();
			Block b;
			for (int i = 0; i <= dist; i++) {
				projection.add(dv);
				b = world.getBlockAt(projection.getX(), projection.getY(), projection.getZ());
				if (!canLookThrough(b)) {
					canRender = false;
					break;
				}
			}
		}

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
					playerLoc.toVector());
			double x = cursorPlayer.getX();
			double y = cursorPlayer.getY();
			if (-halfWidth <= x && x < halfWidth && -halfWidth <= y && y < halfWidth) {
				x = Math.round(x * 2 * scale);
				y = Math.round(y * 2 * scale);
				MapCursor cursor = coords.getCursor3D((byte) x, (byte) y, playerLoc);
				cursor.setCaption(player.getName());
				canvas.getCursors().addCursor(cursor);
			}
		}
		mapPlayer.add(new Vector3i(halfWidth, halfWidth, 0));
		final int mapPlayerX = mapPlayer.getX();
		final int mapPlayerY = mapPlayer.getY();
		
		boolean[][] discovered = map.getDicovered();
		if (canRender && !map.isFullDicovered()) {
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
						if (canLookThrough(b)) {
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

		DrawingMapPixelRenderer pixeler = new DrawingMapPixelRenderer(canvas, map, coords);
		long time = System.currentTimeMillis();
		int totalRendered = 0;
		{ // in order to hide local variables
			int minX = mapPlayerX - RENDER_DISTANCE, maxX = mapPlayerX + RENDER_DISTANCE;
			int minY = mapPlayerY - RENDER_DISTANCE, maxY = mapPlayerY + RENDER_DISTANCE;
			minX = Math.max(minX, 0);
			minY = Math.max(minY, 0);
			maxX = Math.min(maxX, width - 1);
			maxY = Math.min(maxY, width - 1);
			for (int x = minX; x < maxX; x++) {
				for (int y = minY; y < maxY; y++) {
					if (pixeler.tryRender(x, y)) {
						totalRendered++;
					}
				}
			}
		}
		grid.sort();
		int minX = mapPlayerX - RAYS_DISTANCE_SQUARED, maxX = mapPlayerX + RAYS_DISTANCE_SQUARED;
		int minY = mapPlayerY - RAYS_DISTANCE_SQUARED, maxY = mapPlayerY + RAYS_DISTANCE_SQUARED;
		minX = Math.max(minX, 0);
		minY = Math.max(minY, 0);
		maxX = Math.min(maxX, width);
		maxY = Math.min(maxY, width);
		for (int i = 0; i < GRID_SIZE; i++) {
			int n = grid.get(i);
			int firstX = n % GRID_ROWS;
			int firstY = (n * n) / GRID_ROWS % GRID_ROWS;
			minX = minX - minX % GRID_ROWS + firstX;
			minY = minY - minY % GRID_ROWS + firstY;
			maxX = maxX - maxX % GRID_ROWS + firstX;
			maxY = maxY - maxY % GRID_ROWS + firstY;
			//System.out.print(minX + "->" + maxX + " and " + minY + "->" + maxY);
			
			int gridRendered = 0;
			for (int x = minX; x < maxX; x += GRID_ROWS) {
				for (int y = minY; y < maxY; y += GRID_ROWS) {
					Vector3i offsets0 = coords.getWorldCoord(x, y, 0);
					int d1 = offsets0.getX(),
						d2 = offsets0.getY(),
						d3 = offsets0.getZ();
					int dist1 = playerX - (xCenter + d1),
						dist2 = playerY - (yCenter + d2),
						dist3 = playerZ - (zCenter + d3);
					int dist = dist1 * dist1 + dist2 * dist2 + dist3 * dist3;
					if (dist > RAYS_DISTANCE_SQUARED)
						continue;
					
					if (pixeler.tryRender(x, y)) {
						gridRendered++;
					}
				}
			}
			grid.updateTime(i, time, gridRendered);
			totalRendered += gridRendered;
			if (totalRendered >= RENDER_QUOTA) {
				break;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	private static boolean canLookThrough(Block b) {
		return b.isPassable() || !b.getType().isOccluding() || b.getType().isTransparent();
	}
}
