package net.toydotgame.TRC3emu.assembler;

import java.util.ArrayList;
import java.util.List;
import net.toydotgame.utils.Log;
import net.toydotgame.utils.Utils;
import static net.toydotgame.TRC3emu.assembler.Instruction.NONE;
import static net.toydotgame.TRC3emu.assembler.Instruction.ALU;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM8_TO_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM10;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM3_TO_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.REG_TO_IMM3;
import static net.toydotgame.TRC3emu.assembler.Instruction.IMM3_OR_REG;
import static net.toydotgame.TRC3emu.assembler.Instruction.REG_ONLY;

/**
 * Encoder to convert instructions to binary, rearrange instruction words as
 * 2-bytes of little-Endian-encoded memory, and convert data to 8-bit words too
 */
public class Encoder {
	/**
	 * Takes in the {@link Assembler#main(List)}{@code .program} List (list of completed
	 * instructions), and the {@link Assembler#variables} List (list of
	 * numeric constants ∈ (ℤ ∩ [0, 255]).<br>
	 * <br>
	 * This method will encode the instructions found in {@code programData}
	 * into 8-bit words that represent the little-Endian encoding of a 16-bit
	 * instruction word for TRC3. Additionally, it will encode each value in
	 * {@code variableData} as an 8-bit word and place <i>that</i> list at the
	 * end of the program listing.
	 * @param program List of validated, completely numerical instructions
	 * @param variables List of assembly variables
	 * @return Final memory map of the assembled program
	 */
	public static List<String> main(List<Instruction> program, List<Integer> variables) {
		// Encode every instruction: Previous iterations will yield two entries
		// where once once was, so increment each time by 2
		List<String> encodedProgram = new ArrayList<String>();
		for(Instruction instruction : program) {
			String[] encodedInstruction = encodeInstruction(instruction);
			encodedProgram.add(encodedInstruction[1]); // Lo byte
			encodedProgram.add(encodedInstruction[0]); // Hi byte
		}
		
		// Convert variables from 0-255 values to 8-bit words:
		List<String> encodedVariables = new ArrayList<String>();
		for(int value : variables) // We know variableData is all ints
			encodedVariables.add(Utils.paddedBinary(value, 8));
		
		List<String> binary = new ArrayList<String>(encodedProgram);
		binary.addAll(encodedVariables);
		return binary;
	}

	/**
	 * Given a fully validated and prepared {@link Instruction} instance, this
	 * method encodes that to a 16-bit instruction word, split into two
	 * single-byte Strings in a {@code String[]}.
	 * @param instruction {@link Instruction} instance, post-processing
	 * @return {@code String[2]}, where index {@code 0} corresponds to the hi
	 * byte/most significant 8 bits, and index {@code 1} corresponds to the lo
	 * byte/least significant 8 bits in the instruction word
	 */
	private static String[] encodeInstruction(Instruction instruction) {
		String instructionStr = "";
		
		instructionStr += Utils.paddedBinary(instruction.opcode, 5);
		
		List<Integer> args = instruction.operandInts;
		switch(instruction.instructionType) {
			case NONE:
				instructionStr += Utils.paddedBinary(0, 11);
				
				break;
			case ALU:
				instructionStr += "00";
				for(int i = 0; i < 3; i++)
					instructionStr += Utils.paddedBinary(args.get(i), 3);
				
				break;
			case IMM8_TO_REG:
				instructionStr += Utils.paddedBinary(args.get(0), 8);
				instructionStr += Utils.paddedBinary(args.get(1), 3);
				
				break;
			case IMM10:
				instructionStr += Utils.paddedBinary(args.get(0), 10);
				instructionStr += "0";
				
				break;
			case IMM3_TO_REG:
				instructionStr += "00000";
				instructionStr += Utils.paddedBinary(args.get(0), 3);
				instructionStr += Utils.paddedBinary(args.get(1), 3);
				
				break;
			case REG_TO_IMM3:
				instructionStr += "00";
				instructionStr += Utils.paddedBinary(args.get(0), 3);
				instructionStr += Utils.paddedBinary(args.get(1), 3);
				instructionStr += "000";
				
				break;
			case IMM3_OR_REG:
				instructionStr += Utils.paddedBinary(args.get(0), 3);
				instructionStr += "00";
				instructionStr += Utils.paddedBinary(args.get(1), 3);
				instructionStr += "000";
				
				break;
			case REG_ONLY:
				instructionStr += "00000000";
				instructionStr += Utils.paddedBinary(args.get(0), 3);
				
				break;
			default:
				Log.fatalError("Instruction type for opcode `"+instruction.opcode+"` unimplemented!");
		}
		
		if(instructionStr.length() != 16)
			Log.exit("Resulting instruction word is not 2 bytes!: "+instructionStr);
		
		String[] instructionBytes = new String[] {
			instructionStr.substring(0, 8), instructionStr.substring(8)
		};
		
		return instructionBytes;
	}
}
