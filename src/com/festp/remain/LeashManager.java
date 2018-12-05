package com.festp.remain;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLeash;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;

public class LeashManager {
	public static final double R = 8, R_SQUARE = R*R, R_BREAK_SQUARE = 140, PULL_MARGIN = 1, PULL_R2 = (R-PULL_MARGIN)*(R-PULL_MARGIN);
	private static final double THROW_POWER = 10;
	private List<LeashedPlayer> leashed_players = new ArrayList<>();
	private List<LeashLasso> thrown_lasso = new ArrayList<>();
	
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
	
	public void addLeashed(Entity holder, Entity leashed)
	{
		for(LeashedPlayer lp : leashed_players)
			if(lp.workaround.getLeashHolder() == holder)
				return;
		leashed_players.add(new LeashedPlayer(holder, leashed));
	}
	
	public void throwLasso(Entity holder)
	{
		thrown_lasso.add(new LeashLasso(holder, Utils.throwVector(holder.getLocation(), THROW_POWER)));
	}
	
	//invoke on LEFT_CLICK with LEAD in hand
	public void throwTargetLasso(Entity holder)
	{
		Location holder_loc = holder.getLocation();
		//raycast, search for any(not only leashable) entity or block
		Location target = null;
		thrown_lasso.add(new LeashLasso(holder, Utils.throwVector(target, THROW_POWER)));
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
