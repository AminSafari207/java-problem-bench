package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class TestCaseResult {

	private final String id;
	private final String displayName;
	private final Object expected;
	private final Object actual;
	private final boolean passed;

	@Builder
	private TestCaseResult(String id, String displayName, Object expected, Object actual, boolean passed) {
		this.id = ModelChecks.requireNormalizedNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.expected = ModelChecks.requireNonNull(expected, "expected");
		this.actual = ModelChecks.requireNonNull(actual, "actual");
		this.passed = passed;
	}
}
