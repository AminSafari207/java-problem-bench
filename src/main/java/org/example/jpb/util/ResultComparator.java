package org.example.jpb.util;

import java.util.Arrays;
import java.util.Objects;

public final class ResultComparator {

	private ResultComparator() {
		System.out.println("ResultComparator must not be instantiated");
	}

	public static boolean areEqual(Object expected, Object actual) {
		if (expected == actual) {
			return true;
		}

		if (expected == null || actual == null) {
			return false;
		}

		Class<?> expectedClass = expected.getClass();
		Class<?> actualClass = actual.getClass();

		if (expectedClass.isArray() && actualClass.isArray()) {
			if (expected instanceof Object[] e && actual instanceof Object[] a) {
				return Arrays.deepEquals(e, a);
			}

			if (expected instanceof int[] e && actual instanceof int[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof long[] e && actual instanceof long[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof byte[] e && actual instanceof byte[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof short[] e && actual instanceof short[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof char[] e && actual instanceof char[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof boolean[] e && actual instanceof boolean[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof float[] e && actual instanceof float[] a) {
				return Arrays.equals(e, a);
			}

			if (expected instanceof double[] e && actual instanceof double[] a) {
				return Arrays.equals(e, a);
			}
		}

		return Objects.equals(expected, actual);
	}
}
