package com.festp.boss;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.mainListener;

public class BossHandler implements CommandExecutor, Listener {

	private mainListener pl;
	private World defaultWorld;

	public static List<Boss> bosslist = new ArrayList<>();
	
	public BossHandler(mainListener pl) {
		this.pl = pl;
		defaultWorld = pl.mainworld;
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if( !(event.getEntity() instanceof LivingEntity) ) return;
		LivingEntity le = (LivingEntity)event.getEntity();
		for(Boss b : bosslist) {
			if(b.projectileIgnore && le == b.entity)
				if(event.getDamager() instanceof Arrow)
				{
					event.setCancelled(true);
					return;
				}
			if(event.getDamager() == b.entity && event.getCause() != DamageCause.THORNS)
			{
				for(PotionEffect pe : b.effectsOnAttack)
				{
					le.addPotionEffect(pe);
				}
				return;
			}
		}
	}


	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		for(Boss b : bosslist) {
			if(b.getEntity() == event.getEntity())
			{
				event.setDroppedExp(b.xp);
				event.getDrops().clear();
				for(RandomLoot rl : b.drop)
					event.getDrops().add(rl.genLoot());
				b.fullTimeDeath = event.getEntity().getWorld().getFullTime();
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
		if(cmd.getName().equalsIgnoreCase("boss") && sender.isOp())
		{
			if(args.length >= 22 && args[0].equalsIgnoreCase("set"))
			{
				if(args[1] == "~") {sender.sendMessage(ChatColor.RED + "Please enter frightened valid id."); return false;}
				String id = args[1];
				for(Boss boss : bosslist)
				{
					if(boss.id == id)
					{
						Boss b = boss.copy(id);
						if(args[2] != "~")
							b.name = args[2]; // "pp pp pp" - brackets must be closed
						if(args[3] == "~")
							b.entitytype = EntityType.valueOf(args[3]); //will be replaced by bosstype
						try{
							double x;
							double y;
							double z;
							if(args[4] == "~")  	x = b.spawn.getX();
							else  					x = Double.parseDouble(args[4]);
							if(args[5] == "~")  	y = b.spawn.getY();
							else  					y = Double.parseDouble(args[5]);
							if(args[6] == "~")  	z = b.spawn.getZ();
							else  					z = Double.parseDouble(args[6]);
							World w = sender instanceof Player ? ((Player)sender).getWorld() : defaultWorld;
							b.spawn = new Location(w, x, y, z);
						} catch(Exception e) {
							//sender.sendMessage("There was an error. \"spawn\" didn't change.");
							sender.sendMessage(ChatColor.RED + "There was an error with spawn coordinates.");
							return false;
						}
						if(args[7] != "~")
							try{
								double hp = Double.parseDouble(args[7]);
								b.hp = hp;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with hp.");
								return false;
							}
						if(args[8] != "~")
							try{
								double hpregen = Double.parseDouble(args[8]);
								b.hpregen = hpregen;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with hpregen.");
								return false;
							}
						if(args[9] != "~")
							try{
								double speedK = Double.parseDouble(args[9]);
								b.speedK = speedK;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with speedK.");
								return false;
							}
						if(args[10] != "~")
							try{
								double damage = Double.parseDouble(args[10]);
								b.damage = damage;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with damage.");
								return false;
							}
						if(args[11] != "~")
							try{
								double attack_speed = Double.parseDouble(args[11]);
								b.as = attack_speed;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with attack speed.");
								return false;
							}
						if(args[12] != "~")
							try{
								double armor = Double.parseDouble(args[12]);
								b.armor = armor;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with armor.");
								return false;
							}
						if(args[13] != "~" && args[13].contains("[") && args[13].contains("]"))
						{
							try{
								String full = args[13];
								String parts[] = full.split("[");
								if(parts.length == 2) {
									String effectname = parts[0];
									String effectamplifier = parts[1].split("]")[0];
									PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effectname),64000,Integer.parseInt(effectamplifier),true,false);
									b.addEffect(effect);
								}
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with effect.");
								return false;
							}
						}

						if(args[14] != "~")
							try{
								int xp = Integer.parseInt(args[14]);
								b.xp = xp;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with xp.");
								return false;
							}
						String r_hand = args[15];
						String l_hand = args[16];
						String head = args[17];
						String body = args[18];
						String legs = args[19];
						String boots = args[20];
						Material m;
						ItemStack[] handsslots = new ItemStack[2];
						if(r_hand == "~") handsslots[0] = b.handitems[0];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(r_hand);
							if(m == null) handsslots[0] = null;
							else handsslots[0] = new ItemStack(m);
						}
						if(l_hand == "~") handsslots[1] = b.handitems[1];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(l_hand);
							if(m == null) handsslots[1] = null;
							else handsslots[1] = new ItemStack(m);
						}
						ItemStack[] armorslots = new ItemStack[4];
						if(head == "~") armorslots[0] = b.armoritems[0];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(head);
							if(m == null) armorslots[0] = null;
							else armorslots[0] = new ItemStack(m);
						}
						if(body == "~") armorslots[1] = b.armoritems[1];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(body);
							if(m == null) armorslots[1] = null;
							else armorslots[1] = new ItemStack(m);
						}
						if(legs == "~") armorslots[2] = b.armoritems[2];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(legs);
							if(m == null) armorslots[2] = null;
							else armorslots[2] = new ItemStack(m);
						}
						if(boots == "~") armorslots[3] = b.armoritems[3];
						else {
							if(r_hand == "-") m = Material.AIR; 
							else m = Material.getMaterial(boots);
							if(m == null) armorslots[3] = null;
							else armorslots[3] = new ItemStack(m);
						}
						b.handitems = handsslots;
						b.armoritems = armorslots;
						String[] drop;
						boss = b;
						return true;
					}
				}
				////////////////////////////////////////////////CREATING NEW BOSS////////////////////////////////////////////////////
				String name = "";
				if(args[2] == "~") { sender.sendMessage("Name cannot be \"~\"."); return false;}
				else  name = args[2]; // "pp pp pp" - brackets must be closed
				EntityType et = EntityType.UNKNOWN;
				et = EntityType.valueOf(args[3]);
				Location l;
				try{
					double x = Double.parseDouble(args[4]);
					double y = Double.parseDouble(args[5]);
					double z = Double.parseDouble(args[6]);
					World w = sender instanceof Player ? ((Player)sender).getWorld() : defaultWorld;
					l = new Location(w, x, y, z);
				} catch(Exception e) {
					//sender.sendMessage("There was an error. \"spawn\" didn't change.");
					sender.sendMessage(ChatColor.RED + "There was an error with spawn coordinates.");
					return false;
				}
				double hp = 100;
				if(args[7] != "~")
					try{
						hp = Double.parseDouble(args[7]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with hp.");
						return false;
					}
				double hpregen = 0;
				if(args[8] != "~")
					try{
						hpregen = Double.parseDouble(args[8]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with hpregen.");
						return false;
					}
				double speedK = 1;
				if(args[9] != "~")
					try{
						speedK = Double.parseDouble(args[9]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with speedK.");
						return false;
					}
				double damage = 10;
				if(args[10] != "~")
					try{
						damage = Double.parseDouble(args[10]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with damage.");
						return false;
					}
				double attack_speed = 4;
				if(args[11] != "~")
					try{
						attack_speed = Double.parseDouble(args[11]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with attack speed.");
						return false;
					}
				double armor = 10;
				if(args[12] != "~")
					try{
						armor = Double.parseDouble(args[12]);
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with armor.");
						return false;
					}
				
				Boss b = new Boss(id, name, et, l, hp, hpregen, armor, speedK, damage, attack_speed);
				
				if(args[13] != "~" && args[13].contains("[") && args[13].contains("]"))
				{
					try{
						String full = args[13];
						String parts[] = full.split("[");
						if(parts.length == 2) {
							String effectname = parts[0];
							String effectamplifier = parts[1].split("]")[0];
							PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effectname),64000,Integer.parseInt(effectamplifier),true,false);
							b.addEffect(effect);
						}
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with effect.");
						return false;
					}
				}

				if(args[14] != "~")
					try{
						int xp = Integer.parseInt(args[14]);
						b.xp = xp;
					} catch(Exception e) {
						sender.sendMessage(ChatColor.RED + "There was an error with xp.");
						return false;
					}
				String r_hand = args[15];
				String l_hand = args[16];
				String head = args[17];
				String body = args[18];
				String legs = args[19];
				String boots = args[20];
				Material m;
				ItemStack[] handsslots = new ItemStack[2];
				if(r_hand == "~") handsslots[0] = b.handitems[0];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(r_hand);
					if(m == null) handsslots[0] = null;
					else handsslots[0] = new ItemStack(m);
				}
				if(l_hand == "~") handsslots[1] = b.handitems[1];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(l_hand);
					if(m == null) handsslots[1] = null;
					else handsslots[1] = new ItemStack(m);
				}
				ItemStack[] armorslots = new ItemStack[4];
				if(head == "~") armorslots[0] = b.armoritems[0];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(head);
					if(m == null) armorslots[0] = null;
					else armorslots[0] = new ItemStack(m);
				}
				if(body == "~") armorslots[1] = b.armoritems[1];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(body);
					if(m == null) armorslots[1] = null;
					else armorslots[1] = new ItemStack(m);
				}
				if(legs == "~") armorslots[2] = b.armoritems[2];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(legs);
					if(m == null) armorslots[2] = null;
					else armorslots[2] = new ItemStack(m);
				}
				if(boots == "~") armorslots[3] = b.armoritems[3];
				else {
					if(r_hand == "-") m = Material.AIR; 
					else m = Material.getMaterial(boots);
					if(m == null) armorslots[3] = null;
					else armorslots[3] = new ItemStack(m);
				}
				b.handitems = handsslots;
				b.armoritems = armorslots;
				String[] drop;
				bosslist.add(b);
				//b.spawn();
				return true;
			} else if(args.length >= 4 && args[0].equalsIgnoreCase("setai")) {
				sender.sendMessage(ChatColor.RED + "Coming soon, doesn't work now.");
			} else if(args.length == 0 || args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.RED + "Usage: /boss createdefault <boss_id> <bosstype> <x> <y> <z>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss set <boss_id> <name> <entitytype> <x> <y> <z> <hp> <hpregen/s> <speedK> <damage> <attack_speed> <armor> <effects> <xp> <r_hand> <l_hand> <head> <body> <legs> <boots> <drop[]>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss setai <boss_id> <bosstype> <params>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss copy <boss_id> <previous_boss_id> <x> <y> <z>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss respawn <boss_id>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss delete <boss_id>");
				sender.sendMessage(ChatColor.RED + "Usage: /boss list");
				return false;
			} else if(args[0].equalsIgnoreCase("createdefault")) {
				if(args.length != 6)
					sender.sendMessage(ChatColor.RED + "Usage: /boss createdefault <boss_id> <bosstype> <x> <y> <z>");
				else {
					double x,y,z;
					
					if(!args[3].contains("~"))
						try{
							x = Double.parseDouble(args[3]);
						} catch(Exception e) {
							sender.sendMessage(ChatColor.RED + "There was an error with x.");
							return false;
						}
					else if(sender instanceof Player) {
						x = ( (Player)sender ).getLocation().getX();
					} else {
						sender.sendMessage(ChatColor.RED + "Uncorrect x.");
						return false;
					}
					
					if(!args[4].contains("~"))
						try{
							y = Double.parseDouble(args[4]);
						} catch(Exception e) {
							sender.sendMessage(ChatColor.RED + "There was an error with y.");
							return false;
						}
					else if(sender instanceof Player) {
						y = ( (Player)sender ).getLocation().getY();
					} else {
						sender.sendMessage(ChatColor.RED + "Uncorrect y.");
						return false;
					}
					
					if(!args[5].contains("~"))
						try{
							z = Double.parseDouble(args[5]);
						} catch(Exception e) {
							sender.sendMessage(ChatColor.RED + "There was an error with z.");
							return false;
						}
					else if(sender instanceof Player) {
						z = ( (Player)sender ).getLocation().getZ();
					} else {
						sender.sendMessage(ChatColor.RED + "Uncorrect z.");
						return false;
					}
					
					Location l;
					if(sender instanceof Player) l = new Location(((Player)sender).getWorld(), x, y, z);
					else l = new Location(defaultWorld, x, y, z);
					
					for(int i=0; i<bosslist.size(); i++)
					{
						Boss b = bosslist.get(i);
						if(b.id.equals(args[1]))
						{
							b.bar.removeAll();
							b.entity.remove();
							bosslist.remove(i);
							break;
						}
					}
					if(args[2].equalsIgnoreCase("stray") || args[2].equalsIgnoreCase("зимогор")) {
						bosslist.add(new BossStray(args[1], l));
						sender.sendMessage(ChatColor.GREEN + "Boss Stray was created successfully.");
					} else if(args[2].equalsIgnoreCase("husk") || args[2].equalsIgnoreCase("кадавр")) {
						bosslist.add(new BossHusk(args[1], l));
						sender.sendMessage(ChatColor.GREEN + "Boss Husk was created successfully.");
					} else if(args[2].equalsIgnoreCase("squid") || args[2].equalsIgnoreCase("спрут")) {
					} else 
						sender.sendMessage(ChatColor.RED + "There are not existing such bosstype.");
				}
			} else if(args[0].equalsIgnoreCase("copy")) {
				if(args.length != 6)
					sender.sendMessage(ChatColor.RED + "Usage: /boss copy <boss_id> <previous_boss_id> <x> <y> <z>");
				else
				{
					for(Boss b : bosslist) {
						if(b.id.equals(args[1])) {
							Boss newb = b.copy(args[2]);
							Location l;
							try{
								double x = Double.parseDouble(args[4]);
								double y = Double.parseDouble(args[5]);
								double z = Double.parseDouble(args[6]);
								World w = sender instanceof Player ? ((Player)sender).getWorld() : defaultWorld;
								l = new Location(w, x, y, z);
								newb.spawn = l;
							} catch(Exception e) {
								sender.sendMessage(ChatColor.RED + "There was an error with spawn coordinates.");
								return false;
							}
							bosslist.add(newb);
							sender.sendMessage(ChatColor.GREEN + "Boss \""+args[1]+"\" was copied successfully as \""+args[2]+"\".");
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Boss with id \""+args[1]+"\" doesn't exist!");
					return false;
				}
			} else if(args[0].equalsIgnoreCase("respawn")) {
				if(args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Usage: /boss respawn <boss_id>");
				} else {
					for(Boss b : bosslist) {
						if(b.id.equals(args[1])) {
							b.entity.remove();
							b.spawn();
							sender.sendMessage(ChatColor.GREEN + "Boss \""+args[1]+"\" was respawned successfully.");
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Boss with id \""+args[1]+"\" doesn't exist!");
					return false;
				}
			} else if(args[0].equalsIgnoreCase("delete")) {
				if(args.length != 2) {
					sender.sendMessage(ChatColor.RED + "Usage: /boss delete <boss_id>");
				} else {
					for(int i=0; i<bosslist.size(); i++)
					{
						Boss b = bosslist.get(i);
						if(b.id.equals(args[1]))
						{
							b.entity.remove();
							b.bar.removeAll();
							bosslist.remove(i);
							sender.sendMessage(ChatColor.GREEN + "Boss \""+args[1]+"\" was deleted successfully.");
							return true;
						}
					}
					sender.sendMessage(ChatColor.RED + "Boss with id \""+args[1]+"\" doesn't exist!");
					return false;
				}
			} else if(args[0].equalsIgnoreCase("list")) {
				if(args.length != 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /boss list");
				}
				else
				{
					if(bosslist.isEmpty()) sender.sendMessage(ChatColor.GRAY + "No bosses was founded.");
					else sender.sendMessage(ChatColor.GRAY + "Bosses:");
					for(Boss b : bosslist)
					{
						sender.sendMessage(ChatColor.GRAY + "ID: " + b.id +", EntityType: " + b.entitytype.toString() );
					}
				}
			} else if(args[0].equalsIgnoreCase("!!!")) {
				
			}
			return false;
		}
		return false;
	}
}
