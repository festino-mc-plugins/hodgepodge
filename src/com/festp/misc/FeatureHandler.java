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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.Config;
import com.festp.Main;
import com.festp.utils.UtilsType;

public class FeatureHandler implements Listener {
	Main plugin;
	Server server;
	List<Player> testSpawnInPortal = new ArrayList<>();
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
					tpEntityFromPortal(e);
			}
		}
		for (int i = testSpawnInPortal.size() - 1; i >= 0; i--) {
			Player p = testSpawnInPortal.get(i);
			if (p.isInsideVehicle()) {
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
			tpEntityFromPortal(p);
		}
		testSpawnInPortal.clear();
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		testSpawnInPortal.add(event.getPlayer());
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
				int fortuneLevel = event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
				Block block = event.getBlock();
				double randRes = random.nextDouble();
				// 1: (2/3)x1 (1/3)x2 => 2: (2/4)x1 (1/4)x2 (1/4)x3 => 2: (2/5)x1 (1/5)x2 (1/5)x3 (1/5)x4
				int mul = 1;
				for (int i = 1; i < fortuneLevel + 1; i++)
				{
					if ((fortuneLevel + 1) * randRes / i < 1.0) {
						mul = i;
						break;
					}
				}
				Collection<ItemStack> newDrops = block.getDrops();
				block.setType(Material.AIR);
				for (ItemStack newDrop : newDrops)
				{
					if (newDrop.getType() == Material.AIR)
						continue;
					int amount = newDrop.getAmount() * mul;
					int maxAmount = newDrop.getMaxStackSize();
					while (amount > 0)
					{
						int dropAmount = Math.min(maxAmount, amount);
						amount -= dropAmount;
						newDrop.setAmount(dropAmount);
						block.getWorld().dropItemNaturally(block.getLocation(), newDrop);
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
		if (event.getEntityType() != EntityType.PLAYER)
			return;
		if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL))
			return;
		PotionEffect jumpEffect = ((LivingEntity)(event.getEntity())).getPotionEffect(PotionEffectType.JUMP);
		int lvl = jumpEffect == null ? 0 : jumpEffect.getAmplifier() + 1;
		if (lvl <= 0)
			return;
		event.setDamage((event.getDamage() + lvl) * Math.pow(Config.fallDamage, lvl));
	}
	
	private int dir(double x0) {
		if ((int)x0 == (int)(x0 + 0.75)) // neg x
			return -1;
		else if ((int)x0 == (int)(x0 + 0.25)) // pos x
			return 1;
		else
			return 0;
	}
	
	void tpEntityFromPortal(Entity e) {
		if (e instanceof Player && !((Player)e).isOnline()) return;

		Location l = e.getLocation();
		double x0 = l.getX(), z0 = l.getZ();
		int dxPrior = dir(x0), dzPrior = dir(z0);

		Block center = l.getBlock();
		boolean entityIsInPortal = center.getType() == Material.NETHER_PORTAL
				|| center.getRelative(dxPrior, 0, 0).getType() == Material.NETHER_PORTAL
				|| center.getRelative(0, 0, dzPrior).getType() == Material.NETHER_PORTAL
				|| center.getRelative(dxPrior, 0, dzPrior).getType() == Material.NETHER_PORTAL;
		if (entityIsInPortal) {
			Block b, tpTo = null;
			searching:
			{
				int dxT, dx, dz;
				int dxStep = 1;
				if (dxPrior < 0)
					dxStep = -1;
				for (int r = 0; r <= Config.portalSearchRadius; r++) {
					for (int dy = 0; dy <= r / 2; dy++) {
						int temp = r - dy;
						for (dxT = -temp; dxT <= temp; dxT++) {
							dx = dxT * dxStep;
							dz = r - Math.abs(dx);
							b = center.getRelative(dx, dy, dz);
							if(isValidPortalTp(b)) {
								tpTo = b;
								break searching;
							}
							b = center.getRelative(dx, dy, -dz);
							if (isValidPortalTp(b)) {
								tpTo = b;
								break searching;
							}
							b = center.getRelative(dx, -dy, dz);
							if (isValidPortalTp(b)) {
								tpTo = b;
								break searching;
							}
							b = center.getRelative(dx, -dy, -dz);
							if (isValidPortalTp(b)) {
								tpTo = b;
								break searching;
							}
						}
					}
				}
			} // end of searching
			if (tpTo == null) {
				// try paths from portal blocks - can't teleport on (.700, .701)
			}
			if (tpTo == null) {
				tpTo = findFirstNonportal(center, BlockFace.UP);
			}
			if (tpTo == null) {
				Orientable portal = (Orientable) center.getBlockData();
				BlockFace dir = BlockFace.EAST;
				if (portal.getAxis() == Axis.Z)
					dir = BlockFace.SOUTH;
				if (random.nextDouble() >= 0.5)
					dir = dir.getOppositeFace();
				tpTo = findFirstNonportal(center, dir);
			}
			if (tpTo != null) {
				Location shift = tpTo.getLocation().subtract(center.getLocation()).multiply(-0.001);
				shift.setY(0);
				Location tp = tpTo.getLocation().add(shift).add(0.5, 0, 0.5);
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
	
	private static boolean isValidPortalTp(Block b) {
		return b.getType() != Material.NETHER_PORTAL && UtilsType.playerCanStay(b);
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() instanceof Snowball || event.getDamager() instanceof Egg)
		{
			if (event.getEntity() instanceof Player)
			{
				if (event.getDamage() < 1E-10)
					event.setDamage(1E-10);
				if (event.isCancelled())
					event.setCancelled(false);
			}
		}
	}
}
