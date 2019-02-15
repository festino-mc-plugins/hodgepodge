package com.festp.storages;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.festp.mainListener;

import net.minecraft.server.v1_13_R2.NBTTagCompound;

public abstract class Storage
{
	public enum StorageType {BOTTOMLESS, MULTITYPE};
	public enum Grab {NOTHING, NO_PLAYER, ALL}
	public static Grab DEFAULT_GRAB_MODE = Grab.NOTHING;
	protected Grab grab_mode = DEFAULT_GRAB_MODE;
	
	public static mainListener pl;
	
	protected int ID;
	protected StorageType type;
	long start_session, last_load;
	protected boolean edited = false;
	public static final String NBT_KEY = "StorageID";
	
	public Storage(int ID, long full_time)
	{
		this.ID = ID;
		start_session = full_time;
		last_load = full_time;
	}

	public int getID() {
		return ID;
	}

	@Deprecated // if I am going to remove StorageType
	public StorageType getType() {
		return type;
	}
	
	@Deprecated // if I am going to remove StorageType
	public static StorageType getType(ItemStack item) {
		if (!isStorage(item))
			return null;
		if (item.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0)
			return StorageType.BOTTOMLESS;
		if (item.getEnchantmentLevel(Enchantment.DIG_SPEED) > 0)
			return StorageType.MULTITYPE;
		return null;
	}

	/** Any edit of Storage means only that it needs to be saved. */
	public boolean wasEdited() {
		return edited;
	}

	/** Any edit of Storage means only that it needs to be saved. */
	public void setEdited(boolean new_val) {
		edited = new_val;
	}
	
	public void setGrab(Grab grab_mode) {
		this.grab_mode = grab_mode;
	}
	
	public Grab canGrab() {
		return grab_mode;
	}
	
	public boolean canGrab(Inventory inv) {
		if (!isGrabbableInventory(inv))
			return false;
		
		Grab min_grab = Grab.NO_PLAYER;
		if(inv instanceof PlayerInventory)
			min_grab = Grab.ALL;
		return canGrab() == min_grab || min_grab == Grab.NO_PLAYER && canGrab() == Grab.ALL;
	}
	
	public static boolean isGrabbableInventory(Inventory inv) {
		return inv != null && !(inv.getType() == InventoryType.ANVIL || inv.getType() == InventoryType.BEACON || inv.getType() == InventoryType.BREWING
				 || inv.getType() == InventoryType.CRAFTING || inv.getType() == InventoryType.WORKBENCH
				 || inv.getType() == InventoryType.ENCHANTING || inv.getType() == InventoryType.FURNACE || inv.getType() == InventoryType.MERCHANT);
	}
	
	public abstract Inventory getInventory();
	
	public abstract boolean isEmpty();

	public abstract boolean isAllowed(ItemStack item);

	public abstract void drop(Location from);
	
	public void saveToFile() {
		pl.ststorage.saveStorage(this);
	}
	
	public static Storage loadFromFile(int ID) {
		return pl.ststorage.loadStorage(ID);
	}
	
	public static Storage getByItemStack(ItemStack storage) {
		int id = getID(storage);
		if(id < 0) return null;
		return pl.stlist.get(id);
	}
	
	public static boolean isStorage(ItemStack item) {
		return getID(item) >= 0;
	}
	
	public static int getID(ItemStack storage) {
		if(storage == null)
			return -1;
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(storage);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null)
        	return -1;
        if( compound.hasKey(NBT_KEY) )
        	return compound.getInt(NBT_KEY);
		return -1;
	}
	
	public static ItemStack setID(ItemStack i, int ID) {
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        //it guarantee not to stack
        compound.setInt(NBT_KEY, ID);
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
}
