# Assignment 4 — Dynamic Programming and String Processing

Java 17 solution based on the [official template](https://github.com/RZhaniya/daa-assignment-4-archive), revision `4686cc60334aa189c2ac168e10a82d882b32858c`.

## Requirements

- JDK 17 or newer, with `java` and `javac` on PATH.
- Python 3.12 or newer for the launcher and tests.

## Build and test

Run from the repository root:

```sh
python tools/run.py --setup
python tools/run.py --check
python tests/student/check.py
```

The launcher compiles Java with `--release 17`. Public checks cover 44 cases and a repeated mixed batch. Student checks use independent small-input oracles, witness validation and maximum-domain cases.

## Run

```sh
python tools/run.py --input requests.jsonl
```

Each input line is one JSON object; each output line is one JSON result. Supported commands: `lcs`, `merge`, `tree`, `naive`, `kmp`, `rk`, `suffix`, `aho`.

Example request:

```json
{"task":"kmp","text":"abababa","pattern":"aba"}
```

Result:

```json
{"positions":[0,2,4]}
```

See [contracts](docs/guide/CONTRACTS.md) for limits and output fields, and [ANALYSIS.md](ANALYSIS.md) for correctness, complexity, tests and measurements.

## Search comparison

After running setup:

```sh
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

## Submission

`submission.json` selects `Java`; no student ID or variant is required. Submit the full commit URL and `Java` in Moodle Online text, then click **Submit assignment**. Use the commit whose GitHub **Public checks** run passed.
