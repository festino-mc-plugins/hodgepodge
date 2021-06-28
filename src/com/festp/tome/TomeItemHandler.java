package com.festp.tome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.TreeSpecies;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.festp.CraftManager;
import com.festp.Main;

//to AbstractHorse and Donkeys

//entity id had replaced tome id
public class TomeItemHandler implements Listener {
	
	public enum TomeType { MINECART, BOAT, HORSE, CUSTOM_HORSE, ALL, CUSTOM_ALL };
	private static final int REPAIR_COST = 1000;
	public static final String TOME_NBT_KEY = "summonertome";
	
	public static final String lore_eng_minecart_tome = "Summons minecart";
	public static final String lore_eng_boat_tome = "Summons boat";
	public static final String lore_eng_horse_tome = "Summons horse";
	public static final String lore_eng_custom_horse_tome = "Summons custom horse";
	public static final String lore_eng_all_tome = "Summons minecart, boat or horse";
	public static final String lore_eng_custom_all_tome =  "Summons minecart, boat or custom horse";
	public static final String name_eng_minecart_tome = "Minecart tome";
	public static final String name_eng_boat_tome = "Boat tome";
	public static final String name_eng_horse_tome = "Horse tome";
	public static final String name_eng_custom_horse_tome = "Advanced horse tome";
	public static final String name_eng_all_tome = "United tome";
	public static final String name_eng_custom_all_tome =  "Advanced united tome";
	
	public static ItemStack getTome(TomeType type) {
		if (type == null)
			return null;
    	ItemStack tome = new ItemStack(Material.ENCHANTED_BOOK);
    	Repairable rmeta = (Repairable) tome.getItemMeta();
    	rmeta.setRepairCost(REPAIR_COST);
    	tome.setItemMeta((ItemMeta) rmeta);
    	ItemMeta meta = tome.getItemMeta();
    	switch (type) {
    	case MINECART:
        	meta.setDisplayName(TomeItemHandler.name_eng_minecart_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_minecart_tome)); break;
    	case BOAT:
        	meta.setDisplayName(TomeItemHandler.name_eng_boat_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_boat_tome)); break;
    	case HORSE:
    		meta.setDisplayName(TomeItemHandler.name_eng_horse_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_horse_tome)); break;
    	case CUSTOM_HORSE:
    		meta.setDisplayName(TomeItemHandler.name_eng_custom_horse_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_custom_horse_tome)); break;
    	case ALL:
    		meta.setDisplayName(TomeItemHandler.name_eng_all_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_all_tome)); break;
    	case CUSTOM_ALL:
    		meta.setDisplayName(TomeItemHandler.name_eng_custom_all_tome);
        	meta.setLore(Arrays.asList(TomeItemHandler.lore_eng_custom_all_tome)); break;
    	}
    	tome.setItemMeta(meta);
    	tome = TomeFormatter.setType(tome, type);
    	return tome;
	}

	@SuppressWarnings("deprecation")
	public static void addTomeCrafts(Main plugin) {
    	CraftManager cm = plugin.getCraftManager();
    	Server server = plugin.getServer();
    	
    	NamespacedKey key_minecart = new NamespacedKey(plugin, "minecart_tome");
    	NamespacedKey key_boat = new NamespacedKey(plugin, "boat_tome");
    	NamespacedKey key_horse = new NamespacedKey(plugin, "horse_tome");
    	NamespacedKey key_custom_horse = new NamespacedKey(plugin, "custom_horse_tome"); //for both 'custom horse' and 'custom all'
    	NamespacedKey key_all = new NamespacedKey(plugin, "all_tome"); //for both 'all' and 'custom all'
    	NamespacedKey key_custom_all_h = new NamespacedKey(plugin, "custom_all_tome_from_horse");
    	NamespacedKey key_custom_all_a = new NamespacedKey(plugin, "custom_all_tome_from_all");
		
    	cm.addCraftbookRecipe(key_minecart);
    	cm.addCraftbookRecipe(key_boat);
    	cm.addCraftbookRecipe(key_horse);
    	cm.addCraftbookRecipe(key_custom_horse);
    	cm.addCraftbookRecipe(key_all);
    	cm.addCraftbookRecipe(key_custom_all_h);
    	cm.addCraftbookRecipe(key_custom_all_a);
		
		//minecart tome - book, 4 xp bottle and 4 minecarts
    	ItemStack minecart_book = getTome(TomeType.MINECART);
    	ShapelessRecipe minecart_tome = new ShapelessRecipe(key_minecart, minecart_book);
    	minecart_tome.addIngredient(1, Material.BOOK);
    	minecart_tome.addIngredient(4, Material.EXPERIENCE_BOTTLE);
    	minecart_tome.addIngredient(4, Material.MINECART);
    	server.addRecipe(minecart_tome);
		
		//boat tome - book, 2 xp bottle and 6 colors boats
    	//all tomes with boats can be customized by all the 6 boat types
    	ItemStack boat_book = getTome(TomeType.BOAT);
    	ShapelessRecipe boat_tome = new ShapelessRecipe(key_boat, boat_book);
    	boat_tome.addIngredient(1, Material.BOOK);
    	boat_tome.addIngredient(2, Material.EXPERIENCE_BOTTLE);
    	boat_tome.addIngredient(1, Material.ACACIA_BOAT);
    	boat_tome.addIngredient(1, Material.BIRCH_BOAT);
    	boat_tome.addIngredient(1, Material.DARK_OAK_BOAT);
    	boat_tome.addIngredient(1, Material.JUNGLE_BOAT);
    	boat_tome.addIngredient(1, Material.OAK_BOAT);
    	boat_tome.addIngredient(1, Material.SPRUCE_BOAT);
    	server.addRecipe(boat_tome);
    	
		//horse tome - book, 2 xp bottles, 4 saddles, 2 (leads?)
    	//unwearable armor, untakeable saddle
    	ItemStack horse_book = getTome(TomeType.HORSE);
    	ShapelessRecipe horse_tome = new ShapelessRecipe(key_horse, horse_book);
    	horse_tome.addIngredient(1, Material.BOOK);
    	horse_tome.addIngredient(2, Material.EXPERIENCE_BOTTLE);
    	horse_tome.addIngredient(2, Material.LEAD);
    	horse_tome.addIngredient(2, Material.SADDLE);
    	horse_tome.addIngredient(1, Material.APPLE);
    	horse_tome.addIngredient(1, Material.GOLDEN_APPLE);
    	server.addRecipe(horse_tome);
    	
    	RecipeChoice.ExactChoice minecart_choice = new RecipeChoice.ExactChoice(minecart_book);
    	RecipeChoice.ExactChoice boat_choice = new RecipeChoice.ExactChoice(boat_book);
    	RecipeChoice.ExactChoice horse_choice = new RecipeChoice.ExactChoice(horse_book);
    	
		//custom horse tome - horse tome, 4 xp bottles, jump potion, speed potion, instheal potion, label
    	//in craft events because of Horse tome and potions
    	ItemStack custom_horse_book = getTome(TomeType.CUSTOM_HORSE);
    	ShapelessRecipe custom_horse_tome = new ShapelessRecipe(key_custom_horse, custom_horse_book);
    	custom_horse_tome.addIngredient(4, Material.EXPERIENCE_BOTTLE);
    	custom_horse_tome.addIngredient(1, Material.NAME_TAG);
    	
    	ItemStack heal_potion = new ItemStack(Material.POTION);
    	PotionMeta p_meta = (PotionMeta)heal_potion.getItemMeta();
    	p_meta.setBasePotionData(new PotionData(PotionType.INSTANT_HEAL));
    	heal_potion.setItemMeta(p_meta);
    	ItemStack speed_potion = new ItemStack(Material.POTION);
    	p_meta = (PotionMeta)speed_potion.getItemMeta();
    	p_meta.setBasePotionData(new PotionData(PotionType.SPEED));
    	speed_potion.setItemMeta(p_meta);
    	ItemStack jump_potion = new ItemStack(Material.POTION);
    	p_meta = (PotionMeta)jump_potion.getItemMeta();
    	p_meta.setBasePotionData(new PotionData(PotionType.JUMP));
    	jump_potion.setItemMeta(p_meta);
    	RecipeChoice.ExactChoice heal_choice = new RecipeChoice.ExactChoice(heal_potion);
    	RecipeChoice.ExactChoice speed_choice = new RecipeChoice.ExactChoice(speed_potion);
    	RecipeChoice.ExactChoice jump_choice = new RecipeChoice.ExactChoice(jump_potion);
    	custom_horse_tome.addIngredient(heal_choice);
    	custom_horse_tome.addIngredient(speed_choice);
    	custom_horse_tome.addIngredient(jump_choice);
    	//custom_horse_tome.addIngredient(1, Material.ENCHANTED_BOOK);
    	custom_horse_tome.addIngredient(horse_choice);
    	server.addRecipe(custom_horse_tome);
    	
    	//RecipeChoice.ExactChoice custom_horse_choice = new RecipeChoice.ExactChoice(custom_horse_book);
		
		//united tome - horse, minecart, boat tomes, slime block, 5 xp bottles
    	ItemStack all_book = getTome(TomeType.ALL);
    	ShapelessRecipe all_tome = new ShapelessRecipe(key_all, all_book);
    	//all_tome.addIngredient(3, Material.ENCHANTED_BOOK);
    	all_tome.addIngredient(minecart_choice);
    	all_tome.addIngredient(boat_choice);
    	all_tome.addIngredient(horse_choice);
    	all_tome.addIngredient(5, Material.EXPERIENCE_BOTTLE);
    	all_tome.addIngredient(1, Material.SLIME_BLOCK);
    	server.addRecipe(all_tome);
    	
    	//united custom tome - slime block, custom horse, minecart, boat tomes, 5 xp bottles
    	//from custom horse, boat and minecart tomes
    	//from united tome
	}
	
	//set horse name and let spigot using custom potions
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event) {
		TomeType type = TomeFormatter.getTomeType(event.getInventory().getResult());
		if(type == null) return;

		//custom horse/custom all tome:   horse/all tome + NAME from nametag + jump, speed and instheal potions
		if(type == TomeType.CUSTOM_HORSE) { //custom_horse or custom_all from all
			ItemStack[] matrix = event.getInventory().getMatrix();
			
			String custom_name = null;
			boolean correct = true, have_ms = false, have_jump = false, have_hp = false;
			TomeType new_type = null;
			ItemStack old_tome = null;
			for(int i = 0; i < matrix.length; i++) {
				if(!correct) break;
				if(matrix[i].getType() == Material.NAME_TAG) {
					if(matrix[i].getItemMeta().hasDisplayName()) {
						custom_name = matrix[i].getItemMeta().getDisplayName();
					}
				}
				else if(matrix[i].getType() == Material.POTION) {
					PotionMeta potion = (PotionMeta) matrix[i].getItemMeta();
					List<PotionEffect> effect_list;
					if(potion.hasCustomEffects())
						effect_list = potion.getCustomEffects();
					else
						effect_list = new ArrayList<>();
					PotionData pd = potion.getBasePotionData();
					PotionEffect pet = pd.getType().getEffectType().createEffect(1, 1);
					effect_list.add(pet);
					for(PotionEffect pe : effect_list) {
						if(pe.getType() == PotionEffectType.SPEED)
							if(have_ms) {
								correct = false;
								break;
							}
							else {
								have_ms = true;
							}
						else if(pe.getType() == PotionEffectType.JUMP)
							if(have_jump) {
								correct = false;
								break;
							}
							else {
								have_jump = true;
							}
						else if(pe.getType() == PotionEffectType.HEAL)
							if(have_hp) {
								correct = false;
								break;
							}
							else {
								have_hp = true;
							}
					}
				}
				else if(matrix[i].getType() == Material.ENCHANTED_BOOK) {
					if(new_type != null) {
						correct = false;
						break;
					}
					old_tome = matrix[i];
					TomeType old_type = TomeFormatter.getTomeType(matrix[i]);
					if(old_type == TomeType.HORSE) {
						new_type = TomeType.CUSTOM_HORSE;
					}
					else if(old_type == TomeType.ALL) {
						new_type = TomeType.CUSTOM_ALL;
					}
					else {
						correct = false;
						break;
					}
				}
			}
			
			if(correct && new_type != null && have_hp && have_jump && have_ms) {
				if(new_type == TomeType.CUSTOM_ALL) {
					TreeSpecies wood_type = TomeFormatter.get_boat_type(old_tome);
			    	ItemStack custom_all2_book = TomeFormatter.setTome(old_tome, 'A', "o");
			    	custom_all2_book = TomeFormatter.set_boat_type(custom_all2_book, wood_type);
			    	ItemMeta custom_all2_meta = custom_all2_book.getItemMeta();
			    	custom_all2_meta.setDisplayName(name_eng_custom_all_tome);
			    	custom_all2_meta.setLore(Arrays.asList(lore_eng_custom_all_tome));
			    	custom_all2_book.setItemMeta(custom_all2_meta);
			    	event.getInventory().setResult(custom_all2_book);
				}
				if(custom_name != null) {
					ItemStack tome = event.getInventory().getResult();
					tome.getItemMeta().setDisplayName(custom_name);
					event.getInventory().setResult(tome);
				}
		    	return;
			}
			else
				event.getInventory().setResult(null);
		}
		//all/custom all:   all three tomes
		if (type == TomeType.ALL || type == TomeType.CUSTOM_ALL) {
			ItemStack[] matrix = event.getInventory().getMatrix();
			
			boolean correct = true, have_mc = false, have_boat = false; int have_horse = 0;
			TomeType new_type = null;
			ItemStack old_boat = null;
			ItemStack old_custom_horse = null;
			for (int i = 0; i < matrix.length; i++) {
				if (!correct) break;
				else if (matrix[i].getType() == Material.ENCHANTED_BOOK) {
					TomeType old_type = TomeFormatter.getTomeType(matrix[i]);
					if (old_type == TomeType.MINECART) {
						if (have_mc) {
							correct = false;
							break;
						}
						have_mc = true;
					}
					else if (old_type == TomeType.BOAT) {
						if (have_boat) {
							correct = false;
							break;
						}
						have_boat = true;
						old_boat = matrix[i];
					}
					else if (old_type == TomeType.HORSE) {
						if (have_horse > 0) {
							correct = false;
							break;
						}
						have_horse = 1;
						new_type = TomeType.ALL;
					}
					else if (old_type == TomeType.CUSTOM_HORSE) {
						if (have_horse > 0) {
							correct = false;
							break;
						}
						have_horse = 2;
						old_custom_horse = matrix[i];
						new_type = TomeType.CUSTOM_ALL;
					}
					else {
						correct = false;
						break;
					}
				}
			}
			
			if (correct && new_type != null && have_mc && have_boat && have_horse > 0) {
				if (new_type == TomeType.ALL) {
					TreeSpecies wood_type = TomeFormatter.get_boat_type(old_boat);
			    	ItemStack all_book = event.getInventory().getResult();
			    	all_book = TomeFormatter.set_boat_type(all_book, wood_type);
			    	event.getInventory().setResult(all_book);
				}
				if (new_type == TomeType.CUSTOM_ALL) {
					TreeSpecies wood_type = TomeFormatter.get_boat_type(old_boat);
			    	ItemStack custom_all_book = new ItemStack(Material.ENCHANTED_BOOK);
			    	custom_all_book = TomeFormatter.setTome(event.getInventory().getResult(), 'A', "o");
			    	custom_all_book = TomeFormatter.set_boat_type(custom_all_book, wood_type);
			    	custom_all_book = TomeFormatter.set_horse_data(custom_all_book, TomeFormatter.get_horse_data(old_custom_horse));
			    	ItemMeta custom_all_meta = custom_all_book.getItemMeta();
			    	if (old_custom_horse.getItemMeta().hasDisplayName() && !name_eng_custom_horse_tome.contains(old_custom_horse.getItemMeta().getDisplayName()))
				    	custom_all_meta.setDisplayName(old_custom_horse.getItemMeta().getDisplayName());
			    	else
			    		custom_all_meta.setDisplayName(name_eng_custom_all_tome);
			    	custom_all_meta.setLore(Arrays.asList(lore_eng_custom_all_tome));
			    	custom_all_book.setItemMeta(custom_all_meta);
			    	event.getInventory().setResult(custom_all_book);
				}
		    	return;
			}
			else
				event.getInventory().setResult(null);
		}
	}

	//set boat type
	@EventHandler
	public void onCraft(CraftItemEvent event) {
		ItemStack cur_result = event.getInventory().getResult();
		TomeType type = TomeFormatter.getTomeType(cur_result);
		if (type == null) return;
		
		if (type == TomeType.BOAT) {
			cur_result = TomeFormatter.set_boat(cur_result, event.getInventory().getMatrix()[4]); //central cell
	    	event.getInventory().setResult(cur_result);
		}
	}
}
