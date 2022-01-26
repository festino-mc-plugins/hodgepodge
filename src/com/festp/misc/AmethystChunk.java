package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.festp.utils.TimeUtils;

public class AmethystChunk {
	private static final int PART_BITS = Integer.SIZE; // int is 32-bit
	private static final int PART_SIZE = PART_BITS * 16; // int is 32-bit
	private final int sectionCount;
	private final int minY; // if no negative sections
	public static final long UNLOAD_TIME = 20 * 60 * 10;
	public final Chunk chunk;
	public long lastUpdate;
	public int[] verticalChunks;
	public List<Block> antispawnBlocks;
	
	// TODO Fix below y=0
	/** Newly generated chunks are empty. */
	public AmethystChunk(Chunk chunk, long time, boolean empty)
	{
		this.chunk = chunk;
		lastUpdate = time;
		World world = chunk.getWorld();
		minY = world.getMinHeight();
		int worldHeight = world.getMaxHeight() - world.getMinHeight();
		sectionCount = (worldHeight + 15) / 16;
		int size = (worldHeight + PART_SIZE - 1) / PART_SIZE;
		verticalChunks = new int[size];
		for (int i = 0; i < size; i++)
			if (empty)
				verticalChunks[i] = 0xFFFFFFFF;
			else
				verticalChunks[i] = 0x00000000;
		antispawnBlocks = new ArrayList<>();
	}
	
	/** Only initiate the very first load of sections containing [yBegin; yEnd-1]
	 * @return All <b>chunk</b> antispawn blocks*/
	public List<Block> get(int yBegin, int yEnd)
	{
		lastUpdate = TimeUtils.getTicks();

		int indexBegin = getMinSection(yBegin);
		int indexEnd = getMaxSection(yEnd);
		ChunkSnapshot snap = null;
		for (int i = indexBegin; i < indexEnd; i++) {
			int part = i / PART_BITS;
			int bitN = i % PART_BITS;
			int bit = 0x1 << bitN;
			if ((verticalChunks[part] & bit) == 0) {
				if (snap == null)
					snap = chunk.getChunkSnapshot();
				load(part, bitN, snap);
				verticalChunks[part] |= bit;
			}
		}
		return antispawnBlocks;
	}
	
	private void clear(int yBegin, int yEnd)
	{
		int indexBegin = getMinSection(yBegin);
		int indexEnd = getMaxSection(yEnd);
		for (int i = indexBegin; i < indexEnd; i++) {
			int part = i / PART_BITS;
			int bit = 0x1 << i % PART_BITS;
			verticalChunks[part] &= ~bit;
		}
		int minY = indexBegin * 16;
		int maxY = indexEnd * 16 - 1;
		for (int i = antispawnBlocks.size() - 1; i >= 0; i--) {
			Block b = antispawnBlocks.get(i);
			if (minY <= b.getY() && b.getY() <= maxY)
				antispawnBlocks.remove(i);
		}
	}

	public void update(int yBegin, int yEnd)
	{
		//yBegin -= minY;
		//yEnd -= minY;
		clear(yBegin, yEnd);
		get(yBegin, yEnd);
	}
	
	/** @return -16..-1 -> -1 <br> 0..15 -> 0 <br> 16..31 -> 1 <br>*/
	private static int yToSection(int x) {
		if (x >= 0 || x % 16 == 0) {
			return x / 16;
		}
		return x / 16 - 1;
	}
	
	private int getMinSection(int y) {
		return Math.max(yToSection(y), 0);
	}
	
	private int getMaxSection(int y) {
		return Math.min(yToSection(y - 1) + 1, sectionCount);
	}
	
	private void load(int part, int bitN, ChunkSnapshot snap)
	{
		if (!snap.isSectionEmpty(part * 32 + bitN)) {
			int yBegin = 0 + (part * 32 + bitN) * 16;
			int yEnd = yBegin + 16;
			for (int y = yBegin; y < yEnd; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						Material m = snap.getBlockType(x, y, z);
						if (AmethystManager.isCancelling(m)) {
							Block b = chunk.getBlock(x, y, z);
							antispawnBlocks.add(b);
						}
					}
				}
			}
		}
	}
	
	public boolean canUnload(long time)
	{
		return lastUpdate + UNLOAD_TIME < time;
	}
}
