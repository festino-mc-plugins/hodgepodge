package com.festp.maps;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_14_R1.map.CraftMapCanvas;
import org.bukkit.craftbukkit.v1_14_R1.map.CraftMapRenderer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
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
import net.minecraft.server.v1_14_R1.WorldMap;

public class SmallMapManager implements Listener {
	/** New scales (8:1, 4:1, 2:1)
	 *  	craft: item tag
	 *  MapInitializeEvent: cancel, create and store ID+data(scale, pos)
	 *  MapRenderer
	 *  Only cloning remains*/

	static final String SCALE_FIELD = "map_scale";
	private static final boolean USE_SCALE_NAMES = false; // bad for frames, good for understanding
	private SmallMapFileManager file_manager;
	
	public SmallMapManager()
	{
		file_manager = new SmallMapFileManager();
	}

	/** load last session map canvas */
	@EventHandler
	public void onMapLoad(MapInitializeEvent event)
	{
		int id = event.getMap().getId();
		SmallMap map = file_manager.load(id);
		if (map != null)
		{
			SmallRenderer map_renderer = new SmallRenderer(map);
			BufferedImage image = file_manager.loadImage(id);
			setRenderer(event.getMap(), map_renderer);
			if (image != null)
			{
				map_renderer.renderImage(image);
			}
		}
	}

	/** init new small map */
	@EventHandler
	public void onPlayerInitMap(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (event.hasItem() && event.getItem().getType() == Material.MAP)
		{
			if (!Utils.hasDataField(event.getItem(), SCALE_FIELD))
				return;
			
			int scale = Utils.getInt(event.getItem(), SCALE_FIELD);
			
			if (event.getPlayer().getGameMode() == GameMode.SURVIVAL || event.getPlayer().getGameMode() == GameMode.ADVENTURE)
				event.getItem().setAmount(event.getItem().getAmount() - 1);
			else
				if (event.getPlayer().getInventory().firstEmpty() < 0)
					return;
			
			event.setCancelled(true);
			SmallMap map = genSmallMap(event.getPlayer().getLocation(), scale);
			ItemStack map_item = getMap(map.getId(), USE_SCALE_NAMES);
			Utils.giveOrDrop(event.getPlayer(), map_item);
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
		
		if (!isSmallMap(item0))
			return;
		int id = getMapId(item0);
		SmallMap map = file_manager.load(id);
		
		// prepare craft: if 0 or 1 changed
		Runnable pre_task = new Runnable() {
			@Override
			public void run() {
				ItemStack pre_map = getMap(id, true);
				if (inv.contains(Material.PAPER) && map.getScale() / 2 > 1) {
					pre_map = getPreExtandedMap(id);
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
				|| event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && getEmptySlot(event.getWhoClicked().getInventory()) >= 0) )
		{
			ItemStack map_item = inv.getItem(2);
			// extending
			if (inv.contains(Material.PAPER))
			{
				map_item = extendMap(map);
			}
			// locking
			else if (inv.contains(Material.GLASS_PANE))
			{
				MapView view = genNewView(map);

				try {
					Field field_image = view.getClass().getDeclaredField("worldMap");
					field_image.setAccessible(true);
					WorldMap map_image = (WorldMap)field_image.get(view);

					MapView old_view = getView(map);
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
				map_item = getMap(view.getId(), USE_SCALE_NAMES);
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
				event.getWhoClicked().getInventory().setItem(getEmptySlot(event.getWhoClicked().getInventory()), map_item);
		}
	}
	
	public ItemStack extendMap(SmallMap map)
	{
		if (map.getScale() / 2 > 1)
		{
			MapView view = getView(map);
			Location loc = new Location(view.getWorld(), map.getX(), 0, map.getZ());
			SmallMap new_map = genSmallMap(loc, map.getScale() / 2);
			return getMap(new_map.getId(), false);
		}
		else
		{
			MapView view = genNewView(map);
			int x = (int) Math.floor(map.getX() / 128.0) * 128;
			int z = (int) Math.floor(map.getZ() / 128.0) * 128;
			view.setCenterX(x + 64);
			view.setCenterZ(z + 64);
			view.setScale(Scale.CLOSEST); // 1:1
			return getMap(view.getId(), USE_SCALE_NAMES);
		}
	}
	
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
					if (Utils.hasDataField(item, SCALE_FIELD))
						empty_small_maps++;
				}
				else if (isSmallMap(item)) {
					small_maps++;
					small_map = item;
				}
		
		if (small_maps == 1 && paper == 8)
		{
			int id = getMapId(small_map);
			ItemStack pre_map = getPreExtandedMap(id);
			
			event.getInventory().setResult(pre_map);
		}
		else if (small_maps > 1 || empty_small_maps > 0)
			event.getInventory().setResult(null);
	}

	@EventHandler
	public void onCraft(CraftItemEvent event) {
		ItemStack[] matrix = event.getInventory().getMatrix();
		int paper = 0, empty_small_maps = 0, small_maps = 0;
		ItemStack small_map = null;
		for (ItemStack item : matrix)
			if (item != null)
				if (item.getType() == Material.PAPER)
					paper++;
				else if (item.getType() == Material.MAP) {
					if (Utils.hasDataField(item, SCALE_FIELD))
						empty_small_maps++;
				}
				else if (isSmallMap(item)) {
					small_maps++;
					small_map = item;
				}
		
		if (small_maps == 1 && paper == 8)
		{
			int id = getMapId(small_map);
			SmallMap map = file_manager.load(id);
			ItemStack pre_map = extendMap(map);
			
			event.getInventory().setResult(pre_map);
		}
		else if (small_maps > 1 || empty_small_maps > 0) {
			event.setCancelled(true);
			event.getInventory().setResult(null);
		}
	}
	
	public int getEmptySlot(PlayerInventory inv)
	{
		ItemStack[] slots = inv.getStorageContents();
		
		for (int i = 8; i >= 0; i--)
			if (slots[i] == null)
				return i;
		for (int i = 35; i >= 9; i--)
			if (slots[i] == null)
				return i;
		return -1;
	}
	
	/** create new map and attach renderer*/
	public SmallMap genSmallMap(Location l, int scale)
	{
		MapView view = Bukkit.createMap(l.getWorld());
		view.setScale(Scale.CLOSEST);

		int ratio = 128 / scale;
		int start_x = (int)Math.floor(l.getBlockX() / (float)ratio) * ratio;
		int start_z = (int)Math.floor(l.getBlockZ() / (float)ratio) * ratio;
		SmallMap new_map = new SmallMap(view.getId(), scale, start_x, start_z);
		SmallRenderer renderer = new SmallRenderer(new_map);
		setRenderer(view, renderer);
		file_manager.addMap(new_map);
		
		return new_map;
	}
	
	public MapView genNewView(SmallMap map)
	{
		MapView old_view = Bukkit.getMap(map.getId());
		MapView view = Bukkit.createMap(old_view.getWorld());
		return view;
	}
	
	public MapView getView(SmallMap map)
	{
		MapView view = Bukkit.getMap(map.getId());
		return view;
	}
	
	public static void setRenderer(MapView view, MapRenderer map_renderer) {
		for (MapRenderer m : view.getRenderers())
			view.removeRenderer(m);
		view.addRenderer(map_renderer);
	}
	
	public static ItemStack getMap(int id, boolean scale_name)
	{
		ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
		item = Utils.setData(item, "map", id);
		if (isSmallMap(item))
		{
			ItemMeta meta = item.getItemMeta();
			
			SmallMap map = SmallMapFileManager.load(getMapId(item));
			String[] lore = new String[] { "Scaling at " + map.getScale() + ":1" };
			meta.setLore(Lists.asList("", lore));
			if (scale_name)
				meta.setDisplayName("Map (" + map.getScale() + ":1)");
			
			item.setItemMeta(meta);
		}
		return item;
	}
	public static ItemStack getPreExtandedMap(int id)
	{
		SmallMap map = SmallMapFileManager.load(id);
		ItemStack pre_map = getMap(id, true);
		ItemMeta pre_map_meta = pre_map.getItemMeta();
		int scale = map.getScale() / 2;
		pre_map_meta.setDisplayName("Map (" + scale + ":1)");
		String[] lore = new String[] { "Scaling at " + scale + ":1" };
		pre_map_meta.setLore(Lists.asList("", lore));
		pre_map.setItemMeta(pre_map_meta);
		
		return pre_map;
	}
	
	public static Integer getMapId(ItemStack item)
	{
		if (item == null || item.getType() != Material.FILLED_MAP)
			return null;
		return Utils.getInt(item, "map");
	}
	
	public static boolean isSmallMap(int id)
	{
		SmallMap map = SmallMapFileManager.load(id);
		return map != null;
	}
	public static boolean isSmallMap(ItemStack item)
	{
		Integer id = getMapId(item);
		if (id == null)
			return false;
		return isSmallMap(id);
	}
	
	public static void addCrafts(Main plugin)
	{
    	CraftManager cm = plugin.getCraftManager();
    	Server server = plugin.getServer();
    	
		ItemStack result = new ItemStack(Material.MAP, 1);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName("Map (8:1)");
		result.setItemMeta(meta);
		result = Utils.setData(result, SCALE_FIELD, 8);
		
		NamespacedKey key = new NamespacedKey(plugin, "map_8");
    	ShapedRecipe map_8 = new ShapedRecipe(key, result);
    	
    	map_8.shape(new String[] {" P ", "PCP", " P "});
    	map_8.setIngredient('P', Material.PAPER);
    	map_8.setIngredient('C', Material.COMPASS);
    	
    	cm.addCraftbookRecipe(key);
    	server.addRecipe(map_8);
	}
}
