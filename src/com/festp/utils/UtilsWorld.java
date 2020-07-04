package com.festp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class UtilsWorld {
	//we have some Minecraft magic constants
	public static final double THROW_POWER_K = 0.2,
		DECELERATION_H = 0.91, DECELERATION_V = 0.98, ACCELERATION_V = -0.08;

	/** @return Vector in the <i>location</i> direction(using its yaw and pitch)*/
	public static Vector throwVector(Location location, double throw_power) {
		throw_power = THROW_POWER_K * throw_power;
		double yaw = ( location.getYaw() + 90 ) /180*Math.PI,
		pitch = ( location.getPitch() ) /180*Math.PI;
		double vec_x = Math.cos(yaw)*Math.cos(pitch)*throw_power,
			vec_y = -Math.sin(pitch)*throw_power,
			vec_z = Math.sin(yaw)*Math.cos(pitch)*throw_power;
		return new Vector(vec_x,vec_y,vec_z);
	}
	
	public static Vector nextTickVelocity(Vector v)
	{
		Vector new_v = new Vector();
		new_v.setX(DECELERATION_H * v.getX());
		new_v.setY(DECELERATION_V * (v.getY() - ACCELERATION_V));
		new_v.setZ(DECELERATION_H * v.getZ());
		return new_v;
	}

	/** Minecraft parabola path calculating. Doesn't consider obstacles. (as if the world is empty)
	 * Choose the way that is shorter and faster, with smaller angle (instead of mortar shot)
	 * @return <b>null</b> - if projectile can't reach target location. */
	public static Vector throwVector(Location from, Location to, double power)
	{
		return throwVector(from, to, power, true);
	}
	/** Minecraft parabola path calculating. Doesn't consider obstacles. (as if the world is empty)
	 * @return <b>null</b> - if projectile can't reach target location. */
	public static Vector throwVector(Location from, Location to, double power, boolean choose_shorter)
    {
		power = THROW_POWER_K * power;
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double dh = Math.sqrt(dx * dx + dz * dz);
        
        if (dh < Utils.EPSILON)
            return new Vector(0, power, 0);
        
        // lets solve in terms of h and y - approximately find angle of max dy and root with smaller angle
        double v0 = power;
        double min_vh = dh * (1 - DECELERATION_H);
        if (v0 < min_vh + Utils.EPSILON)
        	return null;

        int precision = 20;
        double min_angle = -Math.acos(min_vh / v0),
        		max_angle = dy_extremum_point(dh, v0, precision, -min_angle),
        		mid_angle;
        double max_dy = dy_by_angle(dh, v0, max_angle);
        if (max_dy < dy + Utils.EPSILON)
        	return null;
        
        if (!choose_shorter) {
        	double temp = max_angle;
        	max_angle = -min_angle;
        	min_angle = temp;
        }
        
        double vh = v0, vy = 0, dy_calc;
        for (int i = 0; i < precision; i++)
        {
        	mid_angle = (min_angle + max_angle) / 2;
        	vh = v0 * Math.cos(mid_angle);
        	vy = v0 * Math.sin(mid_angle);
        	dy_calc = dy(dh, vh, vy); // function increases from left to right point
        	if (dy_calc < dy)
        		min_angle = mid_angle;
        	else
        		max_angle = mid_angle;
        }
        
        // return to x and z
        double vx = vh * dx / dh;
        double vz = vh * dz / dh;
 
        return new Vector(vx, vy, vz);
    }
	private static double dy_n(double v0_y, double n)
    {
		double pow = Math.pow(DECELERATION_V, n), k = (1 - pow) / (1 - DECELERATION_V);
    	return k * v0_y + ACCELERATION_V * DECELERATION_V / (1 - DECELERATION_V) * (n - k);
    }
	private static double n(double dh, double vh_0)
    {
		double t = 1 - (1 - DECELERATION_H) * dh / vh_0;
    	return Math.log(t) / Math.log(DECELERATION_H);
    }
	private static double dy(double dh, double v0_h, double v0_y) {
    	return dy_n(v0_y, n(dh, v0_h));
    }
	private static double dy_by_angle(double dh, double v0, double angle) {
    	return dy_n(v0 * Math.sin(angle), n(dh, v0 * Math.cos(angle)));
    }
	/**@return angle of max dy*/
	private static double dy_extremum_point(double dh, double v0, int iterations, double start_angle)
    {
		double min_angle = -start_angle, max_angle = start_angle, mid_angle;
		Vector v = new Vector();
		for (int i = 0; i < iterations; i++)
		{
        	mid_angle = (min_angle + max_angle) / 2;
        	if (dy_by_angle(dh, v0, mid_angle) < dy_by_angle(dh, v0, mid_angle + Utils.EPSILON))
        		min_angle = mid_angle;
        	else
        		max_angle = mid_angle;
		}
    	return (min_angle + max_angle) / 2;
    }
	
	public static Item drop(Location loc, ItemStack stack, double throw_power) {
		if(stack != null && stack.getType() != Material.AIR) {
			Item it = loc.getWorld().dropItem(loc, stack);
			it.setVelocity(throwVector(loc, throw_power));
			it.setPickupDelay(30);
			return it;
		}
		return null;
	}
	


	public static Location searchBlock(Material[] blocks, Location loc, double hor_radius, boolean player_can_stay) {
		boolean x_priority = false, y_priority = false, z_priority = false;
		if(loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = true;
		if(loc.getY() - Math.floor(loc.getY()) > 0.5) y_priority = true;
		if(loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = true;
		boolean x_priorier_z = true;
		if(Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block start_block = loc.getBlock();
		Block found_block = null;
		boolean player_cant_stay = !player_can_stay;
		searching :
		{
			for (int r = 0; r <= 1.1*hor_radius; r++) {
				for (int dy = 0; dy <= r/2; dy++) {
					int temp = r-dy;
					for (int d = 0; d <= temp; d++) {
						int[] dx_pool = (x_priority ? new int[]{d, -d} : new int[]{-d, d}),
							  dz_pool = (z_priority ? new int[]{r-d, d-r} : new int[]{d-r, r-d});
						if (x_priorier_z)
							for (int dx : dx_pool)
								for (int dz : dz_pool) {
									found_block = start_block.getRelative(dx, dy, dz); //low dependency on priority
									if (Utils.contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if (Utils.contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
								}
						else
							for (int dz : dz_pool)
								for (int dx : dx_pool) {
									found_block = start_block.getRelative(dx, dy, dz); //low dependency on priority
									if (Utils.contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if (Utils.contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
								}
					}
				}
			}
			return null;
		}
		return found_block.getLocation().add(0.5, 0.5, 0.5);
	}
	
	public static Location search33space(Material[] blocks, Location loc)
	{
		boolean x_priority = false, z_priority = false;
		if (loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = true;
		if (loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = true;
		double x = loc.getX();
		double dx = x_priority ? 1 : -1;
		double dz = z_priority ? 1 : -1;
		loc.setX(Math.floor(loc.getX()) + 0.5);
		loc.setZ(Math.floor(loc.getZ()) + 0.5);
		if (Utils.contains(blocks, loc.getBlock().getType()))
			if (UtilsWorld.check33space(loc.getBlock()))
				return loc.clone().add(0, 1, 0);
		
		loc.add(dx, 0, 0);
		if (Utils.contains(blocks, loc.getBlock().getType()))
			if (UtilsWorld.check33space(loc.getBlock()))
				return loc.clone().add(0, 1, 0);
		
		loc.setX(x);
		loc.add(0, 0, dz);
		if (Utils.contains(blocks, loc.getBlock().getType()))
			if (UtilsWorld.check33space(loc.getBlock()))
				return loc.clone().add(0, 1, 0);
		
		loc.add(dx, 0, 0);
		if (Utils.contains(blocks, loc.getBlock().getType()))
			if (UtilsWorld.check33space(loc.getBlock()))
				return loc.clone().add(0, 1, 0);
		return null;
	}
	
	public static boolean check33space(Block block)
	{
		boolean empty = true;
		for (int dx = -1; dx <= 1; dx++)
			for (int dz = -1; dz <= 1; dz++)
				if (!UtilsType.playerCanFlyOn(block.getRelative(dx, 0, dz))) {
					empty = false;
					break;
				}
		return empty;
	}

	public static Location searchBlock22Platform(Material[] blocks, Location loc, double hor_radius, boolean full) {
		boolean x_priority = false, y_priority = false, z_priority = false;
		if (loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = true;
		if (loc.getY() - Math.floor(loc.getY()) > 0.5) y_priority = true;
		if (loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = true;
		boolean x_priorier_z = true;
		if (Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block start_block = loc.getBlock();
		Block found_block = null;
		searching :
		{
			for (int r = 0; r <= 1.1*hor_radius; r++) {
				for (int dy = 0; dy <= r/2; dy++) {
					int temp = r-dy;
					for (int d = -temp; d <= temp; d++) {
						int[] dx_pool = (x_priority ? new int[]{d, -d} : new int[]{-d, d}),
							  dz_pool = (z_priority ? new int[]{d, -d} : new int[]{-d, d});
						if (x_priorier_z)
							for (int dz : dz_pool)
								for (int dx : dx_pool) {
									found_block = start_block.getRelative(dx, dy, r-Math.abs(dz));
									if (is22Platform(blocks, found_block, x_priority, z_priority, full))
										break searching;
									found_block = start_block.getRelative(dx, dy, dz);
									if (is22Platform(blocks, found_block, x_priority, z_priority, full))
										break searching;
								}
						else
							for (int dx : dx_pool)
								for (int dz : dz_pool) {
									found_block = start_block.getRelative(dx, dy, r-Math.abs(dz));
									if (is22Platform(blocks, found_block, x_priority, z_priority, full))
										break searching;
									found_block = start_block.getRelative(dx, dy, dz);
									if (is22Platform(blocks, found_block, x_priority, z_priority, full))
										break searching;
								}
					}
				}
			}
			return null;
		}
		return found_block.getLocation().add(
				x_priority ? 1 : 0,
				1,
				z_priority ? 1 : 0);
	}
	
	public static boolean is22Platform(Material[] valid_materials, Block start_block, boolean positive_x, boolean positive_z, boolean full)
	{
		int dx = positive_x ? 1 : -1;
		int dz = positive_z ? 1 : -1;
		int count_blocks = 0, count_flyable = 0;
		if (UtilsType.playerCanFlyOn(start_block)) {
			count_flyable++;
			if (Utils.contains(valid_materials, start_block.getType()))
				count_blocks++;
		}
		Block block = start_block.getRelative(0, 0, dz);
		if (UtilsType.playerCanFlyOn(block)) {
			count_flyable++;
			if (Utils.contains(valid_materials, block.getType()))
				count_blocks++;
		}
		block = start_block.getRelative(dx, 0, 0);
		if (UtilsType.playerCanFlyOn(block)) {
			count_flyable++;
			if (Utils.contains(valid_materials, block.getType()))
				count_blocks++;
		}
		block = start_block.getRelative(dx, 0, dz);
		if (UtilsType.playerCanFlyOn(block)) {
			count_flyable++;
			if (Utils.contains(valid_materials, block.getType()))
				count_blocks++;
		}
		if (count_flyable < 2 * 2)
			return false;
		if (count_blocks == 2 * 2)
			return true;
		if (!full && count_blocks >= 1)
			return true;
		return false;
	}
	
	public static Location find_horse_space(Location loc) {
		Block start_block = loc.add(0, -1, 0).getBlock();
		loc.add(0, 1, 0);
		if ( !UtilsType.playerCanFlyOn(start_block) ) return null;
		int x_priority = -1, z_priority = -3;
		if (loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = 1;
		if (loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = 3;
		boolean x_priorier_z = true;
		if (Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block found_block = null;
		int[] grid = new int[9];
		Block b;
		for (int i = 0; i < 9; i++) {
			b = start_block.getRelative(i%3-1, 0, i/3-1);
			if (UtilsType.playerCanStay(b.getRelative(0, 1, 0))) 
				grid[i] = 2;
			else if ( UtilsType.playerCanFlyOn(b) ) 
				grid[i] = 1;
			else
				grid[i] = 0;
		}
		int[] cells = {4, 4+x_priority, 4+z_priority, 4+x_priority+z_priority};
		for (int i = 0; i < 4; i++) {
			if (grid[cells[0]] == 2 || grid[cells[1]] == 2 || grid[cells[2]] == 2 || grid[cells[3]] == 2)
				if (grid[cells[0]] > 0 && grid[cells[1]] > 0 && grid[cells[2]] > 0 && grid[cells[3]] > 0)
					return start_block.getLocation().add(((cells[3]+3)%3-1) < 0 ? 0 : 1, 1, cells[3]-4 < 0 ? 0 : 1);
			
			if (i == 0)
				if (x_priorier_z) cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
				else cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
			if (i == 1)
				if (x_priorier_z) cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
				else cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
			if (i == 2)cells = new int[]{4, 4-x_priority, 4-z_priority, 4-x_priority-z_priority};
		}
		return null;
	}
	
	/** @return 3 if full, 0 if empty or invalid bd*/
	public static int getCauldronLevel(BlockData bd) {
		if (bd == null || bd.getMaterial() != Material.CAULDRON) {
			return 0;
		}
		Levelled cauldron = (Levelled) bd;
		return cauldron.getLevel();
	}
}
