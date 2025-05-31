package net.toydotgame.TRC3emu;

public class Instruction extends Computer {

	public static void dispatch(String[] args) {
		String opcode = "";
		try {
			opcode = Main.mnemonics[Integer.valueOf(args[0])];
		} catch(NumberFormatException e) {}
		
		switch(opcode) {
			case "NOP": NOP();                          break;
			case "HLT": HLT();                          break;
			case "JMP": JMP(args[1]);                   break;
			case "CAL": CAL(args[1]);                   break;
			case "RET": RET();                          break;
			case "BRA": BRA(args[1], args[2]);          break;
			case "LDI": LDI(args[1], args[2]);          break;
			case "ADI": ADI(args[1], args[2]);          break;
			case "LOD": LOD(args[1], args[2], args[3]); break;
			case "STO": STO(args[1], args[2], args[3]); break;
			case "ADD": ADD(args[1], args[2], args[3]); break;
			case "SUB": SUB(args[1], args[2], args[3]); break;
			case "RSH": RSH(args[1], args[2]); break;
			case "NOR": NOR(args[1], args[2], args[3]); break;
			case "AND": AND(args[1], args[2], args[3]); break;
			case "XOR": XOR(args[1], args[2], args[3]); break;
			default:
				System.err.println("Invalid opcode! Try again");
		}
		
		if(opcode != ""    // Non-pc-incrementing instructions:
		&& opcode != "HLT"
		&& opcode != "JMP"
		&& opcode != "CAL"
		&& opcode != "RET"
		&& opcode != "BRA") pc++;
	}
	
	static void NOP() {
		// Do…uh…nothing
	}
	
	static void HLT() {
		System.out.println("COMPUTER HALTED! Status:");
		Computer.running = false;
	}
	
	static void JMP(String addr) {
		pc = Integer.valueOf(addr);
	}
	
	static void CAL(String addr) {
		push(pc+1);
		pc = Integer.valueOf(addr);
	}
	
	static void RET() {
		if(stack[0] == 0) pc++; // Effectively a NOP with an empty stack
		else pc = pop();
	}
	
	static void BRA(String cond, String addr) {
		boolean branch = false;
		switch(cond) {
			case "eq":
				if(flags[0]) branch = true;
			case "neq":
				if(!flags[0]) branch = true;
			case "c":
				if(flags[1]) branch = true;
			case "nc":
				if(!flags[1]) branch = true;
		}
		
		if(branch) pc = Integer.valueOf(addr);
		else pc++;
	}
	
	static void LDI(String reg, String imm) {
		int dest = Integer.valueOf(reg.substring(1));
		r[dest] = memRead(Integer.valueOf(imm));
	}
	
	static void ADI(String reg, String imm) {
		int dest = Integer.valueOf(reg.substring(1));
		r[dest] += Integer.valueOf(imm);
	}
	
	static void LOD(String reg, String addrReg, String offset) {
		int src = Integer.valueOf(addrReg.substring(1));
		int dest = Integer.valueOf(reg.substring(1));
		int data = memRead(r[src]+Integer.valueOf(offset));
		r[dest] = data;
	}
	
	static void STO(String reg, String addrReg, String offset) {
		int dest = Integer.valueOf(addrReg.substring(1))+Integer.valueOf(offset);
		int data = r[Integer.valueOf(reg.substring(1))];
		memWrite(dest, data);;
	}
	
	static void ADD(String regA, String regB, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int b = r[Integer.valueOf(regB.substring(1))];
		int c = a+b;
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
	
	static void SUB(String regA, String regB, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int b = r[Integer.valueOf(regB.substring(1))];
		int c = a-b;
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
	
	static void RSH(String regA, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int c = a >>> 2;
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
	
	static void NOR(String regA, String regB, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int b = r[Integer.valueOf(regB.substring(1))];
		int c = ~(a|b);
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
	
	static void AND(String regA, String regB, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int b = r[Integer.valueOf(regB.substring(1))];
		int c = a&b;
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
	
	static void XOR(String regA, String regB, String outReg) {
		int a = r[Integer.valueOf(regA.substring(1))];
		int b = r[Integer.valueOf(regB.substring(1))];
		int c = a^b;
		r[Integer.valueOf(outReg.substring(1))] = c;
	}
}
