package com.festp;

public class Logger {
	private static java.util.logging.Logger wrapped = null;
	
	public static void setLogger(java.util.logging.Logger logger) {
		if (wrapped == null)
			wrapped = logger;
	}
	
	public static void info(String msg) {
		wrapped.info(msg);
	}
	
	public static void warning(String msg) {
		wrapped.warning(msg);
	}
	
	public static void severe(String msg) {
		wrapped.severe(msg);
	}
}
