import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public final class SearchBenchmark {
    private static volatile Object sink;
    private static final int REPETITIONS = 100;
    private static final long SEED = 4130;

    private static Map<String, Object> search(int algorithm, Map<String, Object> input) {
        switch (algorithm) {
            case 0: return Algorithms.naive(input);
            case 1: return Algorithms.kmp(input);
            default: return Algorithms.rk(input);
        }
    }

    private static List<Integer> oracle(String text, String pattern) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i <= text.length(); i++) {
            if (text.startsWith(pattern, i)) {
                result.add(i);
            }
        }
        return result;
    }

    private static String randomText(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append((char) ('a' + random.nextInt(26)));
        }
        return result.toString();
    }

    private static void verify(Map<String, Object> input) {
        List<Integer> expected = oracle((String) input.get("text"), (String) input.get("pattern"));
        for (int algorithm = 0; algorithm < 3; algorithm++) {
            if (!search(algorithm, input).get("positions").equals(expected)) {
                throw new AssertionError("Search result differs from independent oracle");
            }
        }
    }

    private static int hash(String text, int start, int length) {
        int result = 0;
        for (int i = 0; i < length; i++) {
            result = (result * 31 + text.charAt(start + i)) % 101;
        }
        return result;
    }

    private static int candidates(String text, String pattern) {
        int target = hash(pattern, 0, pattern.length());
        int count = 0;
        for (int i = 0; i <= text.length() - pattern.length(); i++) {
            if (hash(text, i, pattern.length()) == target) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        List<Map<String, Object>> inputs = new ArrayList<>();
        for (String kind : List.of("random", "repetitive", "dense", "collisions")) {
            for (int n : new int[]{500, 1500, 3000}) {
                int m = n / 10;
                Random random = new Random(SEED + n);
                String text = kind.equals("random") ? randomText(random, n) : "a".repeat(n);
                String pattern;
                switch (kind) {
                    case "random": pattern = text.substring(n / 2, n / 2 + m); break;
                    case "dense": pattern = "a".repeat(m); break;
                    case "collisions": pattern = "a".repeat(m - 2) + "bB"; break;
                    default: pattern = "a".repeat(m - 1) + "b"; break;
                }
                int candidateCount = candidates(text, pattern);
                if ((kind.equals("dense") || kind.equals("collisions")) && candidateCount != n - m + 1) {
                    throw new AssertionError("Expected every window to have the pattern hash");
                }
                Map<String, Object> input = Map.of("text", text, "pattern", pattern, "kind", kind);
                verify(input);
                inputs.add(input);
            }
        }
        verify(Map.of("text", "abdj", "pattern", "ab"));
        for (int round = 0; round < 1000; round++) {
            for (Map<String, Object> input : inputs) {
                for (int algorithm = 0; algorithm < 3; algorithm++) {
                    sink = search(algorithm, input);
                }
            }
        }
        System.out.println("| Case | n | m | Seed | Algorithm | Run 1 (us) | Run 2 (us) | Run 3 (us) | Median (us) | RK candidates | Matches | Correct |");
        System.out.println("|---|---:|---:|---:|---|---:|---:|---:|---:|---:|---:|---|");
        String[] names = {"naive", "KMP", "RK"};
        for (Map<String, Object> input : inputs) {
            String text = (String) input.get("text");
            String pattern = (String) input.get("pattern");
            int candidateCount = candidates(text, pattern);
            int matches = oracle(text, pattern).size();
            double[][] times = new double[3][3];
            for (int run = 0; run < 3; run++) {
                for (int offset = 0; offset < 3; offset++) {
                    int algorithm = (run + offset) % 3;
                    long start = System.nanoTime();
                    for (int repetition = 0; repetition < REPETITIONS; repetition++) {
                        sink = search(algorithm, input);
                    }
                    times[algorithm][run] = (System.nanoTime() - start) / (1000.0 * REPETITIONS);
                }
            }
            for (int algorithm = 0; algorithm < 3; algorithm++) {
                double[] sorted = times[algorithm].clone();
                Arrays.sort(sorted);
                System.out.printf(Locale.ROOT, "| %s | %d | %d | %d | %s | %.3f | %.3f | %.3f | %.3f | %d | %d | yes |%n",
                        input.get("kind"), ((String) input.get("text")).length(),
                        ((String) input.get("pattern")).length(), SEED + ((String) input.get("text")).length(),
                        names[algorithm], times[algorithm][0], times[algorithm][1], times[algorithm][2], sorted[1],
                        candidateCount, matches);
            }
        }
    }
}
