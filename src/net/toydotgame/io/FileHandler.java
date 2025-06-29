package net.toydotgame.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import net.toydotgame.TRC3emu.Log;

/**
 * Wrapper for the {@link java.util.Scanner} and {@link FlushedFileWriter} classes
 * to provide methods to read into {@code List}s and write {@code List}s to files
 * @see #FileHandler(String, int)
 */
public class FileHandler {
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
					Log.fatalError("Couldn't find file \""+this.name+"\"!", 1);
				}
				break;
			case WRITE:
				try {
					this.fileWriter = setupWriter(path);
				} catch (IOException e) {
					Log.exit("Error opening \""+this.name+"\" for writing!");
				}
				break;
			default:
				Log.exit("Invalid mode \""+this.mode+"\" for new FileHandler!");
		}
		
		Log.debug("Created new FileHandler for \""+this.name+"\" (mode="+this.mode+")");
	}
	/**
	 * Creates a read-only {@link FileHandler} instance
	 * @see FileHandler#FileHandler(String, int)
	 */
	public FileHandler(String path) {
		this(path, READ);
	}
	
	private FlushedFileWriter setupWriter(String path) throws IOException {
		if(!this.file.createNewFile())
			Log.log(this.name+" already exists! Overwriting it anyway");

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
			Log.exit("Tried to write to a non-writable FileHandler! (File \""+this.name+"\")");
		
		try {
			for(String i : list)
				this.fileWriter.writeln(i);
			this.fileWriter.close();
		} catch(IOException e) {
			Log.exit("Couldn't write to "+this.name+"! This writer may possibly be closed");
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
			Log.exit("Tried to read from a non-readable FileHandler! (File \""+this.name+"\")");
		
		List<String> list = new ArrayList<String>();
		while(this.scanner.hasNextLine()) {
			// Lint lines to be kind:
			String line = this.scanner.nextLine().replaceAll("\\s+", " ").trim();
			list.add(line);
		}
		
		return list;
	}
}
