package com.festp.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;
import com.mojang.datafixers.util.Pair;

public class SortHoppers_lost implements Listener {
	//private static Inventory null_hopper;
	
	public SortHoppers_lost() {
		
		//null_hopper = ;
	}
	
	@EventHandler
	public void onHopperPickupItem(InventoryPickupItemEvent event) {
		//System.out.println("PICKUP: "+event.getInventory()+" "+(event.getInventory().getType() != InventoryType.HOPPER));
		if(event.getInventory().getType() != InventoryType.HOPPER) return;
		Inventory hop = event.getInventory();
		if(hop.getName().contains("Item Hopper")) return;
		
		if(!canMoveIntoHopper(hop.getName(), event.getItem().getItemStack()))
				event.setCancelled(true);
	}

	@EventHandler
	public void onHopperMoveItem(InventoryMoveItemEvent event) {
		//System.out.println("MOVE: "+event.getDestination()+" "+(event.getDestination().getType() != InventoryType.HOPPER)+" "+(event.getDestination() != event.getInitiator()));
		if(event.getDestination() != event.getInitiator() || event.getDestination().getType() != InventoryType.HOPPER) return;
		Inventory hop = event.getDestination();
		if(hop.getName().contains("Item Hopper")) return;
		
		if(!canMoveIntoHopper(hop.getName(), event.getItem()))
				event.setCancelled(true);
	}
	
	public static boolean canMoveIntoHopper(String filter, ItemStack is) {
		//separate filter by "||" and "or", "minecraft:log", "armor"
		List<String> words = remove_spaces_and_ors(filter);
		Material m = is.getType();
		for(String word : words) {
			if(word.startsWith("minecraft:")) {
				word.substring(10);
			} else if(word.startsWith("mc:")) {
				word.substring(3);
			}
			else if(word.startsWith("name:")) {
				if(is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains(word.substring(5)))
					return true;
				continue;
			}
			else if(word.startsWith("ench:")) {
				if(is.hasItemMeta()) {
					String ench = word.substring(5);
					if(ench.contains("=")) {
						Pair<Enchantment, Integer> e = prepare_ench(ench, '=');
						if(e.getFirst() == null) continue;

						if(is.getEnchantmentLevel(e.getFirst()) == e.getSecond())
							return true;
					}
					else if(ench.contains(">")) {
						Pair<Enchantment, Integer> e = prepare_ench(ench, '>');
						if(e.getFirst() == null) continue;

						if(is.getEnchantmentLevel(e.getFirst()) > e.getSecond())
							return true;
					}
					else if(ench.contains("<")) {
						Pair<Enchantment, Integer> e = prepare_ench(ench, '<');
						if(e.getFirst() == null || e.getSecond() == null) continue;

						if(is.getEnchantmentLevel(e.getFirst()) < e.getSecond())
							return true;
					}
					else {
						Pair<Enchantment, Integer> e = prepare_ench(ench, ' ');
						if(e.getFirst() == null) continue;
						
						if(is.getEnchantmentLevel(e.getFirst()) > 0)
							return true;
					}
					return true;
				}
				continue;
			}
			
			else if(word.contains("all")) {
				return true;
			}
			else if(word.contains("armor")) {
				if(Utils.isArmor(m))
					return true;
			}
			else if(word.contains("tool")) {
				if(Utils.isTool(m))
					return true;
			}
			else if(word.contains("weapon")) {
				if(Utils.isWeapon(m))
					return true;
			}
			
			String[] split_word = word.split(" ");
			boolean all_contains = true;
			for(String wordy : split_word)
				if(!m.toString().contains(wordy)) {
					all_contains = false;
					break;
				}
			if(all_contains)
				return true;
		}
		return false;
	}
	
	private static List<String> remove_spaces_and_ors(String string) {
		List<String> words = new ArrayList<String>();
		while(string.contains("  "))
			string.replaceAll("  ", " ");
		char[] str = string.toCharArray();
		int start_index = -1, end_index = -1;
		boolean word_started = false;
		for(int i = 0; i < str.length; i++) {
			if(start_index >=0 && end_index >= 0 && i+2 < str.length && ( (str[i] == 'o' && str[i+1] == 'r') || (str[i] == '|' && str[i+1] == '|') ) && str[i+2] == ' ') {
				words.add(string.substring(start_index, end_index+1));
				i = i + 2;
				word_started = false;
				continue;
			}
			if(str[i] != ' ')
				if(word_started)
					end_index = i;
				else {
					start_index = i;
					end_index = i;
					word_started = true;
				}
		}
		if(word_started)
			words.add(string.substring(start_index, end_index+1));
		return words;
	}
	
	private static Pair<Enchantment, Integer> get_ench_and_level(String s) {
		String[] parts = s.split(" ");
		Integer lvl;
		try {
			lvl = Integer.parseInt(parts[parts.length-1]);
		} catch(Exception e) {
			lvl = null;
		}
		
		Enchantment ench = null;
		/*Efficiency, Silk Touch, Fortune, Luck of the Sea, Lure, Unbreaking, Mending, Curse of vanishing, Curse of Binding
		 * Protection, Fire protection, Blast Protection, Projectile Protection, Respiration, Aqua Affinity, Thorns
		 * Depth Strider, Frost Walker, Feather Falling, sharpness, smite, Bane of Arthropods, Knockback, Fire Aspect, Looting,
		 * Sweeping Edge, Power, Punch, Flame, Infinity, Loyalty, Impaling, Riptide, Channeling
		 */
		//SWORD
		s = s.toLowerCase();
		if(s.contains("sharp")) {
			ench = Enchantment.DAMAGE_ALL;
		} else if(s.contains("smite")) {
			ench = Enchantment.DAMAGE_UNDEAD;
		} else if(s.contains("bane") || s.contains("arth")) {
			ench = Enchantment.DAMAGE_ARTHROPODS;
		} else if(s.contains("knock")) {
			ench = Enchantment.KNOCKBACK;
		} else if(s.contains("aspect")) {
			ench = Enchantment.FIRE_ASPECT; 
		} else if(s.contains("loot")) {
			ench = Enchantment.LOOT_BONUS_MOBS;
		} else if(s.contains("sweep") || s.contains("edge")) {
			ench = Enchantment.SWEEPING_EDGE;
		//ARMOR
		} else if(s.contains("blast")) {
			ench = Enchantment.PROTECTION_EXPLOSIONS;
		} else if(s.contains("proj")) {
			ench = Enchantment.PROTECTION_PROJECTILE;
		} else if(s.contains("protect")) {
			ench = Enchantment.PROTECTION_FIRE;
		} else if(s.contains("protect")) {
			ench = Enchantment.PROTECTION_ENVIRONMENTAL;
		} else if(s.contains("fall") || s.contains("feather")) {
			ench = Enchantment.PROTECTION_FALL;
		} else if(s.contains("aqua") || s.contains("af")) {
			ench = Enchantment.WATER_WORKER;
		} else if(s.contains("respiration")) {
			ench = Enchantment.OXYGEN;
		} else if(s.contains("depth") || s.contains("str")) {
			ench = Enchantment.DEPTH_STRIDER;
		} else if(s.contains("frost") || s.contains("walk")) {
			ench = Enchantment.FROST_WALKER;
		} else if(s.contains("thorns")) {
			ench = Enchantment.THORNS;
		//TOOLS
		} else if(s.contains("ef")) {
			ench = Enchantment.DIG_SPEED;
		} else if(s.contains("silk") || s.contains("touch")) {
			ench = Enchantment.SILK_TOUCH;
		} else if(s.contains("fort")) {
			ench = Enchantment.LOOT_BONUS_BLOCKS;
		} else if(s.contains("luck")) {
			ench = Enchantment.LUCK;
		} else if(s.contains("lure")) {
			ench = Enchantment.LURE;
		//BOWS
		} else if(s.contains("pow")) {
			ench = Enchantment.ARROW_DAMAGE;
		} else if(s.contains("punch")) {
			ench = Enchantment.ARROW_KNOCKBACK;
		} else if(s.contains("flame") || s.contains("arrow")) {
			ench = Enchantment.ARROW_FIRE;
		} else if(s.contains("inf")) {
			ench = Enchantment.ARROW_INFINITE;
		//TRIDENT
		} else if(s.contains("loyal")) {
			ench = Enchantment.LOYALTY;
		} else if(s.contains("imp")) {
			ench = Enchantment.IMPALING;
		} else if(s.contains("rip")) {
			ench = Enchantment.RIPTIDE;
		} else if(s.contains("chan")) {
			ench = Enchantment.CHANNELING;
		//GENERAL(+CURSES)
		} else if(s.contains("dur")) {
			ench = Enchantment.DURABILITY;
		} else if(s.contains("mend")) {
			ench = Enchantment.MENDING;
		} else if(s.contains("bind")) {
			ench = Enchantment.BINDING_CURSE;
		} else if(s.contains("vanish")) {
			ench = Enchantment.VANISHING_CURSE;
		}
		
		return new Pair<Enchantment, Integer>(ench, lvl);
	}

	private static Pair<Enchantment, Integer> prepare_ench(String ench, char action) {
		if(action != ' ')
			ench.replace(action, ' ');
		while(ench.contains("  "))
			ench.replaceAll("  ", " ");
		return get_ench_and_level(ench);
	}
}
