package com.festp.utils;

import java.util.Random;

public class UtilsRandom {
	private static Random random = new Random();
	
	public static double getDouble() {
		return random.nextDouble();
	}
	
	public static int getInt(int bound) {
		return random.nextInt(bound);
	}
}
