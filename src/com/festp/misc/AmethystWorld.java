package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.festp.utils.TimeUtils;

public class AmethystWorld {
	private static final int MAX_RADIUS = Math.max(AmethystManager.DIAMOND_RADIUS, AmethystManager.NETHERITE_RADIUS);
	private final int worldHeight = 256;
	private List<List<AmethystChunk>> chunks = new ArrayList<>();
	public final World origWorld;
	
	public AmethystWorld(World world)
	{
		origWorld = world;
	}
	
	public boolean cancelSpawn(Location l)
	{
		int chunkX = l.getChunk().getX();
		int chunkZ = l.getChunk().getZ();
		int offsetX = l.getBlockX() - chunkX * 16;
		int offsetZ = l.getBlockZ() - chunkZ * 16;
		int minChunkX = chunkX + (MAX_RADIUS - offsetX + 15) / 16;
		int maxChunkX = chunkX + (MAX_RADIUS + offsetX - 1) / 16;
		int minChunkZ = chunkZ + (MAX_RADIUS - offsetZ + 15) / 16;
		int maxChunkZ = chunkZ + (MAX_RADIUS + offsetZ - 1) / 16;
		int maxR = (MAX_RADIUS + 15) / 16;
		for (int r = 0; r < maxR; r++) {
			for (int dx = -r; dx <= 0; dx++) {
				int x = chunkX + dx;
				if (x < minChunkX)
					continue;
				int dz = -r - dx;
				int z = chunkZ + dz;
				if (z < minChunkZ)
					continue;
				if (hasCancelling(chunkX, chunkZ, l))
					return true;
			}
			for (int dx = -r + 1; dx <= 0; dx++) {
				int x = chunkX + dx;
				if (x < minChunkX)
					continue;
				int dz = r + dx;
				int z = chunkZ + dz;
				if (z > maxChunkZ)
					break;
				if (hasCancelling(chunkX, chunkZ, l))
					return true;
			}
			for (int dx = 1; dx <= r; dx++) {
				int x = chunkX + dx;
				if (x > maxChunkX)
					break;
				int dz = -r + dx;
				int z = chunkZ + dz;
				if (z < minChunkZ)
					continue;
				if (hasCancelling(chunkX, chunkZ, l))
					return true;
			}
			for (int dx = 1; dx < r; dx++) {
				int x = chunkX + dx;
				if (x > maxChunkX)
					break;
				int dz = r - dx;
				int z = chunkZ + dz;
				if (z > maxChunkZ)
					continue;
				if (hasCancelling(chunkX, chunkZ, l))
					return true;
			}
		}
		return false;
	}
	
	private boolean hasCancelling(int chunkX, int chunkZ, Location spawn)
	{
		AmethystChunk chunk = getOrAdd(chunkX, chunkZ);
		List<Block> blocks = chunk.get(spawn.getBlockY() - MAX_RADIUS, spawn.getBlockY() + MAX_RADIUS);
		for (int i = blocks.size() - 1; i >= 0; i--) {
			Block b = blocks.get(i);
			if (!AmethystManager.isCancelling(b)) {
				blocks.remove(i);
				continue;
			}
			int dist = getDist(spawn, b);
			if (b.getType() == Material.DIAMOND_BLOCK && dist <= AmethystManager.DIAMOND_RADIUS
					|| b.getType() == Material.NETHERITE_BLOCK && dist <= AmethystManager.NETHERITE_RADIUS)
				return true;
		}
		return false;
	}
	
	private static int getDist(Location l, Block b)
	{
		int dx = Math.abs(l.getBlockX() - b.getX());
		int dy = Math.abs(l.getBlockY() - b.getY());
		int dz = Math.abs(l.getBlockZ() - b.getZ());
		return Math.max(Math.max(dx, dy), dz);
	}
	
	private AmethystChunk getOrAdd(int chunkX, int chunkZ)
	{
		int xIndex = getByX(chunkX);
		if (xIndex == -1) {
			AmethystChunk res = createOld(chunkX, chunkZ);
			List<AmethystChunk> zChunks = new ArrayList<>();
			zChunks.add(res);
			chunks.add(zChunks);
			return res;
		}
		int foundX = chunks.get(xIndex).get(0).chunk.getX();
		if (chunkX != foundX) {
			if (foundX < chunkX)
				xIndex++;
			AmethystChunk res = createOld(chunkX, chunkZ);
			List<AmethystChunk> zChunks = new ArrayList<>();
			zChunks.add(res);
			chunks.add(xIndex, zChunks);
			return res;
		}
		List<AmethystChunk> zChunks = chunks.get(xIndex);
		int zIndex = getByZ(xIndex, chunkZ);
		int foundZ = zChunks.get(zIndex).chunk.getX();
		if (chunkZ != foundZ) {
			if (foundZ < chunkZ)
				zIndex++;
			AmethystChunk res = createOld(chunkX, chunkZ);
			zChunks.add(zIndex, res);
			return res;
		}
		return zChunks.get(zIndex);
	}
	
	private int getByX(int chunkX)
	{
		if (chunks.size() == 0)
			return -1;
		int low = 0; int high = chunks.size() - 1;
		int lowC = chunks.get(low).get(0).chunk.getX();
		if (lowC >= chunkX) return low;
		int highC = chunks.get(high).get(0).chunk.getX();
		if (highC <= chunkX) return high;
		while (low + 1 < high) {
			int mid = (low + high) / 2;
			int midC = chunks.get(mid).get(0).chunk.getX();
			if (midC == chunkX)
				return mid;
			if (midC < chunkX)
				low = mid;
			else
				high = mid;
		}
		return low;
	}
	
	private int getByZ(int xIndex, int chunkZ)
	{
		List<AmethystChunk> zChunks = chunks.get(xIndex);
		int low = 0; int high = zChunks.size() - 1;
		int lowC = zChunks.get(0).chunk.getZ();
		if (chunkZ <= lowC) return low;
		int highC = zChunks.get(high).chunk.getZ();
		if (highC <= chunkZ) return high;
		while (low + 1 < high) {
			int mid = (low + high) / 2;
			int midC = zChunks.get(mid).chunk.getZ();
			if (midC == chunkZ)
				return mid;
			if (midC < chunkZ)
				low = mid;
			else
				high = mid;
		}
		return low;
	}
	
	private AmethystChunk createOld(int chunkX, int chunkZ)
	{
		return new AmethystChunk(origWorld.getChunkAt(chunkX, chunkZ), worldHeight, TimeUtils.getTicks(), false);
	}
}
