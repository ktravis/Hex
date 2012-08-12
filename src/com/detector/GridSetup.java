package com.detector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.detector.Grid.tileType;

public class GridSetup {
	public static float TILE_WIDTH = 5;
	public static float TILE_HALF_WIDTH = TILE_WIDTH/2;
	public static int MAX_ROW_SIZE = 30;
	public static int MAX_ROWS = 39;
	public static int MAX_TILES = 1024;

	public static tileType[] fill() {
		tileType[] types = new tileType[MAX_TILES];
		System.out.print("Filling...");
		
		int rowSize = 6;
		int index = 0;
		
		tileType startType = tileType.CENTER;
		tileType endType = tileType.CENTER;
		
		for (int row = 0; row < MAX_ROWS; row++) {
			if (row == 8 || row == 11 || row ==	27 || row == 30) {
				startType = endType = tileType.CENTER;
				
				switch (row) {
				case 8: rowSize = 28; break;
				case 11: rowSize = 31; break;
				case 27: rowSize = 31; break;
				case 30: rowSize = 28; break;
				}
			} else if (row < 11 && row > 8) {
				startType = tileType.UR_CORNER;
				endType = tileType.UL_CORNER;
				rowSize = 28 + (row - 8);
			} else if (row < 8) {
				startType = tileType.UR_EDGE;
				endType = tileType.UL_EDGE;
				rowSize = 6 + row*3;
			} else if (row < 27 && row > 11) {
				if (row % 2 == 0) {
					startType = tileType.SPLIT_LEFT;
					endType = tileType.SPLIT_RIGHT;
					rowSize = 32;
				} else {
					startType = endType = tileType.CENTER;
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
				startType = tileType.LR_CORNER;
				endType = tileType.LL_CORNER;
				rowSize = 31 - (row - 27);
			} else if (row > 30) {
				startType = tileType.LR_EDGE;
				endType = tileType.LL_EDGE;
				rowSize = 27 - (row - 31)*3;
			}
			
			
			for (int i = 0; i < rowSize; i++) {
				
				tileType t = tileType.CENTER;

				if (i == 0) {
					t = startType;
				} else if (i == rowSize - 1) {
					t = endType;
				}
				
				if (row == 15 || row == 23) {
					if (i == 15) t = tileType.SPLIT_RIGHT;
					else if (i == 16) t = tileType.SPLIT_LEFT;
				} else if (row == 16 || row == 22) {
					if (i > 12 && i < 25) {
						if (i % 2 == 0) t = tileType.SPLIT_LEFT;
						else t = tileType.SPLIT_RIGHT;
					}
				} else if (row == 17 || row == 19 || row == 21) {
					if (i > 11 && i < 26) {
						if (i % 2 == 0) t = tileType.SPLIT_RIGHT;
						else t = tileType.SPLIT_LEFT;
					}
				} else if (row == 18 || row == 20) {
					if (i > 11 && i < 28) {
						if (i % 2 == 0) t = tileType.SPLIT_RIGHT;
						else t = tileType.SPLIT_LEFT;
					}
				}
				
				if (index == 2 || index == 3) t = tileType.TOP_CORNER;
				if (index == 1020 || index == 1021) t = tileType.BOTTOM_CORNER;
				
				types[index] = t;
				
				index += 1;
				
			}
		}
		
		System.out.print(" done.\n");
		return types;
		
	}
	public static void main(String[] args) {
		tileType[] types = new tileType[MAX_TILES];
		types = fill();
		try {
			
			FileOutputStream f = new FileOutputStream(new File("htypes.txt"));
			
			for (tileType t: types) {
				f.write(String.format("%d\n", t.ordinal()).getBytes());
			}
			
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
