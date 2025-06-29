package net.toydotgame.TRC3emu.emulator;

import java.util.List;
import net.toydotgame.TRC3emu.Log;
import net.toydotgame.TRC3emu.Utils;

public class Emulator {
	/**
	 * Program counter, counts 0–1023.
	 */
	private static int pc;
	/**
	 * Instruction register, bottom two bytes forms the instruction.
	 */
	private static int ir;
	/**
	 * Class-kept copy of memory, initialised in {@link #main(List)}.
	 */
	private static List<Integer> ram;
	/**
	 * Hold the value of the opcode, decoded with {@link #decodeOpcode()}.
	 */
	private static int opcode;
	/**
	 * Hold the value of the operands, decoded with {@link #decodeOperands()}.
	 */
	private static int operands;
	/**
	 * Register file.
	 * @see RegisterFile
	 */
	public static RegisterFile regfile = new RegisterFile();
	
	public static void main(List<Integer> memory) {
		// Load memory into class:
		ram = memory;
		
		while(opcode != 1 && pc < 1024) {
			ir = fetchInstruction();
			opcode = decodeOpcode();
			operands = decodeOperands();
			
			Log.debug("");
			Log.debug(
				"FETCH @ "+Utils.paddedHex(pc<<1, 4)+": "
				+Utils.paddedBinary(ir>>8, 8)+" "+Utils.paddedBinary(ir&0xFF, 8)
				+" (opcode="+opcode+","
				+" operands="+Utils.paddedBinary(operands, 11)+")"
			);
			
			int a, b, c, imm;
			switch(opcode) {
				case 0: // NOP
					break;
				case 1: // HLT
					break; // while() loop terminates if opcode is 1
				case 2:
					ALU.main(operands, ALU.ADD);
					break;
				case 3:
					imm = operands>>3;
					c = operands&0x3;
					regfile.write(c,
						regfile.read(c)+imm
					);
					break;
				case 4:
					ALU.main(operands, ALU.SUB);
					break;
				case 5:
					ALU.main(operands, ALU.XOR);
					break;
				case 6:
					ALU.main(operands, ALU.XNO);
					break;
				case 7:
					ALU.main(operands, ALU.IOR);
					break;
				case 8:
					ALU.main(operands, ALU.NOR);
					break;
				case 9:
					ALU.main(operands, ALU.AND);
					break;
				case 10:
					ALU.main(operands, ALU.NAN);
					break;
				case 11:
					ALU.main(operands, ALU.RSH);
					break;
				case 12:
					imm = operands>>3;
					c = operands&0x3;
					regfile.write(c,
						regfile.read(c)+imm
					);
					break;
				case 13:
				case 14:
				case 15:
				case 16:
				case 17:
				case 18:
				case 19:
				case 20:
				case 21:
				case 22:
				case 23:
				case 24:
				case 25:
				case 26:
				default:
					Log.fatalError("Unimplemented opcode `"+opcode+"`!");
			}
			
			Log.debug("EXECUTE DONE: "+regfile.enumerate());
			
			pc++;
		}
	}
	
	private static int fetchByte(int address) {
		return ram.get(address)&0xFF;
	}
	
	private static void writeByte(int address, int value) {
		ram.set(address, value&0xFF);
	}
	
	private static int fetchInstruction() {
		int instruction = fetchByte(pc<<1);
		instruction |= fetchByte((pc<<1)+1)<<8;
				
		return instruction;
	}
	
	private static int decodeOpcode() {
		return ir>>11; // Mask top 5 bits, right shift by 11 bits
	}
	
	private static int decodeOperands() {
		return ir&0x7FF; // Mask only bottom 11 bits
	}
}
