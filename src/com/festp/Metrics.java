package com.festp;

// not so flexible, because it knows about every module, but therefore simple (global bucket ticks and index)
public class Metrics {
	
	public static enum MetricCategory { TASK_LIST, SLEEPING, INTERACT_HANDLER, INVENTORY_HANDLER, FEATURE_HANDLER, EXP_HOPPERS, DISPENSERS }

	private static final int BUCKET_COUNT = 10;
	private int currentBucket = 0;
	private int maxBucketTicks = 10 * 20;
	private int bucketTicks = 0;
	private long metrics[][] = new long[MetricCategory.values().length][BUCKET_COUNT];
	long[] timeStart = new long[MetricCategory.values().length];
	
	public void tick()
	{
		bucketTicks++;
		if (bucketTicks > maxBucketTicks) {
			bucketTicks = 0;
			currentBucket = (currentBucket + 1) % BUCKET_COUNT;
			for (int i = 0; i < timeStart.length; i++)
				metrics[i][currentBucket] = 0;
		}
	}
	
	public void start(MetricCategory category)
	{
		timeStart[category.ordinal()] = System.nanoTime();
	}
	
	public void end(MetricCategory category)
	{
		long t2 = System.nanoTime();
		int categoryIndex = category.ordinal();
		metrics[categoryIndex][currentBucket] += t2 - timeStart[categoryIndex];
		
		timeStart[categoryIndex] = Long.MIN_VALUE; // to detect errors by negative numbers
	}

	public double get(int i) {
		long fullTime = 0;
		int fullTicks = 0;
		for (int j = 0; j < BUCKET_COUNT; j++)
		{
			fullTime += metrics[i][j];
			if (j == currentBucket)
				fullTicks += bucketTicks;
			else if (metrics[i][j] != 0) // if initialized
				fullTicks += maxBucketTicks;
		}
		if (fullTicks == 0)
			return Double.NaN;
		return ((double)fullTime) / 1_000_000_000 * 20 / fullTicks;
	}
}
