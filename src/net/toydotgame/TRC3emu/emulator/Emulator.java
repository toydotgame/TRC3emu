package net.toydotgame.TRC3emu.emulator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.toydotgame.TRC3emu.Log;
import net.toydotgame.TRC3emu.Main;
import net.toydotgame.TRC3emu.Utils;

public class Emulator {
	/**
	 * Program counter, counts 0–1023.
	 */
	public static int pc;
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
	 * Stack.
	 * @see Stack
	 */
	public static Stack stack = new Stack();
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
	/**
	 * Carry and zero flags. Initialised to {@code false} (does not mirror
	 * Minecraft).
	 */
	public static boolean C, Z;
	/**
	 * Stores the current page for memory reads, used by the memory
	 * read/{@code REA} instruction.
	 */
	private static int page; // Init on page 0
	public static Clip bell; // Expose Clip instance for logic in Main
	/**
	 * Enables the "terminal mode" for the emulator. When <b>disabled</b>, and
	 * the emulator hits a {@code GPI} or {@code GPO} instruction, the emulator
	 * will prompt the user for a numerical input to give to a certain port. The
	 * user can then enter this number and hit <i>Enter</i> to input it.<br>
	 * <br>
	 * When <b>enabled</b> (i.e. <i>in terminal mode</i>), no such prompt will
	 * appear. When a GPI instruction is hit, the emulator will open a {@link
	 * java.util.Scanner Scanner} and read in a single ASCII character, and pass
	 * this as a byte to the port the program is reading from.<br>
	 * <br>
	 * For both cases, the output procedure is vice-versa: in non-terminal mode,
	 * numbers will be echoed out through the normal information logger, in
	 * terminal mode, values will be converted to ASCII and echoed out through
	 * {@link java.lang.System#out System.out}.<br>
	 * <br>
	 * If an invalid u8 byte (non-terminal mode) or non-ASCII character
	 * (terminal mode) is input, the emulator will ignore this and simply prompt
	 * again.
	 */
	public static boolean terminalMode;
	private static Scanner scanner = new Scanner(System.in);
	private static Terminal terminal;
	
	@SuppressWarnings("unused") // Purely for the warning when CLOCK_SPEED is -1
	public static void main(List<Integer> memory) {
		// Load memory into class:
		ram = memory;
		// Create terminal if needed: Will spawn a window
		if(terminalMode) terminal = new Terminal();
		
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
					jump(operands);
					break;
				case 14: // BEQ, aka branch if $Z
					if(Z) jump(operands);
					break;
				case 15: // BNE, aka branch if !$Z
					if(!Z) jump(operands);
					break;
				case 16: // BGT, aka branch if $C
					if(C) jump(operands);
					break;
				case 17: // BLT, aka branch if !$C
					if(!C) jump(operands);
					break;
				case 18: // CAL
					stack.push(pc+1);
					jump(operands);
					break;
				case 19: // RET
					// Even though jump jumps to the desired instruction #, it
					// expects an 11-bit operand reading, so we shift to 11-bit:
					jump(stack.pop()<<1);
					break;
				case 20: // REA
				case 21: // STO
					a = regfile.read(operands>>6&0x7);
					imm = operands>>3&0x7;
					c = operands&0x7;
					
					if(opcode == 20) regfile.write(c, fetchByte(page, a+imm));
					else writeByte(page, a+imm, regfile.read(c));
					break;
				case 22: // GPI
					imm = operands>>3&0x7;
					c = operands&0x7;
					
					regfile.write(c, gpioInput(imm));
					break;
				case 23: // GPO
					Log.fatalError("GPIO instructions not yet implemented!"); // TODO
					break;
				case 24: // BEL
					bell();
					break;
				case 25: // PAS
					imm = operands>>8&0x7;
					b = regfile.read(operands>>3&0x7);
					page = imm|b; // Cannot exceed 7
					Log.error("PAS: "+page);
					break;
				case 26: // PAG
					c = operands&0x7;
					regfile.write(c, page);
					break;
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
		
		if(pc == 1024) Log.debug("Reached end of memory!");
	}
	
	/**
	 * Fetches a byte using an absolute address. Designed for use in system
	 * internals rather than by the instruction set.
	 * @param address Byte address from 0–2047
	 * @return Memory value from that address
	 * @see #fetchByte(int, int)
	 */
	private static int fetchByte(int address) {
		return ram.get(address)&0xFF; // Sanitise just in case memory value isn't reliable
	}
	/**
	 * Fetches a byte using a page number and address (probably from a
	 * register). This one <i>is</i> designed for use by the instruction set.
	 * @param page Page, 0–7
	 * @param address Byte address from 0–255 within page
	 * @return Memory value from that address
	 * @see #fetchByte(int)
	 */
	private static int fetchByte(int page, int address) {
		return ram.get((page<<8)+address)&0xFF;
	}
	
	/**
	 * Writes a value to RAM. Designed for use by the instruction set.
	 * @param page Page, 0–7
	 * @param address Byte address from 0–255 within page
	 * @param value Byte to write
	 * @see #fetchByte(int, int)
	 */
	private static void writeByte(int page, int address, int value) {
		Log.error("Writing value "+(value&0xFF)+" to address "+Integer.toBinaryString((page<<8)+address));
		ram.set((page<<8)+address, value&0xFF);
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
	
	/**
	 * Set PC to desired instruction # (target {@code instruction} (0–1023)is
	 * equivalent to desiring to jump to memory address {@code instruction<<1}
	 * (0–2047).<br>
	 * <br>
	 * This method will set the PC to one <b>below</b> the desired target,
	 * because in {@link #main(List)}, there's a {@code pc++} call regardless of
	 * the instruction called.
	 * @param instruction Program counter value to jump to
	 */
	private static void jump(int instruction) {
		// Account for pc++ run each time: This does not mirror Minecraft
		pc = (instruction>>1)-1;
	}
	
	/**
	 * Ring the bell sound ({@code ring.wav}) when called. This method will
	 * stall in a busy-loop for the time it takes for {@link
	 * javax.sound.sampled.Clip Clip} to begin playing the sound. The purpose of
	 * this stall is so that any check—be it immediately after a call to this
	 * method or much later, will see that the bell is playing if we have called
	 * it, rather than seeing {@code false} for a small amount of time before
	 * {@link javax.sound.sampled.DataLine#start()} is able to dispatch it.<br>
	 * <br>
	 * In {@link Main#emulate()}, there is a call right before exiting to check
	 * if {@link #bell}{@link javax.sound.sampled.DataLine#isRunning()
	 * .isRunning()} is {@code true}, and if so, it will busy-loop until that
	 * value becomes {@code false}, when the program finally exits.
	 * @see Main#stallUntilAudioDone(boolean)
	 */
	private static void bell() {
		AudioInputStream source = null; // Make compiler happy
		try {
			source = AudioSystem.getAudioInputStream( // Create sample stream from URL
				Main.class.getResource("/ring.wav") // Yield URL of JAR resource
			);
		} catch (UnsupportedAudioFileException e) {
			Log.fatalError("BEL sound effect is not a wave file!");
		} catch (IOException e) {
			Log.fatalError("Couldn't find BEL sound effect in JAR file!"
				+"Did you make sure to add `media/` to the build path?");
		}
		
		try {
			// Load the sample as a Clip, meaning it is entirely loaded into memory
			// rather than streamed and played live:
			bell = AudioSystem.getClip();
			bell.open(source); // Load sample from input stream
		} catch (LineUnavailableException e) {
			Log.fatalError("Line not available to play BEL sound!");
		} catch (IOException e) {
			Log.fatalError("Couldn't find BEL sound effect in JAR file!" // Duplicate of above
				+"Did you make sure to add `media/` to the build path?");
		}
		
		bell.start();
		
		while(true) { // Busy loop TERRIBLE!!!
			if(bell.isRunning()) break;
			
			try {
				Thread.sleep(1); // At least slow down busy loop a bit
			} catch (InterruptedException e) {} // User probably killed us, so the bell crashing is desired behaviour. Don't create an error 
		}
	}
	
	private static int gpioInput(int port) {
		if(terminalMode) {
			// TODO: Request char input from terminal UI, find a way to block
			// here until that happens
			
			terminal.in();
			
			return 0; // TODO
		}
				
		int input = -1;
		while(true) {
			try {
				Log.gpioPrompt("Input for port "+port+": ");
				input = Integer.parseInt(scanner.nextLine());
				if(input < 0 || input > 255) throw new NumberFormatException();
				break;
			} catch(NumberFormatException e) {
				Log.error("Invalid input! Enter a decimal byte (0–255).");
			}
		}
		
		return input;
	}
}
