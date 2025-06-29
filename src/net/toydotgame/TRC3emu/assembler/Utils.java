package net.toydotgame.TRC3emu.assembler;

import java.util.Collections;

public class Utils {
	/**
	 * Repeats a given character a given amount of times.
	 * @param copies Number of times to repeat
	 * @param c Character to repeat
	 * @return String consisting of {@code copies} copies of character {@code c}
	 */
	public static String nChars(int copies, char c) {
		if(copies < 0) copies = 0;
		return String.join("", Collections.nCopies(copies, String.valueOf(c)));
	}
	
	/**
	 * Returns if a certain String value is comprised of solely digits and nothing
	 * else.
	 * @param str String to check
	 * @return {@code true} if digits-only, {@code false} otherwise
	 */
	public static boolean isDigital(String str) {
		return str.matches("[0-9]+");
	}
	
	/**
	 * Returns a padded binary representation of the input number.
	 * @param x Input number. If this value is negative or doesn't fit within
	 * {@code len} bits, then the return value <b>will</b> be too long!
	 * @param len Number of bits in output String
	 * @return {@code x} as a binary String, left-padded with zeroes
	 * @see Utils#paddedHex(int, int)
	 */
	public static String paddedBinary(int x, int len) {
		if(len <= 0) len = 1; // String.format() requires a non-zero value
		
		return String.format("%"+len+"s", Integer.toBinaryString(x))
			.replace(" ", "0");
	}
	
	/**
	 * Returns a padded hexadecimal representation of the input number.
	 * @param x Inpuit number. If this value is negative or doesn't fit within
	 * {@code len} nybbles ({@code len/2} bytes), then the return value <b>will</b> be too long!
	 * @param len Number of nybbles in the output String
	 * @return {@code x} as a hexadecimal String, left-padded with zeroes
	 * @see Utils#paddedBinary(int, int)
	 */
	public static String paddedHex(int x, int len) {
		if(len <= 0) len = 1;
		
		return String.format("%0"+len+"X", x);
	}
}
