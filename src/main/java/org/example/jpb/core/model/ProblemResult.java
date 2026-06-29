package org.example.jpb.core.model;

import java.util.List;

public record ProblemResult(String problemName, List<SolutionResult> solutions) {}
