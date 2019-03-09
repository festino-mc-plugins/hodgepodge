package com.festp.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;

import com.festp.utils.Utils;

public class ClosedInventory {
	private UUID closed_inv_player;
	private Integer closed_inv_ticks;
	private Block closed_inv;
	
	public ClosedInventory(UUID closed_inv_player, int closed_inv_ticks, Block closed_inv) {
		this.closed_inv_player = closed_inv_player;
		this.closed_inv_ticks = closed_inv_ticks;
		this.closed_inv = closed_inv;
	}
	
	public int getTicks() {
		return closed_inv_ticks;
	}
	
	public boolean UUID_match(UUID swap_player) {
		return swap_player.equals(closed_inv_player);
	}
	
	public Inventory getInventory() {
		Material block = closed_inv.getType();
		if(Utils.is_shulker_box(block)) {
			return ((ShulkerBox)closed_inv.getState()).getInventory();
		}
		else if(block == Material.CHEST || block == Material.TRAPPED_CHEST) {
			return ((Chest)closed_inv.getState()).getInventory();
		}
		else return null;
	}

	public void oneTick() {
		closed_inv_ticks--;
	}
}
