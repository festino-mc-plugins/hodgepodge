package com.festp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
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
}
