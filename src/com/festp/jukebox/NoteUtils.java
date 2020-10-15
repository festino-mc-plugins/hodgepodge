package com.festp.jukebox;

import java.util.HashMap;

import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.Note.Tone;

public class NoteUtils {
	public static final double SOUND_DISTANCE = 48;
	private static HashMap<String, Integer> INSTRUMENT_NAMES = null;
	
	public static Integer getInstrument(String name) {
		tryInitAlliases();
		return INSTRUMENT_NAMES.get(name);
	}
	
	/**"C0" -> 0<br>
	 * "C1" -> 12<br>
	 * e.t.c<br>
	 * Also supports clicks count: "0" (="F#3") -> 42 (= 6+3*12)*/
	public static int getSemitone(String note) {
		if (isUnsignedInteger(note)) {
			return Integer.parseInt(note) + NoteDiscRecord.STANDART_SEMITONE_OFFSET;
		}
		int octaves;
		try {
			octaves = Integer.parseInt(note.substring(note.length() - 1));
			note = note.substring(0, note.length() - 1);
		} catch (Exception e) {
			octaves = 3;
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
		}
		return semitone + octaves * NoteDiscRecord.OCTAVE;
	}

	public static int getSemitone(Note spigotNote) {
		int semitone = NoteDiscRecord.OCTAVE * (spigotNote.getOctave() + NoteDiscRecord.STANDART_OCTAVE_OFFSET);
		Tone tone = spigotNote.getTone();
		if (tone == Tone.C || tone == Tone.D || tone == Tone.E || tone == Tone.F && !spigotNote.isSharped())
			semitone += NoteDiscRecord.OCTAVE;
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
		int octaves = semitone / NoteDiscRecord.OCTAVE; // move to NoteUtils
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
				for (int j = 0; j < NoteDiscRecord.INSTRUMENTS.length; j++) {
					if (NoteDiscRecord.INSTRUMENTS[j].spigot == inst) {
						String vanillaName = NoteDiscRecord.INSTRUMENTS[j].sound.toString().substring("BLOCK_NOTE_BLOCK_".length());
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
