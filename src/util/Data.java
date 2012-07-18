package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;


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
	
	
	public static FloatBuffer asFloatBuffer(float... args) {
		FloatBuffer buffer = FloatBuffer.allocate(args.length * 4);
		buffer.put(args);
		buffer.flip();
		return buffer;
	}
	
}
