package util;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GLException;

import com.detector.HexDetector;
import com.display.GLWindow;
import com.display.Screen;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.util.awt.Screenshot;

public class KeyHandler extends KeyAdapter {
	HexDetector detector;
	GLWindow owner;
	private boolean holding = false;
	private boolean canCapture = false;
	private boolean t = false;
	
	public KeyHandler(GLWindow o, HexDetector h) {
		this.owner = o;
		detector = h;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		
		float dx = 0, dy = 0;

		if (code == KeyEvent.VK_I) dy = 2.0f;
		if (code == KeyEvent.VK_J) dx = -2.0f;
		if (code == KeyEvent.VK_K) dy = -2.0f;
		if (code == KeyEvent.VK_L) dx = 2.0f;
		
		if (!holding) {
			
			if (code == KeyEvent.VK_ESCAPE) owner.destroy();
			if (code == KeyEvent.VK_R) detector.resetOrientation();
			if (code == KeyEvent.VK_Q) detector.prevActive();
			if (code == KeyEvent.VK_W) detector.nextActive();
			if (code == KeyEvent.VK_S) detector.addLayer();
			if (code == KeyEvent.VK_A) detector.removeLayer();
			if (code == KeyEvent.VK_D) detector.toggleDebug();
			if (code == KeyEvent.VK_T) {
				detector.toggleLabels(true);
			}
			
			if (code == KeyEvent.VK_C) canCapture = owner.capture(canCapture);
			
			
			if (code > 47 && code < 59) {
				detector.setActive(code - 48);
			}
			holding = true;
		} 
		
		if (dx != 0 && dy != 0) detector.setAxisPosition(dx, dy);
	
	}
	
	
	
	@Override
	public void keyReleased(KeyEvent e) {
		holding = false;
		if (e.getKeyCode() == KeyEvent.VK_T) detector.toggleLabels(false);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

}
