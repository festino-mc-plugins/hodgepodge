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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.festp.DelayedTask;
import com.festp.TaskList;
import com.festp.utils.UtilsType;

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
	public void onPlayerJoin(PlayerJoinEvent event) {
		TaskList.add(new DelayedTask(1, new Runnable() { @Override
				public void run() {
					if (event.getPlayer().isInsideVehicle()) {
						SaddledBear.removeIfLinkedEntity(event.getPlayer().getVehicle());
					}
				}
		}));
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
		SaddledBear.removeLinkedEntities(event.getChunk());
		for (Entity en : event.getChunk().getEntities()) {
			if (isSaddled(en)) {
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
		ItemStack item = player.getInventory().getItem(event.getHand());
		
		if (sb == null) {
			if (!(event.getRightClicked() instanceof PolarBear))
				return;
			PolarBear bear = (PolarBear) event.getRightClicked();
			if (isTarget(bear, player))
				return;
			
			if (item.getType() == Material.SADDLE) {
				SaddledBear.setSaddled(bear, true);
				player.getInventory().setItem(event.getHand(), null);
				bears.add(new SaddledBear(bear));
				event.setCancelled(true);
			}
		} else {
			PolarBear bear = sb.getBear();
			if (isTarget(bear, player))
				return;
			
			if (item.getType() == Material.AIR
					&& player.isSneaking()) {
				SaddledBear.setSaddled(bear, false);
				player.getInventory().setItem(event.getHand(), new ItemStack(Material.SADDLE));
				sb.remove();
				bears.remove(sb);
			} else {
				sb.addPassenger(player);
			}
			event.setCancelled(true);
		}
	}
	
	// TODO united class, merge work with TomeClickHandler - check if entity in list, then cancel event if:
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.isCancelled()) return;
		if (!(event.getView().getTopInventory() instanceof AbstractHorseInventory)) return;
		
		Entity horse = (Entity) event.getView().getTopInventory().getHolder();
		if (getSaddledBear(horse) == null) return;
		
		int slot = event.getRawSlot();
		if (slot < 0) return;
		
		boolean illegal = false;
		Inventory inv = event.getClickedInventory();
		InventoryAction action = event.getAction();
		
		if (inv instanceof AbstractHorseInventory) {
			if(action != InventoryAction.CLONE_STACK && action != InventoryAction.UNKNOWN)
				illegal = true;
		}
		else if (inv instanceof PlayerInventory) {
			Material m = event.getCurrentItem().getType();
			if ( (event.getView().getItem(1) == null || event.getView().getItem(1).getType() == Material.AIR)
					&& UtilsType.isHorseArmor(m)) {
					if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
						illegal = true;
					}
				}
		}
		
		if (illegal)
			event.setCancelled(true);
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
