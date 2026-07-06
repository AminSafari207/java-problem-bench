package org.example.jpb.core.model;

import java.util.Objects;

public final class ProblemContract {

	private final Class<?>[] parameterTypes;
	private final Class<?> returnType;

	private ProblemContract(Class<?>[] parameterTypes, Class<?> returnType) {
		Objects.requireNonNull(parameterTypes, "parameterTypes must not be null");
		Objects.requireNonNull(returnType, "returnType must not be null");

		this.parameterTypes = parameterTypes.clone();
		verifyParameterTypesElements(this.parameterTypes);

		this.returnType = returnType;
	}

	public static ReturnStep accepts(Class<?>... parameterTypes) {
		return returnType -> new ProblemContract(parameterTypes, returnType);
	}

	public Class<?>[] parameterTypes() {
		return parameterTypes.clone();
	}

	public Class<?> returnType() {
		return returnType;
	}

	private void verifyParameterTypesElements(Class<?>[] parameterTypes) {
		for (int i = 0; i < parameterTypes.length; i++) {
			if (this.parameterTypes[i] == null) {
				throw new IllegalArgumentException("parameterTypes[" + i + "] must not be null");
			}
		}
	}

	@FunctionalInterface
	public interface ReturnStep {
		ProblemContract expects(Class<?> returnType);
	}
}
