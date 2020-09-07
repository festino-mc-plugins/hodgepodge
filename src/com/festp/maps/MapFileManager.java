package com.festp.maps;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import com.festp.Main;
import com.festp.utils.Utils;

public class MapFileManager {
	private static final String DIR = Main.getPath() + Main.mapsdir + System.getProperty("file.separator");
	private static final String IMG_OLD_FORMAT = "bmp";
	private static final String IMG_FORMAT = "png";
	private static final Color DEFAULT_SAVE_COLOR = Color.MAGENTA;
	
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
		File imageFile = new File(DIR + id + "." + IMG_OLD_FORMAT);
		if (!imageFile.exists())
			imageFile = new File(DIR + id + "." + IMG_FORMAT);
		
		if (!imageFile.exists())
			return null;
		
		try {
			BufferedImage image = ImageIO.read(imageFile);
			if (imageFile.getName().endsWith(IMG_OLD_FORMAT)) {
				imageFile.delete();
				saveImage(id, image);
			}
			return image;
		} catch (IOException e) {
			Utils.printError("Error while loading image '" + imageFile.getName() +"'.");
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
	
	@SuppressWarnings("deprecation")
	public static void saveMapCanvas(IMap map, MapCanvas canvas) {
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB_PRE);
		
		for (int x = 0; x < 128; x++)
			for (int z = 0; z < 128; z++)
			{
				byte map_color = canvas.getPixel(x, z);
				Color color = DEFAULT_SAVE_COLOR;
				try {
					color = MapPalette.getColor(map_color);
				} catch (Exception e) {
					Utils.printError("Map saving color error: color id " + map_color);
				}
				if (0 <= map_color && map_color < PaletteUtils.SHADES_COUNT) {
					// FFFF FFFF FF00 0000
					color = new Color(0, 0, 0, 0);
				}
				image.setRGB(x, z, color.getRGB());
			}
		
		MapFileManager.saveImage(map.getId(), image);
	}
}
