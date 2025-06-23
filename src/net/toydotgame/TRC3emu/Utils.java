package net.toydotgame.TRC3emu;

import java.util.HashMap;
import java.util.Map;

public class Utils {
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
	
	public static String nChars(String inputString, char charToUse) {
		if(inputString.length() == 0) return String.valueOf(charToUse);

		//return String.format("%" + inputString.length() + "s", "").replace(" ", String.valueOf(charToUse));
		return inputString.replaceAll(".", String.valueOf(charToUse));
	}
	
	// Pass null or empty string for line and just get the message without pretty stuff
	public static void printErrForLine(String line, String message) {
		if(line != null && line.length() > 0) {
			System.err.println(line);
			System.err.println(nChars(line, '^') + "\n\t" + Assembler.lineIndex  + ": " + message);
		} else System.err.println(Assembler.lineIndex  + ": " + message);
		Assembler.syntaxErrors++;
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
}
