package net.toydotgame.TRC3emu.assembler;

import static net.toydotgame.TRC3emu.assembler.Instruction.ALU;
import static net.toydotgame.TRC3emu.assembler.Instruction.DEFINITION;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM10;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.INSTRUCTION;
import static net.toydotgame.TRC3emu.assembler.Instruction.INVALID;
import static net.toydotgame.TRC3emu.assembler.Instruction.NONE;
import static net.toydotgame.TRC3emu.assembler.Instruction.REG_ONLY;
import static net.toydotgame.TRC3emu.assembler.Instruction.REG_TO_IMM3;
import static net.toydotgame.TRC3emu.assembler.Instruction.SUBROUTINE;
import static net.toydotgame.TRC3emu.assembler.Instruction.VARIABLE;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.toydotgame.utils.Log;
import net.toydotgame.utils.Utils;

/**
 * Class to handle validating new instructions' token lengths, syntax for
 * aliases, and numeric overflows for operands.
 */
public class Validator {
	/**
	 * Sketchy dispatcher method
	 * @param instruction Instruction to define the type of
	 * @return The {@link Instruction#type} of the line passed in
	 */
	public static int main(Instruction instruction) {
		// I know this is ugly
		if(instruction.text().endsWith(":")) { // Subroutine
			if(validateAlias(instruction, true)) {
				// instructionCounter holds the index of the next instruction, so
				// don't modify it, but do set the index of the subroutine to it:
				instruction.memoryIndex = Instruction.instructionCounter<<1;
				return SUBROUTINE;
			}
			else return INVALID;
		} else if(instruction.text().startsWith(".")) { // Variable
			if(validateAlias(instruction, false)) return VARIABLE;
			else return INVALID;
		} else if(instruction.text().startsWith("#")) { // Definition
			if(validateAlias(instruction, false)) return DEFINITION;
			else return INVALID;
		} else { // Instruction or otherwise
			if(validateInstruction(instruction)) return INSTRUCTION;
			else return INVALID;
		}
	}
	
	/**
	 * Checks the validity of an alias' (subroutine/variable/definition) syntax
	 * as it appears in the {@link Instruction} object passed in. Will
	 * print {@link Assembler#syntaxError(String, Instruction)}s if
	 * checks fail 
	 * @param instruction The source line to check
	 * @param subroutine If the instruction is of type {@link
	 * Instruction#SUBROUTINE} or not
	 * @return {@code true} if the alias has correct token length and a non-
	 * digital name, {@code false} if either of the two aforementioned checks fail
	 */
	private static boolean validateAlias(Instruction instruction, boolean subroutine) {
		// Check if the number of tokens in the instruction is what we want:
		int desiredTokens = 2;
		if(subroutine) desiredTokens = 1;
		
		if(instruction.tokens.size() != desiredTokens) {
			Assembler.syntaxError("Invalid number of tokens in alias definition!", instruction);
			return false;
		}
		
		// Check if the name of the instruction is valid (non-digital):
		String alias = instruction.text().toLowerCase();
		if(subroutine) instruction.alias = alias.substring(0, alias.length()-1);
		else instruction.alias = alias.split(" ", 2)[0].substring(1);
		
		if(Utils.isDigital(instruction.alias)) {
			Assembler.syntaxError("Alias name shouldn't be digits-only!", instruction);
			return false;
		}
		
		// Valid token length and name, we don't know if it's already defined however
		return true;
	}
	
	/**
	 * Validates the syntax is correct for the given {@link
	 * Instruction#instructionType}. Specifically, this method <b>
	 * checks that the number of arguments <u>only</u> is correct</b>.<br>
	 * <br>
	 * The control flow of {@link #main(Instruction)} seems to suggest
	 * that comments can appear as the input instruction to this method, however
	 * the value of {@link Instruction#tokens} is linted using {@link
	 * Instruction#removeComments(String)}, therefore the text of this
	 * instruction will be the instruction only, or {@code ""} if the line was
	 * just a comment.<br>
	 * <br>
	 * This returns {@code false} if the length of the instruction's text is
	 * less than 3 (the minimum length of a lone opcode mnemonic), meaning
	 * <i>finally</i> that comments' types are set to {@link
	 * Instruction#INVALID}, in turn meaning that assembly operations
	 * on instances that were comments will silently fail in further parsing.
	 * @param instruction {@link Instruction} instance, of type
	 * {@link Instruction#INSTRUCTION}
	 * @return {@code true} if syntactically correct, {@code false} otherwise
	 */
	private static boolean validateInstruction(Instruction instruction) {
		// Mnemonics alone are at least 3 chars long, so anything less fails
		int length = instruction.text().length();
		if(length < 3) {
			// Don't raise a syntax error for comment lines:
			if(length != 0) Assembler.syntaxError("Invalid instruction!", instruction);
			return false;
		}
		
		// Type is obviously INSTRUCTION,
		instruction.opcode = parseOpcode(instruction);
		instruction.instructionType = Instruction.instructionTypes.get(instruction.opcode);
		if(instruction.instructionType == null) {
			/* In this case, we have a non-instruction, non-comment, and non-
			 * alias input. It is very likely garbled data or not TRC3 assembly.
			 * This is some kind of syntax error/unimplemented opcode
			 */
			Assembler.syntaxError("Invalid instruction!", instruction);
			return false;
		}
		instruction.memoryIndex = Instruction.instructionCounter++<<1; // Set to counter<<1, then incr. counter
		
		// Count of desired tokens in instruction, including opcode:
		Map<Integer, Integer> desiredTokenCounts = new HashMap<Integer, Integer>();	
		desiredTokenCounts.put(NONE, 1);
		desiredTokenCounts.put(ALU, 4);
		desiredTokenCounts.put(IMM8_TO_REG, 3);
		desiredTokenCounts.put(IMM10, 2);
		desiredTokenCounts.put(IMM3_TO_REG, 3);
		desiredTokenCounts.put(REG_TO_IMM3, 3);
		desiredTokenCounts.put(IMM3_OR_REG, 3);
		desiredTokenCounts.put(REG_ONLY, 2);
		
		// Get token counts:
		int actualSize = instruction.tokens.size();
		int desiredSize = desiredTokenCounts.get(instruction.instructionType);
		boolean isValid = false;		
		
		// Case-by-case validity checking: Also see Assembler#validateOperands()
		switch(instruction.opcode) {
			case 11: // RSH
				if(actualSize == 3 || actualSize == 4) isValid = true;
				break;
			case 25: // PAS
				if(actualSize == 2 || actualSize == 3) isValid = true;
				break;
			default: // All other instructions:
				if(actualSize == desiredSize) isValid = true;
				break;
		}
		
		// Raise syntaxError() if invalid, return true otherwise
		if(!isValid) {
			desiredSize -= 1; // Adjust to number of arguments, not total tokens
			actualSize -= 1;
			Assembler.syntaxError(
				"Wrong number of arguments for instruction! Should be "+desiredSize+", found "+actualSize+".", instruction
			);
			return false;
		}
		return true;
	}
	
	/**
	 * Takes the first element in this instruction's {@link Instruction#tokens}
	 * List, and looks it up in a lookup table.
	 * @param instruction {@link Instruction} instance
	 * @return Numeric opcode, or {@code null} if not found
	 */
	private static Integer parseOpcode(Instruction instruction) {
		return Utils.opcodes.get(instruction.tokens.get(0).toUpperCase());
	}
	
	/**
	 * Given by now, we have validated the number of operands for this
	 * instruction instance, and that all operands are numeric, all we need to
	 * do now is check that all operands are within the bounds of 0 to some
	 * maximum value.<br>
	 * <br>
	 * This method takes in an instruction instance, checks its opcode against
	 * the {@link Instruction#instructionTypes} lookup table, takes a List of
	 * numeric operands (already tested as numeric and provided by {@link
	 * Assembler#validateOverflows(Instruction)}, and checks that they
	 * are {@code >= 0} and also {@code <=} some integer maximum allowed value,
	 * in order to check they will fit in the designated <i>n</i> bits in the
	 * encoded instruction.<br>
	 * <br>
	 * After this instruction has been validated here, it is safe to assume it
	 * is <b>completely valid</b> and ready for encoding.
	 * @param instruction {@link Instruction} instance, needed to yield
	 * {@link Instruction#opcode} instance field and also passed along
	 * to {@link Assembler#syntaxError(String, Instruction)} for the
	 * cases of invalid syntax
	 * @return {@code true} if the instruction has in-bounds operands, {@code
	 * false} otherwise
	 * @see Validator#failUnderOverflow(List, int, Instruction)
	 */
	public static boolean validateOverflows(Instruction instruction) {
		List<Integer> args = instruction.operandInts;
		switch(instruction.instructionType) {
			case NONE:
				return true; // No operands to check
			case ALU:
			case IMM3_TO_REG:
			case REG_TO_IMM3:
			case IMM3_OR_REG:
			case REG_ONLY:
				return failUnderOverflow(args, 7, instruction);
			case IMM8_TO_REG:
				if(!failUnderOverflow(args.get(0), 255, instruction))
					return false;
				if(!failUnderOverflow(args.get(1), 7, instruction))
					return false;
				
				return true;
			case IMM10:
				return failUnderOverflow(args, 1023, instruction);
			default:
				Log.fatalError("Instruction type for opcode `"+instruction.opcode+"` unimplemented!");
		}
		return true;
	}
	
	/**
	 * Checks that the input List of integers are within the range {@code 0}–
	 * {@code max} (inclusive). Will throw an error if any value to check is
	 * {@code null}, however this method is called at {@link
	 * Validator#validateOverflows(Instruction)}, and no value in the input
	 * List there may be {@code null} because {@link
	 * Assembler#validateOverflows(Instruction)} catches that case.
	 * @param args List of integers to check
	 * @param max Maximum value allowed
	 * @param instruction {@link Instruction} instance (for syntax
	 * error logging)
	 * @return {@code true} if all values are within the allowed range, {@code
	 * false} otherwise
	 */
	private static boolean failUnderOverflow(List<Integer> args, int max, Instruction instruction) {
		for(Integer arg : args) {
			if(arg < 0 || arg > max) {
				Assembler.syntaxError("Value `"+arg+"` out of bounds! Must be 0-"+max+".", instruction);
				return false;
			}
		}
		
		return true;
	}
	/**
	 * Mirrors {@link
	 * Validator#failUnderOverflow(List, int, Instruction)}'s function.
	 * Checks a value is within the range {@code 0}–{@code max} (inclusive).
	 * @param arg Integer to check
	 * @param max Maximum value allowed
	 * @param instruction {@link Instruction} instance (for syntax
	 * error logging)
	 * @return {@code true} if value is within the allowed range, {@code false}
	 * otherwise
	 * @see Validator#failUnderOverflow(List, int, Instruction)
	 */
	private static boolean failUnderOverflow(Integer arg, int max, Instruction instruction) {
		if(arg < 0 || arg > max) {
			Assembler.syntaxError("Value `"+arg+"` out of bounds! Must be 0-"+max+".", instruction);
			return false;
		}
		
		return true;
	}
}
