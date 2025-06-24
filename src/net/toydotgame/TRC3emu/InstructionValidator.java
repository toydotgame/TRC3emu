package net.toydotgame.TRC3emu;

import static net.toydotgame.TRC3emu.Utils.ALU;
import static net.toydotgame.TRC3emu.Utils.IMM10;
import static net.toydotgame.TRC3emu.Utils.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.Utils.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.Utils.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.Utils.NONE;
import static net.toydotgame.TRC3emu.Utils.REG_ONLY;
import static net.toydotgame.TRC3emu.Utils.REG_TO_IMM3;

public class InstructionValidator {	
	public static String main(String line) {
		Integer opcode = Utils.getOpcode(line);
		Integer type = Utils.getType(opcode);
		if(type == null) { // Being a little over-protectice and safe with this handling but idc
			Utils.printAssemberSyntaxErr(line, "Instruction type not set for the instruction \"" + line.split(" ")[0] + "\"! (Opcode " + opcode + ")");
			System.exit(2); // printErrForLine() changes the syntaxError count but we don't care because this is a fatal internal error
		}
		int[] args = Utils.getArgs(line);
		
		switch(type) {
			case NONE:
				return String.valueOf(opcode);
			case ALU:
				return validateALU(opcode, args);
			case IMM8_TO_REG:
				return validateImm8ToReg(opcode, args);
			case IMM10:
				return validateImm10(opcode, args);
			case IMM3_TO_REG: // All different binaries, but same validation conditions
			case REG_TO_IMM3:
				return validateImm3Reg(opcode, args);
			case IMM3_OR_REG:
				return validateImm3OrReg(opcode, args);
			case REG_ONLY:
				return validateRegOnly(opcode, args);
			default:
				// This should never be reached normally, because it means
				// `type` is not null (handled above) but also not accounted for
				// /implemented in this switch() case
				Utils.printAssemberSyntaxErr(line, "Type constant " + type + " invalid!");
				System.exit(2);
				return null; // Make compiler happy
		}
	}
	
	private static String validateALU(int opcode, int[] args) {
		if(opcode != 11 && failArgsLength(3, args)) return null;
		else if(args.length != 2 && args.length != 3) { // RSH case
			Utils.printAssemberSyntaxErr("Wrong number of arguments for instruction! Should be 2 or 3, found " + args.length + ".");
			return null;
		} // Else, is a valid RSH or other ALU instruction
		if(failUnderOverflow(7, args)) return null;
		
		return constructInstruction(opcode, args);
	}
	
	private static String validateImm8ToReg(int opcode, int[] args) {
		if(failArgsLength(2, args)) return null;
		if(failUnderOverflow(255, args[0])) return null;
		if(failUnderOverflow(7, args[1])) return null;
		
		return constructInstruction(opcode, args);
	}
	
	private static String validateImm10(int opcode, int[] args) {
		if(failArgsLength(1, args)) return null;
		if(failUnderOverflow(1023, args)) return null;
		
		return constructInstruction(opcode, args);
	}
	
	private static String validateImm3Reg(int opcode, int[] args) {
		if(failArgsLength(2, args)) return null;
		if(failUnderOverflow(7, args)) return null;
		
		return constructInstruction(opcode, args);
	}
	
	private static String validateImm3OrReg(int opcode, int[] args) {
		if(args.length == 1) {
			// Assume for 1 operand passed to a PAS instruction
			// that the programmer is specifying a register, imm=0
			args = new int[] { 0, args[0] };
		} else if(args.length <= 0 || args.length > 2) {
			// Special error message for PAS instead of
			// validateImm3Reg()'s 2 argument specification
			Utils.printAssemberSyntaxErr("Wrong number of arguments for instruction! Should be 1 or 2, found " + args.length + ".");
			return null;
		}
		
		return validateImm3Reg(opcode, args);
	}
	
	private static String validateRegOnly(int opcode, int[] args) {
		if(failArgsLength(1, args)) return null;
		if(failUnderOverflow(7, args)) return null;
		
		return constructInstruction(opcode, args);
	}
	
	// Return false if args[] is desired length, otherwise print error and return false
	private static boolean failArgsLength(int length, int[] args) {
		if(args.length == length) return false;
		
		Utils.printAssemberSyntaxErr("Wrong number of arguments for instruction! Should be " + length + ", found " + args.length + ".");
		return true;
	}
	
	// Return false if every arg is witin unsigned range defined by `length`,
	// print error and return false otherwise
	private static boolean failUnderOverflow(int max, int[] args) {
		for(int i = 0; i < args.length; i++) {
			if(args[i] < 0 || args[i] > max) {
				Utils.printAssemberSyntaxErr(args[i] + " out of bounds! Should be within 0-" + max + " inclusive.");
				return true;
			}
		}
		
		return false;
	}
	// Overload for passing int instead of int[] args
	private static boolean failUnderOverflow(int max, int arg) {
		int[] args = { arg };
		return failUnderOverflow(max, args);
	}
	
	// Return a string of the full instruction from integers
	private static String constructInstruction(int opcode, int[] operands) {
		String str = String.valueOf(opcode);
		for(int i = 0; i < operands.length; i++)
			str += " " + operands[i];
		
		return str;
	}
}
