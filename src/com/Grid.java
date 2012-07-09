package com;

import java.util.ArrayList;

public class Grid {
	public static float TILE_WIDTH = 10;
	public static float TILE_HALF_WIDTH = TILE_WIDTH/2;
	public static int ROW_SIZE = 20;
	public static int MAX_TILES = 400;
	
	private ArrayList<Tile> tiles;
	
	public Grid() {
		tiles = new ArrayList<Tile>();
		fill();
	}
	
	void fill() {
		if (tiles == null) return;
		int start = tiles.size() - 1;
		
		while (start < MAX_TILES) {
			tiles.add(new Tile(this, start));
		}
	}
	
	void add() {
		if (tiles == null || tiles.size() == MAX_TILES) return;
		tiles.add(new Tile(this, tiles.size()));
	}
	
	Tile getTile(int i) {
		return tiles.get(i);
	}
	
	int[] getNeighbors(int i) {
		int[] n = new int[6];
		
		
		if (i/ROW_SIZE % 2 != 0) {
			n[0] = i - ROW_SIZE < 0 ? null : i - ROW_SIZE;
			n[1] = i - ROW_SIZE + 1 < 0 ? null : i - ROW_SIZE + 1;
			n[2] = i - 1 < 0 ? null : i - 1;
			n[3] = i + 1 > MAX_TILES - 1 ? null : i + 1;
			n[4] = i + ROW_SIZE > MAX_TILES - 1 ? null : i + ROW_SIZE;
			n[5] = i + ROW_SIZE + 1 > MAX_TILES - 1 ? null : i + ROW_SIZE + 1;
		} else {
			n[0] = i - ROW_SIZE - 1 < 0 ? null : i - ROW_SIZE - 1;
			n[1] = i - ROW_SIZE < 0 ? null : i - ROW_SIZE;
			n[2] = i - 1 < 0 ? null : i - 1;
			n[3] = i + 1 > MAX_TILES - 1 ? null : i + 1;
			n[4] = i + ROW_SIZE - 1 > MAX_TILES - 1 ? null : i + ROW_SIZE - 1;
			n[5] = i + ROW_SIZE > MAX_TILES - 1 ? null : i + ROW_SIZE;
		}
		
		
		return n;
	}
	
	void remove(Tile t) {
		tiles.remove(t);
	}
	void remove(int ind) {
		tiles.remove(ind);
	}
	void clear() {
		if (tiles != null) tiles.clear();
	}
	
	
	
}
	
