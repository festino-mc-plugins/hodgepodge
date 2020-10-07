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
	// standard aliases
	
	// format => disc
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
	 * Tick separators: <b>','</b><br>
	 * Tick joiners: <b>'&'</b><br>
	 * Empty tick: <b>'.'</b><br>
	 * Whitespaces(will be ignored): <b>' '</b>, <b>'-'</b>, <b>'\n'</b><br>
	 * Note coding: <b>inst id</b> + <b>note</b><br>
	 * &nbsp;&nbsp; <i>inst id</i> from 1 to 16 (according to OpenNBS)<br>
	 * &nbsp;&nbsp; <i>note</i> from "C0" to "B8"<br>
	 * <br><b>Example:</b> 1-F3 & 2-C2,.,1-F#3,...,1-F3 & 3-C#4,.,1-C4
	 * */
	public static byte[] toDiscData(String bookFormat) {
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		HashMap<String, Integer> instruments = new HashMap<>(); // alias : id
		// fill map
		// TODO flexible aliases
		for (int i = 1; i <= NoteDiscRecord.INSTRUMENTS.length; i++) {
			instruments.put(Integer.toString(i), i - 1);
		}
		// default
		instruments.put("default", 0);
		try {
			bookFormat = bookFormat.replace(" ", "").replace("-", "").replace("\n", ""); // TODO? use regex replace
			for (String part : bookFormat.split(",")) {
				if (part.isEmpty()) {
					dataStream.write(getGap(1));
				} else if (part.matches("[.]+")) {
					dataStream.write(getGap(part.length()));
				} else {
					String[] sounds = part.split("&");
					int continueMask;
					for (int i = 0; i < sounds.length; i++) {
						if (i < sounds.length - 1) {
							continueMask = 0x40;
						} else {
							continueMask = 0x00;
						}
						String sound = sounds[i];
						int k = 0;
						String idStr = "";
						while (k < sound.length() && Character.isDigit(sound.charAt(k))) {
							idStr += sound.charAt(k);
							k++;
						}
						if (!instruments.containsKey(idStr))
							continue;
						int id = instruments.get(idStr);
						int pitch = NoteUtils.getPitch(sound.substring(k));
						dataStream.write(id | continueMask);
						dataStream.write(pitch);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataStream.toByteArray();
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
