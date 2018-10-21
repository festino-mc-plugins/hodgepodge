package com.festp.storages;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;
import com.festp.mainListener;
import com.festp.storages.Storage.StorageType;

import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.NBTTagInt;
import net.minecraft.server.v1_13_R2.NBTTagList;
import net.minecraft.server.v1_13_R2.NBTTagString;

public class Storage {
	public enum StorageType {BOTTOMLESS, MULTITYPE};
	public static final Material UNDEFINED_MATERIAL = Material.AIR;
	public static final Material GRAB_NOTHING_MATERIAL = Material.BARRIER;
	public static final Material GRAB_PLAYERNT_MATERIAL = Material.HOPPER;
	public static final Material GRAB_ALL_MATERIAL = Material.PLAYER_HEAD;
	
	public static mainListener pl;
	
	int ID;
	private StorageType type;
	private Inventory inventory;
	public Inventory grabbing_inventory = null;
	public BottomlessInventory unlim_inv;
	long start_session, last_load;
	private boolean edited = false;
	
	public static ItemStack[] empty_inventory = new ItemStack[54];
	
	public Storage(int ID, long full_time) {
		this.ID = ID;
		start_session = full_time;
		last_load = full_time;
		
		this.type = StorageType.MULTITYPE;
		inventory = pl.getServer().createInventory(null, 27, "Storage");
	}
	
	public Storage(int ID, long full_time, Material material) {
		this.ID = ID;
		this.type = StorageType.BOTTOMLESS;
		start_session = full_time;
		last_load = full_time;
		
		unlim_inv = new BottomlessInventory(this);
		unlim_inv.setMaterial(material);
	}
	
	public StorageType getType() {
		return type;
	}
	
	public Inventory getInventory() {
		if(type == StorageType.BOTTOMLESS)
			return unlim_inv.getPage();
		else
			return inventory;
	}
	
	public void setInventory(Inventory inv) {
		inventory = inv;
	}
	
	public boolean wasEdited() {
		return edited;
	}

	public void setEdited(boolean new_val) {
		edited = new_val;
	}
	
	public static Storage loadFromFile(int ID) {
		return pl.ststorage.loadStorage(ID);
	}
	
	public void saveToFile() {
		pl.ststorage.saveStorage(this);
	}
	
	public static Storage getByItemStack(ItemStack storage) {
		int id = getID(storage);
		if(id < 0) return null;
		return pl.stlist.get(id);
	}
	
	public static int getID(ItemStack storage) {
		if(storage == null)
			return -1;
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(storage);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null)
        	return -1;
        if( compound.hasKey("StorageID") )
        	return compound.getInt("StorageID");
        //nmsStack.setTag(compound);
		return -1;
	}
	
	public static boolean isStorage(ItemStack item) {
		return getID(item) >= 0;
	}
	
	public void drop(Location loc) {
		setEdited(true);
		if(type == StorageType.BOTTOMLESS)
			unlim_inv.drop(loc);
		else {
			int i = 0;
			for(ItemStack stack : inventory.getContents()) {
				Utils.drop(loc, stack, 1);
				inventory.setItem(i, null);
				i++;
			}
		}
	}

	public boolean isAllowed(ItemStack item) {
		if(type == StorageType.BOTTOMLESS) {
			if(item != null && Storage.getID(item) < 0 && (item.getType() == unlim_inv.getMaterial() || item.getType() == Material.AIR))
				if(!Utils.isRenamed(item))
					return true;
			return false;
		}
		return Storage.getID(item) < 0;
	}

	public boolean isEmpty() {
		if(type == StorageType.BOTTOMLESS)
			return unlim_inv.getAmount() == 0;
		else {
			int i = 0;
			for(ItemStack stack : inventory.getContents())
				if(stack != null)
					return false;
			return true;
		}
	}

	public int getID() {
		return ID;
	}
}
