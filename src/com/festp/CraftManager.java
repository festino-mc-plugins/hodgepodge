package com.festp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.utils.NbtUtils;
import com.festp.utils.UtilsType;

public class CraftManager implements Listener {
	public enum CraftTag { KEEP_DATA, ONLY_SPECIFIC };
	
	Server server;
	Main plugin;
	
	List<NamespacedKey> recipe_keys = new ArrayList<>();
	
	public CraftManager(Main plugin, Server server) {
		this.plugin = plugin;
		this.server = server;
	}
	
	public void addCrafts() {
		addMainCrafts();
		addFurnaceCrafts();
		addStairsAndSlabsCrafts();
	}
	
	private void giveRecipe(Player p, String recipe) {
		Bukkit.getServer().dispatchCommand(p, "recipe give "+p.getName()+" "+recipe);
	}
	private void giveOwnRecipe(Player p, String recipe) {
		giveRecipe(p, plugin.getName().toLowerCase()+":"+recipe);
	}
	private void giveRecipe(HumanEntity player, NamespacedKey key) {
		player.discoverRecipe(key);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		for(NamespacedKey recipe_name : recipe_keys) {
			giveRecipe(p, recipe_name);
		}
	}
	
	public boolean addCraftbookRecipe(NamespacedKey key) {
		if (recipe_keys.contains(key))
			return false;
		recipe_keys.add(key);
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void addMainCrafts() {
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
		String name___borsch_1 = "borsch";
		String name___chorus_1 = "chorus_from_flower";
		String name___stick_1 = "stick_from_arrows";
		String name___smooth_sandstone_1 = "smooth_ss_from_ss";
		String name___smooth_redsandstone_1 = "smooth_redss_from_redss";
		String name___lead_3x = "lead_3x";
		String name___glass_item_frame = "glass_item_frame";

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
    	NamespacedKey key___borsch = new NamespacedKey(plugin, name___borsch_1);
    	NamespacedKey key___chorus = new NamespacedKey(plugin, name___chorus_1);
    	NamespacedKey key___stick_1 = new NamespacedKey(plugin, name___stick_1);
    	NamespacedKey key___smooth_sandstone = new NamespacedKey(plugin, name___smooth_sandstone_1);
    	NamespacedKey key___smooth_redsandstone = new NamespacedKey(plugin, name___smooth_redsandstone_1);
    	NamespacedKey key___lead_3x = new NamespacedKey(plugin, name___lead_3x);
    	NamespacedKey key___glass_item_frame = new NamespacedKey(plugin, name___glass_item_frame);
		recipe_keys.add(key___torch);
		recipe_keys.add(key___redsand);
		recipe_keys.add(key___clay_1);
		recipe_keys.add(key___clay_2);
		recipe_keys.add(key___sand_1);
		recipe_keys.add(key___redsand_1);
		recipe_keys.add(key___dragonegg);
		recipe_keys.add(key___grass);
		recipe_keys.add(key___mycel_1);
		recipe_keys.add(key___mycel_2);
		recipe_keys.add(key___borsch);
		recipe_keys.add(key___chorus);
		recipe_keys.add(key___stick_1);
		recipe_keys.add(key___smooth_sandstone);
		recipe_keys.add(key___smooth_redsandstone);
		recipe_keys.add(key___lead_3x); // TO DO: add recipes(keys and etc by name) in loop, pairs "name:Recipe"
		recipe_keys.add(key___glass_item_frame);
		
    	ShapelessRecipe torch_from_fireball = new ShapelessRecipe(key___torch, new ItemStack(Material.TORCH, 16) );
    	torch_from_fireball.addIngredient(1, Material.FIRE_CHARGE);
    	torch_from_fireball.addIngredient(1, Material.STICK);
		server.addRecipe(torch_from_fireball);
		
    	ShapedRecipe redsand_from_sand_and_redstone = new ShapedRecipe(key___redsand, new ItemStack(Material.RED_SAND, 8) );
    	redsand_from_sand_and_redstone.shape(new String[]{"SSS", "SRS", "SSS"});
    	redsand_from_sand_and_redstone.setIngredient('S', Material.SAND);
    	redsand_from_sand_and_redstone.setIngredient('R', Material.REDSTONE);
    	server.addRecipe(redsand_from_sand_and_redstone);
    	
    	ShapelessRecipe clay1 = new ShapelessRecipe(key___clay_1, new ItemStack(Material.CLAY_BALL, 8) );
    	clay1.addIngredient(1, Material.IRON_NUGGET);
    	clay1.addIngredient(4, Material.SAND);
    	clay1.addIngredient(4, Material.FLINT);
		server.addRecipe(clay1);
    	
    	ShapelessRecipe clay2 = new ShapelessRecipe(key___clay_2, new ItemStack(Material.CLAY_BALL, 8) );
    	clay2.addIngredient(1, Material.IRON_NUGGET);
    	clay2.addIngredient(4, Material.SAND);
    	clay2.addIngredient(4, Material.GRAVEL);
		server.addRecipe(clay2);

    	ShapelessRecipe sand_from_sandstone = new ShapelessRecipe(key___sand_1, new ItemStack(Material.SAND, 4, (short)0) );
    	sand_from_sandstone.addIngredient(1, Material.SANDSTONE);
		server.addRecipe(sand_from_sandstone);

    	ShapelessRecipe redsand_from_redsandstone = new ShapelessRecipe(key___redsand_1, new ItemStack(Material.SAND, 4,(short)1) );
    	redsand_from_redsandstone.addIngredient(1, Material.RED_SANDSTONE);
		server.addRecipe(redsand_from_redsandstone);

    	ShapedRecipe dragon_egg_from_dragon_egg = new ShapedRecipe(key___dragonegg, new ItemStack(Material.DRAGON_EGG, 2) );
    	dragon_egg_from_dragon_egg.shape(new String[]{"OOO", "ODO", "OOO"});
    	dragon_egg_from_dragon_egg.setIngredient('O', Material.OBSIDIAN);
    	dragon_egg_from_dragon_egg.setIngredient('D', Material.DRAGON_EGG);
    	server.addRecipe(dragon_egg_from_dragon_egg);

    	ShapelessRecipe grass_from_dirt = new ShapelessRecipe(key___grass, new ItemStack(Material.GRASS_BLOCK, 1) );
    	grass_from_dirt.addIngredient(1, Material.DIRT);
    	grass_from_dirt.addIngredient(1, Material.BONE_MEAL);
    	server.addRecipe(grass_from_dirt);

    	ShapelessRecipe mycel_from_dirt1 = new ShapelessRecipe(key___mycel_1, new ItemStack(Material.MYCELIUM, 1) );
    	mycel_from_dirt1.addIngredient(1, Material.DIRT);
    	mycel_from_dirt1.addIngredient(1, Material.BROWN_MUSHROOM);
    	server.addRecipe(mycel_from_dirt1);

    	ShapelessRecipe mycel_from_dirt2 = new ShapelessRecipe(key___mycel_2, new ItemStack(Material.MYCELIUM, 1) );
    	mycel_from_dirt2.addIngredient(1, Material.DIRT);
    	mycel_from_dirt2.addIngredient(1, Material.RED_MUSHROOM);
    	server.addRecipe(mycel_from_dirt2);
    	
    	

    	ShapelessRecipe pocket_borsch = new ShapelessRecipe(key___borsch, new ItemStack(Material.BEETROOT_SOUP, 1) );
    	pocket_borsch.addIngredient(1, Material.BEETROOT);
    	pocket_borsch.addIngredient(1, Material.BOWL);
    	server.addRecipe(pocket_borsch);

    	ShapelessRecipe chorus_from_flower = new ShapelessRecipe(key___chorus, new ItemStack(Material.CHORUS_FRUIT, 10) );
    	chorus_from_flower.addIngredient(1, Material.CHORUS_FLOWER);
    	server.addRecipe(chorus_from_flower);

    	ShapelessRecipe stick_from_arrows = new ShapelessRecipe(key___stick_1, new ItemStack(Material.STICK, 1) );
    	stick_from_arrows.addIngredient(4, Material.ARROW);
    	server.addRecipe(stick_from_arrows);

    	ShapelessRecipe smooth_sandstone_from_andstone = new ShapelessRecipe(key___smooth_sandstone, new ItemStack(Material.SMOOTH_SANDSTONE, 4) );
    	smooth_sandstone_from_andstone.addIngredient(4, Material.CUT_SANDSTONE);
    	server.addRecipe(smooth_sandstone_from_andstone);

    	ShapelessRecipe smooth_redsandstone_from_andstone = new ShapelessRecipe(key___smooth_redsandstone, new ItemStack(Material.SMOOTH_RED_SANDSTONE, 4) );
    	smooth_redsandstone_from_andstone.addIngredient(4, Material.CUT_RED_SANDSTONE);
    	server.addRecipe(smooth_redsandstone_from_andstone);
    	
    	ShapedRecipe glass_item_frame = new ShapedRecipe(key___glass_item_frame, getInvisibleItemFrame());
    	glass_item_frame.shape(new String[]{"SSS", "SGS", "SSS"});
    	glass_item_frame.setIngredient('S', Material.STRING);
    	glass_item_frame.setIngredient('G', Material.GLASS_PANE);
    	server.addRecipe(glass_item_frame);
	}
	
	// test necessary item tags (in craft grid)
	@EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event)
	{
		CraftingInventory ci = event.getInventory();
		ItemStack[] matrix = ci.getMatrix();
		
		//borsch
		if (ci.getResult() != null && ci.getResult().getType() == Material.BEETROOT_SOUP) {
			boolean have_beetroot = false;
			int bowl_count = 0, beetroot_stacks_count = 0;
			for (int i = 0; i < matrix.length; i++) {
				if (matrix[i] != null)
					if (matrix[i].getType() == Material.BOWL)
						bowl_count++;
					else if (matrix[i].getType() == Material.BEETROOT) {
						beetroot_stacks_count++;
						if (matrix[i].getAmount() >= 6)
							have_beetroot = true;
						else if (beetroot_stacks_count != 1)
							break;
					}
			}
			if (bowl_count == 1 && beetroot_stacks_count == 1 && !have_beetroot) {
				ci.setResult(null);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST/*, ignoreCancelled = true*/)
    public void onCraft(CraftItemEvent event)
	{
		if(event.isCancelled())
			return;

		ItemStack[] matrix = event.getInventory().getMatrix();
		ItemStack craftResult = event.getCurrentItem();
		
		if (craftResult.getType() == Material.BEETROOT_SOUP) {
			// borsch
			int index_beetroot = -1707, index_bowl = -1707;
			int countBeetrootSlots = 0, countBowl = 0;
			for (int i = 0; i < matrix.length; i++) {
				if (matrix[i] != null)
					if (matrix[i].getType() == Material.BOWL)
					{
						countBowl++;
						if (index_bowl < 0)
							index_bowl = i;
						else 
							break;
					}
					else if (matrix[i].getType() == Material.BEETROOT)
					{
						countBeetrootSlots++;
						if (index_beetroot < 0 && matrix[i].getAmount() >= 6)
							index_beetroot = i;
						else if (countBeetrootSlots != 1)
							break;
					}
			}
			if (countBowl == 1 && countBeetrootSlots == 1)
			{
				if (index_beetroot >= 0)
				{
					if (event.isShiftClick()) {
						ItemStack[] playerInv = event.getWhoClicked().getInventory().getStorageContents();
						int max_crafts = Math.min(matrix[index_beetroot].getAmount()/6, matrix[index_bowl].getAmount());
						int empty_count = 0;
						for (int i = 0; i < playerInv.length; i++) {
							if (playerInv[i] == null) {
								playerInv[i] = new ItemStack(Material.BEETROOT_SOUP, 1);
								empty_count++;
								if (empty_count >= max_crafts)
									break;
							}
						}
						event.setCancelled(true);
						matrix[index_bowl].setAmount(matrix[index_bowl].getAmount()-empty_count);
						matrix[index_beetroot].setAmount(matrix[index_beetroot].getAmount()-6*empty_count);
						event.getInventory().setMatrix(matrix);
						event.getWhoClicked().getInventory().setStorageContents(playerInv);
					}
					else {
						matrix[index_beetroot].setAmount(matrix[index_beetroot].getAmount()-5);
						event.getInventory().setMatrix(matrix);
					}
				}
				else {
					event.setCancelled(true);
				}
			}
		}
	}
	
	private void addFurnaceCrafts() {
		// remove vanilla recipes
    	Iterator<Recipe> recipes = Bukkit.recipeIterator();
    	while (recipes.hasNext()) {
    		Recipe recipe = recipes.next();
    		if (recipe instanceof FurnaceRecipe) {
    			Material input = ((FurnaceRecipe) recipe).getInput().getType();
    			if (UtilsType.isArmor(input) || UtilsType.isTool(input) || UtilsType.isWeapon(input))
    				if (recipe.getResult().getType().equals(Material.IRON_NUGGET) || recipe.getResult().getType().equals(Material.GOLD_NUGGET) )
    					recipes.remove();
    		}
    	}
    	// add our armor smelting recipes
    	Material[] inputItems = {
    			Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET,
    			Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET,
    			Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET,
    			Material.IRON_AXE, Material.IRON_HOE, Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_SWORD,
    			Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_SWORD};
    	Material[] outputItems = {
    			Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET,
    			Material.GOLD_NUGGET, Material.GOLD_NUGGET, Material.GOLD_NUGGET, Material.GOLD_NUGGET,
    			Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET,
    			Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET, Material.IRON_NUGGET,
    			Material.GOLD_NUGGET, Material.GOLD_NUGGET, Material.GOLD_NUGGET, Material.GOLD_NUGGET, Material.GOLD_NUGGET};
    	int[] outputCounts = {
    			4, 7, 8, 5,
    			4, 7, 8, 5,
    			4, 7, 8, 5,
    			3, 2, 3, 1, 2,
    			3, 2, 3, 1, 2};
    	for (int i = 0; i < inputItems.length; i++)
    	{
    		ItemStack output_stack = new ItemStack(outputItems[i], outputCounts[i]);
    		String key = "furnace_" + inputItems[i].toString().toLowerCase();
    		NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
    		float exp = outputCounts[i];
    		int time = (outputCounts[i] + 10) * 20 / 2;
    		FurnaceRecipe nuggets_recipe = new FurnaceRecipe(namespacedKey, output_stack, inputItems[i], exp, time);
        	server.addRecipe(nuggets_recipe);
    	}
	}
	
	private void addStairsAndSlabsCrafts() {
    	Material[] stairsToBlocks = { Material.BRICK_STAIRS, Material.STONE_STAIRS,
    			Material.ACACIA_STAIRS, Material.BIRCH_STAIRS, Material.DARK_OAK_STAIRS, Material.JUNGLE_STAIRS, Material.SPRUCE_STAIRS, Material.OAK_STAIRS,
    			Material.NETHER_BRICK_STAIRS, Material.RED_NETHER_BRICK_STAIRS, Material.PURPUR_STAIRS, Material.END_STONE_BRICK_STAIRS,
    			Material.DARK_PRISMARINE_STAIRS, Material.PRISMARINE_BRICK_STAIRS, Material.PRISMARINE_STAIRS,
    			Material.COBBLESTONE_STAIRS, Material.STONE_BRICK_STAIRS,
    			Material.MOSSY_COBBLESTONE_STAIRS, Material.MOSSY_STONE_BRICK_STAIRS,
    			Material.ANDESITE_STAIRS, Material.GRANITE_STAIRS, Material.DIORITE_STAIRS,
    			Material.POLISHED_ANDESITE_STAIRS, Material.POLISHED_DIORITE_STAIRS, Material.POLISHED_GRANITE_STAIRS,
    			Material.QUARTZ_STAIRS, Material.RED_SANDSTONE_STAIRS, Material.SANDSTONE_STAIRS,
    			Material.SMOOTH_QUARTZ_STAIRS, Material.SMOOTH_RED_SANDSTONE_STAIRS, Material.SMOOTH_SANDSTONE_STAIRS };
    	Material[] slabsToBlocks = { Material.BRICK_SLAB, Material.STONE_SLAB,
    			Material.ACACIA_SLAB, Material.BIRCH_SLAB, Material.DARK_OAK_SLAB, Material.JUNGLE_SLAB, Material.SPRUCE_SLAB, Material.OAK_SLAB,
    			Material.NETHER_BRICK_SLAB, Material.RED_NETHER_BRICK_SLAB, Material.PURPUR_SLAB, Material.END_STONE_BRICK_SLAB,
    			Material.DARK_PRISMARINE_SLAB, Material.PRISMARINE_BRICK_SLAB, Material.PRISMARINE_SLAB,
    			Material.COBBLESTONE_SLAB, Material.STONE_BRICK_SLAB,
    			Material.MOSSY_COBBLESTONE_SLAB, Material.MOSSY_STONE_BRICK_SLAB,
    			Material.ANDESITE_SLAB, Material.GRANITE_SLAB, Material.DIORITE_SLAB,
    			Material.POLISHED_ANDESITE_SLAB, Material.POLISHED_DIORITE_SLAB, Material.POLISHED_GRANITE_SLAB,
    			Material.QUARTZ_SLAB, Material.RED_SANDSTONE_SLAB, Material.SANDSTONE_SLAB,
    			Material.SMOOTH_QUARTZ_SLAB, Material.SMOOTH_RED_SANDSTONE_SLAB, Material.SMOOTH_SANDSTONE_SLAB };
    	Material[] blocksFromPartial = { Material.BRICKS, Material.STONE,
    			Material.ACACIA_PLANKS, Material.BIRCH_PLANKS, Material.DARK_OAK_PLANKS, Material.JUNGLE_PLANKS, Material.SPRUCE_PLANKS, Material.OAK_PLANKS,
    			Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS, Material.PURPUR_BLOCK, Material.END_STONE_BRICKS,
    			Material.DARK_PRISMARINE, Material.PRISMARINE_BRICKS, Material.PRISMARINE,
    			Material.COBBLESTONE, Material.STONE_BRICKS,
    			Material.MOSSY_COBBLESTONE, Material.MOSSY_STONE_BRICKS,
    			Material.ANDESITE, Material.GRANITE, Material.DIORITE,
    			Material.POLISHED_ANDESITE, Material.POLISHED_DIORITE, Material.POLISHED_GRANITE,
    			Material.QUARTZ_BLOCK, Material.RED_SANDSTONE, Material.SANDSTONE,
    			Material.SMOOTH_QUARTZ, Material.SMOOTH_RED_SANDSTONE, Material.SMOOTH_SANDSTONE };
    	// 1 stair -> 1 block
    	for (int i = 0; i < stairsToBlocks.length; i++) {
        	NamespacedKey temp_key = new NamespacedKey(plugin, "1stairs-"+gen_key_from_material(stairsToBlocks[i]));
        	ShapelessRecipe tempRecipe = new ShapelessRecipe(temp_key, new ItemStack(blocksFromPartial[i], 1) );
        	tempRecipe.addIngredient(1, stairsToBlocks[i]);
    		server.addRecipe(tempRecipe);
    	}
    	// 2 slabs -> 1 block (except quartz, sandstoneS, stone bricks, purpur)
    	for (int i = 0; i < slabsToBlocks.length; i++) {
    		if ( slabsToBlocks[i] != Material.STONE_BRICK_SLAB && slabsToBlocks[i] != Material.QUARTZ_SLAB
    				&& slabsToBlocks[i] != Material.SANDSTONE_SLAB && slabsToBlocks[i] != Material.RED_SANDSTONE_SLAB
    				&& slabsToBlocks[i] != Material.PURPUR_SLAB && slabsToBlocks[i] != Material.NETHER_BRICK_SLAB) {
            	NamespacedKey tempKey = new NamespacedKey(plugin, "2slab-"+gen_key_from_material(slabsToBlocks[i]));
            	ShapelessRecipe tempRecipe = new ShapelessRecipe(tempKey, new ItemStack(blocksFromPartial[i], 1) );
            	tempRecipe.addIngredient(2, slabsToBlocks[i]);
        		server.addRecipe(tempRecipe);
    		}
    	}
    	// 4 slabs -> 1 block
    	for (int i = 0; i < slabsToBlocks.length; i++) {
        	NamespacedKey tempKey = new NamespacedKey(plugin, "4slab-"+gen_key_from_material(slabsToBlocks[i]));
        	ShapelessRecipe tempRecipe = new ShapelessRecipe(tempKey, new ItemStack(blocksFromPartial[i], 2) );
        	tempRecipe.addIngredient(4, slabsToBlocks[i]);
    		server.addRecipe(tempRecipe);
    	}
    	// 6 slabs -> 1 block
    	for (int i = 0; i < slabsToBlocks.length; i++) {
        	NamespacedKey tempKey = new NamespacedKey(plugin, "6slab-"+gen_key_from_material(slabsToBlocks[i]));
        	ShapelessRecipe tempRecipe = new ShapelessRecipe(tempKey, new ItemStack(blocksFromPartial[i], 3) );
        	tempRecipe.addIngredient(6, slabsToBlocks[i]);
    		server.addRecipe(tempRecipe);
    	}
	}
	
	public static ItemStack getInvisibleItemFrame() { // TODO add function to get any special plugin item
    	ItemStack glassItemFrame = new ItemStack(Material.ITEM_FRAME, 1);
    	ItemMeta meta = glassItemFrame.getItemMeta();
    	//meta.setDisplayName("Glass Item Frame");
    	meta.setLocalizedName("Стеклянная рамка");
    	glassItemFrame.setItemMeta(meta);
    	return NbtUtils.setInvisibleEntity(glassItemFrame);
	}
	
	private String gen_key_from_material(Material m) {
		int index = m.toString().lastIndexOf("_");
		return m.toString().substring(0, index < 0 ? m.toString().length() : index);
	}
}
