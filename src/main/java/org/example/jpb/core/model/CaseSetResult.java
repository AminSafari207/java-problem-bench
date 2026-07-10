package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class CaseSetResult {

	private final String id;
	private final String displayName;
	private final List<TestCaseResult> testCaseResults;

	@Builder
	public CaseSetResult(String id, String displayName, List<TestCaseResult> testCaseResults) {
		this.id = ModelChecks.requireNormalizedNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.testCaseResults = ModelChecks.requireNonEmptyCopy(testCaseResults, "testCaseResults");
	}

	public long passedCount() {
		return testCaseResults.stream().filter(TestCaseResult::isPassed).count();
	}

	public long totalCount() {
		return testCaseResults.size();
	}

	public boolean isPassed() {
		return passedCount() == totalCount();
	}
}
