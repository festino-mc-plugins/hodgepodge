package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class AmethystChunk {
	private static final int PART_SIZE = 32 * 16; // int is 32-bit
	public static final long UNLOAD_TIME = 20 * 60 * 10;
	public final Chunk chunk;
	public long lastUpdate;
	public int[] verticalChunks;
	public List<Block> antispawnBlocks;
	
	/** Newly generated chunks are empty. */
	public AmethystChunk(Chunk chunk, int worldHeight, long time, boolean empty)
	{
		this.chunk = chunk;
		lastUpdate = time;
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
		int indexBegin = yBegin / 16;
		int indexEnd = (yEnd - 1) / 16 + 1;
		ChunkSnapshot snap = null;
		for (int i = indexBegin; i < indexEnd; i++) {
			int part = i / 32;
			int bit = 0x1 >> i % 32;
			if ((verticalChunks[part] & bit) == 0) {
				if (snap == null)
					snap = chunk.getChunkSnapshot();
				load(part, bit, snap);
			}
		}
		return antispawnBlocks;
	}
	
	private void load(int part, int bit, ChunkSnapshot snap)
	{
		if (!snap.isSectionEmpty(part * 32 + bit)) {
			int yBegin = 0 + (part * 32 + bit) * 16;
			int yEnd = yBegin + 16;
			for (int y = yBegin; y < yEnd; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						Material m = snap.getBlockType(x, y, z);
						if (AmethystManager.isCancelling(m)) {
							Block b = chunk.getBlock(x, y, z);
							if (b.isBlockPowered())
								antispawnBlocks.add(b);
						}
					}
				}
			}
		}
		verticalChunks[part] |= bit;
	}
	
	public boolean canUnload(long time)
	{
		return lastUpdate + UNLOAD_TIME < time;
	}
}
