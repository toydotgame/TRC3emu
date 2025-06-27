package net.toydotgame.TRC3emu.assembler;

public class Validator {
	/**
	 * Sketchy dispatcher method
	 * @param instruction Instruction to define the type of
	 * @return The {@link AssemblerInstruction#type} of the line passed in
	 */
	public static int main(AssemblerInstruction instruction) {
		// I know this is ugly
		if(instruction.getText().endsWith(":")) { // Subroutine
			if(validateAlias(instruction, true))
				return AssemblerInstruction.SUBROUTINE;
			else return AssemblerInstruction.INVALID;
		} else if(instruction.getText().startsWith(".")) { // Variable
			if(validateAlias(instruction, false))
				return AssemblerInstruction.VARIABLE;
			else return AssemblerInstruction.INVALID;
		} else if(instruction.getText().startsWith("#")) { // Definition
			if(validateAlias(instruction, false))
				return AssemblerInstruction.DEFINITION;
			else return AssemblerInstruction.INVALID;
		} else { // Instruction or otherwise
			if(validateInstruction(instruction))
				return AssemblerInstruction.INSTRUCTION;
			else return AssemblerInstruction.INVALID;
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
		String alias = instruction.getText();
		if(subroutine) alias = alias.substring(0, alias.length());
		else alias = alias.substring(1);
		
		if(alias.matches("[0-9]+")) {
			Assembler.syntaxError("Alias name shouldn't be digits-only!", instruction);
			return false;
		}
		
		// Valid token length and name, we don't know if it's already defined however
		return true;
	}
	
	private static boolean validateInstruction(AssemblerInstruction instruction) {
		// Mnemonics alone are at least 3 chars long, so anything less fails
		if(instruction.getText().length() < 3) return false;
		
		// TODO: Implement
		// Remember valid comments can be passed in here, which _should_ be INVALID
		// type, but should **not** raise a syntaxError() because comments are,
		// well, syntactically valid...
		return true;
	}
}
