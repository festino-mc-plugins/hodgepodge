package com.festp;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandWorker implements Listener, CommandExecutor, TabCompleter {
	private mainListener plugin;
	
	String main_command = "hodge";
	
	String item_command = "item";
	String item_command_usage = ChatColor.GRAY+"Usage: /item "+ChatColor.GRAY+"type "+ChatColor.DARK_GRAY+"damage "+ChatColor.DARK_GRAY+"enchantments, name, ...";
	String item_command_examples = ChatColor.GRAY + "Примеры:\n"
			+ "  /item bow 100% power5 durability3 mending flame \"bow name\"\n"
			+ "  /item удочка 10 приманка3 везучий_рыбак3 прочность2 \"имя удочки\"";
	String item_switch = "minecraft:";
	
	List<String> en_item_names;
	List<String> ru_item_names;
	List<Material> item_materials;
	
	List<String> spigot_string_ids;

	List<String> en_ench_names;
	List<String> ru_ench_names;
	List<Enchantment> enchantments;
	
	final static String name_token = "\"";
	final static int max_name_length = 35;
	
	public CommandWorker(mainListener plugin) {
		this.plugin = plugin;
		en_item_names = Arrays.asList(new String[] {
				"diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe", "diamond_sword",
				"diamond_boots", "diamond_leggings", "diamond_chestplate", "diamond_helmet", "elytra", "turtle_shell", //turtle_shell - name, turtle_helmet - id ?!
				"bow", "shield", "trident", "fishing_rod", "shears", "flint_n_steel" });
		ru_item_names = Arrays.asList(new String[] {
				"алмазная_кирка", "алмазный_топор", "алмазная_лопата", "алмазная_мотыга", "алмазный_меч",
				"алмазные_ботинки", "алмазные_штаны", "алмазный_нагрудник", "алмазный_шлем", "элитра", "черепаший_панцирь",
				"лук", "щит", "трезубец", "удочка", "ножницы", "зажигалка" });
		item_materials = Arrays.asList(new Material[] {
				Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_SWORD,
				Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET, Material.ELYTRA, Material.TURTLE_HELMET,
				Material.BOW, Material.SHIELD, Material.TRIDENT, Material.FISHING_ROD, Material.SHEARS, Material.FLINT_AND_STEEL });
		
		spigot_string_ids = new ArrayList<>();
		for (Material m : Material.values())
			spigot_string_ids.add(m.toString().toLowerCase());
		
		en_ench_names = Arrays.asList(new String[] {
				"power", "flame", "infinity", "punch",
				"curse_of_binding", "curse_of_canishing", "unbreaking", "mending", "thorns",
				"chanelling", "impaling", "loyalty", "riptide",
				"sharpness", "bane_of_arthropods", "smite", "knockback", "looting", "fire_aspect", "sweeping_edge",
				"efficiency", "fortune", "silk_touch",
				"protection", "blast_protection", "fire_protection", "projectile_protection", "respiration", "aqua_affinity",
				"luck_of_the_sea", "lure",
				"depth_strider", "frost_walker", "feather_falling" });
		ru_ench_names = Arrays.asList(new String[] {
				"сила", "огненные_стрелы", "бесконечность", "откидывание",
				"проклятье_несъёмности", "проклятие_утраты", "прочность", "починка", "шипы",
				"громовержец", "пронзатель", "верность", "тягун",
				"острота", "бич_членистоногих", "небесная_кара", "отдача", "добыча", "заговор_огня", "разящий_клинок",
				"эффективность", "удача", "шёлковое_касание",
				"защита", "взрывоустойчивость", "огнеупорность", "защита_от_снарядов", "подводное_дыхание", "подводник",
				"везучий_рыбак", "приманка",
				"подводная_ходьба", "ледоход", "невесомость" });
		enchantments = Arrays.asList(new Enchantment[] {
				Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE, Enchantment.ARROW_KNOCKBACK,
				Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE, Enchantment.DURABILITY, Enchantment.MENDING, Enchantment.THORNS,
				Enchantment.CHANNELING, Enchantment.IMPALING, Enchantment.LOYALTY, Enchantment.RIPTIDE,
				Enchantment.DAMAGE_ALL, Enchantment.DAMAGE_ARTHROPODS, Enchantment.DAMAGE_UNDEAD, Enchantment.KNOCKBACK, Enchantment.LOOT_BONUS_MOBS, Enchantment.FIRE_ASPECT, Enchantment.SWEEPING_EDGE,
				Enchantment.DIG_SPEED, Enchantment.LOOT_BONUS_BLOCKS, Enchantment.SILK_TOUCH,
				Enchantment.PROTECTION_ENVIRONMENTAL, Enchantment.PROTECTION_EXPLOSIONS, Enchantment.PROTECTION_FIRE, Enchantment.PROTECTION_PROJECTILE, Enchantment.OXYGEN, Enchantment.WATER_WORKER,
				Enchantment.LUCK, Enchantment.LURE,
				Enchantment.DEPTH_STRIDER, Enchantment.FROST_WALKER, Enchantment.PROTECTION_FALL });
	}
	
	private void reloadConfig() {
		Config.plugin().reloadConfig();
	}
	
	@EventHandler
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase(main_command))
		{
			if (args.length == 0)
			{
				
			}
			if (args.length == 1)
			{
				if (args[0].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					Config.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
				}
				if (args[0].equalsIgnoreCase("info"))
				{
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin description and source: https://github.com/festino/hodgepodge");
				}
				return true;
			}
			else if (args[0].equalsIgnoreCase("config"))
			{
				if (args.length == 2 && args[1].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					Config.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
					System.out.println("[FestPlugin] Config reloaded.");
				}
				return true;
			}
			else if (args[0].equalsIgnoreCase("storages"))
			{
				if (args.length == 2 && args[1].equalsIgnoreCase("save"))
				{
					plugin.stlist.saveStorages();
				}
				if (args.length == 3 && args[1].equalsIgnoreCase("give"))
				{
					// gives with ID or error
				}
				return true;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("item")) {
			return process_item(sender, args);
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Команда не найдена.");
			return false;
		}
		return false;
	}
	
	private Material getMaterial(String item_id) {
		Material item_material = null;
		if (item_id.length() >= item_switch.length() && item_id.substring(0, item_switch.length()).equals(item_switch)) {
			item_id = item_id.substring(item_switch.length()).toUpperCase();
			try {
				item_material = Material.valueOf(item_id);
			} catch (Exception e) {}
		}
		else {
			int index = en_item_names.indexOf(item_id);
			if (index < 0)
				index = ru_item_names.indexOf(item_id);
			if (index >= 0)
				item_material = item_materials.get(index);
		}
		return item_material;
	}

	//Only "/item". Yet.
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		List<String> options = new ArrayList<>();
		//item <LISTED type> [LIMITED damage] [LISTED enchantment] <enchantment level(merged or separate)> [name MAX 35 CHARS]
		if ( cmd.getName().equalsIgnoreCase("item") )
		{
			if (!(sender instanceof Player) || !sender.isOp())
				return null;
			//material
			if (args.length == 1) {
				String arg = args[0].toLowerCase();
				if (item_switch.startsWith(arg) && !arg.equals(item_switch))
					options.add(item_switch);
				if (arg.length() >= item_switch.length() && arg.substring(0, item_switch.length()).equals(item_switch))
				{
					arg = arg.substring(item_switch.length());
					for (String item_name : spigot_string_ids)
						if (item_name.contains(arg))
							options.add(item_switch + item_name);
				}
				else
				{
					for (String item_name : en_item_names)
						if (item_name.contains(arg))
							options.add(item_name);
					for (String item_name : ru_item_names)
						if (item_name.contains(arg))
							options.add(item_name);
				}
			}
			//damage
			else if (args.length == 2) {
				String item_id = args[0];
				
				Material item_material = getMaterial(item_id);
				if (item_material == null) {
					sender.sendMessage(ChatColor.RED + "Incorrect type \""+item_id+"\". Please follow autocompletion.");
					return null;
				}
				
				options.add(String.valueOf(item_material.getMaxDurability()));
				options.add("100%");
			}
			//enchantments and name
			else if (args.length > 2) {
				boolean unclosed_quotes = false;
				for (int i = args.length-1; i >= 2; i--) {
					if (args[i].endsWith(name_token)) {
						unclosed_quotes = false;
						break;
					}
					if (args[i].startsWith(name_token)) {
						unclosed_quotes = true;
						break;
					}
				}
				if (unclosed_quotes) {
					options.add(args[args.length-1] + name_token);
					return options;
				}
				String arg = args[args.length-1].toLowerCase();
				for (String ench_name : en_ench_names)
					if (ench_name.contains(arg))
						options.add(ench_name);
				for (String ench_name : ru_ench_names)
					if (ench_name.contains(arg))
						options.add(ench_name);
			}
		}
		return options;
	}

	/** /item {@literal<type>} [damage] [enchantments, name, ...] 
      * <br> example: /item diamond_pickaxe 50% efficiency5 silk_touch 2 "Spawner digger"
      * <br> example: /item алмазная_кирка 354 "KAPATEL" эффективность 5 шёлковое_касание 2*/
	public boolean process_item(CommandSender sender, String args[])
	{
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Только игроки могут вводить эту команду.");
			return false;
		}
		if (!sender.isOp()) {
			sender.sendMessage(ChatColor.RED + "Только операторы могут вводить эту команду.");
			return false;
		}
		if (args.length > 0) {
			//Material
			String item_id = args[0];
			if (item_id.equals("?")) {
				sender.sendMessage(item_command_usage);
				sender.sendMessage(item_command_examples);
				return true;
			}

			Material item_material = getMaterial(item_id);
			if (item_material == null) {
				sender.sendMessage(ChatColor.RED + "Incorrect type \""+item_id+"\". Please follow autocompletion.");
				return false;
			}
			
			ItemStack item = new ItemStack(item_material, 1);
			
			if (args.length > 1) {
				//Damage
				String durability_str = args[1];
				durability_str.replace(',', '.');
				int durability;
				try {
				    if (durability_str.endsWith("%")) {
				    	double percent = Double.parseDouble(durability_str.substring(0, durability_str.length()-1));
				    	if (percent < 0 || percent > 100) {
							sender.sendMessage(ChatColor.RED + "Incorrect durability percent \""+percent+"\". Please use percent from 0% to 100%.");
							return false;
				    	}
				    	durability = (int) ( percent * 0.01 * item_material.getMaxDurability());
				    }
				    else {
				    	durability = Integer.parseInt(durability_str);
				    	if (durability < 0 || durability > item_material.getMaxDurability()) {
							sender.sendMessage(ChatColor.RED + "Incorrect damage \""+durability+"\". Please use durability from 0 to "+item_material.getMaxDurability()+" for this material("+item_id+").");
							return false;
				    	}
				    }
				} catch (Exception ex) {
					sender.sendMessage(ChatColor.RED + "Incorrect durability value \""+durability_str+"\". Please use <number> (\"123\", \"1\") or <percent%> (\"1.5%\", \"50%\").");
					return false;
				}
				int damage = item_material.getMaxDurability() - durability;
				
				ItemMeta meta = item.getItemMeta();
				((Damageable)meta).setDamage(damage);
				//Name / Enchantments
				for (int i = 2; i < args.length; i++) {
					String arg = args[i];
					
					//process names "part1 part2 part3"
					if (arg.startsWith(name_token)) { // "name
						String name = arg.substring(1);
						while (!name.endsWith(name_token)) { // name"
							i++;
							if (i >= args.length) {
								sender.sendMessage(ChatColor.RED + "Unclosed quotes(" + name_token + "..._): " + name_token + name + ChatColor.GRAY + "...");
								return false;
					    	}
							name = name + " " + args[i];
						}
						name = name.substring(0, name.length()-1);
						if (name.length() > max_name_length) {
							sender.sendMessage(ChatColor.RED + "Max word length is " + max_name_length + " (current is "+name.length()+"). "
						+"\""+ChatColor.GRAY+name.substring(0, max_name_length)+ChatColor.RED+"|"+ChatColor.GRAY+name.substring(max_name_length)+ChatColor.RED+"\"");
							return false;
						}
						if (meta.hasDisplayName()) {
							sender.sendMessage(ChatColor.RED + "Double item name. ("+name_token+meta.getDisplayName()+name_token + " and "+name_token+name+name_token+")");
							return false;
						}
						meta.setDisplayName(name);
						continue;
					}
					
					//process enchantments
					int index = en_ench_names.indexOf(arg);
					if (index < 0)
						index = ru_ench_names.indexOf(arg);
					
					if (index >= 0) { //first levels
						int level = 1;
						try {
							i++;
							level = Integer.parseInt(args[i]);
						} catch (Exception e) { i--; }
						meta.addEnchant(enchantments.get(index), level, true);
					}
					else { //sharpness5
						String ench_name;
						index = -1;
						int level_start_index;
						for (level_start_index = arg.length()-1; level_start_index > 1; level_start_index--) {
							ench_name = arg.substring(0, level_start_index);
							index = en_ench_names.indexOf(ench_name);
							if (index < 0)
								index = ru_ench_names.indexOf(ench_name);
							if (index >= 0) {
								break;
							}
						}
						if (index < 0) {
							sender.sendMessage(ChatColor.RED + "Incorrect enchantment \""+arg+"\". Please follow autocompletion.");
							return false;
						}
						try {
							int level = Integer.parseInt(arg.substring(level_start_index));
							meta.addEnchant(enchantments.get(index), level, true);
						} catch (Exception ex) {
							sender.sendMessage(ChatColor.RED + "Incorrect enchantment level \""+arg.substring(level_start_index)+"\". Please think about it.");
							return false;
						}
					}
				}
				item.setItemMeta(meta);
			}
			Player p = (Player)sender;
			PlayerInventory player_inv = p.getInventory();
			if (player_inv.getItemInMainHand() == null || player_inv.getItemInMainHand().getType() == Material.AIR)
				player_inv.setItemInMainHand(item);
			else if (player_inv.getItemInOffHand() == null || player_inv.getItemInOffHand().getType() == Material.AIR)
				player_inv.setItemInOffHand(item);
			else {
				for (int i = 0; i < 36; i++) {
					if (player_inv.getItem(i) == null) {
						player_inv.setItem(i, item);
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "В инвентаре нет места под предмет.");
				return false;
			}
			return true;
		}
		sender.sendMessage(item_command_usage);
		sender.sendMessage(item_command_examples);
		return false;
	}
}
