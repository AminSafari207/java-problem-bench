package org.example.jpb.core.model;

import java.util.UUID;
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
		this.id = ModelChecks.defaultIfBlank(id, UUID.randomUUID().toString());
		this.displayName = ModelChecks.requireNonBlank(displayName, "displayName");
		this.arguments = ModelChecks.requireNonNull(arguments, "arguments");
		this.expected = ModelChecks.requireNonNull(expected, "expected");
	}

	public static TestCase of(String id, String displayName, Arguments arguments, Object expected) {
		return new TestCase(id, displayName, arguments, expected);
	}

	public static TestCase of(String displayName, Arguments arguments, Object expected) {
		return new TestCase(null, displayName, arguments, expected);
	}

	public Object[] getDeepClonedArguments() {
		return arguments.deepValues();
	}
}
