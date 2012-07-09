package com;

import java.util.ArrayList;
import java.util.ListIterator;

public class Grid {
	public static float TILE_WIDTH = 10;
	public static float TILE_HALF_WIDTH = TILE_WIDTH/2;
	public static int MAX_ROW_SIZE = 30;
	public static int MAX_ROWS = 39;
	public static int MAX_TILES = 1024;
	
	private boolean active = false;
	boolean isActive() { return active; }
	void setActive(boolean a) { active = a; }
	void toggleActive() { active = !active; }
	private ArrayList<Tile> tiles;
	
	public Grid() {
		tiles = new ArrayList<Tile>();
		fill();
	}
	
	void fill() {
		int rowSize = 6;
		
		
		if (tiles == null) return;

		for (int index = 0; index < MAX_TILES; index++) { 
			for (int row = 0; row < MAX_ROWS; row++) {
				
			}
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
		
		
		if (i/MAX_ROW_SIZE % 2 != 0) {
			n[0] = i - MAX_ROW_SIZE < 0 ? null : i - MAX_ROW_SIZE;
			n[1] = i - MAX_ROW_SIZE + 1 < 0 ? null : i - MAX_ROW_SIZE + 1;
			n[2] = i - 1 < 0 ? null : i - 1;
			n[3] = i + 1 > MAX_TILES - 1 ? null : i + 1;
			n[4] = i + MAX_ROW_SIZE > MAX_TILES - 1 ? null : i + MAX_ROW_SIZE;
			n[5] = i + MAX_ROW_SIZE + 1 > MAX_TILES - 1 ? null : i + MAX_ROW_SIZE + 1;
		} else {
			n[0] = i - MAX_ROW_SIZE - 1 < 0 ? null : i - MAX_ROW_SIZE - 1;
			n[1] = i - MAX_ROW_SIZE < 0 ? null : i - MAX_ROW_SIZE;
			n[2] = i - 1 < 0 ? null : i - 1;
			n[3] = i + 1 > MAX_TILES - 1 ? null : i + 1;
			n[4] = i + MAX_ROW_SIZE - 1 > MAX_TILES - 1 ? null : i + MAX_ROW_SIZE - 1;
			n[5] = i + MAX_ROW_SIZE > MAX_TILES - 1 ? null : i + MAX_ROW_SIZE;
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
	
	void draw() {
		ListIterator<Tile> li = tiles.listIterator();
		
		while (li.hasNext()) {
			Tile t = li.next();
			t.draw();
		}
		
	}
	
}
	
