package com.display;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import util.Data;
import util.KeyHandler;
import util.MouseHandler;

import com.detector.HexDetector;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.Screenshot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import com.jogamp.newt.event.awt.AWTKeyAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;


public class GLWindow extends JFrame {
	private static final long serialVersionUID = -1508624949773190945L;
	
	public static final int DISPLAY_WIDTH = 720;
	public static final int DISPLAY_HEIGHT = 440;
	GLProfile glProfile; 
	GLCapabilities glCapabilities;
	GLCanvas glCanvas;
	GLJPanel histCanvas;
	String[] dataString;
	JTable dataTable;
	GUIBar bar;
	
	HexDetector h;
	boolean fullscreen = false;
	boolean capturing = false;
	boolean wasPlaying = false;
	boolean updateLive = true;
	File currCapture;
	
	public GLWindow(String title) { 
		super(title);
		
		glProfile = GLProfile.getDefault();
		glCapabilities = new GLCapabilities(glProfile);
		glCanvas = new GLCanvas(glCapabilities);
		histCanvas = new GLJPanel();
		
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
                		h.sendMessage(String.format("Capturing file '%s'...", currCapture.toString()));
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
                	h.sendMessage(String.format("Captured file '%s'!", currCapture.toString()));
                }
                
                if (h.isPlaying() && updateLive) bar.update();
                else if (!h.isPlaying() && wasPlaying) bar.update();
                
                wasPlaying = h.isPlaying();
            }
        });
		histCanvas.addGLEventListener( new GLEventListener() {
            @Override
            public void reshape( GLAutoDrawable glautodrawable, int x, int y, int width, int height ) {
                Screen.setup2D( glautodrawable.getGL().getGL2(), width, height );
            }
            @Override
            public void init( GLAutoDrawable glautodrawable ) {
            }
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            }
            @Override
            public void display( GLAutoDrawable glautodrawable ) {
//                Screen.render2D( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight());
        		
            	h.drawHist( glautodrawable.getGL().getGL2(), glautodrawable.getWidth(), glautodrawable.getHeight());
            }
        });
			
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		
		h = new HexDetector();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		JPanel eastPanel = new JPanel();
		
		glCanvas.setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT));
		histCanvas.setPreferredSize(new Dimension(DISPLAY_WIDTH, 200));
		
		centerPanel.add(glCanvas, BorderLayout.CENTER);
		centerPanel.add(histCanvas, BorderLayout.SOUTH);
		
		centerPanel.setPreferredSize(new Dimension(DISPLAY_WIDTH, DISPLAY_HEIGHT + 200));
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		bar = new GUIBar(h);
		eastPanel.add(bar);
		eastPanel.setFocusable(false);
		eastPanel.setPreferredSize(new Dimension(240, DISPLAY_HEIGHT));
		mainPanel.add(eastPanel, BorderLayout.LINE_END);
		
		if (fullscreen) {
			this.setUndecorated(true);
			this.setExtendedState(MAXIMIZED_BOTH);
		}
		
		this.setMinimumSize(new Dimension(DISPLAY_WIDTH + 200, DISPLAY_HEIGHT + 245));
		this.add(mainPanel);

		MouseHandler histHandler = new MouseHandler(h);
		histHandler.setMode(MouseHandler.Mode.HIST);
		new AWTMouseAdapter(histHandler).addTo(histCanvas);
		
		
		MouseHandler inputHandler = new MouseHandler(h);
		new AWTMouseAdapter(inputHandler).addTo(glCanvas);
		
		KeyHandler keyHandler = new KeyHandler(this, h);
		new AWTKeyAdapter(keyHandler).addTo(glCanvas);
		
		eastPanel.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
				glCanvas.dispatchEvent(e);
			}
		});
		
		FPSAnimator animator = new FPSAnimator(60);
		animator.add(glCanvas);
		animator.add(histCanvas);
		this.pack();
		this.setVisible(true);
		animator.start();
		glCanvas.requestFocusInWindow();
		
		
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
			h.sendMessage("Cannot save file to capture directory.");
		}
		return false;
	}
	
	@Override
	public void requestFocus() {
		glCanvas.requestFocus();
	}

	public static void main(String[] args) {
		
		GLWindow s;
		if (args.length > 0) {
			if (args[0].startsWith("-")) {
				if (args[0].contains("--usage")) {
					System.out.println("java -jar EventDisplay.jar [-b 'path/datafile.bin' [-d number]]");
					System.out.println("\tb : load .bin file");
					System.out.println("\td : dump specified number of events to text file, and close");
				} else if (args[0].contains("b")) {
					if (args.length < 2) {
						System.out.println("java -jar EventDisplay.jar [-b 'path/datafile.bin' [-d number]]");
						System.out.println("\tb : load .bin file");
						System.out.println("\td : dump specified number of events to text file, and close");
					} else {
						if (args[2].contains("d")) {
							try {
								HexDetector h = new HexDetector();
								h.setKpixReader(Data.readKpixDataFile(args[1]));
								h.dump(Integer.valueOf(args[3]));
							} catch (NumberFormatException e) {
								System.out.println("java -jar EventDisplay.jar [-b 'path/datafile.bin' [-d number]]");
								System.out.println("\tb : load .bin file");
								System.out.println("\td : dump specified number of events to text file, and close");
							}
						} else {
							s = new GLWindow("Event Display");
						}
					}
				}
			} else {
				s = new GLWindow("Event Display");
			}
		} else {
			s = new GLWindow("Event Display");
		}
		
	}
	
}
