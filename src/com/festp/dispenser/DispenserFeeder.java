package com.festp.dispenser;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;
import com.festp.utils.UtilsRandom;

public class DispenserFeeder
{
	public static final int LOVE_TICKS = 600;
	
	Dispenser disp;
	Block block;
	Material food;
	Integer[] foodSlots;
	
	private DispenserFeeder(Dispenser disp, Block block, Material food, Integer[] foodSlots) {
		this.disp = disp;
		this.block = block;
		this.food = food;
		this.foodSlots = foodSlots;
	}
	
	public static DispenserFeeder tryCreate(Dispenser dispenser, ItemStack item, Block frontBlock)
	{
		Material food = item.getType();
		Animals animal = findAnimal(frontBlock, food);
		
		if (animal == null)
    		return null;
		
		Inventory inv = dispenser.getInventory();
		Integer[] foodSlots = new Integer[inv.getSize()];
		for (int i = 0; i < foodSlots.length; i++) {
			foodSlots[i] = inv.getItem(i) == null ? 0 : inv.getItem(i).getAmount();
		}
		return new DispenserFeeder(dispenser, frontBlock, food, foodSlots);
	}
	
	public void feed()
	{
		Inventory inv = disp.getInventory();
		Integer it = null;
		// find food slot - it was empty on event
		for (int slot = 0; slot < 9; slot++)
			if (inv.getItem(slot) != null && inv.getItem(slot).getType() == food && inv.getItem(slot).getAmount() != foodSlots[slot]) {
				it = slot;
			}
		if (it == null)
			return;
		
		Animals animal = findAnimal(block, food);
		if (animal == null)
			return;
		
		Location particleLocation = animal.getLocation().add(0, animal.getEyeHeight(), 0);
		if (!animal.isAdult()) {
			animal.setAge((int)(animal.getAge() * 0.9F));
			animal.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, particleLocation, 7, 0.25, 0.25, 0.25);
			
		}
		else if (!animal.isLoveMode() && animal.canBreed()) {
			// animal.setBreedCause();
			animal.setLoveModeTicks(LOVE_TICKS);
			animal.getWorld().spawnParticle(Particle.HEART, particleLocation, 7, 0.5, 0.5, 0.5);
		}
		
		ItemStack newItem;
		if (inv.getItem(it).getAmount() > 1) {
			newItem = new ItemStack(food, inv.getItem(it).getAmount() - 1);
		} else {
			newItem = new ItemStack(Material.AIR);
		}
		inv.setItem(it, newItem);
	}
	
	private static Animals findAnimal(Block block, Material food)
	{
		EntityType[] feedable = getFeedable(food);
		if (feedable == null)
			return null;
		return findAnimal(block, feedable);
	}
	
	private static Animals findAnimal(Block block, EntityType[] feedable)
	{
		List<Animals> delayedBabies = new ArrayList<>();
		Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
		for (Entity e : block.getWorld().getNearbyEntities(blockCenter, 0.5, 0.5, 0.5)) {
			if (Utils.contains(feedable, e.getType())) {
				Animals animal = (Animals) e;
				if (!animal.isAdult()) {
					delayedBabies.add(animal);
				} else if (!animal.isLoveMode() && animal.canBreed()) {
					return animal;
				}
			}
		}
		
		if (delayedBabies.size() > 0) {
			int index = UtilsRandom.getInt(delayedBabies.size());
			return delayedBabies.get(index);
		}
		return null;
	}
	
	private static EntityType[] getFeedable(Material food)
	{
		if (food == Material.WHEAT)
			return new EntityType[] { EntityType.COW, EntityType.SHEEP };
		
		boolean isAbove1_9 = Utils.GetVersion() >= 10900;
		if (food == Material.CARROT || food == Material.POTATO
				|| isAbove1_9 && food == Material.BEETROOT)
			return new EntityType[] { EntityType.PIG };
		if (food == Material.WHEAT_SEEDS || food == Material.MELON_SEEDS || food == Material.PUMPKIN_SEEDS
				||  isAbove1_9 && food == Material.BEETROOT_SEEDS)
			return new EntityType[] { EntityType.CHICKEN };
		
		boolean isAbove1_16 = Utils.GetVersion() >= 11600;
		if (!isAbove1_16)
			return null;
		if (food == Material.CRIMSON_FUNGUS)
			return new EntityType[] { EntityType.HOGLIN };
		if (food == Material.WARPED_FUNGUS)
			return new EntityType[] { EntityType.STRIDER };
		
		return null;
	}
}
