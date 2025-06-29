package net.toydotgame.TRC3emu;

/**
 * Simple logging wrapper that handles logging, log levels, and errors
 */
public class Log {
	/**
	 * Log nothing
	 */
	public static final int NOTHING = -1;
	/**
	 * Log only fatal errors when exiting
	 */
	public static final int FATAL = 0;
	/**
	 * Log normal <i>and</i> fatal errors
	 */
	public static final int ERROR = 1;
	/**
	 * Log normal user information output and errors
	 */
	public static final int NORMAL = 2;
	/**
	 * Log line-by-line output, user information, and errors
	 */
	public static final int VERBOSE = 3;
	
	// Log level if #setLogLevel(int) isn't called
	private static int logLevel = NORMAL;
	/**
	 * Default exit code for fatal errors that don't specify otherwise
	 */
	private static final int DEFAULT_EXIT = 2;
	
	/**
	 * Set log level
	 * @param level Log level to set to
	 * @see Log#NOTHING
	 * @see Log#FATAL
	 * @see Log#ERROR
	 * @see Log#NORMAL
	 * @see Log#VERBOSE
	 */
	public static void setLogLevel(int level) {
		logLevel = level;
		debug("Set log level to: " + level);
	}
	
	/**
	 * Prints an exception message and stack trace without throwing an error, then
	 * quits
	 * @param message Exception message
	 * @param exitCode Exit code to quit with
	 */
	public static void fatalError(String message, int exitCode) {
		if(logLevel < FATAL) System.exit(exitCode);
		
		Exception e = new Exception(message);
		System.err.println("[FATAL]:");
		e.printStackTrace();
		System.exit(exitCode);
	}
	/**
	 * Calls {@code fatalError(message, DEFAULT_EXIT)}
	 * @see Log#fatalError(String, int)
	 * @see Log#DEFAULT_EXIT
	 */
	public static void fatalError(String message) {
		fatalError(message, DEFAULT_EXIT);
	}
	
	/**
	 * Akin to {@link Log#fatalError(String, int)}, but doesn't print a stack
	 * trace
	 * @param message Message to print
	 * @param exitCode Exit code
	 * @see Log#fatalError(String, int)
	 */
	public static void exit(String message, int exitCode) {
		if(logLevel < FATAL) System.exit(exitCode);
		
		System.err.println("[FATAL] "+message);
		System.exit(exitCode);
	}
	/**
	 * Calls {@code exit(message, DEFAULT_EXIT)}
	 * @see Log#exit(String, int)
	 * @see Log#DEFAULT_EXIT
	 */
	public static void exit(String message) {
		exit(message, DEFAULT_EXIT);
	}
	
	/**
	 * Prints an error to the standard error stream
	 * @param message Message to print
	 */
	public static void error(String message) {
		if(logLevel < ERROR) return;
		
		System.err.println("[ERROR] "+message);
	}
	
	/**
	 * Prints user information to the standard output stream
	 * @param message Message to print
	 */
	public static void log(String message) {
		if(logLevel < NORMAL) return;
		
		System.out.println(" [INFO] "+message);
	}
	
	/**
	 * Prints granular information to the standard output stream
	 * @param message Message to print
	 */
	public static void debug(String message) {
		if(logLevel < VERBOSE) return;
		
		System.out.println("[DEBUG] "+message);
	}
}
