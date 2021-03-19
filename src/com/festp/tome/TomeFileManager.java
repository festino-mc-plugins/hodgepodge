package com.festp.tome;

import java.io.Reader;
import java.io.StringReader;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.festp.inventory.ItemFileManager;

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
		return ItemFileManager.load(ymlFormat).contents;
	}

	public static String saveInventory(ItemStack[] inv) {
		String data = "";
		Reader reader = new StringReader(data);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
		ItemFileManager.save(ymlFormat, inv);;
		return ymlFormat.saveToString();
	}
}
