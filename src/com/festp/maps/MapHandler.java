package com.festp.maps;

import java.awt.image.BufferedImage;
import java.util.Arrays;

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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
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

		MapRenderer vanillaRenderer = MapUtils.removeRenderers(event.getMap());
		AbstractRenderer renderer = null;
		if (map instanceof SmallMap) {
			renderer = new SmallRenderer((SmallMap) map);
		} else if (map instanceof DrawingMap) {
			renderer = new DrawingRenderer((DrawingMap) map, vanillaRenderer);
		}
		if (renderer != null) {
			MapUtils.setRenderer(event.getMap(), renderer);
			BufferedImage image = MapFileManager.loadImage(id);
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
				ItemMeta meta = mapItem.getItemMeta();
				meta.setLore(Arrays.asList(new String[] { "Drawing" }));
				mapItem.setItemMeta(meta);
				DrawingMap new_map = new DrawingMap(view.getId(), DrawingInfo.buildFrom(player.getLocation()));
				
				MapRenderer vanillaRenderer = MapUtils.removeRenderers(view);
				DrawingRenderer renderer = new DrawingRenderer(new_map, vanillaRenderer);
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
			if (event.getHand() == EquipmentSlot.OFF_HAND)
				return;
			
			DrawingMap map = (DrawingMap) MapFileManager.load(MapUtils.getMapId(item));
			if (map == null)
				return;

			event.setCancelled(true);
			map.setInfo(DrawingInfo.buildFrom(player.getLocation()));
			MapUtils.getView(map).setWorld(player.getWorld());;
			map.needReset = true;
			MapFileManager.save(map);
		}
	}
}
