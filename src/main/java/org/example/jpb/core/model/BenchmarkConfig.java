package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public final class BenchmarkConfig {

	private final int warmupIterations;
	private final int measurementIterations;

	@Builder
	private BenchmarkConfig(Integer warmupIterations, Integer measurementIterations) {
		int resolvedWarmup = warmupIterations != null ? warmupIterations : 1000;
		int resolvedMeasurement = measurementIterations != null ? measurementIterations : 10000;

		if (resolvedWarmup < 0) {
			throw new IllegalArgumentException("warmupIterations must be >= 0");
		}

		if (resolvedMeasurement <= 0) {
			throw new IllegalArgumentException("measurementIterations must be > 0");
		}

		this.warmupIterations = resolvedWarmup;
		this.measurementIterations = resolvedMeasurement;
	}
}
