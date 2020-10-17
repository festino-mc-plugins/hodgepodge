package com.festp.jukebox;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.festp.CraftManager;
import com.festp.DelayedTask;
import com.festp.Main;
import com.festp.Pair;
import com.festp.TaskList;
import com.festp.utils.Utils;

public class NoteDiscCrafter implements Listener {
	private static final int OFFSET = 5;
	private static final int MAX_ERROR_LEN = 20;
	
	@EventHandler
	public void onDiscPreCraft(PrepareItemCraftEvent event) {
		boolean isNoteCraft = true; // TODO avoid code copy
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
				data = NoteBookParser.genDiscData(book); // heavy
			} catch (NoteFormatException e) {
				resultDisc = genErrorDisc(e, "");
			} catch (Exception e) {
				e.printStackTrace();
				resultDisc = genErrorDisc(null, "UNEXPECTED ERROR, CALL FEST");
			}
			if (data == null) {
				event.getInventory().setResult(resultDisc);
			}
		}
	}
	
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
			} catch (NoteFormatException e) {
				resultDisc = genErrorDisc(e, "");
			} catch (Exception e) {
				e.printStackTrace();
				resultDisc = genErrorDisc(null, "UNEXPECTED ERROR, CALL FEST");
			}
			if (data == null) {
				//event.getInventory().setResult(null);
				event.getInventory().setResult(resultDisc);
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
	
	private static ItemStack genErrorDisc(NoteFormatException ex, String loreMsg) {
		ItemStack disc = new ItemStack(Material.MUSIC_DISC_PIGSTEP);
		ItemMeta meta = disc.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Error");
		List<String> lore;
		if (ex == null) {
			lore = Arrays.asList(ChatColor.ITALIC + "" + ChatColor.DARK_RED + loreMsg);
		} else {
			lore = genErrorMessage(ex, loreMsg);
		}
		meta.setLore(lore);
		disc.setItemMeta(meta);
		return disc;
	}
	
	private static List<String> genErrorMessage(NoteFormatException ex, String loreMsg) {
		List<String> pages = ex.getPages();
		Pair<Integer, Integer> begin = ex.getBegin();
		Pair<Integer, Integer> end = ex.getEnd();
		String page1 = pages.get(begin.first);
		String page2 = pages.get(end.first);
		String pageMsg;
		if (begin.first == end.first) {
			String pageNum1 = ChatColor.ITALIC + "" + ChatColor.DARK_RED + "page " + (begin.first + 1);
			String chars1 = "(" + begin.second + " -> " + end.second + " (/" + page2.length() + "))";
			pageMsg = pageNum1 + chars1;
		} else {
			String pageNum1 = ChatColor.ITALIC + "" + ChatColor.DARK_RED + "page " + (begin.first + 1);
			String chars1 = "(" + begin.second + "/" + page1.length() + ")";
			String pageNum2 = " -> page " + (end.first + 1);
			String chars2 = "(" + end.second + "/" + page2.length() + ")";
			pageMsg = pageNum1 + chars1 + pageNum2 + chars2;
		}
		String where;
		if (begin.second < OFFSET) {
			where = ChatColor.BLACK + "|" + ChatColor.DARK_RED + page1.substring(0, begin.second);
		} else {
			where = ChatColor.DARK_RED + page1.substring(begin.second - OFFSET, begin.second);
		}
		int lenCap = MAX_ERROR_LEN;
		int page = begin.first;
		int startChar = begin.second;
		while (lenCap > 0) {
			if (page < end.first) {
				int len = page1.length() - startChar;
				if (len < lenCap) {
					where += ChatColor.RED + page1.substring(startChar, startChar + len) + ChatColor.BLACK + "|";
					page++;
					page1 = pages.get(page);
				} else {
					where += ChatColor.RED + page1.substring(startChar, startChar + lenCap);
				}
				lenCap -= len;
				startChar = 0;
			} else {
				int len = Math.min(lenCap, end.second - startChar);
				where += ChatColor.RED + page1.substring(startChar, startChar + len);
				startChar += len;
				break;
			}
		}
		if (lenCap < 0) {
			where += "...";
		}
		where += ChatColor.DARK_RED + page1.substring(startChar, Math.min(page1.length(), startChar + OFFSET));
		if (page1.length() - startChar <= OFFSET) {
			where += ChatColor.BLACK + "|";
		}
		
		where = where.replace('\n', '_').replace(' ', '_');
		String reason = ChatColor.ITALIC + "" + ChatColor.DARK_RED + ex.getMessage();
		if (loreMsg.isEmpty()) {
			return Arrays.asList(pageMsg, where, reason);
		}
		loreMsg = ChatColor.ITALIC + "" + ChatColor.DARK_RED + loreMsg;
		return Arrays.asList(pageMsg, where, reason, loreMsg);
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
