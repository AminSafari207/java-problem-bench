package org.example.jpb.core.runner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.example.jpb.annotation.Case;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.core.model.CaseResult;
import org.example.jpb.core.model.ProblemResult;
import org.example.jpb.core.model.SolutionResult;
import org.example.jpb.core.model.TestCase;
import org.example.jpb.util.ReflectionExecutor;
import org.example.jpb.util.ResultComparator;

public class ProblemRunner {

	public ProblemResult run(Class<?> problemClass) {
		validateProblemClass(problemClass);

		ProblemRunner runner = new ProblemRunner();

		Object problemInstance = runner.instantiate(problemClass);
		List<TestCase<?, ?>> testCases = runner.collectCases(problemClass, problemInstance);
		List<Method> solutions = runner.collectSolutions(problemClass);

		if (testCases.isEmpty()) {
			throw new IllegalStateException("No @Case found in " + problemClass.getName());
		}

		if (solutions.isEmpty()) {
			throw new IllegalStateException("No @Solution found in " + problemClass.getName());
		}

		Problem problem = problemClass.getAnnotation(Problem.class);
		List<SolutionResult> solutionResults = new ArrayList<>();

		for (Method solution : solutions) {
			SolutionResult result = runner.runSolution(problemInstance, solution, testCases);

			solutionResults.add(result);
		}

		return new ProblemResult(problem.value(), solutionResults);
	}

	private static void validateProblemClass(Class<?> problemClass) {
		if (problemClass == null || !problemClass.isAnnotationPresent(Problem.class)) {
			throw new IllegalArgumentException(problemClass.getName() + " is not annotated with @Problem");
		}
	}

	private Object instantiate(Class<?> problemClass) {
		try {
			var constructor = problemClass.getDeclaredConstructor();
			constructor.setAccessible(true);

			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to instantiate " + problemClass.getName(), e);
		}
	}

	private List<TestCase<?, ?>> collectCases(Class<?> problemClass, Object problemInstance) {
		List<TestCase<?, ?>> testCases = new ArrayList<>();

		for (Method method : problemClass.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(Case.class)) continue;

			Object value = ReflectionExecutor.invoke(method, problemInstance);

			testCases.addAll(extractCases(value, "@Case method " + method.getName()));
		}

		for (Field field : problemClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Case.class)) continue;

			try {
				field.setAccessible(true);

				Object value = field.get(problemInstance);

				testCases.addAll(extractCases(value, "@Case field " + field.getName()));
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to access field: " + field.getName(), e);
			}
		}

		return testCases;
	}

	private List<TestCase<?, ?>> extractCases(Object value, String source) {
		if (value == null) {
			throw new IllegalStateException(source + " returned null");
		}

		if (value instanceof TestCase<?, ?> testCase) {
			return List.of(testCase);
		}

		if (value instanceof List<?> list) {
			List<TestCase<?, ?>> testCases = new ArrayList<>();

			for (Object element : list) {
				if (!(element instanceof TestCase<?, ?> testCase)) {
					throw new IllegalStateException(source + " contains non-TestCase element: " + element);
				}

				testCases.addAll(extractCases(element, source));
			}

			return testCases;
		}

		throw new IllegalStateException(
			source + " must be TestCase or List<TestCase>, but got: " + value.getClass()
		);
	}

	private List<Method> collectSolutions(Class<?> problemClass) {
		List<Method> solutions = new ArrayList<>();

		for (Method method : problemClass.getDeclaredMethods()) {
			if (!method.isAnnotationPresent(Solution.class)) continue;

			if (method.getParameterCount() != 1) {
				throw new IllegalStateException(
					"@Solution method must have exactly 1 parameter: " + method.getName()
				);
			}

			solutions.add(method);
		}

		return solutions;
	}

	private SolutionResult runSolution(Object instance, Method solution, List<TestCase<?, ?>> testCases) {
		Solution solutionAnnotation = solution.getAnnotation(Solution.class);
		List<CaseResult> caseResults = new ArrayList<>();

		for (TestCase<?, ?> testCase : testCases) {
			Object actual = ReflectionExecutor.invoke(solution, instance, testCase.input());
			boolean ok = ResultComparator.areEqual(testCase.expected(), actual);

			caseResults.add(new CaseResult(testCase.name(), testCase.expected(), actual, ok));
		}

		return new SolutionResult(solutionAnnotation.value(), caseResults);
	}

	private String formatValue(Object value) {
		if (value == null) return "null";
		if (value instanceof Object[] array) return Arrays.deepToString(array);
		if (value instanceof int[] array) return Arrays.toString(array);
		if (value instanceof long[] array) return Arrays.toString(array);
		if (value instanceof byte[] array) return Arrays.toString(array);
		if (value instanceof short[] array) return Arrays.toString(array);
		if (value instanceof char[] array) return Arrays.toString(array);
		if (value instanceof boolean[] array) return Arrays.toString(array);
		if (value instanceof float[] array) return Arrays.toString(array);
		if (value instanceof double[] array) return Arrays.toString(array);

		return String.valueOf(value);
	}
}
