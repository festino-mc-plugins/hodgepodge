package com.festp.enderchest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.festp.mainListener;

import net.md_5.bungee.api.ChatColor;

public class EnderChestGroup {
	private mainListener pl;
	List<EnderChest> groups = new ArrayList<>();
	List<EnderChest> admingroups = new ArrayList<>();
	
	public EnderChestGroup(mainListener mainListener) {
		pl = mainListener;
	}

	public void saveEnderChests(EnderFileStorage storage)
	{
		/*for(int i=0; i<groups.size(); i++)
			groups.get(i).saveEnderChest();*/
		for(int i=0; i<admingroups.size(); i++) {
			storage.saveEnderChest(admingroups.get(i));
		}
		for(int i=0; i<groups.size(); i++) {
			storage.saveEnderChest(groups.get(i));
		}
	}

	public void loadEnderChests(EnderFileStorage storage, String[] groupnames)
	{
		//load directory
		//add chests in loop
		for(int i=0; i<groupnames.length; i++) {
			if(groupnames[i].length() < 5) continue;
			groupnames[i] = groupnames[i].substring(0, groupnames[i].length()-4);
			/*if(ecstorage.hasDataFile(groupnames[i])) ecstorage.deleteDataFile(groupnames[i]);
			else */storage.loadEnderChest(groupnames[i]);
		}
	}

	public EnderChest getByNick(String nickname) {
		for(int i=0; i<groups.size(); i++)
		{
			EnderChest ec = groups.get(i);
			for(int j=0; j<ec.group.size(); j++)
				if(ec.group.get(j).equals(nickname))
					return ec;
		}
		return null;
	}

	public EnderChest getAdminByPlayer(Player p) {
		EnderChest ec = null;
		for(AdminChannelPlayer ecp : pl.admin_ecplayers)
			if(ecp.p == p) {
				ec = ecp.adminec;
				break;
			}
		return ec;
	}

	public EnderChest getByGroupname(String groupname) {
		for(int i=0; i<groups.size(); i++)
			if(groups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return groups.get(i);
		return null;
	}

	public EnderChest getByAdminGroupname(String groupname) {
		for(int i=0; i<admingroups.size(); i++)
			if(admingroups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return admingroups.get(i);
		return null;
	}

	public void remove(String groupname) {
		for(int i=0; i<groups.size(); i++)
			if(groups.get(i).getGroupName().equalsIgnoreCase(groupname))
				groups.remove(i);
	}
	
	public static void saveEnderChest(String name)
	{
		
	}
	
	public static void loadEnderChest(String name)
	{
		
	}

	public boolean isNameUsed(String groupname) {
		for(int i=0; i<admingroups.size(); i++)
			if(admingroups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return true;
		for(int i=0; i<groups.size(); i++)
			if(groups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return true;
		return false;
	}
}
