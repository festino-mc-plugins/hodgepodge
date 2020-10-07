package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import com.festp.utils.UtilsType;

public class RecordingBook {
	private static final int MAX_SILENCE = 20 * 8;
	
	public final Player player;
	private float initYaw;
	private float initPitch;
	private final PlayerInventory playerInv;
	private final EquipmentSlot slot;
	private final int handSlot;

	private int silenceTicks = -5;
	private ItemStack book;
	private BookMeta currentMeta;
	private final int startPage;
	private final int startChar;
	
	String tickBuffer = "";
	
	public RecordingBook(Player player, EquipmentSlot slot) {
		this.player = player;
		this.slot = slot;
		
		playerInv = player.getInventory();
		handSlot = playerInv.getHeldItemSlot();
		book = playerInv.getItem(slot);
		currentMeta = (BookMeta) book.getItemMeta();
		startPage = currentMeta.getPageCount();
		if (startPage > 0) {
			startChar = currentMeta.getPage(startPage).length();
		} else {
			startChar = 0;
		}
		// TODO load aliases
	}
	
	public void appendToTick(int instIndex, String note) {
		// TODO use aliases
		String instAlias = (instIndex + 1) + "";
		
		note = instAlias + "-" + note;
		if (tickBuffer.isEmpty()) {
			tickBuffer = note;
		} else {
			tickBuffer += "&" + note;
		}
	}
	
	public void finishTick() {
		if (!canTick()) {
			return;
		}
		if (silenceTicks < 0) {
			initYaw = player.getLocation().getYaw();
			initPitch = player.getLocation().getPitch();
			silenceTicks++;
		}
		// tickBuffer "" => "."; .]. => ..; n]. => ,.; notes => ,notes
		int count = getPageCount(currentMeta);
		if (count == 0) {
			currentMeta.addPage("");
			count = 1;
		}
		
		String page = currentMeta.getPage(count);
		if (!page.isEmpty()) {
			if (tickBuffer.isEmpty()) {
				silenceTicks++;
				if (page.charAt(page.length() - 1) == '.') {
					tickBuffer = ".";
				} else {
					tickBuffer = ",.";
				}
			} else {
				silenceTicks = 0;
				tickBuffer = "," + tickBuffer;
			}
		}
		if (canWrite(currentMeta, count, tickBuffer)) {
			currentMeta.setPage(count, page + tickBuffer);
		} else {
			currentMeta.addPage(tickBuffer);
		}
		
		tickBuffer = "";
		book.setItemMeta(currentMeta);
		playerInv.setItem(slot, book);
		show();
	}
	
	public boolean canTick() {
		return player.isOnline() && (slot != EquipmentSlot.HAND || playerInv.getHeldItemSlot() == handSlot)
				&& silenceTicks <= MAX_SILENCE && (silenceTicks < 0 || player.getLocation().getYaw() == initYaw && player.getLocation().getPitch() == initPitch)
				&& canModifyItem();
	}
	
	public boolean canModifyItem() {
		// if (!player.isOnline()) return false;
		// <= player.saveData()?
		ItemStack book;
		if (slot == EquipmentSlot.HAND) {
			book = playerInv.getItem(handSlot);
		} else {
			book = playerInv.getItem(slot);
		}
		//System.out.print("canTick: " + book.getItemMeta()+ " "+ currentMeta +" "+ (book.getItemMeta() == currentMeta)+" "+book.getItemMeta().equals(currentMeta)+" "+book.equals(this.book));
		return book != null && book.getItemMeta().equals(currentMeta) && currentMeta.getPageCount() < 100;
	}
	
	public void show() {
		openBookAtPage(player, book, getPageCount(book));
	}
	
	public void reduceSize() {
		if (!canModifyItem()) {
			return;
		}
		List<String> pages = new ArrayList<>(currentMeta.getPages());
		for (int page = getPageCount(currentMeta) - 1; page >= 0; page--) {
			String pageStr = pages.get(page);
			int i;
			for (i = pageStr.length() - 1; i >= 0; i--) {
				if (pageStr.charAt(i) != '.' && pageStr.charAt(i) != ',') {
					break;
				}
			}
			if (i >= 0) {
				i++;
				if (page == startPage - 1) {
					i = Math.max(startChar, i);
				}
				pageStr = pageStr.substring(0, i);
				pages.set(page, pageStr);
				break;
			}
			if (page > startPage - 1) {
				pages.remove(page);
			}
		}
		currentMeta.setPages(pages);
		book.setItemMeta(currentMeta);
		playerInv.setItem(slot, book);
		if (!player.isOnline()) {
			player.saveData();
		}
	}
	
	//public static boolean canWrite(String page, String toWrite) { }
	public static boolean canWrite(BookMeta meta, int page, String toWrite) {
		//meta = meta.clone(); - poor performance
		String pageStr0 = meta.getPage(page);
		String pageStr1 = pageStr0 + toWrite;
		meta.setPage(page, pageStr1);
		String pageStr2 = meta.getPage(page);
		meta.setPage(page, pageStr0);
		return pageStr1.length() == pageStr2.length();
	}
	
	/**pages indexed from 1!*/
	public static void openBookAtPage(Player player, ItemStack book, int page) {
		if (book == null || !UtilsType.isBook(book.getType())) {
			return;
		}
		openBookAtPage(player, (BookMeta) book.getItemMeta(), page);
	}
	
	/**pages indexed from 1!*/
	public static void openBookAtPage(Player player, BookMeta bookMeta, int page) {
		if (page < 1) {
			page = 1;
		} else if (page > bookMeta.getPageCount()) {
			page = bookMeta.getPageCount();
		}
		ItemStack pageBook = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta pageMeta = (BookMeta) pageBook.getItemMeta();
		pageMeta.setTitle(pageMeta.getDisplayName());
		pageMeta.setAuthor("YOU WON'T SEE IT. NEVER");
		pageMeta.setPages(bookMeta.getPage(page));
		pageBook.setItemMeta(pageMeta);
		
		player.openBook(pageBook);
	}

	public static int getPageCount(ItemStack book) {
		if (book == null || !UtilsType.isBook(book.getType())) {
			return -1;
		}
		return getPageCount((BookMeta) book.getItemMeta());
	}

	public static int getPageCount(BookMeta bookMeta) {
		return bookMeta.getPageCount();
	}
}
