package org.example.jpb.core.model;

import java.util.Objects;

public final class PreparedCase {

	private final String name;
	private final Arguments arguments;
	private final Object expected;

	public PreparedCase(String name, Arguments arguments, Object expected) {
		this.name = Objects.requireNonNull(name, "name must not be null");
		this.arguments = Objects.requireNonNull(arguments, "arguments must not be null");
		this.expected = expected;
	}

	public String name() {
		return name;
	}

	public Object expected() {
		return expected;
	}

	public Object[] newArguments() {
		return arguments.deepValues();
	}
}
