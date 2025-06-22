package net.toydotgame.TRC3emu;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("No arguments supplied!");
			System.exit(1);
		}
		
		switch(args[0]) {
			case "-h":
			case "--help":
				printHelp();
				break;
			case "-a":
			case "--assemble":
				assemble(args[1]);
				break;
			case "-e":
			case "--emulate":
				emulate(args[1]);
				break;
			default:
				System.err.println("Invalid argument \"" + args[0] + "\"! Try --help for help.");
				System.exit(1);
		}
	}
	
	private static void printHelp() {
		System.out.println("AVAILABLE OPTIONS:\n\n"
			+ "\t-a, --assemble <file>\n"
			+ "\t\tAssembles a TRC3 assembly file to a binary of the same name.\n"
			+ "\n"
			+ "\t-h, --help\n"
			+ "\t\tPrints this help message.\n"
			+ "\n"
			+ "\t-e, --emulate <file>\n"
			+ "\t\tEmulates the execution of provided assembled executable as if it were loaded into memory."
		);
	}
	
	private static void assemble(String filePath) {
		System.out.print("Initialising assembler...");
		
		// Setup scanner to read input file
		File inputFile = new File(filePath);
		Scanner input = null;
		try {
			input = new Scanner(inputFile);
		} catch (FileNotFoundException e) {
			System.err.println("\nCouldn't find \"" + filePath + "\"!");
			System.exit(1);
		}
		
		// Create list of each line of the file to be fed into the assembler
		List<String> lines = new ArrayList<String>();
		while(input.hasNextLine()) {
			String line = input.nextLine().replaceAll("\\s+", " ").trim();
			lines.add(line);
		}
		
		System.out.println(" Done!"); // Init complete
		
		List<String> binary = Assembler.assemble(lines);
		
		System.out.println("Assembled binary:");
		System.out.println(binary);
		
		// TODO: File output
	}
	
	private static void emulate(String filePath) {
		System.out.println("Emulator was called to run " + filePath + ". Currently unimplemented.");
		// TODO: Implement
	}
}
