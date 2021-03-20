 package com.festp.enderchest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.festp.Main;
import com.festp.utils.YamlFilenameFilter;

public class EnderChestGroup {
	private Main pl;
	List<EnderChest> groups = new ArrayList<>();
	List<EnderChest> admingroups = new ArrayList<>();
	
	public EnderChestGroup(Main mainListener) {
		pl = mainListener;
	}

	public void saveEnderChests(EnderFileStorage storage)
	{
		for (int i = 0; i < admingroups.size(); i++) {
			storage.saveEnderChest(admingroups.get(i));
		}
		for (int i = 0; i < groups.size(); i++) {
			storage.saveEnderChest(groups.get(i));
		}
	}

	public void loadEnderChests(EnderFileStorage storage, File ECdir)
	{
		String[] groupnames = ECdir.list(new YamlFilenameFilter());
		//load directory
		//add chests in loop
		for(int i = 0; i < groupnames.length; i++) {
			groupnames[i] = groupnames[i].substring(0, groupnames[i].length() - 4);
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
		for (int i = 0; i < admingroups.size(); i++)
			if (admingroups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return admingroups.get(i);
		return null;
	}

	public EnderChest getByInventory(Inventory inv) {
		for (int i = 0; i < groups.size(); i++)
		{
			EnderChest ec = groups.get(i);
			if (ec.getInventory() == inv)
				return ec;
		}
		for (int i = 0; i < admingroups.size(); i++)
		{
			EnderChest ec = admingroups.get(i);
			if (ec.getInventory() == inv)
				return ec;
		}
		return null;
	}

	public void remove(String groupname) {
		for (int i = 0; i < groups.size(); i++)
			if (groups.get(i).getGroupName().equalsIgnoreCase(groupname))
				groups.remove(i);
	}

	public boolean isNameUsed(String groupname) {
		for (int i = 0; i < admingroups.size(); i++)
			if (admingroups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return true;
		for (int i = 0; i < groups.size(); i++)
			if (groups.get(i).getGroupName().equalsIgnoreCase(groupname))
				return true;
		return false;
	}
}
