package net.toydotgame.TRC3emu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Linker {
	private static List<String> input;
	/*
	 * We dedicate the memory just after the last instruction to store bytes
	 * defined by the user as "constants." This map stores the name of an alias,
	 * bound to the offset _within_ this "data" space where the constant is
	 * held.
	 * Later, the memory allocator will place these bytes at the address where
	 * the end of instructions are, + the data space offset address.
	 * 
	 * We do a similar thing for subroutines:
	 * 1. Parse the instruction listing, counting instruction line numbers
	 * 2. When a subroutine label is hit, define that subroutine as a String label
	 *    and the instruction line number corresponding to it
	 * 3. Substitute all these subroutine labels in to commands
	 */
	private static Map<String, Integer> definitions = new HashMap<String, Integer>();
	private static Map<String, Integer> subroutines = new HashMap<String, Integer>();
	public static int syntaxErrors = 0;
	
	public static List<String> main(List<String> input) {
		Linker.input = input;
		
		// 1. Size of instructions in memory:
		int offset = countInstructions() << 1;
		// 2. Move constant definitions to the end of the list, parse and store
		//    them:
		parseConstants();
		// The assembler handles most syntax errors, so the linker doesn't
		// contain syntax error logic in Main#link(). The only rudimentary
		// handling is the below. Everything else is a fatal error because we
		// assume object files aren't human-written
		if(Assembler.syntaxErrors > 0) {
			System.err.println(syntaxErrors + " errors present. Output will not be written.");
			System.exit(1);
		}
		// 3. Substitute in definitions
		List<String> output = new ArrayList<>();
		
		// 6. Convert to binary string
		//line = convertToBinary(line);
		//if(line == null) continue;		
		
		for(String i : input) System.out.println(i);
		System.out.println(definitions);
		return output;
	}
	
	private static int countInstructions() {
		int count = 0;
		
		for(int i = 0; i < input.size(); i++) {
			String line = input.get(i);
			if(line.startsWith(".") || line.endsWith(":")) continue; // Ignore definitions
			
			count++; // Blindly count all instructions in object
		}
		
		return count;
	}
	
	private static void parseConstants() {
		int constantCount = 0; // Count the number of constants we define
		
		for(int i = 0; i < input.size(); i++) {
			String line = input.get(i);
			if(!line.startsWith(".")) continue;
			
			String constantName = line.split(" ")[0].substring(1);			
			if(definitions.containsKey(constantName)) {
				Utils.printLinkerSyntaxErr(line, "Constant \"" + constantName + "\" already defined!");
				continue;
			}
			
			definitions.put(constantName, constantCount++);
			input.remove(i);
			i--; // Compensate for the fact we just removed the element at this index
		}
	}
	
	private static String convertToBinary(String line) {
		// Instruction is already validated, so we just encode it to binary:
		return InstructionEncoder.main(line);
	}
	
	// Converts 16-bit instructions to 2 8-bit words (little endian) and
	// condenses 8-bit memory values
	private static List<String> malloc(List<String> instructionList) {
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
