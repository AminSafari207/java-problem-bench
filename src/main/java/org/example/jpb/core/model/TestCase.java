package org.example.jpb.core.model;

import java.util.Objects;

public record TestCase<E>(String name, Arguments arguments, E expected) {
	public TestCase {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(arguments, "arguments must not be null");
		Objects.requireNonNull(expected, "expected must not be null");
	}
}
