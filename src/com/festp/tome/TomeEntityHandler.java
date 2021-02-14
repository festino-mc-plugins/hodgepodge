package com.festp.tome;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.festp.DelayedTask;
import com.festp.TaskList;

public class TomeEntityHandler implements Listener {
	
	@EventHandler
	public void onPlayerDropTome(PlayerDropItemEvent event) {
		Entity summoned = SummonUtils.getHasSummoned(event.getItemDrop().getItemStack());
		if (summoned != null) {
			summoned.remove();
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (SummonUtils.wasSummoned(event.getEntity())) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity e : event.getChunk().getEntities())
			if (SummonUtils.wasSummoned(e) && e.getPassengers().size() == 0)
				e.remove();
	}

	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event) {
		if (SummonUtils.wasSummoned(event.getVehicle())) {
			event.setCancelled(true);
			event.getVehicle().remove();
		}
	}

	@EventHandler
	public void onVehicleExit(VehicleExitEvent event) {
		if (event.getVehicle().getPassengers().get(0) == event.getExited()) // probably driver
			if (SummonUtils.wasSummoned(event.getVehicle()))
				event.getVehicle().remove();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		final Player joined = event.getPlayer();
		TaskList.add(new DelayedTask(1, new Runnable() {
			@Override
			public void run() {
				if (joined.isOnline() && joined.isInsideVehicle())
				{
					Entity vehicle = joined.getVehicle();
					if (SummonUtils.wasSummoned(vehicle)) {
						vehicle.remove();
					}
				}
			}
		}));
	}
}
