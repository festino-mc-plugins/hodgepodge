package com.festp.dispenser;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftAnimals;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;
import com.festp.utils.UtilsRandom;

import net.minecraft.server.v1_16_R1.EntityAgeable;
import net.minecraft.server.v1_16_R1.EntityAnimal;

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
		case CRIMSON_FUNGUS:
			feedable = new EntityType[] { EntityType.HOGLIN };
			break;
		case WARPED_FUNGUS:
			feedable = new EntityType[] { EntityType.STRIDER };
			break;
		default:
			break;
		}
		
		if (feedable == null) {
			return;
		}

		List<Animals> delayed_babies = new ArrayList<>();
		boolean animalFound = false;
		for (Entity e : block.getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1)) {
			if (Utils.contains(feedable, e.getType())) {
				Animals animal = (Animals) e;
				if (!animal.isAdult()) {
					delayed_babies.add(animal);
				} else if (!animal.isLoveMode() && animal.canBreed()) {
					// animal.setBreedCause();
					loveanimal = animal;
					animal.setLoveModeTicks(LOVE_TICKS);
					animalFound = true;
					break;
				}
			}
		}
		
		if (!animalFound && delayed_babies.size() > 0) {
			int index = UtilsRandom.getInt(delayed_babies.size());
			Animals animal = delayed_babies.get(index);
			animal.setAge((int)(animal.getAge() * 0.9F));
			animalFound = true;
		}

		if (animalFound) {
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
