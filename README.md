# DAA Assignment 4 — Dynamic Programming and String Processing

A Java command-line project for comparing sequences, finding an optimal order of adjacent block merges, selecting compatible tree vertices, and exact text matching. It is based on the [official Assignment 4 template](https://github.com/RZhaniya/daa-assignment-4-archive) for the Design and Analysis of Algorithms course.

The project provides a reproducible way to study and evaluate algorithms. Students can explore their behaviour, while instructors or reviewers can run examples, validate results and compare search timings. A shared request format allows different tasks to run in one process.

## Implemented features

| Command | Purpose | Result |
|---|---|---|
| `lcs` | Find a longest common subsequence of two strings | Its length and one optimal subsequence |
| `merge` | Minimize the cost of merging adjacent blocks while preserving their order | Minimum cost and a chronological merge plan |
| `tree` | Find a maximum-weight independent set in a tree | Total weight and selected vertex IDs |
| `naive` | Find every pattern occurrence using direct character comparisons | Starting positions |
| `kmp` | Find every occurrence using the prefix function and KMP | Starting positions |
| `rk` | Search using Rabin–Karp with character verification of hash candidates | Exact positions without collision-induced false matches |
| `suffix` | Build a suffix array and Kasai LCP array | SA, LCP and a longest repeated substring |
| `aho` | Count dictionary patterns in independent texts using Aho–Corasick | A matrix of occurrence counts |

The implementation reconstructs optimal solutions, handles empty inputs according to each command's contract, includes overlapping matches and resets task state between requests. Total weights and merge costs use 64-bit `long` arithmetic. A separate `ping` command checks program startup.

## Big O: time and space complexity

Big O describes an asymptotic upper bound as input size grows, ignoring constant factors. The bounds below apply to the implemented algorithms, including reconstruction and result construction, but exclude JSON parsing and serialization. Auxiliary space is working memory; output space is listed separately.

Notation: `a` and `b` are the two LCS lengths; `n` is the number of blocks or vertices, or the text length; `m` is the pattern length; `z` is the number of returned matches. For Aho–Corasick, `L` is total pattern length, `S` is the number of trie states including the root, `p` is the number of patterns, `q` is the number of texts, and `T` is their total length. The alphabet has 95 characters.

| Command | Time complexity | Auxiliary space complexity | Output space |
|---|---|---|---|
| `lcs` | `O((a+1)*(b+1))` | `O((a+1)*(b+1))` | `O(min(a,b)+1)` |
| `merge` | `O(n^3+1)` | `O(n^2+1)` | `O(n+1)` |
| `tree` | `O(n+1)` | `O(n+1)` | `O(n+1)` |
| `naive` | `O(n*m+n+1)` | `O(1)` | `O(z+1)` |
| `kmp` | `O(n+m+z+1)` | `O(m+1)` | `O(z+1)` |
| `rk` | `O(n*m+n+m+1)` worst case | `O(1)` | `O(z+1)` |
| `suffix` | `O(n*log^2(n+1)+1)` | `O(n+1)` | `O(n+1)` |
| `aho` | `O(L+95*S+p+T+q*(S+p))` | `O(95*S+p)` | `O(q*(p+1)+1)` |

The `+1` terms account for empty inputs. Total space also includes input storage: `O(a+b+1)` for LCS, `O(n+1)` for merge/tree/suffix, `O(n+m+1)` for single-pattern searches, and `O(L+T+p+q+1)` for Aho–Corasick.

- Rabin–Karp takes `O(n+m+c*m)` for `1 <= m <= n`, where `c` is the number of equal-hash candidate windows. Character verification makes collisions safe but can cause quadratic work. This implementation keeps a rolling hash and uses constant auxiliary space.
- Suffix-array construction uses prefix doubling with comparison sorting, hence `O(n*log^2(n+1))` for nonempty input. The following Kasai pass takes `O(n+1)` time.
- Aho–Corasick construction takes `O(L+95*S+p)`. Scanning and reverse-failure count propagation take `O(text_length+S+p)` for each independent text.

Recurrences, correctness arguments, reconstruction and the reasons for these bounds are explained in [ANALYSIS.md](ANALYSIS.md#algorithms).

## How it works

1. Create a UTF-8 file with one JSON request per line.
2. Set the `task` field to a command and supply its required input fields.
3. The Python launcher compiles the Java sources and sends the file to the program.
4. `Main` parses each request through `Json` and calls the corresponding `Algorithms` method.
5. The program returns one JSON result per request.

Character positions, block indices and vertex IDs are zero-based. JSON object-field order is unrestricted. Results go to stdout; build and launcher diagnostics go to stderr.

## Technologies

| Component | Role |
|---|---|
| Java 17+ | All eight algorithms, input/output and solution reconstruction |
| Java standard library | Collections, arrays, sorting, streams and timing |
| Local `Json` class based on the template transport | Parsing and serialization for the published input format |
| Python 3.12+ and its standard library | Compilation, execution, public checks and student tests |
| Git and GitHub | Source control and repository hosting |
| GitHub Actions | Automated checks on Ubuntu 24.04 with Temurin 17 and Python 3.12 |

Maven, Gradle, third-party Java libraries and Python package installation are not required. The running program does not use AI models, external APIs or a database. Development assistance is documented in [ANALYSIS.md](ANALYSIS.md#sources-and-ai-assistance).

## Architecture

~~~text
Java/source/
  Main.java                  JSON Lines input and command dispatch
  Json.java                  JSON parsing and serialization
  Algorithms.java            Algorithm implementations
tools/
  run.py                     Build, execution and public checks
  public_validate.py         Result schema, value and witness validation
tests/
  public.json                44 public examples and boundary cases
  student/
    check.py                 Additional checks and independent oracles
    SearchBenchmark.java     Comparison of naive, KMP and Rabin–Karp
    benchmark-results.md     Recorded timings and medians
docs/guide/CONTRACTS.md       Request schemas, output fields and limits
submission.json             Selected language: Java
ANALYSIS.md                 Correctness, complexity, tests and measurements
.github/workflows/public.yml Automated checks
~~~

Data flow: request file → `tools/run.py` → `Main` → `Json` and `Algorithms` → JSON results. During testing, `public_validate.py` also checks the returned values and reconstructed solutions.

Algorithm state is local to a request. Within one `aho` request, the dictionary automaton is reused across texts, with fresh visit counts for each text.

## Installation and execution

### 1. Prepare the environment

Install Git, JDK 17 or newer, and Python 3.12 or newer. Check that the commands are available:

~~~sh
git --version
java -version
javac -version
python --version
~~~

A JDK is required: `javac` must be available on PATH. On Windows, `py -3` can replace `python` if it selects Python 3.12 or newer.

### 2. Clone the repository

~~~sh
git clone https://github.com/8zellorr8/DAA_asik4.git
cd DAA_asik4
~~~

Run all remaining commands from the repository root.

### 3. Check the build

~~~sh
python tools/run.py --setup
~~~

Expected result: `PASS setup: ping`. The launcher creates `Java/build`, compiles with `--release 17` and runs Java with a 384 MiB heap limit. Setup checks compilation and transport; algorithm results are checked separately below.

### 4. Run a request file

Create a UTF-8 file named `requests.jsonl`, then run:

~~~sh
python tools/run.py --input requests.jsonl
~~~

See [CONTRACTS.md](docs/guide/CONTRACTS.md) for the exact fields of each command. After compilation, you can also run `java -Xmx384m -cp Java/build Main` and send JSON Lines through standard input.

Local execution does not require API keys, an `.env` file or external service accounts.

## Reproducible demonstration

After running setup, create `Java/build/demo.jsonl` with the following contents. Each line is a separate request:

~~~json
{"task":"lcs","a":"tea_time","b":"team"}
{"task":"merge","sizes":[2,3,4]}
{"task":"tree","n":3,"weights":[4,9,6],"edges":[[0,1],[1,2]]}
{"task":"naive","text":"aaaaa","pattern":"aaa"}
{"task":"kmp","text":"abababa","pattern":"aba"}
{"task":"rk","text":"abdj","pattern":"ab"}
{"task":"suffix","text":"banana"}
{"task":"aho","patterns":["he","she","he"],"texts":["sheshe","sheshe",""]}
~~~

Run:

~~~sh
python tools/run.py --input Java/build/demo.jsonl
~~~

Expected results, allowing a different object-field order:

~~~json
{"length":4,"sequence":"team"}
{"cost":14,"plan":[[0,0,1],[0,1,2]]}
{"weight":10,"selected":[0,2]}
{"positions":[0,1,2]}
{"positions":[0,2,4]}
{"positions":[0]}
{"sa":[5,3,1,0,4,2],"lcp":[0,1,3,0,0,2],"repeated":"ana"}
{"counts":[[2,2,2],[2,2,2],[0,0,0]]}
~~~

This demonstration covers all eight commands, solution reconstruction, overlaps, an RK hash collision, duplicate patterns and independent texts. Other inputs may have several valid optimal reconstructions.

### Automated tests

~~~sh
python tools/run.py --check
python tests/student/check.py
~~~

Public checks cover 44 cases and repeat the complete set twice in one process: another 88 requests to check state reset.

The student suite performs **19,375 request checks** covering random and boundary cases, exhaustive small inputs, reconstructed solutions, input transformations and maximum published sizes. This count includes repeated requests that check independent state. A successful run ends with:

~~~text
PASS all student checks: 19375 requests
~~~

Independent oracles enumerate subsequences, merge orders and vertex subsets, use direct pattern matching, and sort suffix strings. Automated run history is available in [GitHub Actions](https://github.com/8zellorr8/DAA_asik4/actions).

### Search comparison

After compilation:

~~~sh
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
~~~

The benchmark gives naive, KMP and Rabin–Karp identical inputs at three increasing sizes. It includes random strings, repetitive near-misses, dense matches and deliberately constructed collisions. Correctness is checked before JVM warm-up and three measured runs per algorithm and case.

Recorded results are in [benchmark-results.md](tests/student/benchmark-results.md). The methodology and interpretation are in [ANALYSIS.md](ANALYSIS.md#search-comparison). Timings depend on hardware and runtime conditions; they do not prove asymptotic bounds.

## Data and integrations

- Runtime inputs are user-provided strings, block sizes, tree weights and edges in JSON Lines format.
- Contracts, public tests and the initial transport originate from the [instructor's template](https://github.com/RZhaniya/daa-assignment-4-archive/tree/4686cc60334aa189c2ac168e10a82d882b32858c).
- Additional test inputs are generated locally with fixed seeds documented in the tests and analysis.
- GitHub Actions runs checks on pushes, pull requests and manual workflow dispatch.
- The program does not download input from external APIs, make network requests or persist results in a database.

## Limitations

The interface is command-line only. A web UI, HTTP API, user accounts and persistent result storage are not implemented.

Algorithm inputs use printable ASCII codes 32–126 and are case-sensitive. Cyrillic and arbitrary Unicode are outside the published contract. Inputs are assumed to satisfy the specification; the JSON transport is not a general-purpose validator for arbitrary JSON documents.

| Command | Main input limits |
|---|---|
| `lcs` | Each string up to 1,000 characters; length product up to 1,000,000 |
| `merge` | Up to 200 blocks; each size from 0 to 1,000,000,000 |
| `tree` | Up to 100,000 vertices; weights from −1,000,000,000 to 1,000,000,000; edges must form a tree |
| `naive` | Text and pattern up to 3,000 characters each |
| `kmp` | Text up to 200,000; pattern up to 100,000 characters |
| `rk` | Each string up to 10,000 characters; length product up to 20,000,000 |
| `suffix` | Text up to 50,000 characters |
| `aho` | Up to 10,000 nonempty patterns, total length up to 100,000; up to 100 texts, total length up to 200,000 |

Rabin–Karp uses the assignment's base 31 and modulus 101, with character verification after equal hashes. Results are exact, but worst-case running time can be quadratic. Complete limits and output schemas are in [CONTRACTS.md](docs/guide/CONTRACTS.md).

Only the Java solution is included. The inherited launcher lists other languages, but their implementations are absent from this repository. Local compilation has a 60-second timeout; each request file or test batch has a 20-second timeout.

Passing public and student checks does not guarantee results on unavailable instructor tests.

## Deployment

The repository contains no deployed-application URL or deployment configuration. Run the solution locally using the instructions above. The [GitHub repository](https://github.com/8zellorr8/DAA_asik4) hosts the source; GitHub Actions runs tests.

## Assignment submission

`submission.json` selects `Java`; a student ID or variant is not required. Submit the exact GitHub commit URL containing the full SHA and the language **Java** in Moodle, then click **Submit assignment**. Choose a commit with a successful Public checks run.

[ANALYSIS.md](ANALYSIS.md) contains correctness arguments, complexity bounds, test descriptions, benchmark results, sources and the AI-assistance statement.
