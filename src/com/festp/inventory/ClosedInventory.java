package com.festp.inventory;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import com.festp.utils.UtilsType;

public class ClosedInventory {
	private final UUID player;
	private final Block holder;
	private final InventoryView invView;
	private int ticks;
	
	public ClosedInventory(UUID closedInvPlayer, int closedInvTicks, InventoryView view) {
		this.player = closedInvPlayer;
		this.invView = view;
		this.holder = view.getTopInventory().getLocation().getBlock();
		this.ticks = closedInvTicks;
	}

	public void tick() {
		ticks--;
	}
	
	public int getTicks() {
		return ticks;
	}
	
	public InventoryView getView() {
		return invView;
	}
	
	public boolean matchUUID(UUID playerUuid) {
		return playerUuid.equals(player);
	}
	
	public Inventory getInventory() {
		Material block = holder.getType();
		if(UtilsType.is_shulker_box(block)) {
			return ((ShulkerBox)holder.getState()).getInventory();
		}
		else if(block == Material.CHEST || block == Material.TRAPPED_CHEST) {
			return ((Chest)holder.getState()).getInventory();
		}
		else if(block == Material.BARREL) {
			return ((Barrel)holder.getState()).getInventory();
		}
		else return null;
	}

	public static boolean isSupported(InventoryType type) {
		return type == InventoryType.SHULKER_BOX
			    || type == InventoryType.CHEST
			    || type == InventoryType.BARREL;
	}

	// TODO make configurable
	public static boolean isDroppable(InventoryType type) {
		return isSupported(type) &&
				(type == InventoryType.SHULKER_BOX
			    || type == InventoryType.CHEST
			    || type == InventoryType.BARREL);
	}
}
