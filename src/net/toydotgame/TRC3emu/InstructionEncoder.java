package net.toydotgame.TRC3emu;

import static net.toydotgame.TRC3emu.Utils.ALU;
import static net.toydotgame.TRC3emu.Utils.IMM10;
import static net.toydotgame.TRC3emu.Utils.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.Utils.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.Utils.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.Utils.NONE;
import static net.toydotgame.TRC3emu.Utils.REG_ONLY;
import static net.toydotgame.TRC3emu.Utils.REG_TO_IMM3;

public class InstructionEncoder {
	public static String main(String line) {
		// Unsafe casts to int because null values would've killed us in
		// InstructionValidator anyways before we got here
		int opcode = Utils.getOpcode(line);
		int type = Utils.getType(opcode);
		int[] args = Utils.getArgs(line);

		switch(type) {
			case NONE:
				return Utils.paddedBinary(opcode, 5) + Utils.paddedBinary(0, 11);
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
				Utils.printErrForLine(null, "Unknown error occured when trying to encode an instruction of an unkown type!");
				System.exit(2);
				return null;
		}
	}

	private static String encodeALU(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += "00";
		
		if(args.length == 2) { // RSH
			instruction += Utils.paddedBinary(args[0], 3);
			instruction += "000";
			instruction += Utils.paddedBinary(args[1], 3);
			
			return instruction;
		}
		
		instruction += Utils.paddedBinary(args[0], 3);
		instruction += Utils.paddedBinary(args[1], 3);
		instruction += Utils.paddedBinary(args[2], 3);
		
		return instruction;
	}

	private static String encodeImm8ToReg(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += Utils.paddedBinary(args[0], 8);
		instruction += Utils.paddedBinary(args[1], 3);
		
		return instruction;
	}

	private static String encodeImm10(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += Utils.paddedBinary(args[0], 10);
		instruction += "0";
		
		return instruction;
	}

	private static String encodeImm3ToReg(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += "00000";
		instruction += Utils.paddedBinary(args[0], 3);
		instruction += Utils.paddedBinary(args[1], 3);
		
		return instruction;
	}

	private static String encodeRegToImm3(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += "00";
		instruction += Utils.paddedBinary(args[0], 3);
		instruction += Utils.paddedBinary(args[1], 3);
		instruction += "000";
		
		return instruction;
	}

	private static String encodeImm3OrReg(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += Utils.paddedBinary(args[0], 3);
		instruction += "00";
		instruction += Utils.paddedBinary(args[1], 3);
		instruction += "000";
		
		return instruction;
	}

	private static String encodeRegOnly(int opcode, int[] args) {
		String instruction = Utils.paddedBinary(opcode, 5);
		instruction += "00000000";
		instruction += Utils.paddedBinary(args[0], 3);
		
		return instruction;
	}
	
	private static String[] intArrToString(int[] arr) {
		String[] strArr = new String[arr.length];
		for(int i = 0; i < arr.length; i++)
			strArr[i] = String.valueOf(arr[i]);
		
		return strArr;
	}
}
