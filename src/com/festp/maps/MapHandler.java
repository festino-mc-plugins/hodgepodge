package com.festp.maps;

import java.awt.image.BufferedImage;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

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

		if (!event.hasItem())
			return;
		
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		if (item.getType() == Material.MAP) {
			ItemStack mapItem;
			if (Utils.hasDataField(item, MapUtils.SCALE_FIELD)) {
				int scale = Utils.getInt(item, MapUtils.SCALE_FIELD);
				SmallMap map = SmallMapUtils.genSmallMap(player.getLocation(), scale);
				mapItem = MapUtils.getMap(map.getId());
			} else if (Utils.hasDataField(item, MapUtils.IS_DRAWING_FIELD)) {
				Boolean isDrawing = Utils.getBoolean(item, MapUtils.IS_DRAWING_FIELD);
				if (isDrawing == null || !isDrawing) {
					event.setCancelled(true);
					return;
				}
				MapView view = Bukkit.createMap(event.getPlayer().getWorld());
				view.setScale(Scale.FARTHEST);
				mapItem = MapUtils.getMap(view.getId());
				DrawingMap new_map = new DrawingMap(view.getId(), new DrawingInfo(8, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Position.DOWN_NORTH));
				DrawingRenderer renderer = new DrawingRenderer(new_map);
				MapUtils.setRenderer(view, renderer);
				MapFileManager.addMap(new_map);
			} else {
				return;
			}

			event.setCancelled(true);
			event.setUseInteractedBlock(Result.DENY);
			
			if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)
				item.setAmount(item.getAmount() - 1);
			else
				if (player.getInventory().firstEmpty() < 0)
					return;
			
			Utils.giveOrDrop(player, mapItem);
		}
		if (item.getType() == Material.FILLED_MAP) {
			if (!DrawingMapUtils.isDrawingMap(item))
				return;
			
			DrawingMap map = (DrawingMap) MapFileManager.load(MapUtils.getMapId(item));
			if (map == null)
				return;

			event.setCancelled(true);
			map.setInfo(DrawingInfo.buildFrom(player.getLocation()));
			MapFileManager.save(map);
		}
	}
}
