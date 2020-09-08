package com.festp.maps;

import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.map.CraftMapCanvas;
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
import net.minecraft.server.v1_16_R2.WorldMap;

public class MapCraftHandler implements Listener {

	
	@EventHandler
	public void onPrepareCraft(PrepareItemCraftEvent event)
	{
		ItemStack[] matrix = event.getInventory().getMatrix();
		int paper = 0, small_maps = 0, empty_small_maps = 0;
		ItemStack small_map = null;
		for (ItemStack item : matrix)
			if (item != null)
				if (item.getType() == Material.PAPER)
					paper++;
				else if (item.getType() == Material.MAP) {
					if (Utils.hasDataField(item, MapUtils.SCALE_FIELD))
						empty_small_maps++;
				}
				else if (SmallMapUtils.isSmallMap(item)) {
					small_maps++;
					small_map = item;
				}
		
		if (small_maps == 1 && paper == 8)
		{
			int id = MapUtils.getMapId(small_map);
			ItemStack pre_map = SmallMapUtils.getPreExtendedMap(id);
			
			event.getInventory().setResult(pre_map);
		}
		else if (small_maps > 1 || empty_small_maps > 0)
			event.getInventory().setResult(null);
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		ItemStack res = event.getRecipe().getResult();
		if (Utils.hasDataField(res, MapUtils.IS_DRAWING_FIELD)) {
			Boolean isDrawing = Utils.getBoolean(res, MapUtils.IS_DRAWING_FIELD);
			if (isDrawing != null && isDrawing) {
				// TODO remove 224 99 -1039
				MapView view = Bukkit.createMap(event.getWhoClicked().getWorld());
				view.setScale(Scale.FARTHEST);
				res = MapUtils.getMap(view.getId());
				event.getInventory().setResult(res);
				DrawingMap new_map = new DrawingMap(view.getId(), new DrawingInfo(8, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Position.DOWN_NORTH));
				DrawingRenderer renderer = new DrawingRenderer(new_map);
				MapUtils.setRenderer(view, renderer);
				MapFileManager.addMap(new_map);
				
				
			} else {
				event.setCancelled(true);
			}
			return;
		}
		
		ItemStack[] matrix = event.getInventory().getMatrix();
		int paper = 0, empty_small_maps = 0, small_maps = 0;
		ItemStack small_map = null;
		for (ItemStack item : matrix)
			if (item != null)
				if (item.getType() == Material.PAPER)
					paper++;
				else if (item.getType() == Material.MAP) {
					if (Utils.hasDataField(item, MapUtils.SCALE_FIELD))
						empty_small_maps++;
				}
				else if (SmallMapUtils.isSmallMap(item)) {
					small_maps++;
					small_map = item;
				}
		
		if (small_maps == 1 && paper == 8)
		{
			int id = MapUtils.getMapId(small_map);
			SmallMap map = (SmallMap) MapFileManager.load(id);
			ItemStack pre_map = SmallMapUtils.extendMap(map);
			
			event.getInventory().setResult(pre_map);
		}
		else if (small_maps > 1 || empty_small_maps > 0) {
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
		if (item0 == null || item1 == null)
			return;
		
		int id = MapUtils.getMapId(item0);
		IMap m = MapFileManager.load(id);
		if (m == null && !(m instanceof SmallMap)) {
			event.setCancelled(true);
		}
		if (!SmallMapUtils.isSmallMap(item0))
			return;
		SmallMap map = (SmallMap) m;
		
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
			// locking
			else if (inv.contains(Material.GLASS_PANE))
			{
				MapView view = MapUtils.genNewView(map);

				try {
					Field field_image = view.getClass().getDeclaredField("worldMap");
					field_image.setAccessible(true);
					WorldMap map_image = (WorldMap)field_image.get(view);

					MapView old_view = MapUtils.getView(map);
					Field field_canvases = old_view.getClass().getDeclaredField("canvases");
					field_canvases.setAccessible(true);
					Map<MapRenderer, Map<CraftPlayer, CraftMapCanvas>> canvases = (Map<MapRenderer, Map<CraftPlayer, CraftMapCanvas>>)field_canvases.get(old_view);
					byte[] colors = new byte[128*128];
					for (Map<CraftPlayer, CraftMapCanvas> pair : canvases.values())
						for (CraftMapCanvas canvas : pair.values())
						{
							for (int x = 0; x < 128; x++)
								for (int z = 0; z < 128; z++)
									colors[x + 128*z] = canvas.getPixel(x, z);
							break;
						}
					map_image.colors = colors;
				} catch (Exception e) {
					System.out.print("Error while creating locked copy of map #" + map.getId());
					e.printStackTrace();
				}
				
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
    	

		ItemStack result_draw = new ItemStack(Material.FILLED_MAP, 1);
		result_draw = Utils.setData(result_draw, MapUtils.IS_DRAWING_FIELD, 8);
		
		NamespacedKey key_draw = new NamespacedKey(plugin, "map_draw");
    	ShapedRecipe map_draw = new ShapedRecipe(key_draw, result_draw);
    	
    	map_draw.shape(new String[] {"   ", " P ", "   "});
    	map_draw.setIngredient('P', Material.PAPER);
    	
    	cm.addCraftbookRecipe(key_draw);
    	server.addRecipe(map_draw);
	}
}
