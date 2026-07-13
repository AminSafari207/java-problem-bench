# Architectural flow

This document describes how java-problem-bench works internally.

The README covers the basics. this is for when you want to understand the pipeline or extend it.

---
## Overview

The system has five layers:

1. **Annotations** -- markers on problem classes that declare what a problem is
2. **Model** -- validated classes that hold contracts, prepared artifacts, and results
3. **Preparation** -- reflection-based collector that reads annotations and validates artifacts
4. **Execution** -- correctness runner and benchmark runner
5. **Render** -- output formatters (currently console only)

```
  @Problem class
        |
        v
  ProblemExecutor.execute()
        |
        +-- ProblemPreparator.prepare()
        |     +-- validate & instantiate
        |     +-- read @Contract
        |     +-- collect @CaseSet artifacts
        |     +-- collect @Solution methods
        |     +-- validate signatures & uniqueness
        |     |
        |     v
        |   PreparedProblem
        |
        +-- ProblemRunner.run()
        |     +-- execute & compare
        |     |
        |     v
        |   ProblemResult
        |
        +-- filter passed solutions by id
        |
        +-- BenchmarkRunner.run()  (or skip if none passed)
        |     |
        |     v
        |   ProblemBenchmarkResult
        |
        v
  ProblemExecutionResult
        |
        v
  ProblemConsoleRenderer.renderProblemResult()
  ProblemConsoleRenderer.renderBenchmarkResult()
```

`ProblemRunner` can also be used on its own if you only want correctness and already have a `PreparedProblem`.

---
## Annotations

| Annotation | Target | Purpose |
|---|---|---|
| `@Problem(id, displayName?)` | class | Marks a class as a runnable problem |
| `@Contract(accepts, expects)` | class | Declares the parameter and return types that case sets and solutions must match |
| `@CaseSet(id, displayName?)` | method or field | Provides one or more `TestCase` instances as a grouped set |
| `@Solution(id, displayName?)` | method | A candidate solution to run against all case sets |
| `@Generator` | method | Planned: generate random cases at runtime (not wired yet) |

### ID and display name rules

Every `@Problem`, `@CaseSet`, `@Solution`, and `TestCase` must have a non-blank `id`.

`displayName` is optional everywhere. when it's blank, the model falls back to `id`.

Lengths are enforced via `ModelLimits`:

- `id`: 1--64 characters
- `displayName`: 1--120 characters

IDs must be unique within their scope:

- case set ids must be unique per problem
- solution ids must be unique per problem
- test case ids must be unique within a case set

Values must be non-blank and must not contain leading or trailing whitespace.

### Contract rules

The problem class must declare exactly one `@Contract` annotation.

- `accepts` — array of parameter types, in order
- `expects` — return type

During preparation, the annotation is read from the class and converted into a `ProblemContract` for validation.

### Case set rules

A `@CaseSet` method or field may produce:

- a single `TestCase`, or
- a `List<TestCase>`

Each test case carries an `id`, optional `displayName`, an `Arguments` object, and an expected return value. the runner validates that argument count and types match the contract, and that the expected value is compatible with the declared return type.

### Solution rules

Each `@Solution` method must match the contract exactly: same parameter count, same parameter types, same return type. Primitive and wrapper types are treated as equivalent (`int` vs `Integer`).

Solutions are sorted by `id` before execution.

---
## Model

Most model classes are immutable builder classes validated through `ModelChecks`. Lombok generates the boilerplate.

### ProblemContract

Internal model built from `@Contract` during preparation:

```java
ProblemContract.of(new Class<?>[] { Integer.class, String.class }, Boolean.class);
```

Stores `Class<?>[] acceptedTypes` and `Class<?> expectedType`. Used only for validation at runtime; it does not perform any invocation itself.

Problem authors declare the contract on the class instead:

```java
@Contract(accepts = { Integer.class, String.class }, expects = Boolean.class)
```

### Arguments

Wraps the input values for a test case:

- `Arguments.none()` -- zero arguments
- `Arguments.single(value)` -- one argument
- `Arguments.of(first, rest...)` -- multiple arguments

Values are cloned on access to prevent mutation.

### TestCase

```java
TestCase.of("case-id", Arguments.single(1), 2);
TestCase.of("case-id", "My display name", Arguments.single(1), 2);
```

Fields:

- `id` -- required, unique within its case set
- `displayName` -- optional, falls back to `id`
- `arguments` -- non-null
- `expected` -- non-null

`getDeepClonedArguments()` returns a defensive copy of the argument values.

### Prepared artifacts

`ProblemPreparator` produces a `PreparedProblem` that holds everything needed for execution:

```
PreparedProblem
  id, displayName
  problemClass, problemInstance
  contract
  caseSets: List<PreparedCaseSet>
    id, displayName
    testCases: List<TestCase>
  solutions: List<PreparedSolution>
    id, displayName
    solutionMethod
```

`PreparedProblem.withNewSolutions(...)` creates a copy with a different solution list. `ProblemExecutor` uses this to benchmark only the solutions that passed correctness.

### Result models

After a run completes, results are structured in four levels:

```
ProblemResult
  problemId, problemDisplayName
  solutionResults: List<SolutionResult>
    solutionId, solutionDisplayName
    caseSetResults: List<CaseSetResult>
      caseSetId, caseSetDisplayName
      testCaseResults: List<TestCaseResult>
        testCaseId, testCaseDisplayName
        expected, actual, passed
```

`SolutionResult`, `CaseSetResult`, and `TestCaseResult` expose pass/total counts and `isPassed()` for summary rendering.

Benchmark results are separate:

```
ProblemExecutionResult
  problemResult: ProblemResult
  problemBenchmarkResult: ProblemBenchmarkResult
    problemId, problemDisplayName
    solutionBenchmarkResults: List<SolutionBenchmarkResult>
      solutionId, solutionDisplayName
      status: SUCCESS | FAILED
      stats: BenchmarkStats (sampleCount, totalNanos, minNanos, maxNanos)
      errorMessage (on failure)
    skipReason (when benchmark was skipped)
```

`BenchmarkStats.getAverageNanos()` is computed from `totalNanos / sampleCount`.

### BenchmarkConfig

Controls warmup and measurement:

- `warmupIterations` -- default 1,000, must be >= 0
- `measurementIterations` -- default 10,000, must be > 0

---
## Execution guarantees and current assumptions

The framework currently assumes problem definitions are deterministic and that
solution methods do not rely on external state.

- Case sets and solutions are executed sequentially.
- A single prepared problem instance is used during a run.
- Solutions should not mutate shared problem state.
- Mutable case arguments are copied only for value types supported by
  `Arguments`.
- Benchmark timing includes the current invocation path and test-case traversal.
  It does not yet isolate algorithm time from reflection or framework overhead.
- Benchmark results are comparative and educational, not a replacement for JMH.

---
## Preparation pipeline

`ProblemPreparator.prepare(Class<?> problemClass)` follows this sequence:

### 1. Validate problem class

- Class must not be null
- Class must have `@Problem`

### 2. Instantiate

Creates an instance via the no-argument constructor (made accessible via reflection). The instance is used when invoking `@CaseSet` methods and `@Solution` methods.

### 3. Read contract

Reads `@Contract` from the problem class and builds a `ProblemContract` from `accepts` and `expects`.

### 4. Collect case sets

Iterates declared methods and fields annotated with `@CaseSet`:

- Methods are invoked on the problem instance
- Fields are read from the problem instance
- Return values are normalized into `PreparedCaseSet` objects with their test cases

### 5. Collect solutions

Finds all methods annotated with `@Solution`, makes them accessible, sorts by `id`.

### 6. Validate artifacts

Fails fast if:

- no case sets found
- no solutions found
- duplicate ids within case sets, solutions, or test cases
- any test case has wrong argument count or incompatible types
- any solution has a mismatched signature

### 7. Return

Builds and returns a validated `PreparedProblem`.

---
## Correctness pipeline

`ProblemRunner.run(PreparedProblem)` (or `run(Class<?>)` which prepares first):

For each solution, for each case set, for each test case:

1. Invoke the solution method with deep-cloned case arguments via `ReflectionExecutor`
2. Compare the return value to the expected value via `ResultComparator`
3. Build a `TestCaseResult`, roll up into `CaseSetResult`, then `SolutionResult`

If invocation throws, the runner wraps it in a `RuntimeException` with context about which solution and test case failed.

Assembles all `SolutionResult` objects into a `ProblemResult` and returns it. Rendering is intentionally outside the runner.

---
## Benchmark pipeline

`ProblemExecutor` orchestrates correctness and benchmarking:

1. Prepare the problem through `ProblemPreparator`.
2. Run correctness through `ProblemRunner`.
3. Select solutions that passed all configured correctness cases.
4. If no solution passes, return a skipped `ProblemBenchmarkResult`.
5. Otherwise, run `BenchmarkRunner` on passing solutions only.

`BenchmarkRunner.run(PreparedProblem, BenchmarkConfig)` currently processes one
solution at a time:

1. **Warmup**  
   Invokes the solution across every configured test case
   `warmupIterations` times. This gives the JVM an opportunity to profile and
   compile hot paths, but does not guarantee stable JIT state.

2. **Measurement**  
   Repeats the complete test-case workload `measurementIterations` times.
   Each repetition is one benchmark sample.

3. **Statistics**  
   Records total elapsed nanoseconds and derives minimum, maximum, and average
   sample duration.

4. **Failure handling**  
   An exception during warmup or measurement marks that solution benchmark as
   `FAILED` and stores an error message.

A measurement sample currently represents one full pass across all case sets and
test cases for one solution. Timing is wall-clock elapsed time for that complete
workload.

The current benchmark implementation is intentionally basic:

- it uses `System.nanoTime()`;
- it runs in-process;
- it includes framework overhead such as reflective invocation and traversal;
- it does not yet use batch timing, result sinks, JVM forks, adaptive warmup,
  percentile statistics, or process isolation;
- it is useful for learning and rough comparisons, not as a replacement for
  JMH.
---
## Utilities

### ModelChecks

Central validation helpers used by model builders:

- null/blank/whitespace checks
- length bounds
- unique id enforcement in collections
- non-empty list copies

### ReflectionExecutor

Centralizes reflective invocation. Unwraps `InvocationTargetException` so the original runtime exception or error propagates cleanly.

### ResultComparator

Compares expected and actual values:

- reference equality first
- null handling
- array deep equality for all primitive and object array types
- `Objects.equals` for everything else

This is correctness comparison only. timing is handled separately by `BenchmarkRunner`.

### Console

ANSI-colored output helpers, box drawing, indentation, truncation, padding, and visual-length calculation (so padding works correctly even with color codes).

---
## Renderer

`ProblemConsoleRenderer` has two entry points:

### Correctness rendering

`renderProblemResult(ProblemResult, ConsoleRenderOptions)` prints:

- a boxed problem header
- per-solution sections with pass/fail lines
- per-case-set and per-test-case detail (configurable)
- expected vs actual details on failure
- summary counts (`N / M passed`) colored green or red

### Benchmark rendering

`renderBenchmarkResult(ProblemBenchmarkResult, ConsoleRenderOptions)` prints:

- a boxed benchmark header
- skipped message if benchmark was not run
- a table with status, solution name, samples, avg/min/max timing
- failed benchmark rows with truncated error messages
- successful rows sorted by average time (fastest first)

### ConsoleRenderOptions

| Option | Default | Purpose |
|---|---|---|
| `showIds` | `true` | Show id column in correctness output |
| `showPassedCaseSets` | `false` | Show case sets that fully passed |
| `showPassedTestCases` | `false` | Show individual passed test cases |
| `showBenchmark` | `true` | Render benchmark section at all |
| `maxFailureDetails` | unlimited | Cap how many failed test case details are shown per case set |

The renderer does not influence execution. You can skip it entirely and inspect the result models directly.

---
## Extension points

These are the natural places to add new behavior:

| Extension | Where to hook in |
|---|---|
| Random case generation | New collector in `ProblemPreparator` for `@Generator` methods |
| Problem discovery | New scanner that finds `@Problem` classes on the classpath |
| Richer benchmarking | Per-case timing, JVM warmup control, GC hints, or JMH integration in `BenchmarkRunner` |
| New output formats | Additional renderers alongside `ProblemConsoleRenderer` |
| Selective benchmarking | Filter which case sets or test cases feed into benchmark measurement |

---
## Error handling

The runner fails early and with specific messages. Validation errors use `IllegalStateException` or `IllegalArgumentException` with the problem class name, field/method name, and what went wrong. Execution errors include the solution name and test case name.

This is intentional. When you are iterating on a problem definition, you want to know exactly which annotation or signature is wrong, not get a generic reflection stack trace.

Benchmark failures are softer: a thrown exception during warmup or measurement marks that solution as `FAILED` in the benchmark result instead of aborting the whole run.

---
## Current limitations

- `@Generator` is defined but not collected or executed.
- Only declared methods and fields on the problem class itself are scanned;
  inherited members are ignored.
- Correctness execution is sequential; bounded parallel execution is a future
  improvement.
- The current benchmark is an in-process comparative timer, not a rigorous JVM
  microbenchmark framework:
    - reflective invocation and framework traversal are included in timing;
    - timing is per full test-case workload, not normalized `ns/op`;
    - no result sink currently protects against all optimizer effects;
    - no JVM forks, adaptive warmup, percentile reporting, or environment
      metadata;
    - mutable input isolation is limited to what `Arguments` can defensively copy.
- No problem discovery exists yet; callers pass problem classes explicitly.
- Console alignment is ANSI-aware for the renderer's own styling, but does not
  fully support wide Unicode terminal characters.

These are the gaps the roadmap in the README is meant to close.
