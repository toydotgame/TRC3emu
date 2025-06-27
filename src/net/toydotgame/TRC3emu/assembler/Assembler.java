package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.toydotgame.TRC3emu.Log;
import net.toydotgame.TRC3emu.OldAssembler;

public class Assembler {
	private static int syntaxErrors = 0;
	
	/**
	 * Assembles a source file and returns its binary representation
	 * @param source Input file lines
	 * @return Output file lines
	 */
	public static List<String> main(List<String> source) {
		List<String> binary = new ArrayList<String>();
		
		for(int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			AssemblerInstruction instruction = new AssemblerInstruction(line, i);
			
			// TODO: Do stuff with AssemblerInstruction methods
			
			if(instruction.type != AssemblerInstruction.INVALID)
				binary.add(instruction.getText());
		}
		
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
		Log.error(line);
		Log.error(String.join("", Collections.nCopies(line.length(), "^")));
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
}
