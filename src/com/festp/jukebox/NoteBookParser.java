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
	public static final int MAX_TICKRATE = 20;
	public static final int DEFAULT_TICKRATE = 10;
	
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
		
		bookFormat = bookFormat.replace(" ", "");
		bookFormat = bookFormat.replace("|", "-");
		HashMap<String, Integer> instruments = new HashMap<>(); // alias : id
		for (int i = 1; i <= NoteDiscRecord.INSTRUMENTS.length; i++) {
			instruments.put(Integer.toString(i), i - 1);
		}

		FormatSettings settings = new FormatSettings.NBSSettings(DEFAULT_TICKRATE); 
		boolean hasDefault = false;
		int defaultInst = 0;
		
		// "-" had not to be in aliases
		int aliasesEnd = Math.max(getFirstSep(bookFormat, '='), getLastSep(bookFormat, '-'));
		if (aliasesEnd >= 0) {
			String aliasesStr = bookFormat.substring(0, aliasesEnd);
			bookFormat = bookFormat.substring(aliasesEnd); // heavy?
			aliasesStr = aliasesStr.replace(',', '\n');
			
			int formatSettingsEnd = aliasesStr.indexOf("\n");
			String formatSettingsStr;
			if (formatSettingsEnd < 0) {
				formatSettingsStr = aliasesStr;
				formatSettingsEnd = formatSettingsStr.length();
			} else {
				formatSettingsStr = aliasesStr.substring(0, formatSettingsEnd);
			}
			if (!formatSettingsStr.contains("=")) {
				aliasesStr = aliasesStr.substring(formatSettingsEnd);
				String tickrateStr = "";
				for (int i = formatSettingsEnd - 1; i >= 0; i--) {
					char c = formatSettingsStr.charAt(i);
					if (Character.isDigit(c)) {
						tickrateStr = c + tickrateStr;
					} else {
						break;
					}
				}
				if (tickrateStr.length() <= 2) {
					formatSettingsStr = formatSettingsStr.substring(0, formatSettingsStr.length() - tickrateStr.length());
					if (tickrateStr.length() == 0) {
						settings = FormatSettings.getSettings(formatSettingsStr, DEFAULT_TICKRATE);
					} else {
						settings = FormatSettings.getSettings(formatSettingsStr, Integer.parseInt(tickrateStr));
					}
				}
			}
			int defaultInstRes = getAliases(aliasesStr, instruments);
			if (defaultInstRes >= 0) {
				hasDefault = true;
				defaultInst = defaultInstRes;
			}
		}

		bookFormat = bookFormat.replace("\n", ",");
		try {
			int zeroGap = settings.getGapLength(0);
			String[] ticks = bookFormat.split(",");
			for (String part : ticks) {
				if (part.matches("[.]+") || part.isEmpty()) {
					int length = part.length();
					if (part.isEmpty()) {
						length = 1;
					}
					int gapLength = settings.getGapLength(length) - zeroGap;
					if (gapLength > 0) {
						dataStream.write(getGap(gapLength));
					}
				} else {
					String[] sounds = part.split("&");
					int continueMask = 0x40;
					for (int i = 0; i < sounds.length; i++) {
						if (i == sounds.length - 1) {
							continueMask = 0x00;
						}
						String idStr;
						String note;
						String sound[] = sounds[i].split("-");
						if (sound.length == 1) {
							idStr = "";
							note = sound[0];
						} else if (sound.length == 2) {
							idStr = sound[0];
							note = sound[1];
						} else {
							return null;
						}
						
						int id;
						if (instruments.containsKey(idStr)) {
							id = instruments.get(idStr);
						} else if (hasDefault) {
							id = defaultInst;
						} else {
							return null;
						}
						int pitch = NoteUtils.getPitch(note) + settings.getPitchShift(id);
						if (pitch < 0) {
							pitch = 0;
						}
						dataStream.write(continueMask | id);
						dataStream.write(pitch);
					}
					if (zeroGap > 0) {
						dataStream.write(getGap(zeroGap));
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
	
	private static int getLastSep(String s, char beforeFirst) {
		int index = s.indexOf(beforeFirst);
		if (index < 0) {
			return -1;
		}
		s = s.substring(0, index);
		return Math.max(s.lastIndexOf(','), s.lastIndexOf('\n'));
	}
	
	private static int getFirstSep(String s, char afterLast) {
		int index = s.lastIndexOf(afterLast);
		if (index < 0) {
			return -1;
		}
		index++;
		int index1 = s.indexOf(',', index);
		int index2 = s.indexOf('\n', index);
		if (index1 < 0) {
			return index2;
		} else if (index2 < 0) {
			return index1;
		}
		return Math.min(index1, index2);
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
