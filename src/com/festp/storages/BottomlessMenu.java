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
import com.festp.storages.Storage.Grab;

public class BottomlessMenu implements MenuListener {
	public static final Material GRAB_NOTHING_MATERIAL = Material.BARRIER;
	public static final Material GRAB_PLAYERNT_MATERIAL = Material.HOPPER;
	public static final Material GRAB_ALL_MATERIAL = Material.PLAYER_HEAD;
	
	//HashMap<Material, Integer> map = new HashMap<>();
	private StorageBottomless storage;
	private InventoryMenu menu = null;
	
	public BottomlessMenu(StorageBottomless st) {
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
	
	public Storage getStorage() {
		return storage;
	}
	
	@Override
	public ItemStack onClick(int slot, ItemStack cursor_item, ItemStack slot_item, MenuAction action) {
		//grab
		if(slot == 0) {
			if(action == MenuAction.LEFT_CLICK) {
				Grab new_grab = Grab.NOTHING;
				switch (storage.canGrab()) {
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
				storage.setGrab(new_grab);
				storage.setEdited(true);
			}
			else if(action == MenuAction.RIGHT_CLICK) {
				Grab new_grab = Grab.NOTHING;
				switch (storage.canGrab()) {
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
				storage.setGrab(new_grab);
				storage.setEdited(true);
			}
			return genGrabButton();
		}
		//material
		else if(slot == 8) {
			if(cursor_item != null && cursor_item.getType() != storage.getMaterial() && storage.isAllowedMaterial(cursor_item.getType())) {
				if(!storage.isEmpty() && !storage.isDefined())
					storage.setAmount(0);
				if(storage.isEmpty()) {
					storage.setMaterial(cursor_item.getType());
					storage.setEdited(true);
				}
			}
			return genMaterialButton();
		}
		else
			return cursor_item;
	}
	
	public ItemStack genMaterialButton() {
		Material cur_material = storage.getMaterial();
		String material_name;
		if(cur_material == StorageBottomless.UNDEFINED_MATERIAL) {
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
		if(storage.canGrab() == Grab.ALL) {
			grab_material = GRAB_ALL_MATERIAL;
			grab_name = "Ссасывание: ВСЁ";
		} else if(storage.canGrab() == Grab.NO_PLAYER) {
			grab_material = GRAB_PLAYERNT_MATERIAL;
			grab_name = "Ссасывание: КРОМЕ ИГРОКА";
		} else {
			grab_material = GRAB_NOTHING_MATERIAL;
			grab_name = "Ссасывание: ВЫКЛ";
		}
		ItemStack grab_button = new ItemStack(grab_material);

		ItemMeta grab_meta = grab_button.getItemMeta();
		grab_meta.setDisplayName(grab_name);
		grab_button.setItemMeta(grab_meta);
		
		return grab_button;
	}
}
