package com.festp.inventory;

import org.bukkit.inventory.ItemStack;

public class ItemLoadResult {
	public final ItemStack[] contents;
	public final boolean invalid;
	
	public ItemLoadResult(ItemStack[] contents, boolean invalid)
	{
		this.contents = contents;
		this.invalid = invalid;
	}
}
