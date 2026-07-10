package org.example.jpb.core.model;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public final class PreparedCase {

	private final String id;
	private final String displayName;
	private final List<TestCase> cases;

	@Builder
	private PreparedCase(String id, String displayName, List<TestCase> cases) {
		this.id = ModelChecks.requireNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.cases = ModelChecks.requireNonEmptyCopy(cases, "cases");
	}
}
