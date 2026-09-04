import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the at-fa.dat registry consumed by the AtTin class.
 *
 * <p>Source: the Finanzamtsnummern table of the German Wikipedia article on
 * the Abgabenkontonummer, which is where the numbers are set out. Each row
 * gives the two-digit office number, the office and its Bundesland.</p>
 *
 */
public final class GenerateAtFaDat implements Source {

    @Override
    public String id() {
        return "at-fa";
    }

    @Override
    public String title() {
        return "Regenerate at-fa.dat, the Austrian tax offices";
    }

    @Override
    public String output() {
        return "stdnum4j-eu/src/main/resources/io/github/jefersonsantos06/stdnum/eu/at-fa.dat";
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        return List.of(new Download(
                "https://de.wikipedia.org/w/index.php?title=Abgabenkontonummer&action=raw",
                "abgabenkontonummer.wiki"));
    }

    /** The table cells, once the row markers have been made unambiguous. */
    private static final Pattern ROW = Pattern.compile(
            "^\\|\\s*(?<number>[0-9]{2})\\s*\\\\\\\\\\s*(?<office>[^\\\\]*?)"
                    + "\\s*\\\\\\\\\\s*(?<region>[^\\\\]*?)\\s*(\\\\\\\\.*)?$");
    private static final Pattern TAG = Pattern.compile("<[^>]*>");
    private static final Pattern LINK = Pattern.compile("\\[\\[([^]|]*\\|)?([^]|]+)]]");

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        String page = Files.readString(run.file("abgabenkontonummer.wiki"), StandardCharsets.UTF_8);
        int at = page.indexOf("|+ Finanzamtsnummern");
        if (at < 0) {
            throw new IllegalStateException("no table of office numbers: the article layout has changed");
        }

        Map<String, String[]> offices = new TreeMap<>();
        for (String raw : page.substring(at).split("\r?\n")) {
            String line = clean(raw.strip());
            if (line.startsWith("|}")) {
                break;  // the end of the table
            }
            if (!line.contains("||")) {
                continue;
            }
            Matcher row = ROW.matcher(line.replace("||", "\\\\"));
            if (row.matches() && !row.group("office").isEmpty()) {
                // the table writes "n. a." where an office belongs to no Land
                String region = row.group("region").replace(" ", "").equals("n.a.")
                        ? "" : row.group("region");
                offices.putIfAbsent(row.group("number"),
                        new String[] {row.group("office"), region});
            }
        }
        if (offices.isEmpty()) {
            throw new IllegalStateException("no office rows found: the article layout has changed");
        }

        out.println("# Austrian tax office numbers: the two digits an Abgabenkontonummer opens");
        out.println("# with, and the office and Bundesland they name.");
        out.println("# Generated from the Finanzamtsnummern table of");
        out.println("# https://de.wikipedia.org/wiki/Abgabenkontonummer");
        offices.forEach((number, office) -> {
            StringBuilder sb = new StringBuilder(number)
                    .append(" office=\"").append(office[0]).append('"');
            if (!office[1].isEmpty()) {
                sb.append(" region=\"").append(office[1]).append('"');
            }
            out.println(sb);
        });
    }

    /** A line as plain text: no markup, links reduced to what they show. */
    private static String clean(String line) {
        String text = TAG.matcher(line).replaceAll("");
        text = LINK.matcher(text).replaceAll("$2");
        return text.replace("&nbsp;", " ").replace("''", "").replace("\"", "")
                .replaceAll("[ \\t]+", " ").strip();
    }
}
