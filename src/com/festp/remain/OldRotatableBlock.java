package com.festp.remain;

import org.bukkit.Material;
import org.bukkit.block.Block;

@Deprecated
public interface OldRotatableBlock {

	@Deprecated
	public boolean can_rotate(Material m, byte start, boolean sneaking);
	
	@Deprecated
	public byte rotate(byte start);
	
	//Я хотела сохой с поводном получать соху на силу, которая крутит блоки.
		 //quartz 2-3-4-2-3-4; anvil 0-1-0,2-3-2,...,10-11-10; furnace 0/1/2-3-4-5-; 
		//comparator & powered/unpowered repeater 0-1-2-3-0, 4-5-6-7-4, 8-9-10-11-8, 12-13-14-15-12; hopper 0-2-3-4-5-0/2-3-4-5-6-2
		/*OldRotatableBlock[] RotatableBlocks = new OldRotatableBlock[] {
				//log-like
				new SerialRotatableBlock(Material.ACACIA_LOG, 4, 12), new SerialRotatableBlock(Material.BIRCH_LOG, 4, 12),
				new SerialRotatableBlock(Material.DARK_OAK_LOG, 4, 12), new SerialRotatableBlock(Material.JUNGLE_LOG, 4, 12),
				new SerialRotatableBlock(Material.OAK_LOG, 4, 12), new SerialRotatableBlock(Material.SPRUCE_LOG, 4, 12),
				new SerialRotatableBlock(Material.STRIPPED_ACACIA_LOG, 4, 12), new SerialRotatableBlock(Material.STRIPPED_BIRCH_LOG, 4, 12),
				new SerialRotatableBlock(Material.STRIPPED_DARK_OAK_LOG, 4, 12), new SerialRotatableBlock(Material.STRIPPED_JUNGLE_LOG, 4, 12),
				new SerialRotatableBlock(Material.STRIPPED_OAK_LOG, 4, 12), new SerialRotatableBlock(Material.STRIPPED_SPRUCE_LOG, 4, 12),
				new SerialRotatableBlock(Material.PURPUR_PILLAR, 4, 12), new SerialRotatableBlock(Material.BONE_BLOCK, 4, 12),
				new SerialRotatableBlock(Material.HAY_BLOCK, 4, 12), new SerialRotatableBlock(Material.QUARTZ_BLOCK, 2, 1, 5),
				//stairs bot
				new SerialRotatableBlock(Material.ACACIA_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.BIRCH_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.BRICK_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.COBBLESTONE_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.DARK_OAK_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.JUNGLE_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.NETHER_BRICK_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.PURPUR_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.QUARTZ_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.RED_SANDSTONE_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.SANDSTONE_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.STONE_BRICK_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.SPRUCE_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.OAK_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.PRISMARINE_BRICK_STAIRS, 0, 1, 4, true, false), new SerialRotatableBlock(Material.PRISMARINE_STAIRS, 0, 1, 4, true, false),
				new SerialRotatableBlock(Material.DARK_PRISMARINE_STAIRS, 0, 1, 4, true, false),
				//stairs top
				new SerialRotatableBlock(Material.ACACIA_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.BIRCH_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.BRICK_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.COBBLESTONE_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.DARK_OAK_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.JUNGLE_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.NETHER_BRICK_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.PURPUR_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.QUARTZ_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.RED_SANDSTONE_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.SANDSTONE_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.STONE_BRICK_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.SPRUCE_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.OAK_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.PRISMARINE_BRICK_STAIRS, 4, 1, 8, true, false), new SerialRotatableBlock(Material.PRISMARINE_STAIRS, 4, 1, 8, true, false),
				new SerialRotatableBlock(Material.DARK_PRISMARINE_STAIRS, 4, 1, 8, true, false),
				//stairs swap
				new SerialRotatableBlock(Material.ACACIA_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.BIRCH_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.BRICK_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.COBBLESTONE_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.DARK_OAK_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.JUNGLE_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.NETHER_BRICK_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.PURPUR_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.QUARTZ_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.RED_SANDSTONE_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.SANDSTONE_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.STONE_BRICK_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.SPRUCE_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.OAK_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.PRISMARINE_BRICK_STAIRS, 4, 8, false), new SerialRotatableBlock(Material.PRISMARINE_STAIRS, 4, 8, false),
				new SerialRotatableBlock(Material.DARK_PRISMARINE_STAIRS, 4, 8, false),
				//slabs swap
				new SerialRotatableBlock(Material.ACACIA_SLAB, 8, 16), new SerialRotatableBlock(Material.BIRCH_SLAB, 8, 16), new SerialRotatableBlock(Material.BRICK_SLAB, 8, 16),
				new SerialRotatableBlock(Material.COBBLESTONE_SLAB, 8, 16), new SerialRotatableBlock(Material.DARK_OAK_SLAB, 8, 16), new SerialRotatableBlock(Material.JUNGLE_SLAB, 8, 16),
				new SerialRotatableBlock(Material.NETHER_BRICK_SLAB, 8, 16), new SerialRotatableBlock(Material.PURPUR_SLAB, 8, 16), new SerialRotatableBlock(Material.QUARTZ_SLAB, 8, 16),
				new SerialRotatableBlock(Material.RED_SANDSTONE_SLAB, 8, 16), new SerialRotatableBlock(Material.SANDSTONE_SLAB, 8, 16), new SerialRotatableBlock(Material.STONE_BRICK_SLAB, 8, 16),
				new SerialRotatableBlock(Material.SPRUCE_SLAB, 8, 16), new SerialRotatableBlock(Material.OAK_SLAB, 8, 16), new SerialRotatableBlock(Material.STONE_SLAB, 8, 16),
				new SerialRotatableBlock(Material.PRISMARINE_BRICK_SLAB, 8, 16), new SerialRotatableBlock(Material.PRISMARINE_SLAB, 8, 16), new SerialRotatableBlock(Material.DARK_PRISMARINE_SLAB, 8, 16),
				//furniture
				new SerialRotatableBlock(Material.ANVIL, 0, 1, 2, false), new SerialRotatableBlock(Material.ANVIL, 2, 1, 4, false), new SerialRotatableBlock(Material.ANVIL, 4, 1, 6, false),
				new SerialRotatableBlock(Material.ANVIL, 6, 1, 8, false), new SerialRotatableBlock(Material.ANVIL, 8, 1, 10, false), new SerialRotatableBlock(Material.ANVIL, 10, 1, 12, false),
				new SpecificRotatableBlock(Material.FURNACE, false, (byte)2, (byte)4, (byte)3, (byte)5), new SpecificRotatableBlock(Material.FURNACE, false, (byte)2, (byte)4, (byte)3, (byte)5),
				//pumpkins
				new SerialRotatableBlock(Material.PUMPKIN, 1, 4), new SerialRotatableBlock(Material.CARVED_PUMPKIN, 1, 4), new SerialRotatableBlock(Material.JACK_O_LANTERN, 1, 4),
				//16 circle positions
				new SerialRotatableBlock(Material.LEGACY_STANDING_BANNER, 1, 16), new SerialRotatableBlock(Material.SIGN, 1, 16), //LEGACY_SIGN_POST
				//redstone 6 pos
				new SerialRotatableBlock(Material.PISTON, 1, 6), new SerialRotatableBlock(Material.STICKY_PISTON, 1, 6), new SerialRotatableBlock(Material.OBSERVER, 1, 6),
				new SerialRotatableBlock(Material.DISPENSER, 1, 6, false), new SerialRotatableBlock(Material.DROPPER, 1, 6, false),
				//redstone hard
				new SerialRotatableBlock(Material.LEGACY_DIODE_BLOCK_OFF, 0, 1, 4, false), new SerialRotatableBlock(Material.LEGACY_DIODE_BLOCK_OFF, 4, 1, 8, false), new SerialRotatableBlock(Material.LEGACY_DIODE_BLOCK_OFF, 8, 1, 12, false), new SerialRotatableBlock(Material.LEGACY_DIODE_BLOCK_OFF, 12, 1, 16, false),
				new SerialRotatableBlock(Material.LEGACY_REDSTONE_COMPARATOR_OFF, 0, 1, 4, false), new SerialRotatableBlock(Material.LEGACY_REDSTONE_COMPARATOR_OFF, 4, 1, 8, false),// new SerialRotatableBlock(Material.REDSTONE_COMPARATOR_OFF, 8, 1, 12, false), new SerialRotatableBlock(Material.REDSTONE_COMPARATOR_OFF, 12, 1, 16, false),
				new SpecificRotatableBlock(Material.HOPPER, false, (byte)0, (byte)2, (byte)4, (byte)3, (byte)5),
				//new SerialRotatableBlock(Material.HOPPER, 0, 1, 5, false), new SerialRotatableBlock(Material.HOPPER, 2, 1, 6, false), new SerialRotatableBlock(Material.HOPPER, 0, 2, 2, false)
		};
		/*Material RotatableBlocks[] = new Material[] {Material.LOG,Material.LOG_2,Material.PURPUR_PILLAR,Material.BONE_BLOCK,Material.HAY_BLOCK,Material.QUARTZ_BLOCK,
				Material.ACACIA_STAIRS,
				Material.PUMPKIN, Material.JACK_O_LANTERN,
				Material.STANDING_BANNER, Material.SIGN_POST,
				Material.PISTON_BASE,Material.PISTON_STICKY_BASE,Material.DISPENSER,Material.DROPPER,Material.OBSERVER};
		byte RotatePositionMin[] = new byte[] {0,0,0,0,0,2, //logs
				0, //stairs down
				0,0, //pumpkins
				0,0, //16 circle positions
				0,0,0,0,0}; //redstone 6 pos
		byte RotatePositionSkips[] = new byte[] {4,4,4,4,4,1,
				1,
				1,1,
				1,1,
				1,1,1,1,1};
		byte RotatePositionMax[] = new byte[] {12,12,12,12,12,6,
				4,
				4,4,
				16,16,
				6,6,6,6,6};
		boolean EnableNotSneaking[] = new boolean[] {true,true,true,true,true,
				true,
				true,true,
				true,true,
				true,true,false,false,true};
		boolean EnableSneaking[] = new boolean[] {true,true,true,true,true,
				true,
				true,true,
				true,true,
				true,true,true,true,true};*/
}
