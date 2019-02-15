package com.festp.storages;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.mainListener;
import com.festp.storages.Storage.Grab;

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
		File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir, ID + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		//type
		String stype = ymlFormat.getString("type");
		try {
			if(stype.contains("bottomless")) {
				String smat = ymlFormat.getString("material");
				if(Material.valueOf(smat) == null)
					return null;
				
				StorageBottomless st = new StorageBottomless(ID, pl.mainworld.getFullTime(), Material.valueOf(smat));
				int amount = ymlFormat.getInt("amount");
				st.setAmount(amount);
				String grab = ymlFormat.getString("grab");
				st.setGrab(Storage.Grab.valueOf(grab));
				return st;
			}
			else if(stype.contains("multitype")) {
				int lvl = ymlFormat.getInt("level");
				StorageMultitype st = new StorageMultitype(ID, pl.mainworld.getFullTime(), lvl);
				
				int slots = ymlFormat.getInt("slots");
				ArrayList<ItemStack> items = new ArrayList<ItemStack>();
				for (int i = 0; i < slots; i++) {
					ItemStack item = ymlFormat.getItemStack("s." + i);
					items.add(item);
				}
				ItemStack[] itemsList = (ItemStack[])items.toArray(new ItemStack[items.size()]);
				st.getInventory().setContents(itemsList);
				items.clear();

				String grab = ymlFormat.getString("grab_mode"); // TO DO: pick out strings, create function
				if (grab == null)
					grab = Storage.DEFAULT_GRAB_MODE.toString();
				st.setGrab(Storage.Grab.valueOf(grab));
				String dir = ymlFormat.getString("grab_dir");
				if (dir == null)
					dir = StorageMultitype.DEFAULT_DIR.toString();
				st.setGrabDirection(StorageMultitype.GrabDirection.valueOf(dir));
				String sort_mode = ymlFormat.getString("sort_mode");
				if (sort_mode == null)
					sort_mode = StorageMultitype.DEFAULT_MODE.toString();
				st.setSortMode(StorageMultitype.SortMode.valueOf(sort_mode));
				String sort_time = ymlFormat.getString("sort_time");
				if (sort_time == null)
					sort_time = StorageMultitype.DEFAULT_TIME.toString();
				st.setSortTime(StorageMultitype.HandleTime.valueOf(sort_time));
				String stack_time = ymlFormat.getString("stack_time");
				if (stack_time == null)
					stack_time = StorageMultitype.DEFAULT_TIME.toString();
				st.setStackTime(StorageMultitype.HandleTime.valueOf(stack_time));
				return st;
			}
			else return null;
			
		} catch (Exception e) {
			pl.getLogger().severe("Could not load inventory of ID "+ID+"!");
			return null;
		}
	}

	public boolean saveStorage(Storage st) {
		try
		{
			if(st instanceof StorageBottomless)
				saveInventory_Bottomless((StorageBottomless)st);
			else if(st instanceof StorageMultitype)
				saveInventory_Multitype((StorageMultitype)st);
			
			return true;
		} catch (Exception e) {
			pl.getLogger().severe("Could not save inventory of ID "+st.ID+"!");
			e.printStackTrace();
		}
		return false;
	}

	private void saveInventory_Bottomless(StorageBottomless storage) throws Exception {
		File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir,
				storage.ID + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

		ymlFormat.set("type", "bottomless");
		ymlFormat.set("material", storage.getMaterial().toString());
		ymlFormat.set("amount", storage.getAmount());
		ymlFormat.set("grab", storage.canGrab().toString());

		ymlFormat.save(dataFile);
	}

	private void saveInventory_Multitype(StorageMultitype storage) throws Exception {

		File dataFile = new File("plugins"+System.getProperty("file.separator")+mainListener.pluginname+System.getProperty("file.separator")+mainListener.storagesdir,
				storage.getID() + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		
		Inventory inv = storage.getInventory();
		for (int i = 0; i < inv.getContents().length; i++) {
			ItemStack item = inv.getContents()[i];

			ymlFormat.set("s." + i, item);
		}
		ymlFormat.set("type", "multitype");
		ymlFormat.set("slots", inv.getContents().length);
		ymlFormat.set("level", storage.getLvl());
		ymlFormat.set("grab_mode", storage.canGrab().toString());
		ymlFormat.set("grab_dir", storage.getGrabDirection().toString());
		ymlFormat.set("sort_mode", storage.getSortMode().toString());
		ymlFormat.set("sort_time", storage.getSortTime().toString());
		ymlFormat.set("stack_time", storage.getStackTime().toString());
		ymlFormat.save(dataFile);
	}
}
