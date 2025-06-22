package net.toydotgame.TRC3emu;

public class Utils {
	public static String paddedBinary(int x, int len) {
		if(len <= 0) len = 1;
		// Very bodgily assuming unsigned:
		if(x < 0) x = 0;
		
		return String.format("%" + len + "s", Integer.toBinaryString(x)).replace(" ", "0");
	}
}
