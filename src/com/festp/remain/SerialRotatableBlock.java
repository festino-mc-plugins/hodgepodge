package com.festp.remain;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;

import com.festp.Utils;

import org.bukkit.block.data.type.Stairs;

public class SerialRotatableBlock implements OldRotatableBlock {
	Material block = null;
	boolean enable_not_sneaking = true;
	boolean enable_sneaking = true;
	byte position_min = 0;
	byte position_skips;
	byte position_max;
	private byte mmdif;
	
	
	public SerialRotatableBlock(Material b, byte skips, byte max) {
		this.block = b;
		this.position_skips = skips;
		this.position_max = max;
		this.mmdif = max;
	}
	
	public SerialRotatableBlock(Material b, byte min, byte skips, byte max) {
		this(b, skips, max);
		this.position_min = min;
		this.mmdif = (byte) (max-min);
	}
	
	public SerialRotatableBlock(Material b, byte min, byte skips, byte max, boolean enable_not_sneaking) {
		this(b, min, skips, max);
		this.enable_not_sneaking = enable_not_sneaking;
	}
	
	public SerialRotatableBlock(Material b, byte min, byte skips, byte max, boolean enable_not_sneaking, boolean enable_sneaking) {
		this(b, min, skips, max, enable_not_sneaking);
		this.enable_sneaking = enable_sneaking;
	}
	
	
	
	public SerialRotatableBlock(Material b, int skips, int max) {
		this.block = b;
		this.position_skips = (byte)skips;
		this.position_max = (byte)max;
		this.mmdif = (byte)max;
	}
	
	public SerialRotatableBlock(Material b, int min, int skips, int max) {
		this(b, skips, max);
		this.position_min = (byte)min;
		this.mmdif = (byte) (max-min);
	}
	
	public SerialRotatableBlock(Material b, int min, int skips, int max, boolean enable_not_sneaking) {
		this(b, min, skips, max);
		this.enable_not_sneaking = enable_not_sneaking;
	}
	
	public SerialRotatableBlock(Material b, int min, int skips, int max, boolean enable_not_sneaking, boolean enable_sneaking) {
		this(b, min, skips, max, enable_not_sneaking);
		this.enable_sneaking = enable_sneaking;
	}
	
	//additional
	public SerialRotatableBlock(Material b, int skips, int max, boolean enable_not_sneaking) {
		this(b, skips, max);
		this.enable_not_sneaking = enable_not_sneaking;
	}
	
	public SerialRotatableBlock(Material b, int skips, int max, boolean enable_not_sneaking, boolean enable_sneaking) {
		this(b, skips, max, enable_not_sneaking);
		this.enable_sneaking = enable_sneaking;
	}
	
	
	@Override
	public boolean can_rotate(Material m, byte start, boolean sneaking) {
		if(block == m && start >= position_min && ( (enable_sneaking && sneaking) || (enable_not_sneaking && !sneaking) ) && start < position_max) {
			return true;
		}
		return false;
	}

	@Override
	public byte rotate(byte start) {
		return (byte) ((start-position_min+position_skips)%mmdif+position_min);
	}
}
