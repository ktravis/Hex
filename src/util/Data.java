package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;


public class Data {
	public static final String DEFAULT_CAPTURE_PATH = "out/screenshots/";
	public static final boolean NO_TGA = true;
	
	public static String[] fileRead(String path) {
		System.out.print(String.format("Reading file '%s'...", path));
		
		String[] lines = null;
		ArrayList<String> linearray = new ArrayList<String>();
		
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String line = reader.readLine();
			
			while (line != null) {
				linearray.add(line);
				line = reader.readLine();
			}
			
			lines = new String[linearray.size()];
			
			linearray.toArray(lines);
			
			reader.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("Could not read file, file not found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Could not read file, IOException encountered.");
			e.printStackTrace();
		}

		System.out.print(" done.\n");
		
		return lines;
	}
	
	public static File getCaptureFile(boolean checkDir) {
		File captureDir = new File(DEFAULT_CAPTURE_PATH);
		
		if (!checkDir) {
			if (!captureDir.exists()) {
				System.out.print(String.format("Creating directory '%s'...", DEFAULT_CAPTURE_PATH));
				if (captureDir.mkdirs()) { 
					System.out.print(" done.\n");
				} else {
					System.out.print(" failed.\n");
					return null;
				}
			}
		}
		int n = captureDir.list().length;
		
		if (!NO_TGA) {
			return new File(String.format("%sCapture-%d.tga", DEFAULT_CAPTURE_PATH, n));
		} else {
			return new File(String.format("%sCapture-%d.png", DEFAULT_CAPTURE_PATH, n));
		}
	}
	
	public static int[] parseData(String path) {
		return parseData(path, false, 0, 0);
	}
	
	public static int[] parseData(String path, boolean absolute, float l, float h) {
		int[] data = new int[1024];
		Arrays.fill(data, 0);
		String[] lines = fileRead(path);
		
		float low, high;
		boolean lowSet;
		
		if (!absolute) {
			lowSet = false;
			low = 0;
			high = 0;
		} else {
			lowSet = true;
			low = l;
			high = h;
		}
		
		float[] vals = new float[1024];
		Arrays.fill(vals, 0.0f);
		String[] splitLine;
		
		for (String s : lines) {
			splitLine = s.split("[\t]");
			if (splitLine[2].contains("NG")) {
				data[Integer.valueOf(splitLine[1]) - 1] = 256;	//256 is out of 0-255 color range, used to flag bad pixel
			} else {
				float f = Float.valueOf(splitLine[2].trim());
				vals[Integer.valueOf(splitLine[1]) - 1] = f;
				if (f > high) high = f;
				else if (f < low || !lowSet) {
					low = f;
					lowSet = true;
				}
			}
		}
		System.out.println(String.format("Minimum: %f  Maximum: %f", low, high));
		for (int i = 0; i < 1024; i++) {
			float f = vals[i];
			if (f == 0) {
				if (data[i] == 0) data[i] = 257; //flags data as not reported
				continue;
			} 				
			data[i] = scale(f, low, high);
		}
		return data;
	}
	
	public static int scale(float val, float low, float high) {
//		int h = 0x000;
//		
//		int g = (int) (255*(val - low)/(high - low));
//		
//		h = g << 16 + 255 - g;
		
		int h = (int) (255*(val - low)/(high - low));
		
		return h;
	}
	
	
	public static FloatBuffer asFloatBuffer(float... args) {
		FloatBuffer buffer = FloatBuffer.allocate(args.length * 4);
		buffer.put(args);
		buffer.flip();
		return buffer;
	}
	
}
