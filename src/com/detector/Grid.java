package com.detector;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.media.opengl.GL2;

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
		System.out.println("filling...");
	}
	
	void fill() {
		if (tiles == null) return;
		
		int rowSize = 6;
		int index = 0;
		
		Tile.Type startType = Tile.Type.CENTER;
		Tile.Type endType = Tile.Type.CENTER;
		
		for (int row = 0; row < MAX_ROWS; row++) {
			if (row == 8 || row == 11 || row ==	27 || row == 30) {
				startType = endType = Tile.Type.CENTER;
				
				switch (row) {
				case 8: rowSize = 28; break;
				case 11: rowSize = 31; break;
				case 27: rowSize = 31; break;
				case 30: rowSize = 28; break;
				}
			} else if (row < 11 && row > 8) {
				startType = Tile.Type.UR_CORNER;
				endType = Tile.Type.UL_CORNER;
				rowSize = 28 + (row - 8);
			} else if (row < 8) {
				startType = Tile.Type.UR_EDGE;
				endType = Tile.Type.UL_EDGE;
				rowSize = 6 + row*3;
			} else if (row < 27 && row > 11) {
				if (row % 2 == 0) {
					startType = Tile.Type.SPLIT_LEFT;
					endType = Tile.Type.SPLIT_RIGHT;
					rowSize = 32;
				} else {
					startType = endType = Tile.Type.CENTER;
					rowSize = 31;
				}
				
				if (row > 14 && row < 24) {
					switch (row) {
					case 15: rowSize = 32; break;
					case 23: rowSize = 32; break;
					case 16: rowSize = 38; break;
					case 22: rowSize = 38; break;
					case 17: rowSize = 38; break;
					case 19: rowSize = 38; break;
					case 21: rowSize = 38; break;
					case 18: rowSize = 40; break;
					case 20: rowSize = 40; break;
					}
				}
				
			} else if (row < 30 && row > 27) {
				startType = Tile.Type.LR_CORNER;
				endType = Tile.Type.LL_CORNER;
				rowSize = 31 - (row - 27);
			} else if (row > 30) {
				startType = Tile.Type.LR_EDGE;
				endType = Tile.Type.LL_EDGE;
				rowSize = 27 - (row - 31)*3;
			}
			
			
			for (int i = 0; i < rowSize; i++) {
				
				Tile.Type t = Tile.Type.CENTER;

				if (i == 0) {
					t = startType;
				} else if (i == rowSize - 1) {
					t = endType;
				}
				
				if (row == 15 || row == 23) {
					if (i == 15) t = Tile.Type.SPLIT_RIGHT;
					else if (i == 16) t = Tile.Type.SPLIT_LEFT;
				} else if (row == 16 || row == 22) {
					if (i > 12 && i < 25) {
						if (i % 2 == 0) t = Tile.Type.SPLIT_LEFT;
						else t = Tile.Type.SPLIT_RIGHT;
					}
				} else if (row == 17 || row == 19 || row == 21) {
					if (i > 11 && i < 26) {
						if (i % 2 == 0) t = Tile.Type.SPLIT_RIGHT;
						else t = Tile.Type.SPLIT_LEFT;
					}
				} else if (row == 18 || row == 20) {
					if (i > 11 && i < 28) {
						if (i % 2 == 0) t = Tile.Type.SPLIT_RIGHT;
						else t = Tile.Type.SPLIT_LEFT;
					}
				}
				
				if (index == 2 || index == 3) t = Tile.Type.TOP_CORNER;
				if (index == 1020 || index == 1021) t = Tile.Type.BOTTOM_CORNER;
				
				Tile newTile = new Tile(this, t);
				newTile.setRow(row);
				
				tiles.add(newTile);
				
				index += 1;
				
			}
		}
		
	}
	
	void add(Tile t) {
		if (tiles == null || tiles.size() == MAX_TILES) return;
		tiles.add(t);
	}
	
	Tile getTile(int i) {
		return tiles.get(i);
	}
	
	//WRONG, fix
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
	
	public void draw(GL2 gl2) {
//		ListIterator<Tile> li = tiles.listIterator();
//		while (li.hasNext()) {
//			Tile t = li.next();
//			t.draw(gl2);
//		}
		getTile(1021).draw(gl2);
	}
	
}
	
