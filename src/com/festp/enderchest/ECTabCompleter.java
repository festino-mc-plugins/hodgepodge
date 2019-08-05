package com.festp.enderchest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.festp.Main;

public class ECTabCompleter implements TabCompleter {
	public static Main plugin;
	private static final String ADMIN_CREATE = "acreate",
			CREATE = "create", DELETE = "delete", CHANGE_OWNER = "changeowner", KICK = "kick",
			INVITE = "invite", ACCEPT = "accept", LEAVE = "leave";
	public static final String CMD_FULL = "enderchest", CMD_REDUCED = "ec";
	private static final List<String> no_completion = Arrays.asList("");
	
	public ECTabCompleter(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if( cmd.getName().equalsIgnoreCase(CMD_REDUCED) || cmd.getName().equalsIgnoreCase(CMD_FULL) )
		{
			if(args.length == 1)
			{
				List<String> empty = new ArrayList<>();
				if(sender instanceof Player)
				{
					Player p = (Player)sender;
					List<String> list;
					//ungrouped
					if(plugin.ecgroup.getByNick(p.getName()) == null) list = (List<String>) plugin.getConfig().getList("enderchest.allungroupTips");
					//owner
					else if(plugin.ecgroup.getByNick(p.getName()).getOwner().equals(p.getName())) list = (List<String>) plugin.getConfig().getList("enderchest.allownerTips");
					//member
					else list = (List<String>) plugin.getConfig().getList("enderchest.allgroupTips");
					if(sender.isOp() && !list.contains(ADMIN_CREATE)) list.add(ADMIN_CREATE);
					if(list == null) {
						list = empty;
					}
					return autocomplete(list,args[0]);
				}
			}
			//-create, +-delete, +kick, +invite, +accept, -info, +-leave, +changeowner
			else if(args.length == 2)
			{
				if (args[0].equalsIgnoreCase(INVITE))
				{
					List<String> empty = new ArrayList<>();
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						EnderChest ec = plugin.ecgroup.getByNick(p.getName());
						List<String> list = getUninvitedNames(ec,p.getName());
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
				else if(args[0].equalsIgnoreCase(ACCEPT))
				{
					List<String> empty = new ArrayList<>();
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						List<String> list = new ArrayList<>();
						if(plugin.ecgroup.getByNick(p.getName()) == null) list = getGroupnames(plugin.ecgroup,p.getName());
						list.addAll(getAdminGroupnames(plugin.ecgroup,p.getName()));
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
				else if(args[0].equalsIgnoreCase(KICK))
				{
					List<String> empty = new ArrayList<>();
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						EnderChest ec = plugin.ecgroup.getByNick(p.getName());
						List<String> list = getInvitedNames(ec,p.getName());
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
				else if(args[0].equalsIgnoreCase(CHANGE_OWNER))
				{
					List<String> empty = new ArrayList<>();
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						EnderChest ec = plugin.ecgroup.getByNick(p.getName());
						List<String> list = getInGroupNames(ec,p.getName());
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
				else if(args[0].equalsIgnoreCase(LEAVE))
				{
					List<String> empty = new ArrayList<>();
					if(sender instanceof Player)
					{
						Player p = (Player)sender;
						EnderChest ec = plugin.ecgroup.getByNick(p.getName());
						List<String> list = getInGroupNames(ec,p.getName());
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
				else if(args[0].equalsIgnoreCase(DELETE))
				{
					List<String> empty = new ArrayList<>();
					if(!(sender instanceof Player) || ((Player)sender).isOp())
					{
						List<String> list = getAllGroupnames(plugin.ecgroup);
						return (list == null ? empty : autocomplete(list, args[1]));
					}
				}
			}
		}

		return no_completion;
	}

	private List<String> getUninvitedNames(EnderChest ec, String name) {
		if(ec != null && ec.getOwner() == name)
		{
			List<String> list = new ArrayList<>();
			for(Player p: plugin.getServer().getOnlinePlayers())
				if(!isInvited(ec,p.getName())) list.add(p.getName());
			if(list.size() == 0) return null;
			return list;
		}
		return null;
	}

	private List<String> getInGroupNames(EnderChest ec, String name) {
		if(ec != null && ec.getOwner() == name)
		{
			List<String> list = new ArrayList<>();
			for(String p : ec.group)
				if(p != name) list.add(p);
			if(list.size() == 0) return null;
			return list;
		}
		return null;
	}

	private List<String> getInvitedNames(EnderChest ec, String name) {
		if(ec != null && ec.getOwner() == name)
		{
			List<String> list = new ArrayList<>();
			for(String p : ec.invited)
				if(p != name) list.add(p);
			if(list.size() == 0) return null;
			return list;
		}
		return null;
	}

	private List<String> getGroupnames(EnderChestGroup g, String name) {
		List<String> list = new ArrayList<>();
		for(EnderChest ec : g.groups)
			if(isInvited(ec,name)) list.add(ec.getGroupName());
		//if(list.size() == 0) return null;
		return list;
	}

	private List<String> getAdminGroupnames(EnderChestGroup g, String name) {
		List<String> list = new ArrayList<>();
		for(EnderChest ec : g.admingroups)
			list.add(ec.getGroupName());
		for(AdminChannelPlayer ecp : plugin.admin_ecplayers)
			if(ecp.p.getName() == name)
				list.remove(ecp.adminec.getGroupName());
		return list;
	}

	private List<String> getAllGroupnames(EnderChestGroup g) {
		List<String> list = new ArrayList<>();
		for(EnderChest ec : g.admingroups)
			list.add(ec.getGroupName());
		for(EnderChest ec : g.groups)
			list.add(ec.getGroupName());
		//if(list.size() == 0) return null;
		return list;
	}

	private boolean isInvited(EnderChest ec, String name) {
		for(String n : ec.invited)
			if(n.equalsIgnoreCase(name)) return true;
		return false;
	}

	private List<String> autocomplete(List<String> list, String arg0) {
		if(arg0 != "") 
		{
			List<String> newList = new ArrayList<>();
			for(String c : list)
			{
				if(c.toLowerCase().startsWith(arg0.toLowerCase())) newList.add(c);
			}
			return newList;
		}
		
		return list;
	}
}
