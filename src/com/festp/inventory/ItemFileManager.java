package com.festp.inventory;

import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ItemFileManager {
	
	public static void save(FileConfiguration ymlFormat, ItemStack[] inv) {
		int size = inv.length;
		ymlFormat.set("slots", size);
		for (int i = 0; i < size; i++) {
			ItemStack item = inv[i];
			ymlFormat.set("s." + i, item);
		}
	}
	
	public static ItemStack[] load(FileConfiguration ymlFormat)
	{
		int slots = ymlFormat.getInt("slots");
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		for (int i = 0; i < slots; i++) {
			ItemStack item = ymlFormat.getItemStack("s." + i);
			items.add(item);
		}
		return items.toArray(new ItemStack[0]);
	}
	
	// temporary backward compatibility
	@Deprecated
	public static ItemStack[] loadEC(FileConfiguration ymlFormat)
	{
		if (!ymlFormat.contains("EnderChestSlot.0"))
			return load(ymlFormat);
		int slots = ymlFormat.getInt("slots");
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		for (int i = 0; i < slots; i++) {
			ItemStack item = ymlFormat.getItemStack("EnderChestSlot." + i);
			items.add(item);
		}
		//if error, load usually
		return items.toArray(new ItemStack[0]);
	}
}
