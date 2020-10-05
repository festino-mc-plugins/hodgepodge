package com.festp.maps;

import org.bukkit.inventory.ItemStack;

public class DrawingMapUtils {
	
	public static boolean isDrawingMap(int id)
	{
		IMap map = MapFileManager.load(id);
		return map != null && map instanceof DrawingMap;
	}
	public static boolean isDrawingMap(ItemStack item)
	{
		Integer id = MapUtils.getMapId(item);
		if (id == null)
			return false;
		return isDrawingMap(id);
	}
}
