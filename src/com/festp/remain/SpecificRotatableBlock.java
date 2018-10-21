package com.festp.remain;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class SpecificRotatableBlock implements OldRotatableBlock {
	Material block = null;
	boolean enable_not_sneaking = true;
	boolean enable_sneaking = true;
	byte[] positions;
	
	public SpecificRotatableBlock(Material b, byte... pos) {
		block = b;
		positions = pos;
	}
	
	public SpecificRotatableBlock(Material b, boolean enable_not_sneaking, byte... pos) {
		block = b;
		positions = pos;
		this. enable_not_sneaking =  enable_not_sneaking;
	}
	
	public SpecificRotatableBlock(Material b, boolean enable_not_sneaking, boolean enable_sneaking, byte... pos) {
		block = b;
		positions = pos;
		this. enable_not_sneaking =  enable_not_sneaking;
		this. enable_sneaking =  enable_sneaking;
	}

	@Override
	public boolean can_rotate(Material m, byte start, boolean sneaking) {
		if(block == m && ( (enable_sneaking && sneaking) || (enable_not_sneaking && !sneaking) ) && positions_contains(start)) {
			return true;
		}
		return false;
	}

	@Override
	public byte rotate(byte start) {
		for(int i = 0; i < positions.length; i++) {
			if(positions[i] == start)
				return positions[(i+1)%positions.length];
		}
		return -1;
	}
	
	private boolean positions_contains(byte b) {
		for(byte pb : positions) {
			if(pb == b)
				return true;
		}
		return false;
	}
}
