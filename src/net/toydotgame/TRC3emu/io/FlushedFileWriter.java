package net.toydotgame.TRC3emu.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

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
	
	// Overwrites https://docs.oracle.com/javase/8/docs/api/java/io/Writer.html#write-java.lang.String-
	public void write(String str) throws IOException {
		super.write(str);
		super.flush(); // For some reason the auto-flush when calling .close() doesn't work?
	}
	
	// Extend .write() functionality
	public void writeln(String str) throws IOException {
		this.write(str + "\n");
	}
}
