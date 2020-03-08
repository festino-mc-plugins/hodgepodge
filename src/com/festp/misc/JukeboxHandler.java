package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Jukebox;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.UtilsType;

public class JukeboxHandler implements Listener {
	
	private long time = 0;
	private List<Block> powered = new ArrayList<>();
	
	public void onTick() {
		powered.clear();
	}

	@EventHandler
	public void onJukeboxPower(BlockRedstoneEvent event) {
		if (event.getNewCurrent() > event.getOldCurrent()) {
			Block b = event.getBlock();
			Material m = b.getType();
			if (m == Material.REDSTONE_WIRE) {
				RedstoneWire wire = (RedstoneWire) b.getBlockData();
				play(b.getRelative(BlockFace.DOWN));
				BlockFace additional = null;
				for (BlockFace face : wire.getAllowedFaces()) {
					if (wire.getFace(face) == Connection.SIDE) {
						Block rel_block = b.getRelative(face);
						play(rel_block);
					}
					if (wire.getFace(face) != Connection.NONE) {
						if (additional == null)
							additional = face;
						else
							additional = BlockFace.SELF;
					}
				}
				if (additional != null && additional != BlockFace.SELF)
					play(b.getRelative(additional.getOppositeFace()));
			} else if (m == Material.DAYLIGHT_DETECTOR) {
				playAround(b);
			} else if (m == Material.LEVER || UtilsType.isButton(m) || m == Material.TRIPWIRE_HOOK) {
				playAround(b);
				playAround(b.getRelative(getFace(b).getOppositeFace()));
			} else if (m == Material.DETECTOR_RAIL || UtilsType.isPlessurePlate(m)) {
				playAround(b);
				playAround(b.getRelative(BlockFace.DOWN));
			} else if (m == Material.REDSTONE_TORCH) {
				playFrom(b, BlockFace.DOWN);
			} else if (m == Material.REDSTONE_WALL_TORCH) {
				playFrom(b, getFace(b).getOppositeFace());
			} else if (m == Material.REPEATER || m == Material.COMPARATOR || m == Material.OBSERVER) {
				playFrom(b, getFace(b).getOppositeFace());
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		
		// check if redstone block
		// check and power jukeboxes
		Block b = event.getBlock();
		if (b.getType() == Material.REDSTONE_BLOCK) {
			playAround(b);
		} else if (b.getType() == Material.REDSTONE_TORCH) {
			playFrom(b, BlockFace.DOWN);
		} else if (b.getType() == Material.REDSTONE_WALL_TORCH) {
			playFrom(b, getFace(b).getOppositeFace());
		}
	}
	
	public void playAround(Block b) {
		BlockFace faces[] = { BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
		for (BlockFace face : faces)
			play(b.getRelative(face));
	}
	
	public void playFrom(Block b, BlockFace not_play) {
		BlockFace faces[] = { BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
		for (BlockFace face : faces)
			if (face != not_play)
				play(b.getRelative(face));
	}
	
	public BlockFace getFace(Block directable) {
		Directional directional = (Directional) directable.getBlockData();
		return directional.getFacing();
	}

	public void play(Block b) {
		if (b.getType() == Material.JUKEBOX) {
			if (!canPlay(b))
				return;
			Jukebox jukebox = (Jukebox) b.getState();
			ItemStack record = jukebox.getRecord();
			//jukebox.eject();
			jukebox.setRecord(record);
			jukebox.update();
			powered.add(b);
		}
	}
	
	private boolean canPlay(Block block) {
		for (Block b : powered)
			if (block.equals(b))
				return false;
		return true;
	}
}