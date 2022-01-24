package com.festp.jukebox;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Jukebox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.festp.utils.NBTUtils;
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
					float pitch = sound.getPitch();
					if (0.5 <= pitch && pitch <= 2.0) {
						player.playSound(soundSource, sound.getSpigotSound(), SoundCategory.RECORDS, 3, sound.getPitch());
					} else {
						String instName = sound.getSpigotSound().toString().substring(NoteUtils.SPIGOT_NAME_BEGIN.length());
						String vanillaSoundName = NoteUtils.VANILLA_NAME_BEGIN + instName.toLowerCase();
						// 0.25 => -2, 0.2 -> -2, 0.120 -> -4 ||| 3 -> 2, 4 => 2, 7 -> 2, 8.5 -> 4
						double realShift = Math.log(pitch) / Math.log(2);
						int octaveShift = (int) Math.round(realShift / 2) * 2;
						pitch = pitch * (float) Math.pow(2, -octaveShift);
						String octaveShiftStr = (octaveShift < 0 ? "m" : "") + Math.abs(octaveShift);
						String customSoundName = vanillaSoundName + "_" + octaveShiftStr;
						player.playSound(soundSource, customSoundName, SoundCategory.RECORDS, 3, pitch);
					}
				}
			}
		}
		for (NoteSound sound : sounds) {
			//Bukkit.getPluginManager().callEvent(new NotePlayEvent(jukebox.getBlock(), sound.getSpigotInstrument(), sound.getSpigotNote()));
			Bukkit.getPluginManager().callEvent(new NoteDiscPlayEvent(soundSource, sound));
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
		byte[] data = NBTUtils.getByteArray(discItem, NBT_TAG);
		if (data == null) {
			return null;
		}
		NoteDisc disc = new NoteDisc(jukebox, data);
		return disc;
	}
	
	public static boolean isNoteDisc(ItemStack item) {
		byte[] data = NBTUtils.getByteArray(item, NBT_TAG);
		return data != null;
	}
}
