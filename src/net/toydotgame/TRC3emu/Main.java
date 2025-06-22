package net.toydotgame.TRC3emu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.toydotgame.io.FlushedFileWriter;

public class Main {
	private static File inFile, outFile;
	private static Scanner input;
	private static FlushedFileWriter output;
	public static Map<String, Integer> instructions = loadInstructions();
	public static Map<String, Integer> aliases = loadAliases();
	public static int currentLine = 0;
	
	private static Map<String, Integer> loadInstructions() {
		String[] opcodes = {
			"NOP", "HLT", "ADD", "ADI",
			"SUB", "XOR", "XNO", "IOR",
			"NOR", "AND", "NAN", "RSH",
			"LDI", "JMP", "BEQ", "BNE",
			"BGT", "BLT", "CAL", "RET",
			"REA", "STO", "GPI", "GPO",
			"BEL", "PAS", "PAG"
		};
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i < opcodes.length; i++)
			map.put(opcodes[i], i);
		return map;
	}
	
	private static Map<String, Integer> loadAliases() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("r0", 0);
		map.put("r1", 1);
		map.put("r2", 2);
		map.put("r3", 3);
		map.put("r4", 4);
		map.put("r5", 5);
		map.put("r6", 6);
		map.put("r7", 7);
		return map;
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			System.err.println("No arguments supplied!");
			System.exit(1);
		}
		
		switch(args[0]) {
			case "-h":
			case "--help":
				System.out.println("AVAILABLE OPTIONS:\n\n"
				+ "\t-a, --assemble <file>\n"
				+ "\t\tAssembles a TRC3 assembly file to an object file of the same name.\n"
				+ "\n"
				+ "\t-h, --help\n"
				+ "\t\tPrints this help message.\n"
				+ "\n"
				+ "\t-e, --emulate <file>\n"
				+ "\t\tEmulates the execution of provided assembled executable as if it were loaded into memory."
				);
				break;
			case "-a":
			case "--assemble":
				try {
					assemble(args[1]);
				} catch (IOException e) {
					System.err.println("An error occured when trying to create, open, and/or write to the destination file!:");
					e.printStackTrace();
					System.exit(2);
				}
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
	
	public static void assemble(String filePath) throws IOException {
		// Setup input as a scanner to read lines, and output as a filewriter to write or completely overwrite the contents of the destination file
		try {
			inFile = new File(filePath);
			input = new Scanner(inFile);
		} catch(FileNotFoundException e) {
			System.err.println("Couldn't find the file \"" + filePath + "\" to assemble!");
			System.exit(1);
		}
		String outFilePath = filePath.split("\\.", 2)[0] + ".o";
		outFile = new File(outFilePath);
		if(outFile.createNewFile()) {
			System.out.println("Assembling to " + outFile.getName());
		} else {
			System.out.println(outFile.getName() + " already exists! It will be overwritten.");
		}
		output = new FlushedFileWriter(outFile);
				
		// Main assembly loop:
		while(input.hasNextLine()) {
			currentLine++; // Easier to pre-increment before code
			String[] line = input.nextLine().replaceAll("\\s+", " ").trim().split(" ");
			// Don't bother assembling empty lines in the source file.
			// Weird hack because they aren't null nor empty strings
			if(line[0].length() == 0) continue;
			
			List<String> outputLine = assembleLine(line);
			//String print = String.join(" ", outputLine);
			String print = InstructionAssembler.assembleInstruction(outputLine);
			
			printRow(print, String.join(" ", line));
			if(print.length() > 0) output.writeln(print);;
		}

		input.close();
		output.close();
	}
	
	private static List<String> assembleLine(String[] line) {
		List<String> outputLine = new ArrayList<String>();

		if(line[0].startsWith(";")) return outputLine; // Don't include comments
		
		// Only replace opcodes at index 0
		Integer opcode = instructions.get(line[0].toUpperCase());
		if(opcode != null) {
			line[0] = line[0].toUpperCase(); // Replace original opcode mnemonic with sanitised one for display
			outputLine.add(String.valueOf(opcode));
		} else outputLine.add(line[0]);

		for(int i = 1; i < line.length; i++) {
			if(line[i].startsWith(";")) break; // Don't include comments
			
			Integer replacement = aliases.get(line[i].toLowerCase());
			if(replacement != null) {
				line[i] = line[i].toLowerCase();
				outputLine.add(String.valueOf(replacement));
			} else outputLine.add(line[i]);
		}
		
		return outputLine;
	}
	
	public static void printRow(String col1, String col2) {
		if(col1 == null) col1 = "";
		if(col2 == null) col2 = "";
		
		int col1Width = 21; // Total number of spaces in left column + padding
		int padding = Math.max(0, col1Width-col1.length());
		
		System.out.print(col1);
		System.out.print(String.join("", Collections.nCopies(padding, " "))); // Hack creating an arr/collection of `padding` # of ' ' chars
		System.out.println(col2);
	}
	
	public static void emulate(String filePath) {
		
	}
}
