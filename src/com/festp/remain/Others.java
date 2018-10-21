package com.festp.remain;

import java.util.ArrayList;
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
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftLivingEntity;
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
import com.festp.Utils;
import com.festp.mainListener;

public class Others implements Listener {
	mainListener plugin;
	Server server;
	List<Player> test_spawn_in_portal = new ArrayList<>();
	private Random random = new Random();
	
	public Others(mainListener plugin) {
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
			for(Entity e : w.getEntitiesByClass(Firework.class))
			{
				e.setVelocity( new Vector(0.0d, e.getVelocity().getY(), 0.0d) ); 
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
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(event.getEntity().getKiller() instanceof Player && random.nextDouble() < 0.5) {
			Entity e = event.getEntity().getWorld().spawnEntity(event.getEntity().getEyeLocation(), EntityType.ENDER_DRAGON);
			e.playEffect(EntityEffect.DEATH);
			e.remove();
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled()) return;
		if(event.getBlock().getType().equals(Material.SPAWNER) && event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.SILK_TOUCH) > 1) {
			/*System.out.println(event.getBlock().getState().getData()+" "+event.getBlock().getMetadata("BlockEntityTag"));
			ItemStack spawner = new ItemStack(Material.MOB_SPAWNER);
			spawner.setData(event.getBlock().getState().getData());*/
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

	//Запили тогда ещё сокращение урона от падения при джамп бусте.
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event)
	{
		if(event.getEntityType() == EntityType.PLAYER) {
			EntityDamageEvent eve = (EntityDamageEvent) event;
			int lvl = ((CraftLivingEntity)(eve.getEntity())).getPotionEffect(PotionEffectType.JUMP) == null ? 0 : ((CraftLivingEntity)(eve.getEntity())).getPotionEffect(PotionEffectType.JUMP).getAmplifier()+1;
			if( eve.getCause().equals(EntityDamageEvent.DamageCause.FALL) && lvl > 0 )
			{
				eve.setDamage((eve.getDamage()+lvl)*Math.pow(Config.fallDamage,lvl));
			}
		}
	}
	
	//EntityPortalEvent extends EntityTeleportEvent
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
		return Utils.playerCanStay(b) && b.getType() != Material.NETHER_PORTAL;
	}
}
