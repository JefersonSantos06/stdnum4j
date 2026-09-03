import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates the cfi.dat registry consumed by the Cfi class.
 *
 * <p>Source: the CFI code list the SIX group publishes as a spreadsheet, one
 * sheet per group of instruments plus a Categories sheet listing the
 * categories. Its download link is on
 * https://www.six-group.com/en/products-services/financial-information/data-standards.html
 * and matches {@code .*&#47;cfi/.*xlsx}.</p>
 *
 * <p>The output nests a CFI code the way it is read: the category, the group
 * within it, then the four attribute positions. An attribute position whose
 * only value is X takes any letter and carries nothing, and is written as a
 * bare {@code A-Z}.</p>
 *
 */
public final class GenerateCfiDat implements Source {

    @Override
    public String id() {
        return "cfi";
    }

    @Override
    public String title() {
        return "Regenerate cfi.dat, the ISO 10962 classification";
    }

    @Override
    public String output() {
        return "stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/cfi.dat";
    }

    private static final String STANDARDS = "https://www.six-group.com/en/"
            + "products-services/financial-information/data-standards.html";
    private static final Pattern SPREADSHEET =
            Pattern.compile("href=\"([^\"]*/cfi/[^\"]*\\.xlsx)\"");

    @Override
    public List<Download> seeds() {
        // the file is published under a dated name, so it has to be found
        return List.of(new Download(STANDARDS, "six.html"));
    }

    @Override
    public List<Download> downloads(Map<String, String> seeds) {
        Matcher link = SPREADSHEET.matcher(seeds.get("six.html"));
        List<String> found = new ArrayList<>();
        while (link.find()) {
            if (!found.contains(link.group(1))) {
                found.add(link.group(1));
            }
        }
        if (found.size() != 1) {
            throw new IllegalStateException("the data standards page links "
                    + found.size() + " CFI spreadsheets, expected one: " + found);
        }
        return List.of(new Download(found.get(0), "cfi.xlsx"));
    }

    /** One attribute position: what it means, and the letters it takes. */
    private record Attribute(String name, Map<String, String> values) {
    }

    @Override
    public void generate(Run run, PrintStream out) throws Exception {
        Path file = run.file("cfi.xlsx");
        List<String> groups = new ArrayList<>();
        for (String sheet : Xlsx.sheetNames(file)) {
            if (sheet.length() == 6 && sheet.endsWith("XXXX")) {
                groups.add(sheet);
            }
        }
        groups.sort(String::compareTo);
        Map<String, String> categories = categories(Xlsx.rows(file, "Categories"));
        if (categories.isEmpty() || groups.isEmpty()) {
            throw new IllegalStateException("no categories or groups: the workbook layout has changed");
        }

        out.println("# ISO 10962 CFI codes: the category, the group within it and the four");
        out.println("# attribute positions, nested the way a code is read.");
        out.println("# Generated from the CFI code list published by the SIX group at");
        out.println("# https://www.six-group.com/en/products-services/financial-information/"
                + "data-standards.html");
        for (Map.Entry<String, String> category : categories.entrySet()) {
            out.println(category.getKey() + " category=\"" + category.getValue() + "\"");
            for (String group : groups) {
                if (!group.startsWith(category.getKey())) {
                    continue;
                }
                List<List<String>> rows = Xlsx.rows(file, group);
                out.println(" " + group.charAt(1)
                        + " group=\"" + normalise(cell(rows.get(0), 0)) + "\"");
                printAttributes(out, attributes(rows), 0);
            }
        }
    }

    /** The single-letter categories, in code order. */
    private static Map<String, String> categories(List<List<String>> rows) {
        Map<String, String> categories = new TreeMap<>();
        for (List<String> row : rows) {
            String code = cell(row, 0);
            String name = cell(row, 1);
            if (code.length() == 1 && !name.isEmpty()) {
                categories.put(code, name);
            }
        }
        return categories;
    }

    /**
     * The four attribute positions of a group, in the order the sheet gives
     * them: a row naming an attribute opens one, and the rows under it with a
     * letter in the second column are its values.
     */
    private static List<Attribute> attributes(List<List<String>> rows) {
        List<Attribute> attributes = new ArrayList<>();
        Map<String, String> values = null;
        for (List<String> row : rows) {
            String first = cell(row, 0);
            String letter = cell(row, 1);
            String text = cell(row, 2);
            if (!first.isEmpty() && letter.isEmpty() && !text.isEmpty()) {
                values = new TreeMap<>();
                attributes.add(new Attribute(normalise(text), values));
            } else if (values != null && !letter.isEmpty() && !text.isEmpty()) {
                values.put(letter, normalise(text));
            }
        }
        return attributes;
    }

    /** One attribute position and everything nested under it. */
    private static void printAttributes(PrintStream out, List<Attribute> attributes, int index) {
        if (index >= attributes.size()) {
            return;
        }
        Attribute attribute = attributes.get(index);
        String indent = " ".repeat(index + 2);
        if (attribute.values().size() == 1 && attribute.values().containsKey("X")) {
            // the position is undefined for this group: any letter, no meaning
            out.println(indent + "A-Z");
        } else {
            for (Map.Entry<String, String> value : attribute.values().entrySet()) {
                out.println(indent + value.getKey() + " v=\"" + value.getValue() + "\"");
            }
            out.println(indent + "A-Z a=\"" + attribute.name() + "\"");
        }
        if (index < 3) {
            printAttributes(out, attributes, index + 1);
        }
    }

    /**
     * A name as the registry writes it, without the parenthetical gloss the
     * spreadsheet appends for the reader.
     */
    private static String normalise(String value) {
        return value.replaceAll("(?m) *[(\\[\n].*", "").strip().replace("\"", "");
    }

    private static String cell(List<String> row, int column) {
        return column < row.size() ? row.get(column).strip() : "";
    }
}
