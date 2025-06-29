package net.toydotgame.TRC3emu.emulator;

import java.util.List;
import net.toydotgame.TRC3emu.Log;
import net.toydotgame.TRC3emu.assembler.Utils;

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
	
	public static void main(List<Integer> memory) {
		// Load memory into class:
		ram = memory;
		
		while(opcode != 1 && pc < 1024) {
			ir = fetchInstruction();
			opcode = decodeOpcode();
			
			Log.debug(
				"FETCH @ "+Utils.paddedHex(pc<<1, 4)+": "
				+Utils.paddedBinary(ir>>8, 8)+" "+Utils.paddedBinary(ir&0xFF, 8)
				+" (opcode="+opcode+")");
			
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
		return (ir&0xF800)>>11; // Mask top 5 bits, right shift by 11 bits
	}
}
