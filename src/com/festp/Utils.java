package com.festp;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Turtle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.festp.storages.Storage;

import net.minecraft.server.v1_13_R2.EntityAgeable;
import net.minecraft.server.v1_13_R2.EntityAnimal;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.Particles;//.EnumParticle;

public class Utils {
	private static JavaPlugin plug;
	private static UnsafeValues legacy;
	
	public static void setPlugin(JavaPlugin pl) {
		plug = pl;
		legacy = pl.getServer().getUnsafe();
	}

	public static Material from_legacy(MaterialData md) {
		return legacy.fromLegacy(md);
	}
	
	public static boolean lower_cauldron_water(BlockState cauldron) {
		Cauldron caul = (Cauldron)cauldron.getData();
		if(caul.getData() == 0)
			return false;
		Cauldron caul2 = new Cauldron((byte) (caul.getData()-1));
		cauldron.setData(caul2);
		cauldron.update();
		return true;
	}
	public static boolean full_cauldron_water(BlockState cauldron) {
		Cauldron caul = (Cauldron)cauldron.getData();
		if(caul.getData() == 3)
			return false;
		Cauldron caul2 = new Cauldron((byte) 3);
		cauldron.setData(caul2);
		cauldron.update();
		return false;
	}
	
	public static Vector throwVector(Location loc, double throw_power) {
		throw_power = 0.2*throw_power;
		double yaw = ( loc.getYaw() + 90 ) /180*Math.PI,
		pitch = ( loc.getPitch() ) /180*Math.PI;
		double vec_x = Math.cos(yaw)*Math.cos(pitch)*throw_power,
			vec_y = -Math.sin(pitch)*throw_power,
			vec_z = Math.sin(yaw)*Math.cos(pitch)*throw_power;
		return new Vector(vec_x,vec_y,vec_z);
	}
	
	public static Item drop(Location loc, ItemStack stack, double throw_power) {
		if(stack != null && stack.getType() != Material.AIR) {
			Item it = loc.getWorld().dropItem(loc, stack);
			it.setVelocity(throwVector(loc, throw_power));
			it.setPickupDelay(30);
			return it;
		}
		return null;
	}
	
	public static ItemStack setData(ItemStack i, String field, String data) {
        if (data == null || field == null || i == null)
            return i;
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        
        compound.setString(field, data);
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
	
	public static String vectorToString(Vector v) {
		DecimalFormat dec = new DecimalFormat("#0.00");
		return ("("+dec.format(v.getX())+"; "
				  +dec.format(v.getY())+"; "
				  +dec.format(v.getZ())+")")
				.replace(',', '.');
	}
	
	public static boolean hasDataField(ItemStack i, String field) {
        if (field == null || i == null)
            return false;
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound != null && compound.hasKey(field))
        	return true;
        return false;
	}
	
	public static boolean hasData(ItemStack i, String field, String data) {
        if (data == null || field == null || i == null)
            return false;
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound != null && compound.hasKey(field) && data.equalsIgnoreCase(compound.getString(field)))
        	return true;
        return false;
	}
	
	public static <T extends LivingEntity> T spawnBeacon(Location l, String type, Class<T> entity) {
		T beacon = l.getWorld().spawn(l, entity);
 		beacon.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000000, 1, false, false));
 		beacon.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000000, 5, false, false));
 		beacon.setAI(false);
 		beacon.setSilent(true);
 		beacon.setCollidable(false);
 		beacon.setGravity(false);
 		beacon.setInvulnerable(true);
 		if(beacon instanceof Turtle) {
 			Turtle turtle = (Turtle)beacon;
 			turtle.setBaby();
 			turtle.setAgeLock(true);
 		}
 		else if(beacon instanceof Bat) {
 			Bat bat = (Bat)beacon;
 			//bat.setAwake(true);
 		}
 		else if(beacon instanceof ArmorStand) {
 			ArmorStand stand = (ArmorStand)beacon;
 			stand.setVisible(false);
 			stand.setSmall(true);
 		}
 		ItemStack identificator = new ItemStack(Material.STONE_BUTTON);
 		identificator = setData(identificator, type, "yea");
 		beacon.getEquipment().setHelmet(identificator);
 		return beacon;
	}
	
	public static Entity spawnBeacon_old(Location l, String name) {
		Bat temp = l.getWorld().spawn(l, Bat.class);
		if(name != null)
			temp.setCustomName(name);
 		temp.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000000, 1, false, false));
 		temp.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 100000000, 5, false, false));
 		//temp.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue( 10000 );
 		temp.setAI(false);
 		temp.setSilent(true);
 		temp.setCollidable(false);
 		temp.setGravity(false);
 		temp.setInvulnerable(true);
 		return temp;
	}
	
	public static boolean contains_all_of(String str, String... args) {
		for(String s : args)
			if(!str.contains(s))
				return false;
		return true;
	}
	
	public static boolean isEndBiome(Biome b) {
		return b == Biome.THE_END || b == Biome.END_BARRENS || b == Biome.END_HIGHLANDS || b == Biome.END_MIDLANDS || b == Biome.SMALL_END_ISLANDS;
	}
	
	public static boolean isHead(Material m) {
		return m == Material.WITHER_SKELETON_SKULL || m == Material.SKELETON_SKULL || m == Material.CREEPER_HEAD
				|| m == Material.ZOMBIE_HEAD || m == Material.PLAYER_HEAD || m == Material.DRAGON_HEAD;
	}
	
	public static boolean isSlab(Material m) {
		switch(m) {
		case ACACIA_SLAB: return true;
		case BIRCH_SLAB: return true;
		case BRICK_SLAB: return true;
		case COBBLESTONE_SLAB: return true;
		case DARK_OAK_SLAB: return true;
		case JUNGLE_SLAB: return true;
		case NETHER_BRICK_SLAB: return true;
		case OAK_SLAB: return true;
		case PURPUR_SLAB: return true;
		case QUARTZ_SLAB: return true;
		case RED_SANDSTONE_SLAB: return true;
		case SANDSTONE_SLAB: return true;
		case STONE_BRICK_SLAB: return true;
		case SPRUCE_SLAB: return true;
		case STONE_SLAB: return true;
		}
		return false;
	}
	
	public static boolean isStairs(Material m) {
		switch(m) {
		case ACACIA_STAIRS: return true;
		case BIRCH_STAIRS: return true;
		case BRICK_STAIRS: return true;
		case COBBLESTONE_STAIRS: return true;
		case DARK_OAK_STAIRS: return true;
		case JUNGLE_STAIRS: return true;
		case NETHER_BRICK_STAIRS: return true;
		case OAK_STAIRS: return true;
		case PURPUR_STAIRS: return true;
		case QUARTZ_STAIRS: return true;
		case RED_SANDSTONE_STAIRS: return true;
		case SANDSTONE_STAIRS: return true;
		case STONE_BRICK_STAIRS: return true;
		case SPRUCE_STAIRS: return true;
		}
		return false;
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
		if(isWoodenDoor(m) || m == Material.STONE_BUTTON)
			return true;
		return false;
	}
	
	public static boolean isFence(Material m) {
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
		if(b.isLiquid())
		{
			Levelled liquid = (Levelled) b.getState().getBlockData();
			//if(liquid.getLevel() < liquid.getMaximumLevel())
			if(liquid.getLevel() > 0)
				return true;
		}
		return false;
	}
	
	public static boolean isStationaryLiquid(Block b) {
		if(b.isLiquid())
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
		if(isAir(m) || is_banner(m) || is_bed(m) || is_carpet(m) || is_wall_banner(m) || isTrapdoor(m) || isDoor(m) || isGate(m)
				|| is_flower(m) || isPlate(m) || isButton(m) || isRail(m)
				|| m == Material.TORCH || m == Material.WALL_TORCH|| m == Material.REDSTONE_TORCH || m == Material.REDSTONE_WALL_TORCH|| m == Material.SIGN || m == Material.WALL_SIGN
				|| m == Material.FLOWER_POT || m == Material.REDSTONE_WIRE || m == Material.COMPARATOR || m == Material.REPEATER || m == Material.LEVER
				|| m == Material.TRIPWIRE_HOOK || m == Material.TRIPWIRE)
			return true;
		return false;
	}
	
	public static boolean playerCanStay(Block b) {
		return Utils.isTransparent(b.getType()) && Utils.isTransparent(b.getRelative(0, 1, 0).getType()) && !Utils.isTransparent(b.getRelative(0, -1, 0).getType());
	}
	
	public static boolean playerCanFlyOn(Block b) {
		return Utils.isTransparent(b.getRelative(0, 1, 0).getType()) && Utils.isTransparent(b.getRelative(0, 2, 0).getType());
	}
	
	public static boolean isInteractable(Material m) {
		return Utils.is_shulker_box(m) || Utils.isButton(m) || Utils.isWoodenDoor(m) || Utils.isGate(m) || Utils.isWoodenTrapdoor(m)
				|| m == Material.CHEST || m == Material.TRAPPED_CHEST || m == Material.ENDER_CHEST || m == Material.FURNACE || m == Material.CRAFTING_TABLE
				|| m == Material.DISPENSER || m == Material.DROPPER || m == Material.ENCHANTING_TABLE || m == Material.BREWING_STAND
				|| m == Material.ANVIL || m == Material.CHIPPED_ANVIL || m == Material.DAMAGED_ANVIL
				|| m == Material.HOPPER || m == Material.REPEATER || m == Material.COMPARATOR || m == Material.LEVER
				|| m == Material.COMMAND_BLOCK || m == Material.CHAIN_COMMAND_BLOCK || m == Material.REPEATING_COMMAND_BLOCK;
	}
	
	public static boolean stray_biome(Biome b) {
		return b == Biome.SNOWY_TUNDRA || b == Biome.SNOWY_MOUNTAINS || b == Biome.ICE_SPIKES || b == Biome.FROZEN_OCEAN || b == Biome.DEEP_FROZEN_OCEAN;
				/*|| spawn_place.getBiome() == Biome.SNOWY_BEACH //0.05*/
	}
	public static boolean stray_biome(Block b) {
		return b.getTemperature() < 0.05;
	}
	public static boolean husk_biome(Biome b) {
		return b == Biome.DESERT || b == Biome.DESERT_HILLS || b == Biome.DESERT_LAKES;
	}
	public static boolean husk_biome(Block b) {
		return husk_biome(b.getBiome());
	}
	
	public static BlockFace get_dir(Location l) {
		double yaw = ( l.getYaw() + 45f ); //yaw 0 - south, 90 - west
		if(yaw >= 180f) yaw -= 360;
		if(yaw < 0)
			if(yaw < -90)
				return BlockFace.NORTH;
			else
				return BlockFace.EAST;
		else
			if(yaw < 90)
				return BlockFace.SOUTH;
			else
				return BlockFace.WEST;
		
	}
	
	public static Class getNMSClass(String className) {
		String version = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

		Class c = null;
		try {
		    c = Class.forName(version + "." + className);
		    return c;
		} catch(Exception e) {
		    e.printStackTrace();
		    return null;
		}
	}
	
	public static Object getPrivateField(String fieldName, Class clazz, Object object)
    {
        Field field;
        Object o = null;
        try
        {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            o = field.get(object);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return o;
    }
 
	public static void setPrivateField(String fieldName, Class clazz, Object object, Object new_val)
    {
		if(clazz == null || object == null) return;
		
        Field field;
        try
        {
        	//for(Field f : clazz.getDeclaredFields())
        	//	System.out.println(f.getName()+" "+f.getType());
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, new_val);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
 
	public static boolean setLove(EntityAnimal ea, Material food)
	{
		//setPrivateField("bx", int.class, ea, 600);
		//setPrivateField("love", ea.getClass(), ea, 600);
		if( (Integer)( getPrivateField("b", EntityAgeable.class, ea) ) > 0)
			return false;
		setPrivateField("bC", ea.getClass().getSuperclass(), ea, 600);
		ea.breedItem = CraftItemStack.asNMSCopy(new ItemStack(food));
		ea.world.broadcastEntityEffect(ea, (byte)18);
		//ea.n();
		return true;
	}

	public static void summonHearths(EntityAnimal ea) {
		Random random = new Random();
		double d0 = random.nextGaussian() * 0.02D;
		double d1 = random.nextGaussian() * 0.02D;
		double d2 = random.nextGaussian() * 0.02D;
		
		//ea.world.addParticle(Particles..HEART, ea.locX + random.nextFloat() * ea.width * 2.0F - ea.width, ea.locY + 0.5D + random.nextFloat() * ea.length, ea.locZ + random.nextFloat() * ea.width * 2.0F - ea.width, d0, d1, d2, new int[0]);
	}
	
	public static ItemStack resetName(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(new ItemStack(item.getType()).getItemMeta().getDisplayName());
		item.setItemMeta(meta);
		return item;
	}
	
	public static String ItemStacks_toString(ItemStack[] stacks) {
		String result = "[";
		for(int i = 0; i < stacks.length; i++)
		{
			Storage st = Storage.getByItemStack(stacks[i]);
			if(st != null)
				result += "ItemStack{StorageID="+st.getID()+", type="+st.getType()+"}";
			else
				result += stacks[i];
			if(i != stacks.length-1)
				result += ", ";
		}
		result += "]";
		return result;
	}
	
	public static BlockFace getLeftDirection(BlockFace forward) {
		switch (forward) {
		case EAST:
			return BlockFace.NORTH;
		case NORTH:
			return BlockFace.WEST;
		case WEST:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.EAST;
		default:
			return forward;
		}
	}
	
	public static BlockFace getRightDirection(BlockFace forward) {
		switch (forward) {
		case EAST:
			return BlockFace.SOUTH;
		case SOUTH:
			return BlockFace.WEST;
		case WEST:
			return BlockFace.NORTH;
		case NORTH:
			return BlockFace.EAST;
		default:
			return forward;
		}
	}

	//(wall) banner, bed, carpet, concrete, concrete powder, glass(pane), wool, shulker box, terracotta, glazed, dye or null
	public static DyeColor colorFromMaterial(Material m) {
		switch(m) {
		//banner
		case WHITE_BANNER: return DyeColor.WHITE;
		case ORANGE_BANNER: return DyeColor.ORANGE;
		case MAGENTA_BANNER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_BANNER: return DyeColor.LIGHT_BLUE;
		case YELLOW_BANNER: return DyeColor.YELLOW;
		case LIME_BANNER: return DyeColor.LIME;
		case PINK_BANNER: return DyeColor.PINK;
		case GRAY_BANNER: return DyeColor.GRAY;
		case LIGHT_GRAY_BANNER: return DyeColor.LIGHT_GRAY;
		case CYAN_BANNER: return DyeColor.CYAN;
		case PURPLE_BANNER: return DyeColor.PURPLE;
		case BLUE_BANNER: return DyeColor.BLUE;
		case BROWN_BANNER: return DyeColor.BROWN;
		case GREEN_BANNER: return DyeColor.GREEN;
		case RED_BANNER: return DyeColor.RED;
		case BLACK_BANNER: return DyeColor.BLACK;
		//wall_banner
		case WHITE_WALL_BANNER: return DyeColor.WHITE;
		case ORANGE_WALL_BANNER: return DyeColor.ORANGE;
		case MAGENTA_WALL_BANNER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_WALL_BANNER: return DyeColor.LIGHT_BLUE;
		case YELLOW_WALL_BANNER: return DyeColor.YELLOW;
		case LIME_WALL_BANNER: return DyeColor.LIME;
		case PINK_WALL_BANNER: return DyeColor.PINK;
		case GRAY_WALL_BANNER: return DyeColor.GRAY;
		case LIGHT_GRAY_WALL_BANNER: return DyeColor.LIGHT_GRAY;
		case CYAN_WALL_BANNER: return DyeColor.CYAN;
		case PURPLE_WALL_BANNER: return DyeColor.PURPLE;
		case BLUE_WALL_BANNER: return DyeColor.BLUE;
		case BROWN_WALL_BANNER: return DyeColor.BROWN;
		case GREEN_WALL_BANNER: return DyeColor.GREEN;
		case RED_WALL_BANNER: return DyeColor.RED;
		case BLACK_WALL_BANNER: return DyeColor.BLACK;
		//bed
		case WHITE_BED: return DyeColor.WHITE;
		case ORANGE_BED: return DyeColor.ORANGE;
		case MAGENTA_BED: return DyeColor.MAGENTA;
		case LIGHT_BLUE_BED: return DyeColor.LIGHT_BLUE;
		case YELLOW_BED: return DyeColor.YELLOW;
		case LIME_BED: return DyeColor.LIME;
		case PINK_BED: return DyeColor.PINK;
		case GRAY_BED: return DyeColor.GRAY;
		case LIGHT_GRAY_BED: return DyeColor.LIGHT_GRAY;
		case CYAN_BED: return DyeColor.CYAN;
		case PURPLE_BED: return DyeColor.PURPLE;
		case BLUE_BED: return DyeColor.BLUE;
		case BROWN_BED: return DyeColor.BROWN;
		case GREEN_BED: return DyeColor.GREEN;
		case RED_BED: return DyeColor.RED;
		case BLACK_BED: return DyeColor.BLACK;
		//carpet
		case WHITE_CARPET: return DyeColor.WHITE;
		case ORANGE_CARPET: return DyeColor.ORANGE;
		case MAGENTA_CARPET: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CARPET: return DyeColor.LIGHT_BLUE;
		case YELLOW_CARPET: return DyeColor.YELLOW;
		case LIME_CARPET: return DyeColor.LIME;
		case PINK_CARPET: return DyeColor.PINK;
		case GRAY_CARPET: return DyeColor.GRAY;
		case LIGHT_GRAY_CARPET: return DyeColor.LIGHT_GRAY;
		case CYAN_CARPET: return DyeColor.CYAN;
		case PURPLE_CARPET: return DyeColor.PURPLE;
		case BLUE_CARPET: return DyeColor.BLUE;
		case BROWN_CARPET: return DyeColor.BROWN;
		case GREEN_CARPET: return DyeColor.GREEN;
		case RED_CARPET: return DyeColor.RED;
		case BLACK_CARPET: return DyeColor.BLACK;
		//concrete_powder
		case WHITE_CONCRETE_POWDER: return DyeColor.WHITE;
		case ORANGE_CONCRETE_POWDER: return DyeColor.ORANGE;
		case MAGENTA_CONCRETE_POWDER: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CONCRETE_POWDER: return DyeColor.LIGHT_BLUE;
		case YELLOW_CONCRETE_POWDER: return DyeColor.YELLOW;
		case LIME_CONCRETE_POWDER: return DyeColor.LIME;
		case PINK_CONCRETE_POWDER: return DyeColor.PINK;
		case GRAY_CONCRETE_POWDER: return DyeColor.GRAY;
		case LIGHT_GRAY_CONCRETE_POWDER: return DyeColor.LIGHT_GRAY;
		case CYAN_CONCRETE_POWDER: return DyeColor.CYAN;
		case PURPLE_CONCRETE_POWDER: return DyeColor.PURPLE;
		case BLUE_CONCRETE_POWDER: return DyeColor.BLUE;
		case BROWN_CONCRETE_POWDER: return DyeColor.BROWN;
		case GREEN_CONCRETE_POWDER: return DyeColor.GREEN;
		case RED_CONCRETE_POWDER: return DyeColor.RED;
		case BLACK_CONCRETE_POWDER: return DyeColor.BLACK;
		//concrete
		case WHITE_CONCRETE: return DyeColor.WHITE;
		case ORANGE_CONCRETE: return DyeColor.ORANGE;
		case MAGENTA_CONCRETE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_CONCRETE: return DyeColor.LIGHT_BLUE;
		case YELLOW_CONCRETE: return DyeColor.YELLOW;
		case LIME_CONCRETE: return DyeColor.LIME;
		case PINK_CONCRETE: return DyeColor.PINK;
		case GRAY_CONCRETE: return DyeColor.GRAY;
		case LIGHT_GRAY_CONCRETE: return DyeColor.LIGHT_GRAY;
		case CYAN_CONCRETE: return DyeColor.CYAN;
		case PURPLE_CONCRETE: return DyeColor.PURPLE;
		case BLUE_CONCRETE: return DyeColor.BLUE;
		case BROWN_CONCRETE: return DyeColor.BROWN;
		case GREEN_CONCRETE: return DyeColor.GREEN;
		case RED_CONCRETE: return DyeColor.RED;
		case BLACK_CONCRETE: return DyeColor.BLACK;
		//glass
		case WHITE_STAINED_GLASS: return DyeColor.WHITE;
		case ORANGE_STAINED_GLASS: return DyeColor.ORANGE;
		case MAGENTA_STAINED_GLASS: return DyeColor.MAGENTA;
		case LIGHT_BLUE_STAINED_GLASS: return DyeColor.LIGHT_BLUE;
		case YELLOW_STAINED_GLASS: return DyeColor.YELLOW;
		case LIME_STAINED_GLASS: return DyeColor.LIME;
		case PINK_STAINED_GLASS: return DyeColor.PINK;
		case GRAY_STAINED_GLASS: return DyeColor.GRAY;
		case LIGHT_GRAY_STAINED_GLASS: return DyeColor.LIGHT_GRAY;
		case CYAN_STAINED_GLASS: return DyeColor.CYAN;
		case PURPLE_STAINED_GLASS: return DyeColor.PURPLE;
		case BLUE_STAINED_GLASS: return DyeColor.BLUE;
		case BROWN_STAINED_GLASS: return DyeColor.BROWN;
		case GREEN_STAINED_GLASS: return DyeColor.GREEN;
		case RED_STAINED_GLASS: return DyeColor.RED;
		case BLACK_STAINED_GLASS: return DyeColor.BLACK;
		//glass_pane
		case WHITE_STAINED_GLASS_PANE: return DyeColor.WHITE;
		case ORANGE_STAINED_GLASS_PANE: return DyeColor.ORANGE;
		case MAGENTA_STAINED_GLASS_PANE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_STAINED_GLASS_PANE: return DyeColor.LIGHT_BLUE;
		case YELLOW_STAINED_GLASS_PANE: return DyeColor.YELLOW;
		case LIME_STAINED_GLASS_PANE: return DyeColor.LIME;
		case PINK_STAINED_GLASS_PANE: return DyeColor.PINK;
		case GRAY_STAINED_GLASS_PANE: return DyeColor.GRAY;
		case LIGHT_GRAY_STAINED_GLASS_PANE: return DyeColor.LIGHT_GRAY;
		case CYAN_STAINED_GLASS_PANE: return DyeColor.CYAN;
		case PURPLE_STAINED_GLASS_PANE: return DyeColor.PURPLE;
		case BLUE_STAINED_GLASS_PANE: return DyeColor.BLUE;
		case BROWN_STAINED_GLASS_PANE: return DyeColor.BROWN;
		case GREEN_STAINED_GLASS_PANE: return DyeColor.GREEN;
		case RED_STAINED_GLASS_PANE: return DyeColor.RED;
		case BLACK_STAINED_GLASS_PANE: return DyeColor.BLACK;
		//wool
		case WHITE_WOOL: return DyeColor.WHITE;
		case ORANGE_WOOL: return DyeColor.ORANGE;
		case MAGENTA_WOOL: return DyeColor.MAGENTA;
		case LIGHT_BLUE_WOOL: return DyeColor.LIGHT_BLUE;
		case YELLOW_WOOL: return DyeColor.YELLOW;
		case LIME_WOOL: return DyeColor.LIME;
		case PINK_WOOL: return DyeColor.PINK;
		case GRAY_WOOL: return DyeColor.GRAY;
		case LIGHT_GRAY_WOOL: return DyeColor.LIGHT_GRAY;
		case CYAN_WOOL: return DyeColor.CYAN;
		case PURPLE_WOOL: return DyeColor.PURPLE;
		case BLUE_WOOL: return DyeColor.BLUE;
		case BROWN_WOOL: return DyeColor.BROWN;
		case GREEN_WOOL: return DyeColor.GREEN;
		case RED_WOOL: return DyeColor.RED;
		case BLACK_WOOL: return DyeColor.BLACK;
		//shulker_box
		case WHITE_SHULKER_BOX: return DyeColor.WHITE;
		case ORANGE_SHULKER_BOX: return DyeColor.ORANGE;
		case MAGENTA_SHULKER_BOX: return DyeColor.MAGENTA;
		case LIGHT_BLUE_SHULKER_BOX: return DyeColor.LIGHT_BLUE;
		case YELLOW_SHULKER_BOX: return DyeColor.YELLOW;
		case LIME_SHULKER_BOX: return DyeColor.LIME;
		case PINK_SHULKER_BOX: return DyeColor.PINK;
		case GRAY_SHULKER_BOX: return DyeColor.GRAY;
		case LIGHT_GRAY_SHULKER_BOX: return DyeColor.LIGHT_GRAY;
		case CYAN_SHULKER_BOX: return DyeColor.CYAN;
		case PURPLE_SHULKER_BOX: return DyeColor.PURPLE;
		case BLUE_SHULKER_BOX: return DyeColor.BLUE;
		case BROWN_SHULKER_BOX: return DyeColor.BROWN;
		case GREEN_SHULKER_BOX: return DyeColor.GREEN;
		case RED_SHULKER_BOX: return DyeColor.RED;
		case BLACK_SHULKER_BOX: return DyeColor.BLACK;
		//terracotta
		case WHITE_TERRACOTTA: return DyeColor.WHITE;
		case ORANGE_TERRACOTTA: return DyeColor.ORANGE;
		case MAGENTA_TERRACOTTA: return DyeColor.MAGENTA;
		case LIGHT_BLUE_TERRACOTTA: return DyeColor.LIGHT_BLUE;
		case YELLOW_TERRACOTTA: return DyeColor.YELLOW;
		case LIME_TERRACOTTA: return DyeColor.LIME;
		case PINK_TERRACOTTA: return DyeColor.PINK;
		case GRAY_TERRACOTTA: return DyeColor.GRAY;
		case LIGHT_GRAY_TERRACOTTA: return DyeColor.LIGHT_GRAY;
		case CYAN_TERRACOTTA: return DyeColor.CYAN;
		case PURPLE_TERRACOTTA: return DyeColor.PURPLE;
		case BLUE_TERRACOTTA: return DyeColor.BLUE;
		case BROWN_TERRACOTTA: return DyeColor.BROWN;
		case GREEN_TERRACOTTA: return DyeColor.GREEN;
		case RED_TERRACOTTA: return DyeColor.RED;
		case BLACK_TERRACOTTA: return DyeColor.BLACK;
		//glazed_terracotta
		case WHITE_GLAZED_TERRACOTTA: return DyeColor.WHITE;
		case ORANGE_GLAZED_TERRACOTTA: return DyeColor.ORANGE;
		case MAGENTA_GLAZED_TERRACOTTA: return DyeColor.MAGENTA;
		case LIGHT_BLUE_GLAZED_TERRACOTTA: return DyeColor.LIGHT_BLUE;
		case YELLOW_GLAZED_TERRACOTTA: return DyeColor.YELLOW;
		case LIME_GLAZED_TERRACOTTA: return DyeColor.LIME;
		case PINK_GLAZED_TERRACOTTA: return DyeColor.PINK;
		case GRAY_GLAZED_TERRACOTTA: return DyeColor.GRAY;
		case LIGHT_GRAY_GLAZED_TERRACOTTA: return DyeColor.LIGHT_GRAY;
		case CYAN_GLAZED_TERRACOTTA: return DyeColor.CYAN;
		case PURPLE_GLAZED_TERRACOTTA: return DyeColor.PURPLE;
		case BLUE_GLAZED_TERRACOTTA: return DyeColor.BLUE;
		case BROWN_GLAZED_TERRACOTTA: return DyeColor.BROWN;
		case GREEN_GLAZED_TERRACOTTA: return DyeColor.GREEN;
		case RED_GLAZED_TERRACOTTA: return DyeColor.RED;
		case BLACK_GLAZED_TERRACOTTA: return DyeColor.BLACK;
		//dyes
		case BONE_MEAL: return DyeColor.WHITE;
		case ORANGE_DYE: return DyeColor.ORANGE;
		case MAGENTA_DYE: return DyeColor.MAGENTA;
		case LIGHT_BLUE_DYE: return DyeColor.LIGHT_BLUE;
		case DANDELION_YELLOW: return DyeColor.YELLOW;
		case LIME_DYE: return DyeColor.LIME;
		case PINK_DYE: return DyeColor.PINK;
		case GRAY_DYE: return DyeColor.GRAY;
		case LIGHT_GRAY_DYE: return DyeColor.LIGHT_GRAY;
		case CYAN_DYE: return DyeColor.CYAN;
		case PURPLE_DYE: return DyeColor.PURPLE;
		case LAPIS_LAZULI: return DyeColor.BLUE;
		case COCOA_BEANS: return DyeColor.BROWN;
		case CACTUS_GREEN: return DyeColor.GREEN;
		case ROSE_RED: return DyeColor.RED;
		case INK_SAC: return DyeColor.BLACK;
		default: return null;
		}
	}
	
	public static Material fromColor_banner(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_BANNER; 
		case ORANGE: return Material.ORANGE_BANNER;
		case MAGENTA: return Material.MAGENTA_BANNER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_BANNER;
		case YELLOW: return Material.YELLOW_BANNER;
		case LIME: return Material.LIME_BANNER;
		case PINK: return Material.PINK_BANNER;
		case GRAY: return Material.GRAY_BANNER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_BANNER;
		case CYAN: return Material.CYAN_BANNER;
		case PURPLE: return Material.PURPLE_BANNER;
		case BLUE: return Material.BLUE_BANNER;
		case BROWN: return Material.BROWN_BANNER;
		case GREEN: return Material.GREEN_BANNER;
		case RED: return Material.RED_BANNER;
		case BLACK: return Material.BLACK_BANNER;
		default: return null;
		}
	}
	
	public static Material fromColor_wall_banner(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_WALL_BANNER; 
		case ORANGE: return Material.ORANGE_WALL_BANNER;
		case MAGENTA: return Material.MAGENTA_WALL_BANNER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_WALL_BANNER;
		case YELLOW: return Material.YELLOW_WALL_BANNER;
		case LIME: return Material.LIME_WALL_BANNER;
		case PINK: return Material.PINK_WALL_BANNER;
		case GRAY: return Material.GRAY_WALL_BANNER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_WALL_BANNER;
		case CYAN: return Material.CYAN_WALL_BANNER;
		case PURPLE: return Material.PURPLE_WALL_BANNER;
		case BLUE: return Material.BLUE_WALL_BANNER;
		case BROWN: return Material.BROWN_WALL_BANNER;
		case GREEN: return Material.GREEN_WALL_BANNER;
		case RED: return Material.RED_WALL_BANNER;
		case BLACK: return Material.BLACK_WALL_BANNER;
		default: return null;
		}
	}
	
	public static Material fromColor_bed(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_BED; 
		case ORANGE: return Material.ORANGE_BED;
		case MAGENTA: return Material.MAGENTA_BED;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_BED;
		case YELLOW: return Material.YELLOW_BED;
		case LIME: return Material.LIME_BED;
		case PINK: return Material.PINK_BED;
		case GRAY: return Material.GRAY_BED;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_BED;
		case CYAN: return Material.CYAN_BED;
		case PURPLE: return Material.PURPLE_BED;
		case BLUE: return Material.BLUE_BED;
		case BROWN: return Material.BROWN_BED;
		case GREEN: return Material.GREEN_BED;
		case RED: return Material.RED_BED;
		case BLACK: return Material.BLACK_BED;
		default: return null;
		}
	}
	
	public static Material fromColor_carpet(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CARPET; 
		case ORANGE: return Material.ORANGE_CARPET;
		case MAGENTA: return Material.MAGENTA_CARPET;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CARPET;
		case YELLOW: return Material.YELLOW_CARPET;
		case LIME: return Material.LIME_CARPET;
		case PINK: return Material.PINK_CARPET;
		case GRAY: return Material.GRAY_CARPET;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CARPET;
		case CYAN: return Material.CYAN_CARPET;
		case PURPLE: return Material.PURPLE_CARPET;
		case BLUE: return Material.BLUE_CARPET;
		case BROWN: return Material.BROWN_CARPET;
		case GREEN: return Material.GREEN_CARPET;
		case RED: return Material.RED_CARPET;
		case BLACK: return Material.BLACK_CARPET;
		default: return null;
		}
	}
	
	public static Material fromColor_concrete(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CONCRETE; 
		case ORANGE: return Material.ORANGE_CONCRETE;
		case MAGENTA: return Material.MAGENTA_CONCRETE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CONCRETE;
		case YELLOW: return Material.YELLOW_CONCRETE;
		case LIME: return Material.LIME_CONCRETE;
		case PINK: return Material.PINK_CONCRETE;
		case GRAY: return Material.GRAY_CONCRETE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CONCRETE;
		case CYAN: return Material.CYAN_CONCRETE;
		case PURPLE: return Material.PURPLE_CONCRETE;
		case BLUE: return Material.BLUE_CONCRETE;
		case BROWN: return Material.BROWN_CONCRETE;
		case GREEN: return Material.GREEN_CONCRETE;
		case RED: return Material.RED_CONCRETE;
		case BLACK: return Material.BLACK_CONCRETE;
		default: return null;
		}
	}
	
	public static Material fromColor_concrete_powder(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_CONCRETE_POWDER; 
		case ORANGE: return Material.ORANGE_CONCRETE_POWDER;
		case MAGENTA: return Material.MAGENTA_CONCRETE_POWDER;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_CONCRETE_POWDER;
		case YELLOW: return Material.YELLOW_CONCRETE_POWDER;
		case LIME: return Material.LIME_CONCRETE_POWDER;
		case PINK: return Material.PINK_CONCRETE_POWDER;
		case GRAY: return Material.GRAY_CONCRETE_POWDER;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_CONCRETE_POWDER;
		case CYAN: return Material.CYAN_CONCRETE_POWDER;
		case PURPLE: return Material.PURPLE_CONCRETE_POWDER;
		case BLUE: return Material.BLUE_CONCRETE_POWDER;
		case BROWN: return Material.BROWN_CONCRETE_POWDER;
		case GREEN: return Material.GREEN_CONCRETE_POWDER;
		case RED: return Material.RED_CONCRETE_POWDER;
		case BLACK: return Material.BLACK_CONCRETE_POWDER;
		default: return null;
		}
	}
	
	public static Material fromColor_stained_glass(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_STAINED_GLASS; 
		case ORANGE: return Material.ORANGE_STAINED_GLASS;
		case MAGENTA: return Material.MAGENTA_STAINED_GLASS;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_STAINED_GLASS;
		case YELLOW: return Material.YELLOW_STAINED_GLASS;
		case LIME: return Material.LIME_STAINED_GLASS;
		case PINK: return Material.PINK_STAINED_GLASS;
		case GRAY: return Material.GRAY_STAINED_GLASS;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_STAINED_GLASS;
		case CYAN: return Material.CYAN_STAINED_GLASS;
		case PURPLE: return Material.PURPLE_STAINED_GLASS;
		case BLUE: return Material.BLUE_STAINED_GLASS;
		case BROWN: return Material.BROWN_STAINED_GLASS;
		case GREEN: return Material.GREEN_STAINED_GLASS;
		case RED: return Material.RED_STAINED_GLASS;
		case BLACK: return Material.BLACK_STAINED_GLASS;
		default: return null;
		}
	}
	
	public static Material fromColor_stained_glass_pane(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_STAINED_GLASS_PANE; 
		case ORANGE: return Material.ORANGE_STAINED_GLASS_PANE;
		case MAGENTA: return Material.MAGENTA_STAINED_GLASS_PANE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_STAINED_GLASS_PANE;
		case YELLOW: return Material.YELLOW_STAINED_GLASS_PANE;
		case LIME: return Material.LIME_STAINED_GLASS_PANE;
		case PINK: return Material.PINK_STAINED_GLASS_PANE;
		case GRAY: return Material.GRAY_STAINED_GLASS_PANE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_STAINED_GLASS_PANE;
		case CYAN: return Material.CYAN_STAINED_GLASS_PANE;
		case PURPLE: return Material.PURPLE_STAINED_GLASS_PANE;
		case BLUE: return Material.BLUE_STAINED_GLASS_PANE;
		case BROWN: return Material.BROWN_STAINED_GLASS_PANE;
		case GREEN: return Material.GREEN_STAINED_GLASS_PANE;
		case RED: return Material.RED_STAINED_GLASS_PANE;
		case BLACK: return Material.BLACK_STAINED_GLASS_PANE;
		default: return null;
		}
	}
	
	public static Material fromColor_wool(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_WOOL; 
		case ORANGE: return Material.ORANGE_WOOL;
		case MAGENTA: return Material.MAGENTA_WOOL;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_WOOL;
		case YELLOW: return Material.YELLOW_WOOL;
		case LIME: return Material.LIME_WOOL;
		case PINK: return Material.PINK_WOOL;
		case GRAY: return Material.GRAY_WOOL;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_WOOL;
		case CYAN: return Material.CYAN_WOOL;
		case PURPLE: return Material.PURPLE_WOOL;
		case BLUE: return Material.BLUE_WOOL;
		case BROWN: return Material.BROWN_WOOL;
		case GREEN: return Material.GREEN_WOOL;
		case RED: return Material.RED_WOOL;
		case BLACK: return Material.BLACK_WOOL;
		default: return null;
		}
	}
	
	public static Material fromColor_shulker_box(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_SHULKER_BOX; 
		case ORANGE: return Material.ORANGE_SHULKER_BOX;
		case MAGENTA: return Material.MAGENTA_SHULKER_BOX;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_SHULKER_BOX;
		case YELLOW: return Material.YELLOW_SHULKER_BOX;
		case LIME: return Material.LIME_SHULKER_BOX;
		case PINK: return Material.PINK_SHULKER_BOX;
		case GRAY: return Material.GRAY_SHULKER_BOX;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_SHULKER_BOX;
		case CYAN: return Material.CYAN_SHULKER_BOX;
		case PURPLE: return Material.PURPLE_SHULKER_BOX;
		case BLUE: return Material.BLUE_SHULKER_BOX;
		case BROWN: return Material.BROWN_SHULKER_BOX;
		case GREEN: return Material.GREEN_SHULKER_BOX;
		case RED: return Material.RED_SHULKER_BOX;
		case BLACK: return Material.BLACK_SHULKER_BOX;
		default: return null;
		}
	}
	
	public static Material fromColor_terracotta(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_TERRACOTTA; 
		case ORANGE: return Material.ORANGE_TERRACOTTA;
		case MAGENTA: return Material.MAGENTA_TERRACOTTA;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_TERRACOTTA;
		case YELLOW: return Material.YELLOW_TERRACOTTA;
		case LIME: return Material.LIME_TERRACOTTA;
		case PINK: return Material.PINK_TERRACOTTA;
		case GRAY: return Material.GRAY_TERRACOTTA;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_TERRACOTTA;
		case CYAN: return Material.CYAN_TERRACOTTA;
		case PURPLE: return Material.PURPLE_TERRACOTTA;
		case BLUE: return Material.BLUE_TERRACOTTA;
		case BROWN: return Material.BROWN_TERRACOTTA;
		case GREEN: return Material.GREEN_TERRACOTTA;
		case RED: return Material.RED_TERRACOTTA;
		case BLACK: return Material.BLACK_TERRACOTTA;
		default: return null;
		}
	}

	public static Material fromColor_glazed_terracotta(DyeColor color) {
		switch(color) {
		case WHITE: return Material.WHITE_GLAZED_TERRACOTTA; 
		case ORANGE: return Material.ORANGE_GLAZED_TERRACOTTA;
		case MAGENTA: return Material.MAGENTA_GLAZED_TERRACOTTA;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_GLAZED_TERRACOTTA;
		case YELLOW: return Material.YELLOW_GLAZED_TERRACOTTA;
		case LIME: return Material.LIME_GLAZED_TERRACOTTA;
		case PINK: return Material.PINK_GLAZED_TERRACOTTA;
		case GRAY: return Material.GRAY_GLAZED_TERRACOTTA;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_GLAZED_TERRACOTTA;
		case CYAN: return Material.CYAN_GLAZED_TERRACOTTA;
		case PURPLE: return Material.PURPLE_GLAZED_TERRACOTTA;
		case BLUE: return Material.BLUE_GLAZED_TERRACOTTA;
		case BROWN: return Material.BROWN_GLAZED_TERRACOTTA;
		case GREEN: return Material.GREEN_GLAZED_TERRACOTTA;
		case RED: return Material.RED_GLAZED_TERRACOTTA;
		case BLACK: return Material.BLACK_GLAZED_TERRACOTTA;
		default: return null;
		}
	}
	
	public static Material fromColor_dye(DyeColor color) {
		switch(color) {
		case WHITE: return Material.BONE_MEAL; 
		case ORANGE: return Material.ORANGE_DYE;
		case MAGENTA: return Material.MAGENTA_DYE;
		case LIGHT_BLUE: return Material.LIGHT_BLUE_DYE;
		case YELLOW: return Material.DANDELION_YELLOW;
		case LIME: return Material.LIME_DYE;
		case PINK: return Material.PINK_DYE;
		case GRAY: return Material.GRAY_DYE;
		case LIGHT_GRAY: return Material.LIGHT_GRAY_DYE;
		case CYAN: return Material.CYAN_DYE;
		case PURPLE: return Material.PURPLE_DYE;
		case BLUE: return Material.LAPIS_LAZULI;
		case BROWN: return Material.COCOA_BEANS;
		case GREEN: return Material.CACTUS_GREEN;
		case RED: return Material.ROSE_RED;
		case BLACK: return Material.INK_SAC;
		default: return null;
		}
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
		case BONE_MEAL: return true;
		case ORANGE_DYE: return true;
		case MAGENTA_DYE: return true;
		case LIGHT_BLUE_DYE: return true;
		case DANDELION_YELLOW: return true;
		case LIME_DYE: return true;
		case PINK_DYE: return true;
		case GRAY_DYE: return true;
		case LIGHT_GRAY_DYE: return true;
		case CYAN_DYE: return true;
		case PURPLE_DYE: return true;
		case LAPIS_LAZULI: return true;
		case COCOA_BEANS: return true;
		case CACTUS_GREEN: return true;
		case ROSE_RED: return true;
		case INK_SAC: return true;
		default: return false;
		}
	}
	
	public static boolean is_flower(Material m) {
		switch(m) {
		case SUNFLOWER: return true;
		case WHITE_TULIP: return true;
		case ORANGE_TULIP: return true;
		case PINK_TULIP: return true;
		case RED_TULIP: return true;
		case DANDELION: return true;
		case ROSE_BUSH: return true;
		case POPPY: return true;
		case BLUE_ORCHID: return true;
		case ALLIUM: return true;
		case AZURE_BLUET: return true;
		case OXEYE_DAISY: return true;
		case LILAC: return true;
		case PEONY: return true;
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
				|| m == Material.GRASS|| m == Material.TALL_GRASS|| m == Material.FERN|| m == Material.LARGE_FERN;
	}

	public static Location searchBlock(Material[] blocks, Location loc, double hor_radius, boolean player_can_stay) {
		boolean x_priority = false, y_priority = false, z_priority = false;
		if(loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = true;
		if(loc.getY() - Math.floor(loc.getY()) > 0.5) y_priority = true;
		if(loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = true;
		boolean x_priorier_z = true;
		if(Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block start_block = loc.getBlock();
		Block found_block = null;
		boolean player_cant_stay = !player_can_stay;
		searching :
		{
			for(int r = 0; r <= 1.1*hor_radius; r++) {
				for(int dy = 0; dy <= r/2; dy++) {
					int temp = r-dy;
					for(int d = 0; d <= temp; d++) {
						int[] dx_pool = (x_priority ? new int[]{d, -d} : new int[]{-d, d}),
							  dz_pool = (z_priority ? new int[]{r-d, d-r} : new int[]{d-r, r-d});
						if(x_priorier_z)
							for(int dx : dx_pool)
								for(int dz : dz_pool) {
									found_block = start_block.getRelative(dx, dy, dz); //low dependency on priority
									if(contains(blocks, found_block.getType()) && (player_cant_stay || Utils.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if(contains(blocks, found_block.getType()) && (player_cant_stay || Utils.playerCanStay(found_block)))
										break searching;
								}
						else
							for(int dz : dz_pool)
								for(int dx : dx_pool) {
									found_block = start_block.getRelative(dx, dy, dz); //low dependency on priority
									if(contains(blocks, found_block.getType()) && (player_cant_stay || Utils.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if(contains(blocks, found_block.getType()) && (player_cant_stay || Utils.playerCanStay(found_block)))
										break searching;
								}
					}
				}
			}
			return null;
		}
		return found_block.getLocation().add(0.5, 0.5, 0.5);
	}

	public static Location searchBlock22Platform(Material[] blocks, Location loc, double hor_radius, boolean player_can_stay) {
		boolean x_priority = false, y_priority = false, z_priority = false;
		if(loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = true;
		if(loc.getY() - Math.floor(loc.getY()) > 0.5) y_priority = true;
		if(loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = true;
		boolean x_priorier_z = true;
		if(Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block start_block = loc.getBlock();
		Block found_block = null;
		boolean player_cant_stay = !player_can_stay;
		searching :
		{
			for(int r = 0; r <= 1.1*hor_radius; r++) {
				for(int dy = 0; dy <= r/2; dy++) {
					int temp = r-dy;
					for(int d = -temp; d <= temp; d++) {
						int[] dx_pool = (x_priority ? new int[]{d, -d} : new int[]{-d, d}),
							  dz_pool = (z_priority ? new int[]{d, -d} : new int[]{-d, d});
						if(x_priorier_z)
							for(int dz : dz_pool)
								for(int dx : dx_pool) {
									found_block = start_block.getRelative(dx, dy, r-Math.abs(dz));
									if(is22Platform(blocks, found_block, x_priority, z_priority, player_cant_stay))
										break searching;
									found_block = start_block.getRelative(dx, dy, dz);
									if(is22Platform(blocks, found_block, x_priority, z_priority, player_cant_stay))
										break searching;
								}
						else
							for(int dx : dx_pool)
								for(int dz : dz_pool) {
									found_block = start_block.getRelative(dx, dy, r-Math.abs(dz));
									if(is22Platform(blocks, found_block, x_priority, z_priority, player_cant_stay))
										break searching;
									found_block = start_block.getRelative(dx, dy, dz);
									if(is22Platform(blocks, found_block, x_priority, z_priority, player_cant_stay))
										break searching;
								}
					}
				}
			}
			return null;
		}
		return found_block.getLocation().add(x_priority ? 1 : 0, 0, z_priority ? 1 : 0);
	}
	
	public static boolean is22Platform(Material[] valid_materials, Block start_block, boolean positive_x, boolean positive_z, boolean player_cant_stay) {
		if(contains(valid_materials, start_block.getType()) && (player_cant_stay || Utils.playerCanFlyOn(start_block))) {
			start_block = start_block.getRelative(0, 0, positive_z ? 1 : -1);
			if(contains(valid_materials, start_block.getType()) && (player_cant_stay || Utils.playerCanFlyOn(start_block))) {
				start_block = start_block.getRelative(positive_x ? 1 : -1, 0, 0);
				if(contains(valid_materials, start_block.getType()) && (player_cant_stay || Utils.playerCanFlyOn(start_block))) {
					start_block = start_block.getRelative(0, 0, positive_z ? -1 : 1);
					if(contains(valid_materials, start_block.getType()) && (player_cant_stay || Utils.playerCanFlyOn(start_block))) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static Location find_horse_space(Location loc) {
		Block start_block = loc.add(0, -1, 0).getBlock();
		loc.add(0, 1, 0);
		if(!playerCanFlyOn(start_block)) return null;
		int x_priority = -1, z_priority = -3;
		if(loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = 1;
		if(loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = 3;
		boolean x_priorier_z = true;
		if(Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block found_block = null;
		int[] grid = new int[9];
		Block b;
		for(int i = 0; i < 9; i++) {
			b = start_block.getRelative(i%3-1, 0, i/3-1);
			if(playerCanStay(b.getRelative(0, 1, 0))) 
				grid[i] = 2;
			else if(playerCanFlyOn(b)) 
				grid[i] = 1;
			else
				grid[i] = 0;
		}
		int[] cells = {4, 4+x_priority, 4+z_priority, 4+x_priority+z_priority};
		for(int i=0; i<4; i++) {
			if(grid[cells[0]] == 2 || grid[cells[1]] == 2 || grid[cells[2]] == 2 || grid[cells[3]] == 2)
				if(grid[cells[0]] > 0 && grid[cells[1]] > 0 && grid[cells[2]] > 0 && grid[cells[3]] > 0)
					return start_block.getLocation().add(((cells[3]+3)%3-1) < 0 ? 0 : 1, 1, cells[3]-4 < 0 ? 0 : 1);
			
			if(i == 0)
				if(x_priorier_z) cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
				else cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
			if(i == 1)
				if(x_priorier_z) cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
				else cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
			if(i == 2)cells = new int[]{4, 4-x_priority, 4-z_priority, 4-x_priority-z_priority};
		}
		return null;
	}
	
	public static boolean contains(Material[] list, Material find) {
		for(Material m : list)
			if(m == find)
				return true;
		return false;
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

	public static boolean equal_invs(Inventory inv1, Inventory inv2) {
		return inv1.toString().endsWith(inv2.toString().substring(inv2.toString().length()-8));
	}
	
	public static boolean isRenamed(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
				&& !item.getItemMeta().getDisplayName().equals((new ItemStack(item.getType())).getItemMeta().getDisplayName());
	}
}
