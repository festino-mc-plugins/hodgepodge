package com.festp.amethyst;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public class ActiveAmSpawnBlocking {
	public int R=10, H=7;
	
	public boolean canspawn(Entity e) {
		if( !(e instanceof Monster) )
			return true;
		int x0 = e.getLocation().getBlockX(), y0 = e.getLocation().getBlockY(), z0 = e.getLocation().getBlockZ();
    	for(int r = 0; r<this.R; r++) {
    		for(int y = -this.H; y<=this.H; y++) {
    			for(int x = -r; x<=r; x++) {
    	    		Block tempb = e.getWorld().getBlockAt(x0+x,y0+y,z0+r-Math.abs(x));
    	    		if(tempb.getType() == Material.DIAMOND_BLOCK && tempb.isBlockPowered())
    	    		{
    	    			return false;
    	    		}
    	    		tempb = e.getWorld().getBlockAt(x0+x,y0+y,z0-r+Math.abs(x));
    	    		if(tempb.getType() == Material.DIAMOND_BLOCK && tempb.isBlockPowered())
    	    		{
    	    			return false;
    	    		}
    	    	}
        	}
    	}
		return true;
	}
}
