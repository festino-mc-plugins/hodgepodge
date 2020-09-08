package com.festp.maps;

import org.bukkit.Location;

public enum Position {
	VERTICAL_NORTH, VERTICAL_SOUTH, VERTICAL_WEST, VERTICAL_EAST,
	DOWN_NORTH, DOWN_SOUTH, DOWN_WEST, DOWN_EAST,
	UP_NORTH, UP_SOUTH, UP_WEST, UP_EAST;

	public boolean isUp() {
		return this == UP_NORTH || this == UP_SOUTH || this == UP_WEST || this == UP_EAST;
	}
	public boolean isDown() {
		return this == DOWN_NORTH || this == DOWN_SOUTH || this == DOWN_WEST || this == DOWN_EAST;
	}
	public boolean isVertical() {
		return this == VERTICAL_NORTH || this == VERTICAL_SOUTH || this == VERTICAL_WEST || this == VERTICAL_EAST;
	}
	public boolean isWest() {
		return this == VERTICAL_WEST || this == DOWN_WEST || this == UP_WEST;
	}
	public boolean isEast() {
		return this == VERTICAL_EAST || this == DOWN_EAST || this == UP_EAST;
	}
	public boolean isNorth() {
		return this == VERTICAL_NORTH || this == DOWN_NORTH || this == UP_NORTH;
	}
	public boolean isSouth() {
		return this == VERTICAL_SOUTH || this == DOWN_SOUTH || this == UP_SOUTH;
	}
	
	// n/s/w/e = 180/0/90/270
	// u/d = -90/90
	public static Position get(Location loc) {
		float yaw = loc.getYaw();
		float pitch = loc.getPitch();
		// west
		if (45 < yaw && yaw <= 135) {
			if (pitch <= -45f) {
				return UP_WEST;
			} else if (pitch >= 45f) {
				return DOWN_WEST;
			} else {
				return VERTICAL_WEST;
			}
		} else if (135 < yaw && yaw <= 225) {
			if (pitch <= -45f) {
				return UP_NORTH;
			} else if (pitch >= 45f) {
				return DOWN_NORTH;
			} else {
				return VERTICAL_NORTH;
			}
		} else if (225 < yaw && yaw <= 315) {
			if (pitch <= -45f) {
				return UP_EAST;
			} else if (pitch >= 45f) {
				return DOWN_EAST;
			} else {
				return VERTICAL_EAST;
			}
		} else {
			if (pitch <= -45f) {
				return UP_SOUTH;
			} else if (pitch >= 45f) {
				return DOWN_SOUTH;
			} else {
				return VERTICAL_SOUTH;
			}
		}
	}

	// n/s/w/e = -z/z/-x/x
	public boolean isParallelToX() {
		return isUp() || isDown() || isNorth() || isSouth();
	}
	public boolean isParallelToZ() {
		return isUp() || isDown() || isWest() || isEast();
	}
	public boolean isParallelToY() {
		return isVertical();
	}
}