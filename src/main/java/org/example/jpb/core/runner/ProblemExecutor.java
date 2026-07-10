package org.example.jpb.core.runner;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.jpb.core.benchmark.BenchmarkRunner;
import org.example.jpb.core.model.*;

public class ProblemExecutor {

	private final ProblemPreparator problemPreparator;
	private final ProblemRunner problemRunner;
	private final BenchmarkRunner benchmarkRunner;

	public ProblemExecutor() {
		this.problemPreparator = new ProblemPreparator();
		this.problemRunner = new ProblemRunner();
		this.benchmarkRunner = new BenchmarkRunner();
	}

	public ProblemExecutionResult execute(Class<?> problemClass, BenchmarkConfig benchmarkConfig) {
		PreparedProblem preparedProblem = problemPreparator.prepare(problemClass);
		ProblemResult problemResult = problemRunner.run(preparedProblem);
		PreparedProblem benchmarkReadyProblem = filterPassedSolutions(preparedProblem, problemResult);
		ProblemBenchmarkResult benchmarkResult = benchmarkRunner.run(benchmarkReadyProblem, benchmarkConfig);

		return ProblemExecutionResult
			.builder()
			.problemResult(problemResult)
			.problemBenchmarkResult(benchmarkResult)
			.build();
	}

	private PreparedProblem filterPassedSolutions(
		PreparedProblem preparedProblem,
		ProblemResult problemResult
	) {
		Set<String> passedSolutionNames = problemResult
			.getSolutionResults()
			.stream()
			.filter(SolutionResult::isPassed)
			.map(SolutionResult::getId)
			.collect(Collectors.toSet());

		List<PreparedSolution> passedSolutions = preparedProblem
			.getSolutions()
			.stream()
			.filter(solution -> passedSolutionNames.contains(solution.getDisplayName()))
			.toList();

		return preparedProblem.withNewSolutions(passedSolutions);
	}
}
