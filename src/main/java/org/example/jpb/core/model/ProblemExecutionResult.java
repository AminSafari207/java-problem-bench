package org.example.jpb.core.model;

public record ProblemExecutionResult(
	ProblemResult problemResult,
	ProblemBenchmarkResult problemBenchmarkResult
) {}
