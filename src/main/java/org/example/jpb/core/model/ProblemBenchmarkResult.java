package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

public record ProblemBenchmarkResult(
	String problemName,
	List<SolutionBenchmarkResult> solutionBenchmarkResults
) {}
