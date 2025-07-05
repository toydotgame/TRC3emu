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
	public FlushedFileWriter(String fileName) throws IOException {
		super(fileName);
	}
	public FlushedFileWriter(File file) throws IOException {
		super(file);
	}
	public FlushedFileWriter(FileDescriptor fd) {
		super(fd);
	}
	public FlushedFileWriter(String fileName, boolean append) throws IOException {
		super(fileName, append);
	}
	public FlushedFileWriter(File file, boolean append) throws IOException {
		super(file, append);
	}
	
	/**
	 * Override for {@code write(String)} only. Calls {@code write()} followed by
	 * a {@code flush()}
	 * @param str String to be written
	 * @throws IOException If an I/O error occurs
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
	 * @throws IOException If an I/O error occurs
	 * @see FlushedFileWriter#write(String)
	 */
	public void writeln(String str) throws IOException {
		this.write(str + "\n");
	}
}
