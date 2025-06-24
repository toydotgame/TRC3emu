package net.toydotgame.TRC3emu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import net.toydotgame.io.FlushedFileWriter;

public class Main {
	// Cmdline args yield these settings
	private static final int ASSEMBLE = 0;
	private static final int EMULATE = 1;
	private static final int LINK = 2;
	private static final int ASSEMBLE_AND_LINK = 3;
	public static int mode = -1;
	public static boolean verbose = false;
	private static String inputPath;
	private static String outputPath;
	
	public static void main(String[] args) {
		// Setup options and check them immediately, storing values into variables:
		Options options = setupOptions();
		checkOptions(options, args); // Exits with return code 1 if flags are invalid
		
		switch(mode) {
			case ASSEMBLE:
				assemble();
				break;
			case LINK:
				link();
				break;
			case EMULATE:
				emulate();
				break;
			default:
				System.err.println("Unknown mode " + mode + "! Exiting...");
				System.exit(2);
		}
	}
	
	private static Options setupOptions() {
		Options options = new Options();
		
		OptionGroup mode = new OptionGroup();
		//Option verbose = new Option("a", "assemble", false, "Assemble a TRC3 assembly source file.");
		Option assemble = Option.builder("a")
			.longOpt("assemble")
			.desc("Assemble a TRC3 assembly source file.")
			.hasArg()
			.argName("source")
			.build();
		Option link = Option.builder("l")
			.longOpt("link")
			.desc("Link an object file to create a binary.")
			.hasArg()
			.argName("object")
			.build();
		// TODO: -c, --assemble-and-link to call both
		Option emulate = Option.builder("e")
			.longOpt("emulate")
			.desc("Emulate a previously created binary.")
			.hasArg()
			.argName("binary")
			.build();
		mode.addOption(assemble);
		mode.addOption(link);
		mode.addOption(emulate);
		mode.setRequired(true);
		
		Option verbose = new Option("v",
			"verbose",
			false,
			"Print detailed step-by-step information."
		);
		
		Option output = Option.builder("o")
			.longOpt("output")
			.desc("(Optional) Output binary file. This option is ignored when -a, --assemble is not set. Defaults to a .o/.bin of the same name as the input source file. Specified extensions are included, but will always have .o/.bin appended.")
			.hasArg()
			.argName("destination")
			.build();
		
		options.addOptionGroup(mode);
		options.addOption(verbose);
		options.addOption(output);
		return options;
	}
	
	private static void checkOptions(Options options, String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdline = parser.parse(options, args);
			
			if(cmdline.hasOption("a") || cmdline.hasOption("l")) {
				if(cmdline.hasOption("a")) {
					mode = ASSEMBLE;
					inputPath = cmdline.getOptionValue("a");
				} else if(cmdline.hasOption("l")) {
					mode = LINK;
					inputPath = cmdline.getOptionValue("l");
				}
				
				if(cmdline.hasOption("o"))
					outputPath = cmdline.getOptionValue("o");
				else
					outputPath = inputPath.split("\\.", 2)[0];
			} else if(cmdline.hasOption("e")) {
				mode = EMULATE;
				inputPath = cmdline.getOptionValue("e");
			}
			
			if(cmdline.hasOption("v")) verbose = true;
		} catch(ParseException e) {
			HelpFormatter help = new HelpFormatter();
			
			System.err.println(e.getMessage());
			help.printHelp("TRC3emu.jar -<a|e> <file> [-v]", options);
			
			System.exit(1);
		}
	}
	
	private static void assemble() {
		System.out.print("Initialising assembler...");
		FileHandler input = new FileHandler(inputPath, FileHandler.READ);
		// Read file into String List
		List<String> lines = input.readIntoList();
		System.out.println(" Done!");
		
		// Assemble:
		List<String> instructionList = Assembler.main(lines);
		Utils.verboseLog(lines.size() + " lines read => " + instructionList.size() + " instructions");
		Utils.verboseLog(Assembler.aliases.size()-8 + " aliases defined.");
		Utils.printAssembly(lines, instructionList); // Won't print without -v
		
		if(Assembler.syntaxErrors > 0) {
			System.err.println(Assembler.syntaxErrors + " errors present. Output will not be written.");
			System.exit(1);
		}
		if(instructionList.size() == 0) {
			System.err.println("Output object is empty!");
			System.exit(1);
		}
		
		// Write to output file:
		System.out.print("Writing...");
		FileHandler output = new FileHandler(outputPath + ".o", FileHandler.WRITE);
		output.writeList(instructionList);
		System.out.println(" Done!");
	}
	
	private static void link() {
		System.out.print("Initialising linker...");
		FileHandler input = new FileHandler(inputPath, FileHandler.READ);
		// Read file into String List
		List<String> lines = input.readIntoList();
		System.out.println(" Done!");
		
		// Do linker stuff and convert to binary
		List<String> binary = Linker.main(lines);
		// TODO: Check binary
		
		// Convert the list of 16-bit instructions and 8-bit values to an 8-bit
		// memory listing: Instructions are encoded as little endian
		//lines = Linker.malloc(lines);
		
		// Now, there are no errors, and the instructionList is a
		// valid stream of bytes
		
		/*Utils.verboseLog("Checking compiled binary alignment...");
		if(!Utils.checkBinary(lines)) {
			System.err.println("Binary is mangled! Bytes do not align or there are non-binary characters in the output.");
			System.exit(2);
		}
		
		int length = Utils.countBytes(lines);
		System.out.println("Completed binary size: " + length + " B");
		if(length == 0) {
			System.err.println("No binary exists to write!");
			System.exit(1);
		} else if(length > 2048)
			System.err.println("This binary exceeds the maximum space available in TRC3's RAM! You will not be able to run this in Minecraft.");*/
		
		// Write to output file:
		System.out.print("Writing...");
		FileHandler output = new FileHandler(outputPath + ".bin", FileHandler.WRITE);
		output.writeList(binary);
		System.out.println(" Done!");
	}
	
	private static void emulate() {
		System.out.println("Emulator called.");
		// TODO: Implement
	}
}
