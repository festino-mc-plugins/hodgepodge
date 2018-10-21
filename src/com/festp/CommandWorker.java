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
	
	String item_command = "item";
	String old_item_command_usage = "Usage: /item <type> [damage] [enchantments, name, ...]";
	String item_command_usage = ChatColor.GRAY+"Usage: /item "+ChatColor.GRAY+"type "+ChatColor.DARK_GRAY+"damage "+ChatColor.DARK_GRAY+"enchantments, name, ...";
	String item_command_examples = ChatColor.GRAY + "Примеры:\n"
			+ "  /item bow 100% power5 durability3 mending flame \"bow name\"\n"
			+ "  /item удочка 10 приманка3 везучий_рыбак3 прочность2 \"имя удочки\"";
	
	List<String> en_item_names;
	List<String> ru_item_names;
	List<Material> item_materials;

	List<String> en_ench_names;
	List<String> ru_ench_names;
	List<Enchantment> enchantments;
	
	public CommandWorker(mainListener plugin) {
		this.plugin = plugin;
		en_item_names = Arrays.asList(new String[] { "diamond_pickaxe", "diamond_axe", "diamond_shovel", "diamond_hoe", "diamond_sword",
				"diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots", "elytra",
				"bow", "fishing_rod", "shears", "flint_n_steel" });
		ru_item_names = Arrays.asList(new String[] { "алмазная_кирка", "алмазный_топор", "алмазная_лопата", "алмазная_мотыга", "алмазный_меч",
				"алмазный_шлем", "алмазный_нагрудник", "алмазные_штаны", "алмазные_ботинки", "элитра",
				"лук", "удочка", "ножницы", "зажигалка" });
		item_materials = Arrays.asList(new Material[] { Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HOE, Material.DIAMOND_SWORD,
				Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS, Material.ELYTRA,
				Material.BOW, Material.FISHING_ROD, Material.SHEARS, Material.FLINT_AND_STEEL });

		en_ench_names = Arrays.asList(new String[] { "power", "flame", "infinity", "punch",
				"curse_of_binding", "curse_of_canishing", "unbreaking", "mending", "thorns",
				"chanelling", "impaling", "loyalty", "riptide",
				"sharpness", "bane_of_arthropods", "smite", "knockback", "looting", "fire_aspect", "sweeping_edge",
				"efficiency", "fortune", "silk_touch",
				"protection", "blast_protection", "fire_protection", "projectile_protection", "respiration", "aqua_affinity",
				"luck_of_the_sea", "lure",
				"depth_strider", "frost_walker", "feather_falling" });
		ru_ench_names = Arrays.asList(new String[] { "сила", "огненные_стрелы", "бесконечность", "откидывание",
				"проклятье_несъёмности", "проклятие_утраты", "прочность", "починка", "шипы",
				"громовержец", "пронзатель", "верность", "тягун",
				"острота", "бич_членистоногих", "небесная_кара", "отдача", "добыча", "заговор_огня", "разящий_клинок",
				"эффективность", "удача", "шёлковое_касание",
				"защита", "взрывоустойчивость", "огнеупорность", "защита_от_снарядов", "подводное_дыхание", "подводник",
				"везучий_рыбак", "приманка",
				"подводная_ходьба", "ледоход", "невесомость" });
		enchantments = Arrays.asList(new Enchantment[] { Enchantment.ARROW_DAMAGE, Enchantment.ARROW_FIRE, Enchantment.ARROW_INFINITE, Enchantment.ARROW_KNOCKBACK,
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
		if(cmd.getName().equalsIgnoreCase("fest"))
		{
			//FireTime = Integer.parseInt(args[0]);
			//SPEED = Double.parseDouble(args[0]);
			if(args.length == 1)
			{
				if(args[0].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					Config.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
					System.out.println("[FestPlugin] Config reloaded.");
				}
				if(args[0].equalsIgnoreCase("info"))
				{
					sender.sendMessage(ChatColor.LIGHT_PURPLE + "*Типо понятно для кого*");
				}
				if(args[0].equalsIgnoreCase("biome"))
				{
					sender.sendMessage("Temperature: " + ((Player)sender).getLocation().getBlock().getTemperature() );
				}
				return true;
			}
			else if(args.length == 2 )
			{
				if(args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("reload"))
				{
					reloadConfig();
					Config.loadConfig();
					sender.sendMessage(ChatColor.GREEN + "Конфиги обновлены.");
					System.out.println("[FestPlugin] Config reloaded.");
				}
				if(args[0].equalsIgnoreCase("date"))
				{
					for(OfflinePlayer p : Bukkit.getOfflinePlayers() )
					{
						if( args[1].equalsIgnoreCase( p.getName() ) )
						{
							sender.sendMessage(ChatColor.GREEN + "Log-ins " + p.getName() + "'s information:");
							sender.sendMessage(ChatColor.GREEN + "First log-in: " + new Date(p.getFirstPlayed()) + " " + ((p.getFirstPlayed()/3600000+3)%24) + ":" + p.getFirstPlayed()/60000%60 + ":" + p.getFirstPlayed()/1000%60 ); //ms
							sender.sendMessage(ChatColor.GREEN + "Last log-in: " + new Date(p.getLastPlayed()) + " " + ((p.getLastPlayed()/3600000+3)%24) + ":" + p.getLastPlayed()/60000%60 + ":" + p.getLastPlayed()/1000%60 );
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Игрок не найден.");
					return false;
				}
				return true;
			}
			else if(args.length >= 1 && args[0].equalsIgnoreCase("el"))
			{
				if(!sender.isOp()) {
					System.out.println("Лишь админы знают страшную тайну этой команды!");
					return false;
				}
				Material m;
				if(args.length == 9)
				{
					try {
						m = Material.getMaterial(args[8]);
					} catch (Exception e) {
						m = Material.getMaterial(args[8]);
					}
					if(m == null) m = Material.LIGHT_GRAY_STAINED_GLASS;
				}
				else
					m = Material.LIGHT_GRAY_STAINED_GLASS;
				if(args.length >= 8)
				{
					double x;
					String s = args[1];
					if(s.charAt(0) == '~'){
						s = '0'+s.substring(1);
						x = ((Player)sender).getLocation().getBlockX()+Double.parseDouble(s)+0.5;}
					else
						x = Double.parseDouble(s);
					double y;
					s = args[2];
					if(s.charAt(0) == '~'){
						s = '0'+s.substring(1);
						y = ((Player)sender).getLocation().getBlockY()+Double.parseDouble(s)+0.5;}
					else
						y = Double.parseDouble(s);
					double z;
					s = args[3];
					if(s.charAt(0) == '~'){
						s = '0'+s.substring(1);
						z = ((Player)sender).getLocation().getBlockZ()+Double.parseDouble(s)+0.5;}
					else
						z = Double.parseDouble(s);
					double a = Double.parseDouble(args[4]);
					double b = Double.parseDouble(args[5]);
					double c = Double.parseDouble(args[6]);
					double r = Double.parseDouble(args[7]);
					Location l = new Location(((Player)sender).getWorld(),x,y,z);
					long count = 0;
					for(int i=(int)Math.round(-r*a);i<=r*a;i++){
						for(int j=(int)Math.round(-r*b);j<=r*b;j++){
							for(int o=(int)Math.round(-r*c);o<=r*c;o++){
								if(i*i/a/a+j*j/b/b+o*o/c/c <= r*r)
								{
									count += 1;
									((Player)sender).getWorld().getBlockAt((int)Math.ceil(x+i)-1, (int)Math.ceil(y+j)-1, (int)Math.ceil(z+o)-1).setType(Material.LIGHT_GRAY_STAINED_GLASS);
								}
							}
						}
					}
					sender.sendMessage(count + " блоков заменено");
				}
				else
					sender.sendMessage(ChatColor.RED + "Использование: /fest el <x> <y> <z> <a> <b> <c> <r>");
			}
		}
		//         /item <type> [damage] [enchantments, name, ...]
	    //example: /item diam_pix half ef5 silk2 "KAPATEL"
	    //example: /item алм кирка половина э5 шолк2 "KAPATEL"
		else if(cmd.getName().equalsIgnoreCase("item")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Только игроки могут вводить эту команду.");
				return false;
			}
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "Только операторы могут вводить эту команду.");
				return false;
			}
			if(args.length > 0) {
				//Material
				String item_id = args[0];
				if(item_id.equals("?")) {
					sender.sendMessage(item_command_usage);
					sender.sendMessage(item_command_examples);
					return true;
				}
				int index = en_item_names.indexOf(item_id);
				if(index < 0)
					index = ru_item_names.indexOf(item_id);
				if(index < 0) {
					sender.sendMessage(ChatColor.RED + "Incorrect type \""+item_id+"\". Please follow autocompletion.");
					return false;
				}
				Material item_material = item_materials.get(index);
				ItemStack item = new ItemStack(item_material, 1);
				
				if(args.length > 1) {
					//Damage
					String durability_str = args[1];
					durability_str.replace(',', '.');
					int durability;
					try {
					    if(durability_str.endsWith("%")) {
					    	double percent = Double.parseDouble(durability_str.substring(0, durability_str.length()-1));
					    	if(percent < 0 || percent > 100) {
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
					for(int i = 2; i < args.length; i++) {
						String arg = args[i];
						
						//process names "part1 partk"
						if(arg.startsWith("\"")) { // "name
							String name = arg.substring(1);
							while(!name.endsWith("\"")) { // name"
								i++;
								if(i >= args.length) {
									sender.sendMessage(ChatColor.RED + "Unclosed quotes(\"..._): \"" + name);
									return false;
						    	}
								name = name + " " + args[i];
							}
							name = name.substring(0, name.length()-1);
							if(name.length() > 35) {
								sender.sendMessage(ChatColor.RED + "Max word length is 35 (current is "+name.length()+"). "
							+"\""+ChatColor.GRAY+name.substring(0, 35)+ChatColor.RED+"|"+ChatColor.GRAY+name.substring(35)+ChatColor.RED+"\"");
								return false;
							}
							meta.setDisplayName(name);
							continue;
						}
						
						//process enchantments
						index = en_ench_names.indexOf(arg);
						if(index < 0)
							index = ru_ench_names.indexOf(arg);
						
						if(index >= 0) { //first levels
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
							for(level_start_index = arg.length()-1; level_start_index > 1; level_start_index--) {
								ench_name = arg.substring(0, level_start_index);
								index = en_ench_names.indexOf(ench_name);
								if(index < 0)
									index = ru_ench_names.indexOf(ench_name);
								if(index >= 0) {
									break;
								}
							}
							if(index < 0) {
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
				if(player_inv.getItemInMainHand() == null || player_inv.getItemInMainHand().getType() == Material.AIR)
					player_inv.setItemInMainHand(item);
				else if(player_inv.getItemInOffHand() == null || player_inv.getItemInOffHand().getType() == Material.AIR)
					player_inv.setItemInOffHand(item);
				else {
					for(int i = 0; i < 36; i++) {
						if(player_inv.getItem(i) == null) {
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
		//item <LISTED type> [LIMITED damage] [LISTED enchantment] <enchantment level(merged or separate)> [name MAX 35 CHARS]
		if( cmd.getName().equalsIgnoreCase("item") )
		{
			if(!(sender instanceof Player) || !sender.isOp())
				return null;
			if(args.length == 1) {
				String arg = args[0].toLowerCase();
				for(String item_name : en_item_names)
					if(item_name.contains(arg))
						options.add(item_name);
				for(String item_name : ru_item_names)
					if(item_name.contains(arg))
						options.add(item_name);
			}
			else if(args.length == 2) {
				String item_id = args[0];
				int index = en_item_names.indexOf(item_id);
				if(index < 0)
					index = ru_item_names.indexOf(item_id);
				if(index < 0) {
					sender.sendMessage(ChatColor.RED + "Incorrect type \""+item_id+"\". Please follow autocompletion.");
					return null;
				}
				Material item_material = item_materials.get(index);
				options.add(String.valueOf(item_material.getMaxDurability()));
				options.add("100%");
			}
			else if(args.length > 2) {
				boolean unclosed_quotes = false;
				for(int i = args.length-1; i >= 2; i--) {
					if(args[i].endsWith("\"")) {
						unclosed_quotes = false;
						break;
					}
					if(args[i].startsWith("\"")) {
						unclosed_quotes = true;
						break;
					}
				}
				if(unclosed_quotes)
					return null;
				String arg = args[args.length-1].toLowerCase();
				for(String ench_name : en_ench_names)
					if(ench_name.contains(arg))
						options.add(ench_name);
				for(String ench_name : ru_ench_names)
					if(ench_name.contains(arg))
						options.add(ench_name);
			}
		}
		return options;
	}
}
