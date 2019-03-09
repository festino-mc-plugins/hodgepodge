package com.festp.remain;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLeash;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import com.festp.utils.Utils;

public class LeashedPlayer {
	public static final Class<? extends LivingEntity> BEACON_CLASS = Vex.class;
	public static final String BEACON_ID = "leashed_entity";
	private static final double DIST_CLIMBING = 0.3, player_pull_v = 0.3;
	private static final float from_top = 5, from_bottom = 5;
	private static final float kxz = 0.05f, ky = 0.05f;
	LivingEntity workaround;
	Entity leashed;
	private double cur_R = LeashManager.MAX_R, pull_R, pull_R_square;
	private int cooldown_new = 0;
	private static final int ticks_new = 60;
	private int cooldown_break = 0;
	private static final int ticks_break = 20;
	private int cooldown_remove = 0;
	private boolean controlled = false; //if controlled or hanging under holder

	private Vector old_vel = new Vector();
	private Location old_loc;
	
	public LeashedPlayer(Entity holder, Entity leashed)
	{
		recalc_pull();
		cooldown_break = ticks_break;
		workaround = (LivingEntity) Utils.spawnBeacon(leashed.getLocation(), BEACON_CLASS, BEACON_ID, false);
		this.leashed = leashed;
		workaround.setLeashHolder(holder);
		old_loc = leashed.getLocation();
	}
	
	public boolean tick()
	{
		if(cooldown_new > 0) {
			cooldown_new--;
		}
		else if(cooldown_break > 0) {
			cooldown_break--;
		}
		else if(workaround.isLeashed() && !workaround.isDead() && !leashed.isDead()  && workaround.getWorld() == leashed.getWorld() && workaround.getLeashHolder().getWorld() == leashed.getWorld()
				&& !(leashed instanceof Player && !((Player)leashed).isOnline() ) ) {
			double dist2 = leashed.getLocation().distanceSquared(workaround.getLeashHolder().getLocation());
				
			if(dist2 > LeashManager.R_BREAK_SQUARE && cooldown_remove <= 0)
			{
				leashed.getWorld().dropItem(leashed.getLocation(), new ItemStack(Material.LEAD, 1));
				removeWorkaround();
				return false;
			}
			else
			{
				Vector v = leashed.getVelocity();
				
				double vert = v.getY();
				if (Math.abs(vert - 2.6) < 0.4) // 2.5 in balance, 2.7 in average, 2.9 a maximum, but 2.27 also real
					v.setY(0);
				if (Math.abs(vert - 11.2) < 1) // ??? fix
					v.setY(0);
					
				if (dist2 > pull_R_square)
				{
					v = calc_velocity_to_LeashHolder();
				}
				
				//System.out.printf("   v 1: "+Utils.toString(leashed.getVelocity())+"   " + leashed.getVelocity().getY());
				Vector total_v = player_pull(v);
				if (!leashed.getVelocity().equals(total_v))
					leashed.setVelocity(total_v);
				//System.out.printf("   v 2: "+Utils.toString(leashed.getVelocity()));

				if (leashed instanceof LivingEntity)
					if (isUnderHolder()
							&& (controlled || dist2 > pull_R_square) )
						setGravity(false);
					else
						setGravity(true);
			}
			
			workaround.teleport(leashed);
			if(leashed instanceof Player) {
				Player p = (Player)leashed;
				if(p.isSneaking()) {
					cooldown_new = ticks_new;
					removeWorkaround();
				}
			}
			
			if (cooldown_remove > 0)
			{
				if (dist2 <= pull_R_square)
					cooldown_remove = 0;
				cooldown_remove--;
				// transition to vanilla lead
				if (cooldown_remove <= 0 && leashed instanceof LivingEntity && LeashManager.canLeashEntity(leashed)) 
				{
					((LivingEntity)leashed).setLeashHolder(workaround.getLeashHolder());
					removeWorkaround();
				}
			}
		}
		else {
			workaround.getWorld().dropItem(workaround.getLocation(), new ItemStack(Material.LEAD, 1));
			removeWorkaround();
			return false;
		}
		
		old_loc = leashed.getLocation();
		old_vel = leashed.getVelocity();
		workaround.setVelocity(old_vel); //more actual leash render
		return true;
	}
	
	public void setRemoveCooldown(int ticks)
	{
		cooldown_remove = ticks;
	}
	
	private Vector calc_velocity_to_LeashHolder() {
		Location loc_holder = workaround.getLeashHolder().getLocation(), loc_leashed = leashed.getLocation();
		Vector velocity = new Vector( (loc_holder.getX()-loc_leashed.getX())*kxz,
									  (loc_holder.getY()-loc_leashed.getY())*ky,
									  (loc_holder.getZ()-loc_leashed.getZ())*kxz );

		RayTraceResult ray_res = leashed.getWorld().rayTraceBlocks(loc_leashed, velocity,
				leashed.getWidth()/2 + Utils.EPSILON, FluidCollisionMode.NEVER);
		if (ray_res != null && ray_res.getHitBlock() != null)
			if(leashed.isOnGround() || velocity.getY() < 0.3)
				velocity.add(new Vector(0, 0.3, 0));
		
		return velocity;
	}

	//get player influence if player is under or is above leash holder
	//head angles(pitch): stop swinging(15 degrees from the bottom), climb up(15 degrees from the top)
	private Vector player_pull(Vector v)
	{
		if (leashed instanceof Player) //can't test sprint because it fires only on move
		{
			Location cam = leashed.getLocation();
			float pitch = cam.getPitch();
			double length_change;
			if (pitch >= 90 - from_bottom)
			{
				length_change = Math.max(0, LeashManager.MAX_R - cur_R);
				length_change = Math.min(player_pull_v, length_change);
				cur_R += length_change;
				recalc_pull();
				double min_down = Math.max(v.getY() - length_change, -length_change); //limited min velocity
				double total_down = Math.min(v.getY(), min_down); //if current velocity lower than player can
				v.setY(total_down);
				controlled = true;
			}
			else if (pitch <= -90 + from_top)
			{
				if (isUnderHolder())
				{
					length_change = Math.max(0, cur_R - leashed.getHeight() - 0.5);
					length_change = Math.min(player_pull_v, length_change);
					cur_R -= length_change;
					recalc_pull();
					double max_up = Math.min(v.getY() + length_change, length_change); //limited max velocity
					double total_up = Math.max(v.getY(), max_up); //if current velocity higher than player can
					v.setY(total_up);
					controlled = true;
				}
			}
		}
		
		return v;
	}
	private void recalc_pull() {
		pull_R = cur_R - LeashManager.PULL_MARGIN;
		pull_R_square = pull_R*pull_R;
	}
	
	private boolean isUnderHolder() {
		Location dxz = leashed.getLocation().subtract(workaround.getLeashHolder().getLocation());
		dxz.setY(0);
		return dxz.lengthSquared() < DIST_CLIMBING*DIST_CLIMBING;
	}
	
	public void setGravity(boolean g)
	{
		if (leashed instanceof LivingEntity)
			if (g)
				Utils.noGravityTemp((LivingEntity)leashed, 0);
			else
				Utils.noGravityTemp((LivingEntity)leashed, 50);
	}

	public boolean isCooldownless() {
		return cooldown_new == 0 && cooldown_break == 0;
	}
	
	public void removeWorkaround() {
		setGravity(true);
		if(workaround.isLeashed() && (workaround.getLeashHolder() instanceof LeashHitch || workaround.getLeashHolder() instanceof CraftLeash))
			workaround.getLeashHolder().remove();
		workaround.remove();
	}
	
}
