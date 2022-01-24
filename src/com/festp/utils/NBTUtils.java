package com.festp.utils;

import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Should be checked every spigot update
 */
public class NBTUtils {
	public static NBTTagCompound getTag(net.minecraft.world.item.ItemStack nmsStack)
	{
		return nmsStack.s(); //.getTag();
	}

	public static void setTag(net.minecraft.world.item.ItemStack nmsStack, NBTTagCompound compound) {
    	nmsStack.c(compound); //.setTag(compound);
	}
	
	public static boolean hasKey(NBTTagCompound compound, String key)
	{
		return compound.e(key); //.hasKey(key)
	}

	public static void remove(NBTTagCompound compound, String key) {
        compound.r(key); //.remove(key);
	}
	
	public static int getInt(NBTTagCompound compound, String key)
	{
    	return compound.h(key); //.getInt(key);
	}

	public static void setInt(NBTTagCompound compound, String key, int value) {
        compound.a(key, value); //.setInt(key, n);
	}
	
	public static String getString(NBTTagCompound compound, String key)
	{
    	return compound.l(key); //.getString(key);
	}

	public static void setString(NBTTagCompound compound, String key, String value) {
        compound.a(key, value); //.setString(key, n);
	}
	
	public static boolean getBoolean(NBTTagCompound compound, String key)
	{
    	return compound.q(key);
	}

	public static void setBoolean(NBTTagCompound compound, String key, boolean value) {
        compound.a(key, value);
	}
	
	public static byte[] getByteArray(NBTTagCompound compound, String key)
	{
    	return compound.m(key);
	}

	public static void setByteArray(NBTTagCompound compound, String key, byte[] value) {
        compound.a(key, value);
	}

	
	
	public static ItemStack setData(ItemStack i, String field, Object data) {
        if (data == null || field == null || i == null)
            return i;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = getTag(nmsStack);
        if (compound == null) {
        	compound = new NBTTagCompound();
        	setTag(nmsStack, compound);
        	compound = getTag(nmsStack);
        }
        
        if (data instanceof String)
        	setString(compound, field, (String)data);
        else if (data instanceof Integer)
        	setInt(compound, field, (Integer)data);
        else if (data instanceof Boolean)
        	setBoolean(compound, field, (Boolean)data);
        else if (data instanceof byte[])
        	setByteArray(compound, field, (byte[])data);
        
        setTag(nmsStack, compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
	
	public static String getString(ItemStack i, String field) {
        if (field == null || i == null)
            return null;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = getTag(nmsStack);
        if (compound == null || !hasKey(compound, field))
            return null;
        return getString(compound, field);
	}
	
	private static NBTTagCompound get(ItemStack i, String field) {
        if (field == null || i == null)
            return null;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = getTag(nmsStack);
        return compound;
	}
	public static Integer getInt(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !hasKey(compound, field))
            return null;
        return getInt(compound, field);
	}
	
	public static Boolean getBoolean(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !hasKey(compound, field))
            return null;
        return getBoolean(compound, field);
	}
	
	public static byte[] getByteArray(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if (compound == null || !hasKey(compound, field))
            return null;
        return getByteArray(compound, field);
	}
	
	public static boolean hasDataField(ItemStack i, String field) {
		NBTTagCompound compound = get(i, field);
        if(compound != null && hasKey(compound, field))
        	return true;
        return false;
	}
	
	public static boolean hasData(ItemStack i, String field, String data) {
		NBTTagCompound compound = get(i, field);
        if(data != null && compound != null && hasKey(compound, field) && data.equalsIgnoreCase(getString(compound, field)))
        	return true;
        return false;
	}
}
