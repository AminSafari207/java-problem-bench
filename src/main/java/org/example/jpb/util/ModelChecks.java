package org.example.jpb.util;

import java.util.*;
import java.util.function.Function;

public final class ModelChecks {

	private ModelChecks() {
		throw new AssertionError("No instances");
	}

	public static String requireNormalizedNonBlank(String value, String fieldName) {
		requireNonNull(value, fieldName);

		if (value.isBlank()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}

		if (!value.equals(value.strip())) {
			throw new IllegalArgumentException(fieldName + " must not start or end with whitespace");
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

	public static <T> void requireUniqueIds(
		Collection<T> items,
		Function<T, String> idGetter,
		String fieldName
	) {
		if (items == null || items.isEmpty()) return;

		Set<String> seenIds = new HashSet<>(items.size());

		for (T item : items) {
			String id = idGetter.apply(item);

			if (!seenIds.add(id)) {
				throw new IllegalArgumentException("Duplicate " + fieldName + " id: '" + id + "'");
			}
		}
	}
}
