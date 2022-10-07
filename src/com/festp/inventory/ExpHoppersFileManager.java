package com.festp.inventory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.festp.inventory.ExpHoppers.XpHopper;

public class ExpHoppersFileManager {
	
	 // TODO: make it configurable \/
	private static final String filename = "xp_hoppers.data";
	private final String filepath;
	private static final char F_SEP = '|', F_END = '\n';

	public ExpHoppersFileManager(String dirPath)
	{
		filepath = dirPath + filename;
	}
	
	// save data to FILE
	public void save(List<XpHopper> hoppers)
	{
		File file = new File(filepath);
		try {
	        FileWriter fw = new FileWriter(file);
	        file.delete();
	        file.createNewFile();
	        
			// sort by world
			hoppers.sort(new Comparator<XpHopper>() {
			    @Override
			    public int compare(XpHopper lhs, XpHopper rhs) {
			        UUID e1 = lhs.world.getUID(), e2 = rhs.world.getUID();
			        return e1.compareTo(e2);
			    }});
			
			World w = null;
			for (XpHopper pair : hoppers)
			{
				if (w != pair.world)
				{
					w = pair.world;
			        fw.write(w.getName() +F_END); // rename = broken   
				}
		        fw.write(""+ pair.x +F_SEP+ pair.y +F_SEP+ pair.z +F_SEP+ pair.xp +F_END);
			}
	        fw.close();
	    } catch(IOException e) {
	        throw new RuntimeException(e);
	    }
	}

	// load data from FILE
	public void load(List<XpHopper> hoppers)
	{
		File file = new File(filepath);
	    if (!file.exists())
	    	return;
	    
	    try {
	        FileReader fr = new FileReader(file);
	        int i = 0;
	        World world = null;
	        int x = 0, y = 0, z = 0, xp = 0;
	        String str = "";
	        int c = 0;
	        while (c != -1) {
	        	try {
		        	c = fr.read();
		        	if (c == F_END) {
		        		if (i == 0)
		        			world = Bukkit.getWorld(str);
		        		if (i == 3) {
		        			xp = Integer.parseInt(str);
		        			hoppers.add(
		        				new XpHopper(
		        						world,
		        						x, y, z,
		        						xp
		        				));
			        		i = 0;
		        		}
		        		str = "";
		        	}
		        	else if (c == F_SEP) {
		        		if (world == null) {
			        		while (c != -1 &&  (char)c != F_END)
			        			c = fr.read();
		        			continue;
		        		}
		        		if (i == 0) x = Integer.parseInt(str);
		        		if (i == 1) y = Integer.parseInt(str);
		        		if (i == 2) z = Integer.parseInt(str);
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
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
}
