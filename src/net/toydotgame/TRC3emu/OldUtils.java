package net.toydotgame.TRC3emu;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldUtils {
	// Instruction types:
	public static final int NONE = 0;
	public static final int ALU = 1;
	public static final int IMM8_TO_REG = 2;
	public static final int IMM10 = 3;
	public static final int IMM3_TO_REG = 4;
	public static final int REG_TO_IMM3 = 5;
	public static final int IMM3_OR_REG = 6;
	public static final int REG_ONLY = 7;
	public static Map<Integer, Integer> instructionTypes = loadInstructionTypes(); // Map of opcode to its operand type
	private static Map<Integer, Integer> loadInstructionTypes() {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		map.put(0, NONE);
		map.put(1, NONE);
		map.put(2, ALU);
		map.put(3, IMM8_TO_REG);
		for(int i = 4; i <= 11; i++) map.put(i, ALU);
		map.put(12, IMM8_TO_REG);
		for(int i = 13; i <= 18; i++) map.put(i, IMM10);
		map.put(19, NONE);
		for(int i = 20; i <= 21; i++) map.put(i, ALU);
		map.put(22, IMM3_TO_REG);
		map.put(23, REG_TO_IMM3);
		map.put(24, NONE);
		map.put(25, IMM3_OR_REG);
		map.put(26, REG_ONLY);
		
		return map;
	}

	public static void verboseLog(String message) {
		if(!Main.verbose) return;
		
		System.out.println(message);
	}
	
	public static String nChars(int n, char charToUse) {
		String strToUse = String.valueOf(charToUse);
		if(n <= 0) return strToUse;

		//return String.format("%" + inputString.length() + "s", "").replace(" ", String.valueOf(charToUse));
		//return inputString.replaceAll(".", String.valueOf(charToUse));
		return String.join("", Collections.nCopies(n, strToUse));
	}
	
	// Pass null or empty string for line and just get the message without pretty stuff
	public static void printAssemblerSyntaxErr(String line, String message) {
		if(line != null && line.length() > 0) {
			System.err.println(line);
			System.err.println(nChars(line.length(), '^') + "\n\t" + OldAssembler.lineIndex  + ": " + message);
		} else System.err.println(OldAssembler.lineIndex  + ": " + message);
		OldAssembler.syntaxErrors++;
	}
	public static void printAssemberSyntaxErr(String message) {
		printAssemblerSyntaxErr(null, message);
	}
	
	public static void printLinkerSyntaxErr(String line, String message) {
		if(line != null && line.length() > 0) {
			System.err.println(line);
			System.err.println(nChars(line.length(), '^') + "\n\t" + message);
		} else System.err.println(message);
		Linker.syntaxErrors++;
	}
	public static void printLinkerSyntaxErr(String message) {
		printLinkerSyntaxErr(null, message);
	}
	
	public static boolean isNumeric(String str) {
		try {
			// Integer#valueOf() returns an Integer, not an int. I'm not
			// bothered to handle null values (one line of code) so I'm going to
			// stick with the primitive:
			Integer.parseInt(str); // Don't need to store this
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static String paddedBinary(int x, int len) {
		if(len <= 0) len = 1;
		// Assume unsigned int: (bodgy)
		if(x < 0) x = 0;
		
		return String.format("%" + len + "s", Integer.toBinaryString(x)).replace(" ", "0");
	}
	
	/*
	 * INSTRUCTION GET INFO
	 * Assumes `line` is a string of space-separated decimal opcodes and operands
	 */
	
	public static Integer getOpcode(String line) {
		String[] lineArr = line.split(" ");
		return Integer.valueOf(lineArr[0]);
	}
	
	public static Integer getType(Integer opcode) {
		return instructionTypes.get(opcode);
	}
	
	// Assuming line is all integer strings, which SHOULD be established
	// in earlier parsing. Convert the operand strings in lineArr to an
	// int[] of the standalone args:
	public static int[] getArgs(String line) {
		String[] lineArr = line.split(" ");

		int[] args = new int[lineArr.length-1];
		for(int i = 0; i < args.length; i++) args[i] = Integer.parseInt(lineArr[i+1]);
		
		return args;
	}
	
	public static boolean checkBinary(List<String> binary) {
		if(!checkByteAlignment(binary)) return false;
		
		for(String i : binary)
			if(!i.matches("[01]+")) return false;

		return true;
	}
	
	// Return true/false if list is made of even 8-bit words or not
	public static boolean checkByteAlignment(List<String> byteStream) {
		for(String i : byteStream)
			if(i.length()%8 != 0) return false;
		
		return true;
	}
	
	public static int countBytes(List<String> byteStream) {
		return String.join("", byteStream).length()/8;
	}
	
	// Uses Assembler.instructionMappings to pretty-print a table of conversions
	// made during assembly. The source file listing should always be bigger
	// than the output binary
	public static void printAssembly(List<String> source, List<String> binary) {
		if(!Main.verbose) return; // Save from further processing
		
		Map<Integer, Integer> map = OldAssembler.instructionMappings;
		
		for(int i = 0; i < source.size(); i++) {
			String srcLine = source.get(i);
			if(srcLine.length() == 0) continue; // Why bother printing gaps
			
			Integer asmAddress = map.get(i);
			
			if(asmAddress == null) {
				System.out.print("      ");
				colPrint(null, srcLine);
				continue;
			}
			
			System.out.print(String.format("%04d", asmAddress+1) + ": ");
			
			String asmLine = binary.get(asmAddress);
			if(asmLine.length()/8 == 2)
				asmLine = asmLine.substring(0, 8) + " " + asmLine.substring(8);
			colPrint(asmLine, srcLine);
		}
	}
	
	private static void colPrint(String str1, String str2) {
		final int col1Width = 21;
		if(str1 == null) str1 = "";
		if(str2 == null) str2 = "";
		int padding = col1Width-str1.length();
		if(padding <= 0) padding = 1;
		
		System.out.print(str1);
		System.out.print(nChars(padding, ' '));
		System.out.println(str2);
	}
}
