package com.festp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.CommandWorker;
import com.festp.commands.StorageCommand;
import com.festp.dispenser.DropActions;
import com.festp.enderchest.AdminChannelPlayer;
import com.festp.enderchest.ECCommandWorker;
import com.festp.enderchest.ECTabCompleter;
import com.festp.enderchest.EnderChestGroup;
import com.festp.enderchest.EnderChestHandler;
import com.festp.enderchest.EnderFileStorage;
import com.festp.inventory.ExpHoppers;
import com.festp.inventory.InventoryHandler;
import com.festp.inventory.SortHoppers;
import com.festp.maps.SmallMapManager;
import com.festp.menu.InventoryMenu;
import com.festp.misc.InteractHandler;
import com.festp.misc.LeashManager;
import com.festp.misc.FeatureManager;
import com.festp.misc.Sleeping;
import com.festp.misc.SoulStone;
import com.festp.misc.SummonerTome;
import com.festp.storages.StorageCraftManager;
import com.festp.storages.StorageHandler;
import com.festp.storages.StoragesFileManager;
import com.festp.storages.StoragesList;
import com.festp.utils.BeamedPair;
import com.festp.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin implements Listener
{
	public static final String enderdir = "EnderChestGroups";
	public static final String storagesdir = "Storages";
	public static final String mapsdir = "Maps";
	private static String PATH = "plugins" + System.getProperty("file.separator") + "HodgePodge" + System.getProperty("file.separator");
	private static String pluginname;
	
	Config conf;
	private CraftManager craft_manager;

	public List<AdminChannelPlayer> admin_ecplayers = new ArrayList<>();
	public EnderChestGroup ecgroup = new EnderChestGroup(this);
	public EnderFileStorage ecstorage;
	private int groupticks = 0;
	private int maxgroupticks = 3*60*20; //3 minutes
	
	//TODO: metrics class, no public
	public int metrics_ticks = 0;
	public int max_metrics_ticks = 60*20; //3 minutes
	public long metrics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	public StoragesList stlist = new StoragesList();
	public StoragesFileManager ststorage = new StoragesFileManager(this);
	public StorageHandler sthandler = new StorageHandler(this);
	public StorageCraftManager stcraft = new StorageCraftManager(this, getServer());
	
	public World mainworld = null;
	
	ExpHoppers exp_hop; 
	LeashManager lm;
	
	public static String getPath() {
		return PATH;
	}
	
	long t1;
	public void onEnable()
	{
		pluginname = getName();
		PATH = "plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator");
    	PluginManager pm = getServer().getPluginManager();
    	
		InventoryMenu.setPlugin(this);
		BeamedPair.setPlugin(this);
		Utils.setPlugin(this);
		Utils.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		
		//would better getting main world from server config
		for(World temp_world : getServer().getWorlds())
			if(temp_world.getWorldType() == WorldType.NORMAL && temp_world.getEnvironment() == Environment.NORMAL) {
				mainworld = temp_world;
				break;
			}
		if(mainworld == null) mainworld = getServer().getWorlds().get(0);
		
		conf = new Config(this);
		Config.loadConfig();
    	craft_manager = new CraftManager(this, getServer());
		
    	File ECpluginFolder = new File(PATH + enderdir);
		if (ECpluginFolder.exists() == false) {
    		ECpluginFolder.mkdir();
    	}
		ecstorage = new EnderFileStorage(this);	
		
		int maxID = 0;
		for(Integer ID : StoragesFileManager.getIDList())
			if(ID > maxID)
				maxID = ID;
		StoragesFileManager.nextID = maxID+1;
		getLogger().info("New storages will start from ID " + StoragesFileManager.nextID + ".");
    	
    	CommandWorker command_worker = new CommandWorker(this);
    	getCommand(CommandWorker.MAIN_COMMAND).setExecutor(command_worker);
    	getCommand(CommandWorker.ITEM_COMMAND).setExecutor(command_worker);
    	StorageCommand storage_worker = new StorageCommand(stlist, ststorage);
    	getCommand(StorageCommand.ST_COMMAND).setExecutor(storage_worker);
    	
    	pm.registerEvents(sthandler, this);
    	
    	EnderChestHandler ecH = new EnderChestHandler(this);
    	pm.registerEvents(ecH, this);
    	
    	ECCommandWorker ecCW = new ECCommandWorker(this);
    	getCommand("enderchest").setExecutor(ecCW);
    	getCommand("ec").setExecutor(ecCW);
    	
    	ECTabCompleter ectc = new ECTabCompleter(this);
    	getCommand("enderchest").setTabCompleter(ectc);
    	getCommand("ec").setTabCompleter(ectc);
    	ecgroup.loadEnderChests(ecstorage, ECpluginFolder.list());
    	
    	Sleeping sleep = new Sleeping(this);
    	pm.registerEvents(sleep, this);

    	DropActions drop_actions = new DropActions(this);
    	pm.registerEvents(drop_actions, this);

    	InventoryHandler invs = new InventoryHandler();
    	pm.registerEvents(invs, this);

    	lm = new LeashManager(this);
    	InteractHandler interacts = new InteractHandler(this, lm);
    	pm.registerEvents(interacts, this);

    	FeatureManager features = new FeatureManager(this);
    	pm.registerEvents(features, this);
    	
    	SoulStone ss = new SoulStone();
    	pm.registerEvents(ss, this);
    	
    	SummonerTome summoner_tomes = new SummonerTome(this);
    	pm.registerEvents(summoner_tomes, this);
    	
    	SortHoppers sh = new SortHoppers();
    	pm.registerEvents(sh, this);
    	
    	SmallMapManager minimaps = new SmallMapManager();
    	pm.registerEvents(minimaps, this);

    	exp_hop = new ExpHoppers(getServer());
    	pm.registerEvents(exp_hop, this);
    	
    	craft_manager.addCrafts();
    	pm.registerEvents(craft_manager, this);
    	
		t1 = System.nanoTime();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
			new Runnable() {
				public void run() {
					metrics_ticks++;
					if (metrics_ticks > max_metrics_ticks) {
						for (int i = 0; i < metrics.length; i++)
							metrics[i] = 0;
						metrics_ticks = 0;
					}

					long t2 = System.nanoTime();
					metrics[0] += t2 - t1;
					t1 = t2;
					
					TaskList.tick();

					t2 = System.nanoTime();
					metrics[1] += t2 - t1;
					t1 = t2;
					
					groupticks++;
					if(groupticks >= maxgroupticks) {
						ecgroup.saveEnderChests(ecstorage);
						groupticks = 0;
					}

					t2 = System.nanoTime();
					metrics[2] += t2 - t1;
					t1 = t2;
					
					//skip night by sleep
					sleep.onTick();

					t2 = System.nanoTime();
					metrics[3] += t2 - t1;
					t1 = t2;
					
					//fill cauldrons, feed animals and pump liquids
					drop_actions.onTick();

					t2 = System.nanoTime();
					metrics[4] += t2 - t1;
					t1 = t2;
					
					//items in cauldrons, saddle hp updating, hoe left click cooldown
					interacts.onTick();

					t2 = System.nanoTime();
					metrics[5] += t2 - t1;
					t1 = t2;
					
					//lasso and jumping rope
					lm.tick();

					t2 = System.nanoTime();
					metrics[6] += t2 - t1;
					t1 = t2;
					
					//shulker and chest items dropping on 'F' (default)
					invs.onTick();

					t2 = System.nanoTime();
					metrics[7] += t2 - t1;
					t1 = t2;
					
					//login in portal fix, vertical fireworks, silk touch 2 and jump boost damage reduce
					features.onTick();

					t2 = System.nanoTime();
					metrics[8] += t2 - t1;
					t1 = t2;
					
					//save horse data to tome
					summoner_tomes.onTick();

					t2 = System.nanoTime();
					metrics[9] += t2 - t1;
					t1 = t2;
					
					//beam, unload, save, process
					sthandler.onTick();

					t2 = System.nanoTime();
					metrics[10] += t2 - t1;
					t1 = t2;
					
					//drag xp
					//exp_hop.onTick();

					t2 = System.nanoTime();
					metrics[11] += t2 - t1;
					t1 = t2;
					
					//move and remove
					BeamedPair.tickAll();

					t2 = System.nanoTime();
					metrics[12] += t2 - t1;
					t1 = t2;
					
					ecH.tick();
				}
			}, 0L, 1L);
		
	}
	
	public CraftManager getCraftManager()
	{
		return craft_manager;
	}
	
	public void onDisable()
	{
		lm.onDisable();
		Utils.onDisable();
		
		exp_hop.save();
		ecgroup.saveEnderChests(ecstorage);
		stlist.saveStorages();
		for(Player p : getServer().getOnlinePlayers()) {
			p.closeInventory();
		}
	}
}
