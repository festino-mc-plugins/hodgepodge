package com.festp.jukebox;

import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.festp.jukebox.NoteDiscRecord.NoteInstrument;

public class NoteSoundRecorder implements Listener {
	private final RecordingBookList recordingBookList;
	
	public NoteSoundRecorder(RecordingBookList recordingBookList) {
		this.recordingBookList = recordingBookList;
	}

	@EventHandler
	public void onBookOpen(PlayerInteractEvent event) {
		if (!event.hasItem() || event.getItem().getType() != Material.WRITABLE_BOOK) {
			return;
		}
		if (event.getPlayer().isSneaking()) {
			recordingBookList.add(event.getPlayer(), event.getHand());
		}
	}

	@EventHandler
	public void onNotePlay(NotePlayEvent event) {
		Instrument spigotInst = event.getInstrument();
		NoteInstrument inst = null;
		int instId;
		for (instId = 0; instId < NoteDiscRecord.INSTRUMENTS.length; instId++) {
			if (NoteDiscRecord.INSTRUMENTS[instId].spigot == spigotInst) {
				inst = NoteDiscRecord.INSTRUMENTS[instId];
				break;
			}
		}
		if (inst == null) {
			return;
		}
		Block block = event.getBlock();
		Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
		recordingBookList.play(instId, event.getNote(), blockCenter);
	}

	@EventHandler
	public void onPageClose(PlayerEditBookEvent event) {
		recordingBookList.remove(event.getPlayer());
	}
}
