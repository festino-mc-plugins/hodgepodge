	package com.festp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.remain.SoulStone;
import com.festp.remain.SummonerTome;
import com.festp.storages.Storage;
import com.festp.storages.StoragesFileManager;

import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import net.minecraft.server.v1_13_R2.NBTTagCompound;

public class CraftManager implements Listener {
	Server server;
	mainListener plugin;
	
	ItemMeta storage_meta_bottomless, storage_meta_multitype;
	int items_limit = 64*54;
	int items_stacks_limit = 54;
	
	List<Recipe> temp_recipes = new ArrayList<>();
	List<NamespacedKey> recipe_names = new ArrayList<>();
	
	public CraftManager(mainListener plugin, Server server) {
		this.plugin = plugin;
		this.server = server;
		
		ItemStack someneeded_it = new ItemStack(Material.STONE, 1);
		someneeded_it.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		someneeded_it = setStorageID(someneeded_it, 0);
		storage_meta_bottomless = someneeded_it.getItemMeta();
		storage_meta_bottomless.setDisplayName("Storage"); //Хранилище
		storage_meta_bottomless.setLore(Arrays.asList("0 items"));
		storage_meta_bottomless.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		ItemStack someneeded_it2 = new ItemStack(Material.STONE, 1);
		someneeded_it2.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
		someneeded_it2 = setStorageID(someneeded_it2, 0);
		storage_meta_multitype = someneeded_it2.getItemMeta();
		storage_meta_multitype.setDisplayName("Storage"); //Хранилище
		storage_meta_multitype.setLore(Arrays.asList("Smart storage. Maybe smarter than you.")); //Storage's stupidon't!
		storage_meta_multitype.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
	}
	
	public void addCrafts() {
		printSomeVanillaCrafts();
		
		addFurnaceCrafts();
		addSomeCrafts();
		addStairsAndSlabsCrafts();
		SoulStone.addSoulStoneCrafts(plugin);
		SummonerTome.addTomeCrafts(plugin);
	}
	
	public void giveRecipe(Player p, String recipe) {
		Bukkit.getServer().dispatchCommand(p, "recipe give "+p.getName()+" "+recipe);
	}
	public void giveOwnRecipe(Player p, String recipe) {
		giveRecipe(p, plugin.getName().toLowerCase()+":"+recipe);
	}
	public void giveRecipe(HumanEntity player, NamespacedKey key) {
		player.discoverRecipe(key);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		for(NamespacedKey recipe_name : recipe_names) {
			giveRecipe(p, recipe_name);
		}
	}
	
	private void addSomeCrafts() {
		String name___torch_1 = "torch_from_fireball";
		String name___redsand = "redsand_from_sand_and_redstone";
		String name___clay_1 = "clay1";
		String name___clay_2 = "clay2";
		String name___sand_1 = "sand_from_sandstone";
		String name___redsand_1 = "redsand_from_redsandstone";
		String name___dragonegg_1 = "dragon_egg_from_dragon_egg";
		String name___grass_1 = "grass_from_dirt";
		String name___mycel_1 = "mycel_from_dirt_red";
		String name___mycel_2 = "mycel_from_dirt_brown";
		String name___pump_regular = "regular_pump";
		String name___pump_advanced = "advanced_pump";
		String name___borsch_1 = "borsch";
		String name___chorus_1 = "chorus_from_flower";
		String name___stick_1 = "stick_from_arrows";
		String name___smooth_stone_1 = "smooth_stone_from_slabs";
		String name___smooth_sandstone_1 = "smooth_ss_from_ss";
		String name___smooth_redsandstone_1 = "smooth_redss_from_redss";
		String name___storage_bottomless = "storage_bottomless";
		String name___storage_multitype = "storage_multitype";

		NamespacedKey key___torch = new NamespacedKey(plugin, name___torch_1);
    	NamespacedKey key___redsand = new NamespacedKey(plugin, name___redsand);
    	NamespacedKey key___clay_1 = new NamespacedKey(plugin, name___clay_1);
    	NamespacedKey key___clay_2 = new NamespacedKey(plugin, name___clay_2);
    	NamespacedKey key___sand_1 = new NamespacedKey(plugin, name___sand_1);
    	NamespacedKey key___redsand_1 = new NamespacedKey(plugin, name___redsand_1);
    	NamespacedKey key___dragonegg = new NamespacedKey(plugin, name___dragonegg_1);
    	NamespacedKey key___grass = new NamespacedKey(plugin, name___grass_1);
    	NamespacedKey key___mycel_1 = new NamespacedKey(plugin, name___mycel_1);
    	NamespacedKey key___mycel_2 = new NamespacedKey(plugin, name___mycel_2);
    	NamespacedKey key___pump_regular = new NamespacedKey(plugin, name___pump_regular);
    	NamespacedKey key___pump_advanced = new NamespacedKey(plugin, name___pump_advanced);
    	NamespacedKey key___borsch = new NamespacedKey(plugin, name___borsch_1);
    	NamespacedKey key___chorus = new NamespacedKey(plugin, name___chorus_1);
    	NamespacedKey key___stick_1 = new NamespacedKey(plugin, name___stick_1);
    	NamespacedKey key___smooth_stone = new NamespacedKey(plugin, name___smooth_stone_1);
    	NamespacedKey key___smooth_sandstone = new NamespacedKey(plugin, name___smooth_sandstone_1);
    	NamespacedKey key___smooth_redsandstone = new NamespacedKey(plugin, name___smooth_redsandstone_1);
    	NamespacedKey key___storage_bottomless = new NamespacedKey(plugin, name___storage_bottomless);
    	NamespacedKey key___storage_multitype = new NamespacedKey(plugin, name___storage_multitype);
		recipe_names.add(key___torch);
		recipe_names.add(key___redsand);
		recipe_names.add(key___clay_1);
		recipe_names.add(key___clay_2);
		recipe_names.add(key___sand_1);
		recipe_names.add(key___redsand_1);
		recipe_names.add(key___dragonegg);
		recipe_names.add(key___grass);
		recipe_names.add(key___mycel_1);
		recipe_names.add(key___mycel_2);
		recipe_names.add(key___pump_regular);
		recipe_names.add(key___pump_advanced);
		recipe_names.add(key___borsch);
		recipe_names.add(key___chorus);
		recipe_names.add(key___stick_1);
		recipe_names.add(key___smooth_stone);
		recipe_names.add(key___smooth_sandstone);
		recipe_names.add(key___smooth_redsandstone);
		recipe_names.add(key___storage_bottomless);
		recipe_names.add(key___storage_multitype);
		
    	ShapelessRecipe torch_from_fireball = new ShapelessRecipe(key___torch, new ItemStack(Material.TORCH,16) );
    	torch_from_fireball.addIngredient(1, Material.FIRE_CHARGE);
    	torch_from_fireball.addIngredient(1, Material.STICK);
		server.addRecipe(torch_from_fireball);
		
    	ShapedRecipe redsand_from_sand_and_redstone = new ShapedRecipe(key___redsand, new ItemStack(Material.RED_SAND,8) );
    	redsand_from_sand_and_redstone.shape(new String[]{"SSS", "SRS", "SSS"});
    	redsand_from_sand_and_redstone.setIngredient('S', Material.SAND);
    	redsand_from_sand_and_redstone.setIngredient('R', Material.REDSTONE);
    	server.addRecipe(redsand_from_sand_and_redstone);
    	
    	ShapelessRecipe clay1 = new ShapelessRecipe(key___clay_1, new ItemStack(Material.CLAY_BALL,8) );
    	clay1.addIngredient(1, Material.IRON_NUGGET);
    	clay1.addIngredient(4, Material.SAND);
    	clay1.addIngredient(4, Material.FLINT);
    	//clay.addIngredient(1, Material.WATER_BUCKET.getNewData((byte) 0));
		server.addRecipe(clay1);
    	
    	ShapelessRecipe clay2 = new ShapelessRecipe(key___clay_2, new ItemStack(Material.CLAY_BALL,8) );
    	clay2.addIngredient(1, Material.IRON_NUGGET);
    	clay2.addIngredient(4, Material.SAND);
    	clay2.addIngredient(4, Material.GRAVEL);
		server.addRecipe(clay2);

    	ShapelessRecipe sand_from_sandstone = new ShapelessRecipe(key___sand_1, new ItemStack(Material.SAND,4,(short)0) );
    	sand_from_sandstone.addIngredient(1, Material.SANDSTONE);
		server.addRecipe(sand_from_sandstone);

    	ShapelessRecipe redsand_from_redsandstone = new ShapelessRecipe(key___redsand_1, new ItemStack(Material.SAND,4,(short)1) );
    	redsand_from_redsandstone.addIngredient(1, Material.RED_SANDSTONE);
		server.addRecipe(redsand_from_redsandstone);

    	ShapedRecipe dragon_egg_from_dragon_egg = new ShapedRecipe(key___dragonegg, new ItemStack(Material.DRAGON_EGG,2) );
    	dragon_egg_from_dragon_egg.shape(new String[]{"OOO", "ODO", "OOO"});
    	dragon_egg_from_dragon_egg.setIngredient('O', Material.OBSIDIAN);
    	dragon_egg_from_dragon_egg.setIngredient('D', Material.DRAGON_EGG);
    	server.addRecipe(dragon_egg_from_dragon_egg);

    	ShapelessRecipe grass_from_dirt = new ShapelessRecipe(key___grass, new ItemStack(Material.GRASS_BLOCK,1) );
    	grass_from_dirt.addIngredient(1, Material.DIRT);
    	grass_from_dirt.addIngredient(1, Material.BONE_MEAL);
    	server.addRecipe(grass_from_dirt);

    	ShapelessRecipe mycel_from_dirt1 = new ShapelessRecipe(key___mycel_1, new ItemStack(Material.MYCELIUM,1) );
    	mycel_from_dirt1.addIngredient(1, Material.DIRT);
    	mycel_from_dirt1.addIngredient(1, Material.BROWN_MUSHROOM);
    	server.addRecipe(mycel_from_dirt1);

    	ShapelessRecipe mycel_from_dirt2 = new ShapelessRecipe(key___mycel_2, new ItemStack(Material.MYCELIUM,1) );
    	mycel_from_dirt2.addIngredient(1, Material.DIRT);
    	mycel_from_dirt2.addIngredient(1, Material.RED_MUSHROOM);
    	server.addRecipe(mycel_from_dirt2);
    	
    	
    	
    	//PUMP
    	ItemStack reg_pump = new ItemStack(Material.BLAZE_ROD, 1);
    	ItemMeta reg_pump_meta = reg_pump.getItemMeta();
    	String RUS_reg_pump_name = "Обычная помпа", ENG_reg_pump_name = "Regular Pump";
    	String RUS_reg_pump_lore = "Обычный модуль помпы для раздатчика", ENG_reg_pump_lore = "Regular pump module for dispenser (pumps water and lava)";
    	reg_pump_meta.setDisplayName(RUS_reg_pump_name);
    	reg_pump_meta.setLore(Arrays.asList(RUS_reg_pump_lore));
    	reg_pump.setItemMeta(reg_pump_meta);
    	ShapedRecipe regular_pump = new ShapedRecipe(key___pump_regular, reg_pump);
    	regular_pump.shape(new String[]{"RPR", "PHP", "RPR"});
    	regular_pump.setIngredient('R', Material.REDSTONE);
    	regular_pump.setIngredient('P', Material.PISTON);
    	regular_pump.setIngredient('H', Material.HOPPER);
    	server.addRecipe(regular_pump);

    	ItemStack top_pump = new ItemStack(Material.BLAZE_ROD, 1);
    	ItemMeta top_pump_meta = top_pump.getItemMeta();
    	String RUS_top_pump_name = "Продвинутая помпа", ENG_top_pump_name = "Advanced Pump";
    	String RUS_top_pump_lore = "Продвинутый модуль помпы для раздатчика", ENG_top_pump_lore = "Advanced pump module for dispenser (pumps water and lava)";
    	top_pump_meta.setDisplayName(RUS_top_pump_name);
    	top_pump_meta.setLore(Arrays.asList(RUS_top_pump_lore));
    	top_pump.setItemMeta(top_pump_meta);
    	ShapedRecipe advanced_pump = new ShapedRecipe(key___pump_advanced, top_pump);
    	advanced_pump.shape(new String[]{"RMR", "MSM", "RMR"});
    	advanced_pump.setIngredient('R', Material.REDSTONE_BLOCK);
    	advanced_pump.setIngredient('M', Material.BLAZE_ROD);
    	advanced_pump.setIngredient('S', Material.NETHER_STAR);
    	server.addRecipe(advanced_pump);

    	ShapelessRecipe pocket_borsch = new ShapelessRecipe(key___borsch, new ItemStack(Material.BEETROOT_SOUP,1) );
    	pocket_borsch.addIngredient(1, Material.BEETROOT);
    	pocket_borsch.addIngredient(1, Material.BOWL);
    	server.addRecipe(pocket_borsch);

    	ShapelessRecipe chorus_from_flower = new ShapelessRecipe(key___chorus, new ItemStack(Material.CHORUS_FRUIT,10) );
    	chorus_from_flower.addIngredient(1, Material.CHORUS_FLOWER);
    	server.addRecipe(chorus_from_flower);

    	ShapelessRecipe stick_from_arrows = new ShapelessRecipe(key___stick_1, new ItemStack(Material.STICK,1) );
    	stick_from_arrows.addIngredient(4, Material.ARROW);
    	server.addRecipe(stick_from_arrows);

    	ShapedRecipe smooth_stone_from_slabs = new ShapedRecipe(key___smooth_stone, new ItemStack(Material.SMOOTH_STONE,2) );
    	smooth_stone_from_slabs.shape(new String[]{" S ", "SSS", " S "});
    	smooth_stone_from_slabs.setIngredient('S', Material.STONE_SLAB);
    	server.addRecipe(smooth_stone_from_slabs);

    	ShapelessRecipe smooth_sandstone_from_andstone = new ShapelessRecipe(key___smooth_sandstone, new ItemStack(Material.SMOOTH_SANDSTONE,4) );
    	smooth_sandstone_from_andstone.addIngredient(4, Material.CUT_SANDSTONE);
    	server.addRecipe(smooth_sandstone_from_andstone);

    	ShapelessRecipe smooth_redsandstone_from_andstone = new ShapelessRecipe(key___smooth_redsandstone, new ItemStack(Material.SMOOTH_RED_SANDSTONE,4) );
    	smooth_redsandstone_from_andstone.addIngredient(4, Material.CUT_RED_SANDSTONE);
    	server.addRecipe(smooth_redsandstone_from_andstone);

    	ShapedRecipe storage_bottomless = new ShapedRecipe(key___storage_bottomless, new ItemStack(Material.FIREWORK_STAR,1) );
    	storage_bottomless.shape(new String[]{"OSO", "SES", "OSO"});
    	storage_bottomless.setIngredient('O', Material.OBSIDIAN);
    	storage_bottomless.setIngredient('S', Material.SHULKER_BOX);
    	storage_bottomless.setIngredient('E', Material.ENDER_PEARL);
    	server.addRecipe(storage_bottomless);

    	ShapedRecipe storage_multitype = new ShapedRecipe(key___storage_multitype, new ItemStack(Material.FIREWORK_STAR,1) );
    	storage_multitype.shape(new String[]{"OSO", "SES", "OSO"});
    	storage_multitype.setIngredient('O', Material.OBSIDIAN);
    	storage_multitype.setIngredient('S', Material.SHULKER_BOX);
    	storage_multitype.setIngredient('E', Material.ENDER_EYE);
    	server.addRecipe(storage_multitype);
	}
	
	@EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
		CraftingInventory ci = event.getInventory();
		ItemStack[] matrix = ci.getMatrix();
		//craft grid test
		if(matrix.length == 9 && matrix[0] != null && matrix[1] != null && matrix[2] != null && matrix[3] != null
				&& matrix[5] != null && matrix[6] != null && matrix[7] != null && matrix[8] != null) {
			//Top pump module
			if(matrix[0].getType().equals(Material.REDSTONE_BLOCK) && matrix[2].getType().equals(Material.REDSTONE_BLOCK)
					&& matrix[6].getType().equals(Material.REDSTONE_BLOCK) && matrix[8].getType().equals(Material.REDSTONE_BLOCK)
					&& matrix[1].getType().equals(Material.BLAZE_ROD) && matrix[3].getType().equals(Material.BLAZE_ROD)
					&& matrix[5].getType().equals(Material.BLAZE_ROD) && matrix[7].getType().equals(Material.BLAZE_ROD)
					&& matrix[4] != null && matrix[4].getType().equals(Material.NETHER_STAR))
			{
				if(matrix[1].hasItemMeta() && matrix[3].hasItemMeta() && matrix[5].hasItemMeta() && matrix[7].hasItemMeta()
					&& matrix[1].getItemMeta().hasLore() && matrix[3].getItemMeta().hasLore() && matrix[5].getItemMeta().hasLore() && matrix[7].getItemMeta().hasLore()
					&& ( Utils.contains_all_of(matrix[1].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "помп", "обычн") || Utils.contains_all_of(matrix[1].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "pump", "regular") )
					&& ( Utils.contains_all_of(matrix[3].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "помп", "обычн") || Utils.contains_all_of(matrix[3].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "pump", "regular") )
					&& ( Utils.contains_all_of(matrix[5].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "помп", "обычн") || Utils.contains_all_of(matrix[5].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "pump", "regular") )
					&& ( Utils.contains_all_of(matrix[7].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "помп", "обычн") || Utils.contains_all_of(matrix[7].getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH), "pump", "regular") ))
				{
					
				}
				else
				{
					ci.setResult(new ItemStack(Material.AIR));
				}
			}
			//Storages
			else if(matrix[0].getType().equals(Material.OBSIDIAN) && matrix[2].getType().equals(Material.OBSIDIAN)
					&& matrix[6].getType().equals(Material.OBSIDIAN) && matrix[8].getType().equals(Material.OBSIDIAN)
					/*I tried to prevent buggy colored shulker box crafting, but event doesn't fire on craft, because it wasn't added above,
					 * but craft exists, I think, because of Material.LEGACY_SHULKER_BOX (thx to spigot team?)
					&& Utils.is_shulker_box(matrix[1].getType()) && Utils.is_shulker_box(matrix[3].getType())
					&& Utils.is_shulker_box(matrix[5].getType()) && Utils.is_shulker_box(matrix[7].getType())*/
					&& matrix[4] != null) {
				if(Utils.is_colored_shulker_box(matrix[1].getType()) || Utils.is_colored_shulker_box(matrix[3].getType())
					|| Utils.is_colored_shulker_box(matrix[5].getType()) || Utils.is_colored_shulker_box(matrix[7].getType()))
					ci.setResult(null);
				//is shulkerboxes empty
				if(isShulkerBoxEmpty(matrix[1]) && isShulkerBoxEmpty(matrix[3]) && isShulkerBoxEmpty(matrix[5]) && isShulkerBoxEmpty(matrix[7])) {
					//banned items
					if( !Utils.is_shulker_box(matrix[4].getType()) ) {
						ItemStack craft = new ItemStack(Material.FIREWORK_STAR, 1);
						craft.setData(matrix[4].getData());
						craft = setStorageID(craft, StoragesFileManager.nextID);
						if(matrix[4].getType() == Material.ENDER_PEARL) {
							craft.setItemMeta(storage_meta_bottomless.clone());
						}
						else if(matrix[4].getType() == Material.ENDER_EYE) {
							craft.setItemMeta(storage_meta_multitype.clone());
						}
						else return;
						ci.setResult(craft);
					}
					
				}
			}
		}
		else {
			//Storage
			
			//borsch
			if(ci.getResult() != null && ci.getResult().getType() == Material.BEETROOT_SOUP) {
				boolean have_beetroot = false, have_bowl = false;
				for(int i = 0; i < matrix.length; i++) {
					if(matrix[i] != null)
						if(matrix[i].getType() == Material.BOWL)
							if(have_bowl) {
								have_bowl = false;
								break;
							}
							else 
								have_bowl = true;
						else if(matrix[i].getType() == Material.BEETROOT)
							if(!have_beetroot && matrix[i].getAmount() >= 6) {
								have_beetroot = true;
							}
							else {
								have_beetroot = false;
								break;
							}
				}
				if(!have_beetroot || !have_bowl) {
					ItemStack craft = new ItemStack(Material.AIR, 1);
					ci.setResult(craft);
				}
			}
			//else if()
			
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST/*, ignoreCancelled = true*/)
    public void onCraft(CraftItemEvent event) {
		
		if(event.isCancelled())
			return;

		ItemStack craft_result = event.getCurrentItem();
		if( Storage.getID(craft_result) >= 0 ) {
			int id = StoragesFileManager.nextID++;
			craft_result = setStorageID(craft_result, id);
			Storage st;
			if(craft_result.getEnchantmentLevel(Enchantment.ARROW_INFINITE) > 0)
				st = new Storage(id, plugin.mainworld.getFullTime(), Storage.UNDEFINED_MATERIAL);
			else if(craft_result.getEnchantmentLevel(Enchantment.DIG_SPEED) > 0)
				st = new Storage(id, plugin.mainworld.getFullTime());
			else {
				event.setCancelled(true);
				return;
			}
			
			st.saveToFile();
			
			event.setCurrentItem(craft_result);
		}
		else if(craft_result.getType() == Material.BEETROOT_SOUP) {
			ItemStack[] matrix = event.getInventory().getMatrix();

			//borsch
			//if(matrix.length <= 4) {
				int index_beetroot = -1707, index_bowl = -1707;
				for(int i = 0; i < matrix.length; i++) {
					if(matrix[i] != null)
						if(matrix[i].getType() == Material.BOWL)
							if(index_bowl >= 0) {
								index_bowl = -1707;
								break;
							}
							else 
								index_bowl = i;
						else if(matrix[i].getType() == Material.BEETROOT)
							if(index_beetroot == -1707 && matrix[i].getAmount() >= 6) {
								index_beetroot = i;
							}
							else {
								index_beetroot = -1707;
								break;
							}
				}
				if(index_beetroot >= 0 && index_bowl >= 0) {
					if(event.isShiftClick()) {
						ItemStack[] player_inv = event.getWhoClicked().getInventory().getStorageContents();
						int max_crafts = Math.min(matrix[index_beetroot].getAmount()/6, matrix[index_bowl].getAmount());
						int empty_count = 0;
						for(int i = 0; i < player_inv.length; i++) {
							if(player_inv[i] == null) {
								player_inv[i] = new ItemStack(Material.BEETROOT_SOUP, 1);
								empty_count++;
								if(empty_count >= max_crafts)
									break;
							}
						}
						event.setCancelled(true);
						matrix[index_bowl].setAmount(matrix[index_bowl].getAmount()-empty_count);
						matrix[index_beetroot].setAmount(matrix[index_beetroot].getAmount()-6*empty_count);
						event.getInventory().setMatrix(matrix);
						event.getWhoClicked().getInventory().setStorageContents(player_inv);
					}
					else {
						matrix[index_beetroot].setAmount(matrix[index_beetroot].getAmount()-5);
						event.getInventory().setMatrix(matrix);
					}
				}
				else
					event.setCancelled(true);
			//}
		}
		else if(SoulStone.isSoulStone(craft_result)) {
			if(event.isShiftClick()) {
				ItemStack[] player_inv = event.getWhoClicked().getInventory().getStorageContents();
				System.out.println("CRAFT COUNT: "+craft_result.getAmount());
				int max_crafts = 64;
				ItemStack[] matrix = event.getInventory().getMatrix();
				for(ItemStack is : matrix)
					if(is.getAmount() < max_crafts)
						max_crafts = is.getAmount();
				int empty_count = 0;
				craft_result.setAmount(1);
				for(int i = 0; i < player_inv.length; i++) {
					if(player_inv[i] == null) {
						player_inv[i] = SoulStone.setSoulStoneMeta(new ItemStack(craft_result));
						empty_count++;
						if(empty_count >= max_crafts)
							break;
					}
				}
				event.getWhoClicked().getInventory().setStorageContents(player_inv);
				event.getInventory().setResult(new ItemStack(Material.AIR));
			}
			else
				event.getInventory().setResult(SoulStone.setSoulStoneMeta(event.getInventory().getResult()));
		}
	}
	
	private boolean isShulkerBoxEmpty(ItemStack sb) {
		ItemStack[] inv = ((ShulkerBox)((BlockStateMeta) sb.getItemMeta()).getBlockState()).getInventory().getContents();
		for(ItemStack is : inv)
			if(is != null)
				return false;
		return true;
	}
	
	@Deprecated
	//I can't understand how different planks can be used in vanilla
	private void printSomeVanillaCrafts() {
    	Iterator<Recipe> recipes = Bukkit.recipeIterator();
    	while(recipes.hasNext()) {
    		Recipe recipe = recipes.next();
    		if(recipe instanceof ShapedRecipe) {
    			if(recipe.getResult().getType().equals(Material.CHEST)) {
    				ShapedRecipe chest_recipe = (ShapedRecipe) recipe;
    				System.out.println(chest_recipe.getShape() +" "+ chest_recipe.getIngredientMap());
    				for(int i=0; i < chest_recipe.getIngredientMap().size(); i++) {
    					chest_recipe.getIngredientMap().values();
    				}
    			}
    		}
    		
    	}
	}
	
	private void addFurnaceCrafts() {
		//remove vanilla recipes
    	Iterator<Recipe> recipes = Bukkit.recipeIterator();
    	while(recipes.hasNext()) {
    		Recipe recipe = recipes.next();
    		if(recipe instanceof FurnaceRecipe) {
    			if(recipe.getResult().getType().equals(Material.IRON_NUGGET) || recipe.getResult().getType().equals(Material.GOLD_NUGGET) )
    				recipes.remove();
    		}
    		
    	}
    	//add our armor smelting recipes
    	FurnaceRecipe nuggets_from_armor_IronBoots = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,4), Material.IRON_BOOTS );
    	server.addRecipe(nuggets_from_armor_IronBoots);
    	FurnaceRecipe nuggets_from_armor_IronLeggings = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,7), Material.IRON_LEGGINGS );
    	server.addRecipe(nuggets_from_armor_IronLeggings);
    	FurnaceRecipe nuggets_from_armor_IronChestplate = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,8), Material.IRON_CHESTPLATE );
    	server.addRecipe(nuggets_from_armor_IronChestplate);
    	FurnaceRecipe nuggets_from_armor_IronHelmet = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,5), Material.IRON_HELMET );
    	server.addRecipe(nuggets_from_armor_IronHelmet);

    	FurnaceRecipe nuggets_from_armor_GoldBoots = new FurnaceRecipe(new ItemStack(Material.GOLD_NUGGET,4), Material.GOLDEN_BOOTS );
    	server.addRecipe(nuggets_from_armor_GoldBoots);
    	FurnaceRecipe nuggets_from_armor_GoldLeggings = new FurnaceRecipe(new ItemStack(Material.GOLD_NUGGET,7), Material.GOLDEN_LEGGINGS );
    	server.addRecipe(nuggets_from_armor_GoldLeggings);
    	FurnaceRecipe nuggets_from_armor_GoldChestplate = new FurnaceRecipe(new ItemStack(Material.GOLD_NUGGET,8), Material.GOLDEN_CHESTPLATE );
    	server.addRecipe(nuggets_from_armor_GoldChestplate);
    	FurnaceRecipe nuggets_from_armor_GoldHelmet = new FurnaceRecipe(new ItemStack(Material.GOLD_NUGGET,5), Material.GOLDEN_HELMET );
    	server.addRecipe(nuggets_from_armor_GoldHelmet);

    	FurnaceRecipe nuggets_from_armor_ChainmailBoots = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,4), Material.CHAINMAIL_BOOTS );
    	server.addRecipe(nuggets_from_armor_ChainmailBoots);
    	FurnaceRecipe nuggets_from_armor_ChainmailLeggings = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,7), Material.CHAINMAIL_LEGGINGS );
    	server.addRecipe(nuggets_from_armor_ChainmailLeggings);
    	FurnaceRecipe nuggets_from_armor_ChainmailChestplate = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,8), Material.CHAINMAIL_CHESTPLATE );
    	server.addRecipe(nuggets_from_armor_ChainmailChestplate);
    	FurnaceRecipe nuggets_from_armor_ChainmailHelmet = new FurnaceRecipe(new ItemStack(Material.IRON_NUGGET,5), Material.CHAINMAIL_HELMET );
    	server.addRecipe(nuggets_from_armor_ChainmailHelmet);
	}
	
	private void addStairsAndSlabsCrafts() {
    	Material[] stairs_to_blocks = {Material.ACACIA_STAIRS, Material.BIRCH_STAIRS, Material.BRICK_STAIRS, Material.COBBLESTONE_STAIRS,
    			Material.DARK_OAK_STAIRS, Material.JUNGLE_STAIRS, Material.NETHER_BRICK_STAIRS, Material.PURPUR_STAIRS,
    			Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.SANDSTONE_STAIRS, Material.STONE_BRICK_STAIRS,
    			Material.SPRUCE_STAIRS, Material.OAK_STAIRS};
    	Material[] slabs_to_blocks = {Material.ACACIA_SLAB, Material.BIRCH_SLAB, Material.BRICK_SLAB, Material.COBBLESTONE_SLAB,
    			Material.DARK_OAK_SLAB, Material.JUNGLE_SLAB, Material.NETHER_BRICK_SLAB, Material.PURPUR_SLAB,
    			Material.QUARTZ_SLAB, Material.RED_SANDSTONE_SLAB, Material.SANDSTONE_SLAB, Material.STONE_BRICK_SLAB,
    			Material.SPRUCE_SLAB, Material.OAK_SLAB};
    			//Material.STONE_SLAB};
    	Material[] blocks_from_stairs = {Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.BRICK, Material.COBBLESTONE,
    			Material.DARK_OAK_PLANKS, Material.JUNGLE_PLANKS, Material.NETHER_BRICKS, Material.PURPUR_BLOCK, 
    			Material.QUARTZ_BLOCK, Material.RED_SANDSTONE, Material.SANDSTONE, Material.STONE_BRICKS, 
    			Material.SPRUCE_PLANKS, Material.OAK_PLANKS};
    			//Material.SMOOTH_STONE};
    	for(int i=0; i < stairs_to_blocks.length; i++) {
        	NamespacedKey temp_key = new NamespacedKey(plugin, "4stairs-"+gen_key_from_material(stairs_to_blocks[i]));
        	ShapelessRecipe temp_recipe = new ShapelessRecipe(temp_key, new ItemStack(blocks_from_stairs[i], 6) );
        	temp_recipe.addIngredient(4, stairs_to_blocks[i]);
    		server.addRecipe(temp_recipe);
    	}
    	//2 slabs -> 1 block (except quartz, sandstoneS, stone bricks, purpur)
    	for(int i=0; i < slabs_to_blocks.length; i++) {
    		if( slabs_to_blocks[i] != Material.STONE_BRICK_SLAB && slabs_to_blocks[i] != Material.QUARTZ_SLAB
    				&& slabs_to_blocks[i] != Material.SANDSTONE_SLAB && slabs_to_blocks[i] != Material.RED_SANDSTONE_SLAB
    				&& slabs_to_blocks[i] != Material.PURPUR_SLAB) {
            	NamespacedKey temp_key = new NamespacedKey(plugin, "2slab-"+gen_key_from_material(slabs_to_blocks[i]));
            	ShapelessRecipe temp_recipe = new ShapelessRecipe(temp_key, new ItemStack(blocks_from_stairs[i], 1) );
            	temp_recipe.addIngredient(2, slabs_to_blocks[i]);
        		server.addRecipe(temp_recipe);
    		}
    	}
    	//4 slabs -> 1 block
    	for(int i=0; i < slabs_to_blocks.length; i++) {
        	NamespacedKey temp_key = new NamespacedKey(plugin, "4slab-"+gen_key_from_material(slabs_to_blocks[i]));
        	ShapelessRecipe temp_recipe = new ShapelessRecipe(temp_key, new ItemStack(blocks_from_stairs[i], 2) );
        	temp_recipe.addIngredient(4, slabs_to_blocks[i]);
    		server.addRecipe(temp_recipe);
    	}
    	//6 slabs -> 1 block
    	for(int i=0; i < slabs_to_blocks.length; i++) {
        	NamespacedKey temp_key = new NamespacedKey(plugin, "6slab-"+gen_key_from_material(slabs_to_blocks[i]));
        	ShapelessRecipe temp_recipe = new ShapelessRecipe(temp_key, new ItemStack(blocks_from_stairs[i], 3) );
        	temp_recipe.addIngredient(6, slabs_to_blocks[i]);
    		server.addRecipe(temp_recipe);
    	}
	}
	
	private String gen_key_from_material(Material m) {
		int index = m.toString().lastIndexOf("_");
		return m.toString().substring(0, index < 0 ? m.toString().length() : index);
	}
	
	/*public static ItemStack addAttributes(ItemStack i, int ID){
        net.minecraft.server.v1_12_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        NBTTagList modifiers = new NBTTagList();
        NBTTagCompound attr = new NBTTagCompound();
        attr.set("AttributeName", new NBTTagString("generic.luck"));
        attr.set("Name", new NBTTagString("generic.luck"));
        attr.set("Amount", new NBTTagInt(ID));
        attr.set("Operation", new NBTTagInt(0));
        attr.set("UUIDLeast", new NBTTagInt(894654));
        attr.set("UUIDMost", new NBTTagInt(2872));
        modifiers.add(attr);
        compound.set("AttributeModifiers", modifiers);
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
    }*/
	
	//may be this must be in Storage class with isStorage() and getID()?
	public static ItemStack setStorageID(ItemStack i, int ID) {
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        //it guarantee not to stack
        compound.setInt("StorageID", ID);
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
}
