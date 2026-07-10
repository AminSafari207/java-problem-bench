package org.example.jpb.core.model;

import java.lang.reflect.Method;
import lombok.Builder;
import lombok.Getter;
import org.example.jpb.util.ModelChecks;

@Getter
public class PreparedSolution {

	private final String id;
	private final String displayName;
	private final Method solutionMethod;

	@Builder
	public PreparedSolution(String id, String displayName, Method solutionMethod) {
		this.id = ModelChecks.requireNormalizedNonBlank(id, "id");
		this.displayName = ModelChecks.defaultIfBlank(displayName, this.id);
		this.solutionMethod = ModelChecks.requireNonNull(solutionMethod, "solutionMethod");
	}
}
