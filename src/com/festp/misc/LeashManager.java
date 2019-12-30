package com.festp.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLeash;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.festp.utils.Utils;
import com.festp.utils.UtilsWorld;

import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityLeash;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.WorldServer;

public class LeashManager {
	JavaPlugin plugin;
	
	public static final String LENGTH_KEY = "lead_length";
	public static final double DEFAULT_R = 8, DEFAULT_R_SQUARE = DEFAULT_R*DEFAULT_R,
			BREAK_AREA = 4,
			LASSO_BREAK_SQUARE = 20 * 20,
			PULL_MARGIN = 0;
	private static final double THROW_POWER = 6.5;
	private List<LeashedPlayer> leashed_players = new ArrayList<>();
	private List<LeashLasso> thrown_lasso = new ArrayList<>();
	private static final Predicate<Entity> entity_filter = (e) -> canLeashEntity(e);
	
	public LeashManager(JavaPlugin plugin)
	{
		this.plugin = plugin;
		LeashLasso.setLeashManager(this);
		onEnable();
	}
	
	public void onEnable() { }
	
	public void onDisable()
	{
		for(int i = 0; i < leashed_players.size(); i++) {
			LeashedPlayer lp = leashed_players.get(i);
			lp.removeWorkaround();
		}
		for(int i = 0; i < thrown_lasso.size(); i++) {
			LeashLasso lasso = thrown_lasso.get(i);
			lasso.dropLead();
			lasso.despawnLasso();
		}
	}
	
	public void tick()
	{
		for(int i = leashed_players.size()-1; i >= 0; i--) {
			LeashedPlayer lp = leashed_players.get(i);
			if(!lp.tick()) {
				leashed_players.remove(i);
			}
		}
		for(int i = thrown_lasso.size()-1; i >= 0; i--) {
			LeashLasso lp = thrown_lasso.get(i);
			if(!lp.tick()) {
				thrown_lasso.remove(i);
			}
		}
	}
	
	public LeashedPlayer addLeashed(Entity holder, Entity leashed, ItemStack drops)
	{
		for(LeashedPlayer lp : leashed_players)
			if(lp.workaround.getLeashHolder() == holder)
				return null;
		LeashedPlayer lp = new LeashedPlayer(holder, leashed, drops);
		leashed_players.add(lp);
		return lp;
	}
	
	public LeashedPlayer addLeashed(Entity holder, Entity leashed, ItemStack drops, int remove_cooldown)
	{
		LeashedPlayer lp = addLeashed(holder, leashed, drops);
		lp.setRemoveCooldown(remove_cooldown);
		return lp;
	}
	
	public void throwLasso(Entity holder, ItemStack lead_drops)
	{
		thrown_lasso.add(new LeashLasso(holder, UtilsWorld.throwVector(holder.getLocation(), THROW_POWER), lead_drops));
	}
	
	//invoke on LEFT_CLICK with LEAD in hand
	public void throwTargetLasso(Entity holder, ItemStack lead_drops)
	{
		Location throw_loc = LeashLasso.getThrowLocation(holder);
		//raycast, search for any(not only leashable) entity or block
		RayTraceResult ray_result = holder.getWorld().rayTrace(throw_loc, UtilsWorld.throwVector(throw_loc, 1), LASSO_BREAK_SQUARE,
				FluidCollisionMode.ALWAYS, false, 0.1, entity_filter);
		Vector v = null;
		if (ray_result != null)
			v = ray_result.getHitPosition();
		if (v != null)
		{
			Location target = new Location(holder.getWorld(), v.getX(), v.getY(), v.getZ());
			Vector start_v = UtilsWorld.throwVector(throw_loc, target, THROW_POWER);
			if (start_v != null)
				thrown_lasso.add(new LeashLasso(holder, start_v, lead_drops));
			else
				throwLasso(holder, lead_drops);
		}
		else
			throwLasso(holder, lead_drops);
	}
	
	public static boolean canLeashEntity(Entity e)
	{
		EntityType et = e.getType();
		switch (et) {
		case CAT:
		case CHICKEN:
		case COW:
		case DONKEY:
		case FOX:
		case HORSE:
		case LLAMA:
		case MULE:
		case OCELOT:
		case PIG:
		case RABBIT:
		case SHEEP:
		case WOLF:
			return true;
		default:
			return false;
		}
	}
	
	public boolean click(Entity rightclicked, Player clicking, ItemStack hand)
	{
		for (int i = leashed_players.size()-1; i >= 0; i--) {
			LeashedPlayer lp = leashed_players.get(i);
			if (lp.leashed == rightclicked || lp.workaround == rightclicked) {
				if (lp.isCooldownless()) {
					lp.workaround.remove();
					Utils.giveOrDrop(clicking, new ItemStack(Material.LEAD, 1));
					leashed_players.remove(i);
				}
				return true;
			}
		}
		
		if (hand != null)
		{
			if (hand.getType() == Material.LEAD && rightclicked instanceof Player) {
				ItemStack drop = hand.clone();
				drop.setAmount(1);
	    		addLeashed(clicking, rightclicked, drop);
	        	if (clicking.getGameMode() != GameMode.CREATIVE)
	        		hand.setAmount(hand.getAmount()-1);
	        	return true;
	    	}
		}
		return false;
	}
	
	public boolean isWorkaroundActive(Entity e)
	{
		boolean found = false;
		for(LeashedPlayer lp : leashed_players) {
			if(lp.workaround == e) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	public void removeByLeashHolder(Player p)
	{
		for(int i = leashed_players.size()-1; i >= 0; i--) {
			LeashedPlayer lp = leashed_players.get(i);
			if(lp.workaround.getLeashHolder() == p) {
				lp.workaround.remove();
				Utils.giveOrDrop(lp.leashed, new ItemStack(Material.LEAD, 1));
				leashed_players.remove(i);
			}
		}
	}
	
	public void onUnleash(PlayerUnleashEntityEvent event)
	{
		LivingEntity entity = (LivingEntity)event.getEntity();
		//unleash without lead drop
		if( entity.isLeashed() && entity.getLeashHolder() instanceof CraftLeash/*LeashHitch*/) {
			for(int i = leashed_players.size()-1; i >= 0; i--) {
				LeashedPlayer lp = leashed_players.get(i);
				if(lp.workaround == entity) {
					lp.removeWorkaround();
					leashed_players.remove(i);
				}
			}
		}
		//cancel lasso unleash
		if (Utils.hasBeaconData(entity, LeashLasso.BEACON_ID))
			event.setCancelled(true);
	}
	
	public static double getLeadLength(ItemStack lead)
	{
		if (lead == null || lead.getType() != Material.LEAD)
			return 0;
		
		String len = Utils.getString(lead, LENGTH_KEY);
		if (len == null)
			return DEFAULT_R;
		
		try {
			return Double.parseDouble(len);
		} catch (Exception e) {
			return DEFAULT_R;
		}
	}
	
	public static LeashHitch spawnHitch(Location hitch_loc)
	{
		hitch_loc = hitch_loc.getBlock().getLocation();
		
		// neat way => "Unable to get CCW facing of up/down"
		//System.out.println(hitch_loc);
		//LeashHitch hitch = (LeashHitch) hitch_loc.getWorld().spawnEntity(hitch_loc, EntityType.LEASH_HITCH);//.spawn(hitch_loc, LeashHitch.class);

		/*
		// bugfix - edited code from: https://www.spigotmc.org/threads/1-13-2-exception-when-spawnentity-of-entitytype-leash_hitch.393082/
		// save fence type
		Material m_fence = hitch_loc.getBlock().getType();
		// select blocks, save it and set to air
		// block below
		hitch_loc.add(0, -1, 0);
		BlockData data_down = hitch_loc.getBlock().getBlockData();
		hitch_loc.getBlock().setType(Material.AIR);
		// block above
		hitch_loc.add(0, 2, 0);
		BlockData data_top = hitch_loc.getBlock().getBlockData();
		hitch_loc.getBlock().setType(Material.AIR);

		// select the fence block and update
		hitch_loc.add(0, -1, 0);
		hitch_loc.getBlock().setType(Material.AIR);
		hitch_loc.getBlock().setType(m_fence);
		
		LeashHitch hitch = hitch_loc.getWorld().spawn(hitch_loc, LeashHitch.class);

		// select blocks and back up data
		// block below
		hitch_loc.add(0, -1, 0);
		hitch_loc.getBlock().setBlockData(data_down);
		// block above
		hitch_loc.add(0, 2, 0);
		hitch_loc.getBlock().setBlockData(data_top);
		
		// bugs: chest below or above will drop its items
		*/
		
		// workaround from
		// https://hub.spigotmc.org/jira/browse/SPIGOT-4674?focusedCommentId=32808&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-32808
		WorldServer world = ((CraftWorld) hitch_loc.getWorld()).getHandle();

		EntityLeash nmsLeashHitch = new EntityLeash(world,
				new BlockPosition(hitch_loc.getX(), hitch_loc.getY(), hitch_loc.getZ()));
		world.addEntity(nmsLeashHitch);
		LeashHitch hitch = (LeashHitch) nmsLeashHitch.getBukkitEntity();
		
		return hitch;
	}
}
