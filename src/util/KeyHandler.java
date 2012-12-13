package util;

import com.detector.HexDetector;
import com.display.GLWindow;
import com.display.GUIBar;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;


public class KeyHandler extends KeyAdapter {
	HexDetector detector;
	GLWindow owner;
	private boolean holding = false;
	private boolean canCapture = false;
	
	public KeyHandler(GLWindow o, HexDetector h) {
		this.owner = o;
		detector = h;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		
		float dx = 0, dy = 0;

		int mod = e.getModifiers();
		
		if (!holding) {
			if (code == KeyEvent.VK_ESCAPE) owner.destroy();
			if (code == KeyEvent.VK_R) detector.resetOrientation();
			if (code == KeyEvent.VK_Q) detector.prevActive();
			if (code == KeyEvent.VK_W) detector.nextActive();
			if (code == KeyEvent.VK_S) detector.addLayer();
			if (code == KeyEvent.VK_A) detector.removeLayer();
			if (code == KeyEvent.VK_Y) detector.stepData();
			if (code == KeyEvent.VK_T) detector.toggleLabels();
			if (code == KeyEvent.VK_P) detector.togglePlaying();
			if (code == KeyEvent.VK_SPACE) detector.togglePlaying();
			if (code == KeyEvent.VK_C) canCapture = owner.capture(canCapture);
			if (mod == 2 && code == KeyEvent.VK_D) detector.dump(GUIBar.intPrompt(10));
			else if (code == KeyEvent.VK_D) detector.toggleUI();
			if (code == KeyEvent.VK_I) detector.resetData();
			if (code == KeyEvent.VK_U) detector.stepDataBack();
			if (code == KeyEvent.VK_L) detector.cycleLabelMode();
			if (code == KeyEvent.VK_K) detector.cycleDispMode();
			
			if (code > 47 && code < 59) {
				detector.setActive(code - 48);
			}
			if (mod == 0) holding = true;
		} 
		
		if (dx != 0 && dy != 0) detector.setAxisPosition(dx, dy);
	
	}
	
	
	
	@Override
	public void keyReleased(KeyEvent e) {
		holding = false;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

}
