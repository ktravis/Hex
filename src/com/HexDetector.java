package com;

import java.util.ArrayList;
import java.util.ListIterator;

public class HexDetector {
	ArrayList<Grid> layers = new ArrayList<Grid>();
	private int active = 0;
	
	void setActive(int i) { active = i; }
	void addLayer(Grid g) { layers.add(g); }
	Grid getLayer(int i) { return layers.get(i); } 
	
	
	void draw()  {
		if (layers == null) return;
		
		ListIterator<Grid> li = layers.listIterator();
		
		while (li.hasNext()) {
			Grid g = li.next();
			
			g.draw();
		}
	}

}
