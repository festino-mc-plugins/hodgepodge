package com.festp.storages;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.menu.InventoryMenu;
import com.festp.menu.MenuAction;
import com.festp.menu.MenuListener;
import com.festp.storages.Storage.Grab;
import com.festp.storages.StorageMultitype.GrabDirection;
import com.festp.storages.StorageMultitype.GrabFilter;
import com.festp.storages.StorageMultitype.HandleTime;
import com.festp.storages.StorageMultitype.SortMode;
import com.festp.storages.StorageMultitype.UncraftMode;
import com.festp.utils.Utils;

public class MenuMultitype implements MenuListener {
	public static final Material MISSING_MATERIAL = Material.ROTTEN_FLESH;

	public static final Grab[] ORDER_GRAB = { Grab.NOTHING, Grab.NEW, Grab.NO_PLAYER, Grab.ALL };
	public static final GrabFilter[] ORDER_GRAB_FILTER = { GrabFilter.STACKING, GrabFilter.SIMILAR, GrabFilter.ANY };
	public static final GrabDirection[] ORDER_GRAB_DIR = { GrabDirection.BACKWARD, GrabDirection.FORWARD };
	public static final SortMode[] ORDER_SORT_MODE = { SortMode.ALPHABET, SortMode.VANILLA };
	public static final HandleTime[] ORDER_TIME = { HandleTime.ON_BUTTON, HandleTime.WAIT_N_SECONDS, HandleTime.OPEN_CLOSE, HandleTime.ALWAYS };
	public static final UncraftMode[] ORDER_UNCRAFT = { UncraftMode.DENY, UncraftMode.DROP };
	public static final Material
		GRAB_NOTHING_MATERIAL = Material.BARRIER,
		GRAB_NEW_MATERIAL = Material.IRON_PICKAXE, // also there are option with Material.HOPPER
		GRAB_PLAYERNT_MATERIAL = Material.HOPPER,  //                        and Material.HOPPER_MINECART
		GRAB_ALL_MATERIAL = Material.PLAYER_HEAD,
		GRAB_F_STACKING_MATERIAL = Material.NAME_TAG,
		GRAB_F_SIMILAR_MATERIAL = Material.BOOKSHELF,
		GRAB_F_ANY_MATERIAL = Material.CHEST,
		SORT_BUTTON = Material.HOPPER,
		SORT_VANILLA = Material.GRASS_BLOCK,
		SORT_ALPHABET = Material.NAME_TAG,
		TIME__ON_BUTTON = Material.STONE_BUTTON,
		TIME__WAIT_N_SECONDS = Material.CLOCK,
		TIME__OPEN_CLOSE = Material.ENDER_EYE,
		TIME__ALWAYS = Material.REDSTONE_BLOCK,
		STACK_BUTTON = Material.SLIME_BALL,
		UNCRAFT_DENY = Material.BARRIER,
		UNCRAFT_DROP = Material.GLASS_PANE,
		EXTERNAL_INV_MATERIAL = Material.RED_STAINED_GLASS_PANE,
		RETURN_INV_MATERIAL = Material.RED_STAINED_GLASS_PANE;
	public static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=",
		ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";
		
	// Util Map<String,String> with english and other lang strings
	
	
	private static final int grab_mode_index = 0, grab_filter_index = grab_mode_index + 9, grab_dir_index = grab_mode_index + 18,
			sort_mode_index = 1, sort_time_index = sort_mode_index + 9, sort_index = sort_time_index + 9,
			stack_time_index = 2 + 9, stack_index = stack_time_index + 9,
			uncraft_index = 8,
			external_inv_index = 27 - 1;
	
	private StorageMultitype storage;
	private InventoryMenu menu = null;
	
	public MenuMultitype(StorageMultitype st) {
		
		storage = st;
	}
	
	public Inventory getMenu() {
		if(menu == null) {
			int max_slot = max_slot(grab_mode_index, grab_filter_index, grab_dir_index,
					sort_mode_index, sort_time_index, sort_index,
					stack_time_index, stack_index,
					uncraft_index, external_inv_index);
			ItemStack[] buttons = new ItemStack[max_slot + 1];
			for (int i = 0; i <= max_slot; i++)
				buttons[i] = null;
			
			buttons[grab_mode_index] = genGrabModeButton();
			buttons[grab_filter_index] = genGrabFilterButton();
			buttons[grab_dir_index] = genGrabDirButton();
			buttons[sort_mode_index] = genSortModeButton();
			buttons[sort_time_index] = genSortTimeButton();
			buttons[sort_index] = genSortButton();
			buttons[stack_time_index] = genStackTimeButton();
			buttons[stack_index] = genStackButton();
			buttons[uncraft_index] = genUncraftButton();
			buttons[external_inv_index] = genExternalInvButton();
			
			menu = new InventoryMenu(this, buttons, "Storage settings", 3);
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
	public ItemStack onClick(int slot, ItemStack cursor_item, ItemStack slot_item, MenuAction action, Player clicked) {
		if(slot == grab_mode_index) {
			Grab new_grab = storage.canGrab();
			if(action == MenuAction.LEFT_CLICK)
				new_grab = next(new_grab);
			else if(action == MenuAction.RIGHT_CLICK)
				new_grab = prev(new_grab);
			storage.setGrab(new_grab);
			return genGrabModeButton();
		}
		if(slot == grab_filter_index) {
			GrabFilter grab_filter = storage.getGrabFilter();
			if(action == MenuAction.LEFT_CLICK)
				grab_filter = next(grab_filter);
			else if(action == MenuAction.RIGHT_CLICK)
				grab_filter = prev(grab_filter);
			storage.setGrabFilter(grab_filter);
			return genGrabFilterButton();
		}
		// grab direction (from begin/from end)
		else if(slot == grab_dir_index) {
			GrabDirection dir = storage.getGrabDirection();
			if(action == MenuAction.LEFT_CLICK)
				dir = next(dir);
			else if(action == MenuAction.RIGHT_CLICK)
				dir = prev(dir);
			storage.setGrabDirection(dir);
			
			return genGrabDirButton();
		}
		else if (slot == sort_mode_index) {
			SortMode mode = storage.getSortMode();
			if(action == MenuAction.LEFT_CLICK)
				mode = next(mode);
			else if(action == MenuAction.RIGHT_CLICK)
				mode = prev(mode);
			
			storage.setSortMode(mode);
			storage.sorted = false;
			HandleTime time = storage.getSortTime();
			if (storage.need_action(time))
				storage.sort();

			changeSortButton();
			return genSortModeButton();
		}
		else if (slot == sort_time_index) {
			HandleTime sort_time = storage.getSortTime();
			if(action == MenuAction.LEFT_CLICK)
				sort_time = next(sort_time);
			else if(action == MenuAction.RIGHT_CLICK)
				sort_time = prev(sort_time);

			storage.setSortTime(sort_time);
			if (storage.need_action(sort_time))
				storage.sort();
			
			return genSortTimeButton();
		}
		else if (slot == sort_index) {
			storage.sort();
			return genSortButton();
		}
		else if (slot == stack_time_index) {
			HandleTime stack_time = storage.getStackTime();
			if(action == MenuAction.LEFT_CLICK)
				stack_time = next(stack_time);
			else if(action == MenuAction.RIGHT_CLICK)
				stack_time = prev(stack_time);

			storage.setStackTime(stack_time);
			if (storage.need_action(stack_time))
				storage.mergeStacks();
			
			return genStackTimeButton();
		}
		else if (slot == stack_index) {
			storage.mergeStacks();
			return genStackButton();
		}
		else if (slot == uncraft_index) {
			UncraftMode uncraft_mode = storage.getUncraftMode();
			if(action == MenuAction.LEFT_CLICK)
				uncraft_mode = next(uncraft_mode);
			else if(action == MenuAction.RIGHT_CLICK)
				uncraft_mode = prev(uncraft_mode);
			storage.setUncraftMode(uncraft_mode);
			return genUncraftButton();
		}
		else if (slot == external_inv_index) {
			if (storage.getExternalInventory() != null) {
				if (Utils.equal_invs(clicked.getInventory(), storage.getExternalInventory()))
					clicked.openInventory(storage.getInventory());
				else
					clicked.openInventory(storage.getExternalInventory());
			}
		}
		return slot_item;
	}

	private ItemStack genGrabModeButton() {
		String grab_name = "ERROR_GRAB_MODE_NAME";
		Material grab_material = MISSING_MATERIAL;
		Grab grab_mode = storage.canGrab();
		if(grab_mode == Grab.ALL) {
			grab_material = GRAB_ALL_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÂÑ¨";
		} else if(grab_mode == Grab.NO_PLAYER) {
			grab_material = GRAB_PLAYERNT_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÊÐÎÌÅ ÈÃÐÎÊÀ";
		} else if(grab_mode == Grab.NEW) {
			grab_material = GRAB_NEW_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÒÎËÜÊÎ ÍÎÂÎÅ";
		} else if(grab_mode == Grab.NOTHING) {
			grab_material = GRAB_NOTHING_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÂÛÊË";
		}
		ItemStack grab_button = new ItemStack(grab_material);

		ItemMeta grab_meta = grab_button.getItemMeta();
		grab_meta.setDisplayName(grab_name);
		grab_button.setItemMeta(grab_meta);
		
		return grab_button;
	}

	private ItemStack genGrabFilterButton() {
		String grab_name = "ERROR_GRAB_FILTER_NAME";
		Material grab_material = MISSING_MATERIAL;
		GrabFilter grab_filter = storage.getGrabFilter();
		if(grab_filter == GrabFilter.STACKING) {
			grab_material = GRAB_F_STACKING_MATERIAL;
			grab_name = "Ññàñûâàòü ÄÎÏÎËÍßß ñòàêè";
		} else if(grab_filter == GrabFilter.SIMILAR) {
			grab_material = GRAB_F_SIMILAR_MATERIAL;
			grab_name = "Ññàñûâàòü ÒÀÊÈÅ ÆÅ âåùè";
		} else if(grab_filter == GrabFilter.ANY) {
			grab_material = GRAB_F_ANY_MATERIAL;
			grab_name = "Ññàñûâàòü ËÞÁÛÅ âåùè";
		}
		ItemStack grab_button = new ItemStack(grab_material);

		ItemMeta grab_meta = grab_button.getItemMeta();
		grab_meta.setDisplayName(grab_name);
		grab_button.setItemMeta(grab_meta);
		
		return grab_button;
	}
	
	private ItemStack genGrabDirButton() {
		String dir_name = "ERROR_DIR_NAME";
		ItemStack dir_button = new ItemStack(MISSING_MATERIAL);
		if(storage.getGrabDirection() == GrabDirection.FORWARD) {
			dir_name = "Íàïðàâëåíèå: ÏÐßÌÎÅ";
			dir_button = Utils.getHead(dir_name, ARROW_RIGHT);
		} else if(storage.getGrabDirection() == GrabDirection.BACKWARD) {
			dir_name = "Íàïðàâëåíèå: ÎÁÐÀÒÍÎÅ";
			dir_button = Utils.getHead(dir_name, ARROW_LEFT);
		}
		return dir_button;
	}

	private ItemStack genSortModeButton() {
		String sort_name = "ERROR_SORT_MODE_NAME";
		Material button_material = MISSING_MATERIAL;
		switch (storage.getSortMode())
		{
		case ALPHABET:
			button_material = SORT_ALPHABET;
			sort_name = "Ñîðòèðîâêà: ÏÎ ÀËÔÀÂÈÒÓ";
			break;
		case VANILLA:
			button_material = SORT_VANILLA;
			sort_name = "Ñîðòèðîâêà: ÏÎ ÐÀÇÄÅËÀÌ";
			break;
		}
		ItemStack sort_button = new ItemStack(button_material);
		ItemMeta meta = sort_button.getItemMeta();
		meta.setDisplayName(sort_name);
		sort_button.setItemMeta(meta);
		return sort_button;
	}

	private ItemStack genSortTimeButton() {
		String time_name = "ERROR_SORT_TIME_NAME";
		Material material = MISSING_MATERIAL;
		switch (storage.getSortTime())
		{
		case ON_BUTTON: // TO DO: USE LORE INSTEAD OF NAME and color instead of CAPITAL LETTERS
			time_name = "Ñîðòèðîâàòü ÒÎËÜÊÎ ÏÐÈ ÍÀÆÀÒÈÈ";
			material = TIME__ON_BUTTON;
			break;
		case WAIT_N_SECONDS:
			time_name = "Ñîðòèðîâàòü ×ÅÐÅÇ "+ StorageMultitype.DELAY/20 + " ÑÅÊÓÍÄ ÏÎÑËÅ ÄÅÉÑÒÂÈß"; // ñåêóíä(ó/û)
			material = TIME__WAIT_N_SECONDS;
			break;
		case OPEN_CLOSE:
			time_name = "Ñîðòèðîâàòü ÊÎÃÄÀ ÍÈÊÒÎ ÍÅ ÂÈÄÈÒ";
			material = TIME__OPEN_CLOSE;
			break;
		case ALWAYS:
			time_name = "Ñîðòèðîâàòü ÏÐÈ ËÞÁÎÌ ÈÇÌÅÍÅÍÈÈ";
			material = TIME__ALWAYS;
			break;
		}
		ItemStack sort_time_button = new ItemStack(material);
		ItemMeta meta = sort_time_button.getItemMeta();
		meta.setDisplayName(time_name);
		sort_time_button.setItemMeta(meta);
		return sort_time_button;
	}
	
	private void changeSortButton() {
		menu.changeSlot(sort_index, genSortButton());
	}
	private ItemStack genSortButton() {
		String sort_name = "ERROR_SORT_NAME";
		ItemStack sort_button = new ItemStack(SORT_BUTTON);
		ItemMeta meta = sort_button.getItemMeta();
		switch (storage.getSortMode())
		{
		case VANILLA:
			sort_name = "Îòñîðòèðîâàòü (ÏÎ ÐÀÇÄÅËÀÌ)";
			break;
		case ALPHABET:
			sort_name = "Îòñîðòèðîâàòü (ÏÎ ÀËÔÀÂÈÒÓ)";
			break;
		}
		
		meta.setDisplayName(sort_name);
		sort_button.setItemMeta(meta);
		return sort_button;
	}
	
	private ItemStack genStackButton() {
		String sort_name = "Çàñòàêàòü";
		ItemStack sort_button = new ItemStack(STACK_BUTTON);
		ItemMeta meta = sort_button.getItemMeta();
		meta.setDisplayName(sort_name);
		sort_button.setItemMeta(meta);
		return sort_button;
	}

	private ItemStack genStackTimeButton() {

		String time_name = "ERROR_STACK_TIME_NAME";
		Material material = MISSING_MATERIAL;
		switch (storage.getStackTime())
		{
		case ON_BUTTON: // TO DO: USE LORE INSTEAD OF NAME and color instead of CAPITAL LETTERS
			time_name = "Ñòàêàòü ÒÎËÜÊÎ ÏÐÈ ÍÀÆÀÒÈÈ";
			material = TIME__ON_BUTTON;
			break;
		case WAIT_N_SECONDS:
			time_name = "Ñòàêàòü ×ÅÐÅÇ "+ StorageMultitype.DELAY/20 + " ÑÅÊÓÍÄ ÏÎÑËÅ ÄÅÉÑÒÂÈß"; // ñåêóíä(ó/û)
			material = TIME__WAIT_N_SECONDS;
			break;
		case OPEN_CLOSE:
			time_name = "Ñòàêàòü ÊÎÃÄÀ ÍÈÊÒÎ ÍÅ ÂÈÄÈÒ";
			material = TIME__OPEN_CLOSE;
			break;
		case ALWAYS:
			time_name = "Ñòàêàòü ÏÐÈ ËÞÁÎÌ ÈÇÌÅÍÅÍÈÈ";
			material = TIME__ALWAYS;
			break;
		}
		ItemStack sort_time_button = new ItemStack(material);
		ItemMeta meta = sort_time_button.getItemMeta();
		meta.setDisplayName(time_name);
		sort_time_button.setItemMeta(meta);
		return sort_time_button;
	}

	private ItemStack genUncraftButton() {
		String uncraft_name = "ERROR_UNCRAFT_NAME";
		Material button = MISSING_MATERIAL;
		switch (storage.getUncraftMode())
		{
		case DENY:
			uncraft_name = "Ðàñêðàôò ïðè íåõâàòêå ìåñòà ÇÀÏÐÅÙ¨Í";
			button = UNCRAFT_DENY;
			break;
		case DROP:
			uncraft_name = "Ðàñêðàôò ïðè íåõâàòêå ìåñòà ÂÛÁÐÀÑÛÂÀÅÒ";
			button = UNCRAFT_DROP;
			break;
		}
		ItemStack uncraft_button = new ItemStack(button);
		ItemMeta meta = uncraft_button.getItemMeta();
		meta.setDisplayName(uncraft_name);
		uncraft_button.setItemMeta(meta);
		return uncraft_button;
	}

	private ItemStack genExternalInvButton() {
		String external_inv_name = "ERROR_EXTERNAL_INV_NAME";
		external_inv_name = "Îòêðûòü èíâåíòàðü ñ õðàíèëèùåì";
		ItemStack external_inv_button = new ItemStack(EXTERNAL_INV_MATERIAL);
		ItemMeta meta = external_inv_button.getItemMeta();
		meta.setDisplayName(external_inv_name);
		external_inv_button.setItemMeta(meta);
		return external_inv_button;
	}
	
	private static Grab next(Grab grab) {
		return Utils.next(grab, ORDER_GRAB);
	}
	private static Grab prev(Grab grab) {
		return Utils.prev(grab, ORDER_GRAB);
	}
	
	private static GrabDirection next(GrabDirection dir) {
		return Utils.next(dir, ORDER_GRAB_DIR);
	}
	private static GrabDirection prev(GrabDirection dir) {
		return Utils.prev(dir, ORDER_GRAB_DIR);
	}
	
	private static GrabFilter next(GrabFilter dir) {
		return Utils.next(dir, ORDER_GRAB_FILTER);
	}
	private static GrabFilter prev(GrabFilter dir) {
		return Utils.prev(dir, ORDER_GRAB_FILTER);
	}
	
	private static SortMode next(SortMode grab) {
		return Utils.next(grab, ORDER_SORT_MODE);
	}
	private static SortMode prev(SortMode grab) {
		return Utils.prev(grab, ORDER_SORT_MODE);
	}
	
	private static HandleTime next(HandleTime time) {
		return Utils.next(time, ORDER_TIME);
	}
	private static HandleTime prev(HandleTime time) {
		return Utils.prev(time, ORDER_TIME);
	}
	
	private static UncraftMode next(UncraftMode mode) {
		return Utils.next(mode, ORDER_UNCRAFT);
	}
	private static UncraftMode prev(UncraftMode mode) {
		return Utils.next(mode, ORDER_UNCRAFT);
	}
	
	private int max_slot(int first, int... slots)
	{
		int max = first;
		for (int n : slots)
			max = Math.max(max, n);
		return max;
	}
}