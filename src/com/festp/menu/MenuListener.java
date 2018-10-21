package com.festp.menu;

import org.bukkit.inventory.ItemStack;

public interface MenuListener {
	//pickup all/collect to cursor(LMB), pickup half(RMB), swap[cancel]
	//slot, before -> after, click type ---> 
	public ItemStack onClick(int slot, ItemStack cursor_item, ItemStack slot_item, MenuAction action);
	public void removeMenu();
}
