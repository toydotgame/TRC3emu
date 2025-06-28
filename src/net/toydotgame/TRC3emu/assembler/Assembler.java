package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.toydotgame.TRC3emu.Log;
import net.toydotgame.TRC3emu.Utils;

/**
 * Main entrypoint holding assembly methods and dispatching.
 */
public class Assembler {
	private static int syntaxErrors = 0;
	/**
	 * Denotes the list of alias names and their numeric values for later
	 * substitution. Variables are also added to this list in order to reserve
	 * their names, but their value is initialised to {@code null} to begin with.
	 * Later, {@code null} is replaced with their position in <i>data memory</i>,
	 * which is equivalent to their index in the list of variables, plus a memory
	 * offset equal to the total length of the <i>program data</i> space. (i.e.
	 * all variables are stored in memory after program data)<br>
	 * <br>
	 * In this Map, the values of subroutines and definitions are the memory
	 * address of a subroutine's first instruction and a numeric constant kept
	 * only during assembly, respectively.<br>
	 * Variables' value in this Map are held as their address in memory (as
	 * described above). Their actual value is held in the {@link
	 * Assembler#variables} Map.<br>
	 * <br>
	 * The aliases {@code r0} through {@code r7} are reserved as register aliases.
	 * @see Assembler#variables
	 */
	private static Map<String, Integer> aliases = initAliases();
	private static Map<String, Integer> initAliases() { // Assembler aliases
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i < 8; i++) map.put("r"+i, i); // Register aliases
		return map;
	}
	/**
	 * This Map holds the value of a specific variable, with the key being the
	 * alias name, and the value being the actual literal unsigned 8-bit byte
	 * to be kept. For each key {@code k} ∈ {@code variables}, {@code
	 * aliases.containsKey(k)} is {@code true}. Therefore, given some
	 * alias/variable name {@code s}, {@code aliases.get(s)} yields its location
	 * in the final memory mapping, and {@code variables.get(s)} yields the byte
	 * to be put at that address.<br>
	 * <br>
	 * In practice, the program data and the variable data is stored as two {@code
	 * List}s, with the latter being concatenated after the former to form the
	 * final binary that represents the final memory map.
	 * @see Assembler#aliases
	 */
	private static Map<String, Integer> variables = new HashMap<String, Integer>();
	
	/**
	 * Assembles a source file and returns its binary representation
	 * @param source Input file lines
	 * @return Output file lines
	 */
	public static List<String> main(List<String> source) {
		// Generate program listing:
		List<AssemblerInstruction> program = new ArrayList<AssemblerInstruction>();
		for(int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			/*
			 * Generate a new instruction from the String value of the current
			 * line. The AssemblerInstruction constructor takes in this line plus
			 * a line index (which we can use the fact that `source` is a list
			 * from the original input file to yield our line numbers if we just
			 * index starting at 1 (so i.e. i+1=line index)). The line index is
			 * used for syntax error message pretty-printing.
			 * 
			 * Additionally, the constructor will designate the `type` field, to
			 * denote if it is invalid (i.e. a comment or empty line, or a
			 * syntactically invalid statement that should be skipped in further
			 * operations), a variable, subroutine, or a definition.
			 * 
			 * In this for() loop, we are only adding valid lines of type
			 * INSTRUCTION, as this loop is just yielding the program data section
			 * of memory.
			 */
			AssemblerInstruction instruction = new AssemblerInstruction(line, i+1);
			
			// 1. Replace opcode mnemonics with opcode strings:
			instruction.substituteOpcode();
			// 2. If this statement is a variable/subroutine/definition, then
			//    parse it and add it as an alias/variable
			defineAlias(instruction);
			
			if(instruction.type != AssemblerInstruction.INSTRUCTION) continue;
			// Otherwise, valid instruction:
			program.add(instruction);
		}
		
		// Generate variable listing:
		List<String> variableData = new ArrayList<String>();
		int variableCounter = program.size()<<1; // Start with this address and increment
		for(String alias : aliases.keySet()) {
			Integer value = aliases.get(alias);
			if(value != null) continue;
			// Replace null address with final data location following program data space:
			aliases.put(alias, variableCounter++);
			variableData.add(String.valueOf(variables.get(alias)));
		}
		
		// Take previous list of instructions and finish parsing it:
		List<String> programData = new ArrayList<String>();
		for(AssemblerInstruction instruction : program) {
			// 1. Attempt substituting in alias names if found:
			substituteAliases(instruction);
			// 2. Make operand length 2 for the instructions that assume it:
			validateOperands(instruction);
			// 3. Validate operands:
			validateOverflows(instruction);
			// `instruction` is not an immutable reference so these void methods
			// do actually work fine! (It's been a while since I've Java'd)
			
			if(instruction.type == AssemblerInstruction.INSTRUCTION)
				programData.add(instruction.text());
			
			// TODO: Remove when no longer needed
			Log.debug(instruction.originalText);
			Log.debug("    Type: "+instruction.type);
			Log.debug("    Index: "+instruction.memoryIndex);
			Log.debug("    Assembled: "+instruction.text());
		}
		
		// Concatenate two data spaces into one stream:
		List<String> binary = new ArrayList<String>(programData);
		binary.addAll(variableData);
		
		binary = Encoder.main(binary);
		
		Log.debug("Aliases:\n"+aliases);
		Log.debug("Variables:\n"+variables);
		
		if(syntaxErrors > 0)
			Log.exit(syntaxErrors+" errors occured. No output will be written");
		
		return binary;
	}
	
	/**
	 * Raise a syntax error and print a message to go along with it
	 * @param message Message describing the error
	 * @param instruction {@link AssemblerInstruction} associated with the error
	 */
	public static void syntaxError(String message, AssemblerInstruction instruction) {
		syntaxErrors++;
		
		String line = instruction.originalText;
		int index = instruction.lineIndex;
		
		// Log error for empty line:
		if(line.length() == 0) {
			Log.error(index+": "+message);
			return;
		}
		
		// Log otherwise:
		Log.error(line);
		Log.error(Utils.nChars(line.length(), '^'));
		Log.error("  "+index+": "+message);
	}
	/**
	 * Raise a syntax error and print a message without a line index
	 * @param message Message describing the error
	 * @see Assembler#syntaxError(String, int)
	 */
	public static void syntaxError(String message) {
		syntaxErrors++;
		Log.error(message);
	}

	/**
	 * Defines variables, subroutines, and definitions. If the input instruction
	 * is not one of these, this method silently does nothing.<br>
	 * <br>
	 * Checks that the alias has not already been defined, and if not, it
	 * defines it in the list of {@link #aliases}.
	 * @param instruction {@link AssemblerInstruction} instance
	 */
	private static void defineAlias(AssemblerInstruction instruction) {
		String alias = instruction.alias;
		// If an alias, check that the alias is not already defined, else return
		switch(instruction.type) {
			default:
				return;
			case AssemblerInstruction.VARIABLE:
			case AssemblerInstruction.SUBROUTINE:
			case AssemblerInstruction.DEFINITION:
				if(aliases.containsKey(alias)) {
					syntaxError("Alias \""+alias+"\" already defined!", instruction);
					return;
				}
		}
		
		// Define new alias if above passed: See javadocs for the instruction
		// type constants to see the function of each
		switch(instruction.type) {
			case AssemblerInstruction.VARIABLE:
				try {
					int value = Integer.parseInt(instruction.tokens.get(1));
					if(value < 0 || value > 255) throw new NumberFormatException();
					
					aliases.put(alias, null);
					variables.put(alias, value);
				} catch(NumberFormatException e) {
					syntaxError(
						"Invalid numeric literal \""+instruction.tokens.get(1)+"\" when defining variable \""+alias+"\"! "
						+"Must be a number in the range of 0-255.", instruction
					);
				}
				
				break;
			case AssemblerInstruction.SUBROUTINE:
				aliases.put(alias, instruction.memoryIndex);
				
				break;
			case AssemblerInstruction.DEFINITION:
				try {
					int value = Integer.parseInt(instruction.tokens.get(1));
					if(value < 0) throw new NumberFormatException();
					aliases.put(alias, value);
				} catch(NumberFormatException e) {
					syntaxError(
						"Invalid numeric literal \""+instruction.tokens.get(1)+"\" when defining assembler definition "
						+"\""+alias+"\"! Must be a number above 0.", instruction
					);
				}
		}
	}
	
	/**
	 * Tries to find any uses of an alias in an instruction's operands, and
	 * substitutes in its respective value if so.
	 * @param instruction Instance of {@link AssemblerInstruction}, where {@link
	 * AssemblerInstruction#type}={@link AssemblerInstruction#INSTRUCTION}
	 */
	private static void substituteAliases(AssemblerInstruction instruction) {
		for(int i = 1; i < instruction.tokens.size(); i++) {
			String operand = instruction.tokens.get(i);
			if(Utils.isDigital(operand))
				continue; // Don't substitute what's already a number
			
			if(aliases.containsKey(operand))
				instruction.tokens.set(i, String.valueOf(aliases.get(operand)));
			else {
				syntaxError("Undefined alias \""+operand+"\"!", instruction);
				instruction.type = AssemblerInstruction.INVALID;
			}
		}
	}
	
	/**
	 * There are a few edge cases where valid syntax for an assembly instruction
	 * does not match the inputs TRC3 expects internally:
	 * <ul>
	 * 	<li><b>{@code RSH}:</b> Expects 3 arguments, as it is an ALU instruction.
	 * However, 2 arguments can be provided and the assembler should interpret
	 * that the middle operand (<i>Read B</i>) is to be {@code 0}. {@code RSH}
	 * instructions with 3 arguments need no work</li>
	 * 	<li><b>{@code PAS}:</b> Expects 2 arguments, an immediate and a register
	 * address (in that order). The assembler should, however, allow provision of
	 * only 1 argument, and assume that that argument is the register address.
	 * (Meaning the immediate is {@code 0}) {@code PAS} instructions with 2
	 * arguments need no work</li>
	 * </ul>
	 * For these cases, this method will modify the {@link
	 * AssemblerInstruction#tokens} value for the provided object for later
	 * validation (in {@link #validateOverflows(AssemblerInstruction)}) and
	 * finally encoding into binary.<br>
	 * <br>
	 * Silently does nothing if opcode is not one accounted for in this. It is
	 * safe to assume that the number of operands passed in for a given
	 * instruction will one of the two aforementioned possibilities (the machine
	 * expected amount, or the shorthand amount allowed by the assembler).<br>
	 * <br>
	 * Following execution of this method, it is safe to assume for development
	 * purposes that the number of tokens in the provided object will match the
	 * Map {@code desiredTokenCounts} found in
	 * {@link Validator#validateInstruction(AssemblerInstruction)}.
	 * @param instruction Instance of {@link AssemblerInstruction}, where {@link
	 * AssemblerInstruction#type}={@link AssemblerInstruction#INSTRUCTION}
	 */
	private static void validateOperands(AssemblerInstruction instruction) {
		if(instruction.type == AssemblerInstruction.INVALID) return;
		
		switch(instruction.opcode) {
			case 11: // RSH
				if(instruction.tokens.size() == 4) break;
				// Otherwise, we know it's just 2 operands (3 tokens):
				instruction.tokens.add(2, "0"); // REGFILEread B = 0
				break;
			case 25: // PAS
				if(instruction.tokens.size() == 3) break;
				instruction.tokens.add(1, "0"); // Imm = 0
		}
	}
	
	/**
	 * Hands off to {@link Validator#validateOverflows(AssemblerInstruction)}.
	 * Assumes {@link AssemblerInstruction#INSTRUCTION} as input. The return value
	 * of the {@link Validator} validation, if {@code false}, is used to set the
	 * {@link AssemblerInstruction#type} of this instance to {@link
	 * AssemblerInstruction#INVALID}. Otherwise, nothing is done.<br>
	 * <br>
	 * An instruction's validity is determined by two things:
	 * <ol>
	 * 	<li>There are the correct number of arguments for this instruction. This
	 * is handled by {@link Validator#validateInstruction(AssemblerInstruction)}
	 * and {@link Validator#validateAlias(AssemblerInstruction)}</li>
	 * 	<li>Each argument (operand) fits within the number of bits allocated in
	 * the instruction word format. This is handled here</li>
	 * </ol>
	 * @param instruction Instance of {@link AssemblerInstruction}, where {@link
	 * AssemblerInstruction#type}={@link AssemblerInstruction#INSTRUCTION}
	 * @see Validator#validateOverflows(AssemblerInstruction)
	 * @see Validator#validateAlias(AssemblerInstruction)
	 * @see Validator#validateInstruction(AssemblerInstruction)
	 */
	private static void validateOverflows(AssemblerInstruction instruction) {
		if(instruction.type == AssemblerInstruction.INVALID) return;
		
		List<String> operands =
			instruction.tokens.subList(1, instruction.tokens.size());
		List<Integer> operandInts = new ArrayList<Integer>();
		for(String operand : operands) {
			try {
				int value = Integer.parseInt(operand);
				if(value < 0) throw new NumberFormatException();
				operandInts.add(value);
			} catch(NumberFormatException e) {
				syntaxError(
					"Invalid operand \""+operand+"\"! Should be a number ≥0.",
					instruction
				);
				instruction.type = AssemblerInstruction.INVALID;
				return;
			}
		}
		
		if(!Validator.validateOverflows(instruction.opcode, operandInts))
			instruction.type = AssemblerInstruction.INVALID;
	}
}
