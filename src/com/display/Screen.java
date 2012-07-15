package com.display;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import java.nio.Buffer;

import com.detector.Grid;
import com.detector.HexDetector;
import com.jogamp.common.nio.Buffers;

public class Screen {
	
	
	protected static void setup(GL2 gl2, int width, int height ) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluPerspective( 90.0f, (1.0f*width)/height, 0.01f, 1000.0f );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
    }
	
	protected static void render(GL2 gl2, int width, int height, HexDetector h) {
        gl2.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // draw a triangle filling the window
        gl2.glLoadIdentity();

		
        h.draw(gl2);
        
    }
	
	public static FloatBuffer asFloatBuffer(float... args) {
		FloatBuffer buffer = FloatBuffer.allocate(args.length * 4);
		buffer.put(args);
		buffer.flip();
		return buffer;
	}
}
