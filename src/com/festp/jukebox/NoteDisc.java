package com.festp.jukebox;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;

public class NoteDisc {
	private static final double SOUND_DISTANCE = 48;
	public static final String NBT_TAG = "note_disc_data";

	private final NoteDiscRecord parser;
	public final Jukebox jukebox;
	
	private NoteDisc(Jukebox jukebox, byte[] data) {
		this.jukebox = jukebox;
		parser = new NoteDiscRecord(data);
	}

	/** @return <b>true</b> if ticked (chunk had been loaded) */
	public boolean tick() {
		if (!jukebox.getChunk().isLoaded() || isTerminated()) {
			return false;
		}
		JukeboxUtils.stopVanillaPlaying(jukebox);
		
		List<NoteSound> sounds = parser.getNext();
		Location soundSource = jukebox.getLocation();
		for (Player player : soundSource.getWorld().getPlayers()) {
			if (player.getLocation().distance(soundSource) <= SOUND_DISTANCE) {
				for (NoteSound sound : sounds) {
					// no player.playNote(soundSource, Instrument.BANJO, Note.sharp(octave, tone));
					// because Note.Tone is inconvenient
					player.playSound(soundSource, sound.instrument, SoundCategory.RECORDS, 1, sound.pitch);
				}
			}
		}
		return true;
	}
	
	public boolean isTerminated() {
		return parser.isTerminated() || jukebox.getBlock().getType() != Material.JUKEBOX || jukebox.getRecord() == null;
	}
	
	/** @return <b>null</b> if jukebox isn't containing note disc */
	public static NoteDisc newDisc(Jukebox jukebox) {
		ItemStack discItem = jukebox.getRecord();
		byte[] data = Utils.getByteArray(discItem, NBT_TAG);
		if (data == null) {
			return null;
		}
		NoteDisc disc = new NoteDisc(jukebox, data);
		return disc;
	}
}
