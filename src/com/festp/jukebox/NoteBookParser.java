package com.festp.jukebox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.festp.Pair;
import com.festp.utils.UtilsType;

public class NoteBookParser {
	private static final int MAX_VARINT_BYTES = 8;
	public static final int MAX_TICKRATE = 20;
	public static final int DEFAULT_TICKRATE = 10;
	
	private static class StringListWrapper {
		private List<String> list;
		private String currentPage;
		private int n = 0;
		private int index = -1;
		public StringListWrapper(List<String> list, int startPage, int startChar) {
			this.list = list;
			this.n = startPage;
			this.index = startChar - 1;
			if (0 <= n && n < list.size()) {
				currentPage = list.get(n);
			}
		}
		public int getNext() {
			if (n >= list.size()) {
				return -1;
			}
			do {
				index++;
				if (index >= currentPage.length()) {
					index -= currentPage.length();
					n++;
					while (n < list.size() && list.get(n).isEmpty()) {
						n++;
					}
					if (n >= list.size()) {
						return -1;
					}
					currentPage = list.get(n);
				}
			} while (index < currentPage.length() && isSpace(currentPage.charAt(index)));
			return currentPage.charAt(index);
		}
		
		public Pair<Integer, Integer> getPageAndIndex() {
			return new Pair<Integer, Integer>(getPage(), getIndex());
		}
		
		public int getPage() {
			if (n >= list.size() && list.size() > 0) {
				return list.size() - 1;
			}
			return n;
		}
		
		public int getIndex() {
			if (n >= list.size() && list.size() > 0) {
				return list.get(n - 1).length();
			}
			return index;
		}
	}
	
	/**item.getItemMeta() is heavy
	 * @throws NoteFormatException 
	 * @throws IOException */
	public static byte[] genDiscData(ItemStack item) throws NoteFormatException, IOException {
		if (item == null || !UtilsType.isBook(item.getType())) {
			return null;
		}
		BookMeta meta = (BookMeta) item.getItemMeta();
		return toDiscData(meta.getPages());
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
	 * @throws NoteFormatException
	 * @throws IOException
	 * */
	public static byte[] toDiscData(List<String> bookPages) throws NoteFormatException, IOException {
		if (bookPages.size() == 0) {
			return null;
		}
		ParseResult res = parseHeader(bookPages.get(0)); // TODO process multiple pages
		FormatSettings settings = res.settings;
		final boolean hasDefault = res.defaultInst >= 0;
		final int defaultInst = res.defaultInst;
		final HashMap<String, Integer> instruments = res.aliases;
		
		ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
		int zeroGap = settings.getGapLength(0);

		StringListWrapper charrer = new StringListWrapper(bookPages, 0, res.endIndex);
		while (true) {
			int c = charrer.getNext();
			if (c < 0) {
				break;
			}
			if (isSeparator((char) c)) {
				int gapLength = settings.getGapLength(1) - zeroGap;
				if (gapLength > 0) {
					dataStream.write(getGap(gapLength));
				}
			} else if (c == '.') {
				Pair<Integer, Integer> begin = charrer.getPageAndIndex();
				int dotCount = 1;
				int errorCount = 0;
				c = charrer.getNext();
				while (c >= 0) {
					if (c == '.') {
						if (errorCount > 0) {
							errorCount++;
						}
						dotCount++;
					} else if (isSeparator((char) c)) {
						break;
					} else {
						errorCount++;
					}
					c = charrer.getNext();
				}
				if (errorCount > 0) {
					Pair<Integer, Integer> end = charrer.getPageAndIndex();
					throw new NoteFormatException(bookPages, begin, end, "Dot line is corrupted");
				}
				if (c >= 0) {
					int gapLength = settings.getGapLength(dotCount) - zeroGap;
					if (gapLength > 0) {
						dataStream.write(getGap(gapLength));
					}
				}
			} else {
				int sepCount = 0;
				int inst = 0;
				int pitch = 0;
				String buf = "";
				// controls errors from buf
				Pair<Integer, Integer> begin = charrer.getPageAndIndex();
				while (true) {
					if (c < 0 || isSeparator((char) c) || c == '&') {
						if (sepCount == 0) {
							if (!hasDefault) {
								throw new NoteFormatException(bookPages, begin, charrer.getPageAndIndex(), "default=?");
							}
							inst = defaultInst;
							sepCount++;
						}
						if (sepCount == 1) {
							pitch = NoteUtils.getSemitone(buf) + settings.getPitchShift(inst);
							if (pitch < -NoteUtils.OCTAVE * 2) {
								throw new NoteFormatException(bookPages, begin, charrer.getPageAndIndex(), "Too low-frequency note");
							} else if (pitch >= NoteUtils.OCTAVE * 9) {
								throw new NoteFormatException(bookPages, begin, charrer.getPageAndIndex(), "Too high-frequency note");
							}
							buf = "";
							begin = charrer.getPageAndIndex();
						}
						sepCount = 0;
						int continueMask;
						if (c == '&') {
							continueMask = 0x40;
						} else {
							continueMask = 0x00;
						}
						dataStream.write(continueMask | inst);
						dataStream.write(pitch);
						if (c != '&') {
							break;
						}
					} else if (c == '-') {
						if (sepCount == 0) {
							if (!instruments.containsKey(buf)) {
								throw new NoteFormatException(bookPages, begin, charrer.getPageAndIndex(), "Invalid instrument");
							} 
							inst = instruments.get(buf);
						}
						sepCount++;
						buf = "";
						begin = charrer.getPageAndIndex();
						if (sepCount >= 2) {
							c = charrer.getNext();
							throw new NoteFormatException(bookPages, begin, charrer.getPageAndIndex(), "Extra \"-\"");
						}
					} else {
						buf += (char) c;
					}
					c = charrer.getNext();
				}
				if (zeroGap > 0) {
					dataStream.write(getGap(zeroGap));
				}
			}
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
	
	public static ParseResult parseHeader(String bookFormat) {
		HashMap<String, Integer> instruments = new HashMap<>(); // alias : id
		for (int i = 1; i <= NoteUtils.INSTRUMENTS.length; i++) {
			instruments.put(Integer.toString(i), i - 1);
		}

		FormatSettings settings = new FormatSettings.NBSSettings(DEFAULT_TICKRATE);
		
		String formatSettingsStr = "";
		int i = 0;
		while (i < bookFormat.length() && i < 30 && !isSeparator(bookFormat.charAt(i))) {
			char c = bookFormat.charAt(i);
			if (c == '=' || c == '-') {
				formatSettingsStr = "";
				i = -1;
				break;
			}
			if (!isSpace(c)) {
				formatSettingsStr += c;
			}
			i++;
		}
		i++;
		FormatSettings settingsRes = getFormatSetting(formatSettingsStr);
		if (settingsRes != null) {
			settings = settingsRes;
		}
		
		int defaultInst = -1;

		String defaultInstStr = "";
		int aliasBegin = i;
		int equalityCount = 0;
		int equalityIndex = 0;
		while (true) {
			if (i >= bookFormat.length() || isSeparator(bookFormat.charAt(i))) {
				if (equalityCount == 0) {
					i = aliasBegin;
					break;
				}
				if (equalityCount == 1) {
					String left = bookFormat.substring(aliasBegin, equalityIndex);
					String right = bookFormat.substring(equalityIndex + 1, i);
					left = left.toLowerCase().replace(" ", "");
					right = right.toLowerCase().replace(" ", "");
					if (left.equalsIgnoreCase("default")) {
						defaultInstStr = right;
					} else if (right.equalsIgnoreCase("default")) {
						defaultInstStr = left;
					} else {
						if (!tryPut(left, right, instruments)) {
							tryPut(right, left, instruments);
						}
					}
				}
				i++;
				aliasBegin = i;
				equalityCount = 0;
				if (i >= bookFormat.length()) {
					i = bookFormat.length();
					break;
				}
			}
			char c = bookFormat.charAt(i);
			if (c == '=') {
				if (equalityCount == 0) {
					equalityIndex = i;
				}
				equalityCount++;
			}

			i++;
		}
		defaultInst = getInstrument(defaultInstStr, instruments);
		
		return new ParseResult(settings, instruments, defaultInst, i);
	}
	
	public static boolean isSeparator(char c) {
		return c == '\n' || c == ',';
	}
	
	public static boolean isSpace(char c) {
		return c == ' ' || c == '_';
	}
	
	public static FormatSettings getFormatSetting(String formatSettingsStr) {
		String tickrateStr = "";
		for (int i = formatSettingsStr.length() - 1; i >= 0; i--) {
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
				return FormatSettings.getSettings(formatSettingsStr, DEFAULT_TICKRATE);
			} else {
				return FormatSettings.getSettings(formatSettingsStr, Integer.parseInt(tickrateStr));
			}
		}
		return null;
	}

	public static final class ParseResult {
		public final FormatSettings settings;
		public final HashMap<String, Integer> aliases;
		public final int defaultInst;
		public final int endIndex;
		public ParseResult(FormatSettings settings, HashMap<String, Integer> aliases, int defaultInst, int endIndex) {
			this.settings = settings;
			this.aliases = aliases;
			this.defaultInst = defaultInst;
			this.endIndex = endIndex;
		}
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
}
