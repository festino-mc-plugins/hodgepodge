package com.festp.jukebox;

import java.util.HashMap;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.Note.Tone;

public class NoteUtils {
	public static final int OCTAVE = 12;
	public static final int STANDART_OCTAVE_OFFSET = 3;
	public static final int STANDART_SEMITONE_OFFSET = 6 + STANDART_OCTAVE_OFFSET * OCTAVE;
	/** index is instrument id => order is fixed! <br>
	 * standart range is F#3->F#5 (according to https://minecraft.gamepedia.com/Note_Block#Playing_music) <br>
	 * octaves starting from C0, that is 0x00 in record data<br>
	 * NoteInstrument.semitoneShift = semitone of the instrument sounds like C0<br>*/
	public static final NoteInstrument[] INSTRUMENTS = new NoteInstrument[] {
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_HARP, Instrument.PIANO),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_BASS, Instrument.BASS_GUITAR, +2),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_BASEDRUM, Instrument.BASS_DRUM),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_SNARE, Instrument.SNARE_DRUM),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_HAT, Instrument.STICKS), // "CLICK" in OpenNBS
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_GUITAR, Instrument.GUITAR, +1),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_FLUTE, Instrument.FLUTE, -1),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_BELL, Instrument.BELL, -2),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_CHIME, Instrument.CHIME, -2),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, Instrument.XYLOPHONE, -2),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, Instrument.IRON_XYLOPHONE),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_COW_BELL, Instrument.COW_BELL),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, Instrument.DIDGERIDOO),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_BIT, Instrument.BIT),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_BANJO, Instrument.BANJO),
		new NoteInstrument(Sound.BLOCK_NOTE_BLOCK_PLING, Instrument.PLING),
	};

	public static class NoteInstrument {
		public final Sound sound;
		public final int octaveShift;
		public final int semitoneShift;
		public final int fullSemitoneShift;
		public final Instrument spigot;
		
		public NoteInstrument(Sound sound, Instrument inst, int octaveShift) {
			this.sound = sound;
			this.octaveShift = octaveShift;
			this.semitoneShift = octaveShift * OCTAVE;
			this.fullSemitoneShift = this.semitoneShift - STANDART_SEMITONE_OFFSET;
			this.spigot = inst;
		}
		public NoteInstrument(Sound sound, Instrument inst) {
			this(sound, inst, 0);
		}
	}
	
	public static final double SOUND_DISTANCE = 48;
	private static HashMap<String, Integer> INSTRUMENT_NAMES = null;
	
	public static Integer getInstrument(String name) {
		tryInitAlliases();
		return INSTRUMENT_NAMES.get(name);
	}
	
	/**"C0" -> 0<br>
	 * "C1" -> 12<br>
	 * e.t.c<br>
	 * Also supports clicks count: "0" (="F#3") -> 42 (= 6+3*12)
	 * Values below -128 are errors*/
	public static int getSemitone(String note) {
		if (isUnsignedInteger(note)) {
			int res = Integer.parseInt(note);
			return res + STANDART_SEMITONE_OFFSET;
		}
		int octaves;
		try {
			octaves = Integer.parseInt(note.substring(note.length() - 1));
			note = note.substring(0, note.length() - 1);
		} catch (Exception e) {
			return -129;
		}
		int semitone = 0;
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
		} else {
			return -129;
		}
		return semitone + octaves * OCTAVE;
	}

	public static int getSemitone(Note spigotNote) {
		int semitone = OCTAVE * (spigotNote.getOctave() + STANDART_OCTAVE_OFFSET);
		Tone tone = spigotNote.getTone();
		if (tone == Tone.C || tone == Tone.D || tone == Tone.E || tone == Tone.F && !spigotNote.isSharped())
			semitone += OCTAVE;
		switch (tone) {
		case C: semitone += 0; break;
		case D: semitone += 2; break;
		case E: semitone += 4; break;
		case F: semitone += 5; break;
		case G: semitone += 7; break;
		case A: semitone += 9; break;
		case B: semitone += 11; break;
		}
		return semitone + (spigotNote.isSharped() ? 1 : 0);
	}

	public static String getNote(int semitone) {
		int octaves = semitone / OCTAVE;
		semitone = semitone % 12;
		String note;
		switch (semitone) {
		case 0: note = "C"; break;
		case 1: note = "C#"; break;
		case 2: note = "D"; break;
		case 3: note = "D#"; break;
		case 4: note = "E"; break;
		case 5: note = "F"; break;
		case 6: note = "F#"; break;
		case 7: note = "G"; break;
		case 8: note = "G#"; break;
		case 9: note = "A"; break;
		case 10: note = "A#"; break;
		case 11: note = "B"; break;
		default: note = ""; break;
		}
		return note + octaves;
	}

	public static String getNote(Note spigotNote, int octaveShift) {
		int octaves = spigotNote.getOctave() + octaveShift;
		Tone tone = spigotNote.getTone();
		if (tone == Tone.C || tone == Tone.D || tone == Tone.E || tone == Tone.F && !spigotNote.isSharped())
			octaves++;
		return tone + (spigotNote.isSharped() ? "#" : "") + octaves;
	}
	
	public static boolean isUnsignedInteger(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return str.length() > 0;
	}
	
	private static void tryInitAlliases() {
		if (INSTRUMENT_NAMES == null) {
			Instrument[] allInstruments = {
					Instrument.BANJO, Instrument.BASS_DRUM, Instrument.BASS_GUITAR, Instrument.BELL, Instrument.BIT, Instrument.CHIME,
					Instrument.COW_BELL, Instrument.DIDGERIDOO, Instrument.FLUTE, Instrument.GUITAR, Instrument.IRON_XYLOPHONE, Instrument.PIANO,
					Instrument.PLING, Instrument.SNARE_DRUM, Instrument.STICKS, Instrument.XYLOPHONE
			};
			String[] nbsNames = {
					"Banjo", "Bass Drum", "Double Bass", "Bell", "Bit", "Chime",
					"Cow Bell", "Didgeridoo", "Flute", "Guitar", "Iron Xylophone", "Harp",
					"Pling", "Snare Drum", "Click", "Xylophone"
			};
			String[] rusNames = { // ну и кринж названия
					"Банджо", "Большой барабан", "Бас-гитара", "Металлофон", "Аудиочип", "Колокольчик", // "(битная музыка)"
					"Коровий колокольчик", "Диджериду", "Флейта", "Гитара", "Железный ксилофон", "Пианино", // "/арфа"
					"Звонкая арфа", "Малый барабан", "Палочки", "Ксилофон"
			};
			INSTRUMENT_NAMES = new HashMap<>();
			for (int i = 0; i < allInstruments.length; i++) {
				Instrument inst = allInstruments[i];
				for (int j = 0; j < INSTRUMENTS.length; j++) {
					if (INSTRUMENTS[j].spigot == inst) {
						String vanillaName = INSTRUMENTS[j].sound.toString().substring("BLOCK_NOTE_BLOCK_".length());
						vanillaName = vanillaName.replace("_", "").toLowerCase();
						nbsNames[i] = nbsNames[i].replace(" ", "").toLowerCase();
						rusNames[i] = rusNames[i].replace(" ", "").toLowerCase();
						INSTRUMENT_NAMES.put(vanillaName, j);
						INSTRUMENT_NAMES.put(nbsNames[i], j);
						INSTRUMENT_NAMES.put(rusNames[i], j);
						break;
					}
				}
			}
		}
	}
}
