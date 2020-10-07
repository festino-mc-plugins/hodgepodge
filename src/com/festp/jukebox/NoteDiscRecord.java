package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Instrument;
import org.bukkit.Sound;

import com.festp.Pair;

public class NoteDiscRecord {
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
		public final Instrument spigot;
		
		public NoteInstrument(Sound sound, Instrument inst, int octaveShift) {
			this.sound = sound;
			this.octaveShift = octaveShift;
			this.semitoneShift = -STANDART_SEMITONE_OFFSET + octaveShift * OCTAVE;
			this.spigot = inst;
		}
		public NoteInstrument(Sound sound, Instrument inst) {
			this(sound, inst, 0);
		}
	}
	
	private final byte[] data;
	private int pos;
	private int tick;
	private int tickActual;
	
	public NoteDiscRecord(byte[] data) {
		this.data = data;
		pos = 0;
		tick = 0;
		tickActual = 0;
	}
	
	public List<NoteSound> getNext() {
		List<NoteSound> res = new ArrayList<>();
		if (pos >= data.length) {
			return res;
		}
		boolean isAux = (data[pos] & 0x80) != 0; // => gaps count
		if (isAux) {
			Pair<Integer, Integer> varInt = getVarInt(data, pos, 1);
			int dur = varInt.first;
			if (tickActual >= tick + dur) {
				tick = tickActual;
				pos += varInt.second;
			}
			if (pos >= data.length) {
				return res;
			}
			isAux = (data[pos] & 0x80) != 0;
		}
		if (!isAux) {
			do {
				int id = getByteInt(data, pos, 2);
				pos++;
				if (pos >= data.length) {
					return res;
				}
				int note = getByteInt(data, pos, 0);
				pos++;
				if (id < 0 || id >= INSTRUMENTS.length) {
					return res;
				}
				
				NoteInstrument parsed = INSTRUMENTS[id];
				Sound instrument = parsed.sound;
				float pitch = (float) Math.pow(2, (parsed.semitoneShift + note) / 12d - 1);
				res.add(new NoteSound(instrument, pitch));
			} while (pos < data.length && (data[pos - 2] & 0x40) != 0);
			tick++;
		}
		tickActual++;
		return res;
	}
	
	public boolean isTerminated() {
		return pos >= data.length;
	}
	
	private int getByteInt(byte[] data, int pos, int bitOffset) {
		if (pos >= data.length) {
			return -1;
		}
		return data[pos] & ~(~0 << (8 - bitOffset));
	}
	
	private Pair<Integer, Integer> getVarInt(byte[] data, int pos, int bitOffset) {
	    int x = 0;
	    int n = 0;
	    
        if (pos + n >= data.length) {
            return new Pair<>(0, n);
        }
        byte b = data[pos + n];
        n++;
        x += b & 0x7F;
        if ((b & 0x40) == 0) {
            return new Pair<>(x, n);
        }
        
	    for (int shift = 6; shift < 64; shift += 7) {
	        if (pos + n >= data.length) {
	            return new Pair<>(0, n);
	        }
	        b = data[pos + n];
	        n++;
	        x += (b & 0xFF) << shift;
	        if ((b & 0x80) == 0) {
	            return new Pair<>(x, n);
	        }
	    }
	    // The number is too large to represent in a 64-bit value.
	    return new Pair<>(0, data.length - pos);
	}
}
