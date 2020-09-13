package com.festp.maps;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapView;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.festp.maps.PaletteUtils;
import com.festp.utils.Vector3i;

public class DrawingRenderer extends AbstractRenderer {
	
	static final int RENDER_DISTANCE_SQUARED = 0 * 0;
	static final int RAYS_DISTANCE_SQUARED = 100 * 100;
	static final int SHADES_COUNT = PaletteUtils.SHADES_COUNT;
	static final float ANGLE_WIDTH = 120f, ANGLE_HEIGHT = 70f;

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
		Location playerSight = player.getEyeLocation();
		Vector vecSight = playerSight.toVector();
		int playerX = playerLoc.getBlockX();
		int playerY = playerLoc.getBlockY();
		int playerZ = playerLoc.getBlockZ();
		
		int xCenter = map.getX();
		int yCenter = map.getY();
		int zCenter = map.getZ();
		int scale = map.getScale();
		int width = 128 / scale;
		int halfWidth = width / 2;
		Position pos = map.getState();
		DrawingMapCoordinator coords = new DrawingMapCoordinator(pos, width);
		// TODO update rework (rays? render fov?)
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
					/*Vector3i offsets = coords.getWorldCoord(x, z, 1);
					int realX = xCenter + offsets.getX();
					int realY = yCenter + offsets.getY();
					int realZ = zCenter + offsets.getZ();
					Block block = view.getWorld().getBlockAt(realX, realY, realZ);
					// TODO update rework (rays? render fov?)
					if (!inSight(playerSight, block, ANGLE_WIDTH, ANGLE_HEIGHT))
						continue;*/
					Vector3i offsets = coords.getWorldCoord(x, z, 1);
					offsets = offsets.subtract(offsets0);
					offsets = offsets.multiply(0.51);
					offsets = offsets.add(offsets0);
					int realX = xCenter + offsets.getX();
					int realY = yCenter + offsets.getY();
					int realZ = zCenter + offsets.getZ();
					Location blockLoc = new Location(view.getWorld(), realX + 0.5, realY + 0.5, realZ + 0.5);
					Vector dir = blockLoc.toVector().subtract(vecSight);
					double length = dir.length();
					if (length > 0) {
						//RayTraceResult res = block.rayTrace(playerSight, dir, length, FluidCollisionMode.ALWAYS);
						RayTraceResult res = view.getWorld().rayTraceBlocks(playerSight, dir, length, FluidCollisionMode.ALWAYS, true);
						if (res != null && res.getHitBlock() != null && !res.getHitBlock().equals(blockLoc.getBlock())) {
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
					Block b = view.getWorld().getBlockAt(realX, realY, realZ);
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
	
	private static boolean inSight(Location from, Block block, float angleWidth, float angleHeight) {
		// get nearest point
		Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
		Location diffs = blockCenter.clone().subtract(from);
		diffs.setX(round(diffs.getX(), -0.5, 0.5));
		diffs.setY(round(diffs.getY(), -0.5, 0.5));
		diffs.setZ(round(diffs.getZ(), -0.5, 0.5));
		Location to = blockCenter.subtract(diffs);
		// ray trace
		Vector direction = to.subtract(from).toVector();
		double length = direction.length();
		if (length == 0) {
			return true;
		}
		/// TODO move to UtilsWorld
		/*double yaw  = Math.atan2(from.getZ() - to.getZ(), from.getX() - to.getX()) + 45;
		double pitch =  Math.asin((from.getY() - to.getY()) / to.distance(from));
		float dYaw = (float) Math.abs(from.getYaw() - yaw);
		float dPitch = (float) Math.abs(from.getPitch() - pitch);
		if (dYaw > dYawMax || dPitch > dPitchMax) { // TODO RECTANGLE AREA???
			return false;
		}*/
		Vector sight = from.getDirection();
		//Vector horAxis = new Vector(-sight.getZ(), 0, sight.getX());
		Vector horAxis = new Vector(-Math.cos(from.getYaw()), 0, -Math.sin(from.getYaw()));
		//System.out.print("0=dir   " + direction);
		//System.out.print("1=sig   " + sight + " " + from);
		Vector top = sight.clone().rotateAroundAxis(horAxis, -angleHeight / 2);
		Vector bottom = sight.clone().rotateAroundAxis(horAxis, angleHeight / 2);
		System.out.print("vert plane   " + sight + " -> \n" + top + "\n " + bottom);
		Vector leftTop =  top.clone().rotateAroundY(angleWidth / 2);
		Vector rightTop = top.rotateAroundY(-angleWidth / 2);
		Vector leftBottom =  bottom.clone().rotateAroundY(angleWidth / 2);
		Vector rightBottom = bottom.rotateAroundY(-angleWidth / 2);
		//System.out.print("4=corners   " + leftTop + "\n " + rightTop + "\n " + leftBottom + "\n " + rightBottom);
		//System.out.print("5=extradots   " + leftBottom.clone().crossProduct(leftTop).dot(sight) + " " + leftTop.clone().crossProduct(rightTop).dot(sight)
		//		+ " " + rightTop.clone().crossProduct(rightBottom).dot(sight) + " " + rightBottom.clone().crossProduct(leftBottom).dot(sight));
		//System.out.print("6=corners   " + leftTop + "\n " + rightTop + "\n " + leftBottom + "\n " + rightBottom);
		double dot1 = leftBottom.crossProduct(leftTop).dot(direction);
		double dot2 = leftTop.crossProduct(rightTop).dot(direction);
		double dot3 = rightTop.crossProduct(rightBottom).dot(direction);
		double dot4 = rightBottom.crossProduct(leftBottom).dot(direction);
		//System.out.print("3=d   " + dot1 + " " + dot2 + " " + dot3 + " " + dot4);
		if (dot1 <= 0 || dot2 <= 0 || dot3 <= 0 || dot4 <= 0) {
			return false;
		}
		RayTraceResult res = to.getWorld().rayTraceBlocks(from, direction, length, FluidCollisionMode.ALWAYS, true);
		if (res == null || res.getHitBlock() == null || res.getHitBlock().equals(block)) {
			return true;
		}
		return false;
	}
	
	private static double round(double orig, double low, double high) {
		if (orig < low)
			return low;
		else if (orig > high)
			return high;
		else
			return orig;
	}
}
