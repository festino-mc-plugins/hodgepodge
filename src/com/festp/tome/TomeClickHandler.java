package com.festp.tome;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.festp.tome.TomeItemHandler.TomeType;
import com.festp.utils.UtilsType;
import com.festp.utils.UtilsWorld;

import net.minecraft.server.v1_16_R2.NBTTagCompound;

public class TomeClickHandler implements Listener {

	public static final double searching_radius_minecart_tome = 1.5;
	public static final double searching_radius_boat_tome = 2.5;

	private List<AbstractHorse> save_horse = new ArrayList<>();
	private List<Player> save_player = new ArrayList<>();
	
	public void addSavingTome(AbstractHorse horse, Player p) {
		save_horse.add(horse);
		save_player.add(p);
	}
	
	public void saveAll() {
		for (int i = save_horse.size()-1; i >= 0; i--) {
			process_custom_horse(save_horse.get(i), save_player.get(i));
			save_horse.remove(i);
			save_player.remove(i);
		}
	}
	
	//Customization
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) { //PlayerInteractAtEntityEvent
		if (SummonUtils.wasSummoned(event.getRightClicked())) return;
		
		boolean main_hand = true;
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item == null) {
			main_hand = false;
			item = event.getPlayer().getInventory().getItemInOffHand();
			if (item == null)
				return;
		}

		TomeType type = TomeFormatter.getTomeType(item);
		if (type == null) return;
		if (SummonUtils.hasSummoned(item)) return;
		
		Entity entity = event.getRightClicked();
		if (entity instanceof Boat && (type == TomeType.BOAT || type == TomeType.ALL || type == TomeType.CUSTOM_ALL)
				&& TomeFormatter.get_boat_type(item) != ((Boat)entity).getWoodType()) {
			TreeSpecies prev_type = TomeFormatter.get_boat_type(item);
			if (main_hand)
				event.getPlayer().getInventory().setItemInMainHand(TomeFormatter.set_boat_type(item, ((Boat)entity).getWoodType()));
			else
				event.getPlayer().getInventory().setItemInOffHand(TomeFormatter.set_boat_type(item, ((Boat)entity).getWoodType()));
			((Boat)entity).setWoodType(prev_type);
			event.setCancelled(true);
		}
		else if (entity instanceof AbstractHorse && (type == TomeType.CUSTOM_HORSE || type == TomeType.CUSTOM_ALL)) {
			AbstractHorse horse = (AbstractHorse) entity;
			if (horse.getInventory().getSaddle() != null) {
				HorseFormat old_data = TomeFormatter.get_horse_data(item);
				HorseFormat new_data = HorseFormat.fromHorse(horse);
				if (old_data == null)
					horse.remove();
				else
					old_data.applyToHorse(horse);
				
				ItemStack updated_tome = TomeFormatter.set_horse_data(item, new_data);
				if (main_hand)
					event.getPlayer().getInventory().setItemInMainHand(updated_tome);
				else
					event.getPlayer().getInventory().setItemInOffHand(updated_tome);
				event.setCancelled(true);
			}
		}
	}
	
	//Summoning
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getPlayer().isInsideVehicle()) return;
		if (!(event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK && !UtilsType.isInteractable(event.getClickedBlock().getType()) ))
			return;

		boolean in_main_hand = true;
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item == null || UtilsType.is_dye(item.getType())) { //dye only on coloring click
			in_main_hand = false;
			item = event.getPlayer().getInventory().getItemInOffHand();
			if (item == null || UtilsType.is_dye(item.getType()))
				return;
		}
		TomeType type = TomeFormatter.getTomeType(item);
		if (type == null) return;

		Location player_loc = event.getPlayer().getLocation();
		if (type == TomeType.MINECART) {
			Location l = SummonUtils.findForMinecart(player_loc, searching_radius_minecart_tome);
			if (l == null) return;
			SummonUtils.summonMinecart(l, event.getPlayer(), in_main_hand);
		}
		else if(type == TomeType.BOAT) {
			Location l = SummonUtils.findForBoat(player_loc, searching_radius_boat_tome);
			if(l == null) return;
			SummonUtils.summonBoat(l, event.getPlayer(), in_main_hand, TomeFormatter.get_boat_type(item));
		}
		else if(type == TomeType.HORSE) {
			Location l = UtilsWorld.find_horse_space(player_loc);
			if(l == null) return;
			l.setY(player_loc.getY());
			SummonUtils.summonHorse(l, event.getPlayer(), in_main_hand);
		}
		else if (type == TomeType.CUSTOM_HORSE) {
			Location l = UtilsWorld.find_horse_space(player_loc);
			if (l == null) return;
			l.setY(player_loc.getY());
			AbstractHorse custom_horse = SummonUtils.summonCustomHorse(l, event.getPlayer(), in_main_hand);
			//horse name from tome name
			if (item.getItemMeta().hasDisplayName() && !TomeItemHandler.name_eng_custom_horse_tome.contains(item.getItemMeta().getDisplayName())) {
				custom_horse.setCustomName(item.getItemMeta().getDisplayName());
			}
		}
		else if (type == TomeType.ALL || type == TomeType.CUSTOM_ALL)
		{
			Location l_mc = SummonUtils.findForMinecart(player_loc, searching_radius_minecart_tome);
			Location l_boat = SummonUtils.findForBoat(player_loc, searching_radius_boat_tome);
			TreeSpecies boat_wood = TomeFormatter.get_boat_type(item);
			if (l_mc != null && l_boat != null) {
				if (player_loc.distanceSquared(l_mc) < player_loc.distanceSquared(l_boat)) {
					SummonUtils.summonMinecart(l_mc, event.getPlayer(), in_main_hand);
				}
				else {
					SummonUtils.summonBoat(l_boat, event.getPlayer(), in_main_hand, boat_wood);
				}
			}
			else if (l_mc != null && l_boat == null) {
				SummonUtils.summonMinecart(l_mc, event.getPlayer(), in_main_hand);
			}
			else if (l_mc == null && l_boat != null) {
				SummonUtils.summonBoat(l_boat, event.getPlayer(), in_main_hand, boat_wood);
			}
			else {
				Location l = UtilsWorld.find_horse_space(player_loc);
				if (l == null)
					return;
					
				if (type == TomeType.ALL)
					SummonUtils.summonHorse(l, event.getPlayer(), in_main_hand);
				else
					SummonUtils.summonCustomHorse(l, event.getPlayer(), in_main_hand);
			}
		}
	}
	

	
	//Horse slots and move tome to other inventories
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;
		
		if (event.getView().getTopInventory().getHolder() instanceof AbstractHorse) {
			Player p = (Player) event.getWhoClicked();
			AbstractHorse horse = (AbstractHorse)event.getView().getTopInventory().getHolder();
			if (SummonUtils.wasSummoned(horse))
				addSavingTome(horse, p);
		}
		
		int slot = event.getRawSlot();
		if (slot < 0) return;
		
		boolean illegal = false;
		Inventory inv = event.getClickedInventory();
		InventoryAction action = event.getAction();
		
		if (inv instanceof AbstractHorseInventory) {
			AbstractHorseInventory hinv = (AbstractHorseInventory) inv;
			AbstractHorse horse = (AbstractHorse) hinv.getHolder();
			
			if (horse != null && isSummonable(horse) && SummonUtils.wasSummoned(horse))
				if (slot == 0)
				{
					if(action != InventoryAction.CLONE_STACK && action != InventoryAction.UNKNOWN)
						illegal = true;
				}
				else if (!SummonUtils.isCustomHorse((AbstractHorse) hinv.getHolder())) {
					if (slot < 2 || slot < 17 && hinv.getHolder() instanceof ChestedHorse && ((ChestedHorse)hinv.getHolder()).isCarryingChest())
						if (action != InventoryAction.CLONE_STACK && action != InventoryAction.UNKNOWN)
							illegal = true;
				}
				else {
					//change custom tome
					illegal = illegal_custom_horse(horse, (Player) horse.getOwner());
				}
		}
		else if (inv instanceof PlayerInventory && event.getView().getTopInventory() instanceof AbstractHorseInventory
				&& SummonUtils.wasSummoned((Entity) event.getView().getTopInventory().getHolder())) {
			Material m = event.getCurrentItem().getType();
			if ( (event.getView().getItem(1) == null || event.getView().getItem(1).getType() == Material.AIR)
					&& UtilsType.isHorseArmor(m)) {
					if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						if (SummonUtils.isCustomHorse((AbstractHorse)event.getView().getTopInventory().getHolder())) {
							AbstractHorse horse = (AbstractHorse)event.getView().getTopInventory().getHolder();
							illegal = illegal_custom_horse(horse, (Player) horse.getOwner());
						}
						else
							illegal = true;
					}
				}
		}
		
		if (illegal)
			event.setCancelled(true);
	}
	
	public static boolean isTome(ItemStack item) {
		if(item == null)
			return false;
		net.minecraft.server.v1_16_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null)
        	return false;
        if( compound.hasKey(TomeItemHandler.TOME_NBT_KEY) )
        	return true;
		return false;
	}
	
	private static int find_tome_slot(ItemStack[] inv, Entity entity) {
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] != null && inv[i].getType() == Material.ENCHANTED_BOOK) {
				if (isTome(inv[i]) && SummonUtils.getHasSummoned(inv[i]) == entity) {
					return i;
				}
			}
		}
		return -1;
	}
	private boolean illegal_custom_horse(AbstractHorse horse, Player p) {
		if (p.isOnline()) {
			int slot = find_tome_slot(p.getInventory().getContents(), horse);
			if (slot < 0) {
				horse.remove();
				return true;
			}
			else {
				addSavingTome(horse, p);
				return false;
			}
		}
		else
			return true;
	}
	private static void process_custom_horse(AbstractHorse horse, Player p) {
		if (p.isOnline()) {
			int slot = find_tome_slot(p.getInventory().getContents(), horse);
			if (slot < 0) {
				horse.remove();
			}
			else {
				ItemStack[] player_inv = p.getInventory().getContents();
				player_inv[slot] = TomeFormatter.set_horse_data(player_inv[slot], HorseFormat.fromHorse(horse));
				p.getInventory().setContents(player_inv);
			}
		}
	}
	
	private static boolean isSummonable(Entity e) {
		return e instanceof Boat || e instanceof Minecart || e instanceof AbstractHorse;
	}
}
