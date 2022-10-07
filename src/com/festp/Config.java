package com.festp;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	
	private static JavaPlugin plugin;
	private static MemoryConfiguration c;

	public static Map<String,Boolean> FunctionsON = new HashMap<>();
	public static double fallDamage = 0.71;
	public static int portalSearchRadius = 30;
	public static int maxSleeplessCount = 1;
	public static double maxSleeplessPercent = 0.7;
	public static int maxClosedInvTicks = 20;
	public static final int LEFT_ROTATE_COOLDOWN = 4;
	
	public Config(JavaPlugin jp) {
		this.plugin = jp;
		this.c = jp.getConfig();
	}
	
	public static void loadConfig()
	{
		c.addDefault("falldamagek", 0.6);
		c.addDefault("fun-on-daySleepSkip", true);
		c.addDefault("fun-on-extendedSleep", true);
		c.addDefault("maxNumberOfSleepless", 1);
		c.addDefault("maxPercentOfSleepless", 30);
		c.options().copyDefaults(true);
		plugin.saveConfig();
		//getConfig().save(file);

		Config.fallDamage = plugin.getConfig().getDouble("falldamagek");
		Config.FunctionsON.put("extendedSleep", c.getBoolean("fun-on-extendedSleep"));
		Config.FunctionsON.put("daySleepSkip", c.getBoolean("fun-on-daySleepSkip"));
		Config.maxSleeplessCount = c.getInt("maxNumberOfSleepless");
		Config.maxSleeplessPercent = 1-c.getDouble("maxPercentOfSleepless")/100;

		Logger.info("Config reloaded.");
	}
	
	public static void saveConfig()
	{
		c.set("falldamagek", Config.fallDamage);
		c.set("fun-on-extendedSleep", Config.FunctionsON.get("extendedSleep"));
		c.set("fun-on-daySleepSkip", Config.FunctionsON.get("daySleepSkip"));
		c.set("maxNumberOfSleepless", Config.maxSleeplessCount);
		c.set("maxPercentOfSleepless", (1-Config.maxSleeplessPercent)*100);

		plugin.saveConfig();

		Logger.info("Config successfully saved.");
	}
	
	public static JavaPlugin plugin() {
		return plugin;
	}
}
