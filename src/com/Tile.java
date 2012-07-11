package com;

//NOTE : If tileType == SPLIT -> is 



public class Tile {
	private float x, y;
	private int index;
	private Grid parent;
	private Type tileType;
	private int row = 0;

	// enum "Type" describes the placement of the tile on the grid
	public enum Type {
		CENTER, TOP_CORNER, UL_EDGE, UR_EDGE, 
		UL_CORNER, UR_CORNER, L_EDGE, R_EDGE, LL_CORNER, LR_CORNER, 
		LL_EDGE, LR_EDGE, BOTTOM_CORNER, SPLIT_LEFT, SPLIT_RIGHT //split left/right refers to REMAINING half of tile, relative to initial tile numbering (right to left, top to bottom)
	}

	public Tile(Grid parent, Type t) {
		this.parent = parent;
		tileType = t;
	}

	public Tile(Grid parent, Type t, int i) {
		this.parent = parent;
		index = i;
		tileType = t;
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
	
	void setRow(int r) { row = r; }
	int getRow() { return row; }
}
