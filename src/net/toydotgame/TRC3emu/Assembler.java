package net.toydotgame.TRC3emu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assembler {
	public static int lineIndex = 0; // Count the line in the source file we are working on (for logging)
	public static int syntaxErrors = 0; // If this value is incremented, then we won't save the compiled output
	private static Map<String, Integer> instructions = loadInstructions(); // Key: mnemonic string, value: opcode integer
	// TODO: Make aliases private, remove alias logging from Main
	public static Map<String, Integer> aliases = loadAliases(); // Map of string aliases to numeric constants. User can add to this
	// Map initialisers:
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
	// Map of key: input file line # to value: instruction # 
	public static Map<Integer, Integer> instructionMappings = new HashMap<Integer, Integer>();
	
	public static List<String> main(List<String> input) {
		// Follow an iterative process, assembling one line at a time
		List<String> output = new ArrayList<String>();
		
		for(String line : input) {
			lineIndex++;
			
			// 1. Remove comments and empty lines
			line = removeComments(line);
			line = line.trim(); // Remove trailing whitespace for line-end comments
			if(line.length() == 0) continue; // Remaining line is just whitespace, so don't add it to output
			// 2. Convert opcode mnemonics to decimal opcode
			line = parseOpcode(line);
			if(line == null) continue; // Don't bother to continue parsing a line we know is invalid
			// 3. Look for aliases, define them accordingly, and substitute them in
			// TODO: Subroutine aliases (0 args)
			line = parseAliases(line);
			if(line == null) continue;
			// 4. Validate instructions and remove invalid ones
			line = validateInstruction(line);
			if(line == null) continue;
			// 5. Convert to binary string
			line = convertToBinary(line);
			if(line == null) continue;
			
			// Finally, line is not null. Add the assembled instruction to our output
			output.add(line);
			instructionMappings.put(lineIndex-1, output.size()-1);
		}
		
		return output;
	}
	
	private static String removeComments(String line) {
		int commentIndex = line.indexOf(";");
		
		if(commentIndex == -1) return line; // No comment found
		return line.substring(0, commentIndex);
	}
	
	private static String parseOpcode(String line) {
		if(line.startsWith(".")) return line; // Parse this later in parseAliases()
		
		String[] lineArr = line.split(" ");
		Integer opcode = instructions.get(lineArr[0]); // Allow null values
		if(opcode == null) {
			Utils.printErr(line, "Unknown mnemonic \"" + lineArr[0] + "\"!");
			return null;
		}
		
		lineArr[0] = String.valueOf(opcode);
		
		line = String.join(" ", lineArr);
		return line;
	}
	
	private static String parseAliases(String line) {
		// Get args of instructions, excluding opcode
		String[] lineArr = line.split(" ");
		
		if(line.startsWith(".")) { // Define new alias
			// Error state: wrong # of args
			if(lineArr.length != 2) {
				Utils.printErr(line, "Wrong number of arguments for alias definition! Should be 1, found " + String.valueOf(lineArr.length-1) + ".");
				return null;
			}
			
			// Error state: const already defined
			String alias = lineArr[0].substring(1); // Remove '.'
			Integer fetchedAlias = aliases.get(alias); // Allow null
			if(fetchedAlias != null) {
				Utils.printErr(line, "Alias \"" + alias + "\" already defined!");
				return null;
			}
			
			// Success: define new const, fail if not a number
			try {
				aliases.put(alias, Integer.valueOf(lineArr[1]));
				return null; // Not a syntax error, but we don't want this line in the assembled output
			} catch(NumberFormatException e) {
				Utils.printErr(line, "Failed defining constant! Invalid literal \"" + lineArr[1] + "\".");
				return null;
			}
		} // Else, check for aliases within instructions
		
		for(int i = 1; i < lineArr.length; i++) {
			String token = lineArr[i];
			if(Utils.isNumeric(token)) continue;
			
			Integer fetchedAlias = aliases.get(token);
			if(fetchedAlias == null) {
				Utils.printErr(line, "Unknown alias \"" + token + "\"!");
				return null;
			}
			
			lineArr[i] = String.valueOf(fetchedAlias);
		}
		
		line = String.join(" ", lineArr);
		return line;
	}
	
	private static String validateInstruction(String line) {
		return InstructionValidator.main(line);
	}
	
	private static String convertToBinary(String line) {
		// Instruction is already validated, so we just encode it to binary:
		return InstructionEncoder.main(line);
	}
	
	// Converts 16-bit instructions to 2 8-bit words (little endian) and
	// condenses 8-bit memory values
	public static List<String> malloc(List<String> instructionList) {
		List<String> memory = new ArrayList<String>();
		
		for(String word : instructionList) {
			switch(word.length()/8) { // # of bytes in word
				case 1:
					// TODO: Implement storage of 8-bit literals (probably needs
					// new assembler syntax)
					// TODO: Set up less wasteful way below of moving around
					// 8-bit literals so that pushing empty bytes is needed less
					memory.add(word); // TODO: The assembler doesn't even generate 8-bit words so this isn't reached
					break;
				case 2:
					if(memory.size()%2 != 0) // Push an empty byte to align
						memory.add(Utils.paddedBinary(0, 8));
					// Push lo byte, then hi byte
					memory.add(word.substring(8));
					memory.add(word.substring(0, 8));
					break;
				default:
					System.err.println("Unable to allocate memory for word 0b" + word + "! Should be 1 or 2 bytes.");
					System.exit(2);
					return null; // The compiler. It knows where I live. I must appease it.
			}
		}
		
		return memory;
	}
}
