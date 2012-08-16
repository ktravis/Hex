package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import java.awt.Font;

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
			System.out.printf("Could not read file '%s', file not found.\n", path);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.printf("Could not read file '%s', IOException encountered.\n", path);
			e.printStackTrace();
		}

		System.out.print(" done.\n");
		
		return lines;
	}
	
	public static KpixFileReader readKpixDataFile(String path) {
		try {
			System.out.printf("Reading Binary Datafile '%s'...\n", path);
			return new KpixFileReader(new File(path));
		} catch (FileNotFoundException e) {
			System.out.printf("File '%s' not found.", path);
		} catch (IOException e) {
			System.out.printf("Error reading file '%s'.", path);
			e.printStackTrace();
		}
		return null;
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
	
	public static float[][] parseData(String path) {
		return parseData(path, false, 0, 0);
	}
	
	public static float[][] parseData(String path, boolean absolute, float l, float h) {
		float[] data = new float[1024];
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
		return new float[][] {vals, data};
	}
	
	public static int[] parseData(int[] in) {
		if (in == null) return new int[1024]; 
		int[] d = new int[1024];
		float min = in[0], max = in[0];
		
		for (int f : in) {
			if (f < min) min = f;
			else if (f > max) max = f;
		}
		
		for (int i = 0; i < 1024 && i < in.length; i++) {
			d[i] = scale(in[i], min, max);
		}
		return d;
	}
	public static int[] parseData(float[] in) {
		if (in == null) return new int[1024]; 
		int[] d = new int[1024];
		float min = in[0], max = in[0];
		
		float avg = 0;
		
		for (float f : in) {
			if (f < min) min = f;
			else if (f > max) max = f;
			avg += f;
		}
		
		avg /= 1024;
		
		for (int i = 0; i < 1024 && i < in.length; i++) {
			d[i] = scale(in[i], min, max);
		}
		return d;
	}
	public static int scale(float val, float low, float high) {
		return (int) (255*(val - low)/(high - low));
	}
	public static float[] setScaling(float[] in) {
		float sum = 0, high = 0;
		for (float f : in) {
			if (f > high) high = f;
			sum += f;
		}
		int n = in.length;
		float mean = sum/n;
		
		return new float[] { high,  mean};  
	}
	public static float getMean(float[] in) {
		float sum = 0;
		for (float f : in) {
			sum += f;
		}
		int n = in.length;
		return sum/n;
	}
	
	public static Font getFont(int... size) {
		Font f = new Font("Arial", Font.BOLD, size.length > 0 ? size[0] : 24);
		return f;
	}
	
	public static FloatBuffer asFloatBuffer(float... args) {
		FloatBuffer buffer = FloatBuffer.allocate(args.length * 4);
		buffer.put(args);
		buffer.flip();
		return buffer;
	}
	
}
