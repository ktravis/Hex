package util;

import com.detector.HexDetector;
import com.display.GLWindow;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;

public class KeyHandler extends KeyAdapter {
	HexDetector detector;
	GLWindow owner;
	private boolean holding = false;
	
	public static final int[] NUMS = {
		KeyEvent.VK_0, KeyEvent.VK_1, KeyEvent.VK_2,
		KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
		KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8,
		KeyEvent.VK_9
	};
	
	public KeyHandler(GLWindow o, HexDetector h) {
		this.owner = o;
		detector = h;
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (!holding) {
			int code = e.getKeyCode();
			
			if (code == KeyEvent.VK_ESCAPE) owner.destroy();
			if (code == KeyEvent.VK_R) detector.resetOrientation();
			if (code == KeyEvent.VK_Q) detector.prevActive();
			if (code == KeyEvent.VK_W) detector.nextActive();
			
			if (code > 47 && code < 59) {
				detector.setActive(code - 48);
			}
			holding = true;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		holding = false;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		
	}

}
