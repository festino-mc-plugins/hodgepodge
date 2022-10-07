package com.festp.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.Config;
import com.festp.utils.DirtyUtils;
import com.festp.utils.Utils;
import com.festp.utils.UtilsType;
import com.festp.utils.UtilsWorld;

public class InventoryHandler implements Listener {
	private enum ArmorSlot { BOOTS, LEGGINGS, CHESTPLATE, HELMET };
	private List<ClosedInventory> closedInvs = new ArrayList<>();
	private List<DroppedItem> droppedItems = new ArrayList<>();
	
	public void onTick() {
		// drop inventories
		for (int i = closedInvs.size()-1; i >= 0; i--)
		{
			if (closedInvs.get(i).getTicks() <= 0)
			{
				closedInvs.remove(i);
				continue;
			}
			closedInvs.get(i).tick();
		}
		for (int i = droppedItems.size()-1; i >= 0; i--)
		{
			droppedItems.get(i).time--;
			if (droppedItems.get(i).time < 0)
				droppedItems.remove(i);
		}
	}
	
	// remember shulker box / chest
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (!ClosedInventory.isDroppable(event.getInventory().getType()))
			return;
		if (event.getInventory().getLocation() == null)
			return;
		closedInvs.add( new ClosedInventory(event.getPlayer().getUniqueId(), Config.maxClosedInvTicks, event.getView()) );
	}
	
	// drop shulker box / chest
	@EventHandler
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event)
	{
		if (!event.getPlayer().isSneaking()) // [Shift] + [F] only
			return;
		
		ClosedInventory cinv = null;
		for (int i = 0; i < closedInvs.size(); i++)
			if (closedInvs.get(i).matchUUID(event.getPlayer().getUniqueId())) {
				if (closedInvs.get(i).getTicks() != Config.maxClosedInvTicks) {
					cinv = closedInvs.get(i);
					closedInvs.remove(i);
					break;
				}
			}
		if (cinv != null)
		{
			Inventory inv = cinv.getInventory();
			if (inv == null) return;
			event.setCancelled(true);
			Player p = event.getPlayer();
			ItemStack[] items = inv.getContents();
			for (int i = 0; i < items.length; i++) {
				ItemStack stack = items[i];
				if (stack == null)
					continue;
				
				InventoryClickEvent dropEvent;
				if (stack.getAmount() == 1) // for storages
					dropEvent = new InventoryClickEvent(cinv.getView(), SlotType.CONTAINER, i, ClickType.CONTROL_DROP, InventoryAction.DROP_ONE_SLOT);
				else
					dropEvent = new InventoryClickEvent(cinv.getView(), SlotType.CONTAINER, i, ClickType.CONTROL_DROP, InventoryAction.DROP_ALL_SLOT);
				Bukkit.getPluginManager().callEvent(dropEvent);
				if (!dropEvent.isCancelled()) {
					UtilsWorld.drop(p.getEyeLocation(), stack, 1);
					items[i] = null;
				}
			}
			inv.setContents(items);
		}
	}
	
	// armor equip
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerPickupItem(EntityPickupItemEvent event)
	{
		if (event.isCancelled()) return;
		if (event.getEntityType() != EntityType.PLAYER) return;
		
		Player player = (Player)event.getEntity();
		
		for (int i = droppedItems.size()-1; i >= 0; i--)
		{
			if (droppedItems.get(i).item == event.getItem() && droppedItems.get(i).p == player)
			{
				event.setCancelled(true);
				return;
			}
		}

		ItemStack pickedup = event.getItem().getItemStack();
		if (pickedup.containsEnchantment(Enchantment.BINDING_CURSE))
			return;
		
		ArmorSlot slot = getArmorSlot(pickedup);
		if (slot == null)
			return;
		
		ItemStack currentItem = getBySlot(player.getInventory(), slot);
		int currentPower = getEquipingPower(currentItem);
		int newPower = getEquipingPower(pickedup);
		if (currentPower == 0)
			return;

		boolean isEquiped = false, delete_item = false;
		if (newPower > 0 && currentPower > 0 && currentPower < newPower) {
			event.getItem().setItemStack(currentItem);
			setBySlot(player.getInventory(), slot, pickedup);
			isEquiped = true;
		}
		
		if (newPower >= 0 && currentPower < 0) {
			setBySlot(player.getInventory(), slot, pickedup);
			isEquiped = true;
			delete_item = true;
		}
		
		if (isEquiped) {
			event.setCancelled(true);
			if(delete_item) {
				event.getItem().remove();
			} else {
				event.setCancelled(true);
				player.updateInventory();
			}
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 0.4F);
		}
	}
	
	// quick transfer between players
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		//event.getItemDrop().setPickupDelay(0); - can't because of dropping player existing
		droppedItems.add(new DroppedItem(event.getPlayer(), event.getItemDrop()));
	}
	
	// empty bottles
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.POTION && ( event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE))
		{
			Inventory inv = event.getPlayer().getInventory();
			PotionMeta potionMeta = (PotionMeta)event.getItem().getItemMeta();
			boolean slotWasFound = false;
			// check bottles in deep inventory
			for (int i = 9; i < 36; i++) {
				if (isStackableBottle(inv.getItem(i))) {
					inv.getItem(i).setAmount(inv.getItem(i).getAmount() + 1);
					slotWasFound = true;
					break;
				}
			}
			if (!slotWasFound)
				// check bottles in hotbar
				for (int i = 0; i < 9; i++) {
					if (isStackableBottle(inv.getItem(i))) {
						inv.getItem(i).setAmount(inv.getItem(i).getAmount() + 1);
						slotWasFound = true;
						break;
					}
				}
			if (!slotWasFound)
				// check empty in deep inventory
				for (int i = 9; i < 36; i++) {
					if (inv.getItem(i) == null) {
						inv.setItem(i, new ItemStack(Material.GLASS_BOTTLE));
						slotWasFound = true;
						break;
					}
				}
			
			if (slotWasFound) {
				Collection<PotionEffect> pes = DirtyUtils.getPotionEffects(potionMeta);
				for (PotionEffect penew : pes) {
					PotionEffectType pecur = penew.getType();
					for (PotionEffect peold : event.getPlayer().getActivePotionEffects())
					{
						if (peold.getType() == pecur && ( peold.getDuration() < penew.getDuration() || peold.getAmplifier() < penew.getAmplifier() ) ) {
							event.getPlayer().removePotionEffect(pecur);
						}
					}
				}
				event.getPlayer().addPotionEffects( pes );
				event.getPlayer().addPotionEffects( potionMeta.getCustomEffects() );
				event.setItem(new ItemStack(Material.AIR));
			}
			// else leave empty bottle in hand
		}
	}
	
	private static boolean isStackableBottle(ItemStack stack)
	{
		return stack != null && stack.getType() == Material.GLASS_BOTTLE && stack.getAmount() < Material.GLASS_BOTTLE.getMaxStackSize();
	}
	
	@EventHandler
	public void onPlayerEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) return;
		if (event.getDamager() instanceof Player) {
			PlayerInventory player_inv = ((Player)event.getDamager()).getInventory();
			ItemStack hitting_item = player_inv.getItemInMainHand();
			Material m = hitting_item.getType();
			if (UtilsType.isSword(hitting_item.getType()) || hitting_item.getType() == Material.TRIDENT) {
				if (((Damageable)hitting_item.getItemMeta()).getDamage() + 1 >= m.getMaxDurability()) {
					boolean will_be_replaced = replace_tool(player_inv, player_inv.getHeldItemSlot());
					event.setCancelled(will_be_replaced);
				}
			}
			else if (UtilsType.isTool(hitting_item.getType())) {
				if (((Damageable)hitting_item.getItemMeta()).getDamage() + 2 >= m.getMaxDurability()) {
					boolean will_be_replaced = replace_tool(player_inv, player_inv.getHeldItemSlot());
					event.setCancelled(will_be_replaced);
				}
			}
		}
	}

	// find same tool and replace
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		PlayerInventory player_inv = event.getPlayer().getInventory();
		Material m = event.getBrokenItem().getType();
		
		int slot = player_inv.getHeldItemSlot();
		ItemStack item = player_inv.getItem(slot);
		if (!(item != null && item.hasItemMeta() && item.getItemMeta() instanceof Damageable
				&& ((Damageable)item.getItemMeta()).getDamage() == m.getMaxDurability()))
		{
			slot = player_inv.getSize()-1; //second hand
			item = player_inv.getItem(slot);
			if (!(item != null && item.hasItemMeta() && item.getItemMeta() instanceof Damageable
					&& ((Damageable)item.getItemMeta()).getDamage() == m.getMaxDurability()))
				return;
		}
		
		replace_tool(player_inv, slot);
	}
	
	// return true if tool will be replaced
	private boolean replace_tool(PlayerInventory player_inv, int slot) {
		Material m = player_inv.getItem(slot).getType();
		boolean save_tool = player_inv.getItem(slot).hasItemMeta() && player_inv.getItem(slot).getItemMeta().getEnchantLevel(Enchantment.MENDING) > 0
				|| Utils.isRenamed(player_inv.getItem(slot));
		
		int slot_new_tool = -1;
		// from off hand
		if (slot != player_inv.getSize()-1)
			if (player_inv.getItem(player_inv.getSize()-1) != null && player_inv.getItem(player_inv.getSize()-1).getType() == m && ((Damageable)player_inv.getItem(slot).getItemMeta()).getDamage() < m.getMaxDurability()) {
				slot_new_tool = player_inv.getSize()-1;
			}

		if (slot_new_tool < 0)
			// from hotbar and from inventory
			for (int i = 0; i < 36; i++) {
				if (i != slot && player_inv.getItem(i) != null && player_inv.getItem(i).getType() == m && ((Damageable)player_inv.getItem(i).getItemMeta()).getDamage() < m.getMaxDurability()) {
					slot_new_tool = i;
					break;
				}
			}

		if (save_tool) {
			if (slot_new_tool >= 0) {
				ItemStack temp = player_inv.getItem(slot);
				player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
				player_inv.setItem(slot_new_tool, temp);
			}
			else {
				if (player_inv.getItemInOffHand() == null)
					slot_new_tool = player_inv.getSize()-1;
				else
					for(int i = 0; i < 36; i++)
						if(player_inv.getItem(i) == null) {
							slot_new_tool = i;
							break;
						}
				if (slot_new_tool >= 0) {
					ItemStack temp = player_inv.getItem(slot);
					player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
					player_inv.setItem(slot_new_tool, temp);
				}
				else {
					if (!UtilsType.isTool(player_inv.getItemInOffHand().getType()))
						slot_new_tool = player_inv.getSize()-1;
					else
						for (int i = 0; i < 36; i++)
							if (!UtilsType.isTool(player_inv.getItem(i).getType())) {
								slot_new_tool = i;
								break;
							}
					if (slot_new_tool >= 0) {
						ItemStack temp = player_inv.getItem(slot);
						player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
						player_inv.setItem(slot_new_tool, temp);
					}
				}
			}
		}
		else if (slot_new_tool >= 0) {
			player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
			player_inv.setItem(slot_new_tool, null);
		}
		return slot_new_tool >= 0;
	}
	
	private static ItemStack getBySlot(PlayerInventory inv, ArmorSlot slot) {
		if (slot == ArmorSlot.BOOTS)
			return inv.getBoots();
		if (slot == ArmorSlot.LEGGINGS)
			return inv.getLeggings();
		if (slot == ArmorSlot.CHESTPLATE)
			return inv.getChestplate();
		if (slot == ArmorSlot.HELMET)
			return inv.getHelmet();
		return null;
	}
	
	private static void setBySlot(PlayerInventory inv, ArmorSlot slot, ItemStack is) {
		if (slot == ArmorSlot.BOOTS)
			inv.setBoots(is);
		else if (slot == ArmorSlot.LEGGINGS)
			inv.setLeggings(is);
		else if (slot == ArmorSlot.CHESTPLATE)
			inv.setChestplate(is);
		else if (slot == ArmorSlot.HELMET)
			inv.setHelmet(is);
	}
	
	private static ArmorSlot getArmorSlot(ItemStack is) {
		if (is == null)
			return null;
		
		Material item = is.getType();
		String item_name = item.toString();
		if (item_name.contains("BOOTS"))
			return ArmorSlot.BOOTS;
		if (item_name.contains("LEGGINGS"))
			return ArmorSlot.LEGGINGS;
		if (item_name.contains("CHESTPLATE"))
			return ArmorSlot.CHESTPLATE;
		if (item_name.contains("HELMET"))
			return ArmorSlot.HELMET;
		
		if (item == Material.ELYTRA)
			return ArmorSlot.CHESTPLATE;
		if (item == Material.TURTLE_HELMET)
			return ArmorSlot.HELMET;
		return null;
	}
	 
	private int getEquipingPower(ItemStack is) {
		if (is == null || is.getType().equals(Material.AIR))
			return -1;
		
		if (is.containsEnchantment(Enchantment.BINDING_CURSE))
			return 0;
		
		String mat = is.getType().toString();
		if (mat.contains("NETHERITE"))
			return 6;
		if (mat.contains("DIAMOND"))
			return 5;
		if (mat.contains("IRON"))
			return 4;
		if (mat.contains("CHAIN"))
			return 3;
		if (mat.contains("GOLD"))
			return 2;
		if (mat.contains("LEATHER"))
			return 1;
		
		return 0;
	}
}
