package util;


import com.detector.HexDetector;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;


public class MouseHandler extends MouseAdapter {
	boolean dragging = false;
	float lastx, lasty;
	float newx, newy;
	
	HexDetector detector;
	
	public MouseHandler(HexDetector h) {
		detector = h;
	}

	@Override 
	public void mousePressed(MouseEvent e) {
		dragging = true;
		lastx = e.getX();
		lasty = e.getY();
		
		newx = lastx;
		newy = lasty;
	}
	
	@Override 
	public void mouseReleased(MouseEvent e) {
		dragging = false;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		float dx, dy;
		
		lastx = newx;
		lasty = newy;
		
		newx = e.getX();
		newy = e.getY();
		
		dx = newx - lastx;
		dy = newy - lasty;
		
		detector.updateOrientation(dx, dy);
		
	}
	
	@Override
	public void mouseWheelMoved(MouseEvent e) {
		float dr = e.getWheelRotation();
		
		detector.zoom(dr * 10);
	}
	
	
}
