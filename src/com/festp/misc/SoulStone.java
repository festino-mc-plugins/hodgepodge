package com.festp.misc;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTTagCompound;

public class SoulStone implements Listener {
	static Random id_random = new Random();
	
	public static void addSoulStoneCrafts(JavaPlugin plugin) {
		Server server = plugin.getServer();
		
    	//soul stones - colored firework star
    	NamespacedKey key_soul_zombie = new NamespacedKey(plugin, "soul_zombie");
    	ShapedRecipe soul_zombie = new ShapedRecipe(key_soul_zombie,  SoulStone.getFromType(EntityType.ZOMBIE));
    	soul_zombie.shape(new String[]{"RRR", "RDR", "RRR"});
    	soul_zombie.setIngredient('R', Material.ROTTEN_FLESH);
    	soul_zombie.setIngredient('D', Material.DRAGON_EGG);
    	server.addRecipe(soul_zombie);
	}
	
	public static ItemStack getFromType(EntityType type) {

    	ItemStack stone = new ItemStack(Material.FIREWORK_STAR);
    	stone = setSoulStoneMeta(stone); //random id to prevent stacking (to do: change it?)
    	ItemMeta stone_meta = stone.getItemMeta();
    	FireworkEffectMeta fm = (FireworkEffectMeta) stone_meta;
    	switch(type) {
    	case ZOMBIE:
        	fm.setDisplayName("Zombie soul stone");
        	fm.setLore(Lists.newArrayList("0 zombie souls"));
    		fm.setEffect(FireworkEffect.builder().withColor(Color.AQUA, Color.GREEN, Color.GRAY).build());
    		break;
        default:
        	fm.setDisplayName("Corrupted soul stone");
        	fm.setLore(Lists.newArrayList("so how you could get it?!"));
        	fm.setEffect(FireworkEffect.builder().withColor(Color.PURPLE).build());
        	break;
    	}
    	stone.setItemMeta(fm);
    	return stone;
	}
	
	public static ItemStack getEgg(ItemStack stone) {
		if(!stone.hasItemMeta() || !stone.getItemMeta().hasLore())
			return null;
		String lore = stone.getItemMeta().getLore().get(0);
		Material type = Material.AIR;
		if(lore.contains("zombie")) type = Material.ZOMBIE_SPAWN_EGG;
		return new ItemStack(type, 1);
	}
	
	public static EntityType getEntityType(ItemStack stone) {
		if(!stone.hasItemMeta() || !stone.getItemMeta().hasLore())
			return EntityType.UNKNOWN;
		String lore = stone.getItemMeta().getLore().get(0);
		if(lore.contains("zombie")) return EntityType.ZOMBIE;
		else return EntityType.UNKNOWN;
	}
	
	public void onEntityDeath(EntityDeathEvent event) {
		//event.getEntity().getLastDamageCause().getCause().
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Damageable && ((Damageable)event.getEntity()).getHealth()-event.getFinalDamage() <= 0 && event.getDamager() instanceof Player) {
			Player p = (Player) event.getDamager();
			
			if(isSoulStone(p.getInventory().getItemInMainHand()) && getEntityType(p.getInventory().getItemInMainHand()) == event.getEntityType()) {
				p.getInventory().setItemInMainHand(increaseSouls(p.getInventory().getItemInMainHand()));
				if(getSouls(p.getInventory().getItemInMainHand()) > 3)
					p.getInventory().setItemInMainHand(getEgg(p.getInventory().getItemInMainHand()));
			}
			else if(isSoulStone(p.getInventory().getItemInOffHand()) && getEntityType(p.getInventory().getItemInOffHand()) == event.getEntityType()) {
				p.getInventory().setItemInOffHand(increaseSouls(p.getInventory().getItemInOffHand()));
				if(getSouls(p.getInventory().getItemInMainHand()) > 3)
					p.getInventory().setItemInOffHand(getEgg(p.getInventory().getItemInOffHand()));
			}
		}
	}
	
	public static boolean isSoulStone(ItemStack item) {
		/*if(item.hasItemMeta() && item.getItemMeta().hasLore() && !item.getItemMeta().getLore().isEmpty()
				&& item.getItemMeta().getLore().get(0) && item.getItemMeta().getLore().get(0).contains(""))*/

		if(item == null)
			return false;
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null)
        	return false;
        if( compound.hasKey("soulstone") )
        	return true;
        nmsStack.setTag(compound);
		return false;
	}
	
	public static ItemStack setSoulStoneMeta(ItemStack i) {
		net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound compound = nmsStack.getTag();
        if (compound == null) {
           compound = new NBTTagCompound();
            nmsStack.setTag(compound);
            compound = nmsStack.getTag();
        }
        //it guarantee not to stack
        double ID = id_random.nextDouble();
        System.out.println(ID);
        compound.setString("soulstone", Double.toString(ID));
        nmsStack.setTag(compound);
        i = CraftItemStack.asBukkitCopy(nmsStack);
        return i;
	}
	
	public static int getSouls(ItemStack i) {
		if(i.hasItemMeta() && i.getItemMeta().hasLore())
			return Integer.valueOf(i.getItemMeta().getLore().get(0).split(" ")[0]);
		return -1;
		
	}
	
	public static ItemStack setSouls(ItemStack i, int count) {
		if(i.hasItemMeta()) {
			ItemMeta im = i.getItemMeta();
			im.setLore(Lists.newArrayList(count+" "+i.getItemMeta().getLore().get(0).split(" ")[1]+" souls"));
			i.setItemMeta(im);
		}
		return i;
	}
	
	public static ItemStack increaseSouls(ItemStack i) {
		return setSouls(i, getSouls(i)+1);
	}
}
