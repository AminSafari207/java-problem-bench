package org.example.jpb.runner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.example.jpb.annotation.Case;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.model.TestCase;
import org.example.jpb.util.Console;
import org.example.jpb.util.ReflectionExecutor;
import org.example.jpb.util.ResultComparator;

public class ProblemRunner {

	public void run(Class<?> problemClass) {
		validateProblemClass(problemClass);

		Object problemInstance = instantiate(problemClass);
		List<TestCase<?, ?>> testCases = collectCases(problemClass, problemInstance);
		List<Method> solutions = collectSolutions(problemClass);

		if (testCases.isEmpty()) {
			throw new IllegalStateException("No @Case found in " + problemClass.getName());
		}

		if (solutions.isEmpty()) {
			throw new IllegalStateException("No @Solution found in " + problemClass.getName());
		}

		Problem problem = problemClass.getAnnotation(Problem.class);

		Console.print(
			"\n" + Console.gray("---") + "Problem: " + Console.green(problem.value()) + Console.gray("---")
		);

		for (Method solution : solutions) {
			runSolution(problemInstance, solution, testCases);
		}
	}

	private void validateProblemClass(Class<?> problemClass) {
		if (!problemClass.isAnnotationPresent(Problem.class)) {
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
			if (method.isAnnotationPresent(Solution.class)) {
				throw new IllegalStateException(
					"@Solution method must have exactly 1 parameter: " + method.getName()
				);
			}

			solutions.add(method);
		}

		return solutions;
	}

	private void runSolution(Object instance, Method solution, List<TestCase<?, ?>> testCases) {
		Solution solutionAnnotation = solution.getAnnotation(Solution.class);
		String solutionName = solutionAnnotation.value();

		Console.print("  " + Console.gray("Solution:") + " " + solutionAnnotation.value());

		int passed = 0;

		for (TestCase<?, ?> testCase : testCases) {
			Object actual = ReflectionExecutor.invoke(solution, instance, testCase.input());
			boolean ok = ResultComparator.areEqual(testCase.expected(), actual);

			if (ok) {
				passed++;
				Console.print("    " + Console.green("[PASS]") + " " + testCase.name());
			} else {
				Console.print("    " + Console.red("[FAIL]") + " " + testCase.name());
				Console.print("      " + Console.gray("expected:") + " " + formatValue(testCase.expected()));
				Console.print("      " + Console.gray("actual:  ") + " " + formatValue(actual));
			}
		}

		String summary = String.format("    Passed %d/%d", passed, testCases.size());

		if (passed == testCases.size()) {
			Console.success(summary);
		} else {
			Console.warn(summary);
		}
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
