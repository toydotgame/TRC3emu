package net.toydotgame.TRC3emu;

import java.io.File;
import java.io.FileNotFoundException;
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

public class Main {
	// Cmdline args yield these settings
	private static final int ASSEMBLE = 0;
	private static final int EMULATE = 1;
	public static int mode = -1;
	public static boolean verbose = false;
	private static String inputPath;
	
	public static void main(String[] args) {
		// Setup options and check them immediately, storing values into variables:
		Options options = setupOptions();
		checkOptions(options, args); // Exits with return code 1 if flags are invalid
		// TODO: Output file option (optional)
		
		switch(mode) {
			case ASSEMBLE:
				assemble();
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
		Option emulate = Option.builder("e")
			.longOpt("emulate")
			.desc("Emulate a previously assembled binary.")
			.hasArg()
			.argName("binary")
			.build();
		mode.addOption(assemble);
		mode.addOption(emulate);
		mode.setRequired(true);
		
		Option verbose = new Option("v", "verbose", false, "Print detailed step-by-step information.");
		
		options.addOptionGroup(mode);
		options.addOption(verbose);
		return options;
	}
	
	private static void checkOptions(Options options, String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdline = parser.parse(options, args);
			
			if(cmdline.hasOption("a")) {
				mode = ASSEMBLE;
				inputPath = cmdline.getOptionValue("a");
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
		
		// Setup `input` Scanner to read the source file
		File inputFile = new File(inputPath);
		// Init to null to make compiler happy. If it's ever null, Scanner() has
		// failed so we catch that error and exit 1 anyway:
		Scanner input = null;
		try {
			input = new Scanner(inputFile);
		} catch(FileNotFoundException e) {
			System.err.println("\nCouldn't find file \"" + inputPath + "\"!");
			System.exit(1);
		}
		
		// Read file into String List
		List<String> lines = new ArrayList<String>();
		while(input.hasNextLine()) {
			String line = input.nextLine().replaceAll("\\s+", " ").trim(); // Lint lines to be kind
			lines.add(line); // Here, each `line` String is an instruction with its arguments separated by spaces
		}
		
		System.out.println(" Done!");
		
		// Assemble:
		List<String> instructionList = Assembler.main(lines);
		Utils.verboseLog(lines.size() + " lines read => " + instructionList.size() + " instructions.");
		
		Utils.printAssembly(lines, instructionList); // Won't print without -v
			
		
		Utils.verboseLog("Checking compiled binary alignment...");
		Utils.checkBinary(instructionList); // Raises syntaxErrors count
		
		if(Assembler.syntaxErrors > 0) {
			System.err.println(Assembler.syntaxErrors + " errors present. Output will not be written.");
			System.exit(1);
		}
		
		// Now, there are no errors, and the instructionList is a
		// valid stream of bytes
		
		int length = Utils.countBytes(instructionList);
		System.out.println("Completed binary size: " + length + " B");
		if(length == 0) {
			System.err.println("No binary exists to write!");
			System.exit(1);
		} else if(length > 2048)
			System.err.println("This binary exceeds the maximum space available in TRC3's RAM! You will not be able to run this in Minecraft.");
		
		// TODO: File output
	}
	
	private static void emulate() {
		System.out.println("Emulator called.");
		// TODO: Implement
	}
}
