package com.festp.misc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.Orientable;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffectType;

import com.festp.Config;
import com.festp.Main;
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
		for (World w : server.getWorlds())
		{
			for (Entity e : w.getEntitiesByClass(Damageable.class)) {
				if (e.getVelocity().getY() >= 0d)
					e.setFallDistance(Math.min(e.getFallDistance(), 10f));
			}
			for (Entity e : w.getEntitiesByClass(Boat.class))
			{
				if (e.getPassengers().size() == 0 && e.getLocation().getBlock().getType() == Material.NETHER_PORTAL)
					tp_entity_from_portal(e);
			}
		}
		for (int i = test_spawn_in_portal.size()-1; i >= 0; i--) {
			Player p = test_spawn_in_portal.get(i);
			if (p.isInsideVehicle()/* && event.getPlayer().getVehicle().getType() != EntityType.PLAYER*/) {
				List<Entity> pass = p.getVehicle().getPassengers();
				if (pass.size() > 0) {
					Entity e = pass.get(0);
					if (e instanceof Bat && e.getCustomName().equals(" ")) {
						e.remove();
						//e.removePassenger(p);
						//e.addPassenger(p);
					}
				}
			}
			tp_entity_from_portal(p);
		}
		test_spawn_in_portal.clear();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
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
		if (e instanceof Player && !((Player)e).isOnline()) return;

		Location l = e.getLocation();
		double x0 = l.getX(), z0 = l.getZ();
		int dx_prior = dir(x0), dz_prior = dir(z0);

		Block center = l.getBlock();
		if (center.getType() == Material.NETHER_PORTAL || center.getRelative(dx_prior, 0, dz_prior).getType() == Material.NETHER_PORTAL
				 || center.getRelative(dx_prior, 0, 0).getType() == Material.NETHER_PORTAL || center.getRelative(0, 0, dz_prior).getType() == Material.NETHER_PORTAL) {
			Block b, tp_to = null;
			searching:
			{
				int dxT, dx, dz;
				int dxStep = 1;
				if (dx_prior < 0)
					dxStep = -1;
				for (int r = 0; r <= Config.portal_search_radius; r++) {
					for (int dy = 0; dy <= r/2; dy++) {
						int temp = r - dy;
						for (dxT = -temp; dxT <= temp; dxT++) {
							dx = dxT * dxStep;
							dz = r - Math.abs(dx);
							b = center.getRelative(dx, dy, dz);
							if(is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = center.getRelative(dx, dy, -dz);
							if (is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = center.getRelative(dx, -dy, dz);
							if (is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
							b = center.getRelative(dx, -dy, -dz);
							if (is_valid_portal_tp(b)) {
								tp_to = b;
								break searching;
							}
						}
					}
				}
			} // end of searching
			if (tp_to == null) {
				// try paths from portal blocks - can't teleport on (.700, .701)
			}
			if (tp_to == null) {
				tp_to = findFirstNonportal(center, BlockFace.UP);
			}
			if (tp_to == null) {
				Orientable portal = (Orientable) center.getBlockData();
				BlockFace dir = BlockFace.EAST;
				if (portal.getAxis() == Axis.Z)
					dir = BlockFace.SOUTH;
				if (random.nextDouble() >= 0.5)
					dir = dir.getOppositeFace();
				tp_to = findFirstNonportal(center, dir);
			}
			if (tp_to != null) {
				Location shift = tp_to.getLocation().subtract(center.getLocation()).multiply(-0.001);
				shift.setY(0);
				Location tp = tp_to.getLocation().add(shift).add(0.5, 0, 0.5);
				tp.setYaw(l.getYaw());
				tp.setPitch(l.getPitch());
				e.teleport(tp);
			}
		}
	}
	
	private static Block findFirstNonportal(Block start, BlockFace dir) {
		while (start.getType() == Material.NETHER_PORTAL) {
			start = start.getRelative(dir);
		}
		if (dir == BlockFace.UP) // skip obsidian
			start = start.getRelative(dir);
		if (start.getType() == Material.BEDROCK) // TODO all unbrekeable blocks
			return null;
		return start;
	}
	
	private static boolean is_valid_portal_tp(Block b) {
		return b.getType() != Material.NETHER_PORTAL && UtilsType.playerCanStay(b);
	}
}
