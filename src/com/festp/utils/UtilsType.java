package com.festp.utils;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

public class UtilsType {

	
	public static boolean isEndBiome(Biome b) {
		return b == Biome.THE_END || b == Biome.END_BARRENS || b == Biome.END_HIGHLANDS || b == Biome.END_MIDLANDS || b == Biome.SMALL_END_ISLANDS;
	}
	
	public static boolean isHead(Material m) {
		return m == Material.WITHER_SKELETON_SKULL || m == Material.SKELETON_SKULL || m == Material.CREEPER_HEAD
				|| m == Material.ZOMBIE_HEAD || m == Material.PLAYER_HEAD || m == Material.DRAGON_HEAD;
	}
	
	public static boolean isSlab(Material m) {
		if (m == null)
			return false;
		return m.toString().toLowerCase().contains("slab");
	}
	
	public static boolean isStairs(Material m) {
		if (m == null)
			return false;
		return m.toString().toLowerCase().contains("stairs");
	}
	
	public static boolean isWoodenTrapdoor(Material m) {
		switch(m) {
		case ACACIA_TRAPDOOR: return true;
		case BIRCH_TRAPDOOR: return true;
		case DARK_OAK_TRAPDOOR: return true;
		case JUNGLE_TRAPDOOR: return true;
		case OAK_TRAPDOOR: return true;
		case SPRUCE_TRAPDOOR: return true;
		}
		return false;
	}
	
	public static boolean isTrapdoor(Material m) {
		if(isWoodenTrapdoor(m) || m == Material.IRON_TRAPDOOR)
			return true;
		return false;
	}
	
	public static boolean isLog(Material m) {
		switch(m) {
		case ACACIA_LOG: return true;
		case BIRCH_LOG: return true;
		case DARK_OAK_LOG: return true;
		case JUNGLE_LOG: return true;
		case OAK_LOG: return true;
		case SPRUCE_LOG: return true;
		}
		return false;
	}
	
	public static boolean isSign(Material m) {
		switch(m) {
		case ACACIA_SIGN: return true;
		case BIRCH_SIGN: return true;
		case DARK_OAK_SIGN: return true;
		case JUNGLE_SIGN: return true;
		case OAK_SIGN: return true;
		case SPRUCE_SIGN: return true;
		}
		return false;
	}
	
	public static boolean isWallSign(Material m) {
		switch(m) {
		case ACACIA_WALL_SIGN: return true;
		case BIRCH_WALL_SIGN: return true;
		case DARK_OAK_WALL_SIGN: return true;
		case JUNGLE_WALL_SIGN: return true;
		case OAK_WALL_SIGN: return true;
		case SPRUCE_WALL_SIGN: return true;
		}
		return false;
	}
	
	public static boolean isWoodenDoor(Material m) {
		switch(m) {
		case ACACIA_DOOR: return true;
		case BIRCH_DOOR: return true;
		case DARK_OAK_DOOR: return true;
		case JUNGLE_DOOR: return true;
		case OAK_DOOR: return true;
		case SPRUCE_DOOR: return true;
		}
		return false;
	}
	
	public static boolean isDoor(Material m) {
		if(isWoodenDoor(m) || m == Material.IRON_DOOR)
			return true;
		return false;
	}
	
	public static boolean isWoodenPlate(Material m) {
		switch(m) {
		case ACACIA_PRESSURE_PLATE: return true;
		case BIRCH_PRESSURE_PLATE: return true;
		case DARK_OAK_PRESSURE_PLATE: return true;
		case JUNGLE_PRESSURE_PLATE: return true;
		case OAK_PRESSURE_PLATE: return true;
		case SPRUCE_PRESSURE_PLATE: return true;
		}
		return false;
	}
	
	public static boolean isPlate(Material m) {
		if(isWoodenDoor(m) || m == Material.LIGHT_WEIGHTED_PRESSURE_PLATE || m == Material.HEAVY_WEIGHTED_PRESSURE_PLATE)
			return true;
		return false;
	}
	
	public static boolean isWoodenButton(Material m) {
		switch(m) {
		case ACACIA_BUTTON: return true;
		case BIRCH_BUTTON: return true;
		case DARK_OAK_BUTTON: return true;
		case JUNGLE_BUTTON: return true;
		case OAK_BUTTON: return true;
		case SPRUCE_BUTTON: return true;
		}
		return false;
	}
	public static boolean isButton(Material m) {
		 return isWoodenButton(m) || m == Material.STONE_BUTTON;
	}
	
	public static boolean isWoodenFence(Material m) {
		switch(m) {
		case ACACIA_FENCE: return true;
		case BIRCH_FENCE: return true;
		case DARK_OAK_FENCE: return true;
		case JUNGLE_FENCE: return true;
		case OAK_FENCE: return true;
		case SPRUCE_FENCE: return true;
		}
		return false;
	}
	public static boolean isFence(Material m) {
		return isWoodenFence(m) || m == Material.NETHER_BRICK_FENCE;
	}
	public static boolean isGate(Material m) {
		switch(m) {
		case ACACIA_FENCE_GATE: return true;
		case BIRCH_FENCE_GATE: return true;
		case DARK_OAK_FENCE_GATE: return true;
		case JUNGLE_FENCE_GATE: return true;
		case OAK_FENCE_GATE: return true;
		case SPRUCE_FENCE_GATE: return true;
		}
		return false;
	}
	
	public static boolean isSapling(Material m) {
		switch(m) {
		case ACACIA_SAPLING: return true;
		case BIRCH_SAPLING: return true;
		case DARK_OAK_SAPLING: return true;
		case JUNGLE_SAPLING: return true;
		case OAK_SAPLING: return true;
		case SPRUCE_SAPLING: return true;
		}
		return false;
	}
	
	public static boolean isMushroom(Material m) {
		switch(m) {
		case BROWN_MUSHROOM: return true;
		case RED_MUSHROOM: return true;
		}
		return false;
	}
	
	public static boolean isFlowingLiquid(Block b) {
		if(b != null && b.isLiquid())
		{
			Levelled liquid = (Levelled) b.getState().getBlockData();
			//if(liquid.getLevel() < liquid.getMaximumLevel())
			if(liquid.getLevel() > 0)
				return true;
		}
		return false;
	}
	
	public static boolean isStationaryLiquid(Block b) {
		if(b != null && b.isLiquid())
		{
			Levelled liquid = (Levelled) b.getState().getBlockData();
			//if(liquid.getLevel() == liquid.getMaximumLevel())
			if(liquid.getLevel() == 0)
				return true;
		}
		return false;
	}
	
	public static boolean isAir(Material m) {
		return m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR;
	}
	
	public static boolean isTransparent(Material m) {
		if (isAir(m) || is_banner(m) || is_bed(m) || is_carpet(m) || is_wall_banner(m) || isTrapdoor(m) || isDoor(m) || isGate(m)
				|| is_flower(m) || isPlate(m) || isButton(m) || isRail(m)
				|| m == Material.TORCH || m == Material.WALL_TORCH|| m == Material.REDSTONE_TORCH || m == Material.REDSTONE_WALL_TORCH
				|| isSign(m) || isWallSign(m)
				|| m == Material.FLOWER_POT || m == Material.REDSTONE_WIRE || m == Material.COMPARATOR || m == Material.REPEATER || m == Material.LEVER
				|| m == Material.TRIPWIRE_HOOK || m == Material.TRIPWIRE
				|| m == Material.SNOW)
			return true;
		return false;
	}
	
	public static boolean playerCanStay(Block b) { // TODO: correct slabs
		return isTransparent(b.getType()) && isTransparent(b.getRelative(0, 1, 0).getType()) && !isTransparent(b.getRelative(0, -1, 0).getType());
	}
	
	public static boolean playerCanFlyOn(Block b) {
		return isTransparent(b.getRelative(0, 1, 0).getType()) && isTransparent(b.getRelative(0, 2, 0).getType());
	}
	
	public static boolean isInteractable(Material m) {
		return is_shulker_box(m) || isButton(m) || isWoodenDoor(m) || isGate(m) || isWoodenTrapdoor(m)
				|| m == Material.CHEST || m == Material.TRAPPED_CHEST || m == Material.ENDER_CHEST || m == Material.FURNACE || m == Material.CRAFTING_TABLE
				|| m == Material.DISPENSER || m == Material.DROPPER || m == Material.ENCHANTING_TABLE || m == Material.BREWING_STAND
				|| m == Material.ANVIL || m == Material.CHIPPED_ANVIL || m == Material.DAMAGED_ANVIL
				|| m == Material.HOPPER || m == Material.REPEATER || m == Material.COMPARATOR || m == Material.LEVER
				|| m == Material.LOOM || m == Material.CARTOGRAPHY_TABLE || m == Material.GRINDSTONE || m == Material.STONECUTTER || m == Material.BELL
				//|| m == Material.FLETCHING_TABLE || m == Material.SMITHING_TABLE
				|| m == Material.BARREL || m == Material.LECTERN || m == Material.SMOKER || m == Material.BLAST_FURNACE
				|| m == Material.COMMAND_BLOCK || m == Material.CHAIN_COMMAND_BLOCK || m == Material.REPEATING_COMMAND_BLOCK;
	}
	
	public static boolean stray_biome(Block b) {
		return b.getTemperature() < 0.05;
	}
	public static boolean husk_biome(Biome biome) {
		return biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.DESERT_LAKES;
	}
	public static boolean husk_biome(Block b) {
		return husk_biome(b.getBiome());
	}
	
	
	public static boolean is_banner(Material m) {
		switch(m) {
		case WHITE_BANNER: return true;
		case ORANGE_BANNER: return true;
		case MAGENTA_BANNER: return true;
		case LIGHT_BLUE_BANNER: return true;
		case YELLOW_BANNER: return true;
		case LIME_BANNER: return true;
		case PINK_BANNER: return true;
		case GRAY_BANNER: return true;
		case LIGHT_GRAY_BANNER: return true;
		case CYAN_BANNER: return true;
		case PURPLE_BANNER: return true;
		case BLUE_BANNER: return true;
		case BROWN_BANNER: return true;
		case GREEN_BANNER: return true;
		case RED_BANNER: return true;
		case BLACK_BANNER: return true;
		default: return false;
		}
	}
	
	public static boolean is_wall_banner(Material m) {
		switch(m) {
		case WHITE_WALL_BANNER: return true;
		case ORANGE_WALL_BANNER: return true;
		case MAGENTA_WALL_BANNER: return true;
		case LIGHT_BLUE_WALL_BANNER: return true;
		case YELLOW_WALL_BANNER: return true;
		case LIME_WALL_BANNER: return true;
		case PINK_WALL_BANNER: return true;
		case GRAY_WALL_BANNER: return true;
		case LIGHT_GRAY_WALL_BANNER: return true;
		case CYAN_WALL_BANNER: return true;
		case PURPLE_WALL_BANNER: return true;
		case BLUE_WALL_BANNER: return true;
		case BROWN_WALL_BANNER: return true;
		case GREEN_WALL_BANNER: return true;
		case RED_WALL_BANNER: return true;
		case BLACK_WALL_BANNER: return true;
		default: return false;
		}
	}
	
	public static boolean is_bed(Material m) {
		switch(m) {
		case WHITE_BED: return true;
		case ORANGE_BED: return true;
		case MAGENTA_BED: return true;
		case LIGHT_BLUE_BED: return true;
		case YELLOW_BED: return true;
		case LIME_BED: return true;
		case PINK_BED: return true;
		case GRAY_BED: return true;
		case LIGHT_GRAY_BED: return true;
		case CYAN_BED: return true;
		case PURPLE_BED: return true;
		case BLUE_BED: return true;
		case BROWN_BED: return true;
		case GREEN_BED: return true;
		case RED_BED: return true;
		case BLACK_BED: return true;
		default: return false;
		}
	}
	
	public static boolean is_carpet(Material m) {
		switch(m) {
		case WHITE_CARPET: return true;
		case ORANGE_CARPET: return true;
		case MAGENTA_CARPET: return true;
		case LIGHT_BLUE_CARPET: return true;
		case YELLOW_CARPET: return true;
		case LIME_CARPET: return true;
		case PINK_CARPET: return true;
		case GRAY_CARPET: return true;
		case LIGHT_GRAY_CARPET: return true;
		case CYAN_CARPET: return true;
		case PURPLE_CARPET: return true;
		case BLUE_CARPET: return true;
		case BROWN_CARPET: return true;
		case GREEN_CARPET: return true;
		case RED_CARPET: return true;
		case BLACK_CARPET: return true;
		default: return false;
		}
	}
	
	public static boolean is_concrete(Material m) {
		switch(m) {
		case WHITE_CONCRETE: return true;
		case ORANGE_CONCRETE: return true;
		case MAGENTA_CONCRETE: return true;
		case LIGHT_BLUE_CONCRETE: return true;
		case YELLOW_CONCRETE: return true;
		case LIME_CONCRETE: return true;
		case PINK_CONCRETE: return true;
		case GRAY_CONCRETE: return true;
		case LIGHT_GRAY_CONCRETE: return true;
		case CYAN_CONCRETE: return true;
		case PURPLE_CONCRETE: return true;
		case BLUE_CONCRETE: return true;
		case BROWN_CONCRETE: return true;
		case GREEN_CONCRETE: return true;
		case RED_CONCRETE: return true;
		case BLACK_CONCRETE: return true;
		default: return false;
		}
	}
	
	public static boolean is_concrete_powder(Material m) {
		switch(m) {
		case WHITE_CONCRETE_POWDER: return true;
		case ORANGE_CONCRETE_POWDER: return true;
		case MAGENTA_CONCRETE_POWDER: return true;
		case LIGHT_BLUE_CONCRETE_POWDER: return true;
		case YELLOW_CONCRETE_POWDER: return true;
		case LIME_CONCRETE_POWDER: return true;
		case PINK_CONCRETE_POWDER: return true;
		case GRAY_CONCRETE_POWDER: return true;
		case LIGHT_GRAY_CONCRETE_POWDER: return true;
		case CYAN_CONCRETE_POWDER: return true;
		case PURPLE_CONCRETE_POWDER: return true;
		case BLUE_CONCRETE_POWDER: return true;
		case BROWN_CONCRETE_POWDER: return true;
		case GREEN_CONCRETE_POWDER: return true;
		case RED_CONCRETE_POWDER: return true;
		case BLACK_CONCRETE_POWDER: return true;
		default: return false;
		}
	}
	
	public static boolean is_stained_glass(Material m) {
		switch(m) {
		case WHITE_STAINED_GLASS: return true;
		case ORANGE_STAINED_GLASS: return true;
		case MAGENTA_STAINED_GLASS: return true;
		case LIGHT_BLUE_STAINED_GLASS: return true;
		case YELLOW_STAINED_GLASS: return true;
		case LIME_STAINED_GLASS: return true;
		case PINK_STAINED_GLASS: return true;
		case GRAY_STAINED_GLASS: return true;
		case LIGHT_GRAY_STAINED_GLASS: return true;
		case CYAN_STAINED_GLASS: return true;
		case PURPLE_STAINED_GLASS: return true;
		case BLUE_STAINED_GLASS: return true;
		case BROWN_STAINED_GLASS: return true;
		case GREEN_STAINED_GLASS: return true;
		case RED_STAINED_GLASS: return true;
		case BLACK_STAINED_GLASS: return true;
		default: return false;
		}
	}
	
	public static boolean is_stained_glass_pane(Material m) {
		switch(m) {
		case WHITE_STAINED_GLASS_PANE: return true;
		case ORANGE_STAINED_GLASS_PANE: return true;
		case MAGENTA_STAINED_GLASS_PANE: return true;
		case LIGHT_BLUE_STAINED_GLASS_PANE: return true;
		case YELLOW_STAINED_GLASS_PANE: return true;
		case LIME_STAINED_GLASS_PANE: return true;
		case PINK_STAINED_GLASS_PANE: return true;
		case GRAY_STAINED_GLASS_PANE: return true;
		case LIGHT_GRAY_STAINED_GLASS_PANE: return true;
		case CYAN_STAINED_GLASS_PANE: return true;
		case PURPLE_STAINED_GLASS_PANE: return true;
		case BLUE_STAINED_GLASS_PANE: return true;
		case BROWN_STAINED_GLASS_PANE: return true;
		case GREEN_STAINED_GLASS_PANE: return true;
		case RED_STAINED_GLASS_PANE: return true;
		case BLACK_STAINED_GLASS_PANE: return true;
		default: return false;
		}
	}
	
	public static boolean is_wool(Material m) {
		switch(m) {
		case WHITE_WOOL: return true;
		case ORANGE_WOOL: return true;
		case MAGENTA_WOOL: return true;
		case LIGHT_BLUE_WOOL: return true;
		case YELLOW_WOOL: return true;
		case LIME_WOOL: return true;
		case PINK_WOOL: return true;
		case GRAY_WOOL: return true;
		case LIGHT_GRAY_WOOL: return true;
		case CYAN_WOOL: return true;
		case PURPLE_WOOL: return true;
		case BLUE_WOOL: return true;
		case BROWN_WOOL: return true;
		case GREEN_WOOL: return true;
		case RED_WOOL: return true;
		case BLACK_WOOL: return true;
		default: return false;
		}
	}
	
	public static boolean is_colored_shulker_box(Material m) {
		switch(m) {
		case WHITE_SHULKER_BOX: return true;
		case ORANGE_SHULKER_BOX: return true;
		case MAGENTA_SHULKER_BOX: return true;
		case LIGHT_BLUE_SHULKER_BOX: return true;
		case YELLOW_SHULKER_BOX: return true;
		case LIME_SHULKER_BOX: return true;
		case PINK_SHULKER_BOX: return true;
		case GRAY_SHULKER_BOX: return true;
		case LIGHT_GRAY_SHULKER_BOX: return true;
		case CYAN_SHULKER_BOX: return true;
		case PURPLE_SHULKER_BOX: return true;
		case BLUE_SHULKER_BOX: return true;
		case BROWN_SHULKER_BOX: return true;
		case GREEN_SHULKER_BOX: return true;
		case RED_SHULKER_BOX: return true;
		case BLACK_SHULKER_BOX: return true;
		default: return false;
		}
	}
	
	public static boolean is_colored_terracotta(Material m) {
		switch(m) {
		case WHITE_TERRACOTTA: return true;
		case ORANGE_TERRACOTTA: return true;
		case MAGENTA_TERRACOTTA: return true;
		case LIGHT_BLUE_TERRACOTTA: return true;
		case YELLOW_TERRACOTTA: return true;
		case LIME_TERRACOTTA: return true;
		case PINK_TERRACOTTA: return true;
		case GRAY_TERRACOTTA: return true;
		case LIGHT_GRAY_TERRACOTTA: return true;
		case CYAN_TERRACOTTA: return true;
		case PURPLE_TERRACOTTA: return true;
		case BLUE_TERRACOTTA: return true;
		case BROWN_TERRACOTTA: return true;
		case GREEN_TERRACOTTA: return true;
		case RED_TERRACOTTA: return true;
		case BLACK_TERRACOTTA: return true;
		default: return false;
		}
	}
	
	public static boolean is_shulker_box(Material m) {
		if(is_colored_shulker_box(m) || m.equals(Material.SHULKER_BOX)) return true;
		return false;
	}
	
	public static boolean is_terracotta(Material m) {
		if(is_colored_terracotta(m) || m.equals(Material.TERRACOTTA)) return true;
		return false;
	}

	public static boolean is_glazed_terracotta(Material m) {
		switch(m) {
		case WHITE_GLAZED_TERRACOTTA: return true;
		case ORANGE_GLAZED_TERRACOTTA: return true;
		case MAGENTA_GLAZED_TERRACOTTA: return true;
		case LIGHT_BLUE_GLAZED_TERRACOTTA: return true;
		case YELLOW_GLAZED_TERRACOTTA: return true;
		case LIME_GLAZED_TERRACOTTA: return true;
		case PINK_GLAZED_TERRACOTTA: return true;
		case GRAY_GLAZED_TERRACOTTA: return true;
		case LIGHT_GRAY_GLAZED_TERRACOTTA: return true;
		case CYAN_GLAZED_TERRACOTTA: return true;
		case PURPLE_GLAZED_TERRACOTTA: return true;
		case BLUE_GLAZED_TERRACOTTA: return true;
		case BROWN_GLAZED_TERRACOTTA: return true;
		case GREEN_GLAZED_TERRACOTTA: return true;
		case RED_GLAZED_TERRACOTTA: return true;
		case BLACK_GLAZED_TERRACOTTA: return true;
		default: return false;
		}
	}
	
	public static boolean is_dye(Material m) {
		switch(m) {
		case WHITE_DYE: return true;
		case ORANGE_DYE: return true;
		case MAGENTA_DYE: return true;
		case LIGHT_BLUE_DYE: return true;
		case YELLOW_DYE: return true;
		case LIME_DYE: return true;
		case PINK_DYE: return true;
		case GRAY_DYE: return true;
		case LIGHT_GRAY_DYE: return true;
		case CYAN_DYE: return true;
		case PURPLE_DYE: return true;
		case BLUE_DYE: return true;
		case BROWN_DYE: return true;
		case GREEN_DYE: return true;
		case RED_DYE: return true;
		case BLACK_DYE: return true;
		default: return false;
		}
	}
	
	public static boolean is_flower(Material m) {
		switch(m) {
		case SUNFLOWER:
		case WHITE_TULIP:
		case ORANGE_TULIP:
		case PINK_TULIP:
		case RED_TULIP:
		case DANDELION:
		case ROSE_BUSH:
		case POPPY:
		case BLUE_ORCHID:
		case ALLIUM:
		case AZURE_BLUET:
		case OXEYE_DAISY:
		case LILAC:
		case PEONY:
		case WITHER_ROSE:
		case CORNFLOWER:
		case LILY_OF_THE_VALLEY:
			return true;
		default: return false;
		}
	}
	public static boolean isRail(Material m) {
		switch(m) {
		case RAIL: return true;
		case ACTIVATOR_RAIL: return true;
		case POWERED_RAIL: return true;
		case DETECTOR_RAIL: return true;
		default: return false;
		}
	}

	public static boolean isPlant(Material m) {
		return is_flower(m) || isMushroom(m) || isSapling(m) || m == Material.DEAD_BUSH || m == Material.VINE || m == Material.LILY_PAD
				|| m == Material.GRASS|| m == Material.TALL_GRASS || m == Material.FERN || m == Material.LARGE_FERN;
	}

	
	//Sort Hoppers - ARMOR, TOOL, WEAPON
	public static boolean isHorseArmor(Material m) {
		return m == Material.IRON_HORSE_ARMOR || m == Material.GOLDEN_HORSE_ARMOR || m == Material.DIAMOND_HORSE_ARMOR;
	}
	public static boolean isArmor(Material m) {
		return isBoots(m) || isLeggings(m) || isChestplate(m) || isHelmet(m);
	}
	public static boolean isBoots(Material m) {
		switch(m) {
		case LEATHER_BOOTS: return true;
		case GOLDEN_BOOTS: return true;
		case CHAINMAIL_BOOTS: return true;
		case IRON_BOOTS: return true;
		case DIAMOND_BOOTS: return true;
		default: return false; }
	}
	public static boolean isLeggings(Material m) {
		switch(m) {
		case LEATHER_LEGGINGS: return true;
		case GOLDEN_LEGGINGS: return true;
		case CHAINMAIL_LEGGINGS: return true;
		case IRON_LEGGINGS: return true;
		case DIAMOND_LEGGINGS: return true;
		default: return false; }
	}
	public static boolean isChestplate(Material m) {
		switch(m) {
		case LEATHER_CHESTPLATE: return true;
		case GOLDEN_CHESTPLATE: return true;
		case CHAINMAIL_CHESTPLATE: return true;
		case IRON_CHESTPLATE: return true;
		case DIAMOND_CHESTPLATE: return true;
		default: return false; }
	}
	public static boolean isHelmet(Material m) {
		switch(m) {
		case TURTLE_HELMET: return true;
		case LEATHER_HELMET: return true;
		case GOLDEN_HELMET: return true;
		case CHAINMAIL_HELMET: return true;
		case IRON_HELMET: return true;
		case DIAMOND_HELMET: return true;
		default: return false; }
	}

	public static boolean isTool(Material m) {
		return isPickaxe(m) || isShovel(m) || isAxe(m) || isHoe(m)
				|| m == Material.FLINT_AND_STEEL || m == Material.SHEARS || m == Material.FISHING_ROD;
	}
	public static boolean isPickaxe(Material m) {
		switch(m) {
		case WOODEN_PICKAXE: return true;
		case STONE_PICKAXE: return true;
		case GOLDEN_PICKAXE: return true;
		case IRON_PICKAXE: return true;
		case DIAMOND_PICKAXE: return true;
		default: return false; }
	}
	public static boolean isShovel(Material m) {
		switch(m) {
		case WOODEN_SHOVEL: return true;
		case STONE_SHOVEL: return true;
		case GOLDEN_SHOVEL: return true;
		case IRON_SHOVEL: return true;
		case DIAMOND_SHOVEL: return true;
		default: return false; }
	}
	public static boolean isAxe(Material m) {
		switch(m) {
		case WOODEN_AXE: return true;
		case STONE_AXE: return true;
		case GOLDEN_AXE: return true;
		case IRON_AXE: return true;
		case DIAMOND_AXE: return true;
		default: return false; }
	}
	public static boolean isHoe(Material m) {
		switch(m) {
		case WOODEN_HOE: return true;
		case STONE_HOE: return true;
		case GOLDEN_HOE: return true;
		case IRON_HOE: return true;
		case DIAMOND_HOE: return true;
		default: return false; }
	}

	public static boolean isWeapon(Material m) {
		return isSword(m)
				|| m == Material.BOW || m == Material.TRIDENT;
	}
	public static boolean isSword(Material m) {
		switch(m) {
		case WOODEN_SWORD: return true;
		case STONE_SWORD: return true;
		case GOLDEN_SWORD: return true;
		case IRON_SWORD: return true;
		case DIAMOND_SWORD: return true;
		default: return false; }
	}
}
