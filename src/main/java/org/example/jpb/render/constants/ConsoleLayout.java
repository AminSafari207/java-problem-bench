package org.example.jpb.render.constants;

public final class ConsoleLayout {

	public static final int DEFAULT_WIDTH = 80;

	public static final int INDENT_CASE_SET = 1;
	public static final int INDENT_TEST_CASE = 2;
	public static final int INDENT_DETAIL = 3;

	public static final int SOLUTION_NAME_WIDTH = 24;
	public static final int CASE_SET_NAME_WIDTH = 22;

	public static final int BENCHMARK_STATUS_WIDTH = 9;
	public static final int BENCHMARK_SOLUTION_WIDTH = 24;
	public static final int BENCHMARK_SAMPLES_WIDTH = 10;
	public static final int BENCHMARK_TIME_WIDTH = 11;

	private ConsoleLayout() {
		throw new AssertionError("No instances");
	}
}
