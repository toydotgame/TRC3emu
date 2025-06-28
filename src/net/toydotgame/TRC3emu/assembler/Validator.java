package net.toydotgame.TRC3emu.assembler;

import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.ALU;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.DEFINITION;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.IMM10;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.INSTRUCTION;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.INVALID;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.NONE;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.REG_ONLY;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.REG_TO_IMM3;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.SUBROUTINE;
import static net.toydotgame.TRC3emu.assembler.AssemblerInstruction.VARIABLE;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.toydotgame.TRC3emu.Utils;

/**
 * Class to handle validating new instructions' token lengths, syntax for
 * aliases, and numeric overflows for operands.
 */
public class Validator {
	// Map of opcode to its operand type:
	private static Map<Integer, Integer> instructionTypes = loadInstructionTypes();
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
	 * Sketchy dispatcher method
	 * @param instruction Instruction to define the type of
	 * @return The {@link AssemblerInstruction#type} of the line passed in
	 */
	public static int main(AssemblerInstruction instruction) {
		// I know this is ugly
		if(instruction.text().endsWith(":")) { // Subroutine
			if(validateAlias(instruction, true)) {
				// instructionCounter holds the index of the next instruction, so
				// don't modify it, but do set the index of the subroutine to it:
				instruction.memoryIndex = AssemblerInstruction.instructionCounter<<1;
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
	 * as it appears in the {@link AssemblerInstruction} object passed in. Will
	 * print {@link Assembler#syntaxError(String, AssemblerInstruction)}s if
	 * checks fail 
	 * @param instruction The source line to check
	 * @param subroutine If the instruction is of type {@link
	 * AssemblerInstruction#SUBROUTINE} or not
	 * @return {@code true} if the alias has correct token length and a non-
	 * digital name, {@code false} if either of the two aforementioned checks fail
	 */
	private static boolean validateAlias(AssemblerInstruction instruction, boolean subroutine) {
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
	 * AssemblerInstruction#instructionType}. Specifically, this method <b>
	 * checks that the number of arguments <u>only</u> is correct</b>.<br>
	 * <br>
	 * The control flow of {@link #main(AssemblerInstruction)} seems to suggest
	 * that comments can appear as the input instruction to this method, however
	 * the value of {@link AssemblerInstruction#tokens} is linted using {@link
	 * AssemblerInstruction#removeComments(String)}, therefore the text of this
	 * instruction will be the instruction only, or {@code ""} if the line was
	 * just a comment.<br>
	 * <br>
	 * This returns {@code false} if the length of the instruction's text is
	 * less than 3 (the minimum length of a lone opcode mnemonic), meaning
	 * <i>finally</i> that comments' types are set to {@link
	 * AssemblerInstruction#INVALID}, in turn meaning that assembly operations
	 * on instances that were comments will silently fail in further parsing.
	 * @param instruction {@link AssemblerInstruction} instance, of type
	 * {@link AssemblerInstruction#INSTRUCTION}
	 * @return {@code true} if syntactically correct, {@code false} otherwise
	 */
	private static boolean validateInstruction(AssemblerInstruction instruction) {
		// Mnemonics alone are at least 3 chars long, so anything less fails
		int length = instruction.text().length();
		if(length < 3) {
			// Don't raise a syntax error for comment lines:
			if(length != 0) Assembler.syntaxError("Invalid instruction!", instruction);
			return false;
		}
		
		// Type is obviously INSTRUCTION,
		instruction.opcode = parseOpcode(instruction);
		instruction.instructionType = instructionTypes.get(instruction.opcode);
		instruction.memoryIndex = AssemblerInstruction.instructionCounter++<<1; // Set to counter<<1, then incr. counter
		
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
			Assembler.syntaxError(
				"Wrong number of tokens! Should be "+desiredSize+", found "+actualSize+".", instruction
			);
			return false;
		}
		return true;
	}
	
	/**
	 * Takes the first element in this instruction's {@link #tokens} List, and
	 * looks it up in a lookup table.
	 * @param instruction {@link AssemblerInstruction} instance
	 * @return Numeric opcode, or {@code null} if not found
	 */
	private static Integer parseOpcode(AssemblerInstruction instruction) {
		return AssemblerInstruction.opcodes.get(instruction.tokens.get(0).toUpperCase());
	}
	
	public static boolean validateOverflows(Integer opcode, List<Integer> operands) {
		// TODO: Implement. We know now the provided instruction always has a
		// fixed operand list size, and that all aliases handled. This function
		// requires very little error handling regarding those cases as such
		return true;
	}
}
