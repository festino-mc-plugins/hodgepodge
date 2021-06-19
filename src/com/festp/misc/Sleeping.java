package com.festp.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.festp.Config;
import com.festp.DelayedTask;
import com.festp.TaskList;
import com.festp.Main;
import com.festp.utils.Utils;
import com.festp.utils.UtilsType;

public class Sleeping implements Listener {
	int ticks = 0;
	int info_ticks = 0;
	public static final int SLEEP_TICKS = 100;
	public static final int INFO_TICKS = 10;
	private static Random mob_spawn_random = new Random();
	private enum Skip {day, night};
	int ignoredPlayersCount = 0, sleepingPlayersCount = 0, onlinePlayersCount = 0;
	
	private Main pl;
	
	public Sleeping(Main plugin) {
		this.pl = plugin;
	}
	
	public void onTick()
	{
		ticks++;
		if (ticks > 200)
		{
			update_sleepers();
			try_skip();
			ticks = 0;
		}
		
		info_ticks++;
		if (info_ticks >= INFO_TICKS)
		{
			sendSleepInfo();
			info_ticks = 0;
		}
	}
	
	public void sendSleepInfo() // TO DO: sync sending with bed events
	{
		String sleep_data = "sendSleepInfo() error";
		int sleeping_players = 0;
		List<Integer> sleeping_ticks = new ArrayList<>();
		for (Player p : pl.getServer().getOnlinePlayers())
		{
			if (p.isSleeping())
			{
				sleeping_players++;
				sleeping_ticks.add(p.getSleepTicks());
			}
		}
		 
		int min_players = get_min_sleeping(onlinePlayersCount - ignoredPlayersCount);
		int min_ticks = 0;
		if (sleeping_players >= min_players) {
			Integer[] sleeping_ticks_sorted = sleeping_ticks.toArray(new Integer[0]);
			Arrays.sort(sleeping_ticks_sorted);
			min_ticks = sleeping_ticks_sorted[sleeping_players - min_players];
			sleep_data = (SLEEP_TICKS / 20 - (min_ticks + 10) / 20) + "";
		}
		else {
			sleep_data = sleeping_players + "/" + min_players;
		}
		
		int title_ticks = Math.min(INFO_TICKS * 2, SLEEP_TICKS - min_ticks + 5);
		for (Player p : pl.getServer().getOnlinePlayers())
			if (p.isSleeping())
				p.sendTitle(sleep_data, "", 0, title_ticks, 0);
	}
	
	private int get_min_sleeping(int all_players)
	{
		return (int) Math.ceil(
						Math.max(
							1,
							Math.min(
								all_players - Config.step_count,
								all_players * Config.step_percent
							)));
	}
	
	private Runnable skip_action = new Runnable() {
		@Override
		public void run() { 
			//for (Player p : pl.getServer().getOnlinePlayers())
			//	System.out.println("sleep: "+p.getName()+" ("+p.isSleeping()+" : "+p.getSleepTicks()+" ticks)");
			update_sleepers();
			try_skip();
		}
	};
	@EventHandler
	public void onBedEnter(PlayerBedEnterEvent event) {
		DelayedTask skip_task = new DelayedTask(SLEEP_TICKS + 1, skip_action);
		TaskList.add(skip_task);
	}

	@EventHandler
	public void onPortal(PlayerPortalEvent event) {
		DelayedTask skip_task = new DelayedTask(1, skip_action);
		TaskList.add(skip_task);
		//update_sleepers();
		//try_skip();
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		DelayedTask skip_task = new DelayedTask(1, skip_action);
		TaskList.add(skip_task);
	}
	
	public boolean isDeepSleeping(Player p)
	{
		return p.isSleeping() && p.getSleepTicks() >= SLEEP_TICKS;
	}
	
	public void update_sleepers()
	{
		if (Config.FunctionsON.get("extendedSleep")) {
			ignoredPlayersCount = 0;
			sleepingPlayersCount = 0;
			onlinePlayersCount = 0;
			for (Player p : pl.getServer().getOnlinePlayers())
			{
				//count
				onlinePlayersCount++;
				GameMode gm = p.getGameMode();
				if (!canSleep(p.getLocation().getBlock().getBiome()) || !(gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE))
					ignoredPlayersCount++;
				if (isDeepSleeping(p))
					sleepingPlayersCount++;
				
				//compass
				/*if (p.getBedSpawnLocation() != null)
					p.setCompassTarget(p.getBedSpawnLocation());
				else
					p.setCompassTarget(p.getWorld().getSpawnLocation());*/
			}
		}
		update_resting();
	}
	
	public void try_skip()
	{
		if (sleepingPlayersCount >= get_min_sleeping(onlinePlayersCount-ignoredPlayersCount))
		{
			if (pl.mainworld.getTime() > 12000) {
				skipNight();
			}
			else {
				if (Config.FunctionsON.get("daySleepSkip")) {
					skipDay();
				}
			}
		}
	}
	
	public boolean canSleep(Biome b) {
		return !(UtilsType.isNetherBiome(b) || UtilsType.isEndBiome(b));
	}
	
	public void skipWeather() {
		pl.mainworld.setStorm(false);
		pl.mainworld.setThundering(false);
	}
	
	public void skipNight() {
		int skipped_time = 23980 - (int)pl.mainworld.getTime();
		int time = spawnMobTime(Skip.night, skipped_time);
		if (time < skipped_time) {
			pl.mainworld.setTime((int)pl.mainworld.getTime()+time);
		}
		else {
			pl.mainworld.setTime(23980);
			skipWeather();
		}
	}
	
	public void skipDay() {
		int skipped_time = 13780 - (int)pl.mainworld.getTime();
		int time = spawnMobTime(Skip.day, skipped_time);
		if (time < skipped_time) {
			pl.mainworld.setTime((int)pl.mainworld.getTime()+time);
		}
		else {
			pl.mainworld.setTime(13780);
			skipWeather();
		}
	}
	
	private void update_resting() // anti-phantom
	{
		for (Player p : pl.getServer().getOnlinePlayers())
		{
			if (p.isSleeping())
			{
				p.setBedSpawnLocation(p.getLocation());
				p.setStatistic(Statistic.TIME_SINCE_REST, 0);
			}
		}
	}
	
	public int spawnMobTime(Skip day_time, int skipped_time) {
		int min_time = skipped_time;
		Block spawn_place = null;
		Player awakened = null;
		for (Player p : pl.getServer().getOnlinePlayers())
		{
			if (!isDeepSleeping(p))
				continue;
			
			Block bed_head = p.getLocation().getBlock();
			//get 6 blocks, priority: side blocks of head, back head, side legs, front legs; condition: Utils.playerCanStay(b)+light<=7
			//Not only Material.BEDs for sleep => 4 blocks near the head, but which priority?
			
			Block temp_place = test_blocks(bed_head, Utils.get_dir(p.getLocation()), day_time);
			if (temp_place == null) continue;
			
			int temp_time = time_spawnMob(skipped_time);
			if (temp_time < min_time) {
				min_time = temp_time;
				spawn_place = temp_place;
				awakened = p;
			}
		}
		
		if(min_time < skipped_time) {
			spawnMob(spawn_place);
			awakened.damage(0.0d);
		}
		return min_time;
	}
	
	private Block test_blocks(Block center, BlockFace dir, Skip time) {
		if (dir != BlockFace.NORTH && can_spawn_mob(center.getRelative(BlockFace.NORTH), time))
			return center.getRelative(BlockFace.NORTH);

		if (dir != BlockFace.SOUTH && can_spawn_mob(center.getRelative(BlockFace.SOUTH), time))
			return center.getRelative(BlockFace.SOUTH);

		if (dir != BlockFace.WEST && can_spawn_mob(center.getRelative(BlockFace.WEST), time))
			return center.getRelative(BlockFace.WEST);
		
		if (dir != BlockFace.EAST && can_spawn_mob(center.getRelative(BlockFace.EAST), time))
			return center.getRelative(BlockFace.EAST);
		
		return null;
	}
	
	private boolean can_spawn_mob(Block b, Skip time) {
		Biome biome = b.getBiome();
		return biome != Biome.THE_VOID && b.getBiome() != Biome.MUSHROOM_FIELD_SHORE && b.getBiome() != Biome.MUSHROOM_FIELDS
				&& UtilsType.playerCanStay(b) && (time == Skip.day ? b.getLightLevel() : b.getLightFromBlocks()) <= 7;
	}
	
	private int time_spawnMob(int time) {
		int skipped_time = random_spawn(time);
		if (skipped_time != time) {
			
			return skipped_time;
		} else return time;
	}
	
	private int random_spawn(int time) {
		double random_k = mob_spawn_random.nextDouble();
		double time_k = time/52428.8; //80 chunks * 16*16*256 / 5 mobs / 20 ticks / x free blocks / time
		if (time_k > random_k) {
			return (int) (time * random_k / time_k);
		}
		else return time;
	}
	
	public void spawnMob(Block spawn_place) {
		double random = mob_spawn_random.nextDouble();
		if (random < 0.5) {
			spawnAnyZombie(spawn_place);
		}
		else {
			spawnSkeleton(spawn_place);
		}
	}
	
	public void spawnSkeleton(Block spawn_place) {
		if (UtilsType.stray_biome(spawn_place)) {
			double random = mob_spawn_random.nextDouble();
			if(random < 0.8)
				pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.STRAY);
			else
				pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.SKELETON);
		}
		else
			pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.SKELETON);
	}

	public void spawnAnyZombie(Block spawn_place) {
		if (UtilsType.husk_biome(spawn_place)) {
			double random = mob_spawn_random.nextDouble();
			if(random < 0.8)
				pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.HUSK);
			else
				spawnZombie(spawn_place);
		}
		else
			spawnZombie(spawn_place);
	}
	
	public void spawnZombie(Block spawn_place) {
		double random = mob_spawn_random.nextDouble();
		if (random < 0.5)
			pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.ZOMBIE);
		else
			pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.ZOMBIE_VILLAGER);
	}

}
