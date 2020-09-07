package com.festp.maps;

import java.awt.image.BufferedImage;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;

public class MapHandler implements Listener {

	/** load last session map canvas */
	@EventHandler
	public void onMapLoad(MapInitializeEvent event)
	{
		int id = event.getMap().getId();
		IMap map = MapFileManager.load(id);
		if (map == null)
			return;
		
		AbstractRenderer renderer = null;
		if (map instanceof SmallMap) {
			renderer = new SmallRenderer((SmallMap) map);
		} else if (map instanceof DrawingMap) {
			renderer = new DrawingRenderer((DrawingMap) map);
		}
		if (renderer != null) {
			BufferedImage image = MapFileManager.loadImage(id);
			MapUtils.setRenderer(event.getMap(), renderer);
			if (image != null)
			{
				renderer.renderImage(image);
			}
		}
	}

	/** init new small map */
	@EventHandler
	public void onPlayerInitMap(PlayerInteractEvent event)
	{
		if (event.useInteractedBlock() == Result.DEFAULT)
			return;
		if (event.useItemInHand() == Result.DENY)
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType().isInteractable())
			return;

		if (event.hasItem() && event.getItem().getType() == Material.MAP)
		{
			if (!Utils.hasDataField(event.getItem(), MapUtils.SCALE_FIELD))
				return;
			
			int scale = Utils.getInt(event.getItem(), MapUtils.SCALE_FIELD);
			
			if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE)
				event.getItem().setAmount(event.getItem().getAmount() - 1);
			else
				if (event.getPlayer().getInventory().firstEmpty() < 0)
					return;
			
			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			SmallMap map = SmallMapUtils.genSmallMap(event.getPlayer().getLocation(), scale);
			ItemStack map_item = MapUtils.getMap(map.getId());
			Utils.giveOrDrop(event.getPlayer(), map_item);
		}
	}
}
