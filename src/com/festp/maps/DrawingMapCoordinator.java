package com.festp.maps;

import com.festp.utils.Vector3i;

public class DrawingMapCoordinator {
	final int width;
	private final int halfWidth;
	private final char renderDir;
	private final char horDir;
	private final char vertDir;
	
	//   N=z 
	//W=x   E=X
	//   S=Z
	public DrawingMapCoordinator(Position pos, int width) {
		this.width = width;
		this.halfWidth = width / 2;
		if (pos.isDown() || pos.isUp()) {
			char tempRenderDir = 'y';
			char tempHorDir = 'X';
			char tempVertDir = 'Z';
			if (pos.isWest()) { // north default
				tempHorDir = 'z';
				tempVertDir = 'X';
			} else if (pos.isSouth()) {
				tempHorDir = 'x';
				tempVertDir = 'z';
			} else if (pos.isEast()) {
				tempHorDir = 'Z';
				tempVertDir = 'x';
			}
			if (pos.isUp()) {
				tempRenderDir = invert(tempRenderDir);
				tempVertDir = invert(tempVertDir);
			}
			renderDir = tempRenderDir;
			horDir = tempHorDir;
			vertDir = tempVertDir;
		} else { // if (pos.isVertical()) {
			if (pos.isWest()) {
				renderDir = 'x';
				horDir = 'z';
			} else if (pos.isEast()) {
				renderDir = 'X';
				horDir = 'Z';
			} else if (pos.isNorth()) {
				renderDir = 'z';
				horDir = 'X';
			} else { // if (pos.isSouth()) {
				renderDir = 'Z';
				horDir = 'x';
			}
			vertDir = 'y';
		}
		
	}

	// - get x, y, z directions and start/finish coords - finish = 5 => render
	// + translate virtual coords to real offsets from central point
	// + translate virtual coords to map coords
	/** @param hor is image x, from 0 to width-1 (hor dir)
	 *  @param vert is image y, from 0 to width-1 (vert dir)
	 * 	@param render is render step (render dir)
	 *  @return world offset from central point */
	public Vector3i getWorldCoord(int hor, int vert, int render) {
		hor -= halfWidth;
		vert -= halfWidth;
		if (isNeg(renderDir)) {
			render *= -1;
		}
		if (isNeg(horDir)) {
			hor *= -1;
		}
		if (isNeg(vertDir)) {
			vert *= -1;
		}
		Vector3i vec = new Vector3i(0, 0, 0);
		if (isX(renderDir)) {
			vec.setX(render);
		} else if (isY(renderDir)) {
			vec.setY(render);
		} else if (isZ(renderDir)) {
			vec.setZ(render);
		}
		if (isX(horDir)) {
			vec.setX(hor);
		} else if (isY(horDir)) {
			vec.setY(hor);
		} else if (isZ(horDir)) {
			vec.setZ(hor);
		}
		if (isX(vertDir)) {
			vec.setX(vert);
		} else if (isY(vertDir)) {
			vec.setY(vert);
		} else if (isZ(vertDir)) {
			vec.setZ(vert);
		}
		return vec;
	}
	
	public Vector3i getMapCoord(Vector3i center, Vector3i point) {
		point = point.clone().subtract(center);
		int hor = 0, vert = 0;
		if (isX(horDir)) {
			hor = point.getX();
		} else if (isY(horDir)) {
			hor = point.getY();
		} else if (isZ(horDir)) {
			hor = point.getZ();
		}
		if (isX(vertDir)) {
			vert = point.getX();
		} else if (isY(vertDir)) {
			vert = point.getY();
		} else if (isZ(vertDir)) {
			vert = point.getZ();
		}
		if (isNeg(horDir)) {
			hor *= -1;
		}
		if (isNeg(vertDir)) {
			vert *= -1;
		}
		return new Vector3i(hor, vert, 0);
	}
	
	private char invert(char dir) {
		switch (dir) {
		case 'x': return 'X';
		case 'X': return 'x';
		case 'y': return 'Y';
		case 'Y': return 'y';
		case 'z': return 'Z';
		case 'Z': return 'z';
		}
		return 0;
	}
	
	private boolean isNeg(char dir) {
		switch (dir) {
		case 'x': case 'y': case 'z': return true;
		case 'X': case 'Y': case 'Z': return false;
		default: return false;
		}
	}
	private boolean isX(char dir) {
		switch (dir) {
		case 'x': case 'X': return true;
		default: return false;
		}
	}
	private boolean isY(char dir) {
		switch (dir) {
		case 'y': case 'Y': return true;
		default: return false;
		}
	}
	private boolean isZ(char dir) {
		switch (dir) {
		case 'z': case 'Z': return true;
		default: return false;
		}
	}
}
