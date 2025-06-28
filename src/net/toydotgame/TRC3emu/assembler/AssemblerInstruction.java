package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.toydotgame.TRC3emu.Log;

/**
 * Class to hold a single line of assembly source code and its methods to
 * operate on itself.
 */
public class AssemblerInstruction {
	// Instance fields:
	/**
	 * List containing the space-separated parts of each line.
	 */
	public List<String> tokens;
	/**
	 * Line # the instruction came from (starts at 1 if {@code
	 * AssemblerInstruction(String, 0) was used, for example).
	 */
	public Integer lineIndex;
	/**
	 * Type of the source line. If set to {@code AssemblerInstruction#INVALID},
	 * operations on an instance of this class will silently fail without raising
	 * any errors.
	 */
	public int type = INVALID;
	/**
	 * If {@link #type} is {@link #INSTRUCTION}, then this
	 * field is set. This can be linked to the {@link
	 * AssemblerInstruction#instructionTypes} Map to see the mapping between
	 * opcode and its type.
	 */
	public Integer instructionType;
	/**
	 * Integer opcode of instruction. If {@link #type} is {@code null}, then
	 * this remains unset.
	 */
	public Integer opcode;
	/**
	 * Unique instruction # of this new instance.
	 */
	public Integer memoryIndex;
	/**
	 * If this instance is an alias (variable, subroutine, or definition), then
	 * this value will be populated with its name.
	 */
	public String alias;
	
	// Assembly source types:
	/**
	 * Syntax-errored line, will not be operated on.
	 */
	public static final int INVALID = -1;
	/**
	 * Machine code instruction.
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
	
	// Instruction types:
	public static final int NONE = 0;
	public static final int ALU = 1;
	public static final int IMM8_TO_REG = 2;
	public static final int IMM10 = 3;
	public static final int IMM3_TO_REG = 4;
	public static final int REG_TO_IMM3 = 5;
	public static final int IMM3_OR_REG = 6;
	public static final int REG_ONLY = 7;
	
	// Opcodes:
	public static Map<String, Integer> opcodes = loadOpcodes();
	private static Map<String, Integer> loadOpcodes() {
		// Convert String[] to a map of the value at String[i] to the index i:
		Map<String, Integer> map = new HashMap<String, Integer>();
		String[] mnemonics = {
			"NOP", "HLT", "ADD", "ADI",
			"SUB", "XOR", "XNO", "IOR",
			"NOR", "AND", "NAN", "RSH",
			"LDI", "JMP", "BEQ", "BNE",
			"BGT", "BLT", "CAL", "RET",
			"REA", "STO", "GPI", "GPO",
			"BEL", "PAS", "PAG"
		};
		
		for(int i = 0; i < mnemonics.length; i++) map.put(mnemonics[i], i);
		
		return map;
	}
	
	/**
	 * Holds the address of the next instruction. When an instruction is
	 * sucessfully parsed, its {@link #memoryIndex} is set to the value
	 * held here, then this counter is incremented.<br>
	 * <br>
	 * Assignments to the {@link #memoryIndex} value of an instance should
	 * always take the form of:
	 * {@code
	 * this.memoryIndex = instructionCounter<<1;
	 * }
	 */
	public static int instructionCounter = 0;
	
	/**
	 * Creates a new {@link AssemblerInstruction} object
	 * @param line Line of source to parse
	 * @param index Index # of this line in the source (starting at 0)
	 */
	public AssemblerInstruction(String line, int index) {
		this.tokens = tokenize(removeComments(line));
		this.lineIndex = index+1;
		
		// Define source line type or raise syntax error:
		this.type = Validator.main(this);
		
		// TODO: Remove when no longer needed
		Log.debug("New instruction created: \""+this.getText()+"\"");
		Log.debug("\tType: "+this.type);
		if(this.type != INSTRUCTION) return;
		// These will be null if not INSTRUCTION:
		Log.debug("\tInstruction type: "+this.instructionType);
		Log.debug("\tOpcode: "+this.opcode);
		Log.debug("\tIndex: "+this.memoryIndex);
	}
	
	/**
	 * Removes comments (if any) from source line String.
	 * @param line Source line from assembly file
	 * @return Line without comments, trimmed of whitespace—or the original
	 * source line if no comments were found
	 */
	private static String removeComments(String line) {
		int commentIndex = line.indexOf(";");
		
		if(commentIndex == -1) return line; // No comment found
		return line.substring(0, commentIndex).trim();
	}
	
	/**
	 * Splits the source line String into a List of the line's parts. In TRC3
	 * assembly, instructions, variables, and definitions are delimited by
	 * spaces.
	 * @param line Pre-linted/comment-removed line of assembly
	 * @return List where each element is an individual token in the source
	 * instruction. Order is preserved
	 */
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

	/**
	 * Substitute the first token in {@code this.}{@link #tokens} to the numeric
	 * value from the lookup table {@link AssemblerInstruction#opcodes}.
	 * Silently returns void if this is not an instruction. Raises fatal error
	 * if {@code this} is an instruction but is not found in the {@code opcodes}
	 * lookup table.
	 */
	public void substituteOpcode() {
		if(this.type != INSTRUCTION) return;
		
		// Numeric opcode as String:
		Integer opcode = opcodes.get(tokens.get(0).toUpperCase());
		if(opcode == null) {
			Assembler.syntaxError("No opcode found for this instruction!", this);
		}
		this.tokens.set(0, String.valueOf(opcode));
	}
	
	/**
	 * Hands off to {@link Validator#validateOverflows(AssemblerInstruction)}.
	 * The return value of that, if {@code false}, is used to set the {@link
	 * #type} of this instance to {@link #INVALID}. Otherwise, nothing is done.
	 * <br><br>
	 * An instruction's validity is determined by two things:
	 * <ol>
	 * 	<li>There are the correct number of arguments for this instruction. This
	 * is handled by {@link Validator#validateInstruction(AssemblerInstruction)}
	 * and {@link Validator#validateAlias(AssemblerInstruction)}</li>
	 * 	<li>Each argument (operand) fits within the number of bits allocated in
	 * the instruction word format. This is handled here</li>
	 * </ol>
	 * @see Validator#validateOverflows(AssemblerInstruction)
	 * @see Validator#validateAlias(AssemblerInstruction)
	 * @see Validator#validateInstruction(AssemblerInstruction)
	 */
	public void validate() {
		boolean isValid = Validator.validateOverflows(this);
		if(!isValid) this.type = INVALID;
	}
}
