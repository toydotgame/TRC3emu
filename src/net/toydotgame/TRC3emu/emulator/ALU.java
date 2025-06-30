package net.toydotgame.TRC3emu.emulator;

import net.toydotgame.TRC3emu.Log;

public class ALU extends Emulator {
	// ALU operations:
	public static final int ADD = 0;
	public static final int SUB = 1;
	public static final int XOR = 2;
	public static final int XNO = 3;
	public static final int IOR = 4;
	public static final int NOR = 5;
	public static final int AND = 6;
	public static final int NAN = 7;
	public static final int RSH = 8;
	
	public static void main(int args, int operation) {
		int[] operands = decodeOperands(args);
		
		int a = regfile.read(operands[0]);
		int b = regfile.read(operands[1]);
		int c = operands[2];
		int output = 0;
		Log.debug("a: "+a+", b: "+b+", c: "+c);
		// For all of these operations, we can assume and rely on the fact that
		// register values MUST be ∈ [0, 255]
		switch(operation) {
			case ADD:
				output = a+b; // Set output to set flags
				
				break;
			case SUB:
				// Manual two's complement implementation because a Java int is
				// an s32 rather than a u8:
				output = a+(~b&0xFF)+1;
				
				break;
			case XOR:
				output = a^b; // Cannot exceed 255
				// Implement edge case for Minecraft flood-carry XOR:
				if(a > b) output += 0x100; // Set carry bit (dangerous, requires output < 256)
				
				break;
			case XNO:
				output = ~(a^b)&0xFF; // Can exceed 255 but we snip it
				if(a+b > 0xFF) output += 0x100; // Edge case for flood-carry in Minecraft
				
				break;
			case IOR:
				output = a|b; // Cannot exceed 255
				
				break;
			case NOR:
				/* Like IOR, in Minecraft the u8 OR cannot exceed 255, however
				 * Java's implementation of the bitwise NOT obviously treats
				 * this as an u32 and flips all 32 bits, which we don't want,
				 * so we snip:
				 */
				output = ~(a|b)&0xFF;
				
				break;
			case AND:
				output = a&b; // Minecraft XOR→OR function again, ∴ cannot exceed 255
				
				break;
			case NAN:
				// Manual Minecraft ALU implementation of NAND operation, that
				// ensures an easy way of no overflows
				output = (~a&0xFF)&(~b&0xFF);
				
				break;
			case RSH: // Handle flag setting differently than all other operations:
				output = ((a+b)&0xFF)>>1; // RSH implementation does _not_ let Cout = MSB
				Emulator.C = a+b > 0xFF; // If adder output exceeds 255, set carry
				Emulator.Z = (output&0xFF) == 0; // If only 8 output bits are 0, set zero
				
				regfile.write(c, output);
				return;
			default:
				Log.fatalError("Unimplemented ALU operation with code `"+operation+"`!");
		}
		
		regfile.write(c, output);
		setFlags(output);
	}
	
	private static int[] decodeOperands(int args) {
		return new int[]{
			args>>6&0x7,
			args>>3&0x7,
			args&0x7
		};
	}
	
	private static void setFlags(int output) {
		Emulator.C = output > 0xFF; // If output exceeds 255, set carry
		Emulator.Z = (output&0xFF) == 0; // If only 8 output bits are 0, set zero
	}
}
