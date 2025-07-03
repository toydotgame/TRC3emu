package net.toydotgame.TRC3emu;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.Clip;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import net.toydotgame.TRC3emu.assembler.Assembler;
import net.toydotgame.TRC3emu.emulator.Emulator;
import net.toydotgame.utils.FileHandler;
import net.toydotgame.utils.Log;

public class Main {
	// Cmdline args yield these settings
	private static final int ASSEMBLE = 0;
	private static final int EMULATE = 1;
	private static final int HELP = 2;
	public static int mode = -1;
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
			case EMULATE:
				emulate();
				break;
			case HELP:
				help(options);
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
		Option emulate = Option.builder("e")
			.longOpt("emulate")
			.desc("Emulate a previously created binary.")
			.hasArg().argName("binary")
			.build();
		Option help = Option.builder("h")
			.longOpt("help")
			.desc("Print help message.")
			.build();
		mode.addOption(assemble);
		mode.addOption(emulate);
		mode.addOption(help);
		mode.setRequired(true);
		
		Option verbose = new Option("v",
			"verbose",
			false,
			"Print detailed step-by-step information. If -t, --terminal mode is set, then"
			+"this option is always overridden to be false."
		);
		
		Option terminal = new Option("t",
			"terminal",
			false,
			"(Optional) Spawns a window where GPI/GPO instructions input/output from/to."
			+"This option is ignored when -a, --assemble is set."
		);
		
		Option output = Option.builder("o")
			.longOpt("output")
			.desc("(Optional) Output binary file. This option is ignored when -e, --emulate is set."
				+"Defaults to a .bin of the same name as the input source file. Specified extensions"
				+"are included, but will always have .bin appended."
			)
			.hasArg().argName("destination")
			.build();
		
		options.addOptionGroup(mode);
		options.addOption(verbose);
		options.addOption(terminal);
		options.addOption(output);
		return options;
	}
	
	private static void checkOptions(Options options, String[] args) {
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmdline = parser.parse(options, args);
			
			if(cmdline.hasOption("a")) {
				mode = ASSEMBLE;
				inputPath = cmdline.getOptionValue("a");
				
				if(cmdline.hasOption("o"))
					outputPath = cmdline.getOptionValue("o")
						.split("\\.", 2)[0]+".bin";
				else outputPath = inputPath.split("\\.", 2)[0]+".bin";
			} else if(cmdline.hasOption("e")) {
				mode = EMULATE;
				inputPath = cmdline.getOptionValue("e");
				
				Emulator.terminalMode = cmdline.hasOption("t");
			} else if(cmdline.hasOption("h")) {
				mode = HELP;
			}
			
			if(cmdline.hasOption("v")) Log.setLogLevel(Log.VERBOSE);
		} catch(ParseException e) {
			System.err.println(e.getMessage());
			help(options);			
			System.exit(1);
		}
	}
	
	private static void assemble() {
		Log.log("Assembling...");
		
		// Read file into list:
		FileHandler input = new FileHandler(inputPath);
		List<String> source = input.readIntoList();
		
		// Assemble:
		List<String> binary = Assembler.main(source);
		
		// Quit if the assembler gave us no data:
		if(binary.size() == 0) Log.exit("Output binary is 0 bytes!");
		
		// If there _is_ data, write out:
		FileHandler output = new FileHandler(outputPath, FileHandler.WRITE);
		output.writeList(binary);
		
		Log.log("Done!");
	}
	
	private static void emulate() {
		if(Emulator.terminalMode) Log.log("Running emulator in terminal mode...");
		else Log.log("Running emulator...");
		
		// Read file and create rudimentary memory map:
		List<String> binary = new FileHandler(inputPath).readIntoList();
		if(binary.size() > 2048)
			Log.exit("Input binary won't fit into memory!", 1);
		// Initialise empty fixed-size memory map:
		List<Integer> memory = Arrays.asList(new Integer[2048]);
		Collections.fill(memory, 0);
		
		// Read into memory:
		int bytesRead = 0;
		for(int i = 0; i < binary.size(); i++) {
			String[] binaryLine = binary.get(i).split(" ", 3);
			String word;
			if(binaryLine.length == 1) word = binaryLine[0]; // Normal binary
			else word = binaryLine[1];                       // Verbose binary
			
			try {
				int value = Integer.parseInt(word, 2);
				if(value < 0 || value > 255) throw new NumberFormatException();
				memory.set(i, value);
			} catch(NumberFormatException e) {
				Log.exit("Mangled binary input!", 1);
			}
			bytesRead++;
		}
		Log.debug(bytesRead+" bytes read into memory.");
		
		// Pass memory map into emulator: This is the end of what we need to do
		Emulator.main(memory);
		
		stallUntilAudioDone(Emulator.bell);
		Log.log("Emulator halted!");
		if(Emulator.terminalMode) Emulator.termMan.halt(); 
	}
	
	/**
	 * Stall with a busy-loop if {@link Emulator#bell} is found to be playing
	 * @param audioPlaying Pass in the value of {@link Emulator#bell}{@link
	 * javax.sound.sampled.DataLine#isRunning() .isRunning()}
	 * @see Emulator#bell()
	 */
	private static void stallUntilAudioDone(Clip clip) {
		if(clip == null) return; // Bell was never called
		
		while(clip.isRunning()) { // Busy loop is HORRID I know, but by this point we have literally nothing else to do
			try {
				Thread.sleep(1); // At least pace ourselves to avoid resource starvation or idk
			} catch (InterruptedException e) {} // If we are interrupted, so be it
		}
	}

	private static void help(Options options) {
		HelpFormatter help = new HelpFormatter();
		
		help.printHelp("TRC3emu.jar <[-a | -e <file>] | -h> [-v] [-o <output>]", options);
	}
}
