package com.example.jpb.runner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.jpb.annotation.Case;
import org.example.jpb.annotation.Contract;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.core.benchmark.BenchmarkRunner;
import org.example.jpb.core.model.*;
import org.example.jpb.core.runner.ProblemRunner;
import org.example.jpb.render.console.ProblemConsoleRenderer;
import org.junit.jupiter.api.Test;

class ProblemRunnerTest {

	@Problem(name = "Dummy")
	static class DummyProblem {

		@Contract
		static final ProblemContract contract = ProblemContract.input(Integer.class).returns(Integer.class);

		@Case
		public List<TestCase<Integer>> cases() {
			return List.of(
				new TestCase<>("one", Arguments.single(1), 2),
				new TestCase<>("two", Arguments.single(2), 3)
			);
		}

		@Case
		public List<TestCase<Integer>> cases2 = List.of(
			new TestCase<>("three", Arguments.single(150), 151),
			new TestCase<>("four", Arguments.single(300), 301)
		);

		@Solution(name = "add one")
		public Integer addOne(Integer input) {
			return input + 1;
		}

		@Solution(name = "add two, subtract 1")
		public Integer addTwoSubOne(Integer input) {
			return input + 2 - 1;
		}
	}

	@Test
	void shouldRunAllCasesAndAllSolutionsWithoutThrowing() {
		ProblemRunner problemRunner = new ProblemRunner();
		ProblemResult problemResult = problemRunner.run(DummyProblem.class);
		ProblemConsoleRenderer renderer = new ProblemConsoleRenderer();

		renderer.render(problemResult);

		assertNotNull(problemResult);
		assertEquals("Dummy", problemResult.problemName());
		assertEquals(2, problemResult.solutions().size());
	}
}
