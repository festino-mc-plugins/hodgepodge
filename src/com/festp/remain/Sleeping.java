package com.festp.remain;

import java.util.Random;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.festp.Config;
import com.festp.mainListener;
import com.festp.utils.Utils;

public class Sleeping {
	int ticks = 20;
	private static Random mob_spawn_random = new Random();
	//private static EntityType[] spawn_types = {EntityType.ZOMBIE, EntityType.ZOMBIE_VILLAGER, EntityType.SKELETON};
	private enum Skip {day, night};
	
	private mainListener pl;
	
	public Sleeping(mainListener plugin) {
		this.pl = plugin;
	}
	
	public void onTick() {
		if(ticks > 10)
		{
			ticks = 0;
			if(Config.FunctionsON.get("extendedSleep")) {
				int ignoredPlayersCount = 0;
				int sleepingPlayersCount = 0;
				int onlinePlayersCount = 0;
				for(Player p : pl.getServer().getOnlinePlayers())
				{
					//count
					onlinePlayersCount++;
					if(canSleep(p.getLocation().getBlock().getBiome()))
						ignoredPlayersCount++;
					if(p.isSleeping())
						sleepingPlayersCount++;
					//compass
					if(p.getBedSpawnLocation() != null)
						p.setCompassTarget(p.getBedSpawnLocation());
					else
						p.setCompassTarget(p.getWorld().getSpawnLocation());
				}
				if(sleepingPlayersCount > 0 && (onlinePlayersCount-sleepingPlayersCount-ignoredPlayersCount <= Config.step1
						|| (double)sleepingPlayersCount/(onlinePlayersCount-ignoredPlayersCount) >= Config.step2))
				{
					if(pl.mainworld.getTime() > 12000){
						skipNight();
					}
					else {
						if(Config.FunctionsON.get("daySleepSkip")) {
							skipDay();
						}
					}
				}
			}
		}
		ticks++;
	}
	
	public boolean canSleep(Biome b) {
		return b == Biome.NETHER || Utils.isEndBiome(b);
	}
	
	public void skipWeather() {
		pl.mainworld.setStorm(false);
		pl.mainworld.setThundering(false);
	}
	
	public void skipNight() {
		int skipped_time = 23980 - (int)pl.mainworld.getTime();
		int time = spawnMobTime(Skip.night, skipped_time);
		if(time < skipped_time) {
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
		if(time < skipped_time) {
			pl.mainworld.setTime((int)pl.mainworld.getTime()+time);
		}
		else {
			pl.mainworld.setTime(13780);
			skipWeather();
		}
	}
	
	public int spawnMobTime(Skip day_time, int skipped_time) {
		int min_time = skipped_time;
		Block spawn_place = null;
		Player awakened = null;
		for(Player p : pl.getServer().getOnlinePlayers()) {
			Block bed_head = p.getLocation().getBlock();
			//get 6 blocks, priority: side blocks of head, back head, side legs, front legs; condition: Utils.playerCanStay(b)+light<=7
			//Not only Material.BEDs for sleep => 4 blocks near the head, but which priority?
			
			Block temp_place = test_blocks(bed_head, Utils.get_dir(p.getLocation()), day_time);
			if(temp_place == null) continue;
			
			int temp_time = time_spawnMob(skipped_time);
			if(temp_time < min_time) {
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
		if(dir != BlockFace.NORTH && can_spawn_mob(center.getRelative(BlockFace.NORTH), time))
			return center.getRelative(BlockFace.NORTH);

		if(dir != BlockFace.SOUTH && can_spawn_mob(center.getRelative(BlockFace.SOUTH), time))
			return center.getRelative(BlockFace.SOUTH);

		if(dir != BlockFace.WEST && can_spawn_mob(center.getRelative(BlockFace.WEST), time))
			return center.getRelative(BlockFace.WEST);
		
		if(dir != BlockFace.EAST && can_spawn_mob(center.getRelative(BlockFace.EAST), time))
			return center.getRelative(BlockFace.EAST);
		
		return null;
	}
	
	private boolean can_spawn_mob(Block b, Skip time) {
		return Utils.playerCanStay(b) && (time == Skip.day ? b.getLightLevel() : b.getLightFromBlocks()) <= 7;
	}
	
	private int time_spawnMob(int time) {
		int skipped_time = random_spawn(time);
		if(skipped_time != time) {
			
			return skipped_time;
		} else return time;
	}
	
	private int random_spawn(int time) {
		double random_k = mob_spawn_random.nextDouble();
		double time_k = time/52428.8; //80 chunks * 16*16*256 / 5 mobs / 20 ticks / x free blocks / time
		if(time_k > random_k) {
			return (int) (time * random_k / time_k);
		}
		else return time;
	}
	
	public void spawnMob(Block spawn_place) {
		double random = mob_spawn_random.nextDouble();
		if(random < 0.5) {
			spawnAnyZombie(spawn_place);
		}
		else {
			spawnSkeleton(spawn_place);
		}
	}
	
	public void spawnSkeleton(Block spawn_place) {
		if(Utils.stray_biome(spawn_place)) {
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
		if(Utils.husk_biome(spawn_place)) {
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
		if(random < 0.5)
			pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.ZOMBIE);
		else
			pl.mainworld.spawnEntity(spawn_place.getLocation(), EntityType.ZOMBIE_VILLAGER);
	}

}
