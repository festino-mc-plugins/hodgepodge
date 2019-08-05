package com.festp.enderchest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Main;

public class EnderFileStorage {
    private Main pl;
	
	File dataFile;
	FileConfiguration ymlFormat;
	
	public EnderFileStorage(Main enderchest) {
		this.pl = enderchest;
		
	}

	public boolean hasDataFile(String groupname) {
		return (new File(Main.getPath() + Main.enderdir + System.getProperty("file.separator") + groupname + ".yml")).exists();
	}
	
	public boolean createDataFile(String groupname, String owner) {
		try {
			dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
			if (!dataFile.exists())	{
				dataFile.createNewFile();
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
				ymlFormat.set("owner", owner);
				
				ymlFormat.save(dataFile);
				
				return true;
			}
			
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not create data file for enderchest group " + groupname + "!");
			e.printStackTrace();
		}
		return false;
	}

	public boolean saveInventory(String groupname, Integer size, ItemStack inventory) {
	
		try {
			dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
			
			ymlFormat.set("EnderChestSlot." + size, inventory);
			
			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not save inventory of "+groupname+"!");
			e.printStackTrace();
		}
		return false;
	}

	public boolean saveInventory(String groupname, Integer size, Integer maxSize, ItemStack inventory, String owner, String group, String invited) {
		try {
			dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

			ymlFormat.set("admin", false);
			ymlFormat.set("owner", owner);
			ymlFormat.set("ecgroup",group);
			ymlFormat.set("invited", invited);
			ymlFormat.set("slots", maxSize);
			
			ymlFormat.set("EnderChestSlot." + size, inventory);
			
			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not save inventory of "+groupname+"!");
			e.printStackTrace();
		}
		return false;
	}

	public boolean saveInventory(String groupname, Integer size, Integer maxSize, ItemStack inventory) {
		try {
			dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

			ymlFormat.set("admin", true);
			ymlFormat.set("slots", maxSize);
			
			ymlFormat.set("EnderChestSlot." + size, inventory);
			
			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not save inventory of "+groupname+"!");
			e.printStackTrace();
		}
		return false;
	}

	public boolean saveEnderChest(EnderChest ec) {
		Inventory inv = ec.getInventory();
		dataFile = new File(Main.getPath() + Main.enderdir, ec.getGroupName() + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item = inv.getContents()[i];
			saveInventory(ec.getGroupName(), i, item);
		}
		//////
		if(ec.isadmingroup)
			saveInventory(ec.getGroupName(), inv.getSize()-1, inv.getContents().length, inv.getContents()[inv.getSize()-1]);
		else {
			String ingroup = "";
			for (int i = 0; i < ec.group.size(); i++) {
				ingroup += (i>0 ? "," : "") + ec.group.get(i);
			}
			String invited = "";
			for (int i = 0; i < ec.invited.size(); i++) {
				invited += (i>0 ? "," : "") + ec.invited.get(i);
			}
			saveInventory(ec.getGroupName(), inv.getSize()-1, inv.getContents().length, inv.getContents()[inv.getSize()-1],ec.getOwner(),ingroup,invited);
		}
		return true;
	}
	
	public boolean loadEnderChest(String groupname){
		Inventory inv = pl.getServer().createInventory(null, InventoryType.ENDER_CHEST, groupname);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		//System.out.println(dataFile.getAbsolutePath());
		boolean admingroup = ymlFormat.getBoolean("admin", false);
		EnderChest ec;
		int slots = ymlFormat.getInt("slots");
		for (int i = 0; i < slots; i++) {
			ItemStack item = ymlFormat.getItemStack("EnderChestSlot." + i);
			items.add(item);
		}
		ItemStack[] itemsList = (ItemStack[])items.toArray(new ItemStack[items.size()]);
		inv.setContents(itemsList);
		if(admingroup) {
			ec = new EnderChest(groupname);
			pl.ecgroup.admingroups.add(ec);
		} else {
			String owner = ymlFormat.getString("owner");
			ec = new EnderChest(groupname, owner, false);
			String[] ingroup = ymlFormat.getString("ecgroup") != null ? ymlFormat.getString("ecgroup").split(",") : new String[0];
			for (int i = 0; i < ingroup.length; i++) {
				ec.group.add(ingroup[i]);
			}
			String[] invited = ymlFormat.getString("invited") != null ? ymlFormat.getString("invited").split(",") : new String[0];
			for (int i = 0; i < invited.length; i++) {
				ec.invited.add(invited[i]);
			}
			pl.ecgroup.groups.add(ec);
		}
		ec.setInventory(inv);
		items.clear();
		return true;
	}

	public boolean deleteDataFile(String groupname) {
		try {
			dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
			if (dataFile.exists())
			{
				dataFile.delete();
				return true;
			}
			
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not delete data file " + groupname + "!");
			e.printStackTrace();
		}
		return false;
	}
}
