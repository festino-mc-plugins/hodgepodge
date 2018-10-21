package com.festp.inventory;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class DroppedItem {
	Player p;
	Item it;
	int time;
	
	public DroppedItem(Player p, Item it) {
		this.p = p;
		this.it = it;
		this.time = this.it.getPickupDelay();
		this.it.setPickupDelay(0);
	}
	
	
}
