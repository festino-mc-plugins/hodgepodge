package com.festp.misc;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.inventory.ItemStack;

import com.festp.CraftManager;

public class GlassItemFrameHandler implements Listener {

	@EventHandler
	public void onDeath(HangingBreakEvent ev) {
		if (ev.getEntity().getType() != EntityType.ITEM_FRAME) {
			return;
		}
		ItemFrame itemFrame = (ItemFrame) ev.getEntity();
		if (itemFrame.isVisible()) {
			return;
		}

		Location dropLoc = itemFrame.getLocation();
		double offsetX = dropLoc.getX() - dropLoc.getBlockX() - 0.5;
		double offsetY = dropLoc.getY() - dropLoc.getBlockY() - 0.5;
		double offsetZ = dropLoc.getZ() - dropLoc.getBlockZ() - 0.5;
		Location offset = new Location(dropLoc.getWorld(), offsetX, offsetY, offsetZ);
		dropLoc = dropLoc/*.subtract(0.5, 0.5, 0.5)*/.subtract(offset);
		offset.multiply(12d / 15); // 1/32 & 31/32 -> +-15/32 -> +-3/8
		dropLoc = dropLoc.add(offset);
		
		if (ev.getCause() == RemoveCause.ENTITY) {
			HangingBreakByEntityEvent event = (HangingBreakByEntityEvent) ev;
			Entity entity = event.getRemover();
			if (entity instanceof LivingEntity) {
				LivingEntity remover = (LivingEntity) entity;
				ItemStack handItem = remover.getEquipment().getItemInMainHand();
				if (handItem != null && handItem.containsEnchantment(Enchantment.SILK_TOUCH)) {
					itemFrame.getWorld().dropItem(dropLoc, CraftManager.getInvisibleItemFrame());
				}
			}
		}
		itemFrame.getWorld().playSound(dropLoc, Sound.ENTITY_ITEM_FRAME_BREAK, SoundCategory.NEUTRAL, 1f, 1f);
		ev.setCancelled(true);
		itemFrame.remove();
	}
}
