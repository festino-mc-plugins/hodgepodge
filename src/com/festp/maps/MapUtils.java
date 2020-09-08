package com.festp.maps;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.festp.utils.Utils;
import com.google.common.collect.Lists;

public class MapUtils {
	
	public static final String SCALE_FIELD = "map_scale";
	public static final String IS_DRAWING_FIELD = "is_drawing";
	public static final boolean USE_SCALE_NAMES = false; // bad for frames, good for understanding
	
	public static int getEmptySlot(PlayerInventory inv)
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
	
	public static void setRenderer(MapView view, MapRenderer mapRenderer) {
		for (MapRenderer m : view.getRenderers())
			view.removeRenderer(m);
		view.addRenderer(mapRenderer);
	}
	
	public static MapView genNewView(IMap map)
	{
		MapView old_view = Bukkit.getMap(map.getId());
		MapView view = Bukkit.createMap(old_view.getWorld());
		return view;
	}
	
	public static MapView getView(IMap map)
	{
		MapView view = Bukkit.getMap(map.getId());
		return view;
	}
	
	/* uses USE_SCALE_NAMES as second argument */
	public static ItemStack getMap(int id) {
		return getMap(id, USE_SCALE_NAMES);
	}
	
	public static ItemStack getMap(int id, boolean scale_name)
	{
		ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
		item = Utils.setData(item, "map", id);
		if (SmallMapUtils.isSmallMap(item)) {
			ItemMeta meta = item.getItemMeta();
			
			SmallMap map = (SmallMap) MapFileManager.load(getMapId(item));
			String[] lore = new String[] { "Scaling at " + map.getScale() + ":1" };
			meta.setLore(Lists.asList("", lore));
			if (scale_name)
				meta.setDisplayName("Map (" + map.getScale() + ":1)");
			
			item.setItemMeta(meta);
		}
		return item;
	}
	
	public static Integer getMapId(ItemStack item)
	{
		if (item == null || item.getType() != Material.FILLED_MAP)
			return null;
		return Utils.getInt(item, "map");
	}
}
