package com.festp.jukebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import com.festp.jukebox.FormatSettings.NBSSettings;
import com.festp.jukebox.NoteBookParser.ParseResult;
import com.festp.utils.UtilsType;

public class RecordingBook {
	private static final int MAX_SILENCE = 20 * 8;

	private int ticks = -1;
	private int initTicks = 5;
	private int silenceTicks = 0;
	private ItemStack book;
	private BookMeta currentMeta;
	private final int startPage;
	private final int startChar;

	private final FormatSettings settings;
	private final int defaultInst;
	private final HashMap<Integer, String> instruments;
	
	public final Player player;
	private float initYaw;
	private float initPitch;
	private final PlayerInventory playerInv;
	private final EquipmentSlot slot;
	private final int handSlot;
	
	String tickBuffer = "";
	
	public RecordingBook(Player player, EquipmentSlot slot) {
		this.player = player;
		this.slot = slot;
		
		playerInv = player.getInventory();
		handSlot = playerInv.getHeldItemSlot();
		book = playerInv.getItem(slot);
		currentMeta = (BookMeta) book.getItemMeta();
		startPage = currentMeta.getPageCount();
		String headerPages;
		if (startPage > 0) {
			startChar = currentMeta.getPage(startPage).length();
			headerPages = currentMeta.getPage(1) + "\n";
		} else {
			startChar = 0;
			headerPages = "";
		}
		ParseResult res = NoteBookParser.parseHeader(headerPages); // TODO? check next pages if header is long - check pages for "="
		defaultInst = res.defaultInst;
		settings = res.settings;
		instruments = new HashMap<>();
		for (Entry<String, Integer> entry : res.aliases.entrySet()) {
			Integer id = entry.getValue();
			String alias = entry.getKey();
			if (instruments.containsKey(id)) {
				String curAlias = instruments.get(id);
				if (!(NoteUtils.isUnsignedInteger(alias) && !NoteUtils.isUnsignedInteger(curAlias))
						&& curAlias.compareTo(alias) > 0) {
					instruments.put(id, alias);
				}
			} else {
				instruments.put(id, alias);
			}
		}
	}
	
	public void appendToTick(int instIndex, int nbsSemitone) {
		String noteStr = "";
		if (settings instanceof NBSSettings) {
			int semitone = NoteUtils.getSemitone(noteStr) - NoteUtils.STANDART_SEMITONE_OFFSET;
			if (0 <= nbsSemitone && nbsSemitone <= 24) {
				noteStr = "" + semitone;
			} else {
				noteStr = NoteUtils.getNote(nbsSemitone);
			}
		} else {
			noteStr = NoteUtils.getNote(nbsSemitone - NoteUtils.INSTRUMENTS[instIndex].semitoneShift);
		}
		
		if (instIndex != defaultInst) {
			String instAlias = instruments.get(instIndex);
			if (instAlias == null) {
				return;
			}
			noteStr = instAlias + "-" + noteStr;
		}
		
		if (tickBuffer.isEmpty()) {
			tickBuffer = noteStr;
		} else {
			tickBuffer += "&" + noteStr;
		}
	}
	
	public void finishTick() {
		if (!canTick()) {
			return;
		}
		if (initTicks > 0) {
			initYaw = player.getLocation().getYaw();
			initPitch = player.getLocation().getPitch();
			initTicks--;
		}
		
		if (ticks >= 0) {
			ticks++;
			if (ticks % settings.multiplier != 0) {
				tickBuffer = "";
				return;
			}
		}
		
		int count = getPageCount(currentMeta);
		if (count == 0) {
			currentMeta.addPage("");
			count = 1;
		}
		
		String page = currentMeta.getPage(count);
		// tickBuffer "" => "."; .]. => ..; n]. => ,.; notes => ,notes
		if (!page.isEmpty()) {
			if (tickBuffer.isEmpty()) {
				silenceTicks++;
				if (ticks >= 0) {
					if (page.charAt(page.length() - 1) == '.') {
						tickBuffer = ".";
					} else {
						tickBuffer = ",.";
					}
				}
			} else {
				if (ticks == -1) {
					ticks = 0;
				}
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
				&& silenceTicks <= MAX_SILENCE && (initTicks > 0 || player.getLocation().getYaw() == initYaw && player.getLocation().getPitch() == initPitch)
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
		if (book == null || book.getType() != Material.WRITABLE_BOOK || currentMeta.getPageCount() >= 100) {
			return false;
		}
		return true;
		/*
		//if (!book.hasItemMeta() || !(book.getItemMeta() instanceof BookMeta)) { - too heavy, full book copy
		BookMeta bookMeta = (BookMeta) book.getItemMeta(); // should remove this part \/
		// bookMeta.equals(currentMeta) is too heavy
		return bookMeta.getPageCount() == currentMeta.getPageCount()
				&& bookMeta.hasPages() == currentMeta.hasPages()
				&& bookMeta.getPage(1).equals(currentMeta.getPage(1));
		*/
	}
	
	public void show() {
		openBookAtPage(player, currentMeta, getPageCount(currentMeta));
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
		currentMeta.setPages(pages); // heavy
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
		return pageStr1.length() == pageStr2.length(); // TODO + calc lines height
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

	public static int getPageCount(BookMeta bookMeta) {
		return bookMeta.getPageCount();
	}
	
	/**@deprecated too slow because of <b>book.getItemMeta()</b>*/
	@Deprecated
	public static int getPageCount(ItemStack book) {
		if (book == null || !UtilsType.isBook(book.getType())) {
			return -1;
		}
		return getPageCount((BookMeta) book.getItemMeta());
	}
	/**pages indexed from 1!
	 * @deprecated too slow because of <b>book.getItemMeta()</b>*/
	@Deprecated
	public static void openBookAtPage(Player player, ItemStack book, int page) {
		if (book == null || !UtilsType.isBook(book.getType())) {
			return;
		}
		openBookAtPage(player, (BookMeta) book.getItemMeta(), page);
	}
}
