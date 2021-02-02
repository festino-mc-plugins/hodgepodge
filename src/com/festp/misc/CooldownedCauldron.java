package com.festp.misc;

import org.bukkit.block.Block;

public class CooldownedCauldron {
	int ticks;
	Block cauldron;
	
	public CooldownedCauldron(int ticks, Block cauldron) {
		this.ticks = ticks;
		this.cauldron = cauldron;
	}
}
