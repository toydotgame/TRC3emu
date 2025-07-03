package net.toydotgame.TRC3emu.emulator;

import java.util.Arrays;
import net.toydotgame.utils.Log;

/**
 * Provides a 16-word deep stack interface.
 */
public class Stack {
	private int[] stack;
	
	public Stack() {
		this.stack = new int[16];
	}
	
	/**
	 * Pushes a <u>program counter</u> value (10-bit) to the stack.
	 * @param address 10-bit value to push
	 */
	public void push(int address) {
		if(stack[stack.length-1] != 0)
			Log.error("Stack overflow occured @ address "+(Emulator.pc<<1)+"!");
		
		for(int i = 1; i < this.stack.length; i++)
			this.stack[i] = this.stack[i-1];
		this.stack[0] = address&0x3FF;
		
		Log.log("STACK PUSHED: "+Arrays.toString(stack));
	}
	
	/**
	 * Pops the topmost value off the stack. Returns it and shifts everything 1
	 * back.
	 * @return Value from the top of the stack
	 */
	public int pop() {
		int sum = 0;
		for(int value : this.stack) sum += value;
		if(sum == 0)
			Log.error("Stack undeflow occured @ address "+(Emulator.pc<<1)+"!");
		
		int pop = this.stack[0];
		for(int i = 0; i < this.stack.length-1; i++)
			this.stack[i] = this.stack[i+1];
		
		Log.log("STACK POPPED: Got "+pop+", stack: "+Arrays.toString(stack));
		
		return pop;
	}
}
