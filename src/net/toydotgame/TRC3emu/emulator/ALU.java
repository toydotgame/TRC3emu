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
		switch(operation) {
			case ADD:
				regfile.write(c, a+b);
				break;
			case SUB:
				regfile.write(c, a+~(b)+1); // Manual two's complement because I don't trust Java
				break;
			case XOR:
				regfile.write(c, a^b);
				break;
			case XNO:
				regfile.write(c, ~(a^b));
				break;
			case IOR:
				regfile.write(c, a|b);
				break;
			case NOR:
				regfile.write(c, ~(a|b));
				break;
			case AND:
				regfile.write(c, a&b);
				break;
			case NAN:
				regfile.write(c, ~(a&b));
				break;
			case RSH:
				regfile.write(c, (a+b)>>1);
				break;
			default:
				Log.fatalError("Unimplemented ALU operation with code `"+operation+"`!");
		}
	}
	
	private static int[] decodeOperands(int args) {
		return new int[]{
			args>>6&0x7,
			args>>3&0x7,
			args&0x7
		};
	}
}
