package com.festp.inventory;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class DroppedItem {
	Player p;
	Item item;
	int time;
	
	public DroppedItem(Player p, Item it) {
		this.p = p;
		this.item = it;
		this.time = this.item.getPickupDelay();
		this.item.setPickupDelay(0);
	}
	
	
}
