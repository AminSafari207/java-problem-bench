# java-problem-bench

This is a personal project. i'm trying to figure out what it would look like to define coding problems the way you'd write them on LeetCode.

There are contract, cases and solutions but in plain Java classes instead of scattering logic across a bunch of test files.

It's not a finished library. it's closer to a framework experiment actually, because of annotation-driven problem definitions, a correctness-first runner, and some console output so I can actually see what the hell happened.

Performance timing and all that other "real benchmark" stuff is still on the list.

Requires:

- Java 21
- Maven

---
## Why I'm building this

When i'm working through algorithm problems i usually end up with one solution method and a test class that grows over time. That works, but it gets messy fast, especially when i want to try a second approach, or a third, and compare them against the same inputs.

So i started sketching a model where a problem is just a class:

- `@Contract` says what the test cases and solutions arguments signature should look like
- `@Case` gives you the inputs and expected outputs
- `@Solution` marks the implementations you want to run

The runner validates that everything lines up, executes each solution against every case, and tells you what passed.

---
## Where things stand

Right now it does correctness checking and nothing more.

If you want more details like validation rules, the runner pipeline and ..., it's all in [flow_doc.md](docs/flow_doc.md).

I kept the README short on purpose.

---
## Try it

There's a dummy problem in the test suite. it runs a couple of simple and stuped solutions against some cases and prints the results.

Use:

```bash
mvn test
```

---
## What a problem looks like

Like this:

```java
@Problem(name = "Dummy")
static class DummyProblem {

    @Contract
    static final ProblemContract contract = ProblemContract.input(Integer.class).returns(Integer.class);

    @Case
    public List<TestCase<Integer>> cases() {
        return List.of(
                new TestCase<>("one", Arguments.single(1), 2),
                new TestCase<>("two", Arguments.single(2), 3)
        );
    }

    @Case
    public List<TestCase<Integer>> cases2 = List.of(
            new TestCase<>("three", Arguments.single(150), 151),
            new TestCase<>("four", Arguments.single(300), 301)
    );

    @Solution(name = "add one")
    public Integer addOne(Integer input) {
        return input + 1;
    }

    @Solution(name = "add two, subtract one")
    public Integer addTwoSubOne(Integer input) {
        return input + 2 - 1;
    }
}
```

And to run it yourself:

```java
ProblemRunner runner = new ProblemRunner();
ProblemResult result = runner.run(DummyProblem.class);

new ProblemConsoleRenderer().render(result);
```

Cases can live on methods or fields. you can attach multiple `@Solution` methods and the runner will hit all of them. Signatures get checked against the contract before anything runs, if something doesn't match, it fails early with a message that actually tells you what went wrong.

---
## What's in the repo

```
src/main/java/org/example/jpb/
  annotation/     markers: @Problem, @Contract, @Case, @Solution, @Generator
  core/
    model/        TestCase, Arguments, ProblemContract, result records
    runner/       ProblemRunner
  render/console/ ProblemConsoleRenderer
  util/           Console, ReflectionExecutor, ResultComparator
```

`Main.java` is basically a placeholder at this point.

No CLI yet. (is it really required tho?)

---
## Roadmap

Stuff that's working and i'm happy with:

- [x] Annotation-based problem definitions
- [x] `@Contract` with a small fluent builder for input/return types
- [x] `@Case` on methods and fields, single case or a list
- [x] `@Solution` — run multiple implementations against the same cases
- [x] Contract validation before execution
- [x] `ResultComparator` that handles arrays and the usual equality cases
- [x] Console renderer with pass/fail output
- [x] One JUnit test that exercises the whole path

Stuff i still need to do:

- [ ] Real benchmarking: timing, warmup, iterations. Right now "bench" only means correctness. lol
- [ ] Discovery and loading pipeline for `@Problem` classes:
    - [ ] Scan the runtime classpath
    - [ ] Load compiled classes from `.class` files
    - [ ] Load problems from external `.jar` files
    - [ ] Support raw `.java` source files via compilation + loading
- [ ] Maven plugin or fat JAR, if this ever gets past the experiment stage

## Notes

This is a learning project first. The API will probably shift as i figure out what feels natural.

For the architectural details, go read [flow_doc.md](docs/flow_doc.md).
