package com.festp.boss;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_13_R2.boss.CraftBossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;


public class BossStray extends Boss {

	
	public EntityType[] summons;
	public int summonDelay = 200;
	public int summonTimer = summonDelay;
	public int maxSummons = 7;
	public int summonsR = 7;
	public int[] oneSummonGroup = new int[] {1, 3};
	
	public BossStray(String id, String name, EntityType et, Location spawn, double hp, double hpregen, double armor, double speedK, double damage, double attack_speed) {
		super(id, name, et, spawn, hp, hpregen, armor, speedK, damage, attack_speed);
		
	}
	
	public BossStray(Boss b) {
		this(b.id, b.name, b.entitytype, b.spawn, b.hp, b.hpregen, b.armor, b.speedK, b.damage, b.as);
	}
	
	public BossStray(String id, Location l) {
		super(id,l);
		this.name = "Зимогорище";
		this.entitytype = EntityType.STRAY;
		this.hp = (double) 150;
		this.hpregen = 0.125;
		this.armor = (double) 3;
		this.speedK = 1.5;
		//this.damage = 10;
		this.as = 0.66;
		this.summons = new EntityType[] {EntityType.STRAY};
		this.bar = new CraftBossBar(name, BarColor.PURPLE, BarStyle.SEGMENTED_20, BarFlag.CREATE_FOG);
		this.xp = 1000;
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
		bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 2);
		this.handitems = new ItemStack[] {bow, null};
		
		ItemStack book_dur1 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta1 = (EnchantmentStorageMeta)book_dur1.getItemMeta();
        meta1.addStoredEnchant(Enchantment.DURABILITY, 1, true);
        book_dur1.setItemMeta(meta1);
        
		ItemStack book_dur2 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta2 = (EnchantmentStorageMeta)book_dur2.getItemMeta();
        meta2.addStoredEnchant(Enchantment.DURABILITY, 2, true);
        book_dur2.setItemMeta(meta2);
        
		ItemStack book_dur3 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta3 = (EnchantmentStorageMeta)book_dur3.getItemMeta();
        meta3.addStoredEnchant(Enchantment.DURABILITY, 3, true);
        book_dur3.setItemMeta(meta3);
        
		ItemStack book_frostwalk2 = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta meta4 = (EnchantmentStorageMeta)book_frostwalk2.getItemMeta();
        meta4.addStoredEnchant(Enchantment.FROST_WALKER, 2, true);
        book_frostwalk2.setItemMeta(meta4);
        
		ItemStack pickaxe_silk2 = new ItemStack(Material.GOLDEN_PICKAXE);
		ItemMeta meta5 = pickaxe_silk2.getItemMeta();
		meta5.addEnchant(Enchantment.SILK_TOUCH, 2, true);
		pickaxe_silk2.setItemMeta(meta5);
		
		this.drop = new RandomLoot[] {
				new RandomLoot(book_dur1,1,2,0.5),
				new RandomLoot(book_dur2,1,2,0.3),
				new RandomLoot(book_dur3,0,1,0.15),
				new RandomLoot(book_frostwalk2,1,1,0.5),
				new RandomLoot(pickaxe_silk2,1,1,0.05)
		};
		//this.as = attack_speed;
	}

	public void bossTick() {
		super.bossTick();
		if(entity != null && !entity.isDead())
		{
			if(summonTimer >= summonDelay && isPlayersNearby())
			{
				//+-2 height, square radius
				Random random = new Random();
				int x = entity.getLocation().getBlockX();
				int y = entity.getLocation().getBlockY();
				int z = entity.getLocation().getBlockZ();
				int dx = random.nextInt(summonsR*2+1)-summonsR;
				int dy = random.nextInt(5)-2;
				int dz = random.nextInt(summonsR*2+1)-summonsR;
				Block b = entity.getLocation().getWorld().getBlockAt(x+dx,y+dy,z+dz);
				int N = Math.min( oneSummonGroup[0]+random.nextInt(oneSummonGroup[1]-oneSummonGroup[0]+1), maxSummons - getEntityNum(EntityType.STRAY) );
				while(N>0)
				{
					dx = random.nextInt(summonsR*2+1)-summonsR;
					dy = random.nextInt(5)-2;
					dz = random.nextInt(summonsR*2+1)-summonsR;
					if(!hasLineOfSight(x+dx,y+dy,z+dz)) continue;
					b = entity.getLocation().getWorld().getBlockAt(x+dx,y+dy,z+dz);
					if(b.getType() == Material.AIR)
					{
						entity.getLocation().getWorld().spawnEntity(b.getLocation(), this.summons[random.nextInt(this.summons.length)] );
						N-=1;
					}
				}
				summonTimer = 0;
			}
			else
				summonTimer += 1;
		}
	}

	public boolean isPlayersNearby()
	{
		for(Player p : entity.getServer().getOnlinePlayers())
			if( (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) && p.getWorld() == entity.getWorld() && distance2(entity,p) < summonsR*summonsR)
				return true;
		return false;
	}
	
	public int getEntityNum(EntityType et) {
		int n = 0;
		for(Entity e : entity.getNearbyEntities(summonsR*2, 4, summonsR*2))
			if(e.getType() == et)
				n+=1;
		return n;
	}
	
}
