package com.festp.inventory;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.festp.utils.UtilsType;

public class ClosedInventory {
	private final UUID closed_inv_player;
	private final Block closed_inv;
	private final InventoryView closed_view;
	private int closed_inv_ticks;
	
	public ClosedInventory(UUID closed_inv_player, int closed_inv_ticks, InventoryView view) {
		this.closed_inv_player = closed_inv_player;
		this.closed_view = view;
		this.closed_inv = view.getTopInventory().getLocation().getBlock();
		this.closed_inv_ticks = closed_inv_ticks;
	}
	
	public int getTicks() {
		return closed_inv_ticks;
	}
	
	public InventoryView getView() {
		return closed_view;
	}
	
	public boolean matchUUID(UUID swap_player) {
		return swap_player.equals(closed_inv_player);
	}
	
	public Inventory getInventory() {
		Material block = closed_inv.getType();
		if(UtilsType.is_shulker_box(block)) {
			return ((ShulkerBox)closed_inv.getState()).getInventory();
		}
		else if(block == Material.CHEST || block == Material.TRAPPED_CHEST) {
			return ((Chest)closed_inv.getState()).getInventory();
		}
		else if(block == Material.BARREL) {
			return ((Barrel)closed_inv.getState()).getInventory();
		}
		else return null;
	}

	public void oneTick() {
		closed_inv_ticks--;
	}
}
