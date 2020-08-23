package com.festp.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R2.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.festp.Config;
import com.festp.Main;
import com.festp.utils.Utils;
import com.festp.utils.UtilsType;

public class FeatureHandler implements Listener {
	Main plugin;
	Server server;
	List<Player> test_spawn_in_portal = new ArrayList<>();
	private Random random = new Random();
	
	public FeatureHandler(Main plugin) {
		this.plugin = plugin;
		this.server = plugin.getServer();
	}
	
	public void onTick() {
		for(World w : server.getWorlds())
		{
			for(Entity e : w.getEntitiesByClass(Damageable.class)) {
				if(e.getVelocity().getY() >= 0d)
					e.setFallDistance(Math.min(e.getFallDistance(), 10f));
			}
			for(Entity e : w.getEntitiesByClass(Boat.class))
			{
				if(e.getPassengers().size() == 0 && e.getLocation().getBlock().getType() == Material.NETHER_PORTAL)
					tp_entity_from_portal(e);
			}
		}
		for(int i = test_spawn_in_portal.size()-1; i >= 0; i--) {
			Player p = test_spawn_in_portal.get(i);
			tp_entity_from_portal( p );
			if(p.isInsideVehicle()/* && event.getPlayer().getVehicle().getType() != EntityType.PLAYER*/) {
				List<Entity> pass = p.getVehicle().getPassengers();
				if(pass.size() > 0) {
					Entity e = pass.get(0);
					if(e instanceof Bat && e.getCustomName().equals(" ")) {
						e.remove();
						//e.removePassenger(p);
						//e.addPassenger(p);
					}
				}
			}
		}
		test_spawn_in_portal.clear();
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		test_spawn_in_portal.add(event.getPlayer());
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) return;
		if (event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 1)
		{
			if (event.getBlock().getType().equals(Material.BROWN_MUSHROOM_BLOCK) || event.getBlock().getType().equals(Material.RED_MUSHROOM_BLOCK))
			{
				event.setCancelled(true);
				int fortune_lvl = event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
				Block block = event.getBlock();
				double rand_res = random.nextDouble();
				// 1: (2/3)x1 (1/3)x2 => 2: (2/4)x1 (1/4)x2 (1/4)x3 => 2: (2/5)x1 (1/5)x2 (1/5)x3 (1/5)x4
				int mul = 1;
				for (int i = 1; i < fortune_lvl + 1; i++)
				{
					if ((fortune_lvl + 1) * rand_res / i < 1.0) {
						mul = i;
						break;
					}
				}
				Collection<ItemStack> new_drops = block.getDrops();
				block.setType(Material.AIR);
				for (ItemStack new_drop : new_drops)
				{
					if (new_drop.getType() == Material.AIR)
						continue;
					int amount = new_drop.getAmount() * mul;
					int max_amount = new_drop.getMaxStackSize();
					while (amount > 0)
					{
						int drop_amount = Math.min(max_amount, amount);
						amount -= drop_amount;
						new_drop.setAmount(drop_amount);
						block.getWorld().dropItemNaturally(block.getLocation(), new_drop);
					}
				}
			}
		}
		else if (event.getBlock().getType().equals(Material.SPAWNER) && event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 1)
		{
			ItemStack spawner = new ItemStack(Material.SPAWNER);
			BlockStateMeta bsm = (BlockStateMeta)spawner.getItemMeta();
			BlockState bs = bsm.getBlockState();
			CreatureSpawner cs = (CreatureSpawner)bs;
			cs.setSpawnedType( ((CreatureSpawner)event.getBlock().getState()).getSpawnedType() );
			bsm.setBlockState(bs);
			spawner.setItemMeta(bsm);
			event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), spawner);
		}
	}

	// Jump boost fall damage reduction
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER) {
			EntityDamageEvent eve = (EntityDamageEvent) event;
			int lvl = ((CraftLivingEntity)(eve.getEntity())).getPotionEffect(PotionEffectType.JUMP) == null ? 0 : ((CraftLivingEntity)(eve.getEntity())).getPotionEffect(PotionEffectType.JUMP).getAmplifier()+1;
			if( eve.getCause().equals(EntityDamageEvent.DamageCause.FALL) && lvl > 0 )
			{
				eve.setDamage((eve.getDamage()+lvl)*Math.pow(Config.fallDamage, lvl));
			}
		}
	}
	
	private int dir(double x0) {
		if((int)x0 == (int)(x0+0.75)) //neg x
			return -1;
		else if((int)x0 == (int)(x0+0.25)) //pos x
			return 1;
		else
			return 0;
	}
	
	void tp_entity_from_portal(Entity e) {
		if(e instanceof Player && !((Player)e).isOnline()) return;

		Location l = e.getLocation();
		double x0 = l.getX(), y0 = l.getY(), z0 = l.getZ();
		int dx_prior = dir(x0), dz_prior = dir(z0);
			
		if(e.getLocation().getBlock().getType() == Material.NETHER_PORTAL || l.getBlock().getRelative(dx_prior, 0, dz_prior).getType() == Material.NETHER_PORTAL) {
			//Block start_block = l.getBlock().getRelative(dx, 0, dz);
			Block start_block = l.getBlock();
			Block b, tp_to = null;
			searching:
			{
				int dx, dz;
				if(dx_prior < 0)
				for(int r = 0; r <= Config.portal_search_radius; r++) {
					for(int dy = 0; dy <= r/2; dy++) {
						int temp = r-dy;
						for(dx = -temp; dx <= temp; dx++) {
							dz = r-Math.abs(dx);
							b = start_block.getRelative(dx, dy, dz);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, dy, -dz);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, -dy, dz);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, -dy, -dz);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
						}
					}
				}
				else
				for(int r = 0; r <= Config.portal_search_radius; r++) {
					for(int dy = 0; dy <= r/2; dy++) {
						int temp = r-dy;
						for(dx = temp; dx >= -temp; dx--) {
							b = start_block.getRelative(dx, dy, r-Math.abs(dx));
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, dy, Math.abs(dx)-r);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, -dy, r-Math.abs(dx));
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = start_block.getRelative(dx, -dy, Math.abs(dx)-r);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
						}
					}
				}
			} //end of searching
			if(tp_to != null) {
				Location tp = new Location(start_block.getWorld(), tp_to.getX()+0.5, tp_to.getY()+0.5, tp_to.getZ()+0.5, l.getYaw(), l.getPitch());
				e.teleport(tp);
			}
		}
	}
	
	private static boolean is_valid_portal_tp(Block b) {
		return UtilsType.playerCanStay(b) && b.getType() != Material.NETHER_PORTAL;
	}
}
