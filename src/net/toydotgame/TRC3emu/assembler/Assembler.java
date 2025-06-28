package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.Collections;
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
	private static Map<String, Integer> aliases = new HashMap<String, Integer>();
	private static Map<String, Integer> variables = new HashMap<String, Integer>();
	
	/**
	 * Assembles a source file and returns its binary representation
	 * @param source Input file lines
	 * @return Output file lines
	 */
	public static List<String> main(List<String> source) {
		// Generate program listing:
		List<String> programData = new ArrayList<String>();
		for(int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			AssemblerInstruction instruction = new AssemblerInstruction(line, i);
			
			// TODO: Do stuff with AssemblerInstruction methods
			instruction.substituteOpcode();
			defineAlias(instruction);
			instruction.validate();
			
			Log.debug("\tFinal memory index: "+instruction.memoryIndex);
			if(instruction.type != AssemblerInstruction.INSTRUCTION) continue;
			// Otherwise, non-comment/non-syntax-error:
			Log.debug("\tAssembled instruction to: \""+instruction.getText()+"\"");
			programData.add(instruction.getText());
		}
		
		// Generate variable listing:
		List<String> variableData = new ArrayList<String>();
		int variableCounter = programData.size()<<1; // Start with this address and increment
		for(String alias : aliases.keySet()) {
			Integer value = aliases.get(alias);
			if(value != null) continue;
			// Replace null address with final data location following program data space:
			aliases.put(alias, variableCounter++);
			variableData.add(String.valueOf(variables.get(alias)));
		}
		
		// Concatenate two data spaces into one stream:
		List<String> binary = new ArrayList<String>(programData);
		binary.addAll(variableData);
		
		binary = Encoder.main(binary);
		
		Log.debug("programData: "+programData);
		Log.debug("variableData: "+variableData);
		Log.debug("vars: "+variables);
		Log.debug("aliases: "+aliases);
		Log.log("");
		Log.debug("binary: "+binary);
		
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
		
		String line = instruction.getText();
		int index = instruction.lineIndex;
		
		// Log error for empty line:
		if(line.length() == 0) {
			Log.error(index+": "+message);
			return;
		}
		
		// Log otherwise:
		Log.error(line);
		Log.error(Utils.nChars(line.length(), '^'));
		Log.error("\t"+index+": "+message);
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
					Log.error("VAR: "+alias+":"+value);
				} catch(NumberFormatException e) {
					syntaxError(
						"Invalid numeric literal \""+instruction.tokens.get(1)+"\" when defining variable \""+alias+"\"! "
						+"Must be a number in the range of 0-255.", instruction
					);
				}
				
				break;
			case AssemblerInstruction.SUBROUTINE:
				aliases.put(alias, instruction.memoryIndex);
				Log.error("SUB: "+alias+":"+instruction.memoryIndex);
				
				break;
			case AssemblerInstruction.DEFINITION:
				try {
					int value = Integer.parseInt(instruction.tokens.get(1));
					if(value < 0) throw new NumberFormatException();
					Log.error("DEF: "+alias+":"+value);
					aliases.put(alias, value);
				} catch(NumberFormatException e) {
					syntaxError(
						"Invalid numeric literal \""+instruction.tokens.get(1)+"\" when defining assembler definition "
						+"\""+alias+"\"! Must be a number above 0.", instruction
					);
				}
		}
	}
}
