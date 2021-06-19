package com.festp.storages;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.ShulkerBox;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.Config;
import com.festp.Pair;
import com.festp.Main;
import com.festp.storages.Storage.Grab;
import com.festp.storages.StorageMultitype.HandleTime;
import com.festp.storages.StorageMultitype.UncraftMode;
import com.festp.utils.BeamedPair;
import com.festp.utils.ClickResult;
import com.festp.utils.TimeUtils;
import com.festp.utils.Utils;
import com.festp.utils.UtilsType;

public class StorageHandler implements Listener {
	//Перенос пусть будет на ЛКМ и перенос + на ЛКМ+Shift. Дроп хранилища и так на Q, дроп его содержимого на Ctrl+Q...
	//На ПКМ открытие, а на Shift+ПКМ открытие настроек.
	
	Main plugin;
	private int storage_save_ticks = 0;
	private int storage_save_maxticks = 3*60*20; //3 minutes
	private int storage_unloadcheck_ticks = 0;
	private int storage_unloadcheck_maxticks = 60*20; // 1 munute
	private List<Inventory> updating_invs = new ArrayList<>();
	private List<Inventory> grabbing_invs = new ArrayList<>();
	public static final String LABEL_YOUR_ITEMS = "YOUR ITEMS";
	private static Sound PICKUP_SOUND = Sound.ENTITY_CHICKEN_EGG;
	
	public StorageHandler(Main plugin)
	{
		this.plugin = plugin;
	}
	
	public void onTick()
	{
		//remove unnecessary rays
		BeamedPair.tickAll();
		
		//new beams
		for (World world : plugin.getServer().getWorlds()) {
			for (Item item : world.getEntitiesByClass(Item.class)) {
				// System.out.print(Utils.toString(item.getLocation()));
				if (Storage.isStorage(item.getItemStack())) {
					//Utils.setPrivateField("age", Utils.getNMSClass("EntityItem"), ((CraftItem)item).getHandle(), -32768); // TODO NMS reflection Utils / deobfuscation map using
					item.setTicksLived(1);
					
					if(BeamedPair.existsBeamer(item))
						continue;
					
					item.setInvulnerable(true);
					Player nearest_player = null;
					double dist_squared = Config.storage_signal_radius*Config.storage_signal_radius;
					for (Entity entity : item.getNearbyEntities(Config.storage_signal_radius, Config.storage_signal_radius, Config.storage_signal_radius)) {
						if (entity instanceof Player) {
							double cur_dist_squared = item.getLocation().distanceSquared(entity.getLocation());
							if (BeamedPair.canBeBeamed(item, (Player)entity) && dist_squared >= cur_dist_squared) {
								nearest_player = (Player)entity;
								dist_squared = cur_dist_squared;
							}
						}
					}
					if (nearest_player != null) {
						BeamedPair.add(item, nearest_player);
					}
				}
			}
		}
		
		//antiglitch
		for (int i = updating_invs.size()-1; i >= 0; i--) {
			if (updating_invs.get(i) != null) {
				StorageBottomless.update_item_counts(updating_invs.get(i));
				for (HumanEntity human : updating_invs.get(i).getViewers())
					((Player)human).updateInventory();
			}
			updating_invs.remove(i);
		}
		for (int i = grabbing_invs.size()-1; i >= 0; i--) {
			if (grabbing_invs.get(i) != null) {
				boolean updated = process_grab_inv(grabbing_invs.get(i));
				if (updated) {
					StorageBottomless.update_item_counts(grabbing_invs.get(i));
				}
			}
			grabbing_invs.remove(i);
		}

		storage_unloadcheck_ticks += 1;
		if (storage_unloadcheck_ticks >= storage_unloadcheck_maxticks) {
			plugin.stlist.tryUnload(TimeUtils.getTicks());
			storage_unloadcheck_ticks = 0;
		}
		
		storage_save_ticks += 1;
		if(storage_save_ticks >= storage_save_maxticks) {
			plugin.stlist.saveStorages();
			storage_save_ticks = 0;
		}
	}
	
	public void delayedUpdate(Inventory inv)
	{
		updating_invs.add(inv);
	}
	
	public void delayedGrab(Inventory inv)
	{
		grabbing_invs.add(inv);
	}

	//main loading
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		linkStoragesToInventory(event.getInventory());
    }
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		linkStoragesToInventory(event.getPlayer().getInventory());
	}
	
	public void linkStoragesToInventory(Inventory inv) {
        for (ItemStack is : inv.getContents()) {
        	int id = Storage.getID(is);
        	if (id >= 0) {
        		Storage st = plugin.stlist.get(id);
        		if (st == null)
					plugin.getLogger().severe("Storage(ID="+id+") could not load (on open "+inv+" / "+Utils.toString(inv.getLocation())+")");
        		else
        			st.setExternalInventory(inv);
        	}
        }
	}

	//deny crafting fireworks from firework star and etc
	//TO DO: add a tag for items that can't be used in crafting (but what is about recurrent upgrades?)
	@EventHandler
	public void onPrepareItemCraft(PrepareItemCraftEvent event)
	{
		ItemStack result = event.getInventory().getResult();
		if (result != null)
		{
			Storage storage = null;
			
			for(ItemStack is : event.getInventory().getMatrix())
				if(Storage.isStorage(is)) {
					storage = Storage.getByItemStack(is);
					break;
				}
			
			if (storage instanceof StorageMultitype) {
				if (UtilsType.is_shulker_box(result.getType())) {
					StorageMultitype sm = (StorageMultitype)storage;
					int lvl = sm.getLvl();
					if (sm.getUncraftMode() == UncraftMode.DENY && lvl - 1 > Utils.countEmpty(event.getViewers().get(0).getInventory().getStorageContents())) {
						event.getInventory().setResult(null);
						return;
					}
			    	ItemStack item = new ItemStack(Material.SHULKER_BOX);
			    	BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
			    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
			    	ItemStack renamed = new ItemStack(Material.SHULKER_BOX);
			    	ItemMeta meta = renamed.getItemMeta();
			    	meta.setDisplayName(ChatColor.BOLD + LABEL_YOUR_ITEMS + ChatColor.RESET);
			    	renamed.setItemMeta(meta);
			    	ItemStack[] contents = new ItemStack[27];
			    	contents[0] = renamed;
			    	shulker.getInventory().setContents(contents);
			    	im.setBlockState(shulker);
			    	item.setItemMeta(im);
					event.getInventory().setResult(item);
				}
				else {
					event.getInventory().setResult(null);
				}
			}
			else if (storage instanceof StorageBottomless) {
				StorageBottomless sb = (StorageBottomless)storage;
				if (sb.isEmpty() && UtilsType.is_shulker_box(result.getType())) {
			    	ItemStack item = new ItemStack(Material.SHULKER_BOX);
			    	BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
			    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
			    	ItemStack renamed = new ItemStack(Material.SHULKER_BOX);
			    	ItemMeta meta = renamed.getItemMeta();
			    	meta.setDisplayName(ChatColor.BOLD + LABEL_YOUR_ITEMS + ChatColor.RESET);
			    	renamed.setItemMeta(meta);
			    	ItemStack[] contents = new ItemStack[27];
			    	contents[0] = renamed;
			    	shulker.getInventory().setContents(contents);
			    	im.setBlockState(shulker);
			    	item.setItemMeta(im);
					event.getInventory().setResult(item);
				}
				else {
					event.getInventory().setResult(null);
				}
			}
		}
	}
	//return shulker boxes
	@EventHandler
	public void onCraftItem(CraftItemEvent event)
	{
		ItemStack result = event.getInventory().getResult();
		if (result == null) return;
		Storage st = null;
		
		for(ItemStack is : event.getInventory().getMatrix())
			if(Storage.isStorage(is)) {
				st = Storage.getByItemStack(is);
				break;
			}
		
		if (st instanceof StorageMultitype)
		{
			StorageMultitype storage = (StorageMultitype)st;
			Inventory st_inv = storage.getInventory();
			Inventory player_inv = event.getWhoClicked().getInventory();
			
			//stop unreal crafts (something moved to cursor(swap))
			ClickResult click_result = ClickResult.getClickResult(event);
			if (click_result.items_2_to_1 > 0 || click_result.items_1_to_2 <= 0)
				return;
			
			if (UtilsType.is_shulker_box(result.getType())) {
				int lvl = storage.getLvl();
				int extra_slots = click_result.fillsBottom() ? 1 : 0;
				if (storage.getUncraftMode() == UncraftMode.DENY) {
					int empties = Utils.countEmpty(event.getViewers().get(0).getInventory().getStorageContents());
					if (lvl - 1 + extra_slots > empties)
					{
						if (lvl - 1 > empties)
							event.getInventory().setResult(null);
						event.setCancelled(true);
						return;
					}
				}
				//change cursor
				event.getInventory().setResult(genShulker(st_inv, 0));
				//give extra shulkers
				for (int i = 1; i < lvl; i++)
					Utils.giveOrDrop(player_inv, genShulker(st_inv, 27 * i));
				
				//free space if needed(fixed slot -> hotbar button)
				switch (event.getAction())
				{
				case HOTBAR_MOVE_AND_READD:
				case HOTBAR_SWAP:
					ItemStack hotbar_slot = player_inv.getItem(event.getHotbarButton());
					if (hotbar_slot != null) {
						Utils.giveOrDrop(player_inv, hotbar_slot);
						player_inv.setItem(event.getHotbarButton(), null);
					}
					break;
				default:
					break;
				}
				
				//delete storage
				plugin.ststorage.deleteDataFile(storage.ID);
			}
		}
		else if (st instanceof StorageBottomless)
		{
			StorageBottomless storage = (StorageBottomless)st;
			Inventory player_inv = event.getWhoClicked().getInventory();
			if (UtilsType.is_shulker_box(result.getType())) {
				//stop unreal crafts (something moved to cursor(swap))
				ClickResult click_result = ClickResult.getClickResult(event);
				if (click_result.items_2_to_1 > 0 || click_result.items_1_to_2 <= 0)
					return;
				
				ItemStack empty_shulker = new ItemStack(Material.SHULKER_BOX);
				//change cursor
				event.getInventory().setResult(empty_shulker);
				//give extra shulkers
				for (int i = 1; i < 4; i++)
					Utils.giveOrDrop(player_inv, empty_shulker);
				
				//free space if needed(fixed slot -> hotbar button)
				switch (event.getAction())
				{
				case HOTBAR_MOVE_AND_READD:
				case HOTBAR_SWAP:
					ItemStack hotbar_slot = player_inv.getItem(event.getHotbarButton());
					if (hotbar_slot != null) {
						Utils.giveOrDrop(player_inv, hotbar_slot);
						player_inv.setItem(event.getHotbarButton(), null);
					}
					break;
				default:
					break;
				}
				
				//delete storage
				plugin.ststorage.deleteDataFile(storage.ID);
			}
		}
	}
	private static ItemStack genShulker(Inventory from, int offset)
	{
    	ItemStack item = new ItemStack(Material.SHULKER_BOX);
    	BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
    	ShulkerBox shulker = (ShulkerBox) im.getBlockState();
    	ItemStack[] contents = new ItemStack[27];
    	ItemStack[] stacks = from.getStorageContents();
    	for (int i = 0; i < 27; i++, offset++) {
    		if (offset >= stacks.length)
    			break;
    		contents[i] = stacks[offset];
    		stacks[offset] = null;
    	}
    	from.setStorageContents(stacks);
    	shulker.getInventory().setContents(contents);
    	im.setBlockState(shulker);
    	item.setItemMeta(im);
    	return item;
	}

	//immortality of storage
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntityType() != EntityType.DROPPED_ITEM) return;
		
		Item drop = (Item)event.getEntity();
		if(Storage.isStorage(drop.getItemStack())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent event) {
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		if(!event.hasBlock() || !UtilsType.isInteractable(event.getClickedBlock().getType())) {
			int id = Storage.getID(event.getItem());
			if(id >= 0) {
				event.setCancelled(true);
				Storage st = plugin.stlist.get(id);
				if(st == null) {
					plugin.getLogger().severe("Storage(ID="+id+") could not load (on interact of "
							+event.getPlayer().getName()+Utils.toString(event.getPlayer().getLocation())+")");
					//delete storage tag from itemstack or create new storage - configurable?
					return;
				}
				Player p = event.getPlayer();
				if(st instanceof StorageMultitype) {
					if (p.isSneaking())
						p.openInventory(st.getMenu());
					else
						p.openInventory(st.getInventory());
				}
				else if(st instanceof StorageBottomless)
					p.openInventory(st.getMenu());
			}
		}
	}

	// Storage logic
	@SuppressWarnings("deprecation")
	@EventHandler(priority=EventPriority.HIGHEST)
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
		
		// click with opened Storage Inventory
		Storage st = plugin.stlist.findByInventory(event.getView().getTopInventory());
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
						delayedUpdate(event.getClickedInventory());
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
							delayedUpdate(event.getClickedInventory());
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

		st = Storage.getByItemStack(current_item);
		// click on Storage
		if (st == null && Storage.getID(current_item) >= 0) {
			Utils.printError("Storage(ID="+Storage.getID(current_item)+") could not load (on inv click: "+event.getClick()+" in "
					+Utils.toString(event.getClickedInventory().getLocation())+")");
			// delete storage tag from itemstack?
			return;
		}
		if (st != null) {
			// click with non-empty cursor
			// CHANGE STORAGE TYPE / ADD ITEMS - only Bottomless
			if (cursor != null && cursor.getType() != Material.AIR)
			{
				if (UtilsType.is_shulker_box(cursor.getType())) {
					event.setCancelled(true);
					Inventory box_inv = Utils.getShulkerInventory(cursor);
					if (st instanceof StorageBottomless) {
						StorageBottomless stb = (StorageBottomless)st;
						if (event.getClick() == ClickType.LEFT) // put items to storage
						{
							stb.grabAnyInventory(box_inv);
							event.setCursor(Utils.setShulkerInventory(cursor, box_inv));
						}
						else if (event.getClick() == ClickType.RIGHT) // drag items from storage
						{
							// TODO: StorageBottomless grab functions, grab-Utils, ignore GrabFilter on SHIFT clicks
							Material pattern_material = stb.getMaterial();
							ItemStack pattern = new ItemStack(pattern_material);
							ItemStack[] stacks = box_inv.getContents();
							int storage_amount = stb.getAmount(), grabbed, stack_size = pattern.getMaxStackSize(), stack_amount;
							//stacking
							for (int i = 0; i < stacks.length; i++)
							{
								if (pattern.isSimilar(stacks[i]))
								{
									stack_amount = stacks[i].getAmount();
									grabbed = Math.min(stack_size - stack_amount, storage_amount);
									stacks[i].setAmount(stack_amount + grabbed);
									storage_amount -= grabbed;
									if (storage_amount == 0)
										break;
								}
							}
							if (storage_amount > 0) {
								//grabbing
								for (int i = 0; i < stacks.length; i++)
								{
									if (stacks[i] == null)
									{
										grabbed = Math.min(stack_size, storage_amount);
										stacks[i] = new ItemStack(pattern_material, grabbed);
										storage_amount -= grabbed;
										if (storage_amount == 0)
											break;
									}
								}
							}
							if (stb.getAmount() != storage_amount) {
								stb.setAmount(storage_amount);
								box_inv.setContents(stacks);
								event.setCursor(Utils.setShulkerInventory(cursor, box_inv));
							}
						}
						event.setCurrentItem(stb.getLored(current_item));
					}
					else if (st instanceof StorageMultitype) {
						StorageMultitype stm = (StorageMultitype)st;
						Inventory st_inv = stm.getInventory();
						ItemStack[] stacks = st_inv.getStorageContents();
						if (event.getClick() == ClickType.LEFT) // put items to storage
						{
							box_inv = Utils.getShulkerInventory(cursor);
							stm.grabAnyInventory(box_inv);
							event.setCursor(Utils.setShulkerInventory(cursor, box_inv));
						}
						else if (event.getClick() == ClickType.RIGHT) // drag items from storage
						{
							Pair<Boolean, ItemStack[]> res = StorageMultitype.grabInventory_stacking(box_inv, stacks, stm.getGrabDirection());
							res = StorageMultitype.grabInventory_any(box_inv, res.second, stm.getGrabDirection(), res.first);
							if (res.first) {
								res = StorageMultitype.grabInventory_stacking(box_inv, stacks, stm.getGrabDirection());
								stm.onAction(com.festp.storages.StorageMultitype.InventoryAction.LOSE);
								st_inv.setContents(res.second);
								event.setCursor(Utils.setShulkerInventory(cursor, box_inv));
							}
						}
					}
				}
				// drag cursor or set storage material
				else if (st instanceof StorageBottomless) {
					StorageBottomless stb = (StorageBottomless)st;
					if (stb.getMaterial() != cursor.getType()) {
						if (stb.isEmpty() && StorageBottomless.isAllowedMaterial(cursor.getType())) {
							stb.setMaterial(cursor.getType());
							event.setCancelled(true);
						}
					}
					else {
						event.setCancelled(true);
						stb.changeAmount(cursor.getAmount());
						event.setCursor(null);
						delayedUpdate(event.getClickedInventory());
					}
				}
			}
			
			// click with empty cursor
			// OPEN INV/SETTINGS, DROP INV
			else
			{
				switch(event.getClick())
				{
				case RIGHT:
					if (st instanceof StorageMultitype) {
						event.getWhoClicked().openInventory(st.getInventory());
					}
					else if (st instanceof StorageBottomless) {
						event.getWhoClicked().openInventory(((StorageBottomless) st).getMenu());
					}
					event.setCancelled(true);
					break;
				case SHIFT_RIGHT:
					if (st instanceof StorageMultitype) {
						event.getWhoClicked().openInventory(((StorageMultitype)st).getMenu());
					}
					return;
				case CONTROL_DROP:
					st.drop(event.getWhoClicked().getEyeLocation());
					delayedUpdate(event.getClickedInventory());
					event.setCancelled(true);
					break;
				default:
					for (HumanEntity he : st.getInventory().getViewers())
						if (he != event.getWhoClicked())
							he.closeInventory();
					if(st instanceof StorageBottomless) {
						for (HumanEntity he : ((StorageBottomless) st).getMenu().getViewers())
							if (he != event.getWhoClicked())
								he.closeInventory();
					}
					else if(st instanceof StorageMultitype)
						for (HumanEntity he : ((StorageMultitype) st).getMenu().getViewers())
							if (he != event.getWhoClicked())
								he.closeInventory();
				}
			}
			
			if (!event.isCancelled()) {
				// storage moved to other inventory by special approaches
				switch(event.getAction())
				{
				case MOVE_TO_OTHER_INVENTORY:
					//if there is empty slots
					Inventory inv = null;
					if (event.getView().getTopInventory() == event.getClickedInventory())
						inv = event.getView().getBottomInventory();
					else if (event.getView().getBottomInventory() == event.getClickedInventory())
						inv = event.getView().getTopInventory();
					if (inv != null && inv.firstEmpty() >= 0)
						st.setExternalInventory(inv);
				default:
					break;
				}
			}
		}

		// can move 2 storages at once
		switch (event.getAction())
		{
		case HOTBAR_MOVE_AND_READD:
		case HOTBAR_SWAP:
			if (event.getClick() == ClickType.NUMBER_KEY) {
				if (event.getView().getTopInventory() == event.getClickedInventory())
				{
					ItemStack hotbar_slot = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
					Storage st_clicked = Storage.getByItemStack(current_item), st_hotbar = Storage.getByItemStack(hotbar_slot);
					if (st_clicked != null)
						st_clicked.setExternalInventory(event.getView().getBottomInventory()); // to hotbar
					if (st_hotbar != null)
						st_hotbar.setExternalInventory(event.getView().getTopInventory()); // to top inv
				}
			}
		default:
			break;
		}
		
		// storage moved to other inventory by putting it
		st = Storage.getByItemStack(cursor);
		if (st != null)
		{
			switch (event.getAction())
			{
			case PLACE_ALL:
			case PLACE_ONE:
			case PLACE_SOME:
			case SWAP_WITH_CURSOR:
				st.setExternalInventory(event.getClickedInventory());
				break;
			default:
				break;
			}
			return;
		}
		
		// TO DO: Optimize using Inventory Utils: from(), to(), enum(CURSOR, TOP, BOTTOM, OUTSIDE)
		delayedGrab(event.getView().getTopInventory());
		delayedGrab(event.getView().getBottomInventory());
	}

	@EventHandler
	public void onCraft(CraftItemEvent event)
	{
		if (event.isCancelled())
			return;
		delayedGrab(event.getView().getBottomInventory());		
	}
	
	// TODO Mark bucket in order to Grab.NEW
	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent event)
	{
		delayedGrab(event.getPlayer().getInventory());
	}
	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent event)
	{
		delayedGrab(event.getPlayer().getInventory());
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		Inventory inventory = event.getInventory();
		Storage st = plugin.stlist.findByInventory(inventory);
		if (st instanceof StorageMultitype)
			if (inventory.getViewers().size() == 1) { // because when closing last player is still considered as a viewer
				StorageMultitype sm = (StorageMultitype)st;
				if (sm.getSortTime() == HandleTime.OPEN_CLOSE)
					sm.sort();
				if (sm.getStackTime() == HandleTime.OPEN_CLOSE)
					sm.mergeStacks();
			}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent event) {
		Storage st = Storage.getByItemStack(event.getOldCursor());
		if (st != null)
			st.setExternalInventory(event.getInventory());
		
		st = plugin.stlist.findByInventory(event.getView().getTopInventory());
		if (st != null && st instanceof StorageMultitype) {
			switch (event.getType())
			{
			case EVEN:
			case SINGLE:
				if (event.getInventory() == event.getView().getTopInventory())
					if (!st.isAllowed(event.getOldCursor()))
						event.setCancelled(true);
					else
						((StorageMultitype) st).onAction(com.festp.storages.StorageMultitype.InventoryAction.GAIN);
			}
			
		}
		
		// TO DO: Inventory Utils: from(), to(), enum(CURSOR, TOP, BOTTOM, OUTSIDE)
		delayedGrab(event.getView().getTopInventory());
		delayedGrab(event.getView().getBottomInventory());
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityPickupItemEvent(EntityPickupItemEvent event) {
		if(event.isCancelled()) return;
		if (event.getEntityType() != EntityType.PLAYER) return;

		Player player = (Player) event.getEntity();
		boolean storage_pickup = work_pickup_event(((Player)event.getEntity()).getInventory(), event.getItem(), event);
		if (storage_pickup) {
			player.playSound(player.getLocation(), PICKUP_SOUND, 0.7F, 2F);
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		if(event.isCancelled()) return;
		
		work_pickup_event(event.getInventory(), event.getItem(), event);
	}
	
	/** @return <b>true</b> if event was cancelled */
	private boolean work_pickup_event(Inventory inv, Item entity_item, Cancellable event) {
		ItemStack item = entity_item.getItemStack();
		Storage st = Storage.getByItemStack(item);
		if(st != null) // Storage had been picked up
			st.setExternalInventory(inv);
		else
		{
			int amount = grab(inv, item);
			if (item.getAmount() - amount != 0) {
				event.setCancelled(true);
				if (amount == 0) {
					entity_item.remove();
				}
				else {
					ItemStack edited = entity_item.getItemStack();
					edited.setAmount(amount);
					entity_item.setItemStack(edited);
				}
				StorageBottomless.update_item_counts(inv);
				return true;
			}
		}
		return false;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		if(event.isCancelled()) return;

		Inventory inv = event.getDestination();
		ItemStack item = event.getItem();

		Storage st = Storage.getByItemStack(item);
		if(st != null) // Storage had been moved
			st.setExternalInventory(inv);
		else
		{
			int amount = grab(inv, item);
			event.getItem().setAmount(amount);
			if (amount == 0)
				event.setItem(new ItemStack(Material.AIR));
			
			StorageBottomless.update_item_counts(inv);
		}
	}

	/** @return remaining amount */
	public int grab(Inventory inv, ItemStack item) {
		int amount = item.getAmount();
		List<Storage> storages = findGrabbingStorages(inv, item);
		for (Storage s : storages)
		{
			if (s instanceof StorageMultitype) {
				StorageMultitype sm = (StorageMultitype)s;
				amount -= sm.grabItemStack(item);
				if (amount == 0) {
					break;
				}
			}
			else if (s instanceof StorageBottomless) {
				((StorageBottomless) s).changeAmount(amount);
				amount = 0;
				break;
			}
		}
		return amount;
	}
	
	/** Grab all possible items to storages
	 * @return <b>true</b> - if <i>inv</i> has been updated*/
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
				if (st == null || !st.canGrab(inv)) continue;
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
		List<Storage> list = new ArrayList<>();
		if (!Storage.isGrabbableInventory(inv))
			return list;

		List<Storage> list_new = new ArrayList<>();
		StorageBottomless suit = null, suit_new = null;
		for(ItemStack is : inv.getContents())
		{
			int id = Storage.getID(is);
			Storage st = plugin.stlist.get(id);
			if(st != null)
			{
				if (st.canGrab() == Grab.NEW)
				{
					if (st instanceof StorageBottomless && suit_new == null && ((StorageBottomless) st).canGrab(stack.getType()))
						suit_new = (StorageBottomless) st;
					else if (st instanceof StorageMultitype && ((StorageMultitype) st).canGrab(stack))
						list_new.add(st);
				}
				else if (st.canGrab(inv))
				{
					if (st instanceof StorageBottomless && suit == null && ((StorageBottomless) st).canGrab(stack.getType()))
						suit = (StorageBottomless) st;
					else if (st instanceof StorageMultitype && ((StorageMultitype) st).canGrab(stack))
						list.add(st);
				}
			}
		}
		
		if (suit != null)
			list.add(suit);
		if (suit_new != null)
			list_new.add(suit_new);
		
		list_new.addAll(list);
		return list_new;
	}
}
