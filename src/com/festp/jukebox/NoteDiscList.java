package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Jukebox;

import com.festp.utils.ITickable;

public class NoteDiscList implements ITickable {
	private List<NoteDisc> discs = new ArrayList<>();
	
	public void tick() {
		for (int i = discs.size() - 1; i >= 0; i--) {
			NoteDisc disc = discs.get(i);
			if (disc.isTerminated()) {
				discs.remove(i);
			} else {
				disc.tick();
			}
		}
	}
	
	public boolean add(Jukebox jukebox) {
		NoteDisc disc = NoteDisc.newDisc(jukebox);
		if (disc != null) {
			discs.add(disc);
			return true;
		}
		return false;
	}
	
	public void terminate(Jukebox jukebox) {
		for (int i = discs.size() - 1; i >= 0; i--) {
			NoteDisc disc = discs.get(i);
			if (disc.jukebox.getLocation().equals(jukebox.getLocation())) {
				discs.remove(disc);
			}
		}
	}
}
