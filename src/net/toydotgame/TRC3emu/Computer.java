package net.toydotgame.TRC3emu;

public class Computer {
	// Using `int`s instead of `byte`s because signing is annoying and I'm not bothered to deal with it.
	// This isn't a high-performance application
	public static boolean running = true;              // False halts
	public static int pc = 0;                          // Program counter
	public static int[] r = new int[16];               // 16 1-byte data registers
	public static int[][] fullMem = new int[256][256]; // Main memory, max 256 bytes addressible. Use banking for more
	public static int[] stack = new int[16];           // 16-byte call stack pointing to memory addrs.
	public static int bank = 0;                        // Memory bank ID, 0–255
	public static boolean[] flags = new boolean[8];    // [0] = Zero, [1] = Carry
	
	public static void push(int addr) {
		if(stack[15] > 0) System.err.println("Stack overflow!");
		
		for(int i = stack.length-1; i > 0; i--)	stack[i] = stack[i-1]; // Move everything down one. stack[15] gets lost
		stack[0] = addr;
	}
	
	public static int pop() {
		int value = stack[0];
		for(int i = 0; i < stack.length-1; i++) stack[i] = stack[i+1];
		stack[stack.length-1] = 0;
		
		return value;
	}
	
	public static int memRead(int addr) {
		return fullMem[bank][addr];
	}
	
	public static void memWrite(int addr, int value) {
		fullMem[bank][addr] = value;
	}
	
	public static void regWrite(String outReg, int value) {
		int dest = Integer.valueOf(outReg.substring(1)); 
		if(dest == 0) return;
		r[dest] = value;
	}
}
