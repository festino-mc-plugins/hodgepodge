package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.festp.Config;
import com.festp.Main;
import com.festp.misc.AmethystChunk;
import com.festp.utils.TimeUtils;
import com.festp.utils.Utils;

public class CommandWorker implements CommandExecutor, TabCompleter {
	private Main plugin;
	
	public final static String MAIN_COMMAND = "hodge";
	public final static String MAIN_USAGE = "Usage:\n"
			+ "/" + MAIN_COMMAND + " info\n"
			+ "/" + MAIN_COMMAND + " metrics\n";
	
	public CommandWorker(Main plugin) {
		this.plugin = plugin;
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
				long time = TimeUtils.getTicks();
				long ticks = time, sec = ticks / 20, min = sec / 60, hour = min / 60, days = hour / 24;
				ticks -= sec * 20;
				sec -= min * 60;
				min -= hour * 60;
				hour -= days * 24;
				String timeStr = days + ";" + formatLong(hour, 2) + ":" + formatLong(min, 2) + ":" + formatLong(sec, 2) + "." + formatLong(ticks, 2);
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "The server has been running for " + time + " ticks. (" + timeStr + ")");
				return true;
			}
			else if (args[0].equalsIgnoreCase("metrics"))
			{
				//TODO: metrics class, no public
				if (plugin.metrics_ticks == 0) return false;
				List<Double> norm_metrics = new ArrayList<>();
				for (int i = 0; i < plugin.metrics.length; i++)
					norm_metrics.add( ((double)plugin.metrics[i]) / 1_000_000_000 * 20 / plugin.metrics_ticks);
				
				sender.sendMessage("Last " + plugin.metrics_ticks + " ticks metrics: "
						+ norm_metrics.get(0)+"(re), " + norm_metrics.get(1)+"(tasklist), " + norm_metrics.get(2)+"(groups), " + norm_metrics.get(3)+"(sleep), "
						+ norm_metrics.get(4)+"(dispensers), " + norm_metrics.get(5)+"(cauldrons), " + norm_metrics.get(6)+"(leash), " + norm_metrics.get(7)+"(ih), "
						+ norm_metrics.get(8)+"(portal), " + norm_metrics.get(9)+"(tome), " + norm_metrics.get(10)+"(storage), " + norm_metrics.get(11)+"(xp-hopper), "
						+ norm_metrics.get(12)+"(beams), " + norm_metrics.get(13)+"(ec), " + norm_metrics.get(14)+"(juke/note), " + norm_metrics.get(15)+"(ameth).");
				return true;
			}
			else if (args[0].equalsIgnoreCase("amethyst"))
			{
				if (args.length == 1)
				{
					sender.sendMessage(ChatColor.GREEN + plugin.amethyst_manager.getInfo());
				}
				if (args.length == 2 && args[1].equalsIgnoreCase("world"))
				{
					if (sender instanceof Player) {
						sender.sendMessage(ChatColor.GREEN + plugin.amethyst_manager.getInfo(((Player)sender).getWorld()));
						return true;
					}
				}
				if (args.length == 2 && args[1].equalsIgnoreCase("chunk"))
				{
					if (sender instanceof Player) {
						AmethystChunk chunk = plugin.amethyst_manager.get(((Player)sender).getLocation());
						if (chunk == null) {
							sender.sendMessage(ChatColor.GREEN + "Chunk isn't loaded");
						} else {
							sender.sendMessage(ChatColor.GREEN + "Loaded " + chunk.antispawnBlocks.size() + " blocks:");
							for (Block b : chunk.antispawnBlocks)
								sender.sendMessage(ChatColor.GREEN + "   " + b.getType() + " " + Utils.toString(b.getLocation()));
						}
						return true;
					}
				}
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
