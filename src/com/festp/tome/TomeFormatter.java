package com.festp.tome;


import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.festp.tome.TomeItemHandler.TomeType;

import net.minecraft.server.v1_16_R1.NBTTagCompound;

public class TomeFormatter {
	
	public static ItemStack setType(ItemStack tome, TomeType type) {
    	switch (type) {
    	case MINECART:
    		tome = TomeFormatter.setTome(tome, 'm', " "); break;
    	case BOAT:
    		tome = TomeFormatter.setTome(tome, 'b', "o"); break; // "o" means "oak"
    	case HORSE:
    		tome = TomeFormatter.setTome(tome, 'h', " "); break;
    	case CUSTOM_HORSE:
    		tome = TomeFormatter.setTome(tome, 'H', " "); break;
    	case ALL:
    		tome = TomeFormatter.setTome(tome, 'a', " "); break;
    	case CUSTOM_ALL:
    		tome = TomeFormatter.setTome(tome, 'A', "o"); break;
    	}
    	return tome;
	}

	public static ItemStack set_boat_type(ItemStack tome, TreeSpecies type) {
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
            compound = new NBTTagCompound();
             nmsStack.setTag(compound);
             compound = nmsStack.getTag();
         }
        
        if(compound.hasKey(TomeItemHandler.TOME_NBT_KEY)) {
    		char[] info = compound.getString(TomeItemHandler.TOME_NBT_KEY).toCharArray();
        	switch(type)
        	{
        	case ACACIA: info[1] = 'a'; break;
        	case BIRCH: info[1] = 'b'; break;
        	case DARK_OAK: info[1] = 'd'; break;
        	case JUNGLE: info[1] = 'j'; break;
        	case GENERIC: info[1] = 'o'; break;
        	case REDWOOD: info[1] = 's'; break;
			}
	        compound.setString(TomeItemHandler.TOME_NBT_KEY, new String(info));
	        nmsStack.setTag(compound);
        }
        tome = CraftItemStack.asBukkitCopy(nmsStack);
		return tome;
	}
	
	public static TreeSpecies get_boat_type(ItemStack tome) {
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound == null)
        	return null;
        if(compound != null && compound.hasKey(TomeItemHandler.TOME_NBT_KEY)) {
        	String info = compound.getString(TomeItemHandler.TOME_NBT_KEY);
        	switch(info.charAt(1))
        	{
        	case 'a': return TreeSpecies.ACACIA;
        	case 'b': return TreeSpecies.BIRCH;
        	case 'd': return TreeSpecies.DARK_OAK;
        	case 'j': return TreeSpecies.JUNGLE;
        	case 'o': return TreeSpecies.GENERIC; //oak
        	case 's': return TreeSpecies.REDWOOD; //spruce
			}
        }
		return TreeSpecies.GENERIC;
	}
	
	
	
	public static HorseFormat get_horse_data(ItemStack tome) {
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
	    NBTTagCompound compound = nmsStack.getTag();
	    if (compound != null && compound.hasKey(TomeItemHandler.TOME_NBT_KEY)) {
	    	String info = compound.getString(TomeItemHandler.TOME_NBT_KEY);
	    	if (info.length() > 2) {
	    		return HorseFormat.fromString(info.substring(2));
	    	}
	    }
    	return null;
	}

	public static ItemStack set_horse_data(ItemStack tome, HorseFormat data) {
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
        	compound = new NBTTagCompound();
        	nmsStack.setTag(compound);
        	compound = nmsStack.getTag();
        }
        
        compound.setString(TomeItemHandler.TOME_NBT_KEY, compound.getString(TomeItemHandler.TOME_NBT_KEY).substring(0, 2) + data.toString());
        nmsStack.setTag(compound);
    	
		return CraftItemStack.asBukkitCopy(nmsStack);
	}
	
	public static ItemStack set_boat(ItemStack tome, ItemStack boat) {

		Material wood_type = boat.getType();
		if(wood_type == Material.ACACIA_BOAT)
			return setTome(tome, 'b', "a");
		else if(wood_type == Material.BIRCH_BOAT)
			return setTome(tome, 'b', "b");
		else if(wood_type == Material.DARK_OAK_BOAT)
			return setTome(tome, 'b', "d");
		else if(wood_type == Material.JUNGLE_BOAT)
			return setTome(tome, 'b', "j");
		else if(wood_type == Material.SPRUCE_BOAT)
			return setTome(tome, 'b', "s");
		return tome;
	}
	public static TomeType getTomeType(ItemStack item) {
		if(item == null)
			return null;
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsStack.getTag();
        if(compound == null)
        	return null;
        if(compound != null && compound.hasKey(TomeItemHandler.TOME_NBT_KEY)) {
        	String info = compound.getString(TomeItemHandler.TOME_NBT_KEY);
        	if(info.startsWith("m")) {
				return TomeType.MINECART;
			}
			else if(info.startsWith("b")) {
				return TomeType.BOAT;
			}
			else if(info.startsWith("h")) {
				return TomeType.HORSE;
			}
			else if(info.startsWith("H")) {
				return TomeType.CUSTOM_HORSE;
			}
			else if(info.startsWith("a")) {
				return TomeType.ALL;
			}
			else if(info.startsWith("A")) {
				return TomeType.CUSTOM_ALL;
			}
        }
		return null;
	}
	public static ItemStack setTome(ItemStack i, char data, String metadata) {
		net.minecraft.server.v1_16_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        
        compound.setString(TomeItemHandler.TOME_NBT_KEY, data+metadata);
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
}
