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
import com.festp.storages.BottomlessInventory.Grab;
import com.festp.storages.Storage.StorageType;

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
	
	public StorageHandler(mainListener plugin) {
		this.plugin = plugin;
	}
	
	public void onTick() {
		BeamedPair.tickAll();
		
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
		
		for(int i=updating_invs.size()-1; i >=0; i--) {
			if(updating_invs.get(i) != null) {
				update_item_counts(updating_invs.get(i));
			}
			updating_invs.remove(i);
		}
		for(int i=grabbing_invs.size()-1; i >=0; i--) {
			if(grabbing_invs.get(i) != null) {
				boolean updated = process_grab_inv(grabbing_invs.get(i));
				if(updated) {
					update_item_counts(grabbing_invs.get(i));
				}
			}
			grabbing_invs.remove(i);
		}
		
		for(Player p : plugin.getServer().getOnlinePlayers()) {
			InventoryView opened_inv = p.getOpenInventory();
			if(opened_inv == null) continue;
			boolean updated = false;
			for(Inventory inv : new Inventory[] {opened_inv.getBottomInventory(), opened_inv.getTopInventory()}) {
				updated |= process_grab_inv(inv);
			}
			if(updated) {
				p.updateInventory();
			}
		}
		
		storage_unloadcheck_ticks+=1;
		if(storage_unloadcheck_ticks >= storage_unloadcheck_maxticks) {
			plugin.stlist.tryUnload(plugin.mainworld.getFullTime());
			storage_unloadcheck_ticks = 0;
		}
		
		storage_save_ticks+=1;
		if(storage_save_ticks >= storage_save_maxticks) {
			plugin.stlist.saveStorages();
			storage_save_ticks = 0;
		}
	}
	
	public void update_item_counts(Inventory inv) {
		ItemStack[] stacks = inv.getContents();
		for(int j=0; j < stacks.length; j++) {
			Storage storage = Storage.getByItemStack(stacks[j]);
			if(storage != null && storage.getType() == StorageType.BOTTOMLESS) {
				ItemMeta items_count = stacks[j].getItemMeta();
				items_count.setLore(storage.unlim_inv.getLore());
				stacks[j].setItemMeta(items_count);
				inv.setItem(j, stacks[j]);
			}
		}
		for(HumanEntity human : inv.getViewers()) {
			((Player)human).updateInventory();
		}
	}
	
	public boolean grabbing_inv(Inventory inv) {
		return inv != null && !(inv.getType() == InventoryType.ANVIL || inv.getType() == InventoryType.BEACON || inv.getType() == InventoryType.BREWING
				 || inv.getType() == InventoryType.CRAFTING || inv.getType() == InventoryType.WORKBENCH
				 || inv.getType() == InventoryType.ENCHANTING || inv.getType() == InventoryType.FURNACE || inv.getType() == InventoryType.MERCHANT);
	}
	
	public boolean process_grab_inv(Inventory inv) {
		if (inv == null || !grabbing_inv(inv)) return false;
		ItemStack[] stacks = inv.getContents();
		boolean updated = false;
		for (int i = 0; i < stacks.length; i++) {
			boolean updated_storage = false;
			if (stacks[i] == null) continue;
			int storage_id = Storage.getID(stacks[i]);
			if (storage_id >= 0) {
				Storage st = plugin.stlist.get(storage_id);
				if (!canGrabInventory(inv, st)) continue;
				Pair<Boolean, ItemStack[]> upd_stacks = st.unlim_inv.grabInventory(stacks);
				updated_storage = upd_stacks.first;
				if (updated_storage) {
					updated = true;
					stacks = upd_stacks.second;
					ItemMeta storage_meta = stacks[i].getItemMeta();
					storage_meta.setLore(st.unlim_inv.getLore());
					stacks[i].setItemMeta(storage_meta);
				}
			}
		}
		if (updated) {
			inv.setContents(stacks);
		}
		return updated;
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
	public void onPrepareItemCraft(PrepareItemCraftEvent event) {
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
				if(st.getType() == StorageType.MULTITYPE) // || st.getType() == StorageType.BOTTOMLESS && st.unlim_inv.isDefined()
					event.getPlayer().openInventory(st.getInventory());
				else if(st.getType() == StorageType.BOTTOMLESS)
					event.getPlayer().openInventory(st.unlim_inv.getMenu());
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		if(event.isCancelled()) return;
		
		Inventory inv = event.getInventory();
		Material m = event.getItem().getItemStack().getType();
		
		Storage st = findGrabbingStorage(inv, m);
		if(st != null) {
			event.setCancelled(true);
			st.unlim_inv.changeAmount(event.getItem().getItemStack().getAmount());
			event.getItem().remove();
		}
		
		int id = Storage.getID(event.getItem().getItemStack());
		st = plugin.stlist.get(id);
		if(st != null) {
			process_grab_inv(inv);
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
	
	private Storage findGrabbingStorage(Inventory inv, Material m) {
		Grab grab = Grab.NO_PLAYER;
		if(inv instanceof PlayerInventory)
			grab = Grab.ALL;
		for(ItemStack is : inv.getContents()) {
			int id = Storage.getID(is);
			Storage st = plugin.stlist.get(id);
			if(st != null && st.getType() == StorageType.BOTTOMLESS && st.unlim_inv.getMaterial() == m
					&& (st.unlim_inv.canGrab() == grab || grab == Grab.NO_PLAYER && st.unlim_inv.canGrab() == Grab.ALL)) {
				return st;
			}
		}
		return null;
	}
	
	private boolean canGrabInventory(Inventory inv, Storage st) {
		Grab min_grab = Grab.NO_PLAYER;
		if(inv instanceof PlayerInventory)
			min_grab = Grab.ALL;
		if(st != null && st.getType() == StorageType.BOTTOMLESS && (st.unlim_inv.canGrab() == min_grab || min_grab == Grab.NO_PLAYER && st.unlim_inv.canGrab() == Grab.ALL)) {
			return true;
		}
		return false;
	}
	
	private boolean canGrabInventory(Storage st) {
		if(st == null || st.getType() != StorageType.BOTTOMLESS) return false;
		Inventory inv = st.grabbing_inventory;
		Grab min_grab = Grab.NO_PLAYER;
		if(inv instanceof PlayerInventory)
			min_grab = Grab.ALL;
		if(st.unlim_inv.canGrab() == min_grab || min_grab == Grab.NO_PLAYER && st.unlim_inv.canGrab() == Grab.ALL) {
			return true;
		}
		return false;
	}

	//Storage logic
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;
		if (event.getClick() == ClickType.CREATIVE) return; //TO DO: fix creative inv clicks
		if ((event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) && !grabbing_inv(event.getClickedInventory())) return;
		/*  =left to transfer item
	    -right to open inventory/open settings menu
	    =shift-left to transfer item
	    =drop to drop item
	    -control-drop to drop items from inventory*/
		ItemStack current_item = event.getCurrentItem(), cursor = event.getCursor();
		
		int id_current = Storage.getID( current_item );
		if( id_current >= 0 ) {
			Storage st = plugin.stlist.get(id_current);
			if(st == null) {
				plugin.getLogger().severe("Storage(ID="+id_current+") could not load (on inv click: "+event.getClick()+")");
				//delete storage tag from itemstack?
				return;
			}

			//CHANGE STORAGE TYPE / ADD ITEMS
			if(cursor != null && cursor.getType() != Material.AIR) {
				if(st.getType() == StorageType.BOTTOMLESS) {
					if(st.unlim_inv.getMaterial() != cursor.getType()) {
						if(st.isEmpty() && BottomlessInventory.isAllowedMaterial(cursor.getType())) {
							st.unlim_inv.setMaterial(cursor.getType());
							event.setCancelled(true);
						}
					}
					else {
						event.setCancelled(true);
						st.unlim_inv.changeAmount(cursor.getAmount());
						event.setCursor(null);
						updating_invs.add(event.getClickedInventory());
					}
					
					//current_item.getItemMeta().setLore(st.unlim_inv.getLore());
				}
			}
			
			//OPEN INV/SETTINGS, DROP INV
			else
			switch(event.getClick())
			{
			case RIGHT:
				if(st.getType() == StorageType.MULTITYPE) {
					event.getWhoClicked().openInventory(st.getInventory());
				}
				else if(st.getType() == StorageType.BOTTOMLESS) {
					event.getWhoClicked().openInventory(st.unlim_inv.getMenu());
				}
				event.setCancelled(true);
				break;
			case CONTROL_DROP:
				event.setCancelled(true);
				st.drop(event.getWhoClicked().getEyeLocation());
				updating_invs.add(event.getClickedInventory());
				break;
			default:
				boolean opened = false;
				if(st.getInventory().getViewers().contains(event.getWhoClicked()))
					opened = true;
				st.getInventory().getViewers().clear();
				if(opened)
					event.getWhoClicked().openInventory(st.getInventory());
			}
		}


		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st == null) return;

		int id_cursor = Storage.getID( event.getCursor() );
		/*if(id_cursor >= 0) {
			event.setCancelled(true);
			return; //block all storage interacts - TO DO: allow move inside bottom inv
		}*/
		
		if(st.getType() == StorageType.MULTITYPE) {
			switch(event.getAction())
			{
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
				if(event.getView().getTopInventory() == event.getClickedInventory())
					if(id_cursor >= 0 && !st.isAllowed(event.getCursor())) {
						event.setCancelled(true);
						updating_invs.add(event.getClickedInventory());
						return;
					}
				break;
			case MOVE_TO_OTHER_INVENTORY:
				if(event.getView().getTopInventory() != event.getClickedInventory())
					if(id_current >= 0 && !st.isAllowed(event.getCurrentItem())) {
						event.setCancelled(true);
						updating_invs.add(event.getClickedInventory());
						return;
					}
				break;
			default:
				break;
			}
			st.setEdited(true);
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		//Multitype only allowed
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st != null && st.getType() == StorageType.MULTITYPE) {
			switch(event.getType())
			{
			case EVEN:
			case SINGLE:
				if(!st.isAllowed(event.getOldCursor()))
						event.setCancelled(true);
			}
			
		}
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
}
