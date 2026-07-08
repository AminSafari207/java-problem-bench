package org.example.jpb.util;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class ModelChecks {

	private ModelChecks() {
		throw new AssertionError("No instances");
	}

	public static String requireNonBlank(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}

		return value;
	}

	public static String defaultIfBlank(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}

	public static <T> T requireNonNull(T value, String fieldName) {
		return Objects.requireNonNull(value, fieldName + " must not be null");
	}

	public static int requireNonNegative(int value, String fieldName) {
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " must not be negative");
		}

		return value;
	}

	public static long requireNonNegative(long value, String fieldName) {
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " must not be negative");
		}

		return value;
	}

	public static double requireNonNegative(double value, String fieldName) {
		if (value < 0) {
			throw new IllegalArgumentException(fieldName + " must not be negative");
		}

		return value;
	}

	public static <T> List<T> requireNonEmptyCopy(Collection<T> values, String fieldName) {
		requireNonNull(values, fieldName);

		if (values.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " must not be empty");
		}

		return List.copyOf(values);
	}

	public static <T> List<T> copyOfNullable(Collection<T> values) {
		return values == null ? List.of() : List.copyOf(values);
	}

	public static <T> List<T> requireCopy(Collection<T> values, String fieldName) {
		requireNonNull(values, fieldName);
		return List.copyOf(values);
	}

	public static void requireLessThanOrEqual(long min, long max, String minFieldName, String maxFieldName) {
		if (min > max) {
			throw new IllegalArgumentException(
				minFieldName + " must be less than or equal to " + maxFieldName
			);
		}
	}

	public static void requireLessThanOrEqual(
		double min,
		double max,
		String minFieldName,
		String maxFieldName
	) {
		if (min > max) {
			throw new IllegalArgumentException(
				minFieldName + " must be less than or equal to " + maxFieldName
			);
		}
	}
}
