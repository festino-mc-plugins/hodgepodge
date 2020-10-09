package com.festp.jukebox;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.inventory.ItemStack;

public class JukeboxUtils {
	private final NoteDiscList noteDiscs;
	
	public JukeboxUtils(NoteDiscList noteDiscs) {
		this.noteDiscs = noteDiscs;
	}

	public void play(Block b) {
		if (b.getType() == Material.JUKEBOX) {
			Jukebox jukebox = (Jukebox) b.getState();
			boolean addedNote = tryPlayNoteDisc(jukebox);
			if (!addedNote) {
				ItemStack record = jukebox.getRecord();
				//jukebox.eject();
				jukebox.setRecord(record);
				jukebox.update();
			}
		}
	}
	
	public boolean tryPlayNoteDisc(Jukebox jukebox) {
		noteDiscs.terminate(jukebox);
		boolean added = noteDiscs.add(jukebox);
		if (added) {
			stopVanillaPlaying(jukebox);
		}
		return added;
	}
	
	public static void stopVanillaPlaying(Jukebox jukebox) {
		jukebox.stopPlaying();
	}
}
