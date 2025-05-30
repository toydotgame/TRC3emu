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
		System.out.print("Enter instruction: ");
		String[] inst = in.nextLine().replaceAll("\\s+", " ").trim().split(" ");
		in.close();
		
		for(int i = 0; i < inst.length; i++) {
			String t = inst[i];
			if(instSet.containsKey(t)) inst[i] = instSet.get(t).toString();
		}
		
		System.out.println(String.join("_", inst));
	}
	
	private static Map<String, Integer> setupInstSet() {
		Map<String, Integer> set = new HashMap<>();
		
		for(int i = 0; i < mnemonics.length; i++)
			set.put(mnemonics[i], i);
		
		return set;
	}
}
