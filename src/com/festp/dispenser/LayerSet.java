package com.festp.dispenser;

public class LayerSet {
	int layer[][];
	int r, N;
	int max_distance = 0;
	Integer[] farthest = new Integer[] {null, null};
	private static int NEXT_BLOCK_DIST = -1; // must be < 0
	
	public LayerSet(int radius) {
		r = radius;
		N = 2*r + 1;
		layer = new int[N][N];
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				layer[i][j] = 0;
	}
	
	public void setDistance(int dx, int dz, int distance)
	{
		layer[r + dx][r + dz] = distance;
	}
	
	public void setNext(int dx, int dz)
	{
		layer[r + dx][r + dz] = NEXT_BLOCK_DIST;
	}
	
	public boolean isChecked(int dx, int dz)
	{
		return layer[r + dx][r + dz] > 0;
	}
	
	public boolean isUnchecked(int dx, int dz)
	{
		return layer[r + dx][r + dz] == 0 && !isNext(dx, dz);
	}
	
	public boolean isNext(int dx, int dz)
	{
		return layer[r + dx][r + dz] == NEXT_BLOCK_DIST;
	}
	
	public boolean isDefinedFarthest()
	{
		return farthest[0] != null && farthest[1] != null;
	}
	
	public void print() {
		System.out.println("LAYER:");
		print_raw();
	}
	public void print(int y) {
		System.out.println("LAYER Y="+y+":");
		print_raw();
	}
	private void print_raw()
	{
		for (int i = 0; i < N; i++) {
			String s = "";
			for (int j = 0; j < N; j++) {
				//if (layer[i][j] < 10)
				//	s += " ";
				s += layer[i][j];
			}
			System.out.println(s);
		}
	}
	
	public void print_scale(int y) {
		System.out.println("LAYER Y="+y+":");
		print_scale();
	}
	private void print_scale() {
		int max = 0;
		for(int i = 0; i < N; i++)
			for(int j = 0; j < N; j++) {
				if(layer[i][j] > max) max = layer[i][j];
		}
		double k = 10.0/(max + 1);
		for(int o = 0; o < N; o++) {
			String s = "";
			for(int j = 0; j < N; j++) {
				//s += layer[j][o]==0 ? ("o") : "0";
				s += (int)(layer[j][o]*k);
			}
			System.out.println(s);
		}
	}
}
