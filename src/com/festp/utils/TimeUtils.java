package com.festp.utils;

public class TimeUtils {
	
	private static long ticks = 0;
	
	/** @return ticks from server start */
	public static long getTicks()
	{
		return ticks;
	}
	
	/** Use only from Main!!! */ // TODO improve code visibility
	public static void addTick()
	{
		ticks++;
	}
}
