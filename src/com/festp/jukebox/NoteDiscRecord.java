package com.festp.jukebox;

import java.util.ArrayList;
import java.util.List;

import com.festp.Pair;
import com.festp.jukebox.NoteUtils.NoteInstrument;

public class NoteDiscRecord {
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
				int id = getByteInt(data, pos, 2, false);
				pos++;
				if (pos >= data.length) {
					return res;
				}
				int semitone = getByteInt(data, pos, 0, true);
				pos++;
				if (id < 0 || id >= NoteUtils.INSTRUMENTS.length) {
					return res;
				}
				
				NoteInstrument parsed = NoteUtils.INSTRUMENTS[id];
				res.add(new NoteSound(parsed, semitone));
			} while (pos < data.length && (data[pos - 2] & 0x40) != 0);
			tick++;
		}
		tickActual++;
		return res;
	}
	
	public boolean isTerminated() {
		return pos >= data.length;
	}
	
	private int getByteInt(byte[] data, int pos, int bitOffset, boolean signed) {
		if (pos >= data.length) {
			return -1;
		}
		int res = data[pos] & ~(~0 << (8 - bitOffset));
		if (signed && (data[pos] & 0x80 >> bitOffset) > 0)
			return res - 256;
		return res;
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
