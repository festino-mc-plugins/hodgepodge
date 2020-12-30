package com.festp.misc;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PolarBear;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTables;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import com.festp.utils.Utils;

public class SaddledPolarBear {
	private static final PotionEffect EFFECT_INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 10000, 0, false, false, false);
	private static final PotionEffect EFFECT_IMMORTALITY = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5, 5, false, false, false);
	private static final PotionEffect EFFECT_WITHER = new PotionEffect(PotionEffectType.WITHER, 10000, 10, false, false, false);
	private static final Vector SADDLE_SHIFT = new Vector(0, 0.55, 0);
	
	private PolarBear main;
	private Horse controller;
	private Pig visual;

	private static final int START_DELAY = 20;
	private int cooldown = START_DELAY;
	
	public SaddledPolarBear(PolarBear saddled)
	{
		main = saddled;
		setSaddled(main);
		visual = main.getWorld().spawn(main.getLocation().add(SADDLE_SHIFT), Pig.class, new Consumer<Pig>() {
			@Override
			public void accept(Pig pig) {
				pig.setGravity(false);
				pig.setAI(false);
				pig.setSilent(true);
				pig.setSaddle(true);
				pig.setLootTable(LootTables.EMPTY.getLootTable());
				Utils.setNoCollide(pig, true);
				applyEffects(pig);
				link(pig);
			} });
	}
	
	public void addPassenger(LivingEntity le)
	{
		if (cooldown > 0)
			return;
		if (controller != null) {
			// add second passenger?
			return;
		}
		controller = main.getWorld().spawn(main.getLocation(), Horse.class, new Consumer<Horse>() {
			@Override
			public void accept(Horse horse) {
				horse.setSilent(true);
				Utils.setNoCollide(horse, true);
				horse.setAdult();
				horse.setTamed(true);
				horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
				horse.setLootTable(LootTables.EMPTY.getLootTable());
				horse.setRemoveWhenFarAway(true);
				applyEffects(horse);
				double bearSpeed = main.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() / 2.5;
				double bearHealth = main.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
				horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(bearSpeed);
				horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(bearHealth);
				link(horse);
				horse.addPassenger(le);
			} });
		updateHealth();
	}
	
	public boolean update()
	{
		if (cooldown > 0)
			cooldown--;
		if (!main.isValid() || main.isDead()) {
			remove();
			main.getWorld().dropItemNaturally(main.getLocation(), new ItemStack(Material.SADDLE));
			return false;
		}
		if (controller != null) {
			if (controller.getPassengers().size() == 0) {
				controller.remove();
				controller = null;
			} else {
				updateHealth();
				applyEffects(controller);
				main.teleport(controller.getLocation()); // TODO try move 1 tick forward, if it is not in solid or lava
				main.setVelocity(controller.getVelocity());
			}
		}
		applyEffects(visual);
		//applyWither(visual);
		visual.teleport(main.getLocation().add(SADDLE_SHIFT));
		visual.setVelocity(main.getVelocity());
		return true;
	}
	
	private void updateHealth() {
		controller.setHealth(main.getHealth());
	}
	
	public PolarBear getPolarBear() {
		return main;
	}
	
	public Pig getSaddle() {
		return visual;
	}
	
	public Horse getHorse() {
		return controller;
	}
	
	public void remove() {
		if (controller != null)
			controller.remove();
		visual.remove();
	}
	
	public static void applyEffects(LivingEntity le)
	{
		le.addPotionEffect(EFFECT_INVISIBILITY);
		le.addPotionEffect(EFFECT_IMMORTALITY);
	}
	
	public static void applyWither(LivingEntity le)
	{
		le.addPotionEffect(EFFECT_WITHER);
	}
	
	private static void setSaddled(PolarBear bear) {
		bear.getEquipment().setChestplate(new ItemStack(Material.SADDLE));
	}
	
	public static boolean isSaddled(PolarBear bear) {
		return bear.getEquipment().getChestplate().getType() == Material.SADDLE;
	}
	
	private void link(LivingEntity le) {
		ItemStack helmet = new ItemStack(Material.OAK_BUTTON);
		ItemMeta meta = helmet.getItemMeta();
		meta.setDisplayName(main.getUniqueId().toString());
		helmet.setItemMeta(meta);
		le.getEquipment().setHelmet(helmet);
	}

	public static void removeLinkedEntities(Entity bear, Chunk chunk) {
		String uuid = bear.getUniqueId().toString();
		List<Entity> entities = bear.getNearbyEntities(0.5, 0.5, 0.5);
		for (int i = entities.size() - 1; i >= 0; i--) {
			Entity en = entities.get(i);
			if (en instanceof LivingEntity) {
				ItemStack helmet = ((LivingEntity) en).getEquipment().getHelmet();
				if (helmet != null && helmet.hasItemMeta() && helmet.getItemMeta().getDisplayName().equals(uuid))
					en.remove();
			}
		}
	}
}
