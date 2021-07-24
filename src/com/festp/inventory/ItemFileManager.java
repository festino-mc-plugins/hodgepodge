package com.festp.inventory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.festp.Logger;

public class ItemFileManager {

	private static final String VALUE_STR = "v: ";
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
			String res = ymlFormat.saveToString();
			int startIndex = res.indexOf(VALUE_STR) + VALUE_STR.length();
			int endIndex = res.indexOf('\n', startIndex);
			int index = res.indexOf('\r', startIndex);
			if (index >= 0 && index < endIndex || endIndex < 0)
				endIndex = index;
			index = res.length();
			if (index >= 0 && index < endIndex || endIndex < 0)
				endIndex = index;
			version = Integer.parseInt(res.substring(startIndex, endIndex));
			init = true;
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
	
	public static String updateVersion(String yamlStr)
	{
		try (StringReader reader = new StringReader(yamlStr)) {
			try (StringWriter writer = new StringWriter()) {
				updateVersion(reader, writer);
				return writer.toString();
			} catch (IOException e) {
				Logger.severe("Couldn't update String version:\n" + "\"" + yamlStr + "\"");
			}
		}
		return yamlStr;
	}
	
	public static void backupIfUpdateVersion(File file)
	{
		File backupFile = getBackupPath(file);
		file.renameTo(backupFile);
		try {
			file.createNewFile();
			FileReader reader = new FileReader(backupFile);
			FileWriter writer = new FileWriter(file);
			boolean changed = updateVersion(reader, writer);
			if (!changed) {
				file.delete();
				backupFile.renameTo(file);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static boolean updateVersion(Reader src, Writer dst)
	{
		boolean changed = false;
		try (BufferedReader br = new BufferedReader(src)) {
			try (BufferedWriter bw = new BufferedWriter(dst)) {
		        String line;
		        boolean checkVer = false;
		        while ((line = br.readLine()) != null) {
		        	if (line.endsWith("==: org.bukkit.inventory.ItemStack")) {
		        		checkVer = true;
		        	} else if (checkVer) {
			        	checkVer = false;
		        		int index = line.indexOf(VALUE_STR);
		        		if (index < 0) {
		        			// invalid format
		        		}
		        		index += VALUE_STR.length();
		        		int version = Integer.parseInt(line.substring(index));
		        		if (version > getItemVersion()) {
		        			changed = true;
		        			line = line.substring(0, index) + getItemVersion();
		        		}
		        	}
		        	bw.write(line);
		        	bw.newLine();
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return changed;
	}
	
	public static ItemLoadResult load(FileConfiguration ymlFormat)
	{
		int slots = ymlFormat.getInt("slots");
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		boolean errored = false;
		for (int i = 0; i < slots; i++) {
			try {
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
				ItemStack item = ymlFormat.getItemStack("EnderChestSlot." + i);
				items.add(item);
			} catch(Exception ex) {
				errored = true;
			}
		}
		ItemStack[] inv = items.toArray(new ItemStack[0]);
		return new ItemLoadResult(inv, errored);
	}
	
	/** no backup */
	public static File getBackupPath(File dataFile) {
		String dataDir = dataFile.getParent();
		String backupDir = dataDir + SEP + "backup";
		new File(backupDir).mkdir();
		String prefix = "";
		int i = 0;
		File backupFile;
		while (true) {
			backupFile = new File(backupDir + SEP + prefix + dataFile.getName());
			if (!backupFile.exists()) {
				break;
			}
			i++;
			prefix = "(" + i + ")";
		}
		return backupFile;
	}
	
	public static File backup(File dataFile) {
		try {
			Logger.warning("Backing up file " + dataFile);
			File backupFile = getBackupPath(dataFile);
			Files.copy(dataFile.toPath(), backupFile.toPath());
			return backupFile;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
