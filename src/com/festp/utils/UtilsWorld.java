package com.festp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class UtilsWorld
{
	public static final double THROW_POWER_K = 0.2;

	/** @return Vector in the <i>location</i> direction (uses only <i>location</i> yaw and pitch) */
	public static Vector throwVector(Location location, double throwPower) {
		throwPower = THROW_POWER_K * throwPower;
		double yaw = (location.getYaw() + 90) / 180 * Math.PI;
		double pitch = location.getPitch() / 180 * Math.PI;
		double vec_x = Math.cos(yaw) * Math.cos(pitch) * throwPower,
			vec_y = -Math.sin(pitch) * throwPower,
			vec_z = Math.sin(yaw) * Math.cos(pitch) * throwPower;
		return new Vector(vec_x, vec_y, vec_z);
	}
	
	public static Item drop(Location loc, ItemStack stack, double throwPower) {
		if (stack != null && stack.getType() != Material.AIR) {
			Item it = loc.getWorld().dropItem(loc, stack);
			it.setVelocity(throwVector(loc, throwPower));
			it.setPickupDelay(30);
			return it;
		}
		return null;
	}
}
