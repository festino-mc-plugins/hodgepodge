package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.block.data.type.Cake;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import com.festp.Config;
import com.festp.CooldownPlayer;
import com.festp.Main;
import com.festp.utils.Utils;
import com.festp.utils.UtilsColor;
import com.festp.utils.UtilsType;
import com.festp.utils.UtilsWorld;

public class InteractHandler implements Listener {

	public static final String BEACON_SADDLE_ID = "saddlemob";
	public static final Class<? extends LivingEntity> BEACON_SADDLE_CLASS = Turtle.class;
	
	//cauldrons to wash items
	List<BlockData> cauls = new ArrayList<>();
	List<Integer> caulticks = new ArrayList<>();
	List<Item> world_items = new ArrayList<>();
	List<LivingEntity> world_beacons = new ArrayList<>();

	List<CooldownPlayer> left_rotate_cooldown = new ArrayList<>();
	
	Main plugin;
	Server server;
	LeashManager leash_manager;
	
	public InteractHandler(Main pl, LeashManager lm) {
		this.plugin = pl;
		this.server = pl.getServer();
		this.leash_manager = lm;
	}
	
	private LivingEntity spawnSaddleBeacon(Location l) {
		return Utils.spawnBeacon(l, BEACON_SADDLE_CLASS, BEACON_SADDLE_ID, false);
	}
	public boolean isSaddleBeacon(LivingEntity e) {
		return Utils.hasBeaconData(e, BEACON_SADDLE_ID);
	}
	public boolean isLeashBeacon(LivingEntity e) {
		return Utils.hasBeaconData(e, LeashedPlayer.BEACON_ID);
	}
	
	public void onTick()
	{
		for (int i = left_rotate_cooldown.size() - 1; i >= 0; i--) {
			CooldownPlayer cp = left_rotate_cooldown.get(i);
			if(!cp.tick()) {
				left_rotate_cooldown.remove(i);
			}
		}
		
		for (int i = caulticks.size() - 1; i >= 0; i--) {
			caulticks.set(i, caulticks.get(i) + 1);
			
			if(caulticks.get(i) > 5) {
				cauls.remove(i);
				caulticks.remove(i);
			}
		}

		for (int i = world_items.size() - 1; i >= 0; i--)
		{
			Item item = world_items.get(i);
			
			if (!item.isValid()) {
				world_items.remove(i);
				continue;
			}
			World w = item.getWorld();
			Block b = item.getLocation().getBlock();
			if(b.getType() == Material.CAULDRON) {
				BlockData cauldron = b.getBlockData();
				if (UtilsWorld.getCauldronLevel(cauldron) > 0) {
					int j = cauls.indexOf(cauldron);
					if(j >= 0) {
						continue;
					}
					Material m = item.getItemStack().getType();
					ItemStack drop = null;
					if (UtilsType.is_concrete_powder(m))
						drop = new ItemStack(UtilsColor.fromColor_concrete(UtilsColor.colorFromMaterial(m)));
					else if (UtilsType.is_colored_terracotta(m))
						drop = new ItemStack(Material.TERRACOTTA);
					else if (UtilsColor.colorFromMaterial(m) != DyeColor.WHITE) {
						 if (UtilsType.is_wool(m)) 
							 drop = new ItemStack(Material.WHITE_WOOL, 1);
						 else if (UtilsType.is_carpet(m)) 
							 drop = new ItemStack(Material.WHITE_CARPET, 1);
					}
					else if (m.equals(Material.RED_SAND))
						drop = new ItemStack(Material.SAND);
					else if (m.equals(Material.RED_SANDSTONE))
						drop = new ItemStack(Material.SANDSTONE,1);
					if (drop != null) {
						item.getItemStack().setAmount(item.getItemStack().getAmount()-1);
						w.dropItem(item.getLocation(), new ItemStack(drop));
						Utils.lower_cauldron_water(b.getState());
		                cauls.add(cauldron);
		                caulticks.add(0);
		                
						if (item.getItemStack().getAmount() <= 0)
							world_items.remove(i);
					}
				}
			}
		}
		
		List<LivingEntity> removed_beacons = new ArrayList<>();
		for (LivingEntity beacon : world_beacons)
		{
			if (!beacon.isValid()) {
				removed_beacons.add(beacon);
				continue;
			}
			// saddled entities
			if (isSaddleBeacon(beacon)) {
				if (beacon.getPassengers().size() == 0 || beacon.getVehicle() == null)
					beacon.remove();
				else {
					LivingEntity camel_player = (LivingEntity)beacon.getVehicle();
					
					if (camel_player.getEquipment().getHelmet() == null || camel_player.getEquipment().getHelmet().getType() != Material.SADDLE) // unwear saddle
						beacon.remove();
					else {
						beacon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue( camel_player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() );
						beacon.setHealth( camel_player.getHealth() );
					}
				}
			}
			// leashed players
			else if (isLeashBeacon(beacon)) {
				if (!leash_manager.isWorkaround(beacon) || !beacon.isLeashed()) {
					System.out.print("remove leash beacon");
					beacon.getWorld().dropItem(beacon.getLocation(), new ItemStack(Material.LEAD, 1));
					beacon.remove();
				}
			}
		}
		
		for (LivingEntity beacon : removed_beacons)
			world_beacons.remove(beacon);
		
		// System.out.print(world_beacons.size() +"   " + world_items.size());
	}
	
	// TODO: getCauldroned(): concrete powder -> concrete; dirt -> null
	// 		 isWashable(): getCauldroned() != null
	public boolean isWashable(Item dropped_item) {
		Material material = dropped_item.getItemStack().getType();
		if (UtilsType.is_concrete_powder(material))
			return true;
		if (UtilsType.is_colored_terracotta(material))
			return true;
		if (material == Material.RED_SAND)
			return true;
		if (material == Material.RED_SANDSTONE)
			return true;
		if (UtilsColor.colorFromMaterial(material) != DyeColor.WHITE)
		{
			if (UtilsType.is_glazed_terracotta(material))
				return true;
			if (UtilsType.is_concrete(material))
				return true;
			if (UtilsType.is_wool(material))
				return true;
			if (UtilsType.is_carpet(material))
				return true;
		}
		return false;
	}
	
	// loading new items(for cauldrons) and beacons(saddle/leash)
	public void addEntity(Entity e)
	{
		if (e.getType() == EntityType.DROPPED_ITEM) {
			Item dropped_item = (Item) e;
			if (isWashable(dropped_item))
				world_items.add(dropped_item);
		}
		if (BEACON_SADDLE_CLASS.isInstance(e)) {
			LivingEntity beacon = (LivingEntity) e;
			if (isSaddleBeacon(beacon)
				|| isLeashBeacon(beacon))
				world_beacons.add(beacon);
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		addEntity(event.getEntity());
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		for (Entity e : event.getChunk().getEntities())
			addEntity(e);
	}
	
	/** Saddled players clocks, multi nametag block */
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.isCancelled()) return;
		
        Entity rightclicked = event.getRightClicked();
        Player clicker = event.getPlayer();
		
        if (!isPassenger(rightclicked, clicker) && rightclicked instanceof LivingEntity)
        {
    	   ItemStack hat = ((LivingEntity)rightclicked).getEquipment().getHelmet();
    	   if (rightclicked.getPassengers().size() == 0 && hat != null && hat.getType() == Material.SADDLE)
    	   {
        	   //ride on entity
    		   LivingEntity temp = spawnSaddleBeacon(rightclicked.getLocation());
        	   rightclicked.addPassenger(temp);
        	   temp.addPassenger(clicker);
        	   return;
    	   }
        }
       
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand() != null ? event.getPlayer().getInventory().getItemInMainHand()
    		   : (event.getPlayer().getInventory().getItemInOffHand() != null ? event.getPlayer().getInventory().getItemInOffHand() : null );
       
        boolean cancelled = leash_manager.click(rightclicked, event.getPlayer(), hand);
        if (cancelled) {
        	event.setCancelled(true);
        	return;
        }
        
        if (hand != null) {
        	if (hand.getType() == Material.NAME_TAG) {
            	if (hand.getItemMeta().hasDisplayName() && hand.getItemMeta().getDisplayName() == rightclicked.getCustomName())
        	    	event.setCancelled(true);
        	}
        }
    }
	private boolean isPassenger(Entity target, Entity vehicle) {
		for (Entity passenger : vehicle.getPassengers())
			if (passenger == target)
				return true;
			else {
				boolean loop = isPassenger(target, passenger);
				if (loop)
					return true;
			}
		return false;
	}

	/** cauldron clicks(item clearing), dirt->grass, block recoloring, rotating, bed linen, lasso */
	@SuppressWarnings("deprecation")
	@EventHandler//(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (event.isCancelled() && !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)) return;
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && is_left_click_on_cooldown(player))
			return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() && event.getClickedBlock().getType() == Material.CAKE) {
			if (!player.isSneaking() && player.getFoodLevel() >= 20) {
				Cake cake = (Cake) event.getClickedBlock().getBlockData();
				if (cake.getBites() == cake.getMaximumBites()) {
					event.getClickedBlock().setType(Material.AIR);
				} else {
					cake.setBites(cake.getBites() + 1);
					event.getClickedBlock().setBlockData(cake);
				}
				player.setSaturation(Math.min(player.getSaturation() + 0.4f, player.getFoodLevel()));
				return;
			}
		}
		if (event.hasBlock() && event.getItem() != null) {
			if (!player.isSneaking() && event.getClickedBlock().getType() == Material.CAULDRON)
			{
	                BlockState d = event.getClickedBlock().getState();
					if (d.getData().getData() == 0)
						return;
					MaterialData hand_data = event.getItem().getData();
					Material hand = Utils.from_legacy(hand_data);
					ItemStack washed;
					if (UtilsType.is_colored_terracotta(hand)) {
						washed = new ItemStack(Material.TERRACOTTA, 1);
					} else if (UtilsType.is_wool(hand)) {
						washed = new ItemStack(Material.WHITE_WOOL, 1);
					} else if (UtilsType.is_concrete(hand)) {
						washed = new ItemStack(Material.WHITE_CONCRETE, 1);
					} else if (UtilsType.is_carpet(hand)) {
						washed = new ItemStack(Material.WHITE_CARPET, 1);
					} else if (UtilsType.is_glazed_terracotta(hand)) {
						washed = new ItemStack(Material.WHITE_GLAZED_TERRACOTTA, 1);
					} else if (UtilsType.is_concrete_powder(hand)) {
						washed = new ItemStack(UtilsColor.fromColor_concrete(UtilsColor.colorFromMaterial(hand)), 1);
					} else if (hand.equals(Material.RED_SAND)) {
						washed = new ItemStack(Material.SAND, 1);
					} else if (hand.equals(Material.RED_SANDSTONE)) {
						washed = new ItemStack(Material.SANDSTONE, 1);
					} else return;
					event.setCancelled(true);
					event.getItem().setAmount(event.getItem().getAmount()-1);
					player.getInventory().addItem(washed); // TODO: replace all of "Inventory.addItem()" with working function(Utils.giveOrDrop) #thx1.13 
					Utils.lower_cauldron_water(d.getBlock().getState());
			}
			
			//grass from dirt
			Material currentblock = event.getClickedBlock().getType();
			if (currentblock.equals(Material.DIRT) && event.getItem().getType().equals(Material.BONE_MEAL)) {
				event.getClickedBlock().setType(Material.GRASS_BLOCK);
				event.getItem().setAmount(event.getItem().getAmount() - 1);
				event.setCancelled(true);
				return;
			} 
			
			if (UtilsType.is_dye(event.getItem().getType()) ) 
			{
				DyeColor clicked_block_color = UtilsColor.colorFromMaterial(event.getClickedBlock().getType());
				DyeColor clicking_dye_color = UtilsColor.colorFromMaterial(event.getItem().getType());
				Material block_material = event.getClickedBlock().getType();
				boolean is_wall_banner = UtilsType.is_wall_banner(block_material);
				boolean is_banner = UtilsType.is_banner(block_material);
				if ( (is_banner || is_wall_banner)
						&& event.getItem().getAmount() > 5 )
				{
					Block banner_block = event.getClickedBlock();
					Banner banner = (Banner) banner_block.getState();
					BlockData old_banner_data = banner_block.getBlockData();
					
					if (banner.getBaseColor() != clicking_dye_color) {

						if (is_wall_banner)
							banner.setType(UtilsColor.fromColor_wall_banner(clicking_dye_color));
						if (is_banner)
							banner.setType(UtilsColor.fromColor_banner(clicking_dye_color));
						//b.setBaseColor(clicking_dye_color);
						banner.update(true);
						
						if (is_wall_banner) {
							Directional banner_data = (Directional) banner_block.getBlockData();
							banner_data.setFacing( ((Directional) old_banner_data).getFacing() );
							banner_block.setBlockData(banner_data);
						}
						if (is_banner) {
							Rotatable banner_data = (Rotatable) banner_block.getBlockData();
							banner_data.setRotation( ((Rotatable) old_banner_data).getRotation() );
							banner_block.setBlockData(banner_data);
						}
						
						event.getItem().setAmount(event.getItem().getAmount() - 6);
					}
					return;
					
				}
				
				if (clicked_block_color == clicking_dye_color)
					return;
				if (UtilsType.is_terracotta(currentblock)) event.getClickedBlock().setType(UtilsColor.fromColor_terracotta(clicking_dye_color));
				else if (UtilsType.is_wool(currentblock)) event.getClickedBlock().setType(UtilsColor.fromColor_wool(clicking_dye_color));
				else if (UtilsType.is_concrete_powder(currentblock)) event.getClickedBlock().setType(UtilsColor.fromColor_concrete_powder(clicking_dye_color));
				else if (UtilsType.is_concrete(currentblock)) event.getClickedBlock().setType(UtilsColor.fromColor_concrete(clicking_dye_color));
				else if (UtilsType.is_carpet(currentblock)) event.getClickedBlock().setType(UtilsColor.fromColor_carpet(clicking_dye_color));
				else return;
				event.getItem().setAmount(event.getItem().getAmount()-1);
				return;
			}
			
			Material handMaterial = event.getItem().getType();
			if (UtilsType.isHoe(handMaterial))
			{
				//ROTATE BLOCKS
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK && RotatableBlock.rotate_attempt(event.getClickedBlock(), player.isSneaking())
						|| event.getAction() == Action.LEFT_CLICK_BLOCK && RotatableBlock.left_click_rotate_attempt(event.getClickedBlock(), player.isSneaking())) {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK)
						left_rotate_cooldown.add(new CooldownPlayer(player, Config.LEFT_ROTATE_COOLDOWN));
					int dur_lvl = event.getItem().getEnchantmentLevel(Enchantment.DURABILITY);
					byte dur = (byte) ((Math.random()*(dur_lvl + 1)) > 1 ? 0 : 1);
					event.getItem().setDurability((short) (event.getItem().getDurability()+dur));
					if (event.getItem().getDurability() > event.getItem().getType().getMaxDurability()) {
						if (event.getHand() == EquipmentSlot.HAND)
							player.getInventory().setItemInMainHand(null);
						else
							player.getInventory().setItemInOffHand(null);
					}
				}
			}
			
			//change bed linen
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !player.isSneaking() && 
					UtilsType.is_carpet(handMaterial) && UtilsType.is_bed(event.getClickedBlock().getType())) {
				DyeColor newColor = UtilsColor.colorFromMaterial(handMaterial);
				DyeColor oldColor = UtilsColor.colorFromMaterial(event.getClickedBlock().getType());
				if (oldColor != newColor) {
					Material newBedMaterial = UtilsColor.fromColor_bed(newColor); // TODO BlockType.BED - explicit (enum) argument
					Material newCarpetMaterial = UtilsColor.fromColor_carpet(oldColor);
					Material particleMaterial = UtilsColor.fromColor_wool(oldColor);
					Block blockFoot = event.getClickedBlock();
					Bed bedFoot = (Bed) blockFoot.getBlockData();
					BlockFace face = bedFoot.getFacing();
					Block blockHead;
					Bed bedHead;
					if (bedFoot.getPart() == Part.FOOT) {
						blockHead = blockFoot.getRelative(face);
					} else {
						bedHead = bedFoot;
						blockHead = blockFoot;
						blockFoot = blockHead.getRelative(face.getOppositeFace());
					}

					Location blockCenter = blockFoot.getLocation().add(0.5, 0.5, 0.5);
					blockFoot.setType(particleMaterial, false);
					player.getWorld().spawnParticle(Particle.BLOCK_CRACK, blockCenter, 20, 0.2f, 0.0f, 0.2f, blockFoot.getBlockData());
					player.getWorld().playSound(blockCenter, Sound.BLOCK_WOOL_BREAK, 1.0f, 0.8f);
					
					blockHead.setType(newBedMaterial, false);
					blockFoot.setType(newBedMaterial, false);
					bedFoot = (Bed) blockFoot.getBlockData();
					bedFoot.setFacing(face);
					bedHead = (Bed) blockHead.getBlockData();
					bedHead.setPart(Part.HEAD);
					bedHead.setFacing(face);
					blockHead.setBlockData(bedHead, false);
					blockFoot.setBlockData(bedFoot, false);
					
					if (event.getItem().getAmount() == 1) {
						event.getItem().setType(newCarpetMaterial);
					} else {
						event.getItem().setAmount(event.getItem().getAmount() - 1);
						ItemStack oldLinen = new ItemStack(newCarpetMaterial, 1);
						Utils.giveOrDrop(player.getInventory(), oldLinen);
					}
					event.setCancelled(true);
				}
			}
		} // has both block and item

		// TODO: cancel if clicked on leashed entity
		// jump rope and lasso
		if (event.getItem() != null && event.getItem().getType() == Material.LEAD) {
			ItemStack hand = event.getItem();
			ItemStack lead_drops = hand.clone();
			lead_drops.setAmount(1);
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK && UtilsType.isFence(event.getClickedBlock().getType())) {
				
				List <Entity> entities = player.getNearbyEntities(15, 15, 15);
				for (Entity e : entities)
					if (e instanceof LivingEntity && ((LivingEntity)e).isLeashed() && ((LivingEntity)e).getLeashHolder() == player)
						return;
				
				event.setCancelled(true);
				Location hitch_loc = event.getClickedBlock().getLocation();
				LeashHitch hitch = LeashManager.spawnHitch(hitch_loc);
				leash_manager.addLeashed(hitch, player, lead_drops);
		    	if (player.getGameMode() != GameMode.CREATIVE)
		    		hand.setAmount(hand.getAmount()-1);
			}
			else if (event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK && !UtilsType.isInteractable(event.getClickedBlock().getType())) {
				leash_manager.throwLasso(player, lead_drops);
		    	if (player.getGameMode() != GameMode.CREATIVE)
		    		hand.setAmount(hand.getAmount()-1);
			}
			else if (event.getAction() == Action.LEFT_CLICK_AIR)
			{
				leash_manager.throwTargetLasso(player, lead_drops);
		    	if (player.getGameMode() != GameMode.CREATIVE)
		    		hand.setAmount(hand.getAmount()-1);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		leash_manager.removeByLeashHolder(event.getPlayer());
	}

	@EventHandler
	public void onEntityUnleash(PlayerUnleashEntityEvent event) {
		leash_manager.onUnleash(event);
	}
	
	public boolean is_left_click_on_cooldown(Player p) {
		for(int i = 0; i < left_rotate_cooldown.size(); i++) {
			if(left_rotate_cooldown.get(i).getPlayer() == p)
				return true;
		}
		return false;
	}
}
