package com.festp.jukebox;

import org.bukkit.Instrument;
import org.bukkit.Sound;

import com.festp.jukebox.NoteDiscRecord.NoteInstrument;

public class NoteSound {
	private final int instIndex;
	private final NoteInstrument full;
	private final int realSemitone;
	
	public NoteSound(NoteInstrument inst, int realSemitone) {
		this.full = inst;
		this.realSemitone = realSemitone;
		int id = 0;
		for (int i = 0; i < NoteDiscRecord.INSTRUMENTS.length; i++) {
			if (NoteDiscRecord.INSTRUMENTS[i].spigot == inst.spigot) {
				id = i;
				break;
			}
		}
		this.instIndex = id;
	}
	
	public NoteSound(int instIndex, int realSemitone) {
		this.instIndex = instIndex;
		this.full = NoteDiscRecord.INSTRUMENTS[instIndex];
		this.realSemitone = realSemitone;
	}
	
	public int getInstrumentId() {
		return instIndex;
	}
	
	public Sound getSpigotSound() {
		return full.sound;
	}
	
	public float getPitch() {
		return (float) Math.pow(2, (full.fullSemitoneShift + realSemitone) / 12d - 1);
	}
	
	public int getNbsSemitone() {
		return realSemitone + full.semitoneShift;
	}
	
	public int getRealSemitone() {
		return realSemitone;
	}
	
	public Instrument getSpigotInstrument() {
		return full.spigot;
	}
	
	/* too restricted
	public Note getSpigotNote() {
		return NoteUtils.getSpigotNote(full.semitoneShift + note);
	}*/
}