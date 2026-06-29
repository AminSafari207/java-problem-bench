package com.example.jpb.runner;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.example.jpb.annotation.Case;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.core.model.ProblemResult;
import org.example.jpb.core.model.TestCase;
import org.example.jpb.core.runner.ProblemRunner;
import org.example.jpb.render.console.ConsoleRenderer;
import org.junit.jupiter.api.Test;

class ProblemRunnerTest {

	@Problem("Dummy")
	static class DummyProblem {

		@Case
		public List<TestCase<Integer, Integer>> cases() {
			return List.of(new TestCase<>("one", 1, 2), new TestCase<>("two", 2, 4));
		}

		@Case
		public List<TestCase<Integer, Integer>> cases2 = List.of(
			new TestCase<>("three", 150, 151),
			new TestCase<>("four", 300, 301)
		);

		@Solution("add one")
		public Integer addOne(Integer input) {
			return input + 1;
		}

		@Solution("add two, subtract 1")
		public Integer addTwoSubOne(Integer input) {
			return input + 2 - 1;
		}
	}

	@Test
	void shouldRunAllCasesAndAllSolutionsWithoutThrowing() {
		ProblemRunner runner = new ProblemRunner();
		ProblemResult result = runner.run(DummyProblem.class);
		ConsoleRenderer renderer = new ConsoleRenderer();

		renderer.render(result);

		assertNotNull(result);
		assertEquals("Dummy", result.problemName());
		assertEquals(2, result.solutions().size());
	}
}
