package com.festp.enderchest;

import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.festp.Main;
import com.festp.Pair;

import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockAction;

public class EnderChestHandler implements Listener {
	private int ticks = 0;
	private final int PACKET_TICKS = 1;//, UPDATE_TICKS = 80;
	
	private Main pl;
	private List<Pair<Player, Block>> opened_chests = new ArrayList<>(); 
	
	public EnderChestHandler(Main pl) {
		this.pl = pl;
	}
	
	public void tick() {
		ticks++;
		if (ticks < PACKET_TICKS) {
			return;
		} 
		ticks = 0;
		//System.out.println("ecH TICK");
		for (Pair<Player, Block> pair : opened_chests) {
			//System.out.println("Animation: " + pair.first.getDisplayName() + " <- " + pair.second);
			playOpenAnimation(pair.first, pair.second);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		if (block != null) {
			if (block.getType() == Material.ENDER_CHEST) {
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					Player p = event.getPlayer();
					if (!p.isSneaking()) {
						if (block.getRelative(BlockFace.UP).getType().isOccluding()) {
							event.setCancelled(true);
							return;
						}
						EnderChest ec = pl.ecgroup.getAdminByPlayer(p);
						if(ec == null)
							ec = pl.ecgroup.getByNick(p.getName());
						if(ec != null) {
							event.setCancelled(true);
							p.openInventory(ec.getInventory());
							sendEnderchestOpenSound(p);
							addOpenedEnderchest(p, event.getClickedBlock());
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (p != null) {
			Block chest = getOpenedEnderchest(p);
			if (chest != null) {
				EnderChest ec = pl.ecgroup.getAdminByPlayer(p);
				// TODO highlevel EnderChest, merge this code with aleave from ECCommandWorker
				if (ec != null) {
					for (int i = 0; i < pl.admin_ecplayers.size(); i++)
						if(pl.admin_ecplayers.get(i).p == p) {
							pl.admin_ecplayers.remove(i);
							ECCommandWorker.sendGroupLeaveSound(p);
						}
					p.sendMessage(ChatColor.GREEN+"You successfully left the admin ecgroup.");
				}
				removeOpenedEnderchest(p);
				playCloseAnimation(p, chest);
				sendEnderchestCloseSound(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		for (int i = pl.admin_ecplayers.size() - 1; i >= 0; i--)
			if (pl.admin_ecplayers.get(i).p == e.getPlayer()) {
				pl.admin_ecplayers.remove(i);
			}
	}

	private void addOpenedEnderchest(Player p, Block b) {
		//System.out.println("Added " + p +", " + b);
		playOpenAnimation(p, b);
		opened_chests.add(new Pair<Player, Block>(p, b));
	}
	private Block getOpenedEnderchest(Player p) {
		for (Pair<Player, Block> pair : opened_chests) {
			if (pair.first == p) {
				//System.out.println("Returned " + pair.second);
				return pair.second;
			}
		}
		//System.out.println("Returned null");
		return null;
	}
	private void removeOpenedEnderchest(Player p) {
		for (int i = 0; i < opened_chests.size(); i++) {
			Pair<Player, Block> pair = opened_chests.get(i);
			if (pair.first == p) {
				playOpenAnimation(pair.first, pair.second);
				//System.out.println("Removed " + p);
				opened_chests.remove(i);
				break;
			}
		}
	}
	

	void playOpenAnimation(Player p, Block chest) {
		PacketPlayOutBlockAction packet =
				new PacketPlayOutBlockAction(
					new BlockPosition(chest.getX(), chest.getY(), chest.getZ()),
					org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers.getBlock(chest.getType()),
	                1, // Action ID, always 1 to opening chests
	                3); // Action param, number of players (> 0 to open)
		((CraftPlayer) p).getHandle().b.sendPacket(packet);
	}
	
	void playCloseAnimation(Player p, Block chest) {
		PacketPlayOutBlockAction packet =
				new PacketPlayOutBlockAction(
					new BlockPosition(chest.getX(), chest.getY(), chest.getZ()),
					org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers.getBlock(chest.getType()),
					1, // Action ID, always 1 to close chests
					0); // Action param, number of players (0 to close)
		((CraftPlayer) p).getHandle().b.sendPacket(packet);
	}
	
	public static void sendEnderchestCloseSound(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 1F, 1F);
	}
	
	public static void sendEnderchestOpenSound(Player p) {
		p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 1F, 1F);	
	}
}
