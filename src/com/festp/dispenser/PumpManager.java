package com.festp.dispenser;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PumpManager {
	public static final Material PUMP_MATERIAL = Material.BLAZE_ROD;
	public static final Enchantment bottomless_bucket_metaench = Enchantment.ARROW_INFINITE;

	enum PumpReadiness {READY, MODULE, NONE};
	enum PumpType {NONE, REGULAR, ADVANCED};
	
	public static PumpReadiness test(Dispenser d, ItemStack dropped) {
		Inventory inv = d.getInventory();
		//test empty bucket
		//test pump module??? - it had already worked
		int bucket_index = -2, module_index = -2, multybucket_index = -2, null_index = -1, pipe_index = -1;
		ItemStack is;
		for(int i = -1; i < 9; i++) {
			if(i<0) is = dropped;
			else is = inv.getItem(i);
			if(is != null)
			{
				if(module_index < -1 && isPump(is)) {
					module_index = i;
					if(bucket_index >= -1 || (multybucket_index >= -1 && null_index >= 0)) {
						break;
					}
				}
				else if( is.getType() == Material.BUCKET ) {
					if(is.getEnchantmentLevel(bottomless_bucket_metaench) > 0)
						bucket_index = 9;
					else if(is.getAmount() == 1 && bucket_index < -1)
						bucket_index = i;
					else if( multybucket_index < -1) {
						multybucket_index = i;
						if(null_index < 0) continue;
					}
					if(module_index >= -1) break;
				}
				else if(is.getType() == Material.NETHER_BRICK_FENCE)
					pipe_index = i;
			}
			else if(null_index < 0) null_index = i;
		}
		//System.out.println("TEST: "+ module_index+" "+bucket_index+" "+multybucket_index+" "+null_index);
		if(module_index >= -1) {
			if(bucket_index >= -1 || (multybucket_index >= -1 && null_index >= 0) || pipe_index >= -1) {
				return PumpReadiness.READY;
			}
			return PumpReadiness.MODULE;
		}
		return PumpReadiness.NONE;
	}
	
	public static boolean isPump(ItemStack is) {
		if (is.getType() != PUMP_MATERIAL || !is.hasItemMeta() || !is.getItemMeta().hasLore()) {
			return false;
		}
		String lore = is.getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH);
		if (lore.contains("pump") || lore.contains("помп")) {
			return true;
		}
		return false;
	}
}
