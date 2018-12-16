package com.festp.remain;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.festp.Utils;

public class LeashedPlayer {
	public static final String beacon_id = "leashing";
	private static final float pull_k = 0.02f, kxz = 0.05f, ky = 0.08f;
	LivingEntity workaround;
	Entity leashed;
	private int cooldown_new = 0;
	private static final int ticks_new = 60;
	private int cooldown_break = 0;
	private static final int ticks_break = 20;
	
	//!onGround, Ep = mgh, Ek = mv^2/2, distance = l, vx -> vertical flat, which contains both player and leashHolder
	//Ep = Gh, Ek = v^2
	private static final double G = 15, kspeed = 0.1d, E_friction = /*0.998*/1;  //2g
	private double last_fall_h = 0, E = 0;
	private Vector Vold = new Vector();
	private boolean up_dir = false;
	
	public LeashedPlayer(Entity holder, Entity leashed)
	{
		cooldown_break = ticks_break;
		workaround = (LivingEntity) Utils.spawnBeacon(leashed.getLocation(), beacon_id, Bat.class);
		this.leashed = leashed;
		workaround.setLeashHolder(holder);
	}
	
	public boolean tick()
	{
		if(cooldown_new > 0) {
			cooldown_new -= 1;
		}
		else if(cooldown_break > 0) {
			cooldown_break -= 1;
		}
		else if(workaround.isLeashed() && !workaround.isDead() && !leashed.isDead()  && workaround.getWorld() == leashed.getWorld() && workaround.getLeashHolder().getWorld() == leashed.getWorld()
				&& !(leashed instanceof Player && !((Player)leashed).isOnline() ) ) {
			/*((Bat)workaround).setAwake(true);
			Vector velocity = calc_velocity();
			leashed.setVelocity(velocity);
			workaround.setVelocity(velocity.multiply(-1));*/
			double dist2 = leashed.getLocation().distanceSquared(workaround.getLeashHolder().getLocation());
			if(dist2 > LeashManager.R_BREAK_SQUARE) {
				leashed.getWorld().dropItem(leashed.getLocation(), new ItemStack(Material.LEAD, 1));
				removeWorkaround();
				return false;
			}
			else if(dist2 > LeashManager.PULL_R2) { //R_SQUARE
				Vector velocity = calc_velocity_to_LeashHolder();
				Block b = leashed.getLocation().getBlock();
				int dx = (int)Math.signum(velocity.getX()), dz = (int)Math.signum(velocity.getZ());
				if(velocity.getY() < 0.3)
					if( b.getRelative(dx, 0, 0).getType().isSolid() || b.getRelative(0, 0, dz).getType().isSolid() || b.getRelative(dx, 0, dz).getType().isSolid() )
						velocity.add(new Vector(0, 0.3, 0));
				leashed.setVelocity(velocity);
			}
			workaround.teleport(leashed);
			/* TELEPORT TO PIG OR SOMETHING SAME
			if(dist2 > 100) {
				workaround.remove();
				return false;
			}
			else if(dist2 > 64) {
				Vector velocity = calc_velocity_to_LeashFake();
				leashed.teleport(workaround);
				leashed.setVelocity(velocity);
				//workaround.setVelocity(velocity.multiply(-1));
			}
			else {
				workaround.teleport(leashed);
			}*/
			if(leashed instanceof Player) {
				Player p = (Player)leashed;
				if(p.isSneaking()) {
					cooldown_new = ticks_new;
					removeWorkaround();
				}
			}
		}
		else {
			workaround.getWorld().dropItem(workaround.getLocation(), new ItemStack(Material.LEAD, 1));
			removeWorkaround();
			return false;
		}
		return true;
	}
	
	private Vector calc_dist() {
		Location bat = workaround.getLocation(), player = leashed.getLocation();
		return new Vector(bat.getX()-player.getX(), bat.getY()-player.getY(), bat.getZ()-player.getZ());
	}
	
	private Vector calc_velocity_to_LeashFake() {
		Location bat = workaround.getLocation(), player = leashed.getLocation();
		return new Vector( (bat.getX()-player.getX())*kxz, (bat.getY()-player.getY())*ky, (bat.getZ()-player.getZ())*kxz );
	}
	
	private Vector calc_velocity_to_LeashHolder() {
		Location loc_holder = workaround.getLeashHolder().getLocation(), loc_leashed = leashed.getLocation();
		double dx = loc_holder.getX()-loc_leashed.getX(), dy = loc_holder.getY()-loc_leashed.getY(), dz = loc_holder.getZ()-loc_leashed.getZ();
		double distance = dx*dx + dy*dy + dz*dz;
		
		Vector velocity = new Vector( dx, dy, dz); //to center

		double distance_k;
		if(distance < LeashManager.R_SQUARE) {
			distance_k = 1 - distance / LeashManager.R_SQUARE;
			velocity.multiply(new Vector(1, -1, 1));
		} else
			distance_k = distance / LeashManager.R_SQUARE - 1;
		velocity.multiply(distance_k*pull_k);
		
		Vector old_velocity = leashed.getVelocity();
		old_velocity.add(new Vector(0, -0.01, 0));
		
		System.out.printf(Utils.vectorToString(leashed.getVelocity())+" "+Utils.vectorToString(velocity));
		
		boolean codirectional = Math.signum(old_velocity.getY()) == Math.signum(velocity.getY());
		if(codirectional)
			velocity.setY(velocity.getY()*0.33).add( old_velocity.multiply(E_friction).multiply(new Vector(1, 0.66, 1)) );
		else
			velocity.add(old_velocity.multiply(E_friction));
		
		System.out.println("vec: "+Utils.vectorToString(velocity));
		
		return velocity;
	}

	public boolean isCooldownless() {
		return cooldown_new == 0 && cooldown_break == 0;
	}
	
	public void removeWorkaround() {
		if(workaround.isLeashed() && (workaround.getLeashHolder() instanceof LeashHitch || workaround.getLeashHolder() instanceof CraftLeash))
			workaround.getLeashHolder().remove();
		workaround.remove();
	}
	
}
