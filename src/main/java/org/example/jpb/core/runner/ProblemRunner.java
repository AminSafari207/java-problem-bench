package org.example.jpb.core.runner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.example.jpb.annotation.Case;
import org.example.jpb.annotation.Contract;
import org.example.jpb.annotation.Problem;
import org.example.jpb.annotation.Solution;
import org.example.jpb.core.model.*;
import org.example.jpb.util.ReflectionExecutor;
import org.example.jpb.util.ResultComparator;

public class ProblemRunner {

	public ProblemResult run(Class<?> problemClass) {
		validateProblemClass(problemClass);

		Problem problem = problemClass.getAnnotation(Problem.class);
		Object problemInstance = instantiate(problemClass);
		ProblemContract contract = readContract(problemClass);
		List<TestCase<?>> testCases = collectCases(problemClass, problemInstance);
		List<Method> solutions = collectSolutions(problemClass);

		validateArtifacts(problemClass, contract, testCases, solutions);

		List<SolutionResult> solutionResults = new ArrayList<>();

		for (Method solution : solutions) {
			SolutionResult result = runSolution(problemInstance, solution, testCases);
			solutionResults.add(result);
		}

		return new ProblemResult(problem.name(), solutionResults);
	}

	//#################################
	//## Validators ###################
	//#################################

	private void validateProblemClass(Class<?> problemClass) {
		if (problemClass == null) {
			throw new IllegalArgumentException("problemClass must not be null");
		}

		if (!problemClass.isAnnotationPresent(Problem.class)) {
			throw new IllegalArgumentException(problemClass.getName() + " is not annotated with @Problem");
		}
	}

	private void validateArtifacts(
		Class<?> problemClass,
		ProblemContract contract,
		List<TestCase<?>> testCases,
		List<Method> solutions
	) {
		if (testCases.isEmpty()) {
			throw new IllegalStateException("No @Case found in " + problemClass.getName());
		}

		if (solutions.isEmpty()) {
			throw new IllegalStateException("No @Solution found in " + problemClass.getName());
		}

		validateCases(problemClass, contract, testCases);
		validateSolutions(problemClass, contract, solutions);
	}

	private void validateCases(Class<?> problemClass, ProblemContract contract, List<TestCase<?>> testCases) {
		Class<?>[] expectedParams = contract.parameterTypes();
		Class<?> expectedReturnType = contract.returnType();

		for (TestCase<?> testCase : testCases) {
			Object[] args = testCase.arguments().values();

			if (args.length != expectedParams.length) {
				throw new IllegalStateException(
					"Test case '" +
					testCase.name() +
					"' in " +
					problemClass.getName() +
					" has " +
					args.length +
					" argument(s), but contract requires " +
					expectedParams.length
				);
			}

			for (int i = 0; i < args.length; i++) {
				if (!isValueCompatible(expectedParams[i], args[i])) {
					throw new IllegalStateException(
						"Test case '" +
						testCase.name() +
						"' in " +
						problemClass.getName() +
						"' has incompatible argument at index " +
						i +
						": expected " +
						expectedParams[i].getName() +
						", but got " +
						describeValueType(args[i])
					);
				}
			}

			if (!isValueCompatible(expectedReturnType, testCase.expected())) {
				throw new IllegalStateException(
					"Test case '" +
					testCase.name() +
					"' in " +
					problemClass.getName() +
					"' has incompatible expected value: expected " +
					expectedReturnType.getName() +
					", got " +
					describeValueType(testCase.expected())
				);
			}
		}
	}

	private void validateSolutions(Class<?> problemClass, ProblemContract contract, List<Method> solutions) {
		for (Method solutionMethod : solutions) {
			validateSolutionSignature(problemClass, contract, solutionMethod);
		}
	}

	private void validateSolutionSignature(
		Class<?> problemClass,
		ProblemContract contract,
		Method solutionMethod
	) {
		Class<?>[] actualParams = solutionMethod.getParameterTypes();
		Class<?>[] expectedParams = contract.parameterTypes();

		if (actualParams.length != expectedParams.length) {
			throw new IllegalStateException(
				"@Solution method '" +
				solutionMethod.getName() +
				"' in " +
				problemClass.getName() +
				" has " +
				actualParams.length +
				" parameter(s), but contract requires " +
				expectedParams.length
			);
		}

		for (int i = 0; i < expectedParams.length; i++) {
			if (!sameType(actualParams[i], expectedParams[i])) {
				throw new IllegalStateException(
					"@Solution method '" +
					solutionMethod.getName() +
					"' in " +
					problemClass.getName() +
					"' has incompatible parameter type at index " +
					i +
					": expected " +
					expectedParams[i].getName() +
					", got " +
					actualParams[i].getName()
				);
			}
		}

		if (!sameType(solutionMethod.getReturnType(), contract.returnType())) {
			throw new IllegalStateException(
				"@Solution method '" +
				solutionMethod.getName() +
				"' in " +
				problemClass.getName() +
				"' has incompatible return type: expected " +
				contract.returnType().getName() +
				", got " +
				solutionMethod.getReturnType().getName()
			);
		}
	}

	//#################################
	private Object instantiate(Class<?> problemClass) {
		try {
			var constructor = problemClass.getDeclaredConstructor();
			constructor.setAccessible(true);

			return constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to instantiate " + problemClass.getName(), e);
		}
	}

	private ProblemContract readContract(Class<?> problemClass) {
		Field contractField = null;

		for (Field field : problemClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Contract.class)) {
				continue;
			}

			if (contractField != null) {
				throw new IllegalStateException(
					"Problem class " + problemClass.getName() + " must declare exactly one @Contract field"
				);
			}

			contractField = field;
		}

		if (contractField == null) {
			throw new IllegalStateException(
				"Problem class " + problemClass.getName() + " must declare a @Contract field"
			);
		}

		int modifiers = contractField.getModifiers();

		if (!Modifier.isStatic(modifiers)) {
			throw new IllegalStateException(
				"@Contract field '" +
				contractField.getName() +
				"' in " +
				problemClass.getName() +
				" must be static"
			);
		}

		if (!Modifier.isFinal(modifiers)) {
			throw new IllegalStateException(
				"@Contract field '" +
				contractField.getName() +
				"' in " +
				problemClass.getName() +
				" must be final"
			);
		}

		if (!ProblemContract.class.equals(contractField.getType())) {
			throw new IllegalStateException(
				"@Contract field '" +
				contractField.getName() +
				"' in " +
				problemClass.getName() +
				" must be of type " +
				ProblemContract.class.getName()
			);
		}

		try {
			contractField.setAccessible(true);

			Object value = contractField.get(null);

			if (value == null) {
				throw new IllegalStateException(
					"@Contract field '" +
					contractField.getName() +
					"' in " +
					problemClass.getName() +
					" must not be null"
				);
			}

			return (ProblemContract) value;
		} catch (IllegalAccessException e) {
			throw new RuntimeException(
				"Failed to access @Contract field '" +
				contractField.getName() +
				"' in " +
				problemClass.getName(),
				e
			);
		}
	}

	private List<TestCase<?>> collectCases(Class<?> problemClass, Object problemInstance) {
		List<TestCase<?>> testCases = new ArrayList<>();

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

	private List<TestCase<?>> extractCases(Object value, String source) {
		if (value == null) {
			throw new IllegalStateException(source + " returned null");
		}

		if (value instanceof TestCase<?> testCase) {
			return List.of(testCase);
		}

		if (value instanceof List<?> list) {
			List<TestCase<?>> testCases = new ArrayList<>();

			for (Object element : list) {
				if (!(element instanceof TestCase<?> testCase)) {
					throw new IllegalStateException(source + " contains non-TestCase element: " + element);
				}

				testCases.add(testCase);
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

			method.setAccessible(true);
			solutions.add(method);
		}

		solutions.sort(Comparator.comparing(Method::getName));

		return solutions;
	}

	private SolutionResult runSolution(Object instance, Method solution, List<TestCase<?>> testCases) {
		Solution solutionAnnotation = solution.getAnnotation(Solution.class);
		List<CaseResult> caseResults = new ArrayList<>();

		for (TestCase<?> testCase : testCases) {
			Object actual = ReflectionExecutor.invoke(solution, instance, testCase.arguments().values());
			boolean isPassed = ResultComparator.areEqual(testCase.expected(), actual);

			caseResults.add(new CaseResult(testCase.name(), testCase.expected(), actual, isPassed));
		}

		return new SolutionResult(solutionAnnotation.name(), caseResults);
	}

	//#################################
	//## Helpers ######################
	//#################################

	private boolean isValueCompatible(Class<?> expectedType, Object value) {
		if (value == null) {
			return !expectedType.isPrimitive();
		}

		Class<?> actualType = value.getClass();
		return toWrapper(expectedType).isAssignableFrom(toWrapper(actualType));
	}

	private String describeValueType(Object value) {
		return value == null ? "null" : value.getClass().getName();
	}

	private boolean sameType(Class<?> actual, Class<?> expected) {
		if (actual.equals(expected)) {
			return true;
		}

		return toWrapper(actual).equals(toWrapper(expected));
	}

	private Class<?> toWrapper(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}

		if (type == int.class) return Integer.class;
		if (type == long.class) return Long.class;
		if (type == boolean.class) return Boolean.class;
		if (type == double.class) return Double.class;
		if (type == float.class) return Float.class;
		if (type == char.class) return Character.class;
		if (type == byte.class) return Byte.class;
		if (type == short.class) return Short.class;
		if (type == void.class) return Void.class;

		throw new IllegalArgumentException("Unknown primitive type: " + type.getName());
	}
}
