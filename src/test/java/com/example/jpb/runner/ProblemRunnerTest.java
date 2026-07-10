package com.example.jpb.runner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.jpb.annotation.CaseSet;
import org.example.jpb.annotation.Contract;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.core.model.*;
import org.example.jpb.core.runner.ProblemExecutor;
import org.example.jpb.render.console.ProblemConsoleRenderer;
import org.junit.jupiter.api.Test;

class ProblemRunnerTest {

	@Problem(id = "problem-dummy", displayName = "Dummy")
	static class DummyProblem {

		@Contract
		static final ProblemContract contract = ProblemContract.accepts(Integer.class).expects(Integer.class);

		@CaseSet(id = "case-set-01")
		public List<TestCase> cases() {
			return List.of(
				TestCase.of("one", Arguments.single(1), 2),
				TestCase.of("two", Arguments.single(2), 3)
			);
		}

		@CaseSet(id = "case-set-02")
		public List<TestCase> cases2 = List.of(
			TestCase.of("three", Arguments.single(150), 151),
			TestCase.of("four", Arguments.single(300), 301)
		);

		@Solution(id = "add one")
		public Integer addOne(Integer input) {
			return input + 1;
		}

		@Solution(id = "add two, subtract 1")
		public Integer addTwoSubOne(Integer input) {
			return input + 3 - 1;
		}
	}

	@Test
	void shouldRunAllCasesAndAllSolutionsWithoutThrowing() {
		BenchmarkConfig benchmarkConfig = BenchmarkConfig
			.builder()
			.warmupIterations(1000)
			.measurementIterations(10000)
			.build();

		ProblemExecutor problemExecutor = new ProblemExecutor();
		ProblemExecutionResult problemExecutionResult = problemExecutor.execute(
			DummyProblem.class,
			benchmarkConfig
		);

		ProblemResult problemResult = problemExecutionResult.problemResult();
		ProblemBenchmarkResult problemBenchmarkResult = problemExecutionResult.problemBenchmarkResult();

		ProblemConsoleRenderer renderer = new ProblemConsoleRenderer();

		renderer.renderProblemResult(problemResult);
		renderer.renderBenchmarkResult(problemBenchmarkResult);

		assertNotNull(problemResult);
		assertEquals("Dummy", problemResult.problemName());
		assertEquals(2, problemResult.solutions().size());
	}
}
