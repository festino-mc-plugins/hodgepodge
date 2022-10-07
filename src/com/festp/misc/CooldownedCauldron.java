package com.festp.misc;

import org.bukkit.block.Block;

public class CooldownedCauldron {
	public int ticks;
	public Block cauldron;
	
	public CooldownedCauldron(int ticks, Block cauldron) {
		this.ticks = ticks;
		this.cauldron = cauldron;
	}
}
