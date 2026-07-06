package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BenchmarkStats {

	private final int sampleCount;
	private final long totalNanos;
	private final double averageNanos;
	private final long minNanos;
	private final long maxNanos;
}
