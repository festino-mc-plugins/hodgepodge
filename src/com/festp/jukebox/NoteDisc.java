package com.festp.jukebox;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.Utils;
import com.festp.utils.UtilsType;

public class NoteDisc {
	public static final String NBT_TAG = "note_disc_data";

	private final NoteDiscRecord parser;
	public final Jukebox jukebox;
	private final Location soundSource;
	
	private NoteDisc(Jukebox jukebox, byte[] data) {
		this.jukebox = jukebox;
		soundSource = jukebox.getLocation().add(0.5, 0.5, 0.5);
		parser = new NoteDiscRecord(data);
	}

	/** @return <b>true</b> if ticked (chunk had been loaded) */
	public boolean tick() {
		if (!jukebox.getChunk().isLoaded() || isTerminated()) {
			return false;
		}
		JukeboxUtils.stopVanillaPlaying(jukebox);
		
		List<NoteSound> sounds = parser.getNext();
		for (Player player : soundSource.getWorld().getPlayers()) {
			if (player.getLocation().distance(soundSource) <= NoteUtils.SOUND_DISTANCE) {
				for (NoteSound sound : sounds) {
					// no player.playNote(soundSource, NoteInstrument.BANJO, Note.sharp(octave, tone));
					// because Note.Tone is inconvenient
					player.playSound(soundSource, sound.instrument, SoundCategory.RECORDS, 3, sound.pitch);
				}
			}
		}
		return true;
	}
	
	public boolean isTerminated() {
		Block updatedBlock = jukebox.getBlock();
		return parser.isTerminated() || updatedBlock.getType() != Material.JUKEBOX
				|| UtilsType.isAir(((Jukebox) updatedBlock.getState()).getRecord());
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
	
	public static boolean isNoteDisc(ItemStack item) {
		byte[] data = Utils.getByteArray(item, NBT_TAG);
		return data != null;
	}
}
