import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the cn-loc.dat registry consumed by the CnRic class.
 *
 * <p>Source: the eight Chinese Wikipedia pages listing the administrative
 * division codes, one per numbering region. Pass the raw wikitext of each:</p>
 *
 * <pre>
 *   for i in 1 2 3 4 5 6 7 8; do
 *     curl -L -o "region$i.wiki" \
 *       "https://zh.wikipedia.org/w/index.php?title=%E4%B8%AD%E5%8D%8E%E4%BA%BA%E6%B0%91%E5%85%B1%E5%92%8C%E5%9B%BD%E8%A1%8C%E6%94%BF%E5%8C%BA%E5%88%92%E4%BB%A3%E7%A0%81_($i%E5%8C%BA)&amp;action=raw"
 *   done
 *   java tools/GenerateCnLocDat.java region*.wiki \
 *       &gt; stdnum-apac/src/main/resources/io/github/jefersonsantos06/stdnum/apac/cn-loc.dat
 * </pre>
 *
 * <p>A county that existed only for a stretch of years is written with that
 * stretch in front of it, as {@code [start-end]name}, so a number can be read
 * against the year the holder was born.</p>
 */
public final class GenerateCnLocDat {

    private GenerateCnLocDat() {
    }

    private static final Pattern PROVINCE =
            Pattern.compile("^== *(?<province>.*?) +\\((?<prefix>[0-9]+)\\) +==.*");
    private static final Pattern ENTRY = Pattern.compile(
            "^\\| *(?<number>[0-9]{6}) *"
                    + "\\|\\| *(?<activation>.*?) *"
                    + "\\|\\| *(?<revocation>.*?) *"
                    + "\\|\\| *(?<county>.*?) *"
                    + "\\|\\| *(?<code>.*)");
    private static final Pattern LINK = Pattern.compile("\\[\\[([^]|]*\\|)?([^]|]+)]]");
    /** A county named as current from a year on. */
    private static final Pattern SINCE = Pattern.compile("(?<county>.*) +\\((?<year>[0-9]{4})年至今\\) *");
    /** A county named as having existed up to a year. */
    private static final Pattern UNTIL = Pattern.compile("(?<county>.*) +\\((?<year>[0-9]{4})年前\\) *");
    /** A county abolished for a stretch and then restored. */
    private static final Pattern SUSPENDED =
            Pattern.compile("(?<county>.*) +\\((?<years>[0-9]{4}-[0-9]{4})年曾撤销\\) *");
    /** A county that existed between two years. */
    private static final Pattern BETWEEN =
            Pattern.compile("(?<county>.*) +\\((?<start>[0-9]{4})年?-(?<end>[0-9]{4})年\\) *");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("usage: java GenerateCnLocDat.java <region.wiki>...");
            System.exit(2);
        }
        Map<String, String> provinces = new TreeMap<>();
        // province prefix -> the four digits under it -> the counties named
        Map<String, Map<String, TreeSet<String>>> counties = new TreeMap<>();
        for (String arg : args) {
            parse(Files.readString(Path.of(arg), StandardCharsets.UTF_8), provinces, counties);
        }
        if (provinces.isEmpty()) {
            System.err.println("no provinces found: the page layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# Chinese administrative division codes: the province, and the county");
        out.println("# within it that each six-digit code names.");
        out.println("# Generated from the Chinese Wikipedia pages listing the codes region by");
        out.println("# region, which mirror the National Bureau of Statistics list.");
        for (Map.Entry<String, String> province : provinces.entrySet()) {
            out.println(province.getKey() + " province=\"" + province.getValue() + "\"");
            for (Map.Entry<String, TreeSet<String>> county
                    : counties.getOrDefault(province.getKey(), new TreeMap<>()).entrySet()) {
                out.println("  " + county.getKey()
                        + " county=\"" + String.join(",", county.getValue()) + "\"");
            }
        }
    }

    private static void parse(String page, Map<String, String> provinces,
                              Map<String, Map<String, TreeSet<String>>> counties) {
        String prefix = null;
        for (String raw : page.split("\r?\n")) {
            String line = clean(raw);
            Matcher province = PROVINCE.matcher(line);
            if (province.matches()) {
                prefix = province.group("prefix");
                provinces.put(prefix, province.group("province"));
                continue;
            }
            Matcher entry = ENTRY.matcher(line);
            if (prefix == null || !entry.matches()) {
                continue;
            }
            String number = entry.group("number");
            if (!number.startsWith(prefix)) {
                continue;
            }
            String activation = year(entry.group("activation"));
            String revocation = year(entry.group("revocation"));
            for (String county : parseCounty(entry.group("county"), activation, revocation)) {
                counties.computeIfAbsent(prefix, k -> new TreeMap<>())
                        .computeIfAbsent(number.substring(2), k -> new TreeSet<>())
                        .add(county.strip());
            }
        }
    }

    /** The counties one cell names, each with the years it applied to. */
    private static List<String> parseCounty(String cell, String activation, String revocation) {
        List<String> out = new ArrayList<>();
        for (String value : cell.split("<br>")) {
            Matcher since = SINCE.matcher(value);
            if (since.matches()) {
                out.add("[" + since.group("year") + "-" + revocation + "]" + since.group("county"));
                continue;
            }
            Matcher until = UNTIL.matcher(value);
            if (until.matches()) {
                out.add("[" + activation + "-" + (Integer.parseInt(until.group("year")) - 1) + "]"
                        + until.group("county"));
                continue;
            }
            Matcher suspended = SUSPENDED.matcher(value);
            if (suspended.matches()) {
                out.add(bounded(suspended.group("county"), activation, revocation));
                continue;
            }
            Matcher between = BETWEEN.matcher(value);
            if (between.matches()) {
                out.add("[" + between.group("start") + "-"
                        + (Integer.parseInt(between.group("end")) - 1) + "]"
                        + between.group("county"));
                continue;
            }
            out.add(bounded(value, activation, revocation));
        }
        return out;
    }

    /** A county name, prefixed with its years when either bound is known. */
    private static String bounded(String county, String activation, String revocation) {
        return activation.isEmpty() && revocation.isEmpty()
                ? county
                : "[" + activation + "-" + revocation + "]" + county;
    }

    /** A cell as a year, or empty when it names none. */
    private static String year(String cell) {
        String text = cell.strip();
        return text.matches("[0-9]+") ? Integer.toString(Integer.parseInt(text)) : "";
    }

    /**
     * A line with its wiki links reduced to their text and its fullwidth
     * brackets written as ASCII ones, which is how the patterns read them.
     */
    private static String clean(String line) {
        String text = line.replace("（", " (").replace("）", ") ");
        return LINK.matcher(text).replaceAll("$2");
    }
}
