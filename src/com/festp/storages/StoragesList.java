package com.festp.storages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;
import com.festp.storages.Storage.StorageType;

public class StoragesList {
	public List<Storage> storages = new ArrayList<>();
	static long unload_time = 20*60*60*1;
	
	public void tryUnload(long cur_time) {
		long temp_time = unload_time - cur_time;
		for(int i = storages.size()-1; i >= 0; i--) {
			if(storages.get(i).last_load + temp_time < 0) {
				storages.get(i).saveToFile();
				storages.remove(i);
			}
		}
	}
	
	public void saveStorages() {
		for(int i = 0; i < storages.size(); i++) {
			Storage st = storages.get(i);
			if( st.wasEdited() ) {
				st.saveToFile();
				st.setEdited(false);
			}
		}
	}

	public Storage get(int id) {
		if(id < 0) return null;
		for(Storage st : storages) {
			if( st.ID == id ) {
				return st;
			}
		}
		Storage st = load(id); 
		return st;
	}
	
	public void tryLoad(int id) {
		get(id);
	}
	
	public Storage findByInventory(Inventory inv) {
		for(Storage st : storages) {
			if(st.getType() == StorageType.MULTITYPE) {
				if( Utils.equal_invs(st.getInventory(), inv))
					return st;
			} else if(st.getType() == StorageType.BOTTOMLESS) {
				if( st.unlim_inv.containsPage(inv))
					return st;
			}
		}
		return null;
	}
	
	public Storage load(int ID) {
		Storage loaded = Storage.loadFromFile(ID);
		if(loaded == null)
			return null;
		storages.add(loaded);
		return loaded;
	}
}
