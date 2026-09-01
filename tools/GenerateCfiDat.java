import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * <p>Usage:</p>
 * <pre>
 *   curl -L -o cfi.xlsx &lt;the xlsx linked from the page above&gt;
 *   javac -d tools/classes tools/Xlsx.java tools/GenerateCfiDat.java
 *   java -cp tools/classes GenerateCfiDat cfi.xlsx \
 *       &gt; stdnum-international/src/main/resources/io/github/jefersonsantos06/stdnum/international/cfi.dat
 * </pre>
 */
public final class GenerateCfiDat {

    private GenerateCfiDat() {
    }

    /** One attribute position: what it means, and the letters it takes. */
    private record Attribute(String name, Map<String, String> values) {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: java GenerateCfiDat.java <cfi.xlsx>");
            System.exit(2);
        }
        Path file = Path.of(args[0]);
        List<String> groups = new ArrayList<>();
        for (String sheet : Xlsx.sheetNames(file)) {
            if (sheet.length() == 6 && sheet.endsWith("XXXX")) {
                groups.add(sheet);
            }
        }
        groups.sort(String::compareTo);
        Map<String, String> categories = categories(Xlsx.rows(file, "Categories"));
        if (categories.isEmpty() || groups.isEmpty()) {
            System.err.println("no categories or groups: the workbook layout has changed");
            System.exit(1);
        }

        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
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
