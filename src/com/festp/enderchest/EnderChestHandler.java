package com.festp.enderchest;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.fusesource.jansi.Ansi.Color;

import com.festp.mainListener;
import com.festp.utils.Utils;

import somebodyelse.code.OfflinePlayerLoader;

public class EnderChestHandler implements CommandExecutor, Listener {
	
	/*private EnderChest enderchest;

	public EnderChestHandler(EnderChest enderchest) {
		this.enderchest = enderchest;
	}*/
	private mainListener pl;
	public ECConfig config = new ECConfig();
	
	public EnderChestHandler(mainListener pl) {
		this.pl = pl;
	}
	
	//ec info without ecgroup, multygroup creating and joining, delete files with groups, load files, help on commands, ec info invited by name
	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String cmdlabel, final String[] args) {
		Player p = null;

		if (sender instanceof Player)
			p = (Player) sender;
			
		if (cmdlabel.equalsIgnoreCase("enderchest") || cmdlabel.equalsIgnoreCase("ec")) {
			if(args.length == 2)
			{
				if(args[0].equalsIgnoreCase("create")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_create);
					else {
						sender.sendMessage(add(pl.ecgroup, args[1], p != null ? p.getName() : null, p.getEnderChest()));
						//sender.sendMessage(pl.group.add(args[1], p != null ? p.getUniqueId() : null, p.getEnderChest()));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("acreate")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_acreate);
					if(!sender.isOp()) sender.sendMessage(ChatColor.RED + "You must be op to create admin channels!");
					else {
						sender.sendMessage(addadmin(pl.ecgroup, args[1], p != null ? p.getName() : null));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("invite")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_invite);
					else {
						EnderChest ec = pl.ecgroup.getByNick(p != null ? p.getName() : null);
						if(ec == null) sender.sendMessage(ChatColor.RED + "You are not frightened member of any ecgroup.");
						sender.sendMessage(invite(ec, p != null ? p.getName() : null, args[1]));
						//sender.sendMessage(ec.invite(p != null ? p.getUniqueId() : null, pl.getServer().getPlayer(args[1]).getUniqueId()));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("accept")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_accept);
					else {
						EnderChest ec = pl.ecgroup.getByAdminGroupname(args[1]);
						if(ec != null) sender.sendMessage(aaccept(ec, p));
						else {
							ec = pl.ecgroup.getByGroupname(args[1]);
							if(ec == null) sender.sendMessage(ChatColor.RED + "Group with this name doesn't exists!");
							else sender.sendMessage(accept(ec, p != null ? p.getName() : null, p));
						}
					}
					return true;
				} else if(args[0].equalsIgnoreCase("kick")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_kick);
					else {
						if(p == null) { sender.sendMessage(ChatColor.RED+"Only players can kick players!"); return false; }
						EnderChest ec = pl.ecgroup.getByNick(p.getName());
						sender.sendMessage(kick(ec, p.getName(), args[1]));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("leave")) {
					if(args[1].equals("?")) sender.sendMessage(config.current_locale.usage_leaveowner);
					else {
						if(p == null) { sender.sendMessage(ChatColor.RED+"Only players can leave from groups!"); return false; }
						EnderChest ec = pl.ecgroup.getByNick(p != null ? p.getName() : null);
						sender.sendMessage(leaveowner(ec, p != null ? p.getName() : null,args[1]));
					}
					return true;
					//owners -> new owner
				} else if(args[0].equalsIgnoreCase("delete")) {
					if(args[1].equals("?")) sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest delete <groupname>\" or \"/ec delete <groupname>\"");
					else if(!sender.isOp()) sender.sendMessage(ChatColor.GRAY + "Only ops can delete another's groups! Use \"/enderchest delete\".");
					else {
						sender.sendMessage(deleteadmin(pl.ecgroup, args[1]));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("changeowner")) {
					if(args[1].equals("?")) sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest changeowner <nickname>\" or \"/ec changeowner <nickname>\"");
					else {
						EnderChest ec = pl.ecgroup.getByNick(p != null ? p.getName() : null);
						if(ec == null) sender.sendMessage(ChatColor.RED+"Only players can invite players!");
						sender.sendMessage(changeowner(ec, p != null ? p.getName() : null, args[1]));
					}
					return true;
				}
			}
			else if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("info")) {
					info(p, sender);
					return true;
				} else if(args[0].equalsIgnoreCase("leave")) {
					EnderChest ec = pl.ecgroup.getAdminByPlayer(p);
					if(ec != null) sender.sendMessage(aleave(ec, p));
					else {
						ec = pl.ecgroup.getByNick(p != null ? p.getName() : null);
						if(ec == null) sender.sendMessage(ChatColor.RED + "!");
						else sender.sendMessage(leavemember(ec, p != null ? p.getName() : null));
					}
					return true;
				} else if(args[0].equalsIgnoreCase("delete")) {
					EnderChest ec = pl.ecgroup.getByNick(p != null ? p.getName() : null);
					if(ec == null) sender.sendMessage(ChatColor.RED + "You are not frightened member of any ecgroup.");
					else {
						sender.sendMessage(delete(pl.ecgroup, ec, p != null ? p.getName() : null));
					}
					return true;
				//HELP
				} else if(args[0].equalsIgnoreCase("create")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest create <groupname>\" or \"/ec create <groupname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("acreate")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest acreate <groupname>\" or \"/ec acreate <groupname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("invite")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest invite <nickname>\" or \"/ec invite <nickname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("accept")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest accept <groupname>\" or \"/ec accept <groupname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("kick")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest kick <nickname>\" or \"/ec kick <nickname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("leave")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest leave <nickname>\" or \"/ec leave <nickname>\"");
					return true;
				} else if(args[0].equalsIgnoreCase("changeowner")) {
					sender.sendMessage(ChatColor.GRAY + "Usage: \"/enderchest changeowner <nickname>\" or \"/ec changeowner <nickname>\"");
					return true;
				}
					
			}
			if(args.length == 0 || args[0].equals("?") || args[0].equals("help")) {
				sender.sendMessage(ChatColor.GRAY + "Список команд: (/enderchest = /ec)");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest create <groupname> - создать группу.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest delete [groupname] - удалить группу.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest invite <nickname> - пригласить игрока в группу.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest kick <nickname> - выгнать игрока из группы.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest accept <groupname> - принять приглашение в группу.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest leave [new owner nickname] - выйти из группы.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest info - информация о текущей группе.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest changeowner <nickname> - сменить владельца группы.");
				sender.sendMessage(ChatColor.GRAY + "   /enderchest acreate <groupname> - создать административную группу.");
			}
			else {
				sender.sendMessage(ChatColor.GRAY + "   /enderchest <help или ?> - помощь по командам");
			}
		}
		return true;
	}

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event)
	{
		if (event.getInventory().getType() == InventoryType.ENDER_CHEST && pl.ecgroup.getByInventory(event.getInventory()) == null)
		{
			Player p = (Player)event.getPlayer();
			EnderChest ec = pl.ecgroup.getAdminByPlayer(p);
			if (ec == null) ec = pl.ecgroup.getByNick(p.getName());
			if (ec != null)
			{
				event.setCancelled(true);
				event.getPlayer().openInventory(ec.getInventory());
				sendEnderchestOpenSound(p);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (p != null) {
				if (e.getView().getType().equals(InventoryType.ENDER_CHEST) && pl.ecgroup.getByNick(p.getName()) != null) {
					sendEnderchestCloseSound(p);
				}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		for(int i = pl.admin_ecplayers.size()-1; i>=0; i--)
			if(pl.admin_ecplayers.get(i).p == e.getPlayer()) {
				//pl.admin_ecplayers.get(i).p.getEnderChest().setContents(new ItemStack[27]);
				pl.admin_ecplayers.remove(i);
			}
	}
	
	
	
	public String invite(EnderChest ec, String  sender, String invited)
	{
		if(sender == null) return (ChatColor.RED+"Only players can invite players!");
		if( !sender.equalsIgnoreCase(ec.getOwner()) ) return (ChatColor.RED+"You must be ecgroup owner for this action!");
		for(int i=0; i< ec.group.size(); i++)
			if(ec.group.get(i).equals(invited))
				return (ChatColor.GREEN+"This player is already in ecgroup.");
		for(int i=0; i< ec.invited.size(); i++)
			if(ec.invited.get(i).equals(invited))
				return (ChatColor.GREEN+"This player is already invited.");
		ec.invited.add(invited);
		return (ChatColor.GREEN+"Player \""+invited+"\" successfully invited.");
	}
	
	public String kick(EnderChest ec, String sender, String kicked)
	{
		if(sender == null) return (ChatColor.RED+"Only players can kick players!");
		if(ec == null) return (ChatColor.RED+"You are not frightened member of any ecgroup.");
		if( !sender.equalsIgnoreCase(ec.getOwner()) ) return (ChatColor.RED+"You must be ecgroup owner for this action!");
		//System.out.println("\""+sender +"\" \"" +kicked+"\"");
		if(sender.equalsIgnoreCase(kicked)) return (ChatColor.RED+"You can't kick yourself! Use /enderchest leave <nickname>");
		boolean isKicked = false;
		for(int i=0; i< ec.group.size(); i++)
			if(ec.group.get(i).equals(kicked))
			{
				ec.group.remove(i);
				isKicked = true;
			}
		for(int i=0; i< ec.invited.size(); i++)
			if(ec.invited.get(i).equals(kicked))
			{
				ec.invited.remove(i);
				isKicked = true;
			}
		if(isKicked) return (ChatColor.GREEN+"Player \""+kicked+"\" successfully kicked.");
		else return (ChatColor.RED+"Player is not invited.");
	}

	public String aleave(EnderChest ec, Player p)
	{
		if(p == null) return (ChatColor.RED+"Only players can leave from groups!");
		for(int i = 0; i < pl.admin_ecplayers.size(); i++)
			if(pl.admin_ecplayers.get(i).p == p) {
				pl.admin_ecplayers.remove(i);
				//p.getEnderChest().setContents(new ItemStack[27]);
			}
		return (ChatColor.GREEN+"You successfully left the admin ecgroup.");
	}
	
	public String leavemember(EnderChest ec, String sender)
	{
		if(sender == null) return (ChatColor.RED+"Only players can leave from groups!");
		if(ec == null) return (ChatColor.RED+"You are not frightened member of any ecgroup.");
		if( sender.equalsIgnoreCase(ec.getOwner()) ) return (ChatColor.RED+"You are owner of this ecgroup. Use \"/ec leave <new owner nickname>\" or \"/enderchest leave <new owner nickname>\".");
		pl.getServer().getPlayer(sender).getEnderChest().setContents(new ItemStack[27]);
		ec.group.remove(sender);
		return (ChatColor.GREEN+"You successfully left the ecgroup.");
	}

	public String leaveowner(EnderChest ec, String sender, String newowner)
	{
		if(sender == null) return (ChatColor.RED+"Only players can kick players!");
		if(ec == null) return (ChatColor.RED+"You are not frightened member of any ecgroup.");
		if( !sender.equalsIgnoreCase(ec.getOwner()) ) return (ChatColor.RED+"You are not owner. Use \"/ec leave\" or \"/enderchest leave\".");
		if(!ec.group.contains(newowner)) return (ChatColor.RED+"Player is not in your ecgroup.");
		if(sender.equalsIgnoreCase(newowner)) return (ChatColor.RED+"You are leaving! How you can still be owner?");
		pl.getServer().getPlayer(sender).getEnderChest().setContents(new ItemStack[27]);
		ec.setOwner(newowner);
		ec.group.remove(sender);
		return (ChatColor.GREEN+"You successfully left the ecgroup.");
	}

	public String changeowner(EnderChest ec, String sender, String newowner)
	{
		if(newowner == null) return (ChatColor.RED+"This player doesn't exists in ecgroup!");
		if(sender == null) return (ChatColor.RED+"Only players can kick players!");
		if( !sender.equalsIgnoreCase(ec.getOwner()) ) return (ChatColor.RED+"You must be ecgroup owner for this action!");
		if(!ec.group.contains(newowner)) return (ChatColor.RED+"Player is not in your ecgroup.");
		if(sender.equalsIgnoreCase(newowner)) return (ChatColor.RED+"You are already the owner. -.-");
		ec.setOwner(newowner);
		return (ChatColor.GREEN+"You successfully change owner of the ecgroup.");
	}

	public String accept(EnderChest ec, String invited, Player p)
	{
		if(invited == null) return (ChatColor.RED+"Only players can join groups!");
		if(pl.ecgroup.getByNick(invited) != null) return (ChatColor.RED+"You already have frightened ecgroup!");
		for(int i=0; i< ec.group.size(); i++)
			if(ec.group.get(i).equals(invited))
				return (ChatColor.GREEN+"You are already in this ecgroup.");
		for(int i=0; i< ec.invited.size(); i++)
			if(ec.invited.get(i).equals(invited)) {
				ec.group.add(invited);
				//p.getEnderChest().setContents(ec.getInventory().getContents());
				for(ItemStack it : p.getEnderChest().getContents())
					if(it != null && it.getType() != Material.AIR)
						Utils.giveOrDrop(p, it);
				p.getEnderChest().setContents(new ItemStack[27]);
				sendGroupjoinSound(pl.getServer().getPlayer(invited));
				return (ChatColor.GREEN+"Successfully joined ecgroup \""+ec.getGroupName()+"\".");
			}
		return (ChatColor.RED+"You are not invited to this ecgroup. :(");
	}

	public String aaccept(EnderChest ec, Player p)
	{
		if(p == null) return (ChatColor.RED+"Only players can join groups!");
		EnderChest ec2 = pl.ecgroup.getAdminByPlayer(p);
		if(ec2 == ec) return (ChatColor.RED+"You are already in this admin ecgroup.");
		if(ec2 == null) {
			pl.admin_ecplayers.add(new AdminChannelPlayer(p, ec));
		} else {
			for(AdminChannelPlayer ecp : pl.admin_ecplayers)
				if(ecp.p == p) ecp.adminec = ec;
		}
		//p.getEnderChest().setContents(new ItemStack[27]);
		sendGroupjoinSound(p);
		return (ChatColor.GREEN+"Successfully joined ecgroup \""+ec.getGroupName()+"\".");
	}
	
	public String add(EnderChestGroup g, String groupname, String owner, Inventory inv) {
		if(owner == null) return (ChatColor.RED+"Only players can create groups!");
		if(g.getByNick(owner) != null) return (ChatColor.RED+"You already have frightened ecgroup!");
		if(g.isNameUsed(groupname)) return (ChatColor.RED+"This ecgroup name is already used.");
		EnderChest ec = new EnderChest(groupname, owner, true);
		//inv.getViewers()
		Inventory inv2 = pl.getServer().createInventory(null, InventoryType.ENDER_CHEST, groupname);
		inv2.setContents(inv.getContents());
		inv.setContents(new ItemStack[27]);
		ec.setInventory(inv2);
		g.groups.add(ec);
		sendGroupcreateSound(pl.getServer().getPlayer(owner));
		return (ChatColor.GREEN+"Group \""+groupname+"\" has successfully created.");
	}
	
	public String addadmin(EnderChestGroup g, String groupname, String admin) {
		if(g.isNameUsed(groupname)) return (ChatColor.RED+"This ecgroup name is already used.");
		EnderChest ec = new EnderChest(groupname);
		Inventory inv2 = pl.getServer().createInventory(null, InventoryType.ENDER_CHEST, groupname);
		ec.setInventory(inv2);
		g.admingroups.add(ec);
		if(admin != null) sendGroupcreateSound(pl.getServer().getPlayer(admin));
		return (ChatColor.GREEN+"Admin ecgroup \""+groupname+"\" has successfully created.");
	}
	
	public String delete(EnderChestGroup g, EnderChest ec, String owner) {
		if(owner == null) return (ChatColor.RED+"Only players can delete groups!");
		if(( !owner.equalsIgnoreCase(ec.getOwner()) )) return (ChatColor.RED+"You must be ecgroup owner for this action!");
		//clear and parallel members enderchests
		for(int j=0; j<ec.group.size(); j++) {
			if(ec.group.get(j) == owner) continue;
			OfflinePlayer op = pl.getServer().getOfflinePlayer(ec.group.get(j));
			Player p;
			if(!op.isOnline()) 
				p = OfflinePlayerLoader.loadPlayer(op);
			else
				p = op.getPlayer();
			p.getEnderChest().setContents(new ItemStack[27]);
		}
		//return all group items to group owner's enderchest
		OfflinePlayer op = pl.getServer().getOfflinePlayer(ec.getOwner());
		Player p;
		if(!op.isOnline()) 
			p = OfflinePlayerLoader.loadPlayer(op);
		else
			p = op.getPlayer();
		p.getEnderChest().setContents(ec.getInventory().getContents());
		//delete groups
		g.remove(ec.getGroupName());
		pl.ecstorage.deleteDataFile(ec.getGroupName());
		return (ChatColor.GREEN+"Group has successfully deleted.");
	}
	
	public String deleteadmin(EnderChestGroup g, String groupname) {
		for(int i=0; i<g.groups.size()+g.admingroups.size(); i++) {
			EnderChest ec = i<g.groups.size() ? g.groups.get(i) : g.admingroups.get(i-g.groups.size());
			if(ec.getGroupName().equalsIgnoreCase(groupname)) {
				//clear and parallel members enderchests
				if(!ec.isadmingroup)
				{
					String owner = ec.getOwner();
					for(int j=0; j<ec.group.size(); j++) {
						if(ec.group.get(j) == owner) continue;
						OfflinePlayer op = pl.getServer().getOfflinePlayer(ec.group.get(j));
						Player p;
						if(!op.isOnline()) 
							p = OfflinePlayerLoader.loadPlayer(op);
						else
							p = op.getPlayer();
						p.getEnderChest().setContents(new ItemStack[27]);
					}
				}
				else
				{
					for(int j = pl.admin_ecplayers.size()-1; j>=0; j--) {
						if(pl.admin_ecplayers.get(j).adminec == ec) {
							pl.admin_ecplayers.get(j).p.getEnderChest().setContents(new ItemStack[27]);
							pl.admin_ecplayers.remove(j);
						}
					}
				}
				if(i<g.groups.size()) g.groups.remove(i);
				else g.admingroups.remove(i-g.groups.size());
				pl.ecstorage.deleteDataFile(groupname);
				return (ChatColor.GREEN+"Group has successfully deleted.");
			}
		}
		return (ChatColor.RED+"Can't find ecgroup with this name.");
	}
	
	public void info(Player player, CommandSender sender) {
		if(player == null) {
			sender.sendMessage(ChatColor.RED+"Only players can know info!");
			return;
		}
		EnderChest ec = pl.ecgroup.getByNick(player != null ? player.getName() : null);
		if(ec == null) {
			player.sendMessage(ChatColor.RED+"You don't part in any ecgroup.");
			return;
		}
		player.sendMessage(ChatColor.GRAY + "Your ecgroup name: " + ec.getGroupName());
		//other members
		player.sendMessage(ChatColor.GRAY + "Owner:");
		player.sendMessage(ChatColor.GRAY + "   " + ec.getOwner());
		player.sendMessage(ChatColor.GRAY + "All members:");
		for(int i=0; i<ec.group.size(); i++)
			player.sendMessage(ChatColor.GRAY + "   " + ec.group.get(i));
		//invited
		if(player.getName().equalsIgnoreCase(ec.getOwner()) && (ec.group.size() != ec.invited.size()) )
		{
			player.sendMessage(ChatColor.GRAY + "All invited:");
			if(ec.group.size() != ec.invited.size()) {
				for(int i=0; i<ec.invited.size(); i++) {
					boolean t = false;
					for(int j=0; j<ec.group.size(); j++)
						if(ec.invited.get(i).equals(ec.group.get(j))) t = true;
					if(t) continue;
					player.sendMessage(ChatColor.GRAY + "   " + ec.invited.get(i));
				}
			}
		}
	}
	
	
	
	private boolean hasItemInHand(ItemStack item) {
		if (item == null) {
			return false;
		} else {
			if (item.getType() == Material.AIR) {
				return false;
			}
		}
		return true;
	}
	
	public void sendEnderchestCloseSound(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 1F, 1F);
	}
	
	public void sendEnderchestOpenSound(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1F, 1F);	
	}
	
	public void sendGroupcreateSound(Player p) {
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);	
		//p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);	
	}
	
	public void sendGroupjoinSound(Player p) {
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);	
	}
}
