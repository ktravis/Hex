package com.display;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import util.KeyHandler;
import util.MouseHandler;

import com.detector.HexDetector;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;


public class GLWindow extends JFrame {
	public static final int DISPLAY_WIDTH = 720;
	public static final int DISPLAY_HEIGHT = 540;
	GLProfile glProfile; 
	GLCapabilities glCapabilities;
	GLCanvas glCanvas;
	
	HexDetector h;
	
	public GLWindow(String title) { 
		super(title);
		
		glProfile = GLProfile.getDefault();
		glCapabilities = new GLCapabilities(glProfile);
		glCanvas = new GLCanvas(glCapabilities);
		
		setup();
	}
	
	void setup() {
		glCanvas.addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                Screen.setup( glautodrawable.getGL().getGL2(), width, height );
            }
            
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
                Screen.render( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight(), h);
            }
        });
		
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		
		this.getContentPane().add(glCanvas, BorderLayout.CENTER);
		this.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
		this.setVisible(true);
		
		h = new HexDetector();
		
		MouseHandler inputHandler = new MouseHandler(h);
		new AWTMouseAdapter(inputHandler).addTo(glCanvas);
		
		KeyHandler keyHandler = new KeyHandler(this, h);
		new AWTKeyAdapter(keyHandler).addTo(glCanvas);
		
		FPSAnimator animator = new FPSAnimator(glCanvas, 60);
		animator.add(glCanvas);
		animator.start();
		
	}
	
	public void destroy() {
		this.dispose();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		GLWindow s = new GLWindow("Event Display");
	}
}
