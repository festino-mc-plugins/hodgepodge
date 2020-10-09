package com.festp.jukebox;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.festp.DelayedTask;
import com.festp.TaskList;
import com.festp.utils.ITickable;
import com.festp.utils.UtilsType;

public class JukeboxHandler implements Listener, ITickable {
	
	private final List<Block> powered = new ArrayList<>();
	private final List<Jukebox> clickedJukeboxes = new ArrayList<>();
	private final JukeboxUtils utils;
	
	public JukeboxHandler(NoteDiscList noteDiscs) {
		utils = new JukeboxUtils(noteDiscs);
	}
	
	public void tick() {
		powered.clear();
		
		for (Jukebox jukebox : clickedJukeboxes) {
			TaskList.add(new DelayedTask(1, new Runnable() {
				@Override
				public void run() {
					Jukebox jukeboxUpdated = (Jukebox) jukebox.getBlock().getState();
					utils.tryPlayNoteDisc(jukeboxUpdated);
				}
			}));
		}
		clickedJukeboxes.clear();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onJukeboxClick(PlayerInteractEvent event) {
		if (event.isCancelled()
				|| !event.hasBlock() || event.getAction() != Action.RIGHT_CLICK_BLOCK
				|| !event.hasItem() || !event.getItem().getType().isRecord()) {
			return;
		}
		Block block = event.getClickedBlock();
		if (block.getType() != Material.JUKEBOX) {
			return;
		}
		
		Jukebox jukebox = (Jukebox) block.getState();
		if (NoteDisc.isNoteDisc(event.getItem()) && UtilsType.isAir(jukebox.getRecord())) {
			clickedJukeboxes.add((Jukebox) block.getState());
		}
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
				Block centralBlock = b.getRelative(getFace(b).getOppositeFace());
				if (centralBlock.getType().isOccluding()) {
					playAround(centralBlock);
				}
			} else if (m == Material.DETECTOR_RAIL || UtilsType.isPlessurePlate(m)) {
				playAround(b);
				playAround(b.getRelative(BlockFace.DOWN));
			} else if (m == Material.REDSTONE_TORCH) {
				playFrom(b, BlockFace.DOWN);
			} else if (m == Material.REDSTONE_WALL_TORCH) {
				playFrom(b, getFace(b).getOppositeFace());
			} else if (m == Material.REPEATER || m == Material.COMPARATOR || m == Material.OBSERVER) {
				BlockFace face = getFace(b);
				Block centralBlock = b.getRelative(face.getOppositeFace());
				play(centralBlock);
				if (centralBlock.getType().isOccluding()) {
					playFrom(centralBlock, face);
				}
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
	
	public List<Jukebox> getClickedJukeboxes() {
		return clickedJukeboxes;
	}
	
	public void playAround(Block b) {
		BlockFace faces[] = { BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
		for (BlockFace face : faces)
			play(b.getRelative(face));
	}
	
	public void playFrom(Block b, BlockFace notPlay) {
		BlockFace faces[] = { BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
		for (BlockFace face : faces)
			if (face != notPlay)
				play(b.getRelative(face));
	}
	
	public BlockFace getFace(Block directable) {
		Directional directional = (Directional) directable.getBlockData();
		return directional.getFacing();
	}

	
	private boolean canPlay(Block block) {
		for (Block b : powered)
			if (block.equals(b))
				return false;
		return true;
	}
	

	public void play(Block b) {
		if (!canPlay(b))
			return;
		utils.play(b);
		if (b.getType() == Material.JUKEBOX)
			powered.add(b);
	}
}