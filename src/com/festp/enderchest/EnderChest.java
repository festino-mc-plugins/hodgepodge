package com.festp.enderchest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.md_5.bungee.api.ChatColor;

public class EnderChest {
	
	private String groupname;
	private String owner;
	List<String> group = new ArrayList<>();
	List<String> invited = new ArrayList<>();
	private Inventory inv;
	boolean isadmingroup = false;

	public EnderChest(String groupname)
	{
		this.groupname = groupname;
		this.isadmingroup = true;
	}
	
	public EnderChest(String groupname, String owner, boolean newgroup)
	{
		this.groupname = groupname;
		this.owner = owner;
		if(newgroup) {
			this.group.add(owner);
			this.invited.add(owner);
		}
	}
	
	public String[] getGroup()
	{
		return (String[]) group.toArray();
	}
	
	public String getGroupName()
	{
		return groupname;
	}
	
	public String getOwner()
	{
		return owner;
	}
	
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	public void setInventory(Inventory inv)
	{
		this.inv = inv;
	}

	public Inventory getInventory() {
		return inv;
	}
	
	/*public void saveEnderChest()
	{
		//inv
	}
	
	public void loadEnderChest()
	{
		//inv
	}*/
}
