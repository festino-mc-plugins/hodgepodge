package com.festp.jukebox;

import org.bukkit.Sound;

public class NoteSound {
	public final Sound instrument;
	public final float pitch;
	
	public NoteSound(Sound instrument, float pitch) {
		this.instrument = instrument;
		this.pitch = pitch;
	}
}