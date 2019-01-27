package com.festp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Dispenser;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftAnimals;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftItem;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_13_R2.potion.CraftPotionBrewer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.material.Cauldron;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
//import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.festp.amethyst.ActiveAmSpawnBlocking;
import com.festp.amethyst.PathfinderGoalAvoidAmBlocks;
import com.festp.amethyst.PathfinderGoalAvoidAmEntities;
import com.festp.dispenser.DropActions;
import com.festp.enderchest.AdminChannelPlayer;
import com.festp.enderchest.ECTabCompleter;
import com.festp.enderchest.EnderChestGroup;
import com.festp.enderchest.EnderChestHandler;
import com.festp.enderchest.EnderFileStorage;
import com.festp.inventory.ExpHoppers;
import com.festp.inventory.InventoryHandler;
import com.festp.inventory.SortHoppers;
import com.festp.menu.InventoryMenu;
import com.festp.remain.InteractHandler;
import com.festp.remain.LeashManager;
import com.festp.remain.Others;
import com.festp.remain.Sleeping;
import com.festp.remain.SoulStone;
import com.festp.remain.SummonerTome;
import com.festp.storages.BeamedPair;
import com.festp.storages.Storage;
import com.festp.storages.StorageHandler;
import com.festp.storages.StoragesFileManager;
import com.festp.storages.StoragesList;

import net.minecraft.server.v1_13_R2.EntityAgeable;
import net.minecraft.server.v1_13_R2.EntityAnimal;
import net.minecraft.server.v1_13_R2.EntityCow;
import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityInsentient;
import net.minecraft.server.v1_13_R2.EntityItem;
import net.minecraft.server.v1_13_R2.EntityMonster;
import net.minecraft.server.v1_13_R2.EntityOcelot;
//import net.minecraft.server.v1_13_R2.EnumParticle;
import net.minecraft.server.v1_13_R2.Items;
import net.minecraft.server.v1_13_R2.PathfinderGoalBreed;
import net.minecraft.server.v1_13_R2.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_13_R2.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_13_R2.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_13_R2.PathfinderGoalPanic;
import net.minecraft.server.v1_13_R2.PathfinderGoalTempt;

import ru.tehkode.permissions.bukkit.PermissionsEx;
import somebodyelse.code.FlatFileStorage;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class mainListener extends JavaPlugin implements Listener
{
	Config conf;
	ActiveAmSpawnBlocking spawnblock;
	private CraftManager craft_manager;

	public List<AdminChannelPlayer> admin_ecplayers = new ArrayList<>();
	public EnderChestGroup ecgroup = new EnderChestGroup(this);
	public EnderFileStorage ecstorage;
	private int groupticks = 0;
	private int maxgroupticks = 3*60*20; //3 minutes

	public StoragesList stlist = new StoragesList();
	public StoragesFileManager ststorage = new StoragesFileManager(this);
	
	public static String pluginname;
	public static final String enderdir = "EnderChestGroups";
	public static final String storagesdir = "Storages";
	
	public World mainworld = null;
	
	ExpHoppers eh; 
	LeashManager lm;
	
	public void onEnable()
	{
		pluginname = getName();
    	PluginManager pm = getServer().getPluginManager();
    	
		InventoryMenu.setPlugin(this);
		BeamedPair.setPlugin(this);
		Utils.setPlugin(this);
		Utils.onEnable();
		Storage.pl = this;
		getServer().getPluginManager().registerEvents(this, this);
		
		//would better getting main world from server config
		for(World temp_world : getServer().getWorlds())
			if(temp_world.getWorldType() == WorldType.NORMAL && temp_world.getEnvironment() == Environment.NORMAL) {
				mainworld = temp_world;
				break;
			}
		if(mainworld == null) mainworld = getServer().getWorlds().get(0);
		
		conf = new Config(this);
		spawnblock = new ActiveAmSpawnBlocking();
		Config.loadConfig();
    	craft_manager = new CraftManager(this, getServer());
		
    	File ECpluginFolder = new File("plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator") + enderdir);
		if (ECpluginFolder.exists() == false) {
    		ECpluginFolder.mkdir();
    	}
		ecstorage = new EnderFileStorage(this);	
		
    	File STpluginFolder = new File("plugins" + System.getProperty("file.separator") + pluginname + System.getProperty("file.separator") + storagesdir);
		if (STpluginFolder.exists() == false) {
    		STpluginFolder.mkdir();
    	}
		int maxID = 0;
		for(String s : STpluginFolder.list()) {
			if(s.length() < 5) continue;
			s = s.substring(0, s.length()-4);
			try {
				if(Integer.parseInt(s) > maxID) {
					maxID = Integer.parseInt(s);
				}
			} catch (Exception ex) {
				System.out.println("["+pluginname+"] Storages: Wrong file in directory: " + s);
			}
		}
		ststorage.nextID = maxID+1;
		for(int i=0; i<54; i++) {
			Storage.empty_inventory[i] = null;
		}
    	
    	CommandWorker command_worker = new CommandWorker(this);
    	getCommand("fest").setExecutor(command_worker);
    	getCommand("item").setExecutor(command_worker);
    	
    	StorageHandler handler_storage = new StorageHandler(this);
    	pm.registerEvents(handler_storage, this);
    	
    	EnderChestHandler ecH = new EnderChestHandler(this);
    	//pm.registerEvents(new EnderChestHandler(this), this);
    	pm.registerEvents(ecH, this);
    	getCommand("enderchest").setExecutor(ecH);
    	getCommand("ec").setExecutor(ecH);
    	
    	ECTabCompleter ectc = new ECTabCompleter(this);
    	getCommand("enderchest").setTabCompleter(ectc);
    	getCommand("ec").setTabCompleter(ectc);
    	ecgroup.loadEnderChests(ecstorage, ECpluginFolder.list());
    	
    	Sleeping sl = new Sleeping(this);

    	DropActions drop_actions = new DropActions(this);
    	pm.registerEvents(drop_actions, this);

    	InventoryHandler ih = new InventoryHandler();
    	pm.registerEvents(ih, this);

    	lm = new LeashManager(this);
    	InteractHandler ih2 = new InteractHandler(this, lm);
    	pm.registerEvents(ih2, this);

    	Others features = new Others(this);
    	pm.registerEvents(features, this);
    	
    	SoulStone ss = new SoulStone();
    	pm.registerEvents(ss, this);
    	
    	SummonerTome st = new SummonerTome(this);
    	pm.registerEvents(st, this);
    	
    	SortHoppers sh = new SortHoppers();
    	pm.registerEvents(sh, this);

    	eh = new ExpHoppers(getServer());
    	pm.registerEvents(eh, this); //TO DO: try add canceling interact system
    	
    	craft_manager.addCrafts();
    	pm.registerEvents(craft_manager, this);
    	
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this,
			new Runnable() {
				public void run() {
					for(World w : getServer().getWorlds())
			    	{
			    		for(Entity e : w.getEntities())
			    		{
			    			if(e.getMetadata("amfear") == null || e.getMetadata("amfear").isEmpty()) {
			    				e.setMetadata("amfear", new FixedMetadataValue(conf.plugin(),"1"));
			    				applyAmethystFear(e);
			    			}
			    		}
			    	}
					
					groupticks+=1;
					if(groupticks >= maxgroupticks) {
						ecgroup.saveEnderChests(ecstorage);
						groupticks = 0;
					}
					
					//skip night by sleep
					sl.onTick();
					
					//fill cauldrons, feed animals and pump liquids
					drop_actions.onTick();
					
					//items in cauldrons
					ih2.onTick();
					
					//lasso and jumping rope
					lm.tick();
					
					//shulker and chest items dropping on 'F' (default)
					ih.onTick();
					
					//login in portal fix, vertical fireworks, silk touch 2 and jump boost damage reduce
					features.onTick();
					
					//save horse data to tome
					st.onTick();
					
					//beam, unload, save, process
					handler_storage.onTick();
					
					//drag xp
					eh.onTick();
				}
			},0L,1L);
		
	}
	
	public CraftManager getCraftManager()
	{
		return craft_manager;
	}
	
	public void onDisable()
	{
		lm.onDisable();
		Utils.onDisable();
		
		eh.save();
		ecgroup.saveEnderChests(ecstorage);
		stlist.saveStorages();
		for(Player p : getServer().getOnlinePlayers()) {
			p.closeInventory();
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		Entity e = event.getEntity();
		if(!spawnblock.canspawn(e)) {
			event.setCancelled( true );
			if(e.getVehicle() != null)
				e.getVehicle().remove();
		} else {
			applyAmethystFear(e);
			e.setMetadata("amfear", new FixedMetadataValue(conf.plugin(),"1"));
		}
	}
	
	public void applyAmethystFear(Entity e) { //Avoidance
		if(e instanceof Skeleton || e instanceof Zombie || e instanceof Creeper) {
			EntityInsentient ei = (EntityInsentient)((CraftLivingEntity)e).getHandle();
			//ei.goalSelector.a(0, new PathfinderGoalAvoidAmEntities((EntityCreature)(((CraftLivingEntity)e).getHandle()), 20.0F, 1.2D, 1.5D, 20 ));
			ei.goalSelector.a(0, new PathfinderGoalAvoidAmBlocks((EntityCreature)(((CraftLivingEntity)e).getHandle()), 1.0D, 1.2D, 15, 4 ));
		}
	}
}
