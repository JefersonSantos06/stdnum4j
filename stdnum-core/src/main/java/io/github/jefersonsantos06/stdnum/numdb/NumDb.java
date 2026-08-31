package io.github.jefersonsantos06.stdnum.numdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A hierarchical prefix database for numbers, answering "split this number
 * into its known parts and give me the properties of each part".
 *
 * <p>Data comes from indentation-structured text files. Each line holds one
 * or more prefix ranges plus optional {@code key="value"} properties; deeper
 * indentation nests ranges under the previous line:</p>
 *
 * <pre>
 * # comment
 * 0-2 zone="low"
 *  100-399 kind="a"
 *  200-299 extra="b"
 * 9 zone="nine"
 *  85 kind="x"
 * </pre>
 *
 * <p>{@link #info(String)} walks the number through the tree: at each level
 * the <em>shortest</em> matching range wins, all equally-short matches merge
 * their properties, and matching continues on their children with the rest
 * of the number. Trailing characters not covered by any range become a final
 * part with no properties.</p>
 *
 * <p>Instances are immutable; {@link #load(Class, String)} caches per
 * resource.</p>
 */
public final class NumDb {

    private record Node(int length, String low, String high,
                        Map<String, String> properties, List<Node> children) {
    }

    /** One part of a split number with the properties attached to it. */
    public record Entry(String part, Map<String, String> properties) {
    }

    private static final Pattern PROPERTY_PATTERN =
            Pattern.compile("([0-9A-Za-z_-]+)=\"([^\"]*)\"");

    private static final Map<String, NumDb> CACHE = new ConcurrentHashMap<>();

    private final List<Node> roots;

    private NumDb(List<Node> roots) {
        this.roots = roots;
    }

    /**
     * Loads (and caches) the database from a classpath resource resolved
     * against {@code anchor} — pass the class that ships with the data file.
     *
     * @throws IllegalArgumentException if the resource does not exist
     */
    public static NumDb load(Class<?> anchor, String resource) {
        String key = anchor.getName() + "|" + resource;
        return CACHE.computeIfAbsent(key, k -> {
            InputStream in = anchor.getResourceAsStream(resource);
            if (in == null) {
                throw new IllegalArgumentException(
                        "Resource not found: " + resource + " (anchor " + anchor.getName() + ")");
            }
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return parse(reader);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read " + resource, e);
            }
        });
    }

    private static NumDb parse(BufferedReader reader) throws IOException {
        List<Node> roots = new ArrayList<>();
        Map<Integer, List<Node>> stack = new HashMap<>();
        stack.put(0, roots);
        int lastIndent = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.stripLeading().startsWith("#")) {
                continue;
            }
            int indent = 0;
            while (indent < line.length() && line.charAt(indent) == ' ') {
                indent++;
            }
            String content = line.strip();
            int space = content.indexOf(' ');
            String rangesToken = space < 0 ? content : content.substring(0, space);
            Map<String, String> properties = space < 0
                    ? Map.of()
                    : parseProperties(content.substring(space + 1));

            if (indent > lastIndent) {
                List<Node> parentLevel = stack.get(lastIndent);
                Node parent = parentLevel.get(parentLevel.size() - 1);
                stack.put(indent, parent.children());
            }
            List<Node> level = stack.get(indent);
            if (level == null) {
                throw new IllegalStateException("Inconsistent indentation: " + line);
            }
            for (String item : rangesToken.split(",")) {
                int dash = item.indexOf('-');
                String low = dash < 0 ? item : item.substring(0, dash);
                String high = dash < 0 ? item : item.substring(dash + 1);
                level.add(new Node(low.length(), low, high, properties, new ArrayList<>()));
            }
            lastIndent = indent;
        }
        return new NumDb(roots);
    }

    private static Map<String, String> parseProperties(String text) {
        Map<String, String> properties = new LinkedHashMap<>();
        Matcher matcher = PROPERTY_PATTERN.matcher(text);
        while (matcher.find()) {
            properties.put(matcher.group(1), matcher.group(2));
        }
        return properties;
    }

    /**
     * Splits the number into parts and attaches the merged properties of the
     * ranges each part matched. Never empty for a non-empty number; the last
     * part carries no properties when the tail is not covered by the data.
     */
    public List<Entry> info(String number) {
        List<Entry> result = new ArrayList<>();
        find(number, roots, result);
        return Collections.unmodifiableList(result);
    }

    /** The parts of {@link #info(String)} without the properties. */
    public List<String> split(String number) {
        List<String> parts = new ArrayList<>();
        for (Entry entry : info(number)) {
            parts.add(entry.part());
        }
        return Collections.unmodifiableList(parts);
    }

    private static void find(String number, List<Node> nodes, List<Entry> result) {
        if (number.isEmpty()) {
            return;
        }
        int best = Integer.MAX_VALUE;
        for (Node node : nodes) {
            if (matches(node, number)) {
                best = Math.min(best, node.length());
            }
        }
        if (best == Integer.MAX_VALUE) {
            result.add(new Entry(number, Map.of()));
            return;
        }
        String part = number.substring(0, best);
        Map<String, String> properties = new LinkedHashMap<>();
        List<Node> next = new ArrayList<>();
        for (Node node : nodes) {
            if (node.length() == best && matches(node, number)) {
                properties.putAll(node.properties());
                next.addAll(node.children());
            }
        }
        result.add(new Entry(part, Collections.unmodifiableMap(properties)));
        find(number.substring(best), next, result);
    }

    private static boolean matches(Node node, String number) {
        if (node.length() > number.length()) {
            return false;
        }
        String prefix = number.substring(0, node.length());
        return node.low().compareTo(prefix) <= 0 && prefix.compareTo(node.high()) <= 0;
    }
}
