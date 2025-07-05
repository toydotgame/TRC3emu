package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.toydotgame.utils.Utils;

/**
 * Class to hold a single line of assembly source code and its methods to
 * operate on itself.
 * @see #Instruction(String, int)
 */
public class Instruction {
	// Instance fields:
	/**
	 * List containing the space-separated parts of each line.
	 */
	public List<String> tokens;
	/**
	 * Line # the instruction came from (starts at 1 if {@code
	 * Instruction(String, 0)} was used, for example).
	 */
	public final Integer lineIndex;
	/**
	 * Type of the source line. If set to {@code Instruction#INVALID},
	 * operations on an instance of this class will silently fail without raising
	 * any errors.
	 */
	public int type = INVALID;
	/**
	 * If {@link #type} is {@link #INSTRUCTION}, then this
	 * field is set. This can be linked to the {@link
	 * Instruction#instructionTypes} Map to see the mapping between
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
	/**
	 * A copy of the original text (for syntax error pretty-printing).
	 */
	public final String originalText;
	/**
	 * Integer List of the operands of the instruction, set by {@link
	 * Assembler#validateOverflows(Instruction)}, and used by {@link
	 * Validator#validateOverflows(Instruction)} and {@link
	 * Encoder#encodeInstruction(Instruction)}
	 */
	public List<Integer> operandInts;
	
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
	 * @see Instruction#DEFINITION
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
	
	/**
	 * Map of opcodes to their designated instruction types.
	 */
	public static Map<Integer, Integer> instructionTypes = loadInstructionTypes();
	private static Map<Integer, Integer> loadInstructionTypes() {
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
	 * Creates a new {@link Instruction} object
	 * @param line Line of source to parse
	 * @param index Index # of this line in the source (starting at 0)
	 */
	public Instruction(String line, int index) {
		this.tokens = tokenize(removeComments(line));
		this.lineIndex = index;
		this.originalText = line;
		
		// Define source line type or raise syntax error:
		this.type = Validator.main(this);
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
	 * line. (String getter for {@link Instruction#tokens} kinda)
	 * @return The instruction line
	 */
	public String text() {
		return String.join(" ", this.tokens);
	}

	/**
	 * Substitute the first token in {@code this.}{@link #tokens} to the numeric
	 * value from the lookup table {@link net.toydotgame.utils.Utils#opcodes}.
	 * Silently returns void if this is not an instruction. Raises fatal error
	 * if {@code this} is an instruction but is not found in the {@code opcodes}
	 * lookup table.
	 */
	public void substituteOpcode() {
		if(this.type != INSTRUCTION) return;
		
		// Numeric opcode as String:
		Integer opcode = Utils.opcodes.get(tokens.get(0).toUpperCase());
		if(opcode == null) {
			Assembler.syntaxError("No opcode found for this instruction!", this);
		}
		this.tokens.set(0, String.valueOf(opcode));
	}
}
