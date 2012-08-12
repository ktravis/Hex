package com.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.media.opengl.GL2;

import util.KpixRecord;
import util.KpixDataRecord;
import util.KpixSample;

import com.detector.Grid.tileType;
import com.jogamp.opengl.util.awt.TextRenderer;

import util.Data;
import util.KpixFileReader;


public class HexDetector {
	private TextRenderer tr;
	ArrayList<Grid> layers = new ArrayList<Grid>();
	public static final int BUFFER_SIZE = 16;
	public static final int INIT_LAYERS = 1; 
	private int active = 0;
	private float centralAxisDepth;
	private float targetAxisDepth = 0;
	private float yaw;
	private float pitch;
	private float targetYaw = 0, targetPitch = 0;
	private float xOffset = 0, yOffset = 0;
	private float targetXOffset = 0, targetYOffset = 0;
	private boolean debug = true;			//defaults
	private boolean labels = false;			//--
	private float zoom = -100;
	private float targetZoom = zoom;
	private float[]	trueData = new float[1024];
	private int[] data = new int[1024];
	
	private KpixFileReader kpixReader;
	private int readerIndex = 0;
	private int timeStamp = 0;
	private boolean playing = false;
	private int playSpeed = 1;
	private int playOffset = 1;
	private List<Pair<float[], int[]>> dataBuf;
	private List<Integer> indexBuf;
	private int marker = 0;
	public class Pair<X, Y> { 
		  public final X x; 
		  public final Y y; 
		  public Pair(X x, Y y) { 
		    this.x = x; 
		    this.y = y; 
		  } 
		} 
	
	
	float[][] v;
	
	float[] x;
	float[] y;
	
	public void togglePlaying() { playing = !playing; }
	public void setPlayspeed(int s) { playSpeed = s; } 
	public void toggleDebug() { debug = !debug; }
	public void toggleLabels() { labels = !labels; }
	public void setActive(int i) { 
		if (active == i) return;
		try {
			if (layers.get(i) != null) active = i; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
		} catch (IndexOutOfBoundsException e) { 
			return;
		}
	}
	public void nextActive() { 
		try {
			if (layers.get(active + 1) != null) active += 1; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
		} catch (IndexOutOfBoundsException e) { 
			return;
		}
	}
	public void prevActive() { 
		try {
			if (layers.get(active - 1) != null) active -= 1; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
		} catch (IndexOutOfBoundsException e) { 
			return;
		}
	}
	public void addLayer() { 
		layers.add(new Grid(layers.get(0)));
		setActive(layers.size() - 1);
	}
	public void addLayer(Grid g) { 
		layers.add(g); 
		setActive(layers.size() - 1); 
	}
	public void removeLayer() { 
		if (layers.size() == 1) return;
		System.out.println(String.format("Layer %d removed.", layers.size() - 1));
		if (active == layers.size() - 1) active--;
		layers.remove(layers.size() - 1);
	}
	public void removeLayer(int i) { 
		if (layers.size() == 1 || i < 2) return;
		if (active == i) active--;
		layers.remove(i);
		System.out.println(String.format("Layer %d removed.", i));
	}
	public Grid getLayer(int i) { return layers.get(i); } 

	
	public HexDetector() {
		tr = new TextRenderer(Data.getFont(), false, true);
		Arrays.fill(data, 0);
		layers.add(new Grid());
		
		for (int g = 1; g < INIT_LAYERS; g++) {
			layers.add(new Grid(layers.get(0)));
		}
		setActive(0);
		
//		OLD METHOD, BAD INDICES
//		String[] temp = Data.fileRead("res/pixCoords.txt");
//		x = new float[temp.length];
//		y = new float[temp.length];
//		
//		for (int i = 0; i < temp.length; i++) {
//			x[i] = Float.valueOf(temp[i].split(" ")[0]);
//			y[i] = Float.valueOf(temp[i].split(" ")[1]);
//		}
		
		String[] temp = Data.fileRead("res/final.txt");
		x = new float[Grid.MAX_TILES];
		y = new float[Grid.MAX_TILES];
		
		for (int i = 0; i < x.length; i++) {
			x[i] = Float.valueOf(temp[i].split(" ")[1]);
			y[i] = Float.valueOf(temp[i].split(" ")[2]);
		}
		
		v = new float[Grid.tileType.values().length][];
		
		for (Grid.tileType t : Grid.tileType.values()) {
			v[t.ordinal()] = getArrays(t)[0];
		}
		
		pitch = yaw = 0;
		update();
	}
	
	public void draw(GL2 gl2) {
		update();
		
		gl2.glPushMatrix();
		gl2.glTranslatef(xOffset, -yOffset, zoom);
		gl2.glRotatef(yaw, 0, 1, 0);
		gl2.glRotatef(pitch, 1, 0, 0);
		
		//Detector orientation lines - colored R,G,B to denote local X,Y,Z
		if (debug) {
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor4f(0.8f, 0.0f, 0.0f, 0.5f);
			gl2.glVertex3f(0.0f, 0.0f, 0.0f);
			gl2.glVertex3f(100.0f, 0.0f, 0.0f);
			
			gl2.glColor4f(0.0f, 0.8f, 0.0f, 0.5f);
			gl2.glVertex3f(0.0f, 0.0f, 0.0f);
			gl2.glVertex3f(0.0f, 100.0f, 0.0f);
			
			gl2.glColor4f(0.0f, 0.0f, 0.8f, 0.5f);
			gl2.glVertex3f(0.0f, 0.0f, 0.0f);
			gl2.glVertex3f(0.0f, 0.0f, 100.0f);
			gl2.glEnd();
		}
		
		Grid curr;
		int layerIndex = layers.size() - 1;
		gl2.glRotatef(90, 0, 0, 1);

		boolean reverse = false;
		if (((Math.abs(yaw) > 90 && Math.abs(yaw) < 270) && (pitch < 90 || pitch > 270)) || ((Math.abs(yaw) < 90 || Math.abs(yaw) > 270) && (pitch < 270 && pitch > 90))) reverse = true;;
		if (reverse) layerIndex = 0;

		while((layerIndex >= 0 && !reverse) || (layerIndex < layers.size() && reverse)) {

			curr = layers.get(layerIndex);
			float alpha = 0.5f;
			if (layerIndex == active) alpha = 1.0f;
			float depth = -(layerIndex - 1) * 40 - centralAxisDepth;
			
			for (int index = 0; index < 1024; index++) {
				gl2.glPushMatrix();
				gl2.glTranslatef(y[index]/850, -x[index]/850, depth);
				
				
				if (layerIndex == 0) {
					float c = data[index]/255.0f;
					gl2.glColor4f(c, 0, 1 - c, alpha);
					if (data[index] == 256) gl2.glColor4f(0, 0.8f, 0, alpha);
					else if (data[index] == 257) gl2.glColor4f(1, 1, 1, alpha);
				} else {
					gl2.glColor4f(1, 1, 1, alpha);
				}
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				float[] verts = v[curr.getType(index).ordinal()];
				
				for (int j = 0; j < verts.length; j+=3) {
					gl2.glVertex3f(verts[j], verts[j+1], verts[j+2]);
				}
				gl2.glEnd();
				gl2.glPopMatrix();
			}
			
			if (labels  && layerIndex == 0) {
				tr.begin3DRendering();
				tr.setSmoothing(false);
				tr.setColor(1.0f, 1.0f, 1.0f, alpha);
				
				gl2.glPushMatrix();
				gl2.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
				
				for (int index = 0; index < 1024; index++) {
					String h = String.valueOf(trueData[index]);
					float yy = y[index]/850 - 0.33f;
					float xx = x[index]/850 - 0.30f*h.length();
					if (curr.getType(index) == tileType.SPLIT_RIGHT) {
						yy += 0.75f;
					} else if(curr.getType(index) == tileType.SPLIT_LEFT) {
						yy -= 0.92f;
					}
					
					if (data[index] != 257) tr.draw3D(h, xx, yy, 0.2f + depth, 0.05f);
					tr.flush();
				}
				gl2.glPopMatrix();
				
				tr.end3DRendering();
			}
			
			if (reverse) layerIndex++;
			else layerIndex--;
		}
		
		gl2.glPopMatrix();
		
	}
	
	public void drawHUD(GL2 gl2, int w, int h) {
		if (debug) {
			tr.beginRendering(w, h);
			tr.setColor(1.0f, 1.0f, 1.0f, 0.75f);
			tr.flush();
			tr.draw("layers = "+String.valueOf(layers.size()), 5, 25);
			tr.draw("active = "+String.valueOf(active), 5, 5);
			if (kpixReader != null) {
				tr.draw("reader index = "+String.valueOf(readerIndex), 5, 45);
				tr.draw("data timestamp = "+String.valueOf(timeStamp), 5, 65);
				if (playing) tr.draw("playback speed = "+ String.valueOf(1.0f/playSpeed), 5, 85);
			}
			tr.endRendering();
		}
	}
	
	public void update() {
		targetAxisDepth = getAxisDepth();
		
		if (Math.abs(targetAxisDepth - centralAxisDepth) > 0.01f) {
			centralAxisDepth += (targetAxisDepth - centralAxisDepth) * 0.1f;
		}
		
		if (Math.abs(targetZoom - zoom) > 0.01f) {
			zoom += (targetZoom - zoom) * 0.1f;
		}
		
		if (Math.abs(pitch - targetPitch) > 0.01f || Math.abs(yaw - targetYaw) > 0.01f) {
			if (pitch > 180) {
				pitch += (360 + targetPitch - pitch) * 0.1f;
			} else {		
				pitch += (targetPitch - pitch) * 0.1f;
			}
			if (yaw > 180) {
				yaw += (360 + targetYaw - yaw) * 0.1f;
			} else {
				yaw += (targetYaw - yaw) * 0.1f;
			}
		}
		
		
		if (Math.abs(targetXOffset - xOffset) > 0.01f || Math.abs(targetYOffset - yOffset) > 0.01f) {
			xOffset += (targetXOffset - xOffset) * 0.1f;
			yOffset += (targetYOffset - yOffset) * 0.1f;
		}
		
		//
		if (playing && kpixReader.hasNextRecord()) {
			if (playOffset >= playSpeed) {
				stepData();
				playOffset = 1;
			} else playOffset++;
		}
	}
	
	public void updateOrientation(float dx, float dy) {
		targetAxisDepth = getAxisDepth();
		
		pitch += (dy/2.0f)*0.5f;
		yaw += (dx/2.0f)*0.5f;
		
		pitch = pitch > 360 ? 0 : pitch;
		pitch = (float) (pitch < 0 ? 360 : pitch);
		yaw = Math.abs(yaw) > 360 ? 0 : yaw;
		yaw = (float) (pitch < 0 ? 360 : yaw);
		
		targetPitch = pitch;
		targetYaw = yaw;
		
	}
	
	public void setAxisPosition(float dx, float dy) {
		targetXOffset += dx;
		targetYOffset += dy;
	}
	
	public void resetOrientation() {
		targetPitch = targetYaw = 0;
		targetXOffset = targetYOffset = 0;
	}
	
	public float getAxisDepth() { return 40 - active * 40; }
	
	public void zoom(float dz) { targetZoom += dz; }
	
	public void setData(float[][] d) {
		trueData = d[0];
		data = new int[d[1].length];
		Arrays.fill(data, 0);
		for (int i = 0; i < data.length; i++) {
			data[i] = (int)d[1][i];
		}
		
	}
	
	public void setKpixReader(KpixFileReader r) { 
		if (r == null) return;
		kpixReader = r;
		readerIndex = 0;
		timeStamp = 0;
		dataBuf = new ArrayList<Pair<float[], int[]>>();
		indexBuf = new ArrayList<Integer>();
		stepData();
	}
	
	public void stepData() { 
		if (marker > 0) {
			
			Pair<float[], int[]> d = dataBuf.get(dataBuf.size() - marker);
			trueData = d.x;
			data = d.y;
			readerIndex = indexBuf.get(dataBuf.size() - marker);
			marker--;
//			System.out.println("buffer step");
			return;
		}
		dataBuf.add(new Pair<float[], int[]>(trueData, data));
		indexBuf.add(readerIndex);
		
		try {
			KpixRecord record = kpixReader.readRecord();
			readerIndex++;
			
	        while (kpixReader.hasNextRecord()) {
	        	if (record.getRecordType() == KpixRecord.KpixRecordType.DATA && record.getRecordLength() > 1000) break;
	        	record = kpixReader.readRecord();
	        	readerIndex++;
	        }
	        timeStamp = ((KpixDataRecord)record).getTimestamp();
	        List<KpixSample> temp = ((KpixDataRecord)record).getSamples();
	        trueData = new float[1024];
	        ListIterator<KpixSample> li = temp.listIterator();
	        KpixSample s = li.next();
	        
	        while (li.hasNext()) {
	        	s = li.next();
	        	if (s.getType() != KpixSample.KpixSampleType.KPIX) break;
	        	trueData[s.getChannel()] = s.getAdc();
	        }
        	data = Data.parseData(trueData);
		} catch (Exception e) {
			System.out.println("Failed to step data forward.");
			return;
		}
		if (!playing) System.out.println("Data stepped forward.");
		if (dataBuf.size() > BUFFER_SIZE) {
			dataBuf.remove(0);
			indexBuf.remove(0);
		}
		
	}
	
	public void stepDataBack() { 
		if (dataBuf == null || dataBuf.size() < 1) return;
		System.out.println("test");
		marker++;
		Pair<float[], int[]> d = dataBuf.get(dataBuf.size() - 1 - marker);
		trueData = d.x;
		data = d.y;
		readerIndex = indexBuf.get(indexBuf.size() - 1 - marker);
		
	}
	
	public void resetData() { 
		kpixReader.rewind();
		readerIndex = 0;
		marker = 0;
		dataBuf.clear();
		indexBuf.clear();
		System.out.println("Data record rewound.");
	}
	
	public static float[][] getArrays(Grid.tileType type) {
		if (type == null) return null;
		
		float sideLength = Grid.TILE_HALF_WIDTH;
		float sideNormalRadius = (float) (Math.sqrt(3)*sideLength/2.0f);
		
		float[] verts;
		float[] norms;
		
		switch(type) {
		
		case CENTER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case TOP_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case UL_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		case UR_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				0, sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case UL_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case UR_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case LL_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case LR_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case LL_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case LR_EDGE:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case BOTTOM_CORNER:
		{
			verts = new float[] {
				0, 0, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case SPLIT_LEFT:
		{
			verts = new float[] {
				0, 0, 0,
				0, sideLength, 0,
				-sideNormalRadius, sideLength/2, 0,
				-sideNormalRadius, -sideLength/2, 0,
				0, -sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		
		case SPLIT_RIGHT:
		{
			verts = new float[] {
				0, 0, 0,
				0, -sideLength, 0,
				sideNormalRadius, -sideLength/2, 0,
				sideNormalRadius, sideLength/2, 0,
				0, sideLength, 0,
			};
			norms = new float[] {
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
				0, 0, 1.0f,
			};
			return new float[][]{verts, norms};
		}
		}
		return null;
	}

}
