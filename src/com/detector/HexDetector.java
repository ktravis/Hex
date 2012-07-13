package com.detector;

import java.util.ArrayList;
import java.util.ListIterator;

import javax.media.opengl.GL2;

public class HexDetector {
	ArrayList<Grid> layers = new ArrayList<Grid>();
	private int active = 0;
	
	void setActive(int i) { active = i; }
	void addLayer(Grid g) { layers.add(g); }
	Grid getLayer(int i) { return layers.get(i); } 
	
	
	void draw(GL2 gl2)  {
		if (layers == null) return;
		
		ListIterator<Grid> li = layers.listIterator();
		
		while (li.hasNext()) {
			Grid g = li.next();
			
			g.draw(gl2);
		}
	}

}
