package com.detector;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLArrayData;

import com.display.Screen;

//NOTE : If tileType == SPLIT -> is 



public class Tile {
	private float x, y;
	private int index;
	private Grid parent;
	private Type tileType;
	private int row = 0;
	private float[] verts;
	private float[] norms;
	private FloatBuffer vertBuffer;
	private FloatBuffer normBuffer;

	// enum "Type" describes the placement of the tile on the grid
	public enum Type {
		CENTER, TOP_CORNER, UL_EDGE, UR_EDGE, 
		UL_CORNER, UR_CORNER, LL_CORNER, LR_CORNER, 
		LL_EDGE, LR_EDGE, BOTTOM_CORNER, SPLIT_LEFT, SPLIT_RIGHT //split left/right refers to REMAINING half of tile, relative to initial tile numbering (right to left, top to bottom)
	}

	public Tile(Grid parent, Type t) {
		this.parent = parent;
		tileType = t;
		initArrays();
	}

	public Tile(Grid parent, Type t, int i) {
		this.parent = parent;
		index = i;
		tileType = t;
		initArrays();
	}

	void draw(GL2 gl2) {
		if (verts == null && norms == null) {
		} else {
			gl2.glColor3f(1, 1, 1);
			gl2.glBegin(gl2.GL_TRIANGLE_FAN);
			for (int i = 0; i < verts.length; i += 3) {
				gl2.glVertex3f(verts[i], verts[i+1], verts[i+2]);
			}
			gl2.glEnd();
			
		}
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
	
	void initArrays() {
		if (tileType == null) return;
		
		float sideLength = parent.TILE_HALF_WIDTH;
		float sideNormalRadius = (float) (Math.sqrt(3)*sideLength/2.0f);
		float tileWidth = parent.TILE_WIDTH;
		
		
		switch(tileType) {
		
		case CENTER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case TOP_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case UL_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		case UR_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				0, sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case UL_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case UR_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case LL_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case LR_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case LL_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case LR_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case BOTTOM_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case SPLIT_LEFT:
		{
			verts = new float[] {
				0, 0, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		
		case SPLIT_RIGHT:
		{
			verts = new float[] {
				0, 0, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			break;
		}
		}
		vertBuffer = Screen.asFloatBuffer(verts);
		normBuffer = Screen.asFloatBuffer(norms);
		
		
	}

	void remove() {
		parent.remove(this);
	}
	
	void setRow(int r) { row = r; }
	int getRow() { return row; }
}
