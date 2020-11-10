package com.festp.maps;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.map.CraftMapCanvas;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.map.MapView.Scale;

import com.festp.CraftManager;
import com.festp.DelayedTask;
import com.festp.Main;
import com.festp.TaskList;
import com.festp.utils.Utils;
import com.festp.utils.UtilsWorld;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.WorldMap;

public class MapCraftHandler implements Listener {

	protected static class CraftMapInfo {
		int paperCount = 0;
		int smallCount = 0;
		int emptySmallCount = 0;
		int drawingCount = 0;
		int emptyDrawingCount = 0;
		ItemStack smallMap = null;
		ItemStack drawingMap = null;
		
		public static CraftMapInfo get(ItemStack[] matrix) {
			CraftMapInfo info = new CraftMapInfo();
			for (ItemStack item : matrix)
				if (item != null)
					if (item.getType() == Material.PAPER) {
						info.paperCount++;
					} else if (item.getType() == Material.MAP) {
						if (Utils.hasDataField(item, MapUtils.SCALE_FIELD)) {
							info.emptySmallCount++;
						} else if (Utils.hasDataField(item, MapUtils.IS_DRAWING_FIELD)) {
							info.emptyDrawingCount++;
						}
					} else if (SmallMapUtils.isSmallMap(item)) {
						info.smallCount++;
						info.smallMap = item;
					} else if (DrawingMapUtils.isDrawingMap(item)) {
						info.drawingCount++;
						info.drawingMap = item;
					}
			return info;
		}
	}
	
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event)
	{
		ItemStack[] matrix = event.getInventory().getMatrix();
		CraftMapInfo info = CraftMapInfo.get(matrix);
		
		if (info.smallCount == 1 && info.paperCount == 8) {
			int id = MapUtils.getMapId(info.smallMap);
			ItemStack preMap = SmallMapUtils.getPreExtendedMap(id);
			event.getInventory().setResult(preMap);
		} else if (info.smallCount > 1 || info.emptySmallCount > 0) {
			event.getInventory().setResult(null);
		}
		if (info.emptyDrawingCount > 0 || info.drawingCount > 0) {
			event.getInventory().setResult(null);
		}
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		ItemStack[] matrix = event.getInventory().getMatrix();
		CraftMapInfo info = CraftMapInfo.get(matrix);

		if (info.smallCount == 1 && info.paperCount == 8) {
			int id = MapUtils.getMapId(info.smallMap);
			SmallMap map = (SmallMap) MapFileManager.load(id);
			ItemStack newMap = SmallMapUtils.extendMap(map);
			event.getInventory().setResult(newMap);
		} else if (info.smallCount > 1 || info.emptySmallCount > 0) {
			event.setCancelled(true);
			event.getInventory().setResult(null);
		}
		if (info.emptyDrawingCount > 0 || info.drawingCount > 0) {
			event.setCancelled(true);
			event.getInventory().setResult(null);
		}
	}

	@EventHandler
	public void onCartographyTable(InventoryClickEvent event)
	{
		if (event.isCancelled())
			return;
		if ( !(event.getView().getTopInventory() instanceof CartographyInventory) )
			return;
		
		CartographyInventory inv = (CartographyInventory)event.getView().getTopInventory();
		ItemStack item0 = inv.getItem(0);
		ItemStack item1 = inv.getItem(1);
		if (item0 == null && item1 == null)
			return;
		
		Integer id = MapUtils.getMapId(item0);
		if (id == null) {
			return;
		}
		IMap m = MapFileManager.load(id);
		if (m != null) {
			if (m instanceof SmallMap) {
				onSmallCartography(event, (SmallMap) m);
			} else if (m instanceof DrawingMap) {
				onDrawingCartography(event, (DrawingMap) m);
			} else {
				event.setCancelled(true);
				inv.setItem(2, null);
				((Player)event.getWhoClicked()).updateInventory();
				return;
			}
		}
	}
	
	public boolean copyPixels(IMap mapFrom, MapView viewTo) {
		try {
			Field fieldImage = viewTo.getClass().getDeclaredField("worldMap");
			fieldImage.setAccessible(true);
			WorldMap mapImage = (WorldMap) fieldImage.get(viewTo);

			MapView oldView = MapUtils.getView(mapFrom);
			Field fieldCanvases = oldView.getClass().getDeclaredField("canvases");
			fieldCanvases.setAccessible(true);
			Object preCanvases = fieldCanvases.get(oldView);
			if (!(preCanvases instanceof Map<?, ?>)) {
				Utils.printError("MapCraftHandler couldn't get canvases on locking");
				return false;
			}
			@SuppressWarnings("unchecked")
			Map<MapRenderer, Map<CraftPlayer, CraftMapCanvas>> canvases = (Map<MapRenderer, Map<CraftPlayer, CraftMapCanvas>>) preCanvases;
			byte[] colors = new byte[128*128];
			for (Map<CraftPlayer, CraftMapCanvas> pair : canvases.values())
				for (CraftMapCanvas canvas : pair.values())
				{
					for (int x = 0; x < 128; x++)
						for (int z = 0; z < 128; z++)
							colors[x + 128*z] = canvas.getPixel(x, z);
					break;
				}
			mapImage.colors = colors;
		} catch (Exception e) {
			Utils.printError("Error while creating copy of map #" + mapFrom.getId());
			e.printStackTrace();
		}
		return true;
	}
	
	public void onSmallCartography(InventoryClickEvent event, SmallMap map) {
		CartographyInventory inv = (CartographyInventory)event.getView().getTopInventory();
		ItemStack item0 = inv.getItem(0);
		ItemStack item1 = inv.getItem(1);
		
		int id = map.getId();

		// prepare craft: if 0 or 1 changed
		Runnable pre_task = new Runnable() {
			@Override
			public void run() {
				ItemStack pre_map = MapUtils.getMap(id, true);
				if (inv.contains(Material.PAPER) && map.getScale() / 2 > 1) {
					pre_map = SmallMapUtils.getPreExtendedMap(id);
				}
				else if(inv.contains(Material.GLASS_PANE)) {
					ItemMeta pre_map_meta = pre_map.getItemMeta();
					pre_map_meta.setDisplayName("Map (" + map.getScale() + ":1)");
					pre_map_meta.setLore(Lists.asList("", new String[] {ChatColor.GRAY+"Locked"}));
					pre_map.setItemMeta(pre_map_meta);
				}
				if (inv.contains(Material.PAPER) || inv.contains(Material.GLASS_PANE)) {
					inv.setItem(2, pre_map);
				}
				Runnable update_task = new Runnable() { @Override
					public void run() {
						for (HumanEntity human : inv.getViewers())
							((Player)human).updateInventory();
					} };
				DelayedTask task = new DelayedTask(1, update_task);
				TaskList.add(task);
			} };
		DelayedTask task = new DelayedTask(1, pre_task);
		TaskList.add(task);
		
		//craft: if 2 moved/dropped
		if (inv == event.getClickedInventory() && event.getSlot() == 2 &&
				(event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT
				|| event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF
				|| event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME
				|| (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP)
					&& event.getView().getBottomInventory().getItem(event.getHotbarButton()) == null
				|| event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
					&& MapUtils.getEmptySlot(event.getWhoClicked().getInventory()) >= 0) )
		{
			ItemStack map_item = inv.getItem(2);
			// extending
			if (inv.contains(Material.PAPER))
			{
				map_item = SmallMapUtils.extendMap(map);
			}
			// new locked
			else if (inv.contains(Material.GLASS_PANE))
			{
				MapView view = MapUtils.genNewView(map);
				copyPixels(map, view);
				view.setCenterX(0);
				view.setCenterZ(0);
				view.setScale(Scale.CLOSEST);
				view.setLocked(true);
				map_item = MapUtils.getMap(view.getId());
			}

			event.setCancelled(true);
			item0.setAmount(item0.getAmount() - 1);
			item1.setAmount(item1.getAmount() - 1);
			// TODO: try to stack
			if (event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT)
				UtilsWorld.drop(event.getWhoClicked().getEyeLocation(), map_item, 1);
			else if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF
					|| event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME)
				event.setCursor(map_item);
			else if (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP)
				event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), map_item);
			else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				event.getWhoClicked().getInventory().setItem(MapUtils.getEmptySlot(event.getWhoClicked().getInventory()), map_item);
		}
	}
	
	// TODO refactor to avoid code repeating
	public void onDrawingCartography(InventoryClickEvent event, DrawingMap map) {
		CartographyInventory inv = (CartographyInventory)event.getView().getTopInventory();
		ItemStack item0 = inv.getItem(0);
		ItemStack item1 = inv.getItem(1);
		
		int id = map.getId();

		// prepare craft: if 0 or 1 changed
		Runnable pre_task = new Runnable() {
			@Override
			public void run() {
				ItemStack pre_map = MapUtils.getMap(id, false);
				if (inv.contains(Material.PAPER)) {
					pre_map = null;
				} else if (inv.contains(Material.GLASS_PANE)) {
					ItemMeta pre_map_meta = pre_map.getItemMeta();
					pre_map_meta.setLore(Arrays.asList(new String[] {"", ChatColor.GRAY+"Finished"}));
					pre_map.setItemMeta(pre_map_meta);
				}
				if (inv.contains(Material.PAPER) || inv.contains(Material.GLASS_PANE)) {
					inv.setItem(2, pre_map);
				}
				Runnable update_task = new Runnable() { @Override
					public void run() {
						for (HumanEntity human : inv.getViewers())
							((Player)human).updateInventory();
					} };
				DelayedTask task = new DelayedTask(1, update_task);
				TaskList.add(task);
			} };
		DelayedTask task = new DelayedTask(1, pre_task);
		TaskList.add(task);
		
		//craft: if 2 moved/dropped
		if (inv == event.getClickedInventory() && event.getSlot() == 2 &&
				(event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT
				|| event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF
				|| event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME
				|| (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP)
					&& event.getView().getBottomInventory().getItem(event.getHotbarButton()) == null
				|| event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
					&& MapUtils.getEmptySlot(event.getWhoClicked().getInventory()) >= 0) )
		{
			ItemStack mapItem = inv.getItem(2);
			if (inv.contains(Material.PAPER)) { // extending
				mapItem = null;
			} else if (inv.contains(Material.GLASS_PANE)) { // locking
				MapView view = MapUtils.getView(map);
				copyPixels(map, view);
				view.setCenterX(0);
				view.setCenterZ(0);
				view.setScale(Scale.CLOSEST);
				view.setLocked(true);
				MapFileManager.delete(map);
				for (int i = view.getRenderers().size() - 1; i >= 0; i--) {
					MapRenderer renderer = view.getRenderers().get(i);
					if (renderer instanceof DrawingRenderer) {
						DrawingRenderer drawingRend = (DrawingRenderer) renderer;
						if (drawingRend.saveTask != null) {
							drawingRend.saveTask.terminate();
						}
						view.removeRenderer(drawingRend);
						view.addRenderer(drawingRend.vanillaRenderer);
					}
				}
				mapItem = MapUtils.getMap(view.getId());
			}

			event.setCancelled(true);
			item0.setAmount(item0.getAmount() - 1);
			item1.setAmount(item1.getAmount() - 1);
			// TODO: try to stack
			if (event.getAction() == InventoryAction.DROP_ALL_SLOT || event.getAction() == InventoryAction.DROP_ONE_SLOT)
				UtilsWorld.drop(event.getWhoClicked().getEyeLocation(), mapItem, 1);
			else if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF
					|| event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME)
				event.setCursor(mapItem);
			else if (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP)
				event.getWhoClicked().getInventory().setItem(event.getHotbarButton(), mapItem);
			else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				event.getWhoClicked().getInventory().setItem(MapUtils.getEmptySlot(event.getWhoClicked().getInventory()), mapItem);
		}
	}

	public static void addCrafts(Main plugin)
	{
    	CraftManager cm = plugin.getCraftManager();
    	Server server = plugin.getServer();
    	
		ItemStack result_8 = new ItemStack(Material.MAP, 1);
		ItemMeta meta = result_8.getItemMeta();
		meta.setDisplayName("Map (8:1)");
		result_8.setItemMeta(meta);
		result_8 = Utils.setData(result_8, MapUtils.SCALE_FIELD, 8);
		
		NamespacedKey key_8 = new NamespacedKey(plugin, "map_8");
    	ShapedRecipe map_8 = new ShapedRecipe(key_8, result_8);
    	
    	map_8.shape(new String[] {" P ", "PCP", " P "});
    	map_8.setIngredient('P', Material.PAPER);
    	map_8.setIngredient('C', Material.COMPASS);
    	
    	cm.addCraftbookRecipe(key_8);
    	server.addRecipe(map_8);
    	

		ItemStack result_draw = new ItemStack(Material.MAP, 1);
		result_draw = Utils.setData(result_draw, MapUtils.IS_DRAWING_FIELD, 8);
		meta = result_draw.getItemMeta();
		meta.setLore(Arrays.asList(new String[] { "Drawing" }));
		result_draw.setItemMeta(meta);
		
		NamespacedKey key_draw = new NamespacedKey(plugin, "map_draw");
    	ShapedRecipe map_draw = new ShapedRecipe(key_draw, result_draw);
    	
    	RecipeChoice dyeChoice = new RecipeChoice.MaterialChoice(Material.BLACK_DYE, Material.BLUE_DYE, Material.BROWN_DYE, Material.CYAN_DYE,
    			Material.GRAY_DYE, Material.GREEN_DYE, Material.LIGHT_BLUE_DYE, Material.LIGHT_GRAY_DYE, Material.LIME_DYE, Material.MAGENTA_DYE,
    			Material.ORANGE_DYE, Material.PINK_DYE, Material.PURPLE_DYE, Material.RED_DYE, Material.WHITE_DYE, Material.YELLOW_DYE);
    	map_draw.shape(new String[] {" P ", "DBD", "DDD"});
    	map_draw.setIngredient('P', Material.PAPER);
    	map_draw.setIngredient('B', Material.BOWL);
    	map_draw.setIngredient('D', dyeChoice);
    	
    	cm.addCraftbookRecipe(key_draw);
    	server.addRecipe(map_draw);
	}
}
