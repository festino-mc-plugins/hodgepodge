package com.festp.storages;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Utils;
import com.festp.menu.InventoryMenu;
import com.festp.menu.MenuAction;
import com.festp.menu.MenuListener;
import com.festp.storages.Storage.Grab;
import com.festp.storages.StorageMultitype.GrabDirection;
import com.festp.storages.StorageMultitype.HandleTime;
import com.festp.storages.StorageMultitype.SortMode;
import com.festp.storages.StorageMultitype.UncraftMode;

public class MultitypeMenu implements MenuListener {
	public static final Material MISSING_MATERIAL = Material.ROTTEN_FLESH;
	
	public static final Material
		GRAB_NOTHING_MATERIAL = Material.BARRIER,
		GRAB_PLAYERNT_MATERIAL = Material.HOPPER,
		GRAB_ALL_MATERIAL = Material.PLAYER_HEAD,
		SORT_BUTTON = Material.HOPPER,
		SORT_VANILLA = Material.GRASS_BLOCK,
		SORT_ALPHABET = Material.NAME_TAG,
		TIME__ON_BUTTON = Material.STONE_BUTTON,
		TIME__WAIT_N_SECONDS = Material.CLOCK,
		TIME__OPEN_CLOSE = Material.ENDER_EYE,
		TIME__ACTION = Material.REDSTONE_BLOCK,
		STACK_BUTTON = Material.SLIME_BALL, // book, bookshelf, chest, slime
		UNCRAFT_DENY = Material.BARRIER,
		UNCRAFT_DROP = Material.GLASS_PANE;
	public static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=",
		ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=";
		
	// Util Map<String,String> with english and other lang strings
	
	
	private static final int grab_mode_index = 0, grab_dir_index = 1,
			sort_mode_index = 2, sort_time_index = 3, sort_index = sort_mode_index + 9,
			stack_time_index = 4, stack_index = stack_time_index + 9,
			uncraft_index = 8;
	
	private StorageMultitype storage;
	private InventoryMenu menu = null;
	
	public MultitypeMenu(StorageMultitype st) {
		storage = st;
	}
	
	public Inventory getMenu() {
		if(menu == null) {
			int max_slot = max_slot(grab_mode_index, grab_dir_index, sort_mode_index, sort_time_index, sort_index, stack_time_index, stack_index, uncraft_index);
			ItemStack[] buttons = new ItemStack[max_slot + 1];
			for (int i = 0; i <= max_slot; i++)
				buttons[i] = null;
			
			buttons[grab_mode_index] = genGrabModeButton();
			buttons[grab_dir_index] = genGrabDirButton();
			buttons[sort_mode_index] = genSortModeButton();
			buttons[sort_time_index] = genSortTimeButton();
			buttons[sort_index] = genSortButton();
			buttons[stack_time_index] = genStackTimeButton();
			buttons[stack_index] = genStackButton();
			buttons[uncraft_index] = genUncraftButton();
			
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
	public ItemStack onClick(int slot, ItemStack cursor_item, ItemStack slot_item, MenuAction action) {
		// grab mode
		if(slot == grab_mode_index) {
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
			return genGrabModeButton();
		}
		// grab direction (from begin/from end)
		else if(slot == grab_dir_index) {
			GrabDirection dir = storage.getGrabDirection();
			if (dir == GrabDirection.FORWARD)
				dir = GrabDirection.BACKWARD;
			else if (dir == GrabDirection.BACKWARD)
				dir = GrabDirection.FORWARD;
			storage.setGrabDirection(dir);
			storage.setEdited(true);
			
			return genGrabDirButton();
		}
		else if (slot == sort_mode_index) {
			SortMode mode = storage.getSortMode();
			if (mode == SortMode.ALPHABET)
				mode = SortMode.VANILLA;
			else if (mode == SortMode.VANILLA)
				mode = SortMode.ALPHABET;
			storage.setSortMode(mode);
			storage.setEdited(true);
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
			storage.setEdited(true);
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
			storage.setEdited(true);
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
			storage.setUncraftMode(next(uncraft_mode));
			return genUncraftButton();
		}
		else
			return slot_item;
	}

	private ItemStack genGrabModeButton() {
		String grab_name = "ERROR_GRAB_MODE_NAME";
		Material grab_material = MISSING_MATERIAL;
		if(storage.canGrab() == Grab.ALL) {
			grab_material = GRAB_ALL_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÂÑ¨";
		} else if(storage.canGrab() == Grab.NO_PLAYER) {
			grab_material = GRAB_PLAYERNT_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÊÐÎÌÅ ÈÃÐÎÊÀ";
		} else {
			grab_material = GRAB_NOTHING_MATERIAL;
			grab_name = "Ññàñûâàíèå: ÂÛÊË";
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
		case ACTION:
			time_name = "Ñîðòèðîâàòü ÏÐÈ ËÞÁÎÌ ÄÅÉÑÒÂÈÈ";
			material = TIME__ACTION;
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
		case ACTION:
			time_name = "Ñòàêàòü ÏÐÈ ËÞÁÎÌ ÄÅÉÑÒÂÈÈ";
			material = TIME__ACTION;
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
	
	private HandleTime next(HandleTime time)
	{
		switch (time)
		{
		case ON_BUTTON:
			return HandleTime.WAIT_N_SECONDS;
		case WAIT_N_SECONDS:
			return HandleTime.OPEN_CLOSE;
		case OPEN_CLOSE:
			return HandleTime.ACTION;
		case ACTION:
			return HandleTime.ON_BUTTON;
		}
		return null;
	}
	
	private HandleTime prev(HandleTime time)
	{
		switch (time)
		{
		case ON_BUTTON:
			return HandleTime.ACTION;
		case WAIT_N_SECONDS:
			return HandleTime.ON_BUTTON;
		case OPEN_CLOSE:
			return HandleTime.WAIT_N_SECONDS;
		case ACTION:
			return HandleTime.OPEN_CLOSE;
		}
		return null;
	}
	
	private UncraftMode next(UncraftMode mode)
	{
		switch (mode)
		{
		case DENY:
			return UncraftMode.DROP;
		case DROP:
			return UncraftMode.DENY;
		}
		return null;
	}
	
	private int max_slot(int first, int... slots)
	{
		int max = first;
		for (int n : slots)
			max = Math.max(max, n);
		return max;
	}
}