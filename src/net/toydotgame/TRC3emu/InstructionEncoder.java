package net.toydotgame.TRC3emu;

import static net.toydotgame.TRC3emu.OldUtils.ALU;
import static net.toydotgame.TRC3emu.OldUtils.IMM10;
import static net.toydotgame.TRC3emu.OldUtils.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.OldUtils.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.OldUtils.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.OldUtils.NONE;
import static net.toydotgame.TRC3emu.OldUtils.REG_ONLY;
import static net.toydotgame.TRC3emu.OldUtils.REG_TO_IMM3;

public class InstructionEncoder {
	public static String main(String line) {
		// Unsafe casts to int because null values would've killed us in
		// InstructionValidator anyways before we got here
		int opcode = OldUtils.getOpcode(line);
		int type = OldUtils.getType(opcode);
		int[] args = OldUtils.getArgs(line);

		switch(type) {
			case NONE:
				return OldUtils.paddedBinary(opcode, 5) + OldUtils.paddedBinary(0, 11);
			case ALU:
				return encodeALU(opcode, args);
			case IMM8_TO_REG:
				return encodeImm8ToReg(opcode, args);
			case IMM10:
				return encodeImm10(opcode, args);
			case IMM3_TO_REG:
				return encodeImm3ToReg(opcode, args);
			case REG_TO_IMM3:
				return encodeRegToImm3(opcode, args);
			case IMM3_OR_REG:
				return encodeImm3OrReg(opcode, args);
			case REG_ONLY:
				return encodeRegOnly(opcode, args);
			default:
				// This should be COMPLETELY unreachable, but I'm adding this
				// just to keep the compiler happy
				System.err.println("Unknown error occured when trying to encode an instruction of an unkown type!");
				System.exit(2);
				return null;
		}
	}

	private static String encodeALU(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += "00";
		
		if(args.length == 2) { // RSH
			instruction += OldUtils.paddedBinary(args[0], 3);
			instruction += "000";
			instruction += OldUtils.paddedBinary(args[1], 3);
			
			return instruction;
		}
		
		instruction += OldUtils.paddedBinary(args[0], 3);
		instruction += OldUtils.paddedBinary(args[1], 3);
		instruction += OldUtils.paddedBinary(args[2], 3);
		
		return instruction;
	}

	private static String encodeImm8ToReg(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += OldUtils.paddedBinary(args[0], 8);
		instruction += OldUtils.paddedBinary(args[1], 3);
		
		return instruction;
	}

	private static String encodeImm10(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += OldUtils.paddedBinary(args[0], 10);
		instruction += "0";
		
		return instruction;
	}

	private static String encodeImm3ToReg(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += "00000";
		instruction += OldUtils.paddedBinary(args[0], 3);
		instruction += OldUtils.paddedBinary(args[1], 3);
		
		return instruction;
	}

	private static String encodeRegToImm3(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += "00";
		instruction += OldUtils.paddedBinary(args[0], 3);
		instruction += OldUtils.paddedBinary(args[1], 3);
		instruction += "000";
		
		return instruction;
	}

	private static String encodeImm3OrReg(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += OldUtils.paddedBinary(args[0], 3);
		instruction += "00";
		instruction += OldUtils.paddedBinary(args[1], 3);
		instruction += "000";
		
		return instruction;
	}

	private static String encodeRegOnly(int opcode, int[] args) {
		String instruction = OldUtils.paddedBinary(opcode, 5);
		instruction += "00000000";
		instruction += OldUtils.paddedBinary(args[0], 3);
		
		return instruction;
	}
}
