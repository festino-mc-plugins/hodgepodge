package com.festp.maps;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.festp.DelayedTask;
import com.festp.TaskList;
import com.festp.utils.Utils;

public abstract class AbstractRenderer extends MapRenderer {
	
	private static final Color DEFAULT_SAVE_COLOR = Color.MAGENTA;

	static final int SAVE_TICKS = 20 * 10;
	private int save_ticks = 0;
	
	boolean init;
	final IMap map;
	BufferedImage image;
	DelayedTask saveTask = null;
	
	public AbstractRenderer(IMap map) {
		this.map = map;
	}
	
	public final void renderImage(BufferedImage image) {
		init = true;
		this.image = image;
	}
	
	protected abstract void renderSpecific(MapView view, MapCanvas canvas, Player player);

	@Override
	public final void render(MapView view, MapCanvas canvas, Player player)
	{
		if (init) {
			canvas.drawImage(0, 0, image);
			for (int x = 0; x < 128; x++)
				for (int z = 0; z < 128; z++)
					if (image.getRGB(0, 0) == 0)
						canvas.setPixel(x, z, (byte) 0);
			image = null;
			init = false;
			return;
		}
		
		renderSpecific(view, canvas, player);

		save_ticks++;
		if (save_ticks >= SAVE_TICKS)
		{
			save_ticks = 0;
			save(canvas);
			
			if (saveTask != null) {
				saveTask.terminate();
			}
			saveTask = new DelayedTask(SAVE_TICKS * 2, new Runnable() {
				@Override public void run() {
					save(canvas);
				}
			});
			TaskList.add(saveTask);
		}
	}
	
	public final void save(MapCanvas canvas) {
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
		
		for (int x = 0; x < 128; x++)
			for (int z = 0; z < 128; z++)
			{
				byte map_color = canvas.getPixel(x, z);
				Color color = DEFAULT_SAVE_COLOR;
				try {
					color = MapPalette.getColor(map_color);
				} catch (Exception e) {
					Utils.printError("Map saving color error: color " + map_color);
				}
				image.setRGB(x, z, color.getRGB());
			}
		
		SmallMapFileManager.saveImage(map.getId(), image);
	}
}
