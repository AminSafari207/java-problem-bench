package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class PreparedProblem {

	private final String id;
	private final String displayName;
	private final Class<?> problemClass;
	private final Object problemInstance;
	private final ProblemContract contract;
	private final List<PreparedCase> cases;
	private final List<PreparedSolution> solutions;

	@Builder(toBuilder = true)
	private PreparedProblem(
		String id,
		String displayName,
		Class<?> problemClass,
		Object problemInstance,
		ProblemContract contract,
		List<PreparedCase> cases,
		List<PreparedSolution> solutions
	) {
		this.id = ModelChecks.requireNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.problemClass = ModelChecks.requireNonNull(problemClass, "problemClass");
		this.problemInstance = ModelChecks.requireNonNull(problemInstance, "problemInstance");
		this.contract = ModelChecks.requireNonNull(contract, "contract");
		this.cases = ModelChecks.requireNonEmptyCopy(cases, "cases");
		this.solutions = ModelChecks.requireNonEmptyCopy(solutions, "solutions");
	}

	public PreparedProblem withNewSolutions(List<PreparedSolution> newPreparedSolutions) {
		return toBuilder().solutions(newPreparedSolutions).build();
	}
}
