package net.toydotgame.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.toydotgame.TRC3emu.Utils;

public class FileHandler extends Utils {
	private File file;
	private Scanner scanner = null;
	private FlushedFileWriter fileWriter;
	private int mode = -1;
	public static final int READ = 0;
	public static final int WRITE = 1;

	public FileHandler(String path, int mode) {
		this.file = new File(path);
		
		this.mode = mode;
		switch(this.mode) {
			case READ:
				// Init to null to make compiler happy. If it's ever null, Scanner() has
				// failed so we catch that error and exit 1 anyway:
				try {
					this.scanner = new Scanner(this.file);
				} catch(FileNotFoundException e) {
					System.err.println("\nCouldn't find file \"" + this.name() + "\"!");
					System.exit(1);
				}
				break;
			case WRITE:
				try {
					this.fileWriter = setupWriter(path);
				} catch (IOException e) {
					System.err.println("\nError opening \"" + this.name() + "\" for writing!");
					System.exit(2);
				}
				break;
			default:
				// Create new exception but don't throw it:
				Exception e = new Exception("\nInvalid mode \"" + this.mode + "\" for new FileHandler()!");
				e.printStackTrace();
				System.exit(2);
		}
	}
	
	private FlushedFileWriter setupWriter(String path) throws IOException {
		/*if(this.file.createNewFile())
			Utils.verboseLog("\nWriting to " + this.name() + ".");
		else
			System.err.println("\n" + this.name() + " already exists! It will be overwritten.");*/
		
		this.file.createNewFile();

		return new FlushedFileWriter(this.file);
	}
	
	public void writeList(List<String> list) {
		if(this.mode != WRITE) {
			System.err.println("\nTried to write to a non-writable FileHandler! (File \"" + this.name() + "\")");
			System.exit(2);
		}
		
		try {
			for(String i : list)
				this.fileWriter.writeln(i);
		} catch(IOException e) {
			System.err.println("\nCouldn't write to " + this.name() + "!");
			System.exit(2);
		}
	}
	
	public String name() {
		return this.file.getName();
	}
	
	public List<String> readIntoList() {
		if(this.mode != READ) {
			System.err.println("\nTried to read from a non-readable FileHandler! (File \"" + this.name() + "\")");
			System.exit(2);
		}
		
		List<String> list = new ArrayList<String>();
		while(this.scanner.hasNextLine()) {
			String line = this.scanner.nextLine().replaceAll("\\s+", " ").trim(); // Lint lines to be kind
			list.add(line); // Here, each `line` String is an instruction with its arguments separated by spaces
		}
		
		return list;
	}
}
