package com.festp.inventory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.festp.mainListener;

/* Hopper named "xp": when contains empty bottles, collects XP; once stored N xp turns bottle into xp bottle. Grab only bottles(even player can't put anything else).
 * If you pick up all the bottles, throws out the stored experience. */
public class ExpHoppers implements Listener {
	private static final int SAVE_RATE = 20 * 60 * 3; //3 minutes
	private int save_ticks = 0;
	
	private static final String hopper_name = "xp";
	 //make it configurable \/
	private static final String filename = "xp_hoppers.data";
	private static final String filepath = "plugins/"+mainListener.pluginname+"/"+filename;
	private static final char F_SEP = '|', F_END = '\n';
	private static final int XP_ANOUNT = 18; //3-11 xp from one bottle
	private static final double STORE_HEIGHT = 12.0 / 16;
	private static final Material EXP_BOTTLE = Material.EXPERIENCE_BOTTLE, EMPTY_BOTTLE = Material.GLASS_BOTTLE;

	private List<PairBlockXP> hoppers = new ArrayList<>();
	private Server server;
	
	//check if xp orb on "xp" hopper, check empty bottles inside, grab xp(beacon?); if N(configurable?), spend stored xp
	void tryStoreXP(ExperienceOrb orb)
	{
		Block block = orb.getLocation().add(0, -STORE_HEIGHT, 0).getBlock();
		if (block.getType() != Material.HOPPER)
			return;
		Hopper hopper = (Hopper) block.getState();
		if (hopper.getCustomName() == null || !hopper.getCustomName().toLowerCase().equals(hopper_name))
			return;
		Inventory hinv = hopper.getInventory();
		ItemStack[] items =  hinv.getContents();
		int new_slot = -1, bottle_slot = -1;
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == null)
			{
				if (new_slot < 0)
					new_slot = i;
			}
			else if (items[i].getType() == EMPTY_BOTTLE)
			{
				if (bottle_slot < 0) //first possible slot (canonical)
					bottle_slot = i;
			}
			else if (items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize())
			{
				if (new_slot < 0 || items[new_slot] == null)
					new_slot = i;
			}
		}
		if (new_slot < 0 || bottle_slot < 0)
			return;
		
		//test hopper xp amount
		int new_xp = orb.getExperience();
		Integer amount = getAmount(block);
		new_xp = new_xp + (amount == null ? 0 : amount);
		while (new_xp > XP_ANOUNT)
		{
			new_xp -= XP_ANOUNT;
			items[bottle_slot].setAmount(items[bottle_slot].getAmount() - 1);
			if (items[new_slot] == null)
				items[new_slot] = new ItemStack(EXP_BOTTLE, 1);
			else
				items[new_slot].setAmount(items[new_slot].getAmount() + 1);
			
			if (items[bottle_slot] == null || items[bottle_slot].getAmount() == 0)
			{
				int next_bottle = -1;
				for (int i = bottle_slot + 1; i < items.length; i++)
					if (items[i] != null && items[i].getType() == EMPTY_BOTTLE)
						if (bottle_slot < 0) {
							bottle_slot = i;
							break;
						}
				bottle_slot = next_bottle;
			}
			if (items[new_slot].getAmount() == EXP_BOTTLE.getMaxStackSize())
			{
				int next_slot = -1;
				for (int i = new_slot + 1; i < items.length; i++)
					if (items[i] != null && items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize()) {
						next_slot = i;
						break;
					}
				if (next_slot < 0)
					for (int i = 0; i < items.length; i++)
						if (items[i] == null) {
							next_slot = i;
							break;
						}
				new_slot = next_slot;
			}
					
			if (new_slot < 0 || bottle_slot < 0) {
				ExperienceOrb new_orb = (ExperienceOrb) block.getWorld().spawnEntity(orb.getLocation(), EntityType.EXPERIENCE_ORB);
				new_orb.setExperience(new_xp);
				new_xp = 0;
				break;
			}
		}
		
		hinv.setContents(items);
		
		if (amount == null) {
			if (new_xp > 0)
				hoppers.add(new PairBlockXP(block.getWorld(), block.getX(), block.getY(), block.getZ(), new_xp));
		}
		else
			setAmount(block, new_xp);
		
		orb.remove();
	}
	
	boolean canStore(Block block)
	{
		if (block.getType() != Material.HOPPER)
			return false;
		Hopper hopper = (Hopper) block.getState();
		if (!hopper.getCustomName().toLowerCase().equals(hopper_name))
			return false;
		Inventory hinv = hopper.getInventory();
		ItemStack[] items =  hinv.getContents();
		int new_slot = -1, bottle_slot = -1;
		for (int i = 0; i < items.length; i++)
			if (items[i] == null || items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize())
				new_slot = i;
			else if (items[i].getType() == EMPTY_BOTTLE)
				bottle_slot = i;
		
		if (new_slot < 0 || bottle_slot < 0)
			return false;

		return true;
	}
	
	
	/**@return null if there is no data*/
	Integer getAmount(Block b)
	{
		int x = b.getX(), y = b.getY(), z = b.getZ();
		World w = b.getWorld();
		for (PairBlockXP pair : hoppers)
			if (pair.x == x && pair.z == z && pair.y == y && w == pair.world)
				return pair.xp;
		return null;
	}
	
	void setAmount(Block b, int amount)
	{
		int x = b.getX(), y = b.getY(), z = b.getZ();
		World w = b.getWorld();
		for (PairBlockXP pair : hoppers)
			if (pair.x == x && pair.z == z && pair.y == y && w == pair.world)
			{
				pair.xp = amount;
				return;
			}
	}
	
	//load data
	public ExpHoppers(Server server)
	{
		this.server = server;
		load();
	}
	
	public void onTick() {
		for (World world : server.getWorlds())
			for (ExperienceOrb orb : world.getEntitiesByClass(ExperienceOrb.class))
				tryStoreXP(orb);
		
		for (int i = hoppers.size()-1; i >= 0; i--)
		{
			PairBlockXP pair = hoppers.get(i);
			if (!canStore (pair.world.getBlockAt(pair.x, pair.y, pair.z)))
			{
				ExperienceOrb new_orb = (ExperienceOrb) pair.world.spawnEntity(
						new Location(pair.world, pair.x, pair.y+1, pair.z),
						EntityType.EXPERIENCE_ORB);
				new_orb.setExperience(pair.xp);
				hoppers.remove(i);
			}
		}
		
		save_ticks++;
		if (save_ticks > SAVE_RATE) {
			save_ticks = 0;
			save();
		}
	}

	//save data to FILE
	public void save()
	{
		File file = new File(filepath);
		try {
	        FileWriter fw = new FileWriter(file);
	        file.delete();
	        file.createNewFile();
	        
			//sort by world
			hoppers.sort(new Comparator<PairBlockXP>() {
			    @Override
			    public int compare(PairBlockXP lhs, PairBlockXP rhs) {
			        UUID e1 = lhs.world.getUID(), e2 = rhs.world.getUID();
			        return e1.compareTo(e2);
			    }});
			
			World w = null;
			for (PairBlockXP pair : hoppers)
			{
				if (w != pair.world)
				{
					w = pair.world;
			        fw.write(w.getName() +F_END); //rename = broken   
				}
		        fw.write(""+ pair.x +F_SEP+ pair.y +F_SEP+ pair.z +F_SEP+ pair.xp +F_END);
			}
	        fw.close();
	    } catch(IOException e) {
	        throw new RuntimeException(e);
	    }
	}

	//load data from FILE
	private void load()
	{
		File file = new File(filepath);
	    if(!file.exists())
	    	return;
	    
	    try {
	        FileReader fr = new FileReader(file);
	        int i = 0;
	        World world = null;
	        int x = 0, y = 0, z = 0, xp = 0;
	        String str = "";
	        int c = 0;
	        while(c != -1) {
	        	try {
		        	c = fr.read();
		        	if(c == F_END) {
		        		if(i == 0)
		        			world = server.getWorld(str);
		        		if(i == 3) {
		        			xp = Integer.parseInt(str);
		        			hoppers.add(
		        				new PairBlockXP(
		        						world,
		        						x, y, z,
		        						xp
		        				));
			        		i = 0;
		        		}
		        		str = "";
		        	}
		        	else if(c == F_SEP) {
		        		if (world == null) {
			        		while (c != -1 &&  (char)c != F_END)
			        			c = fr.read();
		        			continue;
		        		}
		        		if(i == 0) x = Integer.parseInt(str);
		        		if(i == 1) y = Integer.parseInt(str);
		        		if(i == 2) z = Integer.parseInt(str);
		        		i++;
		        		str = "";
		        	}
		        	else str += (char)c;
	        	} catch (Exception e) {
	        		while (c != -1 && (char)c != F_END)
	        			c = fr.read();
	        	}
	        }
	        fr.close();
	    } catch(IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private class PairBlockXP
	{
		World world;
		int x, y, z;
		int xp;
		public PairBlockXP(World w, int x, int y, int z, int xp) {
			this.world = w;
			this.x = x;
			this.y = y;
			this.z = z;
			this.xp = xp;
		}
	}
}
