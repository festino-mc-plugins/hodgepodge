package com.festp;

import org.bukkit.entity.Player;

public class CooldownPlayer {
	private Player player;
	private int remaining_time;
	
	public CooldownPlayer(Player p, int full_time) {
		player = p;
		remaining_time = full_time;
	}
	
	public boolean tick() {
		if(remaining_time <= 0)
			return false;
		remaining_time--;
		return true;
	}
	
	public Player getPlayer() {
		return player;
	}
}
