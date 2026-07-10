package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class BenchmarkStats {

	private final int sampleCount;
	private final long totalNanos;
	private final long minNanos;
	private final long maxNanos;

	@Builder
	private BenchmarkStats(int sampleCount, long totalNanos, long minNanos, long maxNanos) {
		this.sampleCount = ModelChecks.requireNonNegative(sampleCount, "sampleCount");
		this.totalNanos = ModelChecks.requireNonNegative(totalNanos, "totalNanos");
		this.minNanos = ModelChecks.requireNonNegative(minNanos, "minNanos");
		this.maxNanos = ModelChecks.requireNonNegative(maxNanos, "maxNanos");

		ModelChecks.requireLessThanOrEqual(this.minNanos, this.maxNanos, "minNanos", "maxNanos");

		if (this.sampleCount == 0) {
			ModelChecks.requireZero(this.totalNanos, "totalNanos");
			ModelChecks.requireZero(this.minNanos, "minNanos");
			ModelChecks.requireZero(this.maxNanos, "maxNanos");
		}
	}

	public double getAverageNanos() {
		return sampleCount == 0 ? 0.0d : (double) totalNanos / sampleCount;
	}
}
