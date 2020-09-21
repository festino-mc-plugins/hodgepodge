package com.festp.maps;

import java.util.Arrays;

public class DrawingMapGrid {
	private final Grid[] grids;
	private final int width;
	
	public DrawingMapGrid(int size, int width, long initTime) {
		this.width = width;
		grids = new Grid[size];
		for (int i = 0; i < size; i++) {
			grids[i] = new Grid(i, initTime);
		}
		// TODO use Collections.shuffle(); or Random (from -1000 ms to 0)
	}
	
	/** Sorts grids into last update time ascending order. */
	public void sort() {
		Arrays.sort(grids);
	}

	public int get(int index) {
		return grids[index].index;
	}
	
	public void updateTime(int index, long curTime, int blocksRendered) {
		double maxBlocks = width * width / grids.length;
		grids[index].time += (curTime - grids[index].time) * (blocksRendered / maxBlocks);
	}
	
	private class Grid implements Comparable<Grid> {
		final int index;
		long time;
		
		public Grid(int index, long time) {
			this.index = index;
			this.time = time;
		}

		@Override
		public int compareTo(Grid g) {
			return (int) (time - g.time);
		}
	}
}
