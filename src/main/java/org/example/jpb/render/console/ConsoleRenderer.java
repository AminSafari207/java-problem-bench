package org.example.jpb.render.console;

import java.util.Arrays;
import org.example.jpb.core.model.CaseResult;
import org.example.jpb.core.model.ProblemResult;
import org.example.jpb.core.model.SolutionResult;
import org.example.jpb.util.Console;

public class ConsoleRenderer {

	public void render(ProblemResult result) {
		renderProblemHeader(result);

		for (SolutionResult solution : result.solutions()) {
			renderSolution(solution);
		}

		Console.print("");
	}

	private void renderProblemHeader(ProblemResult result) {
		Console.print("");
		Console.print(
			Console.gray("---") + "Problem: " + Console.blue(result.problemName()) + Console.gray("---")
		);
	}

	private void renderSolution(SolutionResult solution) {
		Console.print("  " + Console.gray("Solution:") + " " + solution.solutionName());

		for (CaseResult caseResult : solution.cases()) {
			renderCase(caseResult);
		}

		renderSummary(solution);
	}

	private void renderCase(CaseResult caseResult) {
		if (caseResult.passed()) {
			Console.print("    " + Console.green("[PASS]") + " " + caseResult.caseName());
			return;
		}

		Console.print("    " + Console.red("[FAIL]") + " " + caseResult.caseName());
		Console.print("      " + Console.gray("expected:") + " " + formatValue(caseResult.expected()));
		Console.print("      " + Console.gray("actual:  ") + " " + formatValue(caseResult.actual()));
	}

	private void renderSummary(SolutionResult solution) {
		long passed = solution.passedCount();
		long total = solution.totalCount();

		String summary = String.format("    Passed %d/%d", passed, total);

		if (solution.allPassed()) {
			Console.success(summary);
		} else {
			Console.warn(summary);
		}
	}

	private String formatValue(Object value) {
		if (value == null) return "null";

		if (value.getClass().isArray()) {
			return Arrays.deepToString((Object[]) value);
		}

		return String.valueOf(value);
	}
}
