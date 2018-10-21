package com.festp.storages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.mainListener;
import com.festp.storages.BottomlessInventory.Grab;
import com.festp.storages.Storage.StorageType;

public class StoragesFileManager {
	private mainListener pl;
	
	public static int nextID = 1;
	
	public StoragesFileManager(mainListener pl) {
		this.pl = pl;
		Storage.pl = pl;
	}

	public boolean hasDataFile(int ID) {
		return (new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir+System.getProperty("file.separator")+ID+".yml")).exists();
	}
	
	public boolean createDataFile(int ID) {
		try {
			File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
			if (!dataFile.exists())	{
				dataFile.createNewFile();
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
				
				ymlFormat.save(dataFile);
				
				return true;
			}
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not create data file with ID " + ID + "!");
			e.printStackTrace();
		}
		return false;
	}

	public boolean deleteDataFile(int ID) {
		try {
			File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
			if (dataFile.exists())
			{
				dataFile.delete();
				return true;
			}
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not delete data file with ID " + ID + "!");
			e.printStackTrace();
		}
		return false;
	}
	
	public Storage loadStorage(int ID){
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		//type
		String stype = ymlFormat.getString("type");
		try {
			if(stype.contains("bottomless")) {
				String smat = ymlFormat.getString("material");
				if(Material.valueOf(smat) == null)
					return null;
				
				Storage st = new Storage(ID, pl.mainworld.getFullTime(), Material.valueOf(smat));
				int amount = ymlFormat.getInt("amount");
				st.unlim_inv.setAmount(amount);
				String grab = ymlFormat.getString("grab");
				st.unlim_inv.setGrab(Grab.valueOf(grab));
				return st;
			}
			else if(stype.contains("multitype")) {
				Storage st = new Storage(ID, pl.mainworld.getFullTime());
				
				int slots = ymlFormat.getInt("slots");
				for (int i = 0; i < slots; i++) {
					ItemStack item = ymlFormat.getItemStack("s." + i);
					items.add(item);
				}
				ItemStack[] itemsList = (ItemStack[])items.toArray(new ItemStack[items.size()]);
				st.getInventory().setContents(itemsList);
				items.clear();
				return st;
			}
			else return null;
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not load inventory of ID "+ID+"!");
			return null;
		}
	}

	public boolean saveStorage(Storage st) {
		if(st.getType() == StorageType.BOTTOMLESS) {
			saveInventory_Bottomless(st.ID, st.unlim_inv);
		}
		else if(st.getType() == StorageType.MULTITYPE) {
			try {
				File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, st.ID + ".yml");
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
				
				Inventory inv = st.getInventory();
				for (int i = 0; i < inv.getContents().length; i++) {
					ItemStack item = inv.getContents()[i];

					ymlFormat.set("s." + i, item);
					//saveItemStack_Multi(st.ID, i, item);
				}
				ymlFormat.set("type", "multitype");
				
				ymlFormat.set("slots", inv.getContents().length);
				ymlFormat.save(dataFile);
			} catch (IOException e) {
				pl.getLogger().severe("Could not save inventory of ID "+st.ID+"!");
				e.printStackTrace();
			}
			//////
			//saveItemStack_LastMulti(st.ID, inv.getSize()-1, inv.getContents().length, inv.getContents()[inv.getSize()-1]);
		}
		
		return true;
	}

	private boolean saveInventory_Bottomless(int ID, BottomlessInventory inventory) {
	
		try {
			File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

			ymlFormat.set("type", "bottomless");
			ymlFormat.set("material", inventory.getMaterial().toString());
			ymlFormat.set("amount", inventory.getAmount());
			ymlFormat.set("grab", inventory.canGrab().toString());

			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not save inventory of ID "+ID+"!");
			e.printStackTrace();
		}
		return false;
	}

	private boolean saveItemStack_Multi(int ID, Integer size, ItemStack inventory) {
	
		try {
			File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
			
			ymlFormat.set("s." + size, inventory);
			
			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not save inventory of ID "+ID+"!");
			e.printStackTrace();
		}
		return false;
	}

	private boolean saveItemStack_LastMulti(int ID, Integer size, Integer maxSize, ItemStack inventory) {
		try {
			File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

			ymlFormat.set("type", "multitype");
			
			ymlFormat.set("slots", maxSize);
			
			ymlFormat.set("s." + size, inventory);
			
			ymlFormat.save(dataFile);
			return true;
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not save inventory of "+ID+"!");
			e.printStackTrace();
		}
		return false;
	}
}
