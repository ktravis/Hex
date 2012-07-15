package com.display;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.detector.Grid;
import com.detector.HexDetector;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class GLWindow extends JFrame {
	public static final int DISPLAY_WIDTH = 720;
	public static final int DISPLAY_HEIGHT = 540;
	GLProfile glProfile; 
	GLCapabilities glCapabilities;
	GLCanvas glCanvas;
	
	HexDetector h = new HexDetector();
	
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
		
		//BROKEN
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing() {
				dispose();
				System.exit(0);
			}
		});
		
		this.getContentPane().add(glCanvas, BorderLayout.CENTER);
		this.setSize(DISPLAY_WIDTH, DISPLAY_HEIGHT);
		this.setVisible(true);
		
		FPSAnimator animator = new FPSAnimator(glCanvas, 60);
		animator.add(glCanvas);
		animator.start();
		
	}
	
	public static void main(String[] args) {
		GLWindow s = new GLWindow("testing");
	}
}
