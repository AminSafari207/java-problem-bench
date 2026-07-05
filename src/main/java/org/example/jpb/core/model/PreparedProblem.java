package org.example.jpb.core.model;

import java.util.List;

public record PreparedProblem(
	String problemName,
	Class<?> problemClass,
	Object problemInstance,
	ProblemContract contract,
	List<PreparedCase> cases,
	List<PreparedSolution> solutions
) {}
