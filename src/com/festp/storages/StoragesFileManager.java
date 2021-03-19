package com.festp.storages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.festp.Main;
import com.festp.inventory.ItemFileManager;
import com.festp.inventory.ItemLoadResult;
import com.festp.utils.TimeUtils;
import com.festp.utils.Utils;

public class StoragesFileManager {
	
	public static int nextID = 1;
	private static final String SEP = System.getProperty("file.separator");
	
	private Main pl;
	
	
	public StoragesFileManager(Main pl) {
		this.pl = pl;
	}

	public static boolean hasDataFile(int ID) {
		return (new File(Main.getPath() + Main.storagesdir + SEP + ID + ".yml")).exists();
	}
	
	public static List<Integer> getIDList()
	{
		File STpluginFolder = new File(Main.getPath() + Main.storagesdir);
		if (STpluginFolder.exists() == false) {
    		STpluginFolder.mkdir();
    	}
		List<Integer> list = new ArrayList<>();
		for(String s : STpluginFolder.list()) {
			if(s.length() < 5) continue;
			s = s.substring(0, s.length()-4);
			try {
				int ID = Integer.parseInt(s);
				list.add(ID);
			} catch (Exception ex) {
				Utils.printError("Storages: Wrong file in directory: " + s);
			}
		}
		return list;
	}
	
	public boolean createDataFile(int ID) {
		try {
			File dataFile = new File(Main.getPath() + Main.storagesdir, ID + ".yml");
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
			File dataFile = new File(Main.getPath() + Main.storagesdir, ID + ".yml");
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
		File dataFile = new File(Main.getPath() + Main.storagesdir, ID + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		//type
		String stype = ymlFormat.getString("type");
		try {
			if (stype.contains("bottomless")) {
				String smat = ymlFormat.getString("material");
				if(Material.valueOf(smat) == null)
					return null;
				
				StorageBottomless st = new StorageBottomless(ID, TimeUtils.getTicks(), Material.valueOf(smat));
				int amount = ymlFormat.getInt("amount");
				st.setAmount(amount);
				String grab = ymlFormat.getString("grab");
				st.setGrab(Storage.Grab.valueOf(grab));
				return st;
			}
			else if (stype.contains("multitype")) {
				int lvl = ymlFormat.getInt("level");
				StorageMultitype st = new StorageMultitype(ID, TimeUtils.getTicks(), lvl);
				
				ItemLoadResult res = ItemFileManager.load(ymlFormat);
				if (!res.valid)
					ItemFileManager.backup(dataFile);
				st.getInventory().setContents(res.contents);

				String grab = ymlFormat.getString("grab_mode"); // TO DO: pick out strings, create function
				if (grab == null)
					grab = Storage.DEFAULT_GRAB_MODE.toString();
				try { st.setGrab(Storage.Grab.valueOf(grab)); } catch (IllegalArgumentException e) {}

				String grab_filter = ymlFormat.getString("grab_filter");
				if (grab_filter == null)
					grab_filter = StorageMultitype.DEFAULT_FILTER.toString();
				try { st.setGrabFilter(StorageMultitype.GrabFilter.valueOf(grab_filter)); } catch (IllegalArgumentException e) {}
				
				String grab_dir = ymlFormat.getString("grab_dir");
				if (grab_dir == null)
					grab_dir = StorageMultitype.DEFAULT_DIR.toString();
				try { st.setGrabDirection(StorageMultitype.GrabDirection.valueOf(grab_dir)); } catch (IllegalArgumentException e) {}
				
				String sort_mode = ymlFormat.getString("sort_mode");
				if (sort_mode == null)
					sort_mode = StorageMultitype.DEFAULT_MODE.toString();
				try { st.setSortMode(StorageMultitype.SortMode.valueOf(sort_mode)); } catch (IllegalArgumentException e) {}
				
				String sort_time = ymlFormat.getString("sort_time");
				if (sort_time == null)
					sort_time = StorageMultitype.DEFAULT_TIME.toString();
				try { st.setSortTime(StorageMultitype.HandleTime.valueOf(sort_time)); } catch (IllegalArgumentException e) {}
				
				String stack_time = ymlFormat.getString("stack_time");
				if (stack_time == null)
					stack_time = StorageMultitype.DEFAULT_TIME.toString();
				try { st.setStackTime(StorageMultitype.HandleTime.valueOf(stack_time)); } catch (IllegalArgumentException e) {}
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
			if (st instanceof StorageBottomless)
				saveInventory_Bottomless((StorageBottomless)st);
			else if (st instanceof StorageMultitype)
				saveInventory_Multitype((StorageMultitype)st);
			
			return true;
		} catch (Exception e) {
			pl.getLogger().severe("Could not save inventory of ID "+st.ID+"!");
			e.printStackTrace();
		}
		return false;
	}

	private void saveInventory_Bottomless(StorageBottomless storage) throws Exception {
		File dataFile = new File(Main.getPath() + Main.storagesdir,
				storage.ID + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);

		ymlFormat.set("type", "bottomless");
		ymlFormat.set("material", storage.getMaterial().toString());
		ymlFormat.set("amount", storage.getAmount());
		ymlFormat.set("grab", storage.canGrab().toString());

		ymlFormat.save(dataFile);
	}

	private void saveInventory_Multitype(StorageMultitype storage) throws Exception {

		File dataFile = new File(Main.getPath() + Main.storagesdir,
				storage.getID() + ".yml");
		FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(dataFile);
		ymlFormat.set("type", "multitype");
		ymlFormat.set("level", storage.getLvl());
		ymlFormat.set("grab_mode", storage.canGrab().toString());
		ymlFormat.set("grab_filter", storage.getGrabFilter().toString());
		ymlFormat.set("grab_dir", storage.getGrabDirection().toString());
		ymlFormat.set("sort_mode", storage.getSortMode().toString());
		ymlFormat.set("sort_time", storage.getSortTime().toString());
		ymlFormat.set("stack_time", storage.getStackTime().toString());
		
		ItemFileManager.save(ymlFormat, storage.getInventory().getContents());
		
		ymlFormat.save(dataFile);
	}
}
