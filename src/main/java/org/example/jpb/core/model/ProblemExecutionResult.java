package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class ProblemExecutionResult {

	private final ProblemResult problemResult;
	private final ProblemBenchmarkResult problemBenchmarkResult;

	@Builder
	private ProblemExecutionResult(
		ProblemResult problemResult,
		ProblemBenchmarkResult problemBenchmarkResult
	) {
		this.problemResult = ModelChecks.requireNonNull(problemResult, "problemResult");
		this.problemBenchmarkResult =
			ModelChecks.requireNonNull(problemBenchmarkResult, "problemBenchmarkResult");
	}
}
