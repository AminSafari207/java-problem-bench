package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.core.enums.BenchmarkStatus;

@Getter
@Builder
public class SolutionBenchmarkResult {

	private final String solutionName;
	private final BenchmarkStatus status;
	private final BenchmarkStats stats;
	private final String errorMessage;

	public boolean isSuccess() {
		return status == BenchmarkStatus.SUCCESS;
	}

	public boolean isFailed() {
		return status == BenchmarkStatus.FAILED;
	}
}
