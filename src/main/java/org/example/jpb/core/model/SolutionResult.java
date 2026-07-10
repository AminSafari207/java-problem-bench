package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import org.example.jpb.util.ModelChecks;

public class SolutionResult {

	private final String id;
	private final String displayName;
	private final List<CaseSetResult> caseSetResults;

	@Builder
	private SolutionResult(String id, String displayName, List<CaseSetResult> caseSetResults) {
		this.id = ModelChecks.requireNonNull(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.caseSetResults = ModelChecks.requireNonEmptyCopy(caseSetResults, "caseSetResults");
	}

	public long passedCount() {
		return caseSetResults.stream().mapToLong(CaseSetResult::passedCount).sum();
	}

	public long totalCount() {
		return caseSetResults.stream().mapToLong(CaseSetResult::totalCount).sum();
	}

	public boolean isPassed() {
		return caseSetResults.stream().allMatch(CaseSetResult::isPassed);
	}
}
