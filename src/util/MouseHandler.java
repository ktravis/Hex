package util;


import com.detector.HexDetector;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;


public class MouseHandler extends MouseAdapter {
	boolean dragging = false;
	float lastx, lasty;
	float newx, newy;
	private Button down = Button.NONE;
	private Mode mode = Mode.NAV;
	
	public enum Button { NONE, LEFT, RIGHT, MIDDLE, }
	public enum Mode { NAV, HIST, }
	
	HexDetector detector;
	
	public MouseHandler(HexDetector h) {
		detector = h;
	}
	
	public void setMode(Mode m) { this.mode = m; }

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
			
			switch (mode) {
			case NAV : detector.updateOrientation(dx, dy); break;
			case HIST : detector.moveHistHandle((int)lastx, (int)dx); break;
			}
			
		} else if (down == Button.RIGHT) {
			lastx = newx;
			lasty = newy;
			
			newx = e.getX();
			newy = e.getY();
			
			dx = newx - lastx;
			dy = newy - lasty;
			
			switch (mode) {
			case NAV : detector.moveAxis(dx/2.0f, dy/2.0f); break;
			case HIST : break;
			}
		}
		
	}
	
	@Override 
	public void mouseMoved(MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();
		
		switch (mode) {
		case NAV : {
					if (mx < detector.getMBoxX() + detector.getMBoxW() && mx > detector.getMBoxX() && my > detector.getMBoxY()) detector.showMessages(true);
					else detector.showMessages(false); break; 
					}
		case HIST : break;
		}
		
	}
	
	@Override
	public void mouseWheelMoved(MouseEvent e) {
		float dr = e.getWheelRotation();
		
		switch (mode) {
		case NAV : detector.zoom(dr * 10); break;
		case HIST : break;
		}
	}
	
}
