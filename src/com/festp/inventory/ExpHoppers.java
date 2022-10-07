package com.festp.inventory;

import java.util.ArrayList;
import java.util.List;
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

/* Hopper named "xp": when contains empty bottles, collects XP; once stored N xp turns bottle into xp bottle.
 * Grab only bottles(even player can't put anything else).
 * If you pick up all the bottles, throws out the stored experience. */
public class ExpHoppers implements Listener {

	public static class XpHopper
	{
		public World world;
		public int x, y, z;
		public int xp;
		
		public XpHopper(World w, int x, int y, int z, int xp) {
			this.world = w;
			this.x = x;
			this.y = y;
			this.z = z;
			this.xp = xp;
		}
	}
	
	private static final int SAVE_RATE = 20 * 60 * 3;
	private int saveTicks = 0;
	
	private static final String HOPPER_NAME = "xp";
	private static final int XP_ANOUNT = 50; // 3-11 xp from one bottle
	private static final double STORE_HEIGHT = 12.0 / 16;
	private static final Material EMPTY_BOTTLE = Material.GLASS_BOTTLE;
	private static final Material EXP_BOTTLE = Material.EXPERIENCE_BOTTLE;

	private List<XpHopper> hoppers = new ArrayList<>();
	private Server server;
	private ExpHoppersFileManager fileManager;
	
	// load data
	public ExpHoppers(Server server, String dirPath)
	{
		this.server = server;
		fileManager = new ExpHoppersFileManager(dirPath);
		fileManager.load(hoppers);
	}
	
	public void onTick() {
		for (World world : server.getWorlds())
			for (ExperienceOrb orb : world.getEntitiesByClass(ExperienceOrb.class))
				tryStoreXP(orb);
		
		for (int i = hoppers.size()-1; i >= 0; i--)
		{
			XpHopper pair = hoppers.get(i);
			if (!canStore (pair.world.getBlockAt(pair.x, pair.y, pair.z)))
			{
				ExperienceOrb newOrb = (ExperienceOrb) pair.world.spawnEntity(
						new Location(pair.world, pair.x, pair.y+1, pair.z),
						EntityType.EXPERIENCE_ORB);
				newOrb.setExperience(pair.xp);
				hoppers.remove(i);
			}
		}
		
		saveTicks++;
		if (saveTicks > SAVE_RATE) {
			saveTicks = 0;
			saveAll();
		}
	}
	
	public void saveAll()
	{
		fileManager.save(hoppers);
	}
	
	/** Check if xp orb on "xp" hopper, check empty bottles inside, grab xp(beacon?); if N(configurable?), spend stored xp */
	void tryStoreXP(ExperienceOrb orb)
	{
		Block block = orb.getLocation().add(0, -STORE_HEIGHT, 0).getBlock();
		if (block.getType() != Material.HOPPER)
			return;
		Hopper hopper = (Hopper) block.getState();
		if (hopper.getCustomName() == null || !hopper.getCustomName().toLowerCase().equals(HOPPER_NAME))
			return;
		Inventory hopperInv = hopper.getInventory();
		ItemStack[] items =  hopperInv.getContents();
		int newSlot = -1, bottleSlot = -1;
		for (int i = 0; i < items.length; i++)
		{
			if (items[i] == null)
			{
				if (newSlot < 0)
					newSlot = i;
			}
			else if (items[i].getType() == EMPTY_BOTTLE)
			{
				if (bottleSlot < 0) // first possible slot (canonical)
					bottleSlot = i;
			}
			else if (items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize())
			{
				if (newSlot < 0 || items[newSlot] == null)
					newSlot = i;
			}
		}
		if (newSlot < 0 || bottleSlot < 0)
			return;
		
		// test hopper xp amount
		int newXp = orb.getExperience();
		Integer amount = getAmount(block);
		newXp = newXp + (amount == null ? 0 : amount);
		while (newXp > XP_ANOUNT)
		{
			newXp -= XP_ANOUNT;
			items[bottleSlot].setAmount(items[bottleSlot].getAmount() - 1);
			if (items[newSlot] == null)
				items[newSlot] = new ItemStack(EXP_BOTTLE, 1);
			else
				items[newSlot].setAmount(items[newSlot].getAmount() + 1);
			
			if (items[bottleSlot] == null || items[bottleSlot].getAmount() == 0)
			{
				int next_bottle = -1;
				for (int i = bottleSlot + 1; i < items.length; i++)
					if (items[i] != null && items[i].getType() == EMPTY_BOTTLE)
						if (bottleSlot < 0) {
							bottleSlot = i;
							break;
						}
				bottleSlot = next_bottle;
			}
			if (items[newSlot].getAmount() == EXP_BOTTLE.getMaxStackSize())
			{
				int nextSlot = -1;
				for (int i = newSlot + 1; i < items.length; i++)
					if (items[i] != null && items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize()) {
						nextSlot = i;
						break;
					}
				if (nextSlot < 0)
					for (int i = 0; i < items.length; i++)
						if (items[i] == null) {
							nextSlot = i;
							break;
						}
				newSlot = nextSlot;
			}
					
			if (newSlot < 0 || bottleSlot < 0) {
				ExperienceOrb newOrb = (ExperienceOrb) block.getWorld().spawnEntity(orb.getLocation(), EntityType.EXPERIENCE_ORB);
				newOrb.setExperience(newXp);
				newXp = 0;
				break;
			}
		}
		
		hopperInv.setContents(items);
		
		if (amount == null) {
			if (newXp > 0)
				hoppers.add(new XpHopper(block.getWorld(), block.getX(), block.getY(), block.getZ(), newXp));
		}
		else
			setAmount(block, newXp);
		
		orb.remove();
	}
	
	boolean canStore(Block block)
	{
		if (block.getType() != Material.HOPPER)
			return false;
		Hopper hopper = (Hopper) block.getState();
		if (!hopper.getCustomName().toLowerCase().equals(HOPPER_NAME))
			return false;
		Inventory hinv = hopper.getInventory();
		ItemStack[] items =  hinv.getContents();
		int newSlot = -1, bottleSlot = -1;
		for (int i = 0; i < items.length; i++)
			if (items[i] == null || items[i].getType() == EXP_BOTTLE && items[i].getAmount() < EXP_BOTTLE.getMaxStackSize())
				newSlot = i;
			else if (items[i].getType() == EMPTY_BOTTLE)
				bottleSlot = i;
		
		if (newSlot < 0 || bottleSlot < 0)
			return false;

		return true;
	}
	
	/** @return null if there is no data */
	Integer getAmount(Block b)
	{
		int x = b.getX(), y = b.getY(), z = b.getZ();
		World w = b.getWorld();
		for (XpHopper pair : hoppers)
			if (pair.x == x && pair.z == z && pair.y == y && w == pair.world)
				return pair.xp;
		return null;
	}
	
	void setAmount(Block b, int amount)
	{
		int x = b.getX(), y = b.getY(), z = b.getZ();
		World w = b.getWorld();
		for (XpHopper pair : hoppers)
			if (pair.x == x && pair.z == z && pair.y == y && w == pair.world)
			{
				pair.xp = amount;
				return;
			}
	}
}
