package org.example.jpb.core.model;

import java.util.Arrays;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public final class ProblemContract {

	private final Class<?>[] acceptedTypes;
	private final Class<?> expectedType;

	private ProblemContract(Class<?>[] acceptedTypes, Class<?> expectedType) {
		this.acceptedTypes = ModelChecks.requireNonNull(acceptedTypes, "acceptedTypes");
		this.expectedType = ModelChecks.requireNonNull(expectedType, "expectedType");
	}

	public static ProblemContract of(Class<?>[] acceptedTypes, Class<?> expectedType) {
		return new ProblemContract(acceptedTypes, expectedType);
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
}
