package com.festp.inventory;

import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.UtilsType;

public class SortHoppers implements Listener {
	final static String standart_hopper_name = "Item Hopper";
	final static String standart_hopperminecart_name = "Minecart with Hopper";
	
	@EventHandler
	public void onHopperPickupItem(InventoryPickupItemEvent event) {
		if (event.getInventory().getType() != InventoryType.HOPPER) return;
		if (!canMoveIntoHopper(event.getInventory(), event.getItem().getItemStack()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onHopperMoveItem(InventoryMoveItemEvent event) {
		if(event.getDestination().getType() != InventoryType.HOPPER || event.getDestination() != event.getInitiator()) return;
		if (!canMoveIntoHopper(event.getDestination(), event.getItem()))
				event.setCancelled(true);
	}
	public static boolean canMoveIntoHopper(Inventory inv, ItemStack item) {
		InventoryHolder holder = inv.getHolder();
		String name = "";
		if (holder instanceof Container)
		{
			Container hopper = (Container)holder;
			name = hopper.getCustomName();
			if (name == null
				|| name.equals(standart_hopper_name))
				return true;
		}
		else if (holder instanceof Entity)
		{
			Entity hopper_minecart = (Entity)holder;
			name = hopper_minecart.getCustomName();
			if (name == null
				|| name.equals(standart_hopperminecart_name))
				return true;
		}
		return canMoveIntoInventory(name, item);
	}
	
	public static boolean canMoveIntoInventory(String filter, ItemStack is) {
		//separate filter by "||" and "or", "minecraft:log", "armor"
		filter = remove_spaces(filter);
		int start_index = 0;
		
		Boolean cur_fit = null;
		for(int i = 0; i < filter.length(); i++) {
			char cur_char = filter.charAt(i);
			if(cur_char == '|') {
				if(start_index != i)
					cur_fit = word_fit(filter.substring(start_index, i), is);
				if(cur_fit != null && cur_fit == true)
					return true;
				start_index = i+1;
			}
			else if(cur_char == '&') {
				if(start_index != i)
					cur_fit = word_fit(filter.substring(start_index, i), is);
				if(cur_fit != null && cur_fit == false)
				{
					for(int j = i+1; j < filter.length(); j++) {
						if(filter.charAt(i) == '|') {
							start_index = j;
							break;
						}
					}
					if(start_index <= i)
						return false;
				}
				start_index = i+1;
			}
			else if(i == filter.length()-1) {
				if(start_index != i)
					cur_fit = word_fit(filter.substring(start_index), is);
			}
		}
		if(cur_fit != null)
			return cur_fit;
		return false;
	}

	private static String remove_spaces(String str) {
		//or - pork, and - stand
		return str.replace(" ", "");
	}
	
	private static boolean word_fit(String word, ItemStack is) {
		Material m = is.getType();
		
		boolean as_id = false;
		if (word.startsWith("mc:")) {
			word = word.substring(3);
			as_id = true;
		} else if (word.startsWith("minecraft:")) {
			word = word.substring(10);
			as_id = true;
		}
		if (as_id)
			return is.getType().toString().equalsIgnoreCase(word);
		
		boolean as_name = false;
		if (word.startsWith("name:")) {
			word = word.substring(5);
			as_name = true;
		} else if (word.startsWith("n:")) {
			word = word.substring(2);
			as_name = true;
		}
		if (as_name)
			return is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains(word);

		boolean as_ench = false;
		if (word.startsWith("e:")) {
			word = word.substring(2);
			as_ench = true;
		} else if (word.startsWith("ench:")) {
			word = word.substring(5);
			as_ench = true;
		}
		if (as_ench)
			return process_ench(word, is);
		
	    if (word.equalsIgnoreCase("all")) {
			return true;
		}
		else if (word.equalsIgnoreCase("armor")) {
			if (UtilsType.isArmor(m))
				return true;
		}
		else if (word.equalsIgnoreCase("tool")) {
			if (UtilsType.isTool(m))
				return true;
		}
		else if (word.equalsIgnoreCase("weapon")) {
			if (UtilsType.isWeapon(m))
				return true;
		}
		
		if(m.toString().toLowerCase().contains(word))
			return true;
		
		return false;
	}
	
	private static boolean process_ench(String str, ItemStack is) {
		Enchantment ench = null;
		Integer lvl = null;
		char[] string = str.toCharArray();
		Character action = null;
		for(int i = 0; i < string.length; i++) {
			if(string[i] == '=' || string[i] == '>' || string[i] == '<') {
				if(action == null) {
					ench = get_ench(str.substring(0, i));
					if(ench == null) return false;
					action = string[i];
				}
			}
			else if(action != null) {
				try {
					lvl = Integer.parseInt(str.substring(i));
				} catch (Exception e) {
					lvl = null;
				}
				break;
			}
		}
		
		if(action == null) {
			ench = get_ench(str);
			if(ench == null) return false;
			else return is.getEnchantmentLevel(ench) > 0;
		}
		else if(lvl != null) {
			if(action == '=') return is.getEnchantmentLevel(ench) == lvl;
			if(action == '>') return is.getEnchantmentLevel(ench) > lvl;
			if(action == '<') return is.getEnchantmentLevel(ench) < lvl;
		}
		
		return false;
	}
	private static Enchantment get_ench(String ench) {
		//Durability(Unbreaking), mending, vanish curse, binding curse
		if(ench.contains("dur")) return Enchantment.DURABILITY;
		if(ench.contains("mend")) return Enchantment.MENDING;
		if(ench.contains("van")) return Enchantment.VANISHING_CURSE;
		if(ench.contains("bind")) return Enchantment.BINDING_CURSE;
		//sharpness, smite, bane of arthropods, knockback, fire aspect, looting, Sweeping Edge
		if(ench.contains("sharp")) return Enchantment.DAMAGE_ALL;
		if(ench.contains("bane") || ench.contains("arth")) return Enchantment.DAMAGE_ARTHROPODS;
		if(ench.contains("smite")) return Enchantment.DAMAGE_UNDEAD;
		if(ench.contains("knock")) return Enchantment.KNOCKBACK;
		if(ench.contains("asp")) return Enchantment.FIRE_ASPECT;
		if(ench.contains("loot")) return Enchantment.LOOT_BONUS_MOBS;
		if(ench.contains("sweep") || ench.contains("edge")) return Enchantment.SWEEPING_EDGE;
		//Efficiency, Fortune, Silk Touch, Luck of the sea, Lure
		if(ench.contains("eff")) return Enchantment.DIG_SPEED;
		if(ench.contains("for")) return Enchantment.LOOT_BONUS_BLOCKS;
		if(ench.contains("silk") || ench.contains("touch")) return Enchantment.SILK_TOUCH;
		if(ench.contains("luck")) return Enchantment.LUCK;
		if(ench.contains("lure")) return Enchantment.LURE;
		//Power, Punch, Flame, Infinity
		if(ench.contains("pow")) return Enchantment.ARROW_DAMAGE;
		if(ench.contains("pun")) return Enchantment.ARROW_KNOCKBACK;
		if(ench.contains("flame")) return Enchantment.ARROW_FIRE;
		if(ench.contains("inf")) return Enchantment.ARROW_INFINITE;
		//Channeling, Impaling, Loyalty, Riptide
		if(ench.contains("chan")) return Enchantment.CHANNELING;
		if(ench.contains("imp")) return Enchantment.IMPALING;
		if(ench.contains("loyal")) return Enchantment.LOYALTY;
		if(ench.contains("rip")) return Enchantment.RIPTIDE;
		
		//Aqua Affinity, Respiration
		if(ench.contains("aqua") || ench.contains("aff")) return Enchantment.WATER_WORKER;
		if(ench.contains("resp")) return Enchantment.OXYGEN;
		//Protection, Projectile Protection, Blast Protection, Fire Protection, Thorns
		if(ench.contains("prot")) return Enchantment.PROTECTION_ENVIRONMENTAL;
		if(ench.contains("proj")) return Enchantment.PROTECTION_PROJECTILE;
		if(ench.contains("blast")) return Enchantment.PROTECTION_EXPLOSIONS;
		if(ench.contains("fire")) return Enchantment.PROTECTION_FIRE;
		if(ench.contains("thorns")) return Enchantment.THORNS;
		//Feather Falling, Depth Strider, Frost Walker
		if(ench.contains("feat") || ench.contains("fall")) return Enchantment.PROTECTION_FALL;
		if(ench.contains("depth") || ench.contains("str")) return Enchantment.DEPTH_STRIDER;
		if(ench.contains("frost") || ench.contains("walk")) return Enchantment.FROST_WALKER;
		
		//if(ench.contains("")) return Enchantment.;
		
		return null;
	}
}
