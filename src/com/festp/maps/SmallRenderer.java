package com.festp.maps;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.bukkit.Axis;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Slab.Type;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.festp.DelayedTask;
import com.festp.TaskList;
import com.festp.utils.Utils;
import com.festp.utils.UtilsColor;
import com.festp.utils.UtilsType;

public class SmallRenderer extends MapRenderer {
	
	private static final Color DEFAULT_SAVE_COLOR = Color.MAGENTA;
	// main color instances
	private static final Material SAND = Material.SAND,
			CLOTH = Material.COBWEB,
			FOLIAGE = Material.VINE,
			WHITE = Material.SNOW_BLOCK, //SNOW
			STONE = Material.STONE,
			WATER = Material.WATER,
			WOOD = Material.OAK_PLANKS,
			QUARTZ = Material.QUARTZ_BLOCK,
			ORANGE = Material.ORANGE_WOOL,
			MAGENTA = Material.MAGENTA_WOOL,
			LIGHT_BLUE = Material.LIGHT_BLUE_WOOL,
			YELLOW = Material.YELLOW_WOOL,
			LIME = Material.LIME_WOOL,
			PINK = Material.PINK_WOOL,
			GRAY = Material.GRAY_WOOL,
			SILVER = Material.LIGHT_GRAY_WOOL,
			CYAN = Material.CYAN_WOOL,
			PURPLE = Material.PURPLE_WOOL,
			BLUE = Material.BLUE_WOOL,
			BROWN = Material.BROWN_WOOL,
			GREEN = Material.GREEN_WOOL,
			RED = Material.RED_WOOL,
			BLACK = Material.BLACK_WOOL,
			SPRUCE_BROWN = Material.SPRUCE_PLANKS;
	
	static final int RENDER_DISTANCE_SQUARED = 128 * 128;
	static final int SAVE_TICKS = 20 * 10;

	private int save_ticks = 0;
	private Block last_color_block;
	
	boolean init;
	SmallMap map;
	BufferedImage image;
	DelayedTask saveTask = null;
	
	public SmallRenderer(SmallMap map) {
		this.map = map;
	}
	
	public void renderImage(BufferedImage image) {
		init = true;
		this.image = image;
	}
	
	@Override
	public void render(MapView view, MapCanvas canvas, Player player)
	{
		if (init) {
			canvas.drawImage(0, 0, image);
			for (int x = 0; x < 128; x++)
				for (int z = 0; z < 128; z++)
					if (image.getRGB(0, 0) == 0)
						canvas.setPixel(x, z, (byte) 0);
			image = null;
			init = false;
			return;
		}
		
		Integer main_id = getId(player.getInventory().getItemInMainHand());
		if (main_id == null || main_id != view.getId())
		{
			Integer off_id = getId(player.getInventory().getItemInOffHand());
			if (off_id == null || off_id != view.getId())
				return;
		}
		//canvas.setCursors(null);new MapCursorCollection()
		int player_x = player.getLocation().getBlockX();
		int player_z = player.getLocation().getBlockZ();
		
		int scale = map.getScale();
		int blocks = 128 / scale;
		int min_x = map.getX();
		int min_z = map.getZ();
		for (int x = 0; x < blocks; x++)
		{
			// top block pseudorender
			getColor(view.getWorld(), min_x + x, min_z - 1);
			for (int z = 0; z < blocks; z++)
			{
				int real_x = min_x + x,
					real_z = min_z + z;
				int x_dist = player_x - real_x,
					z_dist = player_z - real_z;
				if (x_dist * x_dist + z_dist * z_dist > RENDER_DISTANCE_SQUARED)
					continue;
				
				// brighter block, darker under block
				int last_y = last_color_block.getY();
				byte color = getColor(view.getWorld(), real_x, real_z);
				if (color == getColor(WATER))
				{
					// count water blocks
					Block b = last_color_block;
					int depth = 0;
					while (b.getType() == Material.WATER && b.getY() > 0) {
						depth++;
						b = b.getRelative(BlockFace.DOWN);
					}
					if (depth < 3)
						color += 1; // brighter
					else if (depth > 6)
						color -= 1; // darker
					//some ununderstandable specific
				}
				else
				{
					int y = last_color_block.getY();
					if (last_y < y)
						color += 1; // brighter
					else if (last_y > y)
						color -= 1; // darker
				}
				
				int px_x = x * scale;
				int px_z = z * scale;
				for (int dx = 0; dx < scale; dx++)
					for (int dz = 0; dz < scale; dz++)
						canvas.setPixel(px_x + dx, px_z + dz, color);
			}
		}
		
		// TODO new save system
		save_ticks++;
		if (save_ticks >= SAVE_TICKS)
		{
			save_ticks = 0;
			save(canvas);
			
			if (saveTask != null) {
				saveTask.terminate();
			}
			saveTask = new DelayedTask(SAVE_TICKS * 2, new Runnable() {
				@Override public void run() {
					save(canvas);
				}
			});
			TaskList.add(saveTask);
		}
	}
	
	public void save(MapCanvas canvas) {
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		
		for (int x = 0; x < 128; x++)
			for (int z = 0; z < 128; z++)
			{
				byte map_color = canvas.getPixel(x, z);
				Color color = DEFAULT_SAVE_COLOR;
				try {
					color = MapPalette.getColor(map_color);
				} catch (Exception e) {
					Utils.printError("Map saving color error: color " + map_color);
				}
				image.setRGB(x, z, color.getRGB());
			}
		
		SmallMapFileManager.saveImage(map.getId(), image);
	}
	
	public Integer getId(ItemStack map)
	{
		if (map == null || map.getType() != Material.FILLED_MAP)
			return null;

		return Utils.getInt(map, "map");
	}
	
	/** Ignores height difference (shadows)*/
	public byte getColor(World world, int x, int z)
	{
		//Block b = world.getHighestBlockAt(x, z); // ignores cobwebs
		Block b = world.getBlockAt(x, 255, z);
		Block next = b.getRelative(BlockFace.DOWN);
		while (next.getY() > 0 && UtilsType.isAir(next.getType())) {
			b = next;
			next = b.getRelative(BlockFace.DOWN);
		}
		
		byte color = getColor(b);
		
		boolean transparent = false;
		if (color == 0)
			transparent = true;
		
		while (color >= 0 && color < 4 && b.getY() > 0)
		{
			b = b.getRelative(BlockFace.DOWN);
			color = getColor(b);
			if (color == 0)
				transparent = true;
		}
		
		last_color_block = b;
		if (!transparent && color >= 0 && color < 4)
			return getColor(STONE);
		return color;
	}
	
	/** Emit WorldMap work in CraftMapRenderer */
	public static byte getColor(Block b)
	{
		Material material = b.getType();
		
		BlockData block_data = b.getBlockData();
		if (UtilsType.is_banner(material) || UtilsType.is_wall_banner(material)) {
			material = WOOD;
		} else if (UtilsType.is_flower(material)) {
			material = FOLIAGE;
		} else if (UtilsType.is_bed(material)) {
			Bed bed = (Bed)block_data;
			Part part = bed.getPart();
			if (part == Part.HEAD) {
				material = CLOTH;
			}
			else if (part == Part.FOOT) {
				DyeColor bed_color = UtilsColor.colorFromMaterial(material);
				material = getMaterial(bed_color);
			}
		} else if (UtilsType.is_shulker_box(material)) {
			DyeColor color = UtilsColor.colorFromMaterial(material);
			material = getMaterial(color);
		} else if (UtilsType.isLog(material)) {
			Orientable log = (Orientable)block_data;
			if (log.getAxis() != Axis.Y) {
				if (material == Material.ACACIA_LOG)
					material = STONE;
				else if (material == Material.BIRCH_LOG)
					material = QUARTZ;
				else if (material == Material.DARK_OAK_LOG || material == Material.SPRUCE_LOG)
					material = BROWN;
				else if (material == Material.OAK_LOG || material == Material.JUNGLE_LOG)
					material = SPRUCE_BROWN;
			}
		} else if (material == Material.MUSHROOM_STEM
				|| material == Material.BROWN_MUSHROOM_BLOCK || material == Material.RED_MUSHROOM_BLOCK) {
			MultipleFacing mushroom = (MultipleFacing)block_data;
			if (!mushroom.hasFace(BlockFace.UP))
				material = SAND;
		} else if (material == Material.MUSHROOM_STEM
				|| material == Material.BROWN_MUSHROOM_BLOCK || material == Material.RED_MUSHROOM_BLOCK) {
			MultipleFacing mushroom = (MultipleFacing)block_data;
			if (!mushroom.hasFace(BlockFace.UP))
				material = SAND;
		} else if (block_data instanceof Waterlogged) {
			material = afterWaterRender(material, block_data);
		}
		
		return getColor(material);
	}
	
	public static Material afterWaterRender(Material material, BlockData waterlogged) {
		BlockData block_data = waterlogged;
		Waterlogged watered = (Waterlogged)block_data;
		if (watered.isWaterlogged())
		{
			if (block_data instanceof Bisected)
			{
				Bisected bis = (Bisected)block_data;
				if (bis.getHalf() == Half.TOP)
				{
					if (block_data instanceof TrapDoor)
					{
						TrapDoor trapdoor = (TrapDoor)block_data;
						if (!trapdoor.isOpen())
							return material;
					}
					else if (block_data instanceof Stairs)
						return material;
				}
			}
			else if (block_data instanceof Slab)
			{
				Slab slab = (Slab)block_data;
				if (slab.getType() == Type.TOP)
					return material;
			}
			return WATER;
		}
		return material;
	}
	
	public static byte getColor(Material material)
	{
		int max_color = (58+1) * 4;
		int color = max_color;
		switch (material)
		{
		// TRANSPARENT GRAY
		case AIR: case CAVE_AIR: case VOID_AIR:
			color = 1;
			break;
		// GRASS
		case GRASS_BLOCK: case SLIME_BLOCK:
			color -= 4;
		// SAND
		case SAND: case SANDSTONE: case SANDSTONE_SLAB: case SANDSTONE_STAIRS: case SANDSTONE_WALL: case CHISELED_SANDSTONE: case CUT_SANDSTONE: case CUT_SANDSTONE_SLAB:
		case SMOOTH_SANDSTONE: case SMOOTH_SANDSTONE_SLAB: case SMOOTH_SANDSTONE_STAIRS:
		case BIRCH_LOG: /*vertical*/ case STRIPPED_BIRCH_LOG: case STRIPPED_BIRCH_WOOD: case BIRCH_PLANKS: case BIRCH_STAIRS: case BIRCH_SLAB: /*mushrooms, pores facing up*/ case BONE_BLOCK: case GLOWSTONE:
		case BIRCH_DOOR: case BIRCH_FENCE_GATE: case BIRCH_FENCE:
		case BIRCH_TRAPDOOR: case BIRCH_WALL_SIGN: case BIRCH_SIGN: case BIRCH_PRESSURE_PLATE:
		case END_STONE: case END_STONE_BRICKS: case END_STONE_BRICK_SLAB: case END_STONE_BRICK_STAIRS: case END_STONE_BRICK_WALL:
		case TURTLE_EGG: case SCAFFOLDING:
			color -= 4;
		// CLOTH
		case MUSHROOM_STEM: /*stem facing up*/ case COBWEB: /*bed head*/
			color -= 4;
		// TNT
		case TNT: case REDSTONE_BLOCK: case LAVA: case FIRE:
			color -= 4;
		// ICE
		case ICE: case FROSTED_ICE: case PACKED_ICE: case BLUE_ICE:
			color -= 4;
		// IRON
		case IRON_BLOCK: case IRON_DOOR: case IRON_TRAPDOOR: case HEAVY_WEIGHTED_PRESSURE_PLATE:
		case ANVIL: case CHIPPED_ANVIL: case DAMAGED_ANVIL: case BREWING_STAND: 
		case GRINDSTONE: case LODESTONE: case LANTERN: case SOUL_LANTERN:
			color -= 4;
		// FOLIAGE
		case ACACIA_LEAVES: case BIRCH_LEAVES: case DARK_OAK_LEAVES: case JUNGLE_LEAVES: case OAK_LEAVES: case SPRUCE_LEAVES:
		case ACACIA_SAPLING: case BIRCH_SAPLING: case DARK_OAK_SAPLING: case JUNGLE_SAPLING: case OAK_SAPLING: case SPRUCE_SAPLING:
		case BAMBOO: case GRASS: case TALL_GRASS: /*flowers*/ case FERN: case LARGE_FERN:
		case LILY_PAD: case WHEAT: case CARROTS: case POTATOES: case BEETROOTS: case SUGAR_CANE:
		case PUMPKIN_STEM: case MELON_STEM: case ATTACHED_PUMPKIN_STEM: case ATTACHED_MELON_STEM:
		case CACTUS: case VINE: case COCOA: case SWEET_BERRY_BUSH:
			color -= 4;
		// SNOW
		case WHITE_WOOL: case WHITE_CARPET: case SNOW_BLOCK: case SNOW: case WHITE_STAINED_GLASS: case WHITE_STAINED_GLASS_PANE:
		case WHITE_GLAZED_TERRACOTTA: case WHITE_CONCRETE: case WHITE_CONCRETE_POWDER:
			color -= 4;
		// CLAY
		case CLAY: case INFESTED_STONE: case INFESTED_COBBLESTONE:
		case INFESTED_STONE_BRICKS: case INFESTED_CHISELED_STONE_BRICKS: case INFESTED_CRACKED_STONE_BRICKS: case INFESTED_MOSSY_STONE_BRICKS:
			color -= 4;
		// DIRT (10)
		case JUNGLE_LOG: /*vertical*/ case STRIPPED_JUNGLE_LOG: case STRIPPED_JUNGLE_WOOD: case JUNGLE_PLANKS: case JUNGLE_STAIRS: case JUNGLE_SLAB:
		case JUNGLE_DOOR: case JUNGLE_FENCE_GATE: case JUNGLE_FENCE:
		case JUNGLE_TRAPDOOR: case JUNGLE_WALL_SIGN: case JUNGLE_SIGN: case JUNGLE_PRESSURE_PLATE:
		case DIRT: case COARSE_DIRT: case FARMLAND: case GRASS_PATH: case JUKEBOX: /*mushrooms, cap facing up*/ case BROWN_MUSHROOM_BLOCK:
		case GRANITE: case GRANITE_SLAB: case GRANITE_STAIRS: case GRANITE_WALL: case POLISHED_GRANITE: case POLISHED_GRANITE_SLAB: case POLISHED_GRANITE_STAIRS:
			color -= 4;
		// STONE
		case STONE: case STONE_SLAB: case STONE_STAIRS: case SMOOTH_STONE: case SMOOTH_STONE_SLAB:
		case COBBLESTONE: case COBBLESTONE_SLAB: case COBBLESTONE_STAIRS: case COBBLESTONE_WALL:
		case MOSSY_COBBLESTONE: case MOSSY_COBBLESTONE_SLAB: case MOSSY_COBBLESTONE_STAIRS: case MOSSY_COBBLESTONE_WALL:
		case STONE_BRICKS: case STONE_BRICK_SLAB: case STONE_BRICK_STAIRS: case STONE_BRICK_WALL: case CHISELED_STONE_BRICKS: case CRACKED_STONE_BRICKS:
		case MOSSY_STONE_BRICKS: case MOSSY_STONE_BRICK_SLAB: case MOSSY_STONE_BRICK_STAIRS: case MOSSY_STONE_BRICK_WALL:
		case ANDESITE: case ANDESITE_SLAB: case ANDESITE_STAIRS: case ANDESITE_WALL: case POLISHED_ANDESITE: case POLISHED_ANDESITE_SLAB: case POLISHED_ANDESITE_STAIRS:
		case GRAVEL: case BEDROCK: /*horizontal acacia*/  case ACACIA_WOOD:
		case COAL_ORE: case IRON_ORE: case GOLD_ORE: case REDSTONE_ORE: case LAPIS_ORE: case DIAMOND_ORE: case EMERALD_ORE:
		case STONE_PRESSURE_PLATE: case CAULDRON: case PISTON: case PISTON_HEAD: case STICKY_PISTON: case ENDER_CHEST: case SPAWNER:
		case FURNACE: case DISPENSER: case DROPPER: case HOPPER: case OBSERVER: case SMOKER: case STONECUTTER: case BLAST_FURNACE:
			color -= 4;
		// WATER
		case WATER: case SEAGRASS: case TALL_SEAGRASS: case KELP: case BUBBLE_COLUMN:
			color -= 4;
		// WOOD
		case OAK_LOG: /*vertical*/ case STRIPPED_OAK_LOG: case STRIPPED_OAK_WOOD: case OAK_PLANKS: case OAK_STAIRS: case OAK_SLAB:
		case OAK_DOOR: case OAK_FENCE_GATE: case OAK_FENCE:
		case OAK_TRAPDOOR: case OAK_WALL_SIGN: case OAK_SIGN: case OAK_PRESSURE_PLATE:
		case PETRIFIED_OAK_SLAB:
		case NOTE_BLOCK: case CRAFTING_TABLE: case BOOKSHELF: case CHEST: case TRAPPED_CHEST: case DAYLIGHT_DETECTOR: case LADDER:
		case BAMBOO_SAPLING: case DEAD_BUSH:
		case LECTERN: case COMPOSTER: case LOOM: case BARREL: case SMITHING_TABLE: case FLETCHING_TABLE: case CARTOGRAPHY_TABLE:
		case BEEHIVE:
			color -= 4;
		// QUARTZ
		/*horizontal birch*/ case BIRCH_WOOD: case QUARTZ_BLOCK: case QUARTZ_PILLAR: case QUARTZ_SLAB: case QUARTZ_STAIRS: case CHISELED_QUARTZ_BLOCK: case QUARTZ_BRICKS:
		case SMOOTH_QUARTZ: case SMOOTH_QUARTZ_SLAB: case SMOOTH_QUARTZ_STAIRS: case SEA_LANTERN: case TARGET:
		case DIORITE: case DIORITE_SLAB: case DIORITE_STAIRS: case DIORITE_WALL: case POLISHED_DIORITE: case POLISHED_DIORITE_SLAB: case POLISHED_DIORITE_STAIRS:
			color -= 4;
		// ORANGE
		case ACACIA_LOG: /*vertical*/ case STRIPPED_ACACIA_LOG: case STRIPPED_ACACIA_WOOD: case ACACIA_PLANKS: case ACACIA_STAIRS: case ACACIA_SLAB:
		case ACACIA_DOOR: case ACACIA_FENCE_GATE: case ACACIA_FENCE:
		case ACACIA_TRAPDOOR: case ACACIA_WALL_SIGN: case ACACIA_SIGN: case ACACIA_PRESSURE_PLATE:
		case ORANGE_WOOL: case ORANGE_CARPET: case PUMPKIN: case CARVED_PUMPKIN: case JACK_O_LANTERN:
		case RED_SAND: case RED_SANDSTONE: case RED_SANDSTONE_SLAB: case RED_SANDSTONE_STAIRS: case RED_SANDSTONE_WALL:
		case CHISELED_RED_SANDSTONE: case CUT_RED_SANDSTONE: case CUT_RED_SANDSTONE_SLAB:
		case SMOOTH_RED_SANDSTONE: case SMOOTH_RED_SANDSTONE_SLAB: case SMOOTH_RED_SANDSTONE_STAIRS:
		case TERRACOTTA: case ORANGE_GLAZED_TERRACOTTA: case ORANGE_CONCRETE: case ORANGE_CONCRETE_POWDER: case ORANGE_STAINED_GLASS: case ORANGE_STAINED_GLASS_PANE:
		case HONEY_BLOCK: case HONEYCOMB_BLOCK:
			color -= 4;
		// MAGENTA
		case PURPUR_BLOCK: case PURPUR_PILLAR: case PURPUR_SLAB: case PURPUR_STAIRS:
		case MAGENTA_WOOL: case MAGENTA_CARPET:
		case MAGENTA_GLAZED_TERRACOTTA: case MAGENTA_CONCRETE: case MAGENTA_CONCRETE_POWDER: case MAGENTA_STAINED_GLASS: case MAGENTA_STAINED_GLASS_PANE:
			color -= 4;
		// LIGHT_BLUE
		case SOUL_FIRE: case LIGHT_BLUE_WOOL: case LIGHT_BLUE_CARPET:
		case LIGHT_BLUE_GLAZED_TERRACOTTA: case LIGHT_BLUE_CONCRETE: case LIGHT_BLUE_CONCRETE_POWDER: case LIGHT_BLUE_STAINED_GLASS: case LIGHT_BLUE_STAINED_GLASS_PANE:
			color -= 4;
		// YELLOW
		case HAY_BLOCK: case SPONGE: case WET_SPONGE: case BEE_NEST:
		case YELLOW_WOOL: case YELLOW_CARPET:
		case YELLOW_GLAZED_TERRACOTTA: case YELLOW_CONCRETE: case YELLOW_CONCRETE_POWDER: case YELLOW_STAINED_GLASS: case YELLOW_STAINED_GLASS_PANE:
		case HORN_CORAL_BLOCK: case HORN_CORAL: case HORN_CORAL_WALL_FAN: case HORN_CORAL_FAN:
			color -= 4;
		// LIME
		case MELON:
		case LIME_WOOL: case LIME_CARPET:
		case LIME_GLAZED_TERRACOTTA: case LIME_CONCRETE: case LIME_CONCRETE_POWDER: case LIME_STAINED_GLASS: case LIME_STAINED_GLASS_PANE:
			color -= 4;
		// PINK (20)
		case PINK_WOOL: case PINK_CARPET:
		case PINK_GLAZED_TERRACOTTA: case PINK_CONCRETE: case PINK_CONCRETE_POWDER: case PINK_STAINED_GLASS: case PINK_STAINED_GLASS_PANE:
		case BRAIN_CORAL_BLOCK: case BRAIN_CORAL: case BRAIN_CORAL_WALL_FAN: case BRAIN_CORAL_FAN:
			color -= 4;
		// GRAY
		case GRAY_WOOL: case GRAY_CARPET:
		case GRAY_GLAZED_TERRACOTTA: case GRAY_CONCRETE: case GRAY_CONCRETE_POWDER: case GRAY_STAINED_GLASS: case GRAY_STAINED_GLASS_PANE:
		case DEAD_BRAIN_CORAL_BLOCK: case DEAD_BUBBLE_CORAL_BLOCK: case DEAD_FIRE_CORAL_BLOCK: case DEAD_HORN_CORAL_BLOCK: case DEAD_TUBE_CORAL_BLOCK:
		case DEAD_BRAIN_CORAL: case DEAD_BUBBLE_CORAL: case DEAD_FIRE_CORAL: case DEAD_HORN_CORAL: case DEAD_TUBE_CORAL:
		case DEAD_BRAIN_CORAL_FAN: case DEAD_BUBBLE_CORAL_FAN: case DEAD_FIRE_CORAL_FAN: case DEAD_HORN_CORAL_FAN: case DEAD_TUBE_CORAL_FAN:
		case DEAD_BRAIN_CORAL_WALL_FAN: case DEAD_BUBBLE_CORAL_WALL_FAN: case DEAD_FIRE_CORAL_WALL_FAN: case DEAD_HORN_CORAL_WALL_FAN: case DEAD_TUBE_CORAL_WALL_FAN:
			color -= 4;
		// LIGHT GRAY
		case STRUCTURE_BLOCK: case JIGSAW:
		case LIGHT_GRAY_WOOL: case LIGHT_GRAY_CARPET:
		case LIGHT_GRAY_GLAZED_TERRACOTTA: case LIGHT_GRAY_CONCRETE: case LIGHT_GRAY_CONCRETE_POWDER: case LIGHT_GRAY_STAINED_GLASS: case LIGHT_GRAY_STAINED_GLASS_PANE:
			color -= 4;
		// CYAN
		case PRISMARINE: case PRISMARINE_SLAB: case PRISMARINE_STAIRS: case PRISMARINE_WALL:
		case CYAN_WOOL: case CYAN_CARPET:
		case CYAN_GLAZED_TERRACOTTA: case CYAN_CONCRETE: case CYAN_CONCRETE_POWDER: case CYAN_STAINED_GLASS: case CYAN_STAINED_GLASS_PANE:
			color -= 4;
		// PURPLE
		case REPEATING_COMMAND_BLOCK: case MYCELIUM: case CHORUS_FLOWER: case CHORUS_PLANT: 
		case PURPLE_WOOL: case PURPLE_CARPET:
		case PURPLE_GLAZED_TERRACOTTA: case PURPLE_CONCRETE: case PURPLE_CONCRETE_POWDER: case PURPLE_STAINED_GLASS: case PURPLE_STAINED_GLASS_PANE:
		case BUBBLE_CORAL_BLOCK: case BUBBLE_CORAL: case BUBBLE_CORAL_FAN: case BUBBLE_CORAL_WALL_FAN:
			color -= 4;
		// BLUE
		case BLUE_WOOL: case BLUE_CARPET:
		case BLUE_GLAZED_TERRACOTTA: case BLUE_CONCRETE: case BLUE_CONCRETE_POWDER: case BLUE_STAINED_GLASS: case BLUE_STAINED_GLASS_PANE:
		case TUBE_CORAL_BLOCK: case TUBE_CORAL: case TUBE_CORAL_FAN: case TUBE_CORAL_WALL_FAN:
			color -= 4;
		// BROWN
		case BROWN_MUSHROOM:
		case DARK_OAK_LOG: /*both vertical and horizontal*/ case DARK_OAK_WOOD: case STRIPPED_DARK_OAK_LOG: case STRIPPED_DARK_OAK_WOOD:
		case DARK_OAK_PLANKS: case DARK_OAK_STAIRS: case DARK_OAK_SLAB:
		case DARK_OAK_DOOR: case DARK_OAK_FENCE_GATE: case DARK_OAK_FENCE:
		case DARK_OAK_TRAPDOOR: case DARK_OAK_WALL_SIGN: case DARK_OAK_SIGN: case DARK_OAK_PRESSURE_PLATE:
		/*horizontal spruce*/ case SPRUCE_WOOD: case COMMAND_BLOCK: case SOUL_SAND: case SOUL_SOIL:
		case BROWN_WOOL: case BROWN_CARPET:
		case BROWN_GLAZED_TERRACOTTA: case BROWN_CONCRETE: case BROWN_CONCRETE_POWDER: case BROWN_STAINED_GLASS: case BROWN_STAINED_GLASS_PANE:
			color -= 4;
		// GREEN
		case GREEN_WOOL: case GREEN_CARPET:
		case GREEN_GLAZED_TERRACOTTA: case GREEN_CONCRETE: case GREEN_CONCRETE_POWDER: case GREEN_STAINED_GLASS: case GREEN_STAINED_GLASS_PANE:
		case CHAIN_COMMAND_BLOCK: case END_PORTAL_FRAME: case DRIED_KELP_BLOCK:
		case SEA_PICKLE:
			color -= 4;
		// RED
		case RED_MUSHROOM: case RED_MUSHROOM_BLOCK: case NETHER_WART_BLOCK: case NETHER_WART: case SHROOMLIGHT:
		case RED_WOOL: case RED_CARPET: case ENCHANTING_TABLE:
		case BRICKS: case BRICK_SLAB: case BRICK_STAIRS: case BRICK_WALL:
		case RED_GLAZED_TERRACOTTA: case RED_CONCRETE: case RED_CONCRETE_POWDER: case RED_STAINED_GLASS: case RED_STAINED_GLASS_PANE:
		case FIRE_CORAL_BLOCK: case FIRE_CORAL: case FIRE_CORAL_FAN: case FIRE_CORAL_WALL_FAN:
			color -= 4;
		// BLACK
		case COAL_BLOCK: case BASALT: case POLISHED_BASALT: case ANCIENT_DEBRIS: case NETHERITE_BLOCK:
		case OBSIDIAN: case CRYING_OBSIDIAN: case RESPAWN_ANCHOR: case DRAGON_EGG: case END_GATEWAY: case END_PORTAL:
		case BLACKSTONE: case BLACKSTONE_SLAB: case BLACKSTONE_STAIRS: case BLACKSTONE_WALL:
		case POLISHED_BLACKSTONE: case POLISHED_BLACKSTONE_SLAB: case POLISHED_BLACKSTONE_STAIRS: case POLISHED_BLACKSTONE_WALL:
		case POLISHED_BLACKSTONE_BRICKS: case POLISHED_BLACKSTONE_BRICK_SLAB: case POLISHED_BLACKSTONE_BRICK_STAIRS: case POLISHED_BLACKSTONE_BRICK_WALL:
		case POLISHED_BLACKSTONE_PRESSURE_PLATE: case CRACKED_POLISHED_BLACKSTONE_BRICKS: case GILDED_BLACKSTONE: case CHISELED_POLISHED_BLACKSTONE:
		case BLACK_WOOL: case BLACK_CARPET:
		case BLACK_GLAZED_TERRACOTTA: case BLACK_CONCRETE: case BLACK_CONCRETE_POWDER: case BLACK_STAINED_GLASS: case BLACK_STAINED_GLASS_PANE:
			color -= 4;
		// GOLD (30)
		case GOLD_BLOCK: case LIGHT_WEIGHTED_PRESSURE_PLATE: case BELL:
			color -= 4;
		// DIAMOND
		case DIAMOND_BLOCK: case BEACON: case CONDUIT:
		case PRISMARINE_BRICKS: case PRISMARINE_BRICK_SLAB: case PRISMARINE_BRICK_STAIRS:
		case DARK_PRISMARINE: case DARK_PRISMARINE_SLAB: case DARK_PRISMARINE_STAIRS:
			color -= 4;
		// LAPIS
		case LAPIS_BLOCK:
			color -= 4;
		// EMERALD
		case EMERALD_BLOCK:
			color -= 4;
		// PODZOL (SPRUCE BROWN)
		case SPRUCE_LOG: /*vertical*/ case STRIPPED_SPRUCE_LOG: case STRIPPED_SPRUCE_WOOD:
		case SPRUCE_PLANKS: case SPRUCE_STAIRS: case SPRUCE_SLAB:
		case SPRUCE_DOOR: case SPRUCE_FENCE_GATE: case SPRUCE_FENCE:
		case SPRUCE_TRAPDOOR: case SPRUCE_SIGN: case SPRUCE_WALL_SIGN: case SPRUCE_PRESSURE_PLATE:
		case CAMPFIRE: case SOUL_CAMPFIRE:
		/* horizontal oak and jungle*/ case OAK_WOOD: case JUNGLE_WOOD: case PODZOL:
			color -= 4;
		// NETHERRACK
		case NETHERRACK: case NETHER_QUARTZ_ORE: case NETHER_GOLD_ORE: case MAGMA_BLOCK: case CRIMSON_ROOTS:
		case NETHER_BRICKS: case NETHER_BRICK_SLAB: case NETHER_BRICK_STAIRS: case NETHER_BRICK_WALL: case NETHER_BRICK_FENCE:
		case CHISELED_NETHER_BRICKS: case CRACKED_NETHER_BRICKS:
		case RED_NETHER_BRICKS: case RED_NETHER_BRICK_SLAB: case RED_NETHER_BRICK_STAIRS: case RED_NETHER_BRICK_WALL:
			color -= 4;
		case WHITE_TERRACOTTA:
			color -= 4;
		case ORANGE_TERRACOTTA:
			color -= 4;
		case MAGENTA_TERRACOTTA:
			color -= 4;
		case LIGHT_BLUE_TERRACOTTA:
			color -= 4;
		case YELLOW_TERRACOTTA:
			color -= 4;
		case LIME_TERRACOTTA:
			color -= 4;
		case PINK_TERRACOTTA:
			color -= 4;
		case GRAY_TERRACOTTA:
			color -= 4;
		case LIGHT_GRAY_TERRACOTTA:
			color -= 4;
		case CYAN_TERRACOTTA:
			color -= 4;
		case PURPLE_TERRACOTTA:
			color -= 4;
		case BLUE_TERRACOTTA:
			color -= 4;
		case BROWN_TERRACOTTA:
			color -= 4;
		case GREEN_TERRACOTTA:
			color -= 4;
		case RED_TERRACOTTA:
			color -= 4;
		case BLACK_TERRACOTTA:
			color -= 4;
		// CRIMSON_NYLIUM:
		case CRIMSON_NYLIUM:
			color -= 4;
		// CRIMSON_STEM:
		case CRIMSON_STEM: case STRIPPED_CRIMSON_STEM:
		case CRIMSON_PLANKS: case CRIMSON_SLAB: case CRIMSON_STAIRS: case CRIMSON_DOOR: case CRIMSON_TRAPDOOR: case CRIMSON_PRESSURE_PLATE:
		case CRIMSON_SIGN: case CRIMSON_WALL_SIGN: case CRIMSON_FENCE: case CRIMSON_FENCE_GATE:
			color -= 4;
		// CRIMSON_HYPHAE:
		case CRIMSON_HYPHAE: case STRIPPED_CRIMSON_HYPHAE:
			color -= 4;
		// WARPED_NYLIUM:
		case WARPED_NYLIUM:
			color -= 4;
		// WARPED_STEM:
		case WARPED_STEM: case STRIPPED_WARPED_STEM:
		case WARPED_PLANKS: case WARPED_SLAB: case WARPED_STAIRS: case WARPED_DOOR: case WARPED_TRAPDOOR: case WARPED_PRESSURE_PLATE:
		case WARPED_SIGN: case WARPED_WALL_SIGN: case WARPED_FENCE: case WARPED_FENCE_GATE:
			color -= 4;
		// WARPED_HYPHAE:
		case WARPED_HYPHAE: case STRIPPED_WARPED_HYPHAE:
			color -= 4;
		case WARPED_WART_BLOCK:
			color -= 4;
		// TRANSPARENT
		default:
		}

		if (max_color == color)
			return 0;
		color = color + 1;
		return (byte)color;	
	}
	
	private static Material getMaterial(DyeColor color)
	{
		switch (color)
		{
		case WHITE: return WHITE; 
		case ORANGE: return ORANGE; 
		case MAGENTA: return MAGENTA; 
		case BLACK: return BLACK; 
		case BLUE: return BLUE; 
		case BROWN: return BROWN; 
		case CYAN: return CYAN; 
		case GRAY: return GRAY; 
		case GREEN: return GREEN; 
		case LIGHT_BLUE: return LIGHT_BLUE; 
		case LIGHT_GRAY: return SILVER; 
		case PINK: return PINK; 
		case LIME: return LIME; 
		case RED: return RED; 
		case PURPLE: return PURPLE; 
		case YELLOW: return YELLOW; 
		}
		return null;
	}
	
}
