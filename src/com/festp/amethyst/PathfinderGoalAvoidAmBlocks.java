package com.festp.amethyst;

import org.bukkit.Material;
import org.bukkit.block.Block;

import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.NavigationAbstract;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.Vec3D;

public class PathfinderGoalAvoidAmBlocks extends PathfinderGoal {
	protected EntityCreature frightened;
    protected Block frightening = null;
    private double d;
    private double e;
    private PathEntityAm g;
    private NavigationAbstract h;
    private int R = 10;
    private double R2 = 150;
    private int H = 3;

    public PathfinderGoalAvoidAmBlocks(EntityCreature entitycreature, double d0, double speed, int radius, int height) {
        this(entitycreature, d0, speed);
        this.R = radius;
        this.R2 = radius*radius*1.5;
        this.H = height;
    }

    public PathfinderGoalAvoidAmBlocks(EntityCreature entitycreature, double d0, double d1) {
        this.frightened = entitycreature;
        this.d = d0;
        this.e = d1;
        this.h = entitycreature.getNavigation();
        this.a(1);
    }
    
    public boolean a() { //"should start"
    	Block tempb = null, tempb2 = null;
    	int x0 = frightened.getBukkitEntity().getLocation().getBlockX(), y0 = frightened.getBukkitEntity().getLocation().getBlockY(), z0 = frightened.getBukkitEntity().getLocation().getBlockZ();
    	for(int r = 0; r<this.R; r++) {
    		for(int y = -this.H; y<=this.H; y++) {
    			for(int x = -r; x<=r; x++) {
    	    		tempb2 = frightened.getBukkitEntity().getWorld().getBlockAt(x0+x,y0+y,z0+r-Math.abs(x));
    	    		if(tempb2.getType() == Material.DIAMOND_BLOCK && !tempb2.isBlockPowered())
    	    		{
    	    	    	tempb = tempb2;
    	    			break;
    	    		}
    	    		tempb2 = frightened.getBukkitEntity().getWorld().getBlockAt(x0+x,y0+y,z0-r+Math.abs(x));
    	    		if(tempb2.getType() == Material.DIAMOND_BLOCK && !tempb2.isBlockPowered())
    	    		{
    	    	    	tempb = tempb2;
    	    			break;
    	    		}
    	    	}
        	}
    	}
    	
    	if(tempb != null || frightening != null && ( frightening.getType() != Material.DIAMOND_BLOCK || this.R2 < distance(this.frightened.locX, this.frightened.locY, this.frightened.locZ, this.frightening.getX()+0.5, this.frightening.getY(), this.frightening.getZ()+0.5) ) )
    		this.frightening = tempb;
    	if(this.frightening != null) {
    		Vec3D vec3d = gopoint(this.frightened.locX, this.frightened.locY, this.frightened.locZ, this.frightening.getX()+0.5, this.frightening.getY(), this.frightening.getZ()+0.5);
         	System.out.println("Block Vec3D   "+vec3d.x+" "+ vec3d.y+" "+ vec3d.z);
         	this.g = this.h.a(vec3d.x, vec3d.y, vec3d.z) == null ? null : new PathEntityAm( this.h.a(vec3d.x, vec3d.y, vec3d.z) );
         	this.c();
         	return this.g != null;
    	}
    	return this.frightening != null;
    }

    public void c() { //start if a() is true
        this.h.a(2);

        this.h.a(this.g, 4D);
    }

    public void d() {
        this.frightening = null;
    }

    public void e() {
        if (this.frightening != null) {
            this.frightened.getNavigation().a(this.e);
        } else {
            this.frightened.getNavigation().a(this.d);
        }

    }

    public Vec3D gopoint(double x1, double y1, double z1, double x2, double y2, double z2) //3 - 1 - 2
    {
    	//return new Vec3D(2*this.frightened.locX - this.frightening.getX(), 2*this.frightened.locY - this.frightening.getY(), 2*this.frightened.locZ - this.frightening.getZ());
    	//double l2 = Math.abs(x1-x2) + Math.abs(y1-y2) + Math.abs(z1-z2);
    	double l2 = (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2);
    	double l = ezsqrt(l2,this.R,3);
    	double k = this.R/l;
    	double x3 = x2 + k*(x1-x2);
    	double y3 = y2 + k*(y1-y2);
    	double z3 = z2 + k*(z1-z2);
    	return new Vec3D(Math.floor(x3)+0.5, y3, Math.floor(z3)+0.5);
    	//return new Vec3D(2*x1 - x2, 2*y1 - y2, 2*z1 - z2);
    }
    
    public double ezsqrt(double sq, double S, int it) {
    	if(it>0) {
    		sq = ezsqrt(sq,S,it-1);
    		return (sq+S/sq)/2;
    	}
    	else return (sq+S/sq)/2;
    }
    
    public double distance(double x1, double y1, double z1, double x2, double y2, double z2) {
    	return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2) + (z1-z2)*(z1-z2);
    }
}
