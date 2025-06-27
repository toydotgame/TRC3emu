package net.toydotgame.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.toydotgame.TRC3emu.Utils;

/**
 * Wrapper for the {@link java.util.Scanner} and {@link FlushedFileWriter} classes
 * to provide methods to read into {@code List}s and write {@code List}s to files
 * @see #FileHandler(String, int)
 */
public class FileHandler extends Utils {
	// Instance fields:
	private File file;
	/**
	 * String {@link java.io.File#getName()} for this {@link FileHandler} instance
	 */
	public String name;
	private Scanner scanner = null;
	private FlushedFileWriter fileWriter;
	/**
	 * The mode of this {@link FileHandler} instance. Either {@link #READ} or
	 * {@link #WRITE}
	 */
	public int mode = -1;
	
	/**
	 * Constant to denote read-only {@link FileHandler} objects
	 */
	public static final int READ = 0;
	/**
	 * Constant to denote write-only {@link FileHanlder} objects
	 */
	public static final int WRITE = 1;

	/**
	 * Creates a new {@link FileHandler} instance
	 * @param path Relative path of file
	 * @param mode Either {@link #READ} or {@link #WRITE}
	 * @return FileHandler for given path
	 * @see FileHandler
	 * @see FileHandler#name
	 * @see FileHandler#mode
	 */
	public FileHandler(String path, int mode) {
		this.file = new File(path);
		this.name = this.file.getName();
		this.mode = mode;
		
		switch(this.mode) {
			case READ:
				// Init this.scanner to null to make compiler happy. If it's ever
				// null, Scanner() has failed so we catch that error and exit 1
				// anyway:
				try {
					this.scanner = new Scanner(this.file);
				} catch(FileNotFoundException e) {
					exit("Couldn't find file \"" + this.name + "\"!", 1);
				}
				break;
			case WRITE:
				try {
					this.fileWriter = setupWriter(path);
				} catch (IOException e) {
					exit("Error opening \"" + this.name + "\" for writing!");
				}
				break;
			default:
				exit("Invalid mode \"" + this.mode + "\" for new FileHandler!");
		}
	}
	
	private FlushedFileWriter setupWriter(String path) throws IOException {
		// This returns true/false if the file doesn't/does exist respectively:
		this.file.createNewFile();

		return new FlushedFileWriter(this.file);
	}
	
	/**
	 * Write {@code list} as {@code \n}-delimited lines to the file in this
	 * {@link FileHandler} instance. Will fatally exit if {@link FileHandler#mode}
	 * is not {@link #WRITE}
	 * @param list Lines to write
	 * @see FileHandler#mode
	 * @see FlushedFileWriter#writeln(String)
	 * @see FileHandler#readIntoList()
	 */
	public void writeList(List<String> list) {
		if(this.mode != WRITE)
			exit("Tried to write to a non-writable FileHandler! (File \"" + this.name + "\")");
		
		try {
			for(String i : list)
				this.fileWriter.writeln(i);
			this.fileWriter.close();
		} catch(IOException e) {
			exit("Couldn't write to " + this.name + "!");
		}
	}
	
	/**
	 * Reads the lines from the file in this {@link FileHandler} instance into a
	 * {@code List}. Will fatally exit if {@link FileHandler#mode} is not
	 * {@link FileHandler#READ}
	 * @return List of lines as {@link java.lang.String} objects
	 * @see FileHandler#writeList(List)
	 */
	public List<String> readIntoList() {
		if(this.mode != READ)
			exit("Tried to read from a non-readable FileHandler! (File \"" + this.name + "\")");
		
		List<String> list = new ArrayList<String>();
		while(this.scanner.hasNextLine()) {
			String line = this.scanner.nextLine().replaceAll("\\s+", " ").trim(); // Lint lines to be kind
			list.add(line); // Here, each `line` String is an instruction with its arguments separated by spaces
		}
		
		return list;
	}
	
	/**
	 * Prints an exception message and stack trace without throwing an error
	 * @param message Exception message
	 * @param exitCode Exit code to quit with
	 */
	private void exit(String message, int exitCode) {
		Exception e = new Exception(message);
		e.printStackTrace();
		System.exit(2);
	}
	/**
	 * Calls {@code exit(message, 2)}
	 * @see FileHandler#exit(String, int)
	 */
	private void exit(String message) {
		exit(message, 2);
	}
}
