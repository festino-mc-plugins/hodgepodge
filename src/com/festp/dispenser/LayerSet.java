package com.festp.dispenser;

import org.bukkit.block.Block;

public class LayerSet {
	int layer[][];
	Block farthest;
	int farthest_dist2;
	int N = DropActions.max_dxz*2+1;
	
	public LayerSet(Block farthest, int farthest_dist2) {
		this.layer = new int[N][N];
		for(int o = 0; o < N; o++)
			for(int j = 0; j < N; j++)
				layer[j][o] = 0;
		this.farthest = farthest;
		this.farthest_dist2 = farthest_dist2;
	}
	
	public LayerSet(int[][] layer, Block farthest, int farthest_dist2) {
		this.layer = layer;
		this.farthest = farthest;
		this.farthest_dist2 = farthest_dist2;
	}
	
	public void print() {
		System.out.println("LAYER:");
		printa();
	}
	public void print(int y) {
		System.out.println("LAYER Y="+y+":");
		printa();
	}
	private void printa() {
		for(int o = 0; o < N; o++) {
			String s = "";
			for(int j = 0; j < N; j++) {
				//s += layer[j][o]==0 ? ("o") : "0";
				s += layer[j][o];
			}
			System.out.println(s);
		}
	}
	
	public void print_scale(int y) {
		System.out.println("LAYER Y="+y+":");
		print_scale();
	}
	private void print_scale() {
		int max=0;
		for(int o = 0; o < N; o++)
			for(int j = 0; j < N; j++) {
				if(layer[j][o] > max) max = layer[j][o];
		}
		double k = 10.0/(max+1);
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
