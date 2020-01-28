package com.festp.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_15_R1.potion.CraftPotionBrewer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.Config;
import com.festp.utils.Utils;
import com.festp.utils.UtilsType;
import com.festp.utils.UtilsWorld;

public class InventoryHandler implements Listener {

	private List<ClosedInventory> closed_invs = new ArrayList<>();
	private List<DroppedItem> dropped_items = new ArrayList<>();
	
	private List<Player> todeletecopy_players = new ArrayList<>(); //smelling code
	private List<Player> broken_tools_players = new ArrayList<>();
	
	public void onTick() {
		//drop inventories
		for (int i = closed_invs.size()-1; i >= 0; i--)
		{
			if (closed_invs.get(i).getTicks() <= 0)
			{
				closed_invs.remove(i);
				continue;
			}
			closed_invs.get(i).oneTick();
		}
		//delete armor dupes
		for (int i = todeletecopy_players.size()-1; i >= 0; i--)
		{
			ItemStack[] inv = todeletecopy_players.get(i).getInventory().getStorageContents();
			for (int j=0; j<inv.length; j++) {
				if (inv[j] != null
						&& inv[j].getItemMeta().hasLore()
						&& !inv[j].getItemMeta().getLore().isEmpty()
						&& inv[j].getItemMeta().getLore().get(0).equals("todelete")) {
					todeletecopy_players.get(i).getInventory().clear(j);
					todeletecopy_players.remove(i);
				}
			}
		}
		for (int i = dropped_items.size()-1; i >= 0; i--)
		{
			dropped_items.get(i).time--;
			if (dropped_items.get(i).time < 0)
				dropped_items.remove(i);
		}
		
		broken_tools_players.clear();
	}
	
	//remember shulker box / chest
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		 if(event.getInventory().getType() == InventoryType.SHULKER_BOX
				 || event.getInventory().getType() == InventoryType.CHEST
				 || event.getInventory().getType() == InventoryType.BARREL)
		 {
			 if(event.getInventory().getLocation() != null)
				 closed_invs.add( new ClosedInventory(event.getPlayer().getUniqueId(), Config.max_closed_inv_ticks, event.getInventory().getLocation().getBlock()) );
		 }
	}
	
	//drop shulker box / chest
	@EventHandler
	public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event)
	{
		ClosedInventory cinv = null;
		for(int i=0; i < closed_invs.size(); i++)
			if(closed_invs.get(i).UUID_match(event.getPlayer().getUniqueId())) {
				if(closed_invs.get(i).getTicks() != Config.max_closed_inv_ticks) {
					cinv = closed_invs.get(i);
					closed_invs.remove(i);
					break;
				}
			}
		if(cinv != null)
		{
			Inventory inv = cinv.getInventory();
			if(inv == null) return;
			event.setCancelled(true);
			Player p = event.getPlayer();
			for(ItemStack stack : inv.getContents()) {
				UtilsWorld.drop(p.getEyeLocation(), stack, 1);
			}
			inv.setContents(new ItemStack[inv.getContents().length]);
		}
	}
	
	//armor equip
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerPickupItem(EntityPickupItemEvent event)
	{
		if (event.isCancelled()) return;
		if (event.getEntityType() != EntityType.PLAYER) return;
		
		Player player = (Player)event.getEntity();
		
		for (int i=dropped_items.size()-1; i>=0; i--)
		{
			if (dropped_items.get(i).item == event.getItem() && dropped_items.get(i).p == player)
			{
				event.setCancelled(true);
				return;
			}
		}

		ItemStack pickedup = event.getItem().getItemStack();
		Material armor_material = pickedup.getType();
		String string_id = armor_material.toString();
		//кожанка-золото-кольчуга-железо-алмазы
		boolean isEquiped = false, deleteItem = false;
		if (string_id.contains("HELMET"))
		{
			if ( player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType().equals(Material.AIR) ) {
				isEquiped = true;
				deleteItem = true;
				player.getInventory().setHelmet(pickedup);
			} else if (  armorMaterial(armor_material) < itemMaterial(player.getInventory().getHelmet().getType()) ) {
				isEquiped = true;
				event.getItem().setItemStack(player.getInventory().getHelmet());
				player.getInventory().setHelmet(pickedup);
			}
		}
		else if (string_id.contains("CHESTPLATE"))
		{
			if ( player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType().equals(Material.AIR) ) {
				isEquiped = true;
				deleteItem = true;
				player.getInventory().setChestplate(pickedup);
			} else if ( armorMaterial(armor_material) < itemMaterial(player.getInventory().getChestplate().getType()) ) {
				isEquiped = true;
				event.getItem().setItemStack(player.getInventory().getChestplate());
				player.getInventory().setChestplate(pickedup);
			}
		}
		/*else if(armor_material.equals(Material.ELYTRA) && player.getInventory().getChestplate() == null)
		{
			isEquiped = true;
			deleteItem = true;
			player.getInventory().setChestplate(pickedup);
		}*/
		else if (string_id.contains("LEGGINGS"))
		{
			if ( player.getInventory().getLeggings() == null || player.getInventory().getLeggings().getType().equals(Material.AIR) ) {
				isEquiped = true;
				deleteItem = true;
				player.getInventory().setLeggings(pickedup);
			} else if ( armorMaterial(armor_material) < itemMaterial(player.getInventory().getLeggings().getType()) ) {
				isEquiped = true;
				event.getItem().setItemStack(player.getInventory().getLeggings());
				player.getInventory().setLeggings(pickedup);
			}
		}
		else if (string_id.contains("BOOTS"))
		{
			if ( player.getInventory().getBoots() == null || player.getInventory().getBoots().getType().equals(Material.AIR) ) {
				isEquiped = true;
				deleteItem = true;
				player.getInventory().setBoots(pickedup);
			} else if ( armorMaterial(armor_material) < itemMaterial(player.getInventory().getBoots().getType()) ) {
				isEquiped = true;
				event.getItem().setItemStack(player.getInventory().getBoots());
				player.getInventory().setBoots(pickedup);
			}
		}
		if (isEquiped) {
			/*ItemStack item = pickedup;
			ItemMeta im = item.getItemMeta();
			im.setLore(Arrays.asList("todelete"));
			item.setItemMeta(im);
			event.getItem().setItemStack(item);
			todeletecopy_players.add(player);*/
			event.setCancelled(true);
			//event.getItem().setItemStack(new ItemStack(Material.AIR));
			if(deleteItem)
				event.getItem().remove();
			else
				event.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 0.4F);
		}
	}
	
	//quick transfer between players
	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		//event.getItemDrop().setPickupDelay(0); - can't because of dropping player existing
		dropped_items.add(new DroppedItem(event.getPlayer(), event.getItemDrop()));
	}
	
	//empty bottles, 
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(event.getItem().getType() == Material.POTION && ( event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE))
		{
			Inventory plinv = event.getPlayer().getInventory();
			PotionMeta po = null;
			for(int i=9;i<36;i++) {
				if(plinv.getItem(i) != null && plinv.getItem(i).getType() == Material.GLASS_BOTTLE && plinv.getItem(i).getAmount() < Material.GLASS_BOTTLE.getMaxStackSize()) {
					plinv.getItem(i).setAmount(plinv.getItem(i).getAmount()+1);

					//event.getPlayer().addPotionEffects(Potion.fromItemStack(event.getItem()).getEffects());
					//System.out.println(po+" "+( new CraftPotionBrewer() ).getEffects(po.getBasePotionData().getType(), po.getBasePotionData().isUpgraded(), po.getBasePotionData().isExtended()));
					po = (PotionMeta)event.getItem().getItemMeta();
					
					break;
				}
			}
			if(po == null)
			for(int i=0;i<9;i++) {
				if(plinv.getItem(i) != null && plinv.getItem(i).getType() == Material.GLASS_BOTTLE && plinv.getItem(i).getAmount() < Material.GLASS_BOTTLE.getMaxStackSize()) {
					plinv.getItem(i).setAmount(plinv.getItem(i).getAmount()+1);

					po = (PotionMeta)event.getItem().getItemMeta();
					
					break;
				}
			}
			if(po == null)
			for(int i=9;i<36;i++) {
				if(plinv.getItem(i) == null) {
					plinv.setItem(i, new ItemStack(Material.GLASS_BOTTLE));

					po = (PotionMeta)event.getItem().getItemMeta();
					
					break;
				}
			}
			
			if(po != null) {
				Collection<PotionEffect> pes = ( new CraftPotionBrewer() ).getEffects(po.getBasePotionData().getType(), po.getBasePotionData().isUpgraded(), po.getBasePotionData().isExtended());
				for(PotionEffect penew : pes) {
					PotionEffectType pecur = penew.getType();
					for(PotionEffect peold : event.getPlayer().getActivePotionEffects())
					{
						if(peold.getType() == pecur && ( peold.getDuration() < penew.getDuration() || peold.getAmplifier() < penew.getAmplifier() ) ) {
							event.getPlayer().removePotionEffect(pecur);
						}
					}
				}
				event.getPlayer().addPotionEffects( pes );
				event.getPlayer().addPotionEffects( po.getCustomEffects() );
				event.setItem(new ItemStack(Material.AIR));
			}
		}
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

	//find same tool and replace
	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		PlayerInventory player_inv = event.getPlayer().getInventory();
		Material m = event.getBrokenItem().getType();
		
		int slot = player_inv.getHeldItemSlot();
		if(!(player_inv.getItem(slot) != null && player_inv.getItem(slot).hasItemMeta() && player_inv.getItem(slot).getItemMeta() instanceof Damageable
				&& ((Damageable)player_inv.getItem(slot).getItemMeta()).getDamage() == m.getMaxDurability()))
			slot = player_inv.getSize()-1; //second hand
		if(!(player_inv.getItem(slot) != null && player_inv.getItem(slot).hasItemMeta() && player_inv.getItem(slot).getItemMeta() instanceof Damageable
				&& ((Damageable)player_inv.getItem(slot).getItemMeta()).getDamage() == m.getMaxDurability()))
			return;
		
		replace_tool(player_inv, slot);
	}
	
	//return true if tool will be replaced
	private boolean replace_tool(PlayerInventory player_inv, int slot) {
		Material m = player_inv.getItem(slot).getType();
		boolean save_tool = player_inv.getItem(slot).hasItemMeta() && player_inv.getItem(slot).getItemMeta().getEnchantLevel(Enchantment.MENDING) > 0
				|| Utils.isRenamed(player_inv.getItem(slot));
		
		int slot_new_tool = -1;
		//from off hand
		if(slot != player_inv.getSize()-1)
			if(player_inv.getItem(player_inv.getSize()-1) != null && player_inv.getItem(player_inv.getSize()-1).getType() == m && ((Damageable)player_inv.getItem(slot).getItemMeta()).getDamage() < m.getMaxDurability()) {
				slot_new_tool = player_inv.getSize()-1;
			}

		if(slot_new_tool < 0)
			//from hotbar and from inventory
			for(int i=0; i<36; i++) {
				if(i != slot && player_inv.getItem(i) != null && player_inv.getItem(i).getType() == m && ((Damageable)player_inv.getItem(i).getItemMeta()).getDamage() < m.getMaxDurability()) {
					slot_new_tool = i;
					break;
				}
			}

		if(save_tool) {
			if(slot_new_tool >= 0) {
				ItemStack temp = player_inv.getItem(slot);
				player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
				player_inv.setItem(slot_new_tool, temp);
			}
			else {
				if(player_inv.getItemInOffHand() == null)
					slot_new_tool = player_inv.getSize()-1;
				else
					for(int i=0; i<36; i++)
						if(player_inv.getItem(i) == null) {
							slot_new_tool = i;
							break;
						}
				if(slot_new_tool >= 0) {
					ItemStack temp = player_inv.getItem(slot);
					player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
					player_inv.setItem(slot_new_tool, temp);
				}
				else {
					if(!UtilsType.isTool(player_inv.getItemInOffHand().getType()))
						slot_new_tool = player_inv.getSize()-1;
					else
						for(int i=0; i<36; i++)
							if(!UtilsType.isTool(player_inv.getItem(i).getType())) {
								slot_new_tool = i;
								break;
							}
					if(slot_new_tool >= 0) {
						ItemStack temp = player_inv.getItem(slot);
						player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
						player_inv.setItem(slot_new_tool, temp);
					}
				}
			}
		}
		else if(slot_new_tool >= 0) {
			player_inv.setItem(slot, player_inv.getItem(slot_new_tool));
			player_inv.setItem(slot_new_tool, null);
		}
		return slot_new_tool >= 0;
	}
	 
	public int itemMaterial(Material m) {
		String mat = m.toString();
		if(!(mat.contains("BOOTS") || mat.contains("LEGGINGS") || mat.contains("CHESTPLATE") || mat.contains("HELMET")))
		{
			return 0;
		}
		if(mat.contains("DIAMOND"))
			return 1;
		if(mat.contains("IRON"))
			return 2;
		if(mat.contains("CHAIN"))
			return 3;
		if(mat.contains("GOLD"))
			return 4;
		if(mat.contains("LEATHER"))
			return 5;
		return 0;
	}
	 
	public int armorMaterial(Material m) {
		String mat = m.toString();
		if(mat.contains("DIAMOND"))
			return 1;
		if(mat.contains("IRON"))
			return 2;
		if(mat.contains("CHAIN"))
			return 3;
		if(mat.contains("GOLD"))
			return 4;
		if(mat.contains("LEATHER"))
			return 5;
		return 0;
	}
}
