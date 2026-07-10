package org.example.jpb.core.model;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public final class ProblemContract {

	private final Class<?>[] acceptedTypes;
	private final Class<?> expectedType;

	private ProblemContract(Class<?>[] acceptedTypes, Class<?> expectedType) {
		this.acceptedTypes = requireAcceptedTypes(acceptedTypes);
		this.expectedType = ModelChecks.requireNonNull(expectedType, "expectedType");
	}

	public static ReturnStep accepts(Class<?>... acceptedTypes) {
		Class<?>[] copiedAcceptedTypes = requireAcceptedTypes(acceptedTypes);
		return expectedType -> new ProblemContract(copiedAcceptedTypes, expectedType);
	}

	private static Class<?>[] requireAcceptedTypes(Class<?>[] acceptedTypes) {
		ModelChecks.requireNonNull(acceptedTypes, "acceptedTypes");

		Class<?>[] copy = acceptedTypes.clone();

		for (int i = 0; i < copy.length; i++) {
			ModelChecks.requireNonNull(copy[i], "acceptedTypes[" + i + "]");
		}

		return copy;
	}

	@Override
	public String toString() {
		return (
			"ProblemContract{" +
			"acceptedTypes=" +
			Arrays.toString(acceptedTypes) +
			", expectedType=" +
			expectedType +
			'}'
		);
	}

	@FunctionalInterface
	public interface ReturnStep {
		ProblemContract expects(Class<?> returnType);
	}
}
