package net.toydotgame.TRC3emu;

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
}
