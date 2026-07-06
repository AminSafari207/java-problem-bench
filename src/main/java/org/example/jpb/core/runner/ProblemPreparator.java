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

public class ProblemPreparator {

	public PreparedProblem prepare(Class<?> problemClass) {
		validateProblemClass(problemClass);

		Problem problem = problemClass.getAnnotation(Problem.class);
		Object problemInstance = instantiate(problemClass);
		ProblemContract contract = readContract(problemClass);
		List<TestCase<?>> testCases = collectCases(problemClass, problemInstance);
		List<Method> solutionMethods = collectSolutions(problemClass);

		validateArtifacts(problemClass, contract, testCases, solutionMethods);

		return new PreparedProblem(
			problem.name(),
			problemClass,
			problemInstance,
			contract,
			mapCases(testCases),
			mapSolutions(solutionMethods)
		);
	}

	private Object instantiate(Class<?> problemClass) {
		try {
			var constructor = problemClass.getDeclaredConstructor();
			constructor.setAccessible(true);

			return constructor.newInstance();
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(
				"Invalid problem class: " +
				problemClass.getName() +
				" must declare an accessible no-argument constructor",
				e
			);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to instantiate problem class: " + problemClass.getName(), e);
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

			Object value = ReflectionExecutor.invoke(problemInstance, method);

			testCases.addAll(extractCases(value, "@Case method " + method.getName()));
		}

		for (Field field : problemClass.getDeclaredFields()) {
			if (!field.isAnnotationPresent(Case.class)) continue;

			try {
				field.setAccessible(true);

				Object value = field.get(problemInstance);

				testCases.addAll(extractCases(value, "@Case field " + field.getName()));
			} catch (IllegalAccessException e) {
				throw new RuntimeException(
					"Failed to access @Case field '" + field.getName() + "' in " + problemClass.getName(),
					e
				);
			}
		}

		return testCases;
	}

	private List<TestCase<?>> extractCases(Object value, String source) {
		if (value == null) {
			throw new IllegalStateException(source + " produced null");
		}

		if (value instanceof TestCase<?> testCase) {
			return List.of(testCase);
		}

		if (value instanceof List<?> list) {
			List<TestCase<?>> testCases = new ArrayList<>();

			for (int i = 0; i < list.size(); i++) {
				Object element = list.get(i);

				if (!(element instanceof TestCase<?> testCase)) {
					throw new IllegalStateException(
						source +
						" contains non-TestCase element at index " +
						i +
						": " +
						describeValueType(element)
					);
				}

				testCases.add(testCase);
			}

			return testCases;
		}

		throw new IllegalStateException(
			source + " must produce TestCase or List<TestCase>, but got " + value.getClass().getName()
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

	private List<PreparedCase> mapCases(List<TestCase<?>> testCases) {
		List<PreparedCase> preparedCases = new ArrayList<>();

		for (TestCase<?> testCase : testCases) {
			preparedCases.add(new PreparedCase(testCase.name(), testCase.arguments(), testCase.expected()));
		}

		return preparedCases;
	}

	private List<PreparedSolution> mapSolutions(List<Method> solutionMethods) {
		List<PreparedSolution> preparedSolutions = new ArrayList<>();

		for (Method method : solutionMethods) {
			Solution annotation = method.getAnnotation(Solution.class);

			preparedSolutions.add(new PreparedSolution(annotation.name(), method));
		}

		return preparedSolutions;
	}

	//#################################
	//## Validators ###################
	//#################################

	private void validateProblemClass(Class<?> problemClass) {
		if (problemClass == null) {
			throw new IllegalArgumentException(
				"Invalid problem class: " + problemClass.getName() + " must not be null"
			);
		}

		if (!problemClass.isAnnotationPresent(Problem.class)) {
			throw new IllegalArgumentException(
				"Invalid problem class: " + problemClass.getName() + " is not annotated with @Problem"
			);
		}
	}

	private void validateArtifacts(
		Class<?> problemClass,
		ProblemContract contract,
		List<TestCase<?>> testCases,
		List<Method> solutions
	) {
		if (testCases.isEmpty()) {
			throw new IllegalStateException(
				"Invalid problem definition: no @Case found in " + problemClass.getName()
			);
		}

		if (solutions.isEmpty()) {
			throw new IllegalStateException(
				"Invalid problem definition: no @Solution found in " + problemClass.getName()
			);
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
					", but got " +
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
