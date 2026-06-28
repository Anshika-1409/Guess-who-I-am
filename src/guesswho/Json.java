package guesswho;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Json {
    private Json() {}

    static Map<String, Object> parseObject(String json) {
        Object value = new Parser(json).parse();

        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("JSON body must be an object.");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, item) -> result.put(String.valueOf(key), item));
        return result;
    }

    static String stringify(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String text) {
            return "\"" + escape(text) + "\"";
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                .map(entry -> stringify(String.valueOf(entry.getKey())) + ":" + stringify(entry.getValue()))
                .reduce((left, right) -> left + "," + right)
                .map(items -> "{" + items + "}")
                .orElse("{}");
        }

        if (value instanceof Iterable<?> iterable) {
            List<String> items = new ArrayList<>();
            iterable.forEach(item -> items.add(stringify(item)));
            return "[" + String.join(",", items) + "]";
        }

        throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
    }

    private static String escape(String text) {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String input;
        private int index;

        private Parser(String input) {
            this.input = input == null ? "" : input.trim();
        }

        private Object parse() {
            Object value = parseValue();
            skipWhitespace();

            if (index != input.length()) {
                throw error("Unexpected trailing content.");
            }

            return value;
        }

        private Object parseValue() {
            skipWhitespace();

            if (index >= input.length()) {
                throw error("Unexpected end of JSON.");
            }

            return switch (input.charAt(index)) {
                case '{' -> parseObjectValue();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", true);
                case 'f' -> parseLiteral("false", false);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObjectValue() {
            Map<String, Object> object = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();

            if (peek('}')) {
                expect('}');
                return object;
            }

            while (true) {
                String key = parseString();
                skipWhitespace();
                expect(':');
                object.put(key, parseValue());
                skipWhitespace();

                if (peek('}')) {
                    expect('}');
                    return object;
                }

                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            expect('[');
            skipWhitespace();

            if (peek(']')) {
                expect(']');
                return array;
            }

            while (true) {
                array.add(parseValue());
                skipWhitespace();

                if (peek(']')) {
                    expect(']');
                    return array;
                }

                expect(',');
            }
        }

        private String parseString() {
            skipWhitespace();
            expect('"');
            StringBuilder result = new StringBuilder();

            while (index < input.length()) {
                char current = input.charAt(index++);

                if (current == '"') {
                    return result.toString();
                }

                if (current == '\\') {
                    if (index >= input.length()) {
                        throw error("Invalid string escape.");
                    }

                    char escaped = input.charAt(index++);
                    result.append(switch (escaped) {
                        case '"', '\\', '/' -> escaped;
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'u' -> parseUnicode();
                        default -> throw error("Invalid string escape.");
                    });
                } else {
                    result.append(current);
                }
            }

            throw error("Unterminated string.");
        }

        private char parseUnicode() {
            if (index + 4 > input.length()) {
                throw error("Invalid unicode escape.");
            }

            String hex = input.substring(index, index + 4);
            index += 4;

            try {
                return (char) Integer.parseInt(hex, 16);
            } catch (NumberFormatException error) {
                throw error("Invalid unicode escape.");
            }
        }

        private Object parseNumber() {
            int start = index;

            while (index < input.length() && "-+.0123456789eE".indexOf(input.charAt(index)) >= 0) {
                index++;
            }

            if (start == index) {
                throw error("Expected a JSON value.");
            }

            String number = input.substring(start, index);

            try {
                return number.contains(".") || number.contains("e") || number.contains("E")
                    ? Double.parseDouble(number)
                    : Long.parseLong(number);
            } catch (NumberFormatException error) {
                throw error("Invalid number.");
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!input.startsWith(literal, index)) {
                throw error("Invalid JSON literal.");
            }

            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }

        private boolean peek(char expected) {
            skipWhitespace();
            return index < input.length() && input.charAt(index) == expected;
        }

        private void expect(char expected) {
            skipWhitespace();

            if (index >= input.length() || input.charAt(index) != expected) {
                throw error("Expected '" + expected + "'.");
            }

            index++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " At position " + index + ".");
        }
    }
}
