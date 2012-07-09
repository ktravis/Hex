package com;

public class Tile {
	private float x, y;
	private int index;
	private Grid parent;
	private Type tileType;

	// enum "Type" describes the placement of the tile on the grid
	public enum Type {
		CENTER, TOP_CORNER, UL_EDGE, URT_EDGE, 
		UL_CORNER, UR_CORNER, L_EDGE, R_EDGE, LL_CORNER, LR_CORNER, 
		LL_EDGE, LR_EDGE, BOTTOM_CORNER, SPLIT
	}

	public Tile(Grid parent) {
		this.parent = parent;
		tileType = Type.CENTER;
	}

	public Tile(Grid parent, int i) {
		this.parent = parent;
		index = i;
		tileType = Type.CENTER;
	}

	void draw() {

	}

	int[] getNeighbors() {
		return parent.getNeighbors(index);
	}

	Tile[] getNeighborTiles() {
		Tile[] neighbors = new Tile[6];

		int[] n = getNeighbors();
		for (int i = 0; i < 6; i++) {
			neighbors[i] = parent.getTile(n[i]);
		}

		return neighbors;
	}

	void remove() {
		parent.remove(this);
	}
}
