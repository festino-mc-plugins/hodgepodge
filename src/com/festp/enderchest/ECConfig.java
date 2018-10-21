package com.festp.enderchest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class ECConfig {
	
	List<ECLocale> locales = new ArrayList<>();
	ECLocale current_locale;
	
	public ECConfig() {
		ECLocale eng = new ECLocale();
		eng.usage_create = ChatColor.GRAY + "Usage: \"/enderchest create <groupname>\" or \"/ec create <groupname>\"";
		eng.usage_acreate = ChatColor.GRAY + "Usage: \"/enderchest acreate <groupname>\" or \"/ec acreate <groupname>\"";
		eng.usage_invite = ChatColor.GRAY + "Usage: \"/enderchest invite <nickname>\" or \"/ec invite <nickname>\"";
		eng.usage_accept = ChatColor.GRAY + "Usage: \"/enderchest accept <groupname>\" or \"/ec accept <groupname>\"";
		eng.usage_kick = ChatColor.GRAY + "Usage: \"/enderchest kick <nickname>\" or \"/ec kick <nickname>\"";
		eng.usage_leaveowner = ChatColor.GRAY + "Usage: \"/enderchest leave <nickname>\" or \"/ec leave <nickname>\"";
		ECLocale rus = new ECLocale();
		rus.help = "";
	}
}
