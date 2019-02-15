package com.festp.storages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Config;
import com.festp.Pair;
import com.festp.Utils;
import com.festp.mainListener;
import com.festp.menu.InventoryMenu;
import com.festp.storages.Storage.Grab;
import com.festp.storages.Storage.StorageType;
import com.festp.storages.StorageMultitype.HandleTime;

public class StorageHandler implements Listener {
	//Перенос пусть будет на ЛКМ и перенос + на ЛКМ+Shift. Дроп хранилища и так на Q, дроп его содержимого на Ctrl+Q...
	//На ПКМ открытие, а на Shift+ПКМ открытие настроек.
	
	mainListener plugin;
	private int storage_save_ticks = 0;
	private int storage_save_maxticks = 3*60*20; //3 minutes
	private int storage_unloadcheck_ticks = 0;
	private int storage_unloadcheck_maxticks = 60*20;
	private List<Inventory> updating_invs = new ArrayList<>();
	private List<Inventory> grabbing_invs = new ArrayList<>();
	
	public StorageHandler(mainListener plugin)
	{
		this.plugin = plugin;
	}
	
	public void onTick()
	{
		//remove unnecessary rays
		BeamedPair.tickAll();
		
		//new beams
		for(World world : plugin.getServer().getWorlds()) {
			for(Item item : world.getEntitiesByClass(Item.class)) {
				if(Storage.isStorage(item.getItemStack())) {
					Utils.setPrivateField("age", Utils.getNMSClass("EntityItem"), ((CraftItem)item).getHandle(), 0);
					
					if(BeamedPair.existsBeamer(item))
						continue;
					
					item.setInvulnerable(true);
					Player nearest_player = null;
					double dist_squared = Config.storage_signal_radius*Config.storage_signal_radius;
					for(Entity entity : item.getNearbyEntities(Config.storage_signal_radius, Config.storage_signal_radius, Config.storage_signal_radius)) {
						if(entity instanceof Player) {
							double cur_dist_squared = item.getLocation().distanceSquared(entity.getLocation());
							if(BeamedPair.canBeBeamed(item, (Player)entity) && dist_squared >= cur_dist_squared) {
								nearest_player = (Player)entity;
								dist_squared = cur_dist_squared;
							}
						}
					}
					if(nearest_player != null) {
						BeamedPair.add(item, nearest_player);
					}
				}
			}
		}
		
		//antiglitch
		for(int i=updating_invs.size()-1; i >=0; i--) {
			if(updating_invs.get(i) != null) {
				StorageBottomless.update_item_counts(updating_invs.get(i));
			}
			updating_invs.remove(i);
		}
		for(int i=grabbing_invs.size()-1; i >=0; i--) {
			if(grabbing_invs.get(i) != null) {
				boolean updated = process_grab_inv(grabbing_invs.get(i));
				if(updated) {
					StorageBottomless.update_item_counts(grabbing_invs.get(i));
				}
			}
			grabbing_invs.remove(i);
		}
		
		//opened inventories grabbing
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			InventoryView opened_inv = p.getOpenInventory();
			if (opened_inv == null) continue;
			boolean updated = false;
			for (Inventory inv : new Inventory[] {opened_inv.getBottomInventory(), opened_inv.getTopInventory()}) {
				updated |= process_grab_inv(inv);
			}
			if (updated) {
				p.updateInventory();
			}
		}
		
		storage_unloadcheck_ticks += 1;
		if (storage_unloadcheck_ticks >= storage_unloadcheck_maxticks) {
			plugin.stlist.tryUnload(plugin.mainworld.getFullTime());
			storage_unloadcheck_ticks = 0;
		}
		
		storage_save_ticks+=1;
		if(storage_save_ticks >= storage_save_maxticks) {
			plugin.stlist.saveStorages();
			storage_save_ticks = 0;
		}
	}

	//main loading
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
        for(ItemStack is : e.getInventory().getContents()) {
        	int id = Storage.getID(is);
        	if(id >= 0) {
        		plugin.stlist.tryLoad(id);
        	}
        }
    }

	//deny crafting fireworks from firework star and etc
	//TO DO: add a tag for items that can't be used in crafting (but what is about recurrent upgrades?)
	@EventHandler
	public void onPrepareItemCraft(PrepareItemCraftEvent event)
	{
		if (event.getInventory().getResult() != null && !Utils.is_shulker_box(event.getInventory().getResult().getType()))
		for(ItemStack is : event.getInventory().getMatrix()) {
			if(Storage.isStorage(is)) {
				event.getInventory().setResult(null);
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(!event.hasBlock() || !Utils.isInteractable(event.getClickedBlock().getType())) {
			int id = Storage.getID(event.getItem());
			if(id >= 0) {
				event.setCancelled(true);
				Storage st = plugin.stlist.get(id);
				if(st == null) {
					plugin.getLogger().severe("Storage(ID="+id+") could not load (on interact)");
					//delete storage tag from itemstack or create new storage - configurable?
					return;
				}
				Player p = event.getPlayer();
				if(st instanceof StorageMultitype) {
					if (p.isSneaking())
						p.openInventory(((StorageMultitype)st).getMenu());
					else
						p.openInventory(st.getInventory());
				}
				else if(st instanceof StorageBottomless)
					p.openInventory(((StorageBottomless) st).getMenu());
			}
		}
	}

	//Storage logic
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;
		if (event.getClick() == ClickType.CREATIVE) return; //TO DO: fix creative inv clicks
		if ((event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) && !Storage.isGrabbableInventory(event.getClickedInventory())) return;
		/*  =left to transfer item
	    -right to open inventory/open settings menu
	    =shift-left to transfer item
	    =drop to drop item
	    -control-drop to drop items from inventory*/
		ItemStack current_item = event.getCurrentItem(), cursor = event.getCursor();
		
		int id_current = Storage.getID( current_item );
		//click on Storage
		if( id_current >= 0 ) {
			Storage st = plugin.stlist.get(id_current);
			if(st == null) {
				plugin.getLogger().severe("Storage(ID="+id_current+") could not load (on inv click: "+event.getClick()+")");
				//delete storage tag from itemstack?
				return;
			}

			//click with non-empty cursor
			//CHANGE STORAGE TYPE / ADD ITEMS - only Bottomless
			if(cursor != null && cursor.getType() != Material.AIR)
			{
				if(st instanceof StorageBottomless) {
					StorageBottomless stb = (StorageBottomless)st;
					if(stb.getMaterial() != cursor.getType()) {
						if(stb.isEmpty() && StorageBottomless.isAllowedMaterial(cursor.getType())) {
							stb.setMaterial(cursor.getType());
							event.setCancelled(true);
						}
					}
					else {
						event.setCancelled(true);
						stb.changeAmount(cursor.getAmount());
						event.setCursor(null);
						updating_invs.add(event.getClickedInventory());
					}
				}
			}
			
			//click with empty cursor
			//OPEN INV/SETTINGS, DROP INV
			else
			{
				switch(event.getClick())
				{
				case RIGHT:
					if(st instanceof StorageMultitype) {
						event.getWhoClicked().openInventory(st.getInventory());
					}
					else if(st instanceof StorageBottomless) {
						event.getWhoClicked().openInventory(((StorageBottomless) st).getMenu());
					}
					event.setCancelled(true);
					break;
				case SHIFT_RIGHT:
					if(st instanceof StorageMultitype) {
						event.getWhoClicked().openInventory(((StorageMultitype)st).getMenu());
					}
					break;
				case CONTROL_DROP:
					st.drop(event.getWhoClicked().getEyeLocation());
					updating_invs.add(event.getClickedInventory());
					event.setCancelled(true);
					break;
				default:
					for (HumanEntity he : st.getInventory().getViewers())
						if (he != event.getWhoClicked())
							he.closeInventory();
				}
			}
		}


		//click with opened Storage Inventory
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st == null) return;
		
		if(st instanceof StorageMultitype) {
			StorageMultitype sm = (StorageMultitype)st;
			switch(event.getAction())
			{
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
				if (event.getView().getTopInventory() == event.getClickedInventory())
					if (!st.isAllowed(event.getCursor())) {
						event.setCancelled(true);
						updating_invs.add(event.getClickedInventory());
						return;
					}
					else
						sm.onAction(com.festp.storages.StorageMultitype.InventoryAction.GAIN);
				break;
			case MOVE_TO_OTHER_INVENTORY:
				if (event.getView().getTopInventory() == event.getClickedInventory())
					sm.onAction(com.festp.storages.StorageMultitype.InventoryAction.LOSE);
				else
					if (!st.isAllowed(event.getCurrentItem())) {
						event.setCancelled(true);
						updating_invs.add(event.getClickedInventory());
						return;
					}
					else
						sm.onAction(com.festp.storages.StorageMultitype.InventoryAction.GAIN_MERGING);
				break;
			case HOTBAR_MOVE_AND_READD:
			case HOTBAR_SWAP:
				if (event.getView().getTopInventory() == event.getClickedInventory())
					if (event.getClick() == ClickType.NUMBER_KEY) {
						ItemStack hotbar_slot = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
						if (!st.isAllowed(hotbar_slot)) {
							event.setCancelled(true);
							updating_invs.add(event.getClickedInventory());
							return;
						}
					}
					else
						sm .onAction(com.festp.storages.StorageMultitype.InventoryAction.GAIN);
				break;
			case DROP_ALL_CURSOR:
			case DROP_ALL_SLOT:
			case DROP_ONE_CURSOR:
			case DROP_ONE_SLOT:
			case PICKUP_ALL:
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
				if(event.getView().getTopInventory() == event.getClickedInventory())
					sm.onAction(com.festp.storages.StorageMultitype.InventoryAction.LOSE);
				break;
			case COLLECT_TO_CURSOR:
				sm.onAction(com.festp.storages.StorageMultitype.InventoryAction.LOSE);
				break;
			default:
				break;
			}
			st.setEdited(true);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		Inventory inventory = event.getInventory();
		Storage st = plugin.stlist.findByInventory(inventory);
		if (st instanceof StorageMultitype)
			if (inventory.getViewers().size() == 1) { // because when closing this player is still considered a viewer
				StorageMultitype sm = (StorageMultitype)st;
				if (sm.getSortTime() == HandleTime.OPEN_CLOSE)
					sm.sort();
				if (sm.getStackTime() == HandleTime.OPEN_CLOSE)
					sm.mergeStacks();
			}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		//Multitype only allowed
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st != null && st instanceof StorageMultitype) {
			switch(event.getType())
			{
			case EVEN:
			case SINGLE:
				if (event.getInventory() == event.getView().getTopInventory())
					if(!st.isAllowed(event.getOldCursor()))
						event.setCancelled(true);
					else
						((StorageMultitype) st).onAction(com.festp.storages.StorageMultitype.InventoryAction.GAIN);
			}
			
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		if(event.isCancelled()) return;
		
		Inventory inv = event.getInventory();
		ItemStack item = event.getItem().getItemStack();

		int id = Storage.getID(event.getItem().getItemStack());
		Storage st = plugin.stlist.get(id);
		if(st != null) // Storage had been picked up
		{
			process_grab_inv(inv);
		}
		else
		{
			int amount = item.getAmount();
			List<Storage> storages = findGrabbingStorages(inv, item);
			for (Storage s : storages)
			{
				if (st instanceof StorageMultitype) {
					StorageMultitype sm = (StorageMultitype)st;
					amount -= sm.grabItemStack(item);
					if (amount == 0) {
						break;
					}
				}
				else if (s instanceof StorageBottomless) {
					event.setCancelled(true);
					((StorageBottomless) s).changeAmount(amount);
					amount = 0;
				}
			}
			
			if (amount == 0)
				event.getItem().remove();
			else
				event.getItem().getItemStack().setAmount(amount);
		}
		
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if(event.isCancelled()) return;

		grabbing_invs.add(event.getDestination());
		/*Inventory inv = event.getDestination();
		Material m = event.getItem().getType();
		
		Storage st = findGrabbingStorage(inv, m);
		if(st != null) {
			st.unlim_inv.changeAmount(event.getItem().getAmount());
			event.getItem().setAmount(0);
			return;
		}
		
		int id = Storage.getID(event.getItem());
		st = plugin.stlist.get(id);
		if(st != null) {
			process_grab_inv(inv);
		}*/
	}

	//immortality of storage
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntityType() != EntityType.DROPPED_ITEM) return;
		
		Item drop = (Item)event.getEntity();
		if(Storage.isStorage(drop.getItemStack())) {
			event.setCancelled(true);
			/*for(Player p : plugin.getServer().getOnlinePlayers()) {
				p.set
			}*/
			
			/*Item new_drop = drop.getWorld().dropItem(drop.getLocation(), drop.getItemStack());
			new_drop.setVelocity(drop.getVelocity());
			new_drop.setPickupDelay(drop.getPickupDelay());
			((CraftItem)new_drop).set
			drop.remove();*/
		}
	}
	
	public boolean process_grab_inv(Inventory inv) {
		if (inv == null || !Storage.isGrabbableInventory(inv)) return false;
		ItemStack[] stacks = inv.getContents();
		boolean updated = false;
		for (int i = 0; i < stacks.length; i++) {
			boolean updated_storage = false;
			if (stacks[i] == null) continue;
			int storage_id = Storage.getID(stacks[i]);
			if (storage_id >= 0) {
				Storage st = plugin.stlist.get(storage_id);
				if (!st.canGrab(inv)) continue;
				if (st instanceof StorageBottomless)
				{
					StorageBottomless stb = (StorageBottomless)st;
					Pair<Boolean, ItemStack[]> upd_stacks = stb.grabInventory(stacks);
					updated_storage = upd_stacks.first;
					if (updated_storage) {
						updated = true;
						stacks = upd_stacks.second;
						stacks[i] = stb.getLored(stacks[i]);
					}
				}
				if (st instanceof StorageMultitype)
				{
					StorageMultitype stm = (StorageMultitype)st;
					Pair<Boolean, ItemStack[]> upd_stacks = stm.grabInventory(stacks);
					updated_storage = upd_stacks.first;
					if (updated_storage) {
						updated = true;
						stacks = upd_stacks.second;
					}
				}
			}
		}
		if (updated) {
			inv.setContents(stacks);
		}
		return updated;
	}
	
	private List<Storage> findGrabbingStorages(Inventory inv, ItemStack stack)
	{
		if (!Storage.isGrabbableInventory(inv))
			return null;
		List<Storage> list = new ArrayList<>();
		StorageBottomless suit = null;
		for(ItemStack is : inv.getContents()) {
			int id = Storage.getID(is);
			Storage st = plugin.stlist.get(id);
			if(st != null && st.canGrab(inv))
				if (st instanceof StorageBottomless && suit == null && ((StorageBottomless) st).canGrab(stack.getType()))
					suit = (StorageBottomless) st;
				else if (st instanceof StorageMultitype && ((StorageMultitype) st).canGrab(stack))
					list.add(st);
		}
		
		if (suit != null)
			list.add(suit);
		if (list.size() == 0)
			return null;
		return list;
	}
}
