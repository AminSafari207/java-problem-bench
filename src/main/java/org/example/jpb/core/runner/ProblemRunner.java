package org.example.jpb.core.runner;

import java.util.ArrayList;
import java.util.List;
import org.example.jpb.core.model.*;
import org.example.jpb.util.ReflectionExecutor;
import org.example.jpb.util.ResultComparator;

public class ProblemRunner {

	private final ProblemPreparator preparator;

	public ProblemRunner() {
		this(new ProblemPreparator());
	}

	public ProblemRunner(ProblemPreparator preparator) {
		this.preparator = preparator;
	}

	public ProblemResult run(Class<?> problemClass) {
		PreparedProblem preparedProblem = preparator.prepare(problemClass);
		return run(preparedProblem);
	}

	public ProblemResult run(PreparedProblem preparedProblem) {
		List<SolutionResult> solutionResults = new ArrayList<>();

		for (PreparedSolution preparedSolution : preparedProblem.getSolutions()) {
			SolutionResult result = runSolution(
				preparedProblem.getProblemInstance(),
				preparedSolution,
				preparedProblem.getCases()
			);

			solutionResults.add(result);
		}

		return new ProblemResult(preparedProblem.getDisplayName(), solutionResults);
	}

	private SolutionResult runSolution(
		Object instance,
		PreparedSolution preparedSolution,
		List<PreparedCase> testCases
	) {
		List<CaseResult> caseResults = new ArrayList<>();

		for (PreparedCase testCase : testCases) {
			Object actual;

			try {
				actual =
					ReflectionExecutor.invoke(instance, preparedSolution.method(), testCase.newArguments());
			} catch (RuntimeException e) {
				throw new RuntimeException(
					"Execution failed: @Solution method '" +
					preparedSolution.name() +
					"' failed for test case '" +
					testCase.name() +
					"'",
					e
				);
			}

			boolean passed = ResultComparator.areEqual(testCase.expected(), actual);

			caseResults.add(new CaseResult(testCase.name(), testCase.expected(), actual, passed));
		}

		return new SolutionResult(preparedSolution.name(), caseResults);
	}
}
