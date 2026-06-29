package org.example.jpb.model;

import java.util.Objects;

public record TestCase<I, E>(String name, I input, E expected) {
	public TestCase {
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(input, "input must not be null");
		Objects.requireNonNull(expected, "expected must not be null");
	}
}
