package net.toydotgame.TRC3emu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OldAssembler {
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
			// TODO: Make line global because ts sucks
			line = removeComments(line);
			if(line.length() == 0) continue; // Remaining line is just whitespace, so don't add it to output
			// 2. Convert opcode mnemonics to decimal opcode
			line = parseOpcode(line);
			if(line == null) continue; // Don't bother to continue parsing a line we know is invalid
			// 3. Look for aliases, define them accordingly, and substitute them in
			line = parseAliases(line);
			if(line == null) continue;
			// 4. Validate instructions and remove invalid ones
			//line = validateInstruction(line);
			//if(line == null) continue;
			
			// Finally, line is not null. Add the assembled instruction to our output
			output.add(line);
			instructionMappings.put(lineIndex-1, output.size()-1);
		}
		
		return output;
	}
	
	private static String removeComments(String line) {
		int commentIndex = line.indexOf(";");
		
		if(commentIndex == -1) return line; // No comment found
		return line.substring(0, commentIndex).trim();
	}
	
	private static String parseOpcode(String line) {
		if(isAlias(line)) return line; // Assembler alias definition, parse this later in parseAliases()
		
		String[] lineArr = line.split(" ");
		Integer opcode = instructions.get(lineArr[0].toUpperCase()); // Allow null values
		if(opcode == null) {
			OldUtils.printAssemblerSyntaxErr(line, "Unknown mnemonic \"" + lineArr[0] + "\"!");
			return null;
		}
		
		lineArr[0] = String.valueOf(opcode);
		
		line = String.join(" ", lineArr);
		return line;
	}
	
	private static String parseAliases(String line) {
		// Get args of instructions, excluding opcode
		String[] lineArr = line.split(" ");
		
		if(isAlias(line)) {
			// Error: wrong # of args
			if(!line.endsWith(":") && lineArr.length != 2) {
				OldUtils.printAssemblerSyntaxErr(line, "Wrong number of arguments for alias definition! Should be 1, found " + String.valueOf(lineArr.length-1) + ".");
				return null;
			} else if(line.endsWith(":") && lineArr.length > 1) {
				OldUtils.printAssemblerSyntaxErr(line, "Too many arguments for subroutine label! Should be 0, found " + String.valueOf(lineArr.length-1) + ".");
				return null;
			}
			
			// Error: digit-only alias name
			String alias;
			if(line.endsWith(":")) alias = line.substring(0, line.length()-1).toLowerCase();
			else alias = lineArr[0].substring(1).toLowerCase(); // Remove '#'/'.'
			if(alias.matches("[0-9]+")) {
				OldUtils.printAssemblerSyntaxErr(line, "Alias name cannot consist of only digits!");
				return null;
			}
			
			// Error: already defined
			if(aliases.containsKey(alias)) {
				OldUtils.printAssemblerSyntaxErr(line, "Alias \"" + alias + "\" already defined!");
				return null;
			}
			
			// Success: define new const, fail if not a number
			try {
				/*
				 * Hacky slop:
				 * 1. Define null Integer `value`
				 * 2. If subroutine label, value = -2
				 *    Otherwise, value = args[0] of alias definition
				 *     2a. If alias definition and args[0] supplied is negative,
				 *         throw invalid literal error
				 * 3. If linker constant, value = -1
				 * 4. Store alias name and type (-2=subroutine, -1=linker const,
				 *    otherwise=assembler def) in `aliases`
				 *     4a. If assembler def, remove the line. Otherwise, keep it
				 */
				Integer value;
				if(line.endsWith(":")) value = -2; // -2 denotes subroutine
				else value = Integer.valueOf(lineArr[1]);
				if(value < 0 && lineArr.length == 2) throw new NumberFormatException();
				if(line.startsWith(".")) value = -1; // Use -1 as a value to denote alias is a linker definition, not an assembler constant
				
				aliases.put(alias, value);
				if(line.startsWith("#")) return null; // Not a syntax error, but we don't want this line in the assembled output
				else return line;
			} catch(NumberFormatException e) {
				OldUtils.printAssemblerSyntaxErr(line, "Failed defining constant! Invalid literal \"" + lineArr[1] + "\".");
				return null;
			}
		} // Else, check for aliases within instructions
		
		for(int i = 1; i < lineArr.length; i++) {
			String token = lineArr[i];
			if(OldUtils.isNumeric(token)) continue;
			
			Integer fetchedAlias = aliases.get(token);
			if(fetchedAlias == null) {
				OldUtils.printAssemblerSyntaxErr(line, "Unknown alias \"" + token + "\"!");
				return null;
			} else if(fetchedAlias == -1) {
				continue; // Don't substitute if it's a linker def
			}
			
			lineArr[i] = String.valueOf(fetchedAlias);
		}
		
		line = String.join(" ", lineArr);
		return line;
	}
	
	private static String validateInstruction(String line) {
		return InstructionValidator.main(line);
	}
	
	private static boolean isAlias(String line) {
		if(line.startsWith("#")) return true;
		if(line.startsWith(".")) return true;
		if(line.endsWith(":")) return true;
		
		return false;
	}
}
