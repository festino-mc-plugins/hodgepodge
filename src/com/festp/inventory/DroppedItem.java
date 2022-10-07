package com.festp.inventory;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class DroppedItem {
	public Player p;
	public Item item;
	public int time;
	
	public DroppedItem(Player p, Item it) {
		this.p = p;
		this.item = it;
		this.time = this.item.getPickupDelay();
		this.item.setPickupDelay(0);
	}
	
	
}
