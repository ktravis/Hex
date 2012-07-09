package com;

public class Tile {
	private float x, y;
	private int index;
	private Grid parent;
	
	public Tile(Grid parent) {
		this.parent = parent;
	}
	public Tile(Grid parent, int i) {
		this.parent = parent;
		index = i;
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
