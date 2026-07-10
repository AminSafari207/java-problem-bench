package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.core.enums.BenchmarkStatus;
import org.example.jpb.util.ModelChecks;

@Getter
public class SolutionBenchmarkResult {

	private final String solutionId;
	private final String solutionDisplayName;
	private final BenchmarkStatus status;
	private final BenchmarkStats stats;
	private final String errorMessage;

	@Builder
	private SolutionBenchmarkResult(
		String solutionId,
		String solutionDisplayName,
		BenchmarkStatus status,
		BenchmarkStats stats,
		String errorMessage
	) {
		this.solutionId = ModelChecks.requireNormalizedNonBlank(solutionId, "solutionId");
		this.solutionDisplayName = ModelChecks.defaultIfBlank(solutionDisplayName, this.solutionId);
		this.status = ModelChecks.requireNonNull(status, "status");
		this.stats = stats;
		this.errorMessage = errorMessage;
	}

	public boolean isSuccess() {
		return status == BenchmarkStatus.SUCCESS;
	}

	public boolean isFailed() {
		return status == BenchmarkStatus.FAILED;
	}
}
