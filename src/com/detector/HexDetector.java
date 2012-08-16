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
	private TextRenderer ltr, gtr;
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
	private boolean calibrated = false;
	private int labelMode = 0;
	private float zoom = -100;
	private float targetZoom = zoom;
	private float[]	trueData = new float[1024];
	private int[] data = new int[1024];
	private float mean = 0;
	private float[] means = new float[1024];
	private float[] variances = new float[1024];
	private float SCALE_FACTOR = 0.5f;
	
	private KpixFileReader kpixReader;
	private String fileName;
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
	public void cycleLabelMode() { labelMode++; if (labelMode > 2) labelMode = 0; }
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
	public void setScale(float[] d) { 
		mean = Data.getMean(d);
	}
	public float scale(float in) {
		return (float)(Math.log(Math.log(6*in/mean)));
	}
	
	public HexDetector() {
		ltr = new TextRenderer(Data.getFont(), false, true);
		gtr = new TextRenderer(Data.getFont(14), false, true);
		
		Arrays.fill(data, 0);
		
		String[] temp = Data.fileRead("res/final.txt");
		x = new float[Grid.MAX_TILES];
		y = new float[Grid.MAX_TILES];
		Grid.tileType[] template = new Grid.tileType[Grid.MAX_TILES];
		tileType[] ref = Grid.tileType.values();
		
		for (int i = 0; i < x.length; i++) {
			String[] s = temp[i].split(" ");
			template[i] = ref[Integer.valueOf(s[0])];
			x[i] = Float.valueOf(s[1]);
			y[i] = Float.valueOf(s[2]);
		}
		
		v = new float[ref.length][];
		
		for (Grid.tileType t : ref) {
			v[t.ordinal()] = getArrays(t)[0];
		}
		
		layers.add(new Grid(template));
		
		for (int g = 1; g < INIT_LAYERS; g++) {
			layers.add(new Grid(layers.get(0)));
		}
		setActive(0);
		
		pitch = yaw = 0;
		update();
	}
	
	public void draw(GL2 gl2) {
		update();
		
		gl2.glPushMatrix();
		gl2.glTranslatef(xOffset, -yOffset, zoom);
		gl2.glRotatef(yaw, 0, 1, 0);
		gl2.glRotatef(pitch, 1, 0, 0);
				
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
					if (calibrated) {
						float c = (float) (SCALE_FACTOR*(trueData[index] - means[index])/variances[index]);
						gl2.glColor4f(c, 0, 1 - c, alpha);
					} else {
						float c = trueData[index];
						if (c < 12) gl2.glColor4f(0.1f, 0.1f, 0.1f, alpha); 
						else { 
							c = scale(c);
							gl2.glColor4f(c, 0, 1 - c, alpha);
						}
					}
					
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
				ltr.begin3DRendering();
				ltr.setSmoothing(false);
				ltr.setColor(1.0f, 1.0f, 1.0f, alpha);
				
				gl2.glPushMatrix();
				gl2.glRotatef(-90.0f, 0.0f, 0.0f, 1.0f);
				
				for (int index = 0; index < 1024; index++) {
					String h = "";
					switch (labelMode) {
					case 0: h = String.valueOf((int)(trueData[index] - means[index])); break;
					case 1: h = String.valueOf((int)(trueData[index])); break;
					case 2: h = String.valueOf((int)(100*(trueData[index] - means[index])/variances[index])); break;
					
					}
					float yy = y[index]/850 - 0.33f;
					float xx = x[index]/850 - 0.30f*h.length();
					if (curr.getType(index) == tileType.SPLIT_RIGHT) {
						yy += 0.75f;
					} else if(curr.getType(index) == tileType.SPLIT_LEFT) {
						yy -= 0.92f;
					}
					
					if (data[index] != 257) ltr.draw3D(h, xx, yy, 0.2f + depth, 0.05f);
					ltr.flush();
				}
				gl2.glPopMatrix();
				
				ltr.end3DRendering();
			}
			
			if (reverse) layerIndex++;
			else layerIndex--;
		}
		
		gl2.glPopMatrix();
		
	}
	
	public void drawHUD(GL2 gl2, int w, int h) {
		if (debug) {
			gtr.setSmoothing(false);
			gtr.beginRendering(w, h);
			gtr.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			gtr.flush();
			gtr.draw("layers = "+String.valueOf(layers.size()), w - 75, 20);
			gtr.draw("active = "+String.valueOf(active), w - 75, 5);
			if (kpixReader != null) {
				gtr.draw("reader index = "+String.valueOf(readerIndex), 5, 5);
				gtr.draw("data timestamp = "+String.valueOf(timeStamp), 5, 20);
				if (playing) gtr.draw("playback speed = "+String.valueOf((int)100.0f/playSpeed)+"%", 5, 35);
				if (labels) {
					int offY = playing ? 15 : 0; 
					switch (labelMode) {
					case 0: gtr.draw("label mode = delta", 5, 35 + offY); break;
					case 1: gtr.draw("label mode = ADC", 5, 35 + offY); break;
					case 2: gtr.draw("label mode = %delta", 5, 35 + offY); break;
					}
				}
				if (calibrated) gtr.draw("scale factor = "+String.valueOf(SCALE_FACTOR), 5, 35 + (labels ? 15 : 0) + (playing ? 15 : 0));
				gtr.draw(fileName, 5, h - 15);
			}
			gtr.endRendering();
			
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
		fileName = r.getName();
		readerIndex = 0;
		timeStamp = 0;
		dataBuf = new ArrayList<Pair<float[], int[]>>();
		indexBuf = new ArrayList<Integer>();
		stepData();
	}
	
	public void stepData() { 
		if (marker < dataBuf.size() - 1) {
			marker++;
			Pair<float[], int[]> d = dataBuf.get(marker);
			trueData = d.x;
			data = d.y;
			readerIndex = indexBuf.get(marker);
			return;
		}
		
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
	        if (mean == 0) setScale(trueData);

		} catch (Exception e) {
			System.out.println("Failed to step data forward.");
			playing = false;
			return;
		}
		if (!playing) System.out.println("Data stepped forward.");
		
		dataBuf.add(new Pair<float[], int[]>(trueData, data));
		indexBuf.add(readerIndex);
		marker = dataBuf.size() - 1;
		if (dataBuf.size() > BUFFER_SIZE) {
			dataBuf.remove(0);
			indexBuf.remove(0);
			marker = BUFFER_SIZE - 1;
		}
		
	}
	
	public void stepDataBack() { 
		if (dataBuf == null || dataBuf.size() < 1 || marker == 0) return;
		marker--;
		Pair<float[], int[]> d = dataBuf.get(marker);
		trueData = d.x;
		data = d.y;
		readerIndex = indexBuf.get(marker);
	}
	
	public void resetData() { 
		kpixReader.rewind();
		readerIndex = 0;
		marker = 0;
		dataBuf.clear();
		indexBuf.clear();
		System.out.println("Data record rewound.");
	}
	
	public void calibrateData() { 
		try {
			System.out.print("Calibrating...");
			int count = 0;
			int offset = 0;
			Arrays.fill(means, 0);
			Arrays.fill(variances, 0);
			KpixRecord record = kpixReader.readRecord();
			count++;
			
	        while (kpixReader.hasNextRecord()) {
	        	record = kpixReader.readRecord();
	        	if (offset < 5) {
	        		offset++;
	        		continue;
	        	} 
	        	offset = 0;
	        	if (record.getRecordType() != KpixRecord.KpixRecordType.DATA || record.getRecordLength() < 1000) continue;
        	
	        	
	        	count++;
	        	
		        List<KpixSample> temp = ((KpixDataRecord)record).getSamples();
		        ListIterator<KpixSample> li = temp.listIterator();
		        KpixSample s = li.next();
		        
		        while (li.hasNext()) {
		        	s = li.next();
		        	if (s.getType() != KpixSample.KpixSampleType.KPIX) continue;
		        	int index = s.getChannel();
		        	int adc = s.getAdc();
		        	float delta =  adc - means[index];
		        	means[index] +=  delta/count;
		        	variances[index] += delta*(adc - means[index]);
		        	
		        }
	        	if (count > 500) break;
	        }
	        for (int i = 0; i < variances.length; i++) {
	        	variances[i] = (float) Math.sqrt(variances[i]/count);
	        }
	        

		} catch (Exception e) {
			System.out.println("Failed to calibrate data.");
			e.printStackTrace();
			return;
		}
		
		calibrated = true;
		kpixReader.rewind();
		System.out.println(" done.");
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
