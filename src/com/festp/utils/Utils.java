package com.festp.utils;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.Levelled;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.festp.mainListener;
import com.festp.storages.Storage;
import com.festp.storages.StorageMultitype;

import net.minecraft.server.v1_14_R1.EntityAgeable;
import net.minecraft.server.v1_14_R1.EntityAnimal;
import net.minecraft.server.v1_14_R1.NBTTagCompound;

public class Utils {
	private static mainListener plugin;
	private static UnsafeValues legacy;
	private static Team team_no_collide;
	public static final double EPSILON = 0.0001;
	
	public static void setPlugin(mainListener pl) {
		plugin = pl;
		legacy = pl.getServer().getUnsafe();
	}

	public static mainListener getPlugin() {
		return plugin;
	}
	
	public static void printError(String msg) {
		plugin.getLogger().severe(msg);
	}

	public static void onEnable()
	{
		//create no collide turtle team
		String team_name = "HPTempNoCollide"; //HP is HodgePodge, limit of 16 characters
		Server server = plugin.getServer();
		Scoreboard sb = server.getScoreboardManager().getMainScoreboard();
		team_no_collide = sb.getTeam(team_name);
		if (team_no_collide == null)
			team_no_collide = sb.registerNewTeam(team_name);
		team_no_collide.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
	}
	public static void onDisable()
	{
		team_no_collide.unregister(); //if plugin will be removed anywhen
	}
	
	public static void setNoCollide(Entity e, boolean val)
	{
		String entry = e.getUniqueId().toString();
		if (val) {
			if (!team_no_collide.hasEntry(entry))
				team_no_collide.addEntry(entry);
		} else
			team_no_collide.removeEntry(entry);
	}
	
	public static void noGravityTemp(LivingEntity e, int ticks)
	{
		e.removePotionEffect(PotionEffectType.LEVITATION);
		if (ticks > 0)
			e.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, ticks, 255));
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
	
	public static String toString(Vector v) {
		if (v == null)
			return "(null)";
		DecimalFormat dec = new DecimalFormat("#0.00");
		return ("("+dec.format(v.getX())+"; "
				  +dec.format(v.getY())+"; "
				  +dec.format(v.getZ())+")")
				.replace(',', '.');
	}
	public static String toString(Location l) {
		if (l == null) return toString((Vector)null);
		return toString(new Vector(l.getX(), l.getY(), l.getZ()));
	}
	
	public static ItemStack setData(ItemStack i, String field, String data) {
        if (data == null || field == null || i == null)
            return i;
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
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
	
	public static String getData(ItemStack i, String field) {
        if (field == null || i == null)
            return null;
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null || !compound.hasKey(field))
            return null;
        return compound.getString(field);
	}
	
	public static boolean hasDataField(ItemStack i, String field) {
        if (field == null || i == null)
            return false;
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound != null && compound.hasKey(field))
        	return true;
        return false;
	}
	
	public static boolean hasData(ItemStack i, String field, String data) {
        if (data == null || field == null || i == null)
            return false;
		net.minecraft.server.v1_14_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound != null && compound.hasKey(field) && data.equalsIgnoreCase(compound.getString(field)))
        	return true;
        return false;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack getHead(String headName, String texture_url) {
		ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
		stack = Bukkit.getUnsafe().modifyItemStack(stack,
				"{SkullOwner:{Id:" + UUID.randomUUID().toString() + ",Properties:{textures:[{Value:\"" + texture_url + "\"}]}}}");
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(headName);
		stack.setItemMeta(meta);
		return stack;
	}
	
	public static <T extends LivingEntity> T spawnBeacon(Location l, Class<T> entity, String beacon_id, boolean gravity) {
		T beacon;
 		if(entity == Vex.class) { //doesn't work without the consumer
 			beacon = l.getWorld().spawn(l, entity, (vex) -> {
 				vex.getEquipment().setItemInMainHand(null);
            });
 		}
 		else
 			beacon = l.getWorld().spawn(l, entity);
 			
 		beacon.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000000, 1, false, false));
 		beacon.setInvulnerable(true);
 		if (!gravity) {
 	 		beacon.setAI(false);
 	 		beacon.setGravity(false);
 		}
 		beacon.setSilent(true);
 		beacon.setCollidable(false);
 		
 		if(beacon instanceof Turtle) {
 			Turtle turtle = (Turtle)beacon;
 			turtle.setBaby();
 			turtle.setAgeLock(true);
 		}

 		if(beacon instanceof ArmorStand) {
 			ArmorStand stand = (ArmorStand)beacon;
 			stand.setVisible(false);
 			stand.setSmall(true);
 		}
 		setBeaconData(beacon, beacon_id);
	 	
 		return beacon;
	}
	public static void setBeaconData(LivingEntity beacon, String beacon_id)
	{
		ItemStack identificator = new ItemStack(Material.BARRIER); //to identify issues
	 	identificator = setData(identificator, beacon_id, "+");
 		//if(beacon instanceof ArmorStand)
 		beacon.getEquipment().setChestplate(identificator);
 		//else
 	 	//	beacon.getEquipment().setHelmet(identificator);
	}
	public static boolean hasBeaconData(LivingEntity beacon, String beacon_id)
	{
		ItemStack identificator = beacon.getEquipment().getChestplate();
	 	return hasDataField(identificator, beacon_id);
	}
	
	public static boolean contains_all_of(String str, String... args) {
		for(String s : args)
			if(!str.contains(s))
				return false;
		return true;
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
									if(contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if(contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
								}
						else
							for(int dz : dz_pool)
								for(int dx : dx_pool) {
									found_block = start_block.getRelative(dx, dy, dz); //low dependency on priority
									if(contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
										break searching;
									found_block = start_block.getRelative(dx, -dy, dz);
									if(contains(blocks, found_block.getType()) && (player_cant_stay || UtilsType.playerCanStay(found_block)))
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
		if(contains(valid_materials, start_block.getType()) && (player_cant_stay || UtilsType.playerCanFlyOn(start_block)))
		{
			start_block = start_block.getRelative(0, 0, positive_z ? 1 : -1);
			if(contains(valid_materials, start_block.getType()) && (player_cant_stay || UtilsType.playerCanFlyOn(start_block)))
			{
				start_block = start_block.getRelative(positive_x ? 1 : -1, 0, 0);
				if(contains(valid_materials, start_block.getType()) && (player_cant_stay || UtilsType.playerCanFlyOn(start_block)))
				{
					start_block = start_block.getRelative(0, 0, positive_z ? -1 : 1);
					if(contains(valid_materials, start_block.getType()) && (player_cant_stay || UtilsType.playerCanFlyOn(start_block)))
					{
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
		if ( !UtilsType.playerCanFlyOn(start_block) ) return null;
		int x_priority = -1, z_priority = -3;
		if (loc.getX() - Math.floor(loc.getX()) > 0.5) x_priority = 1;
		if (loc.getZ() - Math.floor(loc.getZ()) > 0.5) z_priority = 3;
		boolean x_priorier_z = true;
		if (Math.abs(loc.getX() - Math.floor(loc.getX()) - 0.5) < Math.abs(loc.getZ() - Math.floor(loc.getZ() - 0.5)))
			x_priorier_z = false;
		Block found_block = null;
		int[] grid = new int[9];
		Block b;
		for (int i = 0; i < 9; i++) {
			b = start_block.getRelative(i%3-1, 0, i/3-1);
			if (UtilsType.playerCanStay(b.getRelative(0, 1, 0))) 
				grid[i] = 2;
			else if ( UtilsType.playerCanFlyOn(b) ) 
				grid[i] = 1;
			else
				grid[i] = 0;
		}
		int[] cells = {4, 4+x_priority, 4+z_priority, 4+x_priority+z_priority};
		for (int i = 0; i < 4; i++) {
			if (grid[cells[0]] == 2 || grid[cells[1]] == 2 || grid[cells[2]] == 2 || grid[cells[3]] == 2)
				if (grid[cells[0]] > 0 && grid[cells[1]] > 0 && grid[cells[2]] > 0 && grid[cells[3]] > 0)
					return start_block.getLocation().add(((cells[3]+3)%3-1) < 0 ? 0 : 1, 1, cells[3]-4 < 0 ? 0 : 1);
			
			if (i == 0)
				if (x_priorier_z) cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
				else cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
			if (i == 1)
				if (x_priorier_z) cells = new int[]{4, 4-x_priority, 4+z_priority, 4-x_priority+z_priority};
				else cells = new int[]{4, 4+x_priority, 4-z_priority, 4+x_priority-z_priority};
			if (i == 2)cells = new int[]{4, 4-x_priority, 4-z_priority, 4-x_priority-z_priority};
		}
		return null;
	}
	
	public static boolean contains(Material[] list, Material find) {
		for(Material m : list)
			if(m == find)
				return true;
		return false;
	}

	public static boolean equal_invs(Inventory inv1, Inventory inv2) {
		return inv1.toString().endsWith(inv2.toString().substring(inv2.toString().length()-8));
	}
	
	public static boolean isRenamed(ItemStack item) {
		return item.hasItemMeta() && item.getItemMeta().hasDisplayName()
				&& !item.getItemMeta().getDisplayName().equals((new ItemStack(item.getType())).getItemMeta().getDisplayName());
	}
	
	/**@return <b>true</b> if the <b>stack</b> was given<br>
	 * <b>false</b> if the <b>stack</b> can't be given without stacking*/
	public static boolean giveUnstackable(Inventory inv, ItemStack stack)
	{
		ItemStack[] stacks = inv.getStorageContents();
		for (int i = 0; i < stacks.length; i++)
		{
			if (stacks[i] == null)
			{
				stacks[i] = stack.clone();
				inv.setStorageContents(stacks);
				stack.setAmount(0);
				return true;
			}
		}
		return false;
	}
	
	/**@return <b>null</b> if the <b>stack</b> was only given<br>
	 * <b>Item</b> if at least one item was dropped*/
	public static Item giveOrDrop(Inventory inv, ItemStack stack)
	{
		int amount = stack.getAmount();
		amount -= StorageMultitype.grabItemStack_stacking(inv, stack);
		if (amount == 0)
			return null;
		amount -= StorageMultitype.grabItemStack_any(inv, stack, amount);
		if (amount == 0)
			return null;
		stack.setAmount(amount);
		return dropUngiven(inv.getLocation(), stack);
	}
	/** Can give items only to players.
	 * @return <b>null</b> if the <b>stack</b> was only given<br>
	 * <b>Item</b> if at least one item was dropped*/
	public static Item giveOrDrop(Entity entity, ItemStack stack)
	{
		if (entity instanceof Player)
			return giveOrDrop(((Player)entity).getInventory(), stack);
		return dropUngiven(entity.getLocation(), stack);
	}
	private static Item dropUngiven(Location l, ItemStack stack) {
		Item item = l.getWorld().dropItem(l, stack);
		item.setVelocity(new Vector());
		item.setPickupDelay(0);
		return item;
	}
	
	public static int countEmpty(ItemStack[] inv)
	{
		int empty = 0;
		for (ItemStack is : inv)
			if (is == null)
				empty++;
		return empty;
	}
	
	public static <T> int indexOf(T needle, T[] haystack)
	{
	    for (int i=0; i<haystack.length; i++)
	    {
	        if (haystack[i] != null && haystack[i].equals(needle)
	            || needle == null && haystack[i] == null) return i;
	    }

	    return -1;
	}
	public static <T> T next(T needle, T[] haystack)
	{
		int index = Utils.indexOf(needle, haystack);
		if (index < 0)
			return null;
		return haystack[(index + 1) % haystack.length];
	}
	public static <T> T prev(T needle, T[] haystack)
	{
		int index = Utils.indexOf(needle, haystack);
		if (index < 0)
			return null;
		return haystack[((index - 1) % haystack.length + haystack.length) % haystack.length];
	}
	
	public static ItemStack setShulkerInventory(ItemStack shulker_box, Inventory inv)
	{
    	BlockStateMeta im = (BlockStateMeta)shulker_box.getItemMeta();
    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
    	shulker.getInventory().setContents(inv.getStorageContents());
    	im.setBlockState(shulker);
    	shulker_box.setItemMeta(im);
    	return shulker_box;
	}
	public static Inventory getShulkerInventory(ItemStack shulker_box)
	{
    	BlockStateMeta im = (BlockStateMeta)shulker_box.getItemMeta();
    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
    	return shulker.getInventory();
	}
}
