package com.festp.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.UtilsType;

public class CrackingAnvilHandler implements Listener
{
	private static final float MIN_FALL_DIST = 0.0f; // currently doesn't work, always 0.0f
	
	@EventHandler
	public void onFallBlock(final EntityChangeBlockEvent event)
	{
		Entity falling = event.getEntity();

		if (!(falling instanceof FallingBlock)) {
	        return;
	    }

		if (event.getTo() != Material.ANVIL) {
	        return;
	    }
		
		if (falling.getFallDistance() < MIN_FALL_DIST) {
	        return;
	    }

		Block blockBelow = event.getBlock().getRelative(0, -1, 0);
		if (!UtilsType.is_shulker_box(blockBelow.getType())) {
	        return;
		}
		
		ShulkerBox shulker = (ShulkerBox) blockBelow.getState();
		Location dropLoc = blockBelow.getLocation().add(0.5, 0.5, 0.5);
		World world = dropLoc.getWorld();
		ItemStack shellDrop = new ItemStack(Material.SHULKER_SHELL, 2);
		
		world.dropItemNaturally(dropLoc, shellDrop);
		for (ItemStack drop : shulker.getInventory())
			if (drop != null)
				world.dropItemNaturally(dropLoc, drop);
		
		blockBelow.setType(Material.AIR);
	}
}
