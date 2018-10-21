package com.festp.enderchest;

import org.bukkit.entity.Player;

public class AdminChannelPlayer {
	EnderChest adminec;
	Player p;
	
	public AdminChannelPlayer(Player p, EnderChest ec)
	{
		this.p = p;
		this.adminec = ec;
	}
	
}
