package org.example.jpb.render.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ConsoleRenderOptions {

	@Builder.Default
	private final boolean showIds = true;

	@Builder.Default
	private final boolean showPassedCaseSets = false;

	@Builder.Default
	private final boolean showPassedTestCases = false;

	@Builder.Default
	private final boolean showBenchmark = true;

	@Builder.Default
	private final int maxFailureDetails = Integer.MAX_VALUE;

	public static ConsoleRenderOptions defaults() {
		return ConsoleRenderOptions.builder().build();
	}
}
