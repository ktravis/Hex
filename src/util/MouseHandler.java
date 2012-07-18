package util;


import com.detector.HexDetector;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;


public class MouseHandler extends MouseAdapter {
	boolean dragging = false;
	float lastx, lasty;
	float newx, newy;
	private Button down = Button.NONE;
	
	public enum Button { NONE, LEFT, RIGHT, MIDDLE, }
	
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
		
		switch (e.getButton()) {
		case 1 : down = Button.LEFT; break;
		case 3 : down = Button.RIGHT; break;
		}
		
	}
	
	@Override 
	public void mouseReleased(MouseEvent e) {
		dragging = false;
		
		down = Button.NONE;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		float dx, dy;
		
		if (down == Button.LEFT) {
			lastx = newx;
			lasty = newy;
			
			newx = e.getX();
			newy = e.getY();
			
			dx = newx - lastx;
			dy = newy - lasty;
			
			detector.updateOrientation(dx, dy);
		} else if (down == Button.RIGHT) {
			lastx = newx;
			lasty = newy;
			
			newx = e.getX();
			newy = e.getY();
			
			dx = newx - lastx;
			dy = newy - lasty;
			
			detector.setAxisPosition(dx/2.0f, dy/2.0f);
		}
		
	}
	
	@Override
	public void mouseWheelMoved(MouseEvent e) {
		float dr = e.getWheelRotation();
		
		detector.zoom(dr * 10);
	}
	
	
}
