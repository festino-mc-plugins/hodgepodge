package com.festp.dispenser;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;

public class DispenserCauldronFiller
{
	Dispenser dispenser;
	Block cauldron;
	Material liquidBucket;
	ArrayList<Integer> liquidSlots;
	
	private DispenserCauldronFiller(Dispenser dispenser, ItemStack item, Block cauldron)
	{
		this.dispenser = dispenser;
		this.cauldron = cauldron;
		liquidBucket = item.getType();
        liquidSlots = new ArrayList<>();
        Inventory inv = dispenser.getInventory();
		for (int slot = 0; slot < 9; slot++)
			if (isValidBucket(inv.getItem(slot)))
				liquidSlots.add(slot);
	}
	
	public static DispenserCauldronFiller tryCreate(Dispenser dispenser, ItemStack item, Block cauldron)
	{
    	if (!isValid(item.getType(), cauldron))
    		return null;
    	return new DispenserCauldronFiller(dispenser, item, cauldron);
	}

	public void fill()
	{
    	if (!isValid(liquidBucket, cauldron))
    		return;
		int j = 0;
        Inventory inv = dispenser.getInventory();
		// find bucket slot - it was empty on event
		for (int slot = 0; slot < 9; slot++)
			if (j < liquidSlots.size() && slot != liquidSlots.get(j) || j >= liquidSlots.size()) {
				if (isValidBucket(inv.getItem(slot))) {
                	inv.setItem(slot, new ItemStack(Material.BUCKET));
            		Utils.fullCauldronWater(cauldron);
	                break;
				}
			} else j++;
	}
	
	private static boolean isValid(Material dispensedItem, Block cauldron)
	{
		Material itemMaterial = dispensedItem;
		if (!isEmptiableBucket(itemMaterial))
			return false;
		// empty cauldron
		if (cauldron.getType() == Material.CAULDRON)
			return true;
		// cauldron is already full
		if (isCauldron(cauldron.getType()) && Utils.getCauldronWater(cauldron) >= 0.99999)
			return false;
		return getBucket(cauldron.getType()) == itemMaterial;
	}
	private boolean isValidBucket(ItemStack stack)
	{
		return stack != null && stack.getType() == liquidBucket;
	}
	
	private static boolean isEmptiableBucket(Material m)
	{
		if (m == Material.WATER_BUCKET)
			return true;
		
		if (!isLavaSnowVersion())
			return false;
		return m == Material.LAVA_BUCKET || m == Material.POWDER_SNOW_BUCKET;
	}
	
	private static boolean isCauldron(Material m) {
		if (m == Material.CAULDRON)
			return true;
		if (isEmptiableBucket(getBucket(m)))
			return true;

		return false;
	}

	
	private static Material getBucket(Material material)
	{
		if (material == Material.WATER_CAULDRON)
			return Material.WATER_BUCKET;

		if (!isLavaSnowVersion())
			return null;
		/*if (material == Material.LAVA_CAULDRON)
			return Material.LAVA_BUCKET;
		if (material == Material.POWDER_SNOW_CAULDRON)
			return Material.POWDER_SNOW_BUCKET;*/
		return null;
	}
	
	private static boolean isLavaSnowVersion()
	{
		return Utils.GetVersion() >= 11700;
	}
}
