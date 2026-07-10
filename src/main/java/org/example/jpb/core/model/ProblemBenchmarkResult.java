package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class ProblemBenchmarkResult {

	private final String problemId;
	private final String problemDisplayName;
	private final List<SolutionBenchmarkResult> solutionBenchmarkResults;

	@Builder
	private ProblemBenchmarkResult(
		String problemId,
		String problemDisplayName,
		List<SolutionBenchmarkResult> solutionBenchmarkResults
	) {
		this.problemId = ModelChecks.requireNormalizedNonBlank(problemId, "problemId");
		this.problemDisplayName = ModelChecks.defaultIfBlank(problemDisplayName, this.problemId);
		this.solutionBenchmarkResults =
			ModelChecks.requireNonEmptyCopy(solutionBenchmarkResults, "solutionBenchmarkResults");

		ModelChecks.requireUniqueIds(
			solutionBenchmarkResults,
			SolutionBenchmarkResult::getSolutionId,
			"solutionBenchmarkResults"
		);
	}
}
