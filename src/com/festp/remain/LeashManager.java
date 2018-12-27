package com.festp.remain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLeash;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.festp.Utils;

public class LeashManager {
	public static final double R = 8, R_SQUARE = R*R,
			R_BREAK_SQUARE = 140,
			LASSO_BREAK_SQUARE = 20 * 20,
			PULL_MARGIN = 0, PULL_R2 = (R-PULL_MARGIN)*(R-PULL_MARGIN);
	private static final double THROW_POWER = 6.5;
	private List<LeashedPlayer> leashed_players = new ArrayList<>();
	private List<LeashLasso> thrown_lasso = new ArrayList<>();
	private static final Predicate<Entity> entity_filter = (e) -> canLeashEntity(e);
	
	public LeashManager()
	{
		LeashLasso.setLeashManager(this);
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
	
	public LeashedPlayer addLeashed(Entity holder, Entity leashed)
	{
		for(LeashedPlayer lp : leashed_players)
			if(lp.workaround.getLeashHolder() == holder)
				return null;
		LeashedPlayer lp = new LeashedPlayer(holder, leashed);
		leashed_players.add(lp);
		return lp;
	}
	
	public LeashedPlayer addLeashed(Entity holder, Entity leashed, int remove_cooldown)
	{
		LeashedPlayer lp = addLeashed(holder, leashed);
		lp.setRemoveCooldown(remove_cooldown);
		return lp;
	}
	
	public void throwLasso(Entity holder)
	{
		thrown_lasso.add(new LeashLasso(holder, Utils.throwVector(holder.getLocation(), THROW_POWER)));
	}
	
	//invoke on LEFT_CLICK with LEAD in hand
	public void throwTargetLasso(Entity holder)
	{
		Location throw_loc = LeashLasso.getThrowLocation(holder);
		//raycast, search for any(not only leashable) entity or block
		RayTraceResult ray_result = holder.getWorld().rayTrace(throw_loc, Utils.throwVector(throw_loc, 1), LASSO_BREAK_SQUARE,
				FluidCollisionMode.ALWAYS, false, 0.1, entity_filter);
		Vector v = null;
		if (ray_result != null)
			v = ray_result.getHitPosition();
		if (v != null)
		{
			Location target = new Location(holder.getWorld(), v.getX(), v.getY(), v.getZ());
			Vector start_v = Utils.throwVector(throw_loc, target, THROW_POWER);
			if (start_v != null)
				thrown_lasso.add(new LeashLasso(holder, start_v));
			else
				throwLasso(holder);
		}
		else
			throwLasso(holder);
	}
	
	public static boolean canLeashEntity(Entity e)
	{
		EntityType et = e.getType();
		switch (et) {
		case CHICKEN:
		case COW:
		case DONKEY:
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
		   for(int i = leashed_players.size()-1; i >= 0; i--) {
			   LeashedPlayer lp = leashed_players.get(i);
			   if(lp.leashed == rightclicked || lp.workaround == rightclicked) {
				   if(lp.isCooldownless()) {
					   lp.workaround.remove();
					   clicking.getWorld().dropItem(clicking.getLocation(), new ItemStack(Material.LEAD, 1));
					   leashed_players.remove(i);
				   }
				   return true;
			   }
		   }
		   
		   if(hand != null)
		   {
			   if(hand.getType() == Material.LEAD && rightclicked instanceof Player) {
	    		   addLeashed(clicking, rightclicked);
	        	   if(clicking.getGameMode() != GameMode.CREATIVE)
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
				lp.workaround.getWorld().dropItem(p.getLocation(), new ItemStack(Material.LEAD, 1));
				leashed_players.remove(i);
			}
		}
	}
	
	public void onUnleash(EntityUnleashEvent event)
	{
		LivingEntity entity = (LivingEntity)event.getEntity();
		if( entity.isLeashed() &&
				entity.getLeashHolder() instanceof CraftLeash/*LeashHitch*/ && event.getReason() == UnleashReason.PLAYER_UNLEASH) {
			for(int i = leashed_players.size()-1; i >= 0; i--) {
				LeashedPlayer lp = leashed_players.get(i);
				if(lp.workaround == entity) {
					lp.workaround.remove();
					//lp.workaround.getLeashHolder().getWorld().dropItem(lp.workaround.getLeashHolder().getLocation(), new ItemStack(Material.LEAD, 1));
					leashed_players.remove(i);
				}
			}
		}
	}
	
}
