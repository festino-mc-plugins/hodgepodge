package com.festp.dispenser;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

public class DropActions implements Listener
{
	// cauldrons to be filled by dispenser
	List<DispenserCauldronFiller> cauldronFillers = new ArrayList<>();
	// dispensers to feed animals
	List<DispenserFeeder> feeders = new ArrayList<>();
	
	public void onTick() {
		// breeding
		for (DispenserFeeder feeder : feeders) {
			feeder.feed();
		}
		feeders.clear();
		
		// cauldrons
		for (DispenserCauldronFiller filler : cauldronFillers) {
			filler.fill();
		}
		cauldronFillers.clear();
	}
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event)
	{
		ItemStack item = event.getItem();
		if (item == null || event.getBlock().getType() != Material.DISPENSER)
			return;
		
		Dispenser dispenser = (Dispenser)event.getBlock().getState();
		final Block block = getActionBlock(dispenser);

        DispenserCauldronFiller filler = DispenserCauldronFiller.tryCreate(dispenser, item, block);
        if (filler != null)
        {
            cauldronFillers.add(filler);
            event.setCancelled(true);
            return;
        }

        DispenserFeeder feeder = DispenserFeeder.tryCreate(dispenser, item, block);
        if (feeder != null)
        {
        	feeders.add(feeder);
            event.setCancelled(true);
            return;
        }
	}
	
	private static Block getActionBlock(Dispenser dispenser)
	{
		Directional directonal = (Directional)dispenser.getBlockData();
		BlockFace face = directonal.getFacing();
		int dx = (face == BlockFace.WEST ? -1 : (face == BlockFace.EAST ? 1 : 0));
		int dy = (face == BlockFace.DOWN ? -1 : (face == BlockFace.UP ? 1 : 0));
		int dz = (face == BlockFace.NORTH ? -1 : (face == BlockFace.SOUTH ? 1 : 0));
		int x = dispenser.getX(), y = dispenser.getY(), z = dispenser.getZ();
		return dispenser.getWorld().getBlockAt(x + dx, y + dy, z + dz);
	}
}
