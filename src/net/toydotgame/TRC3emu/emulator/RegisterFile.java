package net.toydotgame.TRC3emu.emulator;

import java.util.ArrayList;
import java.util.List;

public class RegisterFile {
	private int[] dataRegisters = new int[7];
	
	/**
	 * Write to a register (0–7). Register 0 writes will do nothing.
	 * @param address Register address to write to
	 * @param data Data word (8-bit) to write
	 * @see #read(int)
	 */
	public void write(int address, int data) {
		if(address == 0) return;
		address--; // Shift index down by 1
		dataRegisters[address] = data&0xFF;
	}
	
	/**
	 * Read from a register (0–7). {@code r0} is the <b>zero register</b>,
	 * meaning it will only read a value of {@code 0x0}.
	 * @param address Address to read from
	 * @return {@code 0} if {@code address=0}, 8-bit unsigned integer in the
	 * desired register otherwise
	 */
	public int read(int address) {
		if(address == 0) return 0;
		address--; // Shift index down
		return dataRegisters[address];
	}
	
	public String enumerate() {
		List<String> values = new ArrayList<String>();
		for(int i = 1; i <= 7; i++)
			values.add("r"+i+"="+read(i));
		
		return "["+String.join(", ", values)+"]";
	}
}
