package com.festp.menu;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;
import com.festp.mainListener;

public class InventoryMenu implements Listener {
	private static mainListener plugin;
	private Inventory gui;
	private HashSet<MenuListener> listeners = new HashSet<>();
	
	public static void setPlugin(mainListener ml) {
		plugin = ml;
	}
	
	public InventoryMenu(MenuListener listener, ItemStack[] grid, String title) {
		addListener(listener);
		int size = ( (int) (grid.length / 9)+1 )*9;
		if(size > 54) size = 54;
		gui = Bukkit.createInventory(null, size, title);
		
		for(int i=0; i < Math.min(grid.length, size); i++) {
			gui.setItem(i, grid[i]);
		}
		
		plugin.getServer().getPluginManager().registerEvents(this,  plugin);
	}
	
	public InventoryMenu(MenuListener listener, ItemStack[] grid, String title, int rows) {
		addListener(listener);
		int size = ((int) (grid.length / 9)+1)*9; //minimum size
		size = Math.max(size, rows*9);
		if(size > 54) size = 54;
		gui = Bukkit.createInventory(null, size, title);
		
		for(int i=0; i < Math.min(grid.length, size); i++) {
			gui.setItem(i, grid[i]);
		}
		
		plugin.getServer().getPluginManager().registerEvents(this,  plugin);
	}
	
	public Inventory getGUI() {
		return gui;
	}
	
	public void addListener(MenuListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(MenuListener listener) {
		listeners.remove(listener);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		//event.getClickedInventory() != gui
		if(event.getAction() != InventoryAction.COLLECT_TO_CURSOR)
			if(event.getClickedInventory() == null || !Utils.equal_invs(event.getView().getTopInventory(),gui)
					|| (!Utils.equal_invs(event.getClickedInventory(),gui) && event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY)
					|| event.getAction() == InventoryAction.CLONE_STACK || event.getClick() == ClickType.CREATIVE)
				return;
		event.setCancelled(true);
		
		InventoryAction action = event.getAction();
		ClickType click = event.getClick();
		int slot = event.getSlot();
		if(action == InventoryAction.PICKUP_ALL && click == ClickType.LEFT) {
			for(MenuListener listener : listeners) {
				event.getInventory().setItem(slot,
						listener.onClick(slot, event.getCursor(), event.getCurrentItem(), MenuAction.LEFT_CLICK));
			}
		}
		else if(action == InventoryAction.PICKUP_HALF && click == ClickType.RIGHT) {
			for(MenuListener listener : listeners) {
				event.getInventory().setItem(slot,
						listener.onClick(slot, event.getCursor(), event.getCurrentItem(), MenuAction.RIGHT_CLICK));
			}
		}
		else if(action == InventoryAction.SWAP_WITH_CURSOR && (click == ClickType.LEFT || click == ClickType.RIGHT)) {
			for(MenuListener listener : listeners) {
				event.getInventory().setItem(slot,
						listener.onClick(slot, event.getCursor(), event.getCurrentItem(), MenuAction.SWAP_CLICK));
			}
		}
	}
	
	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		if(Utils.equal_invs(event.getView().getTopInventory(),gui))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(Utils.equal_invs(event.getInventory(), gui)) {
			for(MenuListener listener : listeners)
				listener.removeMenu();
			listeners.clear();
		}
	}

	public void changeSlot(int slot, ItemStack button) {
		gui.setItem(slot, button);
	}
}
