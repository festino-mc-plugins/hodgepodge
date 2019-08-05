package com.festp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private static JavaPlugin plugin;
	private static MemoryConfiguration c;
	public static String pluginName;

	public static Map<String,Boolean> FunctionsON = new HashMap<>();
	public static double fallDamage = 0.71;
	public static int portal_search_radius = 30;
	//public static int pump_search_radius = 30;
	public static int step_count = 1;
	public static double step_percent = 0.7;
	public static int max_closed_inv_ticks = 20;
	public static int storage_signal_radius = 30;
	public static final int LEFT_ROTATE_COOLDOWN = 4;
	
	public Config(JavaPlugin jp) {
		this.plugin = jp;
		pluginName = plugin.getName();
		this.c = jp.getConfig();
	}
	
	public static void loadConfig()
	{
		c.addDefault("falldamagek", 0.6);
		c.addDefault("fun-on-daySleepSkip", true);
		c.addDefault("fun-on-extendedSleep", true);
		c.addDefault("maxNumberOfSleepless", 1);
		c.addDefault("maxPercentOfSleepless", 30);
		c.addDefault("storage-signal-radius", 30);
		List<String> l1 = new ArrayList<>();
		l1.add("accept");
		l1.add("create");
		l1.add("info");
		c.addDefault("enderchest.allungroupTips", l1);
		List<String> l2 = new ArrayList<>();
		l2.add("accept");
		l2.add("info");
		l2.add("leave");
		c.addDefault("enderchest.allgroupTips", l2);
		List<String> l3 = new ArrayList<>();
		l3.add("accept");
		l3.add("info");
		l3.add("leave");
		l3.add("kick");
		l3.add("invite");
		l3.add("changeowner");
		l3.add("delete");
		c.addDefault("enderchest.allownerTips", l3);
		c.options().copyDefaults(true);
		plugin.saveConfig();
		//getConfig().save(file);

		///fest config reload
		Config.fallDamage = plugin.getConfig().getDouble("falldamagek");
		Config.FunctionsON.put("extendedSleep", c.getBoolean("fun-on-extendedSleep"));
		Config.FunctionsON.put("daySleepSkip", c.getBoolean("fun-on-daySleepSkip"));
		Config.step_count = c.getInt("maxNumberOfSleepless");
		Config.step_percent = 1-c.getDouble("maxPercentOfSleepless")/100;
		Config.storage_signal_radius = plugin.getConfig().getInt("storage-signal-radius");

		System.out.println("["+pluginName+"] Config Reloaded.");
	}
	
	public static void saveConfig()
	{
		c.set("falldamagek", Config.fallDamage);
		c.set("fun-on-extendedSleep", Config.FunctionsON.get("extendedSleep"));
		c.set("fun-on-daySleepSkip", Config.FunctionsON.get("daySleepSkip"));
		c.set("maxNumberOfSleepless", Config.step_count);
		c.set("maxPercentOfSleepless", (1-Config.step_percent)*100);
		c.set("storage-signal-radius", Config.storage_signal_radius);

		plugin.saveConfig();
		
		System.out.println("["+pluginName+"] Config successfully saved.");
	}
	
	public static JavaPlugin plugin() {
		return plugin;
	}
}
