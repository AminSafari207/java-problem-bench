package org.example.jpb.core.model;

import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

public class TestCase {

	@Getter
	private final String id;

	@Getter
	private final String displayName;

	private final Arguments arguments;

	@Getter
	private final Object expected;

	@Builder
	private TestCase(String id, String displayName, Arguments arguments, Object expected) {
		this.id = ModelChecks.requireNormalizedNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.arguments = ModelChecks.requireNonNull(arguments, "arguments");
		this.expected = ModelChecks.requireNonNull(expected, "expected");
	}

	public static TestCase of(String id, String displayName, Arguments arguments, Object expected) {
		return new TestCase(id, displayName, arguments, expected);
	}

	public static TestCase of(String id, Arguments arguments, Object expected) {
		return new TestCase(id, null, arguments, expected);
	}

	public Object[] getDeepClonedArguments() {
		return arguments.deepValues();
	}
}
