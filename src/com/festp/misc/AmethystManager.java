package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.google.common.collect.Lists;

public class AmethystManager implements Listener {
	public static final Material AMETHYST_MATERIAL = Material.DIAMOND_BLOCK;
	private static final int MAX_HOR = 10, MAX_VERT = 10, MAX_DIAG = 14;
	private List<EntityType> BLOCKED_TYPES = new ArrayList<>();
	
	private List<Entity> loaded_entities = new ArrayList<>();

	public AmethystManager() {
		BLOCKED_TYPES = Lists.newArrayList(EntityType.SKELETON, EntityType.STRAY,
											EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK,
											EntityType.SPIDER, EntityType.CAVE_SPIDER,
											EntityType.CREEPER);
	}
	
	public void onTick() {
		loaded_entities.clear();
	}

	/*@EventHandler(priority=EventPriority.HIGHEST)
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity e : event.getChunk().getEntities()) {
			System.out.println("\t\tAdding " + e.getType() + " at " + e.getLocation() + "(loaded: " + e.getLocation().getChunk().isLoaded() +")");
			loaded_entities.add(e);
		}
	}*/
	
	// cancel spawn
	/*@EventHandler(priority=EventPriority.LOWEST)
	public void onEntitySpawn(EntitySpawnEvent event) {
		if (event.isCancelled())
			return;
		
		Entity e = event.getEntity();
		if (loaded_entities.contains(e))
			return;
		
		if (BLOCKED_TYPES.contains(event.getEntityType())) {
			System.out.println("\t\tWorking " + event.getEntityType() + " at " + event.getLocation() + "(loaded: " + e.getLocation().getChunk().isLoaded() +")");
			Location l = event.getLocation();
			if (blockedSpawn(l)) {
				event.setCancelled(true);
				if (e.getVehicle() != null)
					e.getVehicle().remove();
			} else {
				// add tag
			}
		}
	}
	
	// powered diamond block nearby
	public static boolean blockedSpawn(Location l) {
		if (!l.getChunk().isLoaded())
			return false;
		
		//Block center = l.getBlock();
		int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();

		for (int dy = -MAX_VERT; dy <= MAX_VERT; dy++) {
			int x_limit = Math.min(MAX_DIAG - Math.abs(dy), MAX_HOR); 
			for (int dx = -x_limit; dx <= x_limit; dx++) {
				int z_limit = Math.min(MAX_DIAG - Math.abs(dx) - Math.abs(dy), MAX_HOR); 
				for (int dz = -z_limit; dx <= z_limit; dz++) {
					//Block b = center.getRelative(dx, dy, dz);
					Block b = l.getWorld().getBlockAt(x + dx, y + dy, z + dz);
					if (isBlocking(b)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private static boolean isBlocking(Block b) {
		return b.getChunk().isLoaded() && b.getType() == AMETHYST_MATERIAL && b.isBlockPowered();
	}*/
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		Entity e = event.getEntity();
		if (!canspawn(e)) {
			event.setCancelled(true);
			if (e.getVehicle() != null)
				e.getVehicle().remove();
		} else {
			// tag
		}
	}
	
	public boolean canspawn(Entity e) {
		if (!(e instanceof Monster))
			return true;
		int x = e.getLocation().getBlockX(), y = e.getLocation().getBlockY(), z = e.getLocation().getBlockZ();
    	for (int r = 0; r < MAX_HOR; r++) {
    		for (int dy = -MAX_VERT; dy <= MAX_VERT; dy++) {
    			for (int dx = -r; dx <= r; dx++) {
    	    		Block tempb = e.getWorld().getBlockAt(x + dx, y + dy, z + r - Math.abs(dx));
    	    		if (tempb.getType() == AMETHYST_MATERIAL && tempb.isBlockPowered())
    	    		{
    	    			return false;
    	    		}
    	    		tempb = e.getWorld().getBlockAt(x + dx, y + dy, z - r + Math.abs(dx));
    	    		if (tempb.getType() == AMETHYST_MATERIAL && tempb.isBlockPowered())
    	    		{
    	    			return false;
    	    		}
    	    	}
        	}
    	}
		return true;
	}
}
