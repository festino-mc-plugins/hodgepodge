package com.festp.maps;

import org.bukkit.block.Block;

public class BlockContainer {
	public Block block;
	
	public void set(Block b) {
		block = b;
	}
	
	public Block get() {
		return block;
	}
	
	public int getY() {
		return block.getY();
	}
}
