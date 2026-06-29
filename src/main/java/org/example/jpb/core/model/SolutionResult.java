package org.example.jpb.core.model;

import java.util.List;

public record SolutionResult(String solutionName, List<CaseResult> cases) {
	public long passedCount() {
		return cases.stream().filter(CaseResult::passed).count();
	}

	public long totalCount() {
		return cases.size();
	}

	public boolean allPassed() {
		return passedCount() == totalCount();
	}
}
