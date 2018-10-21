package com.festp.boss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_13_R2.boss.CraftBossBar;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_13_R2.Vec3D;

public class Boss {

	public String id;
	public BossType bosstype = BossType.CUSTOM;
	
	public String name;
	Entity entity;
	public EntityType entitytype;
	public Location spawn;

	public int max_distance_from_spawn = 50;
	public BossBar bar = new CraftBossBar("Boss", BarColor.PURPLE, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
	public Double hp = null;
	public Double armor = null;
	public boolean projectileIgnore = false;
	public double speedK = 1;
	public ItemStack[] armoritems = new ItemStack[4];
	public ItemStack[] handitems = new ItemStack[2];
	public RandomLoot[] drop = new RandomLoot[0];
	public Double hpregen = null;
	public Double damage = null;
	public PotionEffect[] effectsOnAttack = new PotionEffect[0];
	public double as;
	public int xp = 0;
	public List<PotionEffect> effects = new ArrayList<>();
	
	public long respawnTime = 24000;
	public long fullTimeDeath = -respawnTime;
	
	public Boss(String id, String name, EntityType et, Location spawn)
	{
		this.id = id;
		this.name = name;
		this.entitytype = et;
		this.spawn = spawn;
		this.bar = new CraftBossBar(name, BarColor.PURPLE, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
	}
	
	public Boss(String id, String name, EntityType et, Location spawn, double hp, double hpregen, double armor, double speedK)
	{
		this(id, name, et, spawn);
		this.hp = hp;
		this.hpregen = hpregen;
		this.armor = armor;
		this.speedK = speedK;
	}
	
	public Boss(String id, String name, EntityType et, Location spawn, double hp, double hpregen, double armor, double speedK, double damage, double attack_speed)
	{
		this(id, name, et, spawn, hp, hpregen, armor, speedK);
		this.damage = damage;
		this.as = attack_speed;
	}
	
	public Boss(String id, Location l) {
		this.id = id;
		this.spawn = l;
	}

	public void setArmor(ItemStack[] armorslots) {
		if(armorslots.length == 4)
			this.armoritems = armorslots;
	}
	
	public void setHands(ItemStack[] handslots) {
		if(handslots.length == 2)
			this.handitems = handslots;
	}
	
	public LivingEntity spawn() {
		entity = spawn.getWorld().spawnEntity(spawn, entitytype);
		/*((CraftEntity)entity).getHandle().length *= 1.5;
		((CraftEntity)entity).getHandle().width *= 1.5;*/
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);
		if(hp != null) {
			((LivingEntity)entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
			((LivingEntity)entity).setHealth(hp);
		}
		if(armor != null) ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armor);
		((LivingEntity)entity).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue( speedK*((LivingEntity)entity).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() );
		if(damage != null) ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damage);
		//((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(as);
		((LivingEntity)entity).getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.95);
		((LivingEntity)entity).getEquipment().setItemInMainHand(handitems[0]);
		((LivingEntity)entity).getEquipment().setItemInOffHand(handitems[1]);
		((LivingEntity)entity).getEquipment().setArmorContents(armoritems);
		((LivingEntity)entity).getEquipment().setHelmetDropChance(0);
		((LivingEntity)entity).getEquipment().setChestplateDropChance(0);
		((LivingEntity)entity).getEquipment().setLeggingsDropChance(0);
		((LivingEntity)entity).getEquipment().setBootsDropChance(0);
		((LivingEntity)entity).getEquipment().setItemInMainHandDropChance(0);
		((LivingEntity)entity).getEquipment().setItemInOffHandDropChance(0);
		((LivingEntity)entity).addPotionEffects(effects);
		//System.out.println( ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue()+" "+((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue() );
		return (LivingEntity) entity;
	}
	
	public void addEffect(PotionEffect pe) {
		LivingEntity le = (LivingEntity)entity;
		if(le.hasPotionEffect(pe.getType())) le.removePotionEffect(pe.getType());
		for(int i=0; i<effects.size(); i++)
			if( effects.get(i).getType() == pe.getType() )
				effects.remove(i);
		effects.add(pe);
		le.addPotionEffect(pe);
	}
	
	public void bossTick() {
		bar.removeAll();
		if(entity != null && !entity.isDead())
		{
			if( Math.abs(entity.getLocation().getBlockX() - spawn.getBlockX()) > max_distance_from_spawn || 
					Math.abs(entity.getLocation().getBlockY() - spawn.getBlockY()) > max_distance_from_spawn ||
					Math.abs(entity.getLocation().getBlockZ() - spawn.getBlockZ()) > max_distance_from_spawn)
			{
				entity.teleport(spawn);
			}
			
			for( Entity e : entity.getNearbyEntities(max_distance_from_spawn, max_distance_from_spawn, max_distance_from_spawn) )
				if(e instanceof Player)
					bar.addPlayer( (Player)e );
			
			((LivingEntity)entity).setHealth( Math.min( ((LivingEntity)entity).getHealth() + hpregen, ((LivingEntity)entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() ) );

			bar.setProgress( ((LivingEntity)entity).getHealth() / hp );
		}
		else if(spawn.getWorld().getFullTime()-fullTimeDeath >= respawnTime)
		{
			spawn();
		}
	}
	
	public Boss copy(String id2) {
		Boss newboss = new Boss(id2, name, entitytype, spawn, hp, hpregen, armor, speedK);
		newboss.damage = damage;
		newboss.as = as;
		newboss.max_distance_from_spawn = max_distance_from_spawn;
		newboss.armoritems = new ItemStack[] {armoritems[0].clone(),armoritems[1].clone(),armoritems[2].clone(),armoritems[3].clone()};
		newboss.handitems = new ItemStack[] {handitems[0].clone(),handitems[1].clone()};
		newboss.drop = new RandomLoot[drop.length];
		for(int i=0;i<drop.length;i++)
			newboss.drop[i] = drop[i].clone();
		return newboss;
	}

	public Entity getEntity() {
		return entity;
	}
	
	public double distance2(Entity e1, Entity e2) {
		double dx = e2.getLocation().getX()-e1.getLocation().getX();
		double dy = e2.getLocation().getY()-e1.getLocation().getY();
		double dz = e2.getLocation().getZ()-e1.getLocation().getZ();
		return dx*dx+dy*dy+dz*dz;
	}
	
	public boolean hasLineOfSight(Location l2) {
		Location l1 = entity.getLocation();
		//x = x1(1-t) + x2(t), integer x, minimal next t
		return false;
		//return ((CraftEntity)entity).getHandle().world.rayTrace(new Vec3D(l1.getX(), l1.getY() + /*((CraftEntity)entity).getHandle().getHeadHeight()*/1.6, l1.getZ()), new Vec3D(l2.getX(), l2.getY(), l2.getZ()), false, true, false) == null;
	}
	
	public boolean hasLineOfSight(int x, int y, int z) {
		Location l1 = entity.getLocation();
		return false;
		//return ((CraftEntity)entity).getHandle().world.rayTrace(new Vec3D(l1.getX(), l1.getY() + /*((CraftEntity)entity).getHandle().getHeadHeight()*/1.6, l1.getZ()), new Vec3D(x, y, z), false, true, false) == null;
	}
	
	public void negateEffects(Player p) {
		List<PotionEffect> pelist = (List<PotionEffect>) p.getActivePotionEffects();
		for(int i=pelist.size()-1; i>=0; i--) {
			PotionEffectType pet = pelist.get(i).getType();
			if(pet.equals(PotionEffectType.ABSORPTION)) System.out.println(pelist.get(i).getAmplifier());
			if(pelist.get(i).getAmplifier() < 128)
				if(pet.equals(PotionEffectType.ABSORPTION)) {
					p.damage(4);
					pelist.remove(i);
					//pelist.set(i, new PotionEffect(PotionEffectType.HEALTH_BOOST, pelist.get(i).getDuration(), 128+pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				} else if(pet.equals(PotionEffectType.DAMAGE_RESISTANCE))
					pelist.set(i, new PotionEffect(PotionEffectType.ABSORPTION, pelist.get(i).getDuration(), 128+pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.FAST_DIGGING))
					pelist.set(i, new PotionEffect(PotionEffectType.SLOW_DIGGING, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.FIRE_RESISTANCE))
					pelist.set(i, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, pelist.get(i).getDuration(), 128+pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.HEALTH_BOOST))
					pelist.remove(i);
					//pelist.set(i, new PotionEffect(PotionEffectType.HEALTH_BOOST, pelist.get(i).getDuration(), -1-pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.INCREASE_DAMAGE))
					pelist.set(i, new PotionEffect(PotionEffectType.WEAKNESS, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.INVISIBILITY))
					pelist.set(i, new PotionEffect(PotionEffectType.GLOWING, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.JUMP))
					pelist.set(i, new PotionEffect(PotionEffectType.JUMP, pelist.get(i).getDuration(), 128+pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.LUCK))
					pelist.set(i, new PotionEffect(PotionEffectType.UNLUCK, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.NIGHT_VISION))
					pelist.set(i, new PotionEffect(PotionEffectType.BLINDNESS, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.REGENERATION))
					pelist.set(i, new PotionEffect(PotionEffectType.POISON, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.SATURATION))
					pelist.set(i, new PotionEffect(PotionEffectType.HUNGER, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				else if(pet.equals(PotionEffectType.SPEED))
					pelist.set(i, new PotionEffect(PotionEffectType.SLOW, pelist.get(i).getDuration(), pelist.get(i).getAmplifier(), pelist.get(i).isAmbient(), pelist.get(i).hasParticles()));
				//else if(pet == PotionEffectType.WATER_BREATHING) ;
			else continue;
			p.removePotionEffect(pet);
		}
		p.addPotionEffects(pelist);
		
		
		/*PotionEffect[] pelist =p.getActivePotionEffects().toArray(new PotionEffect[0]);
		for(int i=pelist.length-1; i>=0; i--) {
			PotionEffectType pet = pelist[i].getType();
			if(pet == PotionEffectType.ABSORPTION)
				pelist[i] = new PotionEffect(PotionEffectType.HEALTH_BOOST, pelist[i].getDuration(), -pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.DAMAGE_RESISTANCE)
				pelist[i] = new PotionEffect(PotionEffectType.ABSORPTION, pelist[i].getDuration(), -pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.FAST_DIGGING)
				pelist[i] = new PotionEffect(PotionEffectType.SLOW_DIGGING, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.FIRE_RESISTANCE)
				pelist[i] = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, pelist[i].getDuration(), -pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.HEALTH_BOOST)
				pelist[i] = new PotionEffect(PotionEffectType.HEALTH_BOOST, pelist[i].getDuration(), -pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.INCREASE_DAMAGE)
				pelist[i] = new PotionEffect(PotionEffectType.WEAKNESS, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.INVISIBILITY)
				pelist[i] = new PotionEffect(PotionEffectType.GLOWING, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.JUMP)
				pelist[i] = new PotionEffect(PotionEffectType.JUMP, pelist[i].getDuration(), -pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.LUCK)
				pelist[i] = new PotionEffect(PotionEffectType.UNLUCK, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.NIGHT_VISION)
				pelist[i] = new PotionEffect(PotionEffectType.BLINDNESS, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.REGENERATION)
				pelist[i] = new PotionEffect(PotionEffectType.POISON, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.SATURATION)
				pelist[i] = new PotionEffect(PotionEffectType.HUNGER, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			else if(pet == PotionEffectType.SPEED)
				pelist[i] = new PotionEffect(PotionEffectType.SLOW, pelist[i].getDuration(), pelist[i].getAmplifier(), pelist[i].isAmbient(), pelist[i].hasParticles());
			//else if(pet == PotionEffectType.WATER_BREATHING) ;
			else continue;
			p.removePotionEffect(pet);
		}
		List<PotionEffect> pleasework = new ArrayList<>();
		for(int i=pelist.length-1; i>=0; i--) {
			PotionEffectType pet = pelist[i].getType();
			System.out.println(pet);
			pleasework.add(pelist[i]);
		}
		System.out.println(pleasework.size() +" "+p.getActivePotionEffects().size());
		p.addPotionEffects(pleasework);*/
	}
	
	public void setCooldowns(Player p) {
		for(ItemStack is : p.getInventory().getContents())
			p.setCooldown(is.getType(), 100);
	}
}
