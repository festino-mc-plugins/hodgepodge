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
	private static final double G = 2, DIST_CLIMBING = 0.3,
			NO_TAN = -1;
	LivingEntity workaround;
	Entity leashed;
	private int cooldown_new = 0;
	private static final int ticks_new = 60;
	private int cooldown_break = 0;
	private static final int ticks_break = 20;
	private int cooldown_remove = 0;

	private Location old_loc;
	private Vector old_velocity = new Vector();
	private double old_tan = NO_TAN;
	
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
			if (dist2 > LeashManager.PULL_R2 && leashed instanceof LivingEntity)
				Utils.noGravityTemp((LivingEntity)leashed, 50);
			else
				Utils.noGravityTemp((LivingEntity)leashed, 0);
				
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
			else
				old_tan = NO_TAN;
			
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
		old_velocity = leashed.getVelocity();
		workaround.setVelocity(old_velocity); //more actual leash render
		return true;
	}
	
	public void setRemoveCooldown(int ticks)
	{
		cooldown_remove = ticks;
	}
	
	private Vector calc_velocity_to_LeashHolder() {
		//get leash constraints <- offset from attachment point
		Location loc_holder = workaround.getLeashHolder().getLocation(), loc_leashed = leashed.getLocation();
		double x0 = loc_holder.getX(), y0 = loc_holder.getY(), z0 = loc_holder.getZ();
		double x1 = loc_leashed.getX(), y1 = loc_leashed.getY(), z1 = loc_leashed.getZ();
		double dx = x0 - x1, dy = y0 - y1, dz = z0 - z1;
		
		//tangential velocity
		double dh = loc_leashed.getY() - old_loc.getY();
		double sign = Math.signum(dy) * ((old_velocity.angle(new Vector(dx, 0, dz)) > Math.PI / 2) ? 1 : -1);
		Vector tan_velocity = new Vector(sign*dx, -(dx*dx + dz*dz) / dy, sign*dz);
		if (old_tan == NO_TAN)
			old_tan = old_velocity.length() * Math.cos(tan_velocity.angle(old_velocity));
		double tan = Math.sqrt( Math.abs(old_tan*old_tan - 2*G*dh) ); // V2 = sqrt(V1*V1 - 2*g*dh) - law of energy conservation
		
		//centripetal velocity
		Vector cen_velocity = new Vector(dx, dy, dz);
		double cen = 0;
		
		//get player influence <- head angles(pitch): stop swinging(15 degrees from the bottom), climb up(15 degrees from the top), alter the direction(any other angles)
		float from_top = 15, from_bottom = 15;
		Location cam = leashed.getLocation();
		float pitch = cam.getPitch();
		if (pitch >= 90 - from_bottom)
			tan = tan * Utils.DECELERATION_H;
		else if (pitch <= -90 + from_top)
		{
			//under attachment point
			if (dx*dx + dz*dz < DIST_CLIMBING)
				cen = 0.1; //2 blocks per second
			tan = tan * Utils.DECELERATION_H;
		}
		else
		{
			//increase tan(angle-depending), consider perpendicular to the vertical plane
		}
		
		
		//get environmental influence <- bubbles, cobweb, slime blocks
		//but also catch player movement, that should be filtered(but it ISN'T now)
		//alter tan, consider perpendicular to the vertical plane
		Vector expected_v = Utils.nextTickVelocity(old_velocity);
		Vector actual_v = leashed.getVelocity();
		Vector difference = actual_v.subtract(expected_v);

		tan_velocity.normalize();
		tan_velocity = tan_velocity.multiply(tan);
		
		//final calculations
		double angle_v = sign * tan / (2 * Math.PI * LeashManager.R);
		double alpha = 0; //alpha is the angle between Oxy and plane of swinging
		if (dx*dz != 0)
			alpha = (new Vector(dx, 0, dz)).angle(new Vector (1, 0 , 0));
		if (dz < 0)
			alpha = 2*Math.PI - alpha;

		double start_angle = (new Vector(dx, -dy, dz)).angle(new Vector(0, 1, 0));
		double xz = -LeashManager.R * Math.sin(angle_v + start_angle);
		double y = y0 + LeashManager.R * Math.cos(angle_v + start_angle);
		
		double x = x0 + xz * Math.cos(alpha);
		double z = z0 + xz * Math.sin(alpha);
		Vector new_velocity = new Vector(x - x1, y - y1, z - z1); //tan component, circle motion
		new_velocity = new_velocity.add(cen_velocity.multiply(cen));
		//circle rotation around Oy

		System.out.printf("old_tan: "+old_tan+", new_tan: "+tan+", vec: "+Utils.toString(old_velocity)+" -> "+Utils.toString(new_velocity)+" ("+(int)sign+")");
		System.out.printf(Utils.toString(new Vector(x, y, z)) +" - "+Utils.toString(new Vector(x1, y1, z1)));
		System.out.println("alpha: "+alpha+", start: "+start_angle+", sin new: "+Math.sin(angle_v + start_angle)+", sin/cos alpha: "+Math.sin(alpha)+"/"+Math.cos(alpha));
		old_tan = tan;
		
		return new_velocity;
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
