package com.festp.storages;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Pair;
import com.festp.Utils;
import com.festp.menu.InventoryMenu;
import com.festp.storages.Storage.StorageType;

public class StorageBottomless extends Storage
{
	public static final Material UNDEFINED_MATERIAL = Material.AIR;
	private Material allowed_type;
	private Integer amount = 0;
	public MenuBottomless menu;
	
	public StorageBottomless(int ID, long full_time, Material material)
	{
		super(ID, full_time);
		this.type = StorageType.BOTTOMLESS;
		setMaterial(material);
		
		menu = new MenuBottomless(this);
	}
	
	@Override
	public void grab()
	{
		if (external_inv == null || !canGrab(external_inv))
			return;
		
		Pair<Boolean, ItemStack[]> result = grabInventory(external_inv.getContents());
		if (result.first) {
			external_inv.setContents(result.second);
			Utils.getPlugin().sthandler.delayedUpdate(external_inv);
		}
	}
	
	public Inventory getInventory()
	{
		return getPage();
	}

	public boolean isEmpty()
	{
			return getAmount() == 0;
	}
	
	public Inventory getMenu()
	{
		return menu.getMenu();
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public void changeAmount(int amount) {
		setAmount(getAmount()+amount);
	}

	public Material getMaterial() {
		return allowed_type;
	}
	
	public void setMaterial(Material m) {
		allowed_type = m;
		grab();
	}
	
	public boolean isDefined() {
		return allowed_type != StorageBottomless.UNDEFINED_MATERIAL;
	}
	
	public boolean isAllowed(ItemStack item)
	{
		if(item != null && Storage.getID(item) < 0 && (item.getType() == getMaterial() || item.getType() == Material.AIR))
			if(!Utils.isRenamed(item))
				return true;
		return false;
	}

	public void drop(Location drop_from)
	{
		int amount = 0;
		Inventory inv = getPage();
		for(ItemStack stack: inv.getContents()) {
			if(stack != null) {
				Utils.drop(drop_from, stack, 1);
				amount += stack.getAmount();
			}
		}
		changeAmount(-amount);
		if (amount != 0)
			setEdited(true);
	}
	
	public static boolean isAllowedMaterial(Material m) {
		return !(Utils.is_shulker_box(m) || Utils.isTool(m) || Utils.isWeapon(m) || Utils.isArmor(m) || m == Material.ELYTRA || m == Material.CARROT_ON_A_STICK
				|| m == Material.ENCHANTED_BOOK || m == Material.FIREWORK_STAR || m == Material.FIREWORK_ROCKET || m == Material.SHIELD || m == Material.TIPPED_ARROW
				|| m == Material.POTION || m == Material.SPLASH_POTION || m == Material.LINGERING_POTION || m == Material.WRITABLE_BOOK || m == Material.WRITTEN_BOOK
				|| m == Material.FILLED_MAP || m == Material.AIR || m == Material.SPAWNER);
	}
	
	public Inventory getPage() {
		Inventory inv = Bukkit.createInventory(null, 27, "Storage");
		
		int i = 0;
		int amount = this.amount, cur_amount;
		int max_size = allowed_type.getMaxStackSize();
		
		for(int j = 0; j < 27; j++, i++) {
			if(amount == 0) continue;
			if(amount < max_size) {
				cur_amount = amount;
				amount = 0;
			} else {
				cur_amount = max_size;
				amount -= max_size;
			}
			ItemStack stack = new ItemStack(allowed_type, cur_amount);
			inv.setItem(i, stack);
		}
		return inv;
	}
	
	public ItemStack getLored(ItemStack orig)
	{
		ItemMeta items_count = orig.getItemMeta();
		List<String> lore = Arrays.asList(new String[] {getAmount()+" items ("+getMaterial().toString().replace('_', ' ')+")"});
		items_count.setLore(lore);
		orig.setItemMeta(items_count);
		return orig;
	}
	
	public Pair<Boolean, ItemStack[]> grabInventory(ItemStack[] inv) {
		if (inv == null) return new Pair<Boolean, ItemStack[]>(false, inv);
		boolean updated = false;
		for (int i=0; i < inv.length; i++) {
			if (inv[i] != null && isAllowed(inv[i])) {
				changeAmount(inv[i].getAmount());
				inv[i] = null;
				updated = true;
			}
		}
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}

	public static void update_item_counts(Inventory inv) {
		ItemStack[] stacks = inv.getContents();
		for(int j=0; j < stacks.length; j++) {
			Storage storage = Storage.getByItemStack(stacks[j]);
			if(storage != null && storage instanceof StorageBottomless) {
				StorageBottomless st = (StorageBottomless)storage;
				inv.setItem(j, st.getLored(stacks[j]));
			}
		}
	}
	
	public boolean canGrab(Material m) {
		return m == getMaterial();
	}
}
