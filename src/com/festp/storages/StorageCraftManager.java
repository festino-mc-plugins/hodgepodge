package com.festp.storages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.CraftManager;
import com.festp.Main;

public class StorageCraftManager {
	Server server;
	Main plugin;
	
	ItemStack zero_storage_bottomless, zero_storage_multitype1, zero_storage_multitype2, zero_storage_multitype3;
	ItemMeta storage_meta_bottomless, storage_meta_multitype;
	
	public StorageCraftManager(Main plugin, Server server) {
		this.plugin = plugin;
		this.server = server;
		
		 // Zero storages
		zero_storage_bottomless = new ItemStack(Material.FIREWORK_STAR, 1);
			storage_meta_bottomless = zero_storage_bottomless.getItemMeta();
			storage_meta_bottomless.setDisplayName("Storage");
			storage_meta_bottomless.setLore(Arrays.asList("0 items"));
			storage_meta_bottomless.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		zero_storage_bottomless.setItemMeta(storage_meta_bottomless);
		zero_storage_bottomless.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		zero_storage_bottomless = Storage.setID(zero_storage_bottomless, 0);
		
		zero_storage_multitype1 = new ItemStack(Material.FIREWORK_STAR, 1);
			storage_meta_multitype = zero_storage_multitype1.getItemMeta();
			storage_meta_multitype.setDisplayName("Storage");
			storage_meta_multitype.setLore(Arrays.asList("Smart storage.")); //Smart storage. Maybe smarter than you... Never mind!
			storage_meta_multitype.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		zero_storage_multitype1.setItemMeta(storage_meta_multitype);
		zero_storage_multitype1.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
		zero_storage_multitype1 = Storage.setID(zero_storage_multitype1, 0);
		
		zero_storage_multitype2 = zero_storage_multitype1.clone();
		storage_meta_multitype = zero_storage_multitype2.getItemMeta();
			storage_meta_multitype.setLore(Arrays.asList("Smarter storage.")); //Smarter storage. Who knows what's on its mind?
		zero_storage_multitype2.setItemMeta(storage_meta_multitype);
		zero_storage_multitype2.addUnsafeEnchantment(Enchantment.DIG_SPEED, 2);
		
		zero_storage_multitype3 = zero_storage_multitype1.clone();
		storage_meta_multitype = zero_storage_multitype3.getItemMeta();
			storage_meta_multitype.setLore(Arrays.asList("Just a storage."));
		zero_storage_multitype3.setItemMeta(storage_meta_multitype);
		zero_storage_multitype3.addUnsafeEnchantment(Enchantment.DIG_SPEED, 3);
	}
	
	public ItemStack getBottomless(int ID) {
		if (ID <= 0)
			return null;
		boolean have_file = StoragesFileManager.hasDataFile(ID);
		ItemStack item = Storage.setID(zero_storage_bottomless.clone(), ID);
		if (have_file) {
			Storage storage = Storage.loadFromFile(ID);
			if (storage instanceof StorageBottomless) {
				StorageBottomless sb = (StorageBottomless)storage;
				return sb.getLored(item);
			}
		}
		else
			return item;
		return null;
	}
	
	public ItemStack getMultitype(int ID, int lvl) {
		if (ID <= 0 || lvl <= 0 || lvl > StorageMultitype.MAX_LEVEL)
			return null;
		if (lvl == 1)
			return Storage.setID(zero_storage_multitype1.clone(), ID);
		if (lvl == 2)
			return Storage.setID(zero_storage_multitype2.clone(), ID);
		if (lvl == 3)
			return Storage.setID(zero_storage_multitype3.clone(), ID);
		return null;
	}
	
	//ADD TO PLAYERS
	public void addStorageCrafts()
	{
		String name___storage_bottomless = "storage_bottomless";
		String name___storage_multitype1 = "storage_multitype1";
		String name___storage_multitype2 = "storage_multitype2";
		String name___storage_multitype3 = "storage_multitype3";
		//String name___storage_multitype1to3 = "storage_multitype1to3";
		//String name___storage_multitype2to3 = "storage_multitype2to3";
		String name___storage_back = "storage_back";
    	NamespacedKey key___storage_bottomless = new NamespacedKey(plugin, name___storage_bottomless);
    	NamespacedKey key___storage_multitype1 = new NamespacedKey(plugin, name___storage_multitype1);
    	NamespacedKey key___storage_multitype2 = new NamespacedKey(plugin, name___storage_multitype2);
    	NamespacedKey key___storage_multitype3 = new NamespacedKey(plugin, name___storage_multitype3);
    	//NamespacedKey key___storage_multitype1to3 = new NamespacedKey(plugin, name___storage_multitype1to3);
    	//NamespacedKey key___storage_multitype2to3 = new NamespacedKey(plugin, name___storage_multitype2to3);
    	NamespacedKey key___storage_back = new NamespacedKey(plugin, name___storage_back);
    	
    	CraftManager cm = plugin.getCraftManager();
    	cm.addCraftbookRecipe(key___storage_bottomless);
    	cm.addCraftbookRecipe(key___storage_multitype1);
    	cm.addCraftbookRecipe(key___storage_multitype2);
    	cm.addCraftbookRecipe(key___storage_multitype3);
    	cm.addCraftbookRecipe(key___storage_back);
    	
    	RecipeChoice.MaterialChoice shulker_list = new MaterialChoice(Arrays.asList(
    			Material.SHULKER_BOX,
    			Material.BLACK_SHULKER_BOX, Material.BLUE_SHULKER_BOX, Material.BROWN_SHULKER_BOX, Material.CYAN_SHULKER_BOX,
    			Material.GRAY_SHULKER_BOX, Material.GREEN_SHULKER_BOX, Material.LIGHT_BLUE_SHULKER_BOX, Material.LIGHT_GRAY_SHULKER_BOX,
    			Material.LIME_SHULKER_BOX, Material.MAGENTA_SHULKER_BOX, Material.ORANGE_SHULKER_BOX, Material.PINK_SHULKER_BOX,
    			Material.PURPLE_SHULKER_BOX, Material.RED_SHULKER_BOX, Material.WHITE_SHULKER_BOX, Material.YELLOW_SHULKER_BOX));

    	ShapedRecipe storage_bottomless = new ShapedRecipe(key___storage_bottomless, zero_storage_bottomless); //new ItemStack(Material.FIREWORK_STAR,1)
    	storage_bottomless.shape(new String[]{"OSO", "SES", "OSO"});
    	storage_bottomless.setIngredient('O', Material.OBSIDIAN);
    	storage_bottomless.setIngredient('S', shulker_list);
    	storage_bottomless.setIngredient('E', Material.ENDER_CHEST);
    	server.addRecipe(storage_bottomless);

    	ShapelessRecipe storage_multitype1 = new ShapelessRecipe(key___storage_multitype1, zero_storage_multitype1);
    	storage_multitype1.addIngredient(shulker_list);
    	storage_multitype1.addIngredient(5, Material.NAME_TAG);
    	storage_multitype1.addIngredient(1, Material.ENDER_EYE);
    	server.addRecipe(storage_multitype1);

    	ShapelessRecipe storage_multitype2 = new ShapelessRecipe(key___storage_multitype2, zero_storage_multitype2);
    	storage_multitype2.addIngredient(shulker_list);
    	storage_multitype2.addIngredient(shulker_list);
    	storage_multitype2.addIngredient(5, Material.NAME_TAG);
    	storage_multitype2.addIngredient(1, Material.ENDER_EYE);
    	server.addRecipe(storage_multitype2);

    	ShapelessRecipe storage_multitype3 = new ShapelessRecipe(key___storage_multitype3, zero_storage_multitype3);
    	storage_multitype3.addIngredient(shulker_list);
    	storage_multitype3.addIngredient(shulker_list);
    	storage_multitype3.addIngredient(shulker_list);
    	storage_multitype3.addIngredient(5, Material.NAME_TAG);
    	storage_multitype3.addIngredient(1, Material.ENDER_EYE);
    	server.addRecipe(storage_multitype3);
    	
    	ShapelessRecipe storage_back = new ShapelessRecipe(key___storage_back, new ItemStack(Material.SHULKER_BOX));
    	storage_back.addIngredient(1, Material.FIREWORK_STAR);
    	server.addRecipe(storage_back);
	}
}
