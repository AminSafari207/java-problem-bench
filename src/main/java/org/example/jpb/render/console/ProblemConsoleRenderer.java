package org.example.jpb.render.console;

import java.util.Arrays;

import org.example.jpb.core.model.*;
import org.example.jpb.render.model.ConsoleRenderOptions;
import org.example.jpb.util.Console;

public class ProblemConsoleRenderer {

	private static final int WIDTH = 80;

	//#################################
	//## Problem Correctness ##########
	//#################################

	public void renderProblemResult(ProblemResult result) {
		renderProblemResult(result, ConsoleRenderOptions.defaults());
	}

	public void renderProblemResult(ProblemResult result, ConsoleRenderOptions options) {
		renderProblemHeader(result);

		Console.section("Correctness", WIDTH, 0);

		for (SolutionResult solution : result.getSolutionResults()) {
			renderSolution(solution, options);
		}
	}

	private void renderProblemHeader(ProblemResult result) {
		Console.line();
		Console.boxTitle("Problem: " + Console.blue(result.getProblemDisplayName()), WIDTH);
		Console.line();
	}

	private void renderSolution(SolutionResult solution, ConsoleRenderOptions options) {
		long total = solution.totalCount();
		long passed = solution.passedCount();

		String status = statusLabel(solution.isPassed());
		String id = options.isShowIds() ? " " + Console.gray("id=" + solution.getSolutionId()) : "";
		String count = Console.gray(passed + "/" + total);

		Console.print(
			status +
			" Solution  " +
			Console.padRight(solution.getSolutionDisplayName(), 24) +
			id +
			Console.indent(2) +
			count
		);

		for (CaseSetResult caseSetResult : solution.getCaseSetResults()) {
			if (caseSetResult.isPassed() && !options.isShowPassedCaseSets()) {
				continue;
			}

			renderCaseSet(caseSetResult, options);
		}

		Console.line();
	}

	private void renderCaseSet(CaseSetResult caseSetResult, ConsoleRenderOptions options) {
		long total = caseSetResult.totalCount();
		long passed = caseSetResult.passedCount();

		String status = statusLabel(caseSetResult.isPassed());
		String id = options.isShowIds() ? " " + Console.gray("id=" + caseSetResult.getCaseSetId()) : "";
		String count = Console.gray(passed + "/" + total);

		Console.print(
			Console.indent(1) +
			status +
			" Case set  " +
			Console.padRight(caseSetResult.getCaseSetDisplayName(), 22) +
			id +
			Console.indent(2) +
			count
		);

		for (TestCaseResult testCaseResult : caseSetResult.getTestCaseResults()) {
			if (testCaseResult.isPassed() && !options.isShowPassedTestCases()) {
				continue;
			}

			renderTestCase(testCaseResult, options);
		}
	}

	private void renderTestCase(TestCaseResult testCaseResult, ConsoleRenderOptions options) {
		String status = statusLabel(testCaseResult.isPassed());
		String id = options.isShowIds() ? " " + Console.gray("id=" + testCaseResult.getTestCaseId()) : "";

		Console.print(Console.indent(2) + status + " " + testCaseResult.getTestCaseDisplayName() + id);

		if (testCaseResult.isPassed()) return;

		keyValue(3, "Expected", testCaseResult.getExpected());
		keyValue(3, "Actual", testCaseResult.getActual());
		Console.line();
	}

	//#################################
	//## Benchmark ####################
	//#################################

	public void renderBenchmarkResult(ProblemBenchmarkResult result) {
		renderBenchmarkResult(result, ConsoleRenderOptions.defaults());
	}

	public void renderBenchmarkResult(ProblemBenchmarkResult result, ConsoleRenderOptions options) {
		if (!options.isShowBenchmark()) return;

		renderBenchmarkHeader(result);

		Console.section("Benchmark", WIDTH, 0);

		if (result.isSkipped()) {
			Console.print(Console.yellow("Skipped: ") + result.getSkipReason());
			Console.line();
			return;
		}

		renderBenchmarkTable(result);
	}

	private void renderBenchmarkHeader(ProblemBenchmarkResult result) {
		Console.line();
		Console.boxTitle("Benchmark For Problem: " + Console.blue(result.getProblemDisplayName()), WIDTH);
		Console.line();
	}

	private void renderBenchmarkTable(ProblemBenchmarkResult result) {
		Console.print(
			Console.padRight("Status", 9) +
			Console.padRight("Solution", 24) +
			Console.padRight("Samples", 10) +
			Console.padRight("Avg", 11) +
			Console.padRight("Min", 11) +
			Console.padRight("Max", 11)
		);

		Console.divider('─', WIDTH);

		result
			.getSolutionBenchmarkResults()
			.stream()
			.sorted(this::compareBenchmarkResults)
			.forEach(this::renderBenchmarkRow);

		Console.line();
	}

	private void renderBenchmarkRow(SolutionBenchmarkResult solution) {
		if (solution.isFailed()) {
			Console.print(
				Console.padRight(Console.red("FAILED"), 9) +
				Console.padRight(solution.getSolutionDisplayName(), 24) +
				Console.gray(solution.getErrorMessage())
			);
			return;
		}

		BenchmarkStats stats = solution.getStats();

		Console.print(
			Console.padRight(Console.green("SUCCESS"), 9) +
			Console.padRight(solution.getSolutionDisplayName(), 24) +
			Console.padRight(String.valueOf(stats.getSampleCount()), 10) +
			Console.padRight(formatNanos(Math.round(stats.getAverageNanos())), 11) +
			Console.padRight(formatNanos(stats.getMinNanos()), 11) +
			Console.padRight(formatNanos(stats.getMaxNanos()), 11)
		);
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

	private String formatDecimal(double value) {
		if (value >= 100) {
			return String.format("%.0f", value);
		}

		if (value >= 10) {
			return String.format("%.1f", value);
		}

		return String.format("%.3f", value);
	}

	private String formatNanos(long nanos) {
		if (nanos < 1_000) {
			return nanos + " ns";
		}

		if (nanos < 1_000_000) {
			return formatDecimal(nanos / 1_000.0) + " us";
		}

		if (nanos < 1_000_000_000) {
			return formatDecimal(nanos / 1_000_000.0) + " ms";
		}

		return formatDecimal(nanos / 1_000_000_000.0) + " s";
	}

	private String statusLabel(boolean passed) {
		return passed ? Console.green("[PASS]") : Console.red("[FAIL]");
	}

	private int compareBenchmarkResults(SolutionBenchmarkResult first, SolutionBenchmarkResult second) {
		if (first.isFailed() && second.isFailed()) {
			return first.getSolutionDisplayName().compareToIgnoreCase(second.getSolutionDisplayName());
		}

		if (first.isFailed()) {
			return 1;
		}

		if (second.isFailed()) {
			return -1;
		}

		return Double.compare(first.getStats().getAverageNanos(), second.getStats().getAverageNanos());
	}
}
