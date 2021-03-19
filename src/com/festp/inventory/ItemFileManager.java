package com.festp.inventory;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ItemFileManager {

	private static final String SEP = System.getProperty("file.separator");
	private static boolean init = false;
	private static int version = 0;
	
	public static int getItemVersion()
	{
		if (!init) {
			String data = "";
			Reader reader = new StringReader(data);
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(reader);
			ymlFormat.set("a", new ItemStack(Material.APPLE));
			version = ymlFormat.getInt("a.v");
		}
		return version;
	}
	
	public static void save(FileConfiguration ymlFormat, ItemStack[] inv) {
		int size = inv.length;
		ymlFormat.set("slots", size);
		for (int i = 0; i < size; i++) {
			ymlFormat.set("s." + i, inv[i]);
		}
	}
	
	public static ItemLoadResult load(FileConfiguration ymlFormat)
	{
		int slots = ymlFormat.getInt("slots");
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		boolean errored = false;
		for (int i = 0; i < slots; i++) {
			try {
				if (ymlFormat.contains("s." + i)) {
					int version = ymlFormat.getInt("s." + i + ".v");
					if (version > getItemVersion()) {
						ymlFormat.set("s." + i + ".v", getItemVersion());
						errored = true;
					}
				}
				ItemStack item = ymlFormat.getItemStack("s." + i);
				items.add(item);
			} catch(Exception ex) {
				errored = true;
			}
		}
		ItemStack[] inv = items.toArray(new ItemStack[0]);
		return new ItemLoadResult(inv, errored);
	}
	
	// temporary backward compatibility
	@Deprecated
	public static ItemLoadResult loadEC(FileConfiguration ymlFormat)
	{
		if (!ymlFormat.contains("EnderChestSlot"))
			return load(ymlFormat);
		
		int slots = ymlFormat.getInt("slots");
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		boolean errored = false;
		for (int i = 0; i < slots; i++) {
			try {
				if (ymlFormat.contains("EnderChestSlot." + i)) {
					int version = ymlFormat.getInt("EnderChestSlot." + i + ".v");
					if (version > getItemVersion()) {
						ymlFormat.set("EnderChestSlot." + i + ".v", getItemVersion());
						errored = true;
					}
				}
				ItemStack item = ymlFormat.getItemStack("EnderChestSlot." + i);
				items.add(item);
			} catch(Exception ex) {
				errored = true;
			}
		}
		ItemStack[] inv = items.toArray(new ItemStack[0]);
		return new ItemLoadResult(inv, errored);
	}
	
	public static void backup(File dataFile) {
		String dataDir = dataFile.getParent();
		String backupDir = dataDir + SEP + "backup";
		new File(backupDir).mkdir();
		try {
			boolean notCreated = true;
			String prefix = "";
			while (notCreated) {
				notCreated = false;
				prefix += "_";
				File backupFile = new File(backupDir + SEP + prefix + dataFile.getName());
				try {
					Files.copy(dataFile.toPath(), backupFile.toPath());
				} catch (FileAlreadyExistsException e) {
					notCreated = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
