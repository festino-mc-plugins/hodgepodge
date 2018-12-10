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
				updated = process_grab_inv(inv);
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
	
	public boolean process_grab_inv(Inventory inv) {
		if(inv == null) return false;
		ItemStack[] stacks = inv.getContents();
		boolean updated = false;
		for(int i = 0; i < stacks.length; i++) {
			boolean updated_storage = false;
			if(stacks[i] == null) continue;
			int storage_id = Storage.getID(stacks[i]);
			if(storage_id >= 0) {
				Storage st = plugin.stlist.get(storage_id);
				if(!canGrabInventory(inv, st)) continue;
				Pair<Boolean, ItemStack[]> upd_stacks = st.unlim_inv.grabInventory(stacks);
				updated_storage = upd_stacks.first;
				if(updated_storage) {
					updated = true;
					stacks = upd_stacks.second;
					ItemMeta storage_meta = stacks[i].getItemMeta();
					storage_meta.setLore(st.unlim_inv.getLore());
					stacks[i].setItemMeta(storage_meta);
				}
			}
		}
		if(updated) {
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
        		plugin.stlist.get(id);
        	}
        }
    }

	//deny crafting firework from firework star and etc
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
					System.err.println("Storage(ID="+id+") could not load");
					//delete storage tag from itemstack
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
		if(event.isCancelled()) return;
		if(event.getClick() == ClickType.CREATIVE) return;
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
				System.err.println("Storage(ID="+id_current+") could not load");
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
			switch(event.getType()) {
			case EVEN:
			case SINGLE:
				if(!st.isAllowed(event.getOldCursor()))
						event.setCancelled(true);
			}
			
		}
	}

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

	/*@EventHandler
	public void onCreativeInventoryClick(InventoryCreativeEvent event) {
		onInventoryClick(event);
	}*/

	/*public void onInventoryDrag(InventoryDragEvent event) {
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st == null) return;
		if(st.getType() != StorageType.BOTTOMLESS) return;
		
		BottomlessInventory unlim_inv = st.unlim_inv;
		switch(event.getType()) {
		case EVEN:
		case SINGLE:
			Integer[] raw_slots = (Integer[]) event.getRawSlots().toArray();
			ItemStack[] items = (ItemStack[]) event.getNewItems().values().toArray();
			for(int i=0; i < raw_slots.length; i++) {
				if(items[i] != null && items[i].getType() == unlim_inv.getMaterial() && raw_slots[i] < event.getView().getTopInventory().getSize()) {
					unlim_inv.changeAmount(items[i].getAmount());
				}
			}
			break;
		}
	}*/

	/*
	//Storage logic
	@EventHandler(priority=EventPriority.LOWEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.isCancelled()) return;
		/*  =left to transfer item
	    -right to open inventory/open settings menu
	    =shift-left to transfer item
	    =drop to drop item
	    -control-drop to drop items from inventory* /
		ItemStack current_item = event.getCurrentItem(), cursor = event.getCursor();
		
		int id_current = Storage.getID( current_item );
		if( id_current >= 0 ) {
			Storage st = plugin.stlist.get(id_current);
			if(st == null) {
				System.err.println("Storage(ID="+id_current+") could not load");
				//delete storage tag from itemstack?
				return;
			}

			//CHANGE STORAGE TYPE / ADD ITEMS
			if(cursor != null && cursor.getType() != Material.AIR) {
				if(st.getType() == StorageType.BOTTOMLESS) {
					
					if(st.unlim_inv.getMaterial() != current_item.getType()) {
						if(st.isEmpty()) {
							plugin.stlist.storages.remove(st);
							if(current_item.getItemMeta().getEnchantLevel(Enchantment.ARROW_INFINITE) == 1) //BOTTOMLESS
								plugin.stlist.storages.add(new Storage(StoragesFileManager.nextID, plugin.mainworld.getFullTime(), current_item.getType()));
							event.setCancelled(true);
						}
					}
					else {
						event.setCancelled(true);
						st.unlim_inv.changeAmount(current_item.getAmount());
						event.setCursor(null);
						updating_invs.add(event.getClickedInventory());
					}
					
					current_item.getItemMeta().setLore(st.unlim_inv.getLore());
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
					//settings
					//autotake(deny, on move, always), item(can't change if isn't empty),
					event.getWhoClicked().openInventory(st.unlim_inv.getMenu());
				}
				st.grabbing_inventory = event.getClickedInventory();
				event.setCancelled(true);
				break;
			case DROP:
				st.grabbing_inventory = null;
				break;
			case CONTROL_DROP:
				event.setCancelled(true);
				st.drop(event.getWhoClicked().getEyeLocation());
				break;
			default:
				//MOVE STORAGE TO NEW INV
				if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
					if(event.getClickedInventory() != event.getView().getTopInventory()) {
						if(event.getView().getTopInventory().firstEmpty() >= 0)
							st.grabbing_inventory = event.getView().getTopInventory();
					}
					else if(event.getClickedInventory() != event.getView().getBottomInventory()) {
						if(event.getView().getBottomInventory().firstEmpty() >= 0)
							st.grabbing_inventory = event.getView().getTopInventory();
					}
					if(canGrabInventory(st)) {
						st.unlim_inv.grabInventory();
						current_item.getItemMeta().setLore(st.unlim_inv.getLore());
					}
				}
				//MOVE STORAGE TO NEW INV
				else if(event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_ALL
						|| event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
					st.grabbing_inventory = event.getClickedInventory();
					if(canGrabInventory(st)) {
						st.unlim_inv.grabInventory();
						current_item.getItemMeta().setLore(st.unlim_inv.getLore());
					}
				}
				else {
					boolean opened = false;
					if(st.getInventory().getViewers().contains(event.getWhoClicked()))
						opened = true;
					st.getInventory().getViewers().clear();
					if(opened)
						event.getWhoClicked().openInventory(st.getInventory());
				}
			}
		}

		//MOVE STORAGE TO NEW INV
		int id_cursor = Storage.getID( event.getCursor() );
		if( id_cursor >= 0 ) {
			Storage st = plugin.stlist.get(id_cursor);
			if(st == null) {
				System.err.println("Storage(ID="+id_cursor+") could not load");
				return;
			}
			if(st.getType() == StorageType.BOTTOMLESS)
				if(event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_SOME
				|| event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
					st.grabbing_inventory = event.getClickedInventory();
					if(canGrabInventory(st)) {
						st.unlim_inv.grabInventory();
						cursor.getItemMeta().setLore(st.unlim_inv.getLore());
					}
				}
		}

		event.setCurrentItem(current_item);
		event.getWhoClicked().setItemOnCursor(cursor);
		//updating_invs.add(event.getClickedInventory());
		/*updating_invs.add(event.getView().getBottomInventory());
		updating_invs.add(event.getView().getTopInventory());* /

		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st == null) return;
		
		if(id_cursor >= 0) {
			event.setCancelled(true);
			return; //block all storage interacts - TO DO: allow move inside bottom inv
		}
		
		if(st.getType() == StorageType.MULTITYPE) {
			System.out.println("top "+event.getView().getTopInventory()+" clicked: "+event.getClickedInventory());
			System.out.println("cursor "+event.getCursor()+" current: "+event.getCurrentItem());
			switch(event.getAction())
			{
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
				if(event.getView().getTopInventory() == event.getClickedInventory())
					if(!st.isAllowed(event.getCurrentItem())) {
						System.out.println("LMB CANCELLED");
						event.setCancelled(true);
						//updating_invs.add(event.getClickedInventory());
						return;
					}
				break;
			case MOVE_TO_OTHER_INVENTORY:
				if(event.getView().getTopInventory() != event.getClickedInventory())
					if(!st.isAllowed(event.getCurrentItem())) {
						System.out.println("MOVE CANCELLED");
						event.setCancelled(true);
						//updating_invs.add(event.getClickedInventory());
						return;
					}
				break;
			case COLLECT_TO_CURSOR:
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT:
			case PICKUP_ALL: 
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
			default:
				break;
			}
			//((Player)event.getWhoClicked()).updateInventory();
			st.setEdited(true);
		}
	}

	/*
	//Storage logic
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		/*  =left to transfer item
	    -right to open inventory
	    =shift-left to transfer item
	    -shift-right to open settings menu
	    =drop to drop item
	    -control-drop to drop items from inventory* /
		System.out.println("START: "+event.getCursor()+" "+event.getCurrentItem());
		
		int id = Storage.getID( event.getCurrentItem() );
		if( id >= 0 ) {
			Storage st = plugin.stlist.get(id);
			if(st == null) {
				System.err.println("Storage(ID="+id+") could not load");
				//delete storage tag from itemstack
				return;
			}
			
			//open
			if(event.getClick() == ClickType.RIGHT) {
				if(st.getType() == StorageType.MULTITYPE || st.getType() == StorageType.BOTTOMLESS && st.unlim_inv.isDefined())
					event.getWhoClicked().openInventory(st.getInventory());
				event.setCancelled(true);
			}
			//settings
			else if(event.getClick() == ClickType.SHIFT_RIGHT && st.getType() == StorageType.BOTTOMLESS) {
				//autotake(deny, on move, always), item(can't change if isn't empty),
				
				ItemStack material_button = st.unlim_inv.genMaterialButton();
				ItemStack grab_button = st.unlim_inv.genGrabButton();
				
				InventoryMenu new_menu = new InventoryMenu(st.unlim_inv, new ItemStack[] {
						grab_button, null, null, null, null, null, null, null, material_button},
						"Storage settings", 3);
				
				event.getWhoClicked().openInventory(new_menu.getGUI());
				
				event.setCancelled(true);
			}
			else if(event.getClick() == ClickType.CONTROL_DROP) {
				st.drop(event.getWhoClicked().getEyeLocation());
				event.setCancelled(true);
			}
		}


		
		//if(!(event.getView().getTopInventory() == event.getClickedInventory() || event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) return;

		/*change bottomless if changed bottomless, deny forbidden materials
		update if changed multitype* /
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if(st == null) return;
		
		if(id >= 0) return; //block all storage interacts - TO DO: allow move inside bottom inv
		
		if(st.getType() == StorageType.BOTTOMLESS) {
			BottomlessInventory unlim_inv = st.unlim_inv;
			int max_stack = st.unlim_inv.getMaterial().getMaxStackSize();
			
			/*if(!st.isAllowed(current_item)) {
				event.setCancelled(true);
				return;
			}* /
			
			ItemStack current_item = event.getCurrentItem(), cursor = event.getCursor();

			Inventory storage_inv = event.getView().getTopInventory();
			
			switch(event.getAction())
			{
			case MOVE_TO_OTHER_INVENTORY:
				//emit
				event.setCancelled(true);
				if(event.getClickedInventory() != storage_inv) { //if allowed, move all to storage
					if(st.isAllowed(event.getCursor())) {
						unlim_inv.changeAmount(cursor.getAmount());
						cursor.setAmount(0);
						st.edited = true;
					}
				}
				else { //find free space in player inventory, prior completing stacks
					int first_empty = -1;
					Inventory player_inv = event.getClickedInventory();
					int amount = cursor.getAmount();
					for(int i=0; i < player_inv.getSize(); i++) {
						if(player_inv.getItem(i) == null) {
							if(first_empty < 0)
								first_empty = i;
						}
						else if(player_inv.getItem(i).getType() == unlim_inv.getMaterial()) {
							int damount = Math.min(amount, max_stack-player_inv.getItem(i).getAmount());
							player_inv.getItem(i).setAmount(player_inv.getItem(i).getAmount()+damount);
							amount -= damount;
							if(amount == 0)
								break;
						}
					}
					if(amount > 0 && first_empty >= 0) {
						player_inv.setItem(first_empty, new ItemStack(unlim_inv.getMaterial(), amount));
						amount = 0;
					}
					cursor.setAmount(amount);
					st.edited = true;
				}
				break;
			case COLLECT_TO_CURSOR:
				//emit
				event.setCancelled(true);
				if(event.getClickedInventory() != storage_inv) { //complete stack from storage, start from the least stacks 
					if(st.isAllowed(cursor)) {
						ItemStack[] stacks = storage_inv.getContents();
						Arrays.sort(stacks, new Comparator<ItemStack>() {
						    @Override
							public int compare(ItemStack a, ItemStack b) {
						    	if(a == null)
						    		return -1;
						    	else if(b == null)
						    		return 1;
						        return a.getAmount() - b.getAmount(); 
						    } 
						});
						int amount = max_stack - cursor.getAmount();
						for(int i=0; i<stacks.length; i++) {
							System.out.println(stacks[i]);
							if(stacks[i] == null)
								break;
							int damount = Math.min(amount, max_stack-stacks[i].getAmount());
							cursor.setAmount(cursor.getAmount()+damount);
							unlim_inv.changeAmount(-damount);
							stacks[i].setAmount(stacks[i].getAmount()-damount);
							if(stacks[i].getAmount() == 0)
								stacks[i] = new ItemStack(unlim_inv.getMaterial(), Math.min(max_stack, unlim_inv.getAmount()-unlim_inv.getCurrentPageAmount(storage_inv)+damount));
							amount -= damount;
							if(amount == 0)
								break;
						}	
						st.edited = true;
					}
				}
				else { //get TO CURSOR from player inv
					ItemStack[] stacks = event.getWhoClicked().getInventory().getContents();
					int amount = max_stack - cursor.getAmount();
					for(int i=9; i >= 0; i+=(i<9 ? -1 : 1)) {
						if(i >= stacks.length) i = 8;
						if(stacks[i] == null)
							continue;
						int damount = Math.min(amount, max_stack-stacks[i].getAmount());
						cursor.setAmount(cursor.getAmount()+damount);
						stacks[i].setAmount(stacks[i].getAmount()-damount);
						amount -= damount;
						if(amount == 0)
							break;
					}	
				}
				break;
			case DROP_ONE_SLOT:
				//change
				if(event.getClickedInventory() == storage_inv) {
					unlim_inv.changeAmount(-1);
					st.edited = true;
				}
				break;
			case DROP_ALL_SLOT:
				//change
				if(event.getClickedInventory() == storage_inv) {
					unlim_inv.changeAmount(-current_item.getAmount());
					st.edited = true;
				}
				break;
			case PICKUP_HALF:
				if( event.getClickedInventory() == storage_inv) {
					event.setCancelled(true);
					int amount = current_item.getAmount();
					int damount = (amount+1)/2;
					cursor.setType(unlim_inv.getMaterial());
					System.out.println(cursor);
					cursor.setAmount(damount);
					System.out.println(cursor);
					unlim_inv.changeAmount(-damount);
					current_item.setAmount(amount-damount);
					System.out.println(damount+" "+(amount-damount)+" "+current_item.getAmount());
					if(current_item.getAmount() == 0)
						event.setCurrentItem(new ItemStack(unlim_inv.getMaterial(), Math.min(max_stack, unlim_inv.getAmount()-unlim_inv.getCurrentPageAmount(storage_inv)+damount)));
					st.edited = true;
				}
				break;
			case PICKUP_ONE:
				if( event.getClickedInventory() == storage_inv) {
					event.setCancelled(true);
					cursor.setType(unlim_inv.getMaterial());
					cursor.setAmount(1);
					unlim_inv.changeAmount(-1);
					current_item.setAmount(current_item.getAmount()-1);
					if(current_item.getAmount() == 0)
						event.setCurrentItem(new ItemStack(unlim_inv.getMaterial(), Math.min(max_stack, unlim_inv.getAmount()-unlim_inv.getCurrentPageAmount(storage_inv)+1)));
					st.edited = true;
				}
				break;
			case PICKUP_ALL:
			case PICKUP_SOME:
				if( event.getClickedInventory() == storage_inv) {
					event.setCancelled(true);
					int amount = current_item.getAmount();
					cursor.setType(unlim_inv.getMaterial());
					cursor.setAmount(amount);
					unlim_inv.changeAmount(-amount);
					event.setCurrentItem(new ItemStack(unlim_inv.getMaterial(), Math.min(max_stack, unlim_inv.getAmount()-unlim_inv.getCurrentPageAmount(storage_inv)+amount)));
					st.edited = true;
				}
				break;
			case PLACE_ONE:
				//check if top inv clicked and item is allowed
				//storage one
				if( event.getClickedInventory() == storage_inv) {
					event.setCancelled(true);
					if(st.isAllowed(cursor)) {
						int damount = Math.min(1, max_stack-current_item.getAmount());
						current_item.setAmount(cursor.getAmount()+damount);
						
						unlim_inv.changeAmount(1);
						cursor.setAmount(cursor.getAmount()-1);
						st.edited = true;
					}
				}
				break;
			case PLACE_ALL:
			case PLACE_SOME:
				//check if top inv clicked and item is allowed
				//storage all
			case SWAP_WITH_CURSOR:
				if(event.getClickedInventory() == storage_inv) {
					if(st.isAllowed(cursor)) {
						int damount = Math.min(cursor.getAmount(), max_stack-current_item.getAmount());
						current_item.setAmount(cursor.getAmount()+damount);
						
						unlim_inv.changeAmount(cursor.getAmount());
						cursor.setAmount(0);
						st.edited = true;
					}
					event.setCancelled(true);
				}

				//check if allowed, change if allowed
				/*int old_amount = st.unlim_inv.getPageAmount();
				int new_amount = st.unlim_inv.getCurrentPageAmount(event.getView(), 0, storage_inv.getSize());
				System.out.println(old_amount+" "+new_amount);
				if(old_amount != new_amount) {
					st.unlim_inv.changeAmount(new_amount-old_amount);
					st.edited = true;
					int empty_slot = storage_inv.firstEmpty(), free_amount = st.unlim_inv.getAmount() - new_amount;
					free_amount = (free_amount > st.unlim_inv.getMaterial().getMaxStackSize()) ? st.unlim_inv.getMaterial().getMaxStackSize() : free_amount;
					if(empty_slot >= 0 && free_amount > 0) {
						storage_inv.setItem(empty_slot, new ItemStack(st.unlim_inv.getMaterial(), free_amount));
					}
				}* /
				break;
			default:
				break;
			}
			event.setCurrentItem(current_item);
			event.setCursor(cursor);
			//event.getWhoClicked().setItemOnCursor(cursor);
		}
		else if(st.getType() == StorageType.MULTITYPE) {
			switch(event.getAction())
			{
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
			case MOVE_TO_OTHER_INVENTORY:
				if(event.getView().getTopInventory() != event.getClickedInventory())
					if(!st.isAllowed(event.getCurrentItem())) {
						event.setCancelled(true);
						return;
					}
			case COLLECT_TO_CURSOR:
			case DROP_ALL_SLOT:
			case DROP_ONE_SLOT:
			case PICKUP_ALL: 
			case PICKUP_HALF:
			case PICKUP_ONE:
			case PICKUP_SOME:
				st.edited = true;
			default:
				break;
			}
		}
		
		//change storage type if empty
		id = Storage.getID(event.getCursor());
		if(id >= 0) {
			st = plugin.stlist.get(id);
			if(st.isEmpty()) {
				if(st.unlim_inv.getMaterial() != event.getCurrentItem().getType()) {
					plugin.stlist.storages.remove(st);
					if(event.getCursor().getItemMeta().getEnchantLevel(Enchantment.ARROW_INFINITE) == 1) //BOTTOMLESS
						plugin.stlist.storages.add(new Storage(StoragesFileManager.nextID, plugin.mainworld.getFullTime(), event.getCurrentItem().getType()));
					else if(event.getCursor().getItemMeta().getEnchantLevel(Enchantment.DIG_SPEED) == 1) //MULTITYPE
						plugin.stlist.storages.add(new Storage(StoragesFileManager.nextID, plugin.mainworld.getFullTime()));
					event.setCancelled(true);
				}
			}
		}
		System.out.println("END: "+event.getCursor()+" "+event.getCurrentItem());
	}
	*/
}
