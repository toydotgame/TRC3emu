package net.toydotgame.TRC3emu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstructionAssembler {
	private static final int NONE = 0;
	private static final int ALU = 1;
	private static final int IMM8_TO_REG = 2;
	private static final int IMM10 = 3;
	private static final int IMM3_TO_REG = 4;
	private static final int REG_TO_IMM3 = 5;
	private static final int IMM3_OR_REG = 6;
	private static final int REG_ONLY = 7;
	private static Map<Integer, Integer> operandTypes = loadOperandTypes();
	
	private static Map<Integer, Integer> loadOperandTypes() {
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
	
	public static String assembleInstruction(List<String> instructionAsString) {
		List<Integer> instruction = new ArrayList<Integer>();
		for(int i = 0; i < instructionAsString.size(); i++) { // Convert instruction from strings to ints for easier assembly
			try {
				instruction.add(Integer.valueOf(instructionAsString.get(i)));
			} catch(NumberFormatException e) { // Handle syntax errors. Register/memory/immediates that are too large will be handled later
				System.err.println(String.join(" ", instructionAsString));
				if(i == 0) {
					String arrows = String.format("%" + instructionAsString.get(i).length() + "s", "").replace(" ", "^");
					System.err.print(arrows + "--- Unknown instruction");
				} else System.err.print("Unknown keyword \"" + instructionAsString.get(i) + "\"");
				System.err.println(" at line " + OldMain.currentLine + "! Output file will be incomplete or corrupt.");
				System.exit(1);
			}
		}
		String returnString = "";
		if(instruction.size() == 0) return returnString;
		
		int intOpcode = instruction.get(0);
		OldMain.currentAddress += 2; // Assuming it's a valid instruction by now, so we allocate this address
		
		if(intOpcode != 32) {
			String opcode = toPaddedBinary(intOpcode, 5);
			returnString += opcode;
		}
		
		returnString += assembleOperands(intOpcode, instruction.subList(1, instruction.size()));
		
		try {
			if(returnString.length() == 8) return returnString;
			if(returnString.length() != 16) throw new StringIndexOutOfBoundsException(); // Hack to jump to catch() below if the string isn't what we want
			return returnString.substring(0, 8) + " " + returnString.substring(8); // TODO: Flip for little endian
		} catch(StringIndexOutOfBoundsException e) {
			System.err.println("v Couldn't pretty-print line " + OldMain.currentLine + "! v");
			return returnString;
		}
	}
	
	private static String assembleOperands(int opcode, List<Integer> args) {
		if(opcode == 32) return toPaddedBinary(args.get(0), 8); // Constant definition
		
		Integer type = operandTypes.get(opcode);
		if(type == null) {
			System.err.println("Invalid opcode at line " + OldMain.currentLine + "! Assembled to opcode " + opcode + ", which is undefined.");
			System.exit(1);
		}
		
		switch(type) {
			case ALU:
				if(!(args.size() == 2 && opcode == 11) && args.size() != 3) {
					System.err.println("Wrong number of arguments for ALU/RAM operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 3 (or 2 for right shift operations).");
					System.exit(1);
				}
				exitIfOverflow(7, args, "Register address(es) or 3-bit immediate too large at line " + OldMain.currentLine + "!: " + args);
				
				String a, b, c;
				if(args.size() == 3) {
					a = toPaddedBinary(args.get(0), 3);
					b = toPaddedBinary(args.get(1), 3);
					c = toPaddedBinary(args.get(2), 3);
				} else { // RSH
					a = toPaddedBinary(args.get(0), 3);
					b = toPaddedBinary(0, 3);
					c = toPaddedBinary(args.get(1), 3);
				}
				
				return "00" + a + b + c;
			case IMM8_TO_REG:
				if(args.size() != 2) {
					System.err.println("Wrong number of arguments for operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 2.");
					System.exit(1);
				}
				exitIfOverflow(255, args.get(0), "8-bit immediate too large at line " + OldMain.currentLine + "!: " + args.get(0));
				exitIfOverflow(7, args.get(1), "Register address too large at line " + OldMain.currentLine + "!: " + args.get(1));
				
				return toPaddedBinary(args.get(0), 8) + toPaddedBinary(args.get(1), 3);
			case IMM10:
				if(args.size() != 1) {
					System.err.println("Wrong number of arguments for jump/branch operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 1.");
					System.exit(1);
				}
				exitIfOverflow(1023, args.get(0), "10-bit immediate too large at line " + OldMain.currentLine + "!: " + args.get(0));
				
				return toPaddedBinary(args.get(0), 10) + "0"; // End of jumps is always 0 due to instruction alignment in memory
			case IMM3_TO_REG:
				if(args.size() != 2) {
					System.err.println("Wrong number of arguments for IO in operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 2.");
					System.exit(1);
				}
				exitIfOverflow(7, args, "3-bit immediate/register address too large at line " + OldMain.currentLine + "!: " + args);
				
				return "00000" + toPaddedBinary(args.get(0), 3) + toPaddedBinary(args.get(1), 3);
			case REG_TO_IMM3:
				if(args.size() != 2) {
					System.err.println("Wrong number of arguments for IO out operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 2.");
					System.exit(1);
				}
				exitIfOverflow(7, args, "3-bit immediate/register address too large at line " + OldMain.currentLine + "!: " + args);
				
				return "00" + toPaddedBinary(args.get(0), 3) + toPaddedBinary(args.get(1), 3) + "000";
			case IMM3_OR_REG:
				if(args.size() != 1 && args.size() != 2) {
					System.err.println("Wrong number of arguments for Set page operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 1 or 2.");
					System.exit(1);
				}
				exitIfOverflow(7, args, "3-bit immediate/register address too large at line " + OldMain.currentLine + "!: " + args);
				
				if(args.size() == 1) { // 1 argument, we assume a register always
					return "00000" + toPaddedBinary(args.get(0), 3) + "000";
				} else { // Otherwise, assume something of the form <immediate>, <register>
					return toPaddedBinary(args.get(0), 3) + "00" + toPaddedBinary(args.get(1), 3) + "000";
				}
			case REG_ONLY:
				if(args.size() != 1) {
					System.err.println("Wrong number of arguments for Set page operation at line " + OldMain.currentLine + "! Found " + args.size() + ", should be 1.");
					System.exit(1);
				}
				exitIfOverflow(7, args.get(0), "3-bit register address too large at line " + OldMain.currentLine + "!: " + args.get(0));
				
				return "00000000" + toPaddedBinary(args.get(0), 3);
			default:
				// This error should not be reached but just in case (and we
				// need a default case anyway):
				System.err.println("Unknown instruction type at line " + OldMain.currentLine + "!");
			case NONE:
				return toPaddedBinary(0, 11);
		}
	}
	
	public static String toPaddedBinary(int x, int length) {
		return String.format("%" + length + "s", Integer.toBinaryString(x)).replace(" ", "0");
	}
	
	public static String toPaddedHex(int x, int length) {
		if(x < 0) return String.join("", Collections.nCopies(length, " "));
		
		return String.format("%" + length + "s", Integer.toHexString(x)).replace(" ", "0").toUpperCase();
	}
	
	public static void exitIfOverflow(int max, List<Integer> values, String message) {
		if(!checkOverflow(max, values)) return;
		
		System.err.println(message);
		System.exit(1);
	}
	// Overload for one value:
	public static void exitIfOverflow(int max, int value, String message) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(value);
		exitIfOverflow(max, list, message);
	}
	
	public static boolean checkOverflow(int max, List<Integer> args) {
		for(int i = 0; i < args.size(); i++)
			if(args.get(i) > max) return true;
		return false;
	}
	// Overload to specify one number only:
	public static boolean checkOverflow(int max, int x) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(x);
		return checkOverflow(max, list);
	}
}
