package com.festp.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.StructureSearchResult;

public class ShriekerAncientCityRadar
{
	private static final Material SHRIEKER_MATERIAL = Material.SCULK_SHRIEKER;
	
	private static final int MIN_INTERVAL = 20;
	private static final int MAX_INTERVAL = 100;
	private static int getInterval(double distance01) {
		double spread = (MAX_INTERVAL - MIN_INTERVAL) * distance01 * distance01;
		return MIN_INTERVAL + (int)Math.round(spread); 
	}
	
	private static final float MIN_PITCH = 0.5f;
	private static final float MAX_PITCH = 2.0f;
	private static float getPitch(double distance01) {
		float spread = (MAX_PITCH - MIN_PITCH) * (float)distance01;
		return MAX_PITCH - spread; 
	}
	
	private static final double MIN_DISTANCE = 64.0;
	private static final double MAX_DISTANCE = 1024.0;
	private static double getDistance01(double distance) {
		double res = (distance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);
		if (res < 0)
			return 0;
		return res; 
	}

	private final Map<UUID, Integer> timeSinceSound = new HashMap<>();
	private final LocationCache locations = new LocationCache();
	
	public void tick() {
		List<UUID> removed = new ArrayList<>();
		for (Map.Entry<UUID, Integer> entry : timeSinceSound.entrySet()) {
			int val = entry.getValue() + 1;
			if (val >= MAX_INTERVAL) {
				removed.add(entry.getKey());
			}
			else {
				entry.setValue(val);
			}
		}
		for (UUID uuid : removed) {
			timeSinceSound.remove(uuid);
		}
		removed.clear();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!hasShrieker(player))
				continue;
			
			Location playerLoc = player.getLocation();
			if (playerLoc.getWorld().getEnvironment() != Environment.NORMAL) // TODO dimension cache
				continue;
			
			Location cityLoc = locations.get(playerLoc);
			if (cityLoc == null) {
				cityLoc = getNearestAncientCity(playerLoc);
				locations.store(playerLoc, cityLoc);
			}
			if (cityLoc == null)
				continue;
			
			double distance = playerLoc.distance(cityLoc);
			if (distance > MAX_DISTANCE)
				continue;
			double distance01 = getDistance01(distance);
			
			int interval = getInterval(distance01);
			int time = timeSinceSound.getOrDefault(player.getUniqueId(), MAX_INTERVAL);
			if (time < interval) {
				continue;
			}
			timeSinceSound.put(player.getUniqueId(), 0);
			
			float pitch = getPitch(distance01);
			playRadarSound(player, pitch);
		}
	}
	
	private void playRadarSound(Player player, float pitch) {
		player.getLocation().getWorld().playSound(player, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.BLOCKS, 1.0f, pitch);
	}

	private Location getNearestAncientCity(Location origin) {
		StructureSearchResult res = origin.getWorld().locateNearestStructure(origin, Structure.ANCIENT_CITY, (int) MAX_DISTANCE, false);
		//System.out.println("locate call: " + res.getLocation());
		return res.getLocation();
	}

	private boolean hasShrieker(Player player) {
		PlayerInventory inv = player.getInventory();
		return isShrieker(inv.getItemInMainHand())
			|| isShrieker(inv.getItemInOffHand())
			|| isShrieker(inv.getHelmet());
	}
	private boolean isShrieker(ItemStack is) {
		return is != null && is.getType() == SHRIEKER_MATERIAL;
	}
}
