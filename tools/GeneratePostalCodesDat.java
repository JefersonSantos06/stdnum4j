import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Generates the postal-codes.dat file consumed by the PostalCode class.
 *
 * <p>Source: Google's address metadata, the data behind libaddressinput,
 * which documents for every country and territory the regex its postal code
 * matches ({@code zip}), examples ({@code zipex}) and a display prefix
 * ({@code postprefix}). The data is licensed CC BY 4.0. Fetch the root, which
 * lists the regions, then one file per region, and pass them all with the
 * date they were retrieved.</p>
 *
 * <p>The source's regex describes the code as written, separators and all;
 * the library matches the compact form, so the regex is rewritten here to
 * describe that instead: a literal space or hyphen goes, with the {@code ?}
 * that made it optional; a class holding only separators goes the same way;
 * an optional literal prefix such as {@code (?:PC )?} is lifted out into the
 * prefixes compact() may strip. The way a code is written is then recovered
 * from the examples, each turned into a mask where a digit is {@code 9} and
 * a letter {@code A}. Every example is run through the result before it is
 * written: it must match, and it must format back to itself.</p>
 */
public final class GeneratePostalCodesDat implements Source {

    @Override
    public String id() {
        return "postal-codes";
    }

    @Override
    public String title() {
        return "Regenerate postal-codes.dat, the postal code shapes";
    }

    @Override
    public String output() {
        return "stdnum4j-postal/src/main/resources/io/github/jefersonsantos06"
                + "/stdnum/postal/postal-codes.dat";
    }

    @Override
    public List<Download> seeds() {
        // the index says which regions exist, and there are about 250
        return List.of(new Download(SOURCE, "data.json"));
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        String countries = first(COUNTRIES, seeds.get("data.json"));
        if (countries == null) {
            throw new IllegalStateException(
                    "no countries in the index: the service layout has changed");
        }
        List<Download> all = new ArrayList<>(seeds());
        for (String code : countries.split("~")) {
            if (code.matches("[A-Z]{2}")) {
                all.add(new Download(SOURCE + "/" + code, code + ".json"));
            }
        }
        return all;
    }

    @Override
    public List<String> volatileLines() {
        // the service publishes no version, so the header records when it
        // was read; that alone is not a change
        return List.of("^# .*, retrieved [0-9]{4}-[0-9]{2}-[0-9]{2}\\.$");
    }

    @Override
    public List<String> redFlags() {
        return List.of(
                "stdnum4j-all/src/test/java/io/github/jefersonsantos06/stdnum/all"
                        + "/AllRegisteredContractTest.java (the registered total)",
                "stdnum4j-postal/src/test/java/io/github/jefersonsantos06/stdnum/postal"
                        + "/PostalCodeExamplesTest.java (the region count)",
                "README.md, docs/NUMBERS.md, docs/ARCHITECTURE.md, docs/CONTRIBUTING.md"
                        + " and docs/TESTING.md (the counts in prose)");
    }

    private static final String SOURCE = "https://chromium-i18n.appspot.com/ssl-address/data";

    /** A string field of the flat JSON the service returns. */
    private static Pattern field(String name) {
        return Pattern.compile("\"" + name + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    }

    private static final Pattern KEY = field("key");
    private static final Pattern NAME = field("name");
    private static final Pattern ZIP = field("zip");
    private static final Pattern ZIPEX = field("zipex");
    private static final Pattern POSTPREFIX = field("postprefix");
    private static final Pattern KIND = field("zip_name_type");
    private static final Pattern COUNTRIES = field("countries");

    /** An optional literal prefix at the head of the regex: {@code (?:PC )?}. */
    private static final Pattern LIFTABLE = Pattern.compile("^\\(\\?:([A-Z]+)[ -]?\\)\\?");

    private record Region(String key, String name, String zip, String zipex,
                          String postprefix, String kind) {
    }

    /** What refused its own checks, so the message can say which. */
    private static final List<String> problems = new ArrayList<>();

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        String retrieved = run.retrievedOn().toString();
        Map<String, Region> regions = new TreeMap<>();
        for (Path source : run.files()) {
            String json = Files.readString(source, StandardCharsets.UTF_8);
            String key = first(KEY, json);
            if (key == null || !key.matches("[A-Z]{2}")) {
                continue;   // the index itself, which carries no region
            }
            regions.put(key, new Region(key, first(NAME, json), first(ZIP, json),
                    first(ZIPEX, json), first(POSTPREFIX, json), first(KIND, json)));
        }

        out.println("# Postal code shapes of every country and territory: the pattern a code");
        out.println("# matches once its separators are gone, the way it is written, and the");
        out.println("# examples its issuer publishes.");
        out.println("# Generated from Google's address metadata (libaddressinput),");
        out.println("# " + SOURCE + "/<CC>, retrieved " + retrieved + ".");
        out.println("# Data licence: CC BY 4.0, https://creativecommons.org/licenses/by/4.0/");
        out.println("#");
        out.println("# zip       the source's regex, kept so that a regeneration diff reads");
        out.println("# pattern   that regex over the compact form: separators dropped, an");
        out.println("#           optional prefix lifted into strip. Every backslash is doubled");
        out.println("#           here because the reader turns \\x into x.");
        out.println("# masks     how the code is written, from the examples: 9 digit, A letter");
        out.println("# examples  the source's examples, as written");
        out.println("# strip     prefixes compact() removes when the pattern does not want them");
        out.println("# name      the country; kind, the source's word for the code when it is");
        out.println("#           not simply postal");

        int withZip = 0;
        int withMasks = 0;
        List<String> fallbackNames = new ArrayList<>();
        for (Region region : regions.values()) {
            if (region.zip() == null) {
                continue;
            }
            withZip++;
            List<String> strip = new ArrayList<>();
            if (region.postprefix() != null) {
                addIfNew(strip, region.postprefix().replace("-", "").replace(" ", ""));
            }
            String pattern = compactPattern(region, strip);
            addIfNew(strip, region.key());
            Pattern compiled;
            try {
                compiled = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                fail(region, "pattern does not compile: " + pattern + " (" + e.getDescription() + ")");
                continue;
            }
            List<String> examples = List.of(region.zipex().split(","));
            List<String> masks = masks(region, examples);
            check(region, compiled, strip, examples, masks);

            String name = displayName(region);
            if (name == null) {
                name = titleCase(region.name());
                fallbackNames.add(region.key() + "=" + name);
            }
            if (!masks.isEmpty()) {
                withMasks++;
            }
            StringBuilder line = new StringBuilder(region.key());
            property(line, "zip", region.zip());
            property(line, "pattern", pattern);
            property(line, "masks", String.join(",", masks));
            property(line, "examples", String.join(",", examples));
            property(line, "strip", String.join(",", strip));
            property(line, "name", name);
            if (region.kind() != null && !region.kind().equals("postal")) {
                property(line, "kind", region.kind());
            }
            out.println(line);
        }
        if (!problems.isEmpty()) {
            throw new IllegalStateException(problems.size()
                    + " region(s) failed their checks: " + problems);
        }
        run.warn("regions: " + regions.size() + ", with a postal code: " + withZip
                + ", with masks: " + withMasks);
        if (!fallbackNames.isEmpty()) {
            run.warn("names the JDK does not know, taken from the source: "
                    + fallbackNames);
        }
    }

    // ------------------------------------------------------------------
    // the regex, over the compact form

    /**
     * The source's regex with its separators removed, so that it describes
     * the compact form. An optional literal prefix at the head is lifted out
     * into {@code strip} rather than kept optional, or the code would have two
     * compact forms.
     */
    private static String compactPattern(Region region, List<String> strip) {
        String z = region.zip()
                .replace("(?:^|\\b)", "")
                .replace("(?:$|\\b)", "");
        if (z.startsWith("^")) {
            z = z.substring(1);
        }
        if (z.endsWith("$")) {
            z = z.substring(0, z.length() - 1);
        }
        Matcher liftable = LIFTABLE.matcher(z);
        if (liftable.find()) {
            addIfNew(strip, liftable.group(1));
            z = z.substring(liftable.end());
        }

        StringBuilder out = new StringBuilder(z.length());
        int i = 0;
        while (i < z.length()) {
            char c = z.charAt(i);
            if (c == '\\' && i + 1 < z.length()) {
                char next = z.charAt(i + 1);
                if (next == ' ' || next == '-') {
                    i = skipOptional(z, i + 2);
                } else {
                    out.append(c).append(next);
                    i += 2;
                }
            } else if (c == '[') {
                int end = classEnd(z, i);
                String body = z.substring(i + 1, end);
                String bare = body.replace("\\", "");
                if (!bare.isEmpty() && bare.chars().allMatch(ch -> ch == ' ' || ch == '-')) {
                    i = skipOptional(z, end + 1);
                } else {
                    if (body.contains(" ") || body.contains("\\-")
                            || body.startsWith("-") || body.endsWith("-")) {
                        fail(region, "a class mixes a separator with other characters: [" + body + "]");
                    }
                    out.append(z, i, end + 1);
                    i = end + 1;
                }
            } else if (c == ' ' || c == '-') {
                i = skipOptional(z, i + 1);
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /** The index after a {@code ?} at {@code i}, or {@code i} when there is none. */
    private static int skipOptional(String z, int i) {
        return i < z.length() && z.charAt(i) == '?' ? i + 1 : i;
    }

    /** The index of the {@code ]} closing the class that opens at {@code open}. */
    private static int classEnd(String z, int open) {
        int i = open + 1;
        while (i < z.length()) {
            char c = z.charAt(i);
            if (c == '\\') {
                i += 2;
            } else if (c == ']') {
                return i;
            } else {
                i++;
            }
        }
        throw new IllegalArgumentException("unterminated class in " + z);
    }

    // ------------------------------------------------------------------
    // the masks, from the examples

    private static List<String> masks(Region region, List<String> examples) {
        Set<String> masks = new LinkedHashSet<>();
        for (String example : examples) {
            if (!example.equals(example.toUpperCase(Locale.ROOT))) {
                fail(region, "an example is not upper case: " + example);
            }
            StringBuilder mask = new StringBuilder(example.length());
            boolean literal = false;
            for (int i = 0; i < example.length(); i++) {
                char c = example.charAt(i);
                if (c >= '0' && c <= '9') {
                    mask.append('9');
                } else if (c >= 'A' && c <= 'Z') {
                    mask.append('A');
                } else if (c == ' ' || c == '-') {
                    mask.append(c);
                    literal = true;
                } else {
                    fail(region, "an example carries a separator compact() keeps: " + example);
                }
            }
            if (literal) {
                masks.add(mask.toString());
            }
        }
        return new ArrayList<>(masks);
    }

    // ------------------------------------------------------------------
    // what the library will do with all of this, run here first

    /** {@code PostalCode.compact}, as the class implements it. */
    private static String compact(String raw, Pattern pattern, List<String> strip) {
        String n = raw.toUpperCase(Locale.ROOT).replace(" ", "").replace("-", "");
        for (String prefix : strip) {
            if (n.length() > prefix.length() && n.startsWith(prefix)
                    && !pattern.matcher(n).matches()
                    && pattern.matcher(n.substring(prefix.length())).matches()) {
                return n.substring(prefix.length());
            }
        }
        return n;
    }

    private static void check(Region region, Pattern pattern, List<String> strip,
                              List<String> examples, List<String> masks) {
        for (String example : examples) {
            String compact = compact(example, pattern, strip);
            if (!pattern.matcher(compact).matches()) {
                fail(region, "example " + example + " compacts to " + compact
                        + ", which " + pattern.pattern() + " refuses");
            }
            // the mask that fits by shape, or failing that by length: Mask.apply
            String written = null;
            for (String mask : masks) {
                if (fits(mask, compact)) {
                    written = fill(mask, compact);
                    break;
                }
            }
            for (String mask : masks) {
                if (written == null && slots(mask) == compact.length()) {
                    written = fill(mask, compact);
                }
            }
            if (written == null ? !example.equals(compact) : !written.equals(example)) {
                fail(region, "example " + example + " formats as " + (written == null ? compact : written));
            }
            for (String prefix : strip) {
                for (String glue : new String[] {"-", " ", ""}) {
                    String prefixed = compact(prefix + glue + example, pattern, strip);
                    if (!prefixed.equals(compact)) {
                        fail(region, "prefix " + prefix + " is not stripped from "
                                + prefix + glue + example + ": " + prefixed);
                    }
                }
            }
        }
    }

    private static int slots(String mask) {
        int slots = 0;
        for (int i = 0; i < mask.length(); i++) {
            if (mask.charAt(i) == '9' || mask.charAt(i) == 'A') {
                slots++;
            }
        }
        return slots;
    }

    private static boolean fits(String mask, String compact) {
        if (slots(mask) != compact.length()) {
            return false;
        }
        int at = 0;
        for (int i = 0; i < mask.length(); i++) {
            char m = mask.charAt(i);
            if (m == '9' || m == 'A') {
                char c = compact.charAt(at++);
                boolean ok = m == '9' ? c >= '0' && c <= '9' : c >= 'A' && c <= 'Z';
                if (!ok) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String fill(String mask, String compact) {
        StringBuilder out = new StringBuilder(mask.length());
        int at = 0;
        for (int i = 0; i < mask.length(); i++) {
            char m = mask.charAt(i);
            out.append(m == '9' || m == 'A' ? compact.charAt(at++) : m);
        }
        return out.toString();
    }

    // ------------------------------------------------------------------
    // names, output, helpers

    /** The JDK's English name for the region, or null when it has none. */
    private static String displayName(Region region) {
        String name = new Locale.Builder().setRegion(region.key()).build()
                .getDisplayCountry(Locale.ENGLISH);
        return name.isEmpty() || name.equals(region.key()) ? null : name;
    }

    private static String titleCase(String name) {
        StringBuilder out = new StringBuilder(name.length());
        boolean start = true;
        for (char c : name.toCharArray()) {
            out.append(start ? Character.toUpperCase(c) : Character.toLowerCase(c));
            start = !Character.isLetter(c);
        }
        return out.toString();
    }

    private static void property(StringBuilder line, String key, String value) {
        if (value != null && !value.isEmpty()) {
            line.append(' ').append(key).append("=\"")
                    .append(value.replace("\\", "\\\\").replace("\"", "\\\""))
                    .append('"');
        }
    }

    private static void addIfNew(List<String> list, String value) {
        if (!value.isEmpty() && !list.contains(value)) {
            list.add(value);
        }
    }

    private static String first(Pattern field, String json) {
        Matcher m = field.matcher(json);
        return m.find() ? unescapeJson(m.group(1)) : null;
    }

    private static String unescapeJson(String s) {
        if (s.indexOf('\\') < 0) {
            return s;
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                out.append(c);
                continue;
            }
            char next = s.charAt(++i);
            switch (next) {
                case 'n' -> out.append('\n');
                case 't' -> out.append('\t');
                case 'u' -> {
                    out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> out.append(next);   // \\ \" \/ and anything else stand for themselves
            }
        }
        return out.toString();
    }

    private static void fail(Region region, String why) {
        problems.add(region.key() + ": " + why);
    }
}
