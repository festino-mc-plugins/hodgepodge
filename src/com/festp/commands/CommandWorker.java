package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.festp.Config;
import com.festp.Metrics;

public class CommandWorker implements CommandExecutor, TabCompleter {
	
	private Metrics metrics;
	
	public final static String MAIN_COMMAND = "hodge";
	public final static String MAIN_USAGE = "Usage:\n"
			+ "/" + MAIN_COMMAND + " info\n"
			+ "/" + MAIN_COMMAND + " metrics\n";
	
	public CommandWorker(Metrics metrics) {
		this.metrics = metrics;
	}
	
	private void reloadConfig() {
		Config.plugin().reloadConfig();
	}
	
	private String formatLong(long n, int minDigits)
	{
		String res = "" + n;
		int dif = minDigits - res.length();
		for (int i = 0; i < dif; i++)
			res = '0' + res;
		return res;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(MAIN_COMMAND))
		{
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.GRAY + MAIN_USAGE);
				return true;
			}
			if (args[0].equalsIgnoreCase("reload"))
			{
				reloadConfig();
				Config.loadConfig();
				sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
				return true;
			}
			else if (args[0].equalsIgnoreCase("config"))
			{
				if (args.length == 2 && args[1].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					Config.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
				}
				return true;
			}
			else if (args[0].equalsIgnoreCase("info"))
			{
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin description and source: https://github.com/festino/hodgepodge");
				return true;
			}
			else if (args[0].equalsIgnoreCase("metrics"))
			{
				String res = "";
				for (int i = 0; i < Metrics.MetricCategory.values().length; i++)
					res += "\n" + metrics.get(i) + "(" + Metrics.MetricCategory.values()[i] + ")";
				
				sender.sendMessage("Metrics (tick percent): " + res);
				return true;
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Команда не найдена.");
			return false;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> options = new ArrayList<>();
		if (args.length <= 1) {
			options.add("amethyst");
			options.add("info");
			options.add("metrics");
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("amethyst")) {
				options.add("chunk");
				options.add("world");
			}
		}
		return options;
	}
}
