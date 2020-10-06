package com.festp.jukebox;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.CraftManager;
import com.festp.Main;
import com.festp.utils.Utils;

public class NoteDiscListener implements Listener {
	@EventHandler
	public void onDiscCraft(CraftItemEvent event) {
		boolean isNoteCraft = true;
		int discCount = 0;
		ItemStack book = null;
		for (ItemStack ingredient : event.getInventory().getMatrix()) {
			if (ingredient == null)
				continue;
			if (ingredient.getType().isRecord()) {
				discCount++;
			} else if (ingredient.getType() == Material.WRITABLE_BOOK || ingredient.getType() == Material.WRITTEN_BOOK) {
				if (book != null) {
					isNoteCraft = false;
				} else {
					book = ingredient;
				}
			}
		}
		ItemStack resultDisc = event.getInventory().getResult();
		if (isNoteCraft && book != null && discCount == 1 && resultDisc.getType().isRecord()) {
			BookMeta bookMeta = (BookMeta) book.getItemMeta();
			if (bookMeta.hasTitle()) {
				ItemMeta meta = resultDisc.getItemMeta();
				meta.setLore(Arrays.asList(bookMeta.getTitle()));
				resultDisc.setItemMeta(meta);
			}
			
			byte[] data = NoteBookParser.genDiscData(book);
			if (data == null) {
				event.getInventory().setResult(null);
				event.setCancelled(true);
				return;
			}
			resultDisc = Utils.setData(resultDisc, NoteDisc.NBT_TAG, data);
			event.getInventory().setResult(resultDisc);
		}
	}
	
	public static void addCrafts(Main plugin) {
    	CraftManager cm = plugin.getCraftManager();
    	Server server = plugin.getServer();
    	

		ItemStack resultDisc = new ItemStack(Material.MUSIC_DISC_BLOCKS, 1);
		ItemMeta meta = resultDisc.getItemMeta();
		meta.setDisplayName("Note Disc");
		resultDisc.setItemMeta(meta);
		MaterialChoice anyDisc = new MaterialChoice(Material.MUSIC_DISC_11, Material.MUSIC_DISC_13, Material.MUSIC_DISC_BLOCKS,
				Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI,
				Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD);
		
		NamespacedKey keyDisc1 = new NamespacedKey(plugin, "note_disc_writable");
    	ShapelessRecipe mapDisc1 = new ShapelessRecipe(keyDisc1, resultDisc);
    	
    	mapDisc1.addIngredient(Material.WRITABLE_BOOK);
    	mapDisc1.addIngredient(anyDisc);
    	
    	cm.addCraftbookRecipe(keyDisc1);
    	server.addRecipe(mapDisc1);
    	
		NamespacedKey keyDisc2 = new NamespacedKey(plugin, "note_disc_written");
    	ShapelessRecipe mapDisc2 = new ShapelessRecipe(keyDisc2, resultDisc);
    	
    	mapDisc2.addIngredient(Material.WRITTEN_BOOK);
    	mapDisc2.addIngredient(anyDisc);
    	
    	cm.addCraftbookRecipe(keyDisc2);
    	server.addRecipe(mapDisc2);
	}
}
