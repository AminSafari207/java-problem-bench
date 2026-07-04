package org.example.jpb.core.model;

public final class Arguments {

	private static final Arguments NONE = new Arguments(new Object[0]);

	private final Object[] values;

	private Arguments(Object[] values) {
		this.values = values.clone();
	}

	public static Arguments none() {
		return NONE;
	}

	public static Arguments of(Object first, Object... rest) {
		int len = 1 + rest.length;
		Object[] arr = new Object[len];
		arr[0] = first;

		System.arraycopy(rest, 0, arr, 1, rest.length);

		return new Arguments(arr);
	}

	public static Arguments single(Object value) {
		return new Arguments(new Object[] { value });
	}

	public Object[] values() {
		return values.clone();
	}

	public int size() {
		return values.length;
	}

	public Object get(int index) {
		return values[index];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Arguments(");

		for (int i = 0; i < values.length; i++) {
			sb.append(values[i]);
			if (i + 1 < values.length) {
				sb.append(", ");
			}
		}

		sb.append(")");

		return sb.toString();
	}
}
