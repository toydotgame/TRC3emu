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
	/**
	 * Approximate clock speed in Hz. This value is used for the additional
	 * <i>delay</i> per instruction, because the processing time Java takes
	 * per instruction is negligible.<br>
	 * <br>
	 * To match TRC3's processing speed, a value of approximately {@code 1d/12}
	 * is needed.<br>
	 * <br>
	 * If entering a literal of the form {@code 1/x} Hz (to yield a speed of
	 * {@code x} seconds per clock instead of {@code x} Hz/clocks per second),
	 * <b>remember to type cast the constant {@code 1} to double with something
	 * like {@code 1.0} or {@code 1d}, otherwise Java will concatenate the
	 * result of the division to an {@code int}</b>!
	 */
	private static final double CLOCK_SPEED = 1000;
	
	@SuppressWarnings("unused") // Purely for the warning when CLOCK_SPEED is -1
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
				case 2: // ADD
					ALU.main(operands, ALU.ADD);
					break;
				case 3: // ADI
					imm = operands>>3;
					c = operands&0x7;
					regfile.write(c,
						regfile.read(c)+imm
					);
					break;
				case 4: // SUB
					ALU.main(operands, ALU.SUB);
					break;
				case 5: // XOR
					ALU.main(operands, ALU.XOR);
					break;
				case 6: // XNO
					ALU.main(operands, ALU.XNO);
					break;
				case 7: // IOR
					ALU.main(operands, ALU.IOR);
					break;
				case 8: // NOR
					ALU.main(operands, ALU.NOR);
					break;
				case 9: // AND
					ALU.main(operands, ALU.AND);
					break;
				case 10: // NAN
					ALU.main(operands, ALU.NAN);
					break;
				case 11: // RSH
					ALU.main(operands, ALU.RSH);
					break;
				case 12: // LDI
					imm = operands>>3;
					c = operands&0x7;
					regfile.write(c, imm);
					break;
				case 13: // JMP
					imm = operands>>1;
					pc = imm-1; // Account for pc++ run each time. This does not mirror Minecraft
					break;
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
			
			if(CLOCK_SPEED > 0) {
				try {
					Thread.sleep((long)(1000/CLOCK_SPEED));
				} catch (InterruptedException e) {
					Log.exit("User killed the emulator.", 0);
				}
			} else if(CLOCK_SPEED == 0) {
				Log.log("Clock speed is set to 0. Effectively halted.");
				break;
			}
			
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
