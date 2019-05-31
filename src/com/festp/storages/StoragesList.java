package com.festp.storages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.storages.Storage.StorageType;
import com.festp.utils.Utils;

public class StoragesList {
	private List<Storage> storages = new ArrayList<>();
	static long unload_time = 20*60*60*1;
	
	public void tryUnload(long cur_time) {
		long temp_time = unload_time - cur_time;
		for (int i = storages.size()-1; i >= 0; i--) {
			if (storages.get(i).last_load + temp_time < 0) {
				storages.get(i).saveToFile();
				storages.remove(i);
			}
		}
	}
	
	public void saveStorages() {
		for (int i = 0; i < storages.size(); i++) {
			Storage st = storages.get(i);
			if (st.wasEdited()) {
				st.saveToFile();
				st.setEdited(false);
			}
		}
	}

	/** @return non-<b>null</b> value */
	public List<Storage> getAll() {
		return storages;
	}
	
	public Storage get(int id) {
		if (id < 0) return null;
		for (Storage st : storages) {
			if (st.getID() == id) {
				return st;
			}
		}
		Storage st = load(id);
		return st;
	}

	/**Unloads storage from memory avoid saving*/
	public void remove(int id) {
		for (int i = 0; i < storages.size(); i++) {
			Storage st = storages.get(i);
			if (st.getID() == id) {
				storages.remove(i);
				return;
			}
		}
	}
	
	public Storage findByInventory(Inventory inv) {
		for (Storage st : storages) {
			if (st instanceof StorageMultitype) {
				if ( Utils.equal_invs(st.getInventory(), inv) )
					return st;
			}
		}
		return null;
	}
	
	/**Always adds new list element*/
	private Storage load(int ID) {
		Storage loaded = Storage.loadFromFile(ID);
		if (loaded == null)
			return null;
		storages.add(loaded);
		return loaded;
	}
}
