package org.example.jpb.core.benchmark;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.example.jpb.core.enums.BenchmarkStatus;
import org.example.jpb.core.model.*;
import org.example.jpb.util.ReflectionExecutor;

public class BenchmarkRunner {

	public ProblemBenchmarkResult run(PreparedProblem preparedProblem, BenchmarkConfig config) {
		List<PreparedSolution> preparedSolutions = preparedProblem.solutions();

		validateInputs(preparedProblem, preparedSolutions, config);

		int warmupIterations = config.getWarmupIterations();
		int measurementIterations = config.getMeasurementIterations();

		List<SolutionBenchmarkResult> solutionsBenchmarkResults = new ArrayList<>();

		for (PreparedSolution preparedSolution : preparedSolutions) {
			SolutionBenchmarkResult benchmarkResult = benchmarkSolution(
				preparedProblem,
				preparedSolution,
				warmupIterations,
				measurementIterations
			);

			solutionsBenchmarkResults.add(benchmarkResult);
		}

		return new ProblemBenchmarkResult(
			preparedProblem.problemDisplayName(),
			List.copyOf(solutionsBenchmarkResults)
		);
	}

	private SolutionBenchmarkResult benchmarkSolution(
		PreparedProblem preparedProblem,
		PreparedSolution preparedSolution,
		int warmupIterations,
		int measurementIterations
	) {
		String solutionName = preparedSolution.name();

		try {
			runWarmup(preparedProblem, preparedSolution, warmupIterations);

			BenchmarkStats stats = runMeasurement(preparedProblem, preparedSolution, measurementIterations);

			return SolutionBenchmarkResult
				.builder()
				.solutionName(solutionName)
				.status(BenchmarkStatus.SUCCESS)
				.stats(stats)
				.errorMessage(null)
				.build();
		} catch (Exception e) {
			return SolutionBenchmarkResult
				.builder()
				.solutionName(solutionName)
				.status(BenchmarkStatus.FAILED)
				.stats(null)
				.errorMessage(buildErrorMessage(e))
				.build();
		}
	}

	private void runWarmup(
		PreparedProblem preparedProblem,
		PreparedSolution preparedSolution,
		int warmupIterations
	) {
		for (int i = 0; i < warmupIterations; i++) {
			runAllCases(preparedProblem, preparedSolution);
		}
	}

	private BenchmarkStats runMeasurement(
		PreparedProblem preparedProblem,
		PreparedSolution preparedSolution,
		int measurementIterations
	) {
		long totalNanos = 0L;
		long minNanos = Long.MAX_VALUE;
		long maxNanos = Long.MIN_VALUE;

		for (int i = 0; i < measurementIterations; i++) {
			long startNanos = System.nanoTime();

			runAllCases(preparedProblem, preparedSolution);

			long elapsedNanos = System.nanoTime() - startNanos;

			totalNanos += elapsedNanos;
			minNanos = Math.min(minNanos, elapsedNanos);
			maxNanos = Math.max(maxNanos, elapsedNanos);
		}

		double averageNanos = (double) totalNanos / measurementIterations;

		return BenchmarkStats
			.builder()
			.sampleCount(measurementIterations)
			.totalNanos(totalNanos)
			.averageNanos(averageNanos)
			.minNanos(minNanos)
			.maxNanos(maxNanos)
			.build();
	}

	private void runAllCases(PreparedProblem preparedProblem, PreparedSolution preparedSolution) {
		for (PreparedCase preparedCase : preparedProblem.cases()) {
			Method method = preparedSolution.method();
			Object instance = preparedProblem.problemInstance();
			Object[] arguments = preparedCase.newArguments();

			ReflectionExecutor.invoke(instance, method, arguments);
		}
	}

	private String buildErrorMessage(Exception e) {
		String message = e.getMessage();

		if (message == null || message.isBlank()) {
			return e.getClass().getSimpleName();
		}

		return e.getClass().getSimpleName() + ": " + message;
	}

	//#################################
	//## Validators ###################
	//#################################

	private void validateInputs(
		PreparedProblem preparedProblem,
		List<PreparedSolution> preparedSolutions,
		BenchmarkConfig config
	) {
		if (preparedProblem == null) {
			throw new IllegalArgumentException("preparedProblem must not be null");
		}

		if (preparedSolutions == null) {
			throw new IllegalArgumentException("preparedSolutions must not be null");
		}

		if (config == null) {
			throw new IllegalArgumentException("config must not be null");
		}
	}
}
