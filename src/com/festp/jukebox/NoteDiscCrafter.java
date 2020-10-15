package com.festp.jukebox;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.CraftManager;
import com.festp.DelayedTask;
import com.festp.Main;
import com.festp.TaskList;
import com.festp.utils.Utils;

public class NoteDiscCrafter implements Listener {
	@EventHandler
	public void onDiscCraft(CraftItemEvent event) {
		if (event.isCancelled()) {
			return;
		}
		boolean isNoteCraft = true;
		int coalCount = 0;
		ItemStack book = null;
		int bookIndex = -1;
		for (int i = 0; i < event.getInventory().getMatrix().length; i++) {
			ItemStack ingredient = event.getInventory().getMatrix()[i];
			if (ingredient == null)
				continue;
			if (ingredient.getType() == Material.COAL_BLOCK) {
				coalCount++;
			} else if (ingredient.getType() == Material.WRITABLE_BOOK || ingredient.getType() == Material.WRITTEN_BOOK) {
				if (book != null) {
					isNoteCraft = false;
				} else {
					book = ingredient;
					bookIndex = i;
				}
			}
		}
		ItemStack resultDisc = event.getInventory().getResult();
		if (isNoteCraft && book != null && coalCount == 6 && resultDisc != null && resultDisc.getType().isRecord()) {
			BookMeta bookMeta = (BookMeta) book.getItemMeta();
			if (bookMeta.hasTitle()) {
				ItemMeta meta = resultDisc.getItemMeta();
				meta.setLore(Arrays.asList(bookMeta.getTitle()));
				resultDisc.setItemMeta(meta);
			}
			
			byte[] data = null;
			try {
				data = NoteBookParser.genDiscData(book);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (data == null) {
				event.getInventory().setResult(null);
				event.setCancelled(true);
				return;
			}
			resultDisc = Utils.setData(resultDisc, NoteDisc.NBT_TAG, data);
			event.getInventory().setResult(resultDisc);
			
			final int finalIndex = bookIndex + 1;
			final ItemStack finalBook = book.clone();
			final Player finalPlayer = (Player) event.getWhoClicked();
			final InventoryView view = event.getView();
			final Location finalLoc = view.getTopInventory().getLocation().add(0.5, 1, 0.5);
			TaskList.add(new DelayedTask(0, new Runnable() { // TODO move to Utils
				@Override
				public void run() {
					if (finalPlayer.isOnline()) {
						if (finalPlayer.getOpenInventory() == view) {
							view.setItem(finalIndex, finalBook);
							finalPlayer.updateInventory();
						} else {
							Utils.giveOrDrop(finalPlayer.getInventory(), finalBook);
						}
					} else {
						finalLoc.getWorld().dropItem(finalLoc, finalBook);
					}
				}
			}));
		}
	}
	
	public static void addCrafts(Main plugin) {
    	CraftManager cm = plugin.getCraftManager();
    	Server server = plugin.getServer();
    	

		ItemStack resultDisc = new ItemStack(Material.MUSIC_DISC_BLOCKS, 1);
		ItemMeta meta = resultDisc.getItemMeta();
		meta.setDisplayName("Note Disc");
		resultDisc.setItemMeta(meta);
		//MaterialChoice anyDisc = new MaterialChoice(Material.MUSIC_DISC_11, Material.MUSIC_DISC_13, Material.MUSIC_DISC_BLOCKS,
		//		Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_CHIRP, Material.MUSIC_DISC_FAR, Material.MUSIC_DISC_MALL, Material.MUSIC_DISC_MELLOHI,
		//		Material.MUSIC_DISC_STAL, Material.MUSIC_DISC_STRAD, Material.MUSIC_DISC_WAIT, Material.MUSIC_DISC_WARD);
		
		NamespacedKey keyDisc1 = new NamespacedKey(plugin, "note_disc_writable");
    	ShapelessRecipe mapDisc1 = new ShapelessRecipe(keyDisc1, resultDisc);
    	
    	mapDisc1.addIngredient(6, Material.COAL_BLOCK);
    	mapDisc1.addIngredient(Material.WRITABLE_BOOK);
    	
    	cm.addCraftbookRecipe(keyDisc1);
    	server.addRecipe(mapDisc1);
    	
		NamespacedKey keyDisc2 = new NamespacedKey(plugin, "note_disc_written");
    	ShapelessRecipe mapDisc2 = new ShapelessRecipe(keyDisc2, resultDisc);
    	
    	mapDisc2.addIngredient(6, Material.COAL_BLOCK);
    	mapDisc2.addIngredient(Material.WRITTEN_BOOK);
    	
    	cm.addCraftbookRecipe(keyDisc2);
    	server.addRecipe(mapDisc2);
	}
}
