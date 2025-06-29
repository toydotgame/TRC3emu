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
			.hasArg()
			.argName("binary")
			.build();
		mode.addOption(assemble);
		mode.addOption(emulate);
		mode.setRequired(true);
		
		Option verbose = new Option("v",
			"verbose",
			false,
			"Print detailed step-by-step information."
		);
		
		Option output = Option.builder("o")
			.longOpt("output")
			.desc("(Optional) Output binary file. This option is ignored when -e, --emulate is set. Defaults to a .bin of the same name as the input source file. Specified extensions are included, but will always have .bin appended.")
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
			}
			
			if(cmdline.hasOption("v")) Log.setLogLevel(Log.VERBOSE);
		} catch(ParseException e) {
			HelpFormatter help = new HelpFormatter();
			
			System.err.println(e.getMessage());
			help.printHelp("TRC3emu.jar <-a | -e <file>> [-v] [-o <output>]", options);
			
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
		System.out.println("Emulator called.");
		// TODO: Implement
	}
}
