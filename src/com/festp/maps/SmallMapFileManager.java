package com.festp.maps;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.festp.Main;
import com.festp.utils.Utils;

public class SmallMapFileManager {
	private static final String DIR = Main.getPath() + Main.mapsdir + System.getProperty("file.separator");
	private static final String IMG_FORMAT = "bmp";
	
	private static List<SmallMap> maps = new ArrayList<>(); 
	
	/** on new map craft */
	public static void addMap(SmallMap map)
	{
		save(map);
		maps.add(map);
	}
	
	/** on map loading */
	public static SmallMap load(int id)
	{
		for (SmallMap map : maps)
			if (map.getId() == id)
				return map;
		
		SmallMap map = null;
		File file = new File(DIR + id + ".dat");
		if (file.exists())
		{
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(file);
			Integer scale = (Integer) ymlFormat.get("scale");
			Integer start_x = (Integer) ymlFormat.get("start_x");
			Integer start_z = (Integer) ymlFormat.get("start_z");
			if (scale != null && start_x != null && start_z != null)
			{
				map = new SmallMap(id, scale, start_x, start_z);
				maps.add(map);
			}
		}
		
		return map;
	}
	
	public static void save(SmallMap map)
	{
		File file = new File(DIR, map.getId() + ".dat");
		try {
			FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(file);
			ymlFormat.set("scale", map.getScale());
			ymlFormat.set("start_x", map.getX());
			ymlFormat.set("start_z", map.getZ());
			ymlFormat.save(file);
		} catch (IOException e) {
			System.err.println("Error while creating map file '" + map.getId() + ".dat'.");
			e.printStackTrace();
		}
	}
	
	public static BufferedImage loadImage(int id)
	{
		File image_file = new File(DIR + id + "." + IMG_FORMAT);
		if (!image_file.exists())
			return null;
		
		try {
			BufferedImage image = ImageIO.read(image_file);
			return image;
		} catch (IOException e) {
			Utils.printError("Error while loading image '" + id + "." + IMG_FORMAT +"'.");
			return null;
		}
	}
	
	public static void saveImage(int id, BufferedImage image)
	{
		File image_file = new File(DIR + id + "." + IMG_FORMAT);
		
		try {
			ImageIO.write(image, IMG_FORMAT, image_file);
		} catch (IOException e) {
			Utils.printError("Error while saving image '" + id + "." + IMG_FORMAT +"'.");
		}
	}
}
