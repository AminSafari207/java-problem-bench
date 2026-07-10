package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class ProblemResult {

	private final String problemId;
	private final String problemDisplayName;
	private final List<SolutionResult> solutionResults;

	@Builder
	private ProblemResult(String problemId, String problemDisplayName, List<SolutionResult> solutionResults) {
		this.problemId = ModelChecks.requireNormalizedNonBlank(problemId, "problemId");
		this.problemDisplayName = ModelChecks.defaultIfBlank(problemDisplayName, this.problemId);
		this.solutionResults = ModelChecks.requireNonEmptyCopy(solutionResults, "solutionResults");
	}
}
