package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import com.google.common.collect.Lists;

public class AmethystManager implements Listener {
	public static final Material AMETHYST_MATERIAL = Material.DIAMOND_BLOCK;
	private static final int MAX_HOR = 10, MAX_VERT = 10, MAX_DIAG = 140;
	private List<EntityType> BLOCKED_TYPES = new ArrayList<>();

	public AmethystManager() {
		BLOCKED_TYPES = Lists.newArrayList(EntityType.SKELETON, EntityType.STRAY,
											EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK,
											EntityType.SPIDER, EntityType.CAVE_SPIDER,
											EntityType.CREEPER, EntityType.GUARDIAN);
	}
	
	// cancel spawn
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntitySpawn(EntitySpawnEvent event) {
		if (event.isCancelled())
			return;
		
		Entity e = event.getEntity();
		
		if (BLOCKED_TYPES.contains(event.getEntityType())) {
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
	public boolean blockedSpawn(Location l) {
		Block center = l.getBlock();

		for (int dy = -MAX_VERT; dy <= MAX_VERT; dy++) {
			int x_limit = Math.min(MAX_DIAG - Math.abs(dy), MAX_HOR); 
			for (int dx = -x_limit; dx <= x_limit; dx++) {
				int z_limit = Math.min(MAX_DIAG - Math.abs(dx) - Math.abs(dy), MAX_HOR); 
				for (int dz = -z_limit; dz <= z_limit; dz++) {
					Block b = center.getRelative(dx, dy, dz);
					if (isBlocking(b)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean isBlocking(Block b) {
		return b.getChunk().isLoaded() && b.getType() == AMETHYST_MATERIAL && b.isBlockPowered();
	}
}
