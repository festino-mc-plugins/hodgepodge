package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class MountManager implements Listener {
	private List<SaddledBear> bears = new ArrayList<>();
	
	public MountManager() {};
	
	public void tick()
	{
		for (int i = bears.size() - 1; i >= 0; i--) {
			if (!bears.get(i).update()) {
				bears.remove(i);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Entity vehicle = event.getPlayer().getVehicle();
		if (vehicle != null) {
			if (getSaddledBear(vehicle) != null) {
				vehicle.remove();
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		for (Entity en : event.getChunk().getEntities()) {
			if (isSaddled(en)) {
				SaddledBear.removeLinkedEntities(en, event.getChunk());
				bears.add(new SaddledBear((PolarBear)en));
			}
		}
	}
	
	// transfer damage from controller to bear
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		SaddledBear spb = getSaddledBear(event.getEntity());
		if (spb != null) {
			if (spb.getHorse() != null && event.getEntity().equals(spb.getHorse())) {
				EntityDamageEvent damageEvent = new EntityDamageEvent(spb.getBear(), event.getCause(), event.getDamage());
				Bukkit.getPluginManager().callEvent(damageEvent);
				if (!damageEvent.isCancelled()) {
					spb.getBear().damage(event.getDamage());
				}
				event.setCancelled(true);
			}
		}
	}

	// cancel own bear damaging
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		Entity damaged = event.getEntity();
		Entity damager = event.getDamager();
		if (damager.isInsideVehicle()) {
			SaddledBear spb = getSaddledBear(damager.getVehicle());
			if (spb != null) {
				if (spb.isPart(damaged)) {
					event.setCancelled(true);
				} else {
					if (damaged instanceof LivingEntity) {
						spb.getBear().setTarget((LivingEntity) damaged);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onHorseJumpEvent(HorseJumpEvent event)
	{
		SaddledBear sb = getSaddledBear(event.getEntity());
		if (sb != null) {
			sb.onJump(event.getPower());
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		SaddledBear sb = getSaddledBear(event.getRightClicked());
		Player player = event.getPlayer();
		if (sb == null) {
			if (!(event.getRightClicked() instanceof PolarBear))
				return;
			PolarBear bear = (PolarBear) event.getRightClicked();
			if (isTarget(bear, player))
				return;
			
			if (player.getInventory().getItem(event.getHand()).getType() == Material.SADDLE) {
				SaddledBear.setSaddled(bear);
				bears.add(new SaddledBear(bear));
				player.getInventory().setItem(event.getHand(), null);
				event.setCancelled(true);
			}
		} else {
			if (isTarget(sb.getBear(), player))
				return;
			
			if (player.getInventory().getItem(event.getHand()).getType() == Material.AIR
					&& player.isSneaking()) {
				player.getInventory().setItem(event.getHand(), new ItemStack(Material.SADDLE));
				sb.remove();
				bears.remove(sb);
			} else {
				sb.addPassenger(player);
			}
			event.setCancelled(true);
		}
	}
	
	private SaddledBear getSaddledBear(Entity part) {
		for (SaddledBear sb : bears) {
			if (sb.isPart(part)) {
				return sb;
			}
		}
		return null;
	}
	
	private boolean isSaddled(Entity bear) {
		return bear instanceof PolarBear && SaddledBear.isSaddled((PolarBear)bear);
	}
	
	private boolean isTarget(Mob agressive, LivingEntity victim) {
		return agressive.getTarget() != null && agressive.getTarget().equals(victim);
	}
}
