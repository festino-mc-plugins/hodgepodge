package com.festp.enderchest;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Main;
import com.festp.inventory.ItemFileManager;

public class EnderFileStorage {
    private Main pl;
	
	FileConfiguration ymlFormat;
	
	public EnderFileStorage(Main enderchest) {
		this.pl = enderchest;
	}
	
	private File getFile(String groupName) {
		return new File(Main.getPath() + Main.enderdir + System.getProperty("file.separator") + groupName + ".yml");
	}

	public boolean hasDataFile(String groupName) {
		return getFile(groupName).exists();
	}

	public boolean saveEnderChest(EnderChest ec) {
		String groupName = ec.getGroupName();
		try {
			File dataFile = getFile(groupName);
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

			Inventory inv = ec.getInventory();
			ItemFileManager.save(ymlFormat, inv.getContents());
			
			if (ec.isadmingroup) {
				ymlFormat.set("admin", true);
			} else {
				ymlFormat.set("admin", false);
				ymlFormat.set("owner", ec.getOwner());
				String ingroup = "";
				for (int i = 0; i < ec.group.size(); i++) {
					ingroup += (i > 0 ? "," : "") + ec.group.get(i);
				}
				String invited = "";
				for (int i = 0; i < ec.invited.size(); i++) {
					invited += (i > 0 ? "," : "") + ec.invited.get(i);
				}
				ymlFormat.set("ecgroup", ingroup);
				ymlFormat.set("invited", invited);
			}
			return true;
		} catch (Exception e) {
			pl.getLogger().severe("["+pl.getName()+"] Could not save inventory of "+ groupName +"!");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean loadEnderChest(String groupname){
		Inventory inv = pl.getServer().createInventory(null, InventoryType.ENDER_CHEST, groupname);
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		File dataFile = getFile(groupname);
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		//System.out.println(dataFile.getAbsolutePath());
		boolean admingroup = ymlFormat.getBoolean("admin", false);
		EnderChest ec;
		inv.setContents(ItemFileManager.loadEC(ymlFormat));
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
			File dataFile = new File(Main.getPath() + Main.enderdir, groupname + ".yml");
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
