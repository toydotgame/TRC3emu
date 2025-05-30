package net.toydotgame.TRC3emu;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
	static Map<String, Integer> instSet;
	static String[] mnemonics = {
		"NOP", "HLT", "JMP", "CAL",
		"RET", "BEQ"
	};
	
	public static void main(String[] args) {
		instSet = setupInstSet();
		Scanner in = new Scanner(System.in);
		
		String[] input = getInput(in);
		while(input.length > 0) {
			input = parseInput(input);
			System.out.println("{" + String.join(",", input) + "} is " + input.length + " elements long");
			input = getInput(in);
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
		System.out.print("Enter instruction: ");
		String input = scanner.nextLine().replaceAll("\\s+", " ").trim();
		if(input.length() == 0) {
			return new String[] {};
		}
		
		return input.split(" ");
	}
	
	private static String[] parseInput(String[] input) {
		for(int i = 0; i < input.length; i++)
			if(instSet.containsKey(input[i])) input[i] = instSet.get(input[i]).toString();
		return input;
	}
}
