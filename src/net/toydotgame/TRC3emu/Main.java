package net.toydotgame.TRC3emu;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
	public static Map<String, Integer> instSet;
	public static String[] mnemonics = {
		"NOP", "HLT", "JMP", "CAL", // No OP, HaLT, JuMP, CALl
		"RET", "BRA", "LDI", "ADI", // RETurn, BRAnch, LoaD Immediate, ADd Immediate
		"LOD", "STO", "ADD", "SUB", // LOaD from memory, STOre to memory, ADD, SUBtract
		"RSH", "NOR", "AND", "XOR"  // Right SHift, Bitwise (NOR, AND, XOR)
	};
	
	public static void main(String[] args) {
		instSet = setupInstSet();
		Scanner in = new Scanner(System.in);
		
		printStatus();
		String[] input = getInput(in);
		while(input.length > 0 && Computer.running) {
			input = parseInput(input);
			Instruction.dispatch(input);
			printStatus();
			
			if(Computer.running) input = getInput(in);
		}
		in.close();
	}
	
	private static Map<String, Integer> setupInstSet() {
		Map<String, Integer> set = new HashMap<>();
		
		for(int i = 0; i < mnemonics.length; i++)
			set.put(mnemonics[i], i);
		
		return set;
	}
	
	private static String[] getInput(Scanner scanner) {
		System.out.print("\nEnter instruction: ");
		String input = scanner.nextLine().replaceAll("\\s+", " ").trim();
		
		if(input.length() == 0) return new String[] {};
		
		return input.split(" ");
	}
	
	private static String[] parseInput(String[] input) {
		for(int i = 0; i < input.length; i++)
			if(instSet.containsKey(input[i])) input[i] = instSet.get(input[i]).toString();
		return input;
	}
	
	private static void printStatus() {
		System.out.println();
		
		System.out.println("PC: " + String.valueOf(Computer.pc));
		
		System.out.print("Stack: {");
		for(int i = 0; i < Computer.stack.length; i++) {
			String pointer = String.valueOf(Computer.stack[i]);
			if(pointer.equals("0")) break;
			
			if(i == 0) System.out.print(pointer);
			else System.out.print(", " + pointer);
		}
		System.out.println("}");
		
		System.out.println("Register file (0x):");
		for(int i = 0; i < Computer.r.length; i++) { 
			String value = Integer.toHexString(Computer.r[i]).toUpperCase();
			if(value.length() == 1) value = " " + value;
			System.out.print(value);
			
			if(i%4 != 3) System.out.print(" ");
			else System.out.println();
		}
	}
}
