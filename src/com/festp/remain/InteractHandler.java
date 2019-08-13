package com.festp.remain;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Bed.Part;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftLeash;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.festp.Config;
import com.festp.CooldownPlayer;
import com.festp.Main;
import com.festp.utils.Utils;
import com.festp.utils.UtilsColor;
import com.festp.utils.UtilsType;

public class InteractHandler implements Listener {

	public static final String beacon_id_saddle = "saddlemob";
	
	//cauldrons to wash items
	List<BlockState> cauls = new ArrayList<>();
	List<Integer> caulticks = new ArrayList<>();

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
		return Utils.spawnBeacon(l, Turtle.class, beacon_id_saddle, false);
	}
	
	public void onTick() {
		long t1, t2, t3, t4;
		t1 = System.nanoTime();
		
		for(int i = 0; i< caulticks.size(); i++)
			caulticks.set(i, caulticks.get(i) + 1);
		for(World w : server.getWorlds())
		{
			for(Item e : w.getEntitiesByClass(Item.class))
			{
				Block b = w.getBlockAt(e.getLocation());
				if(b.getType() == Material.CAULDRON) {
					BlockState cauldron = b.getState(); //TO DO: use Levelled BlockData
					if(cauldron.getData().getData() > 0) {
						int i = cauls.indexOf(cauldron);
						if(i >= 0) {
							if(caulticks.get(i) > 4) {
								cauls.remove(i);
								caulticks.remove(i);
							}
							continue;
						}
						Material m = e.getItemStack().getType();
						ItemStack drop = null;
						if(UtilsType.is_concrete_powder(m))
							drop = new ItemStack(UtilsColor.fromColor_concrete(UtilsColor.colorFromMaterial(m)));
						else if(UtilsType.is_colored_terracotta(m))
							drop = new ItemStack(Material.TERRACOTTA);
						else if(UtilsColor.colorFromMaterial(m) != DyeColor.WHITE) {
							 if(UtilsType.is_glazed_terracotta(m)) 
								 drop = new ItemStack(Material.WHITE_GLAZED_TERRACOTTA, 1);
							 else if(UtilsType.is_concrete(m)) 
								 drop = new ItemStack(Material.WHITE_CONCRETE, 1);
							 else if(UtilsType.is_wool(m)) 
								 drop = new ItemStack(Material.WHITE_WOOL, 1);
							 else if(UtilsType.is_carpet(m)) 
								 drop = new ItemStack(Material.WHITE_CARPET, 1);
						}
						else if(m.equals(Material.RED_SAND))
							drop = new ItemStack(Material.SAND);
						else if(m.equals(Material.RED_SANDSTONE))
							drop = new ItemStack(Material.SANDSTONE,1);
						if(drop != null) {
							e.getItemStack().setAmount(e.getItemStack().getAmount()-1);
							w.dropItem(e.getLocation(),new ItemStack(drop));
							Utils.lower_cauldron_water(b.getState());
			                cauls.add(cauldron);
			                caulticks.add(0);
						}
					}
				}
			}
		}
		
		t2 = System.nanoTime();

		for(int i = left_rotate_cooldown.size()-1; i >=0; i--) {
			CooldownPlayer cp = left_rotate_cooldown.get(i);
			if(!cp.tick()) {
				left_rotate_cooldown.remove(i);
			}
		}

		t3 = System.nanoTime();
		
		ItemStack chestplate;
		for(World w : server.getWorlds())
		{
			for(Turtle turtle : w.getEntitiesByClass(Turtle.class))
			{
				// saddled entities
				if (Utils.hasBeaconData(turtle, beacon_id_saddle)) {
					if (turtle.getPassengers().size() == 0 || turtle.getVehicle() == null )
						turtle.remove();
					else {
						LivingEntity camel_player = (LivingEntity)turtle.getVehicle();
						turtle.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue( camel_player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() );
						turtle.setHealth( camel_player.getHealth() );
					}
				}
				// leashed players
				else if (Utils.hasBeaconData(turtle, LeashedPlayer.BEACON_ID)) {
					
					if (!leash_manager.isWorkaroundActive(turtle) || !turtle.isLeashed()) {
						turtle.getWorld().dropItem(turtle.getLocation(), new ItemStack(Material.LEAD, 1));
						turtle.remove();
					}
				}
			}
			
		}
		
		t4 = System.nanoTime();

		//System.out.println( (t2 - t1)/(1000000000.0 / 20) + "(Item), " + (t3 - t2)/(1000000000.0 / 20) + "(cooldown), "+(t4 - t3)/(1000000000.0 / 20)+"(Turtle)." );
	}
	
	@EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event){
		if(event.isCancelled()) return;
		
        Entity rightclicked = event.getRightClicked();
        //if(rightclick instanceof Player)
        if(rightclicked instanceof LivingEntity) {
    	   //ride on entity
    	   ItemStack hat = ((LivingEntity)rightclicked).getEquipment().getHelmet();
    	   if(rightclicked.getPassengers().size() == 0 && hat != null && hat.getType() == Material.SADDLE) {
    		   LivingEntity temp = spawnSaddleBeacon(rightclicked.getLocation());
        	   rightclicked.addPassenger(temp);
        	   temp.addPassenger(event.getPlayer());
        	   return;
    	   }
        }
       
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand() != null ? event.getPlayer().getInventory().getItemInMainHand()
    		   : (event.getPlayer().getInventory().getItemInOffHand() != null ? event.getPlayer().getInventory().getItemInOffHand() : null );
       
        boolean cancelled = leash_manager.click(rightclicked, event.getPlayer(), hand);
        if(cancelled) {
        	event.setCancelled(true);
        	return;
        }
        
        if(hand != null) {
        	if(hand.getType() == Material.NAME_TAG) {
            	if(hand.getItemMeta().hasDisplayName() && hand.getItemMeta().getDisplayName() == rightclicked.getCustomName())
        	    	event.setCancelled(true);
        	}
        }
    }
	
	@SuppressWarnings("deprecation")
	@EventHandler//(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.isCancelled() && !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)) return;
		if(event.getAction() == Action.LEFT_CLICK_BLOCK && is_left_click_on_cooldown(event.getPlayer()))
			return;
		if (event.hasBlock() && event.getItem() != null) {
			if (!event.getPlayer().isSneaking() && event.getClickedBlock().getType() == Material.CAULDRON)
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
					event.getPlayer().getInventory().addItem(washed); //TO DO: replace all of "Inventory.addItem()" with working function #thx1.13 
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
				if ( UtilsType.is_banner(event.getClickedBlock().getType()) || UtilsType.is_wall_banner(event.getClickedBlock().getType()) && event.getItem().getAmount()>5 )
				{
					if ( ((Banner)event.getClickedBlock().getState()).getBaseColor() != ((Dye)event.getItem().getData()).getColor() ) {
						Block banner = event.getClickedBlock();
						Banner b = (Banner)banner.getState();
						b.setBaseColor( ((Dye)event.getItem().getData()).getColor() );
						b.update();
						event.getItem().setAmount(event.getItem().getAmount()-6);
					}
					return;
					
				}
				
				DyeColor clicked_block_color = UtilsColor.colorFromMaterial(event.getClickedBlock().getType());
				DyeColor clicking_dye_color = UtilsColor.colorFromMaterial(event.getItem().getType());
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
			if (handMaterial.equals(Material.WOODEN_HOE) ||
				handMaterial.equals(Material.STONE_HOE) ||
				handMaterial.equals(Material.IRON_HOE) ||
				handMaterial.equals(Material.GOLDEN_HOE) ||
				handMaterial.equals(Material.DIAMOND_HOE) )
			{
				//ROTATE BLOCKS
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK && RotatableBlock.rotate_attempt(event.getClickedBlock(), event.getPlayer().isSneaking())
						|| event.getAction() == Action.LEFT_CLICK_BLOCK && RotatableBlock.left_click_rotate_attempt(event.getClickedBlock(), event.getPlayer().isSneaking())) {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK)
						left_rotate_cooldown.add(new CooldownPlayer(event.getPlayer(), Config.LEFT_ROTATE_COOLDOWN));
					byte dur = (byte) ((Math.random()*(event.getItem().getEnchantmentLevel(Enchantment.DURABILITY)+1)) > 1 ? 0 : 1);
					event.getItem().setDurability((short) (event.getItem().getDurability()+dur));
					if (event.getItem().getDurability() > event.getItem().getType().getMaxDurability())
						if (event.getHand() == EquipmentSlot.HAND)
							event.getPlayer().getInventory().setItemInMainHand(null);
						else
							event.getPlayer().getInventory().setItemInOffHand(null);
				}
			}
			
			//change bed linen
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking() && 
					UtilsType.is_carpet(handMaterial) && UtilsType.is_bed(event.getClickedBlock().getType())) {
				DyeColor c_carpet = UtilsColor.colorFromMaterial(handMaterial);
				DyeColor c_bed = UtilsColor.colorFromMaterial(event.getClickedBlock().getType());
				if(c_bed != c_carpet) {
					Block blockfoot = event.getClickedBlock();
					Bed bedfoot = (Bed)blockfoot.getBlockData();
					BlockFace face = bedfoot.getFacing();
					Block blockhead;
					Bed bedhead;
					if(bedfoot.getPart() == Part.FOOT) {
						blockhead = blockfoot.getRelative(face);
						bedhead = (Bed)blockhead.getBlockData();
					}
					else {
						bedhead = bedfoot;
						blockhead = blockfoot;
						blockfoot = blockhead.getRelative(face.getOppositeFace());
						bedfoot = (Bed)blockfoot.getBlockData();
					}
					blockhead.setType(UtilsColor.fromColor_bed(c_carpet));
					blockfoot.setType(UtilsColor.fromColor_bed(c_carpet));
					bedfoot = (Bed)blockfoot.getBlockData();
					bedfoot.setFacing(face);
					bedhead = (Bed)blockhead.getBlockData();
					bedhead.setPart(Part.HEAD);
					bedhead.setFacing(face);
					blockfoot.setBlockData(bedfoot);
					blockhead.setBlockData(bedhead);
					
					
					if(event.getItem().getAmount() == 1)
						event.getItem().setType(UtilsColor.fromColor_carpet(c_bed));
					else {
						event.getItem().setAmount(event.getItem().getAmount()-1);
						event.getPlayer().getInventory().addItem(new ItemStack(UtilsColor.fromColor_carpet(c_bed), 1));
					}
				}
			}
		} //has both block and item

		//jump rope and lasso
		if(event.getItem() != null && event.getItem().getType() == Material.LEAD) {
			ItemStack hand = event.getItem();
			Player player = event.getPlayer();
			ItemStack lead_drops = hand.clone();
			lead_drops.setAmount(1);
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK && UtilsType.isFence(event.getClickedBlock().getType())) {
				
				List <Entity> entities = event.getPlayer().getNearbyEntities(15, 15, 15);
				for(Entity e : entities)
					if(e instanceof LivingEntity && ((LivingEntity)e).isLeashed() && ((LivingEntity)e).getLeashHolder() == player)
						return;
				
				event.setCancelled(true);
				Location hitch_loc = event.getClickedBlock().getLocation();
				LeashHitch hitch = hitch_loc.getWorld().spawn(hitch_loc, LeashHitch.class);
				leash_manager.addLeashed(hitch, event.getPlayer(), lead_drops);
		    	if(player.getGameMode() != GameMode.CREATIVE)
		    		hand.setAmount(hand.getAmount()-1);
			}
			else if(event.getAction() == Action.RIGHT_CLICK_AIR
					|| event.getAction() == Action.RIGHT_CLICK_BLOCK && !UtilsType.isInteractable(event.getClickedBlock().getType())) {
				leash_manager.throwLasso(event.getPlayer(), lead_drops);
		    	if(player.getGameMode() != GameMode.CREATIVE)
		    		hand.setAmount(hand.getAmount()-1);
			}
			else if(event.getAction() == Action.LEFT_CLICK_AIR)
			{
				leash_manager.throwTargetLasso(event.getPlayer(), lead_drops);
		    	if(player.getGameMode() != GameMode.CREATIVE)
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
