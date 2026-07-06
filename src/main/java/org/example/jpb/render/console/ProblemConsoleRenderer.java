package org.example.jpb.render.console;

import java.util.Arrays;
import org.example.jpb.core.model.*;
import org.example.jpb.util.Console;

public class ProblemConsoleRenderer {

	private static final int WIDTH = 80;

	//#################################
	//## Problem Correctness ##########
	//#################################

	public void renderProblemResult(ProblemResult result) {
		renderProblemHeader(result);

		for (SolutionResult solution : result.solutions()) {
			renderSolution(solution);
		}
	}

	private void renderProblemHeader(ProblemResult result) {
		Console.line();
		Console.boxTitle("Problem: " + Console.blue(result.problemName()), WIDTH);
		Console.line();
	}

	private void renderSolution(SolutionResult solution) {
		Console.section("Solution: " + solution.solutionName(), WIDTH, 1);
		Console.line();

		for (CaseResult caseResult : solution.cases()) {
			renderCase(caseResult);
		}

		Console.line();
		renderSummary(solution);
		Console.line();
		Console.line();
	}

	private void renderCase(CaseResult caseResult) {
		String base = Console.indent(2);

		if (caseResult.passed()) {
			Console.print(base + Console.green("[PASS]") + " " + caseResult.caseName());
			return;
		}

		Console.print(base + Console.red("[FAIL]") + " " + caseResult.caseName());
		Console.line();

		keyValue(3, "Expected", caseResult.expected());
		keyValue(3, "Actual", caseResult.actual());

		Console.line();
	}

	private void renderSummary(SolutionResult solution) {
		long passed = solution.passedCount();
		long total = solution.totalCount();

		String text = "Summary: " + passed + " / " + total + " passed";

		if (solution.passed()) {
			Console.print(Console.indent(2) + Console.green(text));
		} else if (passed == 0) {
			Console.print(Console.indent(2) + Console.red(text));
		} else {
			Console.print(Console.indent(2) + Console.yellow(text));
		}
	}

	//#################################
	//## Benchmark ####################
	//#################################

	public void renderBenchmarkResult(ProblemBenchmarkResult result) {
		renderBenchmarkHeader(result);

		for (SolutionBenchmarkResult solutionBenchmarkResult : result.solutionBenchmarkResults()) {
			renderBenchmarkSolution(solutionBenchmarkResult);
		}
	}

	private void renderBenchmarkHeader(ProblemBenchmarkResult result) {
		Console.line();
		Console.boxTitle("Benchmark For Problem: " + Console.blue(result.problemName()), WIDTH);
		Console.line();
	}

	private void renderBenchmarkSolution(SolutionBenchmarkResult solution) {
		Console.section("Benchmark For Solution: " + solution.getSolutionName(), WIDTH, 1);
		Console.line();

		if (solution.isFailed()) {
			Console.print(Console.indent(2) + Console.red("Status: FAILED"));
			keyValue(3, "Error", solution.getErrorMessage());
			Console.line();
			Console.line();
			return;
		}

		BenchmarkStats stats = solution.getStats();

		Console.print(Console.indent(2) + Console.green("Status: SUCCESS"));
		Console.line();
		keyValue(3, "Samples", stats.getSampleCount());
		keyValue(3, "Total", formatNanos(stats.getTotalNanos()));
		keyValue(3, "Average", formatNanos(Math.round(stats.getAverageNanos())));
		keyValue(3, "Min", formatNanos(stats.getMinNanos()));
		keyValue(3, "Max", formatNanos(stats.getMaxNanos()));
		Console.line();
		Console.line();
	}

	//#################################
	//## Helpers ######################
	//#################################

	private void keyValue(int indent, String key, Object value) {
		Console.print(Console.indent(indent) + String.format("%-9s -> %s", key, formatValue(value)));
	}

	private String formatValue(Object value) {
		if (value == null) return "null";

		if (value instanceof int[] v) return Arrays.toString(v);
		if (value instanceof long[] v) return Arrays.toString(v);
		if (value instanceof double[] v) return Arrays.toString(v);
		if (value instanceof boolean[] v) return Arrays.toString(v);
		if (value instanceof Object[] v) return Arrays.deepToString(v);

		return value.toString();
	}

	private String formatNanos(long nanos) {
		return nanos + " ns";
	}
}
