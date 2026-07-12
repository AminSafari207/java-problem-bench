package org.example.jpb;

import org.example.jpb.core.model.*;
import org.example.jpb.core.runner.ProblemExecutor;
import org.example.jpb.problems.TwoSumProblem;
import org.example.jpb.render.console.ProblemConsoleRenderer;
import org.example.jpb.render.model.ConsoleRenderOptions;

public class Main {

	public static void main(String[] args) {
		BenchmarkConfig benchmarkConfig = BenchmarkConfig
			.builder()
			.warmupIterations(100_000)
			.measurementIterations(1_000_000)
			.build();

		ConsoleRenderOptions consoleRenderOptions = ConsoleRenderOptions
			.builder()
			.showIds(true)
			.showPassedCaseSets(true)
			.showPassedTestCases(true)
			.showBenchmark(true)
			.build();

		ProblemExecutor problemExecutor = new ProblemExecutor();
		ProblemExecutionResult problemExecutionResult = problemExecutor.execute(
			TwoSumProblem.class,
			benchmarkConfig
		);

		ProblemResult problemResult = problemExecutionResult.getProblemResult();
		ProblemBenchmarkResult problemBenchmarkResult = problemExecutionResult.getProblemBenchmarkResult();

		ProblemConsoleRenderer renderer = new ProblemConsoleRenderer();

		renderer.renderProblemResult(problemResult, consoleRenderOptions);
		renderer.renderBenchmarkResult(problemBenchmarkResult, consoleRenderOptions);
	}
}
