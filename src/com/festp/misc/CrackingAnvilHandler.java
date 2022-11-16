package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.UtilsType;

public class CrackingAnvilHandler implements Listener
{
	private static final float MIN_FALL_DIST = 2.5f;
	
	private final List<FallingBlock> fallingAnvils = new ArrayList<>();
	private final List<Float> fallingAnvilsDist = new ArrayList<>();
	
	public void tick()
	{
		for (int i = fallingAnvils.size() - 1; i >= 0; i--) {
			if (!fallingAnvils.get(i).isValid()) {
				fallingAnvils.remove(i);
				fallingAnvilsDist.remove(i);
			}
			else {
				fallingAnvilsDist.set(i, fallingAnvils.get(i).getFallDistance());
			}
		}
		
	}
	
	@EventHandler
	public void onBlockSpawn(final EntitySpawnEvent event)
	{
		Entity falling = event.getEntity();

		if (!(falling instanceof FallingBlock)) {
	        return;
	    }

		FallingBlock fallingBlock = (FallingBlock) falling;
		Material fallingMaterial = fallingBlock.getBlockData().getMaterial();
		if (!UtilsType.isAnvil(fallingMaterial)) {
	        return;
	    }
		
		fallingAnvils.add(fallingBlock);
		fallingAnvilsDist.add(fallingBlock.getFallDistance());
		// store player, break shulker boxes as player
	}
	
	@EventHandler
	public void onBlockLanding(final EntityChangeBlockEvent event)
	{
		Entity falling = event.getEntity();

		if (!(falling instanceof FallingBlock)) {
	        return;
	    }

		if (!UtilsType.isAnvil(event.getTo())) {
	        return;
	    }
		
		//float fallDistance = falling.getFallDistance(); - is always 0.0f
		float fallDistance = 0.0f;
		for (int i = 0; i < fallingAnvils.size(); i++) {
			FallingBlock fallingBlock = fallingAnvils.get(i);
			if (fallingBlock.getUniqueId().equals(falling.getUniqueId())) {
				fallDistance = fallingAnvilsDist.get(i);
				break;
			}
		}
		if (fallDistance < MIN_FALL_DIST) {
	        return;
	    }

		Block blockBelow = event.getBlock().getRelative(0, -1, 0);
		if (UtilsType.is_shulker_box(blockBelow.getType())) {
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
		else if (blockBelow.getType() == Material.STONE) {
			blockBelow.setType(Material.COBBLESTONE);
		}
		else if (blockBelow.getType() == Material.COBBLESTONE) {
			blockBelow.setType(Material.GRAVEL);
		}
		else if (blockBelow.getType() == Material.GRAVEL) {
			blockBelow.setType(Material.SAND);
		}
	}
}
