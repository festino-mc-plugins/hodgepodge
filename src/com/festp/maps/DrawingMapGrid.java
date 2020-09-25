package com.festp.maps;

import java.util.Arrays;
import java.util.Random;

public class DrawingMapGrid {
	private static final Random random = new Random();
	private static final int MS_TICK_DELAY = 1000 / 20;
	
	private final Grid[] grids;
	private final int width;
	
	public DrawingMapGrid(int size, int width, long initTime) {
		this.width = width;
		grids = new Grid[size];
		for (int i = 0; i < size; i++) {
			grids[i] = new Grid(i, initTime - getInitDelay());
		}
	}
	
	/** Sorts grids into last update time ascending order. */
	public void sort() {
		Arrays.sort(grids);
	}

	public int get(int index) {
		return grids[index].index;
	}
	
	public void updateTime(int index, long curTime, int blocksRendered) {
		double maxBlocks = width * width;
		grids[index].time += (curTime - grids[index].time) * (blocksRendered / maxBlocks);
		grids[index].time += getRandomDelay();
	}

	private int getInitDelay() {
		return random.nextInt(MS_TICK_DELAY);
	}
	private int getRandomDelay() {
		int diff = (int) (grids[grids.length - 1].time - grids[0].time);
		int avg_diff = Math.max(MS_TICK_DELAY, diff / grids.length);
		return random.nextInt(avg_diff);
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
			return Long.compare(time, g.time);
		}

		@Override
		public String toString() {
			return "{" + index + ":" + time + "}";
		}
	}
}
