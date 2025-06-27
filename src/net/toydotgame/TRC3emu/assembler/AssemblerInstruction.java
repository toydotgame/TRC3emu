package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.List;
import net.toydotgame.TRC3emu.Log;

/**
 * Class to hold a single line of assembly source code and its methods to
 * operate on itself
 */
public class AssemblerInstruction {
	// Instance fields:
	/**
	 * List containing the space-separated parts of each line
	 */
	public List<String> tokens;
	/**
	 * Line # the instruction came from (starts at 1 if {@code
	 * AssemblerInstruction(String, 0) was used, for example)
	 */
	public int lineIndex;
	/**
	 * Type of the source line. If set to {@code AssemblerInstruction#INVALID},
	 * operations on an instance of this class will silently fail without raising
	 * any errors
	 */
	public int type = INVALID;
	
	// Assembly instruction types:
	/**
	 * Syntax-errored line, will not be operated on
	 */
	public static final int INVALID = -1;
	/**
	 * Machine code instruction
	 */
	public static final int INSTRUCTION = 0;
	/**
	 * A variable for the machine. The assembler will place the value of this
	 * variable in the data space in memory, following the end of the program
	 * memory space. When substituting in references to this name, it will
	 * substitute in the memory address this value is located at (rather than its
	 * value, as a definition would) in the final memory space of the binary. A
	 * variable is defined in the same way as a definition, but with a {@code .}
	 * instead:<br>
	 * {@code .bar 12}
	 * @see AssemblerInstruction#DEFINITION
	 */
	public static final int VARIABLE = 1;
	/**
	 * A subroutine. Declared as a non-digit-only name with the line ending with a
	 * {@code :}. The only thing on that line must be that, with the instruction
	 * it points to on the following line.
	 */
	public static final int SUBROUTINE = 2;
	/**
	 * A definition for the assembler. They are declared in assembly as {@code
	 * #foo}. A definition is kept in the assembler's memory as a memorable name
	 * for a value that should be substituted in. A definition <i>must</i> be
	 * defined as a non-digit-only name and a numeric value:<br>
	 * {@code #foo 42}
	 */
	public static final int DEFINITION = 3;
	
	/**
	 * Creates a new {@link AssemblerInstruction} object
	 * @param line Line of source to parse
	 * @param index Index # of this line in the source (starting at 0)
	 */
	public AssemblerInstruction(String line, int index) {
		this.tokens = tokenize(removeComments(line));
		this.lineIndex = index+1;
		
		// Define instruction type or raise syntax error:
		this.type = Validator.main(this);
		
		Log.debug("New instruction created: \""+this.getText()+"\", which has type "+this.type);
	}
	
	private static String removeComments(String line) {
		int commentIndex = line.indexOf(";");
		
		if(commentIndex == -1) return line; // No comment found
		return line.substring(0, commentIndex).trim();
	}
	
	private static List<String> tokenize(String line) {
		String[] lineArr = line.split(" ");
		List<String> list = new ArrayList<String>();
		for(int i = 0; i < lineArr.length; i++) list.add(lineArr[i]);
		
		return list;
	}
	
	/**
	 * Returns a reconstruction of the partially- or fully-reconstructed source
	 * line. (String getter for {@link AssemblerInstruction#tokens} kinda)
	 * @return The instruction line
	 */
	public String getText() {
		return String.join(" ", this.tokens);
	}
}
