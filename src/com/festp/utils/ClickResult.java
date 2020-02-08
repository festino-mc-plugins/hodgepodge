package com.festp.utils;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ClickResult {
	public enum ClickDir {OUTSIDE, CURSOR, TOP, BOTTOM, BOTH, NOTHING};
	
	public ClickDir from, to;
	public int items_1_to_2 = 0, items_2_to_1 = 0;
	
	public ClickResult(ClickDir from, ClickDir to, int amount_1_to_2, int amount_2_to_1)
	{
		this.from = from;
		this.to= to;
		items_1_to_2 = amount_1_to_2;
		items_2_to_1 = amount_2_to_1;
	}
	
	public boolean fillsBottom()
	{
		return items_1_to_2 > 0 && (to == ClickDir.BOTTOM || to == ClickDir.BOTH)
			|| items_2_to_1 > 0 && (from == ClickDir.BOTTOM || from == ClickDir.BOTH);
	}
	public boolean fillsTop()
	{
		return items_1_to_2 > 0 && (to == ClickDir.TOP || to == ClickDir.BOTH)
			|| items_2_to_1 > 0 && (from == ClickDir.TOP || from == ClickDir.BOTH);
	}
	public boolean fillsCursor()
	{
		return items_1_to_2 > 0 && to == ClickDir.CURSOR
			|| items_2_to_1 > 0 && from == ClickDir.CURSOR;
	}
	
	public boolean fromBottom()
	{
		return items_1_to_2 > 0 && (from == ClickDir.BOTTOM || from == ClickDir.BOTH)
			|| items_2_to_1 > 0 && (to == ClickDir.BOTTOM || to == ClickDir.BOTH);
	}
	public boolean fromTop()
	{
		return items_1_to_2 > 0 && (from == ClickDir.TOP || from == ClickDir.BOTH)
			|| items_2_to_1 > 0 && (to == ClickDir.TOP || to == ClickDir.BOTH);
	}
	public boolean fromCursor()
	{
		return items_1_to_2 > 0 && from == ClickDir.CURSOR
			|| items_2_to_1 > 0 && to == ClickDir.CURSOR;
	}
	
	public static ClickResult getClickResult(InventoryClickEvent event)
	{
		ItemStack cursor = event.getCursor(), current = event.getCurrentItem();
		Inventory clicked = event.getClickedInventory(), top = event.getView().getTopInventory(), bot = event.getView().getBottomInventory();
		boolean is_top = clicked == top, is_bot = clicked == bot;
		ClickDir from = null, to = null;
		ClickDir clicked_result = is_top ? ClickDir.TOP : is_bot ? ClickDir.BOTTOM : ClickDir.NOTHING;
		Integer amount_1_to_2 = 0, amount_2_to_1 = 0;
		switch(event.getAction())
		{
		case PLACE_ALL:
			from = ClickDir.CURSOR;
			to = clicked_result;
			amount_1_to_2 = cursor.getAmount();
			break;
		case PLACE_ONE:
			from = ClickDir.CURSOR;
			to = clicked_result;
			amount_1_to_2 = cursor.getAmount();
			break;
		case PLACE_SOME:
			from = ClickDir.CURSOR;
			to = clicked_result;
			amount_1_to_2 = current.getMaxStackSize() - current.getAmount();
			break;
		case SWAP_WITH_CURSOR:
			from = ClickDir.CURSOR;
			if (is_top)
				to = ClickDir.TOP;
			else if (is_bot)
				to = ClickDir.BOTTOM;
			amount_1_to_2 = cursor.getAmount();
			amount_2_to_1 = current.getAmount();
			break;
		case PICKUP_ALL:
			from = clicked_result;
			to = ClickDir.CURSOR;
			amount_1_to_2 = current.getAmount();
			break;
		case PICKUP_HALF:
			from = clicked_result;
			to = ClickDir.CURSOR;
			amount_1_to_2 = current.getAmount() - current.getAmount() / 2;
			break;
		case PICKUP_ONE:
			from = clicked_result;
			to = ClickDir.CURSOR;
			amount_1_to_2 = 1;
			break;
		case PICKUP_SOME:
			from = clicked_result;
			to = ClickDir.CURSOR;
			amount_1_to_2 = -current.getAmount(); //???
			break;
		case COLLECT_TO_CURSOR: //from top to bottom slots
			from = ClickDir.TOP;
			amount_1_to_2 = 1;
			//count > 0 isSimilar
			from = ClickDir.BOTH;
			to = ClickDir.CURSOR;
			break;
		case CLONE_STACK:
			from = ClickDir.NOTHING;
			to = ClickDir.CURSOR;
			amount_1_to_2 = current.getAmount();
			break;
		case MOVE_TO_OTHER_INVENTORY:
			if (is_top) {
				from = ClickDir.TOP;
				to = ClickDir.BOTTOM;
				int max_stack = current.getMaxStackSize();
				ItemStack[] stacks = bot.getContents();
				for (int i = stacks.length - 1; i >= 0; i--) {
					if (stacks[i] == null) {
						amount_1_to_2 = current.getAmount();
						break;
					}
					else if (stacks[i].getType() == current.getType()) {
						amount_1_to_2 += max_stack - stacks[i].getAmount();
						if (amount_1_to_2 >= current.getAmount()) {
							amount_1_to_2 = current.getAmount();
							break;
						}
					}
				}
			}
			else if (is_bot) {
				from = ClickDir.BOTTOM;
				to = ClickDir.TOP;
				int max_stack = current.getMaxStackSize();
				ItemStack[] stacks = top.getContents();
				for (int i = 0; i < stacks.length; i++) {
					if (stacks[i] == null) {
						amount_1_to_2 = current.getAmount();
						break;
					}
					else if (stacks[i].getType() == current.getType()) {
						amount_1_to_2 += max_stack - stacks[i].getAmount();
						if (amount_1_to_2 >= current.getAmount()) {
							amount_1_to_2 = current.getAmount();
							break;
						}
					}
				}
			}
			break;
		case HOTBAR_MOVE_AND_READD:
		case HOTBAR_SWAP:
			if (event.getClick() == ClickType.NUMBER_KEY) {
				to = ClickDir.BOTTOM;
				from = clicked_result;
				if (is_top) {
					amount_1_to_2 = current.getAmount();
					ItemStack hotbar_slot = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
					amount_2_to_1 = hotbar_slot == null ? 0 : hotbar_slot.getAmount();
				}
			}
			break;
		case DROP_ALL_CURSOR:
			from = ClickDir.CURSOR;
			to = ClickDir.OUTSIDE;
			amount_1_to_2 = cursor.getAmount();
			break;
		case DROP_ONE_CURSOR:
			from = ClickDir.CURSOR;
			to = ClickDir.OUTSIDE;
			amount_1_to_2 = 1;
			break;
		case DROP_ALL_SLOT:
			from = clicked_result;
			to = ClickDir.OUTSIDE;
			amount_1_to_2 = current.getAmount();
			break;
		case DROP_ONE_SLOT:
			from = clicked_result;
			to = ClickDir.OUTSIDE;
			amount_1_to_2 = 1;
			break;
		default:
			from = ClickDir.NOTHING;
			to = ClickDir.NOTHING;
			break;
		}
		
		return new ClickResult(from, to, amount_1_to_2, amount_2_to_1);
	}
}
