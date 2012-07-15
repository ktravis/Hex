package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Data {
	
	
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
}
