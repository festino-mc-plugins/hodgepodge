package com.festp.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class NbtUtils {

	/** Use to get invisible item frames */
	@SuppressWarnings("deprecation")
	public static ItemStack setInvisibleEntity(ItemStack stack)
	{
    	return Bukkit.getUnsafe().modifyItemStack(stack, "{EntityTag:{Invisible:1b}}");
	}
}
