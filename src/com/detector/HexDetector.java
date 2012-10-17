package com.detector;

import java.awt.Color;
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
	private float pitch, yaw;
	private float targetYaw = 0, targetPitch = 0;
	private float xOffset = 0, yOffset = 0;
	private float targetXOffset = 0, targetYOffset = 0;
	private boolean labels = false;			
	private boolean calibrated = false;
	private boolean showMessages = true;
	private boolean newMessage = true;
	private float mBoxAlpha = 1.0f;
	private int mCounter = 50;
	private ArrayList<String> messages;
	private int mBoxY = 0;
	private int mBoxX = 0;
	private int mBoxW = 375;
	private int mBoxH = 25;
	private int dispMode = 0; //0 : Log scale, 1 : Calibrated scale
	private int labelMode = 0;
	private float zoom = -100;
	private float targetZoom = zoom;
	private float[][]	trueData = new float[1024][4];
//	private int[][] data = new int[1024][4];
	private float mean = 0;
	private float[][] means = new float[1024][4];
	private float[][] variances = new float[1024][4];
	private float[][] calibMins = new float[1024][4];
	private int[][] tTimes = new int[1024][4];
	private int currBucket = 0;
	private float SCALE_FACTOR = 0.5f;
	private int highlighted = -1;
	
	private Color hiColor = Color.red;
	private Color loColor = Color.blue;
	private Color zeroColor = Color.green;
	
	private KpixFileReader kpixReader;
	private String filePath = "none";
	private String fileName = "none";
	private String calibFilePath = "none";
	private String calibFileName = "none";
	private int readerIndex = 0;
	private int timeStamp = 0;
	private boolean playing = false;
	private boolean last = false;
	private float playSpeed = 1;
	private int playOffset = 1;
	private List<Pair<float[][], int[][]>> dataBuf;
	private List<Integer> indexBuf;
	private int marker = 0;
	public boolean updateLive = true;
	public boolean adjusted = false;
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
	
	public float[][] getData() { return trueData; }
	public float[][] getMins() { return calibMins; }
	public float[][] getMeans() { return means; }
	public int[][] getTimes() { return tTimes; }
	public void togglePlaying() { playing = !playing; }
	public void setPlayspeed(float s) { playSpeed = s; if (s < 1 && playing) togglePlaying(); } 
	public void toggleLabels() { labels = !labels; }
	public void setLabelMode(int i) { labelMode = i; }
	public void cycleLabelMode() { labelMode++; if (labelMode > 2) labelMode = 0; }
	public void setDispMode(int i) { dispMode = i; }
	public void cycleDispMode() { if (calibrated) dispMode = dispMode < 1 ? 1 : 0; else calibrateData();} 
	public void setActive(int i) { 
		if (active == i) return;
		try {
			if (layers.get(i) != null) active = i; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
			sendMessage(String.format("Layer %d now active.", active));
		} catch (IndexOutOfBoundsException e) { 
			return;
		}
	}
	public void nextActive() { 
		try {
			if (layers.get(active + 1) != null) active += 1; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
			sendMessage(String.format("Layer %d now active.", active));
		} catch (IndexOutOfBoundsException e) { 
			return;
		}
	}
	public void prevActive() { 
		try {
			if (layers.get(active - 1) != null) active -= 1; 
			update();
			System.out.println(String.format("Layer %d now active.", active));
			sendMessage(String.format("Layer %d now active.", active));
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
		sendMessage(String.format("Layer %d removed.", layers.size() - 1));
		if (active == layers.size() - 1) active--;
		layers.remove(layers.size() - 1);
	}
	public void removeLayer(int i) { 
		if (layers.size() == 1 || i < 2) return;
		if (active == i) active--;
		layers.remove(i);
		System.out.println(String.format("Layer %d removed.", i));
		sendMessage(String.format("Layer %d removed.", i));
	}
	public Grid getLayer(int i) { return layers.get(i); } 
	public void setScale(float s) {
		try {
			SCALE_FACTOR = s;
		} catch (Exception e){}
	}
	public void setScale(float[] d) { 
		mean = Data.getMean(d);
	}
	public float scale(float in) {
		return (float)(Math.log(Math.abs(Math.log(6*in/mean))));
	}
	public boolean isCalibrated() { return calibrated; }
	public boolean isPlaying() { return playing; }
	public boolean labels() { return labels; }
	
	public void showMessages(boolean s) { showMessages = s; }
	public int getMBoxX() { return mBoxX; }
	public int getMBoxY() { return mBoxY; }
	public int getMBoxW() { return mBoxW; }
	public int getMBoxH() { return mBoxH; }
	
	public void setColor(int i, Color c) {
		switch (i) {
		case 0: loColor = c; break;
		case 1: hiColor = c; break;
		case 2: zeroColor = c; break;
		}
	}
	public Color getColor(int i) {
		switch (i) {
		case 0: return loColor; 
		case 1: return hiColor; 
		case 2: return zeroColor;
		} return null;
	}
	
	public HexDetector() {
		ltr = new TextRenderer(Data.getFont(), false, true);
		gtr = new TextRenderer(Data.getFont(14), false, true);
		
		messages = new ArrayList<String>();
		sendMessage("Running.");
		
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
				
				float[] verts = v[curr.getType(index).ordinal()];
				
				if (index == highlighted) {
					gl2.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
					gl2.glBegin(GL2.GL_TRIANGLE_FAN);
					for (int j = 0; j < verts.length; j+=3) {
						gl2.glVertex3f(verts[j] * 1.15f, verts[j+1] * 1.15f, verts[j+2] - .05f);
					}
					gl2.glEnd();
				}
				
				if (layerIndex == 0) {
					float[] hi, lo;
					hi = hiColor.getColorComponents(null);
					lo = loColor.getColorComponents(null);
					
					if (calibrated && dispMode == 1) {
						float c = (float) (SCALE_FACTOR*(Math.abs(trueData[index][currBucket] - means[index][currBucket]))/variances[index][currBucket]);
						gl2.glColor4f((c*hi[0] + (1-c)*lo[0]), (c*hi[1] + (1-c)*lo[1]), (c*hi[2] + (1-c)*lo[2]), alpha);
					} else {
						float c = trueData[index][currBucket];
						if (c < 12) gl2.glColor4f(0.1f, 0.1f, 0.1f, alpha); 
						else { 
							c = scale(c);
							gl2.glColor4f((c*hi[0] + (1-c)*lo[0]), (c*hi[1] + (1-c)*lo[1]), (c*hi[2] + (1-c)*lo[2]), alpha);
						}
					}
					if (trueData[index][currBucket] == 0) {
						float[] bad = zeroColor.getColorComponents(null);
						gl2.glColor4f(bad[0], bad[1], bad[2], alpha);
					}
					
				} else {
					gl2.glColor4f(1, 1, 1, alpha);
				}
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				
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
					if (!calibrated) labelMode = (labelMode == 1) || (labelMode == 3) ? labelMode : 1;
					switch (labelMode) {
					case 0: h = String.valueOf((int)(trueData[index][currBucket] - means[index][currBucket])); break;
					case 1: h = String.valueOf((int)(trueData[index][currBucket])); break;
					case 2: h = String.valueOf((int)(100*(trueData[index][currBucket] - means[index][currBucket])/variances[index][currBucket])); break;
					case 3: h = String.valueOf(index); break;
					case 4: h = String.valueOf((int)(trueData[index][currBucket] - calibMins[index][currBucket])); break;
					
					}
					float yy = y[index]/850 - 0.33f;
					float xx = x[index]/850 - 0.30f*h.length();
					if (curr.getType(index) == tileType.SPLIT_RIGHT) {
						yy += 0.75f;
					} else if(curr.getType(index) == tileType.SPLIT_LEFT) {
						yy -= 0.92f;
					}
					
					ltr.draw3D(h, xx, yy, 0.2f + depth, 0.05f);
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
		if (showMessages) mBoxH += .2f*(110 - mBoxH); 
		else mBoxH += .2f*(25 - mBoxH);
		
		
		mBoxX = w/2 - mBoxW/2;
		mBoxY = h - mBoxH - 8;
		
		if ((showMessages || newMessage) && mBoxAlpha < 1.0f) {
			mBoxAlpha += 0.1f * (1 - mBoxAlpha);
		} else if (!(showMessages || newMessage)) {
			mBoxAlpha = mBoxAlpha > 0.001f ? mBoxAlpha - 0.075f * (1.01f - mBoxAlpha) : 0;
		} 
		
		if (mCounter > 0 && newMessage) mCounter--;
		else {
			newMessage = false;
			mCounter = 115;
		}
		
		if (mBoxAlpha > 0.001f) {
			gl2.glMatrixMode(GL2.GL_PROJECTION);
			gl2.glPushMatrix();
			gl2.glLoadIdentity();
			gl2.glOrtho(0, w, h, 0, 0, 1);
			gl2.glColor4f(1.0f, 1.0f, 1.0f, mBoxAlpha * 0.65f);
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glVertex2f(mBoxX, mBoxY + mBoxH);
			gl2.glVertex2f(mBoxX + mBoxW, mBoxY + mBoxH);
			gl2.glVertex2f(mBoxX + mBoxW, mBoxY);
			gl2.glVertex2f(mBoxX, mBoxY);
			gl2.glEnd();
			gl2.glPopMatrix();
			gl2.glMatrixMode(GL2.GL_MODELVIEW);
		}
		
		gtr.beginRendering(w, h);
		gtr.setSmoothing(false);
		gtr.setColor(0.0f, 0.0f, 0.0f, mBoxAlpha);
		int j = showMessages ? mBoxH/25 : 1;
		for (int i = 0; i < j && i < messages.size(); i++) {
			gtr.draw(messages.get(i), mBoxX + 4, (25*(i)) + 16);
		}
		gtr.flush();
		gtr.setColor(1.0f, 1.0f, 1.0f, 1.0f);
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
				case 3: gtr.draw("label mode = indices", 5, 35 + offY); break;
				case 4: gtr.draw("label mode = ADC-min", 5, 35 + offY); break;
				}
			}
			if (calibrated) {
				float[] lo = loColor.getColorComponents(null);
				float[] hi = hiColor.getColorComponents(null);
				float[] zero = zeroColor.getColorComponents(null);
				
				gtr.draw("scale factor = "+String.valueOf(SCALE_FACTOR), 5, 35 + (labels ? 15 : 0) + (playing ? 15 : 0));
				gtr.draw("display mode = "+(dispMode > 0 ? "calib" : "abs"), 5, 50 + (labels ? 15 : 0) + (playing ? 15 : 0));
				gtr.setColor(lo[0], lo[1], lo[2], 1.0f);
				gtr.draw("low", 5, 65 + (labels ? 15 : 0) + (playing ? 15 : 0));
				gtr.setColor(hi[0], hi[1], hi[2], 1.0f);
				gtr.draw("high", 28, 65 + (labels ? 15 : 0) + (playing ? 15 : 0));
				gtr.setColor(zero[0], zero[1], zero[2], 1.0f);
				gtr.draw("zero", 57, 65 + (labels ? 15 : 0) + (playing ? 15 : 0));
			}
			gtr.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			gtr.draw("f: " + fileName, 5, h - 15);
			gtr.draw("c: " + calibFileName, 5, h - 30);
		}
		gtr.flush();
		gtr.endRendering();
		
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
			xOffset += (targetXOffset - xOffset) * 0.13f;
			yOffset += (targetYOffset - yOffset) * 0.13f;
		}
		
		if (playing && kpixReader != null && kpixReader.hasNextRecord()) {
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
	
	public void moveAxis(float dx, float dy) {
		targetXOffset += dx;
		targetYOffset += dy;
	}
	public void setAxisPosition(float x, float y) {
		targetXOffset = x;
		targetYOffset = y;
	}
	
	public void resetOrientation() {
		targetPitch = targetYaw = 0;
		targetXOffset = targetYOffset = 0;
	}
	
	public float getAxisDepth() { return 40 - active * 40; }
	
	public void zoom(float dz) { targetZoom += dz; }
	public void setZoom(float z) { targetZoom = z; }
	
	public void highlightPixel(int index) {
		if (index > 0 && index < 1024) highlighted = index;
	}
	
//	public void setData(float[][] d) {
//		trueData = d[0];
//		data = new int[d[1].length];
//		Arrays.fill(data, 0);
//		for (int i = 0; i < data.length; i++) {
//			data[i] = (int)d[1][i];
//		}
//		
//	}
	
	public void setKpixReader(KpixFileReader r) { 
		if (r == null) return;
		kpixReader = r;
		fileName = r.getName();
		filePath = r.getPath();
		readerIndex = 0;
		timeStamp = 0;
		dataBuf = new ArrayList<Pair<float[][], int[][]>>();
		indexBuf = new ArrayList<Integer>();
		stepData();
	}
	
	public void stepData() { 
		if (kpixReader == null) return;
		if (marker < dataBuf.size() - 1) {
			marker++;
			Pair<float[][], int[][]> d = dataBuf.get(marker);
			trueData = d.x;
			tTimes = d.y;
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
	        trueData = new float[1024][4];
	        ListIterator<KpixSample> li = temp.listIterator();
	        KpixSample s = li.next();
	        
	        while (li.hasNext()) {
	        	s = li.next();
	        	if (s.getType() != KpixSample.KpixSampleType.KPIX) break;
	        	trueData[s.getChannel()][s.getBucket()] = s.getAdc();	//Using s.getChannel() multiple times per sample could be fussy
	        	tTimes[s.getChannel()][s.getBucket()] = s.getTime();
	        	
	        }

		} catch (Exception e) {
			System.out.println("Failed to step data forward.");
			sendMessage("Failed to step data forward.");
			playing = false;
			last = true;
			return;
		}
		last = false;
		
		dataBuf.add(new Pair<float[][], int[][]>(trueData, tTimes));
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
		Pair<float[][], int[][]> d = dataBuf.get(marker);
		trueData = d.x;
		tTimes = d.y;
		readerIndex = indexBuf.get(marker);
	}
	
	public void resetData() { 
		if (kpixReader == null) return;
		kpixReader.rewind();
		readerIndex = 0;
		marker = 0;
		dataBuf.clear();
		indexBuf.clear();
		System.out.println("Data record rewound.");
		sendMessage("Data record rewound.");
		stepData();
	}
	
	public void seek(int index) {
		if (kpixReader == null) return;
		if (readerIndex > index) {
			resetData();
			if (readerIndex > index) {
				sendMessage(String.format("Unable to seek index: %d.", index));
				return;
			}
		}
		while(kpixReader.hasNextRecord()) {
			if (readerIndex == index) return;
			stepData();
		}
		sendMessage(String.format("Unable to seek index: %d.", index));
	}
	
	public void calibrateData() { 
		if (kpixReader == null) return;
		try {
			System.out.print("Calibrating...");
			sendMessage("Calibrating...");
			double count = 0;
			for (int i = 0; i < 4; i++) {
				Arrays.fill(means[i], 0);
				Arrays.fill(variances[i], 0);
				Arrays.fill(calibMins[i], -1);
			}
			kpixReader.rewind();
			KpixRecord record = kpixReader.readRecord();
			count++;
			
	        while (kpixReader.hasNextRecord()) {
	        	record = kpixReader.readRecord();
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
		        	double delta =  adc - means[index][currBucket];
		        	means[index][currBucket] +=  delta/count;
		        	variances[index][currBucket] += delta*(adc - means[index][currBucket]);
		        	
		        	if (calibMins[index][currBucket] < 0 || adc < calibMins[index][currBucket]) calibMins[index][currBucket] = adc;
		        	
		        }
	        }
	        for (int i = 0; i < variances.length; i++) {
	        	variances[i][currBucket] = (float) Math.sqrt(variances[i][currBucket]/count);
	        }
	        

		} catch (Exception e) {
			System.out.println("Failed to calibrate data.");
			sendMessage("Failed to calibrate data.");
			calibrated = false;
			e.printStackTrace();
			return;
		}
		
		calibrated = true;
		calibFileName = kpixReader.getName();
		calibFilePath = kpixReader.getPath();
		dispMode = 1;
		kpixReader.rewind();
		readerIndex = 0;
		System.out.println(" done.");
		sendMessage("Calibrating... done!");
		stepData();
	}
	
	public String currFileName() { return fileName; }
	
	public void sendMessage(String mess) {
		int[] t = Data.getTime();
		
		messages.add(0, String.format("%02d:%02d:%02d ", t[0], t[1], t[2]) + mess);
		newMessage = true;
		if (messages.size() > 9) messages.remove(messages.size() - 1);
	}
	
	public void saveConfig(String fName) {
		String[] lines = new String[]{	
				calibFileName == fileName ? "browse : " + fileName : "browse : " + calibFilePath,
				"calibrate : " + (calibrated ? "true" : "false"),
				calibFileName == fileName ? "" : "browse : " + filePath,
				"live-update : " + (updateLive ? "true" : "false"),
				"adjusted : " + (adjusted ? "true" : "false"), 
				String.format("scale : %f", SCALE_FACTOR),
				"labels : " + (labels ? "true" : "false"),
				"label-type : " + new String[]{"delta", "ADC", "% delta", "indices", "ADC - min"}[labelMode],
				"display : " + (dispMode > 0 ? "calib" : "abs"), 
				"speed : " + String.valueOf(playSpeed * 10), 
				"zoom : " + String.valueOf(zoom),
				String.format("axis : %f, %f", -targetXOffset, targetYOffset)
		};
		Data.saveFile(lines, fName);
		sendMessage("Configuration file '"+fName+"' saved!");
	}
	
	
	public void dump(int n) {
		resetData();
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("frame pixel adc");
		int count = n < 1 ? n - 1 : 0;
		
		while (count < n) {
			for (int i = 0; i < 1024; i++) {
				for (int j = 0; j < 4; j++) {
					lines.add(String.format("%d %d %d %d\n", readerIndex, i, j, Math.round(trueData[j][i])));
				}
			}
			stepData();
			if (last) break;
			if (n > 0) count++;
		}
		
		Data.saveDumpFile(lines.toArray(new String[]{}), fileName.replace("bin", "txt"));
		sendMessage("Successfully dumped "+String.valueOf(n)+" events to file: "+fileName.replace("bin", "txt"));
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
