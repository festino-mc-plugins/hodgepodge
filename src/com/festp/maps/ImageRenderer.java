package com.festp.maps;

import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class ImageRenderer extends MapRenderer {
	
	BufferedImage image;
	
	public ImageRenderer(BufferedImage image) {
		this.image = image;
	}

	@Override
	public void render(MapView view, MapCanvas canvas, Player player) {
		canvas.drawImage(0, 0, image);
	}

}
