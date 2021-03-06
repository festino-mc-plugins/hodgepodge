package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import com.google.common.collect.Lists;

public class AmethystManager implements Listener {
	private static final int MAX_HOR = 10, MAX_VERT = 10, MAX_DIAG = 140;
	public static final int DIAMOND_RADIUS = 10, NETHERITE_RADIUS = 25;
	private List<EntityType> AFRAIDABLE_TYPES = new ArrayList<>();
	private List<AmethystWorld> worlds = new ArrayList<>();

	public AmethystManager() {
		AFRAIDABLE_TYPES = Lists.newArrayList(
				EntityType.SKELETON, EntityType.STRAY,
				EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.HUSK,
				EntityType.SPIDER, EntityType.CAVE_SPIDER,
				EntityType.CREEPER, EntityType.ZOMBIFIED_PIGLIN
		);
		for (World world : Bukkit.getWorlds()) {
			worlds.add(new AmethystWorld(world));
		}
	}
	
	// cancel spawn
	@EventHandler(priority=EventPriority.LOWEST)
	public void onEntitySpawn(EntitySpawnEvent event)
	{
		if (event.isCancelled())
			return;
		
		Entity e = event.getEntity();
		//if (!AFRAIDABLE_TYPES.contains(event.getEntityType())) return;
		
		Location l = event.getLocation();
		AmethystWorld world = getWorld(l);
		if (world.cancelSpawn(l)) {
			event.setCancelled(true);
			if (e.getVehicle() != null)
				e.getVehicle().remove();
		}
	}
	
	private AmethystWorld getWorld(Location l)
	{
		World w = l.getWorld();
		for (AmethystWorld world : worlds)
			if (world.origWorld == w)
				return world;
		AmethystWorld world = new AmethystWorld(w);
		worlds.add(world);
		return world;
	}
	
	// powered blocking block nearby
	public boolean cancelSpawn_Ineffective(Location l)
	{
		Block center = l.getBlock();

		for (int dy = -MAX_VERT; dy <= MAX_VERT; dy++) {
			int x_limit = Math.min(MAX_DIAG - Math.abs(dy), MAX_HOR); 
			for (int dx = -x_limit; dx <= x_limit; dx++) {
				int z_limit = Math.min(MAX_DIAG - Math.abs(dx) - Math.abs(dy), MAX_HOR); 
				for (int dz = -z_limit; dz <= z_limit; dz++) {
					Block b = center.getRelative(dx, dy, dz);
					if (isCancelling(b)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	public static boolean isCancelling(Material m) {
		return m == Material.DIAMOND_BLOCK || m == Material.NETHERITE_BLOCK;
	}
	
	static boolean isCancelling(Block b) {
		return b.getChunk().isLoaded() && isCancelling(b.getType()) && b.isBlockPowered();
	}

	// memory actualizing
	
}
