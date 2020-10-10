package com.festp.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.festp.utils.UtilsType;

public class NoteBookParser {
	private static final int MAX_VARINT_BYTES = 8;
	
	// format => disc
	/**item.getItemMeta() is heavy*/
	public static byte[] genDiscData(ItemStack item) {
		if (item == null || !UtilsType.isBook(item.getType())) {
			return null;
		}
		BookMeta meta = (BookMeta) item.getItemMeta();
		return toDiscData(meta.getPages());
	}
	
	public static byte[] toDiscData(List<String> bookPages) {
		String str = "";
		for (String page : bookPages) {
			str += page + " ";
		}
		return toDiscData(str);
	}
	
	/**<b>Format description</b><br>
	 * Alias separators: <b>'='</b><br>
	 * Whitespaces(will be ignored): <b>' '</b><br>
	 * Tick separators: <b>','</b>, <b>'\n'</b><br>
	 * Tick joiners: <b>'&'</b><br>
	 * Empty tick: <b>'.'</b><br>
	 * Sound separators: <b>'-'</b><br>
	 * Sound coding: <b>inst id</b> + separator + <b>note</b><br>
	 * &nbsp;&nbsp; <i>inst id</i> by default from 1 to 16 (according to OpenNBS)<br>
	 * &nbsp;&nbsp; <i>note</i> from "C0" to "B8" or from 0 to 24<br>
	 * <br><b>Example:</b> 1-F3 & 2-C2,.,1-F#3,...,1-F3 & 3-C#4,.,1-C4
	 * */
	public static byte[] toDiscData(String bookFormat) {
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		
		bookFormat = bookFormat.replace(" ", ""); // TODO? use regex replace
		bookFormat = bookFormat.replace("|", "-");
		HashMap<String, Integer> instruments = new HashMap<>(); // alias : id
		for (int i = 1; i <= NoteDiscRecord.INSTRUMENTS.length; i++) {
			instruments.put(Integer.toString(i), i - 1);
		}

		boolean hasDefault = false;
		int defaultInst = 0;
		// "-" had not to be in aliases
		String aliasesStr = bookFormat.substring(0, bookFormat.indexOf("-"));
		int aliasesEnd = Math.max(aliasesStr.lastIndexOf(","), aliasesStr.lastIndexOf("\n"));
		if (aliasesEnd >= 0) {
			aliasesStr = aliasesStr.substring(0, aliasesEnd);
			int defaultInstRes = getAliases(aliasesStr, instruments);
			if (defaultInstRes >= 0) {
				hasDefault = true;
				defaultInst = defaultInstRes;
			}
		}

		bookFormat = bookFormat.substring(aliasesEnd); // heavy?
		bookFormat = bookFormat.replace("\n", ",");
		try {
			String[] ticks = bookFormat.split(",");
			for (String part : ticks) {
				if (part.isEmpty()) {
					dataStream.write(getGap(1));
				} else if (part.matches("[.]+")) {
					dataStream.write(getGap(part.length()));
				} else {
					String[] sounds = part.split("&");
					int continueMask = 0x40;
					for (int i = 0; i < sounds.length; i++) {
						if (i == sounds.length - 1) {
							continueMask = 0x00;
						}
						String sound[] = sounds[i].split("-");
						if (sound.length != 2) {
							return null;
						}
						
						String idStr = sound[0];
						String note = sound[1];
						
						int id;
						if (instruments.containsKey(idStr)) {
							id = instruments.get(idStr);
						} else if (hasDefault) {
							id = defaultInst;
						} else {
							continue;
						}
						int pitch = NoteUtils.getPitch(note);
						dataStream.write(continueMask | id);
						dataStream.write(pitch);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return dataStream.toByteArray();
	}
	
	/**@return default instrument or negative value*/
	private static int getAliases(String aliasesStr, HashMap<String, Integer> instruments) {
		aliasesStr = aliasesStr.replace("_", "").replace(",", "\n").toLowerCase();
		// no alias intersections, both left and right, names from NBS and minecraft(F3/wiki), default = inst or default = alias
		String defaultInst = "";
		for (String aliasStr : aliasesStr.split("\n")) {
			String[] parts = aliasStr.split("=");
			if (parts.length != 2) {
				continue;
			}
			String left = parts[0];
			String right = parts[1];
			if (left.equals("default")) {
				defaultInst = right;
			} else if (right.equals("default")) {
				defaultInst = left;
			} else {
				if (!tryPut(left, right, instruments)) {
					tryPut(right, left, instruments);
				}
			}
		}
		return getInstrument(defaultInst, instruments);
	}
	
	private static boolean tryPut(String instrument, String alias, HashMap<String, Integer> instruments) {
		int inst = getInstrument(instrument);
		if (inst < 0 || instruments.containsKey(alias)) {
			return false;
		}
		instruments.put(alias, inst);
		return true;
	}
	
	private static int getInstrument(String name) {
		Integer res = NoteUtils.getInstrument(name);
		if (res == null) {
			return -1;
		}
		return res;
	}
	
	private static int getInstrument(String name, HashMap<String, Integer> aliases) {
		if (aliases.containsKey(name)) {
			return aliases.get(name);
		}
		return getInstrument(name);
	}
	
	private static byte[] getGap(int length) {
		byte[] varInt = getVarInt(length, 1);
		varInt[0] |= 0x80;
		return varInt;
	}
	
	private static byte[] getVarInt(int x, int offset) {
		byte[] buf = new byte[MAX_VARINT_BYTES];
		buf[0] = (byte) (x & 0x7F >> offset);
        x >>= 7 - offset;
		int n = 1;
	    if (x != 0) {
	    	buf[0] |= 0x80 >> offset;
	        x -= 1;
		    for (; x > 127; n++) {
		        buf[n] = (byte) (0x80 | (x & 0x7F));
		        x >>= 7;
		        x -= 1;
		    }
		    buf[n] = (byte) x;
		    n++;
	    }
	    return Arrays.copyOf(buf, n);
	}
}
