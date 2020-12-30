package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

public class MountManager implements Listener {
	private List<SaddledPolarBear> bears = new ArrayList<>();
	
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
			if (getSaddledPolarBear(vehicle) != null) {
				vehicle.remove();
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event)
	{
		for (Entity en : event.getChunk().getEntities()) {
			if (isSaddled(en)) {
				SaddledPolarBear.removeLinkedEntities(en, event.getChunk());
				bears.add(new SaddledPolarBear((PolarBear)en));
			}
		}
	}
	
	// TODO transfer damage from controller and visual to bear
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		SaddledPolarBear spb = getSaddledPolarBear(event.getRightClicked());
		Player player = event.getPlayer();
		if (spb == null) {
			if (!(event.getRightClicked() instanceof PolarBear))
				return;
			if (player.getInventory().getItem(event.getHand()).getType() == Material.SADDLE) {
				PolarBear bear = (PolarBear) event.getRightClicked();
				// store saddle
				bears.add(new SaddledPolarBear(bear));
				player.getInventory().setItem(event.getHand(), null);
				event.setCancelled(true);
			}
		} else {
			if (player.getInventory().getItem(event.getHand()).getType() == Material.AIR
					&& player.isSneaking()) {
				player.getInventory().setItem(event.getHand(), new ItemStack(Material.SADDLE));
				spb.remove();
				bears.remove(spb);
			} else {
				spb.addPassenger(player);
			}
			event.setCancelled(true);
		}
	}
	
	private SaddledPolarBear getSaddledPolarBear(Entity part) {
		for (SaddledPolarBear spb : bears) {
			if (part.equals(spb.getPolarBear())
					|| part.equals(spb.getHorse())
					|| part.equals(spb.getSaddle())) {
				return spb;
			}
		}
		return null;
	}
	
	private boolean isSaddled(Entity bear) {
		return bear instanceof PolarBear && SaddledPolarBear.isSaddled((PolarBear)bear);
	}
}
