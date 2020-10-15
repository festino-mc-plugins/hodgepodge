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
		int instId;
		for (instId = 0; instId < NoteDiscRecord.INSTRUMENTS.length; instId++) {
			if (NoteDiscRecord.INSTRUMENTS[instId].spigot == spigotInst) {
				break;
			}
		}
		if (instId == NoteDiscRecord.INSTRUMENTS.length) {
			return;
		}
		Block block = event.getBlock();
		Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
		int semitone = NoteUtils.getSemitone(event.getNote());
		recordingBookList.play(instId, semitone, blockCenter);
	}

	@EventHandler
	public void onNoteDiscPlay(NoteDiscPlayEvent event) {
		NoteSound sound = event.getNoteSound();
		recordingBookList.play(sound.getInstrumentId(), sound.getNbsSemitone(), event.getLocation());
	}

	@EventHandler
	public void onPageClose(PlayerEditBookEvent event) {
		recordingBookList.remove(event.getPlayer());
	}
}
