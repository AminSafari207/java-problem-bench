package org.example.jpb.core.model;

import java.util.List;

public record PreparedProblem(
	String id,
	String problemName,
	Class<?> problemClass,
	Object problemInstance,
	ProblemContract contract,
	List<PreparedCase> cases,
	List<PreparedSolution> solutions
) {
	public PreparedProblem withNewSolutions(List<PreparedSolution> newPreparedSolutions) {
		return new PreparedProblem(
			id,
			problemName,
			problemClass,
			problemInstance,
			contract,
			cases,
			newPreparedSolutions
		);
	}
}
