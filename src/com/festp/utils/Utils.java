package com.festp.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class Utils {
	public static final double EPSILON = 0.0001;

	
	/** @return 3 if full, 0 if empty or invalid bd*/
	public static double getCauldronLevel(BlockData bd) {
		if (bd == null || bd.getMaterial() != Material.WATER_CAULDRON) {
			return 0;
		}
		Levelled cauldron = (Levelled) bd;
		return cauldron.getLevel();
	}
	public static double getCauldronWater(Block cauldron) {
		Levelled caul = (Levelled)cauldron.getBlockData();
		return caul.getLevel() / (double)caul.getMaximumLevel();
	}
	public static boolean lowerCauldronWater(Block cauldron) {
		Levelled caul = (Levelled)cauldron.getBlockData();
		if (caul.getLevel() == 0)
			return false;
		if (caul.getLevel() == 1)
		{
			cauldron.setType(Material.CAULDRON);
			return true;
		}
		caul.setLevel(caul.getLevel() - 1);
		cauldron.setBlockData(caul);
		return true;
	}
	public static boolean fullCauldronWater(Block cauldron) {
		if (cauldron.getType() == Material.CAULDRON) {
			cauldron.setType(Material.WATER_CAULDRON);
		}
		Levelled caul = (Levelled)cauldron.getBlockData();
		if (caul.getLevel() == caul.getMaximumLevel())
			return false;
		caul.setLevel(caul.getMaximumLevel());
		cauldron.setBlockData(caul);
		return true;
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
	
	// used in Sleeping
	public static BlockFace getDir(Location l) {
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
		HashMap<Integer, ItemStack> res = inv.addItem(stack);
		if (res.isEmpty())
			return null;
		return dropUngiven(inv.getLocation(), res.get(0));
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
}
