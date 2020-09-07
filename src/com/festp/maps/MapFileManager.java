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

public class MapFileManager {
	private static final String DIR = Main.getPath() + Main.mapsdir + System.getProperty("file.separator");
	private static final String IMG_FORMAT = "bmp";
	
	private static List<IMap> maps = new ArrayList<>(); 
	
	/** on new map craft */
	public static void addMap(IMap map)
	{
		save(map);
		maps.add(map);
	}
	
	/** on map loading */
	public static IMap load(int id)
	{
		for (IMap map : maps)
			if (map.getId() == id)
				return map;
		
		IMap map = null;
		File file = new File(DIR + id + ".dat");
		if (file.exists())
		{
			try {
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(file);
				Object objSmall = ymlFormat.get("start_x");
				if (objSmall != null) {
					Integer start_x = (Integer) objSmall;
					Integer start_z = (Integer) ymlFormat.get("start_z");
					Integer scale = (Integer) ymlFormat.get("scale");
					map = new SmallMap(id, scale, start_x, start_z);
					maps.add(map);
				}
				Object objDrawing = ymlFormat.get("position");
				if (objDrawing != null) {
					Integer xCenter = (Integer) ymlFormat.get("x_center");
					Integer yCenter = (Integer) ymlFormat.get("y_center");
					Integer zCenter = (Integer) ymlFormat.get("z_center");
					Integer scale = (Integer) ymlFormat.get("scale");
					Position state = Position.valueOf((String) objDrawing);
					map = new DrawingMap(id, new DrawingInfo(scale, xCenter, yCenter, zCenter, state));
					maps.add(map);
				}
			} catch (Exception e) {
				System.out.println("[WARN] Couldn't load map #" + id + "!");
			}
		}
		
		return map;
	}
	
	public static void save(IMap map)
	{
		File file = new File(DIR, map.getId() + ".dat");
		try {
			if (map instanceof SmallMap) {
				SmallMap smallMap = (SmallMap) map;
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(file);
				ymlFormat.set("scale", smallMap.getScale());
				ymlFormat.set("start_x", smallMap.getX());
				ymlFormat.set("start_z", smallMap.getZ());
				ymlFormat.save(file);
			}
			if (map instanceof DrawingMap) {
				DrawingMap drawingMap = (DrawingMap) map;
				FileConfiguration ymlFormat = YamlConfiguration.loadConfiguration(file);
				ymlFormat.set("scale", drawingMap.getScale());
				ymlFormat.set("x_center", drawingMap.getX());
				ymlFormat.set("y_center", drawingMap.getY());
				ymlFormat.set("z_center", drawingMap.getZ());
				ymlFormat.set("position", drawingMap.getState().name());
				ymlFormat.save(file);
			}
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
