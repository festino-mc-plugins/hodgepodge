package com.festp.utils;

import java.util.Collection;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("deprecation")
public class DirtyUtils {

	public static Collection<PotionEffect> getPotionEffects(PotionMeta pm)
	{
		// new CraftPotionBrewer() needs bukkit packages
		return Potion.getBrewer().getEffects(pm.getBasePotionData().getType(), pm.getBasePotionData().isUpgraded(), pm.getBasePotionData().isExtended());
	}
}
