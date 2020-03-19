package com.festp.tome;

import java.io.Reader;
import java.io.StringReader;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class TomeFileManager {
	
	public static ItemStack[] loadInventory(String ymlStr)
	{
		Reader reader = new StringReader(ymlStr);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
		try {
			ymlFormat.loadFromString(ymlStr);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		int slots = ymlFormat.getInt("slots");
		ItemStack[] items = new ItemStack[slots];
		for (int i = 0; i < slots; i++) {
			ItemStack item = ymlFormat.getItemStack("s." + i);
			items[i] = item;
		}
		return items;
	}

	public static String saveInventory(ItemStack[] inv) {
		String data = "";
		Reader reader = new StringReader(data);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
		
		for (int i = 0; i < inv.length; i++) {
			ItemStack item = inv[i];

			ymlFormat.set("s." + i, item);
		}
		ymlFormat.set("slots", inv.length);
		return ymlFormat.saveToString();
	}
}
