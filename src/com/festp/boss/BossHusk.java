package com.festp.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_13_R2.boss.CraftBossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BossHusk extends Boss {


	Random random = new Random();
	public int castR = 7;
	public int castR2 = 49;
	
	public EntityType[] summons;
	public int summonDelay[] = new int[] {20, 60};
	public int summonDelay_cur = summonDelay[0]+random.nextInt(summonDelay[1]-summonDelay[0]+1);
	public int summonTimer = summonDelay_cur;
	public int maxSummons = 8;
	public int summonsR = 1;
	public int[] oneSummonGroup = new int[] {1, 1};
	
	public int fangsDelay[] = new int[] {20, 20};
	public int fangsDelay_cur = fangsDelay[0]+random.nextInt(fangsDelay[1]-fangsDelay[0]+1);
	public int fangsTimer = fangsDelay_cur;
	
	public BossHusk(String id, String name, EntityType et, Location spawn, double hp, double hpregen, double armor, double speedK, double damage, double attack_speed) {
		super(id, name, et, spawn, hp, hpregen, armor, speedK, damage, attack_speed);
		
	}
	
	public BossHusk(Boss b) {
		this(b.id, b.name, b.entitytype, b.spawn, b.hp, b.hpregen, b.armor, b.speedK, b.damage, b.as);
	}
	
	public BossHusk(String id, Location l) {
		super(id,l);
		this.name = "Кадаврище";
		this.entitytype = EntityType.HUSK;
		this.hp = (double) 500;
		this.hpregen = 0.05;
		this.armor = (double) 10;
		this.projectileIgnore = true;
		this.speedK = 0.8;
		this.damage = (double) 3;
		//this.as = 0.66;
		this.summons = new EntityType[] {EntityType.ZOMBIE_VILLAGER,EntityType.ZOMBIE,EntityType.ZOMBIE,EntityType.ZOMBIE};
		this.bar = new CraftBossBar(name, BarColor.PURPLE, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
		this.xp = 2000;
		//ambient shows the player an effect
		PotionEffect hunger = new PotionEffect(PotionEffectType.HUNGER, 200, 1, false, false);
		PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 300, 1, false, false);
		PotionEffect stun = new PotionEffect(PotionEffectType.SLOW, 100, 9, false, false);
		this.effectsOnAttack = new PotionEffect[] {weakness,hunger,stun};
		
		ItemStack book_dur1 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta1 = (EnchantmentStorageMeta)book_dur1.getItemMeta();
        meta1.addStoredEnchant(Enchantment.DURABILITY, 1, true);
        book_dur1.setItemMeta(meta1);
        
		ItemStack book_dur2 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta)book_dur2.getItemMeta();
        meta2.addStoredEnchant(Enchantment.DURABILITY, 2, true);
        book_dur2.setItemMeta(meta2);
        
		ItemStack book_thorns1 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta3 = (EnchantmentStorageMeta)book_thorns1.getItemMeta();
        meta3.addStoredEnchant(Enchantment.THORNS, 1, true);
        book_thorns1.setItemMeta(meta3);
        
		ItemStack book_thorns2 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta4 = (EnchantmentStorageMeta)book_thorns2.getItemMeta();
        meta4.addStoredEnchant(Enchantment.THORNS, 2, true);
        book_thorns2.setItemMeta(meta4);
        
		ItemStack book_def2 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta5 = (EnchantmentStorageMeta)book_def2.getItemMeta();
        meta5.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        book_def2.setItemMeta(meta5);
        
		ItemStack book_def3 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta6 = (EnchantmentStorageMeta)book_def3.getItemMeta();
        meta6.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        book_def3.setItemMeta(meta6);
        
		ItemStack book_cursewear = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta7 = (EnchantmentStorageMeta)book_cursewear.getItemMeta();
        meta7.addStoredEnchant(Enchantment.BINDING_CURSE, 2, true);
        book_cursewear.setItemMeta(meta7);

		ItemStack book_cursevanish = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta8 = (EnchantmentStorageMeta)book_cursevanish.getItemMeta();
        meta8.addStoredEnchant(Enchantment.VANISHING_CURSE, 2, true);
        book_cursevanish.setItemMeta(meta8);
        
		ItemStack shield_fangs = new ItemStack(Material.SHIELD);
		ItemMeta metash1 = shield_fangs.getItemMeta();
		metash1.addEnchant(Enchantment.DURABILITY, 3, true);
		shield_fangs.setItemMeta(metash1);
        
		ItemStack shield_playersneverdie = new ItemStack(Material.SHIELD);
		ItemMeta metash2 = shield_playersneverdie.getItemMeta();
		metash2.addEnchant(Enchantment.DURABILITY, 4, true);
		shield_playersneverdie.setItemMeta(metash2);
		
		this.drop = new RandomLoot[] {
				new RandomLoot(book_dur1,2,3,0.35),
				new RandomLoot(book_dur2,1,2,0.20),
				new RandomLoot(book_thorns1,1,2,0.20),
				new RandomLoot(book_thorns2,1,1,0.10),
				new RandomLoot(book_def2,2,3,0.25),
				new RandomLoot(book_def3,1,2,0.10),
				new RandomLoot(book_cursewear,2,3,0.35),
				new RandomLoot(book_cursevanish,4,5,0.45),
				new RandomLoot(shield_fangs,1,1,0.02),
				new RandomLoot(shield_playersneverdie,1,1,0.01)
		};
		//this.as = attack_speed;
		
		//from feet to head, with custom color
		ItemStack thorns_boots = new ItemStack(Material.LEATHER_BOOTS);
		LeatherArmorMeta meta_a0 = (LeatherArmorMeta) thorns_boots.getItemMeta();
		meta_a0.setColor(Color.fromRGB(200, 200, 20));
		meta_a0.addEnchant(Enchantment.THORNS, 3, true);
		meta_a0.setUnbreakable(true);
		thorns_boots.setItemMeta(meta_a0);
		
		ItemStack thorns_leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		LeatherArmorMeta meta_a1 = (LeatherArmorMeta) thorns_leggings.getItemMeta();
		meta_a1.setColor(Color.fromRGB(200, 200, 20));
		meta_a1.addEnchant(Enchantment.THORNS, 3, true);
		meta_a1.setUnbreakable(true);
		thorns_leggings.setItemMeta(meta_a1);
		
		ItemStack thorns_chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta_a2 = (LeatherArmorMeta) thorns_chestplate.getItemMeta();
		meta_a2.setColor(Color.fromRGB(200, 200, 20));
		meta_a2.addEnchant(Enchantment.THORNS, 3, true);
		meta_a2.setUnbreakable(true);
		thorns_chestplate.setItemMeta(meta_a2);
		
		ItemStack thorns_helmet = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta meta_a3 = (LeatherArmorMeta) thorns_helmet.getItemMeta();
		meta_a3.setColor(Color.fromRGB(200, 200, 20));
		meta_a3.addEnchant(Enchantment.THORNS, 3, true);
		meta_a3.setUnbreakable(true);
		thorns_helmet.setItemMeta(meta_a3);
		
		
		armoritems[0] = thorns_boots;
		armoritems[1] = thorns_leggings;
		armoritems[2] = thorns_chestplate;
		armoritems[3] = thorns_helmet;
	}

	public void bossTick() {
		super.bossTick();
		if(entity != null && !entity.isDead())
		{
			if(isPlayersNearby()) {
				for(Player p : getNearbyPlayers())
					negateEffects(p);
				//summon
				if(summonTimer >= summonDelay_cur)
				{
					int N = Math.min( oneSummonGroup[0]+random.nextInt(oneSummonGroup[1]-oneSummonGroup[0]+1), maxSummons - getEntityNum(summons[0]) - getEntityNum(summons[1]) );
					for(Player p : getNearbyPlayers())
					{
						int x = p.getLocation().getBlockX();
						int y = p.getLocation().getBlockY();
						int z = p.getLocation().getBlockZ();
						int dx = random.nextInt(summonsR*2+1)-summonsR;
						int dy = random.nextInt(5)-2;
						int dz = random.nextInt(summonsR*2+1)-summonsR;
						World world = entity.getLocation().getWorld();
						Block b = world.getBlockAt(x+dx,y+dy,z+dz);
						while(b.getType() != Material.AIR)
						{
							dx = random.nextInt(summonsR*2+1)-summonsR;
							dy = random.nextInt(5)-2;
							dz = random.nextInt(summonsR*2+1)-summonsR;
							if(!hasLineOfSight(x+dx,y+dy,z+dz)) continue;
							b = world.getBlockAt(x+dx,y+dy,z+dz);
						}
						world.spawnEntity(b.getLocation(), this.summons[random.nextInt(this.summons.length)] );
						N-=1;
						if(N<1) break;
					}
					summonTimer = 0;
					summonDelay_cur = summonDelay[0]+random.nextInt(summonDelay[1]-summonDelay[0]+1);
				}
				else
					summonTimer += 1;
				//spikes
				if(fangsTimer >= fangsDelay_cur)
				{
					List<Player> players = getNearbyPlayers();
					double dist2, mindist2 = -1;
					int mini = 0;
					for(int i=0; i < players.size(); i++) {
						dist2 = distance2(entity, players.get(i));
						if(mindist2 < 0 || dist2 < mindist2) {
							mindist2 = dist2;
							mini = i;
						}
					}
					System.out.println(mini);
					World world = entity.getLocation().getWorld();
					int N = 4+(players.size()-1)/3;
					while(N>0 && players.size() > 0) {
						int i = random.nextInt(players.size());
						if(mini >= 0) {
							i = mini;
							mini = -1;
						}
						EvokerFangs fangs = (EvokerFangs)world.spawnEntity(players.get(i).getLocation(),EntityType.EVOKER_FANGS);
						fangs.setOwner((LivingEntity) entity);
						players.remove(i);
						N-=1;
					}
					fangsTimer = 0;
					fangsDelay_cur = fangsDelay[0]+random.nextInt(fangsDelay[1]-fangsDelay[0]+1);
				}
				else
					fangsTimer += 1;
			}
		}
	}
	
	public LivingEntity spawn() {
		LivingEntity le = super.spawn();
		//рикошет
		//entity.set
		return le;
	}

	public boolean isPlayersNearby()
	{
		for(Player p : entity.getServer().getOnlinePlayers())
			if( (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) && p.getWorld() == entity.getWorld() && distance2(entity,p) < castR2)
				return true;
		return false;
	}

	public List<Player> getNearbyPlayers()
	{
		List<Player> list = new ArrayList<>();
		for(Player p : entity.getServer().getOnlinePlayers())
			if( (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) && p.getWorld() == entity.getWorld() && distance2(entity,p) < castR2)
				list.add(p);
		return list;
	}
	
	public int getEntityNum(EntityType et) {
		int n = 0;
		for(Entity e : entity.getNearbyEntities(castR*2, 4, castR*2))
			if(e.getType() == et)
				n+=1;
		return n;
	}
	
}
