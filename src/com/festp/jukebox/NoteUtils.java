package com.festp.jukebox;

import org.bukkit.Note;
import org.bukkit.Note.Tone;

public class NoteUtils {
	public static final double SOUND_DISTANCE = 48;
	
	/**"C0" -> 0<br>
	 * "C1" -> 12<br>
	 * e.t.c*/
	public static int getPitch(String note) {
		int octaves;
		try {
			octaves = Integer.parseInt(note.substring(note.length() - 1));
			note = note.substring(0, note.length() - 1);
		} catch (Exception e) {
			octaves = 3;
		}
		int semitone = -1;
		if (note.equals("C")) {
			semitone = 0;
		} else if (note.equals("C#")) {
			semitone = 1;
		} else if (note.equals("D")) {
			semitone = 2;
		} else if (note.equals("D#")) {
			semitone = 3;
		} else if (note.equals("E")) {
			semitone = 4;
		} else if (note.equals("F")) {
			semitone = 5;
		} else if (note.equals("F#")) {
			semitone = 6;
		} else if (note.equals("G")) {
			semitone = 7;
		} else if (note.equals("G#")) {
			semitone = 8;
		} else if (note.equals("A")) {
			semitone = 9;
		} else if (note.equals("A#")) {
			semitone = 10;
		} else if (note.equals("B") || note.equals("H")) {
			semitone = 11;
		}
		return semitone + octaves * NoteDiscRecord.OCTAVE;
	}

	public static String getNote(Note spigotNote, int octaveShift) {
		int octaves = spigotNote.getOctave() + octaveShift;
		Tone tone = spigotNote.getTone();
		if (tone == Tone.C || tone == Tone.D || tone == Tone.E || tone == Tone.F && !spigotNote.isSharped())
			octaves++;
		return tone + (spigotNote.isSharped() ? "#" : "") + octaves;
	}
}
