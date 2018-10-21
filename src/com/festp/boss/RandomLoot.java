package com.festp.boss;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

public class RandomLoot implements Cloneable {

	ItemStack itemStack;
	int min;
	int max;
	double chance;
	static Random random = new Random();
	
	public RandomLoot(ItemStack itemStack, int min, int max, double chance) {
		this.itemStack = itemStack;
		this.min = min;
		this.max = max;
		this.chance = chance;
	}

	public RandomLoot clone() {
		return new RandomLoot(this.itemStack.clone(), this.min, this.max, this.chance);
	}
	
	public ItemStack genLoot() {
		if(random.nextDouble() <= this.chance) {
			ItemStack loot = this.itemStack.clone();
			loot.setAmount(min + random.nextInt(max-min+1));
			return loot;
		}
		else 
			return null;
	}
}
