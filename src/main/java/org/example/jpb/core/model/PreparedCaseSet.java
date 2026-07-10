package org.example.jpb.core.model;

import java.util.List;
import javax.xml.transform.Source;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public final class PreparedCaseSet {

	private final String id;
	private final String displayName;
	private final List<TestCase> testCases;

	@Builder
	private PreparedCaseSet(String id, String displayName, List<TestCase> testCases) {
		this.id = ModelChecks.requireNormalizedNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.testCases = ModelChecks.requireNonEmptyCopy(testCases, "testCases");

		ModelChecks.requireUniqueIds(this.testCases, TestCase::getId, "testCases");
	}
}
