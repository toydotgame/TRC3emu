package net.toydotgame.TRC3emu;

import java.util.ArrayList;
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
				System.err.println(" at line " + Main.currentLine + "! Output file will be incomplete or corrupt.");
				System.exit(1);
			}
		}
		String returnString = "";
		if(instruction.size() == 0) return returnString;
		
		int intOpcode = instruction.get(0);
		String opcode = toPaddedBinary(intOpcode, 5);
		returnString += opcode;
		
		returnString += assembleOperands(intOpcode, instruction.subList(1, instruction.size()));
		
		try {
			return returnString.substring(0, 8) + " " + returnString.substring(8); // TODO: Flip for little endian
		} catch(StringIndexOutOfBoundsException e) {
			System.err.println("v Couldn't pretty-print line " + Main.currentLine + "! v");
			return returnString;
		}
	}
	
	private static String assembleOperands(int opcode, List<Integer> args) {
		Integer type = operandTypes.get(opcode);
		switch(type) {
			case ALU:
				if(!(args.size() == 2 && opcode == 11) && args.size() != 3) {
					System.err.println("Wrong number of arguments for ALU operation at line " + Main.currentLine + "!");
					System.exit(1);
				}
				if(checkOverflow(7, args)) {
					System.err.println("Register address(es) too large at line " + Main.currentLine + "!: " + args);
					System.exit(1);
				}
				
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
					System.err.println("Wrong number of arguments for operation at line " + Main.currentLine + "!");
					System.exit(1);
				}
				if(checkOverflow(255, args.get(0))) {
					System.err.println("8-bit immediate too large at line " + Main.currentLine + "!: " + args.get(0));
					System.exit(1);
				}
				if(checkOverflow(7, args.get(1))) {
					System.err.println("Register address too large at line " + Main.currentLine + "!: " + args.get(1));
					System.exit(1);
				}
				
				return toPaddedBinary(args.get(0), 8) + toPaddedBinary(args.get(1), 3);
			case IMM10:
				break;
			case IMM3_TO_REG:
				break;
			case REG_TO_IMM3:
				break;
			case IMM3_OR_REG:
				break;
			case REG_ONLY:
				break;
			case NONE:
			default:
				return toPaddedBinary(0, 11);
		}
		return "";
	}
	
	public static String toPaddedBinary(int x, int length) {
		return String.format("%" + length + "s", Integer.toBinaryString(x)).replace(" ", "0");
	}
	
	// Overload to specify one number only:
	public static boolean checkOverflow(int max, int x) {
		List<Integer> list = new ArrayList<Integer>();
		list.add(x);
		return checkOverflow(max, list);
	}
	
	public static boolean checkOverflow(int max, List<Integer> args) {
		for(int i = 0; i < args.size(); i++)
			if(args.get(i) > max) return true;
		return false;
	}
}
