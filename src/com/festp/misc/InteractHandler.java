package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.festp.Config;
import com.festp.CooldownPlayer;
import com.festp.Main;
import com.festp.utils.Utils;
import com.festp.utils.UtilsColor;
import com.festp.utils.UtilsType;

public class InteractHandler implements Listener
{
	public static final int CAULDRON_COOLDOWN = 6;
	
	//cauldrons to wash items
	List<CooldownedCauldron> cauls = new ArrayList<>();
	List<Item> worldItems = new ArrayList<>();

	List<CooldownPlayer> leftRotateCooldown = new ArrayList<>();
	
	Main plugin;
	Server server;
	
	public InteractHandler(Main pl) {
		this.plugin = pl;
		this.server = pl.getServer();
	}
	
	public void onTick()
	{
		for (int i = leftRotateCooldown.size() - 1; i >= 0; i--) {
			CooldownPlayer cp = leftRotateCooldown.get(i);
			if (!cp.tick()) {
				leftRotateCooldown.remove(i);
			}
		}
		
		for (int i = cauls.size() - 1; i >= 0; i--) {
			cauls.get(i).ticks--;
			
			if (cauls.get(i).ticks <= 0) {
				cauls.remove(i);
			}
		}

		for (int i = worldItems.size() - 1; i >= 0; i--)
		{
			Item item = worldItems.get(i);
			
			if (!item.isValid()) {
				worldItems.remove(i);
				continue;
			}
			World w = item.getWorld();
			Block b = item.getLocation().getBlock();
			if (b.getType() == Material.WATER_CAULDRON) {
				BlockData cauldron = b.getBlockData();
				if (Utils.getCauldronLevel(cauldron) > 0) {
					boolean found = false;
					for (CooldownedCauldron cooldowned : cauls)
						if (cooldowned.cauldron.equals(b))
							found = true;
					if (found) {
						continue;
					}
					Material m = item.getItemStack().getType();
					Material dropMaterial = getCauldroned(m);
					if (dropMaterial != null) {
						item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
						w.dropItem(item.getLocation(), new ItemStack(dropMaterial, 1));
						Utils.lowerCauldronWater(b);
						CooldownedCauldron coolCauldron = new CooldownedCauldron(CAULDRON_COOLDOWN, b);
						cauls.add(coolCauldron);
		                
						if (item.getItemStack().getAmount() <= 0)
							worldItems.remove(i);
					}
				}
			}
		}
	}
	
	public Material getCauldroned(Material m)
	{
		if (UtilsType.is_concrete_powder(m))
			return UtilsColor.fromColor_concrete(UtilsColor.colorFromMaterial(m));
		else if (UtilsType.is_colored_terracotta(m))
			return Material.TERRACOTTA;
		else if (UtilsColor.colorFromMaterial(m) != DyeColor.WHITE) {
			 if (UtilsType.is_wool(m)) 
				 return Material.WHITE_WOOL;
			 else if (UtilsType.is_carpet(m)) 
				 return Material.WHITE_CARPET;
			 /*else if (UtilsType.is_glazed_terracotta(m))
				 return Material.WHITE_GLAZED_TERRACOTTA;
			 else if (UtilsType.is_concrete(m))
				 return Material.WHITE_CONCRETE;*/
		}
		else if (m.equals(Material.RED_SAND))
			return Material.SAND;
		else if (m.equals(Material.RED_SANDSTONE))
			return Material.SANDSTONE;
		return null;
	}
	
	public boolean isWashable(Material m)
	{
		return getCauldroned(m) != null;
	}
	public boolean isWashable(Item dropped_item)
	{
		Material m = dropped_item.getItemStack().getType();
		return isWashable(m);
	}
	
	// loading new items(for cauldrons)
	public void addEntity(Entity e)
	{
		if (e.getType() == EntityType.DROPPED_ITEM) {
			Item dropped_item = (Item) e;
			if (isWashable(dropped_item))
				worldItems.add(dropped_item);
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
	
	/** multi nametag block */
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
	{
		if (event.isCancelled()) return;
		
        Entity rightclicked = event.getRightClicked();
       
        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand() != null ? event.getPlayer().getInventory().getItemInMainHand()
    		   : (event.getPlayer().getInventory().getItemInOffHand() != null ? event.getPlayer().getInventory().getItemInOffHand() : null );
        
        if (hand != null) {
        	if (hand.getType() == Material.NAME_TAG) {
            	if (hand.getItemMeta().hasDisplayName() && hand.getItemMeta().getDisplayName() == rightclicked.getCustomName())
        	    	event.setCancelled(true);
        	}
        }
    }

	/** cauldron clicks(item clearing), dirt->grass, block recoloring, rotating, bed linen */
	@SuppressWarnings("deprecation")
	@EventHandler//(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (event.isCancelled() && !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR)) return;
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && isOnLeftClickCooldown(player))
			return;
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() && event.getClickedBlock().getType() == Material.CAKE) {
			if (!player.isSneaking() && player.getFoodLevel() >= 20) {
				Block block = event.getClickedBlock();
				BlockBreakEvent breakEvent = new BlockBreakEvent(block, player);
				Bukkit.getPluginManager().callEvent(breakEvent);
				if (!breakEvent.isCancelled()) {
					Cake cake = (Cake) block.getBlockData();
					if (cake.getBites() == cake.getMaximumBites()) {
						event.getClickedBlock().setType(Material.AIR);
					} else {
						cake.setBites(cake.getBites() + 1);
						event.getClickedBlock().setBlockData(cake);
					}
					player.setSaturation(Math.min(player.getSaturation() + 0.4f, player.getFoodLevel()));
				}
				return;
			}
		}
		if (event.hasBlock() && event.getItem() != null)
		{
			Material handMaterial = event.getItem().getType();
			Block clicked = event.getClickedBlock();
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if (!player.isSneaking() && clicked.getType() == Material.WATER_CAULDRON)
				{
					if (Utils.getCauldronWater(clicked) == 0.0)
						return;
					Material washedMaterial = getCauldroned(event.getItem().getType());
					if (washedMaterial == null)
						return;
					ItemStack washed = new ItemStack(washedMaterial, 1);
					event.setCancelled(true);
					event.getItem().setAmount(event.getItem().getAmount() - 1);
					player.getInventory().addItem(washed); // TODO: replace all of "Inventory.addItem()" with working function(Utils.giveOrDrop) #thx1.13 
					Utils.lowerCauldronWater(clicked);
				}
				
				Material currentblock = clicked.getType();
				// tool shift rightclicks
				if (!player.isSneaking())
				{
					boolean axeCond = UtilsType.isAxe(handMaterial) && (UtilsType.isLog(currentblock) || UtilsType.isWoodBark(currentblock));
					boolean shovelCond = UtilsType.isShovel(handMaterial) && (currentblock == Material.COARSE_DIRT
							|| currentblock == Material.PODZOL || currentblock == Material.ROOTED_DIRT);
					if (axeCond || shovelCond)
					{
						event.setCancelled(true);
						return;
					}
				}
				//grass from dirt
				if (currentblock.equals(Material.DIRT) && handMaterial.equals(Material.BONE_MEAL)) {
					clicked.setType(Material.GRASS_BLOCK);
					event.getItem().setAmount(event.getItem().getAmount() - 1);
					event.setCancelled(true);
					return;
				} 
				
				if (UtilsType.is_dye(handMaterial) ) 
				{
					DyeColor clicked_block_color = UtilsColor.colorFromMaterial(currentblock);
					DyeColor clicking_dye_color = UtilsColor.colorFromMaterial(handMaterial);
					Material block_material = clicked.getType();
					boolean is_wall_banner = UtilsType.is_wall_banner(block_material);
					boolean is_banner = UtilsType.is_banner(block_material);
					if ( (is_banner || is_wall_banner)
							&& event.getItem().getAmount() > 5 )
					{
						Block banner_block = clicked;
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

							if (player.getGameMode() != GameMode.CREATIVE)
								event.getItem().setAmount(event.getItem().getAmount() - 6);
						}
						return;
						
					}
					
					if (clicked_block_color == clicking_dye_color)
						return;
					if (UtilsType.is_terracotta(currentblock)) clicked.setType(UtilsColor.fromColor_terracotta(clicking_dye_color));
					else if (UtilsType.is_wool(currentblock)) clicked.setType(UtilsColor.fromColor_wool(clicking_dye_color));
					else if (UtilsType.is_concrete_powder(currentblock)) clicked.setType(UtilsColor.fromColor_concrete_powder(clicking_dye_color));
					else if (UtilsType.is_concrete(currentblock)) clicked.setType(UtilsColor.fromColor_concrete(clicking_dye_color));
					else if (UtilsType.is_carpet(currentblock)) clicked.setType(UtilsColor.fromColor_carpet(clicking_dye_color));
					else return;
					
					if (player.getGameMode() != GameMode.CREATIVE)
						event.getItem().setAmount(event.getItem().getAmount() - 1);
					
					return;
				}

				//change bed linen
				if (!player.isSneaking() && UtilsType.is_carpet(handMaterial)
						&& UtilsType.is_bed(clicked.getType())) {
					DyeColor newColor = UtilsColor.colorFromMaterial(handMaterial);
					DyeColor oldColor = UtilsColor.colorFromMaterial(clicked.getType());
					if (oldColor != newColor) {
						Material newBedMaterial = UtilsColor.fromColor_bed(newColor); // TODO BlockType.BED - explicit (enum) argument
						Material newCarpetMaterial = UtilsColor.fromColor_carpet(oldColor);
						Material particleMaterial = UtilsColor.fromColor_wool(oldColor);
						Block blockFoot = clicked;
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
			}
			
			if (UtilsType.isHoe(handMaterial))
			{
				//ROTATE BLOCKS
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK && RotatableBlock.rotate_attempt(clicked, player.isSneaking())
						|| event.getAction() == Action.LEFT_CLICK_BLOCK && RotatableBlock.left_click_rotate_attempt(clicked, player.isSneaking())) {
					if (event.getAction() == Action.LEFT_CLICK_BLOCK)
						leftRotateCooldown.add(new CooldownPlayer(player, Config.LEFT_ROTATE_COOLDOWN));
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
		} // has both block and item
	}
	
	public boolean isOnLeftClickCooldown(Player p) {
		for(int i = 0; i < leftRotateCooldown.size(); i++) {
			if(leftRotateCooldown.get(i).getPlayer() == p)
				return true;
		}
		return false;
	}
}
