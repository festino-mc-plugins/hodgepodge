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
	private static final double G = 15, DIST_CLIMBING = 0.3, kspeed = 0.1d, E_friction = /*0.998*/1;  //2g
	private static final float kxz = 0.05f, ky = 0.08f;
	LivingEntity workaround;
	Entity leashed;
	private int cooldown_new = 0;
	private static final int ticks_new = 60;
	private int cooldown_break = 0;
	private static final int ticks_break = 20;
	private int cooldown_remove = 0;

	private Vector old_vel = new Vector();
	private Location old_loc;
	private double last_fall_h = 0, E = 0;
	private boolean up_dir = false;
	
	public LeashedPlayer(Entity holder, Entity leashed)
	{
		cooldown_break = ticks_break;
		workaround = (LivingEntity) Utils.spawnBeacon(leashed.getLocation(), beacon_id, Bat.class);
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
			System.out.printf("   xyz: "+Utils.toString(new Vector(leashed.getLocation().getX(), leashed.getLocation().getY(), leashed.getLocation().getZ())));
				
			if(dist2 > LeashManager.R_BREAK_SQUARE && cooldown_remove <= 0) {
				leashed.getWorld().dropItem(leashed.getLocation(), new ItemStack(Material.LEAD, 1));
				removeWorkaround();
				return false;
			}
			else if(dist2 > LeashManager.PULL_R2) { //R_SQUARE
				Vector velocity = calc_velocity_to_LeashHolder();
				Block b = leashed.getLocation().getBlock();
				int dx = (int)Math.signum(velocity.getX()), dz = (int)Math.signum(velocity.getZ());
				if(leashed.isOnGround())
					if( b.getRelative(dx, 0, 0).getType().isSolid() || b.getRelative(0, 0, dz).getType().isSolid() || b.getRelative(dx, 0, dz).getType().isSolid() )
						velocity.add(new Vector(0, 0.3, 0));
				leashed.setVelocity(velocity);
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
				if (dist2 <= LeashManager.PULL_R2)
					cooldown_remove = 0;
				cooldown_remove--;
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
		if(velocity.getY() <= 0) {
			//levitation, other lifts
			//velocity.setY(0.0d); //gravity works?
			velocity.multiply(2);
			//double move_x, move_z = velocity.getZ();
			//leashed.teleport(leashed.getLocation().add(move_x, 0, move_z));
		}
		else {
			/*if(loc_holder.distanceSquared(loc_leashed) > R_SQUARE) {
				System.out.println(loc_leashed);
				
				//XZ
				double dx = loc_holder.getX() - loc_leashed.getX(), dz = loc_holder.getZ() - loc_leashed.getZ(), length_xz_square = dx*dx + dz*dz;
				if( length_xz_square > R_SQUARE) {
					double xz_mult = Math.sqrt( R_SQUARE/ (length_xz_square+1) );
					loc_leashed.setX(loc_holder.getX()+dx*xz_mult);
					loc_leashed.setZ(loc_holder.getZ()+dz*xz_mult);
				}
				dx = loc_holder.getX() - loc_leashed.getX();
				dz = loc_holder.getZ() - loc_leashed.getZ();
				length_xz_square = dx*dx + dz*dz;
				//Y
				loc_leashed.setY(loc_holder.getY()-Math.sqrt(R_SQUARE-length_xz_square));
				leashed.teleport(loc_leashed);
				workaround.teleport(leashed);

				System.out.println(loc_leashed);
			}*/
			
			//NOT Ep = Gh, Ek = v^2
			//old V => dV => new V(rope acceleration) + dVxz; 

			double dy = loc_holder.getY() - loc_leashed.getY();
			double fall_h = LeashManager.R - dy, Ep = G*fall_h;
			double dh = fall_h - last_fall_h;

			//if zero??? division
			double dx = loc_holder.getX()-loc_leashed.getX(), dz = loc_holder.getZ()-loc_leashed.getZ(), xz_length = Math.sqrt(dx*dx + dz*dz);
			
			if(Math.abs(dh) > 1d) {
				E = Ep + leashed.getVelocity().lengthSquared();
				if(E < 1d)
					E = 0;
				up_dir = false;
				dh = 0;
			}
			//else if(dh > 0.001d) {
			else if( old_vel.getX()*dx < 0 || old_vel.getZ()*dz < 0) {
				up_dir = true;
			}
			else if(E < Ep) {
				up_dir = false;
			}
			last_fall_h = fall_h;

			double abs_kx = Math.abs(dx), abs_kz = Math.abs(dz);
			if(abs_kx > abs_kz) { 
				dz = dz / abs_kx;
				dx = Math.signum(dx);
			}
			else {
				dx = dx / abs_kz;
				dz = Math.signum(dz);
			}

			double dist_k = loc_leashed.distanceSquared(loc_holder)/LeashManager.R_SQUARE;
			double V = dist_k*Math.sqrt(Math.abs(E - Ep));//, h_R = Math.abs(fall_h) / R;//, h_R = Math.max(fall_h, 0) / R;
			//double sin_a = 1 - h_R; if(sin_a < 0) sin_a = 1; double cos_a = Math.sqrt(1 - sin_a*sin_a);
			double sin_a = dy/Math.sqrt(xz_length*xz_length + dy*dy), cos_a = Math.sqrt(1 - sin_a*sin_a);
			double Vxz = V*sin_a, Vy = -V*cos_a;

			//if(fall_h < 0.001d)
			//	Vy = -Vy;
			
			Vector Vnew = new Vector(Vxz*dx, Vy, Vxz*dz);
			
			if(up_dir)
				Vnew.multiply(-1d);
			
			dist_k = dist_k - 1;
			Vector Vcenter = new Vector(dx*cos_a, Math.abs(fall_h)*sin_a, dz*cos_a);
			Vcenter.multiply(dist_k*2);
			Vnew.add(Vcenter);

			Vnew.multiply(kspeed);
			System.out.println("v new:   "+Vnew+"   v center: "+Vcenter+"(dist_k:"+dist_k+")");
			
			Vector player_move = leashed.getVelocity().subtract(old_vel).setY(0);
			
			velocity = Vnew.add(player_move);
			
			if(E < 1d)
				velocity.setX(0).setZ(0);

			//System.out.println("E: "+E+"   Ep: "+Ep+"   h: "+fall_h+"   dh: "+dh+"   UP: "+(up_dir ? "true" : "false") );
			//System.out.println("V: "+V+" Vxz: "+Vxz+"("+kx+"; "+kz+") Vy: "+Vy);
			System.out.println("v total: "+velocity);
			
			E = E*E_friction;
		}
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
