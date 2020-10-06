package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;

import com.festp.Pair;

public class NoteDiscRecord {
	public static final int OCTAVE = 12;
	private static final int STANDART_OFFSET = -(6 + 3 * OCTAVE);
	/** index is instrument id => order is fixed! <br>
	 * standart range is F#3->F#5 (according to https://minecraft.gamepedia.com/Note_Block#Playing_music) <br>
	 * octaves starting from C0, that is 0x00 in record data<br>
	 * Instrument.semitoneShift = semitone of the instrument sounds like C0<br>*/
	public static final Instrument[] INSTRUMENTS = new Instrument[] {
		new Instrument(Sound.BLOCK_NOTE_BLOCK_HARP, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_BASS, STANDART_OFFSET + 2 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_BASEDRUM, STANDART_OFFSET ),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_SNARE, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_HAT, STANDART_OFFSET), // "CLICK" in OpenNBS
		new Instrument(Sound.BLOCK_NOTE_BLOCK_GUITAR, STANDART_OFFSET + 1 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_FLUTE, STANDART_OFFSET - 1 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_BELL, STANDART_OFFSET - 2 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_CHIME, STANDART_OFFSET - 2 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, STANDART_OFFSET - 2 * OCTAVE),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_COW_BELL, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_BIT, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_BANJO, STANDART_OFFSET),
		new Instrument(Sound.BLOCK_NOTE_BLOCK_PLING, STANDART_OFFSET),
	};

	public static class Instrument {
		public final Sound sound;
		public final int semitoneShift;
		
		public Instrument(Sound sound, int semitoneShift) {
			this.sound = sound;
			this.semitoneShift = semitoneShift;
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
				
				Instrument parsed = INSTRUMENTS[id];
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
	    
	    /*
	    
	    for shift := uint(0); shift < 64; shift += 7 {
	        if n >= len(buf) {
	            return 0, 0
	        }
	        b := uint64(buf[n])
	        n++
	        x += (b & 0xFF) << shift
	        if (b & 0x80) == 0 {
	            return x, n
	        }
	    }

	    // The number is too large to represent in a 64-bit value.
	    return 0, 0
	    */
	}
}
