package com.festp.storages;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.utils.Utils;

public class ItemStack_SortV implements Comparable {
	private static final Material[] SECTION_BEGINNINGS = {
			Material.STONE,
			Material.OAK_SAPLING,
			Material.DISPENSER,
			Material.POWERED_RAIL,
			Material.BEACON,
			Material.APPLE,
			Material.IRON_SHOVEL,
			Material.TURTLE_HELMET,
			Material.GHAST_TEAR};
	private static boolean undefined = true;
	private static int[] sections;
	private static int FIRST, TOOL_SECTION, COMBAT_SECTION, TOOL_BOOKS, COMBAT_BOOKS;
	private static List<Enchantment> enchantments = Arrays.asList(new Enchantment[] {
			Enchantment.DIG_SPEED, Enchantment.SILK_TOUCH, Enchantment.DURABILITY, Enchantment.LOOT_BONUS_BLOCKS,
			Enchantment.LUCK, Enchantment.LURE, Enchantment.MENDING, Enchantment.VANISHING_CURSE,
			Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_FALL,
			Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_PROJECTILE, Enchantment.OXYGEN,
			Enchantment.WATER_WORKER, Enchantment.THORNS, Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER,
			Enchantment.BINDING_CURSE,
			Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_UNDEAD, Enchantment.DAMAGE_ARTHROPODS, Enchantment.KNOCKBACK,
			Enchantment.FIRE_ASPECT, Enchantment.LOOT_BONUS_MOBS, Enchantment.SWEEPING_EDGE, Enchantment.DURABILITY,
			Enchantment.ARROW_DAMAGE, Enchantment.ARROW_KNOCKBACK, Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE,
			Enchantment.LOYALTY, Enchantment.IMPALING, Enchantment.RIPTIDE, Enchantment.CHANNELING,
			Enchantment.MENDING, Enchantment.VANISHING_CURSE});
	
	private ItemStack stack;
	private int section, number, ench;
	private boolean default_name;
	
	public ItemStack_SortV(ItemStack is)
	{
		if (undefined)
		{
			calc();
			undefined = false;
		}
		
		stack = is;
		if (stack != null)
		{
			section = getSectionNumber(stack);
			number = getNumber(stack);
			ench = getEnchantmentNumber(stack);
			default_name = !Utils.isRenamed(stack);
			/*System.out.println("stack: " + stack);
			System.out.println("section: " + section);
			System.out.println("\"ID\"'s: " + number);
			System.out.println("enchs: " + ench);*/
		}
	}
	
	public ItemStack getItemStack() {
		return stack;
	}
	
	private static void calc() {
		FIRST = get(Material.STONE);
		sections = new int[SECTION_BEGINNINGS.length];
		for (int i = 0; i < SECTION_BEGINNINGS.length; i++)
			sections[i] = getNumber(SECTION_BEGINNINGS[i]);
		TOOL_SECTION = getSectionNumber(Material.IRON_PICKAXE);
		COMBAT_SECTION = getSectionNumber(Material.IRON_SWORD);
		TOOL_BOOKS = getNumber(Material.SHEARS) + 1;
		COMBAT_BOOKS = getNumber(Material.GOLDEN_BOOTS) + 1;
	}

	@Override
	public int compareTo(Object ob) {
		if (!(ob instanceof ItemStack_SortV))
			return 0;
		ItemStack_SortV is = (ItemStack_SortV)ob;
		ItemStack stack2 = is.stack;
		if (stack == null && stack2 != null)
			return 1;
		else if (stack != null && stack2 == null)
			return -1;
		else if (stack == null && stack2 == null)
			return 0;

		if (section < is.section)
			return -1;
		else if (section > is.section)
			return 1;

		if (number < is.number)
			return -1;
		else if (number > is.number)
			return 1;

		if (stack.hasItemMeta() && stack2.hasItemMeta())
		{
			 // lore
			 if (stack.getItemMeta().hasLore())
				 if (!stack2.getItemMeta().hasLore())
					 return -1;
			 else
				 if (stack2.getItemMeta().hasLore())
					 return 1;
			 
			 // name
			 if (!default_name && is.default_name)
				 return -1;
			 if (default_name && !is.default_name)
				 return 1;
			 if (!default_name && !is.default_name) {
				 int res = stack.getItemMeta().getDisplayName().compareTo(stack2.getItemMeta().getDisplayName());
				 if (res != 0)
					 return res;
			 }
		}
		
		 // enchantment
		 if (ench < is.ench)
			 return -1;
		 if (ench > is.ench)
			 return 1;
		
		return 0;
	}

	public static int getSectionNumber(ItemStack s) {
		if (s.getType() != Material.ENCHANTED_BOOK)
		{
			return getSectionNumber(s.getType());
		}
		else
		{
			Enchantment ench = getFirstEnchantment(s);
			if (isToolEnchantment(ench))
				return TOOL_SECTION;
			if (isCombatEnchantment(ench))
				return COMBAT_SECTION;
		}
		return sections.length;
	}
	
	public static int getEnchantmentNumber(ItemStack s) {
		Enchantment ench = getFirstEnchantment(s);
		return enchantments.indexOf(ench);
	}

	public static Enchantment getFirstEnchantment(ItemStack s) {
		if (s == null || !s.hasItemMeta())
			return null;
		ItemMeta im = s.getItemMeta();
		Map<Enchantment, Integer> enchs;
		if (im instanceof EnchantmentStorageMeta) {
			enchs = ((EnchantmentStorageMeta)im).getStoredEnchants();
			for (Entry<Enchantment, Integer> entry : enchs.entrySet()) {
				Enchantment ench = entry.getKey();
				return ench;
			}
		}
		enchs = im.getEnchants();
		for (Entry<Enchantment, Integer> entry : enchs.entrySet()) {
			Enchantment ench = entry.getKey();
			return ench;
		}
		return null;
	}
	
	public static boolean isToolEnchantment(Enchantment ench) {
		return ench.equals(Enchantment.DIG_SPEED) || ench.equals(Enchantment.SILK_TOUCH) || ench.equals(Enchantment.DURABILITY) || ench.equals(Enchantment.LOOT_BONUS_BLOCKS)
				|| ench.equals(Enchantment.LUCK) || ench.equals(Enchantment.LURE) || ench.equals(Enchantment.MENDING) || ench.equals(Enchantment.VANISHING_CURSE);
	}
	public static boolean isCombatEnchantment(Enchantment ench) {
		return ench.equals(Enchantment.PROTECTION_ENVIRONMENTAL) || ench.equals(Enchantment.PROTECTION_FIRE) || ench.equals(Enchantment.PROTECTION_FALL)
				|| ench.equals(Enchantment.PROTECTION_EXPLOSIONS) || ench.equals(Enchantment.PROTECTION_PROJECTILE) || ench.equals(Enchantment.OXYGEN)
				|| ench.equals(Enchantment.WATER_WORKER) || ench.equals(Enchantment.THORNS) || ench.equals(Enchantment.DEPTH_STRIDER) || ench.equals(Enchantment.FROST_WALKER)
				|| ench.equals(Enchantment.BINDING_CURSE)
				|| ench.equals(Enchantment.DAMAGE_ALL) || ench.equals(Enchantment.DAMAGE_UNDEAD) || ench.equals(Enchantment.DAMAGE_ARTHROPODS) || ench.equals(Enchantment.KNOCKBACK)
				|| ench.equals(Enchantment.FIRE_ASPECT) || ench.equals(Enchantment.LOOT_BONUS_MOBS) || ench.equals(Enchantment.SWEEPING_EDGE) || ench.equals(Enchantment.DURABILITY)
				|| ench.equals(Enchantment.ARROW_DAMAGE) || ench.equals(Enchantment.ARROW_KNOCKBACK) || ench.equals(Enchantment.ARROW_FIRE) || ench.equals(Enchantment.ARROW_INFINITE)
				|| ench.equals(Enchantment.LOYALTY) || ench.equals(Enchantment.IMPALING) || ench.equals(Enchantment.RIPTIDE) || ench.equals(Enchantment.CHANNELING)
				|| ench.equals(Enchantment.MENDING) || ench.equals(Enchantment.VANISHING_CURSE);
	}
	
	public static int getSectionNumber(Material m) {
		int g = getNumber(m);
		if (g < sections[0])
			return sections.length; // after last section 
		
		for (int i = 1; i < sections.length; i++)
			if (g < sections[i])
				return i;
		
		return sections.length;
	}
	
	public static int getNumber(ItemStack s) {
		if (s.getType() == Material.ENCHANTED_BOOK) {
			Enchantment ench = getFirstEnchantment(s);
			if (isToolEnchantment(ench))
				return TOOL_BOOKS;
			if (isCombatEnchantment(ench))
				return COMBAT_BOOKS;
		}
		return getNumber(s.getType());
	}
	
	public static int getNumber(Material m) {
		int g = get(m);
		if (g == 0)
			return FIRST + 1;
		return FIRST - g;
	}
	
	private static int get(Material m) {
		int g = 0;
		switch (m)
		{
		// BUILDING BLOCKS
		case STONE: 						g++;
		case GRANITE: 						g++;
		case POLISHED_GRANITE: 						g++;
		case DIORITE: 						g++;
		case POLISHED_DIORITE: 						g++;
		case ANDESITE: 						g++;
		case POLISHED_ANDESITE: 						g++;
		case GRASS_BLOCK: 						g++;
		case DIRT: 						g++;
		case COARSE_DIRT: 						g++;
		case PODZOL: 						g++;
		case COBBLESTONE: 						g++;
		case OAK_PLANKS: 						g++;
		case SPRUCE_PLANKS: 						g++;
		case BIRCH_PLANKS:  						g++;
		case JUNGLE_PLANKS: 						g++;
		case ACACIA_PLANKS: 						g++;
		case DARK_OAK_PLANKS: 						g++;
		case BEDROCK: 						g++;
		case SAND: 						g++;
		case RED_SAND:  						g++;
		case GRAVEL: 						g++;
		case GOLD_ORE: 						g++;
		case IRON_ORE: 						g++;
		case COAL_ORE: 						g++;
		case OAK_LOG: 						g++;
		case SPRUCE_LOG:  						g++;
		case BIRCH_LOG: 						g++;
		case JUNGLE_LOG: 						g++;
		case ACACIA_LOG: 						g++;
		case DARK_OAK_LOG: 						g++;
		case STRIPPED_OAK_LOG: 						g++;
		case STRIPPED_SPRUCE_LOG: 						g++;
		case STRIPPED_BIRCH_LOG: 						g++;
		case STRIPPED_JUNGLE_LOG: 						g++;
		case STRIPPED_ACACIA_LOG: 						g++;
		case STRIPPED_DARK_OAK_LOG: 						g++;
		case STRIPPED_OAK_WOOD: 						g++;
		case STRIPPED_SPRUCE_WOOD: 						g++;
		case STRIPPED_BIRCH_WOOD: 						g++;
		case STRIPPED_JUNGLE_WOOD: 						g++;
		case STRIPPED_ACACIA_WOOD: 						g++;
		case STRIPPED_DARK_OAK_WOOD: 						g++;
		case OAK_WOOD: 						g++;
		case SPRUCE_WOOD: 						g++;
		case BIRCH_WOOD: 						g++;
		case JUNGLE_WOOD: 						g++;
		case ACACIA_WOOD: 						g++;
		case DARK_OAK_WOOD: 						g++;
		case SPONGE: 						g++;
		case WET_SPONGE: 						g++;
		case GLASS: 						g++;
		case LAPIS_ORE: 						g++;
		case LAPIS_BLOCK: 						g++;
		case SANDSTONE: 						g++;
		case CHISELED_SANDSTONE: 						g++;
		case CUT_SANDSTONE: 						g++;
		case WHITE_WOOL: 						g++;
		case ORANGE_WOOL: 						g++;
		case MAGENTA_WOOL: 						g++;
		case LIGHT_BLUE_WOOL: 						g++;
		case YELLOW_WOOL: 						g++;
		case LIME_WOOL: 						g++;
		case PINK_WOOL: 						g++;
		case GRAY_WOOL: 						g++;
		case LIGHT_GRAY_WOOL: 						g++;
		case CYAN_WOOL: 						g++;
		case PURPLE_WOOL: 						g++;
		case BLUE_WOOL: 						g++;
		case BROWN_WOOL: 						g++;
		case GREEN_WOOL: 						g++;
		case RED_WOOL: 						g++;
		case BLACK_WOOL: 						g++;
		case GOLD_BLOCK: 						g++;
		case IRON_BLOCK: 						g++;
		case OAK_SLAB: 						g++;
		case SPRUCE_SLAB: 						g++;
		case BIRCH_SLAB: 						g++;
		case JUNGLE_SLAB: 						g++;
		case ACACIA_SLAB: 						g++;
		case DARK_OAK_SLAB: 						g++;
		case STONE_SLAB: 						g++;
		case SANDSTONE_SLAB: 						g++;
		case PETRIFIED_OAK_SLAB: 						g++;
		case COBBLESTONE_SLAB: 						g++;
		case BRICK_SLAB: 						g++;
		case STONE_BRICK_SLAB: 						g++;
		case NETHER_BRICK_SLAB: 						g++;
		case QUARTZ_SLAB: 						g++;
		case RED_SANDSTONE_SLAB: 						g++;
		case PURPUR_SLAB: 						g++;
		case PRISMARINE_SLAB: 						g++;
		case PRISMARINE_BRICK_SLAB: 						g++;
		case DARK_PRISMARINE_SLAB: 						g++;
		case SMOOTH_QUARTZ: 						g++;
		case SMOOTH_RED_SANDSTONE: 						g++;
		case SMOOTH_SANDSTONE: 						g++;
		case SMOOTH_STONE: 						g++;
		case BRICKS: 						g++;
		case BOOKSHELF: 						g++;
		case MOSSY_COBBLESTONE: 						g++;
		case OBSIDIAN: 						g++;
		case PURPUR_BLOCK: 						g++;
		case PURPUR_PILLAR: 						g++;
		case PURPUR_STAIRS: 						g++;
		case OAK_STAIRS: 						g++;
		case DIAMOND_ORE: 						g++;
		case DIAMOND_BLOCK: 						g++;
		case COBBLESTONE_STAIRS: 						g++;
		case REDSTONE_ORE: 						g++;
		case ICE: 						g++;
		case SNOW_BLOCK: 						g++;
		case CLAY: 						g++;
		case PUMPKIN: 						g++;
		case CARVED_PUMPKIN: 						g++;
		case NETHERRACK: 						g++;
		case SOUL_SAND: 						g++;
		case GLOWSTONE: 						g++;
		case JACK_O_LANTERN: 						g++;
		case STONE_BRICKS: 						g++;
		case MOSSY_STONE_BRICKS: 						g++;
		case CRACKED_STONE_BRICKS: 						g++;
		case CHISELED_STONE_BRICKS: 						g++;
		case MELON: 						g++;
		case BRICK_STAIRS: 						g++;
		case STONE_BRICK_STAIRS: 						g++;
		case MYCELIUM: 						g++;
		case NETHER_BRICKS: 						g++;
		case NETHER_BRICK_STAIRS: 						g++;
		case END_STONE: 						g++;
		case END_STONE_BRICKS: 						g++;
		case SANDSTONE_STAIRS: 						g++;
		case EMERALD_ORE: 						g++;
		case EMERALD_BLOCK: 						g++;
		case SPRUCE_STAIRS: 						g++;
		case BIRCH_STAIRS: 						g++;
		case JUNGLE_STAIRS: 						g++;
		case NETHER_QUARTZ_ORE: 						g++;
		case CHISELED_QUARTZ_BLOCK: 						g++;
		case QUARTZ_BLOCK: 						g++;
		case QUARTZ_PILLAR: 						g++;
		case QUARTZ_STAIRS: 						g++;
		case WHITE_TERRACOTTA: 						g++;
		case ORANGE_TERRACOTTA: 						g++;
		case MAGENTA_TERRACOTTA: 						g++;
		case LIGHT_BLUE_TERRACOTTA: 						g++;
		case YELLOW_TERRACOTTA: 						g++;
		case LIME_TERRACOTTA: 						g++;
		case PINK_TERRACOTTA: 						g++;
		case GRAY_TERRACOTTA: 						g++;
		case LIGHT_GRAY_TERRACOTTA: 						g++;
		case CYAN_TERRACOTTA: 						g++;
		case PURPLE_TERRACOTTA: 						g++;
		case BLUE_TERRACOTTA: 						g++;
		case BROWN_TERRACOTTA: 						g++;
		case GREEN_TERRACOTTA: 						g++;
		case RED_TERRACOTTA: 						g++;
		case BLACK_TERRACOTTA: 						g++;
		case HAY_BLOCK: 						g++;
		case TERRACOTTA: 						g++;
		case COAL_BLOCK: 						g++;
		case PACKED_ICE: 						g++;
		case ACACIA_STAIRS: 						g++;
		case DARK_OAK_STAIRS: 						g++;
		case WHITE_STAINED_GLASS: 						g++;
		case ORANGE_STAINED_GLASS: 						g++;
		case MAGENTA_STAINED_GLASS: 						g++;
		case LIGHT_BLUE_STAINED_GLASS: 						g++;
		case YELLOW_STAINED_GLASS: 						g++;
		case LIME_STAINED_GLASS: 						g++;
		case PINK_STAINED_GLASS: 						g++;
		case GRAY_STAINED_GLASS: 						g++;
		case LIGHT_GRAY_STAINED_GLASS: 						g++;
		case CYAN_STAINED_GLASS: 						g++;
		case PURPLE_STAINED_GLASS: 						g++;
		case BLUE_STAINED_GLASS: 						g++;
		case BROWN_STAINED_GLASS: 						g++;
		case GREEN_STAINED_GLASS: 						g++;
		case RED_STAINED_GLASS: 						g++;
		case BLACK_STAINED_GLASS: 						g++;
		case PRISMARINE: 						g++;
		case PRISMARINE_BRICKS: 						g++;
		case DARK_PRISMARINE: 						g++;
		case PRISMARINE_STAIRS: 						g++;
		case PRISMARINE_BRICK_STAIRS: 						g++;
		case DARK_PRISMARINE_STAIRS: 						g++;
		case SEA_LANTERN: 						g++;
		case RED_SANDSTONE: 						g++;
		case CHISELED_RED_SANDSTONE: 						g++;
		case CUT_RED_SANDSTONE: 						g++;
		case RED_SANDSTONE_STAIRS: 						g++;
		case MAGMA_BLOCK: 						g++;
		case NETHER_WART_BLOCK: 						g++;
		case RED_NETHER_BRICKS: 						g++;
		case BONE_BLOCK: 						g++;
		case WHITE_CONCRETE: 						g++;
		case ORANGE_CONCRETE: 						g++;
		case MAGENTA_CONCRETE: 						g++;
		case LIGHT_BLUE_CONCRETE: 						g++;
		case YELLOW_CONCRETE: 						g++;
		case LIME_CONCRETE: 						g++;
		case PINK_CONCRETE: 						g++;
		case GRAY_CONCRETE: 						g++;
		case LIGHT_GRAY_CONCRETE: 						g++;
		case CYAN_CONCRETE: 						g++;
		case PURPLE_CONCRETE: 						g++;
		case BLUE_CONCRETE: 						g++;
		case BROWN_CONCRETE: 						g++;
		case GREEN_CONCRETE: 						g++;
		case RED_CONCRETE: 						g++;
		case BLACK_CONCRETE: 						g++;
		case WHITE_CONCRETE_POWDER: 						g++;
		case ORANGE_CONCRETE_POWDER: 						g++;
		case MAGENTA_CONCRETE_POWDER: 						g++;
		case LIGHT_BLUE_CONCRETE_POWDER: 						g++;
		case YELLOW_CONCRETE_POWDER: 						g++;
		case LIME_CONCRETE_POWDER: 						g++;
		case PINK_CONCRETE_POWDER: 						g++;
		case GRAY_CONCRETE_POWDER: 						g++;
		case LIGHT_GRAY_CONCRETE_POWDER: 						g++;
		case CYAN_CONCRETE_POWDER: 						g++;
		case PURPLE_CONCRETE_POWDER: 						g++;
		case BLUE_CONCRETE_POWDER: 						g++;
		case BROWN_CONCRETE_POWDER: 						g++;
		case GREEN_CONCRETE_POWDER: 						g++;
		case RED_CONCRETE_POWDER: 						g++;
		case BLACK_CONCRETE_POWDER: 						g++;
		case DEAD_TUBE_CORAL_BLOCK: 						g++;
		case DEAD_BRAIN_CORAL_BLOCK: 						g++;
		case DEAD_BUBBLE_CORAL_BLOCK: 						g++;
		case DEAD_FIRE_CORAL_BLOCK: 						g++;
		case DEAD_HORN_CORAL_BLOCK: 						g++;
		case TUBE_CORAL_BLOCK: 						g++;
		case BRAIN_CORAL_BLOCK: 						g++;
		case BUBBLE_CORAL_BLOCK: 						g++;
		case FIRE_CORAL_BLOCK: 						g++;
		case HORN_CORAL_BLOCK: 						g++;
		case BLUE_ICE: 						g++;
		case DRIED_KELP_BLOCK: 						g++;
		// DECORATION BLOCKS
		case OAK_SAPLING: 						g++;
		case SPRUCE_SAPLING: 						g++;
		case BIRCH_SAPLING: 						g++;
		case JUNGLE_SAPLING: 						g++;
		case ACACIA_SAPLING: 						g++;
		case DARK_OAK_SAPLING: 						g++;
		case OAK_LEAVES: 						g++;
		case SPRUCE_LEAVES: 						g++;
		case BIRCH_LEAVES: 						g++;
		case JUNGLE_LEAVES: 						g++;
		case ACACIA_LEAVES: 						g++;
		case DARK_OAK_LEAVES: 						g++;
		case COBWEB: 						g++;
		case GRASS: 						g++;
		case FERN: 						g++;
		case DEAD_BUSH: 						g++;
		case SEAGRASS: 						g++;
		case SEA_PICKLE: 						g++;
		case DANDELION: 						g++;
		case POPPY: 						g++;
		case BLUE_ORCHID: 						g++;
		case ALLIUM: 						g++;
		case AZURE_BLUET: 						g++;
		case RED_TULIP: 						g++;
		case ORANGE_TULIP: 						g++;
		case WHITE_TULIP: 						g++;
		case PINK_TULIP: 						g++;
		case OXEYE_DAISY: 						g++;
		case BROWN_MUSHROOM: 						g++;
		case RED_MUSHROOM: 						g++;
		case TORCH: 						g++;
		case END_ROD: 						g++;
		case CHORUS_PLANT: 						g++;
		case CHORUS_FLOWER: 						g++;
		case CHEST: 						g++;
		case CRAFTING_TABLE: 						g++;
		case FARMLAND: 						g++;
		case FURNACE: 						g++;
		case LADDER: 						g++;
		case SNOW: 						g++;
		case CACTUS: 						g++;
		case JUKEBOX: 						g++;
		case OAK_FENCE: 						g++;
		case SPRUCE_FENCE: 						g++;
		case BIRCH_FENCE: 						g++;
		case JUNGLE_FENCE: 						g++;
		case ACACIA_FENCE: 						g++;
		case DARK_OAK_FENCE: 						g++;
		case INFESTED_STONE: 						g++;
		case INFESTED_COBBLESTONE: 						g++;
		case INFESTED_STONE_BRICKS: 						g++;
		case INFESTED_MOSSY_STONE_BRICKS: 						g++;
		case INFESTED_CRACKED_STONE_BRICKS: 						g++;
		case INFESTED_CHISELED_STONE_BRICKS: 						g++;
		case BROWN_MUSHROOM_BLOCK: 						g++;
		case RED_MUSHROOM_BLOCK: 						g++;
		case MUSHROOM_STEM: 						g++;
		case IRON_BARS: 						g++;
		case GLASS_PANE: 						g++;
		case VINE: 						g++;
		case LILY_PAD: 						g++;
		case NETHER_BRICK_FENCE: 						g++;
		case ENCHANTING_TABLE: 						g++;
		case END_PORTAL_FRAME: 						g++;
		case ENDER_CHEST: 						g++;
		case COBBLESTONE_WALL: 						g++;
		case MOSSY_COBBLESTONE_WALL: 						g++;
		case ANVIL: 						g++;
		case CHIPPED_ANVIL: 						g++;
		case DAMAGED_ANVIL: 						g++;
		case WHITE_CARPET: 						g++;
		case ORANGE_CARPET: 						g++;
		case MAGENTA_CARPET: 						g++;
		case LIGHT_BLUE_CARPET: 						g++;
		case YELLOW_CARPET: 						g++;
		case LIME_CARPET: 						g++;
		case PINK_CARPET: 						g++;
		case GRAY_CARPET: 						g++;
		case LIGHT_GRAY_CARPET: 						g++;
		case CYAN_CARPET: 						g++;
		case PURPLE_CARPET: 						g++;
		case BLUE_CARPET: 						g++;
		case BROWN_CARPET: 						g++;
		case GREEN_CARPET: 						g++;
		case RED_CARPET: 						g++;
		case BLACK_CARPET: 						g++;
		case SLIME_BLOCK: 						g++;
		case GRASS_PATH: 						g++;
		case SUNFLOWER: 						g++;
		case LILAC: 						g++;
		case ROSE_BUSH: 						g++;
		case PEONY: 						g++;
		case TALL_GRASS: 						g++;
		case LARGE_FERN: 						g++;
		case WHITE_STAINED_GLASS_PANE: 						g++;
		case ORANGE_STAINED_GLASS_PANE: 						g++;
		case MAGENTA_STAINED_GLASS_PANE: 						g++;
		case LIGHT_BLUE_STAINED_GLASS_PANE: 						g++;
		case YELLOW_STAINED_GLASS_PANE: 						g++;
		case LIME_STAINED_GLASS_PANE: 						g++;
		case PINK_STAINED_GLASS_PANE: 						g++;
		case GRAY_STAINED_GLASS_PANE: 						g++;
		case LIGHT_GRAY_STAINED_GLASS_PANE: 						g++;
		case CYAN_STAINED_GLASS_PANE: 						g++;
		case PURPLE_STAINED_GLASS_PANE: 						g++;
		case BLUE_STAINED_GLASS_PANE: 						g++;
		case BROWN_STAINED_GLASS_PANE: 						g++;
		case GREEN_STAINED_GLASS_PANE: 						g++;
		case RED_STAINED_GLASS_PANE: 						g++;
		case BLACK_STAINED_GLASS_PANE: 						g++;
		case WHITE_SHULKER_BOX: 						g++;
		case ORANGE_SHULKER_BOX: 						g++;
		case MAGENTA_SHULKER_BOX: 						g++;
		case LIGHT_BLUE_SHULKER_BOX: 						g++;
		case YELLOW_SHULKER_BOX: 						g++;
		case LIME_SHULKER_BOX: 						g++;
		case PINK_SHULKER_BOX: 						g++;
		case GRAY_SHULKER_BOX: 						g++;
		case LIGHT_GRAY_SHULKER_BOX: 						g++;
		case CYAN_SHULKER_BOX: 						g++;
		case PURPLE_SHULKER_BOX: 						g++;
		case BLUE_SHULKER_BOX: 						g++;
		case BROWN_SHULKER_BOX: 						g++;
		case GREEN_SHULKER_BOX: 						g++;
		case RED_SHULKER_BOX: 						g++;
		case BLACK_SHULKER_BOX: 						g++;
		case WHITE_GLAZED_TERRACOTTA: 						g++;
		case ORANGE_GLAZED_TERRACOTTA: 						g++;
		case MAGENTA_GLAZED_TERRACOTTA: 						g++;
		case LIGHT_BLUE_GLAZED_TERRACOTTA: 						g++;
		case YELLOW_GLAZED_TERRACOTTA: 						g++;
		case LIME_GLAZED_TERRACOTTA: 						g++;
		case PINK_GLAZED_TERRACOTTA: 						g++;
		case GRAY_GLAZED_TERRACOTTA: 						g++;
		case LIGHT_GRAY_GLAZED_TERRACOTTA: 						g++;
		case CYAN_GLAZED_TERRACOTTA: 						g++;
		case PURPLE_GLAZED_TERRACOTTA: 						g++;
		case BLUE_GLAZED_TERRACOTTA: 						g++;
		case BROWN_GLAZED_TERRACOTTA: 						g++;
		case GREEN_GLAZED_TERRACOTTA: 						g++;
		case RED_GLAZED_TERRACOTTA: 						g++;
		case BLACK_GLAZED_TERRACOTTA: 						g++;
		case TUBE_CORAL: 						g++;
		case BRAIN_CORAL: 						g++;
		case BUBBLE_CORAL: 						g++;
		case FIRE_CORAL: 						g++;
		case HORN_CORAL: 						g++;
		case TUBE_CORAL_FAN: 						g++;
		case BRAIN_CORAL_FAN: 						g++;
		case BUBBLE_CORAL_FAN: 						g++;
		case FIRE_CORAL_FAN: 						g++;
		case HORN_CORAL_FAN: 						g++;
		case PAINTING: 						g++;
		case SIGN: 						g++;
		case WHITE_BED: 						g++;
		case ORANGE_BED: 						g++;
		case MAGENTA_BED: 						g++;
		case LIGHT_BLUE_BED: 						g++;
		case YELLOW_BED: 						g++;
		case LIME_BED: 						g++;
		case PINK_BED: 						g++;
		case GRAY_BED: 						g++;
		case LIGHT_GRAY_BED: 						g++;
		case CYAN_BED: 						g++;
		case PURPLE_BED: 						g++;
		case BLUE_BED: 						g++;
		case BROWN_BED: 						g++;
		case GREEN_BED: 						g++;
		case RED_BED: 						g++;
		case BLACK_BED: 						g++;
		case ITEM_FRAME: 						g++;
		case FLOWER_POT: 						g++;
		case SKELETON_SKULL: 						g++;
		case WITHER_SKELETON_SKULL: 						g++;
		case PLAYER_HEAD: 						g++;
		case ZOMBIE_HEAD: 						g++;
		case CREEPER_HEAD: 						g++;
		case DRAGON_HEAD: 						g++;
		case ARMOR_STAND: 						g++;
		case WHITE_BANNER: 						g++;
		case ORANGE_BANNER: 						g++;
		case MAGENTA_BANNER: 						g++;
		case LIGHT_BLUE_BANNER: 						g++;
		case YELLOW_BANNER: 						g++;
		case LIME_BANNER: 						g++;
		case PINK_BANNER: 						g++;
		case GRAY_BANNER: 						g++;
		case LIGHT_GRAY_BANNER: 						g++;
		case CYAN_BANNER: 						g++;
		case PURPLE_BANNER: 						g++;
		case BLUE_BANNER: 						g++;
		case BROWN_BANNER: 						g++;
		case GREEN_BANNER: 						g++;
		case RED_BANNER: 						g++;
		case BLACK_BANNER: 						g++;
		case END_CRYSTAL: 						g++;
		// REDSTONE
		case DISPENSER: 						g++;
		case NOTE_BLOCK: 						g++;
		case STICKY_PISTON: 						g++;
		case PISTON: 						g++;
		case TNT: 						g++;
		case LEVER: 						g++;
		case STONE_PRESSURE_PLATE: 						g++;
		case OAK_PRESSURE_PLATE: 						g++;
		case SPRUCE_PRESSURE_PLATE: 						g++;
		case BIRCH_PRESSURE_PLATE: 						g++;
		case JUNGLE_PRESSURE_PLATE: 						g++;
		case ACACIA_PRESSURE_PLATE: 						g++;
		case DARK_OAK_PRESSURE_PLATE: 						g++;
		case REDSTONE_TORCH: 						g++;
		case STONE_BUTTON: 						g++;
		case OAK_TRAPDOOR: 						g++;
		case SPRUCE_TRAPDOOR: 						g++;
		case BIRCH_TRAPDOOR: 						g++;
		case JUNGLE_TRAPDOOR: 						g++;
		case ACACIA_TRAPDOOR: 						g++;
		case DARK_OAK_TRAPDOOR: 						g++;
		case OAK_FENCE_GATE: 						g++;
		case SPRUCE_FENCE_GATE: 						g++;
		case BIRCH_FENCE_GATE: 						g++;
		case JUNGLE_FENCE_GATE: 						g++;
		case ACACIA_FENCE_GATE: 						g++;
		case DARK_OAK_FENCE_GATE: 						g++;
		case REDSTONE_LAMP: 						g++;
		case TRIPWIRE: 						g++;
		case OAK_BUTTON: 						g++;
		case SPRUCE_BUTTON: 						g++;
		case BIRCH_BUTTON: 						g++;
		case JUNGLE_BUTTON: 						g++;
		case ACACIA_BUTTON: 						g++;
		case DARK_OAK_BUTTON: 						g++;
		case TRAPPED_CHEST: 						g++;
		case LIGHT_WEIGHTED_PRESSURE_PLATE: 						g++;
		case HEAVY_WEIGHTED_PRESSURE_PLATE: 						g++;
		case DAYLIGHT_DETECTOR: 						g++;
		case REDSTONE_BLOCK: 						g++;
		case HOPPER: 						g++;
		case DROPPER: 						g++;
		case IRON_TRAPDOOR: 						g++;
		case OBSERVER: 						g++;
		case IRON_DOOR: 						g++;
		case OAK_DOOR: 						g++;
		case SPRUCE_DOOR: 						g++;
		case BIRCH_DOOR: 						g++;
		case JUNGLE_DOOR: 						g++;
		case ACACIA_DOOR: 						g++;
		case DARK_OAK_DOOR: 						g++;
		case REPEATER: 						g++;
		case COMPARATOR: 						g++;
		case REDSTONE: 						g++;
		// TRANSPORTATION
		case POWERED_RAIL: 						g++;
		case DETECTOR_RAIL: 						g++;
		case RAIL: 						g++;
		case ACTIVATOR_RAIL: 						g++;
		case MINECART: 						g++;
		case SADDLE: 						g++;
		case OAK_BOAT: 						g++;
		case CHEST_MINECART: 						g++;
		case FURNACE_MINECART: 						g++;
		case CARROT_ON_A_STICK: 						g++;
		case TNT_MINECART: 						g++;
		case HOPPER_MINECART: 						g++;
		case ELYTRA: 						g++;
		case SPRUCE_BOAT: 						g++;
		case BIRCH_BOAT: 						g++;
		case JUNGLE_BOAT: 						g++;
		case ACACIA_BOAT: 						g++;
		case DARK_OAK_BOAT: 						g++;
		// MISCELLANEOUS
		case BEACON: 						g++;
		case TURTLE_EGG: 						g++;
		case CONDUIT: 						g++;
		case SCUTE: 						g++;
		case COAL: 						g++;
		case CHARCOAL: 						g++;
		case DIAMOND: 						g++;
		case IRON_INGOT: 						g++;
		case GOLD_INGOT: 						g++;
		case STICK: 						g++;
		case BOWL: 						g++;
		case STRING: 						g++;
		case FEATHER: 						g++;
		case GUNPOWDER: 						g++;
		case WHEAT_SEEDS: 						g++;
		case WHEAT: 						g++;
		case FLINT: 						g++;
		case BUCKET: 						g++;
		case WATER_BUCKET: 						g++;
		case LAVA_BUCKET: 						g++;
		case SNOWBALL: 						g++;
		case LEATHER: 						g++;
		case MILK_BUCKET: 						g++;
		case PUFFERFISH_BUCKET: 						g++;
		case SALMON_BUCKET: 						g++;
		case COD_BUCKET: 						g++;
		case TROPICAL_FISH_BUCKET: 						g++;
		case BRICK: 						g++;
		case CLAY_BALL: 						g++;
		case SUGAR_CANE: 						g++;
		case KELP: 						g++;
		case PAPER: 						g++;
		case BOOK: 						g++;
		case SLIME_BALL: 						g++;
		case EGG: 						g++;
		case GLOWSTONE_DUST: 						g++;
		case INK_SAC: 						g++;
		case ROSE_RED: 						g++;
		case CACTUS_GREEN: 						g++;
		case COCOA_BEANS: 						g++;
		case LAPIS_LAZULI: 						g++;
		case PURPLE_DYE: 						g++;
		case CYAN_DYE: 						g++;
		case LIGHT_GRAY_DYE: 						g++;
		case GRAY_DYE: 						g++;
		case PINK_DYE: 						g++;
		case LIME_DYE: 						g++;
		case DANDELION_YELLOW: 						g++;
		case LIGHT_BLUE_DYE: 						g++;
		case MAGENTA_DYE: 						g++;
		case ORANGE_DYE: 						g++;
		case BONE_MEAL: 						g++;
		case BONE: 						g++;
		case SUGAR: 						g++;
		case PUMPKIN_SEEDS: 						g++;
		case MELON_SEEDS: 						g++;
		case ENDER_PEARL: 						g++;
		case BLAZE_ROD: 						g++;
		case GOLD_NUGGET: 						g++;
		case NETHER_WART: 						g++;
		case ENDER_EYE: 						g++;
		case BAT_SPAWN_EGG: 						g++;
		case BLAZE_SPAWN_EGG: 						g++;
		case CAVE_SPIDER_SPAWN_EGG: 						g++;
		case CHICKEN_SPAWN_EGG: 						g++;
		case COD_SPAWN_EGG: 						g++;
		case COW_SPAWN_EGG: 						g++;
		case CREEPER_SPAWN_EGG: 						g++;
		case DOLPHIN_SPAWN_EGG: 						g++;
		case DONKEY_SPAWN_EGG: 						g++;
		case DROWNED_SPAWN_EGG: 						g++;
		case ELDER_GUARDIAN_SPAWN_EGG: 						g++;
		case ENDERMAN_SPAWN_EGG: 						g++;
		case ENDERMITE_SPAWN_EGG: 						g++;
		case EVOKER_SPAWN_EGG: 						g++;
		case GHAST_SPAWN_EGG: 						g++;
		case GUARDIAN_SPAWN_EGG: 						g++;
		case HORSE_SPAWN_EGG: 						g++;
		case HUSK_SPAWN_EGG: 						g++;
		case LLAMA_SPAWN_EGG: 						g++;
		case MAGMA_CUBE_SPAWN_EGG: 						g++;
		case MOOSHROOM_SPAWN_EGG: 						g++;
		case MULE_SPAWN_EGG: 						g++;
		case OCELOT_SPAWN_EGG: 						g++;
		case PARROT_SPAWN_EGG: 						g++;
		case PHANTOM_SPAWN_EGG: 						g++;
		case PIG_SPAWN_EGG: 						g++;
		case POLAR_BEAR_SPAWN_EGG: 						g++;
		case PUFFERFISH_SPAWN_EGG: 						g++;
		case RABBIT_SPAWN_EGG: 						g++;
		case SALMON_SPAWN_EGG: 						g++;
		case SHEEP_SPAWN_EGG: 						g++;
		case SHULKER_SPAWN_EGG: 						g++;
		case SILVERFISH_SPAWN_EGG: 						g++;
		case SKELETON_SPAWN_EGG: 						g++;
		case SKELETON_HORSE_SPAWN_EGG: 						g++;
		case SLIME_SPAWN_EGG: 						g++;
		case SPIDER_SPAWN_EGG: 						g++;
		case SQUID_SPAWN_EGG: 						g++;
		case STRAY_SPAWN_EGG: 						g++;
		case TROPICAL_FISH_SPAWN_EGG: 						g++;
		case TURTLE_SPAWN_EGG: 						g++;
		case VEX_SPAWN_EGG: 						g++;
		case VILLAGER_SPAWN_EGG: 						g++;
		case VINDICATOR_SPAWN_EGG: 						g++;
		case WITCH_SPAWN_EGG: 						g++;
		case WITHER_SKELETON_SPAWN_EGG: 						g++;
		case WOLF_SPAWN_EGG: 						g++;
		case ZOMBIE_SPAWN_EGG: 						g++;
		case ZOMBIE_HORSE_SPAWN_EGG: 						g++;
		case ZOMBIE_PIGMAN_SPAWN_EGG: 						g++;
		case ZOMBIE_VILLAGER_SPAWN_EGG: 						g++;
		case EXPERIENCE_BOTTLE: 						g++;
		case FIRE_CHARGE: 						g++;
		case WRITABLE_BOOK: 						g++;
		case EMERALD: 						g++;
		case MAP: 						g++;
		case NETHER_STAR: 						g++;
		case FIREWORK_ROCKET: 						g++;
		case FIREWORK_STAR: 						g++;
		case NETHER_BRICK: 						g++;
		case QUARTZ: 						g++;
		case PRISMARINE_SHARD: 						g++;
		case PRISMARINE_CRYSTALS: 						g++;
		case RABBIT_HIDE: 						g++;
		case IRON_HORSE_ARMOR: 						g++;
		case GOLDEN_HORSE_ARMOR: 						g++;
		case DIAMOND_HORSE_ARMOR: 						g++;
		case CHORUS_FRUIT: 						g++;
		case POPPED_CHORUS_FRUIT: 						g++;
		case BEETROOT_SEEDS: 						g++;
		case SHULKER_SHELL: 						g++;
		case IRON_NUGGET: 						g++;
		case MUSIC_DISC_13: 						g++;
		case MUSIC_DISC_CAT: 						g++;
		case MUSIC_DISC_BLOCKS: 						g++;
		case MUSIC_DISC_CHIRP: 						g++;
		case MUSIC_DISC_FAR: 						g++;
		case MUSIC_DISC_MALL: 						g++;
		case MUSIC_DISC_MELLOHI: 						g++;
		case MUSIC_DISC_STAL: 						g++;
		case MUSIC_DISC_STRAD: 						g++;
		case MUSIC_DISC_WARD: 						g++;
		case MUSIC_DISC_11: 						g++;
		case MUSIC_DISC_WAIT: 						g++;
		case NAUTILUS_SHELL: 						g++;
		case HEART_OF_THE_SEA: 						g++;
		// FOODSTUFFS
		case APPLE: 						g++;
		case MUSHROOM_STEW: 						g++;
		case BREAD: 						g++;
		case PORKCHOP: 						g++;
		case COOKED_PORKCHOP: 						g++;
		case GOLDEN_APPLE: 						g++;
		case ENCHANTED_GOLDEN_APPLE: 						g++;
		case COD: 						g++;
		case SALMON: 						g++;
		case TROPICAL_FISH: 						g++;
		case PUFFERFISH: 						g++;
		case COOKED_COD: 						g++;
		case COOKED_SALMON: 						g++;
		case CAKE: 						g++;
		case COOKIE: 						g++;
		case MELON_SLICE: 						g++;
		case DRIED_KELP: 						g++;
		case BEEF: 						g++;
		case COOKED_BEEF: 						g++;
		case CHICKEN: 						g++;
		case COOKED_CHICKEN: 						g++;
		case ROTTEN_FLESH: 						g++;
		case SPIDER_EYE: 						g++;
		case CARROT: 						g++;
		case POTATO: 						g++;
		case BAKED_POTATO: 						g++;
		case POISONOUS_POTATO: 						g++;
		case PUMPKIN_PIE: 						g++;
		case RABBIT: 						g++;
		case COOKED_RABBIT: 						g++;
		case RABBIT_STEW: 						g++;
		case MUTTON: 						g++;
		case COOKED_MUTTON: 						g++;
		case BEETROOT: 						g++;
		case BEETROOT_SOUP: 						g++;
		// TOOLS
		case IRON_SHOVEL: 						g++;
		case IRON_PICKAXE: 						g++;
		case IRON_AXE: 						g++;
		case FLINT_AND_STEEL: 						g++;
		case WOODEN_SHOVEL: 						g++;
		case WOODEN_PICKAXE: 						g++;
		case WOODEN_AXE: 						g++;
		case STONE_SHOVEL: 						g++;
		case STONE_PICKAXE: 						g++;
		case STONE_AXE: 						g++;
		case DIAMOND_SHOVEL: 						g++;
		case DIAMOND_PICKAXE: 						g++;
		case DIAMOND_AXE: 						g++;
		case GOLDEN_SHOVEL: 						g++;
		case GOLDEN_PICKAXE: 						g++;
		case GOLDEN_AXE: 						g++;
		case WOODEN_HOE: 						g++;
		case STONE_HOE: 						g++;
		case IRON_HOE: 						g++;
		case DIAMOND_HOE: 						g++;
		case GOLDEN_HOE: 						g++;
		case COMPASS: 						g++;
		case FISHING_ROD: 						g++;
		case CLOCK: 						g++;
		case SHEARS: 						g++;
							g++; // tool enchantments
		case LEAD: 						g++;
		case NAME_TAG: 						g++;
		// COMBAT
		case TURTLE_HELMET: 						g++;
		case BOW: 						g++;
		case ARROW: 						g++;
		case IRON_SWORD: 						g++;
		case WOODEN_SWORD: 						g++;
		case STONE_SWORD: 						g++;
		case DIAMOND_SWORD: 						g++;
		case GOLDEN_SWORD: 						g++;
		case LEATHER_HELMET: 						g++;
		case LEATHER_CHESTPLATE: 						g++;
		case LEATHER_LEGGINGS: 						g++;
		case LEATHER_BOOTS: 						g++;
		case CHAINMAIL_HELMET: 						g++;
		case CHAINMAIL_CHESTPLATE: 						g++;
		case CHAINMAIL_LEGGINGS: 						g++;
		case CHAINMAIL_BOOTS: 						g++;
		case IRON_HELMET: 						g++;
		case IRON_CHESTPLATE: 						g++;
		case IRON_LEGGINGS: 						g++;
		case IRON_BOOTS: 						g++;
		case DIAMOND_HELMET: 						g++;
		case DIAMOND_CHESTPLATE: 						g++;
		case DIAMOND_LEGGINGS: 						g++;
		case DIAMOND_BOOTS: 						g++;
		case GOLDEN_HELMET: 						g++;
		case GOLDEN_CHESTPLATE: 						g++;
		case GOLDEN_LEGGINGS: 						g++;
		case GOLDEN_BOOTS: 						g++;
							g++; // combat enchantments
		case SPECTRAL_ARROW: 						g++;
		case TIPPED_ARROW: 						g++;
		case SHIELD: 						g++;
		case TOTEM_OF_UNDYING: 						g++;
		case TRIDENT: 						g++;
		// BREWING
		case GHAST_TEAR: 						g++;
		case POTION: 						g++;
		case GLASS_BOTTLE: 						g++;
		case FERMENTED_SPIDER_EYE: 						g++;
		case BLAZE_POWDER: 						g++;
		case MAGMA_CREAM: 						g++;
		case BREWING_STAND: 						g++;
		case CAULDRON: 						g++;
		case GLISTERING_MELON_SLICE: 						g++;
		case GOLDEN_CARROT: 						g++;
		case RABBIT_FOOT: 						g++;
		case DRAGON_BREATH: 						g++;
		case SPLASH_POTION: 						g++;
		case LINGERING_POTION: 						g++;
		case PHANTOM_MEMBRANE: 						g++;
		
		default: // out of sections
			break;
		}
		
		return g;
	}
	
	public static int getSectionNumber_2(Material m) {
		switch (m)
		{
		case STONE: case GRANITE: case POLISHED_GRANITE: case DIORITE: case POLISHED_DIORITE: case ANDESITE: case POLISHED_ANDESITE:
		case GRASS_BLOCK: case DIRT: case COARSE_DIRT: case PODZOL: case COBBLESTONE:
		case OAK_PLANKS: case SPRUCE_PLANKS: case BIRCH_PLANKS: case JUNGLE_PLANKS: case ACACIA_PLANKS: case DARK_OAK_PLANKS:
		case BEDROCK: case SAND: case RED_SAND: case GRAVEL: case GOLD_ORE: case IRON_ORE: case COAL_ORE:
		case OAK_LOG: case SPRUCE_LOG: case BIRCH_LOG: case JUNGLE_LOG: case ACACIA_LOG: case DARK_OAK_LOG:
		case STRIPPED_OAK_LOG: case STRIPPED_SPRUCE_LOG: case STRIPPED_BIRCH_LOG: case STRIPPED_JUNGLE_LOG: case STRIPPED_ACACIA_LOG: case STRIPPED_DARK_OAK_LOG:
		case STRIPPED_OAK_WOOD: case STRIPPED_SPRUCE_WOOD: case STRIPPED_BIRCH_WOOD: case STRIPPED_JUNGLE_WOOD: case STRIPPED_ACACIA_WOOD: case STRIPPED_DARK_OAK_WOOD:
		case OAK_WOOD: case SPRUCE_WOOD: case BIRCH_WOOD: case JUNGLE_WOOD: case ACACIA_WOOD: case DARK_OAK_WOOD:
		case SPONGE: case WET_SPONGE: case GLASS: case LAPIS_ORE: case LAPIS_BLOCK:
		case SANDSTONE: case CHISELED_SANDSTONE: case CUT_SANDSTONE:
		case WHITE_WOOL: case ORANGE_WOOL: case MAGENTA_WOOL: case LIGHT_BLUE_WOOL: case YELLOW_WOOL: case LIME_WOOL: case PINK_WOOL: case GRAY_WOOL:
		case LIGHT_GRAY_WOOL: case CYAN_WOOL: case PURPLE_WOOL: case BLUE_WOOL: case BROWN_WOOL: case GREEN_WOOL: case RED_WOOL: case BLACK_WOOL:
		case GOLD_BLOCK: case IRON_BLOCK:
		case OAK_SLAB: case SPRUCE_SLAB: case BIRCH_SLAB: case JUNGLE_SLAB: case ACACIA_SLAB: case DARK_OAK_SLAB:
		case STONE_SLAB: case SANDSTONE_SLAB: case PETRIFIED_OAK_SLAB: case COBBLESTONE_SLAB: case BRICK_SLAB: case STONE_BRICK_SLAB:
		case NETHER_BRICK_SLAB: case QUARTZ_SLAB: case RED_SANDSTONE_SLAB: case PURPUR_SLAB: case PRISMARINE_SLAB: case PRISMARINE_BRICK_SLAB: case DARK_PRISMARINE_SLAB:
		case SMOOTH_QUARTZ: case SMOOTH_RED_SANDSTONE: case SMOOTH_SANDSTONE: case SMOOTH_STONE:
		case BRICKS: case BOOKSHELF: case MOSSY_COBBLESTONE: case OBSIDIAN:
		case PURPUR_BLOCK: case PURPUR_PILLAR: case PURPUR_STAIRS:
		case OAK_STAIRS: case DIAMOND_ORE: case DIAMOND_BLOCK: case COBBLESTONE_STAIRS: case REDSTONE_ORE:
		case ICE: case SNOW_BLOCK: case CLAY: case PUMPKIN: case CARVED_PUMPKIN: case NETHERRACK: case SOUL_SAND: case GLOWSTONE: case JACK_O_LANTERN:
		case STONE_BRICKS: case MOSSY_STONE_BRICKS: case CRACKED_STONE_BRICKS: case CHISELED_STONE_BRICKS:
		case MELON: case BRICK_STAIRS: case STONE_BRICK_STAIRS: case MYCELIUM: case NETHER_BRICKS: case NETHER_BRICK_STAIRS:
		case END_STONE: case END_STONE_BRICKS: case SANDSTONE_STAIRS: case EMERALD_ORE: case EMERALD_BLOCK:
		case SPRUCE_STAIRS: case BIRCH_STAIRS: case JUNGLE_STAIRS:
		case NETHER_QUARTZ_ORE: case CHISELED_QUARTZ_BLOCK: case QUARTZ_BLOCK: case QUARTZ_PILLAR: case QUARTZ_STAIRS:
		case WHITE_TERRACOTTA: case ORANGE_TERRACOTTA: case MAGENTA_TERRACOTTA: case LIGHT_BLUE_TERRACOTTA: case YELLOW_TERRACOTTA: case LIME_TERRACOTTA: case PINK_TERRACOTTA: case GRAY_TERRACOTTA:
		case LIGHT_GRAY_TERRACOTTA: case CYAN_TERRACOTTA: case PURPLE_TERRACOTTA: case BLUE_TERRACOTTA: case BROWN_TERRACOTTA: case GREEN_TERRACOTTA: case RED_TERRACOTTA: case BLACK_TERRACOTTA:
		case HAY_BLOCK: case TERRACOTTA: case COAL_BLOCK: case PACKED_ICE: case ACACIA_STAIRS: case DARK_OAK_STAIRS:
		case WHITE_STAINED_GLASS: case ORANGE_STAINED_GLASS: case MAGENTA_STAINED_GLASS: case LIGHT_BLUE_STAINED_GLASS: case YELLOW_STAINED_GLASS: case LIME_STAINED_GLASS: case PINK_STAINED_GLASS: case GRAY_STAINED_GLASS:
		case LIGHT_GRAY_STAINED_GLASS: case CYAN_STAINED_GLASS: case PURPLE_STAINED_GLASS: case BLUE_STAINED_GLASS: case BROWN_STAINED_GLASS: case GREEN_STAINED_GLASS: case RED_STAINED_GLASS: case BLACK_STAINED_GLASS:
		case PRISMARINE: case PRISMARINE_BRICKS: case DARK_PRISMARINE: case PRISMARINE_STAIRS: case PRISMARINE_BRICK_STAIRS: case DARK_PRISMARINE_STAIRS: case SEA_LANTERN:
		case RED_SANDSTONE: case CHISELED_RED_SANDSTONE: case CUT_RED_SANDSTONE: case RED_SANDSTONE_STAIRS:
		case MAGMA_BLOCK: case NETHER_WART_BLOCK: case RED_NETHER_BRICKS: case BONE_BLOCK:
		case WHITE_CONCRETE: case ORANGE_CONCRETE: case MAGENTA_CONCRETE: case LIGHT_BLUE_CONCRETE: case YELLOW_CONCRETE: case LIME_CONCRETE: case PINK_CONCRETE: case GRAY_CONCRETE:
		case LIGHT_GRAY_CONCRETE: case CYAN_CONCRETE: case PURPLE_CONCRETE: case BLUE_CONCRETE: case BROWN_CONCRETE: case GREEN_CONCRETE: case RED_CONCRETE: case BLACK_CONCRETE:
		case WHITE_CONCRETE_POWDER: case ORANGE_CONCRETE_POWDER: case MAGENTA_CONCRETE_POWDER: case LIGHT_BLUE_CONCRETE_POWDER: case YELLOW_CONCRETE_POWDER: case LIME_CONCRETE_POWDER: case PINK_CONCRETE_POWDER: case GRAY_CONCRETE_POWDER:
		case LIGHT_GRAY_CONCRETE_POWDER: case CYAN_CONCRETE_POWDER: case PURPLE_CONCRETE_POWDER: case BLUE_CONCRETE_POWDER: case BROWN_CONCRETE_POWDER: case GREEN_CONCRETE_POWDER: case RED_CONCRETE_POWDER: case BLACK_CONCRETE_POWDER:
		case DEAD_TUBE_CORAL_BLOCK: case DEAD_BRAIN_CORAL_BLOCK: case DEAD_BUBBLE_CORAL_BLOCK: case DEAD_FIRE_CORAL_BLOCK: case DEAD_HORN_CORAL_BLOCK: 
		case TUBE_CORAL_BLOCK: case BRAIN_CORAL_BLOCK: case BUBBLE_CORAL_BLOCK: case FIRE_CORAL_BLOCK: case HORN_CORAL_BLOCK: 
		case BLUE_ICE: case DRIED_KELP_BLOCK:
			return 1;
		case OAK_SAPLING: case SPRUCE_SAPLING: case BIRCH_SAPLING: case JUNGLE_SAPLING: case ACACIA_SAPLING: case DARK_OAK_SAPLING:
		case OAK_LEAVES: case SPRUCE_LEAVES: case BIRCH_LEAVES: case JUNGLE_LEAVES: case ACACIA_LEAVES: case DARK_OAK_LEAVES:
		case COBWEB: case GRASS: case FERN: case DEAD_BUSH: case SEAGRASS: case SEA_PICKLE:
		case DANDELION: case POPPY: case BLUE_ORCHID: case ALLIUM: case AZURE_BLUET: case RED_TULIP: case ORANGE_TULIP: case WHITE_TULIP: case PINK_TULIP: case OXEYE_DAISY:
		case BROWN_MUSHROOM: case RED_MUSHROOM: case TORCH: case END_ROD: case CHORUS_PLANT: case CHORUS_FLOWER: case CHEST: case CRAFTING_TABLE: case FARMLAND: case FURNACE:
		case LADDER: case SNOW: case CACTUS: case JUKEBOX:
		case OAK_FENCE: case SPRUCE_FENCE: case BIRCH_FENCE: case JUNGLE_FENCE: case ACACIA_FENCE: case DARK_OAK_FENCE:
		case INFESTED_STONE: case INFESTED_COBBLESTONE: case INFESTED_STONE_BRICKS: case INFESTED_MOSSY_STONE_BRICKS: case INFESTED_CRACKED_STONE_BRICKS: case INFESTED_CHISELED_STONE_BRICKS:
		case BROWN_MUSHROOM_BLOCK: case RED_MUSHROOM_BLOCK: case MUSHROOM_STEM: case IRON_BARS: case GLASS_PANE: case VINE: case LILY_PAD: case NETHER_BRICK_FENCE:
		case ENCHANTING_TABLE: case END_PORTAL_FRAME: case ENDER_CHEST: case COBBLESTONE_WALL: case MOSSY_COBBLESTONE_WALL: case ANVIL: case CHIPPED_ANVIL: case DAMAGED_ANVIL:
		case WHITE_CARPET: case ORANGE_CARPET: case MAGENTA_CARPET: case LIGHT_BLUE_CARPET: case YELLOW_CARPET: case LIME_CARPET: case PINK_CARPET: case GRAY_CARPET:
		case LIGHT_GRAY_CARPET: case CYAN_CARPET: case PURPLE_CARPET: case BLUE_CARPET: case BROWN_CARPET: case GREEN_CARPET: case RED_CARPET: case BLACK_CARPET:
		case SLIME_BLOCK: case GRASS_PATH: case SUNFLOWER: case LILAC: case ROSE_BUSH: case PEONY: case TALL_GRASS: case LARGE_FERN:
		case WHITE_STAINED_GLASS_PANE: case ORANGE_STAINED_GLASS_PANE: case MAGENTA_STAINED_GLASS_PANE: case LIGHT_BLUE_STAINED_GLASS_PANE: case YELLOW_STAINED_GLASS_PANE: case LIME_STAINED_GLASS_PANE: case PINK_STAINED_GLASS_PANE: case GRAY_STAINED_GLASS_PANE:
		case LIGHT_GRAY_STAINED_GLASS_PANE: case CYAN_STAINED_GLASS_PANE: case PURPLE_STAINED_GLASS_PANE: case BLUE_STAINED_GLASS_PANE: case BROWN_STAINED_GLASS_PANE: case GREEN_STAINED_GLASS_PANE: case RED_STAINED_GLASS_PANE: case BLACK_STAINED_GLASS_PANE:
		case WHITE_SHULKER_BOX: case ORANGE_SHULKER_BOX: case MAGENTA_SHULKER_BOX: case LIGHT_BLUE_SHULKER_BOX: case YELLOW_SHULKER_BOX: case LIME_SHULKER_BOX: case PINK_SHULKER_BOX: case GRAY_SHULKER_BOX:
		case LIGHT_GRAY_SHULKER_BOX: case CYAN_SHULKER_BOX: case PURPLE_SHULKER_BOX: case BLUE_SHULKER_BOX: case BROWN_SHULKER_BOX: case GREEN_SHULKER_BOX: case RED_SHULKER_BOX: case BLACK_SHULKER_BOX:
		case WHITE_GLAZED_TERRACOTTA: case ORANGE_GLAZED_TERRACOTTA: case MAGENTA_GLAZED_TERRACOTTA: case LIGHT_BLUE_GLAZED_TERRACOTTA: case YELLOW_GLAZED_TERRACOTTA: case LIME_GLAZED_TERRACOTTA: case PINK_GLAZED_TERRACOTTA: case GRAY_GLAZED_TERRACOTTA:
		case LIGHT_GRAY_GLAZED_TERRACOTTA: case CYAN_GLAZED_TERRACOTTA: case PURPLE_GLAZED_TERRACOTTA: case BLUE_GLAZED_TERRACOTTA: case BROWN_GLAZED_TERRACOTTA: case GREEN_GLAZED_TERRACOTTA: case RED_GLAZED_TERRACOTTA: case BLACK_GLAZED_TERRACOTTA:
		case TUBE_CORAL: case BRAIN_CORAL: case BUBBLE_CORAL: case FIRE_CORAL: case HORN_CORAL:
		case TUBE_CORAL_FAN: case BRAIN_CORAL_FAN: case BUBBLE_CORAL_FAN: case FIRE_CORAL_FAN: case HORN_CORAL_FAN:
		case PAINTING: case SIGN:
		case WHITE_BED: case ORANGE_BED: case MAGENTA_BED: case LIGHT_BLUE_BED: case YELLOW_BED: case LIME_BED: case PINK_BED: case GRAY_BED:
		case LIGHT_GRAY_BED: case CYAN_BED: case PURPLE_BED: case BLUE_BED: case BROWN_BED: case GREEN_BED: case RED_BED: case BLACK_BED:
		case ITEM_FRAME: case FLOWER_POT: case SKELETON_SKULL: case WITHER_SKELETON_SKULL: case PLAYER_HEAD: case ZOMBIE_HEAD: case CREEPER_HEAD: case DRAGON_HEAD: case ARMOR_STAND:
		case WHITE_BANNER: case ORANGE_BANNER: case MAGENTA_BANNER: case LIGHT_BLUE_BANNER: case YELLOW_BANNER: case LIME_BANNER: case PINK_BANNER: case GRAY_BANNER:
		case LIGHT_GRAY_BANNER: case CYAN_BANNER: case PURPLE_BANNER: case BLUE_BANNER: case BROWN_BANNER: case GREEN_BANNER: case RED_BANNER: case BLACK_BANNER:
		case END_CRYSTAL:
			return 2;
		case DISPENSER: case NOTE_BLOCK: case STICKY_PISTON: case PISTON: case TNT: case LEVER: case STONE_PRESSURE_PLATE:
		case OAK_PRESSURE_PLATE: case SPRUCE_PRESSURE_PLATE: case BIRCH_PRESSURE_PLATE: case JUNGLE_PRESSURE_PLATE: case ACACIA_PRESSURE_PLATE: case DARK_OAK_PRESSURE_PLATE:
		case REDSTONE_TORCH: case STONE_BUTTON:
		case OAK_TRAPDOOR: case SPRUCE_TRAPDOOR: case BIRCH_TRAPDOOR: case JUNGLE_TRAPDOOR: case ACACIA_TRAPDOOR: case DARK_OAK_TRAPDOOR:
		case OAK_FENCE_GATE: case SPRUCE_FENCE_GATE: case BIRCH_FENCE_GATE: case JUNGLE_FENCE_GATE: case ACACIA_FENCE_GATE: case DARK_OAK_FENCE_GATE:
		case REDSTONE_LAMP: case TRIPWIRE:
		case OAK_BUTTON: case SPRUCE_BUTTON: case BIRCH_BUTTON: case JUNGLE_BUTTON: case ACACIA_BUTTON: case DARK_OAK_BUTTON:
		case TRAPPED_CHEST: case LIGHT_WEIGHTED_PRESSURE_PLATE: case HEAVY_WEIGHTED_PRESSURE_PLATE: case DAYLIGHT_DETECTOR:
		case REDSTONE_BLOCK: case HOPPER: case DROPPER: case IRON_TRAPDOOR: case OBSERVER: case IRON_DOOR:
		case OAK_DOOR: case SPRUCE_DOOR: case BIRCH_DOOR: case JUNGLE_DOOR: case ACACIA_DOOR: case DARK_OAK_DOOR:
		case REPEATER: case COMPARATOR: case REDSTONE:
			return 3;
		case POWERED_RAIL: case DETECTOR_RAIL: case RAIL: case ACTIVATOR_RAIL: case MINECART: case SADDLE: case OAK_BOAT: case CHEST_MINECART: case FURNACE_MINECART:
		case CARROT_ON_A_STICK: case TNT_MINECART: case HOPPER_MINECART: case ELYTRA: case SPRUCE_BOAT: case BIRCH_BOAT: case JUNGLE_BOAT: case ACACIA_BOAT: case DARK_OAK_BOAT:
			return 4;
		case BEACON: case TURTLE_EGG: case CONDUIT: case SCUTE: case COAL: case CHARCOAL: case DIAMOND: case IRON_INGOT: case GOLD_INGOT:
		case STICK: case BOWL: case STRING: case FEATHER: case GUNPOWDER: case WHEAT_SEEDS: case WHEAT: case FLINT: case BUCKET:
		case WATER_BUCKET: case LAVA_BUCKET: case SNOWBALL: case LEATHER: case MILK_BUCKET: case PUFFERFISH_BUCKET: case SALMON_BUCKET: case COD_BUCKET: case TROPICAL_FISH_BUCKET:
		case BRICK: case CLAY_BALL: case SUGAR_CANE: case KELP: case PAPER: case BOOK: case SLIME_BALL: case EGG: case GLOWSTONE_DUST:
		case INK_SAC: case ROSE_RED: case CACTUS_GREEN: case COCOA_BEANS: case LAPIS_LAZULI: case PURPLE_DYE: case CYAN_DYE: case LIGHT_GRAY_DYE: case GRAY_DYE:
		case PINK_DYE: case LIME_DYE: case DANDELION_YELLOW: case LIGHT_BLUE_DYE: case MAGENTA_DYE: case ORANGE_DYE: case BONE_MEAL:
		case BONE: case SUGAR: case PUMPKIN_SEEDS: case MELON_SEEDS: case ENDER_PEARL: case BLAZE_ROD: case GOLD_NUGGET: case NETHER_WART: case ENDER_EYE:
		case BAT_SPAWN_EGG: case BLAZE_SPAWN_EGG: case CAVE_SPIDER_SPAWN_EGG: case CHICKEN_SPAWN_EGG: case COD_SPAWN_EGG: case COW_SPAWN_EGG: case CREEPER_SPAWN_EGG:
		case DOLPHIN_SPAWN_EGG: case DONKEY_SPAWN_EGG: case DROWNED_SPAWN_EGG: case ELDER_GUARDIAN_SPAWN_EGG: case ENDERMAN_SPAWN_EGG: case ENDERMITE_SPAWN_EGG:
		case EVOKER_SPAWN_EGG: case GHAST_SPAWN_EGG: case GUARDIAN_SPAWN_EGG: case HORSE_SPAWN_EGG: case HUSK_SPAWN_EGG: case LLAMA_SPAWN_EGG: case MAGMA_CUBE_SPAWN_EGG:
		case MOOSHROOM_SPAWN_EGG: case MULE_SPAWN_EGG: case OCELOT_SPAWN_EGG: case PARROT_SPAWN_EGG: case PHANTOM_SPAWN_EGG: case PIG_SPAWN_EGG: case POLAR_BEAR_SPAWN_EGG:
		case PUFFERFISH_SPAWN_EGG: case RABBIT_SPAWN_EGG: case SALMON_SPAWN_EGG: case SHEEP_SPAWN_EGG: case SHULKER_SPAWN_EGG: case SILVERFISH_SPAWN_EGG: case SKELETON_SPAWN_EGG:
		case SKELETON_HORSE_SPAWN_EGG: case SLIME_SPAWN_EGG: case SPIDER_SPAWN_EGG: case SQUID_SPAWN_EGG: case STRAY_SPAWN_EGG: case TROPICAL_FISH_SPAWN_EGG: case TURTLE_SPAWN_EGG:
		case VEX_SPAWN_EGG: case VILLAGER_SPAWN_EGG: case VINDICATOR_SPAWN_EGG: case WITCH_SPAWN_EGG: case WITHER_SKELETON_SPAWN_EGG: case WOLF_SPAWN_EGG: case ZOMBIE_SPAWN_EGG:
		case ZOMBIE_HORSE_SPAWN_EGG: case ZOMBIE_PIGMAN_SPAWN_EGG: case ZOMBIE_VILLAGER_SPAWN_EGG:
		case EXPERIENCE_BOTTLE: case FIRE_CHARGE: case WRITABLE_BOOK: case EMERALD: case MAP:
		case NETHER_STAR: case FIREWORK_ROCKET: case FIREWORK_STAR: case NETHER_BRICK: case QUARTZ: case PRISMARINE_SHARD: case PRISMARINE_CRYSTALS: case RABBIT_HIDE:
		case IRON_HORSE_ARMOR: case GOLDEN_HORSE_ARMOR: case DIAMOND_HORSE_ARMOR: case CHORUS_FRUIT: case POPPED_CHORUS_FRUIT: case BEETROOT_SEEDS: case SHULKER_SHELL: case IRON_NUGGET:
		case MUSIC_DISC_13: case MUSIC_DISC_CAT: case MUSIC_DISC_BLOCKS: case MUSIC_DISC_CHIRP: case MUSIC_DISC_FAR: case MUSIC_DISC_MALL: case MUSIC_DISC_MELLOHI:
		case MUSIC_DISC_STAL: case MUSIC_DISC_STRAD: case MUSIC_DISC_WARD: case MUSIC_DISC_11: case MUSIC_DISC_WAIT: case NAUTILUS_SHELL: case HEART_OF_THE_SEA:
			return 5;
		case APPLE: case MUSHROOM_STEW: case BREAD: case PORKCHOP: case COOKED_PORKCHOP: case GOLDEN_APPLE: case ENCHANTED_GOLDEN_APPLE:
		case COD: case SALMON: case TROPICAL_FISH: case PUFFERFISH: case COOKED_COD: case COOKED_SALMON:
		case CAKE: case COOKIE: case MELON_SLICE: case DRIED_KELP: case BEEF: case COOKED_BEEF: case CHICKEN: case COOKED_CHICKEN:
		case ROTTEN_FLESH: case SPIDER_EYE: case CARROT: case POTATO: case BAKED_POTATO: case POISONOUS_POTATO: case PUMPKIN_PIE:
		case RABBIT: case COOKED_RABBIT: case RABBIT_STEW: case MUTTON: case COOKED_MUTTON: case BEETROOT: case BEETROOT_SOUP:
			return 6;
		case IRON_SHOVEL: case IRON_PICKAXE: case IRON_AXE: case FLINT_AND_STEEL:
		case WOODEN_SHOVEL: case WOODEN_PICKAXE: case WOODEN_AXE:
		case STONE_SHOVEL: case STONE_PICKAXE: case STONE_AXE:
		case DIAMOND_SHOVEL: case DIAMOND_PICKAXE: case DIAMOND_AXE:
		case GOLDEN_SHOVEL: case GOLDEN_PICKAXE: case GOLDEN_AXE:
		case WOODEN_HOE: case STONE_HOE: case IRON_HOE: case DIAMOND_HOE: case GOLDEN_HOE:
		case COMPASS: case FISHING_ROD: case CLOCK: case SHEARS: case LEAD: case NAME_TAG:
			return 7;
		case TURTLE_HELMET: case BOW: case ARROW: case IRON_SWORD: case WOODEN_SWORD: case STONE_SWORD: case DIAMOND_SWORD: case GOLDEN_SWORD:
		case LEATHER_HELMET: case LEATHER_CHESTPLATE: case LEATHER_LEGGINGS: case LEATHER_BOOTS:
		case CHAINMAIL_HELMET: case CHAINMAIL_CHESTPLATE: case CHAINMAIL_LEGGINGS: case CHAINMAIL_BOOTS:
		case IRON_HELMET: case IRON_CHESTPLATE: case IRON_LEGGINGS: case IRON_BOOTS:
		case DIAMOND_HELMET: case DIAMOND_CHESTPLATE: case DIAMOND_LEGGINGS: case DIAMOND_BOOTS:
		case GOLDEN_HELMET: case GOLDEN_CHESTPLATE: case GOLDEN_LEGGINGS: case GOLDEN_BOOTS:
		case SPECTRAL_ARROW: case TIPPED_ARROW:
		case SHIELD: case TOTEM_OF_UNDYING: case TRIDENT:
			return 8;
		case GHAST_TEAR: case POTION: case GLASS_BOTTLE: case FERMENTED_SPIDER_EYE: case BLAZE_POWDER: case MAGMA_CREAM: case BREWING_STAND: case CAULDRON:
		case GLISTERING_MELON_SLICE: case GOLDEN_CARROT: case RABBIT_FOOT: case DRAGON_BREATH: case SPLASH_POTION: case LINGERING_POTION: case PHANTOM_MEMBRANE:
			return 9;
		default: //out of sections
			return 0;
		}
	}
}
