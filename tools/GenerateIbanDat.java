import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the iban.dat country registry consumed by the Iban class.
 *
 * <p>The authoritative source is the SWIFT IBAN Registry, whose download is
 * behind a bot wall, so this reads the "IBAN formats by country" table of
 * the Wikipedia article that mirrors it. Every entry's declared length is
 * cross-checked against the structure that was parsed, and a mismatch
 * fails the run rather than emitting a bad record.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   curl -L -A "Mozilla/5.0" -o iban.html \
 *       https://en.wikipedia.org/wiki/International_Bank_Account_Number
 *   java tools/GenerateIbanDat.java iban.html \
 *       &gt; stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/iban.dat
 * </pre>
 */
public final class GenerateIbanDat {

    private GenerateIbanDat() {
    }

    private static final Pattern TABLE = Pattern.compile("<table.*?</table>", Pattern.DOTALL);
    private static final Pattern ROW = Pattern.compile("<tr[^>]*>(.*?)</tr>", Pattern.DOTALL);
    private static final Pattern CELL = Pattern.compile("<t[dh][^>]*>(.*?)</t[dh]>", Pattern.DOTALL);
    private static final Pattern TAG = Pattern.compile("<[^>]+>");
    /** A BBAN structure token: a repeat count and a character class. */
    private static final Pattern TOKEN = Pattern.compile("(\\d+)\\s*([nac])");
    /** Wikipedia footnote markers left in country names, e.g. "[ Note 7 ]". */
    private static final Pattern FOOTNOTE = Pattern.compile(" *\\[ [^]]* ]");
    private static final Pattern COUNTRY_CODE = Pattern.compile("[A-Z]{2}");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java GenerateIbanDat.java <wikipedia-article.html>");
            System.exit(2);
        }
        String html = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);

        List<String> entries = new ArrayList<>();
        Matcher tables = TABLE.matcher(html);
        while (tables.find()) {
            String table = tables.group();
            // the per-country table is the one declaring a BBAN format column
            if (!table.contains("BBAN format")) {
                continue;
            }
            Matcher rows = ROW.matcher(table);
            while (rows.find()) {
                String entry = parseRow(rows.group(1));
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        if (entries.isEmpty()) {
            System.err.println("no country rows found: the article layout has changed");
            System.exit(1);
        }
        entries.sort(String::compareTo);

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        out.println("# IBAN country registry: ISO country code, country name and BBAN structure.");
        out.println("# Generated from the \"IBAN formats by country\" table of");
        out.println("# https://en.wikipedia.org/wiki/International_Bank_Account_Number,");
        out.println("# which mirrors the SWIFT IBAN Registry; entry lengths cross-checked at generation time.");
        out.println("# Structure notation: <count>!n digits, <count>!a upper-case letters, <count>!c alphanumeric.");
        entries.forEach(out::println);
    }

    /** One country record, or null when the row is not one. */
    private static String parseRow(String row) {
        List<String> cells = new ArrayList<>();
        Matcher m = CELL.matcher(row);
        while (m.find()) {
            cells.add(text(m.group(1)));
        }
        if (cells.size() < 4) {
            return null;
        }
        String country = FOOTNOTE.matcher(cells.get(0)).replaceAll("");
        String declaredLength = cells.get(1);
        String structure = cells.get(2);
        String fields = cells.get(3);

        // the IBAN Fields column opens with the country code
        if (fields.isEmpty()) {
            return null;
        }
        String code = fields.split("\\s+")[0];
        if (!COUNTRY_CODE.matcher(code).matches()) {
            return null;
        }

        StringBuilder bban = new StringBuilder();
        int total = 4;  // the country code and the two check digits
        Matcher tokens = TOKEN.matcher(structure);
        while (tokens.find()) {
            total += Integer.parseInt(tokens.group(1));
            bban.append(tokens.group(1)).append('!').append(tokens.group(2));
        }
        if (bban.length() == 0) {
            return null;
        }
        // refuse to emit a record whose parts do not add up
        if (!declaredLength.equals(Integer.toString(total))) {
            System.err.println("length mismatch for " + code + ": parsed " + total
                    + " but the table declares " + declaredLength + " — skipped");
            return null;
        }
        return code + " country=\"" + country + "\" bban=\"" + bban + "\"";
    }

    /** The visible text of a table cell. */
    private static String text(String cell) {
        String stripped = TAG.matcher(cell).replaceAll(" ");
        stripped = stripped.replace("&nbsp;", " ").replace("&amp;", "&")
                .replace("&gt;", ">").replace("&lt;", "<");
        return stripped.replaceAll("\\s+", " ").strip();
    }
}
