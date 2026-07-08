package org.example.jpb.core.model;

import java.util.List;

public record PreparedProblem(
	String id,
	String problemDisplayName,
	Class<?> problemClass,
	Object problemInstance,
	ProblemContract contract,
	List<PreparedCase> cases,
	List<PreparedSolution> solutions
) {
	public PreparedProblem withNewSolutions(List<PreparedSolution> newPreparedSolutions) {
		return new PreparedProblem(
			id,
			problemDisplayName,
			problemClass,
			problemInstance,
			contract,
			cases,
			newPreparedSolutions
		);
	}
}
