import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

final class Json {
    private final String text;
    private int index;

    private Json(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        return new Json(text).readValue();
    }

    private void skipWhitespace() {
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
    }

    private Object readValue() {
        skipWhitespace();
        char current = text.charAt(index);
        if (current == '"') {
            return readString();
        }
        if (current == '{') {
            return readObject();
        }
        if (current == '[') {
            return readArray();
        }
        if (text.startsWith("null", index)) {
            index += 4;
            return null;
        }
        if (text.startsWith("true", index)) {
            index += 4;
            return true;
        }
        if (text.startsWith("false", index)) {
            index += 5;
            return false;
        }
        int start = index;
        if (current == '-') {
            index++;
        }
        while (index < text.length() && Character.isDigit(text.charAt(index))) {
            index++;
        }
        return Long.parseLong(text.substring(start, index));
    }

    private Map<String, Object> readObject() {
        index++;
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (text.charAt(index) == '}') {
            index++;
            return result;
        }
        while (true) {
            String key = readString();
            skipWhitespace();
            index++;
            result.put(key, readValue());
            skipWhitespace();
            if (text.charAt(index++) == '}') {
                return result;
            }
            skipWhitespace();
        }
    }

    private List<Object> readArray() {
        index++;
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (text.charAt(index) == ']') {
            index++;
            return result;
        }
        while (true) {
            result.add(readValue());
            skipWhitespace();
            if (text.charAt(index++) == ']') {
                return result;
            }
        }
    }

    private String readString() {
        skipWhitespace();
        index++;
        StringBuilder result = new StringBuilder();
        while (true) {
            char current = text.charAt(index++);
            if (current == '"') {
                return result.toString();
            }
            if (current == '\\') {
                current = text.charAt(index++);
                switch (current) {
                    case 'n': current = '\n'; break;
                    case 'r': current = '\r'; break;
                    case 't': current = '\t'; break;
                    case 'b': current = '\b'; break;
                    case 'f': current = '\f'; break;
                    case 'u':
                        current = (char) Integer.parseInt(text.substring(index, index + 4), 16);
                        index += 4;
                        break;
                    default: break;
                }
            }
            result.append(current);
        }
    }

    static String write(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return writeString((String) value);
        }
        if (value instanceof Map) {
            StringJoiner result = new StringJoiner(",", "{", "}");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                result.add(write(entry.getKey().toString()) + ":" + write(entry.getValue()));
            }
            return result.toString();
        }
        if (value instanceof Iterable) {
            StringJoiner result = new StringJoiner(",", "[", "]");
            for (Object item : (Iterable<?>) value) {
                result.add(write(item));
            }
            return result.toString();
        }
        if (value.getClass().isArray()) {
            StringJoiner result = new StringJoiner(",", "[", "]");
            for (int i = 0; i < Array.getLength(value); i++) {
                result.add(write(Array.get(value, i)));
            }
            return result.toString();
        }
        return value.toString();
    }

    private static String writeString(String text) {
        StringBuilder result = new StringBuilder("\"");
        for (char current : text.toCharArray()) {
            if (current == '"' || current == '\\') {
                result.append('\\').append(current);
            } else if (current < 32) {
                result.append(String.format("\\u%04x", (int) current));
            } else {
                result.append(current);
            }
        }
        return result.append('"').toString();
    }
}
