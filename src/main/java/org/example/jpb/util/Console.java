package org.example.jpb.util;

public final class Console {

	private static final String RESET = "\u001B[0m";
	private static final String RED = "\u001B[31m";
	private static final String GREEN = "\u001B[32m";
	private static final String YELLOW = "\u001B[33m";
	private static final String BLUE = "\u001B[34m";
	private static final String GRAY = "\u001B[90m";

	private Console() {
		System.out.println("Console must not be instantiated");
	}

	public static void print(String message) {
		System.out.println(message);
	}

	public static void info(String message) {
		System.out.println(BLUE + message + RESET);
	}

	public static void success(String message) {
		System.out.println(GREEN + message + RESET);
	}

	public static void error(String message) {
		System.err.println(RED + message + RESET);
	}

	public static void warn(String message) {
		System.out.println(YELLOW + message + RESET);
	}

	public static void debug(String message) {
		System.out.println(GRAY + message + RESET);
	}

	public static String blue(String text) {
		return BLUE + text + RESET;
	}

	public static String green(String text) {
		return GREEN + text + RESET;
	}

	public static String red(String text) {
		return RED + text + RESET;
	}

	public static String gray(String text) {
		return GRAY + text + RESET;
	}
}
