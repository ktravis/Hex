package com.display;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import util.Data;
import util.KeyHandler;
import util.MouseHandler;

import com.detector.HexDetector;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.Screenshot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;


public class GLWindow extends JFrame {
	public static final int DISPLAY_WIDTH = 720;
	public static final int DISPLAY_HEIGHT = 540;
	GLProfile glProfile; 
	GLCapabilities glCapabilities;
	GLCanvas glCanvas;
	String[] dataString;
	
	HexDetector h;
	boolean capturing = false;
	File currCapture;
	
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
                
                if (capturing) {
                	try {
                		System.out.print(String.format("Capturing file '%s'...", currCapture.toString()));
						if (!Data.NO_TGA) {
							Screenshot.writeToTargaFile(currCapture, glCanvas.getWidth(), glCanvas.getHeight());
						} else {
							Screenshot.writeToFile(currCapture, glCanvas.getWidth(), glCanvas.getHeight());
						}
					} catch (GLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
                	capturing = false;
                	System.out.print(" done.\n");
                }
            }
        });
		
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		
		this.getContentPane().add(glCanvas);
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
	
	public boolean capture(boolean checkDir) {
		currCapture = Data.getCaptureFile(checkDir);
		if (currCapture != null) {
			capturing = true;
			return true;
		} else {
			System.out.println("Cannot save file to capture directory.");
		}
		return false;
	}
	
	public static void main(String[] args) {
		
		GLWindow s;
		if (args.length > 0) {
			if (args[0].startsWith("-")) {
				if (args[0].contains("-a")) {
					s = new GLWindow("Event Display");
					if (args.length > 3) s.h.setData(Data.parseData(args[3], true, Float.valueOf(args[1]), Float.valueOf(args[2])));
					else s.h.setData(Data.parseData(args[1], true, 0.0f, 1.0f));
				} else if (args[0].contains("usage")) {
					System.out.println("java -jar EventDisplay.jar [-a (Scale values against absolute bounds) [low, high]] ['path/datafile.txt']");
					if (args.length > 1) {
						s = new GLWindow("Event Display");
						s.h.setData(Data.parseData(args[1]));
					}
				} else if (args[0].contains("-b")) {
					if (args.length < 2) {
						System.out.println("java -jar EventDisplay.jar [-a (Scale values against absolute bounds) [low, high]] ['path/datafile.txt']");
					} else {
						s = new GLWindow("Event Display");
						s.h.setKpixReader(Data.readKpixDataFile(args[1]));
					}
				}
			} else {
				s = new GLWindow("Event Display");
				s.h.setData(Data.parseData(args[0]));
			}
		} else {
			s = new GLWindow("Event Display");
		}
		
	}
	
}
