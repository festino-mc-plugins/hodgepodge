package com.festp.dispenser;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftAnimals;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;

import net.minecraft.server.v1_15_R1.EntityAgeable;
import net.minecraft.server.v1_15_R1.EntityAnimal;

public class DispenserFeeder {
	public static final int LOVE_TICKS = 600;
	
	Dispenser disp;
	Block block;
	Material food;
	Integer[] food_slots;
	
	public DispenserFeeder(Dispenser disp, Block block, Material food, Integer[] food_slots) {
		this.disp = disp;
		this.block = block;
		this.food = food;
		this.food_slots = food_slots;
	}
	
	public static boolean canBreed() {
		return false;
	}
	
	public void feed() {
		//dispencer test and handle slot
		Inventory inv = disp.getInventory();
		Integer it = null;
		for (int slot = 0; slot < 9; slot++)
			if (inv.getItem(slot) != null && inv.getItem(slot).getType() == food && inv.getItem(slot).getAmount() != food_slots[slot]) {
				it = slot;
			}
		if(it == null) return;
		
		//Breeding
		Animals loveanimal = null;
		
		EntityType[] feedable = null;
		switch (food) {
		case WHEAT:
			feedable = new EntityType[] { EntityType.COW, EntityType.SHEEP };
			break;
		case CARROT:
		case POTATO:
		case BEETROOT:
			feedable = new EntityType[] { EntityType.PIG };
			break;
		case WHEAT_SEEDS:
		case MELON_SEEDS:
		case PUMPKIN_SEEDS:
		case BEETROOT_SEEDS:
			feedable = new EntityType[] { EntityType.CHICKEN };
			break;
		default:
			break;
		}
		
		if (feedable == null) {
			return;
		}
		
		boolean animalFound = false;
		
		for (Entity e : block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1)) {
			if (Utils.contains(feedable, e.getType())) {
				Animals animal = (Animals) e;
				if (!animal.isAdult()) {
					animal.setAge((int)(animal.getAge() * 0.9F));
					animalFound = true;
					break;
				} else if (!animal.isLoveMode() && animal.canBreed()) {
					// animal.setBreedCause();
					loveanimal = animal;
					animal.setLoveModeTicks(LOVE_TICKS);
					animalFound = true;
					break;
				}
			}
		}

		if(animalFound) {
			ItemStack new_item;
			if (inv.getItem(it).getAmount() > 1) {
				new_item = new ItemStack(food, inv.getItem(it).getAmount() - 1);
			} else {
				new_item = new ItemStack(Material.AIR);
			}
			inv.setItem(it, new_item);
		}
	}
}
