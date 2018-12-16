package com.festp.remain;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.festp.Utils;

//spawn thrown beacon, which will die on collide(top or bottom)
//(and spawn leash hitch if collides with fence - directly or above(air, water, e.t.c); miss lead if lava; (despawn delay if cactus))
public class LeashLasso {
	public static final String BEACON_ID = "beacon_lasso";
	private static LeashManager manager = null;
	private static final Class<? extends LivingEntity> projectile_class = ArmorStand.class; //Bats are the only ones (?) who don't collide, but can't move
	private static final Class<? extends LivingEntity> beacon_class = Bat.class; //parrots can't move player
	private static final double LEAD_LOWERING = -0.8, FENCE_HALF_WIDTH = (4 / 16) / 2, EPSILON = 0.001;
	private static final int MAX_DESPAWN_DELAY = 20;
	private static final Material[] STICKY_BLOCKS = { Material.CACTUS, Material.SLIME_BLOCK };
	
	LivingEntity projectile;
	LivingEntity workaround;
	Location last_pos;
	Vector old_velocity;
	int despawn_delay = -1;
	
	public static void setLeashManager(LeashManager manager)
	{
		if(LeashLasso.manager == null)
			LeashLasso.manager = manager;
	}
	
	public LeashLasso(Entity holder, Vector velocity)
	{
		last_pos = holder.getLocation();
		old_velocity = velocity;
		Location spawn_loc = holder.getLocation().add(0, holder.getHeight() * 0.9, 0); //Eye location for any Entity, not only LivingEntity
		
		projectile = Utils.spawnBeacon(spawn_loc, BEACON_ID, projectile_class);
		projectile.setGravity(true);
		projectile.setVelocity(velocity);
		
		//because beacon is armorstand that can't draw leash
		workaround = Utils.spawnBeacon(spawn_loc, BEACON_ID, beacon_class);
		workaround.setLeashHolder(holder);

		//if top of projectile in sticky block
		if (Utils.contains(STICKY_BLOCKS, projectile.getLocation().add(0, projectile.getHeight(), 0).getBlock().getType())) {
			projectile.setGravity(false);
			projectile.setVelocity(new Vector());
			despawn_delay = MAX_DESPAWN_DELAY;
		}
	}
	
	/** @return <b>true</b> - if lasso will be alive this tick */
	public boolean tick()
	{
		if (despawn_delay >= 0) {
			if(despawn_delay > 0) {
				despawn_delay--;
				return true;
			}
			else {
				dropLead();
				despawnLasso();
				return false;
			}
		}
		workaround.teleport(projectile.getLocation().add(0, LEAD_LOWERING, 0));
		workaround.setVelocity(projectile.getVelocity());
		Location current_pos = projectile.getLocation();
		double d = current_pos.distanceSquared(last_pos);
		boolean on_ground = d*d < EPSILON;
			
		Block current = projectile.getLocation().getBlock();
		//unleash/break distance -> return lead
		if (!workaround.isLeashed()) {
			despawnLasso();
			return false;
		}
		if(projectile.getLocation().distanceSquared(workaround.getLeashHolder().getLocation()) > LeashManager.LASSO_BREAK_SQUARE) {
			dropLead();
			despawnLasso();
			return false;
		}
		//collide cactus/slime block -> return with delay
		if (getFacedBlock(current_pos, STICKY_BLOCKS) != null) {
			System.out.println(getFacedBlock(current_pos, STICKY_BLOCKS));
			projectile.setGravity(false);
			projectile.setVelocity(new Vector());
			despawn_delay = MAX_DESPAWN_DELAY;
			return true;
		}
		//collide lava/fire -> kill
		else if (current.getType() == Material.LAVA || current.getType() == Material.FIRE) {
			despawnLasso();
			return false;
		}
		//collide top/bottom -> return lead
		if(projectile.getVelocity().lengthSquared() < EPSILON) {
			Location beacon_top = projectile.getLocation().add(0, projectile.getHeight()*0.5, 0);
			Location ceiling = current.getRelative(BlockFace.UP).getLocation();
			if(ceiling.subtract(beacon_top).getY() < EPSILON)
			{
				dropLead();
				despawnLasso();
				return false;
			}
		}
		
		
		//collide with fence -> hitch
		if (on_ground && Utils.isFence(current.getRelative(BlockFace.DOWN).getType()) && Utils.isAir(current.getType())) {
			spawnLeashHitch(current.getRelative(BlockFace.DOWN));
			return false;
		}
		else if(on_ground) {
			dropLead();
			despawnLasso();
			return false;
		}
		if (Utils.isFence(current.getType())) {
			if(isFacedFence(projectile.getLocation(), projectile.getVelocity()))
			{
				spawnLeashHitch(current);
				dropLead();
				despawnLasso();
				return false;
			}
		}
		last_pos = current_pos;
		return true;
	}
	
	private void dropLead()
	{
		Item lead = projectile.getWorld().dropItem(workaround.getLeashHolder().getLocation(), new ItemStack(Material.LEAD, 1));
		lead.setPickupDelay(0);
	}
	private void despawnLasso()
	{
		workaround.remove();
		projectile.remove();
	}
	
	private void spawnLeashHitch(Block b)
	{
		Location hitch_loc = b.getLocation();
		LeashHitch hitch = hitch_loc.getWorld().spawn(hitch_loc, LeashHitch.class);
		manager.addLeashed(hitch, workaround.getLeashHolder());
		despawnLasso();
	}
	
	/** @return <b>true</b> - true if moves to X/Z center of block */
	private static boolean isToCenter(Location loc, Vector velocity)
	{
		Location center = loc.getBlock().getLocation().add(0.5, 0.5, 0.5);
		Location shift = center.subtract(loc);
		return shift.getX()*velocity.getX() >= 0 && shift.getZ()*velocity.getZ() >= 0;
	}
	private static double axis_dist(Location loc)
	{
		Location center = loc.getBlock().getLocation().add(0.5, 0.5, 0.5);
		Location shift = center.subtract(loc);
		return Math.min(Math.abs(shift.getX()), Math.abs(shift.getZ()));
	}
	private boolean isFacedFence(Location loc, Vector velocity)
	{
		return axis_dist(loc) + EPSILON <= projectile.getWidth() * 0.5 + FENCE_HALF_WIDTH /*&& isToCenter(loc, velocity)*/;
	}
	private Block getFacedBlock(Location loc, Material[] type)
	{
		double dx = loc.getX() - (int) loc.getX();
		double dy = loc.getY() - (int) loc.getZ();
		double dz = loc.getY() - (int) loc.getZ();
		double half_width = projectile.getWidth() * 0.5;
		double height = projectile.getHeight();
		Block main_block = loc.getBlock();
		//negative X
		if (-EPSILON < dx - half_width && dx - half_width < EPSILON) {
			Block faced = main_block.getRelative(-1, 0, 0);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		//positive X
		if (-EPSILON < dx + half_width - 1 && dx + half_width - 1 < EPSILON) {
			Block faced = main_block.getRelative(1, 0, 0);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		//negative Y
		if (-EPSILON < dy && dy < EPSILON) {
			Block faced = main_block.getRelative(0, -1, 0);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		//positive Y
		if (-EPSILON < dy + height - 1 && dy + height - 1 < EPSILON) {
			Block faced = main_block.getRelative(0, 1, 0);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		//negative Z
		if (-EPSILON < dz - half_width && dz - half_width < EPSILON) {
			Block faced = main_block.getRelative(0, 0, -1);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		//positive Z
		if (-EPSILON < dz + half_width - 1 && dz + half_width - 1 < EPSILON) {
			Block faced = main_block.getRelative(0, 0, 1);
			if (Utils.contains(type, faced.getType()))
				return faced;
		}
		return null;
	}
}
