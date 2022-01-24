package com.festp.storages;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.DelayedTask;
import com.festp.Pair;
import com.festp.TaskList;
import com.festp.utils.NBTUtils;
import com.festp.utils.Utils;

import net.minecraft.nbt.NBTTagCompound;


public abstract class Storage
{
	public enum StorageType {BOTTOMLESS, MULTITYPE};
	public enum Grab {NOTHING, NEW, NO_PLAYER, ALL}
	public static final Grab DEFAULT_GRAB_MODE = Grab.NOTHING;
	protected Grab grab_mode = DEFAULT_GRAB_MODE;
	
	protected int ID;
	protected StorageType type;
	long start_session, last_load;
	protected boolean edited = false;
	public static final String NBT_KEY = "StorageID";
	protected Inventory external_inv = null; //also null if the Storage is dropped into the world
	
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
		setEdited(true);
		this.grab_mode = grab_mode;
		grab();
	}
	
	public Grab canGrab() {
		return grab_mode;
	}
	
	public boolean canGrab(Inventory inv) {
		if (!isGrabbableInventory(inv))
			return false;
		
		Grab min_grab = Grab.NO_PLAYER;
		if(inv.getType() == InventoryType.PLAYER)
			min_grab = Grab.ALL;
		return canGrab() == min_grab || min_grab == Grab.NO_PLAYER && canGrab() == Grab.ALL;
	}
	
	public static boolean isGrabbableInventory(Inventory inv) {
		return inv != null && !(inv.getType() == InventoryType.ANVIL || inv.getType() == InventoryType.BEACON || inv.getType() == InventoryType.BREWING
				 || inv.getType() == InventoryType.CRAFTING || inv.getType() == InventoryType.WORKBENCH
				 || inv.getType() == InventoryType.ENCHANTING || inv.getType() == InventoryType.FURNACE || inv.getType() == InventoryType.MERCHANT);
	}
	
	public Inventory getExternalInventory() {
		return external_inv;
	}
	
	public void setExternalInventory(Inventory inv) {
		external_inv = inv;
		TaskList.add(grab_task);
	}
	
	DelayedTask grab_task = new DelayedTask(1, new Runnable() { @Override
		public void run() { grab(); }
	});

	public void grab()
	{
		grabInventory(external_inv);
	}
	
	public abstract Inventory getInventory();
	public abstract Inventory getMenu();
	
	public abstract boolean isEmpty();

	public abstract boolean isAllowed(ItemStack item);

	public abstract void drop(Location from);

	/** on Item pickup - do not change <b>stack</b>
	  * @return grabbed amount*/
	public abstract int grabItemStack(ItemStack stack);
	
	/** @return <b>true</b> if <b>inv</b> has been updated, and new inventory contents*/
	public abstract Pair<Boolean, ItemStack[]> grabInventory(ItemStack[] inv);

	/** uses grabInventory(ItemStack[] inv) if canGrab(Inventory) this*/
	public void grabInventory(Inventory inv) {
		if (!canGrab(inv))
			return;
		
		Pair<Boolean, ItemStack[]> result = grabInventory(inv.getStorageContents());
		if (result.first) {
			inv.setStorageContents(result.second);
			Utils.getPlugin().sthandler.delayedUpdate(inv);
		}
	}

	/** uses grabInventory(ItemStack[] inv), ignoring canGrab(Inventory)*/
	public void grabAnyInventory(Inventory inv) {
		if (inv == null)
			return;
		
		Pair<Boolean, ItemStack[]> result = grabInventory(inv.getStorageContents());
		if (result.first) {
			inv.setStorageContents(result.second);
			Utils.getPlugin().sthandler.delayedUpdate(inv);
		}
	}
	
	public void saveToFile() {
		Utils.getPlugin().ststorage.saveStorage(this);
	}
	
	public static Storage loadFromFile(int ID) {
		return Utils.getPlugin().ststorage.loadStorage(ID);
	}
	
	public static Storage getByItemStack(ItemStack storage) {
		int id = getID(storage);
		if(id < 0) return null;
		return Utils.getPlugin().stlist.get(id);
	}
	
	public static boolean isStorage(ItemStack item) {
		return getID(item) >= 0;
	}
	
	public static int getID(ItemStack storage) {
		if(storage == null)
			return -1;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(storage);
        NBTTagCompound compound = NBTUtils.getTag(nmsStack);
        if (compound == null)
        	return -1;
        if (NBTUtils.hasKey(compound, NBT_KEY))
        	return NBTUtils.getInt(compound, NBT_KEY);
		return -1;
	}
	
	public static ItemStack setID(ItemStack i, int ID) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = NBTUtils.getTag(nmsStack);
        if (compound == null) {
            compound = new NBTTagCompound();
            NBTUtils.setTag(nmsStack, compound);
            compound = NBTUtils.getTag(nmsStack);
        }
        //it guarantee not to stack
        NBTUtils.setInt(compound, NBT_KEY, ID);
        NBTUtils.setTag(nmsStack, compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
	
	public String toString() {
		return "Storage(ID="+ID+")";
	}

	public ItemStack getItemStack() {
		if (this instanceof StorageBottomless)
			return Utils.getPlugin().stcraft.getBottomless(ID);
		if (this instanceof StorageMultitype)
			return Utils.getPlugin().stcraft.getMultitype(ID, ((StorageMultitype)this).getLvl());
		return null;
	}
	
	public void updateLastLoad(long new_val) {
		last_load = new_val;
	}
}
