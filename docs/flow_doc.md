# Architectural flow

This document describes how java-problem-bench works internally.

The README covers the basics. this is for when you want to understand the pipeline or extend it.

---
## Overview

The system has four layers:

1. **Annotations** -- markers on problem classes that declare what a problem is
2. **Model** -- typed records and helpers that hold contracts, cases, and results
3. **Runner** -- reflection-based engine that validates and executes everything
4. **Render** -- output formatters (currently console only)

```
  @Problem class
        |
        v
  ProblemRunner.run()
        |
        +-- validate & instantiate
        +-- read @Contract
        +-- collect @Case artifacts
        +-- collect @Solution methods
        +-- validate signatures
        +-- execute & compare
        |
        v
  ProblemResult
        |
        v
  ProblemConsoleRenderer.render()
```

---
## Annotations

| Annotation | Target | Purpose                                                                           |
|---|---|-----------------------------------------------------------------------------------|
| `@Problem(name)` | class | Marks a class as a runnable problem                                               |
| `@Contract` | static final field | Holds a `ProblemContract` describing the method and test case arguments signature |
| `@Case` | method or field | Provides one or more `TestCase` instances                                         |
| `@Solution(name)` | method | A candidate solution to run against all cases                                     |
| `@Generator` | method | Planned: generate random cases at runtime (not wired yet)                         |

### Contract rules

The `@Contract` field must be:

- `static`
- `final`
- of type `ProblemContract`
- non-null

There must be exactly one per problem class.

### Case rules

A `@Case` method or field may produce:

- a single `TestCase<?>`, or
- a `List<TestCase<?>>`

Each case carries a name, an `Arguments` object, and an expected return value. The runner validates that argument count and types match the contract, and that the expected value is compatible with the declared return type.

### Solution rules

Each `@Solution` method must match the contract exactly: same parameter count, same parameter types, same return type. Primitive and wrapper types are treated as equivalent (`int` vs `Integer`).

Solutions are sorted alphabetically by method name before execution.

---
## Model

### ProblemContract

Built with a small fluent API:

```java
ProblemContract.input(Integer.class, String.class).returns(Boolean.class);
```

Stores `Class<?>[] parameterTypes` and `Class<?> returnType`. Used only for validation and documentation at runtime.

it does not perform any invocation itself.

### Arguments

Wraps the input values for a test case:

- `Arguments.none()` -- zero arguments
- `Arguments.single(value)` -- one argument
- `Arguments.of(first, rest...)` -- multiple arguments

Values are cloned on access to prevent mutation.

### TestCase

```java
record TestCase<E>(String name, Arguments arguments, E expected)
```

The generic `E` is the expected return type.

Name and arguments must not be null.

### Result records

After a run completes, results are structured as:

```
ProblemResult
  problemName: String
  solutions: List<SolutionResult>
    solutionName: String
    cases: List<CaseResult>
      caseName: String
      expected: Object
      actual: Object
      passed: boolean
```

`SolutionResult` also exposes `passedCount()`, `totalCount()`, and `allPassed()` for summary rendering.

---
## Runner pipeline

`ProblemRunner.run(Class<?> problemClass)` follows this sequence:

### 1. Validate problem class

- Class must not be null
- Class must have `@Problem`

### 2. Instantiate

Creates an instance via the no-argument constructor (made accessible via reflection). The instance is used when invoking `@Case` methods and `@Solution` methods.

### 3. Read contract

Scans declared fields for `@Contract`, validates modifiers and type, reads the static field value.

### 4. Collect cases

Iterates declared methods and fields annotated with `@Case`:

- Methods are invoked on the problem instance
- Fields are read from the problem instance
- Return values are normalized into a flat `List<TestCase<?>>`

### 5. Collect solutions

Finds all methods annotated with `@Solution`, makes them accessible, sorts by name.

### 6. Validate artifacts

Fails fast if:

- no cases found
- no solutions found
- any case has wrong argument count or incompatible types
- any solution has a mismatched signature

### 7. Execute

For each solution, for each test case:

1. Invoke the solution method with the case arguments via `ReflectionExecutor`
2. Compare the return value to the expected value via `ResultComparator`
3. Build a `CaseResult`

If invocation throws, the runner wraps it in a `RuntimeException` with context about which solution and case failed.

### 8. Return

Assembles all `SolutionResult` objects into a `ProblemResult` and returns it. Rendering is intentionally outside the runner.

## Utilities

### ReflectionExecutor

Centralizes reflective invocation. Unwraps `InvocationTargetException` so the original runtime exception or error propagates cleanly.

### ResultComparator

Compares expected and actual values:

- reference equality first
- null handling
- array deep equality for all primitive and object array types
- `Objects.equals` for everything else

This is correctness comparison only. There is no timing or memory measurement yet.

### Console

ANSI-colored output helpers, box drawing, indentation, and visual-length calculation (so padding works correctly even with color codes).

---
## Renderer

`ProblemConsoleRenderer` takes a `ProblemResult` and prints:

- a boxed problem header
- per-solution sections with pass/fail lines
- expected vs actual details on failure
- a summary line (`N / M passed`) colored green, yellow, or red

The renderer does not influence execution. You can skip it entirely and inspect the `ProblemResult` records directly.

---
## Extension points

These are the natural places to add new behavior:

| Extension | Where to hook in                                                 |
|---|------------------------------------------------------------------|
| Benchmarking | Wrap invocation in `runSolution`, add warmup/measurement loops, and attach benchmark metrics to `CaseResult` or a dedicated benchmark result model |
| Random case generation | New collector in `ProblemRunner` for `@Generator` methods        |
| Problem discovery | New scanner that finds `@Problem` classes on the classpath       |

---
## Error handling

The runner fails early and with specific messages. Validation errors use `IllegalStateException` with the problem class name, field/method name, and what went wrong. Execution errors include the solution name and case name.

This is intentional. When you are iterating on a problem definition, you want to know exactly which annotation or signature is wrong, not get a generic reflection stack trace.

---
## Current limitations

- `@Generator` is defined but not collected or executed
- Only declared methods and fields on the problem class itself are scanned (no inheritance walk)
- No parallel execution of solutions or cases
- `Main` is a placeholder and does not run anything

These are the gaps the roadmap in the README is meant to close.
