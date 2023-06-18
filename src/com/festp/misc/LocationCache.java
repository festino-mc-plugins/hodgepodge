package com.festp.misc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

public class LocationCache {
	private static class LocationPair {
		Location sample;
		Location target;
		
		public LocationPair(Location sample, Location target) {
			this.sample = sample;
			this.target = target;
		}
		
		public Location getSample() {
			return sample;
		}
		
		public Location getTarget() {
			return target;
		}
	}
	
	private static final double STICK_DISTANCE_SQUARED = 128 * 128;
	private static final double SAMPLE_SIZE = 64; // TODO must depend on player count (?)
	
	private final List<LocationPair> samples = new ArrayList<>();

	public Location get(Location sample) {
		double minDist = Double.POSITIVE_INFINITY;
		Location minDistTarget = null;
		for (LocationPair pair : samples) {
			double dist = pair.getSample().distanceSquared(sample);
			if (dist > STICK_DISTANCE_SQUARED)
				continue;
			if (minDistTarget == null || minDist > dist) {
				minDist = dist;
				minDistTarget = pair.getTarget();
			}
		}
		// TODO move element to the end of the list
		return minDistTarget;
	}

	public void store(Location sample, Location target) {
		for (LocationPair pair : samples) {
			double dist = pair.getSample().distanceSquared(sample);
			if (dist <= STICK_DISTANCE_SQUARED) {
				return;
			}
		}
		
		if (samples.size() >= SAMPLE_SIZE) {
			samples.remove(0);
		}
		samples.add(new LocationPair(sample, target));
	}

}
