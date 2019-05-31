package com.festp.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.festp.storages.Storage;
import com.festp.storages.StorageBottomless;
import com.festp.storages.StorageMultitype;
import com.festp.storages.StoragesFileManager;
import com.festp.storages.StoragesList;
import com.festp.utils.Utils;

public class StorageCommand implements Listener, CommandExecutor, TabCompleter {
	
	public final static String ST_COMMAND = "storage";
	private final static String CMD_SAVE = "save";
	private final static String CMD_HELP = "help";
	private final static String CMD_ADD = "create";
	private final static String CMD_DEL = "delete";
	private final static String CMD_OPEN = "open";
	private final static String CMD_LIST = "list";
	private final static String CMD_CLONE = "clone";
	private final static String CMD_GIVE = "get";
	private final static String CMD_INFO = "info";
	private final static String TYPE_BOTTOMLESS = "bottomless";
	private final static String TYPE_MULTITYPE = "multitype";
	private final static String KEY_HAND_ST = "hand";
	private final static String KEY_GET_ID = "id";
	private final static String KEY_GIVE_ST = "give";
	private final static String KEY_DROP_INV = "drop";
	private final static String KEY_LIST_ALL = "all";
	private final static String KEY_OPEN_INV = "inv";
	private final static String KEY_OPEN_SETTINGS = "settings";
	private final static String KEY_EXAMPLE_ID = "0";

	private final static String ERR_ADD_NEED_LVL = ChatColor.RED + "Can't get correct lvl from \"%s\".";
	private final static String ERR_ADD_INCORRECT_TYPE = ChatColor.RED + "Can't add \"%s\" storage.";
	private final static String ERR_NEED_NUMBER = ChatColor.RED + "There a number was expected.";
	private final static String ERR_GIVE_CANT_GET_ID_FROM = ChatColor.RED + "Can't get ID from ";
	private final static String ERR_GIVE_CANT_GET_BY_ID = ChatColor.RED + "Can't get storage from id ";
	private final static String ERR_GIVE_CANT_GET_ST_FROM = ChatColor.RED + "Can't get storage from ";
	private final static String ERR_ST_NOT_LOADED_FROM_ID = ChatColor.RED + "Can't load storage from id ";
	private final static String ERR_CONSOLE_CANT_CMD = ChatColor.RED + "Only players can use this command. Reason: %s";
	private final static String CONSOLE_HAVENT_HAND = ChatColor.RED + "Console hasn't hand.";
	private final static String CONSOLE_HAVENT_EYES = ChatColor.RED + "Console hasn't eyes to see inventory.";
	private final static String CONSOLE_HAVENT_LOC = ChatColor.RED + "Console hasn't location to drop.";
	private final static String CONSOLE_HAVENT_INV = ChatColor.RED + "Console hasn't inventory to get storage.";
	
	private final static String OK_SAVE = ChatColor.GREEN + "Storages were saved successfully.";
	private final static String OK_ADD_TYPE_ID = ChatColor.GREEN + "Created %s storage with id %d.";
	private final static String OK_ADD_TYPE_ID_CANT_GIVE = ChatColor.GREEN + "Created %s storage with id %d. Can't give it because of full inventory.";
	private final static String OK_ADD_TYPE_ID_GIVEN = ChatColor.GREEN + "Created and was given %s storage with id %d. ";
	private final static String OK_DEL_ID = ChatColor.GREEN + "Storage of ID %d was deleted.";
	private final static String OK_DEL_ID_DROP = ChatColor.GREEN + "Storage of ID %d was deleted and dropped its items here.";
	private final static String OK_GET_ID = ChatColor.GREEN + "ID of hand storage is %d.";
	private final static String OK_GIVE = ChatColor.GREEN + "Storage copy was given you successfully.";
	private final static String OK_CLONE = ChatColor.GREEN + "Storage copy was given you successfully.";
	private final static String OK_LIST = "List of all loaded storages:\n";
	private final static String OK_LIST_ALL = "List of all created storages:\n";
	private final static String OK_LIST_EMPTY = ChatColor.GREEN + "Requested list is empty.";

	public final static String USAGE = ChatColor.GRAY + "Usage: ";
	public final static String EN_USAGE_SAVE  = "save all loaded storages";
	public final static String EN_USAGE_ADD   = "create(and give) new storage";
	public final static String EN_USAGE_DEL   = "delete storage from hand or by ID";
	public final static String EN_USAGE_OPEN  = "open storage inventory or menu";
	public final static String EN_USAGE_CLONE = "copy storage from hand (without stacking)";
	public final static String EN_USAGE_GIVE  = "get ID from hand / get a storage copy by ID";
	public final static String EN_USAGE_LIST  = "get all loaded / all created storages";
	public final static String EN_USAGE_INFO  = "get storage info from hand or by ID";
	public final static String USAGE_SAVE = "/" +ST_COMMAND + " " +CMD_SAVE+ " - "
											+ EN_USAGE_SAVE + "\n";
	public final static String USAGE_ADD = "/" +ST_COMMAND + " "  +CMD_ADD+  " <type> [lvl] [give]"+ " - "
											+ EN_USAGE_ADD + "\n";
	public final static String USAGE_DEL = "/" +ST_COMMAND + " "  +CMD_DEL+  " <ID/\"" +KEY_HAND_ST+ "\"> [\"" +KEY_DROP_INV+ "\"]"+ " - "
											+ EN_USAGE_DEL + "\n";
	public final static String USAGE_OPEN = "/" +ST_COMMAND + " " +CMD_OPEN+ " <ID/\"" +KEY_HAND_ST+ "\"> [\"" +KEY_OPEN_INV+ "\"/\"" +KEY_OPEN_SETTINGS+ "\"]"+ " - "
											+ EN_USAGE_DEL + "\n";
	public final static String USAGE_CLONE = "/" +ST_COMMAND + " " +CMD_CLONE+ " - "
											+ EN_USAGE_CLONE + "\n";
	public final static String USAGE_GIVE = "/" +ST_COMMAND + " " +CMD_GIVE+ " <\"" +KEY_HAND_ST+ "\"/\"" +KEY_GET_ID+ "\"/ID>"+ " - "
											+ EN_USAGE_GIVE + "\n";
	public final static String USAGE_LIST = "/" +ST_COMMAND + " " +CMD_LIST+ " [\"" +KEY_LIST_ALL+ "\"]"+ " - "
											+ EN_USAGE_LIST + "\n";
	public final static String USAGE_INFO = "/" +ST_COMMAND + " "+CMD_INFO+ " [ID]"+ " - "
											+ EN_USAGE_INFO + "\n";
	public final static String USAGE_HELP = "   " + ChatColor.GRAY + USAGE_SAVE
										  + "   " + ChatColor.GRAY + USAGE_ADD
										  + "   " + ChatColor.GRAY + USAGE_DEL
										  + "   " + ChatColor.GRAY + USAGE_OPEN
										  + "   " + ChatColor.GRAY + USAGE_CLONE
										  + "   " + ChatColor.GRAY + USAGE_GIVE
										  + "   " + ChatColor.GRAY + USAGE_LIST
										  + "   " + ChatColor.GRAY + USAGE_INFO;

	private StoragesList storage_list;
	private StoragesFileManager storage_filemanager;
	
	public StorageCommand(StoragesList stlist, StoragesFileManager stmanager) {
		storage_list = stlist;
		storage_filemanager = stmanager;
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(ST_COMMAND) && sender.isOp())
		{
			//   /storage   or   /storage help
			if (args.length == 0
				|| args[0].equalsIgnoreCase(CMD_HELP))
			{
				sender.sendMessage(USAGE + "\n" + USAGE_HELP);
				return true;
			}
			//   /storage save
			if (args[0].equalsIgnoreCase(CMD_SAVE))
			{
				if (args.length == 1) {
					storage_list.saveStorages();
					sender.sendMessage(OK_SAVE);
				}
				else {
					sender.sendMessage(USAGE + USAGE_SAVE);
				}
			}
			//   /storage clone
			if (args[0].equalsIgnoreCase(CMD_CLONE))
			{
				if (args.length == 1)
				{
					if (sender instanceof Player) {
						Player p = (Player)sender;
						ItemStack hand = p.getInventory().getItemInMainHand();
						if (!Storage.isStorage(hand))
						{
							sender.sendMessage(ERR_GIVE_CANT_GET_ST_FROM + "hand.");
							return false;
						}
						Utils.giveUnstackable(p.getInventory(), hand);
						sender.sendMessage(OK_CLONE);
						return true;
					}
					else {
						sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_HAND) );
						return false;
					}
				}
				else {
					sender.sendMessage(USAGE + USAGE_CLONE);
					return false;
				}
			}
			//   /storage get <"id"/ID>
			if (args[0].equalsIgnoreCase(CMD_GIVE))
			{
				if (args.length == 2)
				{
					if (sender instanceof Player) {
						Player p = (Player)sender;
						//   /storage get id
						if (args[1].equalsIgnoreCase(KEY_GET_ID))
						{
							//get id from hand
							Integer id = 0;
							ItemStack hand = p.getInventory().getItemInMainHand();
							if (Storage.isStorage(hand))
								id = Storage.getID(hand);
							else
							{
								sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "hand.");
								return false;
							}
							
							if (id != 0)
								sender.sendMessage( String.format(OK_GET_ID, id) );
							return true;
						}
						//   /storage get <ID>
						try {
							Integer id = Integer.parseInt(args[1]);
							Storage st = storage_list.get(id);
							if (st == null) {
								sender.sendMessage(ERR_GIVE_CANT_GET_BY_ID + id);
								return false;
							}
							Utils.giveOrDrop(p.getInventory(), st.getItemStack());
							sender.sendMessage(OK_GIVE);
						} catch (Exception e) {
							sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "\"" + args[1] + "\".");
							return false;
						}
					}
					else {
						sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_INV + "/" + CONSOLE_HAVENT_HAND) );
						return false;
					}
				}
				else {
					sender.sendMessage(USAGE + USAGE_GIVE);
					return false;
				}
			}
			//   /storage create <type> [lvl] ["give"]
			if (args[0].equalsIgnoreCase(CMD_ADD))
			{
				if (args.length == 1)
				{
					sender.sendMessage(USAGE + USAGE_ADD);
					return false;
				}
				else
				{
					boolean give = false, was_given = false;
					Player p = null;
					Storage st = null;
					if (args[1].equalsIgnoreCase(TYPE_BOTTOMLESS))
					{
						if (args.length == 2 || args.length == 3)
						{
							if (args.length == 3)
								if (args[2].equalsIgnoreCase(KEY_GIVE_ST))
								{
									if (!(sender instanceof Player))
									{
										sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_INV) );
										return false;
									}
									p = (Player)sender;
									give = true;
								}
								else
								{
									sender.sendMessage(USAGE + USAGE_ADD);
									return false;
								}
							st = new StorageBottomless(StoragesFileManager.nextID,
									Utils.getPlugin().mainworld.getFullTime(),
									Material.AIR);
						}
						else
						{
							sender.sendMessage(USAGE + USAGE_ADD);
							return false;
						}
					}
					else if (args[1].equalsIgnoreCase(TYPE_MULTITYPE))
					{
						if (args.length == 3 || args.length == 4)
						{
							if (args.length == 4)
								if (args[3].equalsIgnoreCase(KEY_GIVE_ST))
								{
									if (!(sender instanceof Player))
									{
										sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_INV) );
										return false;
									}
									p = (Player)sender;
									give = true;
								}
								else
								{
									sender.sendMessage(USAGE + USAGE_ADD);
									return false;
								}
							try {
								Integer lvl = Integer.parseInt(args[2]);
								st = new StorageMultitype(StoragesFileManager.nextID,
										Utils.getPlugin().mainworld.getFullTime(),
										lvl);
							} catch (Exception e) {
								sender.sendMessage(ERR_NEED_NUMBER + " " + String.format(ERR_ADD_NEED_LVL, args[2]) );
								return false;
							}
						}
						else
						{
							sender.sendMessage( String.format(ERR_ADD_NEED_LVL, "") );
							return false;
						}
					}
					else
					{
						sender.sendMessage( String.format(ERR_ADD_INCORRECT_TYPE, args[1]) );
						return false;
					}
					
					if (st != null)
					{
						if (give)
							was_given = Utils.giveUnstackable(p.getInventory(), st.getItemStack());
						st.saveToFile();
						StoragesFileManager.nextID++;
						if (was_given)
							sender.sendMessage(String.format(OK_ADD_TYPE_ID_GIVEN, args[1], st.getID()));
						else if (give)
							sender.sendMessage(String.format(OK_ADD_TYPE_ID_CANT_GIVE, args[1], st.getID()));
						else
							sender.sendMessage(String.format(OK_ADD_TYPE_ID, args[1], st.getID()));
						return true;
					}
				}
			}
			//   /storage delete <ID/"hand"> ["drop"]
			if (args[0].equalsIgnoreCase(CMD_DEL))
			{
				if (args.length == 1)
				{
					sender.sendMessage(USAGE + USAGE_DEL);
					return false;
				}
				else if (args.length == 2 || args.length == 3)
				{
					boolean drop = false;
					if (args.length == 3)
					{
						if (!args[2].equalsIgnoreCase(KEY_DROP_INV))
						{
							sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_LOC) );
							return false;
						}
						if ( !(sender instanceof Player) )
						{
							sender.sendMessage(USAGE + USAGE_DEL);
							return false;
						}
						drop = true;
					}
					Integer id = 0;
					boolean from_hand = false;
					//   /storage get hand ["drop"]
					if (args[1].equalsIgnoreCase(KEY_HAND_ST))
					{
						if (sender instanceof Player) {
							Player p = (Player)sender;
							ItemStack hand = p.getInventory().getItemInMainHand();
							if (!Storage.isStorage(hand))
							{
								sender.sendMessage(ERR_GIVE_CANT_GET_ST_FROM + "hand.");
								return false;
							}
							from_hand = true;
							id = Storage.getID(hand);
						}
						else {
							sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_HAND) );
							return false;
						}
					}
					
					if (id == 0)
						//   /storage get <ID> ["drop"]
						try {
							id = Integer.parseInt(args[1]);
						} catch (Exception e) {
							sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "\"" + args[1] + "\".");
							sender.sendMessage(USAGE + USAGE_DEL);
							return false;
						}
					if (drop) {
						Player p = (Player)sender;
						Storage storage = storage_list.get(id);
						if (storage != null)
							storage.drop(p.getLocation());
					}
					boolean removed = storage_filemanager.deleteDataFile(id);
					storage_list.remove(id);
					if (removed)
					{
						if (from_hand)
							((Player)sender).getInventory().setItemInMainHand(null);
						
						if (drop)
							sender.sendMessage( String.format(OK_DEL_ID_DROP, id) );
						else
							sender.sendMessage( String.format(OK_DEL_ID, id) );
					}
					return true;
				}
			}
			//   /storage open <ID/"hand"> ["inv"/"settings"]
			if (args[0].equalsIgnoreCase(CMD_OPEN))
			{
				if (!(sender instanceof Player))
				{
					sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_EYES) );
					return false;
				}
				Player p = (Player)sender;
				
				if (args.length == 1 || args.length >= 4) {
					sender.sendMessage(USAGE + USAGE_OPEN);
					return false;
				}

				Integer id = 0;
				if (args[1].equalsIgnoreCase(KEY_HAND_ST))
				{
					ItemStack hand = p.getInventory().getItemInMainHand();
					if (Storage.isStorage(hand)) {
						id = Storage.getID(hand);
					} else {
						sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "hand.");
						return false;
					}
				}
				else {
					try {
						id = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + args[1]);
						sender.sendMessage(USAGE + USAGE_OPEN);
						return false;
					}
				}

				Storage st = storage_list.get(id);
				if (st == null) {
					sender.sendMessage(ERR_GIVE_CANT_GET_BY_ID + id);
					return false;
				}
				
				if (args.length == 3) {
					if (args[2].equalsIgnoreCase(KEY_OPEN_INV))
					{
						if (st instanceof StorageBottomless)
						{
							sender.sendMessage(ChatColor.RED + "Bottomless storage have not inventory(settings only).");
							return false;
						}
						p.openInventory(st.getInventory());
					}
					if (args[2].equalsIgnoreCase(KEY_OPEN_SETTINGS))
						p.openInventory(st.getMenu());
					return true;
				}

				if(st instanceof StorageMultitype)
					p.openInventory(st.getInventory());
				else if(st instanceof StorageBottomless)
					p.openInventory(st.getMenu());
			}
			//   /storage list ["all"]
			if (args[0].equalsIgnoreCase(CMD_LIST))
			{
				List<Storage> storages_list = null;
				boolean all = false;
				if (args.length == 1)
				{
					storages_list = storage_list.getAll();
				}
				else if (args.length == 2 && args[1].equalsIgnoreCase(KEY_LIST_ALL))
				{
					//TODO: unload new loaded storages after 10 minutes
					storages_list = new ArrayList<>();
					Integer[] id_list = storage_filemanager.getIDList().toArray(new Integer[0]);
					Arrays.sort(id_list);
					for (Integer id : id_list)
					{
						Storage st = storage_list.get(id);
						if (st != null)
							storages_list.add(st);
					}
					all = true;
				}
				
				if (storages_list != null)
				{
					String list = "";
					for (Storage st : storage_list.getAll()) {
						if (list != "")
							list += ", ";
						String type = "u";
						if (st instanceof StorageBottomless)
							type = "b";
						else if (st instanceof StorageMultitype)
							type = "m" + ((StorageMultitype)st).getLvl();
						list += st.getID() + "("+ type +")";
						//unload
					}
					if (list == "")
						sender.sendMessage(OK_LIST_EMPTY);
					else if (all)
						sender.sendMessage(ChatColor.GRAY + OK_LIST_ALL + list);
					else
						sender.sendMessage(ChatColor.GRAY + OK_LIST + list);
				}
				else
				{
					sender.sendMessage(USAGE + USAGE_LIST);
					return false;
				}
			}
			//   /storage info [ID]
			if (args[0].equalsIgnoreCase(CMD_INFO))
			{
				int id = 0;
				if (args.length == 1) {
					if (!(sender instanceof Player))
					{
						sender.sendMessage( String.format(ERR_CONSOLE_CANT_CMD, CONSOLE_HAVENT_HAND) );
						return false;
					}
					Player p = (Player)sender;
					ItemStack hand = p.getInventory().getItemInMainHand();
					if (Storage.isStorage(hand))
						id = Storage.getID(hand);
					else {
						sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "hand.");
						return false;
					}
				}
				else if (args.length == 2) {
					try {
						Integer ID = Integer.parseInt(args[1]);
						id = ID;
					} catch (Exception e) {
						sender.sendMessage(ERR_GIVE_CANT_GET_ID_FROM + "\"" + args[1] + "\".");
						sender.sendMessage(USAGE + USAGE_INFO);
						return false;
					}
				}
				else {
					sender.sendMessage(USAGE + USAGE_INFO);
					return false;
				}
				
				Storage s = storage_list.get(id);
				if (s != null)
					sender.sendMessage(s.toString());
				else
					sender.sendMessage(ERR_ST_NOT_LOADED_FROM_ID + id);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args)
	{
		List<String> options = new ArrayList<>();
		String arg = args[0];
		if (args.length == 1)
		{
			List<String> list = new ArrayList<>();
			list.add(CMD_HELP);
			list.add(CMD_ADD);
			list.add(CMD_DEL);
			list.add(CMD_CLONE);
			list.add(CMD_GIVE);
			list.add(CMD_INFO);
			list.add(CMD_LIST);
			list.add(CMD_OPEN);
			list.add(CMD_SAVE);
			for (String s : list)
				if (s.startsWith(arg))
					options.add(s);
		}
		else if (args.length >= 2)
		{
			String arg2 = args[1];
			if (arg.equalsIgnoreCase(CMD_ADD))
			{
				if (args.length == 2)
				{
					options.add(TYPE_BOTTOMLESS);
					options.add(TYPE_MULTITYPE);
				}
				if (args.length == 3)
				{
					if (arg2.equalsIgnoreCase(TYPE_BOTTOMLESS))
						options.add(KEY_GIVE_ST);
					else if (arg2.equalsIgnoreCase(TYPE_MULTITYPE))
						for (int lvl = 1; lvl <= StorageMultitype.MAX_LEVEL; lvl++)
							options.add(lvl+"");
				}
				if (args.length == 4)
				{
					if (arg2.equalsIgnoreCase(TYPE_MULTITYPE))
					{
						try {
							Integer lvl = Integer.parseInt(args[2]);
							if (lvl >= 1 && lvl <= StorageMultitype.MAX_LEVEL)
							options.add(KEY_GIVE_ST);
						} catch (NumberFormatException e) { }
					}
				}
			}
			else if (arg.equalsIgnoreCase(CMD_DEL))
			{
				if (args.length == 2)
				{
					options.add(KEY_EXAMPLE_ID);
					options.add(KEY_HAND_ST);
				}
				if (args.length == 3)
					options.add(KEY_DROP_INV);
			}
			else if (arg.equalsIgnoreCase(CMD_GIVE))
			{
				if (args.length == 2)
				{
					options.add(KEY_EXAMPLE_ID);
					options.add(KEY_GET_ID);
				}
			}
			else if (arg.equalsIgnoreCase(CMD_LIST))
			{
				if (args.length == 2)
					options.add(KEY_LIST_ALL);
			}
			else if (arg.equalsIgnoreCase(CMD_OPEN))
			{
				if (args.length == 2)
				{
					options.add(KEY_EXAMPLE_ID);
					options.add(KEY_HAND_ST);
				}
				if (args.length == 3)
				{
					options.add(KEY_OPEN_INV);
					options.add(KEY_OPEN_SETTINGS);
				}
			}
			else if (arg.equalsIgnoreCase(CMD_INFO))
			{
				if (args.length == 2)
					options.add(KEY_EXAMPLE_ID);
			}
		}
		
		return options;
	}
}
