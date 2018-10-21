package com.festp.storages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Pair;
import com.festp.Utils;
import com.festp.menu.InventoryMenu;
import com.festp.menu.MenuAction;
import com.festp.menu.MenuListener;

public class BottomlessInventory implements MenuListener {
	//HashMap<Material, Integer> map = new HashMap<>();
	private Storage storage;
	private Integer amount = 0;
	private Material allowed_type;
	private InventoryMenu menu = null;
	public enum Grab {NOTHING, NO_PLAYER, ALL}
	private Grab grab_mode = Grab.NOTHING;
	private List<Inventory> pages = new ArrayList<>();
	
	public BottomlessInventory(Storage st) {
		storage = st;
	}
	
	public Inventory getMenu() {
		if(menu == null) {

			ItemStack material_button = genMaterialButton();
			ItemStack grab_button = genGrabButton();
			
			menu = new InventoryMenu(this, new ItemStack[] {
					grab_button, null, null, null, null, null, null, null, material_button},
					"Storage settings", 3);
		}
		return menu.getGUI();
	}

	@Override
	public void removeMenu() {
		menu = null;
	}
	
	public Pair<Boolean, ItemStack[]> grabInventory(ItemStack[] inv) {
		if(inv == null) return new Pair<Boolean, ItemStack[]>(false, inv);
		boolean updated = false;
		for(int i=0; i < inv.length; i++) {
			if(inv[i] != null && storage.isAllowed(inv[i])) {
				changeAmount(inv[i].getAmount());
				inv[i] = null;
				updated = true;
			}
		}
		return new Pair<Boolean, ItemStack[]>(updated, inv);
	}

	/*
	public void grabInventory(Inventory inv) {
		if(inv == null) return;
		ItemStack[] stacks = inv.getContents();
		for(int i=0; i < stacks.length; i++) {
			if(stacks[i] != null && storage.isAllowed(stacks[i])) {
				changeAmount(stacks[i].getAmount());
				inv.setItem(i, null);
			}
		}
		for(HumanEntity human : inv.getViewers()) {
			Player player = (Player)human;
			player.updateInventory();
		}
	}
	
	public void grabInventory() {
		grabInventory(storage.grabbing_inventory);
	}*/
	
	public Storage getStorage() {
		return storage;
	}
	
	public Grab canGrab() {
		return grab_mode;
	}
	
	public void setGrab(Grab grab_mode) {
		this.grab_mode = grab_mode;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Material getMaterial() {
		return allowed_type;
	}
	
	public void setMaterial(Material m) {
		allowed_type = m;
	}
	
	public boolean isDefined() {
		return allowed_type != Storage.UNDEFINED_MATERIAL;
	}
	
	public void changeAmount(int amount) {
		setAmount(getAmount()+amount);
	}
	
	public void drop(Location drop_from) {
		int amount = 0;
		Inventory inv = getPage();
		for(ItemStack stack: inv.getContents()) {
			if(stack != null) {
				Utils.drop(drop_from, stack, 1);
				amount += stack.getAmount();
			}
		}
		changeAmount(-amount);
	}
	
	public int getPageAmount() {
		return Math.min(allowed_type.getMaxStackSize()*27, amount);
	}
	
	public int getCurrentPageAmount(Inventory page) {
		int new_amount = 0;
		for(ItemStack is : page.getContents())
			if(is != null && is.getType() == allowed_type)
				new_amount += is.getAmount();
		return new_amount;
	}
	
	public int getCurrentPageAmount(InventoryView page, int start_index, int end_index) {
		int new_amount = 0;
		ItemStack is;
		System.out.println("---START---");
		for(int i = start_index; i < end_index; i++) {
			is = page.getItem(i);
			System.out.println(is);
			if(is != null && is.getType() == allowed_type)
				new_amount += is.getAmount();
		}
		System.out.println("----END----");
		return new_amount;
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
		
		//storage.setInventory(inv);
		remove_closed_pages();
		pages.add(inv);
		return inv;
	}
	
	private void remove_closed_pages() {
		for(int i=pages.size()-1; i >= 0; i--) {
			if(pages.get(i).getViewers().size() == 0)
				pages.remove(i);
		}
	}
	
	public boolean containsPage(Inventory inv) {
		for(int i=pages.size()-1; i >= 0; i--) {
			if(Utils.equal_invs(inv,pages.get(i)))
				return true;
		}
		return false;
	}

	@Override
	public ItemStack onClick(int slot, ItemStack cursor_item, ItemStack slot_item, MenuAction action) {
		//grab
		if(slot == 0) {
			if(action == MenuAction.LEFT_CLICK) {
				Grab new_grab = Grab.NOTHING;
				switch (canGrab()) {
				case ALL:
					new_grab = Grab.NOTHING;
					break;
				case NO_PLAYER:
					new_grab = Grab.ALL;
					break;
				case NOTHING:
					new_grab = Grab.NO_PLAYER;
					break;
				}
				setGrab(new_grab);
				storage.setEdited(true);
			}
			else if(action == MenuAction.RIGHT_CLICK) {
				Grab new_grab = Grab.NOTHING;
				switch (canGrab()) {
				case ALL:
					new_grab = Grab.NO_PLAYER;
					break;
				case NO_PLAYER:
					new_grab = Grab.NOTHING;
					break;
				case NOTHING:
					new_grab = Grab.ALL;
					break;
				}
				setGrab(new_grab);
				storage.setEdited(true);
			}
			return genGrabButton();
		}
		//material
		else if(slot == 8) {
			if(cursor_item != null && cursor_item.getType() != allowed_type && isAllowedMaterial(cursor_item.getType())) {
				if(amount != 0 && allowed_type == Storage.UNDEFINED_MATERIAL)
					amount = 0;
				if(amount == 0) {
					allowed_type = cursor_item.getType();
					storage.setEdited(true);
				}
			}
			return genMaterialButton();
		}
		else
			return cursor_item;
	}
	
	public static boolean isAllowedMaterial(Material m) {
		return !(Utils.is_shulker_box(m) || Utils.isTool(m) || Utils.isWeapon(m) || Utils.isArmor(m) || m == Material.ELYTRA || m == Material.CARROT_ON_A_STICK
				|| m == Material.ENCHANTED_BOOK || m == Material.FIREWORK_STAR || m == Material.FIREWORK_ROCKET || m == Material.SHIELD || m == Material.TIPPED_ARROW
				|| m == Material.POTION || m == Material.SPLASH_POTION || m == Material.LINGERING_POTION || m == Material.WRITABLE_BOOK || m == Material.WRITTEN_BOOK
				|| m == Material.FILLED_MAP || m == Material.AIR || m == Material.SPAWNER);
	}
	
	public ItemStack genMaterialButton() {
		Material cur_material = getMaterial();
		String material_name;
		if(cur_material == Storage.UNDEFINED_MATERIAL) {
			cur_material = Material.FIREWORK_STAR;
			material_name = "Текущий предмет не выбран";
		} else {
			material_name = "Текущий предмет: "+cur_material;
		}
		ItemStack material_button = new ItemStack(cur_material);
		
		ItemMeta material_meta = material_button.getItemMeta();
		material_meta.setDisplayName(material_name);
		material_button.setItemMeta(material_meta);
		
		return material_button;
	}
	
	public ItemStack genGrabButton() {
		String grab_name;
		Material grab_material;
		if(canGrab() == Grab.ALL) {
			grab_material = Storage.GRAB_ALL_MATERIAL;
			grab_name = "Ссасывание: ВСЁ";
		} else if(canGrab() == Grab.NO_PLAYER) {
			grab_material = Storage.GRAB_PLAYERNT_MATERIAL;
			grab_name = "Ссасывание: КРОМЕ ИГРОКА";
		} else {
			grab_material = Storage.GRAB_NOTHING_MATERIAL;
			grab_name = "Ссасывание: ВЫКЛ";
		}
		ItemStack grab_button = new ItemStack(grab_material);

		ItemMeta grab_meta = grab_button.getItemMeta();
		grab_meta.setDisplayName(grab_name);
		grab_button.setItemMeta(grab_meta);
		
		return grab_button;
	}
	
	public List<String> getLore() {
		return Arrays.asList(new String[] {   getAmount()+" items ("+getMaterial().toString().replace('_', ' ')+")"   });
	}
}
