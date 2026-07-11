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
	private final String skipReason;

	@Builder
	private ProblemBenchmarkResult(
		String problemId,
		String problemDisplayName,
		List<SolutionBenchmarkResult> solutionBenchmarkResults,
		String skipReason
	) {
		this.problemId = ModelChecks.requireNormalizedNonBlank(problemId, "problemId");
		this.problemDisplayName = ModelChecks.defaultIfBlank(problemDisplayName, this.problemId);
		this.solutionBenchmarkResults =
			ModelChecks.requireCopy(solutionBenchmarkResults, "solutionBenchmarkResults");
		this.skipReason = ModelChecks.defaultIfBlank(skipReason, null);

		ModelChecks.requireUniqueIds(
			solutionBenchmarkResults,
			SolutionBenchmarkResult::getSolutionId,
			"solutionBenchmarkResults"
		);
	}

	public boolean isSkipped() {
		return skipReason != null;
	}

	public boolean hasBenchmarks() {
		return !solutionBenchmarkResults.isEmpty();
	}

	public static ProblemBenchmarkResult skipped(String problemId, String problemDisplayName, String reason) {
		return ProblemBenchmarkResult
			.builder()
			.problemId(problemId)
			.problemDisplayName(problemDisplayName)
			.solutionBenchmarkResults(List.of())
			.skipReason(reason)
			.build();
	}
}
