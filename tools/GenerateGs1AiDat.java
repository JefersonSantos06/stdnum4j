import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the gs1-ai.dat registry consumed by the Gs1128 class.
 *
 * <p>Source: the GS1 application identifier vocabulary, which the reference
 * page carries as a JSON-LD block. Each identifier gives its format, whether
 * a separator is needed after it, its short name and its description.</p>
 *
 * <p>Consecutive identifiers that agree on everything but their own number
 * are written as one range, which is how the decimal-place identifiers such
 * as 3100 to 3105 are held.</p>
 *
 * <p>The block is walked by counting braces rather than with a JSON library,
 * the generators here having no dependencies. Only the five scalar fields
 * that matter are read; the nested parts are stepped over.</p>
 */
public final class GenerateGs1AiDat implements Source {

    @Override
    public String id() {
        return "gs1-ai";
    }

    @Override
    public String title() {
        return "Regenerate gs1-ai.dat, the GS1 application identifiers";
    }

    @Override
    public String output() {
        return "stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/gs1-ai.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://ref.gs1.org/ai/",
                "ai.html"));
    }

    /** One application identifier, as the file records it. */
    private record Ai(String ai, String format, boolean fnc1, String name, String description) {
    }

    private static final Pattern SCRIPT = Pattern.compile(
            "<script[^>]*type=\"application/ld\\+json\"[^>]*>(.*?)</script>", Pattern.DOTALL);
    /** A format that names a date, if the description agrees it is one. */
    private static final Pattern DATE_FORMAT =
            Pattern.compile("(N[68]\\[?\\+)?N[0-9]*[.]*[0-9]+]?");
    /** A format that names a count, if the description agrees it is one. */
    private static final Pattern COUNT_FORMAT = Pattern.compile("N[.]*[0-9]+");
    private static final Pattern PART = Pattern.compile("[NXY][0-9]*?[.]*([0-9]+)[\\[\\]]?");

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        String html = Files.readString(run.file("ai.html"), StandardCharsets.UTF_8);
        Matcher script = SCRIPT.matcher(html);
        if (!script.find()) {
            throw new IllegalStateException("no JSON-LD block: the page layout has changed");
        }
        String json = script.group(1);
        int at = json.indexOf("\"applicationIdentifiers\"");
        if (at < 0) {
            throw new IllegalStateException("no application identifiers: the vocabulary has changed");
        }

        List<Ai> identifiers = new ArrayList<>();
        for (String object : objects(json, json.indexOf('[', at))) {
            String ai = string(object, "applicationIdentifier");
            if (ai == null) {
                continue;
            }
            String format = string(object, "formatString");
            // the leading run that repeats the identifier itself is dropped
            String own = "N" + ai.length() + "+";
            if (format != null && format.startsWith(own)) {
                format = format.substring(own.length());
            }
            identifiers.add(new Ai(ai, format,
                    Boolean.parseBoolean(scalar(object, "separatorRequired")),
                    string(object, "title"), string(object, "description")));
        }
        if (identifiers.isEmpty()) {
            throw new IllegalStateException("no application identifiers: the vocabulary has changed");
        }
        identifiers.sort((a, b) -> a.ai().compareTo(b.ai()));

        out.println("# GS1 application identifiers: the format, name and description of each,");
        out.println("# and whether a separator is required after it.");
        out.println("# Generated from the vocabulary published at https://ref.gs1.org/ai/");
        Ai first = null;
        Ai previous = null;
        for (Ai identifier : identifiers) {
            if (previous == null || !sameBut(identifier, previous)) {
                if (first != null) {
                    print(out, first, previous);
                }
                first = identifier;
            }
            previous = identifier;
        }
        if (first != null) {
            print(out, first, previous);
        }
    }

    /** Whether two identifiers agree on everything except their own number. */
    private static boolean sameBut(Ai a, Ai b) {
        return java.util.Objects.equals(a.format(), b.format())
                && a.fnc1() == b.fnc1()
                && java.util.Objects.equals(a.name(), b.name())
                && java.util.Objects.equals(a.description(), b.description());
    }

    private static void print(PrintStream out, Ai first, Ai last) {
        String key = first.ai().equals(last.ai()) ? first.ai() : first.ai() + "-" + last.ai();
        out.println(key
                + " format=\"" + first.format() + "\""
                + " type=\"" + type(first, last) + "\""
                + (first.fnc1() ? " fnc1=\"1\"" : "")
                + " name=\"" + escape(first.name()) + "\""
                + " description=\"" + escape(first.description()) + "\"");
    }

    /** What the value behind an identifier means, as far as the format says. */
    private static String type(Ai first, Ai last) {
        String description = first.description() == null
                ? "" : first.description().toLowerCase(java.util.Locale.ROOT);
        if (!first.ai().equals(last.ai()) && first.ai().length() == 4) {
            // a range of four-digit identifiers is the decimal place count
            return "decimal";
        }
        if (DATE_FORMAT.matcher(first.format()).matches() && description.contains("date")) {
            return "date";
        }
        if (COUNT_FORMAT.matcher(first.format()).matches() && description.contains("count")) {
            return "int";
        }
        return "str";
    }

    /** The top-level objects of the array starting at {@code from}. */
    private static List<String> objects(String json, int from) {
        List<String> objects = new ArrayList<>();
        if (from < 0) {
            return objects;
        }
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = from; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"' -> inString = true;
                case '{' -> {
                    if (depth++ == 0) {
                        start = i;
                    }
                }
                case '}' -> {
                    if (--depth == 0 && start >= 0) {
                        objects.add(json.substring(start, i + 1));
                        start = -1;
                    }
                }
                case ']' -> {
                    if (depth == 0) {
                        return objects;
                    }
                }
                default -> { }
            }
        }
        return objects;
    }

    private static String string(String object, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
                .matcher(object);
        return m.find() ? unescape(m.group(1)) : null;
    }

    private static String scalar(String object, String field) {
        Matcher m = Pattern.compile("\"" + field + "\"\\s*:\\s*([a-z0-9.]+)").matcher(object);
        return m.find() ? m.group(1) : "";
    }

    private static String unescape(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != '\\' || i + 1 >= text.length()) {
                sb.append(c);
                continue;
            }
            char next = text.charAt(++i);
            switch (next) {
                case 'n', 'r', 't' -> sb.append(' ');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(text.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> sb.append(next);
            }
        }
        return sb.toString();
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\"", "").replaceAll("\\s+", " ").strip();
    }

    /** The widest value a format can hold, for the record. */
    static int maxLength(String format) {
        int total = 0;
        for (String part : format.split("\\+")) {
            Matcher m = PART.matcher(part);
            if (m.matches()) {
                total += Integer.parseInt(m.group(1));
            }
        }
        return total;
    }
}
