package com.festp.amethyst;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityCreature;
import net.minecraft.server.v1_13_R2.IEntitySelector;
import net.minecraft.server.v1_13_R2.Navigation;
import net.minecraft.server.v1_13_R2.NavigationAbstract;
import net.minecraft.server.v1_13_R2.PathEntity;
import net.minecraft.server.v1_13_R2.PathfinderGoal;
import net.minecraft.server.v1_13_R2.RandomPositionGenerator;
import net.minecraft.server.v1_13_R2.Vec3D;

public class PathfinderGoalAvoidAmEntities extends PathfinderGoal {
	
    protected EntityCreature frightened;
    protected Entity frightening;
    private double d;
    private double e;
    private float f;
    private PathEntityAm g;
    private NavigationAbstract h;
    private double dist2 = 49.0D;

    public PathfinderGoalAvoidAmEntities(EntityCreature entitycreature, float distance, double d0, double speed, double dist2) {
        this(entitycreature, distance, d0, speed);
        this.dist2 = dist2*dist2;
    }

    public PathfinderGoalAvoidAmEntities(EntityCreature entitycreature, float distance, double d0, double speed) {
        
        this.frightened = entitycreature;
        this.f = distance;
        this.d = d0;
        this.e = speed;
        this.h = entitycreature.getNavigation();
        this.a(1);
    }

    public boolean a() { //"should start"
        List list = new ArrayList(this.frightened.world.getWorld().getNearbyEntities
        		(new Location(this.frightened.world.getWorld(),this.frightened.locX,this.frightened.locY,this.frightened.locZ), (double) this.f, 3.0D, (double) this.f) );
        //.a(this.i, this.a.getBoundingBox().grow((double) this.f, 3.0D, (double) this.f), Predicates.and(new Predicate[] { IEntitySelector.d, this.c, this.j}));
        org.bukkit.entity.Entity emaxk = null;
    	int maxk = 0;
    	for(int i = list.size()-1; i>=0; i--)
    	{
    		if(list.get(i) instanceof LivingEntity) {
        		org.bukkit.entity.LivingEntity le = (org.bukkit.entity.LivingEntity) list.get(i);
        		org.bukkit.inventory.EntityEquipment ee = le.getEquipment();
    			int k = ( ee.getItemInMainHand() != null && ee.getItemInMainHand().getType().toString().contains("DIAMOND") ) ? 1 : 0 +
    				(ee.getItemInOffHand() != null && 	ee.getItemInOffHand().getType().toString().contains("DIAMOND") ? 1 : 0) +
    				(ee.getHelmet() != null && 			ee.getHelmet().getType().toString().contains("DIAMOND") ? 1 : 0) +
    				(ee.getBoots() != null && 			ee.getBoots().getType().toString().contains("DIAMOND") ? 1 : 0) +
    				(ee.getChestplate() != null && 		ee.getChestplate().getType().toString().contains("DIAMOND") ? 1 : 0) +
    				(ee.getLeggings() != null && 		ee.getLeggings().getType().toString().contains("DIAMOND") ? 1 : 0);
    			if(k == 0)
    				list.remove(i);
    			else
    			{
    				if(k > maxk)
    				{
    					maxk = k;
    					emaxk = (org.bukkit.entity.Entity) le;
    				}
    			}
    		}
    		else if(list.get(i) instanceof Item) {
    			if(((Item)list.get(i)).getItemStack().getType().toString().contains("DIAMOND") && maxk < 1)
    			{
    				maxk = 1;
    				emaxk = (org.bukkit.entity.Entity) list.get(i);
    			}
    		}
    	}
    	
        if (list.isEmpty() || maxk == 0) {
            return false;
        } else {
            this.frightening = ((CraftEntity)emaxk).getHandle();
            Vec3D vec3d = new Vec3D(2*this.frightened.locX - this.frightening.locX, 2*this.frightened.locY - this.frightening.locY, 2*this.frightened.locZ - this.frightening.locZ);
        	System.out.println("Entity Vec3D   "+vec3d.x+" "+ vec3d.y+" "+ vec3d.z);
        	
            /*if (vec3d == null) {
                return false;
            } else if (this.b.e(vec3d.x, vec3d.y, vec3d.z) < this.b.h(this.a)) {
                return false;
            } else {*/
                this.g = this.h.a(vec3d.x, vec3d.y, vec3d.z) == null ? null : new PathEntityAm( this.h.a(vec3d.x, vec3d.y, vec3d.z) );
                this.c();
                return this.g != null;//this.g == null ? false : this.g.b(vec3d);
            //}
        }
    }

    /*public boolean b() {
        return !this.h.j();//.m();
    }*/

    public void c() { //start if a() is true
        //this.h.a(this.g, this.d);
        this.h.a(2);
    	
        this.h.a(this.g, 4D);
    }

    public void d() {
        this.frightening = null;
    }

    public void e() {
        if (this.frightened.h(this.frightening) < this.dist2) {
            this.frightened.getNavigation().a(this.e);
        } else {
            this.frightened.getNavigation().a(this.d);
        }

    }
	/*public final EntitySelectorViewableAmArmor frightened = new EntitySelectorViewableAmArmor(this);
    private EntityCreature frightening;
    private double c;
    private double d;
    private Entity e;
    private float f;
    private PathEntityAm g;
    private Navigation h;
    private Class i;

    public PathfinderGoalAvoidAmEntities(EntityCreature entitycreature, Class oclass, float f, double d0, double d1) {
        this.b = entitycreature;
        this.i = oclass;
        this.f = f;
        this.c = d0;
        this.d = d1;
        this.h = (Navigation) entitycreature.getNavigation();
        this.a(1);
    }

    public boolean frightened() {
        List list = this.b.world.a(this.i, this.b.getBoundingBox().grow((double) this.f, 3.0D, (double) this.f), this.a);

        if (list.isEmpty()) {
            return false;
        }

        this.e = (Entity) list.get(0);
        

        Vec3D vec3d = RandomPositionGenerator.b(this.b, 16, 7, vec3d.a(this.e.locX, this.e.locY, this.e.locZ));

        if (vec3d == null) {
            return false;
        } else if (this.e.e(vec3d.x, vec3d.y, vec3d.z) < this.e.g((Entity) this.b)) { //.g(e) = .f(e)
            return false;
        } else {
            this.g = (PathEntityAm) this.h.a(vec3d.x, vec3d.y, vec3d.z);
            return this.g == null ? false : this.g.b(vec3d);
        }
    }

    public boolean frightening() {
        return !this.h.g();
    }

    public void c() {
        this.h.a(this.g, this.c);
    }

    public void d() {
        this.e = null;
    }

    public void e() {
        if (this.b.g(this.e) < 49.0D) { //.g(e) = .f(e)
            this.b.getNavigation().a(this.d);
        } else {
            this.b.getNavigation().a(this.c);
        }
    }

    static EntityCreature frightened(PathfinderGoalAvoidAmEntities pathfindergoalavoidamarmor) {
        return pathfindergoalavoidamarmor.b;
    }*/
}
