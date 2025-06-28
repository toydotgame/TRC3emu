package net.toydotgame.TRC3emu;

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import net.toydotgame.TRC3emu.assembler.Assembler;
import net.toydotgame.io.FileHandler;

public class Main {
	// Cmdline args yield these settings
	private static final int ASSEMBLE = 0;
	private static final int EMULATE = 1;
	private static final int LINK = 2;
	private static final int ASSEMBLE_AND_LINK = 3;
	private static final int EXPERIMENTAL_ASSEMBLE = 4;
	public static int mode = -1;
	public static boolean verbose = false;
	private static String inputPath;
	private static String outputPath; // TODO: Remove
	
	public static void main(String[] args) {
		// Setup options and check them immediately, storing values into variables:
		Options options = setupOptions();
		checkOptions(options, args); // Exits with return code 1 if flags are invalid
		// TODO: Nicer logging implementation for this damn thing
		
		switch(mode) {
			case ASSEMBLE_AND_LINK:
			case ASSEMBLE:
				assemble();
				if(mode == ASSEMBLE) break;
				// outputPath is blindly the extension-stripped value of inputPath
				inputPath = outputPath + ".o";
			case LINK:
				link();
				break;
			case EMULATE:
				emulate();
				break;
			case EXPERIMENTAL_ASSEMBLE:
				experimentalAssemble();
				break;
			default:
				Log.exit("Unknown mode \""+mode+"\"!");
		}
	}
	
	private static Options setupOptions() {
		Options options = new Options();
		
		OptionGroup mode = new OptionGroup();
		Option assemble = Option.builder("a")
			.longOpt("assemble")
			.desc("Assemble a TRC3 assembly source file.")
			.hasArg().argName("source")
			.build();
		Option link = Option.builder("l")
			.longOpt("link")
			.desc("Link an object file to create a binary.")
			.hasArg().argName("object")
			.build();
		Option assembleAndLink = Option.builder("c")
			.longOpt("assemble-and-link")
			.desc("Same as TRC3emu -a <file> && TRCemu -l <file>. Assembles a source to a binary.")
			.hasArg().argName("source")
			.build();
		Option emulate = Option.builder("e")
			.longOpt("emulate")
			.desc("Emulate a previously created binary.")
			.hasArg()
			.argName("binary")
			.build();
		mode.addOption(assemble);
		mode.addOption(link);
		mode.addOption(assembleAndLink);
		mode.addOption(emulate);
		mode.setRequired(true);
		
		Option verbose = new Option("v",
			"verbose",
			false,
			"Print detailed step-by-step information."
		);
		
		Option output = Option.builder("o")
			.longOpt("output")
			.desc("(Optional) Output binary file. This option is ignored when -e, --emulate is set. Defaults to a .o/.bin of the same name as the input source file. Specified extensions are included, but will always have .o/.bin appended.")
			.hasArg().argName("destination")
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
			
			if(cmdline.hasOption("a") || cmdline.hasOption("l") || cmdline.hasOption("c")) {
				if(cmdline.hasOption("a")) {
					mode = EXPERIMENTAL_ASSEMBLE;
					inputPath = cmdline.getOptionValue("a");
				} else if(cmdline.hasOption("l")) {
					mode = LINK;
					inputPath = cmdline.getOptionValue("l");
				} else {
					mode = ASSEMBLE_AND_LINK;
					inputPath = cmdline.getOptionValue("c");
				}
				
				if(cmdline.hasOption("o"))
					outputPath = cmdline.getOptionValue("o");
				else
					outputPath = inputPath.split("\\.", 2)[0];
			} else if(cmdline.hasOption("e")) {
				mode = EMULATE;
				inputPath = cmdline.getOptionValue("e");
			}
			
			if(cmdline.hasOption("v")) Log.setLogLevel(Log.VERBOSE);
		} catch(ParseException e) {
			HelpFormatter help = new HelpFormatter();
			
			System.err.println(e.getMessage());
			help.printHelp("TRC3emu.jar <-a | -l | -c | -e <file>> [-v] [-o <output>]", options);
			
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
		List<String> instructionList = OldAssembler.main(lines);
		OldUtils.verboseLog(lines.size() + " lines read => " + instructionList.size() + " instructions");
		OldUtils.verboseLog(OldAssembler.aliases.size()-8 + " aliases defined.");
		OldUtils.printAssembly(lines, instructionList); // Won't print without -v
		
		if(OldAssembler.syntaxErrors > 0) {
			System.err.println(OldAssembler.syntaxErrors + " errors present. Output will not be written.");
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
	
	private static void experimentalAssemble() {
		Log.log("Assembling...");
		
		// Read file into list:
		FileHandler input = new FileHandler(inputPath);
		List<String> source = input.readIntoList();
		
		// Assemble:
		List<String> binary = Assembler.main(source);
		
		// Quit if the assembler gave us no data:
		if(binary.size() == 0) Log.exit("Output binary is 0 bytes!");
		
		// If there _is_ data, write out:
		String outputPath = inputPath.split("\\.", 2)[0]+".bin";
		FileHandler output = new FileHandler(outputPath, FileHandler.WRITE);
		output.writeList(binary);
		
		Log.log("Done!");
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

		if(binary.size() == 0) {
			System.err.println("Output binary is empty!");
			System.exit(1);
		}		
		
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
