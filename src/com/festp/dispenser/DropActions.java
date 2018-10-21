package com.festp.dispenser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftAgeable;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftAnimals;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.Utils;
import com.festp.mainListener;

import net.minecraft.server.v1_13_R2.EntityAgeable;
import net.minecraft.server.v1_13_R2.EntityAnimal;

public class DropActions implements Listener {
	mainListener pl;

	int max_dy = 50;
	static int max_dxz = 50;
	int min_dxz = -max_dxz;
	int center_xz = max_dxz+1;
	int in_test_max_dxz = max_dxz*2;
	
	static Enchantment bottomless_bucket_metaench = Enchantment.ARROW_INFINITE;
	
	//cauldrons to fill with dispenser
	List<ArrayList<Integer>> disps = new ArrayList<ArrayList<Integer>>();
	List<BlockState> disp_caul = new ArrayList<>();
	//dispensers to feed animals
	List<BlockState> dbf_disp = new ArrayList<>();
	List<Block> dbf_block = new ArrayList<>();
	List<Material> dbf_food = new ArrayList<>();
	List<Integer[]> dbf_food_slots = new ArrayList<>();
	List<EntityAnimal> loveanimals = new ArrayList<>();
	//dispensers to pump the water
	List<Dispenser> disps_pump = new ArrayList<>();

	enum PumpReadiness {READY, MODULE, NONE};
	enum PumpType {NONE, REGULAR, ADVANCED};
	
	public DropActions(mainListener plugin) {
		this.pl = plugin;
	}
	
	public void onTick() {
		//dispbreed
		for(int i=0;i<dbf_disp.size();i++) {
			dispenserAnimals(dbf_disp.get(i),dbf_block.get(i),dbf_food.get(i),dbf_food_slots.get(i));
		}
		dbf_disp = new ArrayList<>();
		dbf_block = new ArrayList<>();
		dbf_food = new ArrayList<>();
		dbf_food_slots = new ArrayList<>();
		//lovehearths
		for(int i=0;i<loveanimals.size();i++) {
			/*int love = (Integer)getPrivateField("bx", EntityAnimal.class, loveanimals.get(i));
			if(love > 0)
			{
				//loveanimals.get(i).n();
				//setPrivateField("bx", EntityAnimal.class, loveanimals.get(i), love);
				if(love%10 == 0) summonHearths(loveanimals.get(i));
			}
			else {*/
				loveanimals.remove(i);
				i--;
			/*}*/
		}
		//dispwater
		for(int i=0;i<disp_caul.size();i+=2) {
			int o = 0;
            Inventory inv = ((Dispenser)disp_caul.get(i)).getInventory();
			for(int slot=0;slot<9;slot++)
				if(o<disps.get(i/2).size() && slot != disps.get(i/2).get(o) || o>=disps.get(i/2).size()) {
					if(inv.getItem(slot) != null && inv.getItem(slot).getType() == Material.WATER_BUCKET) {
	                	inv.setItem(slot,new ItemStack(Material.BUCKET));
	                    Utils.full_cauldron_water(disp_caul.get(i+1));
		                break;
					}
				} else o++;
		}
		disp_caul = new ArrayList<>();
		disps = new ArrayList<ArrayList<Integer>>();
		
		for(int i=disps_pump.size()-1;i>=0;i--) {
			dispenserPump(disps_pump.get(i));
			disps_pump.remove(i);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event)
	{
		if(event.getItem() != null && event.getBlock().getType() == Material.DISPENSER)
		{
            BlockState dispenser = event.getBlock().getState(); //0 - down, 1 - up, 2 - north(-z), 3 - south(+z), 4 - west(-x), 5 - east(+x) 
			byte data = (byte) (dispenser.getData().getData()%8);
			int x2 = (data==4 ? -1 : (data==5 ? 1 : 0)), y2 = (data==0 ? -1 : (data==1 ? 1 : 0)), z2 = (data==2 ? -1 : (data==3 ? 1 : 0));
			int x = event.getBlock().getX(), y = event.getBlock().getY(), z = event.getBlock().getZ();
			final Block block = event.getBlock().getWorld().getBlockAt(x+x2, y+y2, z+z2);
			boolean breed = false;
			
			
			
			if(event.getItem().getType().equals(Material.WATER_BUCKET))
			{
				if(block.getType() == Material.CAULDRON)
				{
	                BlockState cauldron = block.getState();
	                if(cauldron.getData().getData() < 3) {
	                    event.setCancelled(true);
	                    //int i = disp_caul.size()/2;
	                    disp_caul.add(dispenser);
	                    disp_caul.add(cauldron);
	                    Inventory inv = ((Dispenser)dispenser).getInventory();
	                    ArrayList<Integer> e = new ArrayList<>();
						for(int slot=0;slot<9;slot++)
							if(inv.getItem(slot) != null && inv.getItem(slot).getType() == Material.WATER_BUCKET)
								e.add(slot);
						disps.add(e);
	                }
	                return;
				}
			}
			
			
			
			else if(event.getItem().getType().equals(Material.WHEAT))
			{
				for(Entity e : event.getBlock().getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1))
						if((e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP) && ((CraftAnimals) e).isAdult()
								&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
						{
							breed = true;
							break;
						} else if((e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP) && !((CraftAnimals) e).isAdult()) {
							breed = true;
							break;
						}
			}
			else if(event.getItem().getType().equals(Material.CARROT) || event.getItem().getType().equals(Material.POTATO) || event.getItem().getType().equals(Material.BEETROOT))
			{
				for(Entity e : event.getBlock().getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1))
						if(e.getType() == EntityType.PIG && ((CraftAnimals) e).isAdult()
						&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
						{
							breed = true;
							break;
						} else if(e.getType() == EntityType.PIG && !((CraftAnimals) e).isAdult()) {
							breed = true;
							break;
						}
			}
			else if(event.getItem().getType().equals(Material.WHEAT_SEEDS) || event.getItem().getType().equals(Material.MELON_SEEDS) || event.getItem().getType().equals(Material.PUMPKIN_SEEDS) || event.getItem().getType().equals(Material.BEETROOT_SEEDS))
			{
				for(Entity e : event.getBlock().getWorld().getNearbyEntities(block.getLocation(), 1, 1, 1))
						if(e.getType() == EntityType.CHICKEN && ((CraftAnimals) e).isAdult()
						&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
						{
							breed = true;
							break;
						} else if(e.getType() == EntityType.CHICKEN && !((CraftAnimals) e).isAdult()) {
							breed = true;
							break;
						}
			}
			
			if(breed)
			{
				dbf_disp.add(dispenser);
				dbf_block.add(block);
				dbf_food.add(event.getItem().getType());
				Inventory inv = ((Dispenser)dispenser).getInventory();
				dbf_food_slots.add(new Integer[] {
						inv.getItem(0) == null ? 0 : inv.getItem(0).getAmount(),
						inv.getItem(1) == null ? 0 : inv.getItem(1).getAmount(),
						inv.getItem(2) == null ? 0 : inv.getItem(2).getAmount(),
						inv.getItem(3) == null ? 0 : inv.getItem(3).getAmount(), 
						inv.getItem(4) == null ? 0 : inv.getItem(4).getAmount(),
						inv.getItem(5) == null ? 0 : inv.getItem(5).getAmount(),
						inv.getItem(6) == null ? 0 : inv.getItem(6).getAmount(),
						inv.getItem(7) == null ? 0 : inv.getItem(7).getAmount(),
						inv.getItem(8) == null ? 0 : inv.getItem(8).getAmount() });
				event.setCancelled(true);
			}
			
			
			
			else {
				Dispenser d = ((Dispenser)dispenser);
				PumpReadiness pr = dispenserPump_test(d, event.getItem());
				if(pr == PumpReadiness.READY) {
					disps_pump.add(d);
					event.setCancelled(true);
				}
				else if(pr == PumpReadiness.MODULE) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	public void dispenserCauldron() {
		
	}
	
	public void dispenserAnimals(BlockState d, Block b, Material f, Integer[] slots) {
		//dispencer test and handle slot
		Inventory inv = ((Dispenser)d).getInventory();
		Integer it = null;
		for(int slot=0;slot<9;slot++)
			if(inv.getItem(slot) != null && inv.getItem(slot).getType() == f && inv.getItem(slot).getAmount() != slots[slot]) {
				it = slot;
			}
		if(it == null) return;
		
		//Breeding
		boolean animalFound = false;
		EntityAnimal loveanimal = null;
		if(f.equals(Material.WHEAT))
		{
			for(Entity e : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1))
					if((e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP) && ((CraftAnimals) e).isAdult()
							&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
					{
						//(((CraftAnimals) e).getHandle()).f(eh);
						animalFound = Utils.setLove(((CraftAnimals) e).getHandle(), f);
						if(animalFound) loveanimal = ((CraftAnimals) e).getHandle();
						break;
					} else if((e.getType() == EntityType.COW || e.getType() == EntityType.SHEEP) && !((CraftAnimals) e).isAdult()) {
						((CraftAgeable) e).getHandle().setAge((int)(-((CraftAgeable) e).getAge() / 20 * 0.1F), true);
						animalFound = true;
						break;
					}
		}
		else if(f.equals(Material.CARROT) || f.equals(Material.POTATO) || f.equals(Material.BEETROOT))
		{
			for(Entity e : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1))
					if(e.getType() == EntityType.PIG && ((CraftAnimals) e).isAdult()
					&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
					{
						animalFound = Utils.setLove(((CraftAnimals) e).getHandle(), f);
						if(animalFound) loveanimal = ((CraftAnimals) e).getHandle();
						break;
					} else if(e.getType() == EntityType.PIG && !((CraftAnimals) e).isAdult()) {
						((CraftAgeable) e).getHandle().setAge((int)(-((CraftAgeable) e).getAge() / 20 * 0.1F), true);
						animalFound = true;
						break;
					}
		}
		else if(f.equals(Material.WHEAT_SEEDS) || f.equals(Material.MELON_SEEDS) || f.equals(Material.PUMPKIN_SEEDS) || f.equals(Material.BEETROOT_SEEDS))
		{
			for(Entity e : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1))
					if(e.getType() == EntityType.CHICKEN && ((CraftAnimals) e).isAdult()
					&& !(((CraftAnimals) e).getHandle()).isInLove() && !((Integer)( Utils.getPrivateField("b", EntityAgeable.class, (((CraftAnimals) e).getHandle())) ) > 0))
					{
						animalFound = Utils.setLove(((CraftAnimals) e).getHandle(), f);
						if(animalFound) loveanimal = ((CraftAnimals) e).getHandle();
						break;
					} else if(e.getType() == EntityType.CHICKEN && !((CraftAnimals) e).isAdult()) {
						((CraftAgeable) e).getHandle().setAge((int)(-((CraftAgeable) e).getAge() / 20 * 0.1F), true);
						animalFound = true;
						break;
					}
		}
		
		if(animalFound) {
			inv.setItem(it, inv.getItem(it).getAmount() > 1 ? new ItemStack(f, inv.getItem(it).getAmount()-1) : new ItemStack(Material.AIR));
			loveanimals.add(loveanimal);
		}
	}
	
	public PumpReadiness dispenserPump_test(Dispenser d, ItemStack dropped) {
		Inventory inv = d.getInventory();
		//test empty bucket
		//test pump module??? - it had already worked
		int bucket_index = -2, module_index = -2, multybucket_index = -2, null_index = -1, pipe_index = -1;
		ItemStack is;
		for(int i = -1; i < 9; i++) {
			if(i<0) is = dropped;
			else is = inv.getItem(i);
			if(is != null)
			{
				if(module_index < -1 && is.getType() == Material.BLAZE_ROD
						&& is.hasItemMeta() && is.getItemMeta().hasLore()) {
					String lore = is.getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH);
					if(lore.contains("pump") || lore.contains("помп") ) {
						module_index = i;
						if(bucket_index >= -1 || (multybucket_index >= -1 && null_index >= 0)) break;
					}
				}
				else if( is.getType() == Material.BUCKET ) {
					if(is.getEnchantmentLevel(bottomless_bucket_metaench) > 0)
						bucket_index = 9;
					else if(is.getAmount() == 1 && bucket_index < -1)
						bucket_index = i;
					else if( multybucket_index < -1) {
						multybucket_index = i;
						if(null_index < 0) continue;
					}
					if(module_index >= -1) break;
				}
				else if(is.getType() == Material.NETHER_BRICK_FENCE)
					pipe_index = i;
			}
			else if(null_index < 0) null_index = i;
		}
		//System.out.println("TEST: "+ module_index+" "+bucket_index+" "+multybucket_index+" "+null_index);
		if(module_index >= -1) {
			if(bucket_index >= -1 || (multybucket_index >= -1 && null_index >= 0) || pipe_index >= -1) {
				return PumpReadiness.READY;
			}
			return PumpReadiness.MODULE;
		}
		return PumpReadiness.NONE;
	}
	
	public void dispenserPump(Dispenser d) {
		Inventory inv = d.getInventory();
		//test empty bucket
		//test pump module??? - it had already worked
		int bucket_index = -1, module_index = -1;
		PumpType pump_type = PumpType.NONE; 
		//boolean has_pipe = false, has_empty = false;
		int pipe_index = -1, null_index = -1, multybucket_index = -1;
		for(int i = 0; i < 9; i++) {
			ItemStack is;
			is = inv.getItem(i);
			if(is != null)
			{
				if(module_index < 0 && is.getType() == Material.BLAZE_ROD
						&& is.hasItemMeta() && is.getItemMeta().hasLore()) {
					String lore = is.getItemMeta().getLore().get(0).toLowerCase(Locale.ENGLISH); //new Locale("ru")
					if(lore.contains("pump") || lore.contains("помп") ) {
						if(lore.contains("regular") || lore.contains("обычн"))
						{
							module_index = i;
							pump_type = PumpType.REGULAR;
							if(bucket_index >= 0) break;
						}
						else if(lore.contains("advanced") || lore.contains("продвинут"))
						{
							module_index = i;
							pump_type = PumpType.ADVANCED;
							if(bucket_index >= 0) break;
						}
					}
				}
				else if( is.getType() == Material.BUCKET ) {
					if(is.getEnchantmentLevel(bottomless_bucket_metaench) > 0)
						bucket_index = 9;
					else if(is.getAmount() == 1 && bucket_index < 0)
						bucket_index = i;
					else if( multybucket_index < 0) {
						multybucket_index = i;
						if(null_index < 0) continue;
					}
					if(module_index >= 0) break;
				}
				else if(is.getType() == Material.NETHER_BRICK_FENCE)
					pipe_index = i;
			}
			else if(null_index < 0) null_index = i;
		}
		//System.out.println("WORK: "+ module_index+" "+bucket_index+" "+multybucket_index+" "+null_index+"   "+pipe_index );
		if(module_index >= 0 && ( bucket_index >= 0 || pipe_index >= 0 || (null_index >= 0 && multybucket_index >= 0)) ) {
			if(pump_type == PumpType.REGULAR)
				work_regularPump(d, bucket_index, null_index, multybucket_index);
			else if(pump_type == PumpType.ADVANCED)
				work_advancedPump(d, bucket_index);
		}
	}
	
	public void work_regularPump(Dispenser d, int bucket_index, int null_index, int multybucket_index) {
		Inventory inv = d.getInventory();
		byte data = (byte) (d.getData().getData()%8);
		int x2 = (data==4 ? -1 : (data==5 ? 1 : 0)), y2 = (data==0 ? -1 : (data==1 ? 1 : 0)), z2 = (data==2 ? -1 : (data==3 ? 1 : 0));
		/*int x = d.getBlock().getX(), y = d.getBlock().getY(), z = d.getBlock().getZ();
		final Block block = d.getBlock().getWorld().getBlockAt(x+x2, y+y2, z+z2);*/
		Block block = d.getBlock().getRelative(x2, y2, z2);
		Block block_to_pump = null;
		//can place pipe
		System.out.println(y2);
		if(y2 <= 0) {
			int pipes = 0;
			//scroll all placed pipe blocks
			while(block.getType() == Material.NETHER_BRICK_FENCE) {
				block = block.getRelative(0, -1, 0);
				pipes += 1;
			}
			int pipe_index = -1;
			Block test_liquid = block;
			//test available liquid below pipe
			while(/*test_liquid.isEmpty()*/Utils.isAir(test_liquid.getType()) || Utils.isFlowingLiquid(test_liquid) || test_liquid.getType() == Material.NETHER_BRICK_FENCE
					 || Utils.isSlab(test_liquid.getType())) {
				if(test_liquid.isLiquid()) {
					block_to_pump = findBlockToPump_regular(test_liquid);
					if(block_to_pump == null)
						test_liquid = test_liquid.getRelative(0, -1, 0);
					else break;
				}
				test_liquid = test_liquid.getRelative(0, -1, 0);
			}
			//remove fences
			if(findBlockToPump_regular(test_liquid) == null) {
				if(pipes > 0) {
					for(int i = 0; i < 9; i++) {
						ItemStack is;
						is = inv.getItem(i);
						if(is != null && is.getType() == Material.NETHER_BRICK_FENCE && is.getAmount() < 64)
						{
							pipe_index = i;
							is.setAmount(is.getAmount()+1);
							block.getRelative(0, 1, 0).setType(Material.AIR);
							break;
						}
					}
					if(pipe_index < 0 && null_index >= 0) {
						inv.setItem(null_index, new ItemStack(Material.NETHER_BRICK_FENCE, 1));
						block.getRelative(0, 1, 0).setType(Material.AIR);
					}
				}
				return;
			}
			//place fences
			block_to_pump = findBlockToPump_regular(block);
			if(block.isEmpty() || (block.isLiquid() && block_to_pump == null)) {
				for(int i = 0; i < 9; i++) {
					ItemStack is;
					is = inv.getItem(i);
					if(is != null && is.getType() == Material.NETHER_BRICK_FENCE)
					{
						pipe_index = i;
						break;
					}
				}
				if(pipe_index >= 0) {
					ItemStack pipe = inv.getItem(pipe_index);
					pipe.setAmount(pipe.getAmount() - 1);
					block.setType(Material.NETHER_BRICK_FENCE);
					block = block.getRelative(0, -1, 0);
				}
				return;
			}
		}
		if( bucket_index < 0 && ( multybucket_index < 0 || null_index < 0 ) ) {
			return;
		}
		//pump
		if(block_to_pump == null) block_to_pump = findBlockToPump_regular(block);
		if(block_to_pump != null) {
			if(bucket_index < 9) {
				if(bucket_index < 0) {
					inv.getItem(multybucket_index).setAmount(inv.getItem(multybucket_index).getAmount()-1);
					bucket_index = null_index;
				}
				if(block_to_pump.getType() == Material.LAVA)
					inv.setItem(bucket_index, new ItemStack(Material.LAVA_BUCKET));
				else if(block_to_pump.getType() == Material.WATER)
					inv.setItem(bucket_index, new ItemStack(Material.WATER_BUCKET));
			}
			block_to_pump.setType(Material.AIR);
		}
	}
	
	public void work_advancedPump(Dispenser d, int bucket_index) {
		Inventory inv = d.getInventory();
		byte data = (byte) (d.getData().getData()%8);
		int x2 = (data==4 ? -1 : (data==5 ? 1 : 0)), y2 = (data==0 ? -1 : (data==1 ? 1 : 0)), z2 = (data==2 ? -1 : (data==3 ? 1 : 0));
		/*int x = d.getBlock().getX(), y = d.getBlock().getY(), z = d.getBlock().getZ();
		final Block block = d.getBlock().getWorld().getBlockAt(x+x2, y+y2, z+z2);*/
		Block block = d.getBlock().getRelative(x2, y2, z2);
		Block block_to_pump = null;
		if(y2 <= 0) {
			int pipes = 0;
			while(block.getType() == Material.NETHER_BRICK_FENCE) {
				block = block.getRelative(0, -1, 0);
				pipes += 1;
			}
			int pipe_index = -1;
			Block test_liquid = block;
			while(test_liquid.isEmpty())
				test_liquid = test_liquid.getRelative(0, -1, 0);
			//remove fences
			if(!test_liquid.isLiquid()) {
				if(pipes > 0) {
					int null_index = -1;
					for(int i = 0; i < 9; i++) {
						ItemStack is;
						is = inv.getItem(i);
						if(is == null && null_index < 0) {
							null_index = i;
						}
						else if(is != null && is.getType() == Material.NETHER_BRICK_FENCE && is.getAmount() < 64)
						{
							pipe_index = i;
							is.setAmount(is.getAmount()+1);
							block.getRelative(0, 1, 0).setType(Material.AIR);
							break;
						}
					}
					if(pipe_index < 0 && null_index >= 0) {
						inv.setItem(null_index, new ItemStack(Material.NETHER_BRICK_FENCE, 1));
						block.getRelative(0, 1, 0).setType(Material.AIR);
					}
				}
				return;
			}
			//place fences
			block_to_pump = findBlockToPump_advanced(block);
			if(block.isEmpty() || (block.isLiquid() && block_to_pump == null)) {
				for(int i = 0; i < 9; i++) {
					ItemStack is;
					is = inv.getItem(i);
					if(is != null && is.getType() == Material.NETHER_BRICK_FENCE)
					{
						pipe_index = i;
						break;
					}
				}
				if(pipe_index >= 0) {
					ItemStack pipe = inv.getItem(pipe_index);
					pipe.setAmount(pipe.getAmount() - 1);
					block.setType(Material.NETHER_BRICK_FENCE);
					block = block.getRelative(0, -1, 0);
				}
				return;
			}
		}
		if(bucket_index < 0) return;
		//pump
		if(block_to_pump == null) block_to_pump = findBlockToPump_advanced(block);
		if(block_to_pump != null) {
			if(bucket_index < 9)
				if(Utils.isStationaryLiquid(block_to_pump))
					if(block_to_pump.getType() == Material.LAVA)
						inv.setItem(bucket_index, new ItemStack(Material.LAVA_BUCKET));
					else if(block_to_pump.getType() == Material.WATER)
						inv.setItem(bucket_index, new ItemStack(Material.WATER_BUCKET));
			block_to_pump.setType(Material.AIR);
		}
	}

	public Block findBlockToPump_advanced(Block block) {
		//  _.._
		// |    |
		// |____|
		//  ____
		// |    |
		// |    =
		// |____|
		//  ____
		// |    |
		// |_.._|
		int dx = 0, dy = 0, dz = 0;
		Block last_liquid = null, last_stationaryliquid = null, next_block = block;
		/*while( next_block.getType() == Material.WATER || next_block.getType() == Material.STATIONARY_WATER || next_block.getType() == Material.LAVA || next_block.getType() == Material.STATIONARY_LAVA) {
			last_liquid = next_block;
			if(last_liquid.getType() == Material.STATIONARY_WATER || last_liquid.getType() == Material.STATIONARY_LAVA)
				last_stationaryliquid = last_liquid;
			dy += 1;
			
			next_block = block.getRelative(0, dy, 0);
		}
		byte water_height = 0;*/
		int max_distance2 = 0;
		Block max_dist_block = null;
		LayerSet water_layer_last = new LayerSet(null, 0);
		if(block.isLiquid()) {
			List<Integer> z_plus = new ArrayList<>(), z_minus = new ArrayList<>();
			testWater(block, max_dxz+1, max_dxz+1, water_layer_last, 0, z_plus, z_minus);
			
			for(dy = 1;dy <= max_dy; dy++) {
				//water_layer_last.print(block.getY()+dy-1);
				//water_layer_last.print_scale(block.getY()+dy-1);
				z_plus.clear();
				z_minus.clear();
				LayerSet water_layer_next = new LayerSet(null, 0);//water_layer_last.farthest, water_layer_last.farthest_dist2);
				boolean empty_layer = true;
				//int dy2 = 1000*dy*dy; //Magic value, max priority of higher water layers
				for(dx = 0;dx < in_test_max_dxz; dx++)
					for(dz = 0;dz < in_test_max_dxz; dz++) {
						if(water_layer_last.layer[dx][dz]>0) {
							Block upper_block = block.getRelative(dx-center_xz, dy, dz-center_xz);
							//System.out.println(upper_block);
							if(upper_block.isLiquid()) {
								testWater(upper_block, dx, dz, water_layer_next, water_layer_last.layer[dx][dz], z_plus, z_minus);
								if(empty_layer) empty_layer = false;
							}
						}
					}
				if(water_layer_next.farthest == null) {
					if(water_layer_last.farthest != null) {
						max_dist_block = water_layer_last.farthest;
					}
				} else {
					max_dist_block = water_layer_next.farthest;
				}
				water_layer_last = water_layer_next;
				if(empty_layer)
					break;
			}
		}
		/*if(water_layer_last.farthest != null) {
			water_layer_last.farthest.setType(Material.AIR);
		}*/
		//System.out.println("END: "+max_dist_block);
		return max_dist_block;
	}

	public Block findBlockToPump_regular(Block block) {
		int max_distance = 0;
		Block max_dist_block = null;
		if(!block.isLiquid()) return null;
		if(Utils.isStationaryLiquid(block.getRelative(2, 0, 0)) && block.getRelative(1, 0, 0).isLiquid()) {
			max_dist_block = block.getRelative(2, 0, 0);
			max_distance = 3;
		} else if(max_distance < 1 && Utils.isStationaryLiquid(block.getRelative(1, 0, 0))) {
			max_dist_block = block.getRelative(1, 0, 0);
			max_distance = 1;
		}
		if(Utils.isStationaryLiquid(block.getRelative(0, 0, 2)) && block.getRelative(0, 0, 1).isLiquid()) {
			max_dist_block = block.getRelative(0, 0, 2);
			max_distance = 3;
		} else if(max_distance < 1 && Utils.isStationaryLiquid(block.getRelative(0, 0, 1))) {
			max_dist_block = block.getRelative(0, 0, 1);
			max_distance = 1;
		}
		if(Utils.isStationaryLiquid(block.getRelative(-2, 0, 0)) && block.getRelative(-1, 0, 0).isLiquid()) {
			max_dist_block = block.getRelative(-2, 0, 0);
			max_distance = 3;
		} else if(max_distance < 1 && Utils.isStationaryLiquid(block.getRelative(-1, 0, 0))) {
			max_dist_block = block.getRelative(-1, 0, 0);
			max_distance = 1;
		}
		if(Utils.isStationaryLiquid(block.getRelative(0, 0, -2)) && block.getRelative(0, 0, -1).isLiquid()) {
			max_dist_block = block.getRelative(0, 0, -2);
			max_distance = 3;
		} else if(max_distance < 1 && Utils.isStationaryLiquid(block.getRelative(0, 0, -1))) {
			max_dist_block = block.getRelative(0, 0, -1);
			max_distance = 1;
		}
		if(max_distance < 2 && Utils.isStationaryLiquid(block.getRelative(1, 0, 1)) && (block.getRelative(1, 0, 0).isLiquid() || block.getRelative(0, 0, 1).isLiquid()))
			max_dist_block = block.getRelative(1, 0, 1);
		else if(max_distance < 2 && Utils.isStationaryLiquid(block.getRelative(-1, 0, 1)) && (block.getRelative(-1, 0, 0).isLiquid() || block.getRelative(0, 0, 1).isLiquid())) 
			max_dist_block = block.getRelative(-1, 0, 1);
		else if(max_distance < 2 && Utils.isStationaryLiquid(block.getRelative(-1, 0, -1)) && (block.getRelative(-1, 0, 0).isLiquid() || block.getRelative(0, 0, -1).isLiquid())) 
			max_dist_block = block.getRelative(-1, 0, -1);
		else if(max_distance < 2 && Utils.isStationaryLiquid(block.getRelative(1, 0, -1)) && (block.getRelative(1, 0, 0).isLiquid() || block.getRelative(0, 0, -1).isLiquid())) 
			max_dist_block = block.getRelative(1, 0, -1);
		if(max_distance == 0 && Utils.isStationaryLiquid(block)) {
			max_dist_block = block;
		}
		return max_dist_block;
	}
	
	public void testWater(Block b, int dx, int dz, LayerSet layer, int depth, List<Integer> zp, List<Integer> zm) {
		if(b.isLiquid()) {
			if(layer.layer[dx][dz] > 0) {
				if(layer.layer[dx][dz] > depth)
					layer.layer[dx][dz] = depth;
				return;
			}
			layer.layer[dx][dz] = depth;
			int temp_x = dx - center_xz, temp_z = dx - center_xz;
			int new_dist2 = /*temp_x*temp_x + temp_z*temp_z +*/ depth*depth;
			if(Utils.isStationaryLiquid(b) && new_dist2 > layer.farthest_dist2) {
				layer.farthest_dist2 = new_dist2;
				layer.farthest = b;
				//System.out.println("START: "+b);
			}
			Block temp_block = null;
			depth += 1;
			if(dz < in_test_max_dxz) {
				temp_block = b.getRelative(0, 0, 1);
				//testWater(temp_block, dx, dy2, dz+1, layer, depth, zp, zm);
				zp.add(dx);
				zp.add(dz+1);
				zp.add(depth);
			}
			if(dz > 0) {
				zm.add(dx);
				zm.add(dz-1);
				zm.add(depth);
			}
			if(dx < in_test_max_dxz) {
				temp_block = b.getRelative(1, 0, 0);
				//if(layer.layer[dx+1][dz]==0) {
				testWater(temp_block, dx+1, dz, layer, depth, zp, zm);
			}
			if(dx > 0) {
				temp_block = b.getRelative(-1, 0, 0);
				testWater(temp_block, dx-1, dz, layer, depth, zp, zm);
			}
			else
				temp_block = null;
			if(temp_block == null || !temp_block.isLiquid()) {
				while(!zp.isEmpty()) {
					Integer xf = zp.remove(0), zf = zp.remove(0), depthf = zp.remove(0);
					testWater_z(b.getRelative(xf-dx, 0, zf-dz), xf, zf, layer, depthf, zp, zm, true);
				}
				while(!zm.isEmpty()) {
					Integer xf = zm.remove(0), zf = zm.remove(0), depthf = zm.remove(0);
					testWater_z(b.getRelative(xf-dx, 0, zf-dz), xf, zf, layer, depthf, zp, zm, false);
				}
			}
		}
	}
	
	public void testWater_z(Block b, int dx, int dz, LayerSet layer, int depth, List<Integer> zp, List<Integer> zm, boolean zplus) {
		if(b.isLiquid()) {
			if(layer.layer[dx][dz] > 0) {
				if(layer.layer[dx][dz] > depth)
					layer.layer[dx][dz] = depth;
				return;
			}
			layer.layer[dx][dz] = depth;
			int temp_x = dx - center_xz, temp_z = dx - center_xz;
			int new_dist2 = /*temp_x*temp_x + temp_z*temp_z +*/ depth*depth;
			if(Utils.isStationaryLiquid(b) && new_dist2 > layer.farthest_dist2) {
				layer.farthest_dist2 = new_dist2;
				layer.farthest = b;
			}
			Block temp_block = null;
			depth += 1;
			if(dz < in_test_max_dxz && zplus) {
				zp.add(dx);
				zp.add(dz+1);
				zp.add(depth);
			}
			if(dz > 0 && !zplus) {
				zm.add(dx);
				zm.add(dz-1);
				zm.add(depth);
			}
			if(dx < in_test_max_dxz) {
				temp_block = b.getRelative(1, 0, 0);
				testWater(temp_block, dx+1, dz, layer, depth, zp, zm);
				
			}
			if(dx > 0) {
				temp_block = b.getRelative(-1, 0, 0);
				testWater(temp_block, dx-1, dz, layer, depth, zp, zm);
			}
			else
				temp_block = null;
			if(temp_block == null || !temp_block.isLiquid()) {
				while(!zp.isEmpty()) {
					Integer xf = zp.remove(0), zf = zp.remove(0), depthf = zp.remove(0);
					testWater_z(b.getRelative(xf-dx, 0, zf-dz), xf, zf, layer, depthf, zp, zm, true);
				}
				while(!zm.isEmpty()) {
					Integer xf = zm.remove(0), zf = zm.remove(0), depthf = zm.remove(0);
					testWater_z(b.getRelative(xf-dx, 0, zf-dz), xf, zf, layer, depthf, zp, zm, false);
				}
			}
		}
	}
}
