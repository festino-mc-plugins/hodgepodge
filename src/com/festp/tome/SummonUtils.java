package com.festp.tome;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.NBTUtils;
import com.festp.utils.UtilsWorld;

import net.minecraft.nbt.NBTTagCompound;

public class SummonUtils {
	
	private static final Material[] BOAT_BLOCKS =
			{Material.WATER, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE, Material.FROSTED_ICE, Material.SEA_PICKLE, Material.SEAGRASS, Material.TALL_SEAGRASS};
	
	public static Location findForMinecart(Location player_loc, double hor_radius) {
		Location l = UtilsWorld.searchBlock(new Material[]
				{Material.RAIL, Material.ACTIVATOR_RAIL, Material.DETECTOR_RAIL, Material.POWERED_RAIL},
				player_loc, hor_radius, false);
		
		return l;
	}
	public static Minecart summonMinecart(Location l, Player p, boolean main_hand) {
		Minecart mc = l.getWorld().spawn(l, Minecart.class);
		mc.addPassenger(p);
		//mc.setMetadata("fromtome", new FixedMetadataValue(plugin, true));
		setSummoned(mc);
		
		ItemStack tome;
		if(main_hand) tome = p.getInventory().getItemInMainHand();
		else tome = p.getInventory().getItemInOffHand();
		tome = setHasSummoned(tome, mc.getUniqueId());
		if(main_hand) p.getInventory().setItemInMainHand(tome);
		else p.getInventory().setItemInOffHand(tome);
		return mc;
	}
	
	public static Location findForBoat(Location player_loc, double hor_radius) { // TODO: watered bottom blocks
		Location loc = player_loc.clone();
		loc.add(0, 0.5, 0);
		loc.setY(Math.floor(loc.getY() - 1));
		Location l_3x3 = UtilsWorld.search33space(BOAT_BLOCKS, loc);
		Location l_2x2 = UtilsWorld.searchBlock22Platform(BOAT_BLOCKS, loc, hor_radius, false);
		
		Location l = l_3x3;
		if (l == null) {
			l = l_2x2;
		} else if (l_2x2 != null) {
			if (loc.distanceSquared(l_2x2) < loc.distanceSquared(l)) {
				l = l_2x2;
			}
		}
		return l;
	}
	public static Boat summonBoat(Location l, Player p, boolean main_hand, TreeSpecies type) {
		l.setPitch(p.getLocation().getPitch());
		l.setYaw(p.getLocation().getYaw());
		Boat boat = l.getWorld().spawn(l, Boat.class);
		boat.setWoodType(type);
		boat.addPassenger(p);
		setSummoned(boat);
		
		ItemStack tome;
		if(main_hand) tome = p.getInventory().getItemInMainHand();
		else tome = p.getInventory().getItemInOffHand();
		tome = setHasSummoned(tome, boat.getUniqueId());
		if(main_hand) p.getInventory().setItemInMainHand(tome);
		else p.getInventory().setItemInOffHand(tome);
		return boat;
	}

	public static Horse summonHorse(Location l, Player p, boolean main_hand) {
		Horse horse = l.getWorld().spawn(l, Horse.class, (new_horse) ->
		{
			new_horse.setTamed(true);
			new_horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
			new_horse.setOwner(p);
			new_horse.addPassenger(p);
			setSummoned(new_horse);
			
			ItemStack tome;
			if (main_hand)
				tome = p.getInventory().getItemInMainHand();
			else
				tome = p.getInventory().getItemInOffHand();
			
			tome = setHasSummoned(tome, new_horse.getUniqueId());
			if (main_hand)
				p.getInventory().setItemInMainHand(tome);
			else
				p.getInventory().setItemInOffHand(tome);
		});
		return horse;
	}
	
	public static AbstractHorse summonCustomHorse(Location l, Player p, boolean main_hand)
	{
		ItemStack tome_item;
		if (main_hand)
			tome_item = p.getInventory().getItemInMainHand();
		else
			tome_item = p.getInventory().getItemInOffHand();

		HorseFormat horse_data = TomeFormatter.get_horse_data(tome_item);
		
		Class<? extends AbstractHorse> type;
		if (horse_data == null) {
			type = Horse.class;
		} else {
			type = horse_data.getHorseClass();
		}
		
		AbstractHorse horse = summonCustomHorse(l, type, new HorseSetter()
		{
			@Override
			public void set(AbstractHorse new_horse)
			{
				new_horse.setTamed(true);
				new_horse.getInventory().setSaddle(new ItemStack(Material.SADDLE)); // if horse_data == null
				new_horse.setOwner(p);
				new_horse.addPassenger(p);
				setSummoned(new_horse);
				setCustomHorse(new_horse);

				ItemStack tome;
				if (main_hand)
					tome = p.getInventory().getItemInMainHand();
				else
					tome = p.getInventory().getItemInOffHand();
				
				tome = setHasSummoned(tome, new_horse.getUniqueId());
				if (tome.getItemMeta().hasDisplayName() && !(TomeItemHandler.name_eng_custom_horse_tome.contains(tome.getItemMeta().getDisplayName())
						|| TomeItemHandler.name_eng_custom_all_tome.contains(tome.getItemMeta().getDisplayName()))) {
					new_horse.setCustomName(tome.getItemMeta().getDisplayName());
				}
				
				if (horse_data == null)
					tome = TomeFormatter.set_horse_data(tome, HorseFormat.fromHorse(new_horse));
				else
					horse_data.applyToHorse(new_horse);

				if (main_hand)
					p.getInventory().setItemInMainHand(tome);
				else
					p.getInventory().setItemInOffHand(tome);
				
				if (!new_horse.isAdult())
					new_horse.setAgeLock(true);
			}
		});
		
		return horse;
	}
	
	
	public static AbstractHorse summonCustomHorse(Location l, Class<? extends AbstractHorse> type, HorseSetter setter) {
		return l.getWorld().spawn(l, type, (new_horse) -> setter.set(new_horse));
	}
	
	public static interface HorseSetter {
		public void set(AbstractHorse new_horse);
	}


	public static boolean wasSummoned(Entity e) {
		if(e != null) {
			return e.getScoreboardTags().contains("fromtome");
		}
		
		return false;
	}
	public static void setSummoned(Entity e) {
		if(e != null) {
			e.addScoreboardTag("fromtome");
		}
	}
	public static boolean isCustomHorse(Entity horse) {
		if(horse != null && horse instanceof AbstractHorse) {
			return horse.getScoreboardTags().contains("customhorse");
		}
		
		return false;
	}
	public static void setCustomHorse(Entity horse) {
		if(horse != null && horse instanceof AbstractHorse) {		
			horse.addScoreboardTag("customhorse");
		}
	}
	
	public static boolean hasSummoned(ItemStack tome) {
		return getHasSummoned(tome) != null;
	}
	public static ItemStack setHasSummoned(ItemStack tome, UUID entity_uuid) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
        NBTTagCompound compound = NBTUtils.getTag(nmsStack);
        if (compound == null) {
            compound = new NBTTagCompound();
            NBTUtils.setTag(nmsStack, compound);
            compound = NBTUtils.getTag(nmsStack);
        }
        
        if (entity_uuid == null) NBTUtils.remove(compound, "hassummoned");
        else NBTUtils.setString(compound, "hassummoned", entity_uuid.toString());
        NBTUtils.setTag(nmsStack, compound);
        tome = CraftItemStack.asBukkitCopy(nmsStack);
        return tome;
	}
	public static Entity getHasSummoned(ItemStack tome) {
		if (tome == null)
			return null;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(tome);
        NBTTagCompound compound = NBTUtils.getTag(nmsStack);
        if (compound == null)
        	return null;
        if ( NBTUtils.hasKey(compound, "hassummoned") ) {
        	UUID entity_uuid = UUID.fromString(NBTUtils.getString(compound, "hassummoned"));
        	return Bukkit.getEntity(entity_uuid);
        }
		return null;
	}
}
