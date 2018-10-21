package somebodyelse.code;

import java.util.List;

import com.festp.amethyst.PathEntityAm;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityCreature;
import net.minecraft.server.v1_13_R1.IEntitySelector;
import net.minecraft.server.v1_13_R1.Navigation;
import net.minecraft.server.v1_13_R1.NavigationAbstract;
import net.minecraft.server.v1_13_R1.PathEntity;
import net.minecraft.server.v1_13_R1.PathfinderGoal;
import net.minecraft.server.v1_13_R1.RandomPositionGenerator;
import net.minecraft.server.v1_13_R1.Vec3D;

public class PathfinderGoalAvoidEntity<T extends Entity> extends PathfinderGoal {
	
	private final Predicate<Entity> c;
    protected EntityCreature a;
    private double d;
    private double e;
    protected T b;
    private float f;
    private PathEntityAm g;
    private NavigationAbstract h;
    private Class<T> i;
    private Predicate<? super T> j;
    private double dist2 = 49.0D;

    public PathfinderGoalAvoidEntity(EntityCreature entitycreature, Class<T> oclass, float distance, double d0, double speed) {
        this(entitycreature, oclass, Predicates.alwaysTrue(), distance, d0, speed);
    }
    
    public PathfinderGoalAvoidEntity(EntityCreature entitycreature, Class<T> oclass, float distance, double d0, double speed, double dist2) {
        this(entitycreature, oclass, Predicates.alwaysTrue(), distance, d0, speed);
        this.dist2 = dist2*dist2;
    }

    public PathfinderGoalAvoidEntity(EntityCreature entitycreature, Class<T> oclass, Predicate<? super T> predicate, float f, double d0, double d1) {
        this.c = new Predicate() {
            public boolean a(Entity entity) {
                return entity.isAlive() && PathfinderGoalAvoidEntity.this.a.getEntitySenses().a(entity);
            }

            public boolean apply(Object object) {
                return this.a((Entity) object);
            }
        };
        this.a = entitycreature;
        this.i = oclass;
        this.j = predicate;
        this.f = f;
        this.d = d0;
        this.e = d1;
        this.h = entitycreature.getNavigation();
        this.a(1);
    }

    public boolean a() {
        List list = this.a.world.a(this.i, this.a.getBoundingBox().grow((double) this.f, 3.0D, (double) this.f), Predicates.and(new Predicate[] { IEntitySelector.d, this.c, this.j}));
        
        if (list.isEmpty()) {
            return false;
        } else {
            this.b = (T)(Entity) list.get(0);
        	System.out.println(b.locX+" "+b.locY+" "+b.locZ);
            Vec3D vec3d = RandomPositionGenerator.b(this.a, 16, 7, new Vec3D(this.b.locX, this.b.locY, this.b.locZ));

            if (vec3d == null) {
                return false;
            } else if (this.b.e(vec3d.x, vec3d.y, vec3d.z) < this.b.h(this.a)) {
                return false;
            } else {
                this.g = this.h.a(vec3d.x, vec3d.y, vec3d.z) == null ? null : new PathEntityAm( this.h.a(vec3d.x, vec3d.y, vec3d.z) );
                return this.g == null ? false : this.g.b(vec3d);
            }
        }
    }

    public boolean b() {
        return !this.h.j();//.m();
    }

    public void c() {
        this.h.a(this.g, this.d);
    }

    public void d() {
        this.b = null;
    }

    public void e() {
        if (this.a.h(this.b) < this.dist2) {
            this.a.getNavigation().a(this.e);
        } else {
            this.a.getNavigation().a(this.d);
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
