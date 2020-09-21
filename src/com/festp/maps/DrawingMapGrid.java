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
		//System.out.print(index + " " + grids[index].time + " " + ((curTime - grids[index].time) * (blocksRendered / maxBlocks)));
		//System.out.print(grids[index].index + " " + grids[0].time + " " + grids[1].time + " " + grids[2].time);
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
