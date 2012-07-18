package com.display;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import util.Data;

import com.detector.HexDetector;
import com.jogamp.opengl.util.awt.Screenshot;


public class Screen {
	public static boolean WIREFRAME = false;
	
	protected static void setup(GL2 gl2, int width, int height ) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluPerspective( 90.0f, (1.0f*width)/height, 0.01f, 1000.0f );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
        
        gl2.glEnable(GL2.GL_DEPTH_TEST);
        gl2.glDepthFunc(GL2.GL_LEQUAL);
        
        
        gl2.glEnable(GL2.GL_BLEND);
        gl2.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        
    }
	
	protected static void render(GL2 gl2, int width, int height, HexDetector h) {
        gl2.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );

        gl2.glLoadIdentity();
		
        h.draw(gl2);
        
    }
	
	
}
