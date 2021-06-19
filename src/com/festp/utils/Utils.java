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
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Cauldron;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.festp.DelayedTask;
import com.festp.Main;
import com.festp.TaskList;
import com.festp.storages.Storage;
import com.festp.storages.StorageBottomless;
import com.festp.storages.StorageMultitype;

import net.minecraft.nbt.NBTTagCompound;

public class Utils {
	private static Main plugin;
	private static UnsafeValues legacy;
	private static Team team_no_collide;
	private static final String BUKKIT_PACKAGE = "org.bukkit.craftbukkit.";
	public static final double EPSILON = 0.0001;
	
	public static void setPlugin(Main pl) {
		plugin = pl;
		legacy = pl.getServer().getUnsafe();
	}

	public static Main getPlugin() {
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
	public static String toString(Block b) {
		if (b == null) return toString((Location)null);
		return toString(b.getLocation());
	}
	
	// TODO UtilsNBT
	public static ItemStack setData(ItemStack i, String field, Object data) {
        if (data == null || field == null || i == null)
            return i;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
        	compound = new NBTTagCompound();
        	nmsStack.setTag(compound);
        	compound = nmsStack.getTag();
        }
        
        if (data instanceof String)
        	compound.setString(field, (String)data);
        else if (data instanceof Integer)
        	compound.setInt(field, (Integer)data);
        else if (data instanceof Boolean)
        	compound.setBoolean(field, (Boolean)data);
        else if (data instanceof byte[])
        	compound.setByteArray(field, (byte[])data);
        
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
	
	public static String getString(ItemStack i, String field) {
        if (field == null || i == null)
            return null;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null || !compound.hasKey(field))
            return null;
        return compound.getString(field);
	}
	
	private static NBTTagCompound get(ItemStack i, String field) {
        if (field == null || i == null)
            return null;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        return compound;
	}
	public static Integer getInt(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !compound.hasKey(field))
            return null;
        return compound.getInt(field);
	}
	
	public static Boolean getBoolean(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !compound.hasKey(field))
            return null;
        return compound.getBoolean(field);
	}
	
	public static byte[] getByteArray(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !compound.hasKey(field))
            return null;
        return compound.getByteArray(field);
	}
	
	public static boolean hasDataField(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if(compound != null && compound.hasKey(field))
        	return true;
        return false;
	}
	
	public static boolean hasData(ItemStack i, String field, String data) {
		NBTTagCompound compound = get(i, field);
        if(data != null && compound != null && compound.hasKey(field) && data.equalsIgnoreCase(compound.getString(field)))
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
	
	/** format: "entity.CraftHorse" or "org.bukkit.craftbukkit.v1_17_R1.entity.CraftHorse" */
	public static Class<?> getBukkitClass(String name) {
		if (!name.startsWith(BUKKIT_PACKAGE)) {
			String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		    name = BUKKIT_PACKAGE + version + "." + name;
		}
		
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	public static String getShortBukkitClass(Class<?> clazz) {
		String fullName = clazz.getName();
		if (!fullName.startsWith(BUKKIT_PACKAGE)) {
			return fullName;
		}
		String name = fullName.substring(BUKKIT_PACKAGE.length());
		return name.substring(name.indexOf(".") + 1);
	}
	
	public static <T extends LivingEntity> T spawnBeacon(Location l, Class<T> entity_type, String beacon_id, boolean gravity) {
 		T new_beacon =  l.getWorld().spawn(l, entity_type, (beacon) ->
 		{
 			if (entity_type == Vex.class)
 				beacon.getEquipment().setItemInMainHand(null); // must be applied immediately
 	 		
 	 		beacon.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000000, 1, false, false));
 	 		beacon.setInvulnerable(true);
 	 		if (!gravity) {
 	 	 		beacon.setAI(false);
 	 	 		beacon.setGravity(false);
 	 		}
 	 		beacon.setSilent(true);
 	 		beacon.setCollidable(false);
 	 		
 	 		if (beacon instanceof Turtle) {
 	 			Turtle turtle = (Turtle)beacon;
 	 			turtle.setBaby();
 	 			turtle.setAgeLock(true);
 	 		}

 	 		if (beacon instanceof ArmorStand) {
 	 			ArmorStand stand = (ArmorStand)beacon;
 	 			stand.setVisible(false);
 	 			stand.setSmall(true);
 	 		}
 	 		setBeaconData(beacon, beacon_id); // must be applied immediately
        });
 		
 		return new_beacon;
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
				result += st.toString();
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
	
	public static boolean contains(Object[] list, Object find) {
		for (Object m : list)
			if (m == find)
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

	public static void printStackTracePeak(Exception e, int n) {
		String error = "";
		StackTraceElement[] elems = e.getStackTrace();
		for (int i = 0; i < elems.length && i < n; i++) {
			error += elems[i].toString() + "\n";
		}
		printError(error);
	}
	
	public static void delayUpdate(Inventory inv) {
		DelayedTask task = new DelayedTask(1, new Runnable() {
			@Override
			public void run() {
				StorageBottomless.update_item_counts(inv);
				for (HumanEntity human : inv.getViewers()) {
					((Player)human).updateInventory();
				}
				
				// CAN'T PREVENT OFF HAND GLITCH! (InventoryClickEcent cancelling)
				if (inv instanceof PlayerInventory) {
					PlayerInventory pInv = (PlayerInventory) inv;
					ItemStack item = pInv.getItemInOffHand();
					/*if (item == null || UtilsType.isAir(item.getType())) {
						pInv.setItemInOffHand(new ItemStack(Material.STONE));
					} else {
						pInv.setItemInOffHand(null);
					}*/
					pInv.setItemInOffHand(item);
				}
			}
		});
		TaskList.add(task);
	}
}
