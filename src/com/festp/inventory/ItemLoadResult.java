package com.festp.inventory;

import org.bukkit.inventory.ItemStack;

public class ItemLoadResult {
	public final ItemStack[] contents;
	public final boolean valid;
	
	public ItemLoadResult(ItemStack[] contents, boolean valid)
	{
		this.contents = contents;
		this.valid = valid;
	}
}
