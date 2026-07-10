package org.example.jpb.core.model;

import java.util.List;

public record ProblemResult(String problemDisplayName, List<SolutionResult> solutionResults) {}
