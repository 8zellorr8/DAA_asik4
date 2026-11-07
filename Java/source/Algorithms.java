import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Algorithms {
    private Algorithms() {
    }

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a");
        String b = (String) input.get("b");
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        StringBuilder sequence = new StringBuilder();
        int i = a.length();
        int j = b.length();
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                sequence.append(a.charAt(i - 1));
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return Map.of("length", dp[a.length()][b.length()], "sequence", sequence.reverse().toString());
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();
        List<int[]> plan = new ArrayList<>();
        if (n < 2) {
            return Map.of("cost", 0L, "plan", plan);
        }
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) {
            prefix[i + 1] = prefix[i] + ((Number) sizes.get(i)).longValue();
        }
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int length = 2; length <= n; length++) {
            for (int left = 0; left + length <= n; left++) {
                int right = left + length - 1;
                dp[left][right] = Long.MAX_VALUE;
                long sum = prefix[right + 1] - prefix[left];
                for (int middle = left; middle < right; middle++) {
                    long cost = dp[left][middle] + dp[middle + 1][right] + sum;
                    if (cost < dp[left][right]) {
                        dp[left][right] = cost;
                        split[left][right] = middle;
                    }
                }
            }
        }
        buildPlan(0, n - 1, split, plan);
        return Map.of("cost", dp[0][n - 1], "plan", plan);
    }

    private static void buildPlan(int left, int right, int[][] split, List<int[]> plan) {
        if (left == right) {
            return;
        }
        int middle = split[left][right];
        buildPlan(left, middle, split, plan);
        buildPlan(middle + 1, right, split, plan);
        plan.add(new int[]{left, middle, right});
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<Integer> selected = new ArrayList<>();
        if (n == 0) {
            return Map.of("weight", 0L, "selected", selected);
        }
        List<?> weights = (List<?>) input.get("weights");
        List<?> edges = (List<?>) input.get("edges");
        int[] degree = new int[n];
        for (Object item : edges) {
            List<?> edge = (List<?>) item;
            degree[((Number) edge.get(0)).intValue()]++;
            degree[((Number) edge.get(1)).intValue()]++;
        }
        int[][] graph = new int[n][];
        for (int v = 0; v < n; v++) {
            graph[v] = new int[degree[v]];
        }
        Arrays.fill(degree, 0);
        for (Object item : edges) {
            List<?> edge = (List<?>) item;
            int u = ((Number) edge.get(0)).intValue();
            int v = ((Number) edge.get(1)).intValue();
            graph[u][degree[u]++] = v;
            graph[v][degree[v]++] = u;
        }
        int[] parent = new int[n];
        int[] order = new int[n];
        parent[0] = -1;
        int size = 1;
        for (int i = 0; i < size; i++) {
            int v = order[i];
            for (int child : graph[v]) {
                if (child != parent[v]) {
                    parent[child] = v;
                    order[size++] = child;
                }
            }
        }
        long[] take = new long[n];
        long[] skip = new long[n];
        for (int i = n - 1; i >= 0; i--) {
            int v = order[i];
            take[v] = ((Number) weights.get(v)).longValue();
            for (int child : graph[v]) {
                if (parent[child] == v) {
                    take[v] += skip[child];
                    skip[v] += Math.max(take[child], skip[child]);
                }
            }
        }
        boolean[] chosen = new boolean[n];
        for (int v : order) {
            if ((parent[v] == -1 || !chosen[parent[v]]) && take[v] > skip[v]) {
                chosen[v] = true;
                selected.add(v);
            }
        }
        return Map.of("weight", Math.max(take[0], skip[0]), "selected", selected);
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        List<Integer> positions = new ArrayList<>();
        for (int start = 0; start <= text.length() - pattern.length(); start++) {
            if (matchesAt(text, pattern, start)) {
                positions.add(start);
            }
        }
        return Map.of("positions", positions);
    }

    private static boolean matchesAt(String text, String pattern, int start) {
        for (int j = 0; j < pattern.length(); j++) {
            if (text.charAt(start + j) != pattern.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    private static List<Integer> boundaries(int length) {
        List<Integer> positions = new ArrayList<>(length + 1);
        for (int i = 0; i <= length; i++) {
            positions.add(i);
        }
        return positions;
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int m = pattern.length();
        if (m == 0) {
            return Map.of("positions", boundaries(text.length()));
        }
        int[] prefix = new int[m];
        for (int i = 1; i < m; i++) {
            int j = prefix[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) {
                j = prefix[j - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(j)) {
                j++;
            }
            prefix[i] = j;
        }
        List<Integer> positions = new ArrayList<>();
        int matched = 0;
        for (int i = 0; i < text.length(); i++) {
            while (matched > 0 && text.charAt(i) != pattern.charAt(matched)) {
                matched = prefix[matched - 1];
            }
            if (text.charAt(i) == pattern.charAt(matched)) {
                matched++;
            }
            if (matched == m) {
                positions.add(i - m + 1);
                matched = prefix[matched - 1];
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text");
        String pattern = (String) input.get("pattern");
        int n = text.length();
        int m = pattern.length();
        if (m == 0) {
            return Map.of("positions", boundaries(n));
        }
        List<Integer> positions = new ArrayList<>();
        if (m > n) {
            return Map.of("positions", positions);
        }
        long power = 1;
        long patternHash = 0;
        long windowHash = 0;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * 31 + pattern.charAt(i)) % 101;
            windowHash = (windowHash * 31 + text.charAt(i)) % 101;
            if (i < m - 1) {
                power = power * 31 % 101;
            }
        }
        for (int start = 0; start <= n - m; start++) {
            if (windowHash == patternHash && matchesAt(text, pattern, start)) {
                positions.add(start);
            }
            if (start < n - m) {
                windowHash = (windowHash - text.charAt(start) * power % 101 + 101) % 101;
                windowHash = (windowHash * 31 + text.charAt(start + m)) % 101;
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();
        Integer[] sa = new Integer[n];
        int[] ranks = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = i;
            ranks[i] = text.charAt(i);
        }
        for (int width = 1; width < n; width *= 2) {
            final int step = width;
            final int[] current = ranks;
            Arrays.sort(sa, (a, b) -> {
                int first = Integer.compare(current[a], current[b]);
                if (first != 0) {
                    return first;
                }
                int secondA = a + step < n ? current[a + step] : -1;
                int secondB = b + step < n ? current[b + step] : -1;
                return Integer.compare(secondA, secondB);
            });
            int[] next = new int[n];
            for (int i = 1; i < n; i++) {
                int a = sa[i - 1];
                int b = sa[i];
                int secondA = a + width < n ? ranks[a + width] : -1;
                int secondB = b + width < n ? ranks[b + width] : -1;
                boolean different = ranks[a] != ranks[b] || secondA != secondB;
                next[b] = next[a] + (different ? 1 : 0);
            }
            ranks = next;
            if (ranks[sa[n - 1]] == n - 1) {
                break;
            }
        }
        int[] position = new int[n];
        for (int i = 0; i < n; i++) {
            position[sa[i]] = i;
        }
        int[] lcp = new int[n];
        int common = 0;
        int bestLength = 0;
        int bestStart = 0;
        for (int i = 0; i < n; i++) {
            int rank = position[i];
            if (rank == 0) {
                common = 0;
                continue;
            }
            int j = sa[rank - 1];
            while (i + common < n && j + common < n
                    && text.charAt(i + common) == text.charAt(j + common)) {
                common++;
            }
            lcp[rank] = common;
            if (common > bestLength) {
                bestLength = common;
                bestStart = i;
            }
            if (common > 0) {
                common--;
            }
        }
        return Map.of("sa", sa, "lcp", lcp, "repeated", text.substring(bestStart, bestStart + bestLength));
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = (List<?>) input.get("patterns");
        List<?> texts = (List<?>) input.get("texts");
        List<int[]> transitions = new ArrayList<>();
        transitions.add(new int[95]);
        int[] terminal = new int[patterns.size()];
        int states = 1;
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = (String) patterns.get(i);
            int state = 0;
            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;
                if (transitions.get(state)[c] == 0) {
                    transitions.get(state)[c] = states++;
                    transitions.add(new int[95]);
                }
                state = transitions.get(state)[c];
            }
            terminal[i] = state;
        }
        int[][] next = transitions.toArray(new int[0][]);
        int[] failure = new int[states];
        int[] order = new int[states];
        int head = 0;
        int tail = 0;
        for (int c = 0; c < 95; c++) {
            if (next[0][c] != 0) {
                order[tail++] = next[0][c];
            }
        }
        while (head < tail) {
            int state = order[head++];
            for (int c = 0; c < 95; c++) {
                int child = next[state][c];
                if (child != 0) {
                    failure[child] = next[failure[state]][c];
                    order[tail++] = child;
                } else {
                    next[state][c] = next[failure[state]][c];
                }
            }
        }
        List<long[]> counts = new ArrayList<>();
        for (Object item : texts) {
            String text = (String) item;
            long[] visits = new long[states];
            int state = 0;
            for (int i = 0; i < text.length(); i++) {
                state = next[state][text.charAt(i) - 32];
                visits[state]++;
            }
            for (int i = tail - 1; i >= 0; i--) {
                int v = order[i];
                visits[failure[v]] += visits[v];
            }
            long[] row = new long[patterns.size()];
            for (int i = 0; i < patterns.size(); i++) {
                row[i] = visits[terminal[i]];
            }
            counts.add(row);
        }
        return Map.of("counts", counts);
    }
}
