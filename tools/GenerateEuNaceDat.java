import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the eu-nace20.dat and eu-nace21.dat registries consumed by the
 * EuNace class.
 *
 * <p>Source: the NACE codelist Eurostat publishes through its SDMX API, which
 * is the classification itself rather than a rendering of it. Rev. 2 is
 * {@code NACE_R2} and Rev. 2.1 is {@code NACE_R2_1}.</p>
 *
 * <p>Eurostat writes a code with its section letter in front — {@code A0111}
 * for class 01.1.1 — and lists a great many aggregates besides
 * ({@code A-T}, {@code A01_A02}, {@code TOTAL}). Only the four real levels
 * are emitted, nested the way a code is read: the section, the division
 * within it, then one digit of group and one of class.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o nace21.xml \
 *     https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT/NACE_R2_1
 *   java tools/GenerateEuNaceDat.java nace21.xml "Rev. 2.1" \
 *       &gt; stdnum-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/eu-nace21.dat
 * </pre>
 */
public final class GenerateEuNaceDat {

    private GenerateEuNaceDat() {
    }

    private static final Pattern CODE =
            Pattern.compile("<s:Code\\b[^>]*id=\"([^\"]*)\"[^>]*>(.*?)</s:Code>", Pattern.DOTALL);
    private static final Pattern ENGLISH =
            Pattern.compile("<c:Name xml:lang=\"en\">(.*?)</c:Name>", Pattern.DOTALL);
    /**
     * A section, or a division, group or class under one. The letters run
     * past U: Rev. 2.1 added a section V.
     */
    private static final Pattern REAL = Pattern.compile("([A-Z])([0-9]{2})?([0-9])?([0-9])?");

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("usage: java GenerateEuNaceDat.java <codelist.xml> <revision>");
            System.exit(2);
        }
        String xml = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);

        Map<String, String> labels = new TreeMap<>();
        Matcher codes = CODE.matcher(xml);
        while (codes.find()) {
            Matcher real = REAL.matcher(codes.group(1));
            if (!real.matches()) {
                continue;  // an aggregate rather than a heading of the classification
            }
            Matcher english = ENGLISH.matcher(codes.group(2));
            if (english.find()) {
                labels.put(codes.group(1), unescape(english.group(1)));
            }
        }
        if (labels.isEmpty()) {
            System.err.println("no headings found: the codelist layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# NACE " + args[1] + ": the sections, divisions, groups and classes of the");
        out.println("# statistical classification of economic activities.");
        out.println("# Generated from the codelist Eurostat publishes through its SDMX API at");
        out.println("# https://ec.europa.eu/eurostat/api/dissemination/sdmx/2.1/codelist/ESTAT/");
        for (Map.Entry<String, String> heading : labels.entrySet()) {
            Matcher m = REAL.matcher(heading.getKey());
            if (!m.matches()) {
                continue;
            }
            String section = m.group(1);
            String division = m.group(2);
            String group = m.group(3);
            String klass = m.group(4);
            String label = " label=\"" + heading.getValue().replace("\"", "") + "\"";
            if (division == null) {
                out.println(section + label);
            } else if (group == null) {
                // a division carries the section it belongs to, which its own
                // digits do not say
                out.println(division + " section=\"" + section + "\"" + label);
            } else if (klass == null) {
                out.println(" " + group + label);
            } else {
                out.println("  " + klass + label);
            }
        }
    }

    private static String unescape(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&apos;", "'").replace("&amp;", "&").replaceAll("\\s+", " ").strip();
    }
}
