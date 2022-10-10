package com.festp.misc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LinkRestorer implements Listener {

	private final JavaPlugin plugin;
	
	public LinkRestorer(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	private static class Link
	{
		public String orig;
		public int beginIndex;
		public int endIndex;
		public boolean hasProtocol;
		
		public Link(String orig, int beginIndex, int endIndex, boolean hasProtocol)
		{
			this.orig = orig;
			this.beginIndex = beginIndex;
			this.endIndex = endIndex;
			this.hasProtocol = hasProtocol;
		}
		
		public String getString()
		{
			return orig.substring(beginIndex, endIndex);
		}
	}
	
	// may be check if in https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
	
	/**
	 * Generates command for chat messages containing links<br>
	 * {@literal<FEST_Channel>} test https://www.netflix.com/browse extra text<br>
	 * like<br>
	 * /tellraw @a [<br>
	 * {"text":"<"},<br>
	 * {"text":"FEST_Channel",<br>
	 * "hoverEvent":{"action":"show_text","value":"FEST_Channel\nType: Player\n4a9b60fa-6c37-3673-b0ae-02ee83a6356d"},<br>
	 * "clickEvent":{"action":"suggest_command","value":"/tell FEST_Channel"}},<br>
	 * {"text":"> test "},<br>
	 * {"text":"https://www.netflix.com/browse","underlined":true,<br>
	 * "clickEvent":{"action":"open_url","value":"https://www.netflix.com/browse"}},<br>
	 * {"text":" extra text"}<br>
	 * ]
	 * */
	@EventHandler
	public void OnChat(AsyncPlayerChatEvent event)
	{
		String message = event.getMessage();
		
		int lastIndex = 0;
		Link link = selectLink(message, lastIndex);
		if (link == null)
			return;
		
		String nickname = event.getPlayer().getDisplayName();
		String uuid = event.getPlayer().getUniqueId().toString();
		String command = "tellraw @a [";
		command += "{\"text\":\"<\"},";
		command += "{\"text\":\"" + nickname + "\",";
		command += "\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"" + nickname + "\\nType: Player\\n" + uuid + "\"},";
		command += "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell " + nickname + " \"}";
		command += "},";
		command += "{\"text\":\"> \"}";
		while (link != null)
		{
			if (lastIndex < link.beginIndex)
				command += ",{\"text\":\"" + message.substring(lastIndex, link.beginIndex) + "\"}";
			
			String linkStr = link.getString();
			String linkClick = applyBrowserEncoding(linkStr);
			if (!link.hasProtocol)
				linkClick = "https://" + linkClick;
			command += ",{\"text\":\"" + linkStr + "\",\"underlined\":true,"
					+ "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + linkClick + "\"}}";
			
			lastIndex = link.endIndex;
			link = selectLink(message, lastIndex);
		}
		if (lastIndex < message.length())
		{
			command += ",{\"text\":\"" + message.substring(lastIndex) + "\"}";
		}
		command += "]";
		
		final String bukkitCommand = command;
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), bukkitCommand);
			}
		});
		event.setCancelled(true);
	}
	
	private String applyBrowserEncoding(String str) {
		try {
			String r = URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
			return r.replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return str;
		}
	}

	// TODO check telegram desktop code
	private static Link selectLink(String message, int indexBegin)
	{
		// starts with http://, www. or whatever, ends with .ru, .com, ... or /<anything>; also ip is valid
		// parse [<protocol>://]<d>.<d>...<d>.<D>[/<no spaces>] - no spaces
		// <protocol> is from protocol list, else skip ONLY the protocol part
		// <d> must start and end with a-z, 0-9, but can be "-" in the middle
		// <D> is from domain list or is part of IP
		// algorithm: find '.', all .<d> after, checking for <D>, select <d> before, check '/' and then select [/...], try select protocol
		int dotIndex = message.indexOf('.', indexBegin);
		while (0 <= dotIndex)
		{
			if (dotIndex == 0) {
				dotIndex = message.indexOf('.', dotIndex + 2);
				continue;
			}
			int dEnd = dotIndex;
			int dBegin = selectDomainReversed(message, dEnd);
			if (dBegin < 0) {
				dotIndex = message.indexOf('.', dotIndex + 2);
				continue;
			}
			int linkBegin = dBegin;
			// select d, check next, if '.' and d, select... else check if D.
			dBegin = dotIndex + 1;
			dEnd = selectDomain(message, dBegin);
			if (dEnd < 0) {
				dotIndex = message.indexOf('.', dotIndex + 2);
				continue;
			}
			int linkEnd = dEnd;
			int length = message.length();
			boolean isNotLink = false;
			while (true)
			{
				dotIndex = dBegin - 1;
				if (length <= dEnd || message.charAt(dEnd) != '.')
					break;
				dBegin = dEnd + 1;
				dEnd = selectDomain(message, dBegin);
				if (dEnd < 0) {
					isNotLink = true;
					break;
				}
				linkEnd = dEnd;
			}
			if (isNotLink) {
				dotIndex = message.indexOf('.', dotIndex + 2);
				continue;
			}
			
			// D is number => check if valid IP(4 numbers <= 255), else just select link;
			String tld = message.substring(dotIndex + 1, linkEnd);
			if (hasNumber(tld))
			{
				if (!isValidIP(message, linkBegin, linkEnd))
				{
					// may select non-IPs like 0.1-f.a.435.0.0.0.1
					dotIndex = message.indexOf('.', dotIndex + 2);
					continue;
				}
			}
			else {
				if (tld.length() < 2)
				{
					dotIndex = message.indexOf('.', dotIndex + 2);
					continue;
				}
			}
			
			// if valid, select [protocol://] and [/...]
			int protocolBegin = selectProtocolReversed(message, linkBegin);
			boolean hasProtocol = protocolBegin >= 0;
			if (hasProtocol)
				linkBegin = protocolBegin;
			linkEnd = trySelectRest(message, linkEnd);
			return new Link(message, linkBegin, linkEnd, hasProtocol);
		}
		return null;
	}
	
	private static int trySelectRest(String str, int begin)
	{
		int length = str.length();
		if (begin < length && str.charAt(begin) == '/')
		{
			begin++;
			while (begin < length && !Character.isSpaceChar(str.charAt(begin)))
				begin++;
		}
		return begin;
	}
	
	// dirty code
	private static int selectProtocolReversed(String str, int end)
	{
		if (end < 4)
			return -1;
		int schemeEnd = end - 3;
		if (str.charAt(schemeEnd) != ':' || str.charAt(end - 2) != '/' || str.charAt(end - 1) != '/')
			return -1;
		// ftp, irc, http, file, data, https, mailto
		int schemeBegin = schemeEnd - 3;
		if (schemeBegin < 0)
			return -1;
		String scheme = str.substring(schemeBegin, schemeEnd);
		if (scheme.equalsIgnoreCase("ftp") || scheme.equalsIgnoreCase("irc"))
			return schemeBegin;
		
		schemeBegin = schemeEnd - 4;
		if (schemeBegin < 0)
			return -1;
		scheme = str.substring(schemeBegin, schemeEnd);
		if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("file") || scheme.equalsIgnoreCase("data"))
			return schemeBegin;
		
		schemeBegin = schemeEnd - 5;
		if (schemeBegin < 0)
			return -1;
		scheme = str.substring(schemeBegin, schemeEnd);
		if (scheme.equalsIgnoreCase("https"))
			return schemeBegin;
		
		schemeBegin = schemeEnd - 6;
		if (schemeBegin < 0)
			return -1;
		scheme = str.substring(schemeBegin, schemeEnd);
		if (scheme.equalsIgnoreCase("mailto"))
			return schemeBegin;
		return -1;
	}
	
	private static boolean hasNumber(String str)
	{
		int length = str.length();
		for (int i = 0; i < length; i++)
			if (Character.isDigit(str.charAt(i)))
				return true;
		return false;
	}
	
	private static boolean isValidIP(String str, int begin, int end)
	{
		if (end - begin < 7)
			return false;
		int val = 0;
		int count = 1;
		for (int i = begin; i < end; i++)
		{
			char c = str.charAt(i);
			if (c == '.') {
				count++;
				val = 0;
				continue;
			}
			if (!Character.isDigit(c))
				return false;
			int digit = c - '0';
			val = val * 10 + digit;
			if (val > 255)
				return false;
		}
		if (count != 4 || str.charAt(end - 1) == '.')
			return false;
		return true;
	}
	
	private static int selectDomain(String str, int dBegin)
	{
		int length = str.length();
		if (length <= dBegin || !isDomainBorder(str.charAt(dBegin)))
			return -1;
		dBegin++;
		while (dBegin < length && isDomain(str.charAt(dBegin)))
			dBegin++;
		if (!isDomainBorder(str.charAt(dBegin - 1)))
			return -1;
		return dBegin;
	}
	private static int selectDomainReversed(String str, int dEnd)
	{
		dEnd--;
		if (dEnd < 0 || !isDomainBorder(str.charAt(dEnd)))
			return -1;
		dEnd--;
		while (0 <= dEnd && isDomain(str.charAt(dEnd)))
			dEnd--;
		if (!isDomainBorder(str.charAt(dEnd + 1)))
			return -1;
		return dEnd + 1;
	}

	private static boolean isDomainBorder(char c)
	{
		if ('0' <= c && c <= '9' || 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z')
			return true;
		// TODO allow cyrillic and etc
		if ('à' <= c && c <= 'ÿ' || 'À' <= c && c <= 'ß')
			return true;
		return false;
	}
	
	private static boolean isDomain(char c)
	{
		return isDomainBorder(c) || c == '-';
	}
}
