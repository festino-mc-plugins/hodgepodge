package com.festp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.commands.CommandWorker;
import com.festp.commands.ItemCommand;
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
import com.festp.jukebox.JukeboxHandler;
import com.festp.jukebox.JukeboxPacketListener;
import com.festp.jukebox.NoteDiscList;
import com.festp.jukebox.NoteSoundRecorder;
import com.festp.jukebox.RecordingBookList;
import com.festp.jukebox.NoteDiscCrafter;
import com.festp.maps.MapCraftHandler;
import com.festp.maps.MapHandler;
import com.festp.menu.InventoryMenu;
import com.festp.misc.InteractHandler;
import com.festp.misc.LeashManager;
import com.festp.misc.MountManager;
import com.festp.misc.AmethystManager;
import com.festp.misc.FeatureHandler;
import com.festp.misc.Sleeping;
import com.festp.misc.SoulStone;
import com.festp.storages.StorageCraftManager;
import com.festp.storages.StorageHandler;
import com.festp.storages.StoragesFileManager;
import com.festp.storages.StoragesList;
import com.festp.tome.TomeClickHandler;
import com.festp.tome.TomeEntityHandler;
import com.festp.tome.TomeItemHandler;
import com.festp.utils.BeamedPair;
import com.festp.utils.TimeUtils;
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
	public long metrics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

	public StoragesList stlist = new StoragesList();
	public StoragesFileManager ststorage = new StoragesFileManager(this);
	public StorageHandler sthandler = new StorageHandler(this);
	public StorageCraftManager stcraft = new StorageCraftManager(this, getServer());
	
	public AmethystManager amethyst_manager;
	
	public World mainworld = null;
	
	ExpHoppers exp_hop; 
	LeashManager lm;
	
	public static String getPath() {
		return PATH;
	}
	
	long t1;
	public void onEnable() {
		pluginname = getName();
		PATH = "plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator");
    	PluginManager pm = getServer().getPluginManager();
    	
		InventoryMenu.setPlugin(this);
		BeamedPair.setPlugin(this);
		Utils.setPlugin(this);
		Utils.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		
		//would better getting main world from server config
		for (World temp_world : getServer().getWorlds())
			if (temp_world.getEnvironment() == Environment.NORMAL) {
				mainworld = temp_world;
				break;
			}
		if (mainworld == null)
			mainworld = getServer().getWorlds().get(0);
		
		conf = new Config(this);
		Config.loadConfig();
    	craft_manager = new CraftManager(this, getServer());
		
    	File ECpluginFolder = new File(PATH + enderdir);
		if (ECpluginFolder.exists() == false) {
    		ECpluginFolder.mkdir();
    	}
		ecstorage = new EnderFileStorage(this);	
		
		int maxID = 0;
		for (Integer ID : StoragesFileManager.getIDList())
			if (ID > maxID)
				maxID = ID;
		StoragesFileManager.nextID = maxID+1;
		getLogger().info("New storages will start from ID " + StoragesFileManager.nextID + ".");
    	
    	CommandWorker command_worker = new CommandWorker(this);
    	getCommand(CommandWorker.MAIN_COMMAND).setExecutor(command_worker);
    	ItemCommand item_worker = new ItemCommand();
    	getCommand(ItemCommand.ITEM_COMMAND).setExecutor(item_worker);
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
    	MountManager mm = new MountManager();
    	pm.registerEvents(mm, this);
    	
    	InteractHandler interacts = new InteractHandler(this, lm);
    	pm.registerEvents(interacts, this);

    	FeatureHandler features = new FeatureHandler(this);
    	pm.registerEvents(features, this);
    	
    	SoulStone ss = new SoulStone();
    	pm.registerEvents(ss, this);
    	
    	TomeItemHandler summoner_tomes = new TomeItemHandler();
    	pm.registerEvents(summoner_tomes, this);
    	TomeClickHandler click_tomes = new TomeClickHandler();
    	pm.registerEvents(click_tomes, this);
    	TomeEntityHandler entity_tomes = new TomeEntityHandler();
    	pm.registerEvents(entity_tomes, this);
    	
    	SortHoppers sh = new SortHoppers();
    	pm.registerEvents(sh, this);
    	
    	MapCraftHandler mapCrafts = new MapCraftHandler();
    	pm.registerEvents(mapCrafts, this);
    	MapHandler mapHandler = new MapHandler();
    	pm.registerEvents(mapHandler, this);

    	GlassItemFrameHandler glassItemFrames = new GlassItemFrameHandler();
    	pm.registerEvents(glassItemFrames, this);

    	amethyst_manager = new AmethystManager();
    	pm.registerEvents(amethyst_manager, this);

    	exp_hop = new ExpHoppers(getServer());
    	pm.registerEvents(exp_hop, this);
    	
    	craft_manager.addCrafts();
    	pm.registerEvents(craft_manager, this);

    	NoteDiscList noteDiscList = new NoteDiscList();
    	JukeboxHandler jukeboxHandler = new JukeboxHandler(noteDiscList);
    	pm.registerEvents(jukeboxHandler, this);
    	NoteDiscCrafter noteDiscListener = new NoteDiscCrafter();
    	pm.registerEvents(noteDiscListener, this);

    	RecordingBookList recordingBookList = new RecordingBookList();
    	NoteSoundRecorder noteSoundRecorder = new NoteSoundRecorder(recordingBookList);
    	pm.registerEvents(noteSoundRecorder, this);

    	JukeboxPacketListener jukePackets = new JukeboxPacketListener(jukeboxHandler);
    	pm.registerEvents(jukePackets, this);
    	
		t1 = System.nanoTime();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
			new Runnable() {
				public void run() {
					TimeUtils.addTick();
					
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
					
					mm.tick();

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
					click_tomes.saveAll();

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
					
					t2 = System.nanoTime();
					metrics[13] += t2 - t1;
					t1 = t2;
					
					jukeboxHandler.tick();
					
					noteDiscList.tick();
					
					recordingBookList.tick();
					
					t2 = System.nanoTime();
					metrics[14] += t2 - t1;
					t1 = t2;
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
