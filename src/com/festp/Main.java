package com.festp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.CommandWorker;
import com.festp.commands.ItemCommand;
import com.festp.dispenser.DropActions;
import com.festp.inventory.ExpHoppers;
import com.festp.inventory.InventoryHandler;
import com.festp.inventory.SortHoppers;
import com.festp.misc.InteractHandler;
import com.festp.misc.FeatureHandler;
import com.festp.misc.GlassItemFrameHandler;
import com.festp.misc.Sleeping;
import com.festp.utils.Metrics;
import com.festp.utils.Metrics.MetricCategory;

public class Main extends JavaPlugin
{
	private static String PATH = "plugins" + System.getProperty("file.separator") + "HodgePodge" + System.getProperty("file.separator");
	private static String pluginname;
	
	Config conf;
	private CraftManager craftManager;
	
	public World mainWorld = null;
	
	ExpHoppers expHop;
	
	public static String getPath() {
		return PATH;
	}
	
	long t1;
	public void onEnable() {
		Logger.setLogger(getLogger());
		pluginname = getName();
		PATH = "plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator");
    	PluginManager pm = getServer().getPluginManager();
		
		//would better getting main world from server config
		for (World tempWorld : getServer().getWorlds())
			if (tempWorld.getEnvironment() == Environment.NORMAL) {
				mainWorld = tempWorld;
				break;
			}
		if (mainWorld == null)
			mainWorld = getServer().getWorlds().get(0);
		
		conf = new Config(this);
		Config.loadConfig();
    	craftManager = new CraftManager(this, getServer());
    	Metrics metrics = new Metrics();
    	
    	CommandWorker commandWorker = new CommandWorker(metrics);
    	getCommand(CommandWorker.MAIN_COMMAND).setExecutor(commandWorker);
    	ItemCommand itemWorker = new ItemCommand();
    	getCommand(ItemCommand.ITEM_COMMAND).setExecutor(itemWorker);
    	
    	Sleeping sleep = new Sleeping(this);
    	pm.registerEvents(sleep, this);

    	InventoryHandler invs = new InventoryHandler();
    	pm.registerEvents(invs, this);
    	
    	InteractHandler interacts = new InteractHandler(this);
    	pm.registerEvents(interacts, this);

    	FeatureHandler features = new FeatureHandler(this);
    	pm.registerEvents(features, this);
    	
    	SortHoppers sh = new SortHoppers();
    	pm.registerEvents(sh, this);

    	GlassItemFrameHandler glassItemFrames = new GlassItemFrameHandler();
    	pm.registerEvents(glassItemFrames, this);

    	expHop = new ExpHoppers(getServer(), Main.getPath());
    	pm.registerEvents(expHop, this);
    	
    	craftManager.addCrafts();
    	pm.registerEvents(craftManager, this);
    	
    	DropActions dropActions = new DropActions();
    	pm.registerEvents(dropActions, this);
    	
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
			new Runnable() {
				public void run() {
					metrics.tick();

					metrics.start(MetricCategory.TASK_LIST);
					TaskList.tick();
					metrics.end(MetricCategory.TASK_LIST);

					// skip night by sleep
					metrics.start(MetricCategory.SLEEPING);
					sleep.onTick();
					metrics.end(MetricCategory.SLEEPING);
					
					// items in cauldrons, hoe left click cooldown
					metrics.start(MetricCategory.INTERACT_HANDLER);
					interacts.onTick();
					metrics.end(MetricCategory.INTERACT_HANDLER);
					
					// shulker and chest items dropping on 'F' (default)
					metrics.start(MetricCategory.INVENTORY_HANDLER);
					invs.onTick();
					metrics.end(MetricCategory.INVENTORY_HANDLER);
					
					// login in portal fix, vertical fireworks, silk touch 2 and jump boost damage reduce
					metrics.start(MetricCategory.FEATURE_HANDLER);
					features.onTick();
					metrics.end(MetricCategory.FEATURE_HANDLER);
					
					// drag xp
					metrics.start(MetricCategory.EXP_HOPPERS);
					//expHop.onTick();
					metrics.end(MetricCategory.EXP_HOPPERS);

					// fill cauldrons, feed animals
					metrics.start(MetricCategory.DISPENSERS);
					dropActions.onTick();
					metrics.end(MetricCategory.DISPENSERS);
				}
			}, 0L, 1L);
		
	}
	
	public void onDisable()
	{
		expHop.saveAll();
	}
}
