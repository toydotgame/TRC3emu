package net.toydotgame.utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

/**
 * In some cases, {@link java.io.FileWriter} won't flush in the time after
 * {@link FileWriter#write(String)} and before {@link FileWriter#close()}. So,
 * this simple subclass {@code extends FileWriter} by overriding its
 * {@code #write(String)} method with one that automatically flushes after a write
 * @see FlushedFileWriter#write(String)
 * @see FlushedFileWriter#writeln(String)
 */
public class FlushedFileWriter extends FileWriter {
	// Auto-generated constructors for superclass (we don't need to change these):
	/**
	 * @see java.io.FileWriter#FileWriter(String)
	 * @param fileName
	 * @throws IOException
	 */
	public FlushedFileWriter(String fileName) throws IOException {
		super(fileName);
	}
	/**
	 * @see java.io.FileWriter#FileWriter(File)
	 * @param file
	 * @throws IOException
	 */
	public FlushedFileWriter(File file) throws IOException {
		super(file);
	}
	/**
	 * @see java.io.FileWriter#FileWriter(FileDescriptor)
	 * @param fd
	 */
	public FlushedFileWriter(FileDescriptor fd) {
		super(fd);
	}
	/**
	 * @see java.io.FileWriter#FileWriter(String, boolean)
	 * @param fileName
	 * @param append
	 * @throws IOException
	 */
	public FlushedFileWriter(String fileName, boolean append) throws IOException {
		super(fileName, append);
	}
	/**
	 * @see java.io.FileWriter#FileWriter(File, boolean)
	 * @param file
	 * @param append
	 * @throws IOException
	 */
	public FlushedFileWriter(File file, boolean append) throws IOException {
		super(file, append);
	}
	
	/**
	 * Override for {@code write(String)} only. Calls {@code write()} followed by
	 * a {@code flush()}
	 * @param str String to be written
	 * @throws IOException
	 * @see java.io.Writer#write(String)
	 * @see java.io.OutputStreamWriter#flush()
	 */
	public void write(String str) throws IOException {
		super.write(str);
		super.flush(); // For some reason the auto-flush when calling .close() doesn't work?
	}
	
	/**
	 * Equivalent of {@code write(str + "\n")}
	 * @param str String to be written
	 * @throws IOException
	 * @see FlushedFileWriter#write(String)
	 */
	public void writeln(String str) throws IOException {
		this.write(str + "\n");
	}
}
