import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class Main {
    private Main() {
    }

    static Object handle(Map<String, Object> input) {
        String task = (String) input.get("task");
        switch (task) {
            case "ping": return Map.of("status", "ready");
            case "lcs": return Algorithms.lcs(input);
            case "merge": return Algorithms.merge(input);
            case "tree": return Algorithms.tree(input);
            case "naive": return Algorithms.naive(input);
            case "kmp": return Algorithms.kmp(input);
            case "rk": return Algorithms.rk(input);
            case "suffix": return Algorithms.suffix(input);
            case "aho": return Algorithms.aho(input);
            default: throw new IllegalArgumentException("Unknown task: " + task);
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Object result;
                try {
                    Map<String, Object> input = (Map<String, Object>) Json.parse(line);
                    result = handle(input);
                } catch (RuntimeException error) {
                    String message = error.getMessage();
                    result = Map.of("error", message == null ? "Invalid request" : message);
                }
                writer.write(Json.write(result));
                writer.newLine();
                writer.flush();
            }
        } catch (IOException error) {
            System.err.println("Input/output error: " + error.getMessage());
            System.exit(1);
        }
    }
}
