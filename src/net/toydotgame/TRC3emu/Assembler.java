package net.toydotgame.TRC3emu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Assembler {
	private static final int NONE = 0;
	private static final int ALU = 1;
	private static final int IMM8_TO_REG = 2;
	private static final int IMM10 = 3;
	private static final int IMM3_TO_REG = 4;
	private static final int REG_TO_IMM3 = 5;
	private static final int IMM3_OR_REG = 6;
	private static final int REG_ONLY = 7;
	public static Map<String, Integer> instructions = loadInstructions();   // Map of instruction mnemonics to opcodes
	public static Map<String, Integer> aliases = loadAliases();             // Map of strings that parse into numeric constants, the user can add to this
	private static Map<Integer, Integer> operandTypes = loadOperandTypes(); // Map of opcodes to what kind of operands they take
	private static boolean syntaxError = false;
	private static int lineIndex = 0;
	
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
		for(int i = 0; i < opcodes.length; i++) map.put(opcodes[i], i);
		return map;
	}
	private static Map<String, Integer> loadAliases() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i <= 7; i++) map.put("r" + i, i); // Initialise 8 registers
		return map;
	}
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
	
	/* List<String> assemble(List<String> lines)
	 * Reads instructions as a list of strings, and returns a list of bytes as
	 * 8-bit binary strings
	 */
	public static List<String> assemble(List<String> lines) {
		List<String> bytes = new ArrayList<String>();
		
		for(String i : lines) {
			System.out.println("Instruction: " + i + " -> " + assembleInstruction(i));
		}
		
		handleSyntaxErrors(); // Quit if we need to
		
		return bytes;
	}
	
	/* String assembleInstruction(String instruction)
	 * Given a string that contains a human-readable assembly instruction,
	 * returns the appropriate two-byte binary instruction as a string
	 */
	private static String assembleInstruction(String instruction) {
		lineIndex++;
		String[] tokens = instruction.split(" "); // Operate on instruction one section at a time
		Integer opcode = instructions.get(tokens[0]); // Allowed to be null
		
		// Handle unknown opcodes being passed in (even though they should be
		// validated earlier):
		if(opcode == null) {
			System.err.println(String.join(" ", tokens));
			int instructionLength = tokens[0].length();
			if(instructionLength <= 0) instructionLength = 1;
			String arrows = String.format("%" + instructionLength + "s", "").replace(" ", "^");
			System.err.println(arrows + "--- " + lineIndex + ": Unknown instruction!");
			syntaxError = true;
			return Utils.paddedBinary(0, 16); // Return bogus value we won't ever write
		}
		
		// Replace mnemonic with number:
		tokens[0] = Utils.paddedBinary(opcode, 5);
		
		// Pass in the operands as an array, then replace the original array's
		// operands with the parsed ones. First convert operands from human-
		// readable to numeric:
		int[] operands = Stream.of(
			Arrays.copyOfRange(tokens, 1, tokens.length)
		).mapToInt(Integer::parseInt).toArray();
		for(int i = 0; i < operands.length; i++) {
			Integer fetchedAlias = aliases.get(operands[i]); // Allow null
			if(aliases.get(operands[i]) != null) operands[i] = fetchedAlias;
		}
		// Then from numeric to binary (and check format depending on instruction):
		String parsedOperands = parseOperands(opcode, operands);
		
		instruction = tokens[0] + parsedOperands;
		return instruction;
	}
	
	private static String parseOperands(int opcode, int[] operands) {
		Integer type = operandTypes.get(opcode);
		if(type == null) {
			System.err.println(lineIndex + ": Operand type unknown for instruction with opcode `" + opcode + "`!");
			System.exit(2);
		}
		
		switch(type) {
			case ALU:
				if(opcode != 11 && failArgsLength(3, operands)) return "";
				else if(operands.length != 2 && operands.length != 3) { // RSH-specific sytax, check manually
					System.err.println(lineIndex + ": Wrong number of arguments for operation!");
					syntaxError = true;
					return "";
				}
				if(failOverflows(7, operands)) return "";
				
				String a, b, c;
				if(operands.length == 3) {
					a = Utils.paddedBinary(operands[0], 3);
					b = Utils.paddedBinary(args.get(1), 3);
					c = Utils.paddedBinary(args.get(2), 3);
				} else { // RSH
					a = Utils.paddedBinary(args.get(0), 3);
					b = Utils.paddedBinary(0, 3);
					c = Utils.paddedBinary(args.get(1), 3);
				}
				
				return "00" + a + b + c;
			case IMM8_TO_REG:
				break;
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
			default:
				// This error should not be reached but just in case (and we
				// need a default case anyway):
				System.err.println("Unknown instruction type at line " + lineIndex + "!");
				syntaxError = true; // Don't write to file
			case NONE:
				return Utils.paddedBinary(0, 11);
		}
		
		return "";
	}
	
	private static boolean failOverflows(int max, int[] args) {
		for(int i = 0; i < args.length; i++) {
			if(arg > max || arg < 0) {
				System.err.println(lineIndex + ": " + arg + " out of bounds! Should be in the range of 0-" + max);
				syntaxError = true;
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean failArgsLength(int length, int[] args) {
		if(args.length == length) return false;
		
		System.err.println(lineIndex + ": Wrong number of arguments for operation!");
		syntaxError = true;
		return true;
	}
	
	private static void handleSyntaxErrors() {
		if(!syntaxError) return;
		
		System.err.println("\nThe assembler encountered syntax errors. No binary has been written. Exiting...");
		System.exit(1);
	}
}
