# Assignment 4 — analysis

## Environment and commands

Target: Java 17. Local runs: Oracle JDK 25.0.1, Python 3.14.3, Windows, Intel Core i5-11400H at 2.70 GHz, installed RAM 16 GiB, Java heap limit 384 MiB. CI uses Temurin 17, Python 3.12 and Ubuntu 24.04.

Run from the repository root with java and javac on PATH:

    python tools/run.py --setup
    python tools/run.py --check
    python tests/student/check.py
    javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
    java -Xmx384m -cp Java/build SearchBenchmark

To process a file: python tools/run.py --input requests.jsonl. The launcher compiles with --release 17. The local PowerShell session selected the JDK with:

    $env:PATH = 'C:/Program Files/Java/jdk-25/bin;' + $env:PATH

Each request has fresh algorithm state. Strings use printable ASCII, and long stores total costs, weights and counts. The bounds below exclude JSON parsing and serialization.

## Algorithms

### LCS

dp[i][j] is the optimum for prefixes of lengths i and j. Empty prefixes give zero. Equal final characters extend dp[i-1][j-1]; otherwise take max(dp[i-1][j], dp[i][j-1]). Increasing rows and columns ensure dependencies are ready.

For a mismatch, any common subsequence omits at least one final character. For a match, an optimum can use that character last. This proves the recurrence by induction. Backtracking follows the chosen optimum: append equal characters on diagonal moves, otherwise move toward the larger value. Ties prefer the previous row. Reverse the collected characters.

Time and auxiliary memory: O((a+1)(b+1)). Input: O(a+b+1); output: O(min(a,b)+1). Empty strings return length zero. The tea_time/team test catches confusion with substrings.

### Ordered merging

dp[l][r] is the minimum cost to merge original interval [l,r]; singleton cost is zero. Try every last split k:

    dp[l][k] + dp[k+1][r] + prefix[r+1] - prefix[l]

The last merge must join adjacent intervals. Both subplans must be optimal, or replacing one would reduce the total. Increasing interval lengths make these subplans available. Keep the first minimum split.

Emit the left subplan, right subplan, then [l,k,r]. Child blocks therefore exist before the parent merge. Empty and singleton inputs have no operations. Reconstruction depth is at most 200.

Time: O(n^3); auxiliary memory: O(n^2), including an O(n) stack. Input/output: O(n+1). Empty input takes O(1). Tests cover nonadjacent-greedy counterexample [1,100,1], zeros, valid chronology and 64-bit totals.

### Tree independent set

An iterative traversal records parents before children. Reverse that order for:

    take[v] = weight[v] + sum(skip[child])
    skip[v] = sum(max(take[child], skip[child]))

Child subtrees have no connecting edges. Selecting v excludes its children; skipping v permits either state. Induction from leaves proves the recurrence. Zero skip states permit an empty solution, even with negative weights.

Reconstruct in forward order: a selected parent forces exclusion; otherwise choose take only if it is strictly larger. Ties choose skip. Each vertex appears once, and the set is independent with the optimum total.

Primitive adjacency arrays give O(n+1) time and auxiliary memory. Input/output are O(n+1). No tree recursion is used. Tests include negative weights, ties and a 100,000-vertex chain.

### Naive search

Compare characters at every start 0 through n-m, inclusive. Report exactly those starts where all characters agree. Moving by one includes overlaps. An empty pattern matches all n+1 boundaries; an overlong pattern has no valid starts.

Time: O(n*m+n+1); auxiliary memory: O(1); input: O(n+m+1); output: O(z+1), where z is the number of matches. The aaaaa/aaa test requires [0,1,2].

### KMP

prefix[i] is the longest proper prefix that is also a suffix of the pattern prefix ending at i. Build it left to right; on a mismatch follow prefix[j-1] to the next possible border.

During scanning, matched is the pattern-prefix length equal to the current text suffix. The same fallback retains all possible matches without moving the text cursor backward. After a full match, report its start and fall back to prefix[m-1], preserving overlaps. Handle an empty pattern before indexing it.

Each extension increases matched by one; fallbacks decrease it, giving an amortized linear bound. Time: O(n+m+z+1); auxiliary memory: O(m+1); input: O(n+m+1); output: O(z+1). Repetitive near-misses and maximum-size empty-pattern output test fallback and boundaries.

### Exact Rabin–Karp

Use ASCII codes, base 31 and modulus 101, with the highest power on the left. With power = 31^(m-1) mod 101, remove the outgoing character contribution, normalize the remainder, multiply by 31 and add the incoming character. This preserves the polynomial window hash.

An unequal hash rejects a window. Every equal-hash candidate is checked character by character, so collisions cannot produce false matches. Handle empty and overlong patterns before building a window. Both ab and dj hash to 75; only ab is accepted.

For 1 <= m <= n, time is O(n+m+c*m), where c counts equal-hash windows. A bound covering all inputs is O(n*m+n+m+1). Auxiliary memory: O(1); input: O(n+m+1); output: O(z+1). Linear worst-case time is not claimed. Dense false collisions and true matches are tested.

### Suffix array and Kasai LCP

Start with character ranks. At each doubling round sort starts by (rank[i], rank[i+width]), using -1 past the end. Assign equal new ranks exactly to equal pairs. By induction, the pairs order prefixes of twice the previous length; stop when ranks are unique or the width covers the text.

Integer[] and Arrays.sort compare rank pairs in constant time. This gives O(n log²(n+1)) time and O(n+1) auxiliary memory, including sort workspace. No suffix strings are constructed and no empty suffix is returned.

Kasai uses inverse suffix positions to compare each start with its previous sorted suffix. When advancing one character, reuse at least the previous common length minus one; reset at rank zero. Total length increases are O(n), so LCP construction takes O(n) time. Set lcp[0] = 0.

Suffixes sharing a prefix occupy a contiguous sorted range. The largest adjacent LCP therefore gives a longest repeat. Retain the first strictly better length; ties and overlaps are valid. Input/output and auxiliary storage are O(n+1); empty input returns empty arrays and string. Tests cover all-equal, distinct and full-ASCII inputs.

### Aho–Corasick

Build one trie per request with 95 transitions per actual state. Duplicate patterns share terminals, while terminal IDs retain dictionary order. Breadth-first construction fills failure links and missing transitions using already processed shorter states.

A failure link represents the longest proper suffix that is a trie prefix. During scanning, the state tracks the longest matching dictionary prefix. Count state visits, then propagate them along failure links in reverse breadth-first order. Each suffix terminal receives exactly its occurrences, including overlaps, without enumerating every match.

Every text starts at the root with fresh visits. Empty texts return zeros; an empty dictionary returns an empty row per text; no texts returns no rows.

For total pattern length L, states S, p patterns and q texts, construction takes O(L+95*S+p) time and O(95*S+p) auxiliary space. Each text takes O(|text|+S+p). Input: O(L+sum(|text|)+p+q+1); output: O(q*(p+1)+1). Tests cover duplicates, suffix patterns, full ASCII, dense matches and depth 100,000.

## Tests and results

[check.py](tests/student/check.py) uses seed 4130. Independent small-input oracles enumerate subsequences, adjacent merge orders and vertex subsets, use startswith for occurrences and sort actual suffix strings. The public validator checks result schemas and reconstructed witnesses.

The suite runs 1,900 small cases forward and backward in one process (3,800 requests), 15,535 exhaustive and transformation cases, 38 scale cases, and two raw JSON transport checks: 19,375 requests total. ASCII cases cover all codes 32–126, quotes, backslashes, spaces, escaped slash and Unicode escapes representing ASCII. All passed locally, together with 44 public cases and the repeated 88-request public batch.

exhaustive_cases covers every pair of binary strings up to length four for LCS; all binary texts up to length six and patterns up to length four for each search; all binary strings up to length eight for suffix arrays; every block sequence of length 0–5 over sizes 0, 1, 2; every labeled tree of 1–4 vertices generated by Prüfer sequences with all weight assignments from -1, 0, 1; and all ordered dictionaries of 0–3 patterns from the six nonempty binary strings of length at most two. The dictionary enumeration includes duplicates and checks repeated independent texts.

transformation_cases uses seed 74130. Independently computed optima are checked after swapping/reversing LCS inputs, appending a common new character, reversing/scaling block sizes, relabeling tree vertices, reversing edge direction/order and scaling tree weights. Shuffled dictionaries must retain counts in their new column order. Additional scale cases cover negative/zero-weight chains, one large block surrounded by zeros, long periodic suffixes, a match only at the final valid start, dense false RK collisions and deep Aho failure transitions with no matches.

| Command | Fixture in check.py | Expected result | Actual | Checks |
|---|---|---|---|---|
| lcs | small_cases, ascii_cases, scale_cases | exhaustive optimum; scale length 999 | PASS | empty, ties, subsequence reconstruction |
| merge | small_cases, scale_cases | exhaustive cost; scale cost 1,544,000,000,000 | PASS | adjacency, chronology, overflow |
| tree | small_cases, scale_cases | exhaustive optimum; chain weight 50,000,000,000,000 | PASS | independent witness, depth, negatives |
| naive | search-oracle, ascii-search, search-scale | exact oracle starts | PASS | overlaps, full ASCII, boundaries |
| kmp | search-oracle, ascii-search, search-scale | exact starts; 100,001 scale matches | PASS | fallback, reset |
| rk | dense-hash-collisions, search-oracle | no false matches; exact starts | PASS | collisions, normalized remainder |
| suffix | suffix-direct-sort, ascii-suffix, scale_cases | full SA/LCP; equal-input repeat length 49,999 | PASS | ordering, Kasai, overlaps |
| aho | aho-direct-counts, ascii-aho, scale_cases | direct counts; dense duplicate count 1,991 | PASS | suffixes, duplicates, reset, scale |

Scale expectations use known constructions. For large random suffix input, sorting 64-character prefixes is valid because their uniqueness is asserted; adjacent LCP is then calculated directly. Passing these checks does not guarantee unpublished cases.

## Search comparison

[SearchBenchmark.java](tests/student/SearchBenchmark.java) records three runs and medians in [benchmark-results.md](tests/student/benchmark-results.md). All algorithms receive identical fixtures at n = 500, 1500, 3000 and m = n/10:

- random: Java Random(4130+n), uniform lowercase text; pattern is the substring at n/2.
- repetitive: text a^n, pattern a^(m-1)b; no matches.
- dense: text a^n, pattern a^m; every window matches.
- collisions: text a^n, pattern a^(m-2)bB; every window has the pattern hash but none matches.

The collision construction follows 31*97+97 = 31*98+66: aa and bB have equal polynomial hashes. Direct candidate counting verifies every dense/collision window before timing. Deterministic cases do not use the seed.

An independent startswith oracle verifies all outputs first. Warm-up runs all fixtures and algorithms for 1,000 rounds in one JVM. Each of three measurements averages 100 calls; algorithm order rotates. System.nanoTime measures preprocessing, searching, result construction and assignment of the result to a volatile Object. Compilation, fixture generation, oracle and candidate checks, JSON and printing are excluded equally. Times are microseconds per call; garbage collection is not forced.

At n=3000, m=300, observed medians were:

| Case | Naive (us) | KMP (us) | RK (us) | Hash candidates | Matches |
|---|---:|---:|---:|---:|---:|
| random | 8.555 | 8.598 | 23.250 | 25 | 1 |
| repetitive | 1516.641 | 15.368 | 21.705 | 0 | 0 |
| dense | 1555.158 | 19.104 | 3025.529 | 2701 | 2701 |
| collisions | 1523.310 | 15.748 | 3057.738 | 2701 | 0 |

Random mismatches let naive stop early. Repetitive near-misses repeat long comparisons, while KMP reuses borders. RK rejects near-miss windows cheaply when their hashes differ, but checks almost the whole pattern for dense matches and the constructed collisions. The small modulus increases collision risk; even a stronger hash still requires exact verification. These observations illustrate the candidate-sensitive bound, not a proof of asymptotic complexity or a production-hashing comparison.

## Sources and AI assistance

The contracts, required algorithms, complexity targets, examples and rubric come from the [Assignment 4 template](https://github.com/RZhaniya/daa-assignment-4-archive/tree/4686cc60334aa189c2ac168e10a82d882b32858c), base commit `4686cc60334aa189c2ac168e10a82d882b32858c`, and the accompanying [assignment specification](https://github.com/RZhaniya/daa-assignment-4-archive/blob/4686cc60334aa189c2ac168e10a82d882b32858c/docs/Assignment_4_EN.md). The JSON transport, public cases, checker and launcher originate from that template. The JSON code was reformatted for readability and the CI workflow was narrowed to Java. The supplied launcher, public cases and semantic validation logic remain unchanged. No third-party algorithm library is used.

OpenAI Codex helped explain the theory of dynamic programming and string-processing algorithms. This included reasoning about correctness and complexity, checking hypotheses about hash collisions and boundary cases, creating test cases, investigating possible bugs, and running tests and benchmarks.

OpenAI Codex also created additional tests for the Java solution. These include exhaustive small inputs, reproducible random cases, all printable ASCII characters, input transformations, 100,000-vertex chains and stars, 50,000-character suffix-array cases, dense false hash collisions, and Aho–Corasick dictionaries at the published limits.

Verification used the instructor's public cases, independently computed reference results, validation of reconstructed solutions, repeated mixed requests and full-limit inputs. The benchmark checked exact outputs before measuring random, repetitive, dense-match and collision cases. The results and recorded files describe checks that were actually executed against the submitted implementation.

